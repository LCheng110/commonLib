package cn.jpush.im.android.tasks;

import java.util.Map;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;


public class UpdateUserInfoTask extends AbstractTask {

    private static final String KEY_MTIME = "mtime";

    private long mUID;

    private Map<String, Object> values;

    private boolean isUpdateAll;

    private BasicCallback mCallback;

    public UpdateUserInfoTask(long userUID, Map<String, Object> values, boolean isUpdateAll, BasicCallback callback,
                              boolean waitForCompletion) {
        super(callback, waitForCompletion);
        mUID = userUID;
        this.values = values;
        this.isUpdateAll = isUpdateAll;
        mCallback = callback;
    }

    private String createUrl() {
        return JMessage.httpUserCenterPrefix + "/users/" + mUID;
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }


        String requestBody = JsonUtil.toJson(values);
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

    private void updateLocalInfo(int mTime) {
        if (isUpdateAll) {
            UserInfoManager.getInstance().updateAllPublicInfos(mUID, values);
        } else if (values.containsKey(UserInfo.Field.nickname.toString())) {
            UserInfoManager.getInstance().updateNickName(mUID, (String) values.get(UserInfo.Field.nickname.toString()));
        } else if (values.containsKey(UserInfo.Field.birthday.toString())) {
            UserInfoManager.getInstance().updateBirthday(mUID, (String) values.get(UserInfo.Field.birthday.toString()));
        } else if (values.containsKey(UserInfo.Field.gender.toString())) {
            UserInfoManager.getInstance().updateGender(mUID, UserInfo.Gender.get((Integer) values.get(UserInfo.Field.gender.toString())));
        } else if (values.containsKey(UserInfo.Field.region.toString())) {
            UserInfoManager.getInstance().updateRegion(mUID, (String) values.get(UserInfo.Field.region.toString()));
        } else if (values.containsKey(UserInfo.Field.signature.toString())) {
            UserInfoManager.getInstance().updateSignature(mUID, (String) values.get(UserInfo.Field.signature.toString()));
        } else if (values.containsKey(UserInfo.Field.address.toString())) {
            UserInfoManager.getInstance().updateAddress(mUID, (String) values.get(UserInfo.Field.address.toString()));
        } else if (values.containsKey(UserInfo.Field.extras.toString())) {
            UserInfoManager.getInstance().updateExtras(mUID, JsonUtil.toJson(values.get(UserInfo.Field.extras.toString())));
        }
        //用户信息更新完成后需要更新userinfo的mtime
        UserInfoManager.getInstance().updateMTime(mUID, mTime);
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        String mTimeString = JsonUtil.formatToMap(resultContent).get(KEY_MTIME);
        updateLocalInfo(Integer.valueOf(mTimeString));
        CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        CommonUtils.doCompleteCallBackToUser(mCallback, responseCode, responseMsg);
    }
}
