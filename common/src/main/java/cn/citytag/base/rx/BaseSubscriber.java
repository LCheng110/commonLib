package cn.citytag.base.rx;

import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;

/**
 * Created by yangfeng01 on 2017/11/28.
 */

public class BaseSubscriber<T> implements Subscriber<T> {

	@Override
	public void onSubscribe(Subscription s) {

	}

	@Override
	public void onNext(T t) {

	}

	@Override
	public void onError(Throwable t) {

	}

	@Override
	public void onComplete() {

	}
}
