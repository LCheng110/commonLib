package cn.jpush.im.android.storage.table;

import android.database.sqlite.SQLiteDatabase;

import cn.jpush.im.android.storage.EventStorage;

public class EventNotificationTable implements AbstractTable {
    private static final String TAG = "EventNotificationTable";

    public static final String EVENT_TABLE_NAME = "jpush_event";
    private static final String EVENT_NOTIFICATION_TABLE_CREATE = " (group_id TEXT PRIMARY KEY"
            + ",operator TEXT" + ",create_time  VARCHAR(20)" + ",usernames TEXT" + ",userdisplaynames TEXT" + ",othernames TEXT) ";


    public static void create(SQLiteDatabase database) {
        database.execSQL(COMMON_CREATE_TABLE_HEADER + EVENT_TABLE_NAME + EVENT_NOTIFICATION_TABLE_CREATE);
    }

    public static void drop(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + EVENT_TABLE_NAME + ";");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.beginTransaction();
        if (2 > oldVersion) { //FROM 1.1.3 or earlier TO 1.2.0
            database.execSQL(COMMON_CREATE_TABLE_HEADER + EVENT_TABLE_NAME + EVENT_NOTIFICATION_TABLE_CREATE);
        }

        if (3 > oldVersion) { //FROM 1.1.3 b562 TO 1.2.0
            database.execSQL("ALTER TABLE " + EVENT_TABLE_NAME + " ADD COLUMN " + EventStorage.KEY_EVENT_OTHERNAMES + " TEXT");
        }

        if (4 > oldVersion) { //from 1.1.4 to 1.2.0
            database.execSQL("ALTER TABLE " + EVENT_TABLE_NAME + " ADD COLUMN " + EventStorage.KEY_EVENT_USERDISPLAYNAMES + " TEXT");
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }
}
