package cn.jpush.im.android.api.callback;

import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class IntegerCallback extends BasicCallback {
    private static final String TAG = "IntegerCallback";

    protected IntegerCallback() {
    }

    protected IntegerCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    public abstract void gotResult(int responseCode, String responseMessage, Integer value);

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
        gotResult(-1, "", -1);
    }

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        Integer integer = -1;
        if (null != result && result.length > 0 && null != result[0]) {
            integer = (Integer) result[0];
        }
        gotResult(responseCode, responseMessage, integer);
    }
}
