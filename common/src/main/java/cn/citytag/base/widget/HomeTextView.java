package cn.citytag.base.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;

/**
 * Created by baoyiwei on 2018/3/2.
 */
public class HomeTextView extends android.support.v7.widget.AppCompatTextView {
    private boolean adjustTopForAscent = true;

    public HomeTextView(Context context) {
        super(context);
    }

    public HomeTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public HomeTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    Paint.FontMetricsInt fontMetricsInt;

    @Override
    protected void onDraw(Canvas canvas) {
        if (adjustTopForAscent) {//设置是否remove间距，true为remove
            if (fontMetricsInt == null) {
                fontMetricsInt = new Paint.FontMetricsInt();
                getPaint().getFontMetricsInt(fontMetricsInt);
            }
            canvas.translate(0, -(fontMetricsInt.top - fontMetricsInt.ascent));
        }
        super.onDraw(canvas);
    }
}