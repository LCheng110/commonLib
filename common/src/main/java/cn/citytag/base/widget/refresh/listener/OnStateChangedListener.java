package cn.citytag.base.widget.refresh.listener;


import android.support.annotation.NonNull;

import cn.citytag.base.widget.refresh.api.RefreshLayout;
import cn.citytag.base.widget.refresh.constant.RefreshState;

/**
 * 刷新状态改变监听器
 * Created by SCWANG on 2017/5/26.
 */

public interface OnStateChangedListener {
    /**
     * 状态改变事件 {@link RefreshState}
     * @param refreshLayout RefreshLayout
     * @param oldState 改变之前的状态
     * @param newState 改变之后的状态
     */
    void onStateChanged(@NonNull RefreshLayout refreshLayout, @NonNull RefreshState oldState, @NonNull RefreshState
            newState);
}
