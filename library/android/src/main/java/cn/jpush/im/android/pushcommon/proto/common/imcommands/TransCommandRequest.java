package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.pushcommon.proto.Message;

/**
 * Created by jiguang on 2017/9/20.
 */

public class TransCommandRequest extends ImBaseRequest {
    private static final String TAG = "TransCommandRequest";
    private static final int MAX_PACKET_SIZE = 7168;

    @Expose
    long targetUid;
    @Expose
    int type;
    @Expose
    String cmd;

    public TransCommandRequest(long targetUid, int type, String cmd, long rid, long uid) {
        super(IMCommands.TransCommand.CMD, uid, rid);
        this.targetUid = targetUid;
        this.type = type;
        this.cmd = cmd;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Message.TransCommand.Builder builder = Message.TransCommand.newBuilder();
        builder.setTarget(targetUid);
        builder.setType(type);
        builder.setCmd(ByteString.copyFromUtf8(cmd));
        IMProtocol imProtocol = new IMProtocol(IMCommands.TransCommand.CMD,
                IMCommands.TransCommand.VERSION,
                imUid, appKey, builder.build());
        //当imProtocol所构建的Packet字节数大于7168时, 返回null
        if (imProtocol.toProtocolBuffer().toByteArray().length > MAX_PACKET_SIZE) {
            imProtocol = null;
        }
        return imProtocol;
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        int responseCode = imProtocol.getResponse().getCode();
        String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        basicCallbackToUser(responseCode, responseMsg);
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }
}
