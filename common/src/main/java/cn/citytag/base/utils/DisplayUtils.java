package cn.citytag.base.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by yangfeng01 on 2016/1/6.
 */
public class DisplayUtils {

    //    public static int getStatusBarHeight(Context mContext) {
    //        int statusBarHeight = 0;
    //        try {
    //            Class<?> c = Class.forName("com.android.internal.R$dimen");
    //            Object o = c.newInstance();
    //            Field field = c.getField("status_bar_height");
    //            int x = (Integer) field.get(o);
    //            statusBarHeight = mContext.getResources().getDimensionPixelSize(x);
    //        } catch (Exception e) {
    //            e.printStackTrace();
    //            Rect frame = new Rect();
    //            getWindow().getDecorView().getWindowVisibleDisplayFrame(frame);
    //            statusBarHeight = frame.top;
    //        }
    //        return statusBarHeight;
    //    }


    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }

    public static int dp2px(Context context, int dp) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return (int) ((dp * displayMetrics.density) + 0.5);
    }

    /**
     * achieve status bar height
     *
     * @param context
     * @return
     */
    public static int getStatusBarHeight(Context context) {
        int result = 0;
        Resources resources = context.getResources();
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = resources.getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * achieve status bar height
     * <p>
     * decorView是window中的最顶层view，可以从window中获取到decorView，然后decorView有个getWindowVisibleDisplayFrame
     * 方法可以获取到程序显示的区域，包括标题栏，但不包括状态栏。于是，我们就可以算出状态栏的高度了。
     *
     * @param activity
     * @return
     */
    public static int getStatusBarHeight(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect.top;
    }

    /**
     * 获取导航栏高度
     *
     * @param context
     * @return
     */
    public static int getNavigationBarHeight(Context context) {
        int result = 0;
        int resourceId = 0;
        int rid = context.getResources().getIdentifier("config_showNavigationBar", "bool", "android");
        if (rid != 0) {
            resourceId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
            return context.getResources().getDimensionPixelSize(resourceId);
        }
        return 0;
    }

    /**
     * 获取view相对于屏幕的绝对距离
     *
     * @param view
     * @return
     */
    public static int[] getLocationOnScreen(View view) {
        int[] locations = new int[2];
        view.getLocationOnScreen(locations);
        return locations;
    }

    /**
     * achieve Window.ID_ANDROID_CONTENT's view content
     *
     * @param activity
     * @return
     */
    public static Rect getViewRegion(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().findViewById(Window.ID_ANDROID_CONTENT).getDrawingRect(rect);
        return rect;
    }

    /**
     * achieve screen width and height
     *
     * @param context
     * @return
     */
    public static int[] getScreenSize(Context context) {
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        manager.getDefaultDisplay().getMetrics(metrics);
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;
        return new int[]{width, height};
    }

    /**
     * achieve app display region
     *
     * @param activity
     * @return
     */
    public static Rect getAppRegion(Activity activity) {
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);
        return rect;
    }

    public static int getScreenWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    public static int getScreenWidth2(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        return display.getWidth();
    }

    public static int getScreenHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

    public static int getScreenHeight2(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display display = windowManager.getDefaultDisplay();
        return display.getHeight();
    }

    public static boolean isRTLLanguage(Context context) {
        //TODO investigate why ViewUtils.isLayoutRtl(this) not working as intended
        // as getLayoutDirection was introduced in API 17, under 17 we default to LTR
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
            return false;
        }
        Configuration config = context.getResources().getConfiguration();
        return config.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL;
    }

    public static Rect getCenterRect(Context context, View view) {
        final int width = getScreenWidth(context);
        final int height = getScreenHeight(context);
        int left = width / 2 - view.getWidth() / 10;
        int top = height / 2 - view.getHeight() / 10;
        int right = left + view.getWidth() / 5;
        int bottom = top + view.getHeight() / 5;
        return new Rect(left, top, right, bottom);
    }

    public static float getDensity(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        return density;
    }

    /**
     * 判断底部navigator是否已经显示
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static int getScreenRealHeight(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);
        int realHeight = realDisplayMetrics.heightPixels;
        return realHeight;
    }

    /**
     * 判断底部navigator是否已经显示
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public static boolean isNavigationBarShow(Activity activity) {
        WindowManager windowManager = activity.getWindowManager();
        Display d = windowManager.getDefaultDisplay();
        DisplayMetrics realDisplayMetrics = new DisplayMetrics();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        d.getRealMetrics(realDisplayMetrics);
        d.getMetrics(displayMetrics);
        int realHeight = realDisplayMetrics.heightPixels;
        int height = displayMetrics.heightPixels;
        Log.i("sss", "height: " + height + "  realHeight: " + realHeight);
        return realHeight != height;
    }

    /**
     * 获取navigator高度
     */
    public static int getNavigationBarHeight(Activity activity) {
        if (!isNavigationBarShow(activity)) {
            return 0;
        }
        Resources resources = activity.getResources();
        int resourceId = resources.getIdentifier("navigation_bar_height",
                "dimen", "android");
        //获取NavigationBar的高度
        return resources.getDimensionPixelSize(resourceId);
    }
}
