package cn.jpush.im.android.storage;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.event.ConversationRefreshEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalEventNotificationContent;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.storage.table.ConversationTable;
import cn.jpush.im.android.storage.table.MessageTable;
import cn.jpush.im.android.storage.table.OnlineRecvMsgTable;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

/**
 * 直接操作Conversation数据库的类。<br/>
 * <p/>
 * Note:此类中的方法都为protected，必须通过{@link ConversationManager}类来操作和管理Conversation数据库。
 */
public class ConversationStorage {
    private static final String TAG = "ConversationStorage";

    public static final String PREFIX_MSG_TABLE_NAME = "msg";

    public static final String PREFIX_ONLINE_MSG_TABLE_NAME = "online";

    public static final String PREFIX_ONLINE_EVENT_TABLE_NAME = "event";

    public static final String ID = "id";

    public static final String TYPE = "type";

    public static final String TARGET_ID = "target_id";

    public static final String EXTRA = "extra";

    //单聊时是对方用户的appkey,群聊时为空字符串
    public static final String TARGET_APPKEY = "target_appkey";

    public static final String LATEST_TYPE = "latest_type";

    public static final String LATEST_TEXT = "latest_text";

    public static final String LATEST_DATE = "latest_date";

    public static final String UNREAD_CNT = "unread_cnt";

    public static final String UNREAD_CNT_MTIME = "unread_cnt_mtime";

    public static final String MSG_TABLE_NAME = "msg_table_name";

    public static final String TITLE = "title";

    public static final String AVATAR = "avatar";//不同于userinfo和groupinfo的avatar字段，conversation avatar这里存的是头像路径，而不是mediaID。

    final private static Object createConvLock = new Object();

    protected static InternalConversation createConversation(ConversationType type,
                                                             String targetID, String targetAppkey) {
        return createConversation(type, targetID, targetAppkey, null, CommonUtils.getFixedTime(), true);
    }

    protected static InternalConversation createConversation(ConversationType type,
                                                             String targetID, String targetAppkey, String expectedTitle, long expectedLatestMsgDate, boolean saveInBackground) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("createConversation", null)) {
            return null;
        }

        if (null == type || TextUtils.isEmpty(targetID)) {
            Logger.ee(TAG, "[createConversation] invalid parameters type = " + type + " targetId = "
                    + targetID);
            return null;
        } else if (type == ConversationType.group) {
            try {
                //群聊下，先验证targetID是否可以被转成long型，如果报错，直接跳出创建会话逻辑。
                Long.parseLong(targetID);
            } catch (NumberFormatException e) {
                Logger.ee(TAG, "target id in invalid, return from createConversation.", e);
                return null;
            }
        }
        if (ConversationType.single == type && TextUtils.isEmpty(targetAppkey)) {
            targetAppkey = JCoreInterface.getAppKey();
        }
        InternalConversation conv;
        synchronized (createConvLock) {
            conv = ConversationManager.getInstance().getConversation(type, targetID, targetAppkey);
            if (null == conv) {
                conv = new InternalConversation();
                conv.setType(type);
                conv.setTargetId(targetID);
                conv.setLastMsgDate(expectedLatestMsgDate);
                conv.setTargetAppKey(targetAppkey);

                String tableNameSuffix = tableSuffixGenerator(conv.getTargetId(), conv.getTargetAppKey());
                String onlineMsgTableNameSuffix = tableSuffixGenerator(conv.getTargetId(), conv.getTargetAppKey());
                conv.setMsgTableName(PREFIX_MSG_TABLE_NAME + tableNameSuffix);
                conv.setOnlineMsgTableName(PREFIX_ONLINE_MSG_TABLE_NAME + onlineMsgTableNameSuffix);
                Logger.d(TAG, "create conversation . online msg table name is = " + conv.getOnlineMsgTableName());
                CRUDMethods.execSQLSync(MessageTable.getCreateSQL(conv.getMsgTableName()), null);
                CRUDMethods.execSQLSync(OnlineRecvMsgTable.getCreateSQL(conv.getOnlineMsgTableName()), null);

                setDefaultTitleAndAvatar(conv, expectedTitle);

                if (ConversationType.group == conv.getType()) {
                    long groupID = Long.parseLong(conv.getTargetId());
                    //如果是群聊会话，先检查本地是否有已缓存的事件
                    if (EventStorage.queryExistsSync(groupID)) {
                        InternalEventNotificationContent eventNotificationContent = EventStorage.querySync(groupID);
                        //在会话中创建一条event消息
                        long cTime = EventStorage.queryCreateTimeSync(groupID) * 1000;
                        Message eventMsg = conv.createReceiveMessage(
                                eventNotificationContent, "", 0, "系统消息", "", cTime, 0, "", null, true);
                        //删除本地缓存事件
                        if (null != eventMsg) {
                            EventStorage.deleteInBackground(groupID);
                        }
                    }
                }

                //将conversation插入到到本地数据库
                if (saveInBackground) {
                    insertOrUpdateConversationInBackground(conv);
                } else {
                    insertOrUpdateConversationSync(conv);
                }
            }
        }
        return conv;
    }

    private static void setDefaultTitleAndAvatar(InternalConversation conv, String expectedTitle) {
        ConversationType type = conv.getType();
        String targetID = conv.getTargetId();
        String targetAppkey = conv.getTargetAppKey();
        File avatarFile = null;
        if (ConversationType.single == type) {
            UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(targetID, targetAppkey);
            if (null != userInfo) {
                avatarFile = userInfo.getAvatarFile();
                //设置title
                if (!TextUtils.isEmpty(expectedTitle)) {
                    Logger.d(TAG, "create conversation title is expected! title = " + expectedTitle);
                    conv.setTitle(expectedTitle);
                } else if (!TextUtils.isEmpty(userInfo.getNotename())) {
                    Logger.d(TAG, "create conversation note is " + userInfo.getNotename());
                    conv.setTitle(userInfo.getNotename());
                } else if (!TextUtils.isEmpty(userInfo.getNickname())) {
                    Logger.d(TAG, "create conversation nickname is " + userInfo.getNickname());
                    conv.setTitle(userInfo.getNickname());
                } else {
                    Logger.d(TAG, "create conversation nickname is empty !");
                    conv.setTitle(targetID);
                }
            } else {
                if (!TextUtils.isEmpty(expectedTitle)) {
                    Logger.d(TAG, "create conversation title is expected! title = " + expectedTitle);
                    conv.setTitle(expectedTitle);
                } else {
                    //userinfo 为空，直接使用targetID作为title
                    Logger.d(TAG, "create conversation userinfo is null!!");
                    conv.setTitle(targetID);
                }
            }

        } else if (ConversationType.group == type) {
            long groupID = Long.parseLong(conv.getTargetId());
            GroupInfo groupInfo = GroupStorage.queryInfoSync(groupID);
            avatarFile = null == groupInfo ? null : groupInfo.getAvatarFile();
            //设置群组的title
            if (!TextUtils.isEmpty(expectedTitle)) {
                Logger.d(TAG, "create conversation title is expected! title = " + expectedTitle);
                conv.setTitle(expectedTitle);
            } else if (null != groupInfo && !TextUtils.isEmpty(groupInfo.getGroupName())) {
                Logger.d(TAG, "create group conversation group name is " + groupInfo.getGroupName());
                conv.setTitle(String.valueOf(groupInfo.getGroupName()));
            } else if (null != groupInfo) {
                //群名称为空，则使用群成员昵称拼接成title
                Logger.d(TAG, "create group conversation group name is empty !");
                conv.setTitle(GroupStorage.getGroupDefaultTitle(groupID, expectedTitle));
            } else {
                //groupinfo 为空,则使用targetID作为title
                Logger.d(TAG, "create group conversation group info is empty!!");
                conv.setTitle(targetID);
            }
        }

        if (null != avatarFile && avatarFile.exists()) {
            Logger.d(TAG, "create conversation avatar is " + avatarFile.getAbsolutePath());
            conv.setAvatarPath(avatarFile.getAbsolutePath());
        }
    }

    protected static InternalConversation cursorToConversation(Cursor cursor) {
        if (null == cursor) {
            return null;
        }

        String targetID = cursor.getString(cursor.getColumnIndex(TARGET_ID));
        ConversationType conversationType = ConversationType.valueOf(cursor.getString(cursor.getColumnIndex(TYPE)));
        InternalConversation conv = new InternalConversation();
        conv.setId(cursor.getString(cursor.getColumnIndex(ID)));
        conv.setTargetId(targetID);
        conv.setType(conversationType);
        conv.setLatestType(
                ContentType.valueOf(cursor.getString(cursor.getColumnIndex(LATEST_TYPE))));
        conv.setLatestText(cursor.getString(cursor.getColumnIndex(LATEST_TEXT)));
        conv.setLastMsgDate(cursor.getLong(cursor.getColumnIndex(LATEST_DATE)));
        conv.setUnReadMsgCnt(cursor.getInt(cursor.getColumnIndex(UNREAD_CNT)));
        String msgTableName = cursor.getString(cursor.getColumnIndex(MSG_TABLE_NAME));
        conv.setMsgTableName(msgTableName);
        conv.setExtra(cursor.getString(cursor.getColumnIndex(EXTRA)));

        setDefaultTitleAndAvatar(conv, null);//每次从数据库中读出数据到内存时，都需要重新确定下会话的title,防止出现当title是由群成员拼接而成时，未及时更新的问题。
//        String title = cursor.getString(cursor.getColumnIndex(TITLE));
//        if (TextUtils.isEmpty(title)) {
//            //如果title是空，说明是从低版本数据库升级上来的，需要给一个初始化数据。
//            setDefaultTitleAndAvatar(conv, null);
//        } else {
//            conv.setTitle(title);
//            conv.setAvatarPath(avatarPath);
//        }
        String targetAppKey = cursor.getString(cursor.getColumnIndex(TARGET_APPKEY));
        if (TextUtils.isEmpty(targetAppKey) && conversationType == ConversationType.single) {
            //如果target appkey 为空而且是单聊，则给一个默认值
            targetAppKey = JCoreInterface.getAppKey();
            conv.setTargetAppKey(JCoreInterface.getAppKey());
            //同时给数据库appkey字段设置一个默认值
            setDefaultAppkeyToDatabaseInBackground(targetID);
        } else {
            conv.setTargetAppKey(targetAppKey);
        }
        if (conversationType == ConversationType.group) {
            long groupID = safetyParseLong(targetID);
            if (-1 != groupID && !GroupStorage.isExistSync(groupID)) {
                Logger.d(TAG, "group info is null ! get group info and call refresh!");
                getGroupInfoAndCallRefresh(groupID);
            }
        } else if (!UserInfoStorage.queryExistSync(targetID, targetAppKey)) {
            Logger.d(TAG, "user info is null ! get user info and call refresh!");
            getUserInfoAndCallRefresh(targetID, targetAppKey);
        }

        String onlineMsgTableNameSuffix = tableSuffixGenerator(conv.getTargetId(), conv.getTargetAppKey());
        conv.setOnlineMsgTableName(PREFIX_ONLINE_MSG_TABLE_NAME + onlineMsgTableNameSuffix);
        conv.setUnreadCntMtime(cursor.getLong(cursor.getColumnIndex(UNREAD_CNT_MTIME)));
        Logger.d(TAG, "cursor to conversation . online msg table name is = " + conv.getOnlineMsgTableName());
        Logger.d(TAG, "cursor to conversation . conv = " + conv);
        return conv;
    }

    private static long safetyParseLong(String longString) {
        long result;
        try {
            if (!TextUtils.isEmpty(longString)) {
                result = Long.parseLong(longString);
            } else {
                Logger.ee(TAG, "string is empty when parse to Long , string = " + longString);
                return -1;
            }
        } catch (NumberFormatException e) {
            Logger.ee(TAG, "JMessage catch a number format exception,maybe your conversation's target_id is 'String' while conversation_type is 'group'.");
            return -1;
        }
        return result;
    }

    private static void getUserInfoAndCallRefresh(final String userName, final String appkey) {
        JMessageClient.getUserInfo(userName, appkey, new GetUserInfoCallback(false) {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                Logger.d(TAG, "get user info finished ! response code = " + responseCode);
                if (0 == responseCode) {
                    Conversation conversation = getConversationSync(ConversationType.single, info.getUserName(), appkey);
                    ConversationRefreshEvent conversationRefreshEvent = new ConversationRefreshEvent(conversation, ConversationRefreshEvent.Reason.CONVERSATION_INFO_UPDATED);
                    EventBus.getDefault().post(conversationRefreshEvent);
                }
            }
        });
    }

    private static void getGroupInfoAndCallRefresh(long groupID) {
        JMessageClient.getGroupInfo(groupID, new GetGroupInfoCallback(false) {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo info) {
                if (0 == responseCode) {
                    Conversation conversation = getConversationSync(ConversationType.group, info.getGroupID());
                    ConversationRefreshEvent conversationRefreshEvent = new ConversationRefreshEvent(conversation, ConversationRefreshEvent.Reason.CONVERSATION_INFO_UPDATED);
                    EventBus.getDefault().post(conversationRefreshEvent);
                }
            }
        });
    }

    protected static Task<Integer> queryUnreadCntInBackground(ConversationType type, String targetId, String targetAppkey) {

        return CRUDMethods.queryAsync(ConversationTable.CONVERSATION_TABLE_NAME, new String[]{UNREAD_CNT},
                createQuerySelections(type),
                createQuerySelectionArgs(type, targetId, targetAppkey), null, null, null, null).onSuccess(new Continuation<Cursor, Integer>() {
            @Override
            public Integer then(Task<Cursor> task) throws Exception {
                return queryUnreadCntInternal(task.getResult());
            }
        });

    }

    protected static int queryUnreadCntSync(ConversationType type, String targetId, String targetAppkey) {

        Cursor cursor = CRUDMethods.querySync(ConversationTable.CONVERSATION_TABLE_NAME, new String[]{UNREAD_CNT},
                createQuerySelections(type),
                createQuerySelectionArgs(type, targetId, targetAppkey), null, null, null, null);

        return queryUnreadCntInternal(cursor);
    }

    private static int queryUnreadCntInternal(Cursor cursor) {
        int unreadCnt = 0;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    unreadCnt = cursor.getInt(cursor.getColumnIndex(UNREAD_CNT));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return unreadCnt;
    }

    protected static Task<Boolean> insertOrUpdateConversationInBackground(final InternalConversation conv) {
        final ContentValues contentValues = conversationToValues(conv);
        return queryExistInBackground(conv.getType(), conv.getTargetId(), conv.getTargetAppKey()).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.updateAsync(ConversationTable.CONVERSATION_TABLE_NAME, contentValues, ConversationStorage.ID + " = ?",
                            new String[]{conv.getId()});
                } else {
                    return CRUDMethods.insertAsync(ConversationTable.CONVERSATION_TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_IGNORE)
                            .onSuccess(new Continuation<Long, Boolean>() {
                                @Override
                                public Boolean then(Task<Long> task) throws Exception {
                                    return task.getResult() > 0;
                                }
                            });
                }

            }
        });
    }

    protected static boolean insertOrUpdateConversationSync(final InternalConversation conv) {
        final ContentValues contentValues = conversationToValues(conv);
        boolean exist = queryExistSync(conv.getType(), conv.getTargetId(), conv.getTargetAppKey());
        if (exist) {
            return CRUDMethods.updateSync(ConversationTable.CONVERSATION_TABLE_NAME, contentValues, ConversationStorage.ID + " = ?",
                    new String[]{conv.getId()});
        } else {
            return CRUDMethods.insertSync(ConversationTable.CONVERSATION_TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_IGNORE) > 0;
        }
    }

    private static ContentValues conversationToValues(InternalConversation conv) {
        final ContentValues values = new ContentValues();
        values.put(ConversationStorage.ID, conv.getId());
        values.put(ConversationStorage.TYPE, conv.getType().toString());
        values.put(ConversationStorage.TARGET_ID, conv.getTargetId());
        values.put(ConversationStorage.UNREAD_CNT, conv.getUnReadMsgCnt());
        values.put(ConversationStorage.MSG_TABLE_NAME, conv.getMsgTableName());
        values.put(TITLE, conv.getTitle());
        values.put(TARGET_APPKEY, conv.getTargetAppKey());
        values.put(UNREAD_CNT_MTIME, conv.getUnreadCntMtime());
        values.put(ConversationStorage.EXTRA, conv.getExtra());
        File file = conv.getAvatarFile();
        if (null != file && file.exists()) {
            values.put(AVATAR, file.getAbsolutePath());
        }
        InternalMessage latestMsg = (InternalMessage) conv.getLatestMessage();
        if (null != latestMsg) {
            values.put(ConversationStorage.LATEST_TYPE, latestMsg.getContentType().toString());
            if (latestMsg.getContentType().compareTo(ContentType.text) == 0)
            // 如果是文字类消息，则将最近消息内容设置为message的text
            {
                TextContent content = (TextContent) latestMsg.getContent();
                values.put(ConversationStorage.LATEST_TEXT, content.getText());
            } else
            // 如果不是文字类消息，则将最近消息内容设为空，客户端根据需要设置应该显示的内容。
            {
                values.put(ConversationStorage.LATEST_TEXT, "");
            }
            values.put(ConversationStorage.LATEST_DATE, latestMsg.getCreateTime());
        } else {
            // there is no message in this conversation yet,set default
            values.put(ConversationStorage.LATEST_TYPE, ContentType.text.toString());
            values.put(ConversationStorage.LATEST_TEXT, "");
            values.put(ConversationStorage.LATEST_DATE, conv.getLastMsgDate());
        }
        return values;
    }

    protected static boolean updateConversationExtraSync(ConversationType type, String targetId, String targetAppkey, String extra) {
        ContentValues values = new ContentValues();
        values.put(EXTRA, extra);
        return updateSync(type, targetId, targetAppkey, values);
    }

    protected static Task<Boolean> updateLatestMsgInBackground(ConversationType type, String targetId, String targetAppkey, Message msg) {
        ContentValues values = new ContentValues();

        if (null != msg) {
            values.put(ConversationStorage.LATEST_TYPE, msg.getContentType().toString());
            String latestMsgText;
            if (ContentType.text == msg.getContentType()) {
                TextContent content = (TextContent) msg.getContent();
                latestMsgText = content.getText();
            } else if (ContentType.prompt == msg.getContentType()) {
                PromptContent content = (PromptContent) msg.getContent();
                latestMsgText = content.getPromptText();
            } else {
                latestMsgText = "";
            }
            values.put(ConversationStorage.LATEST_TEXT, latestMsgText);
            values.put(ConversationStorage.LATEST_DATE, msg.getCreateTime());
        } else {
            values.put(ConversationStorage.LATEST_TYPE, ContentType.text.toString());
            values.put(ConversationStorage.LATEST_TEXT, "");
        }
        return updateInBackground(type, targetId, targetAppkey, values);
    }

    protected static Task<Boolean> updateAvatarInBackground(ConversationType type, String targetId, String targetAppkey, String avatar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(AVATAR, avatar);
        return updateInBackground(type, targetId, targetAppkey, contentValues);
    }

    protected static Task<Boolean> updateTitleInBackground(ConversationType type, String targetId, String targetAppkey, String title) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(TITLE, title);
        return updateInBackground(type, targetId, targetAppkey, contentValues);
    }

    protected static Task<Boolean> resetUnreadCountInBackground(ConversationType type, String targetId, String targetAppkey) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(UNREAD_CNT, 0);
        return updateInBackground(type, targetId, targetAppkey, contentValues);
    }

    protected static Task<Boolean> updateInBackground(ConversationType type, String targetId, String targetAppkey, ContentValues values) {
        if (!CommonUtils.isInited("updateInBackground")) {
            return Task.forResult(false);
        }
        if (null == targetId || null == values) {
            Logger.ww(TAG, "updateInBackground conversation failed . type = " + type + " target = " + targetId);
            return Task.forResult(false);
        }

        return CRUDMethods.updateAsync(ConversationTable.CONVERSATION_TABLE_NAME, values,
                createQuerySelections(type), createQuerySelectionArgs(type, targetId, targetAppkey));

    }

    protected static boolean updateSync(ConversationType type, String targetId, String targetAppkey, ContentValues values) {
        if (!CommonUtils.isInited("updateSync")) {
            return false;
        }
        if (null == targetId || null == values) {
            Logger.ww(TAG, "updateSync conversation failed . type = " + type + " target = " + targetId);
            return false;
        }

        Logger.d(TAG, "update conversation sync " + values);
        return CRUDMethods.updateSync(ConversationTable.CONVERSATION_TABLE_NAME, values,
                createQuerySelections(type), createQuerySelectionArgs(type, targetId, targetAppkey));

    }

    static Task<Boolean> deleteConversationInBackground(final ConversationType type, final String targetId, final String targetAppkey, final String msgTableName) {
        if (null == type || null == targetId) {
            Logger.ww(TAG, "deleteConversationInBackground failed . type = " + type + " target = " + targetId + " targetAppkey = " + targetAppkey);
            return Task.forResult(false);
        }
        return CRUDMethods.deleteAsync(ConversationTable.CONVERSATION_TABLE_NAME,
                createQuerySelections(type), createQuerySelectionArgs(type, targetId, targetAppkey)).onSuccess(new Continuation<Boolean, Boolean>() {
            @Override
            public Boolean then(Task<Boolean> task) throws Exception {
                if (task.getResult() && null != msgTableName) {
                    //会话删除成功后，要删掉对应的消息表。
                    MessageStorage.dropTableSync(msgTableName);
                }
                return true;
            }
        });

    }

    private static InternalConversation getConversationSync(String selection, String[] selectionArgs) {
        if (!CommonUtils.isInited("getConversationInBackground")) {
            return null;
        }

        Cursor cursor = CRUDMethods.querySync(ConversationTable.CONVERSATION_TABLE_NAME, null,
                selection, selectionArgs, null, null, "latest_date asc", null);
        return getConversationInternal(cursor);
    }

    private static InternalConversation getConversationInternal(Cursor cursor) {
        InternalConversation conv = null;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToFirst()) {
                    conv = cursorToConversation(cursor);
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return conv;
    }

    protected static Task<InternalConversation> getConversationInBackground(ConversationType type, long groupId) {
        if (null == type || 0 == groupId) {
            return Task.forResult(null);
        }
        return getConversationInBackground(StringUtils.createSelectionWithAnd(TYPE, TARGET_ID),
                new String[]{type.toString(), String.valueOf(groupId)});
    }

    protected static InternalConversation getConversationSync(ConversationType type, long groupId) {
        if (null == type || 0 == groupId) {
            return null;
        }
        return getConversationSync(StringUtils.createSelectionWithAnd(TYPE, TARGET_ID),
                new String[]{type.toString(), String.valueOf(groupId)});
    }

    //防止username和groupID重名，所以需要用type + target联合查找
    protected static Task<InternalConversation> getConversationInBackground(ConversationType type, String targetId, String targetAppkey) {
        if (null == type || null == targetId) {
            Logger.ww(TAG, "get conversation failed . type = " + type + " targetId = " + targetId + " target appkey = " + targetAppkey);
            return Task.forResult(null);
        }
        return getConversationInBackground(createQuerySelections(type), createQuerySelectionArgs(type, targetId, targetAppkey));
    }

    protected static InternalConversation getConversationSync(ConversationType type, String targetId, String targetAppkey) {
        if (null == type || null == targetId) {
            Logger.ww(TAG, "get conversation failed . type = " + type + " targetId = " + targetId + " target appkey = " + targetAppkey);
            return null;
        }
        return getConversationSync(createQuerySelections(type), createQuerySelectionArgs(type, targetId, targetAppkey));
    }

    private static Task<InternalConversation> getConversationInBackground(String selection, String[] selectionArgs) {
        if (!CommonUtils.isInited("getConversationInBackground")) {
            return Task.forResult(null);
        }

        return CRUDMethods.queryAsync(ConversationTable.CONVERSATION_TABLE_NAME, null,
                selection, selectionArgs, null, null, "latest_date asc", null).onSuccess(new Continuation<Cursor, InternalConversation>() {
            @Override
            public InternalConversation then(Task<Cursor> task) throws Exception {
                return getConversationInternal(task.getResult());
            }
        });

    }

    protected static Task<List<InternalConversation>> getConversationListWithoutGivenListInBackground(String sqlString) {
        if (TextUtils.isEmpty(sqlString)) {
            //传进来的sqlString是空，则直接拿全量列表
            return getConversationListInBackground(new ArrayList<InternalConversation>());
        }
        return CRUDMethods.rawQueryAsync(sqlString, null).onSuccess(new Continuation<Cursor, List<InternalConversation>>() {
            @Override
            public List<InternalConversation> then(Task<Cursor> task) throws Exception {
                return getConversationListInternal(task.getResult());
            }
        });
    }

    protected static List<InternalConversation> getConversationListWithoutGivenListSync(String sqlString) {
        if (TextUtils.isEmpty(sqlString)) {
            //传进来的sqlString是空，则直接拿全量列表
            return getConversationListSync(new ArrayList<InternalConversation>());
        }
        return getConversationListInternal(CRUDMethods.rawQuerySync(sqlString, null));
    }

    private static List<InternalConversation> getConversationListInternal(Cursor cursor) {
        List<InternalConversation> list = new ArrayList<InternalConversation>();
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    list.add(cursorToConversation(cursor));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    protected static <T extends Conversation> Task<List<T>> getConversationListInBackground(final List<T> list) {
        if (null == list) {
            return Task.forResult(null);
        }

        return CRUDMethods.queryAsync(ConversationTable.CONVERSATION_TABLE_NAME, null, null, null, null, null,
                "latest_date asc", null).onSuccess(new Continuation<Cursor, List<T>>() {
            @Override
            public List<T> then(Task<Cursor> task) throws Exception {
                return getConversationListInternal(task.getResult(), list);
            }
        });
    }

    protected static <T extends Conversation> List<T> getConversationListSync(final List<T> list) {
        if (null == list) {
            return null;
        }

        Cursor cursor = CRUDMethods.querySync(ConversationTable.CONVERSATION_TABLE_NAME, null, null, null, null, null,
                "latest_date asc", null);
        return getConversationListInternal(cursor, list);
    }

    private static <T extends Conversation> List<T> getConversationListInternal(Cursor cursor, List<T> list) {
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    list.add((T) cursorToConversation(cursor));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return list;
    }

    public static Task<List<String>> getAllMessageTableNameInBackground() {

        return CRUDMethods.queryAsync(ConversationTable.CONVERSATION_TABLE_NAME, new String[]{MSG_TABLE_NAME}, null, null, null, null, null, null).onSuccess(new Continuation<Cursor, List<String>>() {
            @Override
            public List<String> then(Task<Cursor> task) throws Exception {
                return getAllMessageTableInternal(task.getResult());
            }
        });

    }

    public static List<String> getAllMessageTableNameSync() {
        Cursor cursor = CRUDMethods.querySync(ConversationTable.CONVERSATION_TABLE_NAME, new String[]{MSG_TABLE_NAME}, null, null, null, null, null, null);
        return getAllMessageTableInternal(cursor);
    }

    private static List<String> getAllMessageTableInternal(Cursor cursor) {
        List<String> msgTableNames = new ArrayList<String>();
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    msgTableNames.add(cursor.getString(cursor.getColumnIndex(MSG_TABLE_NAME)));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return msgTableNames;
    }

    public static Task<Boolean> queryExistInBackground(final ConversationType type, String targetId, String targetAppkey) {
        if (!CommonUtils.isInited("Conversation.queryExistInBackground")) {
            return Task.forResult(false);
        }
        if (null == type || null == targetId) {
            Logger.ww(TAG, "is exist failed . type = " + type + " targetId = " + targetId + " target appkey = " + targetAppkey);
            return Task.forResult(false);
        }

        return queryCountAsync(createQuerySelections(type), createQuerySelectionArgs(type, targetId, targetAppkey))
                .onSuccess(new Continuation<Integer, Boolean>() {
                    @Override
                    public Boolean then(Task<Integer> task) throws Exception {
                        Logger.d(TAG, "query exist result is " + (0 < task.getResult()));
                        return 0 < task.getResult();
                    }
                });

    }

    public static boolean queryExistSync(ConversationType type, String targetId, String targetAppkey) {
        if (!CommonUtils.isInited("Conversation.queryExistSync")) {
            return false;
        }
        if (null == type || null == targetId) {
            Logger.ww(TAG, "query is exist failed . type = " + type + " targetId = " + targetId + " target appkey = " + targetAppkey);
            return false;
        }

        return 0 < queryCountSync(createQuerySelections(type), createQuerySelectionArgs(type, targetId, targetAppkey));
    }

    public static int queryCountSync(String whereClause, String[] args) {
        String sql = "select count(*) as count from " + ConversationTable.CONVERSATION_TABLE_NAME;
        if (!TextUtils.isEmpty(whereClause)) {
            sql += " where " + whereClause;
        }
        return queryCountInternal(CRUDMethods.rawQuerySync(sql, args));
    }

    public static Task<Integer> queryCountAsync(String whereClause, String[] args) {
        String sql = "select count(*) as count from " + ConversationTable.CONVERSATION_TABLE_NAME;
        if (!TextUtils.isEmpty(whereClause)) {
            sql += " where " + whereClause;
        }
        return CRUDMethods.rawQueryAsync(sql, args).onSuccess(new Continuation<Cursor, Integer>() {
            @Override
            public Integer then(Task<Cursor> task) throws Exception {
                return queryCountInternal(task.getResult());
            }
        });
    }

    private static int queryCountInternal(Cursor cursor) {
        int rowCount = 0;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    rowCount = cursor.getInt(cursor.getColumnIndex("count"));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        Logger.d(TAG, "Conversation.queryCountSync  rowCount = " + rowCount);
        return rowCount;
    }

    private static String createQuerySelections(ConversationType type) {
        String selections;
        if (ConversationType.group == type) {
            selections = StringUtils.createSelectionWithAnd(TYPE, TARGET_ID);
        } else {
            selections = StringUtils.createSelectionWithAnd(TYPE, TARGET_ID, TARGET_APPKEY);
        }
        return selections;
    }

    private static String[] createQuerySelectionArgs(ConversationType type, String targetId, String targetAppkey) {
        String[] selectionArgs;
        if (ConversationType.group == type) {
            selectionArgs = new String[]{type.toString(), targetId};
        } else {
            selectionArgs = new String[]{type.toString(), targetId, targetAppkey};
        }
        return selectionArgs;
    }


    private static Task<Boolean> setDefaultAppkeyToDatabaseInBackground(String targetId) {
        if (!CommonUtils.isInited("Conversation.setDefaultAppkeyToDatabaseInBackground")) {
            return Task.forResult(false);
        }
        if (null == targetId) {
            Logger.ww(TAG, "set default appkey to database conversation failed . target = null");
            return Task.forResult(false);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(TARGET_APPKEY, JCoreInterface.getAppKey());

        return CRUDMethods.updateAsync(ConversationTable.CONVERSATION_TABLE_NAME, contentValues,
                StringUtils.createSelectionWithAnd(TYPE, TARGET_ID), new String[]{ConversationType.single.toString(), targetId});

    }

    public static String tableSuffixGenerator(String targetId, String targetAppkey) {
        Logger.d(TAG, "[tableSuffixGenerator] target id = " + targetId + " appkey = " + targetAppkey);
        Logger.d(TAG, "[tableSuffixGenerator] table suffix = " + String.valueOf(Math.abs((targetId + targetAppkey).hashCode())));
        return String.valueOf(Math.abs((targetId + targetAppkey).hashCode()));
    }
}
