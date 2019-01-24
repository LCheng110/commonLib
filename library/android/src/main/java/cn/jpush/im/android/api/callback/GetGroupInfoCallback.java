package cn.jpush.im.android.api.callback;

import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class GetGroupInfoCallback extends BasicCallback {

    private final static String TAG = "GetGroupInfoCallback";

    protected GetGroupInfoCallback() {
    }

    protected GetGroupInfoCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    public abstract void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        InternalGroupInfo groupInfo = null;
        if (null != result && result.length > 0 && null != result[0]) {
            groupInfo = (InternalGroupInfo) result[0];
        }
        gotResult(responseCode, responseMessage, groupInfo);
    }
}
