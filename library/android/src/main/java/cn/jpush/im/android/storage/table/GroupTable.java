package cn.jpush.im.android.storage.table;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.List;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;

public class GroupTable implements AbstractTable {
    private static final String TAG = "GroupTable";

    public static final String GROUP_TABLE_NAME = "jpush_group";
    private static final String GROUP_TABLE_CREATE = " (_id INTEGER PRIMARY KEY AUTOINCREMENT" + ",group_id TEXT"
            + ",group_owner TEXT" + ",group_owner_id TEXT" + ",group_name TEXT" + ",group_desc TEXT" + ",group_level TEXT"
            + ",group_flag TEXT" + ",nodisturb INTEGER" + ",max_member_count INTEGER" + ",group_members TEXT" + ",avatar TEXT" + ",group_blocked INTEGER)";


    public static void create(SQLiteDatabase database) {
        database.execSQL(COMMON_CREATE_TABLE_HEADER + GROUP_TABLE_NAME + GROUP_TABLE_CREATE);
    }

    public static void drop(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + GROUP_TABLE_NAME + ";");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.beginTransaction();
        if (5 > oldVersion) { //from 1.1.5 to 1.2.0
            database.execSQL("ALTER TABLE " + GROUP_TABLE_NAME + " ADD COLUMN " + GroupStorage.GROUP_OWNER_ID + " TEXT");
            transferUsernameToUidInGroup(oldVersion, database);
        }

        if (6 > oldVersion) {
            database.execSQL("ALTER TABLE " + GROUP_TABLE_NAME + " ADD COLUMN " + GroupStorage.GROUP_NODISTURB + " INTEGER");
        }

        if (7 > oldVersion && !checkColumnExists(database, GROUP_TABLE_NAME, GroupStorage.MAX_MEMBER_COUNT)) {
            database.execSQL("ALTER TABLE " + GROUP_TABLE_NAME + " ADD COLUMN " + GroupStorage.MAX_MEMBER_COUNT + " INTEGER");
        }

        if (9 > oldVersion) {
            database.execSQL("ALTER TABLE " + GROUP_TABLE_NAME + " ADD COLUMN " + GroupStorage.GROUP_BLOCKED + " INTEGER");
        }

        if (12 > oldVersion) {
            database.execSQL("ALTER TABLE " + GROUP_TABLE_NAME + " ADD COLUMN " + GroupStorage.GROUP_AVATAR + " TEXT");
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }

    private static void transferUsernameToUidInGroup(int oldVersion, SQLiteDatabase db) {
        if (4 >= oldVersion) {
            Cursor cursor = db.query(GROUP_TABLE_NAME, new String[]{GroupStorage.GROUP_ID, GroupStorage.GROUP_MEMBERS}, null, null, null, null, null);
            if (null != cursor && cursor.getCount() > 0) {
                try {
                    long groupId;
                    List<String> membersName;
                    ContentValues contentValues = new ContentValues();
                    TypeToken<List<String>> typeToken = new TypeToken<List<String>>() {
                    };
                    while (cursor.moveToNext()) {
                        groupId = cursor.getLong(cursor.getColumnIndex(GroupStorage.GROUP_ID));
                        String memberString = cursor.getString(cursor.getColumnIndex(GroupStorage.GROUP_MEMBERS));
                        membersName = JsonUtil.formatToGivenType(memberString, typeToken);
                        //从缓存和本地查找username对应的uid.
                        List<Long> userIds = UserIDHelper.getUserIDsFromLocal(db, membersName, JCoreInterface.getAppKey());
                        //将uid list更新到group members列中
                        contentValues.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(userIds));
                        db.update(GroupTable.GROUP_TABLE_NAME, contentValues, GroupStorage.GROUP_ID + "=?",
                                new String[]{String.valueOf(groupId)});
                    }
                } finally {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }

        }
    }

    /**
     * 检查表中某列是否存在
     *
     * @param db
     * @param tableName  表名
     * @param columnName 列名
     * @return
     */
    private static boolean checkColumnExists(SQLiteDatabase db, String tableName
            , String columnName) {
        boolean result = false;
        Cursor cursor = null;

        try {
            cursor = db.rawQuery("select * from sqlite_master where name = ? and sql like ?"
                    , new String[]{tableName, "%" + columnName + "%"});
            result = null != cursor && cursor.moveToFirst();
        } catch (Exception e) {
            Logger.ee(TAG, "checkColumnExists met an exception..." + e.getMessage());
        } finally {
            if (null != cursor && !cursor.isClosed()) {
                cursor.close();
            }
        }
        Logger.d(TAG, "column \"" + columnName + "\" exists = " + result);
        return result;
    }

}
