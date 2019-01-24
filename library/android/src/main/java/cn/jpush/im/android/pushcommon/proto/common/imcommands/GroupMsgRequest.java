package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.android.pushcommon.proto.Message.GroupMsg;
import cn.jpush.im.android.pushcommon.proto.Message.MessageContent;
import cn.jpush.im.android.utils.JsonUtil;

public class GroupMsgRequest extends MsgRequest {

    private static final String TAG = "GroupMsgRequest";
    @Expose
    long groupId;
    @Expose
    String msgContent;

    MessageSendingOptions options;

    private Message msg;

    public GroupMsgRequest(long groupId, String msgContent, long rid, long uid
            , MessageSendingOptions options, Message msg) {
        super(IMCommands.GroupMsg.CMD, uid, rid);
        this.groupId = groupId;
        this.msgContent = msgContent;
        this.options = options;
        this.msg = msg;
    }

    public static GroupMsgRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, GroupMsgRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        GroupMsg.Builder builder = GroupMsg.newBuilder()
                .setTargetGid(groupId);

        if (null == options) {
            options = new MessageSendingOptions();
        }

        if (null != msgContent) {
            cn.jpush.im.android.pushcommon.proto.Message.CustomNotification customNotification =
                    cn.jpush.im.android.pushcommon.proto.Message.CustomNotification.newBuilder()
                            .setEnabled(options.isCustomNotficationEnabled())
                            .setAtPrefix(ByteString.copyFromUtf8(options.getNotificationAtPrefix()))
                            .setAlert(ByteString.copyFromUtf8(options.getNotificationText()))
                            .setTitle(ByteString.copyFromUtf8(options.getNotificationTitle()))
                            .build();

            MessageContent content = MessageContent.newBuilder()
                    .setContent(ByteString.copyFromUtf8(msgContent))
                    .setCustomNote(customNotification).build();

            cn.jpush.im.android.pushcommon.proto.Message.MessageAction messageAction =
                    cn.jpush.im.android.pushcommon.proto.Message.MessageAction.newBuilder()
                            .setNoOffline(!options.isRetainOffline())
                            .setNoNotification(!options.isShowNotification())
                            .setReadReceipt(options.isNeedReadReceipt())
                            .build();

            builder.setContent(content);
            builder.setAction(messageAction);
        }

        return new IMProtocol(IMCommands.GroupMsg.CMD,
                IMCommands.GroupMsg.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        msgCallbackToUser(msg, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT,
                ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        sendMsgPostExecute(imProtocol, msg, options);
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        msgCallbackToUser(msg, responseCode, responseMsg);
    }

}
