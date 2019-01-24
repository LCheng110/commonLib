package cn.jpush.im.android.api.callback;

import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class ProgressUpdateCallback extends BasicCallback {
    private final static String TAG = "ProgressUpdateCallback";

    protected ProgressUpdateCallback() {
    }

    protected ProgressUpdateCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    public abstract void onProgressUpdate(double percent);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        Double percent = 0d;
        if (null != result && result.length > 0 && null != result[0]) {
            percent = (Double) result[0];
        }
        onProgressUpdate(percent);
    }
}
