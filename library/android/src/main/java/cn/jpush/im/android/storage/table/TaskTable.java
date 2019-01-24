package cn.jpush.im.android.storage.table;

import android.database.sqlite.SQLiteDatabase;

/* Unused */
public class TaskTable implements AbstractTable {
    private static final String TAG = "TaskTable";

    private static final String TASK_TABLE_CREATE = " (_id INTEGER PRIMARY KEY AUTOINCREMENT" + ",task_type TEXT"
            + ",params TEXT)";
    public static final String TASK_TABLE_NAME = "jpush_task";

    public static void create(SQLiteDatabase database) {
        database.execSQL(COMMON_CREATE_TABLE_HEADER + TASK_TABLE_NAME + TASK_TABLE_CREATE);
    }

    public static void drop(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE_NAME + ";");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {

    }
}
