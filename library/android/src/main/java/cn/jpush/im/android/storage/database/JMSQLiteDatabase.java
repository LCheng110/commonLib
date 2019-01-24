/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package cn.jpush.im.android.storage.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import cn.jpush.im.android.bolts.AndroidExecutors;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;


public class JMSQLiteDatabase {

    /**
     * Database connections are locked to the thread that they are created in when using transactions.
     * We must use a single thread executor to make sure that all transactional DB actions are on
     * the same thread or else they will block.
     * <p/>
     * Symptoms include blocking on db.queryInBackground, cursor.moveToFirst, etc.
     */
    public static final ExecutorService dbExecutor = Executors.newFixedThreadPool(1);

    /**
     * another background executor . different from background executor in {@link Task#BACKGROUND_EXECUTOR}.
     * since the thread in {@link Task#BACKGROUND_EXECUTOR} may be blocked to wait the complement of a {@link Task},
     * thus if the thread and the task's continuation runs in the same executor. it will cause dead lock.
     */
    public static final ExecutorService bgExecutor = AndroidExecutors.newCachedThreadPool();

    /**
     * Queue for all database sessions. All database sessions must be serialized in order for
     * transactions to work correctly.
     */
    //TODO (grantland): do we have to serialize sessions of different databases?
    private SQLiteDatabase db;
    private int openFlags;

    /**
     * Creates a Session which opens a database connection and begins a transaction
     */
    private JMSQLiteDatabase(int flags) {
        //TODO (grantland): if (!writable) -- disable transactions?
        //TODO (grantland): if (!writable) -- do we have to serialize everything?
        openFlags = flags;

    }

    /* protected */
    static Task<JMSQLiteDatabase> openDatabaseAsync(Task<Void> task, final SQLiteOpenHelper helper, int flags) {
        final JMSQLiteDatabase db = new JMSQLiteDatabase(flags);
        return db.open(helper, task).continueWithTask(new Continuation<Void, Task<JMSQLiteDatabase>>() {
            @Override
            public Task<JMSQLiteDatabase> then(Task<Void> task) throws Exception {
                return Task.forResult(db);
            }
        });
    }

    static JMSQLiteDatabase openDatabaseSync(final SQLiteOpenHelper helper, int flags) {
        final JMSQLiteDatabase db = new JMSQLiteDatabase(flags);
        db.openSync(helper);
        return db;
    }

    public Task<Boolean> isReadOnlyAsync(Task<Void> task) {
        return task.continueWith(new Continuation<Void, Boolean>() {
            @Override
            public Boolean then(Task<Void> task) throws Exception {
                return db.isReadOnly();
            }
        });
    }

    public Task<Boolean> isOpenAsync(Task<Void> task) {
        return task.continueWith(new Continuation<Void, Boolean>() {
            @Override
            public Boolean then(Task<Void> task) throws Exception {
                return db.isOpen();
            }
        });
    }

    public boolean isOpen() {
        return db != null && db.isOpen();
    }

    /* package */ Task<Void> open(final SQLiteOpenHelper helper, Task<Void> task) {
        return task.continueWith(new Continuation<Void, SQLiteDatabase>() {
            @Override
            public SQLiteDatabase then(Task<Void> task) throws Exception {
                // get*Database() is synchronous and calls through SQLiteOpenHelper#onCreate, onUpdate,
                // etc.
                return (openFlags & SQLiteDatabase.OPEN_READONLY) == SQLiteDatabase.OPEN_READONLY
                        ? helper.getReadableDatabase()
                        : helper.getWritableDatabase();
            }
        }, dbExecutor).continueWithTask(new Continuation<SQLiteDatabase, Task<Void>>() {
            @Override
            public Task<Void> then(Task<SQLiteDatabase> task) throws Exception {
                db = task.getResult();
                return task.makeVoid();
            }
        }, bgExecutor); // We want to jump off the dbExecutor
    }

    JMSQLiteDatabase openSync(final SQLiteOpenHelper helper) {
        db = (openFlags & SQLiteDatabase.OPEN_READONLY) == SQLiteDatabase.OPEN_READONLY
                ? helper.getReadableDatabase()
                : helper.getWritableDatabase();
        return this;
    }

    /**
     * Executes a BEGIN TRANSACTION.
     *
     * @see SQLiteDatabase#beginTransaction
     */
    public Task<Void> beginTransactionAsync(Task<Void> task) {
        return task.continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                db.beginTransaction();
                return task;
            }
        }, dbExecutor).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public void beginTransactionSync() {
        db.beginTransaction();
    }

    /**
     * Sets a transaction as successful.
     *
     * @see SQLiteDatabase#setTransactionSuccessful
     */
    public Task<Void> setTransactionSuccessfulAsync(Task<Void> task) {
        return task.onSuccessTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                db.setTransactionSuccessful();
                return task;
            }
        }, dbExecutor).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public void setTransactionSuccessfulSync() {
        db.setTransactionSuccessful();
    }

    /**
     * Ends a transaction.
     *
     * @see SQLiteDatabase#endTransaction
     */
    public Task<Void> endTransactionAsync(Task<Void> task) {
        return task.continueWith(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                db.endTransaction();
                // We want to swallow any exceptions from our Session task
                return null;
            }
        }, dbExecutor).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public void endTransactionSync() {
        if (null != db) {
            db.endTransaction();
        }
    }

    public boolean inTransactionSync() {
        return null != db && db.inTransaction();
    }

    /**
     * Closes this session, sets the transaction as successful if no errors occurred, ends the
     * transaction and closes the database connection.
     */
    public Task<Void> closeAsync(Task<Void> task) {
        return task.continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                db.close();
                return task;
            }
        }, dbExecutor).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public void close() {
        if (null != db) {
            db.close();
        }
    }

    /**
     * Runs a SELECT queryInBackground.
     *
     * @see SQLiteDatabase#query
     */
    public Task<Cursor> queryAsync(Task<Void> task, final String table, final String[] columns, final String selection,
                                   final String[] args, final String groupBy, final String having, final String orderBy, final String limit) {
        return task.onSuccess(new Continuation<Void, Cursor>() {
            @Override
            public Cursor then(Task<Void> task) throws Exception {
                if (null != limit) {
                    return db.query(table, columns, selection, args, groupBy, having, orderBy, limit);
                } else {
                    return db.query(table, columns, selection, args, groupBy, having, orderBy);
                }
            }
        }, dbExecutor).onSuccess(new Continuation<Cursor, Cursor>() {
            @Override
            public Cursor then(Task<Cursor> task) throws Exception {
                Cursor cursor = JMSQLiteCursor.create(task.getResult(), dbExecutor);
          /* Ensure the cursor window is filled on the dbExecutor thread. We need to do this because
           * the cursor cannot be filled from a different thread than it was created on.
           */
                cursor.getCount();
                return cursor;
            }
        }, dbExecutor).continueWithTask(new Continuation<Cursor, Task<Cursor>>() {
            @Override
            public Task<Cursor> then(Task<Cursor> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public Cursor querySync(final String table, final String[] columns, final String selection,
                            final String[] args, final String groupBy, final String having, final String orderBy, final String limit) {
        if (null != limit) {
            return db.query(table, columns, selection, args, groupBy, having, orderBy, limit);
        } else {
            return db.query(table, columns, selection, args, groupBy, having, orderBy);
        }
    }

    /**
     * Executes an INSERT.
     *
     * @see SQLiteDatabase#insertWithOnConflict
     */
    public Task<Long> insertWithOnConflictAsync(Task<Void> task, final String table, final ContentValues values,
                                                final int conflictAlgorithm) {
        return task.onSuccess(new Continuation<Void, Long>() {
            @Override
            public Long then(Task<Void> task) throws Exception {
                return db.insertWithOnConflict(table, null, values, conflictAlgorithm);
            }
        }, dbExecutor).continueWithTask(new Continuation<Long, Task<Long>>() {
            @Override
            public Task<Long> then(Task<Long> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public long insertWithOnConflictSync(final String table, final ContentValues values,
                                         final int conflictAlgorithm) {
        return db.insertWithOnConflict(table, null, values, conflictAlgorithm);
    }

    /**
     * Executes an INSERT and throws on SQL errors.
     *
     * @see SQLiteDatabase#insertOrThrow
     */
    public Task<Void> insertOrThrowAsync(Task<Void> task, final String table, final ContentValues values) {
        return task.onSuccess(new Continuation<Void, Long>() {
            @Override
            public Long then(Task<Void> task) throws Exception {
                return db.insertOrThrow(table, null, values);
            }
        }, dbExecutor).continueWithTask(new Continuation<Long, Task<Long>>() {
            @Override
            public Task<Long> then(Task<Long> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor).makeVoid();
    }

    public void insertOrThrowSync(final String table, final ContentValues values) {
        db.insertOrThrow(table, null, values);
    }

    /**
     * Executes an UPDATE.
     *
     * @see SQLiteDatabase#update
     */
    public Task<Boolean> updateAsync(Task<Void> task, final String table, final ContentValues values,
                                     final String where, final String[] args) {
        return task.onSuccess(new Continuation<Void, Integer>() {
            @Override
            public Integer then(Task<Void> task) throws Exception {
                return db.update(table, values, where, args);
            }
        }, dbExecutor).continueWithTask(new Continuation<Integer, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Integer> task) throws Exception {
                // We want to jump off the dbExecutor
                return Task.forResult(task.getResult() > 0);
            }
        }, bgExecutor);
    }

    public boolean updateSync(final String table, final ContentValues values,
                              final String where, final String[] args) {
        return db.update(table, values, where, args) > 0;
    }

    /**
     * Executes a DELETE.
     *
     * @see SQLiteDatabase#delete
     */
    public Task<Boolean> deleteAsync(Task<Void> task, final String table, final String where, final String[] args) {
        return task.onSuccess(new Continuation<Void, Integer>() {
            @Override
            public Integer then(Task<Void> task) throws Exception {
                return db.delete(table, where, args);
            }
        }, dbExecutor).continueWithTask(new Continuation<Integer, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Integer> task) throws Exception {
                // We want to jump off the dbExecutor
                return Task.forResult(task.getResult() > 0);
            }
        }, bgExecutor);
    }

    public boolean deleteSync(final String table, final String where, final String[] args) {
        return db.delete(table, where, args) > 0;
    }

    /**
     * Runs a raw queryInBackground.
     *
     * @see SQLiteDatabase#rawQuery
     */
    public Task<Cursor> rawQueryAsync(Task<Void> task, final String sql, final String[] args) {
        return task.onSuccess(new Continuation<Void, Cursor>() {
            @Override
            public Cursor then(Task<Void> task) throws Exception {
                return db.rawQuery(sql, args);
            }
        }, dbExecutor).onSuccess(new Continuation<Cursor, Cursor>() {
            @Override
            public Cursor then(Task<Cursor> task) throws Exception {
                Cursor cursor = JMSQLiteCursor.create(task.getResult(), dbExecutor);
                // Ensure the cursor window is filled on the dbExecutor thread. We need to do this because
                // the cursor cannot be filled from a different thread than it was created on.
                cursor.getCount();
                return cursor;
            }
        }, dbExecutor).continueWithTask(new Continuation<Cursor, Task<Cursor>>() {
            @Override
            public Task<Cursor> then(Task<Cursor> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public Cursor rawQuerySync(final String sql, final String[] args) {
        return db.rawQuery(sql, args);
    }

    /**
     * Executes a SQL.
     *
     * @see SQLiteDatabase#execSQL(String)
     */
    public Task<Void> execSQLAsync(Task<Void> task, final String sql, final String[] args) {
        return task.onSuccess(new Continuation<Void, Void>() {
            @Override
            public Void then(Task<Void> task) throws Exception {
                if (null != args) {
                    db.execSQL(sql, args);
                } else {
                    db.execSQL(sql);
                }
                return null;
            }
        }, dbExecutor).continueWithTask(new Continuation<Void, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Void> task) throws Exception {
                // We want to jump off the dbExecutor
                return task;
            }
        }, bgExecutor);
    }

    public void execSQLSync(final String sql, final String[] args) {
        if (null != args) {
            db.execSQL(sql, args);
        } else {
            db.execSQL(sql);
        }
    }

    public static void clearQueue() {
        ThreadPoolExecutor bgPool = (ThreadPoolExecutor) bgExecutor;
        bgPool.getQueue().clear();

        ThreadPoolExecutor dbPool = (ThreadPoolExecutor) dbExecutor;
        dbPool.getQueue().clear();
    }
}
