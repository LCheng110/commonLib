package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;

import java.util.List;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Group.AddGroupMember;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class AddGroupMemberRequest extends ImBaseRequest {
    private static final String TAG = "AddGroupMemberRequest";
    @Expose
    long groupId;
    @Expose
    List<Long> uidList;

    public AddGroupMemberRequest(long groupId, List<Long> uidList, long rid, long uid) {
        super(IMCommands.AddGroupMember.CMD,uid,rid);
        this.groupId = groupId;
        this.uidList = uidList;
    }

    public static AddGroupMemberRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, AddGroupMemberRequest.class);
    }

    @Override
    protected IMProtocol toProtocolBuffer(long imUid, String appKey) {
        AddGroupMember addGroupMember = AddGroupMember.newBuilder()
                .setGid(groupId)
                .addAllMemberUidlist(uidList)
                .setMemberCount(uidList.size())
                .build();

        return new IMProtocol(IMCommands.AddGroupMember.CMD,
                IMCommands.AddGroupMember.VERSION,
                imUid, appKey, addGroupMember);
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
                int responseCode = imProtocol.getResponse().getCode();
                if (responseCode == 0) {
                    long groupID = groupId;
                    if ((null == uidList) || (uidList.size() <= 0)) {
                        Logger.d(TAG, "uid list is empty when add group member!");
                        return null;
                    }
                    List<Long> members = GroupStorage.queryMemberUserIdsSync(groupID);
                    if (null != members) {
                        members.addAll(uidList);
                        ContentValues values = new ContentValues();
                        values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(members));
                        if (GroupStorage.updateValuesSync(groupID, values)) {
                            //更新缓存中Conversation的群成员
                            ConversationManager.getInstance().addGroupMemberInCacheWithIds(groupID, uidList);
                        }
                    }
                } else {
                    Logger.d(TAG, "add group members failed ! response code is " + responseCode);
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


