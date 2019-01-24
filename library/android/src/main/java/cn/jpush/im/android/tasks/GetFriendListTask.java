package cn.jpush.im.android.tasks;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.List;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class GetFriendListTask extends AbstractTask {
    private static final String TAG = "GetFriendListTask";


    public GetFriendListTask(GetUserInfoListCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
    }

    private String createUrl() {
        long uid = IMConfigs.getUserID();
        if (0 > uid) {
            Logger.ww(TAG, "create get friend list url failed. invalid uid");
            return null;
        }
        return JMessage.httpUserCenterPrefix + "/users/" + uid + "/friends";
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
        final List<InternalUserInfo> userInfoList = JsonUtil.formatToGivenTypeOnlyWithExpose(resultContent,
                new TypeToken<List<InternalUserInfo>>() {
                });

        UserInfoManager.getInstance().resetFriendRelated();
        for (InternalUserInfo userInfo : userInfoList) {
            userInfo.setIsFriend(true);
        }
        //获取好友列表时，需要更新用户的备注信息，同时也要更新会话列表
        UserInfoManager.getInstance().insertOrUpdateUserInfo(userInfoList, true, false, false, true);

        if (waitForCompletion) {
            //如果需要等待，则callback也需要在调用线程中执行
            CommonUtils.doCompleteCallBackToUser(true, mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfoList);
        } else {
            CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfoList);
        }
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        List<UserInfo> friendList = UserInfoManager.getInstance().getFriendList();

        if (null != friendList) {
            //服务器返回错误，但是本地有数据，返回本地数据。
            CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, friendList);
        } else if (waitForCompletion) {
            CommonUtils.doCompleteCallBackToUser(true, mCallback, responseCode,
                    responseMsg);
        } else {
            CommonUtils.doCompleteCallBackToUser(mCallback, responseCode,
                    responseMsg);
        }
    }

}
