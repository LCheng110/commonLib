package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.text.TextUtils;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Friend;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by wangmeng on 15/11/20.
 */
public class UpdateMemoRequest extends ImBaseRequest {
    private static final String TAG = "UpdateMemoRequest";
    @Expose
    long targetUid;
    @Expose
    String newMemoName;
    @Expose
    String newMemoOthers;

    public UpdateMemoRequest(long targetUid, String newMemoName, String newMemoOthers,
                             long rid, long uid) {
        super(IMCommands.UpdateMemo.CMD, uid, rid);
        this.targetUid = targetUid;
        this.newMemoName = newMemoName;
        this.newMemoOthers = newMemoOthers;
    }

    public static UpdateMemoRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, UpdateMemoRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Friend.UpdateMemo.Builder builder = Friend.UpdateMemo.newBuilder()
                .setTargetUid(targetUid);
        if (null != newMemoName) {
            builder.setNewMemoName(ByteString.copyFromUtf8(newMemoName));
        }
        if (null != newMemoOthers) {
            builder.setNewMemoOthers(ByteString.copyFromUtf8(newMemoOthers));
        }

        return new IMProtocol(IMCommands.UpdateMemo.CMD, IMCommands.UpdateMemo.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Logger.d(TAG, "update memo post execute! code = " + imProtocol.getResponse().getCode() + " desc = " + imProtocol.getResponse().getMessage().toStringUtf8());
                if (0 == responseCode) {
                    if (!TextUtils.isEmpty(newMemoName)) {
                        UserInfoManager.getInstance().updateNoteName(targetUid, newMemoName);
                    }

                    if (!TextUtils.isEmpty(newMemoOthers)) {
                        UserInfoManager.getInstance().updateNoteText(targetUid, newMemoOthers);
                    }
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }

}
