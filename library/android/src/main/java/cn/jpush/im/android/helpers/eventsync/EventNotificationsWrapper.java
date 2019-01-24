package cn.jpush.im.android.helpers.eventsync;

import java.util.List;

import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/7/31.
 */

public abstract class EventNotificationsWrapper {

    protected abstract void onMerge(String convID, List<Message.EventNotification> notifications, final int eventKind, final boolean isFullUpdate);

    protected abstract void afterMerge(List<Message.EventNotification> notifications, final int eventKind, final boolean isFullUpdate, final BasicCallback callback);

    public void onProcess(String convID, List<Message.EventNotification> notifications, final int eventKind, final boolean isFullUpdate, final BasicCallback callback) {
        onMerge(convID, notifications, eventKind, isFullUpdate);
        afterMerge(notifications, eventKind, isFullUpdate, callback);
    }
}
