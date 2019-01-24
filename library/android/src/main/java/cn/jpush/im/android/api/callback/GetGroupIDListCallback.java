package cn.jpush.im.android.api.callback;

import java.util.List;

import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class GetGroupIDListCallback extends BasicCallback {

    private final static String TAG = "GetGroupIDListCallback";

    protected GetGroupIDListCallback() {
    }

    protected GetGroupIDListCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    public abstract void gotResult(int responseCode, String responseMessage,
                                   List<Long> groupIDList);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        List<Long> idList = null;
        if (null != result && result.length > 0 && null != result[0]) {
            idList = (List<Long>) result[0];
        }
        gotResult(responseCode, responseMessage, idList);
    }
}
