package cn.citytag.base.network.converter;

import com.alibaba.fastjson.JSON;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import retrofit2.Converter;

/**
 * Created by yangfeng01 on 2017/11/17.
 *
 * 将传过来的数据转换成requestBody
 */


final class FastJsonRequestBodyConverter<T> implements Converter<T, RequestBody> {

	/** 传json字符串的media type */
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8");

	@Override
	public RequestBody convert(T value) {
		byte[] bodyBytes = JSON.toJSONBytes(value);
		return RequestBody.create(MEDIA_TYPE_JSON, bodyBytes);
	}

}
