package cn.jpush.im.android.api.event;

import java.util.List;

public class BaseNotificationEvent {
    private static final String TAG = "BaseNotificationEvent";


    protected long eventId;
    protected int eventType;
    protected String fromUsername;
    protected String fromUserAppKey;
    protected long gid;
    protected List<String> toUsernameList;
    protected String desc;
    protected long cTime;
    protected int extra;
    protected int returnCode;

    /**
     * 获取事件创建的时间，单位：毫秒
     *
     * @return 事件创建的时间。
     */
    public long getCreateTime() {
        return cTime;
    }

    protected String getDesc() {
        return desc;
    }

    /**
     * 事件的唯一标识
     *
     * @return 唯一标识id
     */
    public long getEventId() {
        return eventId;
    }

    protected int getEventType() {
        return eventType;
    }

    protected int getExtra() {
        return extra;
    }

    /**
     * 获取事件发送者的username
     *
     * @return 对方用户的username
     */
    public String getFromUsername() {
        return fromUsername;
    }

    protected long getGid() {
        return gid;
    }

    /**
     * 获取事件发送者用户所属应用的appKey
     *
     * @return 对方用户所属应用的appKey
     */
    // TODO: 16/8/31 from should be upper case
    public String getfromUserAppKey() {
        return fromUserAppKey;
    }


    /**
     * 好友邀请的返回码，
     *
     * @return 0 表示对方同意了你的好友请求，其他对应了添加好友被拒绝的返回码。
     */
    public int getReturnCode() {
        return returnCode;
    }

    protected List<String> getToUsernameList() {
        return toUsernameList;
    }

    public static class Builder {
        private long eventId;
        private int eventType;
        private String fromUsername;
        String fromUserAppKey;
        private long gid;
        private List<String> toUsernameList;
        private String desc;
        private long cTime;
        private int extra;
        private int returnCode;

        public Builder setcTime(long cTime) {
            this.cTime = cTime;
            return this;
        }

        public Builder setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public Builder setEventId(long eventId) {
            this.eventId = eventId;
            return this;
        }

        public Builder setEventType(int eventType) {
            this.eventType = eventType;
            return this;
        }

        public Builder setExtra(int extra) {
            this.extra = extra;
            return this;
        }

        public Builder setFromUsername(String fromUsername) {
            this.fromUsername = fromUsername;
            return this;
        }

        public Builder setGid(long gid) {
            this.gid = gid;
            return this;
        }

        public Builder setReturnCode(int returnCode) {
            this.returnCode = returnCode;
            return this;
        }

        public Builder setfromUserAppKey(String appkey) {
            this.fromUserAppKey = appkey;
            return this;
        }

        public Builder setToUsernameList(List<String> toUsernameList) {
            this.toUsernameList = toUsernameList;
            return this;
        }

        BaseNotificationEvent buildFrom(BaseNotificationEvent event) {
            event.cTime = cTime;
            event.desc = desc;
            event.eventId = eventId;
            event.eventType = eventType;
            event.extra = extra;
            event.fromUsername = fromUsername;
            event.fromUserAppKey = fromUserAppKey;
            event.gid = gid;
            event.returnCode = returnCode;
            event.toUsernameList = toUsernameList;
            return event;
        }

        public BaseNotificationEvent build() {
            BaseNotificationEvent event = new BaseNotificationEvent();
            buildFrom(event);
            return event;
        }
    }

    @Override
    public String toString() {
        return "BaseNotificationEvent{" +
                "cTime=" + cTime +
                ", eventId=" + eventId +
                ", eventType=" + eventType +
                ", fromUsername='" + fromUsername + '\'' +
                ", fromUserAppKey='" + fromUserAppKey + '\'' +
                ", gid=" + gid +
                ", toUsernameList=" + toUsernameList +
                ", desc='" + desc + '\'' +
                ", extra=" + extra +
                ", returnCode=" + returnCode +
                '}';
    }
}
