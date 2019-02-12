package cn.citytag.base.utils.network;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.text.TextUtils;
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import cn.citytag.base.config.BaseConfig;

/**
 * Author: Lusheast
 * E-mail：zhangxiaoyu@maopp.cn
 * Date: on 2019/1/14 09:52
 * Desc:
 */
public class NetStateReceiver extends BroadcastReceiver {
    public static final String TAG = "NetStateReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "网络状态发生变化");
        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.LOLLIPOP) {
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            //获取ConnectivityManager对象对应的NetworkInfo对象
            //获取WIFI连接的信息
            NetworkInfo wifiNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            //获取移动数据连接的信息
            NetworkInfo dataNetworkInfo = connMgr.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            if (wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_WIFI));
                BaseConfig.setIsWife(true);
                BaseConfig.setIs4G(true);
                Log.i(TAG, "WIFI已连接,移动数据已连接");
            } else if (wifiNetworkInfo.isConnected() && !dataNetworkInfo.isConnected()) {
                Log.i(TAG, "WIFI已连接,移动数据已断开");
                BaseConfig.setIsWife(true);
                BaseConfig.setIs4G(false);
                EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_WIFI));
            } else if (!wifiNetworkInfo.isConnected() && dataNetworkInfo.isConnected()) {
                Log.i(TAG, "WIFI已断开,移动数据已连接");
                BaseConfig.setIsWife(false);
                BaseConfig.setIs4G(true);
                EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_MOBILE));

            } else {
                Log.i(TAG, "WIFI已断开,移动数据已断开");
                BaseConfig.setIsWife(false);
                BaseConfig.setIs4G(false);
                EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_NONE));
            }
        } else {
            //获得ConnectivityManager对象
            ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

            //获取所有网络连接的信息
            Network[] networks = connMgr.getAllNetworks();
            //用于存放网络连接信息
            StringBuilder sb = new StringBuilder();
            //通过循环将网络信息逐个取出来
            for (int i = 0; i < networks.length; i++) {
                //获取ConnectivityManager对象对应的NetworkInfo对象
                NetworkInfo networkInfo = connMgr.getNetworkInfo(networks[i]);
                sb.append(networkInfo.getTypeName() + " connect is " + networkInfo.isConnected());
            }

            if (TextUtils.isEmpty(sb)) {
                Log.i(TAG, "WIFI已断开,移动数据已断开");
                BaseConfig.setIsWife(false);
                BaseConfig.setIs4G(false);
                EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_NONE));
            }else{
                Log.i(TAG, "当前网络状态-->>>"+sb.toString());
                if (sb.toString().contains("WIFI")) {
                    BaseConfig.setIsWife(true);
                    BaseConfig.setIs4G(false);
                    EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_WIFI));

                } else if (sb.toString().contains("MOBILE")) {
                    BaseConfig.setIsWife(false);
                    BaseConfig.setIs4G(true);
                    EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_MOBILE));

                } else {
                    BaseConfig.setIsWife(false);
                    BaseConfig.setIs4G(false);
                    EventBus.getDefault().post(new NetChangeEvent(NetChangeEvent.NETWORK_NONE));
                }
            }
        }

    }
}
