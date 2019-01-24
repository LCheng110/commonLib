package cn.jpush.im.android.internalmodel;

import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent;
import cn.jpush.im.android.api.event.OfflineMessageEvent;
import cn.jpush.im.android.api.exceptions.JMFileSizeExceedException;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.storage.MessageStorage;
import cn.jpush.im.android.storage.OnlineMsgRecvStorage;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetGroupInfoTask;
import cn.jpush.im.android.tasks.GetUserInfoTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public class InternalConversation extends Conversation implements Serializable {

    private final static String TAG = "InternalConversation";

    public static final int MAX_ONLINE_MSGID_CACHE_SIZE = 500;

    public static final int ONLINE_MSGID_TRIM_SIZE = 300;

    private final static Object unReadMsgCntLock = new Object();

    private Set<Long> onlineMsgList = null;

    protected String msgTableName;

    private String onlineMsgTableName;

    private String eventIdTableName;

    private String targetName = "";

    protected String targetAppKey = "";

    private long unreadCntMtime = 0;//未读数最后一次被清空的时间

    public InternalConversation() {
        super();
        id = java.util.UUID.randomUUID().toString();
    }

    /**
     * 获取会话targetId,如果是单聊则是对象username,如果是群聊则是对象群组的groupID
     *
     * @return
     */
    public String getTargetId() {
        return targetId;
    }

    /**
     * 获取会话最近一条消息的文本内容
     *
     * @return
     */
    public String getLatestText() {
        return latestText;
    }

    /**
     * 获取会话最近一条消息的创建时间
     *
     * @return
     */
    public long getLastMsgDate() {
        return lastMsgDate;
    }


    /**
     * 获取会话对象的appkey.
     *
     * @return 会话对象的appkey
     */
    @Override
    public String getTargetAppKey() {
        if (null == targetAppKey) {
            Logger.ww(TAG, "target appkey is null ,return default value.");
            if (ConversationType.single == type) {
                return JCoreInterface.getAppKey();
            } else {
                return "";
            }
        }
        return targetAppKey;
    }

    public void setTargetAppKey(String targetAppKey) {
        this.targetAppKey = targetAppKey;
    }

    /**
     * 获取会话最近一条消息的消息内容类型
     *
     * @return
     */
    public ContentType getLatestType() {
        return latestType;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setType(ConversationType type) {
        this.type = type;
    }

    public void setExtra(String extra) {
        this.extra = extra;
    }

    public void setTargetId(String targetId) {
        this.targetId = targetId;
    }

    public void setLatestText(String latestMsg) {
        this.latestText = latestMsg;
    }

    public void setLastMsgDate(long lastMsgDate) {
        this.lastMsgDate = lastMsgDate;
    }

    public void setUnReadMsgCnt(int unReadMsgCnt) {
        this.unReadMsgCnt = unReadMsgCnt;
    }

    public void setLatestType(ContentType latestType) {
        this.latestType = latestType;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setAvatarPath(String filePath) {
        if (null == filePath) {
            avatar = null;
            return;
        }
        File file = new File(filePath);
        if (file.exists()) {
            avatar = file;
        } else {
            avatar = null;
        }
    }

    public String getMsgTableName() {
        return msgTableName;
    }

    public void setMsgTableName(String msgTableName) {
        this.msgTableName = msgTableName;
    }

    public String getOnlineMsgTableName() {
        return onlineMsgTableName;
    }

    public void setOnlineMsgTableName(String onlineMsgTableName) {
        this.onlineMsgTableName = onlineMsgTableName;
    }

    public String getEventIdTableName() {
        return eventIdTableName;
    }

    public void setEventIdTableName(String eventIdTableName) {
        this.eventIdTableName = eventIdTableName;
    }

    public void setTargetInfo(Object info) {
        if (info instanceof InternalUserInfo || info instanceof InternalGroupInfo) {
            targetInfo = info;
        }
    }

    public void setLatestMsg(Message msg) {
        latestMessage = msg;
    }

    public long getUnreadCntMtime() {
        return unreadCntMtime;
    }

    public void setUnreadCntMtime(long unreadCntMtime) {
        this.unreadCntMtime = unreadCntMtime;
    }

    @Override
    public synchronized boolean resetUnreadCount() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("resetUnreadCount", null)) {
            return false;
        }
        int count = unReadMsgCnt;
        boolean result = ConversationManager.getInstance().resetUnreadCount(type, targetId, targetAppKey);
        if (result) {
            if (!isTargetInNoDisturb()) {//改变全局未读数之前，判断target是否是免打扰状态
                JMessage.addAllUnreadMsgCntBy(-count);
            }
            unReadMsgCnt = 0;
            //清除通知栏的通知
            ChatMsgManager.getInstance().cancelNotification(targetId, targetAppKey);
            //发送未读数清空通知
            if (0 < count) {
                RequestProcessor.resetUnreadCnt(this, count, IMConfigs.getNextRid());
            }
        }
        return result;
    }

    @Override
    public synchronized boolean setUnReadMessageCnt(int newCount) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("setUnReadMsgCnt", null)) {
            return false;
        }
        if (0 > newCount) {
            Logger.ww(TAG, "unread count cannot less than 0. set unread count failed.");
            return false;
        }
        int oldCount = getUnReadMsgCnt();

        boolean result = ConversationManager.getInstance().updateConversationUnreadCnt(type, targetId, targetAppKey, newCount, true);
        if (result) {
            if (!isTargetInNoDisturb()) {//改变全局未读数之前，判断target是否是免打扰状态
                JMessage.addAllUnreadMsgCntBy(-oldCount);//先将总未读数减去该会话之前的未读数
                JMessage.addAllUnreadMsgCntBy(newCount);//然后将新的未读数加到总未读数中去
            }
            unReadMsgCnt = newCount;
        }
        return result;
    }

    private boolean increaseUnreadCnt(int increaseBy) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("increaseUnreadCnt", null) || 0 == increaseBy) {
            return false;
        }
        // TODO: 2017/2/23 可能影响newlist 批量处理的效率
        synchronized (unReadMsgCntLock) {  // 加上静态锁防止由于多线程修改数据库造成的问题
            //每次更新unreadCnt前 先从数据库中读取
            unReadMsgCnt = ConversationManager.getInstance().queryUnreadCnt(type, targetId, targetAppKey) + increaseBy;
            unReadMsgCnt = 0 > unReadMsgCnt ? 0 : unReadMsgCnt;
            Logger.d(TAG, "[increaseUnreadCnt] new unread cnt = " + unReadMsgCnt);
            if (!isTargetInNoDisturb()) {//改变全局未读数之前，判断target是否是免打扰状态
                JMessage.addAllUnreadMsgCntBy(increaseBy);
            }
            return ConversationManager.getInstance().updateConversationUnreadCnt(type, targetId, targetAppKey, unReadMsgCnt, false);
        }
    }

    //重新计算未读数
    public synchronized int recalculateUnreadCnt() {
        Logger.d(TAG, "[recalculateUnreadCnt] start. unreadCnt = " + unReadMsgCnt + " unreadCntMtime = " + unreadCntMtime);
        int unreadCnt = 0;
        int offset = 0;
        int limit = 100;
        int oldCnt = unReadMsgCnt;

        //从消息列表中查询最近100条消息的Ctime.
        List<Long> cTimes = getMessageCTimeFromNewest(offset, limit);
        while (null != cTimes && !cTimes.isEmpty()) {
            for (Long cTime : cTimes) {
                Logger.d(TAG, " msg ctime = " + cTime + " unreadCntMtime = " + unreadCntMtime + " " + (cTime > unreadCntMtime));
                if (cTime > unreadCntMtime) {
                    //如果消息的ctime大于会话未读数最后一次清空的时间，则会话未读数加1.
                    unreadCnt++;
                } else {
                    //如果小于会话未读数最后一次清空的时间，更新会话的未读数并返回
                    updateUnreadCntInternal(oldCnt, unreadCnt);
                    return unReadMsgCnt;
                }
            }
            offset += limit;
            cTimes = getMessageCTimeFromNewest(offset, limit);
        }
        updateUnreadCntInternal(oldCnt, unreadCnt);
        return unReadMsgCnt;
    }

    private void updateUnreadCntInternal(int oldCnt, int newCnt) {
        setUnReadMessageCnt(newCnt);
        if (oldCnt != newCnt) {
            //如果新的未读数和老的未读数不同，需要上抛事件通知上层
            EventBus.getDefault().post(new ConversationRefreshEvent(this, ConversationRefreshEvent.Reason.UNREAD_CNT_UPDATED));
        }
        if (0 >= newCnt) {
            //新的未读数为0，将通知栏上属于这个会话的通知清掉
            ChatMsgManager.getInstance().cancelNotification(getTargetId(), getTargetAppKey());
        }
        Logger.d(TAG, "[recalculateUnreadCnt] finish. unreadCnt = " + unReadMsgCnt);
    }

    @Override
    public Message getMessage(int messageId) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getMessage", null)) {
            return null;
        }

        Message lastMsg = getLatestMessage();
        if (null != lastMsg && lastMsg.getId() == messageId) {
            //如果和latestMsg的id相等，则直接返回latestMsg
            return lastMsg;
        }

        try {
            return MessageStorage.querySync(messageId, getTargetInfo(), type, msgTableName);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Message getMessage(long serverMsgId) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getMessage", null)) {
            return null;
        }

        Message lastMsg = getLatestMessage();
        if (null != lastMsg && lastMsg.getServerMessageId() == serverMsgId) {
            //如果和latestMsg的id相等，则直接返回latestMsg
            return lastMsg;
        }

        try {
            return MessageStorage.queryWithServerMsgIdSync(serverMsgId, getTargetInfo(), type, msgTableName);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Message getLatestMessage() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getLatestMessage", null)) {
            return null;
        }
        if (null != latestMessage) {
            //todo:这里会话缓存了最后一条消息的消息对象，所以在消息状态更新时，需要注意同时要用ConversationManager.updateLatestMessage更新缓存中conversation里的latestMsg.
            return latestMessage;
        }

        try {
            InternalMessage message = MessageStorage.queryLatestSync(getTargetInfo(), type, msgTableName);
            if (null != message) {
                message.setTargetName(getTargetName());
            }
            return message;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public Object getTargetInfo() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getTargetInfo", null)) {
            return null;
        }
        if (null != targetInfo) {
            return targetInfo;
        }
        if (type == ConversationType.single) {
            targetInfo = UserInfoManager.getInstance().getUserInfo(targetId, targetAppKey);
            if (null == targetInfo) {
                Logger.d(TAG, "get target info from local. targetinfo is null");
                //本地还没有userInfo时，先构造一个tempInfo返回，之后启动下载任务,下载成功后更新。
                InternalUserInfo tempInfo = new InternalUserInfo();
                tempInfo.setUserName(targetId);
                tempInfo.setAppkey(targetAppKey);
                targetInfo = tempInfo;
                getUserInfoFromServer();
            }
            return targetInfo;
        } else if (type == ConversationType.group) {
            targetInfo = GroupStorage.queryInfoSync(Long.parseLong(targetId));
            if (null == targetInfo) {
                //本地还没有groupInfo时，先构造一个tempInfo返回，之后启动下载任务,下载成功后更新。
                InternalGroupInfo tempInfo = new InternalGroupInfo();
                tempInfo.setGroupID(Long.parseLong(targetId));
                targetInfo = tempInfo;
                getGroupInfoFromServer();
            }
            return targetInfo;
        }
        Logger.ww(TAG, "can not get target info. unsupported conversation type !!");
        return null;
    }

    public boolean isTargetInNoDisturb() {
        boolean isInNoDisturb = false;
        Object target = getTargetInfo();
        if (null != target) {
            if (type == ConversationType.single) {
                UserInfo userInfo = (UserInfo) target;
                isInNoDisturb = (userInfo.getNoDisturb() == 1);
            } else if (type == ConversationType.group) {
                GroupInfo groupInfo = (GroupInfo) target;
                isInNoDisturb = (groupInfo.getNoDisturb() == 1);
            }
        }
        return isInNoDisturb;
    }

    @Override
    public List<Message> getAllMessage() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getAllMessage", null)) {
            return null;
        }

        try {
            return MessageStorage.queryAllSync(new ArrayList<Message>(), getTargetInfo(), type, msgTableName);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }


    @Override
    public List<Message> getMessagesFromOldest(int offset, int limit) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getMessageFromOldest", null)) {
            return null;
        }

        try {
            return MessageStorage.queryFromOldest(new ArrayList<Message>(), offset, limit, getTargetInfo(), type, msgTableName);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public List<Message> getMessagesFromNewest(int offset, int limit) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getMessageFromNewest", null)) {
            return null;
        }

        try {
            return MessageStorage.queryFromLatest(new ArrayList<Message>(), offset, limit, getTargetInfo(), type, msgTableName);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Long> getMessageCTimeFromNewest(int offset, int limit) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getMessageCTimeFromNewest", null)) {
            return null;
        }

        try {
            return MessageStorage.queryCtimeFromNewest(offset, limit, msgTableName);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public boolean deleteMessage(int messageId) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("deleteMessage", null)) {
            return false;
        }

        try {
            boolean result = MessageStorage.deleteSync(messageId, msgTableName);
            if (result) {
                InternalMessage latestMsg = MessageStorage.queryLatestSync(getTargetInfo(), type, msgTableName);
                ConversationManager.getInstance().updateLatestMsg(type, targetId, targetAppKey, latestMsg);
            }
            return result;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteAllMessage() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("deleteAllMessage", null)) {
            return false;
        }

        try {
            boolean result = MessageStorage.deleteAllSync(msgTableName);
            if (result) {
                resetUnreadCount();
                ConversationManager.getInstance().updateLatestMsg(type, targetId, targetAppKey, null);
            }
            return result;
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateConversationExtra(String extra) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateConversationExtra", null)) {
            return false;
        }
        if (null == extra) {
            Logger.ee(TAG, "[updateConversationExtra] invalid parameters! extra = " + extra);
            return false;
        }

        return ConversationManager.getInstance().updateConversationExtra(type, targetId, targetAppKey, extra);
    }

    @Override
    public boolean updateMessageExtra(Message msg, String key, String value) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)) {
            return false;
        }
        try {
            if (null == msg || null == msg.getContent() || null == key || null == value) {
                Logger.ee(TAG, "[updateMessageExtra] invalid parameters! msg = " + msg + "key = "
                        + key + " value = " + value);
                return false;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.getContent().setStringExtra(key, value);
            return MessageStorage.updateMessageContentSync(msgTableName, msg.getId(), internalMessage.getContent());
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateMessageExtra(Message msg, String key, Number value) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)) {
            return false;
        }
        try {
            if (null == msg || null == msg.getContent() || null == key || null == value) {
                Logger.ee(TAG, "[updateMessageExtra] invalid parameters! msg = " + msg + "key = "
                        + key + " value = " + value);
                return false;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.getContent().setNumberExtra(key, value);
            return MessageStorage.updateMessageContentSync(msgTableName, msg.getId(), internalMessage.getContent());
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateMessageExtra(Message msg, String key, Boolean value) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)) {
            return false;
        }
        try {
            if (null == msg || null == msg.getContent() || null == key || null == value) {
                Logger.ee(TAG, "[updateMessageExtra] invalid parameters! msg = " + msg + "key = "
                        + key + " value = " + value);
                return false;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.getContent().setBooleanExtra(key, value);
            return MessageStorage.updateMessageContentSync(msgTableName, msg.getId(), internalMessage.getContent());
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateMessageExtras(Message msg, Map<String, String> extras) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtras", null)) {
            return false;
        }
        try {
            if (null == msg || null == msg.getContent() || null == extras) {
                Logger.ee(TAG, "[updateMessageExtras] invalid parameters! msg = " + msg + "exrtas = " + extras);
                return false;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.getContent().setExtras(extras);
            return MessageStorage.updateMessageContentSync(msgTableName, msg.getId(), internalMessage.getContent());
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Message createSendMessage(MessageContent content, final List<UserInfo> userInfos, boolean isAtAll, String customFromName) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("createSendMessage", null)) {
            return null;
        }

        if (null == content) {
            Logger.ee(TAG, "[createSendMessage] invalid parameters! content is null");
            return null;
        }

        List<Long> atList = null;
        if (userInfos != null && !userInfos.isEmpty() && type == ConversationType.group) {
            atList = new ArrayList<Long>();
            //只有当atlist不为空,而且这个会话是群组会话时,atlist才会生效
            for (UserInfo info : userInfos) {
                atList.add(info.getUserID());
            }
        }
        if (isAtAll) {
            atList = new ArrayList<Long>();
            atList.add(1L);
        }
        int isSetFromName = 0;
        if (!TextUtils.isEmpty(customFromName)) {
            isSetFromName = 1;
        }
        return createSendMessage(content, atList, customFromName, isSetFromName, CommonUtils.getFixedTime(), 0, true);
    }

    @Override
    public Message createSendMessage(MessageContent content, final List<UserInfo> userInfos, String customFromName) {
        return createSendMessage(content, userInfos, false, customFromName);
    }

    @Override
    public Message createSendMessageAtAllMember(MessageContent content, String customFromName) {
        return createSendMessage(content, null, true, customFromName);
    }

    @Override
    public Message createSendMessage(MessageContent content, String customFromName) {
        return createSendMessage(content, null, customFromName);
    }

    @Override
    public Message createSendMessage(MessageContent content) {
        return createSendMessage(content, null);
    }

    public Message createSendMessage(MessageContent content, final List<Long> atList, String customFromName,
                                     int isSetFromName, long createTime, long serverMsgId, boolean needSave) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("createSendMessage", null)) {
            return null;
        }

        if (null == content) {
            Logger.ee(TAG, "[createSendMessage] invalid parameters! content is null");
            return null;
        }

        String fromName = null;
        if (!TextUtils.isEmpty(customFromName)) {
            fromName = customFromName;
        } else {
            InternalUserInfo info = (InternalUserInfo) JMessageClient.getMyInfo();
            if (null != info) {
                // 不包含noteName,因为noteName只是发送者才可见
                fromName = info.getDisplayName(false);
            }
        }

        String fromID = IMConfigs.getUserName();
        //获取targetName
        if (TextUtils.isEmpty(targetName)) {
            targetName = getTargetName();
        }
        InternalMessage msg;
        msg = createMessage(MessageDirect.send, content, fromName, fromID, JCoreInterface.getAppKey(), createTime, serverMsgId, "", atList);
        msg.setIsSetFromName(isSetFromName);
        if (needSave) {
            /**
             * 将消息保存至数据库，并更新会话
             * 注意这里应该用同步方法来插入消息，以确保message对象中的_id属性被填充，
             * 如果_id没有被填充，会导致msg的callback索引找不到
             */
            addAndUpdateLatest(msg);
        }
        return msg;
    }

    protected String getTargetName() {
        String targetName = "";
        if (ConversationType.single == type) {
            InternalUserInfo targetInfo = UserInfoManager.getInstance().getUserInfo(targetId, targetAppKey);
            if (null != targetInfo) {
                //不能使用displayName作为消息的targetName,因为备注名只是发送者才可见
                targetName = targetInfo.getDisplayName(false);
            } else {
                targetName = targetId;
            }
        } else if (ConversationType.group == type) {
            try {
                GroupInfo targetGroupInfo = GroupStorage.queryInfoSync(Long.parseLong(targetId));
                if (null != targetGroupInfo && !TextUtils.isEmpty(targetGroupInfo.getGroupName())) {
                    targetName = targetGroupInfo.getGroupName();
                }
            } catch (NumberFormatException nfe) {
                Logger.ee(TAG, "targetID parse failed! ");
            }
        }
        return targetName;
    }

    @Override
    public Message createSendTextMessage(String text) {
        return createSendTextMessage(text, null);
    }

    @Override
    public Message createSendTextMessage(String text, String customFromName) {
        return createSendMessage(new TextContent(text), customFromName);
    }

    @Override
    public Message createSendImageMessage(File imageFile) throws FileNotFoundException {
        return createSendImageMessage(imageFile, null);
    }

    @Override
    public Message createSendImageMessage(File imageFile, String customFromName) throws FileNotFoundException {
        return createSendMessage(new ImageContent(imageFile), customFromName);
    }

    @Override
    public Message createSendVoiceMessage(File voiceFile, int duration) throws FileNotFoundException {
        return createSendVoiceMessage(voiceFile, duration, null);
    }

    @Override
    public Message createSendVoiceMessage(File voiceFile, int duration, String customFromName) throws FileNotFoundException {
        return createSendMessage(new VoiceContent(voiceFile, duration), customFromName);
    }

    @Override
    public Message createSendCustomMessage(Map<? extends String, ? extends String> valuesMap) {
        return createSendCustomMessage(valuesMap, null);
    }

    @Override
    public Message createSendCustomMessage(Map<? extends String, ? extends String> valuesMap, String customFromName) {
        CustomContent customContent = new CustomContent();
        customContent.setAllValues(valuesMap);
        return createSendMessage(customContent, customFromName);
    }

    @Override
    public Message createSendFileMessage(File file, String fileName) throws FileNotFoundException, JMFileSizeExceedException {
        return createSendFileMessage(file, fileName, null);
    }

    @Override
    public Message createSendFileMessage(File file, String fileName, String customFromName) throws FileNotFoundException, JMFileSizeExceedException {
        return createSendMessage(new FileContent(file, fileName), customFromName);
    }

    @Override
    public Message createLocationMessage(double latitude, double longitude, int scale, String address) {
        return createSendMessage(new LocationContent(latitude, longitude, scale, address));
    }

    @Override
    public void retractMessage(Message message, BasicCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("retractMessage", callback)) {
            return;
        }
        if (message == null) {
            Logger.ee(TAG, "The retract message can not be null.");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        RequestProcessor.msgRetract(JMessage.mContext, message.getServerMessageId(), IMConfigs.getNextRid(), this, message, callback);
    }

    public Message createReceiveMessage(MessageContent content, String fromName, int isSetFromName,
                                        String fromID, String fromAppkey, long createTime, long msgId,
                                        String protocol, List<Long> atList, boolean needSave) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("createReceiveMessage", null)) {
            return null;
        }

        if (null == content || TextUtils.isEmpty(fromID)) {
            Logger.ee(TAG, "[createReceiveMessage] invalid parameters! content = " + content + " fromID = "
                    + fromID);
            return null;
        }
        InternalMessage msg = createMessage(MessageDirect.receive, content, fromName, fromID, fromAppkey, createTime, msgId, protocol, atList);
        msg.setIsSetFromName(isSetFromName);
        if (needSave) {
            addAndUpdateLatest(msg);
        }
        return msg;
    }

    public InternalMessage saveMessage(InternalMessage internalMessage, boolean increaseUnreadCnt) {
        if (increaseUnreadCnt) {
            increaseUnreadCnt(1);
        }
        return (InternalMessage) addAndUpdateLatest(internalMessage);
    }


    public List<InternalMessage> saveMessagesInBatch(final long uid, final List<InternalMessage> createdMsgs, final Collection<Long> newList, final Collection<Long> oldList,
                                                     InternalMessage latestMsg, int unReadMsgCnt) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("saveMessagesInBatch", null)) {
            return null;
        }

        //更新会话未读数。
        increaseUnreadCnt(unReadMsgCnt);

        addAndUpdateLatestInBatch(uid, createdMsgs, latestMsg).continueWith(new Continuation<Void, Object>() {
            @Override
            /**
             * 只有当数据库操作执行完成才能执行下面将消息加入到去重列表以及上抛事件的逻辑。否则上抛的事件的message对象的
             * msgId字段可能还未填充。导致上层拿到的msgId为0.
             *
             * jira:http://jira.jpushoa.com/browse/IM-1920
             *
             */
            public Object then(Task<Void> task) throws Exception {
                if (uid != IMConfigs.getUserID()) {
                    Logger.ww(TAG, "current uid not match uid in protocol. abort this insert.");
                    return null;
                }
                //获取msg content成功之后，将所有msg id插入到OnlineMsgTable中去用于之后和在线收到的消息去重
                insertToOnlineMsgTable(createdMsgs);
                if (!newList.isEmpty() && null != createdMsgs) {
                    List<Message> newMsgs = new ArrayList<Message>();
                    //如果newlist中包含了消息，则上抛事件时需要从所有的msg中筛选出newlist这部分消息上抛给上层。
                    for (InternalMessage msg : createdMsgs) {
                        if (newList.contains(msg.getServerMessageId())) {
                            newMsgs.add(msg);
                        }
                    }
                    EventBus.getDefault().post(new OfflineMessageEvent(InternalConversation.this, newMsgs));
                }
                if (!oldList.isEmpty()) {
                    EventBus.getDefault().post(new ConversationRefreshEvent(InternalConversation.this, ConversationRefreshEvent.Reason.MSG_ROAMING_COMPLETE));
                }
                return null;
            }
        });
        return createdMsgs;
    }

    private InternalMessage createMessage(MessageDirect direct, MessageContent content,
                                          String fromName, String fromID, String fromAppkey,
                                          long createTime, long msgId, String protocol, List<Long> atList) {
        //获取targetName
        if (TextUtils.isEmpty(targetName)) {
            targetName = getTargetName();
        }
        InternalMessage msg = new InternalMessage(direct, content, fromID, fromAppkey, fromName, targetId, targetAppKey, targetName, type, atList);
        msg.setVersion(Consts.PROTOCOL_VERSION_CODE);
        msg.setServerMessageId(msgId);
        msg.setOriginMeta(protocol);
        if (0 == createTime) {
            msg.setCreateTime(CommonUtils.getFixedTime());
        } else {
            msg.setCreateTime(createTime);
        }

        Object targetInfo = getTargetInfo();
        msg.setTargetInfo(targetInfo);
        return msg;
    }

    private Message addAndUpdateLatest(InternalMessage msg) {
        //这里要使用Sync更新数据库，修复收到消息时，通知栏上msg对象id是0的问题
        MessageStorage.insertSync(msg, msgTableName);
        //只有在需要更新最新消息而且最新的消息创建时间比原本的最新消息创建时间晚才更新最新消息
        if (msg.getCreateTime() >= lastMsgDate) {
            ConversationManager.getInstance().updateLatestMsg(type, targetId, targetAppKey, msg);
        }
        return msg;
    }

    private Task<Void> addAndUpdateLatestInBatch(long uid, List<InternalMessage> msgs, InternalMessage latestMsg) {
        Task<Void> task = MessageStorage.insertInTransaction(uid, msgs, msgTableName);
        //只有在需要更新最新消息而且最新的消息创建时间比原本的最新消息创建时间晚才更新最新消息
        if (latestMsg.getCreateTime() >= lastMsgDate) {
            ConversationManager.getInstance().updateLatestMsg(type, targetId, targetAppKey, latestMsg);
        }
        return task;
    }

    public boolean updateMessageContent(Message msg, MessageContent content) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageContent", null)) {
            return false;
        }
        try {
            if (null == msg || null == content) {
                Logger.ee(TAG, "[updateMessageContent] invalid parameters! message = "
                        + msg + " content = " + content);
                return false;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.setContentType(content.getContentType());
            internalMessage.setMsgType(content.getContentType().toString());
            internalMessage.setContent(content);
            return MessageStorage.updateMessageContentSync(msgTableName, msg.getId(), content);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateMessageContentInBackground(Message msg, MessageContent content) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageContent", null)) {
            return;
        }
        try {
            if (null == msg || null == content) {
                Logger.ee(TAG, "[updateMessageContent] invalid parameters! message = "
                        + msg + " content = " + content);
                return;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.setContent(content);
            MessageStorage.updateMessageContentInBackground(msgTableName, msg.getId(), content);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public boolean updateMessageStatus(Message msg, MessageStatus status) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageStatus", null)) {
            return false;
        }
        try {
            if (null == msg || null == status) {
                Logger.ee(TAG, "[updateMessageStatus] invalid parameters! message = "
                        + msg + " status = " + status);
                return false;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.setStatus(status);
            return MessageStorage.updateMessageStatusSync(msgTableName, msg.getId(), status);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void updateMessageStatusInBackground(Message msg, MessageStatus status) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageStatus", null)) {
            return;
        }
        try {
            if (null == msg || null == status) {
                Logger.ee(TAG, "[updateMessageStatus] invalid parameters! message = "
                        + msg + " status = " + status);
                return;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.setStatus(status);
            MessageStorage.updateMessageStatusInBackground(msgTableName, msg.getId(), status);
        } catch (SQLiteException e) {
            e.printStackTrace();
        }
    }

    public boolean updateMessageServerMsgId(Message msg, Long serverMsgId) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageServerMsgId", null)) {
            return false;
        }
        try {
            if (null == msg || 0 >= serverMsgId) {
                Logger.ee(TAG, "[updateMessageStatus] invalid parameters! message = "
                        + msg + " server msg id = " + serverMsgId);
                return false;
            }
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.setServerMessageId(serverMsgId);
            return MessageStorage.updateMessageServerMsgIdSync(msgTableName, msg.getId(), serverMsgId);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMessageTimestamp(Message msg, long timestamp) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageTimestamp", null)) {
            return false;
        }
        try {
            InternalMessage internalMessage = (InternalMessage) msg;
            internalMessage.setCreateTime(timestamp);
            return MessageStorage.updateMessageTimestampSync(msgTableName, msg.getId(), timestamp);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateMessageUnreceiptCnt(Message msg, int unreceiptCnt, long unreceiptMtime) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageUnreceiptCnt", null)) {
            return false;
        }

        InternalMessage internalMessage = (InternalMessage) msg;
        if (unreceiptMtime > internalMessage.getUnreceiptMtime()) {
            internalMessage.setUnreceiptCnt(unreceiptCnt);
            internalMessage.setUnreceiptMtime(unreceiptMtime);
            return updateMessageUnreceiptCnt(msg.getServerMessageId(), unreceiptCnt, unreceiptMtime);
        }
        return false;
    }

    private boolean updateMessageUnreceiptCnt(long serverMsgID, int unreceiptCnt, long unreceiptMtime) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageUnreceiptCnt", null)) {
            return false;
        }
        try {
            return MessageStorage.updateMessageUnreceiptMtimeSync(msgTableName, serverMsgID, unreceiptCnt, unreceiptMtime);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Task<Void> updateMessageUnreceiptCntInBatch(Collection<Receipt.MsgReceiptMeta> msgReceiptMetas) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageUnreceiptCntInBatch", null)) {
            return null;
        }
        for (Receipt.MsgReceiptMeta meta : msgReceiptMetas) {
            InternalMessage msg = (InternalMessage) getLatestMessage();
            if (msg != null && meta.getMsgid() == msg.getServerMessageId()) {
                msg.setUnreceiptCnt(meta.getUnreadCount());//更新会话中缓存的latestMsg的unread cnt和mtime
                msg.setUnreceiptMtime(meta.getMtime());
            }
        }
        try {
            return MessageStorage.updateMessageUnreceiptMtimeInBatch(msgTableName, msgReceiptMetas).continueWith(new Continuation<List<MessageReceiptStatusChangeEvent.MessageReceiptMeta>, Void>() {
                @Override
                public Void then(Task<List<MessageReceiptStatusChangeEvent.MessageReceiptMeta>> task) throws Exception {
                    List<MessageReceiptStatusChangeEvent.MessageReceiptMeta> metas = task.getResult();
                    if (null != metas && !metas.isEmpty()) {
                        //上抛未回执数更新事件
                        EventBus.getDefault().post(new MessageReceiptStatusChangeEvent(InternalConversation.this, metas));
                    }
                    return null;
                }
            });
        } catch (SQLiteException e) {
            e.printStackTrace();
            return null;
        }
    }

    public boolean updateMessageHaveReadStateInBatch(Collection<Long> serverMsgIds) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageHaveReadStateInBatch", null)) {
            return false;
        }

        for (Long serverMsgId : serverMsgIds) {
            InternalMessage msg = (InternalMessage) getLatestMessage();
            if (null != msg && serverMsgId.equals(msg.getServerMessageId())) {
                msg.setHaveRead(1);//更新会话中缓存的latestMsg的haveRead字段
            }
        }
        try {
            return MessageStorage.setMessageHaveReadInBatch(msgTableName, serverMsgIds);
        } catch (SQLiteException e) {
            e.printStackTrace();
            return false;
        }
    }

    public Collection<Long> removeDuplicatesWithOnlineList(Collection<Long> msgIdList) {
        if (null == msgIdList || msgIdList.isEmpty()) {
            Logger.d(TAG, "msg id list is empty, return from remove duplicates");
            return msgIdList;
        }
        if (null == onlineMsgList) {
            onlineMsgList = OnlineMsgRecvStorage.queryAllSync(onlineMsgTableName);
        }

        Logger.d(TAG, " conv " + targetId + " online msg list = " + onlineMsgList);
        msgIdList.removeAll(onlineMsgList);
        return msgIdList;
    }

    public Task<Long> insertToOnlineMsgTable(long serverMsgId, long cTime) {
        if (null == onlineMsgList) {
            onlineMsgList = OnlineMsgRecvStorage.queryAllSync(onlineMsgTableName);
        }
        CommonUtils.trimListSize(onlineMsgList, onlineMsgTableName);
        boolean needInsertToDB = onlineMsgList.add(serverMsgId);
        return needInsertToDB ? OnlineMsgRecvStorage.insertInBackground(serverMsgId, cTime, onlineMsgTableName) : null;
    }

    private Task<Void> insertToOnlineMsgTable(final Collection<InternalMessage> messages) {
        if (null == messages || messages.isEmpty()) {
            return Task.forResult(null);
        }
        if (null == onlineMsgList) {
            onlineMsgList = OnlineMsgRecvStorage.queryAllSync(onlineMsgTableName);
        }
        //首先trim一下conv对象中缓存的onlineMsgList，避免onlineMsgList过长导致去重效率慢。
        CommonUtils.trimListSize(onlineMsgList, onlineMsgTableName);
        return OnlineMsgRecvStorage.insertInBatch(this, messages, onlineMsgTableName);
    }

    public void addServerMsgIdToList(long serverMsgId) {
        onlineMsgList.add(serverMsgId);
    }

    private void getUserInfoFromServer() {
        new GetUserInfoTask(targetId, targetAppKey, new GetUserInfoCallback(false) {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                if (0 == responseCode) {
                    InternalUserInfo internalUserInfo = (InternalUserInfo) targetInfo;
                    internalUserInfo.copyUserInfo((InternalUserInfo) info, false, false, false);
                } else {
                    Logger.ww(TAG, "get userInfo failed .");
                }
            }
        }, true, false).execute();
    }

    private void getGroupInfoFromServer() {
        new GetGroupInfoTask(Long.parseLong(targetId), new GetGroupInfoCallback(false) {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                if (0 == responseCode) {
                    InternalGroupInfo internalGroupInfo = (InternalGroupInfo) targetInfo;
                    internalGroupInfo.copyGroupInfo((InternalGroupInfo) groupInfo, false);
                } else {
                    Logger.ww(TAG, "get groupInfo failed .");
                }
            }
        }, false).execute();
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "type=" + type +
                ", targetId='" + targetId + '\'' +
                ", latestText='" + latestText + '\'' +
                ", latestType=" + latestType +
                ", lastMsgDate=" + lastMsgDate +
                ", unReadMsgCnt=" + unReadMsgCnt +
                ", msgTableName='" + msgTableName + '\'' +
                ", targetAppkey='" + targetAppKey + '\'' +
                ", extra='" + extra + '\'' +
                '}';
    }
}
