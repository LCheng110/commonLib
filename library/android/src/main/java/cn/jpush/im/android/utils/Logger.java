package cn.jpush.im.android.utils;

import cn.jiguang.ald.api.BaseLogger;

/**
 * 默认日志不开放给应用开发者。
 * 方法名是 2 个字母的，应用开发者可看到日志。
 *
 * INTERNAL_USE 控制是否是开发者模式。对外发布时应设置为 false
 *
 * release: 默认打印Logger.ww以上的级别; 如果设置了setDebugMode,则打印Logger.dd以上的
 * debug:  打印 Logger.d和Logger.dd以上的级别
 * dev : 打印所有的日志
 */
public class Logger {
    private static BaseLogger baseLogger = new JMessageLogger();
    public static void setLogger(BaseLogger logger){
        baseLogger = logger;
    }
	public Logger() {
	}
    // 在不打印日志的前提下，可避免字符串的组装
    public static void _d(String tag, String message, Object... args) {
        baseLogger._d(tag,message,args);
    }
    public static void v(String tag, String msg) {
       baseLogger.v(tag,msg);
    }
    
    public static void vv(String tag, String msg) {
       baseLogger.vv(tag,msg);
    }
    
    public static void d(String tag, String msg) {
       baseLogger.d(tag,msg);
    }
    
    public static void dd(String tag, String msg) {
      baseLogger.dd(tag,msg);
    }
    
    public static void i(String tag, String msg) {
      baseLogger.i(tag,msg);
    }
    
    public static void ii(String tag, String msg) {
       baseLogger.ii(tag,msg);
    }
    
    public static void w(String tag, String msg) {
       baseLogger.w(tag,msg);
    }
    
    public static void ww(String tag, String msg) {
      baseLogger.ww(tag,msg);
    }
    
    public static void e(String tag, String msg) {
       baseLogger.e(tag,msg);
    }
    
    public static void ee(String tag, String msg) {
       baseLogger.ee(tag,msg);
    }
    
    
    public static void v(String tag, String msg, Throwable tr) {
       baseLogger.v(tag,msg,tr);
    }

    public static void vv(String tag, String msg, Throwable tr) {
       baseLogger.vv(tag,msg,tr);
    }
    
    public static void d(String tag, String msg, Throwable tr) {
        baseLogger.d(tag,msg,tr);
    }

    public static void dd(String tag, String msg, Throwable tr) {
        baseLogger.dd(tag,msg,tr);
    }

    public static void i(String tag, String msg, Throwable tr) {
        baseLogger.i(tag,msg,tr);
    }

    public static void ii(String tag, String msg, Throwable tr) {
        baseLogger.ii(tag,msg,tr);
    }

    public static void w(String tag, String msg, Throwable tr) {
        baseLogger.w(tag,msg,tr);
    }

    public static void ww(String tag, String msg, Throwable tr) {
        baseLogger.ww(tag,msg,tr);
    }

    public static void e(String tag, String msg, Throwable tr) {
        baseLogger.e(tag,msg,tr);
    }
    
    public static void ee(String tag, String msg, Throwable tr) {
        baseLogger.ee(tag,msg,tr);
    }
    public static void flushCached2File() {
       baseLogger.flushCached2File();
    }
}
