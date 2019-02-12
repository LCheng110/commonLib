package cn.citytag.base.helpers.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.utils.L;

/**
 * Created by yangfeng01 on 2017/11/15.
 * <p>
 * todo 1.申请权限前判断是否shouldShowRational=true，来做原理性弹框
 * todo 2.勾选不再询问后，回调里shouldShowRational=false，从而做引导设置的弹框
 */
public class PermissionChecker {

    private static final String TAG = "PermissionChecker";

    public static final int REQUEST_CODE_PERMISSION_BASIC = 1;
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    private static Map<String, String> sPermissionMap;

    public static String[] ALL_PERMISSIONS = {Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_CONTACTS,};

    public static String[] BASIC_PERMISSIONS = {
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.READ_PHONE_STATE,
            //android.Manifest.permission.READ_CONTACTS,
            //android.Manifest.permission.READ_CALL_LOG,
    };

    public static String[] LOCATION_PERMISSION = {Manifest.permission.ACCESS_COARSE_LOCATION};

    public static String getPermissionName(String target) {
        if (sPermissionMap == null) {
            sPermissionMap = new HashMap<>();
            sPermissionMap.put(Manifest.permission.ACCESS_COARSE_LOCATION, "定位权限");
            sPermissionMap.put(Manifest.permission.READ_EXTERNAL_STORAGE, "存储权限");
            sPermissionMap.put(Manifest.permission.WRITE_EXTERNAL_STORAGE, "读取权限");
            sPermissionMap.put(Manifest.permission.READ_PHONE_STATE, "电话权限");
            sPermissionMap.put(Manifest.permission.READ_CONTACTS, "联系人权限");
            sPermissionMap.put(Manifest.permission.CAMERA, "相机权限");
            sPermissionMap.put(Manifest.permission.RECORD_AUDIO, "麦克风权限");
        }
        try {
            return sPermissionMap.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    /**
     * 检查是否已获取全部的permissions权限
     *
     * @param context     the calling context
     * @param permissions one ore more permissions, such as {@link Manifest.permission#CAMERA}.
     * @return true if all permissions are already granted, false if at least one permission is not
     * yet granted.
     * @see Manifest.permission
     */
    public static boolean hasPermissions(Context context, @NonNull String... permissions) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            L.d(TAG, "hasPermissions: API version < M, returning true by default");
            return true;
        }
        for (String perm : permissions) {
            if (ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    public static void requestPermissions(@NonNull Activity host, int requestCode, @NonNull String... perms) {
        requestPermissions(PermissionHelper.newInstance(host), "", requestCode, perms);
    }

    public static void requestPermissions(@NonNull Activity host, String rational, int requestCode, @NonNull String... perms) {
        requestPermissions(PermissionHelper.newInstance(host), rational, requestCode, perms);
    }

    public static void requestPermissions(@NonNull Fragment host, String rational, int requestCode, @NonNull String... perms) {
        requestPermissions(PermissionHelper.newInstance(host), rational, requestCode, perms);
    }

    public static void requestPermissions(@NonNull Fragment host, int requestCode, @NonNull String... perms) {
        requestPermissions(PermissionHelper.newInstance(host), "", requestCode, perms);
    }


    /**
     * @param helper
     * @param requestCode
     * @param perms
     */
    private static void requestPermissions(@NonNull PermissionHelper helper, String rational, int requestCode, @NonNull String... perms) {
        if (hasPermissions(helper.getContext(), perms)) {
            notifyAlreadyHasPermissions(helper.getHost(), requestCode, perms);
            return;
        }
        helper.requestPermissions(rational, requestCode, perms);
    }

    private static void notifyAlreadyHasPermissions(@NonNull Object object, int requestCode, @NonNull String... perms) {
        int[] grantResults = new int[perms.length];
        for (int i = 0; i < perms.length; i++) {
            grantResults[i] = PackageManager.PERMISSION_GRANTED;
        }

        onRequestPermissionsResult(requestCode, perms, grantResults, object);
    }

    public static void onRequestPermissionsResult(int requestCode,
                                                  @NonNull String[] perms,
                                                  @NonNull int[] grantResults,
                                                  @NonNull Object... receivers) {
        List<String> grantedPerms = new ArrayList<>();
        List<String> deniedPerms = new ArrayList<>();
        for (int i = 0; i < perms.length; i++) {
            String perm = perms[i];
            if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                grantedPerms.add(perm);
            } else {
                deniedPerms.add(perm);
            }
        }
        for (Object object : receivers) {

            if (!grantedPerms.isEmpty()) {
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsGranted(requestCode, grantedPerms);
                }
            }

            if (!deniedPerms.isEmpty())
                if (object instanceof PermissionCallbacks) {
                    ((PermissionCallbacks) object).onPermissionsDenied(requestCode, deniedPerms);
                }

        }

    }

    /**
     * Check if at least one permission in the list of denied permissions has been permanently
     * denied (user clicked "Never ask again").
     *
     * @param host              context requesting permissions.
     * @param deniedPermissions list of denied permissions, usually from {@link
     *                          PermissionCallbacks#onPermissionsDenied(int, List)}
     * @return {@code true} if at least one permission in the list was permanently denied.
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Activity host,
                                                          @NonNull List<String> deniedPermissions) {
        return PermissionHelper.newInstance(host)
                .somePermissionPermanentlyDenied(deniedPermissions);
    }

    /**
     * @see #somePermissionPermanentlyDenied(Activity, List)
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull Fragment host,
                                                          @NonNull List<String> deniedPermissions) {
        return PermissionHelper.newInstance(host)
                .somePermissionPermanentlyDenied(deniedPermissions);
    }

    /**
     * @see #somePermissionPermanentlyDenied(Activity, List).
     */
    public static boolean somePermissionPermanentlyDenied(@NonNull android.app.Fragment host,
                                                          @NonNull List<String> deniedPermissions) {
        return PermissionHelper.newInstance(host)
                .somePermissionPermanentlyDenied(deniedPermissions);
    }

    /**
     * 是否打开了通知权限
     */
    public static boolean isNotificationEnabled() {
        NotificationManagerCompat managerCompat = NotificationManagerCompat.from(BaseConfig.getContext());
        return managerCompat.areNotificationsEnabled();
    }

    /**
     * 是否允许定位
     */
    //public static boolean isLocationEnabled(Context context) {
    //	return hasLocationPermission(context) && isGpsEnabled(context);
    //}

    //private static boolean hasLocationPermission(Context context) {
    //	return PermissionChecker.hasPermissions(context, PermissionChecker.LOCATION_PERMISSION);
    //}

    //public static boolean isGpsEnabled(Context context) {
    //	LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    //	boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    //	return isGpsEnabled;
    //}


    /**
     * Callback interface to receive the results of {@code EasyPermissions.requestPermissions()}
     * calls.
     */
    public interface PermissionCallbacks extends ActivityCompat.OnRequestPermissionsResultCallback {

        void onPermissionsGranted(int requestCode, List<String> perms);

        void onPermissionsDenied(int requestCode, List<String> perms);

    }

}
