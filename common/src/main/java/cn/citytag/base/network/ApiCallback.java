package cn.citytag.base.network;

import cn.citytag.base.network.exception.ApiException;
import cn.citytag.base.utils.UIUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Created by yangfeng01 on 2017/11/17.
 *
 * 可以在这里去统一处理自定义的ApiException，以及一些公共错误的处理，也可以传T进来处理
 */

public abstract class ApiCallback<T> implements Callback<T> {

	public abstract void onSuccess(Call<T> call, Response<T> response);

	public ApiCallback() {

	}

	@Override
	public void onResponse(Call<T> call, Response<T> response) {
		//NetworkUtil.dismissCutscenes();
		if (response != null && response.isSuccessful()) {
			onSuccess(call, response);
		}
	}

	@Override
	public void onFailure(Call<T> call, Throwable t) {
		//NetworkUtil.dismissCutscenes();
		if (t instanceof ApiException) { // 自定义请求错误
			UIUtils.toastMessage(t.getMessage());
		} else { // 其他情况 网络异常，数据加载失败!
			UIUtils.toastMessage(t.getMessage());
		}
	}

}
