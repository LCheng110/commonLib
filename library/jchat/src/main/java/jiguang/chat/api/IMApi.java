package jiguang.chat.api;

import com.alibaba.fastjson.JSONObject;

import cn.citytag.base.app.BaseModel;
import io.reactivex.Observable;
import jiguang.chat.model.IMUserIdModel;
import jiguang.chat.model.OrderPayInfoModel;
import jiguang.chat.model.UserIsTeacherModel;
import retrofit2.http.Body;
import retrofit2.http.POST;
import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Url;

/**
 * Created by zhaoyuanchao on 2018/8/21.
 */

public interface IMApi {
    /**支付页-获取订单详情**/
    @POST("/order/getOrderById")
    Observable<BaseModel<OrderPayInfoModel>> getOrderById(@Body JSONObject jsonObject);

    /**
     * 根据手机号查询userID
     * @param jsonObject
     * @return
     */
    @POST("/im/findUserIdByPhone")
    Observable<BaseModel<IMUserIdModel>> getIMUserId(@Body JSONObject jsonObject);

    /**
     * 是否是达人
     */
    @POST("/user/checkUserIsTeacher")
    Observable<BaseModel<UserIsTeacherModel>> checkUserIsTeacher(@Body JSONObject jsonObject);

    /**
     * 文件下载
     */
    @GET
    Observable<ResponseBody> downloadFile(@Url String fileUrl);


}
