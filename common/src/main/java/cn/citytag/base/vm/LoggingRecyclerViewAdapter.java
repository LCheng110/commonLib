package cn.citytag.base.vm;

import android.databinding.ViewDataBinding;
import android.support.annotation.LayoutRes;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import me.tatarka.bindingcollectionadapter2.BindingRecyclerViewAdapter;
import me.tatarka.bindingcollectionadapter2.OnListBinding;

/**
 * Created by yangfeng01 on 2018/3/9.
 */

public class LoggingRecyclerViewAdapter<T extends OnListBinding> extends BindingRecyclerViewAdapter<T> {

	private static final String TAG = "LoggingAdapter";

	@Override
	public ViewDataBinding onCreateBinding(LayoutInflater inflater, @LayoutRes int layoutId, ViewGroup viewGroup) {
		ViewDataBinding binding = super.onCreateBinding(inflater, layoutId, viewGroup);
		Log.d(TAG, "created binding: " + binding);
		return binding;
	}

	@Override
	public void onBindBinding(ViewDataBinding binding, int variableId, @LayoutRes int layoutRes, int position, T item) {
		super.onBindBinding(binding, variableId, layoutRes, position, item);
		Log.d(TAG, "bound binding: " + binding + " at position: " + position);
	}
}
