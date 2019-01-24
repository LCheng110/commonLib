package com.luck.picture.lib.tools;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.widget.TextViewCompat;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 作者：Lgc
 * 创建时间：2018/11/1
 * 更改时间：2018/11/1
 */
public class ToastUtils {
    private static final int COLOR_DEFAULT = 0xFEFFFFFF;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    private static Toast sToast;
    private static int sGravity = -1;
    private static int sXOffset = -1;
    private static int sYOffset = -1;
    private static int sBgColor = COLOR_DEFAULT;
    private static int sBgResource = -1;
    private static int sMsgColor = COLOR_DEFAULT;

    private ToastUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    /**
     * 安全地显示长时吐司
     *
     * @param text 文本
     */
    public static void showLong(@NonNull final CharSequence text, Context context) {
        show(text, Toast.LENGTH_LONG, context);
    }

    private static void show(final CharSequence text, final int duration, final Context context) {
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                cancel();
                sToast = Toast.makeText(context, text, duration);
                TextView tvMessage = sToast.getView().findViewById(android.R.id.message);
                int msgColor = tvMessage.getCurrentTextColor();
                //it solve the font of toast
                TextViewCompat.setTextAppearance(tvMessage, android.R.style.TextAppearance);
                if (sMsgColor != COLOR_DEFAULT) {
                    tvMessage.setTextColor(sMsgColor);
                } else {
                    tvMessage.setTextColor(msgColor);
                }
                if (sGravity != -1 || sXOffset != -1 || sYOffset != -1) {
                    sToast.setGravity(sGravity, sXOffset, sYOffset);
                }
                setBg(tvMessage);
                sToast.show();
            }
        });
    }

    private static void setBg(final TextView tvMsg) {
        View toastView = sToast.getView();
        if (sBgResource != -1) {
            toastView.setBackgroundResource(sBgResource);
            tvMsg.setBackgroundColor(Color.TRANSPARENT);
        } else if (sBgColor != COLOR_DEFAULT) {
            Drawable tvBg = toastView.getBackground();
            Drawable msgBg = tvMsg.getBackground();
            if (tvBg != null && msgBg != null) {
                tvBg.setColorFilter(new PorterDuffColorFilter(sBgColor, PorterDuff.Mode.SRC_IN));
                tvMsg.setBackgroundColor(Color.TRANSPARENT);
            } else if (tvBg != null) {
                tvBg.setColorFilter(new PorterDuffColorFilter(sBgColor, PorterDuff.Mode.SRC_IN));
            } else if (msgBg != null) {
                msgBg.setColorFilter(new PorterDuffColorFilter(sBgColor, PorterDuff.Mode.SRC_IN));
            } else {
                toastView.setBackgroundColor(sBgColor);
            }
        }
    }

    /**
     * 取消吐司显示
     */
    public static void cancel() {
        if (sToast != null) {
            sToast.cancel();
            sToast = null;
        }
    }
}
