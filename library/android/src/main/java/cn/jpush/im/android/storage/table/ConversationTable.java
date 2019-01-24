package cn.jpush.im.android.storage.table;

import android.database.sqlite.SQLiteDatabase;

import cn.jpush.im.android.storage.ConversationStorage;

public class ConversationTable implements AbstractTable {

    private static final String TAG = "ConversationTable";

    public static final String CONVERSATION_TABLE_NAME = "jpush_conversation";

    private static final String CONVERSATION_TABLE_CREATE = " (_id INTEGER PRIMARY KEY AUTOINCREMENT"
            + ",id TEXT" + ",type TEXT" + ",target_id TEXT" + ",target_nickname TEXT"
            + ",latest_type TEXT" + ",latest_text TEXT" + ",latest_date VARCHAR(20)"
            + ",msg_table_name TEXT" + ",unread_cnt INT" + ",title TEXT" + ",avatar TEXT" + ",unread_cnt_mtime BIGINT"
            + ",target_appkey TEXT" + ",extra TEXT)";

    public static void create(SQLiteDatabase database) {
        database.execSQL(COMMON_CREATE_TABLE_HEADER + CONVERSATION_TABLE_NAME + CONVERSATION_TABLE_CREATE);
    }

    public static void drop(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + CONVERSATION_TABLE_NAME + ";");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.beginTransaction();
        if (2 > oldVersion) { // 1.1.3 ver2 ConversationTable 增加title和avatar字段
            database.execSQL("ALTER TABLE " + CONVERSATION_TABLE_NAME + " ADD COLUMN " + ConversationStorage.TITLE + " TEXT");
            database.execSQL("ALTER TABLE " + CONVERSATION_TABLE_NAME + " ADD COLUMN " + ConversationStorage.AVATAR + " TEXT");
        }
        if (5 > oldVersion) { // 1.2.0 ver5 ConversationTable 增加target_appkey
            database.execSQL("ALTER TABLE " + CONVERSATION_TABLE_NAME + " ADD COLUMN " + ConversationStorage.TARGET_APPKEY + " TEXT");
        }
        if (12 > oldVersion) { // 2.3.0 ver12 ConversationTable 增加unread_cnt_mtime和extra
            database.execSQL("ALTER TABLE " + CONVERSATION_TABLE_NAME + " ADD COLUMN " + ConversationStorage.UNREAD_CNT_MTIME + " BIGINT");
            database.execSQL("ALTER TABLE " + CONVERSATION_TABLE_NAME + " ADD COLUMN " + ConversationStorage.EXTRA + " TEXT");
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }
}
