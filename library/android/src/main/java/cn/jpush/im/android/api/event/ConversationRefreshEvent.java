package cn.jpush.im.android.api.event;

import android.content.Context;

import cn.jpush.im.android.api.model.Conversation;

/**
 * 会话刷新事件.
 * 当会话的相关信息被sdk更新时，sdk会上抛此事件通知上层，上层需要更新已拿到的会话对象。或者刷新UI
 * <p>
 * 2.1.0新增一个该事件的触发条件：当sdk初始化时通过{@link cn.jpush.im.android.api.JMessageClient#init(Context, boolean)}接口
 * 打开了消息漫游，会话中的漫游消息同步到本地完成后，也会触发这个事件通知上层。此时事件的reason为MSG_ROAMING_COMPLETE。
 * <p>
 * 上层通过onEvent方法接收事件.详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 * <p>
 *
 * @since 1.0.0
 */
public class ConversationRefreshEvent {
    private Conversation conv;
    private Reason reason;

    public ConversationRefreshEvent(Conversation conv, Reason reason) {
        this.conv = conv;
        this.reason = reason;
    }


    /**
     * 事件发生的原因
     *
     * @since 2.1.0
     */
    public enum Reason {
        CONVERSATION_INFO_UPDATED,//会话中相关信息更新
        MSG_ROAMING_COMPLETE, //消息漫游完成
        UNREAD_CNT_UPDATED //会话未读数被更新 since 2.3.0
    }

    /**
     * 获取事件发生的原因
     *
     * @return
     * @since 2.1.0
     */
    public Reason getReason() {
        return reason;
    }

    /**
     * 获取事件中所包含的Conversation对象
     *
     * @return
     */
    public Conversation getConversation() {
        return conv;
    }
}
