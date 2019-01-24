package me.tatarka.bindingcollectionadapter2;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by liguangchun on 2018/5/4.
 */

public class FoucsLinearLayoutManager extends LinearLayoutManager {

    public FoucsLinearLayoutManager(Context context) {
        super(context);
    }

    public FoucsLinearLayoutManager(Context context, int orientation, boolean reverseLayout) {
        super(context, orientation, reverseLayout);
    }


    /**
     public FoucsLinearLayoutManager(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
     super(context, attrs, defStyleAttr, defStyleRes);
     }

     * 　　public boolean requestChildRectangleOnScreen (View child, Rect rectangle, boolean immediate)
     * <p>
     * 　　当组里的某个子视图需要被定位在屏幕的某个矩形范围时，调用此方法。重载此方法的ViewGroup可确认以下几点：
     * <p>
     * 　　* 子项目将是组里的直系子项
     * 　　* 矩形将在子项目的坐标体系中
     * 　　重载此方法的ViewGroup应该支持以下几点：
     * 　　* 若矩形已经是可见的，则没有东西会改变
     * 　　* 为使矩形区域全部可见，视图将可以被滚动显示
     * 　　参数
     * 　　child        发出请求的子视图
     * 　　rectangle    子项目坐标系内的矩形，即此子项目希望在屏幕上的定位
     * 　　immediate   设为true，则禁止动画和平滑移动滚动条
     * <p>
     * 　　返回值
     * 　　进行了滚动操作的这个组（group），是否处理此操作。
     *
     * @param parent
     * @param child
     * @param rect
     * @param immediate
     * @return
     */
    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate) {

//这里的child 是整个HeadView 而不是某个具体的editText
      //  L.i("requestChildRectangleOnScreen()====> chlild==" + child.getId() + "parent==" + parent.getId());
        return false;
    }

    @Override
    public boolean requestChildRectangleOnScreen(RecyclerView parent, View child, Rect rect, boolean immediate, boolean focusedChildVisible) {

//这里的child 是整个HeadView 而不是某个具体的editText
      //  L.i("requestChildRectangleOnScreen( focusedChildVisible=)====> chlild==" + child.getId() + "parent==" + parent.getId());
        return false;
    }
}
