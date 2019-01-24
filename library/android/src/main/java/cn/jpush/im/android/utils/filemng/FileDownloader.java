package cn.jpush.im.android.utils.filemng;

import android.graphics.BitmapFactory;
import android.text.TextUtils;

import com.loopj.android.jpush.http.DataAsyncHttpResponseHandler;
import com.qiniu.android.jpush.utils.Crc32;
import com.qiniu.android.jpush.utils.Etag;

import org.apache.http.Header;

import java.io.File;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;

import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.MediaContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.bolts.AndroidExecutors;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.BitmapUtils;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.SendingMsgCallbackManager;
import cn.jpush.im.android.utils.StringUtils;

public class FileDownloader {

    /**
     * another background executor . different from background executor in {@link Task#BACKGROUND_EXECUTOR}.
     * since the thread in {@link Task#BACKGROUND_EXECUTOR} may be blocked to wait the complement of a {@link Task},
     * thus if the thread and the task's continuation runs in the same executor. it will cause dead lock.
     */
    public static final ExecutorService downloadExecutor = AndroidExecutors.newCachedThreadPool();

    private static final String TAG = "FileDownloader";

    private static final String FORMAT_AMR = "amr";

    private static final String FORMAT_MP3 = "mp3";

    private static final String DOT = ".";

    private static final int MAX_RETRY_TIME = 3;

    private static final int FILE_TYPE_ORIGIN = 1;

    private static final int FILE_TYPE_THUMB = 2;

    private static final int FILE_TYPE_VOICE = 3;

    private static final int FILE_TYPE_FILE = 6;

    private int retry_time = 0;

    private DownloadCompletionCallback mCompletionCallback;

    public FileDownloader() {
    }

    // 下载图片消息中的缩略图
    public void downloadThumbnailImage(InternalMessage msg, DownloadCompletionCallback completeCallback, boolean downloadInOtherThread) {
        if (!preDownloadCheck(msg, completeCallback, FILE_TYPE_THUMB)) {
            return;
        }
        mCompletionCallback = completeCallback;
        ImageContent content = (ImageContent) msg.getContent();
        String mediaID = content.getMediaID();
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.outWidth = content.getWidth();
        opts.outHeight = content.getHeight();
        // 根据原图大小计算出缩略图大小
        int sampleSize = BitmapUtils.computeSampleSize(opts, BitmapUtils.TUMBNAIL_ORIGIN_EDGE);
        int thumbnailWidth = opts.outWidth / sampleSize;
        int thumbnailHeight = opts.outHeight / sampleSize;
        FileDownloadHandler downloadHandler = new FileDownloadHandler(msg,
                FileUtil.OUTPUT_CATEGORY_THUMBNAILS);
        downloadHandler.downloadInOtherThread = downloadInOtherThread;
        createUrlAndStartDownload(mediaID, msg.getContentType(), true, thumbnailWidth, thumbnailHeight, downloadHandler);
    }

    // 下载图片消息中的原图
    public void downloadOriginImage(InternalMessage msg, DownloadCompletionCallback completeCallback, boolean downloadInOtherThread) {
        if (!preDownloadCheck(msg, completeCallback, FILE_TYPE_ORIGIN)) {
            return;
        }
        mCompletionCallback = completeCallback;
        ImageContent content = (ImageContent) msg.getContent();
        String mediaID = content.getMediaID();
        FileDownloadHandler downloadHandler = new FileDownloadHandler(msg,
                FileUtil.OUTPUT_CATEGORY_ORIGINS);
        downloadHandler.downloadInOtherThread = downloadInOtherThread;
        createUrlAndStartDownload(mediaID, msg.getContentType(), false, 0, 0, downloadHandler);
    }

    // 下载语音文件
    public void downloadVoiceFile(InternalMessage msg, DownloadCompletionCallback completeCallback, boolean downloadInOtherThread) {

        if (!preDownloadCheck(msg, completeCallback, FILE_TYPE_VOICE)) {
            return;
        }

        mCompletionCallback = completeCallback;
        VoiceContent content = (VoiceContent) msg.getContent();
        String mediaID = content.getMediaID();
//        String provider = StringUtils.getProviderFromMediaID(mediaID);
//        if (null != provider && !provider.equalsIgnoreCase(PrivateCloudUploadManager.PROVIDER_FASTDFS)) {
        // TODO: 2017/8/8 这里需要判断是否是私有云环境，如果是，则不应该再在mediaID中添加mp3后缀，因为私有云环境下的mediaID中已经包含了.mp3后缀了
        //这里在mediaID后面加上".mp3"后缀的原因见wiki:http://wiki.jpushoa.com/pages/viewpage.action?pageId=9562688
        if (!BuildConfig.IS_PCLOUD) {   //这里需要判断是否是私有云环境，如果是，则不应该再在mediaID中添加mp3后缀，因为私有云环境下的mediaID中已经包含了.mp3后缀了
            mediaID += DOT + FORMAT_MP3;//上传语音时会在mediaId后面默认添加".mp3"后缀，所以下载时也需要加上后缀
        }
//        }
        FileDownloadHandler downloadHandler = new FileDownloadHandler(msg,
                FileUtil.OUTPUT_CATEGORY_VOICE);
        downloadHandler.downloadInOtherThread = downloadInOtherThread;
        createUrlAndStartDownload(mediaID, msg.getContentType(), false, 0, 0, downloadHandler);
    }

    //下载文件
    public void downLoadFile(InternalMessage msg, DownloadCompletionCallback completeCallback, boolean downloadInOtherThread) {
        if (!preDownloadCheck(msg, completeCallback, FILE_TYPE_FILE)) {
            return;
        }

        mCompletionCallback = completeCallback;
        FileContent content = (FileContent) msg.getContent();
        String mediaID = content.getMediaID();
        FileDownloadHandler downloadHandler = new FileDownloadHandler(msg,
                FileUtil.OUTPUT_CATEGORY_FILE);
        downloadHandler.downloadInOtherThread = downloadInOtherThread;
        createUrlAndStartDownload(mediaID, msg.getContentType(), false, 0, 0, downloadHandler);
    }

    private boolean preDownloadCheck(InternalMessage msg, DownloadCompletionCallback completeCallback, int fileType) {
        if (!CommonUtils.doInitialCheck("preDownloadCheck", completeCallback)) {
            return false;
        }
        if (null == msg || !(msg.getContent() instanceof MediaContent)) {
            Logger.ww(TAG, "download file failed, msg is null or msg content is not a media content.");
            CommonUtils.doCompleteCallBackToUser(completeCallback, ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS,
                    ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC);
            return false;
        }
        //首先判断对应文件本地是否已经存在，如果有直接返回
        MediaContent content = (MediaContent) msg.getContent();
        String mediaID = content.getMediaID();
        String filePath;
        switch (fileType) {
            case FILE_TYPE_ORIGIN:
                filePath = FileUtil.getImageOriginDirPath(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey())
                        + File.separator + StringUtils.getResourceIDFromMediaID(mediaID);
                break;
            case FILE_TYPE_THUMB:
                filePath = FileUtil.getImageThumbnailDirPath(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey())
                        + File.separator + StringUtils.getResourceIDFromMediaID(mediaID);
                break;
            case FILE_TYPE_VOICE:
                filePath = FileUtil.getVoiceDirPath(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey())
                        + File.separator + StringUtils.getResourceIDFromMediaID(mediaID);
                break;
            case FILE_TYPE_FILE:
                filePath = FileUtil.getFileDirPath(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey())
                        + File.separator + StringUtils.getResourceIDFromMediaID(mediaID);
                break;
            default:
                filePath = null;
        }
        if (null != filePath && new File(filePath).exists()) {
            //如果要下载的文件已存在，直接返回。
            if (FILE_TYPE_THUMB == fileType) {
                ImageContent thumbContent = (ImageContent) content;
                thumbContent.setLocalThumbnailPath(filePath);
            } else {
                content.setLocalPath(filePath);
            }
            updateMessageContent(msg, content);
            updateMessageStatus(msg, MessageStatus.receive_success);
            //从本地直接返回时，直接在caller线程中回调callback，防止线程切换时带来不必要的开销。
            CommonUtils.doCompleteCallBackToUser(true, completeCallback, ErrorCode.NO_ERROR,
                    ErrorCode.NO_ERROR_DESC, new File(filePath));
            return false;
        }
        return true;
    }


    private void createUrlAndStartDownload(final String mediaID, ContentType contentType, boolean isThumb,
                                           int thumbnailWidth, int thumbnailHeight, final FileDownloadHandler handler) {
        String url = "";
        String provider = StringUtils.getProviderFromMediaID(mediaID);
        if (!TextUtils.isEmpty(provider)) {
            if (provider.equalsIgnoreCase(PublicCloudUploadManager.PROVIDER_QINIU)) {
                if (isThumb) {
                    url = StringUtils.getDownloadUrlForQiniu(mediaID, "imageView2", "0", "w",
                            String.valueOf(thumbnailWidth), "h", String.valueOf(thumbnailHeight));
                } else {
                    url = StringUtils.getDownloadUrlForQiniu(mediaID);
                }
            } else if (provider.equalsIgnoreCase(PublicCloudUploadManager.PROVIDER_UNYUN)) {
                if (contentType.equals(ContentType.image)) {
                    if (isThumb) {
                        url = StringUtils.getDownloadUrlForUpyun(mediaID, Consts.UPYUN_IMAGE_BUCKET,
                                Consts.UPYUN_THUMB_NAME);
                    } else {
                        url = StringUtils
                                .getDownloadUrlForUpyun(mediaID, Consts.UPYUN_IMAGE_BUCKET, null);
                    }
                } else if (contentType.equals(ContentType.voice)) {
                    url = StringUtils
                            .getDownloadUrlForUpyun(mediaID, Consts.UPYUN_VOICE_BUCKET, null);
                } else if (contentType.equals(ContentType.file)) {
                    url = StringUtils
                            .getDownloadUrlForUpyun(mediaID, Consts.UPYUN_FILE_BUCKET, null);
                }
            } else if (provider.equalsIgnoreCase(PrivateCloudUploadManager.PROVIDER_FASTDFS)) {
                url = StringUtils.getDownloadUrlForFastDFS(mediaID, isThumb);
            }
            Logger.d(TAG, "created url = " + url);
            if (!StringUtils.isTextEmpty(url)) {
                downloadFile(url, handler);
            } else {
                Logger.ww(TAG, "created url is null , can not start download.");
                CommonUtils.doCompleteCallBackToUser(mCompletionCallback, ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS,
                        ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC);
            }
        } else {
            Logger.ww(TAG, "provider is empty ! can not start download!");
            CommonUtils.doCompleteCallBackToUser(mCompletionCallback, ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS,
                    ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC);
        }
    }

    private void downloadFile(final String url, final FileDownloadHandler handler) {
        if (handler.downloadInOtherThread) {
            //下载消息附件放到单独的线程池里去处理防止阻塞工作线程。例如在批量同步消息时如果阻塞当前线程则会影响整体同步的效率
            Task.call(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    CommonUtils.getSyncHttpClient().get(url, handler);
                    return null;
                }
            }, downloadExecutor);
        } else {
            CommonUtils.getSyncHttpClient().get(url, handler);
        }
    }

    class FileDownloadHandler extends DataAsyncHttpResponseHandler {

        private InternalMessage msg;

        private String directory;

        boolean downloadInOtherThread;

        private double prePercent = 0;

        private int bytesReceived = 0;

        public FileDownloadHandler(InternalMessage msg, String directory) {
            this.msg = msg;
            this.directory = directory;
        }

        @Override
        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
            File file = null;
            MediaContent content = (MediaContent) msg.getContent();
            // 检查下载下来的文件crc是否匹配，若不匹配则触发重新下载。
            // 如果是下载的缩略图，则不检查crc,因为缩略图是在服务器端生成。
            // 如果是语音，也不检查，因为语音在上传过程中已转码，下载下来的和上传的已是两个不同的文件。
            if (!directory.equals(FileUtil.OUTPUT_CATEGORY_THUMBNAILS) && !directory
                    .equals(FileUtil.OUTPUT_CATEGORY_VOICE) && !fileCheck(content, responseBody)) {
                Logger.d(TAG, "file check failed! try to re download");
                doRetryWhenFail(directory);
                return;
            }
            switch (msg.getContentType()) {
                case image:
                    ImageContent imageContent = (ImageContent) content;
                    // 设置message content中图片本地的路径
                    if (directory.equals(FileUtil.OUTPUT_CATEGORY_ORIGINS)) {
                        // 文件保存到本地
                        file = FileUtil.byte2File(responseBody,
                                FileUtil.getImageOriginDirPath(msg.getTargetType(), msg.getTargetID(),
                                        msg.getTargetAppKey()), StringUtils.getResourceIDFromMediaID(content.getMediaID()));
                        imageContent.setLocalPath(file.getAbsolutePath());
                    } else {
                        // 文件保存到本地
                        file = FileUtil.byte2File(responseBody,
                                FileUtil.getImageThumbnailDirPath(msg.getTargetType(), msg.getTargetID(),
                                        msg.getTargetAppKey()), StringUtils.getResourceIDFromMediaID(content.getMediaID()));
                        imageContent.setLocalThumbnailPath(file.getAbsolutePath());
                    }
                    break;
                case voice:
                    file = FileUtil.byte2File(responseBody,
                            FileUtil.getVoiceDirPath(msg.getTargetType(), msg.getTargetID(),
                                    msg.getTargetAppKey()), StringUtils.getResourceIDFromMediaID(content.getMediaID()));
                    content.setLocalPath(file.getAbsolutePath());
                    break;
                case file:
                    file = FileUtil.byte2File(responseBody,
                            FileUtil.getFileDirPath(msg.getTargetType(), msg.getTargetID(),
                                    msg.getTargetAppKey()), StringUtils.getResourceIDFromMediaID(content.getMediaID()));
                    content.setLocalPath(file.getAbsolutePath());
                    break;
                default:
                    break;
            }
            // 更新本地消息的内容和状态
            updateMessageStatus(msg, MessageStatus.receive_success);
            updateMessageContent(msg, content);
            if (downloadInOtherThread) {
                //如果下载本身就是在单独的线程池中执行的，则回调直接在当前线程执行
                CommonUtils.doCompleteCallBackToUser(true, mCompletionCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
            } else {
                CommonUtils.doCompleteCallBackToUser(mCompletionCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
            }
        }

        @Override
        public void onFailure(int statusCode, Header[] headers, byte[] responseBody,
                              Throwable error) {
            Logger.ww(TAG, "file download failed ! statusCode = " + statusCode);
            // 下载失败时自动重试
            doRetryWhenFail(directory);
        }

        @Override
        public void onProgressData(byte[] responseBody, int bytesTotal) {
            bytesReceived += responseBody.length;
            final double percent = (double) bytesReceived / bytesTotal;
            final ProgressUpdateCallback callback = SendingMsgCallbackManager
                    .getDownloadProgressCallbackFromTarget(msg.getTargetID(), msg.getTargetAppKey(), msg.getId());
            if (callback != null) {
                Executor executor = callback.isRunInUIThread() ? Task.UI_THREAD_EXECUTOR
                        : Task.BACKGROUND_EXECUTOR;
                Task.call(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        try {
                            if (prePercent < percent) {
                                CommonUtils.doCompleteCallBackToUser(true, callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, percent);
                                prePercent = percent;
                            }
                        } catch (Throwable e) {
                            Logger.ee(TAG, "error occured in callback when do progress update!", e);
                        }
                        return null;
                    }
                }, executor);
            }
        }

        private void doRetryWhenFail(String type) {
            retry_time++;
            if (retry_time <= MAX_RETRY_TIME) {
                try {
                    Thread.sleep((long) (Math.pow(2, retry_time) * 1000));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (type.equals(FileUtil.OUTPUT_CATEGORY_ORIGINS)) {
                    downloadOriginImage(msg, mCompletionCallback, downloadInOtherThread);
                } else if (type.equals(FileUtil.OUTPUT_CATEGORY_THUMBNAILS)) {
                    downloadThumbnailImage(msg, mCompletionCallback, downloadInOtherThread);
                } else if (type.equals(FileUtil.OUTPUT_CATEGORY_VOICE)) {
                    downloadVoiceFile(msg, mCompletionCallback, downloadInOtherThread);
                } else if (type.equals(FileUtil.OUTPUT_CATEGORY_FILE)) {
                    downLoadFile(msg, mCompletionCallback, downloadInOtherThread);
                }
            } else {
                updateMessageStatus(msg, MessageStatus.receive_fail);
                CommonUtils.doCompleteCallBackToUser(mCompletionCallback, ErrorCode.OTHERS_ERROR.OTHERS_DOWNLOAD_ERROR,
                        ErrorCode.OTHERS_ERROR.OTHERS_DOWNLOAD_ERROR_DESC);
            }
        }

    }

    private boolean updateMessageContent(InternalMessage msg, MessageContent content) {
        InternalConversation conv = ConversationManager.getInstance().getConversation(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey());
        if (null != conv) {
            conv.updateMessageContentInBackground(msg, content);
        }
        return true;
    }

    private boolean updateMessageStatus(InternalMessage msg, MessageStatus status) {
        if (msg.getDirect() == MessageDirect.receive) {
            InternalConversation conv = ConversationManager.getInstance().getConversation(msg.getTargetType(), msg.getTargetID(), msg.getTargetAppKey());
            if (null != conv) {
                conv.updateMessageStatusInBackground(msg, status);
            }
            return true;
        }
        return true;
    }

    private boolean fileCheck(MediaContent content, byte[] responseBody) {
        if (0 == content.getCrc()) {
            //web端发出的媒体消息无法拿到文件crc，即拿到的crc等于0，此时使用media_hash来验证文件
            String etag = Etag.data(responseBody);
            return etag != null && etag.equalsIgnoreCase(content.getHash());
        } else {
            //如果crc值存在，还是使用crc来做验证。
            return content.getCrc() == Crc32.bytes(responseBody);
        }
    }

}
