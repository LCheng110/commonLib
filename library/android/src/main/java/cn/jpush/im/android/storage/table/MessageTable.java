package cn.jpush.im.android.storage.table;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.jpush.im.android.storage.ConversationStorage;
import cn.jpush.im.android.storage.MessageStorage;
import cn.jpush.im.android.utils.Logger;

public class MessageTable implements AbstractTable {
    private static final String TAG = "MessageTable";

    // TODO: 16/8/16 考虑serverMsgId是否应该建立索引？
    private static final String MESSAGE_TABLE_CREATE = " (_id INTEGER PRIMARY KEY AUTOINCREMENT"
            + ",from_name TEXT,from_id TEXT,direct TEXT,content_type TEXT"
            + ",content TEXT,status TEXT,create_time VARCHAR(20),server_message_id BIGINT"
            + ",origin_meta TEXT,from_appkey TEXT,set_from_name INTEGER"
            + ",have_read INTEGER,unreceipt_count INTEGER,unreceipt_mtime BIGINT"
            + ",at_list TEXT)";

    public static void create(SQLiteDatabase database, String tableName) {
        database.execSQL(getCreateSQL(tableName));
    }

    public static String getCreateSQL(String tableName) {
        return COMMON_CREATE_TABLE_HEADER + tableName + MESSAGE_TABLE_CREATE;
    }

    public static void drop(SQLiteDatabase database) {
        Set<String> msgTableNames = getMsgTableNamesAndOnlineMsgTableNames(database).keySet();
        for (String msgTableName : msgTableNames) {
            database.execSQL("DROP TABLE IF EXISTS " + msgTableName + ";");
        }
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.beginTransaction();
        alterMessageTable(oldVersion, database);

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    //同时返回会话所对应的msgTableName和onlineMsgTableName
    //result map key : msgTableNames
    //result map value : onlineMsgTableNames
    private static Map<String, String> getMsgTableNamesAndOnlineMsgTableNames(SQLiteDatabase db) {
        Map<String, String> result = new HashMap<String, String>();
        //拿到所有的message table name
        Cursor cursor = db.query(ConversationTable.CONVERSATION_TABLE_NAME, new String[]{ConversationStorage.MSG_TABLE_NAME, ConversationStorage.TARGET_ID, ConversationStorage.TARGET_APPKEY}, null, null, null, null, null);
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    String msgTableName = cursor.getString(cursor.getColumnIndex(ConversationStorage.MSG_TABLE_NAME));
                    String targetId = cursor.getString(cursor.getColumnIndex(ConversationStorage.TARGET_ID));
                    String targetAppkey = cursor.getString(cursor.getColumnIndex(ConversationStorage.TARGET_APPKEY));
                    String onlineMsgTableName = ConversationStorage.PREFIX_ONLINE_MSG_TABLE_NAME + ConversationStorage.tableSuffixGenerator(targetId, targetAppkey);
                    result.put(msgTableName, onlineMsgTableName);
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        Logger.d(TAG, "get msg and onlineMsg table names. result = " + result);
        return result;
    }

    private static void alterMessageTable(int oldVersion, SQLiteDatabase db) {
        Map<String, String> tableNames = getMsgTableNamesAndOnlineMsgTableNames(db);
        Set<String> msgTableNames = tableNames.keySet();
        Collection<String> onlineMsgTableNames = tableNames.values();
        for (String msgTableName : msgTableNames) {
            if (5 > oldVersion) {//4 以下的数据库版本message表需要增加一列from_appkey
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.FROM_APPKEY + " TEXT");
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.SERVER_MESSAGE_ID + " BIGINT");
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.ORIGIN_META + " TEXT");
            }

            if (9 > oldVersion) {
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.AT_LIST + " TEXT");
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.SET_FROM_NAME + " INTEGER");
            }

            if (12 > oldVersion) {
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.HAVE_READ + " INTEGER");
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.UNRECEIPT_COUNT + " INTEGER");
                db.execSQL("ALTER TABLE " + msgTableName + " ADD COLUMN " + MessageStorage.UNRECEIPT_MTIME + " BIGINT");
            }
        }

        if (9 > oldVersion) {
            //需要将msg表中数据初始化导入到online msg table中去
            OnlineRecvMsgTable.initialImport(db, msgTableNames, onlineMsgTableNames);
        }

        if (11 > oldVersion) {
            //初始化为所有的会话创建一个event id table
            EventIdTable.initialCreate(db, onlineMsgTableNames);
        }
    }
}
