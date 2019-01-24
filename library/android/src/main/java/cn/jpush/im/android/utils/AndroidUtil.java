package cn.jpush.im.android.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Process;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.pushcommon.helper.IMResponseHelper;

public class AndroidUtil {
    private static final String TAG = "AndroidUtil";
    public static final String EXTRA_APP_KEY = "cn.jpush.android.APPKEY";
    public static final String PUSH_MESSAGE_PERMISSION_POSTFIX = ".permission.JPUSH_MESSAGE";

    public static void sendBroadcast(Context context, String action, Bundle bundle) {
        if (null == bundle) {
            Logger.ee(TAG, "Bundle should not be null for sendBroadcast.");
            return;
        }

        Logger.d(TAG, "try to send broadcast , " + " process pid - " + Process.myPid()+ " process uid - " + Process.myUid()+ " process tid - " + Process.myTid() + "is in main thread = " + Thread.currentThread().toString() + " bundle = " + bundle);
        Intent intent = new Intent(action);
        bundle.putString(EXTRA_APP_KEY, JCoreInterface.getAppKey());
        intent.putExtras(bundle);

        String pkgName = context.getPackageName();
        intent.addCategory(pkgName);

        try {
            context.sendBroadcast(intent,
                    String.format("%s" + PUSH_MESSAGE_PERMISSION_POSTFIX,
                            pkgName));
        } catch (SecurityException e) {
            //防止在android7.1以上版本出现崩溃，这里先catch住异常。
            Logger.ee(TAG, "send broadcast failed . try to resend with component name");
            tryAgainSendBroadcast(context, intent, null);
        }
    }

    /**
     * 根据intent查找Receiver名
     * 注意可能只能拿到静态注册的
     *
     * @param permission 传参表示过滤掉不具备权限的，传空表示没有权限要求
     */
    private static List<String> getReceiverNames(Context context, Intent intent, String permission) {
        List<String> receiverList = new ArrayList<String>();
        try {
            List<ResolveInfo> resolveInfos = context.getPackageManager()
                    .queryBroadcastReceivers(intent, 0);
            PackageManager pm = context.getPackageManager();
            for (ResolveInfo info : resolveInfos) {
                if (info.activityInfo != null) {
                    String receiverName = info.activityInfo.name;
                    if (!TextUtils.isEmpty(receiverName)) {      //Receiver不为空
                        boolean hasPermission = true;
                        if (!TextUtils.isEmpty(permission)      //有权限需求的，校验权限
                                && PackageManager.PERMISSION_GRANTED
                                != pm.checkPermission(permission, info.activityInfo.packageName)) {
                            hasPermission = false;
                        }
                        if (hasPermission) {
                            receiverList.add(receiverName);
                        }
                    }
                }
            }
        } catch (Exception ignore) {
        }

        return receiverList;
    }

    /**
     * 发送广播失败时（主要是Android7.1.1），尝试补充类名重试
     * 仅限于个别自定义action :cn.jpush.android.intent.CONNECTION
     */
    public static void tryAgainSendBroadcast(Context context, Intent intent, String permission) {
        if (intent == null) {
            return;
        }
        String action = intent.getAction();
        if (IMResponseHelper.ACTION_IM_RESPONSE.equals(action)) {

            List<String> receiverNames = getReceiverNames(context, intent, permission);

            if (receiverNames != null && !receiverNames.isEmpty()) {
                for (String name : receiverNames) {
                    try {
                        Intent it = (Intent) intent.clone();
                        //设置类名重发
                        it.setAction(null);//去掉action，
                        it.setComponent(new ComponentName(context.getPackageName(), name));
                        Logger.dd(TAG, "receiver name = " + name + " intent = " + intent);
                        if (TextUtils.isEmpty(permission)) {
                            context.sendBroadcast(it);
                        } else {
                            context.sendBroadcast(it, permission);
                        }
                    } catch (Exception e) {
                        Logger.ww(TAG, "sendBroadcast failed again:" + e.getMessage() + ", action:" + action);
                    }
                }
            } else {
                Logger.ww(TAG, "sendBroadcast failed again: receiver not found, action:" + action);
            }
        }
    }

    /**
     * 获取socket 下发的IM消息对应的bodyBuffer的有效长度
     */
    public static int getRealBodyLen(byte[] bodyBuffer) {
        if (null == bodyBuffer || bodyBuffer.length < 2) return -1;
        int packageLen = 0;
        for (int i = 0; i < 2; i++) {
            packageLen = (packageLen << 8)
                    + (bodyBuffer[i] & 0xff);
        }
        packageLen += 2;
        if (packageLen > bodyBuffer.length) {
            Logger.ww(TAG, "invalide body buffer");
            return -1;
        }
        return packageLen;
    }

    public static boolean hasReceiverIntentFilter(Context context, String action, boolean needPackageCategory) {
        PackageManager pm = context.getPackageManager();
        Intent intent = new Intent(action);
        if (needPackageCategory) {
            intent.addCategory(context.getPackageName());
        }
        List<ResolveInfo> list = pm.queryBroadcastReceivers(intent, 0);
        return !list.isEmpty();
    }

    /**
     * Check if the connection is fast
     *
     * @return
     */
    public static boolean isConnectionFast() {
        ConnectivityManager cm = (ConnectivityManager) JMessage.mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info == null || !info.isConnected()) {
            return false;
        }
        int type = info.getType();
        int subType = info.getSubtype();
        String subTypeName = info.getSubtypeName();
        Logger.d(TAG, "network contentType is " + type + " . subType = " + subType + " subTypeName = " + subTypeName);

        if (type == ConnectivityManager.TYPE_WIFI) {
            return true;
        } else if (type == ConnectivityManager.TYPE_MOBILE) {
            switch (subType) {
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_CDMA:
                    return false; // ~ 14-64 kbps
                case TelephonyManager.NETWORK_TYPE_EDGE:
                    return false; // ~ 50-100 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                    return true; // ~ 400-1000 kbps
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                    return true; // ~ 600-1400 kbps
                case TelephonyManager.NETWORK_TYPE_GPRS:
                    return false; // ~ 100 kbps
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                    return true; // ~ 2-14 Mbps
                case TelephonyManager.NETWORK_TYPE_HSPA:
                    return true; // ~ 700-1700 kbps
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                    return true; // ~ 1-23 Mbps
                case TelephonyManager.NETWORK_TYPE_UMTS:
                    return true; // ~ 400-7000 kbps
            /*
             * Above API level 7, make sure to set android:targetSdkVersion
             * to appropriate level to use these
             */
                case 14: //TelephonyManager.NETWORK_TYPE_EHRPD: // API level 11
                    return true; // ~ 1-2 Mbps
                case 12: //TelephonyManager.NETWORK_TYPE_EVDO_B: // API level 9
                    return true; // ~ 5 Mbps
                case 15: //TelephonyManager.NETWORK_TYPE_HSPAP: // API level 13
                    return true; // ~ 10-20 Mbps
                case 11: //TelephonyManager.NETWORK_TYPE_IDEN: // API level 8
                    return false; // ~25 kbps
                case 13: //TelephonyManager.NETWORK_TYPE_LTE: // API level 11
                    return true; // ~ 10+ Mbps
                // Unknown
                case TelephonyManager.NETWORK_TYPE_UNKNOWN:
                default:
                    // http://baike.baidu.com/item/TD-SCDMA 中国移动 联通 电信 三种3G制式
                    Logger.d(TAG, "got an unexpected subType !");
                    return subTypeName.equalsIgnoreCase("TD-SCDMA")
                            || subTypeName.equalsIgnoreCase("WCDMA")
                            || subTypeName.equalsIgnoreCase("CDMA2000");
            }
        } else {
            return false;
        }
    }
}
