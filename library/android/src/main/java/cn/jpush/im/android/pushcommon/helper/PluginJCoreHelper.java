package cn.jpush.im.android.pushcommon.helper;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jiguang.ald.api.MultiSpHelper;
import cn.jpush.im.android.utils.AndroidUtil;
import cn.jpush.im.android.utils.Logger;


/**
 * 此处用来中间中转一下之前push的Config与serviceInterface相关的接口
 */
public class PluginJCoreHelper {
    private static final String TAG = "PluginJCoreHelper";
    public static final String PUSH_NETWORK_CONNECTED = "push_network_connected";

    /**
     * 设置Im 登录的状态
     * @param status
     */
    public static void setImLogStatus(Context context,boolean status) {
        Logger.d(TAG,"Action - setImLogStatus:"+status);
        setImLoggedIn(context,status);
        if (status) {
            addImLoginCount(context.getApplicationContext());
        }
    }
    /**
     * //TODO::当前JPush与IM是独立的业务，两个业务之间不进行任何的交叉处理 置空该方法
     * */
    public static void resetPushStatus(Context context) {
//        if (Configs.isStopExecuted(context)) {
//            Logger.d(TAG, "call stopPush on im-logout-success or im-login-timeout");
//            ServiceInterface.stopPush(context, 1);
//            Configs.setStopExecuted(context, true);
//        } else {
//            Logger.d(TAG, "push-status is running before im-login called");
//        }
    }

    /**
     * JCore中PushReceiver相关的intent回调会通知到该方法中，
     * //TODO::在该方法中请切勿做耗时的动作，当前JCore没有异步来处理该intent
     * */
    public static void onJCoreIntentNotify(Context context, Intent intent){
        String action = intent.getAction();
        if (action.equalsIgnoreCase(ConnectivityManager.CONNECTIVITY_ACTION)) {
            NetworkInfo networkInfo = null;
            if(intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO) instanceof NetworkInfo){
                networkInfo = intent.getParcelableExtra(ConnectivityManager.EXTRA_NETWORK_INFO);
            }
            if (null == networkInfo) {
                Logger.w(TAG, "Not found networkInfo");
                return;
            }
            Logger.d(TAG, "Connection state changed to - " + networkInfo.toString());
            if (ConnectivityManager.TYPE_MOBILE_MMS == networkInfo.getType() ||
                    ConnectivityManager.TYPE_MOBILE_SUPL == networkInfo.getType()) {
                Logger.d(TAG, "MMS or SUPL network state change, to do nothing!");
                return;
            }

            boolean disConnected = intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY, false);
            boolean isConnected = false;
            if (!disConnected && NetworkInfo.State.CONNECTED == networkInfo.getState()) {
                Logger.d(TAG, "Network is connected.");
                isConnected = true;
            }
            sendNetworkChangedToIM(context, isConnected);
        }
    }
    private static void sendNetworkChangedToIM(Context context, boolean isConnected) {
        Logger.dd(TAG, "Action - sendNetworkChangedToIM");
        try {
            Bundle bundle = new Bundle();
            JSONObject jsonObject = new JSONObject();
            jsonObject.put(PUSH_NETWORK_CONNECTED, isConnected);
//            bundle.putString(IMResponseHelper.EXTRA_PUSH2IM_DATA, jsonObject.toString());
//            AndroidUtil.sendBroadcast(context, IMResponseHelper.ACTION_IM_RESPONSE, bundle);
            bundle.putString("push_to_im_data", jsonObject.toString());
            AndroidUtil.sendBroadcast(context, "cn.jpush.im.android.action.IM_RESPONSE", bundle);
        } catch (JSONException e) {
            Logger.w(TAG, "jsonException - " + e.getMessage());
        }
    }


    /**========================================================================================================*/
    /**=====================sp相关的操作，为了更好的兼容之前的的数据，操作的sp 文件跟之前相同============================*/
    /**========================================================================================================*/

    //TODO::已经有的key请切勿修改，后续如需新增sp相关的操作，请加载IM特有的sp文件中，切勿添加JCore提供的SP文件中

    // IM 登录成功时设置为 True, IM Logout 触发时设置为 false
    private static final String KEY_IS_IM_LOGGED_IN = "is_im_logged_in";
    public static boolean isImLoggedIn(Context context) {
        return MultiSpHelper.getBoolean(context.getApplicationContext(), KEY_IS_IM_LOGGED_IN, false);
    }
    public static void setImLoggedIn(Context context,boolean loggedIn) {
       MultiSpHelper.commitBoolean(context.getApplicationContext(), KEY_IS_IM_LOGGED_IN, loggedIn);
    }

    // 注册结果返回,错误码见:
    // http://wiki.jpushoa.com/display/KKPush/Push-Server-Core-Protocol%2832bits%29#Push-Server-Core-Protocol32bits-6
    private static final String KEY_REGISTER_CODE = "jpush_register_code";
    public static void setRegisterCode(Context context, int registerCode) {
        MultiSpHelper.commitInt(context, KEY_REGISTER_CODE, registerCode);
    }
    public static int getRegisterCode(Context context) {
        return MultiSpHelper.getInt(context, KEY_REGISTER_CODE, -1);
    }
    // IM 登陆成功的次数， 当>=0的时候 登陆心跳的flag 为0，否则为64
    private static final String KEY_IM_LOGIN_COUNT = "im_login_count";
    public static int getImLoginCount(Context context) {
        return MultiSpHelper.getInt(context.getApplicationContext(), KEY_IM_LOGIN_COUNT, -1);
    }
    public static void addImLoginCount(Context context) {
        int count = getImLoginCount(context.getApplicationContext());
        MultiSpHelper.commitInt(context.getApplicationContext(), KEY_IM_LOGIN_COUNT, ++count);
    }

    private static final String KEY_LBS_REPORT_ENABLE = "lbs_report_enable";
    public static void setLbsEnabled(Context context, boolean enableFlag) {
        MultiSpHelper.commitBoolean(context, KEY_LBS_REPORT_ENABLE, enableFlag);
    }
    public static boolean isLbsEnabled(Context context) {
        return MultiSpHelper.getBoolean(context, KEY_LBS_REPORT_ENABLE, true);
    }
    /**
     * //TODO::之前isPushLoggedIn使用JCoreInterface.isTcpConnected()替代
     * */
    /** push login status */
    public static boolean isPushLoggedIn() {
        return JCoreInterface.isTcpConnected();
    }
    /**
     * 设置测试ip 与端口
     * //TODO::该接口只有再jcore为非release版本才有效果
     * */
    public static void setTestConnIPPort(String ip,int port){
        JCoreInterface.setTestConnIPPort(ip, port);
    }
}
