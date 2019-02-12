package cn.citytag.base.network.exception;

import android.net.ParseException;
import android.os.NetworkOnMainThreadException;
import android.support.annotation.NonNull;

import com.google.gson.JsonParseException;

import org.json.JSONException;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.net.UnknownServiceException;

import retrofit2.HttpException;

/**
 * Created by yangfeng01 on 2017/11/29.
 */

public class ApiExceptionUtil {

    /**
     * 将服务端正常返回的数据但是属于自定义异常的通过code和message包装成ApiException
     *
     * @param code
     * @param message
     * @return
     */
    public static ApiException onError(int code, String message) {
        return new ApiException(code, message);
    }

    /**
     * 将原始异常转换成自定义的ApiException
     *
     * @param t
     * @return
     */
    public static ApiException onError(@NonNull Throwable t) {
        ApiExceptionEnum apiExceptionEnum = null;
        if (t instanceof HttpException) {
            apiExceptionEnum = ApiExceptionEnum.API_HTTP_EXCEPTION;
        } else if (t instanceof SocketTimeoutException) {
            apiExceptionEnum = ApiExceptionEnum.API_TIME_OUT_EXCEPTION;
        } else if (t instanceof ConnectException) {
            apiExceptionEnum = ApiExceptionEnum.API_CONNECT_EXCEPTION;
        } else if (t instanceof UnknownHostException) {
            apiExceptionEnum = ApiExceptionEnum.API_UNKNOWN_HOST_EXCEPTION;
        } else if (t instanceof UnknownServiceException) {
            apiExceptionEnum = ApiExceptionEnum.API_UNKNOWN_SERVICE_EXCEPTION;
        } else if (t instanceof IOException) {
            apiExceptionEnum = ApiExceptionEnum.API_IO_EXCEPTION;
        } else if (t instanceof NetworkOnMainThreadException) {
            apiExceptionEnum = ApiExceptionEnum.API_NETWORK_ON_MAIN_THREAD_EXCEPTION;
        } else if (t instanceof JsonParseException
                || t instanceof JSONException
                || t instanceof ParseException) {
            apiExceptionEnum = ApiExceptionEnum.API_PARSE_EXCEPTION;
        } else {
            apiExceptionEnum = ApiExceptionEnum.API_EXCEPTION_DEFAULT;
        }
        return new ApiException(apiExceptionEnum);
    }
}
