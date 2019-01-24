package cn.citytag.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import cn.citytag.base.R;
import cn.citytag.base.image.ImageLoader;
import cn.citytag.base.utils.UIUtils;

/**
 * Created by qhm on 2017/9/20
 * <p>
 * 自定义附带角标 头像角标
 */

public class UcAvatarView extends FrameLayout {

    public static final int LOCATION_LEFT_TOP = 1;          //左上角   默认
    public static final int LOCATION_LEFT_BOTTOM = 2;       //左下角
    public static final int LOCATION_RIGHT_TOP = 3;         //右上角
    public static final int LOCATION_RIGHT_BOTTOM = 4;      //右下角

    public static final int CORNER_IMG = 1;                 //角标为图片     默认
    public static final int CORNER_TEXT = 2;                //角标为文字

    private static final float DEFAULT_MAIN_WIDTH = 60;       //默认的主图片大小
    private static final float DEFAULT_CORNER_WIDTH = 10;     //默认的角标大小

    private static final int DEFAULT_CORNER_BACGROUND = R.drawable.ic_boy_tag_small;

    private Context mContext;

    private ImageView iv_main;
    private TextView tv_corner;

    private int location;                   //角标位置类型
    private int corner;                     //角标类型 图片或文字
    private int mainWidth;                //主图片的边长
    private int cornerWidth;              //角标的边长
    private int mainSrc;                    //主图片的图片
    private int cornerBackground;           //角标的背景
    private int mainMargin;                 //主图片margin


    public UcAvatarView(Context context) {
        this(context, null);
    }

    public UcAvatarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        if (isInEditMode()) {
            return;
        }
        initValue(context, attrs);
        initView(context);
    }

    public UcAvatarView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        if (isInEditMode()) {
            return;
        }
        initValue(context, attrs);
        initView(context);
    }

    private void initValue(Context context, AttributeSet attrs) {
        this.mContext = context;
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.UcAvatarView);
        location = a.getInteger(R.styleable.UcAvatarView_location, LOCATION_RIGHT_BOTTOM);
        corner = a.getInteger(R.styleable.UcAvatarView_corner, CORNER_IMG);
        mainWidth = a.getDimensionPixelOffset(R.styleable.UcAvatarView_mainWidth,
                UIUtils.dip2px(context, DEFAULT_MAIN_WIDTH));
        cornerWidth = a.getDimensionPixelOffset(R.styleable.UcAvatarView_cornerWidth,
                UIUtils.dip2px(context, DEFAULT_CORNER_WIDTH));
        mainSrc = a.getResourceId(R.styleable.UcAvatarView_mainSrc, R.drawable.shape_color_placeholder_circle);
        cornerBackground = a.getResourceId(R.styleable.UcAvatarView_cornerBackground,
                DEFAULT_CORNER_BACGROUND);
        mainMargin = a.getDimensionPixelOffset(R.styleable.UcAvatarView_mainMargin, 0);
        a.recycle();
    }

    private void initView(Context context) {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        setLayoutParams(params);
        //添加主图片
        iv_main = new ImageView(context);
        LayoutParams mainParams;
        if (mainWidth == 0) {
            mainParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        } else {
            mainParams = new LayoutParams(mainWidth, mainWidth);
        }
        initMainMargin(mainParams);
        iv_main.setLayoutParams(mainParams);
        iv_main.setScaleType(ImageView.ScaleType.FIT_CENTER);
        //iv_main.setAdjustViewBounds(true);
        iv_main.setImageResource(mainSrc);
//        iv_main.setScaleType(ImageView.ScaleType.FIT_XY);
        this.addView(iv_main);
        //添加角标文件
        tv_corner = new TextView(context);
        LayoutParams cornerParams;
        if (corner == CORNER_IMG) {
            cornerParams = new LayoutParams(cornerWidth, cornerWidth);
        } else {
            cornerParams = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);
        }
        initCornerLocation(cornerParams);
        tv_corner.setLayoutParams(cornerParams);
        //tv_corner.setBackgroundResource(cornerBackground);
        if (cornerBackground == DEFAULT_CORNER_BACGROUND) {
            //tv_corner.setVisibility(GONE);
        }
        this.addView(tv_corner);
    }

    public void setMainWidth(int mainWidth) {
        this.mainWidth = mainWidth;
        initView(getContext());
    }

    /**
     * 设置 主图片的margin布局
     *
     * @param params
     */
    private void initMainMargin(LayoutParams params) {
        switch (location) {
            case LOCATION_LEFT_TOP:
                params.setMargins(mainMargin, mainMargin, 0, 0);
                break;
            case LOCATION_LEFT_BOTTOM:
                params.setMargins(mainMargin, 0, 0, mainMargin);
                break;
            case LOCATION_RIGHT_TOP:
                params.setMargins(0, mainMargin, mainMargin, 0);
                break;
            case LOCATION_RIGHT_BOTTOM:
                params.setMargins(0, 0, mainMargin, mainMargin);
                break;
        }
    }

    /**
     * 设置 角标的位置
     */
    private void initCornerLocation(LayoutParams params) {
        switch (location) {
            case LOCATION_LEFT_TOP:
                break;
            case LOCATION_LEFT_BOTTOM:
                params.gravity = Gravity.BOTTOM;
                break;
            case LOCATION_RIGHT_TOP:
                params.gravity = Gravity.RIGHT;
                break;
            case LOCATION_RIGHT_BOTTOM:
                params.gravity = Gravity.BOTTOM | Gravity.RIGHT;
                break;
        }
    }

    /**
     * 设置主图片
     *
     * @param resId
     */
    public void setMainSrc(int resId) {
        iv_main.setVisibility(VISIBLE);
        ImageLoader.loadImage(iv_main, resId);
    }

    /**
     * 设置主图片
     *
     * @param drawable
     */
    public void setMainSrc(Drawable drawable) {
        iv_main.setVisibility(VISIBLE);
        iv_main.setImageDrawable(drawable);
    }

    /**
     * 设置主图片
     *
     * @param bitmap
     */
    public void setMainSrc(Bitmap bitmap) {
        iv_main.setVisibility(VISIBLE);
        iv_main.setImageBitmap(bitmap);
    }

    /**
     * 设置主图片
     *
     * @param url
     */
    public void setMainSrc(String url) {
        iv_main.setVisibility(VISIBLE);
        ImageLoader.loadImage(iv_main, url);
    }

    /**
     * 设置主图片
     *
     * @param url
     */
    //public void setMainSrcAsCircle(String url) {
    //    iv_main.setVisibility(VISIBLE);
    //    ImageLoader.loadCircleImage(mContext, iv_main, url, DEFAULT_CORNER_BACGROUND, DEFAULT_CORNER_BACGROUND);
    //}
    public void setCircleAvatarUrl(String url) {
        iv_main.setVisibility(VISIBLE);
        ImageLoader.loadCircleImage(mContext, iv_main, url, mContext.getResources().getDrawable(R.drawable.shape_color_placeholder_circle));
    }

    public void setCircleAvatarUrl(String url, Drawable placeholder) {
        iv_main.setVisibility(VISIBLE);
        ImageLoader.loadCircleImage(mContext, iv_main, url, placeholder);
    }

    public void setCircleAvatarUrlAddTag(String url) {
        iv_main.setVisibility(VISIBLE);
        if (((FrameLayout) iv_main.getParent()).getTag() == null || !((FrameLayout) iv_main.getParent()).getTag().equals(url)) {
            ImageLoader.loadCircleImage(mContext, iv_main, url, mContext.getResources().getDrawable(R.drawable.shape_color_placeholder_circle));
            ((FrameLayout) iv_main.getParent()).setTag(url);
        }

    }

    /**
     * 设置主图片Padding
     */
    public void setMainSrcPadding(int left, int top, int right, int bottom) {
        iv_main.setVisibility(VISIBLE);
        setPadding(left, top, right, bottom);
    }

    /**
     * 设置主图片隐藏
     */
    public void setMainSrcVisibility(int visibility) {
        iv_main.setVisibility(visibility);
    }

    /**
     * 设置角标背景
     *
     * @param resId
     */
    //public void setCornerBackground(int resId) {
    //    tv_corner.setBackgroundResource(resId);
    //    tv_corner.setVisibility(VISIBLE);
    //}
    public void setConnerBgResId(int resId) {
        tv_corner.setBackgroundResource(resId);
        tv_corner.setVisibility(VISIBLE);
    }

    /**
     * 设置角标背景
     *
     * @param drawable
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void setCornerBackground(Drawable drawable) {
        tv_corner.setBackground(drawable);
        tv_corner.setVisibility(VISIBLE);
    }

    /**
     * 设置角标文字
     *
     * @param text
     */
    public void setCornerText(String text) {
        if (corner != CORNER_TEXT) {
            return;
        }
        tv_corner.setText(text);
        tv_corner.setVisibility(VISIBLE);
    }

    /**
     * 设置角标文字 及 文字大小
     *
     * @param text
     */
    public void setCornerText(String text, float textSize) {
        if (corner != CORNER_TEXT) {
            return;
        }
        tv_corner.setText(text);
        tv_corner.setTextSize(textSize);
        tv_corner.setVisibility(VISIBLE);
    }

    /**
     * 设置角标文字 及 文字颜色
     *
     * @param text
     */
    public void setCornerText(String text, int textColor) {
        if (corner != CORNER_TEXT) {
            return;
        }
        tv_corner.setText(text);
        tv_corner.setTextColor(textColor);
        tv_corner.setVisibility(VISIBLE);
    }

    /**
     * 设置角标文字 文字大小 文字颜色
     *
     * @param text
     */
    public void setCornerText(String text, float textSize, int textColor) {
        if (corner != CORNER_TEXT) {
            return;
        }
        tv_corner.setText(text);
        tv_corner.setTextSize(textSize);
        tv_corner.setTextColor(textColor);
        tv_corner.setVisibility(VISIBLE);
    }

    /**
     * 设置角标文字padding
     */
    public void setCornerTextPadding(int left, int top, int right, int bottom) {
        if (corner != CORNER_TEXT) {
            return;
        }
        tv_corner.setVisibility(VISIBLE);
        tv_corner.setPadding(left, top, right, bottom);
    }

    //public void hideCorner() {
    //    tv_corner.setVisibility(GONE);
    //}

    public void setIsShowConner(boolean isShowConner) {
        tv_corner.setVisibility(isShowConner ? VISIBLE : GONE);
    }

    /**
     * 修改角标类型
     *
     * @param corner
     */
    public void setCorner(int corner) {
        this.corner = corner;
    }
}