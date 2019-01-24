package cn.jpush.im.android.helpers;

import android.content.ContentValues;

import java.util.List;
import java.util.concurrent.Callable;

import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.storage.CRUDMethods;
import cn.jpush.im.android.storage.ConversationStorage;
import cn.jpush.im.android.storage.MessageStorage;
import cn.jpush.im.android.utils.Logger;

public class MsgStatusResetHelper {
    private static final String TAG = "MsgStatusResetHelper";

    public MsgStatusResetHelper() {

    }

    public void resetStatus() {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                List<String> msgTables = getAllMsgTableNames();
                if (msgTables.size() > 0) {
                    for (String msgTableName : msgTables) {
                        updateStatusInSpecificTable(msgTableName);
                    }
                }
                return null;
            }
        });
    }

    private List<String> getAllMsgTableNames() {
        List<String> msgTableNames = ConversationStorage.getAllMessageTableNameSync();
        Logger.d(TAG, "[getAllMsgTableNames] msgTableNames = " + msgTableNames);
        return msgTableNames;
    }

    private boolean updateStatusInSpecificTable(String tableName) {
        ContentValues values = new ContentValues();
        values.put(MessageStorage.STATUS, MessageStatus.send_fail.toString());
        return CRUDMethods.updateSync(tableName, values,
                MessageStorage.STATUS + "= ?", new String[]{MessageStatus.send_going.toString()});
    }


}
