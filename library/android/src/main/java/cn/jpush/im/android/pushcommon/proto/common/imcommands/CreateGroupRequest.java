package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.pushcommon.proto.Group.CreateGroup;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class CreateGroupRequest extends ImBaseRequest {

    private static final String TAG = "CreateGroupRequest";
    @Expose
    String name;
    @Expose
    String desc;
    @Expose
    int flag;
    @Expose
    int level;
    @Expose
    String avatarMediaID;

    public CreateGroupRequest(String name, String desc, int flag, int level, String avatarMediaID, long rid, long uid) {
        super(IMCommands.CreateGroup.CMD, uid, rid);
        this.name = name;
        this.desc = desc;
        this.flag = flag;
        this.level = level;
        this.avatarMediaID = avatarMediaID;
    }


    public static CreateGroupRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, CreateGroupRequest.class);
    }


    @Override
    protected IMProtocol toProtocolBuffer(long imUid, String appKey) {
        CreateGroup.Builder builder = CreateGroup.newBuilder()
                .setFlag(flag)
                .setGroupLevel(level);
        if (null != name) {
            builder.setGroupName(ByteString.copyFromUtf8(name));
        }
        if (null != desc) {
            builder.setGroupDesc(ByteString.copyFromUtf8(desc));
        }

        if (null != avatarMediaID) {
            builder.setAvatar(ByteString.copyFromUtf8(avatarMediaID));
        }

        return new IMProtocol(IMCommands.CreateGroup.CMD,
                IMCommands.CreateGroup.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        CommonUtils.doCompleteCallBackToUser(getCallback(), ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT,
                ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC, 0L);
        RequestProcessor.requestsCache.remove(rid);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        Task.callInBackground(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                long groupID = 0L;
                int responseCode = imProtocol.getResponse().getCode();
                if (ErrorCode.NO_ERROR == responseCode) {
                    CreateGroup createGroup
                            = (CreateGroup) imProtocol.getEntity();
                    groupID = createGroup.getGid();
                    Logger.d(TAG, "groupID:" + groupID + "----createGroup:" + createGroup);
                    InternalGroupInfo groupInfo = new InternalGroupInfo();
                    groupInfo.setGroupID(groupID);
                    groupInfo.setGroupDescription(desc);
                    groupInfo.setGroupLevel(level);
                    groupInfo.setGroupFlag(flag);
                    groupInfo.setGroupName(name);
                    groupInfo.setAvatarMediaID(avatarMediaID);
                    GroupStorage.insertSync(groupInfo);

                    //创建一个群时默认在本地数据库中把自己加入群组
                    long myUid = IMConfigs.getUserID();
                    List<Long> memberIDs = new ArrayList<Long>();
                    memberIDs.add(myUid);
                    //群成员和owner需要单独更新, 因为insert时仅仅插入了除member和owner以外的信息。
                    final ContentValues values = new ContentValues();
                    values.put(GroupStorage.GROUP_OWNER_ID, myUid);
                    values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(memberIDs));
                    GroupStorage.updateValuesSync(groupID, values);
                } else {
                    Logger.d(TAG, "create group failed ! response code is " + responseCode);
                }
                return groupID;
            }
        }).continueWith(new Continuation<Long, Void>() {
            @Override
            public Void then(Task<Long> task) throws Exception {
                CommonUtils.doCompleteCallBackToUser(callback, imProtocol.getResponse().getCode(),
                        imProtocol.getResponse().getMessage().toStringUtf8(), task.getResult());
                RequestProcessor.requestsCache.remove(rid);
                return null;
            }
        }, getExecutor());
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg, 0L);
        RequestProcessor.requestsCache.remove(rid);
    }

}
