package cn.jpush.im.android.utils.filemng;

import com.qiniu.android.jpush.http.ResponseInfo;
import com.qiniu.android.jpush.storage.QiniuUploadManager;
import com.qiniu.android.jpush.storage.UpCompletionHandler;
import com.qiniu.android.jpush.storage.UploadOptions;

import org.json.JSONObject;

import java.io.File;

import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

class QiniuUploader implements IPublicCloudUploader {
    private static final String TAG = "QiniuUploader";
    private static final String FORMAT_MP3 = ".mp3";

    private PublicCloudUploadManager manager;
    private String mediaID;

    QiniuUploader(PublicCloudUploadManager manager) {
        this.manager = manager;
    }

    @Override
    public void doUpload(String token, String policy, String signature) {
        QiniuUploadManager um = new QiniuUploadManager();
        mediaID = PublicCloudUploadManager.PROVIDER_QINIU + File.separator + manager.contentType + File.separator +
                Consts.PLATFORM_ANDROID + File.separator + manager.resourceID;
        if (manager.fileFromMsg) {
            manager.mediaContent.setMediaID(mediaID);
        }
        UploadOptions options = new UploadOptions(null, null, false, qiniuProgressCallback, null);
        if (manager.contentType == ContentType.voice) {
            //这里在mediaID后面加上".mp3"后缀的原因见wiki:http://wiki.jpushoa.com/pages/viewpage.action?pageId=9562688
            um.put(manager.file.getAbsolutePath(), mediaID + FORMAT_MP3, token, qiniuCompletionHandler, options);
        } else {
            um.put(manager.file.getAbsolutePath(), mediaID, token, qiniuCompletionHandler, options);
        }
    }

    private ProgressUpdateCallback qiniuProgressCallback = new ProgressUpdateCallback(false) {
        double prePercent;

        @Override
        public void onProgressUpdate(double percent) {
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
    };

    private UpCompletionHandler qiniuCompletionHandler = new UpCompletionHandler() {
        @Override
        public void complete(String key, ResponseInfo info, JSONObject response) {
            if (null != info && null != info.error && (info.error.equals("no token") || info.statusCode == PublicCloudUploadManager.UPLOAD_ERROR_QINIU_AUTH_FAILED)) {
                // no token or server returns 401,we should update token
                Logger.d(TAG, "no token or token error, do get token");
                manager.getTokenThenUpload();
            } else if (null != info && info.statusCode >= 200 && info.statusCode < 300 && null == info.error) {
                Logger.d(TAG, "upload success!");
                Logger.d(TAG, "qiniu info = " + info.xlog + " response = " + response);
                manager.doCompleteCallbackToUser(true, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, mediaID);
                if (manager.fileFromMsg) {
                    CommonUtils.doProgressCallbackToUser(manager.message.getTargetID(), manager.message.getTargetAppKey(), manager.message.getId(), 1.0);
                }
            } else if (info != null && info.statusCode == PublicCloudUploadManager.UPLOAD_ERROR_QINIU_DUPLICATE_RES_ID) {
                //当上传失败时,重新生成resourceID，然后自动重试上传
                String newResourceID = StringUtils.createResourceID(manager.fileFormat);
                manager.resourceID = newResourceID;
                if (manager.fileFromMsg) {
                    //如果上传的是消息中的附加文件，还需要更新manager中mediaContent中的mediaID，注意这里的mediaID应该是不带provider的，因为provider是重新获取token之后根据后台的返回才能确定
                    String mediaIDWithoutProvider = File.separator + manager.mediaContent.getContentType() + File.separator +
                            Consts.PLATFORM_ANDROID + File.separator + newResourceID;
                    manager.mediaContent.setMediaID(mediaIDWithoutProvider);
                    manager.updateMessageContent();
                }
                manager.getTokenThenUpload();
            } else {
                manager.doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR,
                        ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
            }
        }
    };
}
