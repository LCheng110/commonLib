package com.qiniu.android.jpush.storage;

public interface Recorder {
    void set(String key, byte[] data);

    byte[] get(String key);

    void del(String key);
}
