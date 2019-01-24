package cn.jpush.im.android.tasks;

import android.text.TextUtils;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;

public class GetTokenTask extends AbstractTask {

    private static final int ERROR_TOKEN_TASK_RUNNING = -101;

    private static AtomicBoolean sIsTokenTaskRunning = new AtomicBoolean(false);

    public GetTokenTask(BasicCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
    }

    private String createGetTokenUrl() {
        if (!JMessage.isTest) {
            return JMessage.httpsUserCenterPrefix + "/token";
        } else {
            return JMessage.httpUserCenterPrefix + "/token";
        }
    }

    @Override
    protected synchronized ResponseWrapper doExecute() throws Exception {
        if (sIsTokenTaskRunning.getAndSet(true)) {
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.responseCode = ERROR_TOKEN_TASK_RUNNING;
            responseWrapper.responseContent = "token task is running";
            return responseWrapper;
        }

        if (!TextUtils.isEmpty(IMConfigs.getUserName()) && !TextUtils
                .isEmpty(IMConfigs.getUserPassword())) {
            String authBase = StringUtils
                    .getBasicAuthorization(String.valueOf(IMConfigs.getUserID()),
                            IMConfigs.getUserPassword());
            ResponseWrapper response;
            try {
                response = mHttpClient.sendGet(createGetTokenUrl(), authBase);
            } catch (APIRequestException e) {
                response = e.getResponseWrapper();
            } catch (APIConnectionException e) {
                response = null;
            }
            return response;

        }
        return null;
    }

    @Override
    protected void doPostExecute(ResponseWrapper result) throws Exception {
        int responseCode = 0;
        String responseMsg = "";
        if (null != result) {
            Logger.i(TAG, "get response : code = " + result.responseCode + " content = "
                    + result.responseContent);
            if (result.responseCode < 300 && result.responseCode >= 200) {
                responseCode = 0;
                responseMsg = "ok";
                Map<String, String> map = JsonUtil.formatToMap(result.responseContent);
                IMConfigs.setToken(map.get("token"));
            } else if (result.responseCode >= 300 && result.responseCode < 400) {
                responseCode = ErrorCode.HTTP_ERROR.HTTP_UNEXPECTED_ERROR;
                responseMsg = ErrorCode.HTTP_ERROR.HTTP_UNEXPECTED_ERROR_DESC;
            } else if (null != result.error && null != result.error.error) {
                responseCode = result.error.error.code;
                responseMsg = result.error.error.message;
            } else {
                responseCode = ErrorCode.HTTP_ERROR.HTTP_SERVER_INTERNAL_ERROR;
                responseMsg = ErrorCode.HTTP_ERROR.HTTP_SERVER_INTERNAL_ERROR_DESC;
            }

            //如果是ERROR_TOKEN_TASK_RUNNING这个错误，表示线程池中有token task正在执行，
            //此时不能将sIsTokenTaskExistsInPool、和sIsTokenTaskRunning置为false.
            if (result.responseCode != ERROR_TOKEN_TASK_RUNNING) {
                sIsTokenTaskExistsInPool.set(false);
                sIsTokenTaskRunning.set(false);
            }

            if (null != mCallback) {
                mCallback.gotResult(responseCode, responseMsg);
            }
        } else if (retryTime < MAX_RETRY_TIME) {
            //Note:因为是重试，task将继续进入线程池等待执行，
            //所以此时只将isTokenTaskRunning置为false,而不将isTokenTaskExistsInPool置为false,
            sIsTokenTaskRunning.set(false);
            doRetry(this, true, true);
        } else if (mCallback != null) {
            //token任务执行完成，将标志位置为false
            sIsTokenTaskExistsInPool.set(false);
            sIsTokenTaskRunning.set(false);
            mCallback.gotResult(ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT, ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT_DESC);
        }
    }
}
