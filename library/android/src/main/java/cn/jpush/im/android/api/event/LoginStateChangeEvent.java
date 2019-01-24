package cn.jpush.im.android.api.event;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;

/**
 * 用户登陆状态变更事件。当前用户的登陆状态变更时，sdk将会抛出此事件通知上层。
 * 通过{@link LoginStateChangeEvent#getReason()}接口可以获取状态变更原因。
 * <br/><br/>
 * 详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 */
public class LoginStateChangeEvent {
    /**
     * 登陆状态变更原因。
     */
    public enum Reason {
        /**
         * 用户在其他设备登陆。sdk会自动登出当前设备的用户。
         */
        user_logout,
        /**
         * 用户信息被服务器端删除。sdk将清除当前设备登陆的用户信息。
         */
        user_deleted,
        /**
         * 用户密码在其他端被修改，sdk会自动登出当前设备的用户
         */
        user_password_change,
        /**
         * 用户登陆状态异常,请重新登录。
         */
        user_login_status_unexpected
    }

    private Reason reason;
    private UserInfo myInfo;

    public LoginStateChangeEvent(UserInfo info, Reason reason) {
        myInfo = info;
        this.reason = reason;
    }

    /**
     * 获取当前被登出账号的信息<br/><br/>
     * 发出LoginStateChange事件的同时sdk会自动登出当前账号，同时清掉本地存储的myInfo.
     * 如果此时上层需要获取当前登出的账号的信息，通过{@link JMessageClient#getMyInfo()}这个接口已经获取不到了。
     * 只能通过此方法拿到myInfo.
     *
     * @return 当前登出账号的信息
     */
    public UserInfo getMyInfo() {
        return myInfo;
    }

    /**
     * 登陆状态变更原因。原因具体描述见{@link LoginStateChangeEvent.Reason}。
     *
     * @return
     */
    public Reason getReason() {
        return reason;
    }
}
