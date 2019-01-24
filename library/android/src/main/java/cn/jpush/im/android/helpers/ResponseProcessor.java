package cn.jpush.im.android.helpers;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import cn.jiguang.ald.api.JResponse;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.CommandNotificationEvent;
import cn.jpush.im.android.api.event.NotificationClickEvent;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.helpers.eventsync.GeneralEventsWrapper;
import cn.jpush.im.android.helpers.sync.SyncConvRespHandler;
import cn.jpush.im.android.helpers.sync.SyncEventRespHandler;
import cn.jpush.im.android.helpers.sync.SyncReceiptRespHandler;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.pushcommon.helper.IMResponseHelper;
import cn.jpush.im.android.pushcommon.proto.Im;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.pushcommon.proto.common.commands.IMResponse;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMCommands;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ImBaseRequest;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;


public class ResponseProcessor {
    private static final String TAG = "ResponseProcessor";
    private static final int RES_INDEX_UNRECEIPTCNT = 0;

    public static final int CONVTYPE_SINGLE = 3;
    public static final int CONVTYPE_GROUP = 4;
    static List<Long> msgIDCacheList = new ArrayList<Long>();

    public static void handleIMTimeout(long rid) {
        final ImBaseRequest request = RequestProcessor.requestsCache.get(rid);
        if (null == request) {
            Logger.d(TAG, "request is null!");
            return;
        }

        request.onResponseTimeout();
    }

    static void handleIMResponse(Context context, int cmd, byte[] head, byte[] body) {
        //根据收到的响应,转到单独的线程中对不同的命令字做相关的后续处理...
        IMResponse response = dataToIMResponse(cmd, head, body);
        IMResponseHelper.handleImResponsePreExecute(context.getApplicationContext(),
                response);
        //这里需要重新parse一遍因为在handleImResponse里会把response里的entity对象改变。

        if (null == response || response.code != 0) {
            Logger.d(TAG, "jpush response return error! response = " + response);
            return;
        }

        long seqID = response.getRid();
        IMProtocol imProtocol = response.getIMProtocol();
        if (null == imProtocol) {//增加对protobuf解析时的容错处理。jira: http://jira.jpushoa.com/browse/IM-1426
            Logger.ww(TAG, "parse im protocol failed. return from handleIMResponse");
            return;
        }
        Logger.d(TAG, "imProtocol:" + imProtocol.toString());
        int imCMD = imProtocol.getCommand();
        Logger.d(TAG, "IMCMD:" + imCMD);

        if (imCMD != IMCommands.Login.CMD && !CommonUtils.isLogin("handleIMResponse")) {
            //当imCmd不是login，而且当前im处在未登录的状态下时，忽略所有收到的响应。
            return;
        }

        switch (imCMD) {
            case IMCommands.ChatMsgSync.CMD:
                handleMessageReceived(imProtocol);
                break;
            case IMCommands.EventNotification.CMD:
                Message.EventNotification eventNotification = (Message.EventNotification) imProtocol.getEntity();
                //仅当eventType == 1时，才会将事件resp发送回给服务器
                if (GeneralEventsWrapper.EVENT_LOGOUT == eventNotification.getEventType()) {
                    //顶掉登录
                    if (eventNotification.getExtra() == 0){
                        Intent intent1 = new Intent();
                        intent1.setAction("cn.citytag.mapgo.action.logout");
                        intent1.putExtra("type", 1);
                        LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent1);
                    }
                    //重新生成一个IMResponse实体，因为send event back过程中会改变improtocol中的entity属性，可能会影响上层的异步数据处理
                    IMResponse sendBackResponse = dataToIMResponse(cmd, head, body);
                    if (null != sendBackResponse) {
                        IMResponseHelper.handleImResponseSendEventBack(context, seqID, sendBackResponse.getIMProtocol());
                    }
                }
                //封禁IP 退出登录
                else if (GeneralEventsWrapper.EVENT_FORBIDDEN_USER == eventNotification.getEventType()){
                    Intent intent1 = new Intent();
                    intent1.setAction("cn.citytag.mapgo.action.logout");
                    intent1.putExtra("type", 0);
                    LocalBroadcastManager.getInstance(context.getApplicationContext()).sendBroadcast(intent1);
                }
                EventProcessor.getInstance().enqueueEvent(eventNotification);
                break;
            case IMCommands.SyncCheck.CMD:
                Logger.d(TAG, "received a sync check response.");
                break;
            case IMCommands.SyncConversationResp.CMD:
                SyncConvRespHandler.getInstance().pageReceived(imProtocol);
                break;
            case IMCommands.SyncEventResp.CMD:
                SyncEventRespHandler.getInstance().pageReceived(imProtocol);
                break;
            case IMCommands.SyncMsgReceiptResp.CMD:
                SyncReceiptRespHandler.getInstance().pageReceived(imProtocol);
                break;
            case IMCommands.MsgReceiptChange.CMD:
                handleMsgReceiptChange(imProtocol);
                break;
            case IMCommands.CommandNotification.CMD:
                handleCommandNotification(imProtocol);
                break;
            default:
                handleOtherResponse(seqID, imProtocol);
        }
    }

    private static void handleOtherResponse(long seqID, IMProtocol imProtocol) {
        ImBaseRequest request = RequestProcessor.requestsCache.get(seqID);
        if (null == request) {
            Logger.d(TAG, "cached request not exist! return from receiver!");
            return;
        }
        request.imProtocolReceived(imProtocol);
    }

    private static IMResponse dataToIMResponse(int cmd, byte[] head, byte[] body) {
        if (null == head || null == body) {
            Logger.ww(TAG, "receive data is null, handle IM response error.");
            return null;
        }
        JResponse response = IMResponseHelper.parseResponseInbound(cmd, head, body);
        if (null == response) {
            Logger.ww(TAG, "JResponse is null , return from dataToProtocol");
            return null;
        }
        Logger.d(TAG, "response:" + response.toString());
        return (IMResponse) response;
    }

    private static void handleMessageReceived(IMProtocol imProtocol) {
        if (!CommonUtils.isLogin("OnReceiveBroadcast")) {
            Logger.ww(TAG, "received im response but user not login yet,discard this message");
            return;
        }
        Message.ChatMsgSync entity = (Message.ChatMsgSync) imProtocol.getEntity();
        Im.ProtocolHead head = imProtocol.getProtocolHead();
        int unreceiptCnt = 0;
        Logger.d(TAG, " message received . get cookie.res.count = " + head.getCookie().getResCount());
        if (0 < head.getCookie().getResCount()) {
            unreceiptCnt = head.getCookie().getRes(RES_INDEX_UNRECEIPTCNT);
        }
        for (final Message.ChatMsg chatMsg : entity.getChatMsgList()) {
            Logger.ii(TAG, " messageID = " + chatMsg.getMsgid());
            if (msgIDCacheList.contains(chatMsg.getMsgid())) {
                Logger.d(TAG, "received a duplicate msgID from server , abort it!msgID = "
                        + chatMsg.getMsgid());
                return;
            }
            msgIDCacheList.add(chatMsg.getMsgid());
            ChatMsgManager.getInstance().parseInbackground(chatMsg, unreceiptCnt);
        }
    }

    private static void handleMsgReceiptChange(IMProtocol imProtocol) {
        final Receipt.MsgReceiptChange entity = (Receipt.MsgReceiptChange) imProtocol.getEntity();
        long target = entity.getConTarget();//回执发起方id：
        // 单聊为对方uid，这个ReceiptChange的接受者必然是消息发送方，所以这里只需要一个对方uid就能确定会话。
        // 群聊为gid.
        final Long finalTarget = target;
        if (CONVTYPE_SINGLE == entity.getMsgType()) {
            UserIDHelper.getUsername(target, new UserIDHelper.GetUsernamesCallback() {
                @Override
                public void gotResult(int code, String msg, List<String> usernames) {
                    if (ErrorCode.NO_ERROR == code && !usernames.isEmpty()) {
                        String username = usernames.get(0);
                        String appkey = UserIDHelper.getUserAppkeyFromLocal(finalTarget);
                        InternalConversation internalConv = ConversationManager.getInstance().getSingleConversation(username, appkey);
                        if (null != internalConv) {
                            internalConv.updateMessageUnreceiptCntInBatch(entity.getMetaListList());
                        }
                    }
                }
            });
        } else if (CONVTYPE_GROUP == entity.getMsgType()) {
            InternalConversation internalConv = ConversationManager.getInstance().getGroupConversation(target);
            if (null != internalConv) {
                internalConv.updateMessageUnreceiptCntInBatch(entity.getMetaListList());
            }
        }
    }

    private static void handleCommandNotification(IMProtocol imProtocol) {
        Message.CommandNotification entity = (Message.CommandNotification) imProtocol.getEntity();
        long senderUID = entity.getSender();
        long targetID = entity.getTarget();
        int type = entity.getType();
        String cmd = entity.getCmd().toStringUtf8();
        CommandNotificationEvent commandNotificationEvent = new CommandNotificationEvent(senderUID, targetID, CommandNotificationEvent.Type.get(type), cmd);
        EventBus.getDefault().post(commandNotificationEvent);
    }

    static void handleNotificationIntent(final Context context, final Intent intent) {
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                String targetID = intent.getStringExtra(ChatMsgManager.TARGET_ID);
                String targetAppkey = intent.getStringExtra(ChatMsgManager.TARGET_APPKEY);
                String convType = intent.getStringExtra(ChatMsgManager.CONVERSATION_TYPE);
                long serverMsgId = intent.getLongExtra(ChatMsgManager.SERVER_MSG_ID, -1);
                InternalConversation conv = ConversationManager.getInstance().getConversation(ConversationType.valueOf(convType), targetID, targetAppkey);
                if (null != conv) {
                    conv.resetUnreadCount();
                    if (EventBus.getDefault().hasSubscriberForEvent(NotificationClickEvent.class)) {
                        cn.jpush.im.android.api.model.Message msgToPost = conv.getMessage(serverMsgId);
                        //这里有可能此时通过serverMsgId拿到的msg是空，因为上抛事件和消息入库是两个异步的过程，如果此时消息还没入库，通过serverMsgId那到的消息就是null.
                        if (null == msgToPost) {
                            msgToPost = conv.getLatestMessage();
                        }
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
                        launchIntent.putExtra("r_push", true);
                        launchIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                        context.startActivity(launchIntent);
                    } else {
                        Logger.dd(TAG, "cannot find subscriber for NotificationClickEvent,try start default launch intent!");
                        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(
                                context.getPackageName());
                        startActivitySafety(context, launchIntent);
                    }
                } else {
                    Logger.w(TAG, "conversation is null! start activity failed!");
                }
                return null;
            }
        });
    }

    private static void startActivitySafety(Context context, Intent intent) {
        try {
            if (null != intent) {
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                context.startActivity(intent);
            } else {
                Logger.ee(TAG, "default launch intent is null! start failed!");
            }
        } catch (ActivityNotFoundException anfe) {
            Logger.ee(TAG, "start activity failed ! due to activity not found exception!");
        }
    }
}
