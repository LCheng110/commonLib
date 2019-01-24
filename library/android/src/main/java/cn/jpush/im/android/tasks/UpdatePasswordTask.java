package cn.jpush.im.android.tasks;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;

public class UpdatePasswordTask extends AbstractTask {

    private String oldPassword;

    private String newPassword;

    private long userUID;

    private BasicCallback callback;

    public UpdatePasswordTask(String oldPassword, String newPassword, long userUID,
                              BasicCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.callback = callback;
        this.oldPassword = StringUtils.toMD5(oldPassword);
        this.newPassword = StringUtils.toMD5(newPassword);
//        this.newPassword = newPassword;
        this.userUID = userUID;
    }

    private String createUrl() {
        return JMessage.httpUserCenterPrefix + "/users/" + userUID + "/password";
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }

        Map<String, String> data = new HashMap<String, String>();
        data.put("old_password", oldPassword);
        data.put("new_password", newPassword);
        String requestBody = JsonUtil.toJson(data);
        String authBase = StringUtils
                .getBasicAuthorization(String.valueOf(IMConfigs.getUserID()), IMConfigs.getToken());
        try {
            response = mHttpClient.sendPut(createUrl(), requestBody, authBase);
            Logger.d(TAG, "Request success, response : " + response.responseContent);
        } catch (APIRequestException e) {
            response = e.getResponseWrapper();
        } catch (APIConnectionException e) {
            response = null;
        }
        return response;
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        IMConfigs.setUserPassword(newPassword);
        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
    }
}
