package cn.citytag.base.widget;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.ImageView;

/**
 * Created by yangfeng01 on 2017/10/27.
 */
public class SquareImageView extends ImageView {

	private static final String TAG = SquareImageView.class.getSimpleName();

	public SquareImageView(Context context) {
		super(context);
	}

	public SquareImageView(Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareImageView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, widthMeasureSpec);
		//Drawable drawable = getDrawable();
		//if (drawable != null) {
		//	int imageWidth = MeasureSpec.getSize(widthMeasureSpec);
		//	int imageHeight = (int) Math.ceil((float) imageWidth * (float) drawable.getIntrinsicHeight() / (float) drawable.getIntrinsicWidth());
		//	L.d(TAG, "drawable != null. " + "imageWidth == " + imageWidth + " ,imageHeight == " + imageHeight);
		//	setMeasuredDimension(imageWidth, imageHeight);
		//	L.d(TAG, "setMeasuredDimension" + "imageWidth == " + imageWidth + " ,imageHeight == " + imageHeight);
		//} else {
		//	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		//}
	}
}
