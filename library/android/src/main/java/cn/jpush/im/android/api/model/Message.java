package cn.jpush.im.android.api.model;


import com.google.gson.jpush.JsonElement;
import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

import cn.jpush.im.android.api.callback.GetReceiptDetailsCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.api.BasicCallback;

public abstract class Message implements Serializable {
    private static final String TAG = "Message";

    protected int _id;

    protected MessageDirect direct = MessageDirect.send;

    protected MessageStatus status = MessageStatus.created;

    protected MessageContent content;

    protected long createTimeInMillis;

    @Expose
    protected Number version;

    @Expose
    @SerializedName("from_name")
    protected String fromName;

    @Expose
    @SerializedName("msg_body")
    protected JsonElement msgBody;

    @Expose
    @SerializedName("msg_type")
    protected String msgTypeString;

    @Expose
    @SerializedName("create_time")
    protected Number createTimeInSeconds;

    @Expose
    @SerializedName("target_type")
    protected ConversationType targetType;

    @Expose
    @SerializedName("target_id")
    protected String targetID;//lazy load property,内部使用时需要使用getXXX方法来读取这个值，而不是直接访问这个属性.

    @Expose
    @SerializedName("target_appkey")
    /** 单聊消息的targetAppkey是对方用户所属应用的appkey,群聊消息的targetAppkey为空字符串*/
    protected String targetAppkey;//lazy load property,内部使用时需要使用getXXX方法来读取这个值，而不是直接访问这个属性.

    @Expose
    @SerializedName("target_name")
    protected String targetName;//lazy load property,内部使用时需要使用getXXX方法来读取这个值，而不是直接访问这个属性.

    @Expose
    @SerializedName("from_type")
    protected String fromType = "user";//固定为"user"

    @Expose
    protected String from_platform = "a"; //android平台固定为"a"

    @Expose
    @SerializedName("from_id")
    protected String fromID;//

    @Expose
    @SerializedName("from_appkey")
    protected String fromAppkey;

    @Expose
    @SerializedName("at_list")
    protected List<Long> atList;

    @Expose
    @SerializedName("sui_mtime")//消息发送者userinfo的mtime，2.2.0版本新增。
    protected int suiMTime;

    @Expose
    protected JsonElement notification;

    protected ContentType contentType = ContentType.unknown;

    protected Object targetInfo;

    protected UserInfo fromUser;

    protected Long serverMessageId;

    protected Message() {
    }

    public int getId() {
        return _id;
    }

    /**
     * 获取@的群成员userInfo列表
     *
     * @param callback 接口回调
     * @since 2.1.0
     */
    public abstract void getAtUserList(GetUserInfoListCallback callback);

    /**
     * 判断自己是否在被@的列表中
     *
     * @return true 在;false 不在
     * @since 2.1.0
     */
    public abstract boolean isAtMe();

    /**
     * 判断消息是否是@所有人消息
     *
     * @return true 是 ; false 不是
     * @since 2.2.0
     */
    public abstract boolean isAtAll();

    /**
     * 获取消息的内容对象
     *
     * @return 消息的内容对象
     */
    public MessageContent getContent() {
        return content;
    }

    /**
     * 获取消息的内容类型
     *
     * @return 消息的内容类型
     */
    public ContentType getContentType() {
        return contentType;
    }

    /**
     * 获取消息的状态
     *
     * @return 消息的状态
     */
    public MessageStatus getStatus() {
        return status;
    }

    /**
     * 获取消息的方向。<br/>发送（send）或者接收(receive)
     *
     * @return 消息的方向
     */
    public MessageDirect getDirect() {
        return direct;
    }

    /**
     * 获取消息发送对象的类型。<br/>单聊(Single)或者群聊(Group)
     *
     * @return 消息发送对象的类型
     */
    public ConversationType getTargetType() {
        return targetType;
    }

    /**
     * 获取消息被创建的时间
     *
     * @return 消息被创建的时间
     */
    public long getCreateTime() {
        return createTimeInMillis;
    }

    /**
     * 获取消息发送者的类型，（sdk用户发送的消息都默认为“user”）
     *
     * @return 消息发送者的类型
     */
    public String getFromType() {
        return fromType;
    }

    /**
     * 获取发送者的displayName
     *
     * @return 发送者的displayName
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Message#getFromUser()} instead.
     */
    @Deprecated
    public String getFromName() {
        return fromName;
    }

    /**
     * 获取消息发送者的username
     *
     * @return 消息发送者的username
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Message#getFromUser()} instead.
     */
    @Deprecated
    public String getFromID() {
        return fromID;
    }

    /**
     * 获取消息发送者的appkey
     *
     * @return 消息发送者的appkey
     */
    public abstract String getFromAppKey();

    /**
     * 获取消息发送者的UserInfo。如果这是一条发送的消息（direct == send）,fromUser是用户自己。
     * 若是接收到的消息（direct == receive）,fromUser则是消息发送方。
     *
     * @return 消息发送者的UserInfo
     */
    public abstract UserInfo getFromUser();

    /**
     * 获取消息发送对象的displayName
     *
     * @return 消息发送对象的displayName
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Message#getTargetInfo()} instead.
     */
    @Deprecated
    public abstract String getTargetName();

    /**
     * 获取消息发送对象的targetID,如果是单聊即是对象用户的username,如果是群聊
     * 则是对象群的groupID
     *
     * @return 消息发送对象的targetID
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Message#getTargetInfo()} instead.
     */
    @Deprecated
    public abstract String getTargetID();

    /**
     * 获取消息发送对象的appkey
     *
     * @return 消息发送对象的appkey
     */
    public abstract String getTargetAppKey();

    /**
     * 获取消息对象的targetInfo。如果这是一条单聊消息，则targetInfo就是聊天对象的{@link UserInfo}。
     * 如果是群聊消息，则targetInfo就是对象群的{@link GroupInfo}。<br/>
     * 使用的时候需要强制转换一下类型。
     *
     * @return 消息对象的targetInfo
     */
    public Object getTargetInfo() {
        return targetInfo;
    }

    /**
     * 设置监听消息所带附件(图片、语音等)上传进度的回调接口
     *
     * @param callback
     */
    public abstract void setOnContentUploadProgressCallback(ProgressUpdateCallback callback);

    /**
     * 判断这条消息是否已设置了监听上传进度的回调接口
     *
     * @return {@code true}表示已存在，其他情况为{@code false}
     */
    public abstract boolean isContentUploadProgressCallbackExists();

    /**
     * 设置监听消息所带附件(图片、语音等)下载进度的回调接口
     *
     * @param callback
     */
    public abstract void setOnContentDownloadProgressCallback(ProgressUpdateCallback callback);

    /**
     * 判断这条消息是否已设置了监听下载进度的回调接口
     *
     * @return {@code true}表示已存在，其他情况为{@code false}
     */
    public abstract boolean isContentDownloadProgressCallbackExists();

    /**
     * 设置监听消息发送完成的回调接口
     *
     * @param sendCompleteCallback
     */
    public abstract void setOnSendCompleteCallback(BasicCallback sendCompleteCallback);

    /**
     * 判断这条消息是否已设置了监听发送完成的回调接口
     *
     * @return {@code true}表示已存在，其他情况为{@code false}
     */
    public abstract boolean isSendCompleteCallbackExists();

    /**
     * 获取消息对应服务器端的messageId
     *
     * @return
     */
    public Long getServerMessageId() {
        return serverMessageId;
    }

    /**
     * 获取消息发送者userinfo最后的更新时间。
     *
     * @return 消息发送者userinfo最后更新事件，单位-秒
     */
    public int getSenderUserInfoMTime() {
        return suiMTime;
    }

    /**
     * 这条消息的已读状态.
     * 默认所有消息的已读状态都是false,当上层调用{@link Message#setHaveRead(BasicCallback)}接口成功后，这条消息的已读状态被更新为true.
     * <p>
     * 注意这个已读状态只会保存在本地，当本地数据被清除，或者用户更换设备登陆之后，已读状态会被重置为false.
     *
     * @return 消息的已读状态. true - 已读,false - 未读
     * @since 2.3.0
     */
    public abstract boolean haveRead();

    /**
     * 将这条消息标记为已读。
     * 如果这条消息的发送方设置了需要已读回执，此时还会给消息发送方发送已读回执，通知发送方此条消息已被阅读.
     * 已读回执发送成功后，消息发送方会收到{@link cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent}事件通知
     * <p>
     * 对于已发送过已读回执的消息，重复调用此接口也不会重复给对方发送回执。
     * <p>
     * 注意设置成功之后，这个已读状态只会保存在本地，当本地数据被清除，或者用户更换设备登陆之后，已读状态会被重置为false.
     *
     * @param callback 回调接口
     * @since 2.3.0
     */
    public abstract void setHaveRead(BasicCallback callback);

    /**
     * 获取针对这条消息还没有发送已读回执的人数.
     * 注意只有当消息的direct为{@link MessageDirect#send},而且发送方在发送消息的时候通过
     * {@link cn.jpush.im.android.api.options.MessageSendingOptions#setNeedReadReceipt(boolean)}接口设置了需要已读回执之后
     * 这个接口才会正常返回人数，否则将返回0。
     *
     * @return 还未发送已读回执的人数.
     * @since 2.3.0
     */
    public abstract int getUnreceiptCnt();

    /**
     * 获取这条消息的未回执人数最后一次更新的时间
     *
     * @return 未回执人数最后一次更新的时间，单位毫秒
     * @since 2.3.0
     */
    public abstract long getUnreceiptMtime();

    /**
     * 设置这条消息的未回执人数。
     * <p>
     * 注意这个接口设置的值仅仅是保存在内存中，不会写入到数据库或者更新到后台
     *
     * @param unreceipteCnt 未回执人数
     * @since 2.3.0
     */
    public abstract void setUnreceiptCnt(int unreceipteCnt);

    /**
     * 设置这条消息的未回执人数更新时间。
     * <p>
     * 注意这个接口设置的值仅仅是保存在内存中，不会写入到数据库或者更新到后台
     *
     * @param unreceipteMtime 未回执人数更新时间
     * @since 2.3.0
     */
    public abstract void setUnreceiptMtime(long unreceipteMtime);

    /**
     * 获取这条消息的已读回执详情.
     * 包括已发送已读回执的UserInfo列表以及未发送已读回执的UserInfo列表。
     * <p>
     * 只有当消息的{@linkplain MessageDirect}为{@link MessageDirect#send}、而且这条消息的{@link MessageStatus}是
     * {@link MessageStatus#send_success}时，可以查看这条消息的已读回执详情
     *
     * @param callback 回调接口
     * @since 2.3.0
     */
    public abstract void getReceiptDetails(GetReceiptDetailsCallback callback);
}
