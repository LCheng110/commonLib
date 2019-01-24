package com.luck.picture.lib.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.VideoView;

/**
 * Created by liguangchun on 2018/5/18.
 */

public class CustomMeasureVideoView extends VideoView {

    private int videoWidth;
    private int videoHeight;

    public CustomMeasureVideoView(Context context) {
        super(context);
    }

    public CustomMeasureVideoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomMeasureVideoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setMeasure(int width, int height) {
        videoWidth = width;
        videoHeight = height;
        requestLayout();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (videoHeight > 0) {
            int width = getDefaultSize(getWidth(), widthMeasureSpec);
            int height = getDefaultSize(getHeight(), heightMeasureSpec);
            if (width > videoWidth) {
                videoHeight = videoHeight * width / videoWidth;
            } else {
                videoHeight = videoHeight * videoWidth / width;
            }
            videoWidth = width;

        }
        setMeasuredDimension(videoWidth, videoHeight);
    }

}
