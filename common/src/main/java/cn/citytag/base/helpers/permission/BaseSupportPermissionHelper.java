package cn.citytag.base.helpers.permission;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;

import java.util.Arrays;

import cn.citytag.base.command.ReplyCommand;
import cn.citytag.base.widget.dialog.ConfirmDialog;
import io.reactivex.functions.Action;

/**
 * Created by yangfeng01 on 2017/11/15.
 * <p>
 * Implementation of {@link PermissionHelper} for Support Library host classes.
 */
public abstract class BaseSupportPermissionHelper<T> extends PermissionHelper<T> {

	private ConfirmDialog confirmDialog;

	protected BaseSupportPermissionHelper(@NonNull T host) {
		super(host);
	}

	public abstract FragmentManager getSupportFragmentManager();

	@Override
	public void showRequestPermissionRational(@NonNull String rational, final int requestCode, @NonNull final String... perms) {
		// implement
		confirmDialog = ConfirmDialog.newInstance(rational)
				.confirm(new ReplyCommand(new Action() {
					@Override
					public void run() {
						if (getHost() instanceof Fragment) {
							PermissionHelper.newInstance((Fragment) getHost()).directRequestPermissions(requestCode,
									perms);
						} else if (getHost() instanceof android.app.Fragment) {
							PermissionHelper.newInstance((android.app.Fragment) getHost()).directRequestPermissions
									(requestCode, perms);
						} else if (getHost() instanceof Activity) {
							PermissionHelper.newInstance((Activity) getHost()).directRequestPermissions(requestCode,
									perms);
						} else {
							throw new RuntimeException("Host must be an Activity or Fragment!");
						}
					}
				}))
				.cancel(new ReplyCommand(new Action() {
					@Override
					public void run() {
						if (confirmDialog.getParentFragment() != null && confirmDialog.getParentFragment() instanceof
								PermissionChecker.PermissionCallbacks) {
							((PermissionChecker.PermissionCallbacks) confirmDialog.getParentFragment())
									.onPermissionsDenied(requestCode, Arrays.asList(perms));
						} else if (getHost() instanceof PermissionChecker.PermissionCallbacks) {
							((PermissionChecker.PermissionCallbacks) getHost()).onPermissionsDenied(requestCode,
									Arrays.asList(perms));
						}
					}
				}));

		confirmDialog.showAllowingStateLoss(getSupportFragmentManager(), ConfirmDialog.TAG);
	}

}
