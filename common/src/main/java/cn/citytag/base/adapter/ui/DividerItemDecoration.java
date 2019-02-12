package cn.citytag.base.adapter.ui;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import cn.citytag.base.utils.UIUtils;

/**
 * Created by yangfeng01 on 2018/3/23.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

	private static final int[] ATTRS = new int[]{
			android.R.attr.listDivider
	};

	public static final int HORIZONTAL_LIST = LinearLayoutManager.HORIZONTAL;

	public static final int VERTICAL_LIST = LinearLayoutManager.VERTICAL;

	private Drawable divider;
	private int dividerHeight;	// 分割高度，单位：dp
	private int orientation;

	public DividerItemDecoration() {
		this(VERTICAL_LIST);
	}

	public DividerItemDecoration(int orientation) {
		//final TypedArray a = context.obtainStyledAttributes(ATTRS);
		//divider = a.getDrawable(0);
		//a.recycle();
		setOrientation(orientation);
	}

	public DividerItemDecoration(int orientation, int dividerHeight) {
		setOrientation(orientation);
		this.dividerHeight = UIUtils.dip2px(dividerHeight);
	}

	public void setOrientation(int orientation) {
		if (orientation != HORIZONTAL_LIST && orientation != VERTICAL_LIST) {
			throw new IllegalArgumentException("invalid orientation");
		}
		this.orientation = orientation;
	}

	public void setDivider(Drawable divider) {
		this.divider = divider;
	}

	public void setDividerHeight(int dividerHeight) {
		this.dividerHeight = UIUtils.dip2px(dividerHeight);
	}

	@Override
	public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
		if (divider == null)
			return;
		if (orientation == VERTICAL_LIST) {
			drawVertical(c, parent);
		} else {
			drawHorizontal(c, parent);
		}
	}

	public void drawVertical(Canvas c, RecyclerView parent) {
		final int left = parent.getPaddingLeft();
		final int right = parent.getWidth() - parent.getPaddingRight();

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
					.getLayoutParams();
			final int top = child.getBottom() + params.bottomMargin;
			final int bottom = top + divider.getIntrinsicHeight();
			divider.setBounds(left, top, right, bottom);
			divider.draw(c);
		}
	}

	public void drawHorizontal(Canvas c, RecyclerView parent) {
		final int top = parent.getPaddingTop();
		final int bottom = parent.getHeight() - parent.getPaddingBottom();

		final int childCount = parent.getChildCount();
		for (int i = 0; i < childCount; i++) {
			final View child = parent.getChildAt(i);
			final RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child
					.getLayoutParams();
			final int left = child.getRight() + params.rightMargin;
			final int right = left + divider.getIntrinsicHeight();
			divider.setBounds(left, top, right, bottom);
			divider.draw(c);
		}
	}

	@Override
	public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
		int dividerHeight = parent.getChildPosition(view) == 0 ? 0 : this.dividerHeight;
		if (orientation == VERTICAL_LIST) {
			outRect.set(0, dividerHeight, 0, 0);
		} else {
			outRect.set(dividerHeight, 0, 0, 0);
		}
	}
}
