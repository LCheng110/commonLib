package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import java.util.concurrent.Callable;

import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by xiongtc on 2016/10/11.
 */
abstract class MsgRequest extends ImBaseRequest {
    private static final String TAG = "MsgRequest";

    public MsgRequest(int cmd, long uid, long rid) {
        super(cmd, uid, rid);
    }

    void msgCallbackToUser(Message msg, int responseCode, String responseMsg) {
        if (null == msg) {
            Logger.ww(TAG, "handleMessage response failed, message is null.");
            return;
        }
        InternalConversation conversation = ConversationManager.getInstance()
                .getConversation(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey());
        if (null != conversation) {
            conversation.updateMessageStatus(msg, MessageStatus.send_fail);
        } else {
            Logger.ww(TAG, "conversation is null!update message status failed !");
        }
        CommonUtils.doMessageCompleteCallbackToUser(msg.getTargetID(), msg.getTargetAppKey(), msg.getId(),
                responseCode, responseMsg);
        RequestProcessor.requestsCache.remove(rid);
    }

    void sendMsgPostExecute(final IMProtocol imProtocol, final Message msg, final MessageSendingOptions options) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Message>() {
            @Override
            public Message call() throws Exception {
                if (null == msg) {
                    Logger.ww(TAG, "sendMessagePostExecute failed, message is null.");
                    return null;
                }

                ConversationType conversationType = msg.getTargetType();
                Long serverMsgId = 0L;
                Long cTimeMS = 0L;
                int unReceiptCnt = 1;
                switch (conversationType) {
                    case single:
                        cn.jpush.im.android.pushcommon.proto.Message.SingleMsg singleMsgEntity = (cn.jpush.im.android.pushcommon.proto.Message.SingleMsg) imProtocol.getEntity();
                        serverMsgId = singleMsgEntity.getMsgid();
                        cTimeMS = singleMsgEntity.getCtimeMs();
                        break;
                    case group:
                        cn.jpush.im.android.pushcommon.proto.Message.GroupMsg groupMsgEntity = (cn.jpush.im.android.pushcommon.proto.Message.GroupMsg) imProtocol.getEntity();
                        serverMsgId = groupMsgEntity.getMsgid();
                        cTimeMS = groupMsgEntity.getCtimeMs();
                        unReceiptCnt = groupMsgEntity.getUnreadCount();
                        break;
                }

                InternalConversation conv = ConversationManager.getInstance()
                        .getConversation(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey());
                if (null == conv) {
                    Logger.ww(TAG, "conversation is null!update message status failed !");
                    return msg;
                }

                if (responseCode == 0) {
                    conv.updateMessageStatus(msg, MessageStatus.send_success);
                    conv.updateMessageServerMsgId(msg, serverMsgId);
                    conv.updateMessageTimestamp(msg, cTimeMS);
                    if (null != options && options.isNeedReadReceipt()) {
                        //当这条消息是需要对方的已读回执的消息时
                        conv.updateMessageUnreceiptCnt(msg, unReceiptCnt, cTimeMS);//消息发送成功后需要更新消息的未回执数和其mtime
                    }
                    //发送成功后还需要将msg id 加入online msgid list中去。用来在线消息去重。
                    conv.insertToOnlineMsgTable(serverMsgId, cTimeMS);
                    Logger.dd(TAG, "send message success. id = " + serverMsgId + " cTimeMS = " + cTimeMS);
                } else {
                    conv.updateMessageStatus(msg, MessageStatus.send_fail);
                    Logger.d(TAG, "send message failed ! response code is " + responseCode);
                }

                Message latestMsg = conv.getLatestMessage();
                if (null != latestMsg && latestMsg.getId() == msg.getId() && latestMsg.hashCode() != msg.hashCode()) {
                    //如果此时会话的latest msg hashcode和发送的消息的msg hashcode不等，但是id是相等的，说明这条被发送的消息是从数据库中重新映射出来的一个新的对象，这时注意也要更新conv的latest msg字段
                    ConversationManager.getInstance().updateLatestMsg(conv.getType(), conv.getTargetId(), conv.getTargetAppKey(), (InternalMessage) msg);
                }

                return msg;
            }
        }).continueWith(new Continuation<Message, Void>() {
            @Override
            public Void then(final Task<Message> task) throws Exception {
                Message msg = task.getResult();
                if (null == msg) {
                    Logger.w(TAG, "msg is null , return from post execute!");
                    return null;
                }
                CommonUtils.doMessageCompleteCallbackToUser(msg.getTargetID(), msg.getTargetAppKey(),
                        msg.getId(), responseCode, responseMsg);
                RequestProcessor.requestsCache.remove(rid);
                return null;
            }
        });
    }
}
