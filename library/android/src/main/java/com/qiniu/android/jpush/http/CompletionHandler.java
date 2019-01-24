package com.qiniu.android.jpush.http;

import org.json.JSONObject;

/**
 * Created by bailong on 14/10/9.
 */
public interface CompletionHandler {
    void complete(ResponseInfo info, JSONObject response);
}
