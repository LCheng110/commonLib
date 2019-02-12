package cn.citytag.base.utils;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import android.support.v7.view.ContextThemeWrapper;

import java.util.List;

import cn.citytag.base.config.BaseConfig;

/**
 * Created by yangfeng01 on 2017/11/16.
 */

public class AppUtils {

    /**
     * 获取包名
     *
     * @param context
     */
    public static String getPackageName(Context context) {
        return context.getPackageName();
    }

    /**
     * 获取版本名称
     */
    public static String getVersionName(Context context) {
        String versionName = "1.0.0";
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionName = packageInfo.versionName;
            if (StringUtils.isEmpty(versionName)) {
                versionName = "";
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionName;
    }

    /**
     * 获取版本号
     *
     * @param context
     * @return
     */
    public static int getVersionCode(Context context) {
        int versionCode = 0;
        PackageManager packageManager = context.getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0);
            versionCode = packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return versionCode;
    }

    /**
     * 获取签名信息
     */
    public static int getSignature(Context context) {
        PackageManager pm = context.getPackageManager();
        PackageInfo pi;
        StringBuilder builder = new StringBuilder();

        try {
            pi = pm.getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signatures = pi.signatures;
            for (Signature signature : signatures) {
                builder.append(signature.toCharsString());
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return builder.toString().hashCode();
    }

    /**
     * 去应用的设置
     */
    public static void startAppSettings(Activity activity, int requestCode) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        activity.startActivityForResult(intent, requestCode);
    }

    public static void call(Activity activity, String phoneNum) {
        Intent intent = new Intent(Intent.ACTION_DIAL);
        Uri data = Uri.parse("tel:" + phoneNum);
        intent.setData(data);
        activity.startActivity(intent);
    }

    /**
     * Get activity from context object
     *
     * @param context context
     * @return object of Activity or null if it is not Activity
     */
    public static Activity getActivityFromContext(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof Activity) {
            return (Activity) context;
        } else {
            return getActivityFromContext(((ContextWrapper) context).getBaseContext());
        }
    }

    /**
     * Get FragmentActivity from context
     *
     * @param context context
     * @return FragmentActivity if it's not null
     */
    public static FragmentActivity getFragmentActivityFromContext(Context context) {
        if (context == null) {
            return null;
        }
        if (context instanceof FragmentActivity) {
            return (FragmentActivity) context;
        } else if (context instanceof ContextThemeWrapper) {
            return getFragmentActivityFromContext(((ContextThemeWrapper) context).getBaseContext());
        }
        return null;
    }

    /**
     * 判断是否安装了支付宝
     *
     * @return true 为已经安装
     */
    public static boolean hasApplication(Activity activity) {
        PackageManager manager = activity.getPackageManager();
        Intent action = new Intent(Intent.ACTION_VIEW);
        action.setData(Uri.parse("alipays://"));
        List list = manager.queryIntentActivities(action, PackageManager.GET_RESOLVED_FILTER);
        return list != null && list.size() > 0;
    }

    /**
     * 是否安装了微信
     * @param context
     */
    public static void startWechat(Context context){
        try {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            ComponentName cmp = new ComponentName("com.tencent.mm","com.tencent.mm.ui.LauncherUI");
            intent.addCategory(Intent.CATEGORY_LAUNCHER);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(cmp);
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            ToastUtils.showShort("请先安装微信");
        }


    }

    /**
     * 获取友盟渠道
     */
    public static void getYouMChannel(Activity activity) {
        ApplicationInfo info = null;
        if (activity == null) return;
        try {
            info = activity.getPackageManager()
                    .getApplicationInfo(activity.getPackageName(), PackageManager.GET_META_DATA);
            if (info != null && info.metaData != null) {
                String msg = info.metaData.getString("UMENG_CHANNEL");
                BaseConfig.setDownChannel(msg);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * setUUid
     */
    public static void setUUid() {
        String uuid = (String) PrefsUtil.get(BaseConfig.getContext(), "mapgo_uuid", "");
        if (uuid == null || uuid.length() == 0) {
            uuid = java.util.UUID.randomUUID().toString();
            PrefsUtil.put(BaseConfig.getContext(), "mapgo_uuid", uuid);
            BaseConfig.setEquipNum(uuid);

        } else {
            BaseConfig.setEquipNum(uuid);
        }
    }


    //这里是进入应用商店，下载指定APP的方法。
    public static void goToMarket(Context context, String packageName) {
        Uri uri = Uri.parse("market://details?id=" + packageName);
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        try {
            context.startActivity(goToMarket);
        } catch (Exception e) {
        }
    }
    //这里是判断APP中是否有相应APP的方法
    public static boolean isAppInstalled(Context context, String packageName) {
        try {
            context.getPackageManager().getPackageInfo(packageName,0);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static boolean isAppInstalled(@NonNull final String packageName) {
        PackageManager packageManager = BaseConfig.getContext().getPackageManager();
        try {
            return packageManager.getApplicationInfo(packageName, 0) != null;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }


}
