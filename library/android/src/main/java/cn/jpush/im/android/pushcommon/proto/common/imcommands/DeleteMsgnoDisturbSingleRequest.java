package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Message.DeleteMsgnoDisturbSingle;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

public class DeleteMsgnoDisturbSingleRequest extends MessageNoDisturbRequest {

    private static final String TAG = "DeleteMsgnoDisturbSingleRequest";
    @Expose
    long targetUid;
    @Expose
    long version;

    public DeleteMsgnoDisturbSingleRequest(long targetUid, long rid, long uid) {
        super(IMCommands.DeleteMsgnoDisturbSingle.CMD, uid, rid);

        this.targetUid = targetUid;
    }

    public static DeleteMsgnoDisturbSingleRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, DeleteMsgnoDisturbSingleRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        DeleteMsgnoDisturbSingle deleteMsgnoDisturbSingle =
                DeleteMsgnoDisturbSingle.newBuilder()
                        .setTargetUid(targetUid)
                        .build();
        return new IMProtocol(IMCommands.DeleteMsgnoDisturbSingle.CMD,
                IMCommands.DeleteMsgnoDisturbSingle.VERSION,
                imUid, appKey, deleteMsgnoDisturbSingle);
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (0 != targetUid && (ErrorCode.NO_ERROR == responseCode
                        || NO_DISTURB_USER_NEVER_SET == responseCode)) {
                    UserInfoManager.getInstance().updateNoDisturb(targetUid, false);
                } else {
                    Logger.ww(TAG, "del user from no-disturb failed. code = " + responseCode);
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }
}
