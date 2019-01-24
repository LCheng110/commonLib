package cn.citytag.base.helpers.other_helper;

import cn.citytag.base.app.BaseModel;
import cn.citytag.base.model.OSSModel;
import io.reactivex.Observable;
import retrofit2.http.GET;

public interface ComApi {

    /** STS临时授权访问 */
    @GET("sts/getStsToken")
    Observable<BaseModel<OSSModel>> getStsToken();
}
