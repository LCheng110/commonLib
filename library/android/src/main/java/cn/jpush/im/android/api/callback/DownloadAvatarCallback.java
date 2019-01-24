package cn.jpush.im.android.api.callback;


import java.io.File;

import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class DownloadAvatarCallback extends BasicCallback {

    private final static String TAG = "DownloadAvatarCallback";

    protected DownloadAvatarCallback() {
    }

    protected DownloadAvatarCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    public abstract void gotResult(int responseCode, String responseMessage, File avatar);

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        File avatar = null;
        if (null != result && result.length > 0 && null != result[0]) {
            avatar = (File) result[0];
        }
        gotResult(responseCode, responseMessage, avatar);
    }
}
