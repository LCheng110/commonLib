package cn.jpush.im.android.pushcommon.proto.common.commands;

import com.google.protobuf.jpush.InvalidProtocolBufferException;

import java.nio.ByteBuffer;

import cn.jiguang.ald.api.JRequest;
import cn.jpush.im.android.pushcommon.proto.JMessageCommands;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.pushcommon.proto.common.utils.ProtocolUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class IMRequest extends JRequest {
    private static final String TAG = "IMRequest";
    int imBodyLength;
    IMProtocol protocol;

    @Override
    public String getName() {
        return "IMRequest";
    }

    public IMProtocol getIMProtocol() {
        return this.protocol;
    }

    public void setIMProtocol(IMProtocol protocol) {
        this.protocol = protocol;
    }

    // outbound
    public IMRequest(long rid, IMProtocol imProtocol) {
    	super(JMessageCommands.IM.VERSION, JMessageCommands.IM.CMD, rid);
    	
    	this.protocol = imProtocol;
    }

    @Override
    public void writeBody() {
        if (null != protocol) {
            byte[] imProtocolByets = protocol.toProtocolBuffer().toByteArray();
            imBodyLength = imProtocolByets.length;
            writeInt2(imBodyLength);
            writeBytes(imProtocolByets);
        }
    }

	@Override
	protected boolean isNeedParseeErrorMsg() {
		return false;
	}

	// inbound
	public IMRequest(Object head, ByteBuffer bodyBuffer) {
		super(head, bodyBuffer);
	}

    @Override
    public void parseBody() {

		this.imBodyLength = this.body.getShort();
        Logger.d("", "IM Body Length - " + imBodyLength);

        ByteBuffer buffer = this.body;
    	
    	try {
			IMProtocol imProtocol = new IMProtocol(ProtocolUtil.getBytesConsumed(buffer));
			this.protocol = imProtocol;
			
		} catch (InvalidProtocolBufferException e) {
            //protobuf解析失败时，把body中的字节全部打出来，方便排查问题
            Logger.dd("IMRequest", "The body - " + StringUtils.toHexLog(buffer.array()));
			e.printStackTrace();

		}
    }

}
