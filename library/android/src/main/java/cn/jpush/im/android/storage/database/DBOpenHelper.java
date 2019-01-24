package cn.jpush.im.android.storage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.util.concurrent.atomic.AtomicInteger;

import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.storage.table.ConversationTable;
import cn.jpush.im.android.storage.table.EventIdTable;
import cn.jpush.im.android.storage.table.EventNotificationTable;
import cn.jpush.im.android.storage.table.GroupTable;
import cn.jpush.im.android.storage.table.MessageTable;
import cn.jpush.im.android.storage.table.TaskTable;
import cn.jpush.im.android.storage.table.UserTable;
import cn.jpush.im.android.utils.Logger;

public class DBOpenHelper extends AbstractSQLiteOpenHelper {
    private static final String TAG = "DBOpenHelper";

    private static AtomicInteger mOpenCounter = new AtomicInteger();
    private static DBOpenHelper instance;
    private static JMSQLiteDatabase mDatabase;
    private static long curUserID;
    private static String mDatabaseName;
    private static DBOpenCallback sCallback;


    private DBOpenHelper(Context context) {
        super(context, mDatabaseName, null, DATABASE_VERSION);
    }

    public synchronized static DBOpenHelper getInstance(Context context) {
        if (null == instance) {
            curUserID = IMConfigs.getUserID();
            mDatabaseName = (curUserID == 0 ? "default" : curUserID) + DATABASE_FOOTER;
            Logger.d(TAG, "[onInit]open or create database " + mDatabaseName);
            return instance = new DBOpenHelper(context);
        }
        return instance;
    }

    //当用户切换时，需要重新加载数据库
    public synchronized static void switchUser(long currentUserID, DBOpenCallback callback) {
        sCallback = callback;
        curUserID = currentUserID;
        mDatabaseName = curUserID + DATABASE_FOOTER;
        if (null != mDatabase && mDatabase.isOpen()) {
            if (mDatabase.inTransactionSync()) {
                mDatabase.endTransactionSync();
            }
            mDatabase.close();//关掉之前的db
        }

        instance = new DBOpenHelper(JMessage.mContext);
        //打开一次database以触发可能的数据库升级逻辑
        mDatabase = instance.getWritableDatabaseSync();
        mOpenCounter.set(0);
        mDatabase.close();


    }

    @Override
    public void onOpen(SQLiteDatabase db) {
        super.onOpen(db);
        if (null != sCallback) {
            sCallback.onOpen(db);
            //回调一次后将callback清掉
            sCallback = null;
        }
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        ConversationTable.create(db);
        GroupTable.create(db);
        TaskTable.create(db);
        UserTable.create(db);
        EventNotificationTable.create(db);
        EventIdTable.create(db, EventIdTable.GENERAL_EVENT_ID_TABLE_NAME);
    }

    public synchronized JMSQLiteDatabase openDatabaseSync() {
        if (mOpenCounter.incrementAndGet() == 1) {
            mDatabase = instance.getWritableDatabaseSync();
            return mDatabase;
        }
        return mDatabase;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d(TAG,
                "[onUpgrade]update database " + mDatabaseName + "version " + oldVersion + " to "
                        + newVersion);
        ConversationTable.onUpgrade(db, oldVersion, newVersion);
        EventNotificationTable.onUpgrade(db, oldVersion, newVersion);
        UserTable.onUpgrade(db, oldVersion, newVersion);
        MessageTable.onUpgrade(db, oldVersion, newVersion);
        GroupTable.onUpgrade(db, oldVersion, newVersion);
        Logger.d(TAG, "[onUpgrade]update database success!");
    }


    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Logger.d(TAG,
                "[onDowngrade]downgrade database " + mDatabaseName + "version " + oldVersion + " to "
                        + newVersion);
        ConversationTable.drop(db);
        EventNotificationTable.drop(db);
        GroupTable.drop(db);
        TaskTable.drop(db);
        UserTable.drop(db);
        EventIdTable.drop(db, EventIdTable.GENERAL_EVENT_ID_TABLE_NAME);
        onCreate(db);
        Logger.d(TAG, "[onDowngrade]downgrade database success!");
    }

    /**
     * sdk数据库变更记录：
     * ver 1 -- 1.1.3以前:初始版本
     * ver 2 -- 1.1.3b562 :conversation 中增加title和avatar字段。添加event table用于暂存群组事件
     * ver 3 -- 1.1.4:event table中添加othernames,用来存储事件发生时，群中其他成员的displayNames
     * ver 4 -- 1.1.5:event table中增加userdisplaynames,用来存储被操作者的displayNames
     * ver 5 -- 1.2.0:userinfo 增加appkey,message 增加from_appkey,conversation 增加target_appkey,group表增加group_owner_id，group_members
     * 从存放username list改为uid list
     * ver 6 -- 1.2.1:userinfo/groupinfo 增加nodisturb. groupinfo增加max_member_count
     * ver 7 -- 1.2.3:修复1.2.1升级的问题
     * ver 8 -- 1.3.1:userinfo table新增friend字段
     * ver 9 -- 2.1.0:增加at_list字段,@群组成员和增加屏蔽群组group_blocked字段. 增加onlineMsgRecvTable
     * ver 10 -- 2.2.0:user table增加mtime，表示userinfo最后更新时间。
     * ver 11 -- 2.2.1:增加event id table，用于事件同步的去重。
     * ver 12 -- 2.3.0:
     *             1.conversationTable增加unread_cnt_mtime，表示会话未读数最后一次被清空的时间。
     *             2.已读回执相关：msgTable增加have_read、unreceipt_count、unreceipt_mtime
     *             3.conversation 中增加extra字段
     *             4.groupTable增加avatar字段
     *             5.userTable 中增加extras
     */
    private static final int DATABASE_VERSION = 12;


    private static final String DATABASE_FOOTER = "jpushim.db";

    public interface DBOpenCallback {
        void onOpen(SQLiteDatabase db);
    }
}
