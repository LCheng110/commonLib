package cn.jpush.im.android.utils.filemng;

import java.io.File;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.content.MediaContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by xiongtc on 2017/3/27.
 */

class UploadManagerImpl implements IUploadManager {
    private static final String TAG = "UploadManagerImpl";

    boolean fileFromMsg = false;
    InternalMessage message;
    String resourceID;
    MediaContent mediaContent;
    File file;
    ContentType contentType;
    String fileFormat = null;
    private BasicCallback usersCompletionCallback;

    @Override
    public boolean prepareToUpload(File file, String format, ContentType type, InternalMessage msg, BasicCallback usersCompletionCallback) {
        this.usersCompletionCallback = usersCompletionCallback;
        if (null == file || !file.exists()) {
            doCompleteCallbackToUser(false, ErrorCode.LOCAL_ERROR.LOCAL_FILE_NOT_FOUND, ErrorCode.LOCAL_ERROR.LOCAL_FILE_NOT_FOUND_DESC, null);
            return false;
        }
        fileFormat = format;
        if (null != msg) {
            fileFromMsg = true;
            message = msg;
            mediaContent = (MediaContent) msg.getContent();
            fileFormat = mediaContent.getFormat();
            resourceID = StringUtils.getResourceIDFromMediaID(mediaContent.getMediaID());
        } else {
            fileFromMsg = false;
            resourceID = StringUtils.createResourceID(format);
        }
        this.contentType = type;
        this.file = file;
        return true;
    }

    /**
     * Uploader上传完成后需要回调此方法通知上层callback
     *
     * @param isUploadFinished
     * @param statusCode
     * @param responseMsg
     * @param mediaID
     */
    void doCompleteCallbackToUser(boolean isUploadFinished, int statusCode,
                                  String responseMsg, String mediaID) {
        if (statusCode != 0 && fileFromMsg) {
            //上传失败时需要将message状态改为send_fail
            if (!updateMessageStatus(MessageStatus.send_fail)) {
                Logger.w(TAG, "update message status failed! return from upload file.");
            }
        } else if (fileFromMsg) {
            //上传成功需要更新message content(更新其中的mediaID)
            mediaContent.setFileUploaded(isUploadFinished);
            if (!updateMessageContent()) {
                Logger.w(TAG, "update message status failed! return from upload file.");
            }
        }
        if (null != usersCompletionCallback) {
            if (fileFromMsg) {
                CommonUtils.doCompleteCallBackToUser(usersCompletionCallback, statusCode, responseMsg);
            } else {
                CommonUtils.doCompleteCallBackToUser(usersCompletionCallback, statusCode, responseMsg, mediaID);
            }
        }
        if (fileFromMsg) {
            FileUploader.removeProgressInCache(message.getTargetID(), message.getTargetAppKey(), message.getId());
        }
    }

    boolean updateMessageContent() {
        InternalConversation conv = ConversationManager.getInstance().getConversation(message.getTargetType(), message.getTargetID(), message.getTargetAppKey());
        return null != conv && conv.updateMessageContent(message, mediaContent);
    }

    private boolean updateMessageStatus(MessageStatus status) {
        InternalConversation conv = ConversationManager.getInstance().getConversation(message.getTargetType(), message.getTargetID(), message.getTargetAppKey());
        return null != conv && conv.updateMessageStatus(message, status);
    }
}
