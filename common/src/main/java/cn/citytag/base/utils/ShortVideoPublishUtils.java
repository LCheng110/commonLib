package cn.citytag.base.utils;

import android.app.Activity;

/**
 * 作者：lnx. on 2018/12/25 19:15
 */
public class ShortVideoPublishUtils {



    private static Activity activity ;


    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        ShortVideoPublishUtils.activity = activity;
    }
}
