package cn.citytag.base.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.animation.DecelerateInterpolator;
import android.widget.Scroller;

import cn.citytag.base.R;
import cn.citytag.base.adapter.holder.SlideEditViewHolder;

/**
 * Created by liucheng on 2018/9/20.
 * item的根布局需要设置为FrameLayout
 */
public class SlideEditRecycleView extends RecyclerView {

    private static final int SCROLL_STATE_IDLE = 0;
    private static final int SCROLL_STATE_DRAGGING = 1;
    private static final int SCROLL_STATE_SLIDING = 2;

    private static final int LAYOUT_STATE_SHOW = 0;
    private static final int LAYOUT_STATE_HIDE = 1;
    private static final int LAYOUT_STATE_WILL_SHOW = 2;
    private static final int LAYOUT_STATE_WILL_HIDE = 3;

    private final VelocityTracker mVelocityTracker;
    private final Scroller mScroller;
    private int mScrollStatus = SCROLL_STATE_IDLE;
    private int mLayoutStatus = LAYOUT_STATE_HIDE;
    private int mTouchSlop; // 最小滑动距离
    private int mLastX;
    private int mLastY;
    private int mDispatchLastX;
    private int mDispatchLastY;
    private int mLayoutLength; // 编辑布局的长度
    private int mPosition;
    private int mCurPosition; // 当前已经出现编辑布局的item位置
    private View mItemLayout; // 编辑布局
    private cn.citytag.base.adapter.OnItemClickListener onItemClick; // item 点击监听
    private boolean isItemMoving; // item 是否正在移动中
    private boolean isStartScroll; // 是否开始滑动状态
    private boolean isListDragging; // 是否说列表上下滑动拖拽

    public SlideEditRecycleView(Context context) {
        this(context, null);
    }

    public SlideEditRecycleView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SlideEditRecycleView(Context context, @Nullable AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        mScroller = new Scroller(context, new DecelerateInterpolator());
        mVelocityTracker = VelocityTracker.obtain();
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    public void setOnItemClick(cn.citytag.base.adapter.OnItemClickListener onItemClick) {
        this.onItemClick = onItemClick;
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent e) {
        int x = (int) e.getX();
        int y = (int) e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_MOVE:
                int dx = mDispatchLastX - x;
                int dy = mDispatchLastY - y;
                if (Math.abs(dx) > Math.abs(dy)) {
                    requestDisallowInterceptTouchEvent(true);
                }
                break;

        }
        mDispatchLastX = x;
        mDispatchLastY = y;
        return super.dispatchTouchEvent(e);
    }

    @Override
    public boolean onTouchEvent(MotionEvent e) {
        mVelocityTracker.addMovement(e);
        int x = (int) e.getX();
        int y = (int) e.getY();
        switch (e.getAction()) {
            case MotionEvent.ACTION_DOWN:
                View view = findChildViewUnder(x, y);
                if (view == null) {
                    recordPoint(x, y);
                    return super.onTouchEvent(e);
                }
                if (getChildViewHolder(view) instanceof SlideEditViewHolder) {
                    SlideEditViewHolder viewHolder = (SlideEditViewHolder) getChildViewHolder(view);
                    if (mLayoutStatus == LAYOUT_STATE_HIDE) {
                        mItemLayout = viewHolder.itemView;
                        mPosition = viewHolder.getAdapterPosition();
                        if (viewHolder.getVisibilityCount() == 1) {
                            mLayoutLength = (int) Math.abs(getResources().getDimension(R.dimen.item_edit_hide_width_single));
                        } else if (viewHolder.getVisibilityCount() == 2) {
                            mLayoutLength = (int) Math.abs(getResources().getDimension(R.dimen.item_edit_hide_width));
                        } else if (viewHolder.getVisibilityCount() == 0) {
                            mLayoutLength = 0;
                            initSlideStatus();
                            return super.onTouchEvent(e);
                        }
                        viewHolder.setOnItemClickListener(new OnItemClickListener() {
                            @Override
                            public void onItemClick() {
                                initSlideStatus();
                            }
                        });
                    } else if (mLayoutStatus == LAYOUT_STATE_SHOW) {
                        if (mCurPosition != viewHolder.getAdapterPosition()) {
                            initSlideStatus();
                            return false;
                        }
                    } else {
                        recordPoint(x, y);
                        return super.onTouchEvent(e);
                    }
                } else {
                    mPosition = 0;
                    recordPoint(x, y);
                    return super.onTouchEvent(e);
                }
                break;
            case MotionEvent.ACTION_MOVE:
                if (mItemLayout == null || mLayoutLength == 0) {
                    recordPoint(x, y);
                    return super.onTouchEvent(e);
                }
                int dx = mLastX - x;
                int dy = mLastY - y;

                int scrollX = mItemLayout.getScrollX();
                if (Math.abs(dx) > mTouchSlop) {
//                if (Math.abs(dx) > mTouchSlop && Math.abs(dx) > Math.abs(dy)) {
                    isItemMoving = true;
                    if (scrollX + dx <= 0) {//左边界检测
                        mItemLayout.scrollTo(0, 0);
                        recordPoint(x, y);
                        return true;
                    } else if (scrollX + dx >= mLayoutLength) {//右边界检测
//                        mItemLayout.scrollTo(mLayoutLength, 0);
                        mItemLayout.scrollBy((int) Math.log10(dx), 0);
                        recordPoint(x, y);
                        return true;
                    }
                    mItemLayout.scrollBy(dx, 0);//item跟随手指滑动
                }
                break;
            case MotionEvent.ACTION_UP:
                if (mItemLayout != null && !isItemMoving && !isListDragging && onItemClick != null) {
                    onItemClick.onItemClick(mItemLayout, mPosition);
                }
                if (mItemLayout == null || mLayoutLength == 0) {
                    return super.onTouchEvent(e);
                }
                if (mCurPosition == mPosition && mLayoutStatus == LAYOUT_STATE_SHOW) {
                    initSlideStatus();
                    return super.onTouchEvent(e);
                }
                isItemMoving = false;

                mVelocityTracker.computeCurrentVelocity(1000);//计算手指滑动的速度
                float xVelocity = mVelocityTracker.getXVelocity();//水平方向速度（向左为负）
                float yVelocity = mVelocityTracker.getYVelocity();//垂直方向速度

                int deltaX = 0;
                int upScrollX = mItemLayout.getScrollX();
                if (Math.abs(xVelocity) > 100 && Math.abs(xVelocity) > Math.abs(yVelocity)) {
                    if (xVelocity <= -100) {//左滑速度大于100，则删除按钮显示
                        deltaX = mLayoutLength - upScrollX;
                        mLayoutStatus = LAYOUT_STATE_WILL_SHOW;
                    } else if (xVelocity > 100) {//右滑速度大于100，则删除按钮隐藏
                        deltaX = -upScrollX;
                        mLayoutStatus = LAYOUT_STATE_WILL_HIDE;
                    }
                } else {
                    if (upScrollX >= mLayoutLength / 2) {//item的左滑动距离大于删除按钮宽度的一半，则则显示删除按钮
                        deltaX = mLayoutLength - upScrollX;
                        mLayoutStatus = LAYOUT_STATE_WILL_SHOW;
                    } else if (upScrollX < mLayoutLength / 2) {//否则隐藏
                        deltaX = -upScrollX;
                        mLayoutStatus = LAYOUT_STATE_WILL_HIDE;
                    }
                }
                //item自动滑动到指定位置
                mScroller.startScroll(upScrollX, 0, deltaX, 0, 200);
                isStartScroll = true;
                invalidate();
                mVelocityTracker.clear();
                break;
        }
        recordPoint(x, y);
        if (isItemMoving) {
            return isItemMoving;
        } else {
            return super.onTouchEvent(e);
        }
    }

    private void recordPoint(int x, int y) {
        mLastX = x;
        mLastY = y;
    }

    @Override
    public void computeScroll() {
        super.computeScroll();
        if (mScroller.computeScrollOffset()) {
            mItemLayout.scrollTo(mScroller.getCurrX(), mScroller.getCurrY());
            invalidate();
        } else if (isStartScroll) {
            isStartScroll = false;
            if (mLayoutStatus == LAYOUT_STATE_WILL_HIDE) {
                mLayoutStatus = LAYOUT_STATE_HIDE;
            }
            if (mLayoutStatus == LAYOUT_STATE_WILL_SHOW) {
                mLayoutStatus = LAYOUT_STATE_SHOW;
                mCurPosition = mPosition;
            }
        }
    }

    @Override
    public void onScrollStateChanged(int state) {
        super.onScrollStateChanged(state);
        isListDragging = state == SCROLL_STATE_DRAGGING;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    private void initSlideStatus() {
        mScrollStatus = SCROLL_STATE_IDLE;
        mLayoutStatus = LAYOUT_STATE_HIDE;
        // 滑动到原来到位置
        mScroller.startScroll(mItemLayout.getScrollX(), 0, -mItemLayout.getScrollX(), 0, 200);
        invalidate();
        mCurPosition = -1;
    }

    public interface OnItemClickListener {
        void onItemClick();
    }
}
