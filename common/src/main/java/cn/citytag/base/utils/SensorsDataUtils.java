package cn.citytag.base.utils;

import android.content.Context;
import android.util.Log;

import com.sensorsdata.analytics.android.sdk.SensorsDataAPI;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import cn.citytag.base.config.BaseConfig;

import static com.sensorsdata.analytics.android.sdk.SensorsDataAPI.DebugMode.DEBUG_AND_TRACK;
import static com.sensorsdata.analytics.android.sdk.SensorsDataAPI.sharedInstance;

/**
 * Created by yuhuizhong on 2018/7/24.
 * 神策埋点Utils
 */

public class SensorsDataUtils {
    //http://maopao-cloud.cloud.sensorsdata.cn:8006/sa?project=default&token=f927ea42096dfa42
    //http://maopao-cloud.cloud.sensorsdata.cn:8006/sa?project=production&token=f927ea42096dfa42
    private static final String SA_SERVER_DEBUG_URL = "http://maopao-cloud.cloud.sensorsdata.cn:8006/sa?project=default&token=f927ea42096dfa42";
    private static final String SA_SERVER_URL = "http://maopao-cloud.cloud.sensorsdata.cn:8006/sa?project=production&token=f927ea42096dfa42";
    private static final SensorsDataAPI.DebugMode SA_DEBUG_MODE = SensorsDataAPI.DebugMode.DEBUG_ONLY; // 对应 测试预发环境，切换到线上时 使用 DEBUG_OFF
    private static final SensorsDataAPI.DebugMode SA_DEBUG_MODE_OFF = SensorsDataAPI.DebugMode.DEBUG_OFF; //对应 正式环境，上架应用市场
    private static final SensorsDataAPI.DebugMode SA_DEBUG_MODE_TRACK = DEBUG_AND_TRACK; //对应测试环境，

    public static void init(Context context) {
        if (BaseConfig.isDebug()) {
            sharedInstance(context,                               // 传入 Context
                    SA_SERVER_DEBUG_URL,                      // 数据接收的 URL
                    DEBUG_AND_TRACK);
        } else {
            SensorsDataAPI.sharedInstance(
                    context,                               // 传入 Context
                    SA_SERVER_URL,                      // 数据接收的 URL
                    SA_DEBUG_MODE_OFF);
        }

        try {
            JSONObject properties = new JSONObject();
            properties.put("PlatformType", "Android");
            sharedInstance().registerSuperProperties(properties);

            if (BaseConfig.getUserId() != 0) {
                SensorsDataAPI.sharedInstance().login(BaseConfig.getUserId() + "");
            }

            // 打开自动采集, 并指定追踪哪些 AutoTrack 事件
            List<SensorsDataAPI.AutoTrackEventType> eventTypeList = new ArrayList<>();
            // $AppStart
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_START);
            // $AppEnd
            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_END);
//            // $AppViewScreen
//            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_VIEW_SCREEN);
//            // $AppClick
//            eventTypeList.add(SensorsDataAPI.AutoTrackEventType.APP_CLICK);
            sharedInstance().enableAutoTrack(eventTypeList);
            sharedInstance().enableLog(false);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 渠道统计及激活事件
     */
    public static void ChannelTrack() {

        try {
            String downloadChannel = null;
            downloadChannel = com.sensorsdata.analytics.android.sdk.util.SensorsDataUtils.getApplicationMetaData(BaseConfig.getContext(),
                    "UMENG_CHANNEL");
            JSONObject properties = new JSONObject();
            //这里示例 DownloadChannel 记录下载商店的渠道(下载渠道)。如果需要多个字段来标记渠道包，请按业务实际需要添加。
            properties.put("DownloadChannel", downloadChannel);
            //记录激活事件、渠道追踪，这里激活事件取名为 AppInstall。
            SensorsDataAPI.sharedInstance().trackInstallation("AppInstall", properties);
            String var = properties.getString("DownloadChannel");
            Log.e("properties", var);
            // Log.e(" properties",properties.get("DownloadChannel"))
        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    //appinstall
    public static void trackInstallation(String str) {
        SensorsDataAPI.sharedInstance().trackInstallation(str);
    }

    //获取预置属性
    public static String getPresentProperties() {
        JSONObject jsonObject = SensorsDataAPI.sharedInstance().getPresetProperties();
        return jsonObject.toString();
    }

    //追踪事件
    public static void track(String str, JSONObject jsonObject) {
        SensorsDataAPI.sharedInstance().track(str, jsonObject);
    }

    public static void track(String str) {
        SensorsDataAPI.sharedInstance().track(str);
    }

    //事件时长 - 开始
    public static void trackTimerStart(String str) {
        SensorsDataAPI.sharedInstance().trackTimerStart(str);
    }

    //事件时长 - 结束
    public static void trackTimerEnd(String str, JSONObject jsonObject) {
        SensorsDataAPI.sharedInstance().trackTimerEnd(str, jsonObject);
    }

    //识别用户
    public static String getAnonymousId() {
        return SensorsDataAPI.sharedInstance().getAnonymousId();
    }

    //用户注册
    public static void login(String userId) {
        SensorsDataAPI.sharedInstance().login(userId);
    }

    //设置用户属性
    public static void profileSet(JSONObject jsonObject) {
        SensorsDataAPI.sharedInstance().profileSet(jsonObject);
    }

    //数值类型的属性
    public static void profileIncrement(String str) {
        profileIncrement(str, 1);
    }

    public static void profileIncrement(String str, int num) {
        SensorsDataAPI.sharedInstance().profileIncrement(str, num);
    }

    //开启神策Crash
    public static void trackAppCrash() {
        SensorsDataAPI.sharedInstance().trackAppCrash();
    }

    //设置预置经纬度信息
    public static void setGPSLocation(double latitude, double longitude) {
        SensorsDataAPI.sharedInstance().setGPSLocation(latitude, longitude);
    }
}
