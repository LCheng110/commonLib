package cn.jpush.im.android.helpers.sync;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.pushcommon.proto.Jmconversation;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.tasks.GetChatMsgTaskMng;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;


/**
 * Created by xiongtc on 16/8/3.
 * <p>
 * 一次SyncConvResp可能包含多个SycnConvPage.而一个SycnConvPage中可能包含了多个SyncConv.
 * 只有当所有SyncConv的localize都处理完成之后，才表示一个resp page处理完成。 只有当所有resp page都处理
 * 完成后，才表示一次SyncConcResp 动作完成。之后再更新本地的SyncKey发送SyncConvACK给后台。
 * 具体流程图见jira: http://wiki.jpushoa.com/display/IM/SDK+-+MessageSync+-+Design
 * <p>
 * 如果其中有任何一个localize失败了，则认为此次SyncConvResp失败，此时不更新本地Synckey,不发送SyncConvACK给后台。
 */
public class SyncConvRespHandler extends SyncRespBaseHandler {
    private static final String TAG = "SyncConvRespHandler";

    private static SyncConvRespHandler instance;

    //缓存的SyncConv会话对象，用于多页之间相同会话的合并, key -- conv_id, Value -- SyncConv
    private Map<String, SyncConv> receivedSyncConvs = new LinkedHashMap<String, SyncConv>();

    private SyncConvRespHandler() {
        super(GetChatMsgTaskMng.getInstance());
    }

    public static synchronized SyncConvRespHandler getInstance() {
        if (null == instance) {
            instance = new SyncConvRespHandler();
        }
        return instance;
    }

    @Override
    protected int getTotalPage(IMProtocol protocol) {
        Jmconversation.SyncConversationResp resp = (Jmconversation.SyncConversationResp) protocol.getEntity();
        return resp.getTotalPages();
    }

    @Override
    protected int getPageNo(IMProtocol protocol) {
        Jmconversation.SyncConversationResp resp = (Jmconversation.SyncConversationResp) protocol.getEntity();
        return null == resp.getConPage() ? 0 : resp.getConPage().getPageNo();
    }

    @Override
    protected long getSyncKey(IMProtocol protocol) {
        Jmconversation.SyncConversationResp resp = (Jmconversation.SyncConversationResp) protocol.getEntity();
        if (0 != resp.getSyncKey()) {
            syncKey = resp.getSyncKey();
        }
        return syncKey;
    }

    @Override
    protected int getTotalEntityCount() {
        return receivedSyncConvs.size();
    }

    @Override
    public void clearCache() {
        super.clearCache();
        receivedSyncConvs.clear();
    }

    @Override
    protected void startLocalize(BasicCallback callback) {
        super.startLocalize(callback);
        //开始本地化处理前，先清一下GetChatMsgTaskMng的缓存
        requestPackager.clearCache();
        //先清掉计数器。
        finishedCounter.set(0);
        //清空缓存的会话SyncConvEntity.防止出现两次SyncCheck挤到一起时，前后的SyncConvEntity重复计算。
        receivedSyncConvs.clear();
        //将所有同步页按照其中的会话重新聚合。
        for (Object value : receivedPages.values()) {
            Jmconversation.SyncConversationResp resp = (Jmconversation.SyncConversationResp) value;
            if (0 != resp.getSyncKey()) {
                syncKey = resp.getSyncKey();
            }
            List<Jmconversation.Conversation> convList = resp.getConPage().getConListList();
            for (Jmconversation.Conversation conv : convList) {
                String convId = conv.getId().toStringUtf8();
                List<Jmconversation.ConversationMsg> newList = conv.getNewListList();
                List<Jmconversation.ConversationMsg> oldList = conv.getOldListList();
                SyncConv entity = receivedSyncConvs.get(convId);
                if (null != entity) {
                    entity.addAllToNewList(newList);
                    entity.addAllToOldList(oldList);
                } else {
                    SyncConv convEntity = new SyncConv(convId, newList, oldList, operationWatcher);
                    receivedSyncConvs.put(convId, convEntity);
                }
            }
        }

        //通知BaseHandler更新entitiesToLocalize这个变量（必须在下面单个SyncConv startLocalize之前调用）
        onEntitiesToLocalizeCntUpdated(receivedSyncConvs.size());

        for (SyncConv sc : receivedSyncConvs.values()) {
            sc.startLocalize();
        }

    }

    /**
     * 消息同步中的会话实体类
     */
    private class SyncConv extends SyncRespEntity {

        private List<Jmconversation.ConversationMsg> newList;
        private List<Jmconversation.ConversationMsg> oldList;

        SyncConv(String convId, List<Jmconversation.ConversationMsg> newList, List<Jmconversation.ConversationMsg> oldList, Watcher operationWatcher) {
            super(convId, operationWatcher);
            this.newList = new ArrayList<Jmconversation.ConversationMsg>(newList);
            this.oldList = new ArrayList<Jmconversation.ConversationMsg>(oldList);
        }

        void addAllToNewList(List<Jmconversation.ConversationMsg> targetNewList) {
            if (null != targetNewList) {
                newList.addAll(targetNewList);
            }
        }

        void addAllToOldList(List<Jmconversation.ConversationMsg> targetOldList) {
            if (null != targetOldList) {
                oldList.addAll(targetOldList);
            }
        }

        @Override
        protected void startLocalize() {
            long myUid = IMConfigs.getUserID();
            if (0 == myUid || uid != myUid) {
                //如果uid是0，或者resp中uid和当前登陆uid不一致，直接返回。
                operationWatcher.update(ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN);
                return;
            }

            String[] ids = convId.split("_");
            Long target = 0L;
            //如果分割出来的parts长度大于1，说明是单聊会话
            if (ids.length > 1) {
                for (String id : ids) {
                    //两个id中和自己uid不等的一方为target
                    if (Long.parseLong(id) != myUid) {
                        target = Long.parseLong(id);
                    }
                }
                //如果两个id都是自己的uid，说明是自己发给自己的消息，target就是自己。
                if (0 == target) {
                    target = myUid;
                }
                final Long finalTarget = target;
                //通过target uid查找到对应username.
                UserIDHelper.getUsername(target, new UserIDHelper.GetUsernamesCallback(false) {
                    @Override
                    public void gotResult(int code, String msg, List<String> usernames) {
                        if (code == ErrorCode.ERROR_NO_SUCH_USER || usernames.isEmpty() || TextUtils.isEmpty(usernames.get(0))) {
                            //如果用户不存在，或者拿到的用户名是空，说明之前有过会话的用户此时可能已被删除了，则跳过这个会话的处理。
                            operationWatcher.update(CODE_NO_NEED_TO_LOCALIZE);
                            return;
                        }

                        if (ErrorCode.NO_ERROR == code && !usernames.isEmpty()) {
                            String username = usernames.get(0);
                            String appkey = UserIDHelper.getUserAppkeyFromLocal(finalTarget);
                            InternalConversation internalConv = ConversationManager.getInstance().getSingleConversation(username, appkey);
                            boolean convAlreadyExist = false;
                            if (null == internalConv) {
                                //这里创建会话时expectedLatestMsgDate这个参数默认填0,而不是填当前时间，是为了防止同步下来的消息无法更新这个会话的latestMsgDate，
                                //导致会话的latestMsgDate和消息的实际情况不相符。（因为当消息创建时，只有消息的latestMsgDate大于会话的latestMsgDate时，才会更新会话的latestMsgDate）
                                internalConv = ConversationManager.getInstance().createConversation(ConversationType.single, username, appkey, null, 0, false);
                            } else {
                                convAlreadyExist = true;
                            }
                            convLocalize(convId, internalConv, convAlreadyExist);
                        } else {
                            operationWatcher.update(code);
                        }
                    }
                });
            } else if (ids.length == 1) { //如果分割出来的parts长度等于1，说明只有一个gid，是群聊会话
                target = Long.parseLong(ids[0]);
                InternalConversation interalConv = ConversationManager.getInstance().getGroupConversation(target);
                boolean convAlreadyExist = false;
                if (null == interalConv) {
                    //这里创建会话时expectedLatestMsgDate 这个参数默认填0,而不是填当前时间，是为了防止同步下来的消息无法更新这个会话的latestMsgDate，
                    //导致会话的latestMsgDate和消息的实际情况不相符。（因为当消息创建时，只有消息的latestMsgDate大于会话的latestMsgDate时，才会更新会话的latestMsgDate）
                    interalConv = ConversationManager.getInstance().createConversation(ConversationType.group, ids[0], "", null, 0, false);
                } else {
                    convAlreadyExist = true;
                }
                convLocalize(convId, interalConv, convAlreadyExist);
            }
        }

        private void convLocalize(final String convId, final InternalConversation internalConversation, boolean convAlreadyExist) {
            Logger.ii(TAG, " conv id = " + convId +
                    " new list totalCount = " + newList.size() +
                    " old list totalCount = " + oldList.size());

            if (uid != IMConfigs.getUserID()) {
                Logger.ww(TAG, "current uid not match uid in protocol. abort this protocol.");
                operationWatcher.update(ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN);
                return;
            }

            Collection<Long> msgIdsSentByMyself = new ArrayList<Long>();
            final List<Long> newListIdWithCtime = new ArrayList<Long>();
            final List<Long> oldListIdWithCtime = new ArrayList<Long>();
            if (null != newList) {
                //遍历会话中new list里所有的message
                for (Jmconversation.ConversationMsg msg : newList) {
                    long msgId = msg.getMsgid();
                    Logger.d(TAG, " new msg msgid " + msgId + " new msg ctime " + msg.getCtime());
                    newListIdWithCtime.add(msgId);
                    if (uid == msg.getSender()) {
                        //如果这条消息的sender是我自己，则把这个msgid加入到msgIdsSentByMyself这个集合中去，之后请求msg content的时候一起发给后台，拿到消息的未回执数
                        msgIdsSentByMyself.add(msgId);
                    }
                }
            }
            if (null != oldList) {
                for (Jmconversation.ConversationMsg msg : oldList) {
                    long msgId = msg.getMsgid();
                    Logger.d(TAG, " old msg msgid " + msgId + " old msg ctime " + msg.getCtime());
                    oldListIdWithCtime.add(msgId);
                    if (uid == msg.getSender()) {
                        msgIdsSentByMyself.add(msgId);
                    }
                }
            }

            //newList与oldlist统一和在线收到的msg部分进行一次去重。去重原因见设计文档:http://wiki.jpushoa.com/display/IM/SDK-Android-Design
            internalConversation.removeDuplicatesWithOnlineList(newListIdWithCtime);
            internalConversation.removeDuplicatesWithOnlineList(oldListIdWithCtime);

            if (newListIdWithCtime.isEmpty() && oldListIdWithCtime.isEmpty() && !convAlreadyExist) {
                //如果去重后的newlist 以及oldlist都是空，而且，这个会话对象之前是不存在的，是在同步过程中才创建的。
                //这时要把这个创建的会话对象删掉，否则在用户上层看来会出现凭空多出一个会话对象的现象。
                //又或者是造成用户手动把会话删除调了，但是同步之后，这个会话又回来了。等很多非上层预期的现象。
                if (internalConversation.getType() == ConversationType.single) {
                    JMessageClient.deleteSingleConversation(internalConversation.getTargetId(), internalConversation.getTargetAppKey());
                } else {
                    JMessageClient.deleteGroupConversation(Long.parseLong(internalConversation.getTargetId()));
                }
                Logger.d(TAG, "newlist and oldlist both empty,delete the conv , and do callback.");
                operationWatcher.update(CODE_NO_NEED_TO_LOCALIZE);
                return;
            } else if (newListIdWithCtime.isEmpty() && oldListIdWithCtime.isEmpty()) {
                //如果去重后的newlist，和oldlist都是空，而这个会话对象之前已存在了，则直接跳过之后的逻辑，直接回调pageWatcher.
                Logger.d(TAG, "newlist and oldlist both empty,and do callback.");
                operationWatcher.update(CODE_NO_NEED_TO_LOCALIZE);
                return;
            }

            //获取会话中消息的msgContent
            requestPackager.prepareToRequest(uid, entitiesToLocalize.get(), convId,
                    new GetChatMsgTaskMng.GetMessageContentCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage, GetChatMsgTaskMng.ConvEntity entity) {
                            Logger.d(TAG, "getMessageContent finished responseCode = " + responseCode + " entity = " + entity);
                            operationWatcher.update(responseCode);
                        }
                    }, newListIdWithCtime, oldListIdWithCtime, msgIdsSentByMyself);
        }
    }

    @Override
    protected void sendSyncACK(long syncKey) {
        if (uid != IMConfigs.getUserID()) {
            Logger.ww(TAG, "current uid not match uid in protocol. do not send sync ack back.");
            return;
        }

        //将syncKey本地化，同时将syncKey通过ACK回给后台
        Logger.ii(TAG, "send sync conv ack back == synckey is " + syncKey);
        IMConfigs.setConvSyncKey(IMConfigs.getUserID(), syncKey);
        RequestProcessor.imSyncConvACK(JMessage.mContext, syncKey, CommonUtils.getSeqID());
    }
}
