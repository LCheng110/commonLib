package cn.citytag.base.view.base;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.PopupWindow;

/**
 * Created by qhm on 2017/6/9
 * <p>
 * PopupWindow base类
 */

public abstract class BasePopupWindow extends PopupWindow {

    protected Context mContext;
    protected View view;
    protected int popupWidth, popupHeight;
    protected LayoutInflater mLayoutInflater;

    public BasePopupWindow(Context context) {
        super(context);
        this.mContext = context;
        this.mLayoutInflater = LayoutInflater.from(context);
        initBasicView();
    }

    public <T extends View> T fv(int viewID) {
        return (T) view.findViewById(viewID);
    }

    public void initBasicView() {
        view = mLayoutInflater.inflate(thisLayout(), null, false);
        setContentView(view);
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        setWidth(ViewGroup.LayoutParams.MATCH_PARENT);
        setHeight(ViewGroup.LayoutParams.MATCH_PARENT);
        doInitView();
        this.setBackgroundDrawable(new ColorDrawable(0x0));
        this.setFocusable(true);
        this.setOutsideTouchable(true);
        doInitData();
        initInfo();
    }

    public abstract int thisLayout();

    public abstract void doInitView();

    public abstract void doInitData();

    private void initInfo() {
        view.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        popupWidth = view.getMeasuredWidth();
        popupHeight = view.getMeasuredHeight();
    }

    public int getPopupHeight(){
        return popupHeight;
    }

    @Override
    public void dismiss() {
        super.dismiss();
    }

    public void setBackgroundShadow(boolean showShadow) {
        ValueAnimator animator = ValueAnimator.ofFloat(showShadow ? 1 : 0.3f, showShadow ? 0.3f : 1);
        animator.setDuration(400);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                backgroundAlpha((Activity) view.getContext(), (Float) valueAnimator.getAnimatedValue());
            }
        });
        animator.start();
    }

    private void backgroundAlpha(Activity activity, float bgAlpha) {
        if (activity == null) {
            return;
        }
        Window window = activity.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = window.getAttributes();
            lp.alpha = bgAlpha;
            if (bgAlpha == 1) { //不移除该Flag的话,在有视频的页面上的视频会出现黑屏的bug
                window.clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            } else { //此行代码主要是解决在华为手机上半透明效果无效的bug
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
            window.setAttributes(lp);
        }
    }

    /**
     * 设置显示在v上方（以v的中心位置为开始位置）
     *
     * @param anchor
     */
    public void showAsDropUp(View anchor) {
        //获取需要在其上方显示的控件的位置信息
        int[] location = new int[2];
        anchor.getLocationOnScreen(location);
        //在控件上方显示
        showAtLocation(anchor, Gravity.NO_GRAVITY, (location[0] + anchor.getWidth() / 2) - popupWidth / 2,
                location[1] - popupHeight);
        Log.i("sss", "location[1]: " + location[1] + "\npopupHeight: " + popupHeight + "\nanchor.getHeight(): " +
                anchor.getHeight());
    }
}
