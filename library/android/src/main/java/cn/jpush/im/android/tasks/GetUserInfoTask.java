package cn.jpush.im.android.tasks;

import android.text.TextUtils;

import com.google.gson.jpush.reflect.TypeToken;

import java.io.File;
import java.util.List;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.filemng.AvatarDownloader;


public class GetUserInfoTask extends GetUserInfoListTask {

    private static final String TAG = "GetUserInfoTask";

    private String userName;

    private long userID;

    private boolean needDownloadAvatar = true;

    public GetUserInfoTask(String userName, String appkey, GetUserInfoCallback callback,
                           boolean needDownloadAvatar, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.userName = userName;
        userList.add(userName);
        this.needDownloadAvatar = needDownloadAvatar;

        if (TextUtils.isEmpty(appkey) || JCoreInterface.getAppKey().equals(appkey)) {
            //如果appkey是空或者等于当前应用appkey，则不是跨应用请求。
            type = IDType.username;
            this.appkey = JCoreInterface.getAppKey();
        } else {
            type = IDType.cross;
            this.appkey = appkey;
        }
    }

    public GetUserInfoTask(long userID, GetUserInfoCallback callback, boolean needDownloadAvatar,
                           boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.userID = userID;
        userList.add(userID);
        this.needDownloadAvatar = needDownloadAvatar;
        type = IDType.uid;
    }


    @Override
    protected void onSuccess(String resultContent) {
        InternalUserInfo info = null;
        List<InternalUserInfo> userInfoList = JsonUtil.formatToGivenTypeOnlyWithExpose(resultContent,
                new TypeToken<List<InternalUserInfo>>() {
                });
        if (null != userInfoList && userInfoList.size() > 0) {
            info = userInfoList.get(0);
            if (null != info) {
                Logger.d(TAG, "info = " + info.toString());
                //获取单个用户信息时，需要自动下载小头像
                File file = info.getAvatarFile();
                if (needDownloadAvatar && !TextUtils.isEmpty(info.getAvatar()) && null != file && !file
                        .exists()) {
                    new AvatarDownloader().downloadSmallAvatar(info.getAvatar(), new DownloadCallback(info));
                    return;
                }
                UserInfoManager.getInstance().insertOrUpdateUserInfo(info, true, false, false, false);
            } else {
                Logger.ww(TAG, "parse userInfo failed . server return error.");
            }
        } else {
            Logger.ww(TAG, "parse userInfo failed . server return error.");
        }

        CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, info);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        getLocalInfoAndDoCallbackToUser(responseCode, responseMsg);
    }

    private class DownloadCallback extends DownloadAvatarCallback {

        private InternalUserInfo internalUserInfo;

        DownloadCallback(InternalUserInfo info) {
            internalUserInfo = info;
        }

        @Override
        public void gotResult(int responseCode, String responseMessage, File avatar) {
            //不管头像下载是否成功，都需要将userInfo存至数据库
            UserInfoManager.getInstance().insertOrUpdateUserInfo(internalUserInfo, true, false, false, false);
            if (ErrorCode.NO_ERROR == responseCode) {
                CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, internalUserInfo);
            } else {
                getLocalInfoAndDoCallbackToUser(responseCode, responseMessage);
            }
        }
    }

    private void getLocalInfoAndDoCallbackToUser(int statusCode, String msg) {
        InternalUserInfo userInfo;
        if (null != userName) {
            userInfo = UserInfoManager.getInstance().getUserInfo(userName, appkey);
        } else {
            userInfo = UserInfoManager.getInstance().getUserInfo(userID);
        }
        if (null != userInfo) {
            CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfo);
        } else {
            CommonUtils.doCompleteCallBackToUser(mCallback, statusCode, msg);
        }
    }
}
