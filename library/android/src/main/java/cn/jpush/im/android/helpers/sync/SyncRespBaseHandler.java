package cn.jpush.im.android.helpers.sync;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.tasks.RequestPackager;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/6/27.
 */

public abstract class SyncRespBaseHandler {
    private static final String TAG = "SyncRespBaseHandler";
    static final int CODE_NO_NEED_TO_LOCALIZE = -100;
    //本次会话同步消息的总页数
    private int totalPage;
    //本次conv resp的syncKey.
    long syncKey;
    //收到消息同步响应时登陆用户的uid
    long uid;
    //已经接收到的会话消息的页，key - pageNo, value - page.
    Map<Integer, Object> receivedPages = new ConcurrentHashMap<Integer, Object>();
    //需要去获取事件eventNotification的pageContent总个数。
    AtomicInteger entitiesToLocalize = new AtomicInteger();
    //已经本地化处理完成的Conv数。
    AtomicInteger finishedCounter = new AtomicInteger();

    //批量请求管理器
    RequestPackager requestPackager;

    //整个消息同步动作的回调
    protected BasicCallback callback;

    Watcher operationWatcher = new Watcher() {
        private int errorCode = ErrorCode.NO_ERROR;
        private String errorDesc = ErrorCode.NO_ERROR_DESC;

        @Override
        public void update(int statusCode) {
            //当这次resp所包含的所有entity都已经本地化完成时，再触发回调
            Logger.dd(TAG, "conversation localize finished. status code = " + statusCode);
            if (CODE_NO_NEED_TO_LOCALIZE == statusCode) {
                //如果错误码是"此pageContent无需本地化处理"，则需要将pageContentsToGetEventNotificationCnt减一，并将这个值传给GetEventNotificationTaskMng，
                //通知对方需要获取msgContent的会话总数少了一个。
                if (null != requestPackager) {
                    requestPackager.updateTotalCount(uid, entitiesToLocalize.decrementAndGet());
                }
            } else if (0 != statusCode) {
                errorCode = statusCode;
                errorDesc = "conversation localize failed.";
            }
            if (finishedCounter.incrementAndGet() == getTotalEntityCount() && null != callback) {
                callback.gotResult(errorCode, errorDesc);
            }
        }

        @Override
        public void resetErrorCode() {
            errorCode = ErrorCode.NO_ERROR;
            errorDesc = ErrorCode.NO_ERROR_DESC;
        }
    };

    SyncRespBaseHandler(RequestPackager packager) {
        this.requestPackager = packager;
    }

    void clearCache() {
        receivedPages.clear();
        finishedCounter.set(0);
        //需要将operationWatcher中的errorcode重置为0，否则错误码会带到之后的同步中去。
        operationWatcher.resetErrorCode();
        //将getChatTaskMng中缓存的Conversation等信息也清掉。
        if (null != requestPackager) {
            requestPackager.clearCache();
        }
    }

    public final void pageReceived(final IMProtocol protocol) {
        uid = protocol.getUid();
        long myUid = IMConfigs.getUserID();
        Logger.dd(TAG, "sync resp received , uid = " + protocol.getUid());
        if (uid != myUid) {
            //如果收到的protocol中的uid和当前登陆的用户的uid不同，则直接丢弃这个protocol。
            Logger.ww(TAG, "current uid not match uid in protocol. abort this protocol.");
            return;
        }

        int totalPageThisTime = getTotalPage(protocol);
        if (0 == totalPageThisTime) { //收到resp中totalPage为0,说明用户是首次SyncCheck,此时直接将收到的syncKey保存到本地，然后回给后台。
            Logger.d(TAG, "total page is 0, just send sync key back");
            sendSyncACK(getSyncKey(protocol));
            return;
        }

        if (totalPage != totalPageThisTime && totalPage != 0) {
            //此次收到的总页数和上一次的总页数不等，说明此次收到的页属于一次新的sync，需要将之前缓存的页清掉。
            Logger.ww(TAG, "total page mismatch. clear cached pages");
            clearCache();
        }

        totalPage = totalPageThisTime;

        int pageNo = getPageNo(protocol);
        if (0 != pageNo) {
            //将收到的页缓存起来，根据pageNo，如果收到重复的直接覆盖之前的页
            receivedPages.put(pageNo, protocol.getEntity());
            Logger.ii(TAG, "[PageReceived] receive page . page no = " + pageNo + " page = " + protocol.getEntity());
            Logger.ii(TAG, "[PageReceived] cached page no" + receivedPages.keySet());
        }

        if (totalPage == receivedPages.size()) {
            //收到了所有页的消息后，才开始处理消息。
            Logger.dd(TAG, "[PageReceived]start convLocalize conv pages. total page " + totalPage);

            Task.callInBackground(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    startLocalize(new BasicCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage) {
                            if (ErrorCode.NO_ERROR == responseCode) {
                                //如果本地化成功，则发送ack给后台
                                sendSyncACK(syncKey);
                            } else {
                                //本次同步失败了，缩短下次同步的间隔。
                                Logger.ww(TAG, "SyncCheck failed,shorten next SyncCheck's interval");
                                RequestProcessor.resetAndStartSyncCheck(30 * 1000);
                            }
                            Logger.ii(TAG, "sync resp finished. code = " + responseCode + " desc = " + responseMessage);
                            clearCache();
                        }
                    });
                    return null;
                }
            });

        }
    }

    abstract int getTotalPage(IMProtocol protocol);

    abstract int getPageNo(IMProtocol protocol);

    abstract long getSyncKey(IMProtocol protocol);

    /**
     * 开始本地化
     *
     * @param callback 整体本地化的回调接口
     */
    void startLocalize(BasicCallback callback) {
        this.callback = callback;
    }

    /**
     * 更新entitiesToLocalize这个变量，用于判断还有多少个entities需要被本地化。
     * 必须由子类主动调用。
     *
     * @param cnt 更新后的需要本地化处理的entities数量
     */
    void onEntitiesToLocalizeCntUpdated(int cnt) {
        entitiesToLocalize.set(cnt);
    }

    abstract void sendSyncACK(long syncKey);

    abstract int getTotalEntityCount();

    interface Watcher {
        void update(int statusCode);

        void resetErrorCode();
    }
}
