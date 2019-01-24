package com.qiniu.android.jpush.http;

/**
 * Created by bailong on 14/10/9.
 */
public interface ProgressHandler {
    void onProgress(int bytesWritten, int totalSize);
}
