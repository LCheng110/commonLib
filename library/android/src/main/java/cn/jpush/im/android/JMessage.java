package cn.jpush.im.android;


import android.content.Context;
import android.text.TextUtils;

import java.io.File;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jpush.im.android.api.JMessageConfigs;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.helpers.MsgStatusResetHelper;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

public class JMessage {
    private static final String TAG = "JMessage";
    public static final String ACTION_NOTI_RECEIVER_PROXY = "cn.jpush.im.android.action.NOTIFICATION_CLICK_PROXY";
    public static Context mContext;
    public static boolean isPCloud = BuildConfig.IS_PCLOUD;
    public static boolean isTest;
    public static String httpUserCenterPrefix = "";
    public static String httpSyncPrefix = "";
    public static String httpsUserCenterPrefix = "";
    public static String httpSDKApiPathPrifix = "";
    public static String httpSyncApiPathPrifix = "";
    public static String httpUserPowerPrefix = "";
    //    public static String httpFastDfsTracker = "";
    public static String sChattingTarget = "";
    private static boolean useCustomEnvironment = false;
    private static String debugApiHost = BuildConfig.DEBUG_API_HOST;
    private static int debugApiPort = BuildConfig.DEBUG_API_HOST_PORT;
    private static int debugSyncApiPort = BuildConfig.DEBUG_API_SYNC_HOST_PORT;
    public static String fastDfsTrackerHost = BuildConfig.FASTDFS_TRACKER_HOST;
    public static int fastDfsTrackerPort = BuildConfig.FASTDFS_TRACKER_PORT;
    public static int fastDfsTrackerHttpPort = BuildConfig.FASTDFS_TRACKER_HTTP_PORT;
    public static String customStorageHostForUpload = null;
    public static int customStoragePortForUpload = -1;
    public static String customStorageHostForDownload = null;
    public static int customStoragePortForDownload = -1;
    public static String customStoragePrefixForDownload = null;
    private static AtomicInteger allUnreadMsgCount = new AtomicInteger(-1);

    public static void init(Context context, boolean msgRoaming) {
        IMConfigs.init(context);
        mContext = context;
        IMConfigs.setNetworkConnected(CommonUtils.isNetworkConnected(mContext));

        IMConfigs.setKeyMsgRoaming(msgRoaming ? 1 : 0);
        if (0 != IMConfigs.getUserID()) {
            //用户已登录，将notifyManager 置为ready状态。
            ChatMsgManager.getInstance().ready();
            //重置数据库中消息状态
            new MsgStatusResetHelper().resetStatus();
//            //这里使用发送广播来启动syncCheck，因为init有可能会在多个进程中重复调用，为了防止出现多个进程同时发送syncCheck的情况。
//            //使用广播来做跨进程通信，统一在上层应用进程中启动SyncCheck.
            //issue:https://community.jiguang.cn/t/caused-by-java-lang-illegalstateexception-cannot-broadcast-before-boot-completed/16252/2
            //暂时把init时的广播发送去掉，因为在push login的时候会触发一次sync check启动，这里的可以认为是多余的
//            Intent intent = new Intent(IMResponseHelper.ACTION_IM_RESPONSE);
//            intent.putExtra(IMReceiver.CMD_START_SYNCCHECK, true);
//            intent.addCategory(context.getPackageName());
//            context.sendBroadcast(intent);
        }

        String configJsonString = IMConfigs.getDefaultConfig();
        if (null != configJsonString) {
            //使已缓存的配置生效
            configHttpUrl(context, JsonUtil.fromJsonOnlyWithExpose(configJsonString, JMessageConfigs.class), false);
        } else {
            JMessage.setEnvironment(false);//使默认配置生效
        }
        //如果是私有云环境，默认不能将jcore切到test环境，否则jcore会自动接入到im的测试环境，无法自定义接入ip了。
//        JCoreInterface.setTestConn(!isPCloud && IMConfigs.getIsTestConn());
    }

    public static void setEnvironment(boolean isTestEnvironment) {
        isTest = isTestEnvironment;
        //加入用户等级聊天接口
        httpUserPowerPrefix = Consts.HTTP_PROTOCOL_PREFIX + debugApiHost + File.pathSeparator +
                8088;
//        IMConfigs.setIsTestConn(isTest); //私有云环境不需要调用这个接口
        if (useCustomEnvironment || isTest) { //如果是启用了自定义环境，或者接入的是测试环境:
            httpUserCenterPrefix = Consts.HTTP_PROTOCOL_PREFIX + debugApiHost + File.pathSeparator +
                    debugApiPort;
            httpsUserCenterPrefix = Consts.HTTP_PROTOCOL_PREFIX + debugApiHost + File.pathSeparator +
                    debugApiPort;
            httpSyncPrefix = Consts.HTTP_PROTOCOL_PREFIX + debugApiHost + File.pathSeparator +
                    debugSyncApiPort;

            //fix http://jira.jpushoa.com/browse/IM-3224
            if (!TextUtils.isEmpty(httpSDKApiPathPrifix)) {
                httpUserCenterPrefix += httpSDKApiPathPrifix;
                httpsUserCenterPrefix += httpSDKApiPathPrifix;
            }
            if (!TextUtils.isEmpty(httpSyncApiPathPrifix)) {
                httpSyncPrefix += httpSyncApiPathPrifix;
            }
        } else {
            httpUserCenterPrefix = Consts.HTTP_PROTOCOL_PREFIX + Consts.API_HOST;
            httpSyncPrefix = Consts.HTTP_PROTOCOL_PREFIX + Consts.SYNC_HOST;
            httpsUserCenterPrefix = Consts.HTTPS_PROTOCOL_PREFIX + Consts.SECURE_API_HOST;
        }
//        if (!isTest) {
//            httpUserCenterPrefix = Consts.HTTP_PROTOCOL_PREFIX + Consts.API_HOST;
//            httpSyncPrefix = Consts.HTTP_PROTOCOL_PREFIX + Consts.SYNC_HOST;
//            httpsUserCenterPrefix = Consts.HTTPS_PROTOCOL_PREFIX + Consts.SECURE_API_HOST;
//
//        } else {
//            httpUserCenterPrefix = Consts.HTTP_PROTOCOL_PREFIX + debugApiHost + File.pathSeparator +
//                    debugApiPort;
//            httpsUserCenterPrefix = Consts.HTTP_PROTOCOL_PREFIX + debugApiHost + File.pathSeparator +
//                    debugApiPort;
//            httpSyncPrefix = Consts.HTTP_PROTOCOL_PREFIX + debugApiHost + File.pathSeparator +
//                    debugSyncApiPort;
//        }
//        httpFastDfsTracker = fastDfsTrackerHost + File.pathSeparator +
//                fastDfsTrackerPort;
    }

    /**
     * 增加消息未读总数
     *
     * @param delta 增加的数量
     */
    public static int addAllUnreadMsgCntBy(int delta) {
        if (allUnreadMsgCount.get() != -1) {
            allUnreadMsgCount.addAndGet(delta);
            if (0 > allUnreadMsgCount.get()) {
                allUnreadMsgCount.set(0);
            }
            Logger.d(TAG, "[allUnreadMsgCount] all unread cnt change . delta = " + delta + " all cnt = " + allUnreadMsgCount.get());
            return allUnreadMsgCount.get();
        }
        return -1;
    }

    /**
     * 获取消息未读总数
     */
    public static int getAllUnreadMsgCnt() {
        if (allUnreadMsgCount.get() < 0) {
            ConversationManager.getInstance().initAllUnReadCount(allUnreadMsgCount);
        }
        return allUnreadMsgCount.get();
    }

    /**
     * 将消息总未读数重置
     */
    public static void resetAllUnreadMsgCnt() {
        allUnreadMsgCount.set(-1);
    }


    public static void configHttpUrl(Context context, JMessageConfigs jMessageConfigs, boolean localize) {
        if (null == jMessageConfigs) {
            Logger.ee(TAG, "[configHttpUrl] failed. jmessageConfigs cannot be null");
            return;
        }
        if (null != context) {
            IMConfigs.init(context);
        }
        useCustomEnvironment = true;
        debugApiHost = jMessageConfigs.httpIp;
        debugApiPort = jMessageConfigs.httpPort;
        httpSDKApiPathPrifix = jMessageConfigs.sdkApiPathPrefix;
        httpSyncApiPathPrifix = jMessageConfigs.syncApiPathPrefix;
        debugSyncApiPort = jMessageConfigs.syncHttpPort;
        fastDfsTrackerHost = jMessageConfigs.fastDfsTrackerHost;
        fastDfsTrackerPort = jMessageConfigs.fastDfsTrackerPort;
        fastDfsTrackerHttpPort = jMessageConfigs.fastDfsTackerHttpPort;
        customStorageHostForUpload = jMessageConfigs.fastDfsStorageHostForUpload;
        customStoragePortForUpload = jMessageConfigs.fastDfsStoragePortForUpload;
        customStorageHostForDownload = jMessageConfigs.fastDfsStorageHostForDownload;
        customStoragePortForDownload = jMessageConfigs.fastDfsStoragePortForDownload;
        customStoragePrefixForDownload = jMessageConfigs.fastDfsStoragePrefixForDownload;
        JMessage.setEnvironment(false);//使配置生效
        if (localize) {
            IMConfigs.setDefaultConfig(JsonUtil.toJson(jMessageConfigs));//配置落地
        }
    }
}
