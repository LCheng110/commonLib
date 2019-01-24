package cn.citytag.base.helpers.other_helper;

import android.content.Context;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

/**
 * Created by yangfeng01 on 2017/12/6.
 */

public class LocationHelper {

    private Context applicationContext;

    //声明AMapLocationClient类对象
    private AMapLocationClient locationClient;

    private AMapLocationClientOption locationOption;

    //声明定位回调监听器
    //private AMapLocationListener locationListener = new AMapLocationListener() {
    //  @Override
    //  public void onLocationChanged(AMapLocation aMapLocation) {
    //    if (aMapLocation != null) {
    //      if (aMapLocation.getErrorCode() == 0) {
    //        //解析定位结果
    //      }
    //    }
    //  }
    //};

    public static LocationHelper newInstance(Context context) {
        LocationHelper helper = new LocationHelper(context);
        return helper;
    }

    private LocationHelper(Context context) {
        init(context);
    }

    private void init(Context context) {
        applicationContext = context.getApplicationContext();
        locationClient = new AMapLocationClient(applicationContext);

        //初始化定位参数
        locationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        locationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        locationOption.setGpsFirst(false);
        //设置是否返回地址信息（默认返回地址信息）
        locationOption.setNeedAddress(true);
        locationOption.setLocationCacheEnable(true);
        //设置是否只定位一次,默认为false
        locationOption.setOnceLocation(true);
        //设置定位间隔,单位毫秒,默认为2000ms
        locationOption.setInterval(0);
        //给定位客户端对象设置定位参数
        locationClient.setLocationOption(locationOption);
    }

    public void startLocation(AMapLocationListener aMapLocationListener) {
        // 设置定位回调监听
        locationClient.setLocationListener(new AMapLocationListenerWrapper(aMapLocationListener));
        // 启动定位
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        // 启动定位
        locationClient.startLocation();
    }

    public void releaseLocation() {
        if (locationClient != null) {
            locationClient.stopLocation();
            locationClient.onDestroy();
        }
        locationClient = null;
    }

    public AMapLocation getLastLocation() {
        return locationClient.getLastKnownLocation();
    }

    private static class AMapLocationListenerWrapper implements AMapLocationListener {

        private AMapLocationListener aMapLocationListener;

        public AMapLocationListenerWrapper(AMapLocationListener aMapLocationListener) {
            this.aMapLocationListener = aMapLocationListener;
        }

        @Override
        public void onLocationChanged(AMapLocation aMapLocation) {
            aMapLocationListener.onLocationChanged(aMapLocation);
        }
    }
}