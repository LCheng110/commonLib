package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import java.util.concurrent.Executor;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.pushcommon.proto.common.commands.IMRequest;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class ImBaseRequest {
    private static final String TAG = "ImBaseRequest";

    @Expose
    protected long rid = 0;
    @Expose
    protected long uid = 0;

    protected int cmd;

    protected BasicCallback callback;

    public ImBaseRequest(int cmd, long uid, long rid) {
        this.cmd = cmd;
        this.rid = rid;
        this.uid = uid;
    }

    public String toJson() {
        return JsonUtil.toJsonOnlyWithExpose(this);
    }

    abstract IMProtocol toProtocolBuffer(long imUid, String appKey);

    public IMRequest toProtocolBuffer(String appKey) {
        return new IMRequest(rid, toProtocolBuffer(uid, appKey));
    }

    public static class BasicCallbackContinuation implements Continuation<Void, Void> {

        private ImBaseRequest request;
        private int responseCode;

        private String responseMsg;

        public BasicCallbackContinuation(ImBaseRequest request, int responseCode,
                                         String responseMsg) {
            this.request = request;
            this.responseCode = responseCode;
            this.responseMsg = responseMsg;
        }

        @Override
        public Void then(Task<Void> task) throws Exception {
            request.basicCallbackToUser(responseCode, responseMsg);
            return null;
        }
    }

    public Executor getExecutor() {
        Executor executor;
        if (callback != null) {
            executor = callback.isRunInUIThread() ? Task.UI_THREAD_EXECUTOR
                    : Task.BACKGROUND_EXECUTOR;
        } else {
            executor = Task.UI_THREAD_EXECUTOR;
        }
        return executor;
    }

    protected void basicCallbackToUser(int responseCode, String responseMsg) {
        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
        RequestProcessor.requestsCache.remove(rid);
    }

    public int getCmd() {
        return this.cmd;
    }


    public void setCallback(BasicCallback callback) {
        this.callback = callback;
    }

    public BasicCallback getCallback() {
        return this.callback;
    }

    //作为接收到响应的一个统一的入口,所有的响应需要经过统一处理之后再分到具体的request中处理。
    public void imProtocolReceived(IMProtocol imProtocol) {
        ImBaseRequest request = RequestProcessor.requestsCache.get(rid);
        if (null == request) {
            Logger.d(TAG, "cached request not exist! return from receiver!");
            return;
        }

        boolean isHandled = false;
        int responseCode = imProtocol.getResponse().getCode();
        int errorResponseCode = ErrorCode.NO_ERROR;
        String errorResponseMsg = ErrorCode.NO_ERROR_DESC;
        if (imProtocol.getCommand() != IMCommands.Logout.CMD) {
            switch (responseCode) {
                case ErrorCode.ERROR_USER_OFFLINE:
                case ErrorCode.ERROR_USER_DEVICE_NOT_MATCH:
                    EventBus.getDefault().post(new LoginStateChangeEvent(
                            JMessageClient.getMyInfo(), LoginStateChangeEvent.Reason.user_login_status_unexpected));
                    JMessageClient.logout();
                    errorResponseCode = ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN;
                    errorResponseMsg = ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC;
                    isHandled = true;
                    break;
            }
        }

        if (isHandled) {
            Logger.ww(TAG, "response code is handled,return from parse response.");
            request.onErrorResponse(errorResponseCode, errorResponseMsg);
            return;
        }
        request.onResponse(imProtocol);
    }

    public abstract void onResponseTimeout();

    public abstract void onResponse(IMProtocol imProtocol);

    //当服务器返回指定错误码时,每个request类型需要根据情况做处理。
    public abstract void onErrorResponse(int responseCode, String responseMsg);

}
