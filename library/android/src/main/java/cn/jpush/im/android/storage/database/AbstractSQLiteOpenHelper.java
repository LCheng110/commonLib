/*
 * Copyright (c) 2015-present, Parse, LLC.
 * All rights reserved.
 *
 * This source code is licensed under the BSD-style license found in the
 * LICENSE file in the root directory of this source tree. An additional grant
 * of patent rights can be found in the PATENTS file in the same directory.
 */
package cn.jpush.im.android.storage.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import cn.jpush.im.android.bolts.Task;


/**
 * package
 */
abstract class AbstractSQLiteOpenHelper {

    private final SQLiteOpenHelper helper;

    public AbstractSQLiteOpenHelper(Context context, String name, SQLiteDatabase.CursorFactory factory,
                                    int version) {
        helper = new SQLiteOpenHelper(context, name, factory, version) {
            @Override
            public void onOpen(SQLiteDatabase db) {
                super.onOpen(db);
                AbstractSQLiteOpenHelper.this.onOpen(db);
            }

            @Override
            public void onCreate(SQLiteDatabase db) {
                AbstractSQLiteOpenHelper.this.onCreate(db);
            }

            @Override
            public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                AbstractSQLiteOpenHelper.this.onUpgrade(db, oldVersion, newVersion);
            }

            @Override
            public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
                AbstractSQLiteOpenHelper.this.onDowngrade(db, oldVersion, newVersion);
            }
        };
    }

    protected Task<JMSQLiteDatabase> getReadableDatabaseAsync() {
        return getDatabaseAsync(false);
    }

    protected Task<JMSQLiteDatabase> getWritableDatabaseAsync() {
        return getDatabaseAsync(true);
    }

    protected JMSQLiteDatabase getReadableDatabaseSync() {
        return getDatabaseSync(false);
    }

    protected JMSQLiteDatabase getWritableDatabaseSync() {
        return getDatabaseSync(true);
    }

    private Task<JMSQLiteDatabase> getDatabaseAsync(final boolean writable) {
        return JMSQLiteDatabase.openDatabaseAsync(Task.<Void>forResult(null),
                helper, !writable ? SQLiteDatabase.OPEN_READONLY : SQLiteDatabase.OPEN_READWRITE);
    }

    private JMSQLiteDatabase getDatabaseSync(final boolean writable) {
        return JMSQLiteDatabase.openDatabaseSync(helper, !writable ? SQLiteDatabase.OPEN_READONLY : SQLiteDatabase.OPEN_READWRITE);
    }

    public void onOpen(SQLiteDatabase db) {
        // do nothing
    }

    public abstract void onCreate(SQLiteDatabase db);

    public abstract void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion);

    public abstract void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion);
}
