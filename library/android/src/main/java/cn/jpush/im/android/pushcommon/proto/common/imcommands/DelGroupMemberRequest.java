package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;

import java.util.List;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Group.DelGroupMember;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class DelGroupMemberRequest extends ImBaseRequest {

    private static final String TAG = "DelGroupMemberRequest";
    @Expose
    long groupId;
    @Expose
    List<Long> uidList;

    public DelGroupMemberRequest(long groupId, List<Long> uidList, long rid, long uid) {
        super(IMCommands.DelGroupMember.CMD, uid, rid);
        this.groupId = groupId;
        this.uidList = uidList;
    }

    public static DelGroupMemberRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, DelGroupMemberRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        DelGroupMember del = DelGroupMember.newBuilder()
                .setGid(groupId)
                .setMemberCount(uidList.size())
                .addAllMemberUidlist(uidList)
                .build();
        return new IMProtocol(IMCommands.DelGroupMember.CMD,
                IMCommands.DelGroupMember.VERSION,
                imUid, appKey, del);
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
                    if ((null == uidList) || (uidList.size() <= 0)) {
                        Logger.d(TAG, "uid list is empty when del group member!");
                        return null;
                    }
                    Logger.d(TAG, "del uid:" + uidList);
                    List<Long> members = GroupStorage.queryMemberUserIdsSync(groupId);
                    if (null != members) {
                        members.removeAll(uidList);
                        ContentValues values = new ContentValues();
                        values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(members));
                        if (GroupStorage.updateValuesSync(groupId, values)) {
                            //更新缓存中Conversation的群成员
                            ConversationManager.getInstance().removeGroupMemberFromCache(groupId, uidList);
                        }
                    }
                } else {
                    Logger.d(TAG, "del group members failed ! response code is " + responseCode);
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
