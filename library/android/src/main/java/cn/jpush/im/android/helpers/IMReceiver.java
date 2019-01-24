package cn.jpush.im.android.helpers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.gson.jpush.annotations.SerializedName;
import com.google.gson.jpush.reflect.TypeToken;

import java.util.Map;

import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.pushcommon.helper.IMResponseHelper;
import cn.jpush.im.android.pushcommon.helper.PluginJCoreHelper;
import cn.jpush.im.android.pushcommon.proto.JMessageCommands;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class IMReceiver extends BroadcastReceiver {

    private static final String TAG = "IMReceiver";

    public static final String CMD_START_SYNCCHECK = "cmd_start_sync_check";

    @Override
    public void onReceive(Context context, Intent intent) {
        // an Intent broadcast.
        JMessageClient.init(context);
        String theAction = intent.getAction();
        if (theAction == null) {
            Logger.d(TAG, "onReceive - the action is null");
            return;
        }
        Logger.d(TAG, "onReceive - " + theAction);

        if (theAction.equals(IMResponseHelper.ACTION_IM_RESPONSE)) {
//            boolean startSync = intent.getBooleanExtra(CMD_START_SYNCCHECK, false);
//            if (startSync) {
//                //应用内部广播，用来启动syncCheck
//                Logger.d(TAG, "received a start sync cmd");
//                RequestProcessor.startSyncCheck();
//                return;
//            }

            String extraData = intent.getStringExtra(IMResponseHelper.EXTRA_PUSH2IM_DATA);
            String sPushActionType = intent.getStringExtra(IMResponseHelper.EXTRA_PUSH_TYPE);
            if (!StringUtils.isEmpty(extraData)) {
                Logger.d(TAG, "extraData - " + extraData);
                PushLoginTime pushLoginTimeEntity = JsonUtil.formatToGivenType(extraData,
                        new TypeToken<PushLoginTime>() {
                        });
                if (0 != pushLoginTimeEntity.localTime && 0 != pushLoginTimeEntity.serverTime) {
                    onServerTimeReceived(pushLoginTimeEntity);
                } else {
                    onNetworkStatusChange(extraData);
                }
            } else if (!StringUtils.isEmpty(sPushActionType)) {
                onPushLoginStatusChanged(context, intent, sPushActionType);
            } else {
                int cmd = intent.getIntExtra(IMResponseHelper.DATA_MSG_CMD, JMessageCommands.IM.CMD);
                byte[] head = intent.getByteArrayExtra(IMResponseHelper.DATA_MSG_HEAD);
                byte[] body = intent.getByteArrayExtra(IMResponseHelper.DATA_MSG_BODY);
                ResponseProcessor.handleIMResponse(context, cmd, head, body);
            }
        } else if (theAction.equals(JMessage.ACTION_NOTI_RECEIVER_PROXY)) {
            ResponseProcessor.handleNotificationIntent(context, intent);
        } else {
            Logger.ww(TAG, "unhandled action! abort it.");
        }
    }

    private void onServerTimeReceived(PushLoginTime pushLoginTimeEntity) {
        IMConfigs.setPushLocalTime(pushLoginTimeEntity.localTime);
        IMConfigs.setPushServerTime(pushLoginTimeEntity.serverTime);
    }

    private void onNetworkStatusChange(String extraData) {
        Map<String, Object> map = JsonUtil.formatToObjectMap(extraData);
        Logger.ww(TAG, "format extra data .map = " + map);
        if (null != map && null != map.get(PluginJCoreHelper.PUSH_NETWORK_CONNECTED)) { //since map.get may return null. we should do this before cast.
            boolean isConn = (Boolean) map.get(PluginJCoreHelper.PUSH_NETWORK_CONNECTED);
            IMConfigs.setNetworkConnected(isConn);
            if (!isConn) {
                //如果网络连接断开,需要停止SyncCheck
                RequestProcessor.stopSyncCheck();
            }
        } else {
            Logger.ww(TAG, "format extra data error for some reason. map = " + map);
        }
    }

    private void onPushLoginStatusChanged(Context context, Intent intent, String sPushActionType) {
        Logger.d(TAG, "sPushActionType - " + sPushActionType);
        if (IMResponseHelper.EXTRA_PUSH_TYPE_LOGIN.equals(sPushActionType)) {
            IMResponseHelper.handlePushLogin(context, intent.getExtras());
            // TODO: 2016/11/4 这里先把版本判断的逻辑注释掉，因为应用如果走新版本 -> 老版本 -> 新版本这个流程时,最后一次新版本升级就不会有版本上报了。之后等新版本全面上线，使用率比较高后再考虑放开这段逻辑
//            String localSdkVersion = IMConfigs.getSdkVersion();
//            if (TextUtils.isEmpty(localSdkVersion) || !localSdkVersion.equals(JMessageClient.getSdkVersionString())) {
            //push登陆成功，同时判断本地保存的版本号为空或者和当前sdk版本号不同，则执行版本上报。
            RequestProcessor.imReportInfo(context, JMessageClient.getSdkVersionString(), CommonUtils.getSeqID());
//            }
            //push登陆成功的同时需要发起SyncCheck,这里不用发送广播的方式启动syncCheck，
            //因为这里已经是在广播的onReceive方法中了，已经确保了是上层应用进程。
            RequestProcessor.startSyncCheck();
        }
        if (IMResponseHelper.EXTRA_PUSH_TYPE_LOGOUT.equals(sPushActionType)) {
            IMResponseHelper.handlePushLogout(context, intent.getExtras());
            //push登出,同时需要停止SyncCheck
            RequestProcessor.stopSyncCheck();
        }
    }

    class PushLoginTime {
        @SerializedName("push_login_local_time")
        public long localTime;
        @SerializedName("push_login_server_time")
        public long serverTime;
    }

}
