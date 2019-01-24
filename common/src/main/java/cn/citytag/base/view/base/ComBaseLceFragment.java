package cn.citytag.base.view.base;

import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.List;

import cn.citytag.base.BR;
import cn.citytag.base.OnLceCallback;
import cn.citytag.base.R;
import cn.citytag.base.StateModel;
import cn.citytag.base.vm.LceVM;
import cn.citytag.base.widget.StateLayout;

/**
 * Created by yangfeng01 on 2018/4/21.
 */
public abstract class ComBaseLceFragment<CVB extends ViewDataBinding, VM extends LceVM> extends ComBaseFragment<CVB, VM> implements StateLayout.OnRetryClickListener, OnLceCallback {

	private StateLayout stateLayout;

	@Nullable
	@Override
	public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
			savedInstanceState) {
		cn.citytag.base.databinding.FragmentBaseLceComBinding lceBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_base_lce_com, container, false);
		FrameLayout content = lceBinding.flContent;
		stateLayout = lceBinding.stateLayout;
		stateLayout.setOnRetryClickListener(this);
		int layoutId = getLayoutResId();
		if (layoutId > 0) {
			cvb = DataBindingUtil.inflate(inflater, layoutId, container, false);
			content.removeAllViews();
			content.addView(cvb.getRoot());
			StateModel stateModel = new StateModel();
			stateModel.setCallback(this);
			viewModel = createViewModel();
			viewModel.initStateModel(stateModel);
			cvb.setVariable(BR.viewModel, viewModel);
			rootView = lceBinding.getRoot();
			afterOnCreateView(inflater, container, savedInstanceState);
		} else {
			throw new IllegalArgumentException("layout is not a inflate");
		}
		return rootView;
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
}
