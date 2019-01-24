package cn.jpush.im.android.utils;

import android.text.TextUtils;
import android.util.SparseArray;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.api.BasicCallback;


public class SendingMsgCallbackManager {

    private static final String TAG = "SendingMsgCallbackManager";


    /**
     * callback是依附于message对象之上，所以需要有一个target到message的hashCode的映射来
     * 判断是否是同一个message对象
     */
    private static Map<String, Integer> targetToHashMap = new HashMap<String, Integer>();

    private static SparseArray<Object[]> callbacksMap = new SparseArray<Object[]>();


    public static void saveCallbacks(String targetID, String appkey, int msgID, int hashcode,
                                     ProgressUpdateCallback progressUploadCallback,
                                     ProgressUpdateCallback progressDownloadCallback, BasicCallback completeCallback) {

        Logger.d(TAG, "save callback targetID = " + targetID + " msgID = " + msgID + " hashcode = "
                + hashcode + " callbacks = " + progressUploadCallback + progressDownloadCallback
                + completeCallback);
        String targetKey = createTargetKey(targetID, appkey, msgID);
        targetToHashMap.put(targetKey, hashcode);
        Object[] objects = callbacksMap.get(hashcode);
        if (objects == null) {
            objects = new Object[3];
        }
        if (null != progressUploadCallback) {
            objects[0] = progressUploadCallback;
        }
        if (null != progressDownloadCallback) {
            objects[1] = progressDownloadCallback;
        }
        if (null != completeCallback) {
            objects[2] = completeCallback;
        }
        callbacksMap.put(hashcode, objects);
    }

    public static ProgressUpdateCallback getUploadProgressCallbackFromTarget(String targetID, String appkey,
                                                                             int msgID) {
        String targetKey = createTargetKey(targetID, appkey, msgID);
        if (null != targetToHashMap.get(targetKey)
                && targetToHashMap.get(targetKey) != 0) {
            return getUploadProgressCallbackFromHash(targetToHashMap.get(targetKey));
        } else {
            return null;
        }
    }

    public static ProgressUpdateCallback getUploadProgressCallbackFromHash(int hashcode) {
        Object[] result = callbacksMap.get(hashcode);
        if (result != null && result[0] instanceof ProgressUpdateCallback) {
            Logger.d(TAG, "get UploadProgressCallback key = " + hashcode + " callbacks = " + result[0]
                    .toString());
            return ((ProgressUpdateCallback) result[0]);
        }
        return null;
    }

    public static ProgressUpdateCallback getDownloadProgressCallbackFromTarget(String targetID, String appkey,
                                                                               int msgID) {
        String targetKey = createTargetKey(targetID, appkey, msgID);
        if (null != targetToHashMap.get(targetKey)
                && targetToHashMap.get(targetKey) != 0) {
            return getDownloadProgressCallbackFromHash(targetToHashMap.get(targetKey));
        } else {
            return null;
        }
    }

    public static ProgressUpdateCallback getDownloadProgressCallbackFromHash(int hashcode) {
        Object[] result = callbacksMap.get(hashcode);
        if (result != null && result[1] instanceof ProgressUpdateCallback) {
            return ((ProgressUpdateCallback) result[1]);
        }
        return null;
    }

    public static BasicCallback getCompleteCallbackFromTarget(String targetID, String appkey, int msgID) {
        String targetKey = createTargetKey(targetID, appkey, msgID);
        if (null != targetToHashMap.get(targetKey)
                && targetToHashMap.get(targetKey) != 0) {
            return getCompleteCallbackFromHash(targetToHashMap.get(targetKey));
        } else {
            return null;
        }
    }

    public static BasicCallback getCompleteCallbackFromHash(int hashcode) {
        Object[] result = callbacksMap.get(hashcode);
        if (result != null && result[2] instanceof BasicCallback) {
            Logger.d(TAG, "get CompleteCallback key = " + hashcode + " callbacks = " + result[2]
                    .toString());
            return ((BasicCallback) result[2]);
        }
        return null;
    }


    public static void clear() {
        targetToHashMap.clear();
        callbacksMap.clear();
    }

    public static void removeCallbacks(String targetID, String appkey, int msgID) {
        Logger.d(TAG, "removeCallbacks targetID = " + targetID + " msgID = " + msgID);
        String targetKey = createTargetKey(targetID, appkey, msgID);
        if (null != targetToHashMap.get(targetKey)) {
            int hashcode = targetToHashMap.get(targetKey);
            targetToHashMap.remove(targetKey);
            callbacksMap.remove(hashcode);
        } else {
            Logger.w(TAG, "remove callback failed!");
        }
    }

    private static String createTargetKey(String targetID, String appkey, int msgID) {
        if (null != appkey && !TextUtils.isEmpty(targetID)) { //对于appkey不需要判断空字符串，因为群聊会话中不存在appkey的概念，
            return targetID + "_" + appkey + "_" + msgID;
        }
        Logger.d(TAG, "create targetKey failed! appkey = " + appkey + " targetID = " + targetID + " msgID = " + msgID);
        return null;
    }

}
