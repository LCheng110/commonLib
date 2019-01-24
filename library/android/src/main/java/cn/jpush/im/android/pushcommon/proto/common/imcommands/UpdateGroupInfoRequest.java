package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Group.UpdateGroupInfo;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

public class UpdateGroupInfoRequest extends ImBaseRequest {

    private static final String TAG = "UpdateGroupInfoRequest";
    @Expose
    long groupId;
    @Expose
    String name;
    @Expose
    String desc;
    @Expose
    String avatarMediaID;

    public UpdateGroupInfoRequest(long groupId, String name, String desc, String avatarMediaID, long rid, long uid) {
        super(IMCommands.UpdateGroupInfo.CMD, uid, rid);
        this.groupId = groupId;
        this.name = name;
        this.desc = desc;
        this.avatarMediaID = avatarMediaID;
    }

    public static UpdateGroupInfoRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, UpdateGroupInfoRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        UpdateGroupInfo.Builder builder = UpdateGroupInfo.newBuilder()
                .setGid(groupId);
        if (null != name) {
            builder.setName(ByteString.copyFromUtf8(name));
        }
        if (null != desc) {
            builder.setInfo(ByteString.copyFromUtf8(desc));
        }
        if (null != avatarMediaID) {
            builder.setAvatar(ByteString.copyFromUtf8(avatarMediaID));
        }

        return new IMProtocol(IMCommands.UpdateGroupInfo.CMD,
                IMCommands.UpdateGroupInfo.VERSION,
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
                Logger.d(TAG, "update group info finished ! response code is " + responseCode);
                if (responseCode == 0) {
                    ContentValues values = new ContentValues();
                    if (null != name) {
                        values.put(GroupStorage.GROUP_NAME, name);
                    }
                    if (null != desc) {
                        values.put(GroupStorage.GROUP_DESC, desc);
                    }

                    if (null != avatarMediaID) {
                        values.put(GroupStorage.GROUP_AVATAR, avatarMediaID);
                    }

                    GroupStorage.updateValuesInBackground(groupId, values);
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
