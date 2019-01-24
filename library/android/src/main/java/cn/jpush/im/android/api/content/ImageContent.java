package cn.jpush.im.android.api.content;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.google.gson.jpush.annotations.Expose;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.BitmapUtils;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.filemng.FileDownloader;
import cn.jpush.im.api.BasicCallback;

public class ImageContent extends MediaContent {

    private static final String TAG = "ImageContent";

    @Expose
    private Number width;

    @Expose
    private Number height;

    @Expose
    private String img_link;

    @Expose
    private String localThumbnailPath;

    protected ImageContent() {
        super();
    }

    public static abstract class CreateImageContentCallback extends BasicCallback {
        private final static String TAG = "CreateImageContentCallback";

        protected CreateImageContentCallback() {
        }

        protected CreateImageContentCallback(boolean isRunInUIThread) {
            super(isRunInUIThread);
        }

        public abstract void gotResult(int responseCode, String responseMessage, ImageContent imageContent);

        @Override
        public void gotResult(int i, String s) {
            Logger.ee(TAG, "Should not reach here! ");
        }

        @Override
        public void gotResult(int responseCode, String responseMessage, Object... result) {
            ImageContent imageContent = null;
            if (null != result && result.length > 0 && null != result[0]) {
                imageContent = (ImageContent) result[0];
            }
            gotResult(responseCode, responseMessage, imageContent);
        }
    }

    /**
     * 创建一个图片类型的MessageContent，同时会依据传入的图片生成相应的缩略图，并存储在本地。
     * <p>
     * 创建ImageContent过程可能会比较耗时，具体根据传入图片的大小而定。 建议使用异步创建ImageContent的接口
     * {@link ImageContent#createImageContentAsync(File, CreateImageContentCallback)}。
     *
     * @param imageFile 本地图片的File对象
     * @throws FileNotFoundException 如果file为null或路径指向的文件不存在，将抛出此异常
     */
    public ImageContent(File imageFile) throws FileNotFoundException {
        createImageContentWithFile(imageFile, null);
    }

    /**
     * 创建一个图片类型的MessageContent，同时会依据传入的图片生成相应的缩略图，并存储在本地。
     * <p>
     * 此接口可以指定文件在后台存储时的扩展名，如果填空或者不填，则后台存储文件时将没有扩展名。
     * <p>
     * 创建ImageContent过程可能会比较耗时，具体根据传入图片的大小而定。 建议使用异步创建ImageContent的接口
     * {@link ImageContent#createImageContentAsync(File, CreateImageContentCallback)}。
     *
     * @param imageFile 本地图片的File对象
     * @param format    文件扩展名，注意名称中不要包括"."
     * @throws FileNotFoundException 如果file为null或路径指向的文件不存在，将抛出此异常
     * @since 2.2.1
     */
    public ImageContent(File imageFile, String format) throws FileNotFoundException {
        createImageContentWithFile(imageFile, format);
    }

    private void createImageContentWithFile(File imageFile, String format) throws FileNotFoundException {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("ImageContent", null)) {
            return;
        }
        initMediaMetaInfo(imageFile, ContentType.image, format);
        BitmapFactory.Options opts = new BitmapFactory.Options();
        localThumbnailPath = BitmapUtils.createThumbnailAndSave(local_path, opts, BitmapUtils.TUMBNAIL_ORIGIN_EDGE, getResourceId());
        width = opts.outWidth;
        height = opts.outHeight;
    }


    /**
     * 创建一个图片类型的MessageContent，同时会依据传入的图片生成相应的缩略图，并存储在本地。
     * <p>
     * 创建ImageContent过程可能会比较耗时，具体根据传入图片的大小而定。 建议使用异步创建ImageContent的接口
     * {@link ImageContent#createImageContentAsync(Bitmap, CreateImageContentCallback)}。
     *
     * @param image 图片的Bitmap对象
     */
    public ImageContent(Bitmap image) {
        createImageContentWithBitmap(image, null);
    }

    /**
     * 创建一个图片类型的MessageContent，同时会依据传入的图片生成相应的缩略图，并存储在本地。
     * <p>
     * 此接口可以指定文件在后台存储时的扩展名，如果填空或者不填，则后台存储文件时将没有扩展名。
     * <p>
     * 创建ImageContent过程可能会比较耗时，具体根据传入图片的大小而定。 建议使用异步创建ImageContent的接口
     * {@link ImageContent#createImageContentAsync(Bitmap, CreateImageContentCallback)}。
     *
     * @param image  图片的Bitmap对象
     * @param format 文件扩展名，注意名称中不要包括"."
     * @since 2.2.1
     */
    public ImageContent(Bitmap image, String format) {
        createImageContentWithBitmap(image, format);
    }

    private void createImageContentWithBitmap(Bitmap image, String format) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("ImageContent", null)) {
            return;
        }
        resourceId = StringUtils.createResourceID(format);
        String originPath = BitmapUtils.saveOriginToLocal(image, resourceId);
        if (null != originPath) {
            File originFile = new File(originPath);
            try {
                initMediaMetaInfo(originFile, ContentType.image, format);
                BitmapFactory.Options opts = new BitmapFactory.Options();
                localThumbnailPath = BitmapUtils.createThumbnailAndSave(originPath, opts, BitmapUtils.TUMBNAIL_ORIGIN_EDGE, getResourceId());
                width = opts.outWidth;
                height = opts.outHeight;
            } catch (FileNotFoundException e) {
                Logger.ww(TAG, "init media meta info failed.");
                e.printStackTrace();
            }
        } else {
            Logger.ee(TAG, "create ImageContent failed ! bitmap is null");
        }
    }

    /**
     * 异步创建ImageContent。因为创建ImageContent时需要生成对应的缩略图并存储，整个过程会有耗时，有可能造成
     * 线程阻塞。所以推荐使用此异步接口来创建ImageContent。
     *
     * @param imageFile 本地图片的File对象
     * @param callback  创建ImageContent的回调接口
     */
    public static void createImageContentAsync(final File imageFile, final CreateImageContentCallback callback) {
        createImegeContentAsyncWithFile(imageFile, null, callback);
    }

    /**
     * 异步创建ImageContent。因为创建ImageContent时需要生成对应的缩略图并存储，整个过程会有耗时，有可能造成
     * 线程阻塞。所以推荐使用此异步接口来创建ImageContent。
     * <p>
     * 此接口可以指定文件在后台存储时的扩展名，如果填空或者不填，则后台存储文件时将没有扩展名。
     *
     * @param imageFile 本地图片的File对象
     * @param format    文件扩展名，注意名称中不要包括"."
     * @param callback  创建ImageContent的回调接口
     * @since 2.2.1
     */
    public static void createImageContentAsync(final File imageFile, final String format, final CreateImageContentCallback callback) {
        createImegeContentAsyncWithFile(imageFile, format, callback);
    }

    private static void createImegeContentAsyncWithFile(final File imageFile, final String format, final CreateImageContentCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("createImageContentAsync", callback)) {
            return;
        }
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    ImageContent content = new ImageContent(imageFile, format);
                    CommonUtils.doCompleteCallBackToUser(callback,
                            ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, content);
                } catch (FileNotFoundException e) {
                    CommonUtils.doCompleteCallBackToUser(callback,
                            ErrorCode.LOCAL_ERROR.LOCAL_FILE_NOT_FOUND, ErrorCode.LOCAL_ERROR.LOCAL_FILE_NOT_FOUND_DESC);
                } catch (Exception e) {
                    CommonUtils.doCompleteCallBackToUser(callback,
                            ErrorCode.LOCAL_ERROR.LOCAL_CREATE_IMAGE_CONTENT_FAIL, ErrorCode.LOCAL_ERROR.LOCAL_CREATE_IMAGE_CONTENT_FAIL_DESC);
                }
                return null;
            }
        });
    }

    /**
     * 异步创建ImageContent。因为创建ImageContent时需要生成对应的缩略图并存储，整个过程会有耗时，有可能造成
     * 线程阻塞。所以推荐使用此异步接口来创建ImageContent。
     *
     * @param image    图片的Bitmap对象
     * @param callback 创建ImageContent的回调接口
     */
    public static void createImageContentAsync(final Bitmap image, final CreateImageContentCallback callback) {
        createImageContentAsyncWithBitmap(image, null, callback);
    }

    /**
     * 异步创建ImageContent。因为创建ImageContent时需要生成对应的缩略图并存储，整个过程会有耗时，有可能造成
     * 线程阻塞。所以推荐使用此异步接口来创建ImageContent。
     * <p>
     * 此接口可以指定文件在后台存储时的扩展名，如果填空或者不填，则后台存储文件时将没有扩展名。
     *
     * @param image    图片的Bitmap对象
     * @param format   文件扩展名，注意名称中不要包括"."
     * @param callback 创建ImageContent的回调接口
     * @since 2.2.1
     */
    public static void createImageContentAsync(final Bitmap image, final String format, final CreateImageContentCallback callback) {
        createImageContentAsyncWithBitmap(image, format, callback);
    }

    private static void createImageContentAsyncWithBitmap(final Bitmap image, final String format, final CreateImageContentCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("createImageContentAsync", callback)) {
            return;
        }
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                try {
                    ImageContent content = new ImageContent(image, format);
                    CommonUtils.doCompleteCallBackToUser(callback,
                            ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, content);
                } catch (Exception e) {
                    CommonUtils.doCompleteCallBackToUser(callback,
                            ErrorCode.LOCAL_ERROR.LOCAL_CREATE_IMAGE_CONTENT_FAIL, ErrorCode.LOCAL_ERROR.LOCAL_CREATE_IMAGE_CONTENT_FAIL_DESC);
                }
                return null;
            }
        });
    }

    /**
     * 获取原图宽度
     *
     * @return 图片宽度像素值
     */
    public int getWidth() {
        return null != width ? width.intValue() : 0;
    }

    /**
     * 获取原图高度
     *
     * @return 图片高度像素值
     */
    public int getHeight() {
        return null != height ? height.intValue() : 0;
    }

    public String getImg_link() {
        return img_link;
    }

    public void setImg_link(String img_link) {
        this.img_link = img_link;
    }


    /**
     * 获取对应缩略图的本地路径
     *
     * @return 缩略图的本地路径
     */
    public String getLocalThumbnailPath() {
        return localThumbnailPath;
    }

    public void setLocalThumbnailPath(String localThumbnailPath) {
        this.localThumbnailPath = localThumbnailPath;
    }

    /**
     * 下载图片消息中的原图
     *
     * @param message  图片消息对象
     * @param callback 下载完成后的回调接口
     */
    public void downloadOriginImage(final Message message,
                                    final DownloadCompletionCallback callback) {
        if (!CommonUtils.doInitialCheck("downloadOriginImage", callback)) {
            return;
        }
        String originPath = getLocalPath();
        if (TextUtils.isEmpty(originPath) || !new File(originPath).exists()) {
            if (!IMConfigs.getNetworkConnected()) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                return;
            }
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    FileDownloader dm = new FileDownloader();
                    dm.downloadOriginImage((InternalMessage) message, callback, false);
                    return null;
                }
            });
        } else {
            File file = new File(originPath);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
        }
    }

    /**
     * 下载图片消息中原图对应的缩略图<br/>sdk会在接收到图片消息时自动下载缩略图文件，仅当自动下载失败，
     * 消息状态为receive_fail时，需要用户调用此接口手动下载
     *
     * @param message  图片消息对象
     * @param callback 下载完成后的回调接口
     */
    public void downloadThumbnailImage(final Message message,
                                       final DownloadCompletionCallback callback) {
        if (!CommonUtils.doInitialCheck("downloadThumbnailImage", callback)) {
            return;
        }
        String thumbnailPath = getLocalThumbnailPath();
        if (TextUtils.isEmpty(thumbnailPath) || !new File(thumbnailPath).exists()) {
            if (!IMConfigs.getNetworkConnected()) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                return;
            }
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    FileDownloader dm = new FileDownloader();
                    dm.downloadThumbnailImage((InternalMessage) message, callback, false);
                    return null;
                }
            });
        } else {
            File file = new File(thumbnailPath);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
        }
    }

}
