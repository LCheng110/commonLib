package cn.jpush.im.android.utils.filemng;

/**
 * Created by xiongtc on 2017/3/24.
 */

public interface IPublicCloudUploader {
    void doUpload(String token, String policy, String signature);
}
