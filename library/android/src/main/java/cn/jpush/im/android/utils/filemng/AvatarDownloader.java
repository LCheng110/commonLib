package cn.jpush.im.android.utils.filemng;

import com.loopj.android.jpush.http.DataAsyncHttpResponseHandler;

import org.apache.http.Header;

import java.io.File;
import java.util.Vector;
import java.util.concurrent.Callable;

import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.utils.BitmapUtils;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class AvatarDownloader extends FileDownloader {
    private static final String TAG = "AvatarDownloader";

    private String downloadAvatarUrl = null;
    private DownloadAvatarCallback mCallback;

    private final static Vector<String> sMediaIdCache = new Vector<String>();

    public void downloadBigAvatar(String mediaID, DownloadAvatarCallback callback) {
        downloadFile(mediaID, callback, false, 0, 0);
    }

    public void downloadSmallAvatar(String mediaID, DownloadAvatarCallback callback) {
        int avatarEdge = (int) (BitmapUtils.mDisplayMetrics.density * BitmapUtils.SMALL_AVATAR_EDGE);
        downloadFile(mediaID, callback, true, avatarEdge, avatarEdge);
    }

    private void downloadFile(final String mediaID, DownloadAvatarCallback callback, final boolean isSmallAvatar, int width,
                              int height) {
        mCallback = callback;
        String provider = StringUtils.getProviderFromMediaID(mediaID);
        if (null != mediaID && null != provider) {

            if (!needDownload(mediaID, isSmallAvatar)) {
                Logger.d(TAG, "the avatar file is already exists,no need to download again! mediaID = " + mediaID);
                return;
            }

            try {
                if (duplicateDownloadCheck(mediaID, isSmallAvatar)) {
                    Logger.d(TAG, "duplicateDownloadCheck returns true! return from download file.");
                    return;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            if (provider.equalsIgnoreCase(PublicCloudUploadManager.PROVIDER_QINIU)) {
                if (isSmallAvatar) {
                    downloadAvatarUrl = StringUtils.getDownloadUrlForQiniu(mediaID, "imageView2", "0", "w",
                            String.valueOf(width), "h", String.valueOf(height));
                } else {
                    downloadAvatarUrl = StringUtils.getDownloadUrlForQiniu(mediaID);
                }

            } else if (provider.equalsIgnoreCase(PublicCloudUploadManager.PROVIDER_UNYUN)) {
                if (isSmallAvatar) {
                    downloadAvatarUrl = StringUtils.getDownloadUrlForUpyun(mediaID, Consts.UPYUN_IMAGE_BUCKET,
                            Consts.UPYUN_THUMB_NAME);
                } else {
                    downloadAvatarUrl = StringUtils
                            .getDownloadUrlForUpyun(mediaID, Consts.UPYUN_IMAGE_BUCKET, null);
                }
            } else if (provider.equalsIgnoreCase(PrivateCloudUploadManager.PROVIDER_FASTDFS)) {
                downloadAvatarUrl = StringUtils.getDownloadUrlForFastDFS(mediaID, isSmallAvatar);
            } else {
                Logger.ww(TAG, "unsupported provider contentType ! can not start download !mediaID = " + mediaID);
                CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS,
                        ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC);
                releaseLock(mediaID, isSmallAvatar);
                return;
            }
            Logger.d(TAG, "created url = " + downloadAvatarUrl);
            //下载头像的动作放到单独的线程池里去处理防止阻塞工作线程。
            //例如在大量获取头像请求发起时，如果直接阻塞工作线程，线程池中工作线程全部被占用，就会造成其他操作无法及时响应的问题
            Task.call(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    CommonUtils.getSyncHttpClient().get(downloadAvatarUrl, new AvatarDownloadHandler(mediaID, isSmallAvatar));
                    return null;
                }
            }, downloadExecutor);

        } else if (null != mediaID) {
            Logger.ww(TAG, "provider is null ! can not start download avatar!");
            CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS,
                    ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC);
        } else {
            Logger.ww(TAG, "user do not have a avatar yet,can not start download!");
            CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.LOCAL_ERROR.LOCAL_USER_AVATAR_NOT_SPECIFIED,
                    ErrorCode.LOCAL_ERROR.LOCAL_USER_AVATAR_NOT_SPECIFIED_DESC);
        }

    }

    //下载前检查，本地是否存在相应的文件，如果存在直接返回。
    private boolean needDownload(String mediaID, boolean isSmallAvatar) {
        if (isSmallAvatar) {
            String avatarPath = FileUtil.getAvatarFilePath(mediaID);
            Logger.d(TAG, "small avatar path = " + avatarPath);
            File avatarFile = new File(avatarPath);
            if (avatarFile.exists()) {
                CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, avatarFile);
                return false;
            }
        } else {
            String bigAvatarPath = FileUtil.getBigAvatarFilePath(mediaID);
            Logger.d(TAG, "big avatar path = " + bigAvatarPath);
            File bigAvatarFile = new File(bigAvatarPath);
            if (bigAvatarFile.exists()) {
                CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, bigAvatarFile);
                return false;
            }
        }
        return true;
    }

    //下载重复请求检查，如果有相同mediaID的请求正在执行，则之后同mediaID的
    //请求全部等待。等待第一个执行完后唤醒。
    private boolean duplicateDownloadCheck(String mediaID, boolean isSmallAvatar) throws InterruptedException {
        synchronized (sMediaIdCache) {
            if (containsMediaId(mediaID, isSmallAvatar)) {
                Logger.d(TAG, "contains duplicate media id in cache , wait until other task finishes");

                //此处将阻塞直到其他线程唤醒。
                sMediaIdCache.wait(90000);

                File avatarFile = new File(FileUtil.getAvatarFilePath(mediaID));
                File bigAvatarFile = new File(FileUtil.getBigAvatarFilePath(mediaID));
                if (avatarFile.exists() && isSmallAvatar) {
                    Logger.d(TAG, "awake from download lock,is small avatar ! avatar file = " + avatarFile);
                    CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, avatarFile);
                } else if (bigAvatarFile.exists() && !isSmallAvatar) {
                    Logger.d(TAG, "awake from download lock,is big avatar ! avatar file = " + bigAvatarFile);
                    CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, bigAvatarFile);
                } else {
                    Logger.d(TAG, "awake from download lock,but avatar file not exist. maybe download failed !");
                    CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT,
                            ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT_DESC);
                }
                return true;
            } else {
                addToMediaIdCache(mediaID, isSmallAvatar);
                return false;
            }
        }
    }

    private void addToMediaIdCache(String mediaId, boolean isSmallAvatar) {
        Logger.d(TAG, "addToMediaIdCache key " + mediaId + isSmallAvatar);
        sMediaIdCache.add(mediaId + isSmallAvatar);
    }

    private void removeFromMediaIdCache(String mediaId, boolean isSmallAvatar) {
        Logger.d(TAG, "removeFromMediaIdCache key " + mediaId + isSmallAvatar);
        sMediaIdCache.remove(mediaId + isSmallAvatar);
    }

    private boolean containsMediaId(String mediaId, boolean isSmallAvatar) {
        Logger.d(TAG, "containsMediaId key " + mediaId + isSmallAvatar);
        return sMediaIdCache.contains(mediaId + isSmallAvatar);
    }

    public class AvatarDownloadHandler extends DataAsyncHttpResponseHandler {

        private int retryTime = 0;

        private static final int MAX_RETRY_TIME = 3;

        private String mediaID;

        private boolean isSmallAvatar;

        public AvatarDownloadHandler(String mediaID, boolean isSmallAvatar) {
            this.mediaID = mediaID;
            this.isSmallAvatar = isSmallAvatar;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            File file;
            if (!isSmallAvatar) {
                file = FileUtil.byte2File(responseBody, FileUtil.getBigAvatarDirPath(), StringUtils.getResourceIDFromMediaID(mediaID));
            } else {
                file = FileUtil.byte2File(responseBody, FileUtil.getAvatarDirPath(), StringUtils.getResourceIDFromMediaID(mediaID));
            }
//            //这里用同步更新数据库两个原因。
//            // 1 防止和getUserinfoTask中insert in background冲突，导致插入两次
//            // 2 此处能确保是在子线程，不需要另起线程去执行数据库操作。
            // TODO: 2017/9/7 这里insert或者update userinfo应该是多余的动作，先把这里注释掉
//            UserInfoManager.getInstance().insertOrUpdateUserInfoSync(userInfo, true, false, false, false);
            if (null != mCallback) {
                CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
            }
            releaseLock(mediaID, isSmallAvatar);
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                              Throwable error) {
            Logger.ii(TAG, "download avatar failed ! statusCode = " + statusCode);
            // 下载失败时自动重试
            doRetryWhenFail();
        }

        private void doRetryWhenFail() {
            retryTime++;
            if (retryTime <= MAX_RETRY_TIME) {
                threadSleep(retryTime);
                CommonUtils.getSyncHttpClient().get(downloadAvatarUrl, this);
                return;
            } else if (null != mCallback) {
                CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT,
                        ErrorCode.HTTP_ERROR.HTTP_RETRY_REACH_LIMIT_DESC);
            }
            releaseLock(mediaID, isSmallAvatar);
        }

        private void threadSleep(int currentRetryTime) {
            try {
                Thread.sleep((long) (Math.pow(2, currentRetryTime) * 1000));
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public void releaseLock(String mediaID, boolean isSmallAvatar) {
        Logger.d(TAG, "release mediaID cache ! mediaID = " + mediaID);
        synchronized (sMediaIdCache) {
            sMediaIdCache.notifyAll();
            removeFromMediaIdCache(mediaID, isSmallAvatar);
        }
    }
}
