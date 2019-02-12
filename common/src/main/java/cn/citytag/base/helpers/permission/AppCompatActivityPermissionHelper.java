package cn.citytag.base.helpers.permission;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;


/**
 * Created by yangfeng01 on 2017/11/15.
 *
 * Permissions helper for {@link AppCompatActivity}.
 */
class AppCompatActivityPermissionHelper extends BaseSupportPermissionHelper<AppCompatActivity> {


	protected AppCompatActivityPermissionHelper(@NonNull AppCompatActivity host) {
		super(host);
	}

	@Override
	public FragmentManager getSupportFragmentManager() {
		return getHost().getSupportFragmentManager();
	}

	@Override
	public void directRequestPermissions(int requestCode, @NonNull String... perms) {
		ActivityCompat.requestPermissions(getHost(), perms, requestCode);
	}

	@Override
	public boolean shouldShowRequestPermissionRational(@NonNull String perm) {
		return ActivityCompat.shouldShowRequestPermissionRationale(getHost(), perm);
	}

	@Override
	public Context getContext() {
		return getHost();
	}
}
