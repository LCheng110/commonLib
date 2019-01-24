package cn.jpush.im.android.api.callback;

import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class GetUserInfoListCallback extends BasicCallback {
    private static final String TAG = "GetUserInfoListCallback";

    protected GetUserInfoListCallback() {
    }

    protected GetUserInfoListCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    public abstract void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList);

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        List<UserInfo> userInfoList = null;
        if (null != result && result.length > 0 && null != result[0]) {
            userInfoList = (List<UserInfo>) result[0];
        }
        gotResult(responseCode, responseMessage, userInfoList);
    }
}
