package cn.citytag.base.network.exception;

/**
 * Created by yangfeng01 on 2017/11/17.
 *
 */
public class ApiException extends RuntimeException {

	private int code;
	private String message;

	public ApiException(String message) {
		super(message);
		this.message = message;
	}

	public ApiException(ApiExceptionEnum apiExceptionEnum) {
		this(apiExceptionEnum.getErrorCode(), "当前网络不稳定，请检查网络");
	}

	public ApiException(int code, String message) {
		super(message);
		this.code = code;
		this.message = message;
	}

	public int getCode() {
		return code;
	}

	@Override
	public String getMessage() {
		return message;
	}

}