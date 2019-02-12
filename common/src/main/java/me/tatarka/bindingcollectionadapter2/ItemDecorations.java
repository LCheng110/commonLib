package me.tatarka.bindingcollectionadapter2;

import android.support.v7.widget.RecyclerView;

import cn.citytag.base.adapter.ui.DividerItemDecoration;
import cn.citytag.base.adapter.ui.GridSpaceItemDecoration;
import cn.citytag.base.adapter.ui.TopSpaceItemDecoration;

/**
 * Created by yangfeng01 on 2017/11/20.
 */
public class ItemDecorations {

	protected ItemDecorations() {
	}

	public interface ItemDecorationFactory {
		RecyclerView.ItemDecoration create(RecyclerView recyclerView);
	}

	/**
	 * 根据屏幕宽度以及间隔宽度得到每个item宽度，从而等分的itemDecoration
	 *
	 * @param spanCount
	 * @param spanSpace
	 * @param includeEdge
	 * @return
	 */
	public static ItemDecorationFactory gridDivider(final int spanCount, final int spanSpace, final boolean includeEdge) {
		return new ItemDecorationFactory() {
			@Override
			public RecyclerView.ItemDecoration create(RecyclerView recyclerView) {
				return new GridSpaceItemDecoration(spanCount, spanSpace, includeEdge);
			}
		};
	}

	public static ItemDecorationFactory topSpace(final int spanCount, final int topSpaceDp) {
		return new ItemDecorationFactory() {
			@Override
			public RecyclerView.ItemDecoration create(RecyclerView recyclerView) {
				return new TopSpaceItemDecoration(spanCount, topSpaceDp);
			}
		};
	}

	public static ItemDecorationFactory divider(@Orientation final int orientation, final int dividerHeight) {
		return new ItemDecorationFactory() {
			@Override
			public RecyclerView.ItemDecoration create(RecyclerView recyclerView) {
				return new DividerItemDecoration(orientation, dividerHeight);
			}
		};
	}


}
