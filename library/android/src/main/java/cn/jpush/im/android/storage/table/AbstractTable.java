package cn.jpush.im.android.storage.table;

public interface AbstractTable {
    String TAG = "AbstractTable";

    String COMMON_CREATE_TABLE_HEADER = "create table if not exists ";

    String COMMON_CREATE_INDEX_HEADER = "create index if not exists ";
}
