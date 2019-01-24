package cn.jpush.im.android.api.callback;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class GetUserInfoCallback extends BasicCallback {

    private final static String TAG = "GetUserInfoCallback";

    protected GetUserInfoCallback() {
    }

    protected GetUserInfoCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    public abstract void gotResult(int responseCode, String responseMessage, UserInfo info);

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        InternalUserInfo userInfo = null;
        if (null != result && result.length > 0 && null != result[0]) {
            userInfo = (InternalUserInfo) result[0];
        }
        gotResult(responseCode, responseMessage, userInfo);
    }
}