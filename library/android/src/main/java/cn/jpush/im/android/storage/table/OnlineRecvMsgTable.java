package cn.jpush.im.android.storage.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.storage.ConversationStorage;
import cn.jpush.im.android.storage.MessageStorage;
import cn.jpush.im.android.storage.OnlineMsgRecvStorage;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by xiongtc on 16/8/16.
 */
public class OnlineRecvMsgTable implements AbstractTable {
    private static final String TAG = "OnlineRecvMsgTable";

    private static final String ONLINE_RECV_MSG_TABLE_CREATE = " (message_id BIGINT PRIMARY KEY" + ",create_time BIGINT)";


    public static void create(SQLiteDatabase database, String tableName) {
        database.execSQL(getCreateSQL(tableName));
    }

    public static String getCreateSQL(String tableName) {
        return COMMON_CREATE_TABLE_HEADER + tableName + ONLINE_RECV_MSG_TABLE_CREATE;
    }

    public static void drop(SQLiteDatabase database) {
        database.beginTransaction();
        List<String> onlineMsgTableNames = getOnlineMsgRecvTableNames(database);
        for (String onlineMsgTableName : onlineMsgTableNames) {
            database.execSQL("DROP TABLE IF EXISTS " + onlineMsgTableName + ";");
        }
        database.setTransactionSuccessful();
        database.endTransaction();
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        //do nothing...
    }

    //从消息table中把最近200条的msg id 初始化导入online recv msg table中。
    static void initialImport(SQLiteDatabase db, Set<String> msgTableNames, Collection<String> onlineMsgTableNames) {
        Logger.d(TAG, "start initial import.");
        db.beginTransaction();
        Iterator<String> iterator = msgTableNames.iterator();
        Iterator<String> iterator2 = onlineMsgTableNames.iterator();
        while (iterator.hasNext() && iterator2.hasNext()) {
            String msgTableName = iterator.next();
            String onlineMsgTableName = iterator2.next();
            //初始从msg table中导入时，online msg table还不存在，需要首先创建online msg table.
            create(db, onlineMsgTableName);

            String sql = "select * from " + msgTableName + " order by " + MessageStorage.CREATE_TIME + " desc "
                    + "limit " + InternalConversation.ONLINE_MSGID_TRIM_SIZE + " offset 0";
            Cursor cursor = db.rawQuery(sql, null);
            if (null != cursor && cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        // 这里如果数据库是从很早的版本升级上来的话，所有的msg server id 都是0，这里插入到online msg table中就会有问题。进而就可能导致第一次消息同步拿下来的newlist无法去重。
                        // 针对这个问题，后台做了相应处理，当用户第一次登陆到新版本上时，还是通过老的离线方式下发消息。
                        long serverMsgId = cursor.getLong(cursor.getColumnIndex(MessageStorage.SERVER_MESSAGE_ID));
                        if (0 == serverMsgId) {
                            Logger.d(TAG, " server msg id is 0, abort this msg");
                            continue;
                        }
                        long cTime = cursor.getLong(cursor.getColumnIndex(MessageStorage.CREATE_TIME));
                        ContentValues values = new ContentValues();
                        values.put(OnlineMsgRecvStorage.SERVER_ID, serverMsgId);
                        values.put(OnlineMsgRecvStorage.CREATE_TIME, cTime);
                        db.insertWithOnConflict(onlineMsgTableName, null, values, SQLiteDatabase.CONFLICT_IGNORE);
                    }
                } finally {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }

        }
        db.setTransactionSuccessful();
        db.endTransaction();
        Logger.d(TAG, "initial import finished.");
    }

    private static List<String> getOnlineMsgRecvTableNames(SQLiteDatabase db) {
        List<String> onlineMsgTableNames = new ArrayList<String>();
        //拿到所有的message table name
        Cursor cursor = db.query(ConversationTable.CONVERSATION_TABLE_NAME, new String[]{ConversationStorage.TARGET_ID, ConversationStorage.TARGET_APPKEY}, null, null, null, null, null);
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    String targetId = cursor.getString(cursor.getColumnIndex(ConversationStorage.TARGET_ID));
                    String targetAppkey = cursor.getString(cursor.getColumnIndex(ConversationStorage.TARGET_APPKEY));

                    //将取到的targeID和targetAppkey拼装之后，再取hashCode，加上前缀就是online msg table的名字。
                    String onlineMsgTableName = ConversationStorage.PREFIX_ONLINE_MSG_TABLE_NAME + ConversationStorage.tableSuffixGenerator(targetId, targetAppkey);
                    onlineMsgTableNames.add(onlineMsgTableName);
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        Logger.d(TAG, "alterMessageTable table names = " + onlineMsgTableNames);
        return onlineMsgTableNames;
    }

}
