package cn.citytag.base.app.observer;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.CallSuper;

import cn.citytag.base.app.BaseModel;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.helpers.aroute.ARouteHandleType;
import cn.citytag.base.helpers.aroute.IntentRoute;
import cn.citytag.base.network.HttpConstant;
import cn.citytag.base.network.exception.ApiException;
import cn.citytag.base.network.exception.ApiExceptionUtil;
import cn.citytag.base.utils.GuestJudgeUtils;
import cn.citytag.base.utils.L;
import cn.citytag.base.widget.ProgressHUD;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

/**
 * Created by yangfeng01 on 2017/11/28.
 */

public abstract class BaseObserver<T> implements Observer<BaseModel<T>> {

    private static final String TAG = "BaseObserver";

    private Context context;
    private Disposable disposable;

    /**
     * 最终的回调
     *
     * @param t data或者整个ApiResponse
     */
    public abstract void onNext2(@NonNull T t);

    /**
     * 最终的回调
     *
     * @param t 转换过后的Exception
     */
    public abstract void onError2(@NonNull Throwable t);

    /**
     * 当返回码不是成功码的时候回调此方法，可以重写
     *
     * @param msg code
     */
    public void onFail(int code, String msg) {

    }

    /**
     * 返回信息（msg）
     *
     * @param msg
     */
    public void onNext1(String msg) {

    }

    public BaseObserver(Activity context) {
        this(context, true);
    }

    public BaseObserver() {

    }

    public BaseObserver(Context context, boolean showProgress) {
        this(context, showProgress, null);
    }

    public BaseObserver(Context context, boolean showProgress, String loadingText) {
        this.context = context;
        if (showProgress) {
            ProgressHUD.show(context, loadingText, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!disposable.isDisposed()) {
                        disposable.dispose();
                    }
                }
            });
        }
    }

    public BaseObserver(Context context, boolean showProgress, String loadingText, DialogInterface.OnCancelListener cancelListener) {
        this.context = context;
        if (showProgress) {
            ProgressHUD.show(context, loadingText, true, new DialogInterface.OnCancelListener() {
                @Override
                public void onCancel(DialogInterface dialog) {
                    if (!disposable.isDisposed()) {
                        disposable.dispose();
                    }
                    if (cancelListener != null) {
                        cancelListener.onCancel(dialog);
                    }
                }
            });
        }
    }

    @Override
    public void onSubscribe(@NonNull Disposable d) {
        L.d(TAG, "onSubscribe d == " + d);
        disposable = d;
    }

    @Override
    public void onNext(@NonNull BaseModel<T> value) {
        L.d(TAG, "onNext");
        ProgressHUD.dismissHUD();

        int code = value.getCode();
        if (code != HttpConstant.CODE_SUCCESS
                && code != 10010
                && code != 10011
                && code != 10050) {
            disposeCodeDataNullIng(value);
        } else {
            T t = value.getData();
            onNext1(value.getMsg());
            onNext2(t);

        }

    }

    /**
     * @param value 当data为空时
     */
    private void disposeCodeDataNullIng(BaseModel<T> value) {
        if (value.getCode() == 30001) {
            IntentRoute.getIntentRoute().withType(ARouteHandleType.CODE_LOGOUT_TYPE)
                    .navigation();
        } else if (value.getCode() == 800001) {
            GuestJudgeUtils.checkGuest(BaseConfig.getCurrentActivity());
        } else {
            // Toast.makeText(BaseConfig.getContext(), value.getMsg(), Toast.LENGTH_SHORT).show();
            // onFail(value.getCode(), value.getMsg());
            onFailure(new ApiException(value.getCode(), value.getMsg()));
        }

    }

    @Override
    public void onError(@NonNull Throwable e) {
        ProgressHUD.dismissHUD();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
        // 将其他所有异常转换为ApiException
        onFailure(ApiExceptionUtil.onError(e));

    }

    @Override
    public void onComplete() {
        L.d(TAG, "onComplete");
        ProgressHUD.dismissHUD();
        if (!disposable.isDisposed()) {
            disposable.dispose();
        }
    }

    @CallSuper
    public void onFailure(@NonNull Throwable t) {
        L.d(TAG, "onFailure");
        // 一些统一处理的操作
        // UIUtils.toastMessage(t.getMessage());
        // 子类去处理
        onError2(t);
    }

}
