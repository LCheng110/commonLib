package cn.citytag.base.adapter.ui;

import android.graphics.Rect;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.citytag.base.utils.L;
import cn.citytag.base.utils.UIUtils;

/**
 * Created by yangfeng01 on 2017/10/24.
 */

public class GridSpaceItemDecoration extends RecyclerView.ItemDecoration {

    private static final String TAG = "GridSpaceItemDecoration";
    private int spanCount; //
    private int spacing;
    private boolean includeEdge;

    public GridSpaceItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = UIUtils.dip2px(spacing);
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // item position
        int childCount = parent.getAdapter().getItemCount();
        int itemPosition = ((RecyclerView.LayoutParams) view.getLayoutParams()).getViewLayoutPosition();
        int column = position % spanCount; // item column

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)
            L.d(TAG, "position == " + position + " , outRect.left == " + outRect.left + " , outRect.right == " + outRect.right + " , view.getLeft == " + view.getLeft());

            int[] locations = new int[2];
            view.getLocationOnScreen(locations);
            L.i(TAG, "position == " + position + " , child view location on screen left == " + locations[0] + " top == " + locations[1]);
            outRect.top = spacing;
        } else {
            outRect.left = column * spacing / spanCount;
            outRect.right = spacing - (column + 1) * spacing / spanCount;
            outRect.top = spacing;
        }
    }

    private boolean isLastRaw(RecyclerView parent, int pos, int spanCount,
                              int childCount) {
        RecyclerView.LayoutManager layoutManager = parent.getLayoutManager();
        if (layoutManager instanceof GridLayoutManager) {
            return pos + spanCount >= childCount;
        }
        return false;
    }

}
