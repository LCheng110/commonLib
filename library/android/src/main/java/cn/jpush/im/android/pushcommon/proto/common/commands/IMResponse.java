package cn.jpush.im.android.pushcommon.proto.common.commands;

import com.google.protobuf.jpush.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

import cn.jiguang.ald.api.JResponse;
import cn.jpush.im.android.pushcommon.proto.JMessageCommands;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.pushcommon.proto.common.utils.ProtocolUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class IMResponse extends JResponse {
    private static final String TAG = "IMResponse";
    int imBodyLength;
    IMProtocol protocol;

    @Override
    public String getName() {
        return "IMResponse";
    }

    public IMProtocol getIMProtocol() {
        return this.protocol;
    }

    // for outbound
    public IMResponse(long rid, long juid, IMProtocol protocol) {
        super(JMessageCommands.IM.VERSION, JMessageCommands.IM.CMD, rid, juid, NO_RESP_CODE, null);

        this.protocol = protocol;
    }

    @Override
    public void writeBody() {
        super.writeBody();

        if (null != protocol) {
            byte[] protocolBytes = protocol.toProtocolBuffer().toByteArray();

            imBodyLength = protocolBytes.length;
            Logger.d(TAG, "IM Body Length - " + imBodyLength);

            writeInt2(imBodyLength);
            writeBytes(protocolBytes);
        }
    }

    /**
     * JResponse在解析时是否需要解析code这个字段
     */
    @Override
    protected boolean isNeedParseeErrorMsg() {
        return false;
    }

    public void parseBody() {
        super.parseBody();

        this.imBodyLength = this.body.getShort();
        ByteBuffer buffer = this.body;

        try {
            IMProtocol imProtocol = new IMProtocol(ProtocolUtil.getBytesConsumed(buffer));
            this.protocol = imProtocol;
        } catch (InvalidProtocolBufferException e) {
            //protobuf解析失败时，把body中的字节全部打出来，方便排查问题
            Logger.d("IMRequest", "The body - " + StringUtils.toHexLog(buffer.array()));
            e.printStackTrace();
        }

    }

    // inbound
    public IMResponse(byte[] headBytes, ByteBuffer bodyBuffer) {
        super(bodyBuffer, headBytes);
    }

    public String toString() {
        String objString = (null == protocol) ? "NULL Object" : protocol.toString();
        return "[IMResponse] - protocol:" + objString + " - " + super.toString();
    }

}
