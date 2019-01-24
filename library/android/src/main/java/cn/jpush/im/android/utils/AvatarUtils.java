package cn.jpush.im.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.BitmapCacheHelper;
import cn.jpush.im.android.utils.filemng.AvatarDownloader;

/**
 * Created by hxhg on 2017/9/7.
 */

public class AvatarUtils {

    private static final String TAG = AvatarUtils.class.getSimpleName();


    public static File getAvatarFile(String avatarMediaID) {
        File smallAvatarFile = null;
        String smallAvatarFilePath;
        if (ExpressionValidateUtil.validMediaID(avatarMediaID)) {
            smallAvatarFilePath = FileUtil.getAvatarFilePath(avatarMediaID);
            smallAvatarFile = new File(smallAvatarFilePath);
            if (!smallAvatarFile.exists()) {
                //若小头像不存在，则取大头像来生成小头像。
                File bigAvatar = getBigAvatarFile(avatarMediaID);
                if (null != bigAvatar) {
                    Bitmap thumbAvatar = BitmapUtils.createThumbnail(bigAvatar.getAbsolutePath(), null, BitmapUtils.SMALL_AVATAR_EDGE);
                    smallAvatarFile = FileUtil.saveBitmapToFile(thumbAvatar, FileUtil.getAvatarFilePath(avatarMediaID));
                }
            }
        }
        return smallAvatarFile;
    }

    public static void getAvatarFileAsync(final String avatarMediaID, final DownloadAvatarCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getSmallAvatarAsync",
                callback)) {
            return;
        }
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                File smallAvatarFile = getAvatarFile(avatarMediaID);
                if (null == smallAvatarFile || !smallAvatarFile.exists()) {
                    Logger.d(TAG, "small avatar file is null, try to download");
                    if (!IMConfigs.getNetworkConnected()) {
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                        return null;
                    }
                    new AvatarDownloader().downloadSmallAvatar(avatarMediaID, callback);
                } else {
                    CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, smallAvatarFile);
                }
                return null;
            }
        });
    }

    public static void getAvatarBitmap(final String avatarMediaID, final GetAvatarBitmapCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getSmallAvatarBitmapAsync",
                callback)) {
            return;
        }

        //如果缓存中有头像，直接返回。
        Bitmap smallAvatarBitmap = BitmapCacheHelper.getInstance().getBitmapFromMemCache(avatarMediaID);
        if (null == smallAvatarBitmap) {
            getAvatarFileAsync(avatarMediaID, new DownloadAvatarCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, File avatar) {
                    Bitmap smallAvatar = null;
                    if (null != avatar) {
                        smallAvatar = BitmapFactory.decodeFile(avatar.getAbsolutePath());
                        BitmapCacheHelper.getInstance().addBitmapToCache(avatarMediaID, smallAvatar);
                    }
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage, smallAvatar);
                }
            });
        } else {
            //这里直接在caller线程执行回调，防止线程切换导致的效率降低
            CommonUtils.doCompleteCallBackToUser(true, callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, smallAvatarBitmap);
        }
    }

    public static void getBigAvatarBitmap(final String avatarMediaID, final GetAvatarBitmapCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getBigAvatarBitmap",
                callback)) {
            return;
        }
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                Bitmap bigAvatarBitmap = getBigAvatarBitmap(avatarMediaID);
                if (null == bigAvatarBitmap) {
                    Logger.d(TAG, "big avatar bitmap is null, try to download");
                    if (!IMConfigs.getNetworkConnected()) {
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                        return null;
                    }

                    getBigAvatarFileAsync(avatarMediaID, new DownloadAvatarCallback() {
                        @Override
                        public void gotResult(int responseCode, String responseMessage, File avatar) {
                            Bitmap bigAvatar = null;
                            if (null != avatar) {
                                bigAvatar = BitmapFactory.decodeFile(avatar.getAbsolutePath());
                            }
                            CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage, bigAvatar);
                        }
                    });
                } else {
                    CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, bigAvatarBitmap);
                }
                return null;
            }
        });
    }

    private static Bitmap getBigAvatarBitmap(String avatarMediaID) {
        Bitmap bigAvatar = null;
        File bigAvatarFile = getBigAvatarFile(avatarMediaID);
        if (null != bigAvatarFile) {
            bigAvatar = BitmapFactory.decodeFile(bigAvatarFile.getAbsolutePath());
        }
        return bigAvatar;
    }

    public static File getBigAvatarFile(String avatarMediaID) {
        String avatarFilePath;
        if (ExpressionValidateUtil.validMediaID(avatarMediaID)) {
            avatarFilePath = FileUtil.getBigAvatarFilePath(avatarMediaID);
        } else {
            //旧版中数据库里存的是图片本地路径
            avatarFilePath = avatarMediaID;
        }
        Logger.d(TAG, "[getBigAvatarFile] avatarFilePath = " + avatarFilePath);
        if (!TextUtils.isEmpty(avatarFilePath)) {
            File avatarFile = new File(avatarFilePath);
            return avatarFile.exists() ? avatarFile : null;
        }
        return null;
    }

    public static void getBigAvatarFileAsync(final String avatarMediaID, final DownloadAvatarCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getAvatarFileAsync", callback)) {
            return;
        }
        File file = getBigAvatarFile(avatarMediaID);
        if (null == file || !file.exists()) {
            if (!IMConfigs.getNetworkConnected()) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                return;
            }
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    new AvatarDownloader().downloadBigAvatar(avatarMediaID, callback);
                    return null;
                }
            });
        } else {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
        }

    }
}
