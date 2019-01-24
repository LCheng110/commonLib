package cn.jpush.im.android.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;

/**
 * Created by xiongtc on 16/8/16.
 */
public class OnlineMsgRecvStorage {
    private static final String TAG = "OnlineMsgRecvStorage";

    public static final String SERVER_ID = "message_id";

    public static final String CREATE_TIME = "create_time";


    public static Task<Long> insertInBackground(long serverMsgId, long cTime, String msgTableName) {
        ContentValues values = new ContentValues();
        values.put(SERVER_ID, serverMsgId);
        values.put(CREATE_TIME, cTime);

        return CRUDMethods.insertAsync(msgTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
    }

    public static Task<Void> insertInBatch(final InternalConversation conv, final Collection<InternalMessage> msgs, final String msgTableName) {
        return CRUDMethods.execInTransactionAsync(new CRUDMethods.TransactionCallback<Void>() {
            @Override
            public Void execInTransaction() {
                for (InternalMessage msg : msgs) {
                    long serverMsgId = msg.getServerMessageId();
                    ContentValues values = new ContentValues();
                    values.put(SERVER_ID, serverMsgId);
                    values.put(CREATE_TIME, msg.getCreateTime());
                    CRUDMethods.insertSync(msgTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
                    conv.addServerMsgIdToList(serverMsgId);//将这个msgId加入到conv的去重列表里去。
                }
                return null;
            }
        });
    }

    //从online msg id 表中，删除指定行数（按server msg id 升序排列）。
    public static void removeRowSync(String msgTableName, int rowCount) {
        CRUDMethods.execSQLSync("DELETE FROM " + msgTableName + " WHERE " + SERVER_ID + " IN (SELECT "
                + SERVER_ID + " FROM " + msgTableName + " ORDER BY " + CREATE_TIME + " ASC LIMIT " + rowCount + ")", null);
    }


    public static Set<Long> queryAllSync(String msgTableName) {
        Set<Long> result = new HashSet<Long>();
        Cursor cursor = CRUDMethods.querySync(msgTableName, null, null, null, null, null, CREATE_TIME + " asc", null);
        if (cursor != null && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    result.add(cursor.getLong(cursor.getColumnIndex(SERVER_ID)));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }

        return result;
    }
}
