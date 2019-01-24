package cn.jpush.im.android.api.event;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * 消息被发送方撤回事件.
 * 此事件在消息被发送方撤回时触发。
 * <p>
 * 上层通过onEvent方法接收事件.详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 */

public class MessageRetractEvent {
    private Conversation conversation;
    private Message message;

    public MessageRetractEvent(Conversation conversation, Message message) {
        this.message = message;
        this.conversation = conversation;
    }

    /**
     * 获取被撤回消息所属的会话对象
     *
     * @return 会话对象
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     * 获取被撤回的message对象.
     * 注意此时的message的messageContent对象已经从撤回前的真正的消息内容变为了PromptContent类型的提示文字
     *
     * @return 消息对象
     */
    public Message getRetractedMessage() {
        return message;
    }
}
