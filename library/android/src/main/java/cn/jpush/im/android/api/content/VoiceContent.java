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
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.filemng.FileDownloader;

public class VoiceContent extends MediaContent {

    @Expose
    private Number duration;

    protected VoiceContent() {
        super();
    }

    public VoiceContent(File voiceFile, int duration) throws FileNotFoundException {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("VoiceContent", null)) {
            return;
        }
        this.duration = duration;
        initMediaMetaInfo(voiceFile, ContentType.voice, null);//语音文件在上传时会默认添加上"mp3"后缀，这里mediaId不加后缀。
    }

    /**
     * 获取语音文件时长
     *
     * @return 语音文件的时长
     */
    public int getDuration() {
        return null != duration ? duration.intValue() : 0;
    }

    /**
     * 下载语音消息中的语音文件。<br/>sdk会在接收到语音消息时自动下载语音文件，仅当自动下载失败，
     * 消息状态为receive_fail时，需要用户调用此接口手动下载
     *
     * @param message  语音消息对象
     * @param callback 下载完成时的回调接口
     */
    public void downloadVoiceFile(final Message message,
                                  final DownloadCompletionCallback callback) {
        if (!CommonUtils.doInitialCheck("downloadVoiceFile", callback)) {
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
                    FileDownloader dm = new FileDownloader();
                    dm.downloadVoiceFile((InternalMessage) message, callback, false);
                    return null;
                }
            });
        } else {
            File file = new File(path);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, file);
        }
    }

}
