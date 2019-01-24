package cn.citytag.base.view.base;

import android.app.Activity;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;

import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.List;

import cn.citytag.base.BR;
import cn.citytag.base.app.IStatLabel;
import cn.citytag.base.helpers.permission.PermissionChecker;
import cn.citytag.base.view.delegate.ComBaseFragmentDelegate;
import cn.citytag.base.vm.BaseVM;
import me.yokeyword.fragmentation.ExtraTransaction;
import me.yokeyword.fragmentation.ISupportFragment;
import me.yokeyword.fragmentation.SupportFragmentDelegate;
import me.yokeyword.fragmentation.SupportHelper;
import me.yokeyword.fragmentation.anim.FragmentAnimator;

/**
 * Created by yangfeng01 on 2017/11/8.
 */

public abstract class ComBaseFragment<CVB extends ViewDataBinding, VM extends BaseVM> extends RxFragment implements ISupportFragment, IStatLabel, PermissionChecker.PermissionCallbacks {

    protected String tag = getClass().getSimpleName();
    protected View rootView;

    /**
     * 具体的子类Fragment CVB用具体的代替
     */
    protected CVB cvb;

    /**
     * 具体的子类Fragment VM用具体的代替
     */
    protected VM viewModel;

    protected FragmentActivity activity;

    private ComBaseFragmentDelegate delegate;

    protected abstract int getLayoutResId();

    protected abstract VM createViewModel();

    protected abstract void afterOnCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState);

    /**
     * 对应Activity StatusBarUtil.setTransparentForImageViewInFragment(this, null);的时候
     * Fragment顶部加一个fake status bar
     */

    protected void afterDestroy() {

    }

    protected void setToolbarColor() {

    }

    public <T> T findViewById(@IdRes int id) {
        return (T) rootView.findViewById(id);
    }

    /**
     * 设置Toolbar的公共方法
     *
     * @param toolbar
     */
    protected void setupToolbar(Toolbar toolbar) {
        AppCompatActivity activity = ((AppCompatActivity) getActivity());
        activity.setSupportActionBar(toolbar);
        ActionBar actionBar = activity.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    public ComBaseFragment() {
        delegate = new ComBaseFragmentDelegate(this, tag);
    }

    @Override
    public SupportFragmentDelegate getSupportDelegate() {
        return delegate.getSupportDelegate();
    }

    @Override
    public ExtraTransaction extraTransaction() {
        return delegate.extraTransaction();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        delegate.onAttach(activity);
        this.activity = delegate.getActivity();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        delegate.onCreate(savedInstanceState);
    }

    @Override
    public Animation onCreateAnimation(int transit, boolean enter, int nextAnim) {
        return delegate.onCreateAnimation(transit, enter, nextAnim);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        delegate.onActivityCreated(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        int layoutResId = getLayoutResId();
        if (layoutResId > 0) {
            cvb = DataBindingUtil.inflate(inflater, layoutResId, container, false);
            rootView = cvb.getRoot();
            viewModel = createViewModel();
            cvb.setVariable(BR.viewModel, viewModel);
            setToolbarColor();
            afterOnCreateView(inflater, container, savedInstanceState);
        } else {
            throw new IllegalArgumentException("layout is not a inflate");
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        delegate.onViewCreated(view, savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();
        delegate.onStart();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
        delegate.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
        delegate.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        delegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        delegate.onPermissionsGranted(requestCode, perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        delegate.onPermissionsDenied(requestCode, perms);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        delegate.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        delegate.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        delegate.onPause();
    }

    @Override
    public void onDestroyView() {
        delegate.onDestroyView();
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.detach();
        }
    }

    @Override
    public void onDestroy() {
        delegate.onDestroy();
        super.onDestroy();
        afterDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        delegate.onHiddenChanged(hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        delegate.setUserVisibleHint(isVisibleToUser);
    }

    @Deprecated
    @Override
    public void enqueueAction(Runnable runnable) {
        delegate.enqueueAction(runnable);
    }

    /**
     * Causes the Runnable r to be added to the action queue.
     * <p>
     * The runnable will be run after all the previous action has been run.
     * <p>
     * 前面的事务全部执行后 执行该Action
     */
    @Override
    public void post(Runnable runnable) {
        delegate.post(runnable);
    }

    /**
     * Called when the enter-animation end.
     * 入栈动画 结束时,回调
     */
    @Override
    public void onEnterAnimationEnd(@Nullable Bundle savedInstanceState) {
        delegate.onEnterAnimationEnd(savedInstanceState);
    }

    /**
     * Lazy initial，Called when fragment is first called.
     * <p>
     * 同级下的 懒加载 ＋ ViewPager下的懒加载  的结合回调方法
     */
    @Override
    public void onLazyInitView(@Nullable Bundle savedInstanceState) {
        delegate.onLazyInitView(savedInstanceState);
    }

    /**
     * Called when the fragment is visible.
     * 当Fragment对用户可见时回调
     * <p>
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    @Override
    public void onSupportVisible() {
        delegate.onSupportVisible();
    }

    /**
     * Called when the fragment is invivible.
     * <p>
     * Is the combination of  [onHiddenChanged() + onResume()/onPause() + setUserVisibleHint()]
     */
    @Override
    public void onSupportInvisible() {
        delegate.onSupportInvisible();
    }

    /**
     * Return true if the fragment has been supportVisible.
     */
    @Override
    final public boolean isSupportVisible() {
        return delegate.isSupportVisible();
    }

    /**
     * Set fragment animation with a higher priority than the ISupportActivity
     * 设定当前Fragmemt动画,优先级比在SupportActivity里高
     */
    @Override
    public FragmentAnimator onCreateFragmentAnimator() {
        return delegate.onCreateFragmentAnimator();
    }

    /**
     * 获取设置的全局动画 copy
     *
     * @return FragmentAnimator
     */
    @Override
    public FragmentAnimator getFragmentAnimator() {
        return delegate.getFragmentAnimator();
    }

    /**
     * 设置Fragment内的全局动画
     */
    @Override
    public void setFragmentAnimator(FragmentAnimator fragmentAnimator) {
        delegate.setFragmentAnimator(fragmentAnimator);
    }

    /**
     * 按返回键触发,前提是SupportActivity的onBackPressed()方法能被调用
     *
     * @return false则继续向上传递, true则消费掉该事件
     */
    @Override
    public boolean onBackPressedSupport() {
        return delegate.onBackPressedSupport();
    }

    /**
     * 类似 {@link Activity#setResult(int)}
     * <p>
     * Similar to {@link Activity#setResult(int, Intent)}
     *
     * @see #startForResult(ISupportFragment, int)
     */
    @Override
    public void setFragmentResult(int resultCode, Bundle bundle) {
        delegate.setFragmentResult(resultCode, bundle);
    }

    /**
     * 类似  {@link Activity#onActivityResult(int, int, Intent)}
     * <p>
     * Similar to {@link Activity#onActivityResult(int, int, Intent)}
     *
     * @see #startForResult(ISupportFragment, int)
     */
    @Override
    public void onFragmentResult(int requestCode, int resultCode, Bundle data) {
        delegate.onFragmentResult(requestCode, resultCode, data);
    }

    /**
     * 在start(TargetFragment,LaunchMode)时,启动模式为SingleTask/SingleTop, 回调TargetFragment的该方法
     * 类似 {@link Activity#onNewIntent(Intent)}
     * <p>
     * Similar to {@link Activity#onNewIntent(Intent)}
     *
     * @param args putNewBundle(Bundle newBundle)
     * @see #start(ISupportFragment, int)
     */
    @Override
    public void onNewBundle(Bundle args) {
        delegate.onNewBundle(args);
    }

    /**
     * 添加NewBundle,用于启动模式为SingleTask/SingleTop时
     *
     * @see #start(ISupportFragment, int)
     */
    @Override
    public void putNewBundle(Bundle newBundle) {
        delegate.putNewBundle(newBundle);
    }


    /****************************************(Optional methods)******************************************************/
    // 自定制Support时，可移除不必要的方法

    /**
     * 隐藏软键盘
     */
    protected void hideSoftInput() {
        delegate.hideSoftInput();
    }

    /**
     * 显示软键盘,调用该方法后,会在onPause时自动隐藏软键盘
     */
    protected void showSoftInput(final View view) {
        delegate.showSoftInput(view);
    }

    /**
     * 加载根Fragment, 即Activity内的第一个Fragment 或 Fragment内的第一个子Fragment
     *
     * @param containerId 容器id
     * @param toFragment  目标Fragment
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
    public void start(final ISupportFragment toFragment, @LaunchMode int launchMode) {
        delegate.start(toFragment, launchMode);
    }

    /**
     * Launch an fragment for which you would like a result when it poped.
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

    /**
     * Pop the child fragment.
     */
    public void popChild() {
        delegate.popChild();
    }

    /**
     * Pop the last fragment transition from the manager's fragment
     * back stack.
     * <p>
     * 出栈到目标fragment
     *
     * @param targetFragmentClass   目标fragment
     * @param includeTargetFragment 是否包含该fragment
     */
    public void popTo(Class<?> targetFragmentClass, boolean includeTargetFragment) {
        delegate.popTo(targetFragmentClass, includeTargetFragment);
    }

    /**
     * 获取栈内的fragment对象
     */
    public <T extends ISupportFragment> T findChildFragment(Class<T> fragmentClass) {
        return SupportHelper.findFragment(getChildFragmentManager(), fragmentClass);
    }

}
