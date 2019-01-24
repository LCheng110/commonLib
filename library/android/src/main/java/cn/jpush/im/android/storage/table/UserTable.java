package cn.jpush.im.android.storage.table;

import android.database.sqlite.SQLiteDatabase;

import cn.jpush.im.android.storage.UserInfoStorage;

public class UserTable implements AbstractTable {
    private static final String TAG = "UserTable";

    public static final String USERS_TABLE_NAME = "jpush_users";
    private static final String USERS_TABLE_CREATE = " (_id INTEGER PRIMARY KEY AUTOINCREMENT" + ",uid TEXT"
            + ",username TEXT" + ",nickname TEXT" + ",note_name TEXT" + ",note_text TEXT"
            + ",star INTEGER" + ",blacklist INTEGER" + ",avatar TEXT" + ",birthday TEXT"
            + ",signature TEXT" + ",gender TEXT" + ",region TEXT" + ",address TEXT"
            + ",nodisturb INTEGER" + ",friend INTEGER" + ",appkey TEXT" + ",mtime INTEGER"
            + ",extras TEXT)";

    private static final String USERS_UID_INDEX_NAME = "user_id_index";
    private static final String USERS_UID_INDEX_CREATE = " on " + USERS_TABLE_NAME + "(uid)";
    private static final String USERS_USERNAME_INDEX_NAME = "user_username_index";
    private static final String USERS_USERNAME_INDEX_CREATE = " on " + USERS_TABLE_NAME + "(username,appkey)";


    public static void create(SQLiteDatabase database) {
        database.execSQL(COMMON_CREATE_TABLE_HEADER + USERS_TABLE_NAME + USERS_TABLE_CREATE);
        createIndex(database);
    }

    private static void createIndex(SQLiteDatabase database) {
        database.execSQL(COMMON_CREATE_INDEX_HEADER + USERS_UID_INDEX_NAME + USERS_UID_INDEX_CREATE);
        database.execSQL(COMMON_CREATE_INDEX_HEADER + USERS_USERNAME_INDEX_NAME + USERS_USERNAME_INDEX_CREATE);
    }

    public static void drop(SQLiteDatabase database) {
        database.execSQL("DROP TABLE IF EXISTS " + USERS_TABLE_NAME + ";");
    }

    public static void onUpgrade(SQLiteDatabase database, int oldVersion, int newVersion) {
        database.beginTransaction();
        if (4 >= oldVersion) { //from 1.1.5 to 1.2.0
            database.execSQL("ALTER TABLE " + USERS_TABLE_NAME + " ADD COLUMN " + UserInfoStorage.KEY_APPKEY + " TEXT");
        }
        if (5 >= oldVersion) {
            database.execSQL("ALTER TABLE " + USERS_TABLE_NAME + " ADD COLUMN " + UserInfoStorage.KEY_NODISTURB + " INTEGER");
        }

        if (7 >= oldVersion) {
            database.execSQL("ALTER TABLE " + USERS_TABLE_NAME + " ADD COLUMN " + UserInfoStorage.KEY_ISFRIEND + " INTEGER");
        }

        if (8 >= oldVersion) {
            database.execSQL("ALTER TABLE " + USERS_TABLE_NAME + " ADD COLUMN " + UserInfoStorage.KEY_ADDRESS + " TEXT");
        }

        if (9 >= oldVersion) {
            database.execSQL("ALTER TABLE " + USERS_TABLE_NAME + " ADD COLUMN " + UserInfoStorage.KEY_MTIME + " INTEGER");
        }

        if (11 >= oldVersion) {
            database.execSQL("ALTER TABLE " + USERS_TABLE_NAME + " ADD COLUMN " + UserInfoStorage.KEY_EXTRAS + " TEXT");
        }

        database.setTransactionSuccessful();
        database.endTransaction();
    }
}
