package cn.jpush.im.android.common.resp;


import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

public class ResponseWrapper {

    private static final String TAG = "ResponseWrapper";

    private static final int RESPONSE_CODE_NONE = -1;

    public int responseCode = RESPONSE_CODE_NONE;

    public String responseContent;

    public byte[] rawData;

    public ErrorObject error;     // error for non-200 response, used by new API

    public int rateLimitQuota;

    public int rateLimitRemaining;

    public int rateLimitReset;

    public void setRateLimit(String quota, String remaining, String reset) {
        if (null == quota) {
            return;
        }

        try {
            rateLimitQuota = Integer.parseInt(quota);
            rateLimitRemaining = Integer.parseInt(remaining);
            rateLimitReset = Integer.parseInt(reset);

            Logger.d(TAG,
                    "JPush API Rate Limiting params - quota:" + quota + ", remaining:" + remaining
                            + ", reset:" + reset);
        } catch (NumberFormatException e) {
            Logger.d(TAG, "Unexpected - parse rate limiting headers error.");
        }
    }

    public void setErrorObject() {
        error = JsonUtil.fromJson(responseContent, ErrorObject.class);
    }

    public boolean isServerResponse() {
        if (responseCode == 200) {
            return true;
        }
        if (responseCode > 0 && null != error && error.error.code > 0) {
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return JsonUtil.toJson(this);
    }

    public class ErrorObject {

        public long msg_id;

        public ErrorEntity error;
    }

    public class ErrorEntity {

        public int code;

        public String message;

        @Override
        public String toString() {
            return JsonUtil.toJson(this);
        }
    }

}
