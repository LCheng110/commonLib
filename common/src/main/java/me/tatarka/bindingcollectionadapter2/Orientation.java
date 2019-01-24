package me.tatarka.bindingcollectionadapter2;

import android.support.annotation.IntDef;
import android.support.v7.widget.LinearLayoutManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Created by yangfeng01 on 2018/3/23.
 */
@IntDef({LinearLayoutManager.VERTICAL, LinearLayoutManager.HORIZONTAL})
@Retention(RetentionPolicy.SOURCE)
public @interface Orientation {

}
