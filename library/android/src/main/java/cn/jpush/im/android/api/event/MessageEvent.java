package cn.jpush.im.android.api.event;


import cn.jpush.im.android.api.model.Message;

/**
 * 在线消息事件.
 * 当用户在线期间,所收到的每一条消息,都会通过一个MessageEvent的形式通知给上层。
 * <p>
 * 上层通过onEvent方法接收事件.详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 * <p>
 * 需要注意此事件和离线消息事件{@link OfflineMessageEvent}的区别。
 *
 * @since 1.0.0
 */
public class MessageEvent extends MessageBaseEvent {

    public MessageEvent(int responseCode, String responseDesc, Message msg) {
        super(responseCode, responseDesc, msg);
    }

}
