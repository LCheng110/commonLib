package cn.jpush.im.android.pushcommon.helper;

import android.content.Context;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import java.lang.ref.WeakReference;
import java.util.Deque;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jiguang.ald.api.JRequest;
import cn.jiguang.ald.api.JResponse;
import cn.jiguang.ald.api.SdkType;
import cn.jpush.im.android.helpers.ResponseProcessor;
import cn.jpush.im.android.pushcommon.proto.JMessageCommands;
import cn.jpush.im.android.pushcommon.proto.common.commands.IMRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMCommands;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

/**
 * 一个单独的线程，负责所有对外命令发送。
 * <p/>
 * Service 通过该线程的 Handler 把任务转移进来。
 * <p/>
 * <p/>
 * 登录状态时，直接对外发送，进入 SentQueue。这部分不会重发。会超时。
 * <p/>
 * 未登录状态时，临时缓存请求队列到 RequestingQueue。登录后，按顺序发送。
 */
public class RequestingThread extends HandlerThread {
    private static final String TAG = "RequestingThread";

    private static final int TIMEOUT_SENT = 10 * 1000;
    private static final int QUEUE_MAX_SIZE = 100;

    // 请求缓存，用于超时与请求重发，需要收到响应的request才需要进入这个cache,其他比如recv之类的请求，就不需要进入
    //到这个队列。
    private Map<Long, Requesting> mRequestingCache = new ConcurrentHashMap<Long, Requesting>();

    // 请求队列：当未登录状态时，缓存请求队列 - 某些命令可能要删除排在前边的相同命令（覆盖）
    private Deque<Requesting> mRequestingQueue = new LinkedBlockingDeque<Requesting>();

    // 已发出队列：heartbeat 超时，表示网络不好；这里的超时会向上层给出。
    private Deque<Requesting> mSentQueue = new LinkedBlockingDeque<Requesting>();

    private Context mContext;
    private Handler mRequestHandler;

    private boolean mLoggedIn = false;
    private int mSid = 0;
    private long mJuid = 0l;
    private String mAppkey = "";

    public RequestingThread(final Context context) {
        super("RequestingThread");
        this.mContext = context;

        mLoggedIn = PluginJCoreHelper.isPushLoggedIn();
        mSid = JCoreInterface.getSid();
        mJuid = JCoreInterface.getUid();
        mAppkey = JCoreInterface.getAppKey();

        this.start();

        mRequestHandler = new MyHandler(Looper.getMainLooper(), this);
    }

    @Override
    public void run() {
        Logger.d(TAG, "RequestingThread started - threadId:" + Thread.currentThread().getId());
        super.run();
    }

    public void handleResponse(long connection, Object obj) {
        Message message = Message.obtain(mRequestHandler, MSG_RESPONSE, obj);
        message.sendToTarget();
    }

    public void sendRequest(JRequest request, int timeout) {
        Message message = Message.obtain(mRequestHandler, MSG_REQUEST, timeout, 0, request);
        message.sendToTarget();
    }

    // TODO: Remove all timeouts ?
    public void onLoggedIn() {
        Logger.dd(TAG, "Action - onLoggedIn");
        mLoggedIn = true;
        mSid = JCoreInterface.getSid();
        mJuid = JCoreInterface.getUid();
        mAppkey = JCoreInterface.getAppKey();
        Logger.d(TAG, "sid:" + mSid + ", juid:" + mJuid + ", appkey:" + mAppkey);

        mRequestHandler.sendEmptyMessage(MSG_SEND_OLD_QUEUE);
    }

    public void onDisconnected() {
        Logger.vv(TAG, "Action - onDisconnected");

        mLoggedIn = false;

        restoreSentQueue();
    }

    // TODO: 16/5/30 适时的清理
    public void quitThread() {
        if (Build.VERSION.SDK_INT >= 18) {
            this.quitSafely();
        } else {
            this.quit();
        }
    }

    private void restoreSentQueue() {
        Logger.d(TAG, "Action - restoreSentQueue - sentQueueSize:" + mSentQueue.size());
        mRequestHandler.removeMessages(MSG_SENT_TIMEOUT);

        Requesting requesting;
        while ((requesting = mSentQueue.pollLast()) != null) {
            mRequestingQueue.offerFirst(requesting);
        }
        printRequestingQueue();
        printRequestingCache();
    }

    private void handleResponseInternal(long connection, Object obj) {
        JResponse response = (JResponse) obj;
        Logger.d(TAG, "Action - handleResponse - connection:" + connection
                + ", response:" + response.toString());

        // TODO: 16/5/27 需要判断是否是同一个接入, 否则频繁断连可能存在重复收到消息
        // ----


        Long rid = response.getRid();

        // 清理发送队列
        Requesting origin = dequeSentQueue(rid);
        if (null == origin) {
            Logger.w(TAG, "Not found the request in SentQueue when response.");
        } else {
            // should be the exact request RID
            rid = origin.request.getRid();
            endSentTimeout(rid);
        }

        // 清理请求队列
        Requesting requesting = mRequestingCache.get(rid);
        if (null != requesting) {
            endRequestTimeout(requesting);
        } else {
            Logger.w(TAG, "Not found requesting in RequestingCache when response.");
        }
    }

    private void onSentTimeout(Requesting requesting) {
        Logger.v(TAG, "Action - onSentTimeout - " + requesting.toString());

        Long rid = requesting.request.getRid();
        dequeSentQueue(rid);

        if (requesting.timeout > 0) {
            if (mLoggedIn) {
                // Trigger heartbeat for checking network state.

                Logger.v(TAG, "Retry to send request - " + requesting.toString());
                requesting.retryAgain();
                sendCommandWithLoggedIn(requesting);
            } else {
                Logger.v(TAG, "Want retry to send but not logged in. Sent move to RequestingQueue");
                mRequestingQueue.offerFirst(requesting);
            }

            if (requesting.times >= 2) {
                // TODO: 16/5/27
                //mMainHandler.sendEmptyMessageDelayed(PushService.MSG_KEEP_ALIVE_NOMAL, 1000 * 2);
            }
        } else {
            onRequestTimeout(requesting);
        }
    }

    // 向业务层传递。即使无特定 timeout 即是底层默认的 10s 超时
    private void onRequestTimeout(Requesting requesting) {
        Logger.d(TAG, "Action - onRequestTimeout - " + requesting.toString());

        int cmd = requesting.request.getCommand();
        Long rid = requesting.request.getRid();

        endRequestTimeout(requesting);

        switch (cmd) {
            case JMessageCommands.IM.CMD:
                IMRequest imRequest = (IMRequest) requesting.request;
                IMProtocol imProtocol = imRequest.getIMProtocol();
                onImTimeoutToReceiver(imProtocol.getCommand(), rid);
                break;

            default:
                Logger.d(TAG, "Ignore other command timeout.");
        }
    }


    private void resendRequestingQueue() {
        Logger.d(TAG, "Action - resendRequestingQueue - size:" + mRequestingQueue.size());

        printRequestingQueue();
        printRequestingCache();

        Requesting requesting;
        while ((requesting = mRequestingQueue.pollFirst()) != null) {

            requesting.retryAgain();
            sendCommandWithLoggedIn(requesting);
        }
    }

    private void sendRequestInternal(JRequest request, int timeout) {
        Logger.dd(TAG, "Action - sendRequestInternal - connection:" /*+ NetworkingClient.sConnection.get()*/
                + ", timeout:" + timeout
                + ", threadId:" + Thread.currentThread().getId());
        Logger.v(TAG, request.toString());
        Long rid = request.getRid();

        boolean isImPushCommand = false;
        if (request.getCommand() == JMessageCommands.IM.CMD) {
            IMRequest imRequest = (IMRequest) request;
            if (IMServiceHelper.isImPushCommand(imRequest.getIMProtocol().getCommand())) {
                isImPushCommand = true;
            }
        }

        Requesting requesting = new Requesting(request, timeout);
        if (!isImPushCommand) {
            mRequestingCache.put(rid, requesting);
        }

        if (timeout > TIMEOUT_SENT) {
            startRequestTimeout(requesting);
        }

        preProcessRequests(request);

        if (mLoggedIn) {
            requesting.retryAgain();
            sendCommandWithLoggedIn(requesting);
        } else {
            Logger.i(TAG, "Not logged in currently. Give up to send now.");
            mRequestingQueue.offerLast(requesting);
        }
    }

    private void sendCommandWithLoggedIn(Requesting requesting) {
        Logger.dd(TAG, "Action - sendCommandWithLoggedIn");
        Logger.dd(TAG, "request is " + requesting.toString());
        JRequest request = requesting.request;
        Long rid = request.getRid();

        int cmd = request.getCommand();
        boolean isImPushReceived = false;

        Logger.dd(TAG, "Request params - cmd:" + cmd);
        switch (cmd) {

            case JMessageCommands.IM.CMD:
                if (request.getJuid() != mJuid) {
                    Logger.w(TAG, "unexpected! push hasn't been login before im request, juid maybe is null");
                    Logger.v(TAG, "juid:" + mJuid + ", request.getHead().getJuid():" + request.getJuid());
                    request.setJuid(mJuid);
                }

                if (request.getSid() != mSid) {
                    Logger.w(TAG, "unexpected! this request is out, reassembly request and again");
                    Logger.v(TAG, "sid:" + mSid + ", request.getHead().getSis():" + request.getSid());
                    request.setSid(mSid);
                }

                if (StringUtils.isEmpty(((IMRequest) request).getIMProtocol().getAppKey())) {
                    Logger.w(TAG, "unexpected! appkey is null, reassembly request and again");
                    // TODO: 16/6/15 reset im protocol
                    ((IMRequest) request).setIMProtocol(((IMRequest) request).getIMProtocol().resetProtocol(mAppkey));
                }

                JCoreInterface.sendData(mContext, SdkType.JMESSAGE.name(),JMessageCommands.IM.CMD,((IMRequest) request).writeBodyAndToBytes());
                int imCmd = ((IMRequest) request).getIMProtocol().getCommand();
                // TODO: 16/5/27 一般上行命令,当然可以根据自己的需求来定 , 用来判断是否需要入请求／发送队列
                if (IMServiceHelper.isImPushCommand(imCmd)) {
                    isImPushReceived = true;
                }
                break;

            default:
                Logger.w(TAG, "Unprocessed request yet.");
        }

        if (!isImPushReceived) {
            enqueSentQueue(requesting);
            startSentTimeout(rid);
        } else {
            Logger.v(TAG, "Don't need join the queue for push response");
        }
    }

    private void preProcessRequests(JRequest request) {
        // TODO: 16/5/27 nothing
    }

    private synchronized void enqueSentQueue(Requesting requesting) {
        Logger.d(TAG, "Action - enqueSentQueue");

        // TODO: 需要再考虑,效率问题
        if (isInSentQueue(requesting.request.getRid())) {
            return;
        }

        mSentQueue.offerLast(requesting);
        printSentQueue();
    }

    private synchronized Requesting dequeSentQueue(Long rid) {
        Logger.d(TAG, "Action - dequeSentQueue");

        Requesting found = null;
        for (Requesting requesting : mSentQueue) {
            long requestRid = requesting.request.getRid();
            if (rid.longValue() == requestRid) {
                mSentQueue.remove(requesting);
                found = requesting;
            }
        }

        return found;
    }

    private boolean isInSentQueue(long responseRid) {
        for (Requesting requesting : mSentQueue) {
            if (requesting.request.getRid() == responseRid)
                return true;
        }
        return false;
    }


    private void startRequestTimeout(Requesting requesting) {
        Logger.v(TAG, "Action - startRequestTimeout");
        Long rid = requesting.request.getRid();

        Message message = Message.obtain(mRequestHandler, MSG_REQUEST_TIMEOUT, rid);
        mRequestHandler.sendMessageDelayed(message, requesting.timeout);
    }

    private void endRequestTimeout(Requesting requesting) {
        Logger.v(TAG, "Action - endRequestTimeout");
        Long rid = requesting.request.getRid();

        Requesting succeed = mRequestingCache.remove(rid);
        if (null == succeed) {
            Logger.w(TAG, "Unexpected - failed to remove requesting from cache.");
        }
        mRequestingQueue.remove(requesting);

        mRequestHandler.removeMessages(MSG_REQUEST_TIMEOUT, rid);
    }

    private void startSentTimeout(Long rid) {
        Logger.d(TAG, "Action - startSentTimeout");
        Message message = Message.obtain(mRequestHandler, MSG_SENT_TIMEOUT, rid);
        mRequestHandler.sendMessageDelayed(message, TIMEOUT_SENT - 200);
    }

    private void endSentTimeout(Long rid) {
        Logger.d(TAG, "Action - endSentTimeout - rid:" + rid);
        mRequestHandler.removeMessages(MSG_SENT_TIMEOUT, rid);
    }


    private void onImTimeoutToReceiver(int imCmd, long rid) {
        Logger.d(TAG, "Action - onImTimeoutToReceiver, imCmd:" + imCmd);

        // TODO: 16/5/27 上层相关处理
        Logger.e(TAG, "im request timeout for cmd:" + imCmd);

        switch (imCmd) {
            case IMCommands.Login.CMD:
                // TODO: 16/5/27 登陆失败，应该重置Push的状态
                PluginJCoreHelper.resetPushStatus(mContext);
                break;
            default:
                // TODO: 16/5/27 nothing
        }
        //tcp请求超时处理
        Logger.d(TAG, "request timeout.  cmd = " + imCmd + " rid = " + rid);
        ResponseProcessor.handleIMTimeout(rid);
    }

    private void printSentQueue() {
        if (mSentQueue != null) {
            int size = mSentQueue.size();
            Logger.v(TAG, "Action - printSentQueue - size:" + size);
        }
    }

    private void printRequestingQueue() {
        int size = 0;
        if (mRequestingQueue != null) {
            size = mRequestingQueue.size();
        }

        Logger.v(TAG, "Action - printRequestingQueue - size:" + size);
        int index = 0;
        for (Requesting requesting : mRequestingQueue) {
            index++;
            Logger.v(TAG, index + "/" + size + " - " + requesting.toString());
        }
    }

    private void printRequestingCache() {
        int size = 0;
        if (mRequestingCache != null) {
            size = mRequestingCache.size();
        }
        Logger.v(TAG, "Action - printRequestingCache - size:" + size);
        int index = 0;
        for (Requesting requesting : mRequestingCache.values()) {
            index++;
            Logger.v(TAG, index + "/" + size + " - " + requesting.toString());
        }
    }

    private static class MyHandler extends Handler {
        private WeakReference<RequestingThread> mThread;

        public MyHandler(Looper looper, RequestingThread requestingThread) {
            super(looper);
            mThread = new WeakReference<RequestingThread>(requestingThread);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            Logger.v(TAG, "Handle msg - threadId:" + Thread.currentThread().getId());
            RequestingThread thread = mThread.get();
            if (null == thread) {
                Logger.d(TAG, "When received msg, the instance already gc.");
                return;
            }

            Long rid;
            Requesting requesting;

            switch (msg.what) {
                case MSG_SENT_TIMEOUT:
                    rid = (Long) msg.obj;
                    requesting = thread.mRequestingCache.get(rid);
                    if (null == requesting) {
                        Logger.w(TAG, "Unexpected: no cached request when sent timeout.");
                        return;
                    }
                    thread.onSentTimeout(requesting);
                    break;

                case MSG_REQUEST_TIMEOUT:
                    rid = (Long) msg.obj;
                    requesting = thread.mRequestingCache.get(rid);
                    if (null == requesting) {
                        Logger.w(TAG, "Unexpected - not found request in cache.");
                        break;
                    }
                    thread.onRequestTimeout(requesting);
                    break;

                case MSG_REQUEST:
                    if (null == msg.obj) {
                        Logger.ww(TAG, "Unexpected - want to send null request.");
                    } else {
                        thread.sendRequestInternal((JRequest) msg.obj, msg.arg1);
                    }
                    break;

                case MSG_RESPONSE:
                    thread.handleResponseInternal(0l, msg.obj);
                    break;

                case MSG_SEND_OLD_QUEUE:
                    thread.resendRequestingQueue();
                    break;

                default:
                    Logger.w(TAG, "Unhandled msg - " + msg.what);
            }
        }
    }

    private static class Requesting {
        JRequest request;
        int timeout;
        int times = 0;

        public Requesting(JRequest request, int timeout) {
            this.request = request;
            this.timeout = timeout;
        }

        public void retryAgain() {
            timeout = timeout - TIMEOUT_SENT;
            times++;
        }

        public String toString() {
            return "[Requesting] - timeout:" + timeout
                    + ", times:" + times +
                    ", request:" + request.toString();
        }
    }

    private static final int MSG_REQUEST = 7401;
    private static final int MSG_RESPONSE = 7402;
    private static final int MSG_REQUEST_TIMEOUT = 7403;
    private static final int MSG_SENT_TIMEOUT = 7404;

    private static final int MSG_SEND_OLD_QUEUE = 7405;


}
