package cn.jpush.im.android.helpers.sync;

import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/8/22.
 */

public class SyncReceiptRespHandler extends SyncRespBaseHandler {
    private static final String TAG = SyncReceiptRespHandler.class.getSimpleName();

    private static SyncReceiptRespHandler instance;
    private Map<String, ConversationMREntity> receivedConvMREntity = new LinkedHashMap<String, ConversationMREntity>();

    private SyncReceiptRespHandler() {
        super(null);//回执同步不需要另外的批量打包请求，这里requestPackager传null
    }

    public static synchronized SyncReceiptRespHandler getInstance() {
        if (null == instance) {
            instance = new SyncReceiptRespHandler();
        }
        return instance;
    }


    @Override
    int getTotalPage(IMProtocol protocol) {
        Receipt.SyncMsgReceiptResp resp = (Receipt.SyncMsgReceiptResp) protocol.getEntity();
        return null == resp ? 0 : resp.getTotalPages();
    }

    @Override
    int getPageNo(IMProtocol protocol) {
        Receipt.SyncMsgReceiptResp resp = (Receipt.SyncMsgReceiptResp) protocol.getEntity();
        return (null == resp || null == resp.getMrPage()) ? 0 : resp.getMrPage().getPageNo();
    }

    @Override
    long getSyncKey(IMProtocol protocol) {
        Receipt.SyncMsgReceiptResp resp = (Receipt.SyncMsgReceiptResp) protocol.getEntity();
        Logger.d(TAG, "[getSyncKey] , resp.getSyncKey() = " + resp.getSyncKey());
        if (0 != resp.getSyncKey()) {
            syncKey = resp.getSyncKey();
        }
        return syncKey;
    }

    @Override
    void sendSyncACK(long syncKey) {
        if (uid != IMConfigs.getUserID()) {
            Logger.ww(TAG, "current uid not match uid in protocol. do not send sync event ack back.");
            return;
        }

        //将syncKey本地化，同时将syncKey通过ACK回给后台
        Logger.ii(TAG, "send sync receipt ack back == syncReceiptKey is " + syncKey);
        IMConfigs.setSyncReceiptKey(IMConfigs.getUserID(), syncKey);
        RequestProcessor.imSyncReceiptACK(JMessage.mContext, syncKey, CommonUtils.getSeqID());
    }

    @Override
    void clearCache() {
        super.clearCache();
        receivedConvMREntity.clear();
    }

    @Override
    void startLocalize(BasicCallback callback) {
        super.startLocalize(callback);

        //先清掉计数器。
        finishedCounter.set(0);
        //清空缓存的会话SyncConvEntity.防止出现两次SyncCheck挤到一起时，前后的SyncConvEntity重复计算。
        receivedConvMREntity.clear();
        //将所有同步页按照其中的会话重新聚合。
        for (Object value : receivedPages.values()) {
            Receipt.SyncMsgReceiptResp resp = (Receipt.SyncMsgReceiptResp) value;
            if (0 != resp.getSyncKey()) {
                syncKey = resp.getSyncKey();
            }
            Receipt.MsgReceiptPage msgReceiptPage = resp.getMrPage();

            for (Receipt.ConversationMR convMR : msgReceiptPage.getConListList()) {
                String convId = convMR.getConId().toStringUtf8();
                List<Receipt.MsgReceiptMeta> metas = convMR.getMsgListList();
                ConversationMREntity cachedEntity = receivedConvMREntity.get(convId);
                if (null != cachedEntity) {
                    cachedEntity.addToMsgReceiptMetas(metas);
                } else {
                    ConversationMREntity convEntity = new ConversationMREntity(convId, metas, operationWatcher);
                    receivedConvMREntity.put(convId, convEntity);
                }
            }
        }

        //通知BaseHandler更新entitiesToLocalize这个变量（必须在下面单个SyncConv startLocalize之前调用）
        onEntitiesToLocalizeCntUpdated(receivedConvMREntity.size());

        for (ConversationMREntity conversationMREntity : receivedConvMREntity.values()) {
            conversationMREntity.startLocalize();
        }
    }

    @Override
    int getTotalEntityCount() {
        return receivedConvMREntity.size();
    }

    private class ConversationMREntity extends SyncRespEntity {

        private List<Receipt.MsgReceiptMeta> msgReceiptMetas = new ArrayList<Receipt.MsgReceiptMeta>();

        ConversationMREntity(String convId, List<Receipt.MsgReceiptMeta> msgReceiptMetas, Watcher operationWatcher) {
            super(convId, operationWatcher);
            this.msgReceiptMetas.addAll(msgReceiptMetas);
        }

        void addToMsgReceiptMetas(List<Receipt.MsgReceiptMeta> metas) {
            if (null != msgReceiptMetas) {
                msgReceiptMetas.addAll(metas);
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

            Logger.ii(TAG, "conv id = " + convId + " receipt metas size = " + msgReceiptMetas.size());
            final Map<Long, Receipt.MsgReceiptMeta> cachedMetas = new HashMap<Long, Receipt.MsgReceiptMeta>();
            for (Receipt.MsgReceiptMeta meta : msgReceiptMetas) {
                Receipt.MsgReceiptMeta cachedMeta = cachedMetas.get(meta.getMsgid());
                if (null == cachedMeta || meta.getMtime() > cachedMeta.getMtime()) {
                    //只有当缓存的meta信息为空，或者已缓存的meta的mtime小于当前meta的mtime时，将当前这个meta对象放入到map中去。
                    cachedMetas.put(meta.getMsgid(), meta);
                }
            }

            //通过conv_id来确定会话。
            String[] ids = convId.split("_");
            Long target = 0L;
            if (ids.length > 1) {//senderUid_targetUid
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
                UserIDHelper.getUsername(target, new UserIDHelper.GetUsernamesCallback() {
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
                            if (null != internalConv) {
                                internalConv.updateMessageUnreceiptCntInBatch(cachedMetas.values());
                            }
                        }
                        operationWatcher.update(code);
                    }
                });

            } else if (ids.length == 1) {//targetGid
                target = Long.parseLong(ids[0]);
                InternalConversation internalConv = ConversationManager.getInstance().getGroupConversation(target);
                if (null != internalConv) {
                    internalConv.updateMessageUnreceiptCntInBatch(cachedMetas.values());
                }
                operationWatcher.update(ErrorCode.NO_ERROR);
            }
        }
    }
}
