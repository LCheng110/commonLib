package cn.jpush.im.android.helpers.eventsync.ReadReceiptEvents;

import com.google.gson.jpush.annotations.Expose;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.helpers.ResponseProcessor;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/8/29.
 */

public class ReadReceiptEvents extends Kind8BaseEvents {
    private static final String TAG = ReadReceiptEvents.class.getSimpleName();

    private Map<Long, ReadReceiptEntity> cachedReadReceipts = new HashMap<Long, ReadReceiptEntity>();

    public ReadReceiptEvents(List<Message.EventNotification> notifications) {
        super(notifications);
    }

    @Override
    public void merge() {
        for (Message.EventNotification eventNotification : notifications) {
            if (JCoreInterface.getUid() == eventNotification.getFromUid()) {
                //这里fromUid拿到的是请求设备的juid. 按照后台文档，接收端SDK设备juid如果和req_juid相同，则不做任何处理，忽略此事件。
                Logger.w(TAG, "event from uid is myself , abort this event notification");
                continue;
            }

            ReadReceiptEntity entity = JsonUtil.fromJson(eventNotification.getDescription().toStringUtf8(), ReadReceiptEntity.class);
            entity.setMsgIds(eventNotification.getMsgidListList());
            ReadReceiptEntity cachedEntity = cachedReadReceipts.get(entity.target);
            Logger.d(TAG, "ReadReceiptEntity on merge = " + entity);
            if (null == cachedEntity) {
                //当缓存的entity为空，将新的entity放到缓存中去
                cachedReadReceipts.put(entity.target, entity);
            } else {
                cachedEntity.addToMsgIdList(entity.getMsgIds());
            }
        }
        Logger.d(TAG, "ReadReceiptEvents on merge . cached read cnt" + cachedReadReceipts);
    }

    @Override
    public void afterMerge(final BasicCallback callback) {
        if (cachedReadReceipts.isEmpty()) {
            //如果缓存的read receipts是空，说明事件都是由自己发出的事件而被过滤了，此时直接触发回调
            afterMergeFinished(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
            return;
        }

        for (final ReadReceiptEntity readReceiptEntity : cachedReadReceipts.values()) {
            final Long finalTarget = readReceiptEntity.target;
            if (ResponseProcessor.CONVTYPE_SINGLE == readReceiptEntity.type) {
                UserIDHelper.getUsername(readReceiptEntity.target, new UserIDHelper.GetUsernamesCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<String> usernames) {
                        if (ErrorCode.NO_ERROR == code && !usernames.isEmpty()) {
                            String username = usernames.get(0);
                            String appkey = UserIDHelper.getUserAppkeyFromLocal(finalTarget);
                            InternalConversation internalConv = ConversationManager.getInstance().getSingleConversation(username, appkey);
                            if (null != internalConv) {
                                internalConv.updateMessageHaveReadStateInBatch(readReceiptEntity.getMsgIds());
                            }
                        }
                        afterMergeFinished(callback, code, msg);
                    }
                });
            } else if (ResponseProcessor.CONVTYPE_GROUP == readReceiptEntity.type) {
                InternalConversation internalConv = ConversationManager.getInstance().getGroupConversation(readReceiptEntity.target);
                if (null != internalConv) {
                    internalConv.updateMessageHaveReadStateInBatch(readReceiptEntity.getMsgIds());
                }
                afterMergeFinished(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
            }
        }
    }

    private class ReadReceiptEntity {
        @Expose
        long target;
        @Expose
        int type;
        Set<Long> msgIds = new HashSet<Long>();

        private Set<Long> getMsgIds() {
            return msgIds;
        }

        private void addToMsgIdList(Collection<Long> newIds) {
            if (null != msgIds) {
                msgIds.addAll(newIds);
            }
        }

        private void setMsgIds(Collection<Long> msgIds) {
            if (null == this.msgIds) {//gson在反序列化内部类时，不会调用内部类的默认构造方法，而是用的UnsafeAllocator，所以会导致这里msgIds为null。
                this.msgIds = new HashSet<Long>();
            } else {
                this.msgIds.clear();
            }
            addToMsgIdList(msgIds);
        }
    }
}
