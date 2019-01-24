package cn.jpush.im.android.api.callback;

import cn.jpush.im.android.api.model.UserStatus;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by zhaoyuanchao on 2018/11/14.
 * 获取用户聊天权限状态
 */

public abstract class GetUserStatusCallback extends BasicCallback{
    private final static String TAG = "GetUserStatusCallback";

    protected GetUserStatusCallback(){

    }
    protected GetUserStatusCallback(boolean isRunInUIThread){
        super(isRunInUIThread);
    }
    @Override
    public void gotResult(int responseCode, String responseMessage) {

    }

    public abstract void gotResult(int responseCode, String responseMessage, UserStatus userStatus);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        UserStatus userStatus = null;
       if (null != result && result.length > 0 && null != result[0]){
           userStatus = (UserStatus) result[0];
       }
        gotResult(responseCode,responseMessage,userStatus);
    }
}
