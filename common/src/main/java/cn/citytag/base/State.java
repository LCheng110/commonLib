package cn.citytag.base;

import android.support.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import static cn.citytag.base.State.CONTENT;
import static cn.citytag.base.State.EMPTY;
import static cn.citytag.base.State.ERROR;
import static cn.citytag.base.State.LOADING;

/**
 * Created by yangfeng01 on 2018/4/19.
 */
@IntDef({CONTENT, LOADING, EMPTY, ERROR})
@Retention(RetentionPolicy.SOURCE)
public @interface State {

	int CONTENT = 0;
	int LOADING = 1;
	int EMPTY = 2;
	int ERROR = 3;
}
