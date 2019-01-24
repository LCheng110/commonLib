package cn.jpush.im.android.helpers;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.Logger;


/**
 * 此类用于管理正在发送的msg list，当用户登出时，所有正在发送的消息需要重置状态为send_failed.
 */
public class MessageSendingMaintainer {

    private static final String TAG = "MessageSendingMaintainer";

    /**
     * 存放正在发送消息的List,其中messageIdentifier string的组成规则为：TargetID_Appkey_MessageID.
     * 如：aaaa_appkey111_20(单聊) 、 1000029_group_3(群聊)
     */
    private static List<String> messageSendingIdentifierList = new ArrayList<String>();

    public static void addIdentifier(String targetID, String appkey, int msgID) {
        String msgIdentifier = createMsgIdentifier(targetID, appkey, msgID);
        if (null != msgIdentifier) {
            messageSendingIdentifierList.add(msgIdentifier);
        }
    }

    public static void removeIdentifier(String targetID, String appkey, int msgID) {
        String msgIdentifier = createMsgIdentifier(targetID, appkey, msgID);
        if (null != msgIdentifier) {
            messageSendingIdentifierList.remove(msgIdentifier);
        }
    }

    /**
     * 用户登出时，若还有正在发送中的消息，此时需要调用此方法将所有正在发送中的消息的状态
     * 置为send_failed.
     */
    public static void resetAllSendingMessageStatus() {
        //将所有正在发送中的消息重置为send_fail.
        for (String msgIdentifier : messageSendingIdentifierList) {
            String appkey = getAppkeyFromIdentifier(msgIdentifier);
            String targetID = getTargetIDFromIdentifier(msgIdentifier);
            int msgID = getMessageIDFromIdentifier(msgIdentifier);
            ConversationType type;
            if ("".equals(appkey)) {
                type = ConversationType.group;
            } else {
                type = ConversationType.single;
            }
            InternalConversation conversation = ConversationManager.getInstance().getConversation(type, targetID, appkey);
            if (null != conversation) {
                Message message = conversation.getMessage(msgID);// TODO: 2017/8/7 这里再获取消息对象的动作是多余的，应该直接通过msgId更新数据库中msg的状态
                conversation.updateMessageStatus(message, MessageStatus.send_fail);
            } else {
                Logger.d(TAG, "conversation not exist,reset message status failed.");
            }
        }
        //重置之后将list清空
        messageSendingIdentifierList.clear();
    }

    private static String createMsgIdentifier(String targetID, String appkey, int msgID) {
        if (null != appkey && !TextUtils.isEmpty(targetID)) {
            if ("".equals(appkey)) {
                appkey = "group";//如果appkey是空，说明是群聊，使用一个字符串占位
            }
            return targetID + File.separator + appkey + File.separator + msgID;
        }
        return "";
    }


    private static String getTargetIDFromIdentifier(String messageIdentifier) {
        String targetID = null;
        if (null != messageIdentifier) {
            targetID = messageIdentifier.split(File.separator)[0];
        }
        return targetID;
    }

    private static String getAppkeyFromIdentifier(String messageIdentifier) {
        String appkey = "";
        if (null != messageIdentifier) {
            appkey = messageIdentifier.split(File.separator)[1];
            if ("group".equals(appkey)) {
                appkey = ""; //如果appkey是群组的占位符，则将appkey置为空字符串
            }
        }
        return appkey;
    }

    private static int getMessageIDFromIdentifier(String messageIdentifier) {
        int messageID = 0;
        if (null != messageIdentifier) {
            String messageIDString = messageIdentifier.split(File.separator)[2];
            try {
                messageID = Integer.parseInt(messageIDString);
            } catch (NumberFormatException e) {
                Logger.ww(TAG, "can not get message id from message identifier. identifier = ");
            }
        }
        return messageID;
    }

}
