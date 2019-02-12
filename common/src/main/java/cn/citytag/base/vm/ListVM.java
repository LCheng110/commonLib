package cn.citytag.base.vm;

import android.databinding.ViewDataBinding;

import me.tatarka.bindingcollectionadapter2.OnListBinding;

/**
 * Created by yangfeng01 on 2017/12/9.
 *
 * RecyclerView中每一条item对应的listViewModel都应该继承ListVM，而不是BaseVM或BaseRvVM
 */

public abstract class ListVM<T extends ViewDataBinding> implements OnListBinding<T> {

	private int itemViewType;

	public ListVM() {

	}

	public ListVM(int itemViewType) {
		this.itemViewType = itemViewType;
	}

	public int getViewType() {
		return 0;
	}

	@Override
	public void onBinding(T binding) {

	}
}
