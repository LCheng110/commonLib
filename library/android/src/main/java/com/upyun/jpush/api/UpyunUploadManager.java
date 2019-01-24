package com.upyun.jpush.api;

import com.upyun.jpush.api.listener.CompleteListener;
import com.upyun.jpush.api.listener.ProgressListener;

import java.io.File;

import cn.jpush.im.android.Consts;
import cn.jpush.im.android.api.enums.ContentType;

public class UpyunUploadManager {

    public static final int CONNECT_TIMEOUT = 10 * 1000;
    public static final int RESPONSE_TIMEOUT = 30 * 1000;

    private static final long MIN_FILE_LENGTH = 100 * 1024; //100kb

    public void upload(String policy, String signature, ContentType type,
            String filePath, ProgressListener progressListener,
            CompleteListener completeListener) throws Exception {

        File file = new File(filePath);
        String bucket;
        switch (type) {
            case voice:
                bucket = Consts.UPYUN_VOICE_BUCKET;
                break;
            case image:
                bucket = Consts.UPYUN_IMAGE_BUCKET;
                break;
            default:
                bucket = Consts.UPYUN_FILE_BUCKET;
        }
        if (file.length() < MIN_FILE_LENGTH) {
            UpYunFormUploader formUploader = new UpYunFormUploader();
            formUploader.upload(policy, signature, bucket, filePath,
                    progressListener, completeListener);
        } else {
            UpYunMultipartUploader multipartUploader = new UpYunMultipartUploader(bucket);
            multipartUploader.upload(policy, signature, file, progressListener,
                    completeListener);
        }
    }
}
