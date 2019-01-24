package cn.jpush.im.android.utils;

import android.text.TextUtils;

import com.google.gson.jpush.JsonElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.MediaContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.storage.ConversationManager;


public class MessageProtocolParser {

    private static final String TAG = "MessageProtocolParser";

    public static String messageToProtocol(InternalMessage message) {
        String protocol;
        if (null != message) {
            Logger.i(TAG, "before translate, msg = " + message.toString());
            protocol = message.toJson();
            Logger.i(TAG, "after translate, protocol = " + protocol);
            return protocol;
        }
        return null;
    }

    public static Message protocolToMessage(cn.jpush.im.android.pushcommon.proto.Message.ChatMsg chatMsg, int unreceitptCnt) {
        if (null != chatMsg) {
            MessageDirect direct;
            if (chatMsg.getFromUid() == IMConfigs.getUserID()) {
                //如果消息的fromUid是自己，说明这条消息是发送的消息。
                direct = MessageDirect.send;
            } else {
                direct = MessageDirect.receive;
            }

            String protocol = chatMsg.getContent().getContent().toStringUtf8();
            if (!TextUtils.isEmpty(protocol)) {
                try {
                    Logger.d(TAG, "before translate, protocol = " + protocol);
                    MessageSendingOptions options = new MessageSendingOptions();
                    options.setShowNotification(!chatMsg.getAction().getNoNotification());
                    options.setCustomNotificationEnabled(chatMsg.getContent().getCustomNote().getEnabled());
                    options.setNotificationTitle(chatMsg.getContent().getCustomNote().getTitle().toStringUtf8());
                    options.setNotificationText(chatMsg.getContent().getCustomNote().getAlert().toStringUtf8());
                    options.setNotificationAtPrefix(chatMsg.getContent().getCustomNote().getAtPrefix().toStringUtf8());
                    InternalMessage internalMessage = protocolToInternalMessage(protocol, direct, chatMsg.getCtimeMs(), chatMsg.getMsgid(), unreceitptCnt, chatMsg.getCtimeMs(), options, true);
                    Message msg = saveMsgToLocal(internalMessage);
                    Logger.d(TAG, "after translate, msg = " + msg);
                    return msg;
                } catch (Exception e) {
                    Logger.ee(TAG, "exception occurs when translate protocol to message" + e.getMessage(), e);
                    MessageEvent messageEvent = new MessageEvent(ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR, ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC, null);
                    EventBus.getDefault().post(messageEvent);
                }
            } else {
                Logger.ee(TAG, "protocol is null, give up to parse");
                MessageEvent messageEvent = new MessageEvent(ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR, ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC, null);
                EventBus.getDefault().post(messageEvent);
            }
        } else {
            Logger.ee(TAG, "protocol is null, give up to parse");
            MessageEvent messageEvent = new MessageEvent(ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR, ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC, null);
            EventBus.getDefault().post(messageEvent);
        }
        return null;
    }

    public static InternalMessage protocolToInternalMessage(String protocol, MessageDirect direct, long createTime, long msgId, int unreceiptCnt, long unreceiptMtime, MessageSendingOptions options, boolean sendEvent) {
        InternalMessage internalMessage = JsonUtil
                .fromJsonOnlyWithExpose(protocol, InternalMessage.class);
        if (null == internalMessage) {
            Logger.ww(TAG, "parse protocol failed,return null");
            return null;
        }
        int version = internalMessage.getVersion();
        String targetId = internalMessage.getTargetID();
        String fromId = internalMessage.getFromID();
        String contentTypeString = internalMessage.getMsgTypeString();
        JsonElement msgBody = internalMessage.getMsgBody();

        //必要属性检查
        if (version <= 0 || StringUtils.isTextEmpty(targetId) || StringUtils.isTextEmpty(fromId)
                || null == contentTypeString || null == msgBody) {
            String desc = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_INVALID_KEY_VALUE_DESC;
            Logger.ee(TAG, "[MessageProtocolParser]" + desc + " version = " + version +
                    " targetId = " + targetId + " fromId = " + fromId + " contentTypeString = " + contentTypeString +
                    " msgBody = " + msgBody);
            if (sendEvent) {
                MessageEvent messageEvent = new MessageEvent(ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_INVALID_KEY_VALUE,
                        desc, null);
                EventBus.getDefault().post(messageEvent);
            }
            return null;
        } else if (Consts.PROTOCOL_VERSION_CODE != internalMessage.getVersion()) {
            String desc = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_VERSION_NOT_MATCH_DESC;
            Logger.ee(TAG, "[MessageProtocolParser]" + desc + " version = "
                    + internalMessage.getVersion());
            if (sendEvent) {
                MessageEvent messageEvent = new MessageEvent(ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_VERSION_NOT_MATCH, desc, null);
                EventBus.getDefault().post(messageEvent);
            }
            return null;
        } else if (ConversationType.group == internalMessage.getTargetType()) {
            //群聊下，先验证targetID是否可以被转成long型，如果报错，直接跳出解析逻辑。
            try {
                Long.parseLong(targetId);
            } catch (NumberFormatException e) {
                String desc = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC;
                Logger.ee(TAG, "[MessageProtocolParser] target id is invalid, return from message parse.");
                if (sendEvent) {
                    MessageEvent messageEvent = new MessageEvent(ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR,
                            desc, null);
                    EventBus.getDefault().post(messageEvent);
                }
                return null;
            }
        }

        //非必要属性默认值检查
        ConversationType targetType = internalMessage.getTargetType();
        String targetAppkey = internalMessage.getTargetAppKey();
        String fromAppkey = internalMessage.getFromAppKey();
        if (null == targetType) {
            Logger.w(TAG, "no target contentType in message json, set default.");
            internalMessage.setTargetType(ConversationType.single);
        }

        if (targetType == ConversationType.single && StringUtils.isTextEmpty(targetAppkey)) {
            //只有当单聊时，targetAppkey如果不存在，才需要给一个默认值。群聊不需要targetAppkey
            internalMessage.setTargetAppkey(JCoreInterface.getAppKey());
        }

        if (StringUtils.isTextEmpty(fromAppkey)) {
            Logger.w(TAG, "no from appkey in message json, set default.");
            internalMessage.setFromAppkey(JCoreInterface.getAppKey());
        }

        internalMessage.setCreateTime(createTime);
        internalMessage.setServerMessageId(msgId);

        ContentType contentType;
        try {
            contentType = ContentType.valueOf(contentTypeString);
        } catch (IllegalArgumentException e) {
            Logger.ww(TAG, "received an unsupported msg contentType!");
            contentType = ContentType.unknown;
            //如果无法识别msg contentType，则将msg body存下来下次再解析
            internalMessage.setOriginMeta(protocol);
        }
        internalMessage.setContentType(contentType);
        internalMessage.setMsgType(contentTypeString);
        MessageContent content = MessageContent.fromJson(msgBody, contentType);
        internalMessage.setContent(content);
        internalMessage.setDirect(direct);
        internalMessage.setMessageSendingOptions(options);

        if (direct == MessageDirect.send) {
            internalMessage.setStatus(MessageStatus.send_success);
            internalMessage.setUnreceiptCnt(unreceiptCnt);
            if (0 == unreceiptMtime) {
                unreceiptMtime = createTime;//默认消息的ctime当做未回执数的mtime.
            }
            internalMessage.setUnreceiptMtime(unreceiptMtime);
        } else {
            if (targetType == ConversationType.single) {
                //对于收到的单聊消息，这里要将internalMessage对象中target相关的几个属性修改下，
                //因为sdk中的msg对象是的target的含义和protocol中定义的target含义不同 ：=> protocol中target指消息的接收方，而sdk中msg对象的target指聊天的对象。
                //在收到单聊消息时，这里就会出现一个矛盾：protocol中的target是消息接收者，也就是我自己。 而实际sdk中msg的target应该是聊天对方。
                //所以这里将from字段的属性设置到target上，确保sdk中的msg对象的target指的是聊天对象。
                internalMessage.setTargetAppkey(internalMessage.getFromAppKey());
                internalMessage.setTargetID(internalMessage.getFromID());
                internalMessage.setTargetName(internalMessage.getFromName());
            }
            if (internalMessage.getContent() instanceof MediaContent) {
                internalMessage.setStatus(MessageStatus.receive_fail);
            } else {
                internalMessage.setStatus(MessageStatus.receive_success);
            }
        }

        return internalMessage;
    }

    public static Message saveMsgToLocal(InternalMessage internalMessage) {
        if (null == internalMessage) {
            return null;
        }

        InternalConversation conv = getConversationFromMsg(internalMessage);

        if (null == conv) {
            Logger.ww(TAG, "conversation is null for some reason. quit from parser");
            return null;
        }

        InternalMessage msg;

        boolean isShowCustome = true;
        if (internalMessage.getContent() instanceof CustomContent){
            CustomContent customContent = (CustomContent) internalMessage.getContent();
            if ( customContent.getNumberValue("CmdType") != null){
                //满足这个条件，不需要展示出来
                if ( customContent.getNumberValue("CmdType").intValue() == 0 ||
                        customContent.getNumberValue("CmdType").intValue()==2){
                    isShowCustome = false;
                }
            }
        }

        boolean increaseUnreadCnt = internalMessage.getDirect() == MessageDirect.receive
//                && internalMessage.getContentType() != ContentType.custom
                && isShowCustome
                && internalMessage.getCreateTime() > conv.getUnreadCntMtime();
        //以下三个条件都满足时，才能更新未读数。
        // 1 消息direct为是接收
        // 2 不是自定义消息。
        // 3 消息的创建时间晚于本地记录的会话最后一次的未读数清空时间。

        msg = conv.saveMessage(internalMessage, increaseUnreadCnt);

        //设置消息的targetInfo对象，这一步是必须的
        msg.setTargetInfo(conv.getTargetInfo());
        return msg;
    }

    public static List<InternalMessage> saveChatMsgToLocalInBatch(long uid, List<cn.jpush.im.android.pushcommon.proto.Message.ChatMsg> chatMsgs,
                                                                  Map<Long, Receipt.MsgReceiptMeta> metaMap, Collection<Long> newList, Collection<Long> oldList) {

        InternalConversation conv = null;
        InternalMessage latestMsg = null;
        int unreadCnt = 0;
        List<InternalMessage> messages = new ArrayList<InternalMessage>();
        long myUid = IMConfigs.getUserID();
        for (cn.jpush.im.android.pushcommon.proto.Message.ChatMsg chatMsg : chatMsgs) {
            InternalMessage msg;
            MessageSendingOptions options = new MessageSendingOptions();
            options.setShowNotification(!chatMsg.getAction().getNoNotification());
            options.setCustomNotificationEnabled(chatMsg.getContent().getCustomNote().getEnabled());
            options.setNotificationTitle(chatMsg.getContent().getCustomNote().getTitle().toStringUtf8());
            options.setNotificationText(chatMsg.getContent().getCustomNote().getAlert().toStringUtf8());
            options.setNotificationAtPrefix(chatMsg.getContent().getCustomNote().getAtPrefix().toStringUtf8());
            if (chatMsg.getFromUid() == myUid) {
                //如果消息的fromUid是自己，说明这条消息是发送的消息。
                Receipt.MsgReceiptMeta meta = metaMap.get(chatMsg.getMsgid());
                int unreceiptCnt = 0;//只有是我发出的消息，才需要初始化未回执数。
                long unreceiptMtime = 0L;
                if (null != meta) {
                    unreceiptCnt = meta.getUnreadCount();
                    unreceiptMtime = meta.getMtime();
                    Logger.d(TAG, "save chat msg to local in batch . MsgReceiptMeta unread cnt = " + unreceiptCnt + " mtime = " + unreceiptMtime);
                }
                msg = protocolToInternalMessage(chatMsg.getContent().getContent().toStringUtf8(), MessageDirect.send, chatMsg.getCtimeMs(),
                        chatMsg.getMsgid(), unreceiptCnt, unreceiptMtime, options, true);
            } else {
                msg = protocolToInternalMessage(chatMsg.getContent().getContent().toStringUtf8(), MessageDirect.receive, chatMsg.getCtimeMs(),
                        chatMsg.getMsgid(), 0, 0, options, true);
            }
            if (null == msg) {
                continue;
            }
            messages.add(msg);//放入list之后使消息按创建时间升序排列
            if (null == conv) {
                conv = getConversationFromMsg(msg);
            }
            if (null != conv) {
                boolean isShowCustome = true;
                if (msg.getContent() instanceof CustomContent){
                    CustomContent customContent = (CustomContent) msg.getContent();
                    if ( customContent.getNumberValue("CmdType") != null){
                        //满足这个条件，不需要展示出来
                        if ( customContent.getNumberValue("CmdType").intValue() == 0 ||
                                customContent.getNumberValue("CmdType").intValue()==2){
                            isShowCustome = false;
                        }
                    }
                }
                //以下三个条件都满足时，才能更新未读数。
                // 1 消息direct为是接收
                // 2 不是自定义消息。
                // 3 消息的创建时间晚于本地记录的会话最后一次的未读数清空时间。
                // 4 消息属于newlist
                if (msg.getDirect() == MessageDirect.receive
                        && isShowCustome
//                        && ContentType.custom != msg.getContentType()
                        && msg.getCreateTime() > conv.getUnreadCntMtime()
                        && newList.contains(msg.getServerMessageId())) {
                    unreadCnt++;
                }
                //设置消息的targetInfo对象，这一步对于构建一个msg对象来说是必须的
                msg.setTargetInfo(conv.getTargetInfo());
            }

            //定位这批消息中，最后一条消息是哪一条，之后更新会话的latestMsg字段时要用。
            if (null == latestMsg || msg.getCreateTime() > latestMsg.getCreateTime()) {
                latestMsg = msg;
            }
        }

        if (messages.isEmpty()) {
            Logger.ww(TAG, "message list is empty,quit from parser");
            return null;
        }
        if (null == conv) {
            Logger.ww(TAG, "conversation is null for some reason. quit from parser");
            return null;
        }
        //把消息列表按创建时间升序排列
        Collections.sort(messages, new MsgComparator());
        return conv.saveMessagesInBatch(uid, messages, newList, oldList, latestMsg, unreadCnt);
    }

    private static InternalConversation getConversationFromMsg(InternalMessage msg) {
        String targetID = msg.getTargetID();
        String targetName = msg.getTargetName();
        String targetAppKey = msg.getTargetAppKey();
        ConversationType targetType = msg.getTargetType();

        InternalConversation conv = ConversationManager.getInstance().createConversation(targetType, targetID, targetAppKey, targetName, msg.getCreateTime(), false);
        //此时如果用户正好登出了，创建conv会失败。
        if (null == conv) {
            return null;
        }
        if (targetType == ConversationType.single) {
            conv.setTargetName(msg.getFromName());
        } else {
            conv.setTargetName(targetName);
        }

        return conv;
    }

    /**
     * 按照消息的createTime升序排列。
     */
    private static class MsgComparator implements Comparator<Message> {
        @Override
        public int compare(Message lhs, Message rhs) {
            if (lhs.getCreateTime() > rhs.getCreateTime()) {
                return 1;
            } else if (lhs.getCreateTime() < rhs.getCreateTime()) {
                return -1;
            }
            return 0;
        }
    }
}
