package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.pushcommon.proto.User;
import cn.jpush.im.android.utils.JsonUtil;

public class ReportInfoRequest extends ImBaseRequest {

    @Expose
    String sdkVersion;

    public ReportInfoRequest(String sdkVersion, long rid, long uid) {
        super(IMCommands.ReportInformation.CMD, uid, rid);

        this.sdkVersion = sdkVersion;
    }

    public static ReportInfoRequest fromJson(String json) {
        return JsonUtil.fromJson(json, ReportInfoRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        User.ReportInformation.Builder builder = User.ReportInformation.newBuilder();
        if (null != sdkVersion) {
            builder.setSdkVersion(ByteString.copyFromUtf8(sdkVersion));
        }

        return new IMProtocol(IMCommands.ReportInformation.CMD,
                IMCommands.ReportInformation.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        //do nothing...
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        //do nothing...
        int responseCode = imProtocol.getResponse().getCode();
        if (ErrorCode.NO_ERROR == responseCode) {
            //版本号上报成功，则将版本保存至本地
            IMConfigs.setSdkVersion(JMessageClient.getSdkVersionString());
        }
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        //do nothing...
    }

}
