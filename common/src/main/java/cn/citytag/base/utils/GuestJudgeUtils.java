package cn.citytag.base.utils;


import android.app.Activity;

import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.helpers.aroute.ARouteHandleType;
import cn.citytag.base.helpers.aroute.IntentRoute;

/**
 * 作者：lnx. on 2018/10/9 14:52
 * 判断是否是游客
 */
public class GuestJudgeUtils {


    private static Activity activity ;

    public static boolean checkGuest(Activity activityFrom){

        setActivity(activityFrom);
        if(BaseConfig.isGuest()){
//            Navigation.startLoginRegister();
            IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_LOGIN).navigation();
            return true;
        }
        return  false;
    }

    public static Activity getActivity() {
        return activity;
    }

    public static void setActivity(Activity activity) {
        GuestJudgeUtils.activity = activity;
    }






}
