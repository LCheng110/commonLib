package cn.jpush.im.android.helpers.eventsync;

import android.content.ContentValues;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.ContactManager;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.event.ContactNotifyEvent;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.api.event.MessageRetractEvent;
import cn.jpush.im.android.api.event.MyInfoUpdatedEvent;
import cn.jpush.im.android.api.event.UserDeletedEvent;
import cn.jpush.im.android.api.event.UserLogoutEvent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.helpers.MessageSendingMaintainer;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalEventNotificationContent;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMCommands;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.EventIdListManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetGroupInfoTask;
import cn.jpush.im.android.tasks.GetUserInfoTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by hxhg on 2017/7/31.
 */

public class GeneralEventsWrapper extends EventNotificationsWrapper {
    private static final String TAG = GeneralEventsWrapper.class.getSimpleName();

    public static final int EVENT_LOGOUT = 1;

    private static final int EVENT_PASSWORD_CHANGE = 2;

    private static final int EVENT_IVITATION_RESP = 5;

    private static final int EVENT_CONTACT_DELETED = 6;

    private static final int EVENT_CONTACT_UPDATED_BY_DEVAPI = 7;

    private static final int EVENT_NODISTURB_SYNC = 37;

    private static final int EVENT_BLOCK_GROUP_SYNC = 39;

    private static final int EVENT_BLACKLIST_SYNC = 38;

    private static final int EVENT_USERINFO_UPDATED = 40;

    private static final int EVENT_MESSAGE_RETRACT = 55;

    public static final int EVENT_FORBIDDEN_USER = 1000;

    @Override
    protected void onMerge(String convID, List<Message.EventNotification> notifications, int eventKind, boolean isFullUpdate) {
        //收到此类事件不需要做事件合并处理，此方法留空。
    }

    @Override
    protected void afterMerge(List<Message.EventNotification> notifications, int eventKind, boolean isFullUpdate, BasicCallback callback) {
        //单独处理每一个事件。
        for (Message.EventNotification eventNotification : notifications) {
            processEvent(eventNotification);
        }
        //触发回调通知调用方
        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
    }

    private void processEvent(Message.EventNotification eventNotification) {
        if (!CommonUtils.isLogin("OnReceiveBroadcast")) {
            Logger.ww(TAG, "received im response but user not login yet,discard this message");
            return;
        }
        Logger.ii(TAG, " eventID = " + eventNotification.getEventId() + " event type = " + eventNotification.getEventType());
        if (EventIdListManager.getInstance().containsEventID(0L, eventNotification.getEventId())) {
            Logger.dd(TAG,
                    "received a duplicate eventID from server , abort it!eventID = " + eventNotification
                            .getEventId());
            return;
        }

        long gid = 0;
        switch (eventNotification.getEventType()) {
            case EVENT_LOGOUT:
                handleLogoutEvent(eventNotification);
                break;
            case EVENT_PASSWORD_CHANGE:
                handlePasswordChangeEvent();
                break;
            case IMCommands.CreateGroup.CMD:
            case IMCommands.AddGroupMember.CMD:
            case IMCommands.DelGroupMember.CMD:
            case IMCommands.ExitGroup.CMD:
            case IMCommands.UpdateGroupInfo.CMD:
                gid = eventNotification.getGid();
                handleGroupEvent(eventNotification);
                break;
            case EVENT_BLACKLIST_SYNC:
                JMessageClient.getBlacklist(null);
                break;
            case EVENT_NODISTURB_SYNC:
                JMessageClient.getNoDisturblist(null);
                break;
            case EVENT_BLOCK_GROUP_SYNC:
                JMessageClient.getBlockedGroupsList(null);
                break;
            case EVENT_CONTACT_UPDATED_BY_DEVAPI:
                ContactManager.getFriendList(null);
                break;
            case EVENT_IVITATION_RESP:
            case EVENT_CONTACT_DELETED:
                Logger.d(TAG, "invitation resp---- from uid = " + eventNotification.getFromUid() +
                        " gid = " + eventNotification.getGid() + " to uid list = " + eventNotification.getToUidlistList() +
                        " extra = " + eventNotification.getExtra() + " return code = " + eventNotification.getReturnCode());
                handleContactResp(eventNotification);
                break;
            case EVENT_USERINFO_UPDATED:
                //收到用户信息变更事件，需要更新当前登陆用户的用户信息。
                JMessageClient.getUserInfo(IMConfigs.getUserName(), new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                        if (0 == responseCode && null != info) {
                            EventBus.getDefault().post(new MyInfoUpdatedEvent(info));
                        }
                    }
                });
                break;
            case EVENT_MESSAGE_RETRACT:
                processMessageRetract(eventNotification);
                break;
            default:
                Logger.ww(TAG, "received an unsupported event . event type = " + eventNotification.getEventType());
                break;
        }

        //收到事件时，需要将event id插入到去重列表中去。
        EventIdListManager.getInstance().insertToEventIdList(gid, eventNotification.getEventId(), eventNotification.getCtime());
    }

    private static void handleLogoutEvent(Message.EventNotification notification) {
        UserInfo myinfo = JMessageClient.getMyInfo();
        if (0 != notification.getFromUid()) {
            Logger.dd(TAG, "do logout when received logout event");
            EventBus.getDefault().post(new UserLogoutEvent(myinfo));
            EventBus.getDefault().post(new LoginStateChangeEvent(myinfo, LoginStateChangeEvent.Reason.user_logout));
            JMessageClient.logout();
        } else {
            Logger.dd(TAG, "do clean when received user deleted event");
            EventBus.getDefault().post(new UserDeletedEvent(myinfo));
            EventBus.getDefault().post(new LoginStateChangeEvent(myinfo, LoginStateChangeEvent.Reason.user_deleted));

            //将所有正在发送的消息状态置为send_fail.
            MessageSendingMaintainer.resetAllSendingMessageStatus();
            //用户被删除，直接清数据而不Logout.
            RequestProcessor.clearCachedInfos();
        }
    }

    private static void handlePasswordChangeEvent() {
        UserInfo myInfo = JMessageClient.getMyInfo();
        Logger.ii(TAG, "do logout when received password change event");
        EventBus.getDefault().post(new LoginStateChangeEvent(myInfo, LoginStateChangeEvent.Reason.user_password_change));
        JMessageClient.logout();
    }

    private static void handleContactResp(final Message.EventNotification notification) {
        UserIDHelper.getUsername(notification.getFromUid(), new UserIDHelper.GetUsernamesCallback() {
            @Override
            public void gotResult(int code, String msg, List<String> usernames) {
                if (6 == notification.getEventType()) {
                    //如果收到的是被好友删除的请求，需要更新数据库中isFriend字段
                    Logger.d(TAG, "received a contact delete event, update local info");
                    UserInfoManager.getInstance().updateIsFriendFlag(notification.getFromUid(), false);
                    UserInfoManager.getInstance().updateNoteText(notification.getFromUid(), "");
                    UserInfoManager.getInstance().updateNoteName(notification.getFromUid(), "");
                }

                if (2 == notification.getExtra() && 0 == notification.getReturnCode()) {
                    Logger.d(TAG, "received a contact accepted event, update local info");
                    UserInfoManager.getInstance().updateIsFriendFlag(notification.getFromUid(), true);
                }

                String appkey = UserIDHelper.getUserAppkeyFromLocal(notification.getFromUid());
                final ContactNotifyEvent.Builder builder = new ContactNotifyEvent.Builder();
                builder.setcTime(notification.getCtime()).setDesc(notification.getDescription().toStringUtf8())
                        .setEventId(notification.getEventId()).setEventType(notification.getEventType())
                        .setExtra(notification.getExtra()).setGid(notification.getGid())
                        .setReturnCode(notification.getReturnCode());
                if (null != usernames && !usernames.isEmpty() && !TextUtils.isEmpty(usernames.get(0))) {
                    builder.setFromUsername(usernames.get(0));
                }
                if (TextUtils.isEmpty(appkey)) {
                    appkey = JCoreInterface.getAppKey();
                }
                builder.setfromUserAppKey(appkey);
                ContactNotifyEvent event = builder.build();
                EventBus.getDefault().post(event);
            }
        });
    }

    private static void processMessageRetract(final Message.EventNotification notification) {
        long fromUid = notification.getFromUid();
        long convTargetUid = fromUid;//会话对象的uid,默认会话对象的uid就是fromUid,这一点在大多数情况下没问题。
        if (fromUid == IMConfigs.getUserID()) {
            //根据多端在线后台的行为：所有自己操作的撤回动作，同时自己也会收到一条fromUid为自己的事件。
            //所以当这里的fromUid为自己时，需要从toUidList中获取第一个uid,来确定会话target的uid,否则
            List<Long> toUidList = notification.getToUidlistList();
            if (null != toUidList && !toUidList.isEmpty()) {
                convTargetUid = toUidList.get(0);//对于收到的fromUid为自己的撤回事件，需要从toUidList中拿第一个uid当做convTargetUid
            }
        }
        final InternalUserInfo fromUserInfo = UserInfoManager.getInstance().getUserInfo(fromUid);
        InternalUserInfo convTargetInfo = UserInfoManager.getInstance().getUserInfo(convTargetUid);
        if (null == convTargetInfo) {
            Logger.dd(TAG, "fromUserInfo is null,retrieve it from server.");
            new GetUserInfoTask(convTargetUid, new GetUserInfoCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                    if (0 == responseCode) {
                        updateContentAndPostEvent(notification, fromUserInfo, (InternalUserInfo) info);
                    }
                }
            }, false, false).execute();
        } else {
            updateContentAndPostEvent(notification, fromUserInfo, convTargetInfo);
        }
    }

    private static void updateContentAndPostEvent(Message.EventNotification notification, InternalUserInfo fromUser, InternalUserInfo convTargetInfo) {
        long fromGid = notification.getFromGid();
        long serverMsgID = notification.getMsgidList(0);
        InternalConversation conversation;
        if (fromGid == 0) { //fromGid == 0 表示是单聊消息撤回。
            //单聊撤回
            conversation = ConversationManager.getInstance().getSingleConversation(convTargetInfo.getUserName(), convTargetInfo.getAppKey());
        } else {//其他情况下,fromGid表示群的groupId
            //群聊撤回
            conversation = ConversationManager.getInstance().getGroupConversation(fromGid);
        }

        if (null == conversation) {
            Logger.ww(TAG, "Message retract when conversation is null");
            return;
        }

        InternalMessage message = (InternalMessage) conversation.getMessage(serverMsgID);
        if (message != null) {
            //将被撤回的消息内容更新为PromptContent
            String promptText;
            if (IMConfigs.getUserID() != notification.getFromUid()) {
                promptText = fromUser.getDisplayName(true) + "撤回了一条消息";
            } else {
                //发起消息撤回的是我自己
                promptText = "你撤回了一条消息";
            }
            PromptContent content = new PromptContent(promptText);
            Logger.d(TAG, "update msg content to prompt content, text is " + content.getPromptText());
            conversation.updateMessageContent(message, content);
            //发送撤回事件给上层。
            EventBus.getDefault().post(new MessageRetractEvent(conversation, message));
            if (serverMsgID == conversation.getLatestMessage().getServerMessageId()) {
                //如果撤回的消息是会话中的最后一条消息，需要更新通知栏上的通知内容
                ChatMsgManager.getInstance().updateNotification(message);
                //还要更新会话中的latestMsg相关字段
                ConversationManager.getInstance().updateLatestMsg(conversation.getType(), conversation.getTargetId(), conversation.getTargetAppKey(), message);
            }
        } else {
            Logger.d(TAG, "On Message Retract Event Received.This is an offline message");
        }
        //收到撤回事件时，也把这个msgid加到会话的去重列表里去，防止出现事件先下发，而被撤回的消息后下发导致的本地消息撤回失败的问题。
        conversation.insertToOnlineMsgTable(serverMsgID, notification.getCtime());
    }

    private void handleGroupEvent(final Message.EventNotification notification) {
        //事件同步的版本，后台需要将用户在其他通道上发起的操作再推给用户一遍，这时不应该将自己发起的事件过滤。
//        if (IMConfigs.getUserID() == notification.getFromUid()) {
//            Logger.d(TAG, "received a group member change event from self, abort it! event type = " + notification.getEventType());
//            return;
//        }
        //群成员变化的event由于要直接给到上层，所以其中被操作的username list是必不可少的，
        //所以每次收到群成员变化事件时需要先从uid list转到username list
        UserIDHelper.getUserNames(notification.getToUidlistList(), new UserIDHelper.GetUsernamesCallback() {
            @Override
            public void gotResult(int code, String msg, List<String> usernames) {
                Logger.d(TAG,
                        "on event received !uid = " + notification.getToUidlistList() + " userNames = " + usernames + " type = " + notification
                                .getEventType());
                if (0 != code || usernames == null) {
                    Logger.ww(TAG, "get usernames failed when received a event! ");
                    return;
                }
                switch (notification.getEventType()) {
                    case IMCommands.AddGroupMember.CMD:
                        processAddMembersMessage(notification.getFromUid(), notification.getGid()
                                , notification.getEventId(), notification.getToUidlistList(), usernames, notification.getCtimeMs());
                        break;
                    case IMCommands.DelGroupMember.CMD:
                        processDeleteMembersMessage(notification.getFromUid(), notification.getGid()
                                , notification.getEventId(), notification.getToUidlistList(), usernames, notification.getCtimeMs(), notification.getDescription().toStringUtf8());
                        break;
                    case IMCommands.ExitGroup.CMD:
                        processExitGroupMessage(notification.getFromUid(), notification.getGid()
                                , notification.getEventId(), notification.getToUidlistList(), usernames, notification.getCtimeMs(), notification.getDescription().toStringUtf8());
                        break;
                    case IMCommands.UpdateGroupInfo.CMD:
                        handleGroupInfoUpdateEvent(notification);
                        break;
                }
            }
        });
    }

    private synchronized void processExitGroupMessage(long operator, final long groupID, long eventId,
                                                      List<Long> userIds, List<String> username, long cTimeInMills, String description) {


        final List<Long> memberUids = GroupStorage.queryMemberUserIdsSync(groupID);

        //更新db和缓存。同时判断是否需要更新群主信息。
        if (null != memberUids) {
            memberUids.removeAll(userIds);
            ContentValues values = new ContentValues();
            values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(memberUids));
            onSomeoneExitGroup(values, description);//有人退群，此时需要判断下是否需要更新群主信息。
            if (GroupStorage.updateValuesSync(groupID, values)) {
                //更新conversation缓存中群成员
                ConversationManager.getInstance().removeGroupMemberFromCache(groupID, userIds);
            } else {
                Logger.ww(TAG, "[processExitGroupMessage] usernames not in group member list,or update fail.");
            }
        }

        InternalConversation conv = ConversationManager.getInstance().getGroupConversation(groupID);
        int isGroupBlocked = GroupStorage.queryIntValueSync(groupID, GroupStorage.GROUP_BLOCKED);
        //当涉及到群成员变化,而且这个群不是被屏蔽的状态时，需要主动创建会话上抛事件消息
        if (null == conv && isGroupBlocked == 0) {
            conv = ConversationManager.getInstance().createGroupConversation(groupID);
        }

        //只有当会话存在，而且群没有被屏蔽的前提下，才会上抛事件
        if (null != conv && isGroupBlocked == 0) {
            EventNotificationContent content = InternalEventNotificationContent.createEventNotificationContent(groupID, operator, EventNotificationContent.EventNotificationType.group_member_exit, userIds, username, null);
            if (null != content) {
                cn.jpush.im.android.api.model.Message eventMsg = conv.createReceiveMessage(content, "", 0, "系统消息", "", cTimeInMills, eventId, "", null, true);
                ChatMsgManager.getInstance().sendOnlineMsgEvent(eventMsg);
            }
        } else {
            Logger.dd(TAG, "[processExitGroupMessage] do not need to send event message!");
        }
    }

    private synchronized void processDeleteMembersMessage(long operator, long groupID, long eventId,
                                                          List<Long> userIds, List<String> usernames, long cTimeInMills, String description) {
        List<Long> membersUids = GroupStorage.queryMemberUserIdsSync(groupID);
        //更新db和缓存。
        if (null != membersUids) {
            membersUids.removeAll(userIds);
            ContentValues values = new ContentValues();
            values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(membersUids));
            onSomeoneExitGroup(values, description);//有人退群，此时需要判断下是否需要更新群主信息。
            if (GroupStorage.updateValuesSync(groupID, values)) {
                //更新conversation缓存中群成员
                ConversationManager.getInstance().removeGroupMemberFromCache(groupID, userIds);
            }
        }


        UserInfo myInfo = JMessageClient.getMyInfo();
        boolean containsMyself = myInfo != null && usernames.contains(myInfo.getUserName());
        int isGroupBlocked = GroupStorage.queryIntValueSync(groupID, GroupStorage.GROUP_BLOCKED);
        if (containsMyself) { //如果是用户自己被踢出群组
            //还原自己在这个群的屏蔽状态和免打扰状态(此操作要在主动退群和自己被踢出群两处都进行处理)
            ContentValues contentValues = new ContentValues();
            contentValues.put(GroupStorage.GROUP_BLOCKED, 0);
            contentValues.put(GroupStorage.GROUP_NODISTURB, 0);
            GroupStorage.updateValuesInBackground(groupID, contentValues);
        }

        InternalConversation conv = ConversationManager.getInstance().getGroupConversation(groupID);
        //如果本地不存在会话，这时需要判断此群是否被屏蔽，如果没有被屏蔽或者被踢出的人中包含了我自己，本地都需要创建会话，然后上抛事件。
        if (null == conv && (isGroupBlocked == 0 || containsMyself)) {
            conv = ConversationManager.getInstance().createGroupConversation(groupID);
        }

        //只有当会话存在，而且群没有被屏蔽，或者被踢的人中包含了我自己的前提下，才会上抛事件
        if (null != conv && (isGroupBlocked == 0 || containsMyself)) {
            EventNotificationContent content = InternalEventNotificationContent.createEventNotificationContent(groupID, operator, EventNotificationContent.EventNotificationType.group_member_removed, userIds, usernames, null);
            if (null != content) {
                cn.jpush.im.android.api.model.Message eventMsg = conv.createReceiveMessage(content, "", 0, "系统消息", "", cTimeInMills, eventId, "", null, true);
                ChatMsgManager.getInstance().sendOnlineMsgEvent(eventMsg);
            }
        } else {
            Logger.dd(TAG, "[processDeleteMembersMessage] do not need to send event message!");
        }
    }

    private void onSomeoneExitGroup(ContentValues values, String description) {
        long newOwner = StringUtils.getNewOwnerFromDescription(description);
        if (0 != newOwner) {
            values.put(GroupStorage.GROUP_OWNER_ID, newOwner);
        }
    }

    private synchronized void processAddMembersMessage(final long operator, final long groupID, final long eventId, final List<Long> userIds, final List<String> usernames, final long cTimeInMills) {
        long userId = IMConfigs.getUserID();
        if (0 != userId && userIds.contains(userId)) {//当前用户被加入群组，需要刷新整个群信息
            Logger.d(TAG, "refresh group members ! when user added in group");
            new GetGroupInfoTask(groupID, new GetGroupInfoCallback(false) {
                @Override
                public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                    List<Long> membersUids;
                    if (0 == responseCode) {
                        //如果成功获取到成员列表，则将成员回给上层显示
                        InternalGroupInfo internalGroupInfo = (InternalGroupInfo) groupInfo;
                        membersUids = new ArrayList<Long>(internalGroupInfo.getGroupMemberUserIds());
                        sendAddMemberEvent(operator, groupID, eventId, cTimeInMills, userIds, usernames, membersUids, true);
                    } else {
                        //如果获取失败，则从数据库中获取成员回给上层
                        membersUids = GroupStorage.queryMemberUserIdsSync(groupID);
                        if (null != membersUids) {
                            membersUids.addAll(userIds);
                        } else {
                            membersUids = new ArrayList<Long>();//用一个空list占位。
                        }
                        sendAddMemberEvent(operator, groupID, eventId, cTimeInMills, userIds, usernames, membersUids, true);
                    }
                }
            }, true, false).execute();//这里needRefreshMembers应该是true,必须刷新群成员信息。
            return;
        }
        List<Long> members = GroupStorage.queryMemberUserIdsSync(groupID);
        if (null != members) {
            members.addAll(userIds);
        }
        sendAddMemberEvent(operator, groupID, eventId, cTimeInMills, userIds, usernames, members, false);
    }

    private void sendAddMemberEvent(long operator, long groupID, long eventId, long cTimeInMills, List<Long> addedMemberIds, List<String> addedMemberUsernames, List<Long> groupMemberIds, boolean needReloadMembers) {
        if (null != groupMemberIds) {
            ContentValues values = new ContentValues();
            values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(groupMemberIds));
            if (GroupStorage.updateValuesSync(groupID, values)) {
                if (needReloadMembers) {
                    //重新加载members
                    ConversationManager.getInstance().updateGroupMemberNamesAndReload(groupID, groupMemberIds);
                } else {
                    //仅向conversation缓存中增加群成员
                    ConversationManager.getInstance().addGroupMemberInCacheWithIds(groupID, addedMemberIds);
                }
            } else {
                Logger.ww(TAG, "[processAddMembersMessage] update local group member failed !");
//                EventStorage.insertOrUpdateWhenExistsInBackground(groupID, cTime, eventNotificationContent);
            }
        }

        //就算本地群成员更新失败了，还是需要尝试创建会话上抛事件。见jira:http://jira.jpushoa.com/browse/IM-2397
        InternalEventNotificationContent eventNotificationContent = InternalEventNotificationContent.createEventNotificationContent(groupID, operator,
                EventNotificationContent.EventNotificationType.group_member_added,
                addedMemberIds, addedMemberUsernames, groupMemberIds);
        InternalConversation conv = ConversationManager.getInstance().getGroupConversation(groupID);
        int isGroupBlocked = GroupStorage.queryIntValueSync(groupID, GroupStorage.GROUP_BLOCKED);
        //当本地没有会话对象,而这个群不是被屏蔽的状态时，需要主动创建会话上抛事件消息
        if (conv == null && isGroupBlocked == 0 && null != eventNotificationContent) {
            conv = ConversationManager.getInstance().createGroupConversation(groupID);
        }

        //只有当会话存在，而且群没有被屏蔽的前提下，才会上抛事件
        if (null != conv && isGroupBlocked == 0 && null != eventNotificationContent) {
            cn.jpush.im.android.api.model.Message eventMsg = conv.createReceiveMessage(
                    eventNotificationContent, "", 0, "系统消息", "", cTimeInMills, eventId, "", null, true);
            ChatMsgManager.getInstance().sendOnlineMsgEvent(eventMsg);
        }
    }

    private void handleGroupInfoUpdateEvent(Message.EventNotification eventNotification) {
        long gid = eventNotification.getGid();
        new GetGroupInfoTask(gid, null, false, false).execute();//收到群信息更新事件，首先触发一次获取群信息

        //针对会话以及群组的实际情况，决定是否需要将事件上抛。
        EventNotificationContent content = new InternalEventNotificationContent(gid, eventNotification.getFromUid(), EventNotificationContent.EventNotificationType.group_info_updated,
                null, null, false, null);
        InternalConversation conv = ConversationManager.getInstance().getGroupConversation(gid);
        int isGroupBlocked = GroupStorage.queryIntValueSync(gid, GroupStorage.GROUP_BLOCKED);
        //当本地没有会话对象,而这个群不是被屏蔽的状态时，需要主动创建会话上抛事件消息
        if (conv == null && isGroupBlocked == 0) {
            conv = ConversationManager.getInstance().createGroupConversation(gid);
        }

        //只有当会话存在，而且群没有被屏蔽的前提下，才会上抛事件
        if (null != conv && isGroupBlocked == 0) {
            cn.jpush.im.android.api.model.Message eventMsg = conv.createReceiveMessage(
                    content, "", 0, "系统消息", "", eventNotification.getCtimeMs(), eventNotification.getEventId(), "", null, true);
            ChatMsgManager.getInstance().sendOnlineMsgEvent(eventMsg);
        }
    }
}
