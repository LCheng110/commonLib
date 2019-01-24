package cn.citytag.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.os.Build;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.View;

import cn.citytag.base.R;


/**
 * Created by yangfeng01 on 2017/11/23.
 */

public class CircleProgressbar extends View {

	private static final int DEFAULT_PROGRESS_COLOR = 0xffb2b3ff;
	private static final int DEFAULT_PROGRESS_WIDTH = 10;

	private int progress;
	private int color;
	private int width;
	private int height;
	private int progressWidth;
	private int rectLeft;
	private int rectTop;
	private int rectRight;
	private int rectBottom;

	private Paint paint;
	private RectF rectF;

	public CircleProgressbar(Context context) {
		this(context, null);
	}

	public CircleProgressbar(Context context, @Nullable AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public CircleProgressbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(context, attrs);
	}

	@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
	public CircleProgressbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(context, attrs);
	}

	private void init(Context context, AttributeSet attrs) {
		TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.CircleProgressbar);
		color = a.getColor(R.styleable.CircleProgressbar_progressColor, DEFAULT_PROGRESS_COLOR);
		progressWidth = a.getDimensionPixelSize(R.styleable.CircleProgressbar_progressWidth, DEFAULT_PROGRESS_WIDTH);
		a.recycle();

		progress = 0;

		paint = new Paint(Paint.ANTI_ALIAS_FLAG | Paint.DITHER_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setColor(color);
		paint.setStrokeWidth(progressWidth);
	}

	@Override
	protected void onSizeChanged(int w, int h, int oldw, int oldh) {
		width = w;
		height = h;
		rectLeft = progressWidth /2;
		rectTop = progressWidth /2;
		rectRight = width - progressWidth / 2;
		rectBottom = height - progressWidth / 2;
		rectF = new RectF(rectLeft, rectTop, rectRight, rectBottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		canvas.drawArc(rectF, 270, 360 * progress / 100, false, paint);
	}

	public void setProgress(int progress) {
		if (this.progress == progress) {
			return;
		}
		if (progress <= 0) {
			progress = 0;
		}
		if (progress >= 100) {
			progress = 100;
		}
		this.progress = progress;
		invalidate();
	}


}
