package cn.jpush.im.android.storage;

import android.content.ContentValues;
import android.database.Cursor;

import java.util.concurrent.Callable;

import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.storage.database.DBOpenHelper;
import cn.jpush.im.android.storage.database.JMSQLiteDatabase;
import cn.jpush.im.android.utils.CommonUtils;

public class CRUDMethods {
    private static final String TAG = "CRUDMethods";

    protected static Task<Long> insertAsync(final String tableName, final ContentValues values, final int conflictAlgorithm) {
        if (!CommonUtils.isInited("insertInBackground " + tableName)) {
            return Task.forResult(0L);
        }
        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().insertWithOnConflictAsync(
                Task.<Void>forResult(null), tableName, values, conflictAlgorithm);
    }

    protected static long insertSync(final String tableName, final ContentValues values, final int conflictAlgorithm) {
        if (!CommonUtils.isInited("insert" + tableName)) {
            return 0L;
        }

        JMSQLiteDatabase database = DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync();
        return database.insertWithOnConflictSync(tableName, values, conflictAlgorithm);
    }


    protected static Task<Boolean> updateAsync(final String tableName, final ContentValues contentValues, final String whereClause, final String[] args) {
        if (!CommonUtils.isInited("updateInBackground " + tableName)) {
            return Task.forResult(false);
        }
        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().updateAsync(
                Task.<Void>forResult(null), tableName, contentValues, whereClause, args);
    }

    public static boolean updateSync(final String tableName, final ContentValues contentValues, final String whereClause, final String[] args) {
        if (!CommonUtils.isInited("update" + tableName)) {
            return false;
        }
        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().updateSync(tableName, contentValues, whereClause, args);
    }

    protected static Task<Boolean> deleteAsync(final String tableName, final String where, final String[] args) {
        if (!CommonUtils.isInited("deleteInBackground " + tableName)) {
            return Task.forResult(false);
        }
        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().deleteAsync(
                Task.<Void>forResult(null), tableName, where, args);
    }

    protected static boolean deleteSync(final String tableName, final String where, final String[] args) {
        if (!CommonUtils.isInited("deleteInBackground " + tableName)) {
            return false;
        }

        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().deleteSync(tableName, where, args);
    }

    /**
     * 此方法调用后需要手动将cursor关闭。
     */
    protected static Task<Cursor> queryAsync(final String tableName, final String[] columns, final String selection, final String[] args
            , final String groupBy, final String having, final String orderBy, final String limit) {
        if (!CommonUtils.isInited("queryInBackground " + tableName)) {
            return Task.forResult(null);
        }

        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().queryAsync(
                Task.<Void>forResult(null), tableName, columns, selection, args, groupBy, having, orderBy, limit);
    }

    protected static Cursor querySync(final String tableName, final String[] columns, final String selection, final String[] args
            , final String groupBy, final String having, final String orderBy, final String limit) {
        if (!CommonUtils.isInited("query" + tableName)) {
            return null;
        }

        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().querySync(tableName, columns, selection, args, groupBy, having, orderBy, limit);
    }

    /**
     * 此方法调用后需要手动将cursor关闭。
     */
    protected static Task<Cursor> rawQueryAsync(final String sql, final String[] args) {
        if (!CommonUtils.isInited("rawQuery ")) {
            return Task.forResult(null);
        }

        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().rawQueryAsync(
                Task.<Void>forResult(null), sql, args);
    }

    protected static Cursor rawQuerySync(final String sql, final String[] args) {
        if (!CommonUtils.isInited("rawQuery ")) {
            return null;
        }
        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().rawQuerySync(sql, args);
    }

    protected static Task<Void> execSQLAsync(final String sql, final String[] args) {
        if (!CommonUtils.isInited("execSQLAsync ")) {
            return Task.forResult(null);
        }

        return DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().execSQLAsync(
                Task.<Void>forResult(null), sql, args);
    }

    protected static void execSQLSync(final String sql, final String[] args) {
        if (!CommonUtils.isInited("execSQLSync ")) {
            return;
        }

        DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync().execSQLSync(sql, args);
    }

    public static <T> Task<T> execInTransactionAsync(final TransactionCallback<T> callback) {
        if (!CommonUtils.isInited("execInTransactionAsync ")) {
            return Task.forResult(null);
        }

        return Task.call(new Callable<T>() {
            @Override
            public T call() throws Exception {
                final JMSQLiteDatabase database = DBOpenHelper.getInstance(JMessage.mContext).openDatabaseSync();
                database.beginTransactionSync();
                T result = null;
                try {
                    result = callback.execInTransaction();
                    database.setTransactionSuccessfulSync();
                } finally {
                    database.endTransactionSync();
                }
                return result;
            }
        }, JMSQLiteDatabase.bgExecutor);
    }

    public interface TransactionCallback<T> {
        T execInTransaction();
    }
}
