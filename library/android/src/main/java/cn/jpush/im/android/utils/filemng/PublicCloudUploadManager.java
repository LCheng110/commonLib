package cn.jpush.im.android.utils.filemng;

import com.upyun.jpush.api.UpYunMultipartUploader;
import com.upyun.jpush.api.utils.UpYunUtils;

import java.io.File;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.tasks.GetUploadTokenTask;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by xiongtc on 2017/3/23.
 */

class PublicCloudUploadManager extends UploadManagerImpl {
    private static final String TAG = "PublicCloudUploadManager";

    static final int UPLOAD_ERROR_QINIU_DUPLICATE_RES_ID = 614;
    static final int UPLOAD_ERROR_QINIU_AUTH_FAILED = 401;
    static final String PROVIDER_QINIU = "qiniu";
    static final String PROVIDER_UNYUN = "upyun";
    private static final int MAX_UPLOAD_RETRY_TIME = 3;

    @Override
    public boolean prepareToUpload(File file, String format, ContentType type, InternalMessage msg, BasicCallback usersCompletionCallback) {
        if (super.prepareToUpload(file, format, type, msg, usersCompletionCallback)) {
            getTokenThenUpload();
            return true;
        }
        return false;
    }

    void getTokenThenUpload() {
        long fileSize = file.length();
        int blockNum = 1;
        try {
            blockNum = UpYunUtils.getBlockNum(file, UpYunMultipartUploader.BLOCK_SIZE);
        } catch (Exception e) {
            e.printStackTrace();
        }
        String md5 = StringUtils.toMD5(FileUtil.File2byte(file.getAbsolutePath()));
        if (null == md5) {
            Logger.ee(TAG, "md5 generate failed . return from upload file.");
            doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR,
                    ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
            return;
        }
        new GetUploadTokenTask(contentType, resourceID, blockNum, md5.toLowerCase(),
                fileSize, getTokenCallback, false).execute();
    }

    private GetUploadTokenTask.GetTokenCallback getTokenCallback = new GetUploadTokenTask.GetTokenCallback(false) {
        int uploadRetryTime = 0;

        @Override
        public void gotResult(int statusCode, String msg, String provider, String token,
                              String policy, String signature) {
            if (statusCode == 0) {
                // get token success,upload this file again
                if (uploadRetryTime < MAX_UPLOAD_RETRY_TIME) {
                    uploadRetryTime++;
                    threadSleep(uploadRetryTime);
                    IPublicCloudUploader uploader;
                    if (provider.equals(PROVIDER_QINIU)) {
                        Logger.d(TAG, "do upload to qiniu again! token = " + token);
                        uploader = new QiniuUploader(PublicCloudUploadManager.this);
                    } else if (provider.equals(PROVIDER_UNYUN)) {
                        Logger.d(TAG, "do upload to upyun ! policy = " + policy + " signature = "
                                + signature);
                        uploader = new UpyunUploader(PublicCloudUploadManager.this);
                    } else {
                        Logger.ww(TAG, "unsupported provider! provider = " + provider);
                        doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR,
                                ErrorCode.OTHERS_ERROR.OTHERS_UPLOAD_ERROR_DESC, null);
                        return;
                    }
                    uploader.doUpload(token, policy, signature);
                } else {
                    Logger.ww(TAG, "authentication error ! the generated token cannot pass the authentication");
                    doCompleteCallbackToUser(false, ErrorCode.OTHERS_ERROR.OTHERS_AUTH_ERROR,
                            ErrorCode.OTHERS_ERROR.OTHERS_AUTH_ERROR_DESC, null);
                }
            } else {
                doCompleteCallbackToUser(false, statusCode, msg, null);
            }
        }
    };


    private void threadSleep(int currentRetryTime) {
        try {
            Thread.sleep((long) (Math.pow(2, currentRetryTime) * 1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
