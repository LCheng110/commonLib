package cn.jpush.im.android.api.callback;

import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class CreateGroupCallback extends BasicCallback {

    private final static String TAG = "CreateGroupCallback";

    protected CreateGroupCallback() {
    }

    protected CreateGroupCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int responseCode, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        long groupID = 0L;
        if (null != result && result.length > 0 && null != result[0]) {
            groupID = (Long) result[0];
        }
        gotResult(responseCode, responseMessage, groupID);
    }

    public abstract void gotResult(int responseCode, String responseMsg, long groupId);
}
