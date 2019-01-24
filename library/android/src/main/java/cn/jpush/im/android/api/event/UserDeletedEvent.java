package cn.jpush.im.android.api.event;

import cn.jpush.im.android.api.model.UserInfo;


/**
 * 用户被删除事件。当前登陆的用户账号被服务端删除时，sdk将会抛出此事件通知上层，同时会清空当前设备登录的账号信息。
 * 上层通过onEvent方法接收事件<br/><br/>
 * 详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 *
 * @deprecated deprecated in sdk version 1.2.0,use {@link LoginStateChangeEvent} instead.
 */
@Deprecated
public class UserDeletedEvent {
    private UserInfo myInfo;

    public UserDeletedEvent(UserInfo myInfo) {
        this.myInfo = myInfo;
    }

    /**
     * 获取当前被删除账号的信息<br/><br/>
     * 发出UserDeleted事件的同时sdk会清掉本地存储的myInfo.
     * 如果此时上层需要获取当前登出的账号的信息，通过JMessageClient.getMyInfo这个接口已经获取不到了。
     * 只能通过此方法拿到myInfo.
     *
     * @return  当前登出账号的信息
     */
    public UserInfo getMyInfo() {
        return myInfo;
    }
}
