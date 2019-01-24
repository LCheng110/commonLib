package cn.jpush.im.android.utils.filemng;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.api.content.MediaContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public class FileUploader {

    private static Map<String, Double> progressCache = new HashMap<String, Double>();

    public FileUploader() {
    }

    public void doUploadMsgAttachFile(InternalMessage msg, BasicCallback usersCompletionCallback) {
        IUploadManager uploadManager;
        if (BuildConfig.WITH_FASTDFS) {
            uploadManager = new PrivateCloudUploadManager();
        } else {
            uploadManager = new PublicCloudUploadManager();
        }
        MediaContent mediaContent = (MediaContent) msg.getContent();
        File file = mediaContent.getLocalPath() == null ? null : new File(mediaContent.getLocalPath());
        uploadManager.prepareToUpload(file, mediaContent.getFormat(), mediaContent.getContentType(), msg, usersCompletionCallback);
    }

    public void doUploadAvatar(File avatar, String format, UploadAvatarCallback usersCompletionCallback) {
        IUploadManager uploadManager;
        if (BuildConfig.WITH_FASTDFS) {
            uploadManager = new PrivateCloudUploadManager();
        } else {
            uploadManager = new PublicCloudUploadManager();
        }
        uploadManager.prepareToUpload(avatar, format, ContentType.image, null, usersCompletionCallback);
    }

    public static Double getProgressInCache(String targetId, String appkey, int msgId) {
        return progressCache.get(targetId + appkey + msgId);
    }

    public static Double updateProgressInCache(String targetId, String appkey, int msgId, double progress) {
        return progressCache.put(targetId + appkey + msgId, progress);
    }

    public static void removeProgressInCache(String targetId, String appkey, int msgId) {
        progressCache.remove(targetId + appkey + msgId);
    }

    public static abstract class UploadAvatarCallback extends BasicCallback {

        private final static String TAG = "UploadAvatarCallback";

        protected UploadAvatarCallback() {
        }

        protected UploadAvatarCallback(boolean isRunInUIThread) {
            super(isRunInUIThread);
        }

        @Override
        public void gotResult(int responseCode, String s) {
            Logger.e(TAG, "Should not reach here! ");
        }

        public abstract void gotResult(int responseCode, String responseMsg, String mediaID);

        @Override
        public void gotResult(int responseCode, String responseMessage, Object... result) {
            String mediaID = null;
            if (null != result && result.length > 0 && null != result[0]) {
                mediaID = (String) result[0];
            }
            gotResult(responseCode, responseMessage, mediaID);
        }
    }

}
