package cn.citytag.base.view.delegate;

import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.view.MotionEvent;

import com.umeng.analytics.MobclickAgent;

import cn.citytag.base.view.base.ComBaseActivity;
import cn.citytag.base.app.delegate.ActivityDelegate;
import cn.citytag.base.app.receiver.AppBroadCastReceiver;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.helpers.permission.PermissionChecker;
import cn.citytag.base.utils.ActivityUtils;
import cn.citytag.base.utils.UIUtils;

import static cn.citytag.base.constants.Constants.REQUEST_CODE_PERMISSION_SETTING;
import static cn.citytag.base.app.receiver.BroadcastReceiverManager.ACTION_LOGOUT;

/**
 * Created by yangfeng01 on 2017/11/9.
 */
public class ComBaseActivityDelegate implements ActivityDelegate {

	protected ComBaseActivity activity;
	protected String tag;
	private boolean hasFragment;
	private AppBroadCastReceiver receiver;

	public ComBaseActivityDelegate(ComBaseActivity activity, String tag, boolean hasFragment) {
		this.activity = activity;
		this.tag = tag;
		this.hasFragment = hasFragment;
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
			UIUtils.toastMessage("系统版本过低，暂不支持");
			activity.finish();
		}
		ActivityUtils.push(activity);
		BaseConfig.setCurrentActivity(activity);
		registerAppReceiver();
	}

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
	public void onNewIntent(Intent intent) {
		BaseConfig.setCurrentActivity(activity);
	}

	@Override
	public void onStart() {

	}

	@Override
	public void onResume() {
		BaseConfig.setCurrentActivity(activity);
		if (!hasFragment) {
			MobclickAgent.onPageStart(tag);	// 统计页面
		}
		MobclickAgent.onResume(activity);	// 统计时长
	}

	@Override
	public void onPause() {
		if (!hasFragment) {
			MobclickAgent.onPageEnd(tag);	// 统计页面
		}
		MobclickAgent.onPause(activity);	// 统计时长
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onDestroy() {
		ActivityUtils.remove(activity);
		if (activity != null && activity.equals(BaseConfig.getCurrentActivity())) {
			BaseConfig.setCurrentActivity(null);
		}
		unregisterAppReceiver();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_CODE_PERMISSION_SETTING) {
			// Do something after user returned from app settings screen, like showing a Toast.
		}
	}

	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
			grantResults, PermissionChecker.PermissionCallbacks callbacks) {
		// PermissionChecker handles the request result.
		PermissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults, callbacks);
	}

	private void registerAppReceiver() {
		receiver = new AppBroadCastReceiver();
		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(ACTION_LOGOUT);
		LocalBroadcastManager.getInstance(activity).registerReceiver(receiver, intentFilter);
	}

	private void unregisterAppReceiver() {
		LocalBroadcastManager.getInstance(activity).unregisterReceiver(receiver);
	}

}
