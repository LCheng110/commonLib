package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.storage.ConversationManager;


/**
 * Created by ${chenyn} on 2016/12/29.
 */

public class MsgRetractRequest extends ImBaseRequest {
    private static final String TAG = MsgRetractRequest.class.getSimpleName();
    @Expose
    long msgID;

    InternalConversation conv;
    InternalMessage message;

    public MsgRetractRequest(long msgID, long uid, long rid, InternalConversation conv, InternalMessage message) {
        super(IMCommands.MsgRetract.CMD, uid, rid);

        this.msgID = msgID;
        this.conv = conv;
        this.message = message;
    }


    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Message.MsgRetract.Builder builder = Message.MsgRetract.newBuilder();
        builder.setRecvUid(msgID);
        return new IMProtocol(IMCommands.MsgRetract.CMD,
                IMCommands.MsgRetract.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        //请求超时
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (responseCode == 0) {
                    //请求响应成功后要更新发送方content以及数据库
                    PromptContent content = new PromptContent("你撤回了一条消息");
                    conv.updateMessageContent(message, content);
                    if (message.getServerMessageId().equals(conv.getLatestMessage().getServerMessageId())) {
                        //如果撤回的消息是会话中的最后一条消息，则要更新会话中的latestMsg相关字段
                        ConversationManager.getInstance().updateLatestMsg(conv.getType(), conv.getTargetId(), conv.getTargetAppKey(), message);
                    }
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }
}
