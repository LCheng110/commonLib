package cn.jpush.im.android.helpers.eventsync;

import android.content.ContentValues;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalEventNotificationContent;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMCommands;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.EventIdListManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.tasks.GetGroupInfoTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/7/31.
 */

public class GroupEventsWrapper extends EventNotificationsWrapper {
    private static final String TAG = GroupEventsWrapper.class.getSimpleName();
    /* 事件分类，具体定义见后台文档http://wiki.jpushoa.com/pages/viewpage.action?pageId=12683930 */
    public static final int EVENT_KIND_3 = 3;//群组信息、成员变化事件

    private final Set<Long> uidsRemoved = new LinkedHashSet<Long>();
    private final Set<Long> uidsAdded = new LinkedHashSet<Long>();
    private long newOwner = 0L;
    private boolean refreshGroupInfo = false;

    @Override
    protected void onMerge(String convID, List<Message.EventNotification> notifications, int eventKind, boolean isFullUpdate) {
        long myUid = IMConfigs.getUserID();

        //这里首先将eventNotifications放到一个treeMap中按照ctime排序。
        TreeMap<Long, Message.EventNotification> tempMap = new TreeMap<Long, Message.EventNotification>();
        for (Message.EventNotification eventNotification : notifications) {
            tempMap.put(eventNotification.getCtimeMs(), eventNotification);
        }
        //将排完序的eventNotifications,重新遍历、合并群成员。
        for (Message.EventNotification notification : tempMap.values()) {
            Logger.d(TAG, "[processEventInBatch] id " + notification.getEventId() + " ctime = " + notification.getCtime() + " type = "
                    + notification.getEventType() + " to uid list  = " + notification.getToUidlistList());
            List<Long> toUidList = notification.getToUidlistList();
            switch (notification.getEventType()) {
                case IMCommands.AddGroupMember.CMD:
                    uidsRemoved.removeAll(toUidList);
                    uidsAdded.addAll(toUidList);
                    break;
                case IMCommands.DelGroupMember.CMD:
                    uidsRemoved.addAll(toUidList);
                    uidsAdded.removeAll(toUidList);
                    newOwner = StringUtils.getNewOwnerFromDescription(notification.getDescription().toStringUtf8());
                    break;
                case IMCommands.ExitGroup.CMD:
                    uidsRemoved.addAll(toUidList);
                    uidsAdded.removeAll(toUidList);
                    newOwner = StringUtils.getNewOwnerFromDescription(notification.getDescription().toStringUtf8());
                    break;
                case IMCommands.UpdateGroupInfo.CMD:
                    refreshGroupInfo = true;//收到群信息更新的事件，需要在之后触发一次获取群信息的动作来更新本地的群信息
                    break;
            }
        }
        //如果当前用户被加入到群组中，也需要把isFullUpdate置为true，重新获取一遍群成员。
        if (uidsAdded.contains(myUid)) {
            isFullUpdate = true;
        }
        Logger.d(TAG, "final uids list after merge, add " + uidsAdded + " removed " + uidsRemoved + " fullUpdate = " + isFullUpdate);
    }

    @Override
    protected void afterMerge(final List<Message.EventNotification> notifications, int eventKind, final boolean isFullUpdate, final BasicCallback callback) {
        List<Long> allUids = new ArrayList<Long>(uidsAdded);
        allUids.addAll(uidsRemoved);//将最终涉及到的用户uid统一一次性获取用户信息。
        //这里需要批量请求所有事件中涉及到的用户的用户信息，因为上抛事件时需要用户的username或者nickname,仅仅只有uid是不够的。
        final long gid = notifications.get(0).getGid();
        if (!allUids.isEmpty()) {
            UserIDHelper.getUserNames(allUids, new UserIDHelper.GetUsernamesCallback() {
                @Override
                public void gotResult(int code, String msg, List<String> usernames) {
                    //如果获取username失败了，直接回调callback并返回
                    if (ErrorCode.NO_ERROR != code) {
                        CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                    } else {
                        updateLocalValueThenPostEvent(gid, notifications, isFullUpdate, callback);
                    }
                }
            });
        } else {
            updateLocalValueThenPostEvent(gid, notifications, isFullUpdate, callback);
        }
    }

    private void updateLocalValueThenPostEvent(final long gid, final List<Message.EventNotification> notifications, final boolean isFullUpdate, final BasicCallback callback) {
        final long finalNewOwner = newOwner;
        if (isFullUpdate) {
            // TODO: 2017/6/26 这里需要考虑使用批量获取群成员的接口，否则多个群的场景下，发起多个获取群成员的请求会造成效率低下
            new GetGroupInfoTask(gid, new GetGroupInfoCallback(false) {
                @Override
                public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                    if (ErrorCode.NO_ERROR == responseCode) {
                        //事件id加入到去重列表
                        EventIdListManager.getInstance().insertToEventIdList(gid, notifications);
                        //批量上抛事件到上层
                        sendEventNotificationInBatch(gid, notifications);
                        //触发回调通知调用方
                    }
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
                }
            }, true, false, false).execute();//这里needRefreshMembers应该是true,必须刷新群成员信息。downloadAvatar = false。批量处理群事件时不获取群的头像。
        } else {
            if (refreshGroupInfo) {
                //如果需要刷新群信息，需要起一个获取群信息的任务
                new GetGroupInfoTask(gid, null, false, false).execute();
            }
            //更新群成员到数据库
            Set<Long> membersSet = new LinkedHashSet<Long>();//使用一个中间变量set来做群成员合并。
            List<Long> membersInList = GroupStorage.queryMemberUserIdsSync(gid);
            if (null != membersInList) {
                membersSet.addAll(membersInList);
                membersSet.addAll(uidsAdded);
                membersSet.removeAll(uidsRemoved);
                membersInList.clear();
                membersInList.addAll(membersSet);
                ContentValues values = new ContentValues();
                if (0 != finalNewOwner) {
                    //如果newOwner不为0，说明群主发生了变化，此时需要更新群主信息
                    values.put(GroupStorage.GROUP_OWNER_ID, finalNewOwner);

                }
                values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(membersInList));
                if (GroupStorage.updateValuesSync(gid, values)) {
                    //本地数据更新成功，通知缓存中会话对象重新加载group members.
                    ConversationManager.getInstance().updateGroupMemberNamesAndReload(gid, membersInList);
                }
            }
            //事件id加入到去重列表
            EventIdListManager.getInstance().insertToEventIdList(gid, notifications);
            //批量上抛事件到上层
            sendEventNotificationInBatch(gid, notifications);
            //触发回调通知调用方
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
        }
    }

    private void sendEventNotificationInBatch(final long gid, List<Message.EventNotification> eventNotifications) {
        boolean isConvAlreadyExist = true;
        InternalConversation conv = ConversationManager.getInstance().getGroupConversation(gid);
        int isGroupBlocked = GroupStorage.queryIntValueSync(gid, GroupStorage.GROUP_BLOCKED);
        //当本地没有会话对象,而这个群不是被屏蔽的状态时，需要主动创建会话上抛事件消息
        if (conv == null && isGroupBlocked == 0) {
            isConvAlreadyExist = false;
            conv = ConversationManager.getInstance().createConversation(ConversationType.group, String.valueOf(gid), "", null, 0L, false);
        }

        //只有当会话存在，而且群没有被屏蔽的前提下，才会上抛事件
        if (null != conv && isGroupBlocked == 0) {
            List<cn.jpush.im.android.api.model.Message> eventNotificationMsgs = new ArrayList<cn.jpush.im.android.api.model.Message>();
            List<Long> groupMembers = GroupStorage.queryMemberUserIdsSync(gid);
            //遍历整个事件列表，针对每个事件，将事件中的toUidList转成usernameList，然后创建相应的eventNotification类型的message，然后上抛给应用层
            for (Message.EventNotification eventNotification : eventNotifications) {
                List<Long> toUidListCopy = new ArrayList<Long>(eventNotification.getToUidlistList());
                //在之前的处理中，已经请求过事件中所有涉及到的用户的用户信息了，这里只需要从本地去查username。
                //如果toUidList中存在在本地找不到对应username的，说明这个uid对应用户已经被删除了，此时需要将这个uid从toUidList中去除。
                List<String> usernames = new ArrayList<String>();
                for (Long uid : toUidListCopy) {//检查toUidList中每一个uid是否能在本地拿到对应的username.
                    if (0 == uid) {
                        //如果uid是0，说明是api操作，跳过username检查
                        continue;
                    }
                    if (null == UserIDHelper.getUserNameFromLocal(uid)) {
                        //从本地拿不到对应的username，说明这个用户不存在，直接从toUidList中去除
                        toUidListCopy.remove(uid);
                    } else {
                        usernames.add(UserIDHelper.getUserNameFromLocal(uid));
                    }
                }

                if (IMCommands.UpdateGroupInfo.CMD != eventNotification.getEventType() && toUidListCopy.isEmpty()) {
                    //过滤掉toUidList中不存在的uid之后，如果list为空，则不走之后创建通知消息的逻辑，直接调过这条事件的处理
                    Logger.d(TAG, "to uid list is empty , skip this event notification, type = " + eventNotification.getEventType());
                    continue;
                }

                EventNotificationContent content;
                switch (eventNotification.getEventType()) {
                    case IMCommands.AddGroupMember.CMD:
                        content = InternalEventNotificationContent.createEventNotificationContent(gid,
                                eventNotification.getFromUid(), EventNotificationContent.EventNotificationType.group_member_added,
                                toUidListCopy, usernames, groupMembers);
                        break;
                    case IMCommands.DelGroupMember.CMD:
                        content = InternalEventNotificationContent.createEventNotificationContent(gid,
                                eventNotification.getFromUid(), EventNotificationContent.EventNotificationType.group_member_removed,
                                toUidListCopy, usernames, groupMembers);
                        break;
                    case IMCommands.ExitGroup.CMD:
                        content = InternalEventNotificationContent.createEventNotificationContent(gid,
                                eventNotification.getFromUid(), EventNotificationContent.EventNotificationType.group_member_exit,
                                toUidListCopy, usernames, groupMembers);
                        break;
                    case IMCommands.UpdateGroupInfo.CMD:
                        content = new InternalEventNotificationContent(gid, eventNotification.getFromUid(),
                                EventNotificationContent.EventNotificationType.group_info_updated, null, null, false, null);
                        break;
                    default:
                        content = null;
                }

                if (null != content) {
                    cn.jpush.im.android.api.model.Message eventMsg = conv.createReceiveMessage(
                            content, "", 0, "系统消息", "", eventNotification.getCtimeMs(), eventNotification.getEventId(), "", null, true);
                    eventNotificationMsgs.add(eventMsg);
                }
            }
            if (!eventNotificationMsgs.isEmpty()) {
                //将一个群内所有的群事件一次性上抛给上层。
                EventBus.getDefault().post(new OfflineMessageEvent(conv, eventNotificationMsgs));
            } else if (!isConvAlreadyExist) {
                //如果最终需要上抛的eventNotificationMsgs是空，而且这个会话在这次事件处理前是不存在的（由于事件处理而被创建），此时需要将这个会话删掉，防止上层出现空会话的情况。
                JMessageClient.deleteGroupConversation(gid);
            }
        }
    }
}
