package cn.citytag.base.utils;

import android.util.Log;

/**
 * Created by yangfeng01 on 2017/11/9.
 */
public class L {
    public static boolean sDebug = true;

    public static int v(String tag, String msg) {
        if(!sDebug)
            return 0;
        return Log.v(tag, msg);
    }

    public static int v(String tag, String msg, Throwable tr) {
        if(!sDebug)
            return 0;
        return Log.v(tag, msg, tr);
    }

    public static int d(String tag, String msg) {
        if(!sDebug)
            return 0;
        return Log.d(tag, msg);
    }

    public static int d(String tag, String msg, Throwable tr) {
        if(!sDebug)
            return 0;
        return Log.d(tag, msg, tr);
    }

    //public static int d(String format, Object... args) {
    //    return d("DF", String.format(format, args));
    //}

    public static int i(String tag, String msg) {
        if(!sDebug)
            return 0;
        return Log.i(tag, msg);
    }

    public static int i(String tag, String msg, Throwable tr) {
        if(!sDebug)
            return 0;
        return Log.i(tag, msg, tr);
    }

    public static int w(String tag, String msg) {
        return Log.w(tag, msg);
    }

    public static int w(String tag, String msg, Throwable tr) {
        return Log.w(tag, msg, tr);
    }

    public static int w(String tag, Throwable tr) {
        return Log.w(tag, tr);
    }

    public static int e(String tag, String msg) {
        return Log.e(tag, msg);
    }

    public static int e(String tag, String msg, Throwable tr) {
        return Log.e(tag, msg, tr);
    }

    public static int e(String format, Object... args) {
        return e("EF", String.format(format, args));
    }

}
