package cn.citytag.base.utils;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

/**
 * Created by yangfeng01 on 2017/12/21.
 */

public class Utils {

	/**
	 * 获取版本名称
	 */
	public static String getVersionName(Context context) {
		String versionName = "1.0";
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
}
