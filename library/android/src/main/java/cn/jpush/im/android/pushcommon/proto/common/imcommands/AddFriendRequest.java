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
 * Created by wangmeng on 15/11/19.
 */
public class AddFriendRequest extends ImBaseRequest {
    private static final String TAG = "AddFriendRequest";
    @Expose
    long targetUid;
    @Expose
    String memoName;
    @Expose
    String memoOthers;
    @Expose
    int fromType;
    @Expose
    String why;

    public AddFriendRequest(long targetUid, String memoName, String memoOthers, int fromType,
                            String why, long rid, long uid) {
        super(IMCommands.AddFriend.CMD,uid,rid);
        this.targetUid = targetUid;
        this.memoName = memoName;
        this.memoOthers = memoOthers;
        this.fromType = fromType;
        this.why = why;
    }

    public static AddFriendRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, AddFriendRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Friend.AddFriend.Builder builder = Friend.AddFriend.newBuilder()
                .setTargetUid(targetUid)
                .setFromType(fromType);
        if (null != memoName) {
            builder.setMemoName(ByteString.copyFromUtf8(memoName));
        }
        if (null != memoOthers) {
            builder.setMemoOthers(ByteString.copyFromUtf8(memoOthers));
        }
        if (null != why) {
            builder.setWhy(ByteString.copyFromUtf8(why));
        }

        return new IMProtocol(IMCommands.AddFriend.CMD, IMCommands.AddFriend.VERSION,
                imUid, appKey, builder.build());
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
                int respCode = imProtocol.getResponse().getCode();
                Logger.d(TAG, "add friend post execute! code = " + respCode + " desc = " + imProtocol.getResponse().getMessage().toStringUtf8());
                //如果发送的是同意好友请求的响应。则将好友关系更新到数据库
                if (2 == fromType &&
                        TextUtils.isEmpty(why) && 0 == respCode) {
                    Logger.d(TAG, "accept resp send success . update users isFriend flag . target uid = " + targetUid);
                    UserInfoManager.getInstance().updateIsFriendFlag(targetUid, true);
                }

                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, imProtocol.getResponse().getCode(), imProtocol.getResponse().getMessage().toStringUtf8()), getExecutor());
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }
}
