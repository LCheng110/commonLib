package cn.citytag.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

/**
 * drawableLeft与Text一起居中
 *
 * @author ysl.
 * @date 2017/2/12.
 */
public class DrawableCenterTextView extends android.support.v7.widget.AppCompatTextView {

    public DrawableCenterTextView(Context context, AttributeSet attrs,
                                  int defStyle) {
        super(context, attrs, defStyle);
    }

    public DrawableCenterTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DrawableCenterTextView(Context context) {
        super(context);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Drawable[] drawables = getCompoundDrawables();
        Drawable drawableLeft = drawables[0];
        Drawable drawableRight = drawables[2];
        if (drawableLeft != null) {
            float textWidth = getPaint().measureText(getText().toString());
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth = drawableLeft.getIntrinsicWidth();
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            float dx = (getWidth() - bodyWidth) / 2.05f;
            canvas.translate(dx, 0);
            super.onDraw(canvas);
            canvas.translate(-dx, 0); // 绘制完成后画布归位，否则会造成背景效果偏移
        } else if (drawableRight != null) {
            float textWidth = getPaint().measureText(getText().toString());
            int drawablePadding = getCompoundDrawablePadding();
            int drawableWidth = drawableRight.getIntrinsicWidth();
            float bodyWidth = textWidth + drawableWidth + drawablePadding;
            float dx = (getWidth() - bodyWidth) / 2.05f;
            setPadding(0, getPaddingTop(), (int) (getWidth() - bodyWidth), getPaddingBottom());
            canvas.translate(dx, 0);
            super.onDraw(canvas);
            canvas.translate(-dx, 0); // 绘制完成后画布归位，否则会造成背景效果偏移
        } else {
            super.onDraw(canvas);
        }
    }
}