package cn.citytag.base.widget.video;

import android.content.Context;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import android.view.View;

/**
 * 播放控手势控制。通过对view的GestureDetector事件做监听，判断水平滑动还是垂直滑动。
 * 最后的结果通过{@link MpGestureView.GestureListener}返回出去。
 * 主要在{@link MpGestureView}中使用到此类。
 */
public class MpGestureControl {

    public Context mContext;
    /**
     * 播放控制层
     **/
    private View mGesturebleView;

    // 手势决定器
    private GestureDetector mGestureDetector;
    // 手势监听
    private MpGestureView.GestureListener mGestureListener;
    // 绑定到GestureDetector的。
    private final OnGestureListener mOnGestureListener = new OnGestureListener() {
        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent e) {
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            Log.d("qhm", "onDown");
            return true;
        }

    };

    /**
     * @param mContext
     * @param gestureView 播放控制层
     */
    public MpGestureControl(Context mContext, View gestureView) {
        this.mContext = mContext;
        this.mGesturebleView = gestureView;
        init();
    }

    private void init() {
        mGestureDetector = new GestureDetector(mContext, mOnGestureListener);
        mGesturebleView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        //GestureDetector增加双击事件的监听。。里面包含了单击事件
        mGestureDetector.setOnDoubleTapListener(new GestureDetector.OnDoubleTapListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                Log.d("qhm", "onSingleTapConfirmed");
                //			处理点击事件
                if (mGestureListener != null) {
                    mGestureListener.onSingleTap();
                }
                return false;
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                Log.d("qhm", "onDoubleTap");
                if (mGestureListener != null) {
                    mGestureListener.onDoubleTap();
                }
                return false;
            }

            @Override
            public boolean onDoubleTapEvent(MotionEvent e) {
                return false;
            }
        });
    }

    /**
     * 设置手势监听事件
     *
     * @param mGestureListener 手势监听事件
     */
    void setOnGestureControlListener(MpGestureView.GestureListener mGestureListener) {
        this.mGestureListener = mGestureListener;
    }


}
