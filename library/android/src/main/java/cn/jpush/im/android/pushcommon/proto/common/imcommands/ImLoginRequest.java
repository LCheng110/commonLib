package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import android.database.sqlite.SQLiteDatabase;

import com.google.gson.jpush.annotations.Expose;
import com.google.protobuf.jpush.ByteString;

import java.util.List;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetNoDisurbListCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.pushcommon.proto.User.Login;
import cn.jpush.im.android.storage.database.DBOpenHelper;
import cn.jpush.im.android.tasks.GetBlackListTask;
import cn.jpush.im.android.tasks.GetBlockedGroupsTask;
import cn.jpush.im.android.tasks.GetFriendListTask;
import cn.jpush.im.android.tasks.GetNoDisturbListTask;
import cn.jpush.im.android.tasks.GetUserInfoTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public class ImLoginRequest extends ImBaseRequest {

    private static final String TAG = "ImLoginRequest";
    @Expose
    String username;
    @Expose
    String password;
    @Expose
    int platform;
    @Expose
    String sdkVersion;
    @Expose
    int hasMd5Flag;

    public ImLoginRequest(String username, String password, int platform, String sdkVersion, long rid) {
        super(IMCommands.Login.CMD, 0L, rid);

        this.username = username;
        this.password = password;
        this.platform = platform;
        this.sdkVersion = sdkVersion;
    }

    public static ImLoginRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, ImLoginRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Login.Builder builder = Login.newBuilder()
                .setPlatform(platform);

        if (null != username) {
            builder.setUsername(ByteString.copyFromUtf8(username));
        }
        if (null != password) {
            builder.setPassword(ByteString.copyFromUtf8(password));
        }
        if (null != sdkVersion) {
            builder.setSdkVersion(ByteString.copyFromUtf8(sdkVersion));
        }

        return new IMProtocol(IMCommands.Login.CMD,
                IMCommands.Login.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        Logger.d("loginPostExecute", "do loginPostExecute !");
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                final int responseCode = imProtocol.getResponse().getCode();
                final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
                if (0 == responseCode) {
                    final long realUid = imProtocol.getUid();
                    //登陆成功，切换数据库。
                    DBOpenHelper.switchUser(realUid, new DBOpenHelper.DBOpenCallback() {
                        @Override
                        public void onOpen(SQLiteDatabase database) {
                            //数据库切换完成。（包括数据库升级）
                            Logger.d(TAG, "login: userName = " + username + " password = " + password
                                    + " userID = " + realUid);
                            //将账号数据写入sp
                            IMConfigs.setUserID(realUid);
                            IMConfigs.setUserName(username);
                            IMConfigs.setUserPassword(password);
                            //im登陆成功，说明version已经成功上报。需要把sdk version存到本地。
                            IMConfigs.setSdkVersion(JMessageClient.getSdkVersionString());
                            //登陆成功后,需要启动SyncCheck.这里不用发送广播的方式启动syncCheck，
                            //因为此处已经确保了是上层应用进程。
                            RequestProcessor.startSyncCheck();
                            //登录成功后起异步任务获取用户基本配置和信息
                            prepareUserConfigAfterLogin(realUid);
                            //preference文件更新
                            updatePreference();
                            //上层回调。
                            CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
                            RequestProcessor.requestsCache.remove(rid);
                        }
                    });
                } else {
                    Logger.d(TAG, "login failed ! response code is " + responseCode);
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
                    RequestProcessor.requestsCache.remove(rid);
                }
                return null;
            }
        });
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }

    private static void prepareUserConfigAfterLogin(long uid) {
        new GetUserInfoTask(uid, null, true, false).execute();
        new GetBlackListTask(null, false).execute();
        new GetNoDisturbListTask(new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                // TODO: 16/3/13 无论获取免打扰名单成功或失败，都将NotifyManager置为ready状态，如果失败了可能有问题
                ChatMsgManager.getInstance().ready();
            }
        }, false).execute();
        new GetBlockedGroupsTask(null, false).execute();
        new GetFriendListTask(null, false).execute();
    }

    private static void updatePreference() {
        //用户登录时需要将老版本preference中已过期的notificationMode字段对应迁移到新版notiFlag中去
        int notiFlag = IMConfigs.getNotificationFlag();
        if (IMConfigs.INT_UNSET == notiFlag) { //如果notiFlag是处于未设置的状态，首次登陆时需要迁移。
            int deprecatedMode = IMConfigs.getNotificationMode();
            if (IMConfigs.INT_UNSET == deprecatedMode) {
                //如果老版本preference中的notificationMode也是未设置的状态，则直接将新的flag设置为默认。
                notiFlag = JMessageClient.FLAG_NOTIFY_DEFAULT;
            } else {
                //需要尝试将老版本preference中已过期的notificationMode字段对应更新到notificationFlag中去。
                switch (deprecatedMode) {
                    case JMessageClient.NOTI_MODE_DEFAULT:
                        notiFlag = JMessageClient.FLAG_NOTIFY_DEFAULT;
                        break;
                    case JMessageClient.NOTI_MODE_NO_NOTIFICATION:
                        notiFlag = JMessageClient.FLAG_NOTIFY_DISABLE;
                        break;
                    case JMessageClient.NOTI_MODE_NO_SOUND:
                        notiFlag = JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_VIBRATE;
                        break;
                    case JMessageClient.NOTI_MODE_NO_VIBRATE:
                        notiFlag = JMessageClient.FLAG_NOTIFY_WITH_LED | JMessageClient.FLAG_NOTIFY_WITH_SOUND;
                        break;
                    case JMessageClient.NOTI_MODE_SILENCE:
                        notiFlag = JMessageClient.FLAG_NOTIFY_SILENCE;
                        break;
                    default:
                        notiFlag = JMessageClient.FLAG_NOTIFY_DEFAULT;
                }
            }
            IMConfigs.setNotificationFlag(notiFlag);
        }
    }

}
