package cn.citytag.base.ui;

import android.graphics.Rect;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by yangfeng01 on 2017/11/20.
 */

public class TopSpaceItemDecoration extends RecyclerView.ItemDecoration {

	private int spanCount;
	private int topSpanDp;

	public TopSpaceItemDecoration(int spanCount, int topSpanDp) {
		this.spanCount = spanCount;
		this.topSpanDp = topSpanDp;
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		int position = parent.getChildAdapterPosition(view);
		if (position < spanCount) {
			outRect.top = (int) (parent.getContext().getResources().getDisplayMetrics().density * topSpanDp);
		}
	}

}
