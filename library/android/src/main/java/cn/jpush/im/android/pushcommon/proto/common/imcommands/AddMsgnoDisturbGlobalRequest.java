package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Message.AddMsgnoDisturbGlobal;
import cn.jpush.im.android.utils.JsonUtil;


public class AddMsgnoDisturbGlobalRequest extends MessageNoDisturbRequest {

    @Expose
    int none;
    @Expose
    long version;

    public AddMsgnoDisturbGlobalRequest(long rid, long uid) {
        super(IMCommands.AddMsgnoDisturbGlobal.CMD,uid,rid);
    }

    public static AddMsgnoDisturbGlobalRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, AddMsgnoDisturbGlobalRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        AddMsgnoDisturbGlobal addMsgnoDisturbGlobal =
                AddMsgnoDisturbGlobal.newBuilder()
                        .build();
        return new IMProtocol(IMCommands.AddMsgnoDisturbGlobal.CMD,
                IMCommands.AddMsgnoDisturbGlobal.VERSION,
                imUid, appKey, addMsgnoDisturbGlobal);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        setNoDisturbGlobalPostExecute(imProtocol.getResponse().getCode(),
                imProtocol.getResponse().getMessage().toStringUtf8(), 1);
    }

}
