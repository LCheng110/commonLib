package cn.jpush.im.android.api.event;

import java.util.List;

import cn.jpush.im.android.api.model.Conversation;

/**
 * 会话中消息的已回执人数变更事件。
 * <p>
 * 对于用户发送的需要接收方发送已读回执的消息，接收方成功发送已读回执后，sdk会上抛这个事件通知上层。
 * 上层通过这个事件可以知道是哪个会话中的哪条消息的未回执人数发生了变化
 * <p>
 * 上层通过onEvent方法接收事件.详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 * <p>
 *
 * @Since 2.3.0
 */
public class MessageReceiptStatusChangeEvent {
    private Conversation conversation;
    private List<MessageReceiptMeta> messageReceiptMetas;

    public MessageReceiptStatusChangeEvent(Conversation conversation, List<MessageReceiptMeta> messageReceiptMetas) {
        this.conversation = conversation;
        this.messageReceiptMetas = messageReceiptMetas;
    }

    /**
     * 获取事件发生的会话。
     *
     * @return 会话对象
     */
    public Conversation getConversation() {
        return conversation;
    }

    /**
     * 获取未回执数发生变化的消息的{@link MessageReceiptMeta}。
     *
     * @return
     */
    public List<MessageReceiptMeta> getMessageReceiptMetas() {
        return messageReceiptMetas;
    }

    /**
     * 消息对应未回执数相关元信息。
     * <p>
     * 其中包括了:消息的server msg id、当前的未回执数、以及未回执数更新的时间
     */
    public static class MessageReceiptMeta {
        private long serverMsgId;
        private int unReceiptCnt;
        private long unReceiptMtime;

        public MessageReceiptMeta(long serverMsgId, int unReceiptCnt, long unReceiptMtime) {
            this.serverMsgId = serverMsgId;
            this.unReceiptCnt = unReceiptCnt;
            this.unReceiptMtime = unReceiptMtime;
        }

        /**
         * 获取未回执数被改变的消息的serverMsgId
         *
         * @return 消息的serverMsgId
         */
        public long getServerMsgId() {
            return serverMsgId;
        }

        /**
         * 获取未回执数被改变的消息的当前未发送已读回执的人数
         *
         * @return 消息当前未发送已读回执的人数
         */
        public int getUnReceiptCnt() {
            return unReceiptCnt;
        }

        /**
         * 获取未回执数被改变的消息的未回执人数变更时间
         *
         * @return 消息未回执人数的变更时间
         */
        public long getUnReceiptMtime() {
            return unReceiptMtime;
        }
    }
}
