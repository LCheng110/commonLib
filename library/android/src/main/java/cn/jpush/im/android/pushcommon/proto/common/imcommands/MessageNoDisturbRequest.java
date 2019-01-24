package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.bolts.Task;

/**
 * Created by xiongtc on 2016/10/10.
 */
public abstract class MessageNoDisturbRequest extends ImBaseRequest {
    private static final String TAG = "MessageNoDisturbRequest";
    public static final int NO_DISTURB_USER_ALREADY_SET = 831001;
    public static final int NO_DISTURB_GROUP_ALREADY_SET = 833003;
    public static final int NO_DISTURB_USER_NEVER_SET = 832001;
    public static final int NO_DISTURB_GROUP_NEVER_SET = 834001;

    public static final int NO_DISTURB_GLOBAL_ALREADY_SET = 835001;
    public static final int NO_DISTURB_GLOBAL_NEVER_SET = 856001;

    public MessageNoDisturbRequest(int cmd, long uid, long rid) {
        super(cmd, uid, rid);
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }

    void setNoDisturbGlobalPostExecute(final int responseCode, String responseMsg, final int isNodisturbGlobal) {
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                //当请求返回0（no error）或者返回重复添加或者删除的错误码时，都应该更新本地数据
                if (ErrorCode.NO_ERROR == responseCode || NO_DISTURB_GLOBAL_ALREADY_SET == responseCode
                        || NO_DISTURB_GLOBAL_NEVER_SET == responseCode) {
                    IMConfigs.setNodisturbGlobal(isNodisturbGlobal);
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, responseCode, responseMsg), getExecutor());
    }
}
