package com.qiniu.android.jpush.storage;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.callback.ProgressUpdateCallback;

public final class UploadOptions {
    Map<String, String> params;
    String mimeType;
    boolean checkCrc;
    ProgressUpdateCallback progressHandler;
    UpCancellationSignal cancellationSignal;

    public UploadOptions(Map<String, String> params, String mimeType, boolean checkCrc,
    		ProgressUpdateCallback progressHandler, UpCancellationSignal cancellationSignal) {
        this.params = filterParam(params);
        this.mimeType = mimeType;
        this.checkCrc = checkCrc;
        this.progressHandler = progressHandler;
        this.cancellationSignal = cancellationSignal;
    }

    private static Map<String, String> filterParam(Map<String, String> params) {
        Map<String, String> ret = new HashMap<String, String>();
        if (params == null) {
            return ret;
        }

        for (Map.Entry<String, String> i : params.entrySet()) {
            if (i.getKey().startsWith("x:")) {
                ret.put(i.getKey(), i.getValue());
            }
        }
        return ret;
    }
}
