package cn.citytag.base.network.converter;

import com.alibaba.fastjson.JSON;

import java.io.IOException;
import java.lang.reflect.Type;

import cn.citytag.base.app.BaseModel;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.network.HttpClient;
import cn.citytag.base.utils.L;
import okhttp3.ResponseBody;
import okio.BufferedSource;
import okio.Okio;
import retrofit2.Converter;

/**
 * Created by yangfeng01 on 2017/11/17.
 * <p>
 * <p>将服务端返回的responseBody数据转换成FastJson格式</p>
 *
 * @param <T> data的类型
 */
class FastJsonResponseBodyConverter<T extends BaseModel> implements Converter<ResponseBody, T> {

    private Type type;

    public FastJsonResponseBodyConverter(Type type) {
        this.type = type;
    }

    @Override
    public T convert(ResponseBody value) throws IOException {
        // TODO: 2017/11/17 因为有类型形参T，看能不能直接把T转换成实体类型
        BufferedSource bufferedSource = Okio.buffer(value.source());
        String responseStr = bufferedSource.readUtf8();
        bufferedSource.close();
        L.d(HttpClient.HTTP_TAG, "Response:" + responseStr);
        L.d(HttpClient.HTTP_TAG, "UserId:" + BaseConfig.getUserId());
        T t = null;
        try {
            t = JSON.parseObject(responseStr, type);
            // 这里不能很好地判断是不是服务器自定义失败，因为不同请求success code有可能不一样
            // TODO: 2017/12/22 ApiException也有可能需要data

        } catch (com.alibaba.fastjson.JSONException e) {
            e.printStackTrace();
        } finally {
            value.close();
        }
        return t;
    }
}
