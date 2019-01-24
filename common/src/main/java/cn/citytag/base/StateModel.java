package cn.citytag.base;

import android.databinding.BaseObservable;

import java.util.List;

import static cn.citytag.base.State.CONTENT;
import static cn.citytag.base.State.EMPTY;
import static cn.citytag.base.State.ERROR;
import static cn.citytag.base.State.LOADING;

/**
 * Created by yangfeng01 on 2018/4/19.
 */
public class StateModel extends BaseObservable {

	@State
	private int state;

	private OnLceCallback callback;

	public int getState() {
		return state;
	}

	public void setState(@State int state) {
		this.state = state;
		switch (state) {
			case CONTENT:
				callback.onContent();
				break;

			case LOADING:
				callback.onLoading();
				break;

			case EMPTY:
				callback.onEmpty();
				break;

			case ERROR:
				callback.onError();
				break;

			default:
				break;
		}
		notifyChange();
	}

	public void setState(@State int state, List<Integer> skipIds) {
		this.state = state;
		switch (state) {
			case CONTENT:
				callback.onContent(skipIds);
				break;

			case LOADING:
				callback.onLoading(skipIds);
				break;

			case EMPTY:
				callback.onEmpty(skipIds);
				break;

			case ERROR:
				callback.onError(skipIds);
				break;

			default:
				break;
		}
		notifyChange();
	}

	public void setCallback(OnLceCallback callback) {
		this.callback = callback;
	}

}
