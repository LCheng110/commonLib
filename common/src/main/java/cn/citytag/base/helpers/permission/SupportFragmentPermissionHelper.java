package cn.citytag.base.helpers.permission;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;


/**
 * Created by yangfeng01 on 2017/11/15.
 */

public class SupportFragmentPermissionHelper extends BaseSupportPermissionHelper<Fragment> {

	protected SupportFragmentPermissionHelper(@NonNull Fragment host) {
		super(host);
	}

	@Override
	public FragmentManager getSupportFragmentManager() {
		return getHost().getChildFragmentManager();
	}

	@Override
	public void directRequestPermissions(int requestCode, @NonNull String... perms) {
		getHost().requestPermissions(perms, requestCode);
	}

	@Override
	public boolean shouldShowRequestPermissionRational(@NonNull String perm) {
		return getHost().shouldShowRequestPermissionRationale(perm);
	}

	@Override
	public Context getContext() {
		return getHost().getActivity();
	}
}
