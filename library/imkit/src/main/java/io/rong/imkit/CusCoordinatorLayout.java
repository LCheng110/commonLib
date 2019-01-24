package io.rong.imkit;

import android.content.Context;
import android.support.design.widget.CoordinatorLayout;
import android.util.AttributeSet;
import android.view.View;

import io.rong.imkit.interfaces.IIsOccupy;

/**
 * Created by liguangchun on 2018/3/2.
 */

public class CusCoordinatorLayout extends CoordinatorLayout {
    private IIsOccupy iIsOccupy;

    public IIsOccupy getiIsOccupy() {
        return iIsOccupy;
    }

    public void setiIsOccupy(IIsOccupy iIsOccupy) {
        this.iIsOccupy = iIsOccupy;
    }

    public CusCoordinatorLayout(Context context) {
        super(context);
    }

    public CusCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CusCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onNestedPreScroll(View target, int dx, int dy, int[] consumed, int type) {
        if (!iIsOccupy.isOccupy()) {
            super.onNestedPreScroll(target, dx, dy, consumed, type);
        }

    }


}
