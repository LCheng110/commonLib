package cn.jpush.im.android.helpers.sync;

/**
 * Created by hxhg on 2017/6/27.
 */

abstract class SyncRespEntity {

    String convId;
    SyncRespBaseHandler.Watcher operationWatcher;

    SyncRespEntity(String convId, SyncRespBaseHandler.Watcher operationWatcher) {
        this.convId = convId;
        this.operationWatcher = operationWatcher;
    }

    protected abstract void startLocalize();
}
