package cn.citytag.base.network.converter;

import android.support.annotation.Nullable;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by yangfeng01 on 2018/1/4.
 */

public class StringConverterFactory extends Converter.Factory {
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8");

	public static StringConverterFactory create() {
		return new StringConverterFactory();
	}

	@Nullable
	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		if (!String.class.equals(type)) {
			return null;
		}
		return new Converter<ResponseBody, String>() {
			@Override
			public String convert(ResponseBody value) throws IOException {
				return value.string();
			}
		};
	}

	@Nullable
	@Override
	public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[]
			methodAnnotations, Retrofit retrofit) {
		if (!String.class.equals(type)) {
			return null;
		}
		return new Converter<String, RequestBody>() {
			@Override
			public RequestBody convert(String value) {
				return RequestBody.create(MEDIA_TYPE_JSON, value);
			}
		};
	}
}
