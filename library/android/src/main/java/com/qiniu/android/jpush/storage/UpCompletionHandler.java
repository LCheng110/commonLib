package com.qiniu.android.jpush.storage;

import com.qiniu.android.jpush.http.ResponseInfo;

import org.json.JSONObject;

public interface UpCompletionHandler {
    void complete(String key, ResponseInfo info, JSONObject response);
}
