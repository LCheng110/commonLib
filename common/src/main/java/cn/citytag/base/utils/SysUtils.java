package cn.citytag.base.utils;

import android.content.Context;

import java.lang.reflect.Field;

/**
 * Created by yangfeng01 on 2017/11/14.
 *
 * 由于配置项写在app的module里，要在common module里调用可使用反射的方式
 */

public class SysUtils {

	private static final String TAG = SysUtils.class.getSimpleName();

	/**
	 * Gets a field from the project's BuildConfig. This is useful when, for example, flavors
	 * are used at the project level to set custom fields.
	 *
	 * @param context   Used to find the correct file
	 * @param fieldName The name of the field-to-access
	 * @return The value of the field, or {@code null} if the field is not found.
	 */
	private static Object getBuildConfigValue(Context context, String fieldName) {
		int resId = context.getResources().getIdentifier("build_config_package", "string", context.getPackageName());
		L.i(TAG, "context.getPackageName == " + context.getPackageName());
		L.d(TAG, context.getString(resId));
		Class<?> clazz = null;
		try {
			clazz = Class.forName(context.getString(resId) + ".BuildConfig");
			Field field = clazz.getField(fieldName);
			return field.get(null);
		} catch (Exception e) {
			e.printStackTrace();
			L.e(TAG, e.getMessage());
		}
		return null;
	}

	/**
	 * Android studio 多个module时，非当前运行的module获取BuildConfig.DEBUG都是false
	 * 这里通过获取当前应用的context下的BuildConfig来判断才正确
	 *
	 * @param context
	 * @return
	 */
	public static boolean isDebug(Context context) {
		Object object = getBuildConfigValue(context, "DEBUG");
		return object != null && ((Boolean) object);
	}

	public static String getBuildConfigStr(Context context, String buildConfigField) {
		Object object = getBuildConfigValue(context, buildConfigField);
		return (String) object;
	}

	public static Integer getBuildConfigInt(Context context, String buildConfigField) {
		Object object = getBuildConfigValue(context, buildConfigField);
		return (Integer) object;
	}


}
