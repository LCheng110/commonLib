package cn.jpush.im.android.api.callback;

import java.util.List;

import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by ${chenyn} on 16/8/12.
 */
public abstract class GetGroupInfoListCallback extends BasicCallback {
    private static final String TAG = "GetGroupInfoListCallback";


    protected GetGroupInfoListCallback() {
    }

    protected GetGroupInfoListCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int i, String s) {
        Logger.ee(TAG, "Should not reach here! ");
    }

    public abstract void gotResult(int responseCode, String responseMessage, List<GroupInfo> groupInfos);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        List<GroupInfo> shieldingGroup = null;
        if (null != result && result.length > 0 && null != result[0]) {
            shieldingGroup = (List<GroupInfo>) result[0];
        }
        gotResult(responseCode, responseMessage, shieldingGroup);
    }
}

