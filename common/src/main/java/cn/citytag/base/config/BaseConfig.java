package cn.citytag.base.config;

import android.app.Activity;
import android.content.Context;
import android.provider.Settings;
import android.text.TextUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import cn.citytag.base.utils.Md5Util;

/**
 * Created by yangfeng01 on 2017/11/14.
 */

public class BaseConfig {

    //昵称 头像 性别

    private static Context applicationContext;
    private static WeakReference<Activity> currentActivity;// 当前正在显示的Activity
    private static WeakReference<Activity> verifyActivity;// 实名认证后调回的Activity

    /**
     * 是否Debug模式
     */
    private static boolean sIsDebug;
    private static boolean isLogin = false;
    private static boolean isGuest = true;
    private static boolean isLoginNew = false;

    private static String bqsDF = ""; //白骑士

    private static String equipNum = "";  //设备标识

    private static long userId;          // 用户ID
    private static String userName;      // 用户名称
    private static String userAvatar;    // 用户头像
    private static String userSex;       // 用户性别
    private static String token = "";    // token
    private static String userSign = ""; // 腾讯IM的userSign
    private static boolean isMineInit = false;

    public static String getEquipNum() {
        return equipNum;
    }

    public static void setEquipNum(String equipNumVar) {
        equipNum = equipNumVar;
    }

    private static String sign = "";     // token加盐
    private static String alias;

    private static String longitude = "";        // 经度
    private static String latitude = "";        // 纬度
    private static String countryName = "";        // 国家名称
    private static String cityCode = "";        // 城市编码
    private static String provinceName = "";        // 省名称
    private static String cityName = "";        // 城市名称
    private static String areaCode = "";        // 地区代码
    private static String areaName = "";        // 地区名称
    private static String locationAddress = "";    // 具体地址
    private static String poiName = "";
    private static String backGround = "";         //背景图
    private static boolean isNet = false;
    private static int RealState = 0;

    private static boolean isHaveNet;
    private static boolean isWife;
    private static boolean is4G;
    private static int isValue = -1;  //是否打开邀请好友

    private static boolean isActor = false;  //是否加入家族

    private static String downChannel = "";

    private static ArrayList<String> mWhiteList;   //webView时是否注入js的白名单

    private static boolean isOppoGameOn = false;
    private static boolean isGameOn = false ;

    private static String ContractSkillId;
    private static boolean isShowBoard;
    private static String multiUrl;

    public static String getMultiUrl() {
        return multiUrl;
    }

    public static void setMultiUrl(String multiUrl) {
        BaseConfig.multiUrl = multiUrl;
    }

    public static String getContractSkillId() {
        return ContractSkillId;
    }

    public static void setContractSkillId(String contractSkillId) {
        ContractSkillId = contractSkillId;
    }

    public static boolean isIsShowBoard() {
        return isShowBoard;
    }

    public static void setIsShowBoard(boolean isShowBoard) {
        BaseConfig.isShowBoard = isShowBoard;
    }

    public static boolean isIsOppoGameOn() {
        return isOppoGameOn;
    }

    public static void setIsOppoGameOn(boolean isOppoGameOn) {
        BaseConfig.isOppoGameOn = isOppoGameOn;
    }

    public static void setIsGameOn(boolean isGameOn){
        BaseConfig.isGameOn =isGameOn ;
    }

    public static boolean isIsGameOn() {
        return isGameOn;
    }

    public static ArrayList<String> getWhiteList() {
        return mWhiteList;
    }

    public static void setWhiteList(ArrayList<String> mWhiteList) {
        BaseConfig.mWhiteList = mWhiteList;
    }

    public static String getDownChannel() {
        return downChannel;
    }

    public static void setDownChannel(String downChannelVar) {
        downChannel = downChannelVar;
    }

    public static boolean isIsHaveNet() {
        return isHaveNet;
    }

    public static void setIsHaveNet(boolean isHaveNet) {
        BaseConfig.isHaveNet = isHaveNet;
    }

    public static boolean isIsWife() {
        return isWife;
    }

    public static void setIsWife(boolean isWife) {
        BaseConfig.isWife = isWife;
    }

    public static boolean isIs4G() {
        return is4G;
    }

    public static void setIs4G(boolean is4G) {
        BaseConfig.is4G = is4G;
    }

    public static void init(Context context) {
        applicationContext = context;
    }

    public static Context getContext() {
        return applicationContext;
    }

    public static String getBqsDF() {
        return bqsDF;
    }

    public static void setBqsDF(String bqsDFVar) {
        bqsDF = bqsDFVar;
    }

    /**
     * 是否已登录
     *
     * @return
     */
    @Deprecated
    public static boolean isLogin() {
        //return !StringUtils.isEmpty(token) && !StringUtils.isEmpty(rongToken) && userId != 0;
        //if (isLogin)
        //return true;
        //return StringUtils.isNotEmpty(rongToken);
        return isLogin;
    }

    public static void setIsLogin(boolean isLogin) {

        BaseConfig.isLogin = isLogin;
    }

    /**
     * 是否登录 包含游客模式 352新加
     *
     * @return
     */
    public static boolean isLoginNew() {
        //return !StringUtils.isEmpty(token) && !StringUtils.isEmpty(rongToken) && userId != 0;
        return isLoginNew;

    }

    public static void setIsLoginNew(boolean isLoginNew) {

        BaseConfig.isLoginNew = isLoginNew;
    }


    /**
     * 是否是游客模式
     *
     * @return
     */
    public static boolean isGuest() {
        //return !StringUtils.isEmpty(token) && !StringUtils.isEmpty(rongToken) && userId != 0;
        return isGuest;
    }

    public static void setIsGuest(boolean isGuest) {

        BaseConfig.isGuest = isGuest;
    }

    public static void setApplicationContext(Context applicationContext) {
        BaseConfig.applicationContext = applicationContext;
    }

    public static Activity getCurrentActivity() {
        return currentActivity != null ? currentActivity.get() : null;
    }

    public static void setCurrentActivity(Activity activity) {
        currentActivity = new WeakReference<>(activity);
    }

    public static boolean isDebug() {
        return sIsDebug;
    }

    public static void setUserId(long userId) {
        BaseConfig.userId = userId;
    }




    public static long getUserId() {
        return userId;
    }

    public static String getUserName() {
        return userName;
    }

    public static void setUserName(String userName) {
        BaseConfig.userName = userName;
    }

    public static String getUserAvatar() {
        return userAvatar;
    }

    public static void setUserAvatar(String userAvatar) {
        BaseConfig.userAvatar = userAvatar;
    }

    public static String getUserSex() {
        return userSex;
    }

    public static void setUserSex(String userSex) {
        BaseConfig.userSex = userSex;
    }

    public static String getToken() {
        return token;
    }

    public static void setToken(String token) {
        BaseConfig.token = token;
    }

    public static String getSign() {
        if (TextUtils.isEmpty(token)) {
            return "";
        }
        return Md5Util.generate(token);
//        return token;
    }

    public static String getUserSign() {
        return userSign;
    }

    public static void setUserSign(String userSign) {
        BaseConfig.userSign = userSign;
    }

    public static String getAlias() {
        return alias;
    }

    public static void setAlias(String alias) {
        BaseConfig.alias = alias;
    }



    public static void setIsDebug(boolean isDebug) {
        sIsDebug = isDebug;
    }

    public static String getLongitude() {
        return longitude;
    }

    public static void setLongitude(String longitude) {
        BaseConfig.longitude = longitude;
    }

    public static String getLatitude() {
        return latitude;
    }

    public static void setLatitude(String latitude) {
        BaseConfig.latitude = latitude;
    }

    public static String getCountryName() {
        return countryName;
    }

    public static void setCountryName(String countryName) {
        BaseConfig.countryName = countryName;
    }

    public static String getCityCode() {
        return cityCode;
    }

    public static void setCityCode(String cityCode) {
        BaseConfig.cityCode = cityCode;
    }

    public static String getCityName() {
        return cityName;
    }

    public static void setCityName(String cityName) {
        if (!TextUtils.isEmpty(cityName)) {
            BaseConfig.cityName = cityName;
        }
    }

    public static String getAreaCode() {
        return areaCode;
    }

    public static void setAreaCode(String areaCode) {
        BaseConfig.areaCode = areaCode;
    }

    public static String getAreaName() {
        return areaName;
    }

    public static void setAreaName(String areaName) {
        BaseConfig.areaName = areaName;
    }

    public static String getLocationAddress() {
        return locationAddress;
    }

    public static void setLocationAddress(String locationAddress) {
        BaseConfig.locationAddress = locationAddress;
    }



    public static String getPoiName() {
        return poiName;
    }

    public static void setPoiName(String poiName) {
        BaseConfig.poiName = poiName;
    }

    public static boolean isNet() {
        return isNet;
    }

    public static void setIsNet(boolean isNet) {
        BaseConfig.isNet = isNet;
    }

    public static String getProvinceName() {
        return provinceName;
    }

    public static void setProvinceName(String provinceName) {
        BaseConfig.provinceName = provinceName;
    }

    public static int getRealState() {
        return RealState;
    }

    public static void setRealState(int realState) {
        RealState = realState;
    }

    public static String getBackGround() {
        return backGround;
    }

    public static void setBackGround(String backGround) {
        BaseConfig.backGround = backGround;
    }

    public static int getIsValue() {
        return isValue;
    }

    public static void setIsValue(int isValue) {
        BaseConfig.isValue = isValue;
    }

    public static String getAndroidId(Context context) {
        String ANDROID_ID = Settings.System.getString(context.getContentResolver(), Settings.System.ANDROID_ID);
        return ANDROID_ID;
    }

    public static boolean isIsActor() {
        return isActor;
    }

    public static void setIsActor(boolean isActor) {
        BaseConfig.isActor = isActor;
    }

    public static boolean isIsMineInit() {
        return isMineInit;
    }

    public static void setIsMineInit(boolean isMineInit) {
        BaseConfig.isMineInit = isMineInit;
    }

    public static Activity getVerifyActivity() {
        return verifyActivity != null ? verifyActivity.get() : null;
    }

    public static void setVerifyActivity(Activity activity) {
        verifyActivity = new WeakReference<>(activity);
    }

}
