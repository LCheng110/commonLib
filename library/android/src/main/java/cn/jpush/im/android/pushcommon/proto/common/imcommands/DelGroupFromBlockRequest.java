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
 * @desc :取消屏蔽
 */
public class DelGroupFromBlockRequest extends GroupBlockRequest {
    private static final String TAG = "DelGroupFromBlockRequest";
    @Expose
    long groupId;
    @Expose
    long version;

    public DelGroupFromBlockRequest(long groupId, long rid, long uid) {
        super(IMCommands.DelGroupFromBlock.CMD, uid, rid);

        this.groupId = groupId;
    }

    public static DelGroupFromBlockRequest fromJson(String json) {
        return JsonUtil.fromJson(json, DelGroupFromBlockRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Group.DelMsgshieldGroup delGroupFromShielding =
                Group.DelMsgshieldGroup.newBuilder()
                        .setGid(groupId)
                        .build();
        return new IMProtocol(IMCommands.DelGroupFromBlock.CMD,
                IMCommands.DelGroupFromBlock.VERSION,
                imUid, appKey, delGroupFromShielding);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (0 != groupId && (ErrorCode.NO_ERROR == responseCode
                        || BLOCK_GROUP_NEVER_SET == responseCode)) {
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(GroupStorage.GROUP_BLOCKED, 0);
                    GroupStorage.updateValuesSync(groupId, contentValues);
                } else {
                    Logger.ww(TAG, "del group from shielding failed. code = " + responseCode);
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }
}
