package cn.jpush.im.android.internalmodel;


import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;

public class InternalEventNotificationContent extends EventNotificationContent {
    private static final String TAG = InternalEventNotificationContent.class.getSimpleName();

    protected InternalEventNotificationContent(long groupID, long operator, EventNotificationType eventNotificationType, List<String> userNames, List<String> userDisplayNames, boolean isContainsMyself) {
        super();
        this.groupID = groupID;
        this.operator = operator;
        this.eventNotificationType = eventNotificationType;
        this.userNames = userNames;
        this.userDisplayNames = userDisplayNames;
        this.isContainsMyself = isContainsMyself;
        contentType = ContentType.eventNotification;
    }

    public InternalEventNotificationContent(long groupID, long operator, EventNotificationType eventNotificationType, List<String> userNames, List<String> userDisplayNames, boolean isContainsMyself, List<String> otherMemberDisplayNames) {
        this(groupID, operator, eventNotificationType, userNames, userDisplayNames, isContainsMyself);
        this.otherMemberDisplayNames = otherMemberDisplayNames;
    }

    public long getOperator() {
        return operator;
    }

    public List<String> getOtherMemberDisplayNames() {
        return otherMemberDisplayNames;
    }

    public List<String> getUserDisplayNames() {
        return userDisplayNames;
    }

    /**
     * @param groupID               群id
     * @param operatorID            事件操作者uid
     * @param eventNotificationType 事件类型
     * @param userIds               被操作的用户userId集合
     * @param usernames             被操作的用户username集合
     * @param groupMemberUserIds    事件发生时群中群成员的userId集合
     * @return
     */
    public static InternalEventNotificationContent createEventNotificationContent(long groupID, long operatorID,
                                                                                  EventNotificationContent.EventNotificationType eventNotificationType,
                                                                                  List<Long> userIds, List<String> usernames, List<Long> groupMemberUserIds) {
        if (userIds.isEmpty() || usernames.isEmpty()) {
            Logger.w(TAG, "key parameter is empty, return from createEventNotificationContent");
            return null;
        }

        //在生成event notification中的user displayName时需要包含用户的noteName。
        List<String> userDisplayNames = CommonUtils.translateUserIdToDisplaynames(userIds, true);
        List<String> groupMemberDisplayNames = null;
        boolean isContainsMyself = userIds.contains(IMConfigs.getUserID());
        if (null != groupMemberUserIds) {
            List<Long> groupMemberUidsCopy = new ArrayList<Long>();
            //在事件中最多只展示300个其他群成员displayName。
            if (groupMemberUserIds.size() > 300) {
                groupMemberUidsCopy.addAll(groupMemberUserIds.subList(0, 300));
            } else {
                groupMemberUidsCopy.addAll(groupMemberUserIds);
            }
            //最终在event中展示的群成员不应该包括事件的操作者和用户自己
            groupMemberUidsCopy.remove(operatorID);
            groupMemberUidsCopy.remove(IMConfigs.getUserID());
            groupMemberDisplayNames = CommonUtils.translateUserIdToDisplaynames(groupMemberUidsCopy, true);
        }

        return new InternalEventNotificationContent(groupID, operatorID, eventNotificationType, usernames, userDisplayNames, isContainsMyself, groupMemberDisplayNames);
    }
}
