package cn.jpush.im.android.storage;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalEventNotificationContent;
import cn.jpush.im.android.storage.table.EventNotificationTable;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

/**
 * 暂时没有用到
 */
public class EventStorage {

    private static final String TAG = "EventStorage";

    public static final String KEY_EVENT_GROUP_ID = "group_id";

    public static final String KEY_EVENT_OPERATOR_ID = "operator";

    public static final String KEY_EVENT_CREATE_TIME = "create_time";

    public static final String KEY_EVENT_USERNAMES = "usernames";

    public static final String KEY_EVENT_USERDISPLAYNAMES = "userdisplaynames";

    public static final String KEY_EVENT_OTHERNAMES = "othernames";

    public static Task<Boolean> insertOrUpdateWhenExistsInBackground(final long groupID, final long createTime, final InternalEventNotificationContent eventNotificationContent) {

        return queryExistsInBackground(groupID).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return updateInBackground(groupID, createTime, eventNotificationContent);
                } else {
                    return insertInBackground(groupID, createTime, eventNotificationContent).onSuccess(new Continuation<Long, Boolean>() {
                        @Override
                        public Boolean then(Task<Long> task) throws Exception {
                            return task.getResult() > 0;
                        }
                    });
                }
            }
        });

    }

    private static Task<Long> insertInBackground(long groupID, long createTime, InternalEventNotificationContent eventNotificationContent) {
        if (!CommonUtils.isInited("EventStorage.insertInBackground")) {
            return Task.forResult(0L);
        }
        Logger.d(TAG, "insertInBackground to event table . groupID = " + groupID);

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_EVENT_GROUP_ID, groupID);
        contentValues.put(KEY_EVENT_CREATE_TIME, createTime);
        contentValues.put(KEY_EVENT_OPERATOR_ID, eventNotificationContent.getOperator());
        contentValues.put(KEY_EVENT_USERNAMES, JsonUtil.toJson(eventNotificationContent.getUserNames()));
        contentValues.put(KEY_EVENT_USERDISPLAYNAMES, JsonUtil.toJson(eventNotificationContent.getUserDisplayNames()));
        contentValues.put(KEY_EVENT_OTHERNAMES, JsonUtil.toJson(eventNotificationContent.getOtherMemberDisplayNames()));
        return CRUDMethods.insertAsync(EventNotificationTable.EVENT_TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
    }

    private static Task<Boolean> updateInBackground(long groupID, long createTime, InternalEventNotificationContent eventNotificationContent) {
        if (!CommonUtils.isInited("EventStorage.updateInBackground")) {
            return Task.forResult(false);
        }
        Logger.d(TAG, "updateInBackground event table . groupID = " + groupID);
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_EVENT_OPERATOR_ID, eventNotificationContent.getOperator());
        contentValues.put(KEY_EVENT_CREATE_TIME, createTime);
        contentValues.put(KEY_EVENT_USERNAMES, JsonUtil.toJson(eventNotificationContent.getUserNames()));
        contentValues.put(KEY_EVENT_USERDISPLAYNAMES, JsonUtil.toJson(eventNotificationContent.getUserDisplayNames()));
        contentValues.put(KEY_EVENT_OTHERNAMES, JsonUtil.toJson(eventNotificationContent.getOtherMemberDisplayNames()));
        return CRUDMethods.updateAsync(EventNotificationTable.EVENT_TABLE_NAME, contentValues, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)});
    }

    public static Task<Boolean> deleteInBackground(long groupID) {
        if (!CommonUtils.isInited("EventStorage.deleteInBackground")) {
            return Task.forResult(false);
        }

        return CRUDMethods.deleteAsync(EventNotificationTable.EVENT_TABLE_NAME, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)});
    }

    private static InternalEventNotificationContent cursorToEvent(long groupID, Cursor cursor) {
        if (null == cursor) {
            return null;
        }
        InternalEventNotificationContent eventNotificationContent;
        long operator = cursor.getLong(cursor.getColumnIndex(KEY_EVENT_OPERATOR_ID));
        String usernamesString = cursor.getString(cursor.getColumnIndex(KEY_EVENT_USERNAMES));
        String otherMemberDisplayNameString = cursor.getString(cursor.getColumnIndex(KEY_EVENT_OTHERNAMES));
        String userDisplauNamesString = cursor.getString(cursor.getColumnIndex(KEY_EVENT_USERDISPLAYNAMES));
        List<String> userNames = JsonUtil.formatToGivenType(usernamesString, new TypeToken<List<String>>() {
        });//数据库中拿到的是被添加成员的displayName
        List<String> otherMemberDisplayNames = JsonUtil.formatToGivenType(otherMemberDisplayNameString, new TypeToken<List<String>>() {
        });
        List<String> userDisplayNames = JsonUtil.formatToGivenType(userDisplauNamesString, new TypeToken<List<String>>() {
        });
        if (null != otherMemberDisplayNames) {
            eventNotificationContent = new InternalEventNotificationContent(groupID, operator,
                    EventNotificationContent.EventNotificationType.group_member_added, userNames, userDisplayNames, false, otherMemberDisplayNames);
        } else {
            eventNotificationContent = new InternalEventNotificationContent(groupID, operator,
                    EventNotificationContent.EventNotificationType.group_member_added, userNames, userDisplayNames, false, new ArrayList<String>());
        }
        return eventNotificationContent;
    }

    public static Task<InternalEventNotificationContent> queryInBackground(final long groupID) {
        if (!CommonUtils.isInited("EventStorage.queryInBackground")) {
            return Task.forResult(null);
        }

        return CRUDMethods.queryAsync(EventNotificationTable.EVENT_TABLE_NAME, null, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)}, null, null, null, null).onSuccess(new Continuation<Cursor, InternalEventNotificationContent>() {
            @Override
            public InternalEventNotificationContent then(Task<Cursor> task) throws Exception {
                return queryInternal(task.getResult(), groupID);
            }
        });
    }

    public static InternalEventNotificationContent querySync(final long groupID) {
        if (!CommonUtils.isInited("EventStorage.querySync")) {
            return null;
        }

        return queryInternal(CRUDMethods.querySync(EventNotificationTable.EVENT_TABLE_NAME, null, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)}, null, null, null, null), groupID);
    }

    private static InternalEventNotificationContent queryInternal(Cursor cursor, long groupID) {
        InternalEventNotificationContent eventNotificationContent = null;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    eventNotificationContent = cursorToEvent(groupID, cursor);
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return eventNotificationContent;
    }

    public static Task<Long> queryCreateTimeInBackground(long groupID) {
        if (!CommonUtils.isInited("EventStorage.queryCreateTimeInBackground")) {
            return Task.forResult(0L);
        }

        return CRUDMethods.queryAsync(EventNotificationTable.EVENT_TABLE_NAME, new String[]{KEY_EVENT_CREATE_TIME}, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)}, null, null, null, null).onSuccess(new Continuation<Cursor, Long>() {
            @Override
            public Long then(Task<Cursor> task) throws Exception {
                return queryCreateTimeInternal(task.getResult());
            }
        });
    }

    public static Long queryCreateTimeSync(long groupID) {
        if (!CommonUtils.isInited("EventStorage.queryCreateTimeInBackground")) {
            return 0L;
        }

        return queryCreateTimeInternal(CRUDMethods.querySync(EventNotificationTable.EVENT_TABLE_NAME, new String[]{KEY_EVENT_CREATE_TIME}, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)}, null, null, null, null));
    }

    private static long queryCreateTimeInternal(Cursor cursor) {
        long createTime = 0L;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    createTime = cursor.getLong(cursor.getColumnIndex(KEY_EVENT_CREATE_TIME));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return createTime;
    }

    public static Task<Boolean> queryExistsInBackground(long groupID) {
        if (!CommonUtils.isInited("EventStorage.queryExistsInBackground")) {
            return Task.forResult(false);
        }

        return CRUDMethods.queryAsync(EventNotificationTable.EVENT_TABLE_NAME, null, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)}, null, null, null, null).onSuccess(new Continuation<Cursor, Boolean>() {
            @Override
            public Boolean then(Task<Cursor> task) throws Exception {
                return queryExistsInternal(task.getResult());
            }
        });
    }

    public static boolean queryExistsSync(long groupID) {
        if (!CommonUtils.isInited("EventStorage.queryExistsSync")) {
            return false;
        }

        return queryExistsInternal(CRUDMethods.querySync(EventNotificationTable.EVENT_TABLE_NAME, null, KEY_EVENT_GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)}, null, null, null, null));
    }

    private static boolean queryExistsInternal(Cursor cursor) {
        boolean result = false;
        if (null != cursor && cursor.getCount() > 0) {
            result = true;
            cursor.close();
        } else if (null != cursor) {
            cursor.close();
        }
        return result;
    }
}
