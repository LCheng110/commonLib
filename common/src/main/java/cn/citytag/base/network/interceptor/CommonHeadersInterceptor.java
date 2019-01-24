package cn.citytag.base.network.interceptor;

import android.util.Log;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.utils.SensorsDataUtils;
import cn.citytag.base.utils.StringUtils;
import cn.citytag.base.utils.Utils;
import okhttp3.Interceptor;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by yangfeng01 on 2017/11/14.
 * <p>
 * 公共请求头
 */
public class CommonHeadersInterceptor implements Interceptor {

    private Map<String, String> headerMap = new HashMap<>();

    @Override
    public Response intercept(Chain chain) throws IOException {
        String token = BaseConfig.getToken();
        if (StringUtils.isEmpty(token)) {
            token = "";
        }
        String sign = BaseConfig.getSign();
        if (StringUtils.isEmpty(sign)) {
            sign = "";
        }
        Request request;
//        if(chain.request().url().uri().toString().contains("idCardFile")){
//            request  = chain.request()
//                    .newBuilder()
//                    .addHeader("netType", "a")
//                    .addHeader("appVersion", Utils.getVersionName(BaseConfig.getContext()))
//                    .addHeader("token", token)
//                    .addHeader("Context-Type","Multipart/Form-Data")
//                    .addHeader("sign", sign)
//                    .addHeader("userId", BaseConfig.getUserId() + "")
//                    .addHeader("tokenKey", BaseConfig.getBqsDF())
//                    .addHeader("downChannel", BaseConfig.getDownChannel())
//                    .addHeader("equipNum", BaseConfig.getEquipNum())
//                    .addHeader("idfa", BaseConfig.getAndroidId(BaseConfig.getContext()) + "")
//                    .build();
//        }else {
        request = chain.request()
                .newBuilder()
                .addHeader("netType", "a")
                .addHeader("appVersion", Utils.getVersionName(BaseConfig.getContext()))
                .addHeader("token", token)
                .addHeader("sign", sign)
                .addHeader("userId", (BaseConfig.getUserId() == 0 ? "" : BaseConfig.getUserId()) + "")
                .addHeader("tokenKey", BaseConfig.getBqsDF())
                .addHeader("downChannel", BaseConfig.getDownChannel())
                .addHeader("equipNum", BaseConfig.getEquipNum())
                .addHeader("idfa", BaseConfig.getAndroidId(BaseConfig.getContext()) + "")
                .addHeader("anonymousId", SensorsDataUtils.getAnonymousId())
                .build();

        Log.i("okhttp", "intercept: " + request.toString());
//        }
        return chain.proceed(request);
    }

    public static class Builder {

        private CommonHeadersInterceptor interceptor;

        public Builder() {
            interceptor = new CommonHeadersInterceptor();
        }

        public Builder addHeader(String key, String value) {
            interceptor.headerMap.put(key, value);
            return this;
        }
    }
}
