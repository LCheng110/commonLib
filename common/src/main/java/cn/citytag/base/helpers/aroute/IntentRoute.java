package cn.citytag.base.helpers.aroute;

import android.content.Intent;
import android.os.Bundle;

import cn.citytag.base.config.BaseConfig;

/**
 * Li
 */
public class IntentRoute {

    private Intent intent; //跳转intent

    private final String COM_INTENT = "cn.citytag.mapgo.view.activity.ARouteActivity";
    private volatile static IntentRoute intentRoute = null;

    public static IntentRoute getIntentRoute() {
        if (intentRoute == null)
            synchronized (IntentRoute.class) {
                if (intentRoute == null) {
                    intentRoute = new IntentRoute();
                }
            }

        return intentRoute;
    }

    /**
     * app 模块识别跳转的type
     */
    public IntentRoute withType(int type) {
        intent = new Intent();
        intent.putExtra(ARouteHandleType.TYPE, type);
        return intentRoute;
    }

    /**
     * with Extra  附加信息
     */
    public IntentRoute withExtra(String extra) {
        intent.putExtra(ARouteHandleType.EXTRA, extra);
        return intentRoute;
    }

    public IntentRoute withExtra2(String extra2) {
        intent.putExtra(ARouteHandleType.EXTRA2, extra2);
        return intentRoute;
    }

    public IntentRoute withExtra3(String extra3) {
        intent.putExtra(ARouteHandleType.EXTRA3, extra3);
        return intentRoute;
    }

    public IntentRoute withExtra4(String extra4) {
        intent.putExtra(ARouteHandleType.EXTRA4, extra4);
        return intentRoute;
    }

    public IntentRoute withExtraBundle(Bundle bundle) {
        intent.putExtra(ARouteHandleType.EXTRA_BUNDLE, bundle);
        return intentRoute;
    }

    /**
     * 进行跳转
     */
    public void navigation() {
        intent.setAction(COM_INTENT);
//        if (BaseConfig.getCurrentActivity() != null) {
//            BaseConfig.getCurrentActivity().startActivity(intent);
//        }

        if (BaseConfig.getContext() != null){
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK );
            BaseConfig.getContext().startActivity(intent);
        }
    }
}
