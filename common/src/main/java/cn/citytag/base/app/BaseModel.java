package cn.citytag.base.app;

import java.io.Serializable;

/**
 * Created by yangfeng01 on 2017/11/3.
 */

public class BaseModel<T> implements Serializable {

	private static final long serialVersionUID = 1L;

	private int code;
	private String msg;

	/** 真正需要的数据，这里的T类型可能为对象，数组，还可能是布尔值，字符串... */
	private T data;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

	public T getData() {
		return data;
	}

	public void setData(T data) {
		this.data = data;
	}

	/**
	 * 是否请求成功，可重写
	 *
	 * @return
	 */
	public boolean isSuccess() {
		return code == 1000;
	}

}
