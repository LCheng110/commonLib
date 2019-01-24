package cn.jpush.im.android.helpers.eventsync.ReadReceiptEvents;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ResetUnreadCntRequest;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/8/17.
 */

public class ReadCountMtimeUpdateEvents extends Kind8BaseEvents {
    private static final String TAG = ReadCountMtimeUpdateEvents.class.getSimpleName();

    private Map<Long, ReadCntUpdateEntity> cachedReadCnts = new HashMap<Long, ReadCntUpdateEntity>();

    public ReadCountMtimeUpdateEvents(List<Message.EventNotification> notifications) {
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

            ReadCntUpdateEntity entity = JsonUtil.fromJson(eventNotification.getDescription().toStringUtf8(), ReadCntUpdateEntity.class);
            entity.mTime = eventNotification.getCtimeMs();
            ReadCntUpdateEntity cachedEntity = cachedReadCnts.get(entity.target);
            Logger.d(TAG, "ReadCntUpdateEntity on merge = " + entity);
            if (null == cachedEntity || cachedEntity.mTime < entity.mTime) {
                //当缓存的entity为空，或者缓存的entity的mtime小于新的entity的mtime时，用新的entity替换cachedEntity
                cachedReadCnts.put(entity.target, entity);
            }
        }
        Logger.d(TAG, "ReadCountMtimeUpdateEvents on merge . cached read cnt" + cachedReadCnts);
    }

    @Override
    public void afterMerge(final BasicCallback callback) {
        if (cachedReadCnts.isEmpty()) {
            //如果缓存的readCnts是空，说明事件都是由自己发出的事件而被过滤了，此时直接触发回调
            afterMergeFinished(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
            return;
        }

        Collection<ReadCntUpdateEntity> values = cachedReadCnts.values();
        for (final ReadCntUpdateEntity entity : values) {
            Logger.d(TAG, "ReadCntUpdateEntity = " + entity);
            switch (entity.type) {
                case ResetUnreadCntRequest.TARGET_TYPE_SINGLE:
                    UserIDHelper.getUsername(entity.target, new UserIDHelper.GetUsernamesCallback(false) {
                        @Override
                        public void gotResult(int code, String msg, List<String> usernames) {
                            if (null != usernames && !usernames.isEmpty()) {
                                String targetID = usernames.get(0);
                                String targetAppkey = UserIDHelper.getUserAppkeyFromLocal(entity.target);
                                InternalConversation conversation = ConversationManager.getInstance().createConversation(ConversationType.single, targetID, targetAppkey);
                                if (null != conversation) {
                                    ConversationManager.getInstance().updateConversationUnreadCntMtime(ConversationType.single, targetID, targetAppkey, entity.mTime);
                                }
                            }
                            afterMergeFinished(callback, code, msg);
                        }
                    });
                    break;
                case ResetUnreadCntRequest.TARGET_TYPE_GROUP:
                    String targetID = String.valueOf(entity.target);
                    InternalConversation conversation = ConversationManager.getInstance().createConversation(ConversationType.group, targetID, "");
                    if (null != conversation) {
                        ConversationManager.getInstance().updateConversationUnreadCntMtime(ConversationType.group, targetID, "", entity.mTime);
                    }
                    afterMergeFinished(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
                    break;
            }
        }
    }

    private class ReadCntUpdateEntity {
        long target;
        int type;
        int read_count;
        long mTime;

        @Override
        public String toString() {
            return "ReadCntUpdateEntity{" +
                    "target=" + target +
                    ", type=" + type +
                    ", read_count=" + read_count +
                    ", mTime=" + mTime +
                    '}';
        }
    }
}
