package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Message.AddMsgnoDisturbGroup;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class AddMsgnoDisturbGroupRequest extends MessageNoDisturbRequest {
    private static final String TAG = "AddMsgnoDisturbGroupRequest";
    @Expose
    long groupId;
    @Expose
    long version;

    public AddMsgnoDisturbGroupRequest(long groupId, long rid, long uid) {
        super(IMCommands.AddMsgnoDisturbGroup.CMD, uid, rid);

        this.groupId = groupId;
    }

    public static AddMsgnoDisturbGroupRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, AddMsgnoDisturbGroupRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        AddMsgnoDisturbGroup addMsgnoDisturbGroup =
                AddMsgnoDisturbGroup.newBuilder()
                        .setGid(groupId)
                        .build();
        return new IMProtocol(IMCommands.AddMsgnoDisturbGroup.CMD,
                IMCommands.AddMsgnoDisturbGroup.VERSION,
                imUid, appKey, addMsgnoDisturbGroup);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (0 != groupId && (ErrorCode.NO_ERROR == responseCode
                        || NO_DISTURB_GROUP_ALREADY_SET == responseCode)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(GroupStorage.GROUP_NODISTURB, 1);
                    GroupStorage.updateValuesSync(groupId, contentValues);
                } else {
                    Logger.ww(TAG, "add group to no-disturb failed. code = " + responseCode);
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }
}
