package cn.jpush.im.android.helpers.eventsync;

import cn.jpush.im.android.utils.Logger;

/**
 * Created by hxhg on 2017/7/31.
 */

public class EventNotificationWrapperBuilder {
    private static final String TAG = EventNotificationWrapperBuilder.class.getSimpleName();

    public static EventNotificationsWrapper buildWrapper(int kind, int eventType) {
        Logger.d(TAG, "[buildWrapper] build a eventNotificationWrapper. kind = " + kind + " eventType = " + eventType);
        if (GroupEventsWrapper.EVENT_KIND_3 == kind) {
            return new GroupEventsWrapper();
        } else if (Kind7EventsWrapper.EVENT_KIND_7 == kind ||
                //判断下发的事件type是否属于king7这个类型，如果属于，也需要走UserProfileEventsWrapper来处理
                Kind7EventsWrapper.isEventTypeBelongsToKind7(eventType)) {
            return new Kind7EventsWrapper();
        } else if (Kind8EventsWrapper.EVENT_KIND_8 == kind ||
                Kind8EventsWrapper.isEventTypeBelongsToKind8(eventType)) {
            return new Kind8EventsWrapper();
        } else {
            return new GeneralEventsWrapper();
        }
    }
}
