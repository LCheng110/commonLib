package cn.jpush.im.android.utils.filemng;

import java.io.File;

import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by xiongtc on 2017/3/23.
 */

public interface IUploadManager {
    boolean prepareToUpload(File file, String format, ContentType type, InternalMessage msg, BasicCallback usersCompletionCallback);
}
