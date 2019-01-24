package cn.citytag.base.helpers.other_helper;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.widget.TextView;

/**
 * 作者：Lgc
 * 创建时间：2018/11/16
 * 更改时间：2018/11/16
 */
public class HomePageTabAnimHelper {

    /**
     * 字体变大
     * @param textView
     */
    public static void animBigFont(TextView textView) {
        if (textView == null) return;
        ObjectAnimator anim = ObjectAnimator.ofFloat(textView
                , "scaleY", 1f, 22f / 16f);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(textView
                , "scaleX", 1f, 22f / 16f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anim, anim1);
        set.setDuration(500);
        set.start();
    }

    /**
     * 字体变小
     * @param textView
     */
    public static void animSmallFont(TextView textView) {
        if (textView == null) return;
        ObjectAnimator anim = ObjectAnimator.ofFloat(textView
                , "scaleY", 22f / 16f, 1f);
        ObjectAnimator anim1 = ObjectAnimator.ofFloat(textView
                , "scaleX", 22f / 16f, 1f);
        AnimatorSet set = new AnimatorSet();
        set.playTogether(anim, anim1);
        set.setDuration(500);
        set.start();
    }
}
