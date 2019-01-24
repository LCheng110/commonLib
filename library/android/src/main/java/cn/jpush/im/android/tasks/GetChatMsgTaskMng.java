package cn.jpush.im.android.tasks;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.AndroidUtil;
import cn.jpush.im.android.utils.Logger;

/**
 * 用于将多个会话中msgid聚合到一起，批量一次请求msg content,这样可以提高效率
 */
public class GetChatMsgTaskMng extends RequestPackager {
    private static final String TAG = "GetChatMsgTaskMng";

    private static int MSG_LIMIT_PER_REQUEST_FAST_NETWORK = 3000;
    private static int MSG_LIMIT_PER_REQUEST_SLOW_NETWORK = 300;

    private static GetChatMsgTaskMng instance = null;

    //该批次请求所缓存的会话对象map
    private Map<String, ConvEntity> cachedEntities = new HashMap<String, ConvEntity>();
    //该批次请求所缓存的会话对象中包含的消息总数
    private int cachedMsgCnt = 0;

    private int convSent = 0;//已发送过请求的会话数。

    private GetChatMsgTask.GetChatMsgCallback getChatMsgCallback = new GetChatMsgTask.GetChatMsgCallback() {
        @Override
        public void gotResult(int responseCode, String responseMessage, Map<String, ConvEntity> entities) {
            for (ConvEntity entity : entities.values()) {
                //批量会话消息获取成功后，回调每个SyncConv对应的callback
                ((GetMessageContentCallback) entity.callback).gotResult(responseCode, responseMessage, entity);
            }
        }
    };

    private GetChatMsgTaskMng(int limitInFast, int limitInSlow) {
        super(limitInFast, limitInSlow);
    }

    public static synchronized GetChatMsgTaskMng getInstance() {
        if (null == instance) {
            instance = new GetChatMsgTaskMng(MSG_LIMIT_PER_REQUEST_FAST_NETWORK, MSG_LIMIT_PER_REQUEST_SLOW_NETWORK);
        }
        return instance;
    }

    /**
     * 以会话为单位，将会话中的newlist和oldlist中数据加到缓存中准备请求msgContent。
     * <p>
     * 当缓存中存储的消息数量达到上限，或者缓存的会话数已经达到了要处理的会话总数，才会真正发起一次getMsgContent请求。
     *
     * @param uid        当前用户uid
     * @param totalCount 需要处理的会话总数
     * @param convId     此次处理的会话id，（指sync conversation协议中的conv_id）
     * @param callback   回调接口,这里必须是GetMessageContentCallback类型的callback
     * @param params     参数，这里应该传三个Collection<Long>类型的参数，
     *                   其中第一个为newList,
     *                   第二个为oldList
     *                   第三个为由我自己发出的消息msgid集合,
     */
    @Override
    public synchronized void prepareToRequest(long uid, int totalCount, String convId, Object callback, Object... params) {
        int perRequestMsgLimit;
        if (AndroidUtil.isConnectionFast()) {
            perRequestMsgLimit = limitPerRequestInFastNetWork;
        } else {
            perRequestMsgLimit = limitPerRequestInSlowNetWork;
        }

        if (null != params && params.length > 1) {
            Collection<Long> newList = (Collection<Long>) params[0];
            Collection<Long> oldList = (Collection<Long>) params[1];
            Collection<Long> msgIdsSentByMyself = (Collection<Long>) params[2];
            Logger.d(TAG, "current request limit is " + perRequestMsgLimit + " totalConvCount = " + totalCount + " convs sent = " + convSent);
            ConvEntity entity = new ConvEntity(convId, newList, oldList, msgIdsSentByMyself, callback);
            if ((perRequestMsgLimit < (cachedMsgCnt + entity.getTotalMsgIdsSize())) && cachedMsgCnt > 0) {
                Logger.d(TAG, "cached msg cnt exceed its limit, send request and clear cache");
                //如果本次请求的msgId数量加上之前累积的msgId数量超过了一次http请求的上限，则直接把之前缓存的entities发起一次请求。
                sendRequest(uid);
            }
            cachedMsgCnt += entity.getTotalMsgIdsSize();
            cachedEntities.put(convId, entity);

            if (cachedEntities.size() >= (totalCount - convSent)) {
                sendRequest(uid);
                convSent = 0;//剩下会话已经全部发送完成，convSent重置为0
            }
        } else {
            Logger.ww(TAG, "params are null");
        }
    }

    //重新设置totalConvCount，同时如果GetChatMsgTaskMng已经缓存了足够多的会话，则可以发起一次getMsgContent请求。
    public synchronized void updateTotalCount(long uid, int totalConvCount) {
        Logger.d(TAG, "total conv count to " + totalConvCount + " cur conv count = " + cachedEntities.size());
        if (cachedEntities.size() >= (totalConvCount - convSent)) {
            sendRequest(uid);
            convSent = 0;//剩下会话已经全部发送完成，convSent重置为0
        }
    }

    @Override
    protected void sendRequest(long uid) {
        if (cachedEntities.isEmpty()) {
            Logger.w(TAG, "cachedEntities is empty, return from sendRequest");
            return;
        }
        Logger.d(TAG, "send request , cachedMsgCnt = " + cachedMsgCnt + " cachedEntities = " + cachedEntities);
        Map<String, ConvEntity> requestEntitiesCopy = new HashMap<String, ConvEntity>(cachedEntities);
        new GetChatMsgTask(uid, requestEntitiesCopy, getChatMsgCallback).execute();
        convSent += requestEntitiesCopy.size();
        clearCache();
    }

    public static class ConvEntity {

        @Expose
        @SerializedName("con_id")
        private String convId;

        @Expose
        @SerializedName("msgid")
        private Collection<Long> msgIds;//所有的msgId列表，包括newList和oldList

        @Expose
        @SerializedName("unread_count_msgid")
        private Collection<Long> msgIdsSentByMyself;//所有msgid中，属于我自己发送的那部分。这部分数据单独给到后台，后台返回这部分消息的未回执数。

        private Collection<Long> newList;//请求的msg content的msgId中，属于newList部分的msgId.
        private Collection<Long> oldList;//请求的msg content的msgId中，属于oldList部分的msgId.
        private Collection<InternalMessage> messages = null;//通过msgId获取到msg content之后，实际的msg列表
        private Object callback;

        private ConvEntity(String convId, Collection<Long> newList, Collection<Long> oldList, Collection<Long> msgIdsSentByMyself, Object callback) {
            this.convId = convId;
            this.newList = newList;
            this.oldList = oldList;
            this.msgIdsSentByMyself = msgIdsSentByMyself;
            this.callback = callback;
            msgIds = new ArrayList<Long>(newList);
            msgIds.addAll(oldList);
        }

        public String getConvId() {
            return convId;
        }

        public Collection<InternalMessage> getMessages() {
            return messages;
        }

        public void setMessages(Collection<InternalMessage> messages) {
            this.messages = messages;
        }

        public Collection<Long> getNewList() {
            return newList;
        }

        public Collection<Long> getOldList() {
            return oldList;
        }

        public int getTotalMsgIdsSize() {
            return msgIds.size();
        }

        public Object getCallback() {
            return callback;
        }

        @Override
        public String toString() {
            return "ConvEntity{" +
                    "convId='" + convId + '\'' +
                    ", msgIds size=" + msgIds.size() +
                    ", msgIdsSentByMyself size =" + ((null != msgIdsSentByMyself) ? msgIdsSentByMyself.size() : null) +
                    ", newList size =" + newList.size() +
                    ", messages size =" + ((null != messages) ? messages.size() : null) +
                    '}';
        }
    }

    //清空当前单次getMsgContent请求的缓存
    @Override
    public void clearCache() {
        Logger.d(TAG, "on clear cache");
        cachedEntities.clear();
        cachedMsgCnt = 0;
    }

    public interface GetMessageContentCallback {
        void gotResult(int responseCode, String responseMessage, ConvEntity entity);
    }
}
