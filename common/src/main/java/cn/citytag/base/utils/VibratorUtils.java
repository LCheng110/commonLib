package cn.citytag.base.utils;

import android.os.Vibrator;

import cn.citytag.base.config.BaseConfig;

//振动类
public class VibratorUtils {
    private static VibratorUtils vibratorUtils;
    private boolean isVibrator;
    private Vibrator vibrator;

    public static VibratorUtils getInstance() {
        if (vibratorUtils == null) {
            vibratorUtils = new VibratorUtils();
        }
        return vibratorUtils;
    }

    private VibratorUtils() {
        vibrator = (Vibrator) BaseConfig.getContext().getSystemService(BaseConfig.getContext().VIBRATOR_SERVICE);
    }

    //振动
    public void vibrate() {
        if (!isVibrator) {
            vibrator.vibrate(new long[]{1000, 500, 1000, 500}, 0);
            isVibrator = true;
        }
    }

    //自定义振动方式
    public void vibrate(long[] array , int repeat) {
        if (!isVibrator) {
            vibrator.vibrate(array, repeat);
            isVibrator = true;
        }
    }



    //取消
    public void cancel() {
        if (isVibrator) {
            vibrator.cancel();
            isVibrator = false;
        }
    }
}
