package cn.citytag.base.app.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.citytag.base.helpers.aroute.ARouteHandleType;
import cn.citytag.base.helpers.aroute.IntentRoute;

import static cn.citytag.base.app.receiver.BroadcastReceiverManager.ACTION_LOGOUT;

/**
 * Created by yangfeng01 on 2018/1/4.
 */

public class AppBroadCastReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        int type = intent.getIntExtra("type", 1);


        if (ACTION_LOGOUT.equals(action)) { // 退出登录

            IntentRoute.getIntentRoute().withType(ARouteHandleType.LOGOUT_TYPE)
                    .withExtra(String.valueOf(type)).navigation();

        }


    }
}
