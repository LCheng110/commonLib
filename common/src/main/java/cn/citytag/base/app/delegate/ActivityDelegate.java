package cn.citytag.base.app.delegate;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * Created by yangfeng01 on 2017/11/9.
 */

public interface ActivityDelegate extends BaseDelegate {

	void onCreate(@Nullable Bundle savedInstanceState);

	void onPostCreate(@Nullable Bundle savedInstanceState);

	void onNewIntent(Intent intent);

	void onStart();

	void onResume();

	void onPause();

	void onStop();

	void onDestroy();

	void onActivityResult(int requestCode, int resultCode, Intent data);

}
