package cn.citytag.base.helpers.permission;

import android.app.FragmentManager;
import android.support.annotation.NonNull;


/**
 * Created by yangfeng01 on 2017/11/15.
 */

public abstract class BaseFrameworkPermissionHelper<T> extends PermissionHelper<T> {

	protected BaseFrameworkPermissionHelper(@NonNull T host) {
		super(host);
	}

	public abstract FragmentManager getFragmentManager();

	@Override
	public void showRequestPermissionRational(@NonNull String rational, int requestCode, @NonNull String... perms) {

	}
}
