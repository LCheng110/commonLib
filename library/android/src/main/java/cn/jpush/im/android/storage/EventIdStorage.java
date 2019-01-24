package cn.jpush.im.android.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.storage.table.EventIdTable;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by hxhg on 2017/6/21.
 */

public class EventIdStorage {
    private static final String TAG = "EventIdStorage";

    private static final String EVENT_ID = "event_id";

    private static final String CREATE_TIME = "create_time";

    static Task<Long> insertInBackground(long serverMsgId, long cTime, String eventIdTableName) {
        ContentValues values = new ContentValues();
        values.put(EVENT_ID, serverMsgId);
        values.put(CREATE_TIME, cTime);

        return CRUDMethods.insertAsync(eventIdTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    static Task<Void> insertInBatch(final Collection<Message.EventNotification> eventNotifications, final String eventIdTableName, final long gid) {
        return CRUDMethods.execInTransactionAsync(new CRUDMethods.TransactionCallback<Void>() {
            @Override
            public Void execInTransaction() {
                for (Message.EventNotification eventNotification : eventNotifications) {
                    long eventId = eventNotification.getEventId();
                    ContentValues values = new ContentValues();
                    values.put(EVENT_ID, eventId);
                    values.put(CREATE_TIME, eventNotification.getCtimeMs());
                    try {
                        CRUDMethods.insertSync(eventIdTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
                    } catch (SQLiteException e) {
                        if (e.getMessage().contains("no such table")) {
                            Logger.w(TAG, "no such table " + eventIdTableName + " find in db when insert in batch ,try to create one");
                            CRUDMethods.execSQLSync(EventIdTable.getCreateSQL(eventIdTableName), null);
                        }
                    }
                    //将eventId加入到统一管理的eventid list中去。
                    EventIdListManager.getInstance().addEventIdToList(gid, eventId);
                }
                return null;
            }
        });
    }

    //从event id 表中，删除指定行数（按server msg id 升序排列）。
    public static void removeRowSync(String eventIdTableName, int rowCount) {
        CRUDMethods.execSQLSync("DELETE FROM " + eventIdTableName + " WHERE " + EVENT_ID + " IN (SELECT "
                + EVENT_ID + " FROM " + eventIdTableName + " ORDER BY " + CREATE_TIME + " ASC LIMIT " + rowCount + ")", null);
    }


    public static Set<Long> queryAllSync(String eventIdTableName) {
        Set<Long> result = new HashSet<Long>();
        try {
            Cursor cursor = CRUDMethods.querySync(eventIdTableName, null, null, null, null, null, CREATE_TIME + " asc", null);
            if (cursor != null && cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        result.add(cursor.getLong(cursor.getColumnIndex(EVENT_ID)));
                    }
                } finally {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException e) {
            if (e.getMessage().contains("no such table")) {
                Logger.w(TAG, "no such table " + eventIdTableName + " find in db when query all,try to create one");
                CRUDMethods.execSQLSync(EventIdTable.getCreateSQL(eventIdTableName), null);
            }
        }
        return result;
    }
}
