package cn.citytag.base.network;


//import com.readystatesoftware.chuck.ChuckInterceptor;

import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import cn.citytag.base.network.converter.FastJsonConverterFactory;
import cn.citytag.base.network.converter.StringConverterFactory;
import cn.citytag.base.network.interceptor.CommonHeadersInterceptor;
import cn.citytag.base.network.interceptor.ResponseInterceptor;
import cn.citytag.base.utils.L;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;

/**
 * Created by yangfeng01 on 2017/11/13.
 * <p>
 * Single instance
 */

public class HttpClient {

    public static final String HTTP_TAG = "HttpClient";
    private static final int DEFAULT_CONNECT_TIMEOUT = 6;
    private static final int DEFAULT_READ_TIMEOUT = 15;
    private static final int DEFAULT_WRITE_TIMEOUT = 15;

    private Retrofit retrofit;
    private static TreeMap<String, Object> sApiMap;

    private HttpClient() {
        update();
    }

    private void update() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(DEFAULT_CONNECT_TIMEOUT, TimeUnit.SECONDS);
        builder.readTimeout(DEFAULT_READ_TIMEOUT, TimeUnit.SECONDS);
        builder.writeTimeout(DEFAULT_WRITE_TIMEOUT, TimeUnit.SECONDS);

        HttpLoggingInterceptor loggingInterceptor = new okhttp3.logging.HttpLoggingInterceptor();
        if (L.sDebug) {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
        } else {
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
        }
        // TODO: 2018/1/5 先去掉loggingInterceptor，为了使ResponseInterceptor生效，原因不明
//        builder.addInterceptor(new ChuckInterceptor(BaseConfig.getContext()).showNotification(true));
        builder.addInterceptor(loggingInterceptor);
        builder.addInterceptor(new CommonHeadersInterceptor());
        builder.addInterceptor(new ResponseInterceptor());

        retrofit = new Retrofit.Builder()
                .baseUrl(HttpConstant.API_URL)    // 	// 五：http://192.168.127.214:8080/	// 涛：http://192.168.130.115:8080/  杰//http://192.168.130.80:8080/    奎//192.168.127.226
                .addConverterFactory(StringConverterFactory.create())
                .addConverterFactory(FastJsonConverterFactory.create())    // requestBody转换格式发送，responseBody转换成客户端需要的格式
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())  // 将返回的Call<T>转换成RxJava的Observerable<T>
                .client(builder.build())
                .build();

    }

    private static TreeMap<String, Object> getApiMap() {
        if (sApiMap == null) {
            sApiMap = new TreeMap<>();
        }
        return sApiMap;
    }

    public static <T> T getApi(Class<T> clazz) {
        if (getApiMap().containsKey(clazz.getSimpleName())) {
            return (T) getApiMap().get(clazz.getSimpleName());
        }
        T api = HttpClient.getInstance().retrofit.create(clazz);
        getApiMap().put(clazz.getSimpleName(), api);
        return api;
    }

    private static HttpClient getInstance() {
        return HttpClientHolder.HTTP_CLIENT;
    }

    private static class HttpClientHolder {
        private static final HttpClient HTTP_CLIENT = new HttpClient();
    }

}