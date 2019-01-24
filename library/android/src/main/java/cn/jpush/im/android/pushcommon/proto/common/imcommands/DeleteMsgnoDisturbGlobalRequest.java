package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Message.DeleteMsgnoDisturbGlobal;
import cn.jpush.im.android.utils.JsonUtil;


public class DeleteMsgnoDisturbGlobalRequest extends MessageNoDisturbRequest {

    @Expose
    int none;
    @Expose
    long version;

    public DeleteMsgnoDisturbGlobalRequest(long rid, long uid) {
        super(IMCommands.DeleteMsgnoDisturbGlobal.CMD, uid, rid);
    }

    public static DeleteMsgnoDisturbGlobalRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, DeleteMsgnoDisturbGlobalRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        DeleteMsgnoDisturbGlobal deleteMsgnoDisturbGlobal =
                DeleteMsgnoDisturbGlobal.newBuilder()
                        .build();
        return new IMProtocol(IMCommands.DeleteMsgnoDisturbGlobal.CMD,
                IMCommands.DeleteMsgnoDisturbGlobal.VERSION,
                imUid, appKey, deleteMsgnoDisturbGlobal);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        setNoDisturbGlobalPostExecute(imProtocol.getResponse().getCode(),
                imProtocol.getResponse().getMessage().toStringUtf8(), 0);
    }
}
