package cn.jpush.im.android.tasks;


import android.text.TextUtils;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;

public class GetUserInfoListTask extends AbstractTask {

    private static final String TAG = "GetUserInfoListTask";

    private static final int PAGE_SIZE = 2500;//分页请求每页请求的数量

    public enum IDType {
        username, uid, cross
    }

    List<Object> userList;

    protected IDType type;

    protected String appkey;

    private List<InternalUserInfo> resultInfoList = new ArrayList<InternalUserInfo>();

    protected GetUserInfoListTask(BasicCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        userList = new ArrayList<Object>();
    }

    public GetUserInfoListTask(List<Object> userList, IDType type, GetUserInfoListCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.userList = userList;
        this.type = type;
    }

    public GetUserInfoListTask(List<Object> userList, String appkey, GetUserInfoListCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.userList = userList;
        if (TextUtils.isEmpty(appkey) || JCoreInterface.getAppKey().equals(appkey)) {
            //如果appkey是空或者等于当前应用appkey，则不是跨应用请求。
            type = IDType.username;
        } else {
            type = IDType.cross;
            this.appkey = appkey;
        }
    }

    protected String createUrl() {
        if (null == userList || null == type) {
            return null;
        }

        String url = JMessage.httpUserCenterPrefix + "/users/batch?idtype=" + type.toString();
        if (type == IDType.cross) {
            url += "&appkey=" + appkey;
        }
        return url;
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper result = super.doExecute();
        if (null != result) {
            return result;
        }

        final List<Task<ResponseWrapper>> tasks = createTasks();
        if (null != tasks) {
            Task<ResponseWrapper> task = Task.whenAll(tasks).continueWith(new Continuation<Void, ResponseWrapper>() {
                @Override
                public ResponseWrapper then(Task<Void> task) throws Exception {
                    ResponseWrapper responseWrapper = null;
                    int rCode;
                    String rContent;
                    for (Task<ResponseWrapper> t : tasks) {
                        if (null != t.getResult()) {
                            rCode = t.getResult().responseCode;
                            rContent = t.getResult().responseContent;
                            if (rCode >= 200 && rCode < 300) {
                                //针对每个分页请求的返回，分别将result parse成InternalUserInfo对象。
                                Logger.d(TAG, "get userinfo by page success. response content = " + rContent);
                                resultInfoList.addAll(JsonUtil.formatToGivenTypeOnlyWithExpose(rContent,
                                        new TypeToken<List<InternalUserInfo>>() {
                                        }));
                            } else {
                                Logger.d(TAG, "get userinfo by page failed . code = " + rCode + " content = " + rContent);
                            }
                            responseWrapper = t.getResult();
                        } else {
                            Logger.ww(TAG, "get userinfo by page failed task result is null");
                            responseWrapper = new ResponseWrapper();
                            responseWrapper.responseCode = ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS;
                            responseWrapper.responseContent = ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC;
                        }
                    }
                    return responseWrapper;
                }
            });

            task.waitForCompletion();//等待所有的分页获取的异步任务返回。
            result = task.getResult();
        }
        return result;
    }

    /**
     * 将整个userList切分为多页多个异步任务分别请求，防止一次请求的数据量过大。
     * 所有的分页请求中，有一个失败则全局失败。
     *
     * @return 生成的分页获取的异步任务
     */
    private List<Task<ResponseWrapper>> createTasks() {
        if (null == userList || 0 == userList.size() || null == type) {
            return null;
        }
        Logger.d(TAG, " request by page , user list = " + userList);
        final List<Task<ResponseWrapper>> tasks = new ArrayList<Task<ResponseWrapper>>();
        final String url = createUrl();

        int size = userList.size();
        int curPage = 0;    //当前页数(从0开始)
        int pages = 0 == size % PAGE_SIZE ? size / PAGE_SIZE : (size / PAGE_SIZE) + 1; //总页数

        for (; curPage < pages; curPage++) {
            final int start = curPage * PAGE_SIZE;
            final int end = (size - curPage * PAGE_SIZE) > PAGE_SIZE ? (curPage + 1) * PAGE_SIZE : size;
            tasks.add(Task.callInBackground(new Callable<ResponseWrapper>() {
                @Override
                public ResponseWrapper call() throws Exception {
                    if (!TextUtils.isEmpty(url)) {
                        String authBase = StringUtils.getBasicAuthorization(
                                String.valueOf(IMConfigs.getUserID())
                                , IMConfigs.getToken());
                        ResponseWrapper response;
                        try {
                            response = mHttpClient.sendPost(url, JsonUtil.toJson(userList.subList(start, end)), authBase);
                        } catch (APIRequestException e) {
                            response = e.getResponseWrapper();
                        } catch (APIConnectionException e) {
                            response = null;
                        }
                        return response;
                    } else {
                        Logger.ww(TAG, "created url is null!");
                        return null;
                    }
                }
            }));
        }
        return tasks;
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        //批量获取用户信息时，不要同步更新会话信息，否则效率太低
        UserInfoManager.getInstance().insertOrUpdateUserInfo(resultInfoList, false, false, false, false);
        CommonUtils.doCompleteCallBackToUser(waitForCompletion, mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, resultInfoList);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        if (waitForCompletion) {
            CommonUtils.doCompleteCallBackToUser(true, mCallback, responseCode,
                    responseMsg);
        } else {
            CommonUtils.doCompleteCallBackToUser(mCallback, responseCode,
                    responseMsg);
        }
    }
}
