package cn.jpush.im.android.api.callback;

import java.util.List;

import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class GetNoDisurbListCallback extends BasicCallback {
    private static final String TAG = "GetNoDisurbListCallback";


    protected GetNoDisurbListCallback() {

    }

    protected GetNoDisurbListCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    public abstract void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        List<UserInfo> noDisturbUsers = null;
        List<GroupInfo> noDisturbGroups = null;
        if (null != result && result.length > 1) {
            if (null != result[0]) {
                noDisturbUsers = (List<UserInfo>) result[0];
            }
            if (null != result[1]) {
                noDisturbGroups = (List<GroupInfo>) result[1];
            }
        }
        gotResult(responseCode, responseMessage, noDisturbUsers, noDisturbGroups);
    }
}
