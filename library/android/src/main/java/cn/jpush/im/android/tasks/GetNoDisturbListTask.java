package cn.jpush.im.android.tasks;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.reflect.TypeToken;

import java.util.List;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetNoDisurbListCallback;
import cn.jpush.im.android.api.callback.IntegerCallback;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class GetNoDisturbListTask extends AbstractTask {
    private static final String TAG = "GetNoDisturbListTask";

    private static final int NODISTURB_VERSION = 0;

    private boolean isNoDisturbGlobalRequest = false;

    public GetNoDisturbListTask(GetNoDisurbListCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        isNoDisturbGlobalRequest = false;
    }

    public GetNoDisturbListTask(IntegerCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        isNoDisturbGlobalRequest = true;
    }

    public GetNoDisturbListTask(boolean waitForCompletion) {
        super(null, waitForCompletion);
    }

    private String createUrl() {
        long userID = IMConfigs.getUserID();
        if (0 != userID) {
            return JMessage.httpUserCenterPrefix + "/users/" + userID + "/nodisturb?version=" + NODISTURB_VERSION;
        }
        return null;
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }


        String url = createUrl();
        if (null != url) {
            String authBase = StringUtils.getBasicAuthorization(
                    String.valueOf(IMConfigs.getUserID())
                    , IMConfigs.getToken());
            try {
                response = mHttpClient.sendGet(url, authBase);
            } catch (APIRequestException e) {
                response = e.getResponseWrapper();
            } catch (APIConnectionException e) {
                response = null;
            }
            return response;
        } else {
            Logger.d(TAG, "created url is null!");
            return null;
        }
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        Logger.d(TAG, "on success .result = " + resultContent);
        NoDisturbListEntity entity = JsonUtil.formatToGivenType(resultContent, new TypeToken<NoDisturbListEntity>() {
        });

        if (null == entity) {
            Logger.ww(TAG, "failed to parse response data. entity is null");
            return;
        }

        List<InternalUserInfo> resultUsers = null;
        List<InternalGroupInfo> resultGroups = null;
        UserInfoManager userInfoManager = UserInfoManager.getInstance();

        //设置免打扰状态之前，先将本地所有免打扰状态重置为0。
        userInfoManager.resetNodisturbStatus();
        if (null != entity.getUsers()) {
            resultUsers = entity.getUsers();
            for (InternalUserInfo userInfo : resultUsers) {
                userInfo.setNoDisturbInLocal(1);
            }
            //此处需要更新userinfo中的本地字段--nodisturb
            userInfoManager.insertOrUpdateUserInfo(resultUsers, true, false, true, false);
        }

        //重置所有群的免打扰状态为0
        GroupStorage.resetNodisturbStatusInBackground();
        if (null != entity.getGroups()) {
            resultGroups = entity.getGroups();
            for (InternalGroupInfo groupInfo : entity.getGroups()) {
                groupInfo.setNoDisturbInLocal(1);
                //此处需要更新groupinfo中的本地字段--nodisturb
                GroupStorage.insertOrUpdateWhenExistsInBackground(groupInfo, true, false);
            }
        }

        //设置全局免打扰
        int global = entity.getGlobal();
        IMConfigs.setNodisturbGlobal(global);
        Logger.d(TAG, "get no disturb global , value is " + global);

        if (!isNoDisturbGlobalRequest) {
            CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, resultUsers, resultGroups);
        } else {
            CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, global);
        }
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        Logger.d(TAG, "on error . result code = " + responseCode + " result = " + responseMsg);
        CommonUtils.doCompleteCallBackToUser(mCallback, responseCode, responseMsg);
    }

    private class NoDisturbListEntity {
        @Expose
        private List<InternalUserInfo> users;
        @Expose
        private List<InternalGroupInfo> groups;
        @Expose
        private int global;

        public List<InternalUserInfo> getUsers() {
            return users;
        }

        public List<InternalGroupInfo> getGroups() {
            return groups;
        }

        public int getGlobal() {
            return global;
        }
    }
}
