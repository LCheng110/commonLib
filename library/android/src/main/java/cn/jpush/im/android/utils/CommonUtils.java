package cn.jpush.im.android.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.text.TextUtils;

import com.loopj.android.jpush.http.SyncHttpClient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executor;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.MessageSendingMaintainer;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.ConversationStorage;
import cn.jpush.im.android.storage.EventIdStorage;
import cn.jpush.im.android.storage.OnlineMsgRecvStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.api.BasicCallback;


public class CommonUtils {

    private static final String TAG = "CommonUtils";

    private static SyncHttpClient sSyncHttpClient;

    public synchronized static SyncHttpClient getSyncHttpClient() {
        if (null == sSyncHttpClient) {
            sSyncHttpClient = new SyncHttpClient();
        }

        return sSyncHttpClient;
    }

    public static void setSyncHttpClient(SyncHttpClient syncHttpClient) {
        sSyncHttpClient = syncHttpClient;
    }


    public static synchronized long getSeqID() {
        long seqId = IMConfigs.getNextRid();
        Logger.d(TAG, "seqID = " + seqId);
        return seqId;
    }

    public static long getFixedTime() {
        long serverLoginTime = IMConfigs.getPushServerTime();
        long serverLocalTime = IMConfigs.getPushLocalTime();
        Logger.d(TAG, "serverLoginTime = " + serverLoginTime + " serverLocalTime = " + serverLocalTime);
        return serverLoginTime + (System.currentTimeMillis() - serverLocalTime);
    }

    public static boolean validateStrings(String methodName, String... params) {
        boolean isValid = true;
        if (null != params && params.length > 0) {
            for (int i = 0; i <= params.length - 1; i++) {
                if (TextUtils.isEmpty(params[i]) || TextUtils.isEmpty(params[i].trim())) {
                    Logger.ee(TAG, "[" + methodName + "] invalid parameter! parameter " + i + " is empty");
                    isValid = false;
                    break;
                }
            }
        } else {
            Logger.ee(TAG, "[" + methodName + "] invalid parameters! parameters are null");
            isValid = false;
        }
        return isValid;
    }

    public static boolean isLogin(String methodName) {
        if (0 == IMConfigs.getUserID()) {
            Logger.ee(TAG, "[" + methodName + "] have not logged in!");
            return false;
        }
        return true;
    }

    public static boolean isInited(String methodName) {
        if (null == JMessage.mContext) {
            Logger.ee(TAG, "[" + methodName + "]sdk have not init, you should call JMessageClient.init first.");
            return false;
        }
        return true;
    }

    public static boolean validateObjects(String methodName, Object... params) {
        boolean isValid = true;
        if (null != params && params.length > 0) {
            for (int i = 0; i <= params.length - 1; i++) {
                if (null == params[i]) {
                    Logger.ee(TAG, "[" + methodName + "] invalid parameter! parameter " + i + " is null");
                    isValid = false;
                    break;
                }
                if (params[i] instanceof Collection
                        && !validateCollectionSize(methodName, (Collection) params[i])) {
                    isValid = false;
                    break;
                }
            }
        } else {
            Logger.ee(TAG, "[" + methodName + "] invalid parameters! parameters are null");
            isValid = false;
        }
        return isValid;
    }

    private static final int UIDS_LIST_SIZE = 500;

    public static boolean validateCollectionSize(String methodName, Collection... params) {
        boolean isValid = true;
        if (null != params && params.length > 0) {
            for (int i = 0; i <= params.length - 1; i++) {
                if (params[i].isEmpty()) {
                    Logger.ee(TAG, "[" + methodName + "] invalid parameter! list is empty");
                    isValid = false;
                    break;
                }

                if (UIDS_LIST_SIZE < params[i].size()) {
                    Logger.ee(TAG, "[" + methodName + "] invalid parameter! list size limit exceeded," +
                            "limit is " + UIDS_LIST_SIZE);
                    isValid = false;
                    break;
                }
            }
        } else {
            Logger.ee(TAG, "[" + methodName + "] invalid parameters! parameters are null");
            isValid = false;
        }
        return isValid;
    }

    public static boolean isNetworkConnected(Context context) {
        ConnectivityManager mConnectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        if (mNetworkInfo != null) {
            return mNetworkInfo.isAvailable();
        }
        return false;
    }

    public static boolean doInitialCheck(String methodName, BasicCallback callback) {
        if (!isInited(methodName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT,
                    ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT_DESC);
            return false;
        }
        if (!IMConfigs.getNetworkConnected()) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED,
                    ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
            return false;
        }
        if (!isLogin(methodName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN,
                    ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return false;
        }
        return true;
    }

    public static boolean doInitialCheckWithoutNetworkCheck(String methodName, BasicCallback callback) {
        if (!isInited(methodName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT,
                    ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT_DESC);
            return false;
        }
        if (!isLogin(methodName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN,
                    ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return false;
        }
        return true;
    }


    public static boolean handleRegCode(int registerCode, BasicCallback callback) {
        boolean isHandle;
        int code;
        String desc;
        switch (registerCode) {
            case 1005:
                code = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_APPKEY_APPID_NOT_MATCH;
                desc = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_APPKEY_APPID_NOT_MATCH_DESC;
                isHandle = true;
                break;
            case 1006:
                code = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_PACKAGE_NOT_EXIST;
                desc = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_PACKAGE_NOT_EXIST_DESC;
                isHandle = true;
                break;
            case 1007:
                code = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_INVALID_IMEI;
                desc = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_INVALID_IMEI_DESC;
                isHandle = true;
                break;
            case 1008:
                code = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_WRONG_APPKEY;
                desc = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_WRONG_APPKEY_DESC;
                isHandle = true;
                break;
            case 1009:
                code = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_APPKEY_PLATFORM_NOT_MATCH;
                desc = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_ERROR_APPKEY_PLATFORM_NOT_MATCH_DESC;
                isHandle = true;
                break;
            case 0:
                code = ErrorCode.NO_ERROR;
                desc = ErrorCode.NO_ERROR_DESC;
                isHandle = false;
                break;
            case -1:
                code = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_NOT_FINISHED;
                desc = ErrorCode.PUSH_REGISTER_ERROR.PUSH_REGISTER_NOT_FINISHED_DESC;
                isHandle = true;
                break;
            default:
                code = registerCode;
                desc = "push 注册失败";
                isHandle = true;
                break;
        }
        if (isHandle) {
            doCompleteCallBackToUser(callback, code, desc);
        }
        return isHandle;
    }

    public static void doProgressCallbackToUser(String targetID, String appkey, int msgID, final double percent) {
        final ProgressUpdateCallback callback = SendingMsgCallbackManager
                .getUploadProgressCallbackFromTarget(targetID, appkey, msgID);
        if (null != callback) {
            Executor executor = callback.isRunInUIThread() ? Task.UI_THREAD_EXECUTOR
                    : Task.BACKGROUND_EXECUTOR;
            Task.call(new Callable<Void>() {
                @Override
                public Void call() throws Exception {
                    try {
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC,
                                percent);
                    } catch (Throwable e) {
                        Logger.ee(TAG, "error occured in callback when do progress update!", e);
                    }
                    return null;
                }
            }, executor);
        }
    }

    public static void doCompleteCallBackToUser(boolean inCallerThread, final BasicCallback callback,
                                                final int responseCode, final String desc, final Object... params) {
        if (callback != null) {
            if (!inCallerThread) {
                Executor executor = callback.isRunInUIThread() ? Task.UI_THREAD_EXECUTOR
                        : Task.BACKGROUND_EXECUTOR;
                Task.call(new Callable<Void>() {
                    @Override
                    public Void call() throws Exception {
                        callbackToUser(callback, responseCode, desc, params);
                        return null;
                    }
                }, executor);
            } else {
                callbackToUser(callback, responseCode, desc, params);
            }
        } else {
            Logger.d(TAG, "complete callback is null !");
        }
    }

    public static void doCompleteCallBackToUser(final BasicCallback callback,
                                                final int responseCode, final String desc,
                                                final Object... params) {
        doCompleteCallBackToUser(false, callback, responseCode, desc, params);
    }


    //所有callback的聚合方法
    private static void callbackToUser(final BasicCallback callback,
                                       final int responseCode, final String desc,
                                       final Object... params) {
        try {
            callback.gotResult(responseCode, desc, params);
        } catch (Exception e) {
            Logger.ww(TAG, "error occured in users callback, msg = " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void doMessageCompleteCallbackToUser(String targetID, String appkey, int msgID,
                                                       final int responseCode, final String desc) {
        BasicCallback callback = SendingMsgCallbackManager
                .getCompleteCallbackFromTarget(targetID, appkey, msgID);
        doCompleteCallBackToUser(callback, responseCode, desc);
        SendingMsgCallbackManager.removeCallbacks(targetID, appkey, msgID);
        MessageSendingMaintainer.removeIdentifier(targetID, appkey, msgID);
    }

    public static List<String> translateUserIdToDisplaynames(List<Long> userIds, boolean includeNoteName) {
        List<String> displayNames = new ArrayList<String>();
        if (null != userIds) {
            List<InternalUserInfo> userInfos = UserInfoManager.getInstance().getUserInfoList(userIds);
            for (UserInfo info : userInfos) {
                displayNames.add(((InternalUserInfo) info).getDisplayName(includeNoteName));
            }
        }
        return displayNames;
    }

    public static void trimListSize(Set<Long> list, String tableName) {
        if (InternalConversation.MAX_ONLINE_MSGID_CACHE_SIZE < list.size()) {
            Logger.d(TAG, "clean when online list is too large. size is " + list.size());
            List<Long> tempList = new ArrayList<Long>(list);
            tempList = tempList.subList(InternalConversation.ONLINE_MSGID_TRIM_SIZE, tempList.size());
            list.retainAll(tempList);
            if (tableName.startsWith(ConversationStorage.PREFIX_ONLINE_MSG_TABLE_NAME)) {
                //如果是online开头，说明是消息去重列表
                OnlineMsgRecvStorage.removeRowSync(tableName, InternalConversation.ONLINE_MSGID_TRIM_SIZE);
            } else if (tableName.startsWith(ConversationStorage.PREFIX_ONLINE_EVENT_TABLE_NAME)) {
                //如果是event开头，说明是事件去重列表
                EventIdStorage.removeRowSync(tableName, InternalConversation.ONLINE_MSGID_TRIM_SIZE);
            }
            Logger.d(TAG, "after clean . size is " + list.size());
        }
    }
}
