package cn.citytag.base.widget;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.util.AttributeSet;
import android.widget.ImageView;

import cn.citytag.base.R;


/**
 * Created by yangfeng01 on 2017/11/28.
 */
public class ProgressImageView extends ImageView {

    public ProgressImageView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public ProgressImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public ProgressImageView(Context context) {
        super(context);
        init();
    }

    private void init() {
        setBackgroundResource(R.drawable.spinner);
        final AnimationDrawable frameAnimation = (AnimationDrawable) getBackground();
        post(new Runnable(){
            public void run(){
                frameAnimation.start();
            }
        });
    }
}