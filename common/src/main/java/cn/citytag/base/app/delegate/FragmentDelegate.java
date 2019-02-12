package cn.citytag.base.app.delegate;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.animation.Animation;

/**
 * Created by yangfeng01 on 2017/11/8.
 *
 * Fragment回调方法的代理接口
 */

public interface FragmentDelegate extends BaseDelegate {

	void onAttach(Activity activity);

	void onCreate(@Nullable Bundle savedInstanceState);

	Animation onCreateAnimation(int transit, boolean enter, int nextAnim);

	//@Nullable
	//public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState);

	void onViewCreated(View view, @Nullable Bundle savedInstanceState);

	void onActivityCreated(@Nullable Bundle savedInstanceState);

	void onStart();

	void onViewStateRestored(@Nullable Bundle savedInstanceState);

	void onResume();

	void onPause();

	void onSaveInstanceState(Bundle outState);

	void onStop();

	void onDestroyView();

	void onDestroy();

	void onHiddenChanged(boolean hidden);

	void setUserVisibleHint(boolean isVisibleToUser);

}
