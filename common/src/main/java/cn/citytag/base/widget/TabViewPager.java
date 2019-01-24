package cn.citytag.base.widget;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.view.MotionEvent;

/**
 * Created by yangfeng01 on 2017/11/8.
 * 控制是否能左右滑动的ViewPager
 */

public class TabViewPager extends ViewPager {

	private boolean swipable = false;
	private boolean smoothScroll = false;

	public boolean isSmoothScroll() {
		return smoothScroll;
	}

	public TabViewPager(Context context) {
		super(context);
	}

	public TabViewPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
		return swipable && super.onInterceptTouchEvent(ev);
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
		return swipable && super.onTouchEvent(ev);
	}

	public void setSwipable(boolean swipable) {
		this.swipable = swipable;
	}

	public void setSmoothScroll(boolean smoothScroll){
		this.smoothScroll =smoothScroll ;
	}

	public void setCurrentItem(int item) {
		super.setCurrentItem(item, smoothScroll);//表示切换的时候，不需要切换时间。
	}
}
