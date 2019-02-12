package me.tatarka.bindingcollectionadapter2;

import android.databinding.ViewDataBinding;

/**
 * Created by yangfeng01 on 2018/3/26.
 */

public interface OnListBinding<T extends ViewDataBinding> {
	void onBinding(T binding);
}
