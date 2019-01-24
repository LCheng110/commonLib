package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Message.AddMsgnoDisturbSingle;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class AddMsgnoDisturbSingleRequest extends MessageNoDisturbRequest {

    private static final String TAG = "AddMsgnoDisturbSingleRequest";
    @Expose
    long targetUid;
    @Expose
    long version;

    public AddMsgnoDisturbSingleRequest(long targetUid, long rid, long uid) {
        super(IMCommands.AddMsgnoDisturbSingle.CMD, uid, rid);

        this.targetUid = targetUid;
    }

    public static AddMsgnoDisturbSingleRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, AddMsgnoDisturbSingleRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        AddMsgnoDisturbSingle addMsgnoDisturbSingle =
                AddMsgnoDisturbSingle.newBuilder()
                        .setTargetUid(targetUid)
                        .build();

        return new IMProtocol(IMCommands.AddMsgnoDisturbSingle.CMD,
                IMCommands.AddMsgnoDisturbSingle.VERSION,
                imUid, appKey, addMsgnoDisturbSingle);
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (0 != targetUid && (ErrorCode.NO_ERROR == responseCode
                        || NO_DISTURB_USER_ALREADY_SET == responseCode)) {
                    UserInfoManager.getInstance().updateNoDisturb(targetUid, true);
                } else {
                    Logger.ww(TAG, "add user to no-disturb failed. code = " + responseCode);
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }
}
