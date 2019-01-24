package cn.jpush.im.android.common;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.text.TextUtils;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.content.MediaContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetGroupInfoTask;
import cn.jpush.im.android.tasks.GetGroupMembersTask;
import cn.jpush.im.android.tasks.GetUserInfoTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.MessageProtocolParser;
import cn.jpush.im.android.utils.filemng.FileDownloader;

public class ChatMsgManager {

    private static final String TAG = "ChatMsgManager";

    private static final long NOTIFY_TIME_INTERVAL = 5 * 1000L;
    public static final String CONVERSATION_TYPE = "conv_type";
    public static final String TARGET_ID = "target_id";
    public static final String TARGET_APPKEY = "target_appkey";
    public static final String SERVER_MSG_ID = "server_msg_id";

    private static ChatMsgManager mInstance;

    private NotificationManager mNotificationManager;

    private ObjectQueue msgQueue;

    private ApplicationInfo info;

    private AtomicBoolean readyToNotify = new AtomicBoolean(false);

    private long preNotifyTime = 0;

    private class EnqueuedMsg {
        public InternalMessage msg;
        boolean sendNotify;
        boolean sendOnlineMsgEvent;

        public EnqueuedMsg(InternalMessage msg, boolean sendNotify, boolean sendOnlineMsgEvent) {
            this.msg = msg;
            this.sendNotify = sendNotify;
            this.sendOnlineMsgEvent = sendOnlineMsgEvent;
        }
    }

    private ChatMsgManager() {
        msgQueue = new ObjectQueue();
        mNotificationManager = (NotificationManager) JMessage.mContext
                .getSystemService(Context.NOTIFICATION_SERVICE);
        info = JMessage.mContext.getApplicationInfo();
    }

    public synchronized static ChatMsgManager getInstance() {
        if (null == mInstance) {
            mInstance = new ChatMsgManager();
        }
        return mInstance;
    }

    private void enqueueOrExecute(InternalMessage msg, boolean needNotify, boolean sendOnlineMsgEvent) {
        if (!readyToNotify.get()) {
            //免打扰列表信息还未准备好，所有进入队列等待。
            Logger.d(TAG, "notify manager not ready yet, msg enqueue. msgQueue cnt = " + msgQueue.m_nItemCnt);
            msgQueue.putTail(new EnqueuedMsg(msg, needNotify, sendOnlineMsgEvent));
        } else {
            //免打扰列表已准备好，直接发送通知和event.
            Logger.d(TAG, "notify manager ready, send notify to user");
            parseMessagePostExecute(msg, needNotify, sendOnlineMsgEvent);
        }
    }

    //清掉所有notification，重置状态到not ready,清空缓存队列。
    public void reset() {
        cancelAllNotification();
        readyToNotify.set(false);
        msgQueue.clear();
    }

    //将manager状态置为ready,开始正常处理消息，并且消费队列中缓存的消息。
    public void ready() {
        readyToNotify.set(true);
        if (readyToNotify.get()) {
            //免打扰列表已准备好，开始消费消息队列中的消息
            Logger.d(TAG, "ready to notify. msgQueue cnt = " + msgQueue.m_nItemCnt);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    while (msgQueue.hasItem()) {
                        Logger.d(TAG, "begin to consume. now cnt = " + msgQueue.m_nItemCnt);
                        EnqueuedMsg enqueuedMsg = (EnqueuedMsg) msgQueue.consume();
                        parseMessagePostExecute(enqueuedMsg.msg, enqueuedMsg.sendNotify, enqueuedMsg.sendOnlineMsgEvent);
                    }
                }
            }).start();
        }
    }

    private void sendNotification(InternalMessage msg) {
        long curNotifyTime = System.currentTimeMillis();
        int notiFlag = IMConfigs.getNotificationFlag();
        if (Math.abs(curNotifyTime - preNotifyTime) < NOTIFY_TIME_INTERVAL) {
            //为了防止连续收到消息时，频繁震动和声音。这里加一个间隔时间
            notiFlag = JMessageClient.FLAG_NOTIFY_SILENCE;
        } else {
            preNotifyTime = curNotifyTime;
        }
        Notification noti = createNotification(msg, notiFlag);
        if (null != noti) {
            mNotificationManager.notify((msg.getTargetID() + msg.getTargetAppKey()).hashCode(), noti);
        }
    }

    public void updateNotification(InternalMessage msgToSend) {
        if (null != msgToSend) {
            tryToNotify(msgToSend);
        }
    }

    public void cancelNotification(String targetID, String appkey) {
        if (null != targetID && null != appkey) {
            mNotificationManager.cancel((targetID + appkey).hashCode());
        }
    }

    private void cancelAllNotification() {
        mNotificationManager.cancelAll();
    }


    /**
     * 将proto中chatMsg对象 parse成im业务中的InternalMessage对象。
     * <p>
     * 注意这里处理的都是在线消息所以都需要发送通知、 发送在线消息事件
     *
     * @param chatMsg proto中chatMsg对象
     * @return
     */
    private InternalMessage parseSync(final Message.ChatMsg chatMsg, int unreceiptCnt) {
        if (!CommonUtils.isLogin("parseSync")) {
            Logger.ww(TAG, "parse chat msg failed. user not logged in .");
            return null;
        }

        Message.MessageContent content = chatMsg.getContent();
        Logger.d(TAG,
                "ChatMsgSync messageContent = " + content.getContent().toStringUtf8());
        Logger.d(TAG, "chatMsg no notification = " + chatMsg.getAction().getNoNotification() +
                " no offline = " + chatMsg.getAction().getNoOffline() + " retract = " + chatMsg.getAction().getMsgRetract());
        InternalMessage msg = (InternalMessage) MessageProtocolParser
                .protocolToMessage(chatMsg, unreceiptCnt);

        if (!startDownloadFile(msg, true, true, false)) {
            //如果没有启动下载，直接走之后的处理逻辑
            enqueueOrExecute(msg, true, true);
        }
        return msg;
    }


    /**
     * 批量将proto中chatMsg对象 parse成im业务中的InternalMessage对象。
     * 注意这个方法处理的都是离线消息，所以解析完成后需要根据不同策略发送通知、事件、以及更新未读数。具体策略如下：
     * <p>
     * 1 离线消息中new list部分: 对于received的消息需要发送通知、不发送在线消息事件MessageEvent(应该发送离线消息事件OfflineMessageEvent)
     * <p>
     * 2 离线消息中old list部分: 不发送通知、不发送在线消息事件
     *
     * @return
     */
    public Collection<InternalMessage> parseSyncInBatch(long uid, final List<Message.ChatMsg> chatMsgs, Map<Long, Receipt.MsgReceiptMeta> metaMap,
                                                        Collection<Long> newList, Collection<Long> oldList) {
        if (!CommonUtils.isLogin("parseSyncInBatch")) {
            Logger.ww(TAG, "parse in batch failed. user not logged in .");
            return null;
        }
        List<InternalMessage> messages = MessageProtocolParser.saveChatMsgToLocalInBatch(uid, chatMsgs, metaMap, newList, oldList);
        if (null == messages) {
            return null;
        }
        Object[] messageArray = messages.toArray();
        boolean isLastOne = false;
        boolean shouldDownload;
        boolean needNotify = !newList.isEmpty();
        InternalMessage msgToSendNotify = null;
        int size = messageArray.length;
        for (int i = 0; i < size; i++) {
            InternalMessage msg = (InternalMessage) messageArray[i];

            if (msg.getMessageSendingOptions().isShowNotification()) {
                msgToSendNotify = msg;
            }

            //消息列表默认按创建时间升序排列，这里只针对最新20条媒体消息才需要下载附件。
            //相关设计文档见wiki: http://wiki.jpushoa.com/pages/editpage.action?pageId=12691041
            shouldDownload = (size <= 20 || size - 20 <= i) && (msg.getContent() instanceof MediaContent);
            if (size - 1 == i) {
                isLastOne = true;
            }

            if (shouldDownload) {
                //批量处理消息时为了优化处理效率，仅当批处理到最后一条消息时，才发送尝试发送通知栏通知。
                startDownloadFile(msg, needNotify && isLastOne, false, true);
            } else if (needNotify && isLastOne) { //这里这个'tryToNotify && isLastOne'判断要放在enqueueOrExecute之前做，如果走到enqueueOrExecute里面就有可能启动userinfo/groupinfo获取的网络任务，在批量消息获取情况下，这个动作会造成大量重复获取的网络任务阻塞网络执行线程池
                enqueueOrExecute(msgToSendNotify, true, false);
            }
        }

        return messages;
    }

    public void parseInbackground(final Message.ChatMsg chatMsg, final int unreceiptCnt) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                parseSync(chatMsg, unreceiptCnt);
                return null;
            }
        });
    }

    private boolean startDownloadFile(InternalMessage msg,
                                      boolean sendNotify, boolean sendOnlineMsgEvent, boolean downloadInOtherThread) {
        boolean isStarted = false;
        // 开始下载对应的文件
        FileDownloader dm = new FileDownloader();
        switch (msg.getContentType()) {
            case image:
                // 如果是图片消息，则自动下载缩略图，下载完成后再给用户发送广播。
                dm.downloadThumbnailImage(msg, new FileDownloadCompletionCallback(msg, sendNotify, sendOnlineMsgEvent), downloadInOtherThread);
                isStarted = true;
                break;
            case voice:
                dm.downloadVoiceFile(msg, new FileDownloadCompletionCallback(msg, sendNotify, sendOnlineMsgEvent), downloadInOtherThread);
                isStarted = true;
                break;
            default:
                break;
        }
        return isStarted;
    }

    private class FileDownloadCompletionCallback extends DownloadCompletionCallback {

        private InternalMessage msg;

        private boolean sendNotify = true;

        private boolean sendOnlineMsgEvent = true;

        public FileDownloadCompletionCallback(InternalMessage message, boolean sendNotify, boolean sendOnlineMsgEvent) {
            super(false);
            msg = message;
            this.sendNotify = sendNotify;
            this.sendOnlineMsgEvent = sendOnlineMsgEvent;
        }

        @Override
        public void onComplete(int responseCode, String responseMessage, File file) {
            String targetID = msg.getTargetID();
            ConversationType conversationType = msg.getTargetType();
            InternalConversation conv = ConversationManager.getInstance().getConversation(conversationType, targetID, msg.getTargetAppKey());
            if (null == conv) {
                Logger.w(TAG, "conversation is null!return from FileDownloadCompletionCallback");
                return;
            }
            if (sendNotify) {
                enqueueOrExecute(msg, true, sendOnlineMsgEvent);
            }
        }
    }

    private void parseMessagePostExecute(InternalMessage msg, boolean needNotify, boolean sendOnlineMsgEvent) {
        if (null == msg) {
            Logger.ii(TAG, "msg to send notify is null,no need to send notify.");
            return;
        }

        InternalConversation conv = ConversationManager.getInstance()
                .getConversation(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey());
        if (null != conv) {
            //需要首先判断消息所属会话对应的相关信息是否完整，如果不完整则获取一遍信息。
            switch (conv.getType()) {
                case group:
                    //如果是群消息，先检查groupinfo是否存在，若不存在先拉取群消息
                    long groupID;
                    try {
                        groupID = Long.parseLong(conv.getTargetId());
                    } catch (NumberFormatException e) {
                        Logger.ee(TAG,
                                "JMessage catch a number format exception,maybe your conversation's target_id is 'String' while conversation_type is 'group'.");
                        return;
                    }
                    InternalGroupInfo info = GroupStorage.queryInfoSync(groupID);
                    if (null == info) {
                        new GetGroupInfoTask(groupID, null, false, false, false).execute();//这里不触发获取群头像
                    } else {
                        Set<Long> memberNames = info.getGroupMemberUserIds();
                        if (null == memberNames || memberNames.isEmpty()) {
                            new GetGroupMembersTask(groupID, null, false, true).execute();
                        }
                    }
                    break;
                case single:
                    if (null == UserInfoManager.getInstance().getUserInfo(conv.getTargetId(), conv.getTargetAppKey())) {
                        //收到消息时本地还没有对应用户信息，需要先获取用户信息。
                        new GetUserInfoTask(conv.getTargetId(), conv.getTargetAppKey(), null, false, false).execute();
                    }
                    break;
            }


            if (sendOnlineMsgEvent) {
                //检查消息发送者的userinfo是否过期
                UserInfo fromUserInfo = msg.getFromUser();
                if (null != fromUserInfo && msg.getSenderUserInfoMTime() > fromUserInfo.getmTime()) {
                    Logger.d(TAG, "msg sender`s userinfo outdated, need update");
                    //msg中本地缓存的fromUserInfo已过期，需要更新。
                    new GetUserInfoTask(fromUserInfo.getUserID(), null, true, false).execute();
                }

                //只在收到在线消息时，才将这个消息插入onlineMsgTable中去。
                conv.insertToOnlineMsgTable(msg.getServerMessageId(), msg.getCreateTime());
                sendOnlineMsgEvent(msg);
            }

            //只有当需要、并且这条消息是接收到的消息时，才发送通知。
            //因为有可能消息同步下来的消息是发送的消息，这时也不需要发送通知
            if (needNotify && msg.getDirect() == MessageDirect.receive) {
                tryToNotify(msg);
            }
        } else {
            Logger.d(TAG, "parseMessagePostExecute user failed. conversation is null");
        }
    }

    public void sendOnlineMsgEvent(cn.jpush.im.android.api.model.Message msg) {
        MessageEvent event = new MessageEvent(ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, msg);
        EventBus.getDefault().post(event);
    }

    //发送通知栏通知，这里要经过多重的判断最终才能决定是否需要发送通知
    private void tryToNotify(InternalMessage msg) {
        boolean noDisrurbGlobalFlag = (1 == IMConfigs.getNodisturbGlobal());//用户是否设置了全局免打扰？
        ConversationType targetType = msg.getTargetType();
        String target;
        boolean noDisturbFlag;//用户是否将对方设置为了免打扰？
        if (ConversationType.single == targetType) {
            target = msg.getTargetID() + msg.getTargetAppKey();
            noDisturbFlag = (1 == ((UserInfo) msg.getTargetInfo()).getNoDisturb());
        } else {
            target = msg.getTargetID();
            noDisturbFlag = (1 == ((GroupInfo) msg.getTargetInfo()).getNoDisturb());
        }

        boolean showNotificationFlag = true;
        if (null != msg.getMessageSendingOptions()) {
            showNotificationFlag = msg.getMessageSendingOptions().isShowNotification();//这条消息是否被设置为"免通知"？
        }
        boolean chattingTargetFlag = target.equals(JMessage.sChattingTarget);//消息对方是否被设置为当前聊天对象？
        boolean notiFlagDisabled = (0 != (IMConfigs.getNotificationFlag() & JMessageClient.FLAG_NOTIFY_DISABLE));//当前用户的设置中，是否将通知栏禁用了？
        boolean isCustomMsg = msg.getContentType() == ContentType.custom;//这条消息是否是自定义消息？
        boolean isCustomNotificationEnabled = (null != msg.getMessageSendingOptions() && msg.getMessageSendingOptions().isCustomNotficationEnabled());//消息发送方是否有自定义通知栏内容？

        //判断是否展示通知的条件：
        boolean sendNotify = !noDisrurbGlobalFlag//用户没有开启全局免打扰
                && !noDisturbFlag//用户没有将对方加入免打扰列表
                && showNotificationFlag//发送方没有将这条消息设置为"免通知"
                && !chattingTargetFlag//UI层设置的当前聊天对象不是对方
                && !notiFlagDisabled//用户本地设置没有将通知禁用
                && (!isCustomMsg || isCustomNotificationEnabled);//这条消息不是自定义消息，或者 是自定义消息，但是消息发送方有自定义通知栏内容。

        if (sendNotify) {
            sendNotification(msg);
        }
    }

    @TargetApi(16)
    private Notification createNotification(InternalMessage msgToSend, int notiFlag) {
        Notification notice;
        String contentText;
        String tickerText;
        InternalConversation conv = ConversationManager.getInstance().getConversation(msgToSend.getTargetType(), msgToSend.getTargetID(), msgToSend.getTargetAppKey());
        if (null == conv) {
            Logger.w(TAG, "conversation is null,failed to send notification.");
            return null;
        }
        int unReadCnt = conv.getUnReadMsgCnt();
        InternalUserInfo fromUser = (InternalUserInfo) msgToSend.getFromUser();
        String fromName = msgToSend.getFromName();
        String senderName;
        String title;

        //senderName展示的逻辑是： content.fromName && (setFromName == true) > user.noteName > content.fromName && (setFromName == false) > user.displayName.
        if (!TextUtils.isEmpty(fromName) && msgToSend.getIsSetFromName().intValue() == 1) {
            senderName = fromName;
        } else if (null != fromUser && !TextUtils.isEmpty(fromUser.getNotename())) {
            senderName = fromUser.getNotename();
        } else if (!TextUtils.isEmpty(fromName)) {
            senderName = fromName;
        } else {
            //通知栏展示的发送者名，前面已经判断过了noteName,此处不需要再拿noteName
            senderName = null != fromUser ? fromUser.getDisplayName(false) : msgToSend.getFromID();
        }

        //title的展示逻辑是：
        // 单聊 - senderName + unReadCnt.
        // 群聊 - (groupName > conversation title > 默认名称) + unReadCnt
        String unreadMsgString = "";
        if (unReadCnt > 1) {
            unreadMsgString = String.format(Locale.CHINA, "(%d条未读)\r", unReadCnt);
        }
        if (ConversationType.group == conv.getType()) {
            InternalGroupInfo groupInfo = (InternalGroupInfo) conv.getTargetInfo();
            //修复bug:http://jira.jpushoa.com/browse/IM-2129
            if (null != groupInfo && !TextUtils.isEmpty(groupInfo.getGroupName())) {
                //本地拿到群名不为空，以群名为最优先展示。
                title = groupInfo.getGroupName();
            } else if (!TextUtils.isEmpty(conv.getTitle())) {
                //targetName为空，直接拿本地的conversation title展示。
                title = conv.getTitle();
            } else {
                //若本地conversation title也为空，说明群信息还没取下来，先显示“群聊”
                title = "群聊";
            }
        } else {
            title = senderName;
        }
        //未读消息数显示在名称后面
        title += unreadMsgString;

        if (ContentType.prompt == msgToSend.getContentType()) {
            //如果消息是prompt类型消息，优先将promptContent中文字展示出来。
            contentText = tickerText = ((PromptContent) msgToSend.getContent()).getPromptText();
        } else if (null == msgToSend.getMessageSendingOptions() || !msgToSend.getMessageSendingOptions().isCustomNotficationEnabled()) {
            String atMeText = "";
            if (msgToSend.isAtMe()) {
                atMeText = "[有人@我]";
            }
            if (msgToSend.isAtAll()) {
                atMeText = "[@所有人]";
            }

            String contentTextPrefix = atMeText + senderName + ":";
            switch (msgToSend.getContentType()) {
                case text:
                    contentText = tickerText = contentTextPrefix + ((TextContent) msgToSend.getContent()).getText();
                    break;
                case voice:
                    contentText = contentTextPrefix + "[语音]";
                    tickerText = contentTextPrefix + "发来一段语音";
                    break;
                case image:
                    contentText = contentTextPrefix + "[图片]";
                    tickerText = contentTextPrefix + "发来一张图片";
                    break;
                case video:
                    contentText = contentTextPrefix + "[视频]";
                    tickerText = contentTextPrefix + "发来一段视频";
                    break;
                case file:
                    contentText = contentTextPrefix + "[文件]";
                    tickerText = contentTextPrefix + "发来一个文件";
                    break;
                case location:
                    contentText = contentTextPrefix + "[位置]";
                    tickerText = contentTextPrefix + "发来一条位置消息";
                    break;
                default:
                    contentText = contentTextPrefix + "[其他]";
                    tickerText = contentTextPrefix + "发来一条消息";
            }
        } else {
            title = msgToSend.getMessageSendingOptions().getNotificationTitle();
            String notiText = "";
            if (msgToSend.isAtMe() || msgToSend.isAtAll()) {
                notiText += msgToSend.getMessageSendingOptions().getNotificationAtPrefix();
            }
            Logger.d(TAG, "noti is atme = " + msgToSend.isAtMe() + " is at all = " + msgToSend.isAtAll() + " at prefix = " + msgToSend.getMessageSendingOptions().getNotificationAtPrefix());
            notiText += msgToSend.getMessageSendingOptions().getNotificationText();
            Logger.d(TAG, " noti text = " + notiText);
            contentText = tickerText = notiText;
        }

        Intent intent = new Intent(cn.jpush.im.android.JMessage.ACTION_NOTI_RECEIVER_PROXY);
        intent.addCategory(JMessage.mContext.getPackageName());
        intent.putExtra(TARGET_ID, msgToSend.getTargetID());
        intent.putExtra(TARGET_APPKEY, msgToSend.getTargetAppKey());
        intent.putExtra(CONVERSATION_TYPE, conv.getType().toString());
        intent.putExtra(SERVER_MSG_ID, msgToSend.getServerMessageId());
        PendingIntent contentIntent = PendingIntent.getBroadcast(JMessage.mContext, (msgToSend.getTargetID() + msgToSend.getTargetAppKey()).hashCode(),
                intent, PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT < (Build.VERSION_CODES.GINGERBREAD + 2)) {
            notice = new Notification();
            //notice.setLatestEventInfo(JMessage.mContext, title, contentText, contentIntent);
            notice.icon = info.icon;
            notice.when = System.currentTimeMillis();
            notice.tickerText = tickerText;
            notice.flags = Notification.FLAG_AUTO_CANCEL;
        } else {
            Notification.Builder builder = new Notification.Builder(JMessage.mContext).setContentTitle(title)
                    .setContentText(contentText).setTicker(tickerText).setSmallIcon(info.icon)
                    .setLargeIcon(BitmapFactory.decodeResource(JMessage.mContext.getResources(), info.icon))
                    .setAutoCancel(true).setContentIntent(contentIntent);
            if (Build.VERSION.SDK_INT < (Build.VERSION_CODES.GINGERBREAD + 7)) {
                notice = builder.getNotification();
            } else {
                notice = builder.build();
            }
        }

        if (0 != (notiFlag & JMessageClient.FLAG_NOTIFY_WITH_SOUND)) {
            notice.defaults |= Notification.DEFAULT_SOUND;
        }
        if (0 != (notiFlag & JMessageClient.FLAG_NOTIFY_WITH_VIBRATE)) {
            notice.defaults |= Notification.DEFAULT_VIBRATE;
        }
        if (0 != (notiFlag & JMessageClient.FLAG_NOTIFY_WITH_LED)) {
            notice.flags |= Notification.FLAG_SHOW_LIGHTS;
            //可以使用系统默认的亮灯方式,或者手动设置
            notice.defaults |= Notification.DEFAULT_LIGHTS;
        }
        return notice;
    }
}
