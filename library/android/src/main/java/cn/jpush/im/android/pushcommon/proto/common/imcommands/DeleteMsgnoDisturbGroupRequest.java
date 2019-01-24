package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Message.DeleteMsgnoDisturbGroup;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class DeleteMsgnoDisturbGroupRequest extends MessageNoDisturbRequest {

    private static final String TAG = "DeleteMsgnoDisturbGroupRequest";
    @Expose
    long groupId;
    @Expose
    long version;

    public DeleteMsgnoDisturbGroupRequest(long groupId, long rid, long uid) {
        super(IMCommands.DeleteMsgnoDisturbGroup.CMD, uid, rid);

        this.groupId = groupId;
    }

    public static DeleteMsgnoDisturbGroupRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, DeleteMsgnoDisturbGroupRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        DeleteMsgnoDisturbGroup deleteMsgnoDisturbGroup =
                DeleteMsgnoDisturbGroup.newBuilder()
                        .setGid(groupId)
                        .build();
        return new IMProtocol(IMCommands.DeleteMsgnoDisturbGroup.CMD,
                IMCommands.DeleteMsgnoDisturbGroup.VERSION,
                imUid, appKey, deleteMsgnoDisturbGroup);
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (0 != groupId && (ErrorCode.NO_ERROR == responseCode
                        || NO_DISTURB_GROUP_NEVER_SET == responseCode)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(GroupStorage.GROUP_NODISTURB, 0);
                    GroupStorage.updateValuesSync(groupId, contentValues);
                } else {
                    Logger.ww(TAG, "del group from no-disturb failed. code = " + responseCode);
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }
}
