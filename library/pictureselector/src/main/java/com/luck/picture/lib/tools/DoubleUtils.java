package com.luck.picture.lib.tools;

/**
 * author：luck
 * project：PictureSelector
 * package：com.luck.picture.lib.tool
 * email：893855882@qq.com
 * data：2017/5/25
 */

public class DoubleUtils {
    /**
     * Prevent continuous click, jump two pages
     */
    private static long lastClickTime;
    private final static long TIME = 800;
    private final static long CamreaTIME = 1000;

    public static boolean isFastDoubleClick() {
        long time = System.currentTimeMillis();
        if (time - lastClickTime < TIME) {
            return true;
        }
        lastClickTime = time;
        return false;
    }
    public static boolean isFastDoubleClick(long lastTime) {
        long time = System.currentTimeMillis();
        if (time - lastTime < CamreaTIME) {
            return true;
        }
        return false;
    }
}
