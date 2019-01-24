package cn.jpush.im.android.tasks;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.utils.AndroidUtil;
import cn.jpush.im.android.utils.Logger;

/**
 * Created by hxhg on 2017/7/6.
 */

public class GetGroupInfoListTaskMng extends RequestPackager {
    private static final String TAG = "GetGroupInfoListTaskMng";

    private static int MSG_LIMIT_PER_REQUEST_FAST_NETWORK = 3000;
    private static int MSG_LIMIT_PER_REQUEST_SLOW_NETWORK = 300;

    private static GetGroupInfoListTaskMng instance = null;

    //该批次请求所缓存的会话对象map
    private Map<Long, GroupEntity> cachedEntities = new HashMap<Long, GroupEntity>();
    private int entitiesSent = 0;//已发送过请求的entity数。


    private GetGroupInfoListTask.GetGroupInfoInBatchCallback getGroupInfoListCallback = new GetGroupInfoListTask.GetGroupInfoInBatchCallback() {
        @Override
        public void gotResult(int responseCode, String responseMessage, Map<Long, GroupEntity> entities) {
            for (GroupEntity entity : entities.values()) {
                //批量会话消息获取成功后，回调每个SyncConv对应的callback
                ((GetGroupInfoCallback) entity.callback).gotResult(responseCode, responseMessage, entity.getGroupInfo());
            }
        }
    };

    private GetGroupInfoListTaskMng(int limitInFastNetWork, int limitInSlowNetWork) {
        super(limitInFastNetWork, limitInSlowNetWork);
    }

    public static synchronized GetGroupInfoListTaskMng getInstance() {
        if (null == instance) {
            instance = new GetGroupInfoListTaskMng(MSG_LIMIT_PER_REQUEST_FAST_NETWORK, MSG_LIMIT_PER_REQUEST_SLOW_NETWORK);
        }
        return instance;
    }

    @Override
    public void prepareToRequest(long uid, int totalCount, String convId, Object callback, Object... params) {
        int perRequestMsgLimit;
        if (AndroidUtil.isConnectionFast()) {
            perRequestMsgLimit = limitPerRequestInFastNetWork;
        } else {
            perRequestMsgLimit = limitPerRequestInSlowNetWork;
        }

        if (null != params && params.length > 0) {
            Long gid = (Long) params[0];
            Logger.d(TAG, "current request limit is " + perRequestMsgLimit + " totalEntitiesCount = " + totalCount + " entities sent = " + entitiesSent);
            GroupEntity entity = new GroupEntity(gid, callback);
            if ((perRequestMsgLimit < (cachedEntities.size() + 1))) {
                Logger.d(TAG, "cached msg cnt exceed its limit, send request and clear cache");
                //如果之前累积的gid数量加上本次请求的gid,超过了一次http请求的上限，则直接把之前缓存的entities发起一次请求。
                sendRequest(uid);
            }
            cachedEntities.put(gid, entity);

            if (cachedEntities.size() >= (totalCount - entitiesSent)) {
                sendRequest(uid);
                entitiesSent = 0;
            }
        } else {
            Logger.ww(TAG, "params are null");
        }
    }

    @Override
    protected void sendRequest(long uid) {
        if (cachedEntities.isEmpty()) {
            Logger.w(TAG, "cachedEntities is empty, return from sendRequest");
            return;
        }
        Logger.d(TAG, "send request , cachedCnt = " + cachedEntities.size() + "cachedEntities = " + cachedEntities);
        Map<Long, GroupEntity> requestEntitiesCopy = new HashMap<Long, GroupEntity>(cachedEntities);
        new GetGroupInfoListTask(uid, requestEntitiesCopy, getGroupInfoListCallback, false).execute();
        entitiesSent += requestEntitiesCopy.size();
        clearCache();
    }

    @Override
    public void updateTotalCount(long uid, int totalCount) {
        Logger.d(TAG, "total conv count to " + totalCount + " cur conv count = " + cachedEntities.size());
        if (cachedEntities.size() >= (totalCount - entitiesSent)) {
            sendRequest(uid);
            entitiesSent = 0;
        }
    }

    @Override
    public void clearCache() {
        cachedEntities.clear();
    }

    public static class GroupEntity {
        private long gid;
        private GroupInfo groupInfo;
        private Object callback;

        public GroupEntity(long gid, Object callback) {
            this.gid = gid;
            this.callback = callback;
        }

        public Object getCallback() {
            return callback;
        }

        public void setGroupInfo(GroupInfo groupInfo) {
            this.groupInfo = groupInfo;
        }

        public GroupInfo getGroupInfo() {
            return groupInfo;
        }

        @Override
        public String toString() {
            return "GroupEntity{" +
                    "gid=" + gid +
                    ", groupInfo=" + groupInfo +
                    ", callback=" + callback +
                    '}';
        }
    }

}
