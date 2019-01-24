package cn.jpush.im.android.helpers;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.eventsync.EventNotificationWrapperBuilder;
import cn.jpush.im.android.helpers.eventsync.EventNotificationsWrapper;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/6/22.
 */

public class EventProcessor {

    private static final String TAG = "EventProcessor";

    private ExecutorService eventExecutor = Executors.newFixedThreadPool(5);

    private static EventProcessor instance;

    private EventProcessor() {
    }

    public static synchronized EventProcessor getInstance() {
        if (null == instance) {
            instance = new EventProcessor();
        }
        return instance;
    }

    public void enqueueEvent(final Message.EventNotification eventNotification) {
        Task.call(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                int eventKind = 0;//单个事件处理，不需要根据eventKind做分类，这里的eventKind默认给0.需要根据eventType再决定
                EventNotificationsWrapper wrapper = EventNotificationWrapperBuilder.buildWrapper(eventKind, eventNotification.getEventType());
                if (null != wrapper) {
                    List<Message.EventNotification> eventNotifications = new ArrayList<Message.EventNotification>();
                    eventNotifications.add(eventNotification);
                    wrapper.onProcess(null, eventNotifications, eventKind, false, null);
                }
                return null;
            }
        }, eventExecutor);
    }

    public void enqueueEventList(final String convID, final List<Message.EventNotification> eventNotifications, final int eventKind, final boolean isFullUpdate, final BasicCallback callback) {
        if (null == eventNotifications || eventNotifications.isEmpty()) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
            return;
        }
        Task.call(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                processEventInBatch(convID, eventNotifications, eventKind, isFullUpdate, callback);
                return null;
            }
        }, eventExecutor);
    }

    public void clearCache() {
        ((ThreadPoolExecutor) eventExecutor).getQueue().clear();
    }

    private void processEventInBatch(String convID, List<Message.EventNotification> eventNotifications, int eventKind, boolean isFullUpdate, BasicCallback callback) {
        EventNotificationsWrapper wrapper = EventNotificationWrapperBuilder.buildWrapper(eventKind, 0);
        if (null != wrapper) {
            wrapper.onProcess(convID, eventNotifications, eventKind, isFullUpdate, callback);
        }
    }

}
