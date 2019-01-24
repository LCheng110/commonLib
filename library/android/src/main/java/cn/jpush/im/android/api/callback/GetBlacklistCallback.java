package cn.jpush.im.android.api.callback;

import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class GetBlacklistCallback extends BasicCallback {
    private final static String TAG = "GetBlackListCallback";

    protected GetBlacklistCallback() {
    }

    protected GetBlacklistCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    public abstract void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        List<UserInfo> blacklist = null;
        if (null != result && result.length > 0 && null != result[0]) {
            blacklist = (List<UserInfo>) result[0];
        }
        gotResult(responseCode, responseMessage, blacklist);
    }
}
