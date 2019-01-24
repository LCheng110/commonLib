package cn.jpush.im.android.api.model;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.exceptions.JMFileSizeExceedException;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.api.BasicCallback;

public abstract class Conversation implements Serializable {

    private static final String TAG = "Conversation";

    protected String id;
    protected ConversationType type = ConversationType.single;

    protected String targetId = "";

    protected Object targetInfo;

    protected String title = "";

    protected int unReadMsgCnt;

    protected File avatar = null;

    protected Message latestMessage;

    protected String latestText = "";

    protected ContentType latestType = ContentType.text;

    protected long lastMsgDate;

    protected String extra = "";

    /**
     * 获取Conversation的ID
     *
     * @return Conversation的ID
     */
    public String getId() {
        return id;
    }

    /**
     * 获取会话类型
     *
     * @return 当前会话的会话类型
     */
    public ConversationType getType() {
        return type;
    }

    /**
     * 获取会话的extra
     *
     * @return
     * @since 2.3.0
     */
    public String getExtra() {
        return extra;
    }

    /**
     * 获取会话targetId,如果是单聊则是对象username,如果是群聊则是对象群组的groupID.
     *
     * @return targetID
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Conversation#getTargetInfo()} instead.
     */
    @Deprecated
    public String getTargetId() {
        return targetId;
    }

    /**
     * 获取会话target appkey,只有单聊会话中会有target appkey这个概念，群聊直接返回空字符串
     *
     * @return target appkey
     */
    public abstract String getTargetAppKey();

    /**
     * 获取会话最近一条消息的文本内容
     *
     * @return 最近一条消息的文本内容
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Conversation#getLatestMessage()}  instead.
     */
    @Deprecated
    public String getLatestText() {
        if (null != latestMessage) {
            switch (latestMessage.getContentType()) {
                case text:
                    TextContent content = (TextContent) latestMessage.getContent();
                    latestText = content.getText();
                    break;
                default:
                    latestText = "";
                    break;
            }
        }
        return latestText;
    }

    /**
     * 获取会话最近一条消息的创建时间
     *
     * @return 最近一条消息的消息发送时间。默认返回0.
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Conversation#getLatestMessage()}  instead.
     */
    @Deprecated
    public long getLastMsgDate() {
        if (null != latestMessage) {
            lastMsgDate = latestMessage.getCreateTime();
        }
        return lastMsgDate;
    }

    /**
     * 获取会话最近一条消息的消息内容类型
     *
     * @return 最近一条消息的消息内容类型。默认返回{@link ContentType#text}.
     * @deprecated deprecated in sdk version 1.1.4. Use {@link Conversation#getLatestMessage()}  instead.
     */
    @Deprecated
    public ContentType getLatestType() {
        if (null != latestMessage) {
            latestType = latestMessage.getContentType();
        }
        return latestType;
    }

    /**
     * 获取会话包含的未读消息数
     *
     * @return 当前会话的未读消息数。
     */
    public int getUnReadMsgCnt() {
        return 0 > unReadMsgCnt ? 0 : unReadMsgCnt;
    }

    /**
     * 设置会话中的未读消息数
     * 被设置的未读数仅在本地生效，不会被同步至多端在线的另一端。
     *
     * @param count 指定的未读消息数
     */
    public abstract boolean setUnReadMessageCnt(int count);

    /**
     * 获取会话对象的标题。<br/>
     * 如果会话为群聊 <br/>
     * --- 没有群名称，会自动使用群成员中前五个人的displayName
     * 拼接成title。<br/>
     * --- 有群名称，则显示群名称<br/>
     * <p/>
     * 如果会话为单聊<br/>
     * --- 用户有昵称，显示昵称。<br/>
     * --- 没有昵称，显示username
     *
     * @return 会话的标题。
     */
    public abstract String getTitle();

    /**
     * 获取会话对象的头像
     *
     * @return 当前会话对象的头像。
     */
    public File getAvatarFile() {
        return avatar;
    }

    /**
     * 获取会话中最近一条消息。
     *
     * @return 最近一条消息对象，如果会话中不包含任何消息则返回null
     */
    public abstract Message getLatestMessage();

    /**
     * 获取会话对象的Info。
     * 如果是单聊是聊天对象的{@link UserInfo}.
     * 如果是群聊则是聊天对象群的{@link GroupInfo}.<br/>
     * 使用时需要转型
     *
     * @return 会话对象的完整Info
     */
    public abstract Object getTargetInfo();


    /**
     * 创建会话，如果本地已存在对应会话，则不会重新创建，直接返回本地会话对象。
     *
     * @param type    会话类型
     * @param groupID 群组的groupID
     * @return 会话对象
     * @deprecated deprecated in sdk version 1.1.4.
     * Use {@link Conversation#createGroupConversation(long)}、{@link Conversation#createSingleConversation(String)}  instead.
     */
    @Deprecated
    public static Conversation createConversation(ConversationType type, long groupID) {
        return createConversation(type, String.valueOf(groupID));
    }

    /**
     * 创建会话，如果本地已存在对应会话，则不会重新创建，直接返回本地会话对象。
     * 创建的会话默认是当前appkey下的用户，如果需要给其他appkey下的用户发消息，
     * 请使用{@link Conversation#createSingleConversation(String, String)}接口指定appkey。
     *
     * @param type     会话类型
     * @param username 用户的username
     * @return 会话对象
     * @deprecated deprecated in sdk version 1.1.4.
     * Use {@link Conversation#createGroupConversation(long)}、{@link Conversation#createSingleConversation(String)}  instead.
     */
    @Deprecated
    public static Conversation createConversation(ConversationType type,
                                                  String username) {
        return ConversationManager.getInstance().createConversation(type, username, JCoreInterface.getAppKey());
    }

    /**
     * 创建群聊会话，如果本地已存在对应会话，则不会重新创建，直接返回本地会话对象。
     *
     * @param groupID 群组的groupID
     * @return 会话对象
     */
    public static Conversation createGroupConversation(long groupID) {
        return ConversationManager.getInstance().createGroupConversation(groupID);
    }

    /**
     * 创建单聊会话，如果本地已存在对应会话，则不会重新创建，直接返回本地会话对象。<br/>
     * 创建的会话默认是当前appkey下的用户，如果需要给其他appkey下的用户发消息，
     * 请使用{@link Conversation#createSingleConversation(String, String)}接口指定appkey。
     *
     * @param username 用户的username
     * @return 会话对象
     */
    public static Conversation createSingleConversation(String username) {
        return ConversationManager.getInstance().createSingleConversation(username, JCoreInterface.getAppKey());
    }

    /**
     * 创建单聊会话，如果本地已存在对应会话，则不会重新创建，直接返回本地会话对象。<br/>
     * 通过指定appkey，可以实现给其他appkey下的用户发消息。
     *
     * @param username 用户的username
     * @param appkey   用户所属应用的appkey
     * @return 会话对象
     */
    public static Conversation createSingleConversation(String username, String appkey) {
        return ConversationManager.getInstance().createSingleConversation(username, appkey);
    }

    /**
     * 重置会话的未读数。
     * 当用户在多端同时登陆的情况下，对端的会话未读数也会同时被清除掉。
     *
     * @return {@code true}表示成功重置，其他则为{@code false}
     */
    public abstract boolean resetUnreadCount();

    /**
     * 获取本会话中指定local message id的一条消息.<br/>
     *
     * @param localMsgId 指定消息的local message id.
     * @return 消息对象，如果local message id找不到对应消息则返回null
     */
    public abstract Message getMessage(int localMsgId);


    /**
     * 获取本会话中指定server message id的一条消息.<br/>
     *
     * @param serverMsgId 指定消息的server message id.
     * @return 消息对象，如果server message id找不到对应消息则返回null
     */
    public abstract Message getMessage(long serverMsgId);


    /**
     * 获取会话中所有消息，消息按照时间升序排列.<br/>
     *
     * @return 包含会话中所有消息的List
     */
    public abstract List<Message> getAllMessage();

    /**
     * 会话中消息按时间升序排列，从其中的offset位置，获取limit条数的消息.<br/>
     *
     * @param offset 获取消息的起始位置
     * @param limit  获取消息的条数
     * @return 符合查询条件的消息List, 如果查询失败则返回空的List。
     */
    public abstract List<Message> getMessagesFromOldest(int offset, int limit);

    /**
     * 会话中消息按时间降序排列，从其中的offset位置，获取limit条数的消息.<br/>
     *
     * @param offset 获取消息的起始位置
     * @param limit  获取消息的条数
     * @return 符合查询条件的消息List, 如果查询失败则返回空的List。
     */
    public abstract List<Message> getMessagesFromNewest(int offset, int limit);

    /**
     * 删除会话中指定messageId的消息
     *
     * @param messageId 被删除消息的message id。
     * @return 删除成功返回true, 其他情况返回false。
     */
    public abstract boolean deleteMessage(int messageId);

    /**
     * 删除会话中的所有消息，但不会删除会话本身。
     *
     * @return 删除成功返回true, 其他情况返回false。
     */
    public abstract boolean deleteAllMessage();

    /**
     * 更新会话的extra
     * 此接口会将相应的数据更新到缓存和数据库。
     * 注意该字段内容只会保存在本地数据库和缓存，本地数据清除或切换设备时不会再同步和保存
     *
     * @param extra 待更新的extra, 不能为null
     * @return 更新成功返回true, 其他情况返回false
     * @since 2.3.0
     */
    public abstract boolean updateConversationExtra(String extra);

    /**
     * 更新message中的extra.
     * 此接口会将相应的数据更新到数据库。
     *
     * @param msg   被更新的message对象
     * @param key   待更新的key,不能为null
     * @param value 待更新的value,不能为null
     * @return 更新成功返回true, 其他情况返回false
     */
    public abstract boolean updateMessageExtra(Message msg, String key, String value);

    /**
     * 更新message中的extra
     * 此接口会将相应的数据更新到数据库。
     *
     * @param msg   被更新的message对象
     * @param key   待更新的key,不能为null
     * @param value 待更新的value,不能为null
     * @return 更新成功返回true, 其他情况返回false
     */
    public abstract boolean updateMessageExtra(Message msg, String key, Number value);

    /**
     * 更新message中的extra
     * 此接口会将相应的数据更新到数据库。
     *
     * @param msg   被更新的message对象
     * @param key   待更新的key,不能为null
     * @param value 待更新的value,不能为null
     * @return 更新成功返回true, 其他情况返回false
     */
    public abstract boolean updateMessageExtra(Message msg, String key, Boolean value);

    /**
     * 更新message中的extras
     * 此接口会将相应的数据更新到数据库。
     *
     * @param msg    被更新的message对象
     * @param extras 待更新的extras，将整个替换之前的extras，而不是增减逻辑。不能为null
     * @return 更新成功返回true, 其他情况返回false
     */
    public abstract boolean updateMessageExtras(Message msg, Map<String, String> extras);

    /**
     * 创建一条发送的消息，同时将消息保存至本地数据库
     *
     * @param content 消息内容对象
     * @return 消息对象
     */
    public abstract Message createSendMessage(MessageContent content);

    /**
     * 创建一条发送的消息，同时将消息保存至本地数据库,并且可以自定义fromName，自定义的fromName
     * 将在消息接收方的通知栏作为展示名展示出来。如果未设置则会依照用户的备注 > 昵称 > 用户名的优先级来展示
     *
     * @param content        消息内容对象
     * @param customFromName 自定义fromName
     * @return 消息对象
     */
    public abstract Message createSendMessage(MessageContent content, String customFromName);

    /**
     * 创建一条@群成员的消息。
     * <p>
     * 可以自定义customFromName,自定义的customFromName将在对方的通知栏作为
     * 展示名展示出来,如果未设置则会依照用户的备注 > 昵称 > 用户名的优先级来展示
     *
     * @param content        消息内容对象
     * @param atList         被@群成员的UserInfo List
     * @param customFromName 自定义fromName
     * @return 消息对象
     * @since 2.1.0
     */
    public abstract Message createSendMessage(MessageContent content, List<UserInfo> atList, String customFromName);

    /**
     * 创建一条@群组中所有人的消息
     * <p>
     * 可以自定义customFromName,自定义的customFromName将在对方的通知栏作为
     * 展示名展示出来,如果未设置则会依照用户的备注 > 昵称 > 用户名的优先级来展示
     *
     * @param content        消息内容对象
     * @param customFromName 自定义fromName
     * @return 返回消息对象
     * @since 2.2.0
     */
    public abstract Message createSendMessageAtAllMember(MessageContent content, String customFromName);

    /**
     * 创建一条发送的文本消息，同时将消息保存至本地数据库
     *
     * @param text 消息的文本内容
     * @return 消息对象
     */
    public abstract Message createSendTextMessage(String text);

    /**
     * 创建一条发送的文本消息，同时将消息保存至本地数据库,并且可以自定义fromName，自定义的fromName
     * 将在消息接收方的通知栏作为展示名展示出来。如果未设置则会依照用户的昵称>用户名的优先级来展示
     *
     * @param text           消息的文本内容
     * @param customFromName 自定义fromName
     * @return 消息对象
     */
    public abstract Message createSendTextMessage(String text, String customFromName);

    /**
     * 创建一条发送的图片消息，同时将消息保存至本地数据库
     *
     * @param imageFile 图片文件
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public abstract Message createSendImageMessage(File imageFile) throws FileNotFoundException;

    /**
     * 创建一条发送的图片消息，同时将消息保存至本地数据库,并且可以自定义fromName，自定义的fromName
     * 将在消息接收方的通知栏作为展示名展示出来。如果未设置则会依照用户的昵称>用户名的优先级来展示
     *
     * @param imageFile      图片文件
     * @param customFromName 自定义fromName
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public abstract Message createSendImageMessage(File imageFile, String customFromName) throws FileNotFoundException;


    /**
     * 创建一条发送的语音消息，同时将消息保存至本地数据库
     *
     * @param voiceFile 语音文件
     * @param duration  语音时长
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public abstract Message createSendVoiceMessage(File voiceFile, int duration) throws FileNotFoundException;


    /**
     * 创建一条发送的语音消息，同时将消息保存至本地数据库,并且可以自定义fromName，自定义的fromName
     * 将在消息接收方的通知栏作为展示名展示出来。如果未设置则会依照用户的昵称>用户名的优先级来展示
     *
     * @param voiceFile      语音文件
     * @param duration       语音时长
     * @param customFromName 自定义fromName
     * @return 消息对象
     * @throws FileNotFoundException
     */
    public abstract Message createSendVoiceMessage(File voiceFile, int duration, String customFromName) throws FileNotFoundException;

    /**
     * 创建一条发送的自定义消息，同时将消息保存至本地数据库
     *
     * @param valuesMap 消息体map
     * @return 消息对象
     */
    public abstract Message createSendCustomMessage(Map<? extends String, ? extends String> valuesMap);

    /**
     * 创建一条发送的自定义消息，同时将消息保存至本地数据库,并且可以自定义fromName，自定义的fromName
     * 将在消息接收方的通知栏作为展示名展示出来。如果未设置则会依照用户的昵称>用户名的优先级来展示
     *
     * @param valuesMap      消息体map
     * @param customFromName 自定义fromName
     * @return 消息对象
     */
    public abstract Message createSendCustomMessage(Map<? extends String, ? extends String> valuesMap, String customFromName);

    /**
     * 创建一条发送文件的消息,同时将消息保存至本地数据库,并且可以自定义fromName;自定义的fromName
     * 将会在消息接收方的通知栏作为展示名展示出来,如果传值为null那么会依照用户的昵称>用户名的优先级来展示
     *
     * @param file           发送的文件
     * @param fileName       发送的文件名称
     * @param customFromName 自定义fromName
     * @return 消息对象
     * @throws FileNotFoundException
     * @since 1.4.0
     */
    public abstract Message createSendFileMessage(File file, String fileName, String customFromName) throws FileNotFoundException, JMFileSizeExceedException;

    /**
     * 创建一条发送文件的消息,同时将消息保存至本地数据库
     *
     * @param file     发送的文件
     * @param fileName 发送的文件名称
     * @return 消息对象
     * @throws FileNotFoundException
     * @since 1.4.0
     */
    public abstract Message createSendFileMessage(File file, String fileName) throws FileNotFoundException, JMFileSizeExceedException;

    /**
     * 创建一条地理位置，同时将消息保存至本地数据库
     *
     * @param latitude  纬度信息
     * @param longitude 经度信息
     * @param scale     地图缩放比例
     * @param address   详细位置信息
     * @return 消息对象
     * @since 1.4.0
     */
    public abstract Message createLocationMessage(double latitude, double longitude, int scale, String address);

    /**
     * 撤回一条发送的消息。
     *
     * @param message  被撤回的消息
     * @param callback 结果回调
     * @since 2.2.0
     */
    public abstract void retractMessage(Message message, BasicCallback callback);
}
