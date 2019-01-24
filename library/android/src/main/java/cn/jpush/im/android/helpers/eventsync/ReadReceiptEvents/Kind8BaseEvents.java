package cn.jpush.im.android.helpers.eventsync.ReadReceiptEvents;

import java.util.List;

import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.storage.EventIdListManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/8/17.
 */

public abstract class Kind8BaseEvents {
    List<Message.EventNotification> notifications;

    Kind8BaseEvents(List<Message.EventNotification> notifications) {
        this.notifications = notifications;
    }

    public abstract void merge();

    public abstract void afterMerge(BasicCallback callback);

    public void afterMergeFinished(BasicCallback callback, int code, String msg) {
        //事件id加入到去重列表,这里的gid填0，所有的kind8类型事件都将进入到通用事件去重容器中去。
        EventIdListManager.getInstance().insertToEventIdList(0L, notifications);
        CommonUtils.doCompleteCallBackToUser(callback, code, msg);
    }
}
