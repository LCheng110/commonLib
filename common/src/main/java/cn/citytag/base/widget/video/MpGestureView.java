package cn.citytag.base.widget.video;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * 手势滑动的view。用于UI中处理手势的滑动事件，从而去实现手势改变亮度，音量，seek等操作。
 */
public class MpGestureView extends View implements MpVideoViewAction {

    private static final String TAG = MpGestureView.class.getSimpleName();
    //手势控制
    protected MpGestureControl mGestureControl;
    //监听器
    private GestureListener mOutGestureListener = null;
    //隐藏原因
    private HideType mHideType = null;

    public MpGestureView(Context context) {
        super(context);
        init();
    }

    public MpGestureView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MpGestureView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //创建手势控制
        mGestureControl = new MpGestureControl(getContext(), this);
        //设置监听
        mGestureControl.setOnGestureControlListener(new GestureListener() {
            @Override
            public void onSingleTap() {
                //锁屏的时候，单击还是有用的。。不然没法显示锁的按钮了
                if (mOutGestureListener != null) {
                    mOutGestureListener.onSingleTap();
                }
            }

            @Override
            public void onDoubleTap() {
                if (mOutGestureListener != null) {
                    mOutGestureListener.onDoubleTap();
                }
            }

        });
    }

    public void setHideType(HideType hideType) {
        this.mHideType = hideType;
    }

    public interface GestureListener {
        /**
         * 单击事件
         */
        void onSingleTap();

        /**
         * 双击事件
         */
        void onDoubleTap();
    }

    /**
     * 设置手势监听事件
     *
     * @param gestureListener 手势监听事件
     */
    public void setOnGestureListener(GestureListener gestureListener) {
        mOutGestureListener = gestureListener;
    }

    @Override
    public void reset() {
        mHideType = null;
    }

    @Override
    public void show() {
        if (mHideType == HideType.End) {
            //如果是由于错误引起的隐藏，那就不能再展现了
            Log.d(TAG, "show END");
        } else {
            Log.d(TAG, "show ");
            setVisibility(VISIBLE);
        }
    }

    @Override
    public void hide(HideType hideType) {
        if (mHideType != HideType.End) {
            mHideType = hideType;
        }
        setVisibility(GONE);
    }

    @Override
    public void setScreenModeStatus(MpScreenMode mode) {

    }

}
