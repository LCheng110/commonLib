package cn.jpush.im.android.helpers.eventsync.ReadReceiptEvents;

import java.util.List;

import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/8/17.
 */

public abstract class ReadReceiptBaseEvents {
    List<Message.EventNotification> notifications;

    ReadReceiptBaseEvents(List<Message.EventNotification> notifications) {
        this.notifications = notifications;
    }

    public abstract void merge();

    public abstract void afterMerge(BasicCallback callback);
}
