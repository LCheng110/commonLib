package cn.jpush.im.android.utils.filemng;

import com.upyun.jpush.api.UpyunUploadManager;
import com.upyun.jpush.api.listener.CompleteListener;
import com.upyun.jpush.api.listener.ProgressListener;

import java.io.File;

import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;


class UpyunUploader implements IPublicCloudUploader {
    private static final String TAG = "UpyunUploader";

    private PublicCloudUploadManager manager;
    private String mediaID;

    UpyunUploader(PublicCloudUploadManager manager) {
        this.manager = manager;
    }

    @Override
    public void doUpload(String token, String policy, String signature) {
        UpyunUploadManager upyunManager = new UpyunUploadManager();

        mediaID = PublicCloudUploadManager.PROVIDER_QINIU + File.separator + manager.contentType + File.separator +
                Consts.PLATFORM_ANDROID + File.separator + manager.resourceID;
        if (manager.fileFromMsg) {
            manager.mediaContent.setMediaID(mediaID);
        }
        try {
            upyunManager.upload(policy, signature, manager.contentType, manager.file.getAbsolutePath(), upyunProgressListener,
                    upyunCompleteListener);
        } catch (Exception e) {
            manager.doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR,
                    ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
            e.printStackTrace();
        }
    }

    private ProgressListener upyunProgressListener = new ProgressListener() {
        double prePercent;

        @Override
        public void transferred(long transferedBytes, long totalBytes) {
            double percent = transferedBytes / (totalBytes * 1.0);
            if ((prePercent < percent) && (percent != 1.0)) {
                prePercent = percent;
                if (manager.fileFromMsg) {
                    String targetID = manager.message.getTargetID();
                    int msgID = manager.message.getId();
                    String targetAppkey = manager.message.getTargetAppKey();
                    FileUploader.updateProgressInCache(targetID, targetAppkey, msgID, percent);
                    CommonUtils.doProgressCallbackToUser(targetID, targetAppkey, msgID, percent);
                }
            }
        }
    };

    private CompleteListener upyunCompleteListener = new CompleteListener() {

        @Override
        public void result(boolean isComplete, int statusCode, String reason) {
            if (isComplete) {
                if (manager.fileFromMsg) {
                    CommonUtils.doProgressCallbackToUser(manager.message.getTargetID(), manager.message.getTargetAppKey(), manager.message.getId(), 1.0);
                }
                manager.doCompleteCallbackToUser(true, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, mediaID);
            } else {
                if (statusCode == 401 || statusCode == 403) {// 401和403分别是upyun分块和表单上传的验证失败返回码
                    Logger.d(TAG, "upyun no token or token error, do get token statusCode = "
                            + statusCode);
                    manager.getTokenThenUpload();
                } else {
                    manager.doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR,
                            ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
                }
            }
        }
    };
}
