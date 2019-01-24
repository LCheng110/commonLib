package cn.jpush.im.android.internalmodel;

import android.text.TextUtils;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.Vector;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.GroupBlockRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.MessageNoDisturbRequest;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetGroupMembersTask;
import cn.jpush.im.android.utils.AvatarUtils;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.android.utils.filemng.FileUploader;
import cn.jpush.im.api.BasicCallback;

public class InternalGroupInfo extends GroupInfo {

    public static final String TAG = "InternalGroupInfo";

    protected Set<Long> groupMemberUserIds = new LinkedHashSet<Long>();

    private long ownerId = -1;

    public void set_id(int _id) {
        this._id = _id;
    }

    public void setGroupID(long groupID) {
        this.groupID = groupID;
    }

    public void setGroupOwner(String ownerName) {
        this.groupOwner = ownerName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public void setGroupDescription(String groupDescription) {
        this.groupDescription = groupDescription;
    }

    public void setGroupLevel(int groupLevel) {
        this.groupLevel = groupLevel;
    }

    public void setGroupFlag(int groupFlag) {
        this.groupFlag = groupFlag;
    }

    public void setMaxMemberCount(int maxMemberCount) {
        this.maxMemberCount = maxMemberCount;
    }

    public void setGroupMemberUserIds(Collection<Long> groupMemberUserIds) {
        this.groupMemberUserIds.clear();
        this.groupMemberUserIds.addAll(groupMemberUserIds);
    }


    public Set<Long> getGroupMemberUserIds() {
        return groupMemberUserIds;
    }

    @Override
    public synchronized List<UserInfo> getGroupMembers() {

        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMembers", null)) {
            return null;
        }
        if (null != groupMemberInfos && 0 != groupMemberInfos.size()) {
            return groupMemberInfos;
        } else {
            loadMemberList();
            return groupMemberInfos;
        }
    }

    @Override
    public UserInfo getGroupMemberInfo(String username) {
        return getGroupMemberInfo(username, JCoreInterface.getAppKey());
    }

    /**
     * @param username 指定群成员的username
     * @param appKey   指定的appKey,当appKey空时默认为本应用appKey
     */
    public UserInfo getGroupMemberInfo(String username, String appKey) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMemberInfo", null)) {
            return null;
        }
        if (null == username) {
            Logger.ww(TAG, "username is null ! failed to get across application group member info");
            return null;
        }
        if (TextUtils.isEmpty(appKey)) {
            appKey = JCoreInterface.getAppKey();
        }
        List<UserInfo> memberlist = getGroupMembers();
        try {
            for (UserInfo member : memberlist) {
                if (username.equals(member.getUserName()) && appKey.equals(member.getAppKey())) {
                    return member;
                }
            }
        } catch (NoSuchElementException e) {
            Logger.ee(TAG, "get group member info failed. caused by " + e.getMessage());
        }
        Logger.d(TAG, "can not find group member info with given username and appKey!");
        return null;
    }

    @Override
    public void setNoDisturb(int noDisturb, BasicCallback callback) {
        if (1 == noDisturb) {
            RequestProcessor.imAddGroupToNoDisturb(JMessage.mContext, groupID, IMConfigs.getNextRid(),
                    new InnerGroupNoDisturbCallback(noDisturb, callback));
        } else {
            RequestProcessor.imDelGroupFromNoDisturb(JMessage.mContext, groupID, IMConfigs.getNextRid(),
                    new InnerGroupNoDisturbCallback(noDisturb, callback));
        }
    }

    @Override
    public void setBlockGroupMessage(int blockGroupMessage, BasicCallback callback) {
        if (blockGroupMessage == 1) {
            RequestProcessor.imAddGroupToBlock(JMessage.mContext, groupID, IMConfigs.getNextRid(),
                    new InnerBlockGroupCallback(blockGroupMessage, callback));
        } else {
            RequestProcessor.imDelGroupFromBlock(JMessage.mContext, groupID, IMConfigs.getNextRid(),
                    new InnerBlockGroupCallback(blockGroupMessage, callback));
        }
    }

    @Override
    public int isGroupBlocked() {
        //local字段使用lazy load
        if (-1 == isGroupBlocked) {
            isGroupBlocked = GroupStorage.queryIntValueSync(groupID, GroupStorage.GROUP_BLOCKED);
        }
        return isGroupBlocked;
    }

    public void setAvatarMediaID(String avatarMediaID) {
        this.avatarMediaID = avatarMediaID;
    }

    @Override
    public File getAvatarFile() {
        return AvatarUtils.getAvatarFile(avatarMediaID);
    }

    @Override
    public void getAvatarBitmap(final GetAvatarBitmapCallback callback) {
        AvatarUtils.getAvatarBitmap(avatarMediaID, callback);
    }

    @Override
    public File getBigAvatarFile() {
        return AvatarUtils.getBigAvatarFile(avatarMediaID);
    }

    @Override
    public void getBigAvatarBitmap(GetAvatarBitmapCallback callback) {
        AvatarUtils.getBigAvatarBitmap(avatarMediaID, callback);
    }

    @Override
    public void updateAvatar(final File avatar, String format, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateGroupAvatar", callback)) {
            return;
        }

        if (null == avatar || !avatar.exists()) {
            Logger.ee(TAG, "avatar file not exists");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        new FileUploader().doUploadAvatar(avatar, format, new FileUploader.UploadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, final String mediaID) {
                if (ErrorCode.NO_ERROR == responseCode && null != mediaID) {
                    RequestProcessor.imUpdateGroupInfo(JMessage.mContext, groupID, null, null, mediaID, CommonUtils.getSeqID(), new BasicCallback(false) {
                        @Override
                        public void gotResult(int responseCode, String responseMessage) {
                            if (ErrorCode.NO_ERROR == responseCode) {
                                InternalGroupInfo.this.setAvatarMediaID(mediaID);
                                //将头像文件拷贝到应用内部目录下
                                try {
                                    FileUtil.copyFileUsingStream(avatar, new File(FileUtil.getBigAvatarFilePath(mediaID)));
                                } catch (IOException e) {
                                    Logger.d(TAG, "copy avatar to app file path failed.", e);
                                }
                            }
                            CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
                        }
                    });
                } else {
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
                }
            }
        });
    }

    @Override
    public void updateName(final String groupName, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateGroupName", callback)) {
            return;
        }
        if (!CommonUtils.validateStrings("updateGroupName", groupName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        if (!ExpressionValidateUtil.validOtherNames(groupName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME_DESC);
            return;
        }
        RequestProcessor.imUpdateGroupInfo(JMessage.mContext, groupID, groupName, null, null, CommonUtils.getSeqID(), new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                if (ErrorCode.NO_ERROR == responseCode) {
                    InternalGroupInfo.this.setGroupName(groupName);
                }
                CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
            }
        });
    }

    @Override
    public void updateDescription(final String groupDesc, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateGroupDescription", callback)) {
            return;
        }

        if (!CommonUtils.validateStrings("updateGroupName", groupDesc)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }

        if (!ExpressionValidateUtil.validOthers(groupDesc)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }
        RequestProcessor.imUpdateGroupInfo(JMessage.mContext, groupID, null, groupDesc, null, CommonUtils.getSeqID(), new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                if (ErrorCode.NO_ERROR == responseCode) {
                    InternalGroupInfo.this.setGroupDescription(groupDesc);
                }
                CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
            }
        });
    }

    private class InnerGroupNoDisturbCallback extends BasicCallback {

        private int noDisturb;

        private BasicCallback userCallback;

        public InnerGroupNoDisturbCallback(int noDisturb, BasicCallback userCallback) {
            this.noDisturb = noDisturb;
            this.userCallback = userCallback;
        }

        @Override
        public void gotResult(int code, String desc) {
            if (1 == noDisturb) {
                //如果code是0或者返回重复添加的错误码，说明群组已经被加入到了免打扰列表。
                if (ErrorCode.NO_ERROR == code
                        || MessageNoDisturbRequest.NO_DISTURB_GROUP_ALREADY_SET == code) {
                    setNoDisturbInLocal(1);
                }
            } else {
                //如果code是0或者返回群组不在免打扰列表的错误码，说明群组已经从免打扰列表移除。
                if (ErrorCode.NO_ERROR == code
                        || MessageNoDisturbRequest.NO_DISTURB_GROUP_NEVER_SET == code) {
                    setNoDisturbInLocal(0);
                }
            }
            CommonUtils.doCompleteCallBackToUser(userCallback, code, desc);
        }
    }

    private class InnerBlockGroupCallback extends BasicCallback {

        private int blockGroupMessage;
        private BasicCallback blockGroupCallback;

        public InnerBlockGroupCallback(int blockGroupMessage, BasicCallback blockGroupCallback) {
            this.blockGroupMessage = blockGroupMessage;
            this.blockGroupCallback = blockGroupCallback;
        }

        @Override
        public void gotResult(int responseCode, String responseMessage) {
            if (blockGroupMessage == 1) {
                if (ErrorCode.NO_ERROR == responseCode
                        || GroupBlockRequest.BLOCK_GROUP_ALREADY_SET == responseCode) {
                    setBlockGroupInLocal(1);
                }
            } else {
                if (ErrorCode.NO_ERROR == responseCode
                        || GroupBlockRequest.BLOCK_GROUP_NEVER_SET == responseCode) {
                    setBlockGroupInLocal(0);
                }
            }
            CommonUtils.doCompleteCallBackToUser(blockGroupCallback, responseCode, responseMessage);
        }
    }

    public void setNoDisturbInLocal(int noDisturb) {
        this.noDisturb = noDisturb;
    }

    public void setBlockGroupInLocal(int blockGroup) {
        this.isGroupBlocked = blockGroup;
    }

    @Override
    public int getNoDisturb() {
        //local字段使用lazy load
        if (-1 == noDisturb) {
            noDisturb = GroupStorage.queryIntValueSync(groupID, GroupStorage.GROUP_NODISTURB);
        }
        return noDisturb;
    }

    @Override
    public String getGroupOwner() {
        return UserIDHelper.getUserNameFromLocal(getOwnerId());
    }

    @Override
    public String getOwnerAppkey() {
        return UserIDHelper.getUserAppkeyFromLocal(getOwnerId());
    }

    private long getOwnerId() {
        //local字段使用lazy load
        if (-1 == ownerId) {
            ownerId = GroupStorage.queryOwnerIdSync(groupID);
        }
        return ownerId;
    }

    public void setOwnerId(long ownerId) {
        this.ownerId = ownerId;
    }

    public synchronized void loadMemberList() {
        if (null != groupMemberInfos) {
            groupMemberInfos.clear();
        } else {
            groupMemberInfos = new Vector<UserInfo>();
        }
        List<InternalUserInfo> members = UserInfoManager.getInstance().getUserInfoList(groupMemberUserIds);
        if (null != members && groupMemberUserIds.size() == members.size()) {
            groupMemberInfos.addAll(members);
        } else {
            //get group members from server then update.
            new GetGroupMembersTask(groupID, new GetGroupMembersCallback(false) {
                @Override
                public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                    if (0 == responseCode && groupMemberInfos.size() == 0) {
                        //只有memberinfo list的size是0时才做插入动作。否则可能引起重复插入。
                        groupMemberInfos.addAll(members);
                    } else {
                        Logger.d(TAG, "get group members failed !");
                    }
                }
            }, false, true).execute();
        }
    }

    public boolean addMemberToList(UserInfo memberInfo) {
        if (null != groupMemberInfos && null != memberInfo) {
            for (UserInfo info : groupMemberInfos) {
                if (info.getUserID() == memberInfo.getUserID()) {
                    Logger.d(TAG, "member already exists in member info list,return from addMemberToList");
                    return false;
                }
            }
            groupMemberInfos.add(memberInfo);
            Logger.d(TAG, "add member to list success !");
            return true;
        } else {
            return false;
        }
    }

    public boolean addMemberIdToNameList(List<Long> userIds) {
        if (null != groupMemberUserIds && null != userIds) {
            groupMemberUserIds.addAll(userIds);
            Logger.d(TAG, "add member id to list success !");
            return true;
        } else {
            return false;
        }
    }

    public boolean removeMemberFromList(Long userId) {
        if (null != groupMemberInfos) {
            for (UserInfo info : groupMemberInfos) {
                if (info.getUserID() == userId) {
                    groupMemberInfos.remove(info);
                    Logger.d(TAG, "remove member from list success !");
                    return true;
                }
            }
        }
        return false;
    }

    public boolean removeMemberIdFromIdList(List<Long> userIds) {
        if (null != groupMemberUserIds) {
            groupMemberUserIds.removeAll(userIds);
            Logger.d(TAG, "remove member name from name list success !");
            return true;
        }
        return false;
    }

    void copyGroupInfo(InternalGroupInfo groupInfo, boolean updateLocalFields) {
        setGroupID(groupInfo.groupID);
        setOwnerId(groupInfo.ownerId);
        setMaxMemberCount(groupInfo.maxMemberCount);
        setGroupDescription(groupInfo.groupDescription);
        setGroupFlag(groupInfo.groupFlag);
        setGroupLevel(groupInfo.groupLevel);
        setGroupMemberUserIds(groupInfo.groupMemberUserIds);
        setGroupName(groupInfo.groupName);
        setGroupOwner(groupInfo.groupName);
        set_id(groupInfo._id);
        if (updateLocalFields) {
            setNoDisturbInLocal(groupInfo.noDisturb);
            setBlockGroupInLocal(groupInfo.isGroupBlocked);
        }
    }

    @Override
    public String toString() {
        return "Group{" +
                "_id=" + _id +
                ", groupID=" + groupID +
                ", groupOwnerID='" + ownerId + '\'' +
                ", groupName='" + groupName + '\'' +
                ", groupDescription='" + groupDescription + '\'' +
                ", groupLevel=" + groupLevel +
                ", groupFlag=" + groupFlag +
                ", maxMemberCount=" + maxMemberCount +
                ", groupMembers=" + groupMemberUserIds +
                ", groupAvatar=" + avatarMediaID +
                '}';
    }
}
