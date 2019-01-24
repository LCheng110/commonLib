package cn.jpush.im.android.api.event;

import cn.jpush.im.android.api.model.Message;

/**
 * 通知栏点击事件。如果上层使用了sdk提供的通知栏消息，则当点击通知栏时，sdk将会发送此事件给上层，
 * ，上层通过onEvent方法接收事件，可以自定义点击通知栏之后的跳转。<br/><br/>
 * 详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 */
public class NotificationClickEvent extends MessageBaseEvent {

    public NotificationClickEvent(int responseCode, String responseDesc, Message msg) {
        super(responseCode, responseDesc, msg);
    }

}
