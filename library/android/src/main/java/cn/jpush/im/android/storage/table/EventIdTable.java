package cn.jpush.im.android.storage.table;

import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;

import cn.jpush.im.android.storage.ConversationStorage;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by hxhg on 2017/6/21.
 */

public class EventIdTable implements AbstractTable {
    private static final String TAG = "EventIdTable";

    public static final String GENERAL_EVENT_ID_TABLE_NAME = "event_general";

    private static final String EVENT_ID_TABLE_CREATE = " (event_id BIGINT PRIMARY KEY" + ",create_time BIGINT)";


    public static void create(SQLiteDatabase database, String tableName) {
        database.execSQL(getCreateSQL(tableName));
    }

    public static String getCreateSQL(String tableName) {
        return COMMON_CREATE_TABLE_HEADER + tableName + EVENT_ID_TABLE_CREATE;
    }

    public static void drop(SQLiteDatabase database, String tableName) {
        database.execSQL("DROP TABLE IF EXISTS " + tableName + ";");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //do nothing...
    }

    //初始化针对每个会话创建eventId table.
    static void initialCreate(SQLiteDatabase db, Collection<String> onlineMsgTableNames) {
        Logger.d(TAG, "start initial import.");
        db.beginTransaction();
        for (String onlineMsgTableName : onlineMsgTableNames) {
            //这里拿到了onlineMsgTableNames,由于onlineMsg table和eventId table的表明差别仅仅在前缀的不同，所以将前缀替换下就是eventId table的表名
            String eventTableName = onlineMsgTableName.replace(ConversationStorage.PREFIX_ONLINE_MSG_TABLE_NAME, ConversationStorage.PREFIX_ONLINE_EVENT_TABLE_NAME);
            create(db, eventTableName);
        }

        //创建总体的event id table，用来存储除了群成员变化事件之外的事件
        create(db, GENERAL_EVENT_ID_TABLE_NAME);
        db.setTransactionSuccessful();
        db.endTransaction();
        Logger.d(TAG, "initial import finished.");
    }
}
