package cn.jpush.im.android.pushcommon.helper;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jiguang.ald.api.JResponse;
import cn.jiguang.ald.api.SdkType;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.pushcommon.proto.Im;
import cn.jpush.im.android.pushcommon.proto.JMessageCommands;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.common.commands.IMRequest;
import cn.jpush.im.android.pushcommon.proto.common.commands.IMResponse;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMCommands;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ImBaseRequest;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

/**
 * Created by zhangfl on 16/5/27.
 */
public class IMServiceHelper {

    private static final String TAG = "IMServiceHelper";

    public static boolean isImPushCommand(int imCmd) {
        if (imCmd == IMCommands.ChatMsgSync.CMD
                || imCmd == IMCommands.EventNotification.CMD
                || imCmd == IMCommands.EventSync.CMD
                || imCmd == IMCommands.SyncConversationACK.CMD
                || imCmd == IMCommands.SyncEventACK.CMD
                || imCmd == IMCommands.SyncMsgReceiptACK.CMD
                || imCmd == IMCommands.UpdateUnreadCount.CMD) {
            return true;
        }
        return false;
    }

    public static long getNextRid() {
        return JCoreInterface.getNextRid();
    }

    private static RequestingThread mRequestingThread = null;
    private static MyHandler mHandler = null;

    public static void imRequest(Context context, ImBaseRequest request) {
        Logger.i(TAG, "action - imRequest");
        if (null == context) {
            Logger.e(TAG, "unexpected, context is null");
            return;
        }

        if (null == mRequestingThread) {
            mRequestingThread = new RequestingThread(context);
        }
        if (null == mHandler) {
            mHandler = new MyHandler(context, Looper.getMainLooper());
        }

        // TODO: 16/6/15 如果appkey为null,  是否应该终止请求...
        if (StringUtils.isEmpty(JCoreInterface.getAppKey())) {
            Logger.ww(TAG, "unexpected! appKey is null, please check your manifest");
            return;
        }

        //这里toJson仅仅是为了打印request中的参数
        String json = request.toJson();
        int imCmd = request.getCmd();
        if (null == json || 0 == imCmd) {
            Logger.ee(TAG, "Unexpected - illegal IM request.");
            return;
        }
        Logger.d(TAG, "imCmd:" + imCmd + ", request:" + json);

        IMRequest imRequest = request.toProtocolBuffer(JCoreInterface.getAppKey());
        // request的toProtocolBuffer(long imUid, String appKey);会返回null，当其准备返回的IMProtocol所构建的Packet字节数大于7168时。
        if (imRequest.getIMProtocol() == null) {
            Logger.ee(TAG, "protocol is null, maybe caused by invalid Request parameter");
            CommonUtils.doCompleteCallBackToUser(request.getCallback(), ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }
        imRequest.setSid(JCoreInterface.getSid());
        imRequest.setJuid(JCoreInterface.getUid());

        int timeout = 1000 * 60;       // 60s
        if (imCmd == IMCommands.SingleMsg.CMD
                || imCmd == IMCommands.GroupMsg.CMD) {
            timeout = 1000 * 60 * 5;    // 5min
        }
        if (imCmd == IMCommands.Login.CMD) {
            // TODO: 16/5/26 设置标志，用来保存push的状态，在IM-Logout成功抑或是IM-Login失败的时候要恢复
            //TODO:: lee 待讨论的问题，此时是否可以直接调用JCore的
            JCoreInterface.restart(context, SdkType.JMESSAGE.name(), new Bundle(), false);
//            ServiceInterface.imLoginPrepareRequest(context);
        }
        mRequestingThread.sendRequest(imRequest, timeout);
    }


    public static void handlePushLogin(Context context, Bundle bundle) {
        if (null == mHandler) {
            mHandler = new MyHandler(context, Looper.getMainLooper());
        }
        android.os.Message.obtain(mHandler, IMResponseHelper.MSG_PUSH_LOGIN, bundle).sendToTarget();
    }

    public static void handlePushLogout(Context context, Bundle bundle) {
        if (null == mHandler) {
            mHandler = new MyHandler(context, Looper.getMainLooper());
        }
        android.os.Message.obtain(mHandler, IMResponseHelper.MSG_PUSH_LOGOUT, bundle).sendToTarget();
    }

    static void handleImResponsePreExecute(Context context, JResponse response) {
        if (null == mHandler) {
            mHandler = new MyHandler(context, Looper.getMainLooper());
        }

        IMResponse imResponse = (IMResponse) response;
        IMProtocol imProtocol = imResponse.getIMProtocol();
        if (null == imProtocol) {
            Logger.e(TAG, "imProtocol is null, maybe caused by error IM cmd in IMProtocol(byte[] data)");
            return;
        }
        int imCMD = imProtocol.getCommand();
        long rid = imResponse.getRid();
        Logger.d(TAG, "Action - handleImResponsePreExecute - imCmd:" + imCMD + ", rid:" + rid);

        // TODO: 16/5/26 到requestingThread中进行处理，清理相关的队列
        // 应先清楚哪些命令需要进行下述动作...  在请求的时候如果入请求／发送队列 就需要
        if (!IMServiceHelper.isImPushCommand(imCMD)) {
            android.os.Message.obtain(mHandler,
                    IMResponseHelper.MSG_ON_RESPONSE,
                    response).sendToTarget();
        }

        switch (imCMD) {
            case IMCommands.Login.CMD:
                Im.Response resp = imProtocol.getResponse();
                if (resp != null && resp.getCode() == 0) {
                    Logger.d(TAG, "IM login success!");
                    PluginJCoreHelper.setImLogStatus(context, true);
                } else {
                    Logger.d(TAG, "IM login failed!");
                    PluginJCoreHelper.setImLogStatus(context, false);
                    // TODO: 16/6/3 登录失败应重置Push的状态
                    PluginJCoreHelper.resetPushStatus(context);
                }
                break;

            case IMCommands.Logout.CMD:
                int code = imProtocol.getResponse().getCode();
                if (code == 0) {
                    Logger.d(TAG, "IM logout success");
                    PluginJCoreHelper.setImLogStatus(context, false);

                    // TODO: 16/5/24 登出成功应重置Push状态
                    PluginJCoreHelper.resetPushStatus(context);
                } else {
                    Logger.e(TAG, "IM logout failed");
                }
                break;

//            case IMCommands.EventNotification.CMD:
//                // TODO: 16/8/11 当消息正常入库之后才能发送recv
//                sendEventBack(context, rid, imProtocol);
//                break;
//
//            case IMCommands.ChatMsgSync.CMD:
//                //jmessage 2.1.0版本之后收到消息不需要发送recv
//                //sendChatMsgSyncBack(mHandler, rid, imProtocol);
//                break;

            default:
                // do nothing
        }

    }

    //sdk收到事件响应给后台
    static void sendEventBack(Context context, long rid, IMProtocol imProtocol) {
        if (!CommonUtils.isLogin("sendEventBack")) {
            Logger.d(TAG, "not login yet, give up to send event back.");
            return;
        }

        if (null == mHandler) {
            mHandler = new MyHandler(context, Looper.getMainLooper());
        }

        Message.EventNotification event = (Message.EventNotification) imProtocol.getEntity();
        Logger.d(TAG, "Action - sendEventBack - rid:" + rid + ", eventId:" + event.getEventId());

        Message.EventNotification respEvent = Message.EventNotification.newBuilder()
                .setEventId(event.getEventId())
                .setEventType(event.getEventType())
                .setFromUid(event.getFromUid())
                .setGid(event.getGid())
                .build();
        imProtocol.setEntity(respEvent);
        IMRequest request = new IMRequest(rid, imProtocol);
        request.setSid(JCoreInterface.getSid());
        request.setJuid(JCoreInterface.getUid());
        android.os.Message.obtain(mHandler, IMResponseHelper.MSG_EVENT_NOTIFICATION_BACK, request).sendToTarget();
    }

//    private static void sendChatMsgSyncBack(Handler mainHandler, long rid, IMProtocol imProtocol) {
//        Logger.d(TAG, "Action - sendChatMsgSyncBack - rid:" + rid);
//        Message.ChatMsgSync chatMsgSync = (Message.ChatMsgSync) imProtocol.getEntity();
//        Message.ChatMsgSync.Builder respChatMsgSyncBuilder = Message.ChatMsgSync.newBuilder();
//        for (Message.ChatMsg msg : chatMsgSync.getChatMsgList()) {
//            Logger.v(TAG, "ChatMsg Received - msgId:" + msg.getMsgid());
//            respChatMsgSyncBuilder.addChatMsg(Message.ChatMsg.newBuilder()
//                    .setMsgid(msg.getMsgid())
//                    .setMsgType(msg.getMsgType())
//                    .setFromUid(msg.getFromUid())
//                    .setFromGid(msg.getFromGid())
//                    .build());
//        }
//        imProtocol.setEntity(respChatMsgSyncBuilder.build());
//        IMRequest request = new IMRequest(rid, imProtocol);
//        request.setSid(Configs.getSid());
//        request.setJuid(Configs.getUid());
//        android.os.Message.obtain(mainHandler, IMResponseHelper.MSG_CHAT_MSG_SYNC_BACK, request).sendToTarget();
//    }


    // TODO: 16/6/6 在生命周期比较长的组建中使用,,,eg:service
    private static class MyHandler extends Handler {

        private Context mContext;

        public MyHandler(Context context, Looper looper) {
            super(looper);
            this.mContext = context;

            if (null == mRequestingThread) {
                mRequestingThread = new RequestingThread(context);
            }
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);

            Logger.v(TAG, "handleMessage:" + msg.toString());

            switch (msg.what) {
                case IMResponseHelper.MSG_CHAT_MSG_SYNC_BACK:
//                    JCoreInterface.sendData(mContext,SdkType.JMESSAGE.name(), JMessageCommands.IM.CMD,((IMRequest) msg.obj).writeBodyAndToBytes());
                    break;
                case IMResponseHelper.MSG_EVENT_NOTIFICATION_BACK:

                    JCoreInterface.sendData(mContext, SdkType.JMESSAGE.name(), JMessageCommands.IM.CMD, ((IMRequest) msg.obj).writeBodyAndToBytes());
//                    ServiceInterface.imRequest(mContext, ((IMRequest) msg.obj).writeBodyAndToBytes());
                    break;
                case IMResponseHelper.MSG_ON_RESPONSE:
                    // TODO: 16/5/26 长连接线程返回的命令，进入发送线程做后续队列处理
                    mRequestingThread.handleResponse(0l, msg.obj);
                    break;

                case IMResponseHelper.MSG_PUSH_LOGIN:
                    mRequestingThread.onLoggedIn();
                    break;
                case IMResponseHelper.MSG_PUSH_LOGOUT:
                    mRequestingThread.onDisconnected();
                    break;
                default:
                    Logger.w(TAG, "Unexpected: unhandled msg - " + msg.what);
            }
        }
    }

}
