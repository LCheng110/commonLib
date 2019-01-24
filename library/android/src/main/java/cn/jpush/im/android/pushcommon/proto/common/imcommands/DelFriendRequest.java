package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

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
public class DelFriendRequest extends ImBaseRequest {
    private static final String TAG = "DelFriendRequest";
    @Expose
    long targetUid;

    public DelFriendRequest(long targetUid, long rid, long uid) {
        super(IMCommands.DelFriend.CMD, uid, rid);
        this.targetUid = targetUid;
    }

    public static DelFriendRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, DelFriendRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Friend.DelFriend.Builder builder = Friend.DelFriend.newBuilder()
                .setTargetUid(targetUid);

        return new IMProtocol(IMCommands.DelFriend.CMD, IMCommands.DelFriend.VERSION, imUid, appKey,
                builder.build());
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Logger.d(TAG, "remove friend post execute! code = " + imProtocol.getResponse().getCode() + " desc = " + imProtocol.getResponse().getMessage().toStringUtf8());
                if (0 == imProtocol.getResponse().getCode()) {
                    //如果删除好友成功，本地好友关系需要同步更新
                    Logger.d(TAG, "remove friend rep send success . update users isFriend flag . target uid = " + targetUid);
                    UserInfoManager.getInstance().updateIsFriendFlag(targetUid, false);
                    UserInfoManager.getInstance().updateNoteName(targetUid, "");
                    UserInfoManager.getInstance().updateNoteText(targetUid, "");
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, imProtocol.getResponse().getCode(),
                imProtocol.getResponse().getMessage().toStringUtf8()), getExecutor());
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }
}
