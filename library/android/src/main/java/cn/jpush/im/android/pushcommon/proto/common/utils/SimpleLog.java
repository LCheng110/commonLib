package cn.jpush.im.android.pushcommon.proto.common.utils;

public class SimpleLog {
	private static final String TAG = "[JPush] - ";
	
	public static void debug(String msg) {
	}
	
	public static void info(String msg) {
		System.out.println(TAG + msg);
		
	}
	
	public static void warn(String msg) {
		System.out.println(TAG + msg);
		
	}
	
	public static void error(String msg) {
		System.out.println(TAG + msg);
		
	}
}
