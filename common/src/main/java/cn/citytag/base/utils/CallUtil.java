package cn.citytag.base.utils;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import cn.citytag.base.utils.ext.Act0;

/**
 * Created by yuety on 15/10/15.
 */
public class CallUtil {

    //通过handler 延时发送
    private static Handler handler;

    public static void asyncCall(int delayMillis, Act0 fun) {
        if (handler == null) {
            handler = new Handler();
        }
        handler.postDelayed(() -> fun.run(), delayMillis);
    }

    public static void asyncCall(Act0 fun) {
        asyncCall(10, fun);
    }

    public static void removeCall() {
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
    }

    public static void mainCall(Act0 fun) {
        new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(Message msg) {
                fun.run();
            }
        }.sendEmptyMessage(1);
    }
}
