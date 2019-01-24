package cn.jpush.im.android.api.event;

import cn.jpush.im.android.api.model.UserInfo;

/**
 * 当前登陆用户的用户信息被更新事件.
 * <p>
 * 当前登陆的用户的用户信息被服务端api所修改时，sdk会主动更新当前用户的用户信息到本地，然后会上抛此事件
 * 通知上层。
 * <p>
 * 事件处理方法见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 *
 * @since 2.1.0
 */
public class MyInfoUpdatedEvent {

    private UserInfo myInfo;

    public MyInfoUpdatedEvent(UserInfo myInfo) {
        this.myInfo = myInfo;
    }

    /**
     * 获取更新之后的我的userinfo
     */
    public UserInfo getMyInfo() {
        return myInfo;
    }
}
