package cn.citytag.base.network.interceptor;

import com.alibaba.fastjson.JSON;

import java.io.IOException;

import cn.citytag.base.app.BaseModel;
import okhttp3.Interceptor;
import okhttp3.MediaType;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Created by ASUS on 2018/1/5.
 */

public class ResponseInterceptor implements Interceptor {
	private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=UTF-8");

	@Override
	public Response intercept(Chain chain) throws IOException {
		Response response = chain.proceed(chain.request());
		if (response.code() == 500) {
			//throw ApiExceptionUtil.onError(500, "");
			try {
				final String msg = response.body().string();
				BaseModel baseModel = new BaseModel();
				baseModel.setCode(500);
				baseModel.setMsg("网络异常，请稍后重试");
				baseModel.setData(null);
				byte[] bodyBytes = JSON.toJSONBytes(baseModel);
				String str = new String(bodyBytes);
				return response.newBuilder().code(500).body(ResponseBody.create(MEDIA_TYPE_JSON,
						bodyBytes)).build();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return response;
	}
}
