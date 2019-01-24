package cn.citytag.base.utils;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.lang.reflect.Method;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.constants.Constants;

/**
 * Created by yangfeng01 on 2017/11/9.
 */

public class UIUtils {

    private static float sDensity;

    /**
     * dip转换成px
     *
     * @param context
     * @param dpValue
     * @return
     */
    public static int dip2px(Context context, float dpValue) {
        return dip2px(dpValue);
    }

    public static int dip2px(float dpValue) {
        ensureDensity();
        return (int) (dpValue * sDensity + 0.5f);
    }

    /**
     * px转换成dip
     *
     * @param context
     * @param pxValue
     * @return
     */
    public static int px2dip(Context context, float pxValue) {
        return px2dip(pxValue);
    }

    public static int px2dip(float pxValue) {
        ensureDensity();
        return (int) (pxValue / sDensity + 0.5f);
    }

    public static void toastMessage(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        ToastUtil.toastMessage(BaseConfig.getContext(), msg);
    }
    public static void toastMessagelong(String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        ToastUtil.toastMessagelong(BaseConfig.getContext(), msg);
    }
    public static void toastMessage(Context context, String msg) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        ToastUtil.toastMessage(context, msg);
    }

    public static void toastMessage(int msg) {
        if (msg == 0) {
            return;
        }
        ToastUtil.toastMessage(ActivityUtils.peek(), msg);
    }

    public static void toastMessage(String msg, int time) {
        if (StringUtils.isEmpty(msg)) {
            return;
        }
        ToastUtil.toastMessage(ActivityUtils.peek(), msg, time);
    }

    private static void ensureDensity() {
        if (sDensity == 0) {
            sDensity = BaseConfig.getContext().getResources().getDisplayMetrics().density;
        }
    }

    /**
     * 判断是否有刘海屏
     * 已适配华为/OPPO/小米
     */
    public static boolean hasNotchInScreen(Context context) {
        boolean retHw = false;
        boolean retVivo = false;
        boolean retMi = false;
        boolean oppoHasNotch = context.getPackageManager().hasSystemFeature("com.oppo.feature.screen.heteromorphism");
        try {
            retMi = getInt(context, "ro.miui.notch", 0) == 1;
            Log.i("hasNotchInScreen", "xiaomi: " + getInt(context, "ro.miui.notch", 0));
        } catch (Exception e) {
            Log.e("hasNotchInScreen", "hasNotchInScreen MiException");
        }
        ClassLoader cl = context.getClassLoader();
        try {
            Class HwNotchSizeUtil = cl.loadClass("com.huawei.android.util.HwNotchSizeUtil");
            Method getHw = HwNotchSizeUtil.getMethod("hasNotchInScreen");
            retHw = (boolean) getHw.invoke(HwNotchSizeUtil);
        } catch (ClassNotFoundException e) {
            Log.e("hasNotchInScreen", "hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("hasNotchInScreen", "hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            Log.e("hasNotchInScreen", "hasNotchInScreen Exception");
        }
        try {
            Class FtFeature = cl.loadClass("com.util.FtFeature");
            Method getVivo = FtFeature.getMethod("isFeatureSupport", int.class);
            retVivo = (boolean) getVivo.invoke(FtFeature, 0x00000020);
        } catch (ClassNotFoundException e) {
            Log.e("hasNotchInScreen", "hasNotchInScreen ClassNotFoundException");
        } catch (NoSuchMethodException e) {
            Log.e("hasNotchInScreen", "hasNotchInScreen NoSuchMethodException");
        } catch (Exception e) {
            Log.e("hasNotchInScreen", "hasNotchInScreen Exception");
        }
        Log.i("hasNotchInScreen", "hasNotchInScreen: " + (retHw || retVivo || oppoHasNotch));
        return retHw || retVivo || oppoHasNotch || retMi;
    }

    /**
     * 获取刘海屏高度(以最高刘海高度为准)
     * 已适配华为/OPPO/小米
     */
    public static int getNotchHeight(Context context) {
        return 89;
    }

    /**
     * 设置透明的状态栏及全屏
     *
     * @param context
     */
    public static void setTransparentStatusBar(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = ((Activity) context).getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public static void clearToast() {
        ToastUtil.cancel();
    }

    private static class ToastUtil {

        private static Toast sToast;

        static void toastMessage(Context context, String msg) {
            toastMessage(context, msg, Toast.LENGTH_SHORT);
        }
        static void toastMessagelong(Context context, String msg) {
            toastMessage(context, msg, Toast.LENGTH_LONG);
        }
        static void toastMessage(Context context, int resId) {
            toastMessage(context, context.getString(resId), Toast.LENGTH_SHORT);
        }

        static void toastMessage(Context context, int resId, int time) {
            toastMessage(context, context.getString(resId), time);
        }

        static void toastMessage(Context context, String msg, int time) {
            if (sToast == null) {
                sToast = Toast.makeText(context, msg, time);
            } else {
                sToast.setText(msg);
            }
            if (sToast != null) {
                sToast.show();
                L.d("yf", "show Toast..................");
            }
        }

        static void cancel() {
            if (sToast != null) {
                sToast.cancel();
                sToast = null;
            }
        }

    }

    /**
     * 根据给定的key返回int类型值.
     *
     * @param key 要查询的key
     * @param def 默认返回值
     * @return 返回一个int类型的值, 如果没有发现则返回默认值
     * @throws IllegalArgumentException 如果key超过32个字符则抛出该异常
     */
    public static Integer getInt(Context context, String key, int def) throws IllegalArgumentException {

        Integer ret = def;

        try {

            ClassLoader cl = context.getClassLoader();
            @SuppressWarnings("rawtypes")
            Class SystemProperties = cl.loadClass("android.os.SystemProperties");

            //参数类型
            @SuppressWarnings("rawtypes")
            Class[] paramTypes = new Class[2];
            paramTypes[0] = String.class;
            paramTypes[1] = int.class;

            Method getInt = SystemProperties.getMethod("getInt", paramTypes);

            //参数
            Object[] params = new Object[2];
            params[0] = new String(key);
            params[1] = new Integer(def);

            ret = (Integer) getInt.invoke(SystemProperties, params);

        } catch (IllegalArgumentException iAE) {
            throw iAE;
        } catch (Exception e) {
            ret = def;
            //TODO
        }

        return ret;

    }

    /**
     * 获取屏幕的宽
     *
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        return dm.widthPixels;
    }

    /**
     * 获取屏幕的高度
     *
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getRealMetrics(dm);
        return dm.heightPixels;
    }

    /**
     * 是否在屏幕右侧
     *
     * @param mContext 上下文
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    public static boolean isInRight(Context mContext, int xPos) {
        return (xPos > getScreenWidth(mContext) / 2);
    }

    /**
     * 是否在屏幕左侧
     *
     * @param mContext 上下文
     * @param xPos     位置的x坐标值
     * @return true：是。
     */
    public static boolean isInLeft(Context mContext, int xPos) {
        return (xPos < getScreenWidth(mContext) / 2);
    }


    /**
     * @param str
     * @return
     */
    public static String emptyProtect(String str) {
        if (TextUtils.isEmpty(str)) {
            return Constants.EMPTY_PROTECT;
        } else {
            return str;
        }
    }

    /**
     * 获取 imei
     *
     * @return
     */
    public static String getImei(Context context) {
        String imei = null;
        try {
            TelephonyManager telephonyManager = ((TelephonyManager) context.
                    getSystemService(Context.TELEPHONY_SERVICE));
            imei = telephonyManager.getDeviceId();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return imei;
    }

    /**
     * 获取Android ID
     *
     * @return
     */
    public static String getDeviceId(Context context) {
        return Settings.System.getString(context.getContentResolver(),
                Settings.System.ANDROID_ID);
    }


    /**
     * 获取 serial
     *
     * @return
     */
    public static String getSerialNumber() {
        String serial = null;
        try {
            Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class);
            serial = (String) get.invoke(c, "ro.serialnocustom");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return serial;
    }

    /**
     * 获取 IP 地址
     *
     * @param context
     * @return
     */
    public static String getIPAddress(Context context) {
        NetworkInfo info = ((ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) { //当前使用2G/3G/4G网络
                try {
                    //Enumeration<NetworkInterface> en=NetworkInterface.getNetworkInterfaces();
                    for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces();
                         en.hasMoreElements(); ) {
                        NetworkInterface intf = en.nextElement();
                        for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses();
                             enumIpAddr.hasMoreElements(); ) {
                            InetAddress inetAddress = enumIpAddr.nextElement();
                            if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                                return inetAddress.getHostAddress();
                            }
                        }
                    }
                } catch (SocketException e) {
                    e.printStackTrace();
                }

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) { //当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        } else {
            //当前无网络连接,请在设置中打开网络
        }
        return null;
    }


    /**
     * 将得到的int类型的IP转换为String类型
     *
     *
     *
     */
    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

}
