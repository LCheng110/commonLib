package cn.jpush.im.android.utils.filemng;

import java.io.File;
import java.util.concurrent.Callable;

import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.api.BasicCallback;


public class PrivateCloudUploadManager extends UploadManagerImpl {
    private static final String TAG = "PrivateCloudUploadManager";
    static final String PROVIDER_FASTDFS = "fastdfs";

    public static final String THUMB_HDPI = "thumb_l";
    public static final String THUMB_XHDPI = "thumb_m";
    public static final String THUMB_XXHDPI = "thumb_h";
    public static final float DENSITY_HDPI = 1.5f;  //480X800
    public static final float DENSITY_XHDPI = 2.0f; //720X1280
    public static final float DENSITY_XXHDPI = 3.0f;    //1080X1920

    @Override
    public boolean prepareToUpload(final File file, final String format, final ContentType type, final InternalMessage msg, BasicCallback usersCompletionCallback) {
        if (super.prepareToUpload(file, format, type, msg, usersCompletionCallback)) {
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    IPrivateCloudUploader uploader;
                    if (BuildConfig.IS_HAIXIN) {
                        //海鑫渠道，文件上传直接走sdk api
                        uploader = new SDKApiUploader(PrivateCloudUploadManager.this);
                    } else {
                        uploader = new FastDfSUploader(PrivateCloudUploadManager.this);
                    }
                    uploader.doUpload();
                    return null;
                }
            });
            return true;
        }
        return false;
    }


}
