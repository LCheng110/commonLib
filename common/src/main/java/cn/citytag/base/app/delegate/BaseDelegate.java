package cn.citytag.base.app.delegate;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by yangfeng01 on 2017/11/9.
 */

public interface BaseDelegate {

	void onCreate(@Nullable Bundle savedInstanceState);

	void onStart();

	void onResume();

	void onPause();

	void onStop();

	void onDestroy();

}
