package cn.jpush.im.android.api;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicBoolean;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jiguang.ald.api.SdkType;
import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetBlacklistCallback;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoListCallback;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.callback.GetNoDisurbListCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserStatusCallback;
import cn.jpush.im.android.api.callback.IntegerCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.MediaContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.event.CommandNotificationEvent;
import cn.jpush.im.android.api.exceptions.JMFileSizeExceedException;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.android.api.options.RegisterOptionalUserInfo;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.pushcommon.helper.IMResponseHelper;
import cn.jpush.im.android.pushcommon.helper.JMessageAction;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.storage.UserInfoStorage;
import cn.jpush.im.android.tasks.GetBlackListTask;
import cn.jpush.im.android.tasks.GetBlockedGroupsTask;
import cn.jpush.im.android.tasks.GetGroupIDListTask;
import cn.jpush.im.android.tasks.GetGroupInfoTask;
import cn.jpush.im.android.tasks.GetGroupMembersTask;
import cn.jpush.im.android.tasks.GetNoDisturbListTask;
import cn.jpush.im.android.tasks.GetUserInfoTask;
import cn.jpush.im.android.tasks.GetUserStatueTask;
import cn.jpush.im.android.tasks.RegisterTask;
import cn.jpush.im.android.tasks.UpdatePasswordTask;
import cn.jpush.im.android.tasks.UpdateUserInfoTask;
import cn.jpush.im.android.utils.AndroidUtil;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.android.utils.filemng.FileUploader;
import cn.jpush.im.api.BasicCallback;

/**
 * 极光IM SDK 的主入口类，提供大部分的接口调用。
 * Main Entrance for JMessage IM SDK.
 */
// JMessageClient in release
// JMessageClient文件在不同buildType下各有一份拷贝，目前包括release、debug、pcloud、pcloudForFastDFS。
//不同的文件会在执行不同buildType的打包任务是被打包到sdk中去。
//所以需要注意之后所有针对JMessageClient这个类的改动都需要看情况同步到这几份文件中去。
public class JMessageClient {

    private static final String TAG = "JMessageClient";

    /**
     * 不展示通知
     *
     * @deprecated deprecated in 2.2.0,use {@link JMessageClient#setNotificationFlag(int)} instead.
     */
    public static final int NOTI_MODE_NO_NOTIFICATION = 0;

    /**
     * 展示通知，有声音有震动
     *
     * @deprecated deprecated in 2.2.0,use {@link JMessageClient#setNotificationFlag(int)} instead.
     */
    public static final int NOTI_MODE_DEFAULT = 1;

    /**
     * 展示通知，无声音有震动
     *
     * @deprecated deprecated in 2.2.0,use {@link JMessageClient#setNotificationFlag(int)} instead.
     */
    public static final int NOTI_MODE_NO_SOUND = 2;

    /**
     * 展示通知，有声音无震动
     *
     * @deprecated deprecated in 2.2.0,use {@link JMessageClient#setNotificationFlag(int)} instead.
     */
    public static final int NOTI_MODE_NO_VIBRATE = 3;

    /**
     * 展示通知，无声音无震动
     *
     * @deprecated deprecated in 2.2.0,use {@link JMessageClient#setNotificationFlag(int)} instead.
     */
    public static final int NOTI_MODE_SILENCE = 4;


    /**
     * 展示通知栏通知，所有设置均默认打开。
     * <p>
     * 此为通知栏的默认行为
     *
     * @since 2.2.0
     */
    public static final int FLAG_NOTIFY_DEFAULT = 0x7FFFFFFF;//最高位表示通知开关，其他位为设置位
    /**
     * 展示通知栏通知,其他设置均为关闭。
     *
     * @since 2.2.0
     */
    public static final int FLAG_NOTIFY_SILENCE = 0;
    /**
     * 不展示通知栏通知
     *
     * @since 2.2.0
     */
    public static final int FLAG_NOTIFY_DISABLE = 0x80000000;//最高位为1表示关闭通知
    /**
     * 收到通知时，发出声音
     *
     * @since 2.2.0
     */
    public static final int FLAG_NOTIFY_WITH_SOUND = 0x00000001;//第一位表示声音开关
    /**
     * 收到通知时，产生震动
     *
     * @since 2.2.0
     */
    public static final int FLAG_NOTIFY_WITH_VIBRATE = 0x00000002;//第二位表示震动开关
    /**
     * 收到通知时，点亮呼吸灯
     *
     * @since 2.2.0
     */
    public static final int FLAG_NOTIFY_WITH_LED = 0x00000004;//第三位表示led开关

    private static AtomicBoolean isInited = new AtomicBoolean(false);

    static {
        //TODO::如果init接口在某个service或者activity则会出问题
        JCoreInterface.initAction(SdkType.JMESSAGE.name(), JMessageAction.class);
//        JCoreInterface.initActionExtra(SdkType.JMESSAGE.name(), JMessageActionExtra.class);
    }

    public static void configHost(JMessageConfigs jMessageConfigs){
        JMessage.configHttpUrl(null, jMessageConfigs, false);
    }

    public static void configHost(String apiHost, int apiPort, int syncApiPort, String sdkApiPathPrefix, String syncApiPathPrefix, String trackerHost,
                                  int trackerPort, int trackerHttpPort, String storageHostForUpload, int storagePortForUpload,
                                  String storageHostForDownload, int storagePortForDownload, String storagePrefixForDownload) {
        JMessageConfigs jMessageConfigs = new JMessageConfigs();
        jMessageConfigs.httpIp = apiHost;
        jMessageConfigs.httpPort = apiPort;
        jMessageConfigs.syncHttpPort = syncApiPort;
        jMessageConfigs.sdkApiPathPrefix = sdkApiPathPrefix;
        jMessageConfigs.syncApiPathPrefix = syncApiPathPrefix;
        jMessageConfigs.fastDfsTrackerHost = trackerHost;
        jMessageConfigs.fastDfsTrackerPort = trackerPort;
        jMessageConfigs.fastDfsTackerHttpPort = trackerHttpPort;
        jMessageConfigs.fastDfsStorageHostForUpload = storageHostForUpload;
        jMessageConfigs.fastDfsStoragePortForUpload = storagePortForUpload;
        jMessageConfigs.fastDfsStorageHostForDownload = storageHostForDownload;
        jMessageConfigs.fastDfsStoragePortForDownload = storagePortForDownload;
        jMessageConfigs.fastDfsStoragePrefixForDownload = storagePrefixForDownload;
        configHost(jMessageConfigs);
    }

    /**
     * 动态配置jcore以及jmessage各种连接域名的接口。
     * <p>
     * 自定义配置会被保存至本地，下次启动时会自动启用已保存的配置。
     * <p>
     * 配置生效之后，im会主动登出当前已登录的用户。上层用户需要重新登陆
     *
     * @param context
     * @param jCoreConfigs    jcore的配置信息
     * @param jMessageConfigs jmessge的配置信息
     */
    public static void configHost2(Context context, JCoreConfigs jCoreConfigs, JMessageConfigs jMessageConfigs) {
        //先重新配置jmessage，内部触发logout.然后触发jcore重连。
        if (null != jMessageConfigs) {
            jMessageConfigs.apply(context);
        }

        if (null != context && null != jCoreConfigs) {
            jCoreConfigs.apply(context, true);
        }
    }

    /**
     * SDK初始化,默认不启用消息记录漫游。
     * <p>
     * 在调用IM其他接口前必须先调此接口初始化SDK，推荐在application类中调用。
     *
     * @param context 应用程序上下文对象
     */
    public static synchronized void init(Context context) {
        init(context, false);
    }

    /**
     * SDK初始化,同时指定是否启用消息记录漫游，如果启用，sdk将会把当前登陆用户的消息历史记录同步到本地。
     * <p>
     * 在调用IM其他接口前必须先调此接口初始化SDK，推荐在application类中调用。
     *
     * @param context    应用程序上下文对象
     * @param msgRoaming 是否需要消息记录漫游。 true-需要，false-不需要
     * @since 2.1.0
     */
    public static synchronized void init(Context context, boolean msgRoaming) {
        if (isInited.get()) {
            return;
        }
        if (null == context) {
            Logger.ee(TAG, "init context should not be null!");
            return;
        }
        Context appContext = context.getApplicationContext();
        if (JCoreInterface.init(appContext, false)) {
            if (!AndroidUtil.hasReceiverIntentFilter(context, IMResponseHelper.ACTION_IM_RESPONSE, true)) {
                Logger.ee(TAG, "AndroidManifest.xml missing required intent filter for IMReceiver: " + IMResponseHelper.ACTION_IM_RESPONSE);
            }
            //init各个模块
            JMessage.init(appContext, msgRoaming);
            JCoreInterface.restart(context, SdkType.JMESSAGE.name(), new Bundle(), false);

            int buildID = BuildConfig.BUILD_ID + BuildConfig.BUILD_ID_BASE;
            Logger.ii(TAG, "JMessage SDK init finished! version = " + BuildConfig.SDK_VERSION + " build id = " + buildID);
            isInited.set(true);
        }
    }

    /**
     * 设置debug模式，sdk将会输出更多debug信息。仅在开发阶段使用，应用对外发布时应关闭。
     *
     * @param isDebugMode true-打开debug模式， false-关闭debug模式。
     */
    public static void setDebugMode(boolean isDebugMode) {
        JCoreInterface.setDebugMode(isDebugMode);
    }

    /**
     * 用户注册
     *
     * @param userName 开发者注册的用户标识，应该唯一。
     * @param password 用户登录密码，推荐将字符串加密。
     * @param callback 回调接口
     */
    public static void register(String userName, String password, BasicCallback callback) {
        register(userName, password, null, callback);
    }

    /**
     * 用户注册
     *
     * @param userName         开发者注册的用户标识，应该唯一。
     * @param password         用户登录密码，推荐将字符串加密。
     * @param optionalUserInfo 注册时的用户其他信息
     * @param callback         回调接口
     * @since 2.3.0
     */
    public static void register(String userName, String password, RegisterOptionalUserInfo optionalUserInfo, BasicCallback callback) {
        if (!CommonUtils.isInited("register")) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT_DESC);
            return;
        }

        if (!IMConfigs.getNetworkConnected()) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
            return;
        }
        if (!CommonUtils.validateStrings("register", userName, password)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }

        userName = userName.trim();
        password = password.trim();
        if (!ExpressionValidateUtil.validUserName(userName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME_DESC);
            return;
        }
        if (!ExpressionValidateUtil.validPassword(password)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PASSWORD,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PASSWORD_DESC);
            return;
        }

        Map<String, Object> optionalRequestMap = null;
        if (optionalUserInfo != null) {
            optionalRequestMap = optionalUserInfo.getRequestMap();
        }
        RegisterTask task = new RegisterTask(userName, password, optionalRequestMap, callback, false);
        task.execute();
    }

    /**
     * 用户登录
     *
     * @param userName 开发者注册的用户名，应该唯一。
     * @param password 用户登录密码，推荐将字符串加密。
     * @param callback 回调接口
     */
    public static void login(String userName, String password, BasicCallback callback) {
        if (!CommonUtils.isInited("login")) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT_DESC);
            return;
        }
        if (!IMConfigs.getNetworkConnected()) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
            return;
        }
        if (!CommonUtils.validateStrings("login", userName, password)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        userName = userName.trim();
        password = password.trim();
        if (!ExpressionValidateUtil.validUserName(userName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME_DESC);
            return;
        }
        if (!ExpressionValidateUtil.validPassword(password)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PASSWORD,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PASSWORD_DESC);
            return;
        }
        RequestProcessor.imLogin(JMessage.mContext, userName, password, CommonUtils.getSeqID(), callback);

    }

    /**
     * 获取用户信息。默认获取当前应用appkey下的用户信息。
     *
     * @param username 开发者注册的用户名。
     * @param callback 回调接口
     */
    public static void getUserInfo(String username, GetUserInfoCallback callback) {
        getUserInfo(username, JCoreInterface.getAppKey(), callback);
    }

    /**
     * 获取用户信息，此接口可用来获取不同appkey下用户的信息,如果appkey为空，则默认获取当前appkey下的用户信息。
     *
     * @param username 开发者注册的用户名
     * @param appkey   指定的appkey
     * @param callback 回调接口
     */
    public static void getUserInfo(String username, String appkey, GetUserInfoCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getUserInfo", callback)) {
            return;
        }
        if (!ExpressionValidateUtil.validUserName(username)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME_DESC);
            return;
        }
        if (TextUtils.isEmpty(JCoreInterface.getAppKey())) {
            Logger.d(TAG, "JPush appkey is null. jpush have not inited yet.");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT,
                    ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT_DESC);
            return;
        }

        if (TextUtils.isEmpty(appkey)) {
            appkey = JCoreInterface.getAppKey();
        }

        if (!IMConfigs.getNetworkConnected()) {
            InternalUserInfo userInfo = UserInfoManager.getInstance().getUserInfo(username, appkey);
            if (null == userInfo) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
            } else {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfo);
            }
            return;
        }
        new GetUserInfoTask(username, appkey, callback, true, false).execute();
    }

    /**
     * 用户登出接口，调用后用户将无法收到消息。登出动作必定成功，开发者可以不需要关心结果回调。
     */
    public static void logout() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("logout", null)) {
            return;
        }
        String userName = IMConfigs.getUserName();
        if (null != userName) {
            RequestProcessor.imLogout(JMessage.mContext, userName, CommonUtils.getSeqID());
        }
    }

    /**
     * 判断输入的字符串是否与当前用户的密码匹配
     *
     * @param password 被匹配的字符串
     * @return 正确匹配返回true，其他情况返回false
     */
    public static boolean isCurrentUserPasswordValid(String password) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("isCurrentUserPasswordValid", null)) {
            return false;
        }
        if (!CommonUtils.validateStrings("isCurrentUserPasswordValid", password)) {
            Logger.ee(TAG, "[isCurrentUserPasswordValid] 参数不合法");
            return false;
        }
        return ExpressionValidateUtil.validCurrentUserPassword(password);
    }

    /**
     * 更新密码
     *
     * @param oldPassword 当前用户原密码
     * @param newPassword 当前用户新密码
     * @param callback    回调接口
     */
    public static void updateUserPassword(String oldPassword, String newPassword,
                                          BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateUserPassword", callback)) {
            return;
        }
        if (!CommonUtils.validateStrings("updateUserPassword", oldPassword, newPassword)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        oldPassword = oldPassword.trim();
        newPassword = newPassword.trim();
        if (!ExpressionValidateUtil.validPassword(newPassword)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PASSWORD,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PASSWORD_DESC);
            return;
        }
        UpdatePasswordTask task = new UpdatePasswordTask(oldPassword, newPassword,
                IMConfigs.getUserID(), callback, false);
        task.execute();
    }


    /**
     * 更新当前登录的用户的用户信息
     *
     * @param updateField 需要更新的字段名
     * @param userInfo    当前用户的UserInfo对象
     * @param callback    回调对象
     */
    public static void updateMyInfo(UserInfo.Field updateField, UserInfo userInfo, BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateMyInfo", callback)) {
            return;
        }
        if (null == updateField || null == userInfo) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        long userID = IMConfigs.getUserID();
        Map<String, Object> values = new HashMap<String, Object>();
        boolean isUpdateAll = updateField == UserInfo.Field.all;

        if (isUpdateAll || updateField == UserInfo.Field.address) {
            String address = userInfo.getAddress();
            if (!ExpressionValidateUtil.validOthers(address)) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                        ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
                return;
            }
            values.put(UserInfo.Field.address.toString(), address);
        }

        if (isUpdateAll || updateField == UserInfo.Field.birthday) {
            values.put(UserInfo.Field.birthday.toString(), ((InternalUserInfo) userInfo).getBirthdayString());
        }

        if (isUpdateAll || updateField == UserInfo.Field.gender) {
            InternalUserInfo.Gender gender = userInfo.getGender();
            if (null == gender) {
                Logger.ee(TAG, "[updateUserInfo] update userInfo failed,gender is null.");
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
                return;
            }
            //性别需要将gender转成相应的ordinal才能更新到服务器
            values.put(UserInfo.Field.gender.toString(), gender.ordinal());
        }

        if (isUpdateAll || updateField == UserInfo.Field.nickname) {
            String nickname = userInfo.getNickname();
            if (!ExpressionValidateUtil.validOtherNames(nickname)) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME,
                        ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME_DESC);
                return;
            }
            values.put(UserInfo.Field.nickname.toString(), nickname);
        }

        if (isUpdateAll || updateField == UserInfo.Field.region) {
            String region = userInfo.getRegion();
            if (!ExpressionValidateUtil.validOthers(region)) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                        ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
                return;
            }
            values.put(UserInfo.Field.region.toString(), region);
        }

        if (isUpdateAll || updateField == UserInfo.Field.signature) {
            String signature = userInfo.getSignature();
            if (!ExpressionValidateUtil.validOthers(signature)) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                        ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
                return;
            }
            values.put(UserInfo.Field.signature.toString(), signature);
        }

        if (isUpdateAll || updateField == UserInfo.Field.extras) {
            Map<String, String> extras = userInfo.getExtras();
            if (!ExpressionValidateUtil.validExtras(extras)) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                        ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
                return;
            }
            values.put(UserInfo.Field.extras.toString(), extras);
        }
        new UpdateUserInfoTask(userID, values, isUpdateAll, callback, false).execute();

    }


    /**
     * 更新用户头像，建议用户在上传头像前先对头像先进行压缩，否则在调用{@link UserInfo#getBigAvatarBitmap(GetAvatarBitmapCallback)}
     * 接口拿头像的原图时，有可能会抛出OOM异常。
     *
     * @param avatar   头像文件
     * @param callback 回调对象
     */
    public static void updateUserAvatar(final File avatar, final BasicCallback callback) {
        updateUserAvatar(avatar, null, callback);
    }

    /**
     * 更新用户头像，建议用户在上传头像前先对头像先进行压缩，否则在调用{@link UserInfo#getBigAvatarBitmap(GetAvatarBitmapCallback)}
     * 接口拿头像的原图时，有可能会抛出OOM异常。
     * <p>
     * 此接口可以指定头像文件在后台存储时的扩展名，如果填空或者不填，则后台存储文件时将没有扩展名。
     *
     * @param avatar   头像文件
     * @param format   文件扩展名，注意名称中不要包括"."
     * @param callback 回调对象
     * @since 2.2.1
     */
    public static void updateUserAvatar(final File avatar, final String format, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateUserAvatar", callback)) {
            return;
        }
        if (null == avatar || !avatar.exists()) {
            Logger.ee(TAG, "avatar file not exists");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        final long userID = IMConfigs.getUserID();
        new FileUploader().doUploadAvatar(avatar, format, new FileUploader.UploadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, final String mediaID) {
                if (responseCode == 0) {
                    Map<String, Object> values = new HashMap<String, Object>();
                    values.put(UserInfoStorage.KEY_AVATAR, mediaID);
                    new UpdateUserInfoTask(userID, values, false, new BasicCallback(false) {
                        @Override
                        public void gotResult(int responseCode, String msg) {
                            if (0 == responseCode) {
                                UserInfoManager.getInstance().updateAvatar(userID, mediaID);
                                //将头像文件拷贝到应用内部目录下
                                try {
                                    FileUtil.copyFileUsingStream(avatar, new File(FileUtil.getBigAvatarFilePath(mediaID)));
                                } catch (IOException e) {
                                    Logger.d(TAG, "copy avatar to app file path failed.", e);
                                }
                            }
                            CommonUtils.doCompleteCallBackToUser(callback, responseCode, msg);
                        }
                    }, false).execute();
                } else {
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, "upload avatar failed!");
                }
            }
        });
    }

    /**
     * 创建一条单聊文本消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username 聊天对象用户名
     * @param text     文本内容
     * @return 消息对象
     */
    public static Message createSingleTextMessage(String username, String text) {
        return createMessage(ConversationType.single, username, JCoreInterface.getAppKey(), new TextContent(text));
    }

    /**
     * 创建一条单聊文本消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username 聊天对象用户名
     * @param appKey   聊天对象所属应用的appKey
     * @param text     文本内容
     * @return 消息对象
     */
    public static Message createSingleTextMessage(String username, String appKey, String text) {
        return createMessage(ConversationType.single, username, appKey, new TextContent(text));
    }

    /**
     * 创建一条群聊文本信息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param groupID 群组的groupID
     * @param text    文本内容
     * @return 消息对象
     */
    public static Message createGroupTextMessage(long groupID, String text) {
        return createMessage(ConversationType.group, String.valueOf(groupID), JCoreInterface.getAppKey(), new TextContent(text));
    }

    /**
     * 创建一条单聊图片信息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username  聊天对象的用户名
     * @param imageFile 图片文件
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public static Message createSingleImageMessage(String username, File imageFile) throws FileNotFoundException {
        return createMessage(ConversationType.single, username, JCoreInterface.getAppKey(), new ImageContent(imageFile));
    }

    /**
     * 创建一条单聊图片信息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username  聊天对象的用户名
     * @param appKey    聊天对象所属应用的appKey
     * @param imageFile 图片文件
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public static Message createSingleImageMessage(String username, String appKey, File imageFile) throws FileNotFoundException {
        return createMessage(ConversationType.single, username, appKey, new ImageContent(imageFile));
    }

    /**
     * 创建一条群聊图片信息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param groupID   群组的groupID
     * @param imageFile 图片文件
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public static Message createGroupImageMessage(long groupID, File imageFile) throws FileNotFoundException {
        return createMessage(ConversationType.group, String.valueOf(groupID), JCoreInterface.getAppKey(), new ImageContent(imageFile));
    }

    /**
     * 创建一条单聊语音信息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username  聊天对象的用户名
     * @param voiceFile 语音文件
     * @param duration  语音文件时长
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public static Message createSingleVoiceMessage(String username, File voiceFile, int duration) throws FileNotFoundException {
        return createMessage(ConversationType.single, username, JCoreInterface.getAppKey(), new VoiceContent(voiceFile, duration));
    }

    /**
     * 创建一条单聊语音信息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username  聊天对象的用户名
     * @param appKey    聊天对象所属应用的appKey
     * @param voiceFile 语音文件
     * @param duration  语音文件时长
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public static Message createSingleVoiceMessage(String username, String appKey, File voiceFile, int duration) throws FileNotFoundException {
        return createMessage(ConversationType.single, username, appKey, new VoiceContent(voiceFile, duration));
    }

    /**
     * 创建一条群聊语音信息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param groupID   群组groupID
     * @param voiceFile 语音文件
     * @param duration  语音文件时长
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public static Message createGroupVoiceMessage(long groupID, File voiceFile, int duration) throws FileNotFoundException {
        return createMessage(ConversationType.group, String.valueOf(groupID), JCoreInterface.getAppKey(), new VoiceContent(voiceFile, duration));
    }

    /**
     * 创建一条单聊file消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param userName 聊天对象的用户名
     * @param appKey   聊天对象所属应用的appKey
     * @param file     发送的文件
     * @param fileName 指定发送的文件名称,如果不填或为空，则默认使用文件原名。
     * @return 消息对象
     * @throws FileNotFoundException
     * @since 1.4.0
     */
    public static Message createSingleFileMessage(String userName, String appKey, File file, String fileName) throws FileNotFoundException, JMFileSizeExceedException {
        return createMessage(ConversationType.single, userName, appKey, new FileContent(file, fileName));
    }

    /**
     * 创建一条群聊file消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param groupID  群组groupID
     * @param file     发送的文件
     * @param fileName 指定发送的文件名称,如果不填或为空，则默认使用文件原名。
     * @return 消息对象
     * @throws FileNotFoundException
     * @since 1.4.0
     */
    public static Message createGroupFileMessage(long groupID, File file, String fileName) throws FileNotFoundException, JMFileSizeExceedException {
        return createMessage(ConversationType.group, String.valueOf(groupID), JCoreInterface.getAppKey(), new FileContent(file, fileName));
    }

    /**
     * 创建一条单聊地理位置消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username  聊天对象的用户名
     * @param appKey    聊天对象所属应用的appKey
     * @param latitude  纬度信息
     * @param longitude 经度信息
     * @param scale     地图缩放比例
     * @param address   详细地址信息
     * @return 消息对象
     * @since 1.4.0
     */
    public static Message createSingleLocationMessage(String username, String appKey, double latitude, double longitude, int scale, String address) {
        return createMessage(ConversationType.single, username, appKey, new LocationContent(latitude, longitude, scale, address));
    }

    /**
     * 创建一条群聊地理位置消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param groupId   群组groupID
     * @param latitude  纬度信息
     * @param longitude 经度信息
     * @param scale     地图缩放比例
     * @param address   详细地址信息
     * @return 消息对象
     * @since 1.4.0
     */

    public static Message createGroupLocationMessage(long groupId, double latitude, double longitude, int scale, String address) {
        return createMessage(ConversationType.group, String.valueOf(groupId), JCoreInterface.getAppKey(), new LocationContent(latitude, longitude, scale, address));
    }

    /**
     * 创建一条单聊自定义消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username  聊天对象username
     * @param valuesMap 包含自定义键值对的map.
     * @return 消息对象
     */
    public static Message createSingleCustomMessage(String username, Map<? extends String, ? extends String> valuesMap) {
        CustomContent customContent = new CustomContent();
        customContent.setAllValues(valuesMap);
        return createMessage(ConversationType.single, username, JCoreInterface.getAppKey(), customContent);
    }

    /**
     * 创建一条单聊自定义消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param username  聊天对象username
     * @param appKey    聊天对象所属应用的appKey
     * @param valuesMap 包含自定义键值对的map.
     * @return 消息对象
     */
    public static Message createSingleCustomMessage(String username, String appKey, Map<? extends String, ? extends String> valuesMap) {
        CustomContent customContent = new CustomContent();
        customContent.setAllValues(valuesMap);
        return createMessage(ConversationType.single, username, appKey, customContent);
    }

    /**
     * 创建一条群聊自定义消息，此方法是创建message的快捷接口，对于不需要关注会话实例的开发者可以使用此方法
     * 快捷的创建一条消息。其他的情况下推荐使用{@link Conversation#createSendMessage(MessageContent)}
     * 接口来创建消息
     *
     * @param groupID   群组groupID
     * @param valuesMap 包含了自定义键值对的map
     * @return 消息对象
     */
    public static Message createGroupCustomMessage(long groupID, Map<? extends String, ? extends String> valuesMap) {
        CustomContent customContent = new CustomContent();
        customContent.setAllValues(valuesMap);
        return createMessage(ConversationType.group, String.valueOf(groupID), JCoreInterface.getAppKey(), customContent);
    }

    /**
     * 创建一条@群组中user的消息,此方法是创建@群成员message的快捷接口,对于不需要关注会话实例的开发者可以使用此方法;
     * 如果想要关注会话实例或需要自定义fromName的开发者请通过{@link Conversation#createSendMessage(MessageContent, List, String)}
     * 接口来创建消息.
     *
     * @param groupID 群组groupID
     * @param atList  将要@的群成员的UserInfo List
     * @param content 创建的消息体
     * @return 消息对象
     * @since 2.1.0
     */
    public static Message createAtGroupMembersMessage(long groupID, List<UserInfo> atList, MessageContent content) {
        return createMessage(ConversationType.group, String.valueOf(groupID), JCoreInterface.getAppKey(), content, atList);
    }

    private static Message createMessage(ConversationType convType, String targetID, String appkey, MessageContent content, List<UserInfo> userInfos) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("createMessage", null)) {
            Logger.ee(TAG, "Create message failed ! SDK have not inited or have not login");
            return null;
        }

        if (null == convType || null == targetID || null == content) {
            Logger.ee(TAG, "Create message failed ! invalid parameter. - convType = " + convType +
                    " targetID = " + targetID + " content = " + content);
            return null;
        }

        Conversation conversation = ConversationManager.getInstance().getConversation(convType, targetID, appkey);
        if (null == conversation) {
            if (convType == ConversationType.single) {
                conversation = Conversation.createSingleConversation(targetID, appkey);
            } else {
                conversation = Conversation.createGroupConversation(Long.parseLong(targetID));
            }
        }
        return userInfos == null ? conversation.createSendMessage(content) : conversation.createSendMessage(content, userInfos, null);
    }

    private static Message createMessage(ConversationType convType, String targetID, String appkey, MessageContent content) {
        return createMessage(convType, targetID, appkey, content, null);
    }

    /**
     * 发送消息，使用默认发送配置参数
     *
     * @param message 消息对象
     */
    public static void sendMessage(final Message message,boolean isUserPower) {
        sendMessage(message, new MessageSendingOptions(),isUserPower);
    }

    /**
     * 发送消息.并且可以使用{@link MessageSendingOptions}对发送的一些发送参数做配置。
     * 注意这些配置仅对本次消息发送生效。
     *
     * @param message 消息对象
     * @param options 消息发送时的控制选项。
     * @since 2.2.0
     */
    public static void sendMessage(Message message, MessageSendingOptions options,boolean isUserPower) {

        if (null == message) {
            Logger.ee(TAG, "[sendMessage] message should not be null");
            return;
        }
        ConversationType conversationType = message.getTargetType();
        final InternalMessage internalMessage = (InternalMessage) message;
        final String targetID = message.getTargetID();
        final String targetAppkey = internalMessage.getTargetAppKey();
        final int msgID = message.getId();
        InternalConversation conv = ConversationManager.getInstance().getConversation(conversationType,
                targetID, targetAppkey);
        if (null == conv) {
            Logger.ww(TAG, "send message failed. conversation is null");
            CommonUtils.doMessageCompleteCallbackToUser(targetID, targetAppkey, msgID,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        if (!CommonUtils.isInited("sendMessage")) {
            conv.updateMessageStatus(message, MessageStatus.send_fail);
            CommonUtils.doMessageCompleteCallbackToUser(targetID, targetAppkey, msgID,
                    ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT, ErrorCode.LOCAL_ERROR.LOCAL_HAVE_NOT_INIT_DESC);
            return;
        }
        if (!IMConfigs.getNetworkConnected()) {
            conv.updateMessageStatus(message, MessageStatus.send_fail);
            CommonUtils.doMessageCompleteCallbackToUser(targetID, targetAppkey, msgID,
                    ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
            return;
        }
        if (!CommonUtils.isLogin("sendMessage")) {
            conv.updateMessageStatus(message, MessageStatus.send_fail);
            CommonUtils.doMessageCompleteCallbackToUser(targetID, targetAppkey, msgID,
                    ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        if (!ExpressionValidateUtil.validMessageLength(message)) {
            conv.updateMessageStatus(message, MessageStatus.send_fail);
            CommonUtils.doMessageCompleteCallbackToUser(targetID, targetAppkey, msgID,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_MESSAGE_CONTENT_LENGTH, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_MESSAGE_CONTENT_LENGTH_DESC);
            return;
        }
        internalMessage.send(options);
    }

    /**
     * 转发消息。
     *
     * @param message  需要转发的消息对象
     * @param conv     目标会话
     * @param options  消息转发时的控制选项。仅对此次发送生效
     * @param callback 回调函数
     * @since 2.3.0
     */
    public static void forwardMessage(Message message, Conversation conv, MessageSendingOptions options, BasicCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("forwardMessage", callback)) {
            return;
        }

        if (null == message || null == conv) {
            Logger.ee(TAG, "[forwardMessage] forward message failed ! invalid parameter.- message = " + message +
                    "conversation = " + conv);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }

        if (message.getStatus() != MessageStatus.send_success && !MessageStatus.isReceiveStatus(message.getStatus())) {
            Logger.ee(TAG, "[forwardMessage] message status is not send_success or receive status");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_CREATE_FORWARD_MESSAGE_ERROR,
                    ErrorCode.LOCAL_ERROR.LOCAL_CREATE_FORWARD_MESSAGE_ERROR_DESC);
            return;
        }

        MessageContent content = message.getContent();
        if (content instanceof MediaContent) {
            ((MediaContent) content).setFileUploaded(true);
        }
        Message forwardMessage = conv.createSendMessage(content);

        if (null == forwardMessage) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_CREATE_FORWARD_MESSAGE_ERROR,
                    ErrorCode.LOCAL_ERROR.LOCAL_CREATE_FORWARD_MESSAGE_ERROR_DESC);
            return;
        }

        forwardMessage.setOnSendCompleteCallback(callback);
        sendMessage(forwardMessage, null == options ? new MessageSendingOptions() : options,true);
    }

    /**
     * 发送消息透传给个人。
     * 消息不会进入到后台的离线存储中去，仅当对方用户当前在线时，透传消息才会成功送达。
     * 透传命令送达时，接收方会收到一个{@link CommandNotificationEvent}事件通知。
     * sdk不会将此类透传消息内容本地化。
     *
     * @param username 目标的用户名
     * @param appKey   目标的appKey, 如果传入null或空字符串，则默认用本应用的appKey
     * @param msg      发送的消息内容
     * @param callback 回调函数
     * @since 2.3.0
     */
    public static void sendSingleTransCommand(final String username, String appKey, final String msg, final BasicCallback callback) {
        if (!ExpressionValidateUtil.validUserName(username)) {
            Logger.ee(TAG, "[sendSingleTransCommand] sendSingleTransCommand failed ! invalid parameter.- username = " + username);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }
        if (TextUtils.isEmpty(appKey)) {
            appKey = JCoreInterface.getAppKey();
        }
        UserIDHelper.getUserID(username, appKey, new UserIDHelper.GetUseridsCallback() {
            @Override
            public void gotResult(int code, String responseMsg, List<Long> userids) {
                if (code == 0) {
                    long targetID = userids.get(0);
                    RequestProcessor.imTransCommandSend(JMessage.mContext, targetID, CommandNotificationEvent.Type.single,
                            msg, CommonUtils.getSeqID(), callback);
                } else {
                    Logger.ee(TAG, "[sendSingleTransCommand] sendSingleTransCommand failed ! userID not found. code = " +
                            code + "desc = " + responseMsg);
                    CommonUtils.doCompleteCallBackToUser(callback, code, responseMsg);
                }
            }
        });
    }

    /**
     * 发送消息透传给群。
     * 消息不会进入到后台的离线存储中去，仅当对方用户当前在线时，透传消息才会成功送达。
     * 透传命令送达时，接收方会收到一个{@link CommandNotificationEvent}事件通知。
     * sdk不会将此类透传消息内容本地化。
     *
     * @param gid      群组的gid
     * @param msg      发送的消息内容
     * @param callback 回调函数
     * @since 2.3.0
     */
    public static void sendGroupTransCommand(long gid, String msg, BasicCallback callback) {
        RequestProcessor.imTransCommandSend(JMessage.mContext, gid, CommandNotificationEvent.Type.group,
                msg, CommonUtils.getSeqID(), callback);
    }

    /**
     * 从本地数据库中获取会话列表，默认按照会话的最后一条消息的时间，降序排列
     *
     * @return 返回当前用户的会话列表，没有会话则返回空的列表
     */
    public static List<Conversation> getConversationList() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getConversationList", null)) {
            return null;
        }
        List<Conversation> conversationList = new ArrayList<Conversation>();
        conversationList.addAll(ConversationManager.getInstance().getAllConversation(true));
        return conversationList;
    }

    /**
     * 从本地数据库中获取会话列表,默认不排序。
     *
     * @return 返回当前用户的会话列表，没有会话则返回空的列表
     * @since 1.4.0
     */
    public static List<Conversation> getConversationListByDefault() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getConversationListByDefault", null)) {
            return null;
        }
        List<Conversation> conversationList = new ArrayList<Conversation>();
        conversationList.addAll(ConversationManager.getInstance().getAllConversation(false));
        return conversationList;
    }

    /**
     * 获取单聊会话信息，默认获取本appkey下username的单聊会话。
     *
     * @param username 对象的userName
     * @return 返回会话对象，若不存在和指定对象的会话则返回null
     */
    public static Conversation getSingleConversation(String username) {
        return ConversationManager.getInstance().getSingleConversation(username, JCoreInterface.getAppKey());
    }

    /**
     * 获取与指定appkey下username的单聊会话信息,如果appkey为空则默认取本应用appkey下对应username的会话。
     *
     * @param username 用户的username
     * @param appkey   用户所属应用的appkey
     * @return 返回会话对象，若不存在和指定对象的会话则返回null
     */
    public static Conversation getSingleConversation(String username, String appkey) {
        return ConversationManager.getInstance().getSingleConversation(username, appkey);
    }

    /**
     * 获取群组会话信息
     *
     * @param groupID 群组的groupID
     * @return 返回会话信息，若不存在和指定对象的会话则返回null
     */
    public static Conversation getGroupConversation(long groupID) {
        return ConversationManager.getInstance().getGroupConversation(groupID);
    }

    /**
     * 删除单聊的会话，同时删除掉本地聊天记录。默认删除本appkey下username的会话
     *
     * @param userName 用户的username
     * @return 删除成功返回true, 否则返回false
     */
    public static boolean deleteSingleConversation(String userName) {
        return ConversationManager.getInstance().deleteSingleConversation(userName, JCoreInterface.getAppKey());
    }

    /**
     * 删除与指定appkey下username的单聊的会话，同时删除掉本地聊天记录。,如果appkey为空则默认尝试删除
     * 本应用appkey下对应username的会话。
     *
     * @param username 用户的username
     * @param appkey   用户所属应用的appkey
     * @return 删除成功返回true, 否则返回false
     */
    public static boolean deleteSingleConversation(String username, String appkey) {
        if (TextUtils.isEmpty(appkey)) {
            appkey = JCoreInterface.getAppKey();
        }
        return ConversationManager.getInstance().deleteSingleConversation(username, appkey);
    }

    /**
     * 删除群聊的会话，同时删除掉本地聊天记录
     *
     * @param groupID 对象的userName
     * @return 删除成功返回true, 否则返回false
     */
    public static boolean deleteGroupConversation(long groupID) {
        return ConversationManager.getInstance().deleteGroupConversation(groupID);
    }

    /**
     * 获取当前的用户信息
     *
     * @return 登录成功则返回用户信息，已登出或未登录则对应用户信息为null
     */
    public static UserInfo getMyInfo() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getMyInfo", null)) {
            return null;
        }

        long userID = IMConfigs.getUserID();
        InternalUserInfo userInfo = UserInfoManager.getInstance().getUserInfo(userID);
        if (null == userInfo) {
            userInfo = new InternalUserInfo();
            //还没有拿到完整的userInfo,先从config中拿到基本信息返回给前端。
            userInfo.setUserID(userID);
            userInfo.setUserName(IMConfigs.getUserName());
            userInfo.setAppkey(JCoreInterface.getAppKey());
        }
        return (UserInfo) userInfo.clone();
    }

    /**
     * 设置通知的展示类型
     *
     * @param mode 通知展示类型，包括：<br/>
     *             {@link #NOTI_MODE_NO_NOTIFICATION} 不展示通知  <br/>
     *             {@link #NOTI_MODE_DEFAULT} 展示通知，有声音有震动。<br/>
     *             {@link #NOTI_MODE_NO_SOUND} 展示通知，无声音有震动<br/>
     *             {@link #NOTI_MODE_NO_VIBRATE} 展示通知，有声音无震动<br/>
     *             {@link #NOTI_MODE_SILENCE} 展示通知，无声音无震动
     * @deprecated deprecated in 2.2.0 use {@link JMessageClient#setNotificationFlag(int)} instead
     */
    public static void setNotificationMode(int mode) {
        if (!CommonUtils.isInited("setNotificationMode")) {
            return;
        }

        int notiFlag;
        switch (mode) {
            case NOTI_MODE_DEFAULT:
                notiFlag = JMessageClient.FLAG_NOTIFY_DEFAULT;
                break;
            case NOTI_MODE_NO_NOTIFICATION:
                notiFlag = JMessageClient.FLAG_NOTIFY_DISABLE;
                break;
            case NOTI_MODE_NO_SOUND:
                notiFlag = JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_VIBRATE;
                break;
            case NOTI_MODE_NO_VIBRATE:
                notiFlag = JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_SOUND;
                break;
            case NOTI_MODE_SILENCE:
                notiFlag = JMessageClient.FLAG_NOTIFY_WITH_LED;
                break;
            default:
                notiFlag = JMessageClient.FLAG_NOTIFY_DEFAULT;
        }
        IMConfigs.setNotificationFlag(notiFlag);
    }

    /**
     * 设置通知的展示类型，默认所有设置都会打开
     *
     * @param flag notification flag,包括
     *             {@link JMessageClient#FLAG_NOTIFY_WITH_SOUND},
     *             {@link JMessageClient#FLAG_NOTIFY_WITH_LED},
     *             {@link JMessageClient#FLAG_NOTIFY_WITH_VIBRATE}等.
     *             支持 '|' 符号联结各个参数
     * @since 2.2.0
     */
    public static void setNotificationFlag(int flag) {
        if (!CommonUtils.isInited("setNotificationMode")) {
            return;
        }
        IMConfigs.setNotificationFlag(flag);
    }

    /**
     * 获取当前通知栏的展示类型。
     *
     * @return 通知栏展示类型
     */
    public static int getNotificationFlag() {
        if (!CommonUtils.isInited("getNotificationMode")) {
            return FLAG_NOTIFY_DEFAULT;
        }
        return IMConfigs.getNotificationFlag();
    }

    /**
     * 在进入聊天会话界面时调用，设置当前正在聊天的对象，sdk用来判断notification是否需要展示，默认进入的
     * 是本应用appkey下用户的会话。此接口传入的数据采用覆盖逻辑，后面传入的参数会覆盖掉之前的设置。
     *
     * @param username 聊天对象的username
     * @deprecated deprecated in 1.2.0 use{@link JMessageClient#enterSingleConversation(String, String)}
     * instead.
     */
    @Deprecated
    public static void enterSingleConversaion(String username) {
        enterConversation(ConversationType.single, username, JCoreInterface.getAppKey());
    }

    /**
     * 在进入聊天会话界面时调用，设置当前正在聊天的对象，sdk用来判断notification是否需要展示，默认进入的
     * 是本应用appkey下用户的会话。此接口传入的数据采用覆盖逻辑，后面传入的参数会覆盖掉之前的设置。
     *
     * @param username 聊天对象的username
     */
    public static void enterSingleConversation(String username) {
        enterConversation(ConversationType.single, username, JCoreInterface.getAppKey());
    }

    /**
     * 在进入聊天会话界面时调用，设置当前正在聊天的对象，sdk用来判断notification是否需要展示。若appkey为空
     * 则默认填充本应用的appkey。此接口传入的数据采用覆盖逻辑，后面传入的参数会覆盖掉之前的设置。
     *
     * @param username 聊天对象的username
     * @param appkey   对象所属appkey
     */
    public static void enterSingleConversation(String username, String appkey) {
        enterConversation(ConversationType.single, username, appkey);
    }

    /**
     * 推荐在进入聊天会话界面时调用，设置当前正在聊天的对象，用于判断notification是否需要展示
     * 此接口传入的数据采用覆盖逻辑，后面传入的参数会覆盖掉之前的设置。
     *
     * @param groupID 聊天群组的groupID
     */
    public static void enterGroupConversation(long groupID) {
        enterConversation(ConversationType.group, String.valueOf(groupID), "");
    }

    private static void enterConversation(ConversationType type, String targetID, String appkey) {
        if (!CommonUtils.isInited("enterConversation") || null == type || null == targetID) {
            Logger.ee(TAG, "enterConversation failed. type = " + type + " targetID = " + targetID);
            return;
        }

        if (ConversationType.single == type && TextUtils.isEmpty(appkey)) {
            appkey = JCoreInterface.getAppKey();
        }
        InternalConversation conversation = ConversationManager.getInstance().getConversation(type, targetID, appkey);
        if (null != conversation) {
            conversation.resetUnreadCount();
        }
        ChatMsgManager.getInstance().cancelNotification(targetID, appkey);
        if (ConversationType.single == type) {
            JMessage.sChattingTarget = targetID + appkey;
        } else {
            JMessage.sChattingTarget = targetID;
        }
    }

    /**
     * 推荐在退出聊天会话界面时调用，清除当前正在聊天的对象，用于判断notification是否需要展示
     *
     * @deprecated deprecated in 1.2.0. use {@link JMessageClient#exitConversation()} instead.
     */
    @Deprecated
    public static void exitConversaion() {
        exitConversation();
    }

    /**
     * 推荐在退出聊天会话界面时调用，清除当前正在聊天的对象，用于判断notification是否需要展示
     */
    public static void exitConversation() {
        JMessage.sChattingTarget = "";
    }

    /**
     * 创建群组，群组创建成功后，创建者会默认包含在群成员中。
     *
     * @param groupName 群组名称
     * @param groupDesc 群组描述
     * @param callback  回调接口
     */
    public static void createGroup(String groupName, String groupDesc,
                                   CreateGroupCallback callback) {
        if (!CommonUtils.doInitialCheck("createGroup", callback)) {
            return;
        }
        //暂时写死flag 和 level
        int testFlag = 1;
        int groupLevel = 3;

        RequestProcessor.imCreateGroup(JMessage.mContext, groupName, groupDesc, groupLevel, testFlag, null,
                CommonUtils.getSeqID(), callback);
    }

    /**
     * 创建群组，群组创建成功后，创建者会默认包含在群成员中。
     * 使用此接口创建群组时可以指定群头像,并且可以指定头像文件在后台存储时的扩展名，如果填空或者不填，则后台存储文件时将没有扩展名。
     *
     * @param groupName       群组名称
     * @param groupDesc       群组描述
     * @param groupAvatarFile 群组头像文件
     * @param format          头像文件扩展名，注意名称中不要包括"."
     * @param callback        回调接口
     * @since 2.3.0
     */
    public static void createGroup(final String groupName, final String groupDesc, final File groupAvatarFile, String format,
                                   final CreateGroupCallback callback) {
        if (!CommonUtils.doInitialCheck("createGroup", callback)) {
            return;
        }

        if (null == groupAvatarFile || !groupAvatarFile.exists()) {
            Logger.ee(TAG, "avatar file not exists");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }

        new FileUploader().doUploadAvatar(groupAvatarFile, format, new FileUploader.UploadAvatarCallback(false) {
            @Override
            public void gotResult(int responseCode, String responseMsg, String mediaID) {
                if (ErrorCode.NO_ERROR == responseCode) {
                    //暂时写死flag 和 level
                    int testFlag = 1;
                    int groupLevel = 3;
                    //将头像文件拷贝到应用内部目录下
                    try {
                        FileUtil.copyFileUsingStream(groupAvatarFile, new File(FileUtil.getBigAvatarFilePath(mediaID)));
                    } catch (IOException e) {
                        Logger.d(TAG, "copy avatar to app file path failed.", e);
                    }
                    RequestProcessor.imCreateGroup(JMessage.mContext, groupName, groupDesc, groupLevel, testFlag, mediaID,
                            CommonUtils.getSeqID(), callback);
                } else {
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
                }
            }
        });
    }

    /**
     * 向群组中添加成员。本方法所有传入的username,会默认在本应用下查找,
     * 若要跨应用添加其他用户请使用{@link JMessageClient#addGroupMembers(long, String, List, BasicCallback)}
     *
     * @param groupID      群组的groupID
     * @param userNameList 添加进群组的成员username集合
     * @param callback     回调接口
     */
    public static void addGroupMembers(final long groupID, final List<String> userNameList,
                                       final BasicCallback callback) {
        addGroupMembers(groupID, JCoreInterface.getAppKey(), userNameList, callback);
    }

    /**
     * 向群组中添加成员,通过指定appKey可以实现跨应用添加其他appKey下用户进群组
     *
     * @param groupID      群组的groupID
     * @param appKey       指定的appKey,如果为空则在本应用appKey下查找用户
     * @param userNameList 添加进群组的成员username集合
     * @param callback     回调接口
     */
    public static void addGroupMembers(final long groupID, final String appKey, final List<String> userNameList,
                                       final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("addGroupMembers", callback)) {
            return;
        }
        if (!CommonUtils.validateObjects("addGroupMembers", userNameList)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserIDHelper.getUserIDs(userNameList, appKey, new UserIDHelper.GetUseridsCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<Long> userids) {
                        if (userids == null) {
                            CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                        } else {
                            RequestProcessor.imAddGroupMember(JMessage.mContext, groupID, userids, CommonUtils.getSeqID(), callback);
                        }
                    }
                });
                return null;
            }
        });
    }

    /**
     * 踢出群组成员。本方法所有传入的username,都默认在本应用下查找,
     * 若要跨应用踢出其他appKey下的群成员请使用{@link JMessageClient#removeGroupMembers(long, String, List, BasicCallback)}
     *
     * @param groupID      群组的groupID
     * @param userNameList 踢出群组的成员username集合
     * @param callback     回调接口
     */
    public static void removeGroupMembers(final long groupID, final List<String> userNameList,
                                          final BasicCallback callback) {
        removeGroupMembers(groupID, JCoreInterface.getAppKey(), userNameList, callback);
    }

    /**
     * 踢出群组中成员,通过指定appKey可以实现跨应用踢出群组成员
     *
     * @param groupID      群组的groupID
     * @param appKey       指定的appKey,如果appKey为空则在本应用appKey下查找用户
     * @param userNameList 踢出群组成员的username集合
     * @param callback     回调接口
     */
    public static void removeGroupMembers(final long groupID, final String appKey, final List<String> userNameList,
                                          final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("removeGroupMembers", callback)) {
            return;
        }

        if (!CommonUtils.validateObjects("removeGroupMembers", userNameList)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                UserIDHelper.getUserIDs(userNameList, appKey, new UserIDHelper.GetUseridsCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<Long> userids) {
                        if (userids == null) {
                            CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                        } else {
                            RequestProcessor.imDelGroupMember(JMessage.mContext, groupID, userids, CommonUtils.getSeqID(), callback);
                        }
                    }
                });
                return null;
            }
        });
    }

    /**
     * 修改群组名称
     *
     * @param groupID   群组的groupID
     * @param groupName 群名称
     * @param callback  回调接口
     * @deprecated deprecated in 2.3.0,use {@link cn.jpush.im.android.api.model.GroupInfo#updateName(String, BasicCallback)} instead
     */
    public static void updateGroupName(long groupID, String groupName, BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateGroupName", callback)) {
            return;
        }
        if (!CommonUtils.validateStrings("updateGroupName", groupName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        if (!ExpressionValidateUtil.validOtherNames(groupName)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_NAME_DESC);
            return;
        }
        InternalGroupInfo groupInfo = GroupStorage.queryInfoSync(groupID);
        String groupDesc = null == groupInfo ? "" : groupInfo.getGroupDescription();
        RequestProcessor
                .imUpdateGroupInfo(JMessage.mContext, groupID, groupName, groupDesc, null, CommonUtils.getSeqID(), callback);
    }

    /**
     * 修改群组描述
     *
     * @param groupID   群组groupID
     * @param groupDesc 群组描述
     * @param callback  回调接口
     * @deprecated deprecated in 2.3.0,use {@link cn.jpush.im.android.api.model.GroupInfo#updateDescription(String, BasicCallback)} instead
     */
    public static void updateGroupDescription(long groupID, String groupDesc,
                                              BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("updateGroupDescription", callback)) {
            return;
        }
        if (!ExpressionValidateUtil.validOthers(groupDesc)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }
        InternalGroupInfo groupInfo = GroupStorage.queryInfoSync(groupID);
        String groupName = null == groupInfo ? "" : groupInfo.getGroupName();
        RequestProcessor
                .imUpdateGroupInfo(JMessage.mContext, groupID, groupName, groupDesc, null, CommonUtils.getSeqID(), callback);
    }

    /**
     * 退出群组，如果是群主退群，则相关群主权利移交给下一位群成员。如果此时群组中没有其他人，
     * 则群组将会被解散。
     *
     * @param groupId  群组的groupID
     * @param callback 回调接口
     */
    //Todo: instance method
    public static void exitGroup(long groupId, BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("exitGroup", callback)) {
            return;
        }
        RequestProcessor.imExitGroup(JMessage.mContext, groupId, CommonUtils.getSeqID(), callback);
    }

    /**
     * 从服务器获取当前用户所加入的群组的groupID的list，同时会将数据保存到本地数据库
     *
     * @param callback 回调接口
     */
    public static void getGroupIDList(GetGroupIDListCallback callback) {
        if (!CommonUtils.doInitialCheck("getGroupIDList", callback)) {
            return;
        }
        if (!IMConfigs.getNetworkConnected()) {
            int errorCode = ErrorCode.NO_ERROR;
            String desc = ErrorCode.NO_ERROR_DESC;
            List<Long> idList = GroupStorage.queryIDListSync();
            if (null == idList) {
                errorCode = ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED;
                desc = ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC;
            }
            CommonUtils.doCompleteCallBackToUser(callback, errorCode, desc, idList);
        } else {
            GetGroupIDListTask task = new GetGroupIDListTask(IMConfigs.getUserID(), callback, false);
            task.execute();
        }
    }


    /**
     * 从服务器获取指定群的基本信息。
     *
     * @param groupID  群组的groupID
     * @param callback 回调接口
     */
    public static void getGroupInfo(long groupID, GetGroupInfoCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupInfo",
                callback)) {
            return;
        }

        if (!IMConfigs.getNetworkConnected()) {
            //网络断开的情况下，先从本地获取，如果本地不存在再返回错误
            InternalGroupInfo groupInfo = GroupStorage.queryInfoSync(groupID);
            if (null == groupInfo) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
            } else {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, groupInfo);
            }
            return;
        }
        GetGroupInfoTask task = new GetGroupInfoTask(groupID, callback, false);
        task.execute();
    }

    /**
     * 从本地获取群成员username list，如果本地不存在则从服务器获取。
     *
     * @param groupID  群组groupID
     * @param callback 回调接口
     */
    public static void getGroupMembers(long groupID, GetGroupMembersCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMembers",
                callback)) {
            return;
        }

        List<Long> members = GroupStorage.queryMemberUserIdsSync(groupID);
        if (null != members && !members.isEmpty()) {
            List<InternalUserInfo> membersInfo = UserInfoManager.getInstance().getUserInfoList(members);
            if (members.size() != membersInfo.size()) {
                //如果从本地获取到的群成员数量和群成员uid数量不相等，说明本地获取的群成员不完整，需要从后台请求一次。
                Logger.ww(TAG, "group member info not fully acquired. try to get from server");
                new GetGroupMembersTask(groupID, callback, false, false).execute();
            } else {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, membersInfo);
            }
        } else {
            if (!IMConfigs.getNetworkConnected()) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED
                        , ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                return;
            }
            GetGroupMembersTask task = new GetGroupMembersTask(groupID, callback, false, false);
            task.execute();
        }
    }

    /**
     * 注册事件接收者，具体用法参考官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
     * 一节
     *
     * @param receiver 消息接收类对象
     */
    public static void registerEventReceiver(Object receiver) {
        registerEventReceiver(receiver, 0);
    }

    /**
     * 注册事件接收者，具体用法参考官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
     * 一节
     *
     * @param receiver 事件接收类对象
     * @param priority 事件接收者接收事件的优先级，默认值为0，优先级越高将越先接收到事件。（优先级只对同一个线程模式中的接收者有效）
     */
    public static void registerEventReceiver(Object receiver, int priority) {
        if (null != receiver) {
            if (!EventBus.getDefault().isRegistered(receiver)) {
                EventBus.getDefault().register(receiver, priority);
            } else {
                Logger.ww(TAG, "this receiver is already registed ! receiver = " + receiver);
            }
        } else {
            Logger.ee(TAG, "receiver object should not be null!");
        }
    }

    /**
     * 解绑事件接收者,解绑后接收者将不再接收到事件
     *
     * @param receiver 消息接收类对象
     */
    public static void unRegisterEventReceiver(Object receiver) {
        if (null != receiver) {
            EventBus.getDefault().unregister(receiver);
        } else {
            Logger.ee(TAG, "receiver object should not be null!");
        }
    }

    /**
     * 获取被当前用户加入黑名单的用户列表
     *
     * @param callback 回调接口
     */
    public static void getBlacklist(GetBlacklistCallback callback) {
        if (!CommonUtils.doInitialCheck("getBlacklist",
                callback)) {
            return;
        }
        new GetBlackListTask(callback, false).execute();
    }

    /**
     * 赵元超添加 获取用户是否有聊天资格的接口
     */
    public static void getUserStatus(String phone,String bePhone,GetUserStatusCallback callback,boolean waitForCompletion){
        if (!CommonUtils.doInitialCheck("getUserStatus",
                callback)) {
            return;
        }
        new GetUserStatueTask(phone,bePhone,callback,waitForCompletion).execute();
    }

    /**
     * 将用户加入黑名单，对方用户将无法给你发消息。此方法默认在本应用下查找,
     * 若想要跨应用添加其他appKey下用户请使用{@link JMessageClient#addUsersToBlacklist(List, String, BasicCallback)}
     *
     * @param usernames 被加入黑名单的用户username列表
     * @param callback  回调接口
     */
    public static void addUsersToBlacklist(final List<String> usernames, final BasicCallback callback) {
        addUsersToBlacklist(usernames, JCoreInterface.getAppKey(), callback);
    }

    /**
     * 将用户添加进黑名单,对方将无法给你发消息。通过指定appKey可以实现跨应用将用户加入黑名单
     *
     * @param usernames 准备添加进黑名单的username集合
     * @param appKey    指定的appKey,如果appKey为空则在本应用appKey下查找用户
     * @param callback  回调接口
     */
    public static void addUsersToBlacklist(final List<String> usernames, final String appKey, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("addUsersToBlacklist", callback)) {
            return;
        }
        if (!CommonUtils.validateObjects("addUsersToBlacklist", usernames)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UserIDHelper.getUserIDs(usernames, appKey, new UserIDHelper.GetUseridsCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<Long> userids) {
                        if (null != userids) {
                            RequestProcessor.imAddToBlackList(JMessage.mContext, userids, CommonUtils.getSeqID(), callback);
                        } else {
                            CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                        }
                    }
                });
                return null;
            }
        });
    }

    /**
     * 将用户移出黑名单。本方法所有传入的username,默认移除的是本应用下用户,
     * 若想要跨应用移除其他appKey下的用户请使用{@link JMessageClient#delUsersFromBlacklist(List, String, BasicCallback)}
     *
     * @param usernames 被移出黑名单的用户username集合
     * @param callback  回调接口
     */
    public static void delUsersFromBlacklist(final List<String> usernames, final BasicCallback callback) {
        delUsersFromBlacklist(usernames, JCoreInterface.getAppKey(), callback);
    }

    /**
     * 将用户移出黑名单,通过指定appKey可以实现跨应用将用户移出黑名单
     *
     * @param usernames 准备移出黑名单的username集合
     * @param appKey    指定的appKey,如果appKey为空则在本应用appKey下查找用户
     * @param callback  回调接口
     */
    public static void delUsersFromBlacklist(final List<String> usernames, final String appKey, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("delUsersFromBlacklist", callback)) {
            return;
        }
        if (!CommonUtils.validateObjects("delUsersFromBlacklist", usernames)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                UserIDHelper.getUserIDs(usernames, appKey, new UserIDHelper.GetUseridsCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<Long> userids) {
                        if (null != userids) {
                            RequestProcessor.imDelFromBlackList(JMessage.mContext, userids, CommonUtils.getSeqID(), callback);
                        } else {
                            CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                        }
                    }
                });
                return null;
            }
        });
    }

    /**
     * 获取当前用户设置的免打扰名单列表
     *
     * @param callback 回调接口
     */
    public static void getNoDisturblist(GetNoDisurbListCallback callback) {
        if (!CommonUtils.doInitialCheck("getNoDisturblist",
                callback)) {
            return;
        }
        new GetNoDisturbListTask(callback, false).execute();
    }

    /**
     * 获取当前用户设置的屏蔽群组列表
     *
     * @param callback 回调接口
     * @since 2.1.0
     */
    public static void getBlockedGroupsList(GetGroupInfoListCallback callback) {
        if (!CommonUtils.doInitialCheck("getBlockedGroupsList",
                callback)) {
            return;
        }
        new GetBlockedGroupsTask(callback, false).execute();
    }

    /**
     * 设置全局免打扰标识，设置之后用户在所有设备上收到消息时通知栏都不会弹出消息通知。
     *
     * @param noDisturbGlobal 1表示设置，其他表示取消
     * @param callback        回调接口
     */
    public static void setNoDisturbGlobal(int noDisturbGlobal, BasicCallback callback) {
        if (!CommonUtils.doInitialCheck("setNoDisturbGlobal",
                callback)) {
            return;
        }
        RequestProcessor.imSetNoDisturbGlobal(JMessage.mContext, noDisturbGlobal, CommonUtils.getSeqID(), callback);
    }

    /**
     * 获取全局免打扰标识，回调结果中，1表示已设置，其他表示未设置。
     *
     * @param callback 回调接口
     */
    public static void getNoDisturbGlobal(IntegerCallback callback) {
        if (!CommonUtils.doInitialCheck("getNoDisturbGlobal",
                callback)) {
            return;
        }
        int noDisturbGlobal = IMConfigs.getNodisturbGlobal();
        if (-1 == noDisturbGlobal) {
            new GetNoDisturbListTask(callback, false).execute();
            return;
        }
        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, noDisturbGlobal);
    }

    /**
     * 获取未读消息总数
     *
     * @return 未读消息总数
     * @since 2.1.2
     */
    public static int getAllUnReadMsgCount() {
        return JMessage.getAllUnreadMsgCnt();
    }

    /**
     * 获取sdk版本号
     *
     * @return sdk版本号
     */
    public static String getSdkVersionString() {
        return BuildConfig.SDK_VERSION;
    }

}
