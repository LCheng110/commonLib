package cn.jpush.im.android.tasks;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class GetGroupMembersTask extends AbstractTask {

    private static final int ERROR_NO_NEED_TO_REQUEST = -201;
    private static final int MAX_RETRY_TIME = 3;

    private static final String TAG = "GetGroupMembersTask";
    private long groupID;
    private boolean canReturnFromLocal;//标志这个任务是否可以从本地直接返回数据
    private GetGroupMembersCallback callback;

    //将所有正在请求群成员的gid缓存起来， 防止同一时间有多个相同gid的群成员请求发出，造成不必要的开销
    private final static Set<Long> sGidCache = new HashSet<Long>();

    public GetGroupMembersTask(long groupID, GetGroupMembersCallback callback,
                               boolean waitForCompletion, boolean canReturnFromLocal) {
        super(callback, waitForCompletion);
        this.groupID = groupID;
        this.callback = callback;
        this.canReturnFromLocal = canReturnFromLocal;
    }

    private String createGetGroupMembersUrl() {
        return JMessage.httpUserCenterPrefix + "/groups/" + groupID + "/members";
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }
        try {
            //重复请求检查，如果已有相同gid的任务正在执行，则其他相同的请求将会等待
            duplicateDownloadCheck(groupID);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        List<Long> memberIds = GroupStorage.queryMemberUserIdsSync(groupID);
        if (canReturnFromLocal && null != memberIds && !memberIds.isEmpty()) {
            //canReturnFromLocal为true，说明是sdk内部为了同步群成员数据而发起的请求，此时如果本地已经有members数据了
            //则可以忽略这个请求直接从本地返回。
            //相对应的，如果canReturnFromLocal为false,说明这个请求是用户调用api发起的请求，此时就算本地有数据也不应
            //从本地返回，应该请求后台
            Logger.d(TAG, "no need to send request when get group member.");
            ResponseWrapper responseWrapper = new ResponseWrapper();
            responseWrapper.responseCode = ERROR_NO_NEED_TO_REQUEST;
            responseWrapper.responseContent = JsonUtil.toJson(memberIds);
            return responseWrapper;
        }


        String url = createGetGroupMembersUrl();
        sGidCache.add(groupID);//启动下载前将gid加入缓存，防止重复下载
        String authBase = StringUtils
                .getBasicAuthorization(String.valueOf(IMConfigs.getUserID()), IMConfigs.getToken());
        try {
            response = mHttpClient.sendGet(url, authBase);
        } catch (APIRequestException e) {
            response = e.getResponseWrapper();
        } catch (APIConnectionException e) {
            response = null;
        }
        return response;
    }

    private void getUserInfos(List<Long> userIds) {

        final Map<Long, UserInfo> idToUserInfoMap = new LinkedHashMap<Long, UserInfo>();
        ArrayList<Object> idsNotFound = new ArrayList<Object>();
        for (long uid : userIds) {
            //检查是否有对应的userinfo
            UserInfo info = UserInfoManager.getInstance().getUserInfo(uid);
            if (null == info) {
                idToUserInfoMap.put(uid, null);
                idsNotFound.add(uid);
            } else {
                idToUserInfoMap.put(uid, info);
            }
        }

        Logger.d(TAG, "ids NotFound : " + idsNotFound);
        if (0 < idsNotFound.size()) {
            //有一些id没有找到对应的userinfo,启动批量下载userinfo。
            GetUserInfoListTask task = new GetUserInfoListTask(idsNotFound, GetUserInfoListTask.IDType.uid, new GetUserInfoListCallback() {
                @Override
                public void gotResult(int code, String msg, List<UserInfo> userList) {
                    Logger.d(TAG, "[get group member task] get userinfo list code = " + code + " msg = " + msg);
                    if (0 == code && null != userList) {
                        for (UserInfo info : userList) {
                            idToUserInfoMap.put(info.getUserID(), info);
                        }
                        Logger.d(TAG, "idToUserInfoMap = " + idToUserInfoMap);
                        Collection<UserInfo> values = idToUserInfoMap.values();
                        if (values.contains(null)) { //拿到的结果中包含有null说明有用户的uid对应的info不存在，返回错误。
                            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS_DESC);
                        } else {
                            List<UserInfo> infos = new ArrayList<UserInfo>();
                            infos.addAll(values);
                            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, infos);
                        }

                    } else {
                        CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                    }
                    //唤醒其他所有等待线程
                    notifyAllThenRemove(groupID);//将gid从缓存中移除,同时唤醒其他等待线程。

                }
            }, false);

            try {
                /*
                直接在当前线程执行getUserList任务，不要使用task.execute。
                如果用execute将会把任务放进线程池中等待调度执行，在工作线程全被GetGroupMember
                任务占用的情况下会导致死锁。*/
                task.doPostExecute(task.doExecute());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            List<UserInfo> infos = new ArrayList<UserInfo>();
            infos.addAll(idToUserInfoMap.values());
            //如果此任务可以直接从本地返回，则返回时直接在caller thread中回调callback,优化效率。
            CommonUtils.doCompleteCallBackToUser(canReturnFromLocal, callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, infos);
            //获取成功，唤醒其他线程。
            notifyAllThenRemove(groupID);
        }
    }

    private void duplicateDownloadCheck(long gid) throws InterruptedException {
        synchronized (sGidCache) {
            int retryTime = 0;
            while (sGidCache.contains(gid)) {
                Logger.d(TAG, "[getGroupMembersTask]contains duplicate gid." + gid);
                //缓存中存在重复的gid，说明已有任务在运行，使用wait等待其执行完后唤醒。
                sGidCache.wait(45 * 1000);
                Logger.d(TAG, "wakeup from duplicate check");
                if (++retryTime > MAX_RETRY_TIME) {
                    Logger.d(TAG, "break from getMemberTask duplicate check");
                    break;
                }
            }
            sGidCache.add(gid);
        }
    }

    private void notifyAllThenRemove(long gid) {
        Logger.d(TAG, "notifyAllThenRemove ! gid = " + gid);
        synchronized (sGidCache) {
            sGidCache.remove(gid);
            sGidCache.notifyAll();
        }
    }

    public static void clearGidCache() {
        sGidCache.clear();
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        final List<Long> memberIDs = new ArrayList<Long>();
        List<Members> memberList = JsonUtil
                .formatToGivenTypeOnlyWithExpose(resultContent,
                        new TypeToken<List<Members>>() {
                        });
        // 获取uid的list
        long ownerID = 0;
        for (Members member : memberList) {
            long userID = member.getUid();
            if (member.getFlag() == 1) {
                ownerID = member.getUid();
            }
            memberIDs.add(userID);
        }
        Logger.d(TAG, "memberIDs = " + memberIDs + " owner id = " + ownerID);
        final ContentValues values = new ContentValues();
        values.put(GroupStorage.GROUP_OWNER_ID, ownerID);
        values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(memberIDs));
        GroupStorage.updateValuesSync(groupID, values);

        //获取群成员（uid list）后需要同时获取uid所对应的userinfo
        getUserInfos(memberIDs);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        if (responseCode == ERROR_NO_NEED_TO_REQUEST) {
            List<Long> memberIds = JsonUtil.formatToGivenType(responseMsg, new TypeToken<List<Long>>() {
            });
            getUserInfos(memberIds);
            return;
        }
        notifyAllThenRemove(groupID);
        CommonUtils.doCompleteCallBackToUser(callback, responseCode,
                responseMsg);
    }

    class Members {

        @Expose
        private long uid;

        @Expose
        private long juid;

        @Expose
        private int flag;

        @Expose
        private String username;

        @Expose
        private String password;

        @Expose
        private String app_key;

        @Expose
        private ArrayList<Object> groups;

        public long getUid() {
            return uid;
        }

        public int getFlag() {
            return flag;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        @Override
        public String toString() {
            return "Members{" +
                    "uid=" + uid +
                    ", juid=" + juid +
                    ", flag=" + flag +
                    ", username='" + username + '\'' +
                    ", password='" + password + '\'' +
                    ", app_key='" + app_key + '\'' +
                    ", groups=" + groups +
                    '}';
        }
    }

}
