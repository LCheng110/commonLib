package cn.jpush.im.android;


import cn.jpush.im.android.utils.JMessageBasePreference;

public class IMConfigs extends JMessageBasePreference {

    public static final int INT_UNSET = -100;

    public static final String KEY_USER_NAME = "im_user_name";

    public static void setUserName(String userName) {
        commitString(KEY_USER_NAME, userName);
    }

    public static String getUserName() {
        return getString(KEY_USER_NAME, null);
    }

    public static final String KEY_USER_PASSWORD = "im_user_pwd";

    public static void setUserPassword(String password) {
        commitString(KEY_USER_PASSWORD, password);
    }

    public static String getUserPassword() {
        return getString(KEY_USER_PASSWORD, null);
    }

    public static final String KEY_USER_ID = "im_user_id";

    public static void setUserID(long userID) {
        commitLong(KEY_USER_ID, userID);
    }

    public static long getUserID() {
        return getLong(KEY_USER_ID, 0);
    }

    private static final String KEY_NOTIFICATION_MODE = "im_noti_mode";

    public static int getNotificationMode() {
        return getInt(KEY_NOTIFICATION_MODE, INT_UNSET);
    }

    private static final String KEY_TOKEN = "im_token";

    public static void setToken(String token) {
        commitString(KEY_TOKEN, token);
    }

    public static String getToken() {
        return getString(KEY_TOKEN, "");
    }

    private static final String KEY_PUSH_LOCAL_TIME = "push_login_local_time";

    public static void setPushLocalTime(long timeInMills) {
        commitLong(KEY_PUSH_LOCAL_TIME, timeInMills);
    }

    public static long getPushLocalTime() {
        return getLong(KEY_PUSH_LOCAL_TIME, System.currentTimeMillis());
    }

    private static final String KEY_PUSH_SERVER_TIME = "push_login_server_time";

    public static void setPushServerTime(long timeInMills) {
        commitLong(KEY_PUSH_SERVER_TIME, timeInMills);
    }

    public static long getPushServerTime() {
        return getLong(KEY_PUSH_SERVER_TIME, System.currentTimeMillis());
    }

    private static final String KEY_NETWORK_CONNECTED = "push_network_connected";

    public static void setNetworkConnected(boolean isConn) {
        commitBoolean(KEY_NETWORK_CONNECTED, isConn);
    }

    public static boolean getNetworkConnected() {
        return getBoolean(KEY_NETWORK_CONNECTED, true);
    }

    private static final String KEY_IS_TEST_CONN = "im_is_test_conn";

    public static void setIsTestConn(boolean isTestConn) {
        commitBoolean(KEY_IS_TEST_CONN, isTestConn);
    }

    public static boolean getIsTestConn() {
        return getBoolean(KEY_IS_TEST_CONN, false);
    }

    private static final String KEY_DEFAULT_CONFIG = "im_default_config";

    public static void setDefaultConfig(String configJson) {
        commitString(KEY_DEFAULT_CONFIG, configJson);
    }

    public static String getDefaultConfig() {
        return getString(KEY_DEFAULT_CONFIG, null);
    }

    private static final String KEY_NODISTURB_GLOBAL = "nodisturb_global";

    public static void setNodisturbGlobal(int nodisturbGlobal) {
        commitInt(KEY_NODISTURB_GLOBAL, nodisturbGlobal);
    }

    //如果从未设置过，则默认返回-1
    public static int getNodisturbGlobal() {
        return getInt(KEY_NODISTURB_GLOBAL, -1);
    }

    private static final String KEY_SYNC_KEY_PREFIX = "synckey";

    public static void setConvSyncKey(long uid, long syncKey) {
        commitLong(KEY_SYNC_KEY_PREFIX + uid, syncKey);
    }

    public static long getConvSyncKey(long uid) {
        return getLong(KEY_SYNC_KEY_PREFIX + uid, 0L);
    }

    private static final String KEY_SYNC_EVENT_KEY_PREFIX = "sync_event_key";

    public static void setSyncEventKey(long uid, long syncKey) {
        commitLong(KEY_SYNC_EVENT_KEY_PREFIX + uid, syncKey);
    }

    public static long getSyncEventKey(long uid) {
        return getLong(KEY_SYNC_EVENT_KEY_PREFIX + uid, 0L);
    }

    private static final String KEY_SYNC_RECEIPT_KEY_PREFIX = "sync_receipt_key";

    public static void setSyncReceiptKey(long uid, long syncKey) {
        commitLong(KEY_SYNC_RECEIPT_KEY_PREFIX + uid, syncKey);
    }

    public static long getSyncReceiptKey(long uid) {
        return getLong(KEY_SYNC_RECEIPT_KEY_PREFIX + uid, 0L);
    }

    private static final String KEY_MSG_ROAMING = "msg_roaming";

    public static void setKeyMsgRoaming(int msgRoaming) {
        commitInt(KEY_MSG_ROAMING, msgRoaming);
    }

    //如果从未设置过，则默认返回0,即没有漫游消息
    public static int getMsgRoaming() {
        return getInt(KEY_MSG_ROAMING, 0);
    }

    private static final String KEY_NOTIFICATION_FLAG = "im_noti_flag";

    public static void setNotificationFlag(int flag) {
        commitInt(KEY_NOTIFICATION_FLAG, flag);
    }

    public static int getNotificationFlag() {
        return getInt(KEY_NOTIFICATION_FLAG, INT_UNSET);
    }

    private static final String KEY_SDK_VERSION = "sdk_version";

    public static void setSdkVersion(String version) {
        commitString(KEY_SDK_VERSION, version);
    }

    public static String getSdkVersion() {
        return getString(KEY_SDK_VERSION, "");
    }
}
