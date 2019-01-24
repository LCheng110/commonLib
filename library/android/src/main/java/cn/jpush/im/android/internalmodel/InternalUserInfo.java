package cn.jpush.im.android.internalmodel;

import android.text.TextUtils;

import java.io.File;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.MessageNoDisturbRequest;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.AvatarUtils;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public class InternalUserInfo extends UserInfo implements Cloneable {

    private static final String TAG = "InternalUserInfo";

    public String getBirthdayString() {
        return birthday;
    }

    public String getGenderString() {
        return gender;
    }

    public void setUserID(long userID) {
        this.userID = userID;
    }

    public void setBirthdayString(String birthday) {
        this.birthday = birthday;
    }

    public void setGenderString(String gender) {
        this.gender = gender;
    }

    public void setAvatarMediaID(String mediaID) {
        this.avatarMediaID = mediaID;
    }

    @Override
    public void setUserExtras(Map<String, String> extras) {
        if (!ExpressionValidateUtil.validExtras(extras)) {
            Logger.ee(TAG, "set userExtras failed. extras is invalid.");
            return;
        }

        this.extras.clear();
        this.extras.putAll(extras);
    }

    @Override
    public void setUserExtras(String key, String value) {
        if (null != extras && null == value) {
            extras.remove(key);
        } else if (null != extras) {
            extras.put(key, value);
            if (!ExpressionValidateUtil.validExtras(extras)) {
                Logger.ee(TAG, "set userExtras failed. extra is invalid.");
                extras.remove(key);
            }
        }
    }


    public String getExtrasJson() {
        return JsonUtil.toJson(extras);
    }

    public void setExtrasFromJson(String extraJson) {
        if (null != extraJson) {
            extras = JsonUtil.fromJson(extraJson, ConcurrentHashMap.class);
        }
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setAppkey(String appkey) {
        this.appkey = appkey;
    }

    public void setIsFriend(boolean isFriend) {
        this.isFriend = isFriend ? 1 : 0;
    }

    public void setStar(int star) {
        this.star = star;
    }

    public void setBlacklist(int blacklist) {
        this.blacklist = blacklist;
    }

    public void setmTime(int mTime) {
        this.mTime = mTime;
    }

    @Override
    public String getAppKey() {
        if (null == appkey || TextUtils.isEmpty(appkey.trim())) {
            Logger.ww(TAG, "appkey is null ,return default value.");
            return JCoreInterface.getAppKey();
        }
        return appkey;
    }

    @Override
    public void setBirthday(long timeInMillis) {
        Date date;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        date = new Date(timeInMillis);
        birthday = simpleDateFormat.format(date);
    }

    @Override
    public void setNoDisturb(int noDisturb, BasicCallback callback) {
        if (1 == noDisturb) {
            RequestProcessor.imAddUserToNoDisturb(JMessage.mContext, userID, IMConfigs.getNextRid(),
                    new InnerUserNoDisturbCallback(noDisturb, callback));
        } else {
            RequestProcessor.imDelUserFromNoDisturb(JMessage.mContext, userID, IMConfigs.getNextRid(),
                    new InnerUserNoDisturbCallback(noDisturb, callback));
        }
    }

    private class InnerUserNoDisturbCallback extends BasicCallback {

        private int noDisturb;

        private BasicCallback userCallback;

        InnerUserNoDisturbCallback(int noDisturb, BasicCallback userCallback) {
            this.noDisturb = noDisturb;
            this.userCallback = userCallback;
        }

        @Override
        public void gotResult(int code, String desc) {
            if (1 == noDisturb) {
                if (ErrorCode.NO_ERROR == code
                        || MessageNoDisturbRequest.NO_DISTURB_USER_ALREADY_SET == code) {
                    setNoDisturbInLocal(1);
                }
            } else {
                if (ErrorCode.NO_ERROR == code
                        || MessageNoDisturbRequest.NO_DISTURB_USER_NEVER_SET == code) {
                    setNoDisturbInLocal(0);
                }
            }
            CommonUtils.doCompleteCallBackToUser(userCallback, code, desc);
        }
    }

    public void setNoDisturbInLocal(int noDisturb) {
        this.noDisturb = noDisturb;
    }

    @Override
    public long getBirthday() {
        if (!TextUtils.isEmpty(birthday)) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = simpleDateFormat.parse(birthday);
                return date.getTime();
            } catch (ParseException e) {
                e.printStackTrace();
                return 0l;
            }
        }
        return 0l;
    }

    //返回小头像，之前的保留接口。
    @Override
    public File getAvatarFile() {
        return AvatarUtils.getAvatarFile(avatarMediaID);
    }

    //异步返回小头像File对象，保留接口
    @Override
    public void getAvatarFileAsync(final DownloadAvatarCallback callback) {
        AvatarUtils.getAvatarFileAsync(avatarMediaID, callback);
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
    public void getBigAvatarBitmap(final GetAvatarBitmapCallback callback) {
        AvatarUtils.getBigAvatarBitmap(avatarMediaID, callback);
    }

    @Override
    public int getBlacklist() {
        //local字段使用lazy load
        if (-1 == blacklist) {
            blacklist = UserInfoManager.getInstance().isUserInBlackList(userID);
        }
        return blacklist;
    }

    @Override
    public int getNoDisturb() {
        //local字段使用lazy load
        if (-1 == noDisturb) {
            noDisturb = UserInfoManager.getInstance().isUserInNoDisturb(userID);
        }
        return noDisturb;
    }

    @Override
    public boolean isFriend() {
        //local字段使用lazy load
        if (-1 == isFriend) {
            isFriend = UserInfoManager.getInstance().isUserYourFriend(userID);
        }
        return isFriend == 1;
    }

    @Override
    public String getNotename() {
        //lazy load
        if (null == notename) {
            notename = UserInfoManager.getInstance().queryUserNotename(userID);
        }
        return notename;
    }

    @Override
    public String getNoteText() {
        //lazy load
        if (null == noteText) {
            noteText = UserInfoManager.getInstance().queryUserNoteText(userID);
        }
        return noteText;
    }

    @Override
    public String getDisplayName() {
        return getDisplayName(true);
    }

    public String getDisplayName(boolean includeNoteName) {
        String noteName = getNotename();//noteName是lazyLoad的变量，这里需要通过接口getNotename()来访问，直接访问noteName变量会为空
        if (includeNoteName && !TextUtils.isEmpty(noteName)) {
            return noteName;
        } else if (!TextUtils.isEmpty(nickname)) {
            return nickname;
        } else {
            return userName;
        }
    }

    @Override
    public void removeFromFriendList(final BasicCallback callback) {
        RequestProcessor.imRemoveContact(JMessage.mContext, userID, CommonUtils.getSeqID(), new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                if (ErrorCode.NO_ERROR == responseCode) {
                    isFriend = 0;
                    notename = "";
                    noteText = "";
                }
                CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
            }
        });
    }

    @Override
    public void updateNoteName(final String noteName, final BasicCallback callback) {
        if (!ExpressionValidateUtil.validNullableName(noteName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME_DESC);
            return;
        }

        RequestProcessor.imUpdateMemo(JMessage.mContext, noteName, null, userID, CommonUtils.getSeqID(), new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                if (ErrorCode.NO_ERROR == responseCode) {
                    InternalUserInfo.this.notename = noteName;
                }
                CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
            }
        });
    }

    @Override
    public void updateNoteText(final String noteText, final BasicCallback callback) {
        if (!ExpressionValidateUtil.validNullableInput(noteText)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }

        RequestProcessor.imUpdateMemo(JMessage.mContext, null, noteText, userID, CommonUtils.getSeqID(), new BasicCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage) {
                if (ErrorCode.NO_ERROR == responseCode) {
                    InternalUserInfo.this.noteText = noteText;
                }
                CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
            }
        });
    }

    public void copyUserInfo(InternalUserInfo userInfo, boolean updateBlackList, boolean updateNoDisturb, boolean updateMemo) {
        setUserName(userInfo.getUserName());
        setUserID(userInfo.getUserID());
        setBirthdayString(userInfo.getBirthdayString());
        setBirthday(userInfo.getBirthday());
        setGenderString(userInfo.getGenderString());
        try {
            setGender(Gender.get(Integer.parseInt(userInfo.getGenderString())));
        } catch (NumberFormatException e) {
            Logger.dd(TAG, "user gender not specified, use default.");
            setGender(Gender.unknown);
        }
        setAddress(userInfo.getAddress());
        setAvatarMediaID(userInfo.getAvatar());
        setNickname(userInfo.getNickname());
        setRegion(userInfo.getRegion());
        setSignature(userInfo.getSignature());
        setStar(userInfo.getStar());
        setmTime(userInfo.getmTime());
        if (updateBlackList) {
            setBlacklist(userInfo.getBlacklist());
        }
        if (updateNoDisturb) {
            setNoDisturbInLocal(userInfo.getNoDisturb());
        }
        if (updateMemo) {
            setNotename(userInfo.getNotename());
            setNoteText(userInfo.getNoteText());
            setIsFriend(userInfo.isFriend());
        }
    }

    @Override
    public Object clone() {
        InternalUserInfo o = null;
        try {
            o = (InternalUserInfo) super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.ww(TAG, "clone userinfo failed!");
            e.printStackTrace();
        }
        return o;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "userID=" + userID +
                ", userName='" + userName + '\'' +
                ", nickname='" + nickname + '\'' +
                ", notename='" + notename + '\'' +
                ", noteText='" + noteText + '\'' +
                ", star=" + star +
                ", blacklist=" + blacklist +
                ", nodisturb=" + noDisturb +
                ", avatarMediaID='" + avatarMediaID + '\'' +
                ", birthday='" + birthday + '\'' +
                ", signature='" + signature + '\'' +
                ", gender='" + gender + '\'' +
                ", region='" + region + '\'' +
                ", address='" + address + '\'' +
                ", appkey='" + appkey + '\'' +
                ", isFriend='" + isFriend + '\'' +
                ", mtime='" + mTime + '\'' +
                ", extras='" + extras + '\'' +
                '}';
    }
}
