package cn.citytag.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;

import cn.citytag.base.R;
import cn.citytag.base.utils.UIUtils;

/**
 * Created by zhaoyuanchao on 2018/5/1.
 */

public class SupCircleImageView extends CircleImageView {
    private Bitmap scaledBitmap;
    private Bitmap GFBitmap;
    private Bitmap showBitmap;
    private Bitmap showGFBitmap;


    //导师
    private Bitmap tutorBitmap;
    private Bitmap showTutorBitmap;

    private Paint mPaint = new Paint();
    private int width;
    private int px;
    private boolean isShow = false;
    private int size = 40;
    private int picSize = 10;
    //    private int suppic = R.drawable.icon_daren_pp;
    private Drawable suppic;
    private boolean isGF = false;

    public boolean isGF() {
        return isGF;
    }

    public void setGF(boolean GF) {
        isGF = GF;
        invalidate();
    }

    private boolean isTutor; //是否是导师

    public boolean isTutor() {
        return isTutor;
    }

    public void setTutor(boolean tutor) {
        isTutor = tutor;
    }

    public SupCircleImageView(Context context) {
        this(context, null);
    }

    public SupCircleImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SupCircleImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.SupCircleImageView);
        suppic = ta.getDrawable(R.styleable.SupCircleImageView_suppic);
        size = ta.getInteger(R.styleable.SupCircleImageView_picSize, 40);
        picSize = ta.getInteger(R.styleable.SupCircleImageView_supPicSize, 10);
        isShow = ta.getBoolean(R.styleable.SupCircleImageView_isShow, false);
        isGF = ta.getBoolean(R.styleable.SupCircleImageView_isGF, false);
        isTutor = ta.getBoolean(R.styleable.SupCircleImageView_isTutor, false);
        ta.recycle();
        if (suppic != null) {
            scaledBitmap = ((BitmapDrawable) suppic).getBitmap();
            GFBitmap = ((BitmapDrawable) suppic).getBitmap();
            tutorBitmap = ((BitmapDrawable) suppic).getBitmap();
        } else {
            //4.0 版本， 应产品需求， 标签样式修改为：1.不显示； 2:官方 -《官》  3:达人等认证 - 《六边形黑V》
            tutorBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_talent_yellow_v);
            scaledBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.ic_talent_yellow_v);
            GFBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.icon_gf);
        }
        px = UIUtils.dip2px(picSize);
        width = UIUtils.dip2px(size);
        showBitmap = Bitmap.createScaledBitmap(scaledBitmap, px, px, false);
        showGFBitmap = Bitmap.createScaledBitmap(GFBitmap, px, px, false);
        showTutorBitmap = Bitmap.createScaledBitmap(tutorBitmap, px, px, false);
    }

    public void setShow(boolean isShow) {
        this.isShow = isShow;
    }

    public void setPicSize(int size) {
        this.size = size;
    }

    public void setSupPicSize(int picSize) {
        this.picSize = picSize;
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (isShow) {

            if (isGF) {
                canvas.drawBitmap(showGFBitmap, width - px, width - px, mPaint);
            } else {
                if (isTutor) {
                    canvas.drawBitmap(showTutorBitmap, width - px, width - px, mPaint);
                } else {
                    canvas.drawBitmap(showBitmap, width - px, width - px, mPaint);
                }

            }

        }

    }
}
