package cn.jpush.im.android.tasks;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.utils.AndroidUtil;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by hxhg on 2017/6/29.
 */

public class GetEventNotificationTaskMng extends RequestPackager {
    private static final String TAG = "GetEventNotificationTaskMng";

    private static int MSG_LIMIT_PER_REQUEST_FAST_NETWORK = 5000;
    private static int MSG_LIMIT_PER_REQUEST_SLOW_NETWORK = 300;

    private static GetEventNotificationTaskMng instance = null;

    //该批次请求所缓存的EventEntity对象map
    private Map<String, EventEntity> cachedEntities = new HashMap<String, EventEntity>();
    //该批次请求所缓存的EventEntity对象中包含的eventId总数
    private int cachedEventIdCnt = 0;

    private int entitiesSent = 0;//已发送过请求的entity数。

    private GetEventNotificationTask.GetEventNotificationInBatchCallback getChatMsgCallback = new GetEventNotificationTask.GetEventNotificationInBatchCallback() {
        @Override
        public void gotResult(int responseCode, String responseMessage, Map<String, EventEntity> entities) {
            for (EventEntity entity : entities.values()) {
                //批量会话消息获取成功后，回调每个SyncConv对应的callback
                ((GetEventNotificationCallback) entity.callback).gotResult(responseCode, responseMessage, entity);
            }
        }
    };

    private GetEventNotificationTaskMng(int limitInFast, int limitInSlow) {
        super(limitInFast, limitInSlow);
    }

    public static synchronized GetEventNotificationTaskMng getInstance() {
        if (null == instance) {
            instance = new GetEventNotificationTaskMng(MSG_LIMIT_PER_REQUEST_FAST_NETWORK, MSG_LIMIT_PER_REQUEST_SLOW_NETWORK);
        }
        return instance;
    }

    /**
     * 以eventPageConent为单位，将其中的eventid list加到缓存中准备请求eventNotification content。
     * <p>
     * 当缓存中存储的消息数量达到上限，或者缓存的会话数已经达到了要处理的会话总数，才会真正发起一次getEventNotification请求。
     *
     * @param uid        当前用户uid
     * @param totalCount 需要处理的会话总数
     * @param convId     此次处理的会话id，（指sync conversation协议中的conv_id）
     * @param callback   回调接口,这里必须是GetEventNotificationCallback类型的callback
     * @param params     参数，这里应该是eventId列表
     */
    @Override
    public synchronized void prepareToRequest(long uid, int totalCount, String convId, Object callback, Object... params) {
        int perRequestMsgLimit;
        if (AndroidUtil.isConnectionFast()) {
            perRequestMsgLimit = limitPerRequestInFastNetWork;
        } else {
            perRequestMsgLimit = limitPerRequestInSlowNetWork;
        }

        if (null != params && params.length > 0) {
            Collection<Long> eventIds = (Collection<Long>) params[0];
            Logger.d(TAG, "current request limit is " + perRequestMsgLimit + " totalEventEntityCount = " + totalCount + " entities sent = " + entitiesSent);
            EventEntity entity = new EventEntity(convId, eventIds, callback);
            if ((perRequestMsgLimit < (cachedEventIdCnt + eventIds.size())) && cachedEventIdCnt > 0) {
                Logger.d(TAG, "cached msg cnt exceed its limit, send request and clear cache");
                //如果本次请求的msgId数量加上之前累积的msgId数量超过了一次http请求的上限，则直接把之前缓存的entities发起一次请求。
                sendRequest(uid);
            }
            cachedEventIdCnt += eventIds.size();
            cachedEntities.put(convId, entity);

            if (cachedEntities.size() >= (totalCount - entitiesSent)) {
                sendRequest(uid);
                entitiesSent = 0;
            }
        } else {
            Logger.ww(TAG, "params are null");
        }
    }

    //重新设置totalEntityCount，同时如果GetEventNotificationTaskMng已经缓存了足够多的会话，则可以发起一次getEventNotificationContent请求。
    @Override
    public synchronized void updateTotalCount(long uid, int totalEventEntityCount) {
        Logger.d(TAG, "total event entity count = " + totalEventEntityCount + " cur event entity count = " + cachedEntities.size());
        if (cachedEntities.size() >= (totalEventEntityCount - entitiesSent)) {
            sendRequest(uid);
            entitiesSent = 0;
        }
    }

    @Override
    protected void sendRequest(long uid) {
        if (cachedEntities.isEmpty()) {
            Logger.w(TAG, "cachedEntities is empty, return from sendRequest");
            return;
        }
        Logger.d(TAG, "send request , cached event id cnt = " + cachedEventIdCnt + " cachedEntities = " + cachedEntities);
        Map<String, EventEntity> requestEntitiesCopy = new HashMap<String, EventEntity>(cachedEntities);
        new GetEventNotificationTask(uid, requestEntitiesCopy, getChatMsgCallback).execute();
        entitiesSent += requestEntitiesCopy.size();
        clearCache();
    }

    public static class EventEntity {

        @Expose
        @SerializedName("cont_id")
        private String convId;

        @Expose
        @SerializedName("eid")
        private Collection<Long> eventIds;//所有的eventId列表

        private Message.ConEventResponse conEventResponse;
        private Object callback;

        private EventEntity(String convId, Collection<Long> eventIds, Object callback) {
            this.convId = convId;
            this.eventIds = eventIds;
            this.callback = callback;
        }

        public String getConvId() {
            return convId;
        }


        public Object getCallback() {
            return callback;
        }

        public void setConEventResponse(Message.ConEventResponse conEventResponse) {
            this.conEventResponse = conEventResponse;
        }

        public Message.ConEventResponse getConEventResponse() {
            return conEventResponse;
        }

        @Override
        public String toString() {
            return "EventEntity{" +
                    "convId='" + convId + '\'' +
                    ", eventIds=" + eventIds +
                    ", callback=" + callback +
                    '}';
        }
    }

    //清空当前单次getMsgContent请求的缓存
    @Override
    public void clearCache() {
        cachedEntities.clear();
        cachedEventIdCnt = 0;
    }

    public interface GetEventNotificationCallback {
        void gotResult(int responseCode, String responseMessage, EventEntity entity);
    }

}
