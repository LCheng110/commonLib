package cn.jpush.im.android.storage;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.storage.table.EventIdTable;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by hxhg on 2017/6/23.
 */

//事件去重容器管理类
public class EventIdListManager {
    private static final String TAG = "EventIdListManager";

    //缓存所有事件idlist,其中key值为groupid，当不为群组事件时，key为0
    private Map<Long, Set<Long>> eventIdListMap = new HashMap<Long, Set<Long>>();

    private static EventIdListManager instance;

    private EventIdListManager() {
    }

    public synchronized static EventIdListManager getInstance() {
        if (null == instance) {
            instance = new EventIdListManager();
        }
        return instance;
    }

    /**
     * 事件列表去重
     *
     * @param gid         如果是群组事件，填gid. 如果不是，填0
     * @param srcEventIds 需要去重的eventId list
     * @return
     */
    public Collection<Long> removeDuplicatesWithEventIdList(long gid, Collection<Long> srcEventIds) {
        if (null == srcEventIds || srcEventIds.isEmpty()) {
            Logger.d(TAG, "event id list is empty, return from remove duplicates");
            return srcEventIds;
        }
        Set<Long> eventIdList = getEventIdList(gid);
        Logger.d(TAG, " conv " + gid + " event id list = " + eventIdList);
        srcEventIds.removeAll(eventIdList);
        return srcEventIds;
    }

    public Task<Long> insertToEventIdList(long gid, long eventId, long cTime) {
        String eventIdTableName = getEventIdTableName(gid);
        Set<Long> eventIdList = getEventIdList(gid);

        CommonUtils.trimListSize(eventIdList, eventIdTableName);
        boolean needInsertToDB = eventIdList.add(eventId);
        return needInsertToDB ? EventIdStorage.insertInBackground(eventId, cTime, eventIdTableName) : null;
    }

    public Task<Void> insertToEventIdList(final long gid, Collection<Message.EventNotification> events) {
        if (null == events || events.isEmpty()) {
            return Task.forResult(null);
        }

        String eventIdTableName = getEventIdTableName(gid);
        Set<Long> eventIdList = getEventIdList(gid);

        //首先trim一下conv对象中缓存的onlineMsgList，避免onlineMsgList过长导致去重效率慢。
        CommonUtils.trimListSize(eventIdList, eventIdTableName);
        return EventIdStorage.insertInBatch(events, eventIdTableName, gid);
    }

    public boolean containsEventID(long gid, long eventId) {
        Set<Long> eventIdList = getEventIdList(gid);
        return null != eventIdList && eventIdList.contains(eventId);
    }

    void addEventIdToList(long gid, long eventId) {
        Set<Long> eventIdList = getEventIdList(gid);
        eventIdList.add(eventId);
    }

    private Set<Long> getEventIdList(long gid) {
        Set<Long> eventIdList = eventIdListMap.get(gid);
        if (null == eventIdList) {
            String eventIdTableName = getEventIdTableName(gid);
            eventIdList = EventIdStorage.queryAllSync(eventIdTableName);
            eventIdListMap.put(gid, eventIdList);
        }
        return eventIdList;
    }

    private String getEventIdTableName(long gid) {
        String eventIdTableName;
        if (0 != gid) {
            eventIdTableName = ConversationStorage.PREFIX_ONLINE_EVENT_TABLE_NAME + ConversationStorage.tableSuffixGenerator(String.valueOf(gid), "");
        } else {
            eventIdTableName = EventIdTable.GENERAL_EVENT_ID_TABLE_NAME;
        }
        return eventIdTableName;
    }

    public void clearCache() {
        eventIdListMap.clear();
    }
}
