package cn.jpush.im.android.api.event;

import java.util.List;

import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;

/**
 * 离线消息事件.
 * <p>
 * sdk会将消息下发分为在线下发和离线下发两种情况,其中用户在离线状态(包括用户登出或者网络断开)期间所收到的消息我们称之为离线消息。
 * 当用户上线收到离线消息后,这里的处理与之前版本不同的是:
 * <p>
 * 2.1.0版本之前:sdk会和在线时收到的消息一样,每收到一条消息都会上抛一个在线消息事件{@link MessageEvent}来通知上层。
 * <br>
 * 2.1.0版本之后:sdk会以会话为单位,以一个{@link OfflineMessageEvent}离线事件的形式上抛。事件中包含了会话对象
 * 、和所有离线消息的对象。<br>
 * 注意一个会话如果有多条离线消息，也只会对应上抛一个离线事件,这个事件中就包含了所有离线消息的相关信息。这样会大大减轻上层在收到消息事件需要刷新UI的应用场景下,UI刷新的压力。
 * <p>
 * 上层通过onEvent方法接收事件.详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 *
 * @since 2.1.0
 */
public class OfflineMessageEvent {

    private Conversation conversation;
    private List<Message> offlineMsgList;

    public OfflineMessageEvent(Conversation conversation, List<Message> offlineMsgList) {
        this.conversation = conversation;
        this.offlineMsgList = offlineMsgList;
    }

    /**
     * 获取收到离线消息的会话对象
     *
     * @return 会话对象
     */
    public Conversation getConversation() {
        return conversation;
    }


    /**
     * 获取收到的离线消息列表,包含了该会话此次离线收到的所有离线消息列表。其中也有可能包含自己发出去的消息。
     *
     * @return 离线消息列表
     */
    public List<Message> getOfflineMessageList() {
        return offlineMsgList;
    }

}
