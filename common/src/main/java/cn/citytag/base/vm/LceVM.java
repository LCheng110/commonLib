package cn.citytag.base.vm;

import android.content.Context;
import android.support.annotation.NonNull;

import java.util.List;

import cn.citytag.base.model.State;
import cn.citytag.base.model.StateModel;
import cn.citytag.base.widget.refresh.SmartRefreshLayout;
import cn.citytag.base.widget.refresh.api.RefreshLayout;
import cn.citytag.base.widget.refresh.constant.RefreshState;
import cn.citytag.base.widget.refresh.footer.ClassicsFooter;
import cn.citytag.base.widget.refresh.header.ClassicsHeader;
import cn.citytag.base.widget.refresh.listener.OnRefreshLoadMoreListener;

/**
 * Created by yangfeng01 on 2017/11/1.
 */
public class LceVM extends BaseVM implements OnLceCallback {

    private StateModel stateModel;

    protected int currentPageBase = 1;
    protected boolean isRefreshBase = true;

    public void initStateModel(StateModel stateModel) {
        this.stateModel = stateModel;
    }

    public void setState(@State int state) {
        stateModel.setState(state);
    }

    public void setState(@State int state, List<Integer> skipIds) {
        stateModel.setState(state, skipIds);
    }

    public int getState() {
        return stateModel.getState();
    }

    @Override
    public boolean isLoading() {
        return stateModel.getState() == State.LOADING;
    }

    @Override
    public void onLoading() {
        setState(State.LOADING);
    }

    @Override
    public void onLoading(List<Integer> skipIds) {
        setState(State.LOADING, skipIds);
    }

    @Override
    public void onContent() {
        setState(State.CONTENT);
    }

    @Override
    public void onContent(List<Integer> skipIds) {
        setState(State.CONTENT, skipIds);
    }

    @Override
    public void onEmpty() {
        setState(State.EMPTY);
    }

    @Override
    public void onEmpty(List<Integer> skipIds) {
        setState(State.EMPTY, skipIds);
    }

    @Override
    public void onError() {
        setState(State.ERROR);
    }

    @Override
    public void onError(List<Integer> skipIds) {
        setState(State.ERROR, skipIds);
    }


    protected void initBaseRefresh(Context context, SmartRefreshLayout refreshLayout) {
        refreshLayout.setRefreshHeader(new ClassicsHeader(context));
        refreshLayout.setRefreshFooter(new ClassicsFooter(context));
        refreshLayout.setOnRefreshLoadMoreListener(new OnRefreshLoadMoreListener() {
            @Override
            public void onLoadMore(@NonNull RefreshLayout refreshLayout) {

                onBaseLoadMore(refreshLayout);
            }

            @Override
            public void onRefresh(@NonNull RefreshLayout refreshLayout) {

                onBaseRefresh(refreshLayout);

            }
        });

    }

    protected void getDataBase() {

    }

    protected void onBaseRefresh(RefreshLayout refreshLayout) {
        isRefreshBase = true;
        currentPageBase = 1;
        getDataBase();

    }

    protected void onBaseLoadMore(RefreshLayout refreshLayout) {
        isRefreshBase = false;
        ++currentPageBase;
        getDataBase();

    }


    protected void finishBaseRefresh(SmartRefreshLayout refreshLayout) {

        if (refreshLayout == null) {
            return;
        }

        if (refreshLayout.getState() == RefreshState.Refreshing) {
            refreshLayout.finishRefresh();
        }

        if (refreshLayout.getState() == RefreshState.Loading) {
            refreshLayout.finishLoadMore();
        }

    }

}
