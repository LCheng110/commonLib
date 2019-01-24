package cn.citytag.base.network.entity;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import java.lang.reflect.Type;

import cn.citytag.base.utils.StringUtils;

/**
 * Created by yangfeng01 on 2017/11/17.
 */
public class ApiResponse {

	private JSONObject response;
	private int code;
	private String msg;
	private JSONObject data;

	public ApiResponse(JSONObject jsonObject) {
		response = jsonObject;
		if (response.containsKey("code")) {
			code = response.getIntValue("code");
		}
		if (response.containsKey("msg")) {
			msg = response.getString("msg");
		}
		if (response.containsKey("data")) {
			data = response.getJSONObject("data");
		}
	}

	public JSONObject getResponse() {
		return response;
	}

	public int getCode() {
		return code;
	}

	public String getMsg() {
		return msg;
	}

	public JSONObject getData() {
		return data;
	}

	/**
	 *
	 *
	 * @param type
	 * @param <T>
	 * @return
	 */
	public <T> T getData(Type type) {
		if (data != null && !StringUtils.isEmpty(data.toJSONString())) {
			return JSON.parseObject(data.toJSONString(), type);
		} else {
			return null;
		}
	}

}
