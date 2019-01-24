package cn.jpush.im.android.tasks;

import android.text.TextUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.bolts.AndroidExecutors;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.connection.NativeHttpClient;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

/**
 * @author
 */
public abstract class AbstractTask {

    public static final String TAG = "AbstractTask";

    protected NativeHttpClient mHttpClient = new NativeHttpClient(IMConfigs.getToken());

    //所有的http task放到一个单独的线程池中执行
    public static final ExecutorService httpTaskExecutor = AndroidExecutors.newCachedThreadPool();

    protected static final int MAX_RETRY_TIME = 3;

    protected static AtomicBoolean sIsTokenTaskExistsInPool = new AtomicBoolean(false);

    protected BasicCallback mCallback = null;

    protected boolean waitForCompletion = false;

    private boolean mIsCanceled = false;

    protected Task boltsTask;

    protected int retryTime = 0;

    protected AbstractTask(BasicCallback callback, boolean waitForCompletion) {
        mCallback = callback;
        this.waitForCompletion = waitForCompletion;
    }

    protected ResponseWrapper doExecute() throws Exception {
        String token = IMConfigs.getToken();
        ResponseWrapper result = null;
        if (TextUtils.isEmpty(token)) {
            result = new ResponseWrapper();
            result.responseCode = 401;
        }
        return result;
    }

    protected void doPostExecute(ResponseWrapper result) throws Exception {
        int responseCode;
        String responseMsg;
        if (result != null) {
            Logger.d(TAG, "get response : code = " + result.responseCode + " content = "
                    + result.responseContent);
            if (handleErrorCode(result)) {
                return;
            }

            if (result.responseCode >= 200 && result.responseCode < 300) {
                onSuccess(result.responseContent);
                onSuccess(result.rawData);
                return;
            } else if (result.responseCode >= 300 && result.responseCode < 400) {
                responseCode = ErrorCode.HTTP_ERROR.HTTP_UNEXPECTED_ERROR;
                responseMsg = ErrorCode.HTTP_ERROR.HTTP_UNEXPECTED_ERROR_DESC;
            } else if (null != result.error && null != result.error.error) {
                responseCode = result.error.error.code;
                responseMsg = result.error.error.message;
            } else if (0 > result.responseCode) {
                //如果result的code小于0，我们认为是sdk定义的本地特殊的错误码，此时直接将code传到onError回调中
                responseCode = result.responseCode;
                responseMsg = result.responseContent;
            } else {
                responseCode = ErrorCode.HTTP_ERROR.HTTP_SERVER_INTERNAL_ERROR;
                responseMsg = ErrorCode.HTTP_ERROR.HTTP_SERVER_INTERNAL_ERROR_DESC;
            }
        } else if (retryTime < MAX_RETRY_TIME) {
            doRetry(this, true, false);
            return;
        } else {
            responseCode = ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT;
            responseMsg = ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT_DESC;
        }
        onError(responseCode, responseMsg);
    }

    protected void onSuccess(String resultContent) {
    }

    protected void onSuccess(byte[] rawData) {
    }

    protected void onError(int responseCode, String responseMsg) {
    }

    public void execute() {
        execute(false);
    }

    public synchronized void cancel() {
        mIsCanceled = true;
    }

    public synchronized boolean isCanceled() {
        return mIsCanceled;
    }

    public final void execute(final Boolean isTokenTask) {
        if (isTokenTask) {
            sIsTokenTaskExistsInPool.set(true);
        }
        boltsTask = Task.call(new Callable<ResponseWrapper>() {
            @Override
            public ResponseWrapper call() throws Exception {
                try {
                    ResponseWrapper executeResult;
                    if (!isTokenTask && sIsTokenTaskExistsInPool.get()) {
                        //对于非token任务，如果有token task已存在，则直接将此任务重新入队等待重试。
                        // 而不是执行完返回401之后重新创建一个token task。
                        // 而且此次重试不增加重试次数，将一直重试到token task结束为止。
                        Logger.d(TAG, "[doPostExecute]ERROR_TOKEN_TASK_EXISTS handled,just retry.");
                        doRetry(AbstractTask.this, false, false);
                    } else {
                        Logger.d(TAG, AbstractTask.this + " token is " + IMConfigs.getToken());
                        executeResult = doExecute();
                        doPostExecute(executeResult);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
                return null;
            }
        }, httpTaskExecutor);

        if (waitForCompletion) {
            try {
                boltsTask.waitForCompletion();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    void doRetry(final AbstractTask task, boolean increaseRetryTime, boolean isTokenTask) {
        if (increaseRetryTime) {
            task.retryTime++;
        }
        try {
            Thread.sleep((long) (Math.pow(2, task.retryTime) * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        task.execute(isTokenTask);
    }

    private boolean handleErrorCode(ResponseWrapper result) {
        if (0 == IMConfigs.getUserID()) {
            //如果本地存的uid是0，说明用户未登录，此时直接返回
            return false;
        }
        if (result.responseCode >= 500 && retryTime < MAX_RETRY_TIME) {
            doRetry(this, true, false);
            return true;
        } else if (401 == result.responseCode && retryTime < MAX_RETRY_TIME) {
            new GetTokenTask(new BasicCallback(false) {
                @Override
                public void gotResult(int responseCode, String msg) {
                    doRetry(AbstractTask.this, true, false);
                }
            }, false).execute(true);
            return true;
        } else {
            return false;
        }
    }

    public static void resetFlag() {
        sIsTokenTaskExistsInPool.set(false);
    }
}
