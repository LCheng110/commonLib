package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Group;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by ${chenyn} on 16/8/1.
 *
 * @desc :设置屏蔽
 */
public class AddGroupToBlockRequest extends GroupBlockRequest {
    private static final String TAG = "AddGroupToBlockRequest";
    @Expose
    long groupId;
    @Expose
    long version;

    public AddGroupToBlockRequest(long groupId, long rid, long uid) {
        super(IMCommands.AddGroupToBlock.CMD,uid,rid);

        this.groupId = groupId;
    }

    public static AddGroupToBlockRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, AddGroupToBlockRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Group.AddMsgshieldGroup addMsgshieldGroup =
                Group.AddMsgshieldGroup.newBuilder()
                        .setGid(groupId)
                        .build();
        return new IMProtocol(IMCommands.AddGroupToBlock.CMD,
                IMCommands.AddGroupToBlock.VERSION,
                imUid, appKey, addMsgshieldGroup);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (0 != groupId && (ErrorCode.NO_ERROR == responseCode
                        || BLOCK_GROUP_ALREADY_SET == responseCode)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(GroupStorage.GROUP_BLOCKED, 1);
                    GroupStorage.updateValuesSync(groupId, contentValues);
                } else {
                    Logger.ww(TAG, "add group to shielding failed. code = " + responseCode);
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }
}
