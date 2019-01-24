package cn.jpush.im.android.api.content;

import android.text.TextUtils;

import com.google.gson.jpush.annotations.Expose;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.exceptions.JMFileSizeExceedException;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.filemng.FileDownloader;

/**
 * @since 1.4.0
 */
public class FileContent extends MediaContent {

    @Expose
    private String fname;

    private static final long MAX_FILE_SIZE = 0;//0代表文件大小无限制

    protected FileContent() {
        super();
    }

    /**
     * 创建一个file类型的message content。使用文件原名填充消息协议中的fname字段。
     *
     * @param file 发送的文件对象
     * @throws FileNotFoundException
     * @throws JMFileSizeExceedException
     */
    public FileContent(File file) throws FileNotFoundException, JMFileSizeExceedException {
        internalFileContent(file, null);
    }

    /**
     * 创建一个file类型的message content。使用指定的fileName填充消息协议中的fname字段。
     *
     * @param file     发送的文件对象
     * @param fileName 指定发送的文件名称,如果不填或为空，则默认使用文件原名。
     * @throws FileNotFoundException
     * @throws JMFileSizeExceedException
     */
    public FileContent(File file, String fileName) throws FileNotFoundException, JMFileSizeExceedException {
        internalFileContent(file, fileName);
    }

    private void internalFileContent(File file, String fileName) throws FileNotFoundException, JMFileSizeExceedException {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("FileContent", null)) {
            return;
        }

        if (null == file) {
            throw new FileNotFoundException("file parameter should not be null");
        }

        if (0 < MAX_FILE_SIZE && file.length() > MAX_FILE_SIZE) {
            throw new JMFileSizeExceedException("Allowed maximum file size is " + MAX_FILE_SIZE + " bytes");
        }

        if (TextUtils.isEmpty(fileName)) {
            this.fname = file.getName();
        } else {
            this.fname = fileName;
        }
        initMediaMetaInfo(file, ContentType.file, StringUtils.getFormatFromFileName(fname));
    }

    /**
     * 获取文件名称
     *
     * @return 文件名称
     */
    public String getFileName() {
        return fname;
    }

    /**
     * 下载消息中文件。注意：sdk收到文件消息后，不会自动下载文件附件，
     * <br/>需要用户手动调用此接口完成下载。
     *
     * @param message  文件消息对象
     * @param callback 下载完成时的回调接口
     */
    public void downloadFile(final Message message, final DownloadCompletionCallback callback) {
        if (!CommonUtils.doInitialCheck("downloadFile", callback)) {
            return;
        }
        String path = getLocalPath();
        if (TextUtils.isEmpty(path) || !new File(path).exists()) {
            if (!IMConfigs.getNetworkConnected()) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                return;
            }
            Task.callInBackground(new Callable<Object>() {
                @Override
                public Object call() throws Exception {
                    FileDownloader fd = new FileDownloader();
                    fd.downLoadFile((InternalMessage) message, callback, false);
                    return null;
                }
            });
        } else {
            File file = new File(path);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
        }
    }

}
