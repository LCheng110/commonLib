package cn.jpush.im.android.tasks;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetUserStatusCallback;
import cn.jpush.im.android.api.model.UserStatus;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.StringUtils;

/**
 * Created by zhaoyuanchao on 2018/11/14.
 */

public class GetUserStatueTask extends AbstractTask {
    //发起聊天的手机号
    private String phone;
    //被聊天人的手机号
    private String bePhone;
    private GetUserStatusCallback callback;
    public GetUserStatueTask(String phone,String bePhone,GetUserStatusCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.callback = callback;
        this.phone = phone;
        this.bePhone = bePhone;
    }

    private String createUrl(){
        return JMessage.httpUserPowerPrefix + "/v1/user/chatCheck";
//        return "http://106.14.160.28:8088/v1/user/chatCheck";
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper responseWrapper = super.doExecute();
        if (null != responseWrapper){
            return responseWrapper;
        }
        Map<String,String> data = new HashMap<String, String>();
        data.put("phone",phone);
        data.put("bePhone",bePhone);
        String requestBoby = JsonUtil.toJson(data);
        String authBase = StringUtils.getBasicAuthorization(String.valueOf(IMConfigs.getUserID()),IMConfigs.getToken());
        try {
            responseWrapper = mHttpClient.sendPost(createUrl(),requestBoby,authBase);
        }catch (APIRequestException e){
            responseWrapper = e.getResponseWrapper();
        }catch (APIConnectionException e){
            responseWrapper = null;
        }

        return responseWrapper;
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        UserStatus userStatus = JsonUtil.fromJson(resultContent, UserStatus.class);
        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR,ErrorCode.NO_ERROR_DESC,userStatus);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        CommonUtils.doCompleteCallBackToUser(callback,responseCode,responseMsg);
    }
}
