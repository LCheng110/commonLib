package cn.jpush.im.android.helpers;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/8/28.
 */

public class MsgReceiptReportRequestPackager {

    private static final String TAG = MsgReceiptReportRequestPackager.class.getSimpleName();

    private static final int SEND_REQUEST = 100;

    private static final Long SEND_INTERVAL = 2 * 1000L;// 请求时间间隔.

    private static final int SEND_LIMIT = 50;// 单次请求msgid个数上限.

    private static MsgReceiptReportRequestPackager instance = null;

    private static Handler requestHandler;
    private Map<Long, RequestEntity> cachedEntities = new HashMap<Long, RequestEntity>();

    private MsgReceiptReportRequestPackager() {
    }

    public static synchronized MsgReceiptReportRequestPackager getInstance() {
        if (null == instance) {
            instance = new MsgReceiptReportRequestPackager();
            requestHandler = new RequestHandler(Looper.getMainLooper());
        }
        return instance;
    }

    public synchronized void addServerMsgId(long targetId, int msgType, long serverMsgId, BasicCallback callback) {
        Logger.d(TAG, "[addServerMsgId] targetID = " + targetId + " msgType = " + msgType + " serverMsgId = " + serverMsgId);
        RequestEntity cachedEntity = cachedEntities.get(targetId);
        if (null == cachedEntity) {
            cachedEntity = new RequestEntity(targetId, msgType, serverMsgId, callback);
            Logger.d(TAG, "new entity created. cachedEntity = " + cachedEntity);
            cachedEntities.put(targetId, cachedEntity);
            Message message = requestHandler.obtainMessage(SEND_REQUEST, cachedEntity);
            /*这里不用makePackageAndSend这个方法创建一个clone entity再发送的原因是：对于请求间隔时间内可能会收到多个msgid，cachedEntity中的数据就可能会变化，而此时对于这批msgid,是需要批量一次性请求上去的.
              如果使用clone的entity就会导致第一批只有最开始被clone的一个msgid被请求上去，而之后2s之内收到的其他msgid只能在这个请求完成之后才会走请求，这样就有问题。
             */
            requestHandler.sendMessageDelayed(message, SEND_INTERVAL);
        } else {
            cachedEntity.addMsgIdAndCallback(serverMsgId, callback);
            if (SEND_LIMIT <= cachedEntity.getCachedMsgIdsCnt()) {//当某个entity中已缓存的msgid个数超过单次请求上限时
                Logger.d(TAG, "cached msg ids cnt exceed its limit. send request. cachedEntity = " + cachedEntity);
                makePackageAndSend(cachedEntity);
            }
        }
    }

    //将传过来的entity clone一份然后立即发送回执请求。
    private void makePackageAndSend(RequestEntity entity) {
        requestHandler.removeMessages(SEND_REQUEST, entity);//清掉handler中已有的请求,这里必须带上entity,表示这里仅清掉这个entity所对应的请求，否则会导致其他会话的请求也被清掉
        //这里需要使用entity.clone主要是为了将entity中的serverMsgIds clone一份，防止在请求发送出去还没响应的这段时间，
        //上层又有新的msgId传递过来导致entity中的serverMsgIds被改变。
        final RequestEntity cloned = entity.clone();// TODO: 2017/9/26 这里是否可以将clone动作去掉，统一在sendRequest时做clone然后发送就行？
        Message message = requestHandler.obtainMessage(SEND_REQUEST, cloned);//将cloned entity对象发送出去，确保请求msgID个数严格按照限制来。
        entity.removeMsgIdsAndCallbacksFromCache(cloned.serverMsgIds, cloned.callbacks);//将请求已经发出这部分的msgID和callback从cache中去掉。这里必须清掉，否则当缓存msgid数量超过一批的最大值之后，每增加一个msgid都会重复发出请求。
        requestHandler.sendMessage(message);//立即发送请求
    }

    private synchronized void onRequestFinished(RequestEntity entitySent) {
        Logger.d(TAG, "onRequestFinished . entitySent = " + entitySent);
        if (null != entitySent) {
            RequestEntity cachedEntity = cachedEntities.get(entitySent.targetId);
            Logger.d(TAG, "onRequestFinished . cached entity = " + cachedEntity);
            if (null != cachedEntity) {
                cachedEntity.removeMsgIdsAndCallbacksFromCache(entitySent.serverMsgIds, entitySent.callbacks);//这里再次将请求出去的msgid和callback从cached entity中删除一遍，保证不出问题
                if (0 < cachedEntity.getCachedMsgIdsCnt()) {
                    //请求完成之后，cachedEntity中还存在有msgid，说明在请求阶段，上层又有新的msgId添加进来，此时再次发起发送请求
                    makePackageAndSend(cachedEntity);
                } else {
                    cachedEntities.remove(entitySent.targetId);//cachedEntity中所有msgId都已经处理完，将这个entity从cache中清掉。
                }
            }
        }
    }

    private static class RequestHandler extends Handler {

        RequestHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case SEND_REQUEST:
                    sendRequest((RequestEntity) msg.obj);
                    break;
            }
        }

        private void sendRequest(RequestEntity entitySent) {
            Logger.d(TAG, "[sendRequest] send receipt report request . cachedEntity = " + entitySent);
            final RequestEntity cloned = entitySent.clone();
            if (null != cloned) {
                RequestProcessor.imMsgReceiptReportRequest(cloned, new BasicCallback(false) {
                    @Override
                    public void gotResult(int responseCode, String responseMessage) {
                        cloned.onRequestFinished(responseCode, responseMessage);//通知entity本身，请求已完成。
                        MsgReceiptReportRequestPackager.getInstance().onRequestFinished(cloned);//通知外部RequestPackager，这部分entity的请求已完成。
                    }
                }, IMConfigs.getNextRid());
            }
        }
    }

    public static class RequestEntity implements Cloneable {
        public long targetId;//消息会话目标id， 单聊为请求发起方uid，群聊则为gid
        public int msgType;
        public HashSet<Long> serverMsgIds = new HashSet<Long>();
        CopyOnWriteArrayList<BasicCallback> callbacks = new CopyOnWriteArrayList<BasicCallback>();

        RequestEntity(long targetId, int msgType, Long serverMsgId, BasicCallback callback) {
            this.targetId = targetId;
            this.msgType = msgType;
            Collections.synchronizedSet(serverMsgIds);
            serverMsgIds.add(serverMsgId);
            callbacks.add(callback);
        }

        synchronized void addMsgIdAndCallback(long serverMsgId, BasicCallback callback) {
            Logger.d(TAG, "[addMsgIdAndCallback]. before add . target id = " + targetId + "server msg ids = " + serverMsgIds + " callback size " + callbacks.size());
            serverMsgIds.add(serverMsgId);
            callbacks.add(callback);
            Logger.d(TAG, "[addMsgIdAndCallback]. after add . server msg ids = " + serverMsgIds + " callback size " + callbacks.size());
        }

        synchronized void removeMsgIdsAndCallbacksFromCache(Collection<Long> msgIds, Collection<BasicCallback> callbacks) {
            Logger.d(TAG, "[removeMsgIdsAndCallbacksFromCache]. before remove . msgids = " + serverMsgIds + " callbacks size = " + this.callbacks.size());
            serverMsgIds.removeAll(msgIds);
            this.callbacks.removeAll(callbacks);
            Logger.d(TAG, "[removeMsgIdsAndCallbacksFromCache]. after remove . msgids = " + serverMsgIds + " callbacks = " + this.callbacks.size());
        }

        int getCachedMsgIdsCnt() {
            return serverMsgIds.size();
        }

        void onRequestFinished(int responseCode, String responseMsg) {
            Logger.d(TAG, "send receipt report request finished. code = " + responseCode + " msg = " + responseMsg);
            for (BasicCallback callback : callbacks) {//分别触发所有上层过来的请求的callback。
                CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
            }
            callbacks.clear();
        }

        @Override
        protected RequestEntity clone() {
            RequestEntity o = null;
            try {
                o = (RequestEntity) super.clone();
                o.serverMsgIds = (HashSet<Long>) serverMsgIds.clone();
                Collections.synchronizedSet(o.serverMsgIds);
                o.callbacks = (CopyOnWriteArrayList<BasicCallback>) callbacks.clone();
            } catch (CloneNotSupportedException e) {
                Logger.ww(TAG, "clone RequestEntity content failed!");
                e.printStackTrace();
            }
            return o;
        }

        @Override
        public String toString() {
            return "RequestEntity{" +
                    "targetId=" + targetId +
                    ", msgType=" + msgType +
                    ", serverMsgIds=" + serverMsgIds +
                    ", callbacks.size=" + callbacks.size() +
                    '}';
        }
    }

}
