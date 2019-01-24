package cn.jpush.im.android.api.event;

import cn.jpush.im.android.utils.Logger;


/**
 * 联系人相关通知事件类。收到任何联系人相关的事件时，sdk将会抛出此事件通知上层。
 * 通过{@link ContactNotifyEvent#getType()}接口可以获取事件具体类型原因。
 * <br/><br/>
 * 详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 *
 * @since 1.4.0
 */
public class ContactNotifyEvent extends BaseNotificationEvent {
    private static final String TAG = "ContactNotifyEvent";

    private static final int EVENT_TYPE_CONTACT_INVITE = 5;
    private static final int EVENT_TYPE_CONTACT_DELETED = 6;

    private static final int EXTRA_CONTACT_REQ = 1;
    private static final int EXTRA_CONTACT_RESP = 2;

    private static final int RC_ACCEPTED = 0;

    private Type type;


    /**
     * 联系人通知事件类型
     */
    public enum Type {
        /**
         * 收到好友邀请
         */
        invite_received,
        /**
         * 对方接受了你的好友邀请
         */
        invite_accepted,
        /**
         * 对方拒绝了你的好友邀请
         */
        invite_declined,
        /**
         * 对方将你从好友中删除
         */
        contact_deleted,
    }

    /**
     * 获取联系人通知事件的具体类型。
     *
     * @return 通知事件类型
     */
    public Type getType() {
        return type;
    }

    /**
     * 获取事件发生的理由，该字段由对方发起请求时所填，对方如果未填则将返回默认字符串。
     *
     * @return 事件发生的理由
     */
    public String getReason() {
        return desc;
    }

    /**
     * 获取事件发送者的username
     *
     * @return 对方用户的username
     */
    @Override
    public String getFromUsername() {
        return super.getFromUsername();
    }

    public static class Builder extends BaseNotificationEvent.Builder {
        @Override
        public ContactNotifyEvent build() {
            ContactNotifyEvent event = new ContactNotifyEvent();
            buildFrom(event);
            int eventType = event.getEventType();
            int extra = event.getExtra();
            int returnCode = event.getReturnCode();
            if (EVENT_TYPE_CONTACT_INVITE == eventType && EXTRA_CONTACT_REQ == extra) {
                event.type = Type.invite_received;
            } else if (EVENT_TYPE_CONTACT_INVITE == eventType && EXTRA_CONTACT_RESP == extra) {
                if (RC_ACCEPTED == returnCode) {
                    event.type = Type.invite_accepted;
                } else {
                    event.type = Type.invite_declined;
                }
            } else if (EVENT_TYPE_CONTACT_DELETED == eventType) {
                event.type = Type.contact_deleted;
            } else {
                Logger.ww(TAG, "unsupported event type. type = " + eventType);
            }
            return event;
        }
    }

    @Override
    public String toString() {
        return super.toString() +
                "ContactNotifyEvent{" +
                "type=" + type +
                '}';
    }
}
