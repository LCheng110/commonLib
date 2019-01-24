package com.upyun.jpush.api.listener;

public interface ProgressListener {
    void transferred(long transferedBytes, long totalBytes);
}
