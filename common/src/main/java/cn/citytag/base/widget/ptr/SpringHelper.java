package cn.citytag.base.widget.ptr;

import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.view.View;
import android.view.ViewParent;

/**
 * Created by yangfeng01 on 2017/12/13.
 */
class SpringHelper {

    /**
     * 在当前布局中查找AppBarLayout并返回，没有找到返回null
     * #解决和AppBarLayout自身滚动冲突的问题#
     */
    public static AppBarLayout findAppBarLayout(View v) {
        AppBarLayout appBarLayout = null;
        ViewParent p = v.getParent();
        while (p != null) {
            if (p instanceof CoordinatorLayout) {
                break;
            }
            p = p.getParent();
        }
        if (p instanceof CoordinatorLayout) {
            CoordinatorLayout coordinatorLayout = (CoordinatorLayout) p;
            final int childCount = coordinatorLayout.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                final View child = coordinatorLayout.getChildAt(i);
                if (child instanceof AppBarLayout) {
                    appBarLayout = (AppBarLayout) child;
                    break;
                }
            }
        }
        return appBarLayout;
    }
}
