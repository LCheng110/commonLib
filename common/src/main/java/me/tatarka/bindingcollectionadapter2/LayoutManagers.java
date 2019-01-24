package me.tatarka.bindingcollectionadapter2;

import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;

import cn.citytag.base.ui.FullyGridLayoutManager;
import cn.citytag.base.ui.FullyLinearLayoutManager;
import cn.citytag.base.utils.CatchGridLayoutManager;

/**
 * A collection of factories to create RecyclerView LayoutManagers so that you can easily set them
 * in your layout.
 */
public class LayoutManagers {
    protected LayoutManagers() {
    }

    public interface LayoutManagerFactory {
        RecyclerView.LayoutManager create(RecyclerView recyclerView);
    }

    /**
     * A {@link LinearLayoutManager}.
     */
    public static LayoutManagerFactory linear() {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new WrapContentLinearLayoutManager(recyclerView.getContext());
            }
        };
    }

    public static LayoutManagerFactory focusLinear() {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new FoucsLinearLayoutManager(recyclerView.getContext());
            }
        };
    }


    public static LayoutManagerFactory horilinear() {
        return linear(LinearLayoutManager.HORIZONTAL, false);
    }

    public static LayoutManagerFactory linear(@Orientation final int orientation) {
        return linear(orientation, false);
    }

    /**
     * A {@link LinearLayoutManager} with the given orientation and reverseLayout.
     */
    public static LayoutManagerFactory linear(@Orientation final int orientation, final boolean reverseLayout) {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new LinearLayoutManager(recyclerView.getContext(), orientation, reverseLayout);
            }
        };
    }

    public static LayoutManagerFactory linearWrapContent() {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new WrapContentLinearLayoutManager(recyclerView.getContext());
            }
        };
    }

    /**
     * A {@link GridLayoutManager} with the given spanCount.
     */
    public static LayoutManagerFactory grid(final int spanCount) {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new CatchGridLayoutManager(recyclerView.getContext(), spanCount);
            }
        };
    }

    /**
     * A {@link GridLayoutManager} with the given spanCount, orientation and reverseLayout.
     **/
    public static LayoutManagerFactory grid(final int spanCount, @Orientation final int orientation, final boolean reverseLayout) {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new CatchGridLayoutManager(recyclerView.getContext(), spanCount, orientation, reverseLayout);
            }
        };
    }

    /**
     * A {@link StaggeredGridLayoutManager} with the given spanCount and orientation.
     */
    public static LayoutManagerFactory staggeredGrid(final int spanCount, @Orientation final int orientation) {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                StaggeredGridLayoutManager staggeredGridLayoutManager = new StaggeredGridLayoutManager(spanCount,orientation);
                staggeredGridLayoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_NONE);
                return staggeredGridLayoutManager;
            }
        };
    }

    public static LayoutManagerFactory fullyLinear() {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new FullyLinearLayoutManager(recyclerView.getContext());
            }
        };
    }

    public static LayoutManagerFactory fullyGrid(final int spanCount) {
        return new LayoutManagerFactory() {
            @Override
            public RecyclerView.LayoutManager create(RecyclerView recyclerView) {
                return new FullyGridLayoutManager(recyclerView.getContext(), spanCount);
            }
        };
    }

}
