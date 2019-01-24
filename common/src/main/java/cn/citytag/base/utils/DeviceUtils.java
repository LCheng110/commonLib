package cn.citytag.base.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.preference.PreferenceManager;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.ViewConfiguration;

import java.util.UUID;

/**
 * Created by yangfeng01 on 2016/4/1.
 */
public class DeviceUtils {

	private static final String TAG = DeviceUtils.class.getSimpleName();

	private DeviceUtils() {

	}

	/**
	 * 通过判断设备是否有返回键、菜单键(不是虚拟键,是手机屏幕外的按键)来确定是否有navigation bar
	 *
	 * @param context
	 * @return
	 */
	public static boolean hasNavigationBar(Context context) {
		boolean result = false;
		boolean hasPhysicalMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
		boolean hasPhysicalBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);
		if (!hasPhysicalMenuKey && !hasPhysicalBackKey) {
			result = true;    // 做任何你需要做的,这个设备有一个导航栏
		}
		return result;
	}

	public static int getNavigationBarHeight(Context context) {
		int result = 0;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			boolean hasMenuKey = ViewConfiguration.get(context).hasPermanentMenuKey();
			boolean hasBackKey = KeyCharacterMap.deviceHasKey(KeyEvent.KEYCODE_BACK);

			if (!hasMenuKey && !hasBackKey) {
				//The device has a navigation bar
				Resources resources = context.getResources();

				int orientation = resources.getConfiguration().orientation;
				int resourceId;
				if (isTablet(context)) {
					resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ?
							"navigation_bar_height" : "navigation_bar_height_landscape", "dimen", "android");
				} else {
					resourceId = resources.getIdentifier(orientation == Configuration.ORIENTATION_PORTRAIT ?
							"navigation_bar_height" : "navigation_bar_width", "dimen", "android");
				}
				if (resourceId > 0) {
					return resources.getDimensionPixelSize(resourceId);
				}
			}
		}
		return result;
	}

	private static boolean isTablet(Context c) {
		return (c.getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK) >=
				Configuration.SCREENLAYOUT_SIZE_LARGE;
	}

	public static String getImei(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getDeviceId();
	}

	public static String getImsi(Context context) {
		TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		return telephonyManager.getSubscriberId();
	}

	/**
	 * 获取手机厂商和设备
	 *
	 * @return
	 */
	public static String getManufacturerModel() {
		return Build.MANUFACTURER + "-" + Build.MODEL;
	}

	public static String getDeviceId(Context context) {
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
		String uuid;

		uuid = sp.getString("device_id", null);
		if (uuid != null)
			return uuid;

		try {
			uuid = getUniqueId(context);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
			uuid = UUID.randomUUID().toString();
		}
		String md5 = EncryptUtil.md5(uuid);
		sp.edit().putString("device_id", md5).apply();
		return md5;
	}

	private static String getUniqueId(Context context) {
		final TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
		final String tmDevice, tmSerial, androidId;
		tmDevice = "" + tm.getDeviceId();
		tmSerial = "" + tm.getSimSerialNumber();
		androidId = "" + android.provider.Settings.Secure.getString(context.getContentResolver(), android.provider
				.Settings.Secure.ANDROID_ID);
		return tmDevice + tmSerial + androidId;
	}

	/**
	 * Return pseudo unique ID
	 *
	 * @return ID
	 */
	public static String getUniquePsuedoID() {
		// If all else fails, if the user does have lower than API 9 (lower
		// than Gingerbread), has reset their device or 'Secure.ANDROID_ID'
		// returns 'null', then simply the ID returned will be solely based
		// off their Android device information. This is where the collisions
		// can happen.
		// Thanks http://www.pocketmagic.net/?p=1662!
		// Try not to use DISPLAY, HOST or ID - these items could change.
		// If there are collisions, there will be overlapping data
		String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI
				.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL
				.length() % 10) + (Build.PRODUCT.length() % 10);

		// Thanks to @Roman SL!
		// http://stackoverflow.com/a/4789483/950427
		// Only devices with API >= 9 have android.os.Build.SERIAL
		// http://developer.android.com/reference/android/os/Build.html#SERIAL
		// If a user upgrades software or roots their device, there will be a duplicate entry
		String serial = null;
		try {
			serial = Build.class.getField("SERIAL").get(null).toString();

			// Go ahead and return the serial for api => 9
			return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
		} catch (Exception exception) {
			// String needs to be initialized
			serial = "serial"; // some value
		}

		// Thanks @Joe!
		// http://stackoverflow.com/a/2853253/950427
		// Finally, combine the values we have found by using the UUID class to create a unique identifier
		return new UUID(m_szDevIDShort.hashCode(), serial.hashCode()).toString();
	}
}
