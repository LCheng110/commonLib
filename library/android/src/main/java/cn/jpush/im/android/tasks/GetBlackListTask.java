package cn.jpush.im.android.tasks;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.List;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetBlacklistCallback;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class GetBlackListTask extends AbstractTask {

    public GetBlackListTask(GetBlacklistCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
    }

    private String createUrl() {
        long userID = IMConfigs.getUserID();
        if (0 != userID) {
            return JMessage.httpUserCenterPrefix + "/users/" + userID + "/blacklist";
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
        List<InternalUserInfo> userInfoList = JsonUtil.formatToGivenTypeOnlyWithExpose(resultContent,
                new TypeToken<List<InternalUserInfo>>() {
                });
        //设置黑名单之前，先将本地所有黑名单状态重置为0
        UserInfoManager infoManager = UserInfoManager.getInstance();
        infoManager.resetBlacklistStatus();

        for (InternalUserInfo userInfo : userInfoList) {
            userInfo.setBlacklist(1);
        }
        infoManager.insertOrUpdateUserInfo(userInfoList, true, true, false, false);

        CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfoList);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        CommonUtils.doCompleteCallBackToUser(mCallback, responseCode, responseMsg);
    }

}
