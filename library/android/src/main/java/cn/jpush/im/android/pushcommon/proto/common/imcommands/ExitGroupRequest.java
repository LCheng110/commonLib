package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;

import java.util.List;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Group.ExitGroup;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class ExitGroupRequest extends ImBaseRequest {

    private static final String TAG = "ExitGroupRequest";
    @Expose
    long groupId;

    public ExitGroupRequest(long groupId, long rid, long uid) {
        super(IMCommands.ExitGroup.CMD, uid, rid);
        this.groupId = groupId;
    }

    public static ExitGroupRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, ExitGroupRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        ExitGroup exit = ExitGroup.newBuilder()
                .setGid(groupId)
                .build();
        return new IMProtocol(IMCommands.ExitGroup.CMD,
                IMCommands.ExitGroup.VERSION,
                imUid, appKey, exit);
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (responseCode == 0) {
                    List<Long> members = GroupStorage.queryMemberUserIdsSync(groupId);
                    if (null != members) {
                        long myUid = IMConfigs.getUserID();
                        //还原自己在这个群的屏蔽状态和免打扰状态(此操作要在主动退群和被踢出群两处都进行处理)
                        ContentValues contentValues = new ContentValues();
                        contentValues.put(GroupStorage.GROUP_BLOCKED, 0);
                        contentValues.put(GroupStorage.GROUP_NODISTURB, 0);
                        GroupStorage.updateValuesInBackground(groupId, contentValues);

                        //从群成员中删除自己
                        members.remove(myUid);
                        ContentValues values = new ContentValues();
                        //更新groupinfo中owner信息
                        if (0 < members.size()) {
                            values.put(GroupStorage.GROUP_OWNER_ID, members.get(0));
                        }
                        //更新groupinfo 中群成员信息
                        values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(members));
                        GroupStorage.updateValuesInBackground(groupId, values);
                    }
                } else {
                    Logger.d(TAG, "exit group failed ! response code is " + responseCode);
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
