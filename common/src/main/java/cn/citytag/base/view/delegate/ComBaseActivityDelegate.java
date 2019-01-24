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

import cn.citytag.base.app.delegate.ActivityDelegate;
import cn.citytag.base.app.receiver.AppBroadCastReceiver;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.helpers.permission.PermissionChecker;
import cn.citytag.base.utils.ActivityUtils;
import cn.citytag.base.utils.UIUtils;
import cn.citytag.base.view.base.ComBaseActivity;
import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.ISupportActivity;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportActivityDelegate;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

import static cn.citytag.base.app.receiver.BroadcastReceiverManager.ACTION_LOGOUT;
import static cn.citytag.base.constants.Constants.REQUEST_CODE_PERMISSION_SETTING;

/**
 * Created by yangfeng01 on 2017/11/9.
 */
public class ComBaseActivityDelegate implements ActivityDelegate, ISupportActivity {

	protected ComBaseActivity activity;
	protected String tag;
	private boolean hasFragment;
	private AppBroadCastReceiver receiver;

	final SupportActivityDelegate delegate;

	public ComBaseActivityDelegate(ComBaseActivity activity, String tag, boolean hasFragment) {
		this.activity = activity;
		this.tag = tag;
		this.hasFragment = hasFragment;
		 delegate = new SupportActivityDelegate(activity);
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
		delegate.onCreate(savedInstanceState);
	}

	@Override
	public void onPostCreate(@Nullable Bundle savedInstanceState) {
		delegate.onPostCreate(savedInstanceState);
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
		delegate.onDestroy();
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

	@Override
	public SupportActivityDelegate getSupportDelegate() {
		return delegate;
	}

	@Override
	public ExtraTransaction extraTransaction() {
		return delegate.extraTransaction();
	}

	@Override
	public FragmentAnimator getFragmentAnimator() {
		return delegate.getFragmentAnimator();
	}

	@Override
	public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
		delegate.setFragmentAnimator(fragmentAnimator);
	}

	@Override
	public FragmentAnimator onCreateFragmentAnimator() {
		return delegate.onCreateFragmentAnimator();
	}

	@Override
	public void post(Runnable runnable) {
		delegate.post(runnable);
	}

	@Override
	public void onBackPressed() {
		delegate.onBackPressed();
	}

	@Override
	public void onBackPressedSupport() {
		delegate.onBackPressedSupport();
	}

	@Override
	public boolean dispatchTouchEvent(MotionEvent ev) {
		return delegate.dispatchTouchEvent(ev);
	}

	/***************************************(Optional methods)******************************************************/
	public void loadRootFragment(int containerId, @NonNull ISupportFragment toFragment) {
		delegate.loadRootFragment(containerId, toFragment);
	}

	public void start(ISupportFragment toFragment) {
		delegate.start(toFragment);
	}

	/**
	 * @param launchMode Same as Activity's LaunchMode.
	 */
	public void start(ISupportFragment toFragment, int launchMode) {
		delegate.start(toFragment, launchMode);
	}

	/**
	 * Pop the fragment.
	 */
	public void pop() {
		delegate.pop();
	}

	/**
	 * Pop the last fragment transition from the manager's fragment
	 * back stack.
	 */
	public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
		delegate.popTo(targetFragmentClass, includeTargetFragment);
	}

	/**
	 * If you want to begin another FragmentTransaction immediately after popTo(), use this method.
	 * 如果你想在出栈后, 立刻进行FragmentTransaction操作，请使用该方法
	 */
	public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable) {
		delegate.popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable);
	}

	public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment, Runnable afterPopTransactionRunnable, int popAnim) {
		delegate.popTo(targetFragmentClass, includeTargetFragment, afterPopTransactionRunnable, popAnim);
	}

}
