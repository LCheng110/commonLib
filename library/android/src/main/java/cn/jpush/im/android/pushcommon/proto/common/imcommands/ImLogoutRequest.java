package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import cn.jpush.im.android.pushcommon.proto.User.Logout;
import cn.jpush.im.android.utils.JsonUtil;


public class ImLogoutRequest extends ImBaseRequest {

    private static final String TAG = "ImLogoutRequest";
    @Expose
    String username;

    public ImLogoutRequest(String username, long rid, long uid) {
        super(IMCommands.Logout.CMD, uid, rid);

        this.username = username;
    }

    public static ImLogoutRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, ImLogoutRequest.class);
    }


    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Logout.Builder builder = Logout.newBuilder();
        if (null != username) {
            builder.setUsername(ByteString.copyFromUtf8(username));
        }

        return new IMProtocol(IMCommands.Logout.CMD,
                IMCommands.Logout.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        //do nothing...
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        //do nothing...
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        //do nothing...
    }

}
