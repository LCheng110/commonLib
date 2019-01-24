package cn.citytag.base.network.converter;

import android.support.annotation.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Converter;
import retrofit2.Retrofit;

/**
 * Created by yangfeng01 on 2017/11/17.
 *
 * request和response的转换器
 */
public class FastJsonConverterFactory extends Converter.Factory {

	public static FastJsonConverterFactory create() {
		return new FastJsonConverterFactory();
	}

	/**
	 * 重写父类中的requestBodyConverter，用来转换发送给服务器的数据
	 *
	 * @param type
	 * @param parameterAnnotations
	 * @param methodAnnotations
	 * @param retrofit
	 * @return
	 */
	@Override
	public Converter<?, RequestBody> requestBodyConverter(Type type, Annotation[] parameterAnnotations, Annotation[]
			methodAnnotations, Retrofit retrofit) {
		return new FastJsonRequestBodyConverter<>();
	}

	/**
	 * 重写父类中的responseBodyConverter，用来转换服务器返回数据
	 *
	 * @param type
	 * @param annotations
	 * @param retrofit
	 * @return
	 */
	@Nullable
	@Override
	public Converter<ResponseBody, ?> responseBodyConverter(Type type, Annotation[] annotations, Retrofit retrofit) {
		return new FastJsonResponseBodyConverter<>(type);
	}
}
