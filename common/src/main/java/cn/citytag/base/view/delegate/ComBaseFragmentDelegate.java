package cn.citytag.base.view.delegate;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.animation.Animation;

import com.umeng.analytics.MobclickAgent;

import java.util.List;

import cn.citytag.base.app.delegate.FragmentDelegate;
import cn.citytag.base.helpers.permission.PermissionChecker;
import cn.citytag.base.utils.L;
import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragmentDelegate;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * Created by yangfeng01 on 2017/11/9.
 */

public class ComBaseFragmentDelegate implements FragmentDelegate, ISupportFragment, PermissionChecker.PermissionCallbacks {

	final SupportFragmentDelegate delegate;
	private ISupportFragment fragment;
	private String tag;

	public ComBaseFragmentDelegate(ISupportFragment fragment, String tag) {
		this.fragment = fragment;
		this.tag = tag;
		delegate = new SupportFragmentDelegate(fragment);
	}

	public FragmentActivity getActivity() {
		return delegate.getActivity();
	}

	@Override
	public void onAttach(Activity activity) {
		delegate.onAttach(activity);
	}

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		delegate.onCreate(savedInstanceState);
	}

	@Override
	public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
		return delegate.onCreateAnimation(transit, enter, nextAnim);
	}

	public void onActivityCreated(@Nullable Bundle savedInstanceState) {
		delegate.onActivityCreated(savedInstanceState);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		delegate.onSaveInstanceState(outState);
	}

	@Override
	public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {

	}

	@Override
	public void onStart() {

	}

	@Override
	public void onViewStateRestored(@Nullable Bundle savedInstanceState) {

	}

	@Override
	public void onResume() {
		MobclickAgent.onPageStart(tag); //统计页面，"MainScreen"为页面名称，可自定义
		delegate.onResume();
	}

	@Override
	public void onPause() {
		delegate.onPause();
		MobclickAgent.onPageEnd(tag); //统计页面，"MainScreen"为页面名称，可自定义
	}

	@Override
	public void onStop() {

	}

	@Override
	public void onDestroyView() {
		delegate.onDestroyView();
	}

	@Override
	public void onDestroy() {
		delegate.onDestroy();
	}

	@Override
	public void onHiddenChanged(boolean hidden) {
		delegate.onHiddenChanged(hidden);
	}

	@Override
	public void setUserVisibleHint(boolean isVisibleToUser) {
		delegate.setUserVisibleHint(isVisibleToUser);
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
		// EasyPermissions handles the request result.
		PermissionChecker.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
	}

	@Override
	public void onPermissionsGranted(int requestCode, List<String> perms) {
		L.d(tag, "onPermissionsGranted == " + requestCode + ", granted permissions == " + perms);
	}

	@Override
	public void onPermissionsDenied(int requestCode, List<String> perms) {
		L.d(tag, "onPermissionsDenied == " + requestCode + ", denied permissions == " + perms);
	}

	@Override
	public SupportFragmentDelegate getSupportDelegate() {
		return delegate;
	}

	@Override
	public ExtraTransaction extraTransaction() {
		return delegate.extraTransaction();
	}

	@Override
	public void enqueueAction(Runnable runnable) {
		delegate.enqueueAction(runnable);
	}

	@Override
	public void post(Runnable runnable) {
		delegate.post(runnable);
	}

	@Override
	public void onEnterAnimationEnd(@Nullable Bundle savedInstanceState) {
		delegate.onEnterAnimationEnd(savedInstanceState);
	}

	@Override
	public void onLazyInitView(@Nullable Bundle savedInstanceState) {
		delegate.onLazyInitView(savedInstanceState);
	}

	@Override
	public void onSupportVisible() {
		delegate.onSupportVisible();
	}

	@Override
	public void onSupportInvisible() {
		delegate.onSupportInvisible();
	}

	@Override
	public boolean isSupportVisible() {
		return delegate.isSupportVisible();
	}

	@Override
	public FragmentAnimator onCreateFragmentAnimator() {
		return delegate.onCreateFragmentAnimator();
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
	public void setFragmentResult(int resultCode, Bundle bundle) {
		delegate.setFragmentResult(resultCode, bundle);
	}

	@Override
	public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
		delegate.onFragmentResult(requestCode, resultCode, data);
	}

	@Override
	public void onNewBundle(Bundle args) {
		delegate.onNewBundle(args);
	}

	@Override
	public void putNewBundle(Bundle newBundle) {
		delegate.putNewBundle(newBundle);
	}

	@Override
	public boolean onBackPressedSupport() {
		return delegate.onBackPressedSupport();
	}

	/******************************************************************************************************************/

	/**
	 * 隐藏软键盘
	 */
	public void hideSoftInput() {
		delegate.hideSoftInput();
	}

	/**
	 * 显示软键盘，调用该方法后，会在onPause时自动隐藏软键盘
	 */
	public void showSoftInput(View view) {
		delegate.showSoftInput(view);
	}

	/**
	 * 加载根Fragment，即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
	 * @param containerId
	 * @param toFragment
	 */
	public void loadRootFragment(int containerId, ISupportFragment toFragment) {
		delegate.loadRootFragment(containerId, toFragment);
	}

	public void loadRootFragment(int containerId, ISupportFragment toFragment, boolean addToBackStack, boolean allowAnim) {
		delegate.loadRootFragment(containerId, toFragment, addToBackStack, allowAnim);
	}

	public void start(ISupportFragment toFragment) {
		delegate.start(toFragment);
	}

	/**
	 * @param launchMode Similar to Activity's LaunchMode.
	 */
	public void start(ISupportFragment toFragment, int launchMode) {
		delegate.start(toFragment, launchMode);
	}

	/**
	 * Launch an fragment for which you would like a result when it popped.
	 */
	public void startForResult(ISupportFragment toFragment, int requestCode) {
		delegate.startForResult(toFragment, requestCode);
	}

	/**
	 * Start the target Fragment and pop itself
	 */
	public void startWithPop(ISupportFragment toFragment) {
		delegate.startWithPop(toFragment);
	}

	public void replaceFragment(ISupportFragment toFragment, boolean addToBackStack) {
		delegate.replaceFragment(toFragment, addToBackStack);
	}

	public void pop() {
		delegate.pop();
	}

	public void popChild() {
		delegate.popChild();
	}

	/**
	 * Pop the last fragment transition from the manager's fragment
	 * back stack.
	 * <p>
	 * 出栈到目标fragment
	 * </p>
	 * @param targetFragmentClass 目标fragment
	 * @param includeTargetFragment 是否包含该fragment
	 */
	public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
		delegate.popTo(targetFragmentClass, includeTargetFragment);
	}

}
