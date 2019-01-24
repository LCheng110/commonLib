package cn.jpush.im.android.helpers.eventsync;

import java.util.List;

import cn.jpush.im.android.helpers.eventsync.ReadReceiptEvents.Kind8BaseEvents;
import cn.jpush.im.android.helpers.eventsync.ReadReceiptEvents.ReadCountMtimeUpdateEvents;
import cn.jpush.im.android.helpers.eventsync.ReadReceiptEvents.ReadReceiptEvents;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/8/17.
 */

public class Kind8EventsWrapper extends EventNotificationsWrapper {
    private static final String TAG = Kind8EventsWrapper.class.getSimpleName();

    public static final int EVENT_KIND_8 = 8;
    private static final int EVENT_TYPE_READ_COUNT_UPDATE = 200;//会话消息未读数更新
    private static final int EVENT_TYPE_READ_RECEIPT = 201;//消息已读回执
    private Kind8BaseEvents events = null;

    /**
     * 判断在线下发的事件的eventType是否属于event kind8类型。
     * 因为在线事件没有kind信息，只能通过eventType来判断。
     *
     * @param eventType 事件的type
     */
    static boolean isEventTypeBelongsToKind8(int eventType) {
        return eventType >= EVENT_TYPE_READ_COUNT_UPDATE && eventType <= EVENT_TYPE_READ_RECEIPT;
    }

    @Override
    protected void onMerge(String convID, List<Message.EventNotification> notifications, int eventKind, boolean isFullUpdate) {
        int gid = 0;
        try {
            if (null != convID) {
                //截取convID中的第二段作为gid.
                gid = Integer.parseInt(convID.split("_")[1]);
            } else if (null != notifications && !notifications.isEmpty()) {
                gid = (int) notifications.get(0).getGid();
            }
        } catch (NumberFormatException e) {
            Logger.ee(TAG, "error occurs when parse gid from conv_id");
        }

        switch (gid) {
            case EVENT_TYPE_READ_COUNT_UPDATE:
                events = new ReadCountMtimeUpdateEvents(notifications);
                break;
            case EVENT_TYPE_READ_RECEIPT:
                events = new ReadReceiptEvents(notifications);
                break;
            default:
                Logger.ww(TAG, "Kind8EventsWrapper unsupported gid. gid = " + gid);
        }

        if (null != events) {
            events.merge();
        }

    }

    @Override
    protected void afterMerge(List<Message.EventNotification> notifications, int eventKind, boolean isFullUpdate, BasicCallback callback) {
        if (null != events) {
            events.afterMerge(callback);
        }
    }
}
