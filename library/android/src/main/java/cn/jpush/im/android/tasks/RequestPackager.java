package cn.jpush.im.android.tasks;

/**
 * Created by hxhg on 2017/6/30.
 */

public abstract class RequestPackager {

    int limitPerRequestInFastNetWork = 3000;
    int limitPerRequestInSlowNetWork = 300;

    RequestPackager(int limitInFastNetWork, int limitInSlowNetWork) {
        limitPerRequestInFastNetWork = limitInFastNetWork;
        limitPerRequestInSlowNetWork = limitInSlowNetWork;
    }

    public abstract void prepareToRequest(long uid, int totalCount, String convId, Object callback, Object... params);

    protected abstract void sendRequest(long uid);

    public abstract void updateTotalCount(long uid, int totalCount);

    public abstract void clearCache();

}
