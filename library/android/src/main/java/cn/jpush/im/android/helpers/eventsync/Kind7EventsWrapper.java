package cn.jpush.im.android.helpers.eventsync;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.storage.EventIdListManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetBlackListTask;
import cn.jpush.im.android.tasks.GetBlockedGroupsTask;
import cn.jpush.im.android.tasks.GetFriendListTask;
import cn.jpush.im.android.tasks.GetGroupInfoListTask;
import cn.jpush.im.android.tasks.GetGroupInfoListTaskMng;
import cn.jpush.im.android.tasks.GetNoDisturbListTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/7/31.
 */

public class Kind7EventsWrapper extends EventNotificationsWrapper {
    private static final String TAG = Kind7EventsWrapper.class.getSimpleName();

    public static final int EVENT_KIND_7 = 7;//UserProfile相关列表数据多端在线更新同步通知事件

    //===== 以下extra以及kind定义见wiki：http://wiki.jpushoa.com/pages/viewpage.action?pageId=12683930 ====//
    private static final int EXTRA_TARGET_ADDED = 1; //目标被添加
    private static final int EXTRA_TARGET_REMOVED = 2;//目标被删除
    private static final int EXTRA_CONTACT_ADDED = 5;//好友被添加
    private static final int EXTRA_CONTACT_DELETED = 6;//好友被删除
    private static final int EXTRA_CONTACT_INFO_UPDATED = 7;//好友信息被更新
    private static final int EXTRA_NODISTURB_ADD_SINGLE = 31;//用户被加入免打扰列表
    private static final int EXTRA_NODISTURB_REMOVE_SINGLE = 32;//用户被移除免打扰列表
    private static final int EXTRA_NODISTURB_ADD_GROUP = 33;//群组被加入免打扰列表
    private static final int EXTRA_NODISTURB_REMOVE_GROUP = 34;//群组被移除免打扰列表
    private static final int EXTRA_NODISTURB_ADD_GLOBAL = 35;//设置全局免打扰
    private static final int EXTRA_NODISTURB_REMOVE_GLOBAL = 36;//关闭全局免打扰

    private static final int EVENT_TYPE_CONTACT = 100;//好友应答同意、删除好友请求和更新好友备注信息
    private static final int EVENT_TYPE_BLACKLIST = 101;//黑名单添加、删除成功
    private static final int EVENT_TYPE_NODISTURB = 102;//免打扰添加、删除成功
    private static final int EVENT_TYPE_GROUP_BLOCK = 103;//群屏蔽添加、删除成功

    private int eventType;
    private final Set<Long> uidsRemoved = new LinkedHashSet<Long>();
    private final Set<Long> uidsAdded = new LinkedHashSet<Long>();
    private final Map<Long, ContactInfoEntity> contactsMap = new HashMap<Long, ContactInfoEntity>();

    private final Set<Long> gidsRemoved = new LinkedHashSet<Long>();
    private final Set<Long> gidsAdded = new LinkedHashSet<Long>();

    @Override
    protected void onMerge(String convID, List<Message.EventNotification> notifications, int eventKind, boolean isFullUpdate) {
        if (isFullUpdate) {
            //如果fullUpdate标志位true,直接跳过事件合并
            Logger.d(TAG, "isFullUpdate = true, return from event merge.");
            try {
                if (null != convID) {
                    //截取convID中的第二段gid作为eventType.
                    eventType = Integer.parseInt(convID.split("_")[1]);
                }
            } catch (NumberFormatException e) {
                Logger.ee(TAG, "error occurs when parse gid from conv_id");
            }
            return;
        }

        //这里首先将eventNotifications放到一个treeMap中按照ctime排序。
        TreeMap<Long, Message.EventNotification> tempMap = new TreeMap<Long, Message.EventNotification>();
        for (Message.EventNotification eventNotification : notifications) {
            tempMap.put(eventNotification.getCtimeMs(), eventNotification);
        }
        //将排完序的eventNotifications,重新遍历、合并toUidList。
        for (Message.EventNotification notification : tempMap.values()) {
            if (JCoreInterface.getUid() == notification.getFromUid()) {
                //这里fromUid拿到的是请求设备的juid. 按照后台文档，接收端SDK设备juid如果和req_juid相同，则不做任何处理，忽略此事件。
                Logger.w(TAG, "event from uid is myself , abort this event notification");
                continue;
            }
            Logger.d(TAG, "[processEventInBatch] id " + notification.getEventId() + " ctime = " + notification.getCtime() + " type = "
                    + notification.getEventType() + " extra = " + notification.getExtra() + " desc = " + notification.getDescription().toStringUtf8()
                    + " to uid list  = " + notification.getToUidlistList());
            List<Long> toUidList = notification.getToUidlistList();
            eventType = notification.getEventType();
            int eventExtra = notification.getExtra();
            switch (eventExtra) {
                case EXTRA_TARGET_ADDED:
                    //extra == 1，无法直接确定toUidList中包含的是uid还是gid,需要进一步根据eventType来判断。
                    modifyIdList(toCheckIsUidInside(eventType), true, toUidList);
                    break;
                case EXTRA_TARGET_REMOVED:
                    //extra == 2，无法直接确定toUidList中包含的是uid还是gid,需要进一步根据eventType来判断。
                    modifyIdList(toCheckIsUidInside(eventType), false, toUidList);
                    break;
                case EXTRA_CONTACT_ADDED:
                    modifyIdList(true, true, toUidList);
//                    //将description中信息加到map中去，description中包含了好友的备注信息
//                    //这里好友添加事件有可能包含多个uid(由dev api操作)，这里不能只取第一个
//                    for (Long uid : toUidList) {
//                        setContactInfoEntityToMap(uid, JsonUtil.fromJsonOnlyWithExpose(notification.getDescription().toStringUtf8(), ContactInfoEntity.class));
//                    }
                    break;
                case EXTRA_CONTACT_DELETED:
                    modifyIdList(true, false, toUidList);
                    //备注信息从map缓存中去掉
                    //这里好友添加事件有可能包含多个uid(由dev api操作)，这里不能只取第一个
                    for (Long uid : toUidList) {
                        contactsMap.remove(uid);
                    }
                    break;
                case EXTRA_CONTACT_INFO_UPDATED:
                    //根据后台定义，好友信息修改类型事件toUidList中只包含单个用户，这里直接拿toUidList中第一个元素
                    //并且将description中信息加到map中去，description中包含了好友的备注信息
                    modifyIdList(true, true, toUidList);
                    setContactInfoEntityToMap(toUidList.get(0), JsonUtil.fromJsonOnlyWithExpose(notification.getDescription().toStringUtf8(), ContactInfoEntity.class));
                    break;
                case EXTRA_NODISTURB_ADD_SINGLE:
                    modifyIdList(true, true, toUidList);
                    break;
                case EXTRA_NODISTURB_ADD_GROUP:
                    modifyIdList(false, true, toUidList);
                    break;
                case EXTRA_NODISTURB_ADD_GLOBAL:
                    IMConfigs.setNodisturbGlobal(1);
                    break;
                case EXTRA_NODISTURB_REMOVE_SINGLE:
                    modifyIdList(true, false, toUidList);
                    break;
                case EXTRA_NODISTURB_REMOVE_GROUP:
                    modifyIdList(false, false, toUidList);
                    break;
                case EXTRA_NODISTURB_REMOVE_GLOBAL:
                    IMConfigs.setNodisturbGlobal(0);
                    break;
            }
        }
        Logger.d(TAG, "final uids list after merge, kind = " + eventKind + "eventType = " + eventType + " add " + uidsAdded + " removed " + uidsRemoved + " gids add = " + gidsAdded + " gids removed = " + gidsRemoved +
                "\n fullUpdate = false");
    }

    private boolean toCheckIsUidInside(int eventType) {
        return EVENT_TYPE_GROUP_BLOCK != eventType;
    }

    private void modifyIdList(boolean isUid, boolean isAdd, List<Long> toUidList) {
        if (isUid && isAdd) {
            //是添加的动作，而且添加的对象是uid
            uidsRemoved.removeAll(toUidList);
            uidsAdded.addAll(toUidList);
        } else if (isUid) {
            //是删除的动作，删除的对象是uid
            uidsRemoved.addAll(toUidList);
            uidsAdded.removeAll(toUidList);
        } else if (isAdd) {
            //是添加的动作，添加的对象是gid
            gidsRemoved.removeAll(toUidList);
            gidsAdded.addAll(toUidList);
        } else {
            //是删除的动作，删除的对象是gid
            gidsRemoved.addAll(toUidList);
            gidsAdded.removeAll(toUidList);
        }
    }

    @Override
    protected void afterMerge(List<Message.EventNotification> notifications, int eventKind, final boolean isFullUpdate, final BasicCallback callback) {
        if (isFullUpdate) {
            Logger.d(TAG, "isFullUpdate = true, refresh list by type . type = " + eventType);
            refreshListByType();
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
        } else {
            List<Long> allUids = new ArrayList<Long>(uidsAdded);
            allUids.addAll(uidsRemoved);//将最终涉及到的用户uid统一一次性获取用户信息。

            List<Long> allGids = new ArrayList<Long>(gidsAdded);
            allGids.addAll(gidsRemoved);

            if (allUids.isEmpty() && allGids.isEmpty()) {
                //如果allUid和allGid都是空，可能事件都是由用户自己产生的事件从而被过滤掉了，此时直接触发回调
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
                return;
            }

            if (!allUids.isEmpty()) {
                //整体对于所有uid取一次userinfo，确保每个uid在本地都能拿到userinfo。
                UserIDHelper.getUserNames(allUids, new UserIDHelper.GetUsernamesCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<String> usernames) {
                        if (ErrorCode.NO_ERROR == code) {
                            updateLocalDataByType();
                        }
                        CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                    }
                });
            }

            if (!allGids.isEmpty()) {
                Map<Long, GetGroupInfoListTaskMng.GroupEntity> groupEntities = new LinkedHashMap<Long, GetGroupInfoListTaskMng.GroupEntity>();
                for (long gid : allGids) {
                    GetGroupInfoListTaskMng.GroupEntity entity = new GetGroupInfoListTaskMng.GroupEntity(gid, null);
                    groupEntities.put(gid, entity);
                }
                //整体对于所有gid取一次groupInfo，确保每个gid在本地都能拿到groupInfo。
                new GetGroupInfoListTask(IMConfigs.getUserID(), groupEntities, new GetGroupInfoListTask.GetGroupInfoInBatchCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage, Map<Long, GetGroupInfoListTaskMng.GroupEntity> entityMap) {
                        if (ErrorCode.NO_ERROR == responseCode) {
                            updateLocalDataByType();
                        }
                        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
                    }
                }, false).execute();
            }
        }
        //事件id加入到去重列表,这里的gid填0，表示这里的事件都将进入到通用事件去重容器中去。
        EventIdListManager.getInstance().insertToEventIdList(0L, notifications);
    }

    private void refreshListByType() {
        switch (eventType) {
            case EVENT_TYPE_CONTACT:
                new GetFriendListTask(null, false).execute();
                break;
            case EVENT_TYPE_BLACKLIST:
                new GetBlackListTask(null, false).execute();
                break;
            case EVENT_TYPE_NODISTURB:
                new GetNoDisturbListTask(false).execute();
                break;
            case EVENT_TYPE_GROUP_BLOCK:
                new GetBlockedGroupsTask(null, false).execute();
                break;
        }
    }

    private void updateLocalDataByType() {
        ContentValues values = new ContentValues();
        switch (eventType) {
            case EVENT_TYPE_CONTACT:
                UserInfoManager.getInstance().addAllUidsFriendRelatedInfo(contactsMap);
                UserInfoManager.getInstance().removeAllUidsFriendRelatedInfo(uidsRemoved);
                break;
            case EVENT_TYPE_BLACKLIST:
                UserInfoManager.getInstance().updateAllUidsBlacklistFlag(uidsAdded, true);
                UserInfoManager.getInstance().updateAllUidsBlacklistFlag(uidsRemoved, false);
                break;
            case EVENT_TYPE_NODISTURB:
                UserInfoManager.getInstance().updateAllUidsNoDisturbFlag(uidsAdded, true);
                UserInfoManager.getInstance().updateAllUidsNoDisturbFlag(uidsRemoved, false);
                values.put(GroupStorage.GROUP_NODISTURB, 1);
                GroupStorage.updateAllGidsWithValue(gidsAdded, values);
                values.put(GroupStorage.GROUP_NODISTURB, 0);
                GroupStorage.updateAllGidsWithValue(gidsRemoved, values);
                break;
            case EVENT_TYPE_GROUP_BLOCK:
                values.put(GroupStorage.GROUP_BLOCKED, 1);
                GroupStorage.updateAllGidsWithValue(gidsAdded, values);
                values.put(GroupStorage.GROUP_BLOCKED, 0);
                GroupStorage.updateAllGidsWithValue(gidsRemoved, values);
                break;
        }
    }

    private void setContactInfoEntityToMap(long uid, ContactInfoEntity entity) {
        ContactInfoEntity cachedEntity = contactsMap.get(uid);
        if (null != cachedEntity) {
            if (null != entity.memo_name) {
                cachedEntity.memo_name = entity.memo_name;
            }

            if (null != entity.memo_others) {
                cachedEntity.memo_others = entity.memo_others;
            }
        } else {
            contactsMap.put(uid, entity);
        }
    }

    /**
     * 判断下发的事件的eventType是否属于event kind7类型
     *
     * @param eventType 事件的type
     */
    static boolean isEventTypeBelongsToKind7(int eventType) {
        return eventType >= EVENT_TYPE_CONTACT && eventType <= EVENT_TYPE_GROUP_BLOCK;
    }

    public class ContactInfoEntity {
        @Expose
        public String memo_name = null;
        @Expose
        public String memo_others = null;
    }

}
