package cn.jpush.im.android.tasks;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;

public class GetUploadTokenTask extends AbstractTask {

    private Map<String, Object> params = new HashMap<String, Object>();

    private static final String UPTOKEN_PREFIX = "/syncUploadToken";

    private static final String KEY_FILE_TYPE = "file_type";

    private static final String KEY_RESOUCE_ID = "resource_id";

    private static final String KEY_FILE_BLOCKS = "file_blocks";

    private static final String KEY_FILE_HASH = "file_hash";

    private static final String KEY_FILE_SIZE = "file_size";

    private GetTokenCallback callback;

    public GetUploadTokenTask(ContentType type, String resourceID, int blockNum, String md5,
                              long fileSize, GetTokenCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        params.put(KEY_FILE_TYPE, type);
        params.put(KEY_RESOUCE_ID, resourceID);
        params.put(KEY_FILE_BLOCKS, blockNum);
        params.put(KEY_FILE_HASH, md5);
        params.put(KEY_FILE_SIZE, fileSize);
        this.callback = callback;
    }

    private String createUploadTokenUrl() {
        String url = JMessage.httpUserCenterPrefix + UPTOKEN_PREFIX;
        url += "?" + KEY_FILE_TYPE + "=" + params.get(KEY_FILE_TYPE);
        url += "&" + KEY_RESOUCE_ID + "=" + params.get(KEY_RESOUCE_ID);
        url += "&" + KEY_FILE_BLOCKS + "=" + params.get(KEY_FILE_BLOCKS);
        url += "&" + KEY_FILE_HASH + "=" + params.get(KEY_FILE_HASH);
        url += "&" + KEY_FILE_SIZE + "=" + params.get(KEY_FILE_SIZE);
        return url;
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }


        String url = createUploadTokenUrl();
        String authBase = StringUtils
                .getBasicAuthorization(String.valueOf(IMConfigs.getUserID()), IMConfigs.getToken());
        try {
            response = mHttpClient.sendGet(url, authBase);
        } catch (APIRequestException e) {
            response = e.getResponseWrapper();
        } catch (APIConnectionException e) {
            response = null;
        }
        return response;
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        String provider;
        String token = null;
        String policy = null;
        String signature = null;
        Map<String, String> resultMap = JsonUtil.formatToMap(resultContent);
        provider = resultMap.get("provider");
        if (provider.equals("qiniu")) {
            token = resultMap.get("token");
        } else if (provider.equals("upyun")) {
            policy = resultMap.get("policy");
            signature = resultMap.get("signature");
        }
        callback.gotResult(ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, provider, token, policy, signature);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        callback.gotResult(responseCode, responseMsg, null, null, null, null);
    }

    public static abstract class GetTokenCallback extends BasicCallback {

        protected GetTokenCallback(boolean isRunInUIThread) {
            super(isRunInUIThread);
        }

        protected GetTokenCallback() {
        }

        @Override
        public void gotResult(int i, String s) {

        }

        public abstract void gotResult(int statusCode, String msg, String provider, String token,
                                       String policy, String signature);
    }
}
