package cn.citytag.base.helpers.permission;

import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import java.util.List;

import cn.citytag.base.utils.StringUtils;


/**
 * Created by yangfeng01 on 2017/11/15.
 */

public abstract class PermissionHelper<T> {

	private static final String TAG = "PermissionHelper";

	private T host;

	/**
	 * 直接去申请系统权限
	 *
	 * @param requestCode
	 * @param perms
	 */
	public abstract void directRequestPermissions(int requestCode, @NonNull String... perms);

	/**
	 * 是否需要做原理性解释弹框
	 * 1.requestPermissions时，shouldShowRequestPermissionRational == true，则弹框原理性解释
	 * 2.onRequestPermissionsResult回调时，shouldShowRequestPermissionRational == false，则弹框引导去设置里打开
	 *
	 * @param perm
	 * @return
	 */
	public abstract boolean shouldShowRequestPermissionRational(@NonNull String perm);

	public abstract void showRequestPermissionRational(@NonNull String rational,
													   int requestCode,
													   @NonNull String... perms);

	public abstract Context getContext();

	protected PermissionHelper(@NonNull T host) {
		this.host = host;
	}

	public T getHost() {
		return host;
	}

	@NonNull
	public static PermissionHelper newInstance(Activity host) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return new LowApiPermissionHelper(host);
		}

		if (host instanceof AppCompatActivity) {
			return new AppCompatActivityPermissionHelper((AppCompatActivity) host);
		} else {
			return new ActivityPermissionHelper(host);
		}
	}

	@NonNull
	public static PermissionHelper newInstance(Fragment host) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return new LowApiPermissionHelper(host);
		}

		return new SupportFragmentPermissionHelper(host);
	}

	@NonNull
	public static PermissionHelper newInstance(android.app.Fragment host) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
			return new LowApiPermissionHelper(host);
		}

		return new FrameworkFragmentPermissionHelper(host);
	}

	public void requestPermissions(String rational, int requestCode, @NonNull String... perms) {
		if (StringUtils.isEmpty(rational)) {
			directRequestPermissions(requestCode, perms);
		} else {
			if (shouldShowRational(perms)) {
				showRequestPermissionRational(rational, requestCode, perms);
			} else {
				directRequestPermissions(requestCode, perms);
			}
		}
	}

	private boolean shouldShowRational(String... perms) {
		for (String perm : perms) {
			if (shouldShowRequestPermissionRational(perm)) {
				return true;
			}
		}
		return false;
	}

	public boolean somePermissionPermanentlyDenied(@NonNull List<String> perms) {
		for (String deniedPermission : perms) {
			if (permissionPermanentlyDenied(deniedPermission)) {
				return true;
			}
		}

		return false;
	}

	public boolean permissionPermanentlyDenied(@NonNull String perms) {
		return !shouldShowRequestPermissionRational(perms);
	}

}
