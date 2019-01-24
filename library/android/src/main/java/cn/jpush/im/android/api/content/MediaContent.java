package cn.jpush.im.android.api.content;

import android.text.TextUtils;

import com.google.gson.jpush.annotations.Expose;
import com.qiniu.android.jpush.utils.Crc32;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import cn.jpush.im.android.Consts;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public abstract class MediaContent extends MessageContent {

    private static final String TAG = "MediaContent";
    protected String resourceId;

    @Expose
    private boolean isFileUploaded = false;

    @Expose
    protected String local_path;

    @Expose
    protected Number media_crc32;

    @Expose
    protected String hash;

    // 最终的 media_id 在上传时会在前边加上 provider
    @Expose
    protected String media_id;

    @Expose
    protected Number fsize;

    @Expose
    protected String format;

    protected MediaContent() {
        super();
    }

    void initMediaMetaInfo(File file, ContentType type, String format) throws FileNotFoundException {
        if (null == file || !file.exists() || file.length() == 0) {
            throw new FileNotFoundException();
        }
        if (!file.isFile()) {
            Logger.ee(TAG, "input media file is a directory.failed to create media content.");
            throw new FileNotFoundException();
        }
        try {
            media_crc32 = Crc32.file(file);
        } catch (IOException e) {
            e.printStackTrace();
            // ignore
        }
        contentType = type;
        local_path = file.getAbsolutePath();
        if (TextUtils.isEmpty(resourceId)) {
            resourceId = StringUtils.createResourceID(format);
        }
        fsize = file.length();
        this.format = format;
        media_id = File.separator + type + File.separator + Consts.PLATFORM_ANDROID + File.separator + resourceId;
    }

    /**
     * 获取消息中包含的媒体文件的本地路径
     *
     * @return 媒体文件的本地路径
     */
    public String getLocalPath() {
        return local_path;
    }

    public void setLocalPath(String local_path) {
        this.local_path = local_path;
    }

    /**
     * 获取消息中包含的媒体文件的crc校验码
     *
     * @return 媒体文件的crc校验码
     */
    public Long getCrc() {
        return null != media_crc32 ? media_crc32.longValue() : 0;
    }

    public String getHash() {
        return hash;
    }

    public String getMediaID() {
        return media_id;
    }

    public void setMediaID(String mediaID) {
        media_id = mediaID;
    }

    public String getResourceId() {
        return resourceId;
    }

    /**
     * 获取消息中包含的媒体文件的文件大小，单位：byte
     *
     * @return 媒体文件的文件大小
     */
    public long getFileSize() {
        return null != fsize ? fsize.longValue() : 0;
    }

    public boolean isFileUploaded() {
        return isFileUploaded;
    }

    public void setFileUploaded(boolean isFileUploaded) {
        this.isFileUploaded = isFileUploaded;
    }

    /**
     * 获取附件的格式.
     *
     * @return 附件格式
     */
    public String getFormat() {
        return format;
    }


    /**
     * @deprecated deprecated in 2.2.1. use constructor to set format
     */
    public void setFormat(String format) {
        this.format = format;
    }
}
