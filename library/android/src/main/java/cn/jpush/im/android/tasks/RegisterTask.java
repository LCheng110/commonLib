package cn.jpush.im.android.tasks;

import java.util.HashMap;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;

public class RegisterTask extends AbstractTask {

    public final static String TAG = "RegisterTask";

    private final static int ERROR_PUSH_UNFINISHED = -200;

    private String userId;

    private String password;

    private Map<String, Object> optionalRequestMap;

    private BasicCallback callback;

    public RegisterTask(String userId, String password, Map<String, Object> optionalRequestMap, BasicCallback callback,
                        boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.userId = userId;
        this.password = StringUtils.toMD5(password);
        this.optionalRequestMap = optionalRequestMap;
        this.callback = callback;
    }

    private String createRegisterUrl() {
        return JMessage.httpsUserCenterPrefix + "/users";
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        //检查push的registerCode,来判断是否需要发起注册
        if (!RequestProcessor.needSendRequest(JMessage.mContext, callback)) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.responseCode = ERROR_PUSH_UNFINISHED;
            return responseWrapper;
        }
        Map<String, Object> data = new HashMap<String, Object>();
        data.put("username", userId);
        data.put("password", password);
        data.put("appkey", JCoreInterface.getAppKey());
        if (optionalRequestMap != null) {
            data.putAll(optionalRequestMap);
        }

        String content = JsonUtil.toJson(data);
        ResponseWrapper response;
        try {
            response = mHttpClient.sendPost(createRegisterUrl(), content, "");
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
        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
    }
}
