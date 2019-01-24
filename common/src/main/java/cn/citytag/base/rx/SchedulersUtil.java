package cn.citytag.base.rx;

import io.reactivex.Observable;
import io.reactivex.ObservableSource;
import io.reactivex.ObservableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by yangfeng01 on 2017/11/29.
 *
 * RxJava相关的帮助类
 */

public class SchedulersUtil {

	/**
	 * 切换线程
	 * observable.compose(SwitchSchedulers.applySchedulers())
	 *
	 * @param <T>
	 * @return
	 */
	public static <T>ObservableTransformer<T, T> applySchedulers() {
		return new ObservableTransformer<T, T>() {
			@Override
			public ObservableSource<T> apply(@NonNull Observable<T> observable) {
				return observable.subscribeOn(Schedulers.io())
						.observeOn(AndroidSchedulers.mainThread());

			}
		};
	}
}
