package cn.citytag.base.view.base;

import android.app.Activity;
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

import com.trello.rxlifecycle2.components.support.RxFragment;

import java.util.List;

import cn.citytag.base.BR;
import cn.citytag.base.vm.BaseVM;
import cn.citytag.base.app.IStatLabel;
import cn.citytag.base.helpers.permission.PermissionChecker;

/**
 * Created by yangfeng01 on 2017/11/8.
 */

public abstract class ComBaseFragment<CVB extends ViewDataBinding, VM extends BaseVM> extends RxFragment implements IStatLabel, PermissionChecker.PermissionCallbacks {

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
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.activity = (FragmentActivity) activity;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
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
            cvb.setVariable(BR.baseVM, viewModel);
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
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (viewModel != null) {
            viewModel.detach();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        afterDestroy();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
    }

}
