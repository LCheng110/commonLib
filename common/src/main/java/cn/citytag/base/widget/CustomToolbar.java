package cn.citytag.base.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.annotation.StyleRes;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.TextView;

import cn.citytag.base.R;

/**
 * Created by yangfeng01 on 2017/11/7.
 */

public class CustomToolbar extends Toolbar {

    private TextView titleTextView;
    private CharSequence titleText;
    private int titleTextAppearance;
    private int titleTextColor = getResources().getColor(R.color.color_333333);

    public CustomToolbar(Context context) {
        this(context, null);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, R.attr.toolbarStyle);
    }

    public CustomToolbar(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.Toolbar, defStyleAttr, 0);
        titleTextAppearance = a.getResourceId(R.styleable.Toolbar_titleTextAppearance, 0);
        a.recycle();
    }

    @Override
    public void setTitle(CharSequence title) {
        if (!TextUtils.isEmpty(title)) {
            ensureTitleView();
            if (titleTextView.getParent() == null) {
                LayoutParams lp = new LayoutParams(Gravity.CENTER);
                titleTextView.setGravity(Gravity.CENTER);
                addView(titleTextView, lp);
            }
        } else if (titleTextView != null && titleTextView.getParent() != null) {
            removeView(titleTextView);
        }
        if (titleTextView != null) {
            if (title == null) return;
            title = (title.length() >= 15 ? title.subSequence(0, 15) + "···" : title);
            titleTextView.setText(title);
        }
        titleText = title;
    }

    private void ensureTitleView() {
        if (titleTextView == null) {
            Context context = getContext();
            titleTextView = new TextView(context);
            titleTextView.setSingleLine();
            titleTextView.setEllipsize(TextUtils.TruncateAt.END);
            if (titleTextAppearance != 0) {
                titleTextView.setTextAppearance(context, titleTextAppearance);
            }
            if (titleTextColor != 0) {
                titleTextView.setTextColor(titleTextColor);
            }
        }
    }

    @Override
    public void setTitleTextColor(@ColorInt int titleTextColor) {
        this.titleTextColor = titleTextColor;
        if (titleTextView != null) {
            titleTextView.setTextColor(titleTextColor);
        }
    }

    @Override
    public void setTitleTextAppearance(Context context, @StyleRes int textAppearance) {
        this.titleTextAppearance = textAppearance;
        if (titleTextView != null) {
            titleTextView.setTextAppearance(context, textAppearance);
        }
    }

    public TextView getTitleTextView() {
        return titleTextView;
    }

    @Override
    public CharSequence getTitle() {
        return titleText;
    }


}
