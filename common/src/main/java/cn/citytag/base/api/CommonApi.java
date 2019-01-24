package cn.citytag.base.api;

import com.alibaba.fastjson.JSONObject;

import cn.citytag.base.app.BaseModel;
import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.POST;

/**
 * Created by yuhuizhong on 2018/4/28.
 * 通用类
 */

public interface CommonApi {

    /**打赏**/
    @POST("reward/doReward")
    Observable<BaseModel> checkMsg(@Body JSONObject jsonObject);
}
