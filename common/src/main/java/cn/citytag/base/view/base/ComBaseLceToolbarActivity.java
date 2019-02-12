package cn.citytag.base.view.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import java.util.List;

import cn.citytag.base.BR;
import cn.citytag.base.R;
import cn.citytag.base.model.StateModel;
import cn.citytag.base.utils.StringUtils;
import cn.citytag.base.vm.LceVM;
import cn.citytag.base.vm.OnLceCallback;
import cn.citytag.base.widget.CustomToolbar;
import cn.citytag.base.widget.StateLayout;

/**
 * Created by yangfeng01 on 2017/11/1.
 */

public abstract class ComBaseLceToolbarActivity<CVB extends ViewDataBinding, VM extends LceVM> extends ComBaseActivity<CVB, VM> implements StateLayout.OnRetryClickListener, OnLceCallback {

	/** LCE Activity 自带toolbar，控制状态切换的时候toolbar是否显示 */
	private CustomToolbar toolbar;
	private StateLayout stateLayout;
	private FrameLayout container;
	private TextView tvMenu;

	///**
	// * 子Activity中设置是否需要Toolbar须调用
	// *
	// * @param title
	// */
	//public void setupToolbar(String title, boolean isShowToolbar) {
	//	if (isShowToolbar) {
	//		toolbar.setVisibility(View.VISIBLE);
	//		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();
	//		lp.setMargins(0, (int) getResources().getDimension(R.dimen.toolbar_height), 0, 0);
	//		stateLayout.requestLayout();
	//		super.setupToolbar(toolbar, title);
	//	} else {
	//		toolbar.setVisibility(View.GONE);
	//		FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();
	//		lp.setMargins(0, 0, 0, 0);
	//		stateLayout.requestLayout();
	//	}
	//}

	public void setupToolbar(boolean visibility) {
		toolbar.setVisibility(visibility ? View.VISIBLE : View.GONE);
	}

	//protected abstract void onViewModelCreated();

	public boolean setLabelAsToolbarTitle() {
		return true;
	}

	@Override
	protected void initView(@Nullable Bundle savedInstanceState) {
		cn.citytag.base.databinding.ActivityBaseLceComBinding lceBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_lce_com);
		container = lceBinding.flContent;
		tvMenu = lceBinding.tvRight;
		toolbar = lceBinding.toolbar;
		toolbar.setVisibility(View.GONE);
	    stateLayout = lceBinding.stateLayout;
		stateLayout.setOnRetryClickListener(this);
		int layoutId = getLayoutResId();
		if (layoutId > 0) {
			cvb = DataBindingUtil.inflate(LayoutInflater.from(this), layoutId, container, true);
			container.removeAllViews();
			container.addView(cvb.getRoot());
			StateModel stateModel = new StateModel();
			stateModel.setCallback(this);
			viewModel = createViewModel();
			viewModel.initStateModel(stateModel);
			cvb.setVariable(BR.baseVM, viewModel);
			//if (setLabelAsToolbarTitle()) {
			//setupToolbar(getStatName(), true);
			//}
			afterOnCreate(savedInstanceState);
		} else {
			throw new IllegalArgumentException("layout is not a inflate");
		}

	}

	public TextView getTvMenu(){
		return tvMenu;
	}

	public CustomToolbar getToolbar() {
		return toolbar;
	}

	@Override
	public void onRetryClick() {

	}

	public void setEmptyDesc(String emptyDesc) {
		stateLayout.setEmptyDesc(emptyDesc);
	}

	public void setEmptyView(View emptyView) {
		stateLayout.setEmptyView(emptyView);
	}

	public void setErrorImdId(int imgId) {
		stateLayout.setErrorImdId(imgId);
	}

	public void setErrorDesc(String desc) {
		stateLayout.setEmptyDesc(desc);
	}

	@Override
	public boolean isLoading() {
		return stateLayout.isLoading();
	}

	@Override
	public void onLoading() {
		stateLayout.showLoading();
	}

	@Override
	public void onLoading(List<Integer> skipIds) {
		stateLayout.showLoading(skipIds);
	}

	@Override
	public void onContent() {
		stateLayout.showContent();
	}

	@Override
	public void onContent(List<Integer> skipIds) {
		stateLayout.showContent(skipIds);
	}

	@Override
	public void onEmpty() {
		stateLayout.showEmpty();
	}

	@Override
	public void onEmpty(List<Integer> skipIds) {
		stateLayout.showEmpty(skipIds);
	}

	@Override
	public void onError() {
		stateLayout.showError();
	}

	@Override
	public void onError(List<Integer> skipIds) {
		stateLayout.showError(skipIds);
	}

	public void setupToolbar(String title) {
		setupToolbar(toolbar, title);
	}

	@Override
	public void setupToolbar(Toolbar toolbar, String title) {
		boolean isShowToolbar = StringUtils.isNotEmpty(title);
		if (isShowToolbar) {
			toolbar.setVisibility(View.VISIBLE);
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();
			lp.setMargins(0, (int) getResources().getDimension(R.dimen.toolbar_height), 0, 0);
			stateLayout.requestLayout();
			super.setupToolbar(toolbar, title);
		} else {
			toolbar.setVisibility(View.GONE);
			FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) container.getLayoutParams();
			lp.setMargins(0, 0, 0, 0);
			stateLayout.requestLayout();
		}
	}

	public void setupMenu(String menuName, View.OnClickListener listener) {

	}

	protected  void  setStateLayout(StateLayout stateLayout){
		this.stateLayout = stateLayout ;
	}

}
