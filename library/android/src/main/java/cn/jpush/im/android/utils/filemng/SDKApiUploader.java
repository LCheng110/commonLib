package cn.jpush.im.android.utils.filemng;

import android.os.Looper;
import android.util.Log;

import com.loopj.android.jpush.http.AsyncHttpClient;
import com.loopj.android.jpush.http.AsyncHttpResponseHandler;
import com.qiniu.android.jpush.common.Config;
import com.qiniu.android.jpush.http.CompletionHandler;
import com.qiniu.android.jpush.http.ProgressHandler;
import com.qiniu.android.jpush.http.ResponseInfo;

import org.apache.http.Header;
import org.apache.http.message.BasicHeader;
import org.apache.jpush.http.entity.mime.MultipartEntity;
import org.apache.jpush.http.entity.mime.content.ByteArrayBody;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.common.connection.NativeHttpClient;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

/**
 * Created by hxhg on 2017/10/17.
 */

public class SDKApiUploader implements IPrivateCloudUploader {
    private static final String TAG = SDKApiUploader.class.getSimpleName();

    private PrivateCloudUploadManager manager;
    private static AsyncHttpClient client = new AsyncHttpClient();

    private Header[] headers = new Header[]{
            new BasicHeader("Authorization", StringUtils.getBasicAuthorization(IMConfigs.getUserName(), IMConfigs.getToken())),
            new BasicHeader("User-Agent", NativeHttpClient.JPUSH_USER_AGENT),
            new BasicHeader("Connection", "Keep-Alive"),
            new BasicHeader("Accept-Charset", NativeHttpClient.CHARSET),
            new BasicHeader("Charset", NativeHttpClient.CHARSET),
            new BasicHeader("X-App-Key", JCoreInterface.getAppKey()),
            new BasicHeader("jm-channel", "m")};

    SDKApiUploader(PrivateCloudUploadManager manager) {
        this.manager = manager;
    }

    private String createUrl() {
        String type = "";
        switch (manager.contentType) {
            case image:
                type = manager.contentType.name();
                break;
            case voice:
            case file:
                type = ContentType.file.name();//语音和文件都填file。

        }
        return JMessage.httpUserCenterPrefix + "/privatecloud/resource?type=" + type;
    }

    @Override
    public void doUpload() {
        postDataSync(createUrl(), headers, new ProgressHandler() {
            double prePercent;

            @Override
            public void onProgress(int bytesWritten, int totalSize) {
                double percent = (double) (bytesWritten) / totalSize;
                if ((prePercent < percent) && (percent != 1.0)) {
                    prePercent = percent;
                    if (manager.fileFromMsg) {
                        String targetID = manager.message.getTargetID();
                        int msgID = manager.message.getId();
                        String targetAppkey = manager.message.getTargetAppKey();
                        FileUploader.updateProgressInCache(targetID, targetAppkey, msgID, percent);
                        CommonUtils.doProgressCallbackToUser(targetID, targetAppkey, msgID, prePercent);
                    }
                }
            }
        }, new CompletionHandler() {
            @Override
            public void complete(ResponseInfo info, JSONObject response) {
                Logger.d(TAG, "upload by sdk api complete. info = " + info + " response = " + response);
                if (null != info && info.statusCode >= 200 && info.statusCode < 300 && null == info.error) {
                    Logger.d(TAG, "upload by sdk api success! info = " + info.xlog + " response = " + response);
                    try {
                        String mediaID = response.getString("media_id");
                        Log.d(TAG, "got mediaID from response. mediaID = " + mediaID);
                        if (manager.fileFromMsg) {
                            manager.mediaContent.setMediaID(mediaID);
                        }
                        manager.doCompleteCallbackToUser(true, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, mediaID);
                        if (manager.fileFromMsg) {
                            CommonUtils.doProgressCallbackToUser(manager.message.getTargetID(), manager.message.getTargetAppKey(), manager.message.getId(), 1.0);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    manager.doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR,
                            ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
                }
            }
        });
    }

    private void postDataSync(String url, Header[] headers, ProgressHandler progressHandler, CompletionHandler completionHandler) {
        AsyncHttpResponseHandler handler = new ResponseHandler(completionHandler, progressHandler);
        MultipartEntity entity = new MultipartEntity();
        entity.addPart(manager.file.getName(), new ByteArrayBody(FileUtil.File2byte(manager.file.getAbsolutePath()), manager.file.getName()));
        Log.d(TAG, "file uploader .post url = " + url);
        client.post(null, url, headers, entity, null, handler);
    }

    private class ResponseHandler extends AsyncHttpResponseHandler {
        private static final String TAG = "ResponseHandler";

        private ProgressHandler progressHandler;
        private CompletionHandler completionHandler;

        ResponseHandler(CompletionHandler completionHandler, ProgressHandler progressHandler) {
            super(Looper.getMainLooper());
            this.completionHandler = completionHandler;
            this.progressHandler = progressHandler;
        }

        private ResponseInfo buildResponseInfo(int statusCode, Header[] headers, byte[] responseBody,
                                               Throwable error) {
            String err = null;
            if (statusCode != 200) {
                if (responseBody != null) {
                    try {
                        err = new String(responseBody, Config.CHARSET);
                        JSONObject obj = new JSONObject(err);
                        err = obj.optString("error", err);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                } else {
                    if (error != null) {
                        err = error.getMessage();
                        if (err == null) {
                            err = error.toString();
                        }
                    }
                }
            }
            if (statusCode == 0) {
                statusCode = ResponseInfo.NetworkError;
            }

            return new ResponseInfo(statusCode, null, null, err);
        }

        private JSONObject buildJsonResp(byte[] body) throws Exception {

            String str = new String(body, Config.CHARSET);
            return new JSONObject(str);
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            JSONObject obj = null;
            Exception exception = null;
            try {
                obj = buildJsonResp(responseBody);
            } catch (Exception e) {
                exception = e;
            }

            ResponseInfo info = buildResponseInfo(statusCode, headers, null, exception);
            Logger.dd(TAG, "upload success ! info - " + info.toString());

            completionHandler.complete(info, obj);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
            ResponseInfo info = buildResponseInfo(statusCode, headers, responseBody, error);
            Logger.ww(TAG, "upload failed ! info - " + info.toString());
            completionHandler.complete(info, null);
        }

        @Override
        public void onProgress(int bytesWritten, int totalSize) {
            if (progressHandler != null) {
                progressHandler.onProgress(bytesWritten, totalSize);
            }
        }

        @Override
        public void onStart() {
            super.onStart();
        }
    }

}