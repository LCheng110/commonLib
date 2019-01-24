package cn.jpush.im.android.api.content;

import android.text.TextUtils;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import java.util.List;

import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;


public class EventNotificationContent extends MessageContent {

    public enum EventNotificationType {
        group_member_added, group_member_removed, group_member_exit, group_info_updated
    }

    @Expose
    protected EventNotificationType eventNotificationType;

    @Expose
    //被操作的群成员的username集合,对外给出
    protected List<String> userNames;

    @Expose
    //事件的发起者
    protected long operator;

    @Expose
    //被操作的群成员的displayName集合（可用于界面展示）
    protected List<String> userDisplayNames;

    @Expose
    protected boolean containsGroupOwner;

    @Expose
    @SerializedName("groupMemberUserNames")
    protected List<String> otherMemberDisplayNames;//event发生时群组中其他的群成员的displayNames

    @Expose
    protected long groupID;

    @Expose
    //这次事件被操作的成员中是否包括我自己。
    protected boolean isContainsMyself = false;

    public List<String> getUserDisplayNames() {
        return userDisplayNames;
    }

    private StringBuffer eventText = new StringBuffer();

    protected EventNotificationContent() {
        super();
    }

    /**
     * 获取通知事件的类型
     *
     * @return 通知事件的类型
     */
    public EventNotificationType getEventNotificationType() {
        return eventNotificationType;
    }

    /**
     * 获取通知事件涉及到的用户的username列表。
     *
     * @return 涉及到用户的用户名列表
     */
    public List<String> getUserNames() {
        return userNames;
    }

    /**
     * 返回用户名列表中是否包含该群的群主
     *
     * @return 如果包含返回true, 其他情况返回false
     * @deprecated deprecated in sdk version 1.1.5
     */
    @Deprecated
    public boolean containsGroupOwner() {
        return containsGroupOwner;
    }

    /**
     * 获取通知事件相应的文字描述
     *
     * @return 文字描述字符串
     */
    public String getEventText() {
        if (!TextUtils.isEmpty(eventText)) {
            return eventText.toString();
        }

        if (null == userDisplayNames) {
            userDisplayNames = userNames;//如果displayname是空，则直接用userNames来展示以兼容旧版本。
        }

        boolean isOperateByMyself = false;
        boolean isSystemEvent = false;
        if (operator == IMConfigs.getUserID()) {
            isOperateByMyself = true;
        } else if (0 == operator) {
            isSystemEvent = true;
        }

        String opratorDisplayName;
        InternalUserInfo userInfo = UserInfoManager.getInstance().getUserInfo(operator);
        if (null != userInfo) {
            opratorDisplayName = userInfo.getDisplayName(true);
        } else if (isSystemEvent) {
            opratorDisplayName = "系统消息：";
        } else {
            opratorDisplayName = String.valueOf(operator);
        }
        switch (eventNotificationType) {
            case group_member_added:
                if (!isOperateByMyself && isContainsMyself) {
                    if (null != otherMemberDisplayNames && !otherMemberDisplayNames.isEmpty()) {
                        if (!isSystemEvent) {
                            eventText.append(opratorDisplayName).append("邀请你加入了群聊，群聊的人还有 ").append(otherMemberDisplayNames);
                        } else {
                            eventText.append(opratorDisplayName).append("你加入了群聊，群聊的人还有 ").append(otherMemberDisplayNames);
                        }
                    } else {
                        if (!isSystemEvent) {
                            eventText.append(opratorDisplayName).append("邀请你加入了群聊");
                        } else {
                            eventText.append(opratorDisplayName).append("你加入了群聊");
                        }
                    }
                } else if (!isOperateByMyself) {
                    if (!isSystemEvent) {
                        eventText.append(opratorDisplayName).append("邀请用户").append(userDisplayNames).append("加入了群聊 ");
                    } else {
                        eventText.append(opratorDisplayName).append("用户").append(userDisplayNames).append("加入了群聊 ");
                    }
                } else {
                    eventText.append("你邀请 ").append(userDisplayNames).append(" 加入了群聊");
                }

                break;
            case group_member_removed:
                if (isContainsMyself) {
                    if (!isSystemEvent) {
                        eventText.append("你被").append(opratorDisplayName).append("请出了群聊");
                    } else {
                        eventText.append(opratorDisplayName).append("你被请出了群聊");
                    }
                } else {
                    if (isOperateByMyself) {
                        eventText.append("你将 ").append(userDisplayNames).append(" 请出了群聊");
                    } else {
                        eventText.append("用户").append(userDisplayNames).append(" 被请出了群聊");
                    }
                }

                break;
            case group_member_exit:
                eventText.append("用户 ").append(userDisplayNames).append(" 退出了群聊");

                break;
            case group_info_updated:
                if (!isSystemEvent) {
                    if (!isOperateByMyself) {
                        eventText.append("用户 ").append(opratorDisplayName).append(" 修改了群信息");
                    } else {
                        eventText.append("你修改了群信息");
                    }
                } else {
                    eventText.append(opratorDisplayName).append(" 群信息被更新了");
                }
                break;
            default:
                eventText.append("未知事件");
        }
        return eventText.toString();
    }

}
