package cn.citytag.base.vm;

import java.util.List;

/**
 * Created by yangfeng01 on 2018/4/27.
 */
public interface OnLceCallback {

	boolean isLoading();

	void onLoading();

	void onLoading(List<Integer> skipIds);

	void onContent();

	void onContent(List<Integer> skipIds);

	void onEmpty();

	void onEmpty(List<Integer> skipIds);

	void onError();

	void onError(List<Integer> skipIds);
}
