package cn.citytag.base.utils;

import android.content.Context;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;

/**
 * Created by yangfeng01 on 2018/5/6.
 */
public class CatchGridLayoutManager extends GridLayoutManager {

	public CatchGridLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
	}

	public CatchGridLayoutManager(Context context, int spanCount) {
		super(context, spanCount);
	}

	public CatchGridLayoutManager(Context context, int spanCount, int orientation, boolean reverseLayout) {
		super(context, spanCount, orientation, reverseLayout);
	}

	@Override
	public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
		try {
			super.onLayoutChildren(recycler, state);
		} catch (IndexOutOfBoundsException e) {
			e.printStackTrace();
		}
	}
}
