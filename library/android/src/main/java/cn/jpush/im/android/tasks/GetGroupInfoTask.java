package cn.jpush.im.android.tasks;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.filemng.AvatarDownloader;

public class GetGroupInfoTask extends AbstractTask {

    private long groupID;
    private static final int ERROR_GROUPINFO_NOT_EXIST = 898006;
    private static final int ERROR_NO_AUTH = 898011;
    private boolean needRefreshMembers = false;//是否需要重新完整获取一遍群成员
    private boolean needDownloadAvatar = true;

    private GetGroupInfoCallback callback;

    public GetGroupInfoTask(long groupID, GetGroupInfoCallback callback,
                            boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.groupID = groupID;
        this.callback = callback;
    }

    public GetGroupInfoTask(long groupID, GetGroupInfoCallback callback,
                            boolean needRefreshMembers, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.groupID = groupID;
        this.callback = callback;
        this.needRefreshMembers = needRefreshMembers;
    }

    public GetGroupInfoTask(long groupID, GetGroupInfoCallback callback,
                            boolean needRefreshMembers, boolean needDownloadAvatar, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.groupID = groupID;
        this.callback = callback;
        this.needRefreshMembers = needRefreshMembers;
        this.needDownloadAvatar = needDownloadAvatar;
    }

    private String createGetGroupInfoUrl() {
        return JMessage.httpUserCenterPrefix + "/groups/" + groupID;
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }


        String url = createGetGroupInfoUrl();
        String authBase = StringUtils
                .getBasicAuthorization(String.valueOf(IMConfigs.getUserID()), IMConfigs.getToken());
        try {
            response = mHttpClient.sendGet(url, authBase);
        } catch (APIRequestException e) {
            response = e.getResponseWrapper();
        } catch (APIConnectionException e) {
            response = null;
        }
        return response;
    }

    private void getGroupMember(final InternalGroupInfo groupInfo) {
        new GetGroupMembersTask(groupID, new GetGroupMembersCallback(false) {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                if (0 == responseCode && null != callback) {
                    groupInfo.setOwnerId(GroupStorage.queryOwnerIdSync(groupInfo.getGroupID()));
                    List<Long> userIds = new ArrayList<Long>();
                    for (UserInfo userInfo : members) {
                        userIds.add(userInfo.getUserID());
                    }
                    groupInfo.setGroupMemberUserIds(userIds);
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage, groupInfo);
                } else {
                    getLocalInfoAndDoCallback(responseCode, responseMessage);
                }
            }
        }, false, false).execute();//这里canReturnFromLocal必须为false，否则起不到刷新群成员的效果
    }

    private void getLocalInfoAndDoCallback(int errorCode, String errorMsg) {
        InternalGroupInfo groupInfo = GroupStorage.queryInfoSync(groupID);
        if (null != groupInfo && errorCode != ERROR_GROUPINFO_NOT_EXIST && errorCode != ERROR_NO_AUTH) {
            //如果本地有数据，还是返回成功。
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, groupInfo);
        } else {
            CommonUtils.doCompleteCallBackToUser(callback, errorCode, errorMsg);
        }
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        final InternalGroupInfo groupInfo = JsonUtil.fromJsonOnlyWithExpose(resultContent, InternalGroupInfo.class);
        GroupStorage.insertOrUpdateWhenExistsSync(groupInfo, false, false);

        File avatarFile = groupInfo.getAvatarFile();
        if (needDownloadAvatar && !TextUtils.isEmpty(groupInfo.getAvatar()) && null != avatarFile && !avatarFile
                .exists()) {
            new AvatarDownloader().downloadSmallAvatar(groupInfo.getAvatar(), new DownloadAvatarCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, File avatar) {
                    //不管获取头像是否成功，都需要继续走之后的逻辑
                    getMemberInfoAndCallback(groupInfo);
                }
            });
        } else {
            getMemberInfoAndCallback(groupInfo);
        }
    }

    private void getMemberInfoAndCallback(InternalGroupInfo groupInfo) {
        //如果是第一次获取群信息（还没有群成员信息），则需要先同步群成员列表。
        List<Long> memberUserNames = GroupStorage.queryMemberUserIdsSync(groupID);
        Long ownerID = GroupStorage.queryOwnerIdSync(groupID);
        if (needRefreshMembers || null == memberUserNames || memberUserNames.isEmpty() || 0 == ownerID) {
            //群成员为空或者ownerId为0，这时需要重新获取一遍群成员。
            Logger.d(TAG, "need refresh members list , start get group member task!");
            getGroupMember(groupInfo);
        } else {
            groupInfo.setGroupMemberUserIds(memberUserNames);
            groupInfo.setOwnerId(ownerID);
            ConversationManager.getInstance().updateGroupMemberNamesAndReload(groupID, memberUserNames);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, groupInfo);
        }
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        getLocalInfoAndDoCallback(responseCode, responseMsg);
    }
}
