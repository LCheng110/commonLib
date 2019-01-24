package cn.jpush.im.android.api.callback;

import android.graphics.Bitmap;

import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class GetAvatarBitmapCallback extends BasicCallback {
    private static final String TAG = "DownloadAvatarBitmapCallback";

    protected GetAvatarBitmapCallback() {
    }

    protected GetAvatarBitmapCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    public abstract void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap);

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        Bitmap bitmap = null;
        if (null != result && result.length > 0 && null != result[0]) {
            bitmap = (Bitmap) result[0];
        }
        gotResult(responseCode, responseMessage, bitmap);
    }
}
