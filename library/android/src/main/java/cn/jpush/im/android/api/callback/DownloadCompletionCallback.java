package cn.jpush.im.android.api.callback;

import java.io.File;

import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class DownloadCompletionCallback extends BasicCallback {

    private final static String TAG = "DownloadCompletionCallback";

    protected DownloadCompletionCallback() {
    }

    protected DownloadCompletionCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    public abstract void onComplete(int responseCode, String responseMessage, File file);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        File file = null;
        if (null != result && result.length > 0 && null != result[0]) {
            file = (File) result[0];
        }
        onComplete(responseCode, responseMessage, file);
    }
}
