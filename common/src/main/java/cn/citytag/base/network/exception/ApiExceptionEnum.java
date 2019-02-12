package cn.citytag.base.network.exception;

/**
 * Created by yangfeng01 on 2017/11/17.
 *
 * 用方便阅读的方式转换原始异常，Restful包括：
 *
 * 1.服务端自定义操作错误，code != 1000
 * 2.除服务端自定义错误外的其他异常
 */
public enum ApiExceptionEnum {

	/**
	 * common exceptions
	 */
	API_EXCEPTION_DEFAULT(0, "服务器异常，请稍后重试", "服务器异常，请稍后重试"),
	API_DATA_EMPTY(1, "data对象为空", "data对象为空"),
	API_TIME_OUT_EXCEPTION(2, "服务器响应超时", "服务器响应超时"),
	API_CONNECT_EXCEPTION(3, "当前网络不稳定，请检查网络", "网络连接异常，请检查网络"),
	API_UNKNOWN_HOST_EXCEPTION(4, "当前网络不稳定，请检查网络", "无法解析主机，请检查网络连接"),
	API_UNKNOWN_SERVICE_EXCEPTION(5, "未知的服务器错误", "未知的服务器错误"),
	API_IO_EXCEPTION(6, "当前网络不稳定，请检查网络", "没有网络，请检查网络连接"),
	API_NETWORK_ON_MAIN_THREAD_EXCEPTION(7, "主线程不能网络请求", "主线程不能网络请求"),
	API_RUNTIME_EXCEPTION(8, "运行时错误", "运行时错误"),
	API_HTTP_EXCEPTION(9, "http异常", "http异常"),
	API_PARSE_EXCEPTION(10, "转换异常", "转换异常"),
	/**
	 * server custom errors
	 */
	ERROR_BALANCE_NOT_ENOUGH(19002, "账号金额不足，请进行充值", "账号金额不足，请进行充值");




	/** 异常code */
	private int errorCode;

	/** 异常信息 */
	private String errorMsg;

	/** 异常描述 */
	private String desc;

	ApiExceptionEnum(int errorCode, String errorMsg, String desc) {
		this.errorCode = errorCode;
		this.errorMsg = errorMsg;
		this.desc = desc;
	}

	public int getErrorCode () {
		return errorCode;
	}

	public String getErrorMsg() {
		return errorMsg;
	}

	public String getDesc() {
		return desc;
	}
}
