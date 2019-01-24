package cn.jpush.im.android.storage;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.text.TextUtils;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.Consts;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.event.MessageReceiptStatusChangeEvent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.MessageProtocolParser;
import cn.jpush.im.android.utils.StringUtils;

public class MessageStorage {

    private static final String TAG = "MessageStorage";
    /**
     * column names in database
     **/
    //message在database中的id,注意需要和server_message_id区分开
    public static final String ID = "_id";

    public static final String AT_LIST = "at_list";

    public static final String FROM_NAME = "from_name";

    public static final String SET_FROM_NAME = "set_from_name";

    public static final String IS_MESSAGE_RETRACT = "is_message_retract";

    public static final String FROM_ID = "from_id";

    public static final String FROM_APPKEY = "from_appkey";

    public static final String DIRECT = "direct";

    public static final String CONTENT = "content";

    public static final String CONTENT_TYPE = "content_type";

    public static final String STATUS = "status";

    public static final String CREATE_TIME = "create_time";

    //message对应服务器上的唯一标示,可以用于message的全局唯一性判断。注意和message_id区分开
    public static final String SERVER_MESSAGE_ID = "server_message_id";

    public static final String ORIGIN_META = "origin_meta";

    public static final String HAVE_READ = "have_read";//since 2.3.0 DB version 12

    public static final String UNRECEIPT_COUNT = "unreceipt_count";//since 2.3.0 DB version 12

    public static final String UNRECEIPT_MTIME = "unreceipt_mtime";//since 2.3.0 DB version 12

    private static InternalMessage cursorToMessage(String msgTableName, Cursor cursor) {
        if (null == cursor) {
            return null;
        }

        String contentTypeString = cursor.getString(cursor.getColumnIndex(CONTENT_TYPE));

        long cTime = cursor.getLong(cursor.getColumnIndex(CREATE_TIME));
        long serverMsgId = cursor.getLong(cursor.getColumnIndex(SERVER_MESSAGE_ID));
        int _id = cursor.getInt(cursor.getColumnIndex(ID));
        ContentType contentType;
        InternalMessage msg;

        try {
            contentType = ContentType.valueOf(contentTypeString);
        } catch (IllegalArgumentException e) {
            //如果解析message type失败，可能是收到了不兼容的消息类型，则将type设置为unknown.
            contentType = ContentType.unknown;
        }

        if (contentType == ContentType.unknown) {
            Logger.ii(TAG, "unknown message type, try to parse again.");
            String protocol = cursor.getString(cursor.getColumnIndex(ORIGIN_META));
            MessageDirect direct = MessageDirect.valueOf(cursor.getString(cursor.getColumnIndex(DIRECT)));
            msg = MessageProtocolParser.protocolToInternalMessage(protocol, direct, cTime, serverMsgId, 0, 0, null, false);
        } else {
            msg = new InternalMessage();
            msg.setContent(MessageContent
                    .fromJson(cursor.getString(cursor.getColumnIndex(CONTENT)), contentType));
            msg.setContentType(contentType);
            msg.setMsgType(contentTypeString);
            msg.setCreateTime(cTime);
            msg.setServerMessageId(serverMsgId);
        }

        if (null != msg) {
            msg.setVersion(Consts.PROTOCOL_VERSION_CODE);
            msg.setId(_id);
            msg.setFromName(cursor.getString(cursor.getColumnIndex(FROM_NAME)));
            msg.setFromID(cursor.getString(cursor.getColumnIndex(FROM_ID)));
            msg.setDirect(MessageDirect.valueOf(cursor.getString(cursor.getColumnIndex(DIRECT))));
            msg.setStatus(MessageStatus.valueOf(cursor.getString(cursor.getColumnIndex(STATUS))));
            msg.setIsSetFromName(cursor.getInt(cursor.getColumnIndex(SET_FROM_NAME)));
            msg.setHaveRead(cursor.getInt(cursor.getColumnIndex(HAVE_READ)));
            msg.setUnreceiptCnt(cursor.getInt(cursor.getColumnIndex(UNRECEIPT_COUNT)));
            msg.setUnreceiptMtime(cursor.getLong(cursor.getColumnIndex(UNRECEIPT_MTIME)));

            //数据库中获取atlist列表
            String atListString = cursor.getString(cursor.getColumnIndex(AT_LIST));
            List<Long> atList = JsonUtil.formatToGivenType(atListString, new TypeToken<List<Long>>() {
            });
            msg.setAtList(atList);
            //设置from appkey，若为空则给默认值。
            String fromAppKey = cursor.getString(cursor.getColumnIndex(FROM_APPKEY));
            if (TextUtils.isEmpty(fromAppKey)) {
                msg.setFromAppkey(JCoreInterface.getAppKey());
                setDefaultAppkeyToDatabaseInBackground(msgTableName, msg.getId());
            } else {
                msg.setFromAppkey(fromAppKey);
            }

            //有可能之前是unknown类型的message，此时parse成功了，需要更新contentType和content
            if (contentType == ContentType.unknown && msg.getContentType() != ContentType.unknown) {
                updateMessageContentTypeInBackground(msgTableName, msg.getId(), msg.getContentType());
                updateMessageContentInBackground(msgTableName, msg.getId(), msg.getContent());
            }
        }


        return msg;
    }

    public static Task<Long> insertInBackground(final InternalMessage msg, final String msgTableName) {
        final ContentValues values = messageToValues(msg);
        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Long>>() {
            @Override
            public Task<Long> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.insertAsync(msgTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
                } else {
                    Logger.dd(TAG, "insertInBackground failed. table not exist.");
                    return Task.forResult(0L);
                }
            }
        }).onSuccessTask(new Continuation<Long, Task<Long>>() {
            @Override
            public Task<Long> then(Task<Long> task) throws Exception {
                long id = task.getResult();
                if (id > 0) {
                    msg.setId(task.getResult().intValue());
                }
                return task;
            }
        });
    }


    public static Long insertSync(final InternalMessage msg, String msgTableName) {
        final ContentValues values = messageToValues(msg);

        long rowID = 0L;
        if (tableExistSync(msgTableName)) {
            if (0 == msg.getServerMessageId()) {
                //如果msg对象中的serverMsgId是0，说明这条消息是刚创建出来、还未发送的消息，直接入库
                rowID = CRUDMethods.insertSync(msgTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
            } else {
                int msgId = queryMsgIdSync(msg.getServerMessageId(), msgTableName);
                if (0 == msgId) {
                    //查询出的msgid为0，说明数据库中没有相应记录，此时才能插入数据库。
                    rowID = CRUDMethods.insertSync(msgTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
                } else {
                    rowID = msgId;
                }
            }
            msg.setId((int) rowID);
        } else {
            Logger.dd(TAG, "insertSync failed. table not exist.");
        }
        return rowID;
    }

    public static Task<Void> insertInTransaction(final long uid, final Collection<InternalMessage> msgs, final String msgTableName) {
        return CRUDMethods.execInTransactionAsync(new CRUDMethods.TransactionCallback<Void>() {
            @Override
            public Void execInTransaction() {
                if (tableExistSync(msgTableName)) {
                    if (uid != IMConfigs.getUserID()) {
                        Logger.ww(TAG, "current uid not match uid in protocol. abort this insert.");
                        return null;
                    }
                    for (InternalMessage msg : msgs) {
                        final ContentValues values = messageToValues(msg);
                        int msgId = queryMsgIdSync(msg.getServerMessageId(), msgTableName);
                        long rowID = msgId;
                        if (0 == msgId) {
                            rowID = CRUDMethods.insertSync(msgTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
                        }
                        msg.setId((int) rowID);
                    }
                } else {
                    Logger.dd(TAG, "insertSync failed. table not exist.");
                }
                return null;
            }
        });
    }

    public static Long insertWithValuesSync(final ContentValues values, String msgTableName) {

        long rowID = 0L;
        if (tableExistSync(msgTableName)) {
            rowID = CRUDMethods.insertSync(msgTableName, values, SQLiteDatabase.CONFLICT_IGNORE);
        } else {
            Logger.dd(TAG, "insertSync failed. table not exist.");
        }

        return rowID;
    }

    private static ContentValues messageToValues(InternalMessage msg) {
        final ContentValues values = new ContentValues();
        values.put(FROM_NAME, msg.getFromName());
        values.put(FROM_ID, msg.getFromID());
        values.put(DIRECT, msg.getDirect().toString());
        values.put(AT_LIST, JsonUtil.toJson(msg.getAtList()));

        if (ContentType.custom == msg.getContentType()) {
            values.put(CONTENT, ((CustomContent) msg.getContent()).toJson());
        } else {
            values.put(CONTENT, msg.getContent().toJson());
        }

        values.put(CONTENT_TYPE, msg.getContentType().toString());
        values.put(STATUS, msg.getStatus().toString());
        values.put(CREATE_TIME, msg.getCreateTime());
        values.put(FROM_APPKEY, msg.getFromAppKey());
        values.put(SERVER_MESSAGE_ID, msg.getServerMessageId());
        values.put(ORIGIN_META, msg.getOriginMeta());
        values.put(SET_FROM_NAME, msg.getIsSetFromName().intValue());
        values.put(HAVE_READ, msg.haveRead() ? 1 : 0);
        values.put(UNRECEIPT_COUNT, msg.getUnreceiptCnt());
        values.put(UNRECEIPT_MTIME, msg.getUnreceiptMtime());
        return values;
    }

    public static Task<InternalMessage> queryInBackground(final int messageId, final Object targetInfo, final ConversationType type,
                                                          final String msgTableName) {
        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Cursor>>() {
            @Override
            public Task<Cursor> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.queryAsync(msgTableName, null, StringUtils.createSelectionWithAnd(ID),
                            new String[]{String.valueOf(messageId)}, null, null, CREATE_TIME + " asc",
                            null);
                } else {
                    Logger.dd(TAG, "queryInBackground failed. table not exist.");
                    return Task.forResult(null);
                }
            }
        }).onSuccess(new Continuation<Cursor, InternalMessage>() {
            @Override
            public InternalMessage then(Task<Cursor> task) throws Exception {
                return queryInternal(task.getResult(), msgTableName, targetInfo, type);
            }
        });
    }

    public static int queryMsgIdSync(final long serverMsgId, final String msgTableName) {
        int msgId = 0;
        Cursor cursor = CRUDMethods.querySync(msgTableName, new String[]{ID},
                StringUtils.createSelectionWithAnd(SERVER_MESSAGE_ID),
                new String[]{String.valueOf(serverMsgId)}, null, null, null, null);
        if (null != cursor && cursor.getCount() > 0) {
            cursor.moveToNext();
            msgId = cursor.getInt(cursor.getColumnIndex(ID));
            cursor.close();
        } else if (null != cursor) {
            cursor.close();
        }
        return msgId;
    }

    public static InternalMessage querySync(int messageId, final Object targetInfo, final ConversationType type,
                                            final String msgTableName) {
        if (tableExistSync(msgTableName)) {
            Cursor cursor = CRUDMethods.querySync(msgTableName, null, StringUtils.createSelectionWithAnd(ID),
                    new String[]{String.valueOf(messageId)}, null, null, CREATE_TIME + " asc",
                    null);
            return queryInternal(cursor, msgTableName, targetInfo, type);
        } else {
            Logger.dd(TAG, "querySync failed. table not exist.");
            return null;
        }
    }

    public static InternalMessage queryWithServerMsgIdSync(long serverMsgId, final Object targetInfo, final ConversationType type,
                                                           final String msgTableName) {
        if (tableExistSync(msgTableName)) {
            Cursor cursor = CRUDMethods.querySync(msgTableName, null, StringUtils.createSelectionWithAnd(SERVER_MESSAGE_ID),
                    new String[]{String.valueOf(serverMsgId)}, null, null, CREATE_TIME + " asc", null);
            return queryInternal(cursor, msgTableName, targetInfo, type);
        } else {
            Logger.dd(TAG, "querySync failed. table not exist.");
            return null;
        }
    }

    private static InternalMessage queryInternal(Cursor cursor, String msgTableName, Object targetInfo, ConversationType type) {
        InternalMessage msg = null;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                if (cursor.moveToFirst()) {
                    msg = createMessage(msgTableName, cursor, targetInfo, type);
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return msg;
    }

    public static <T extends Message> List<T> queryFromOldest(ArrayList<T> list, int offset, int limit, Object targetInfo,
                                                              ConversationType type, String msgTableName) throws SQLiteException {
        return queryListSync(list, "asc", offset, limit, targetInfo, type, msgTableName);
    }

    public static <T extends Message> List<T> queryFromLatest(ArrayList<T> list, int offset, int limit, Object targetInfo,
                                                              ConversationType type, String msgTableName) throws SQLiteException {
        return queryListSync(list, "desc", offset, limit, targetInfo, type, msgTableName);
    }

    public static <T extends Message> Task<List<T>> queryListInBackground(final ArrayList<T> list, final String order, final int offset, final int limit, final Object targetInfo,
                                                                          final ConversationType type, final String msgTableName) {

        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Cursor>>() {
            @Override
            public Task<Cursor> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    String sql = "select * from " + msgTableName + " order by " + CREATE_TIME + " " + order
                            + " limit " + limit + " offset " + offset;
                    return CRUDMethods.rawQueryAsync(sql, null);
                } else {
                    Logger.dd(TAG, "queryInBackground list failed. table not exist.");
                    return Task.forResult(null);
                }
            }
        }).onSuccess(new Continuation<Cursor, List<T>>() {
            @Override
            public List<T> then(Task<Cursor> task) throws Exception {
                return queryListInternal(task.getResult(), list, msgTableName, targetInfo, type);
            }
        });
    }

    public static <T extends Message> List<T> queryListSync(final ArrayList<T> list, final String order, final int offset, final int limit, final Object targetInfo,
                                                            final ConversationType type, final String msgTableName) {
        if (tableExistSync(msgTableName)) {
            String sql = "select * from " + msgTableName + " order by " + CREATE_TIME + " " + order
                    + " limit " + limit + " offset " + offset;
            return queryListInternal(CRUDMethods.rawQuerySync(sql, null), list, msgTableName, targetInfo, type);
        } else {
            Logger.dd(TAG, "queryInBackground list failed. table not exist.");
            return list;
        }
    }

    private static <T extends Message> List<T> queryListInternal(Cursor cursor, List<T> list, String msgTableName, Object targetInfo, ConversationType type) {
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    InternalMessage msg = createMessage(msgTableName, cursor, targetInfo, type);
                    list.add((T) msg);
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }

        return list;
    }

    public static List<Long> queryCtimeFromNewest(int offset, int limit, String msgTableName) {
        List<Long> result = new ArrayList<Long>();
        if (tableExistSync(msgTableName)) {
            String sql = "select " + CREATE_TIME + " from " + msgTableName + " order by " + CREATE_TIME + " desc limit "
                    + limit + " offset " + offset;
            Cursor cursor = CRUDMethods.rawQuerySync(sql, null);
            if (null != cursor && cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        result.add(cursor.getLong(cursor.getColumnIndex(CREATE_TIME)));
                    }
                } finally {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "queryCtimeFromNewest ,  offset = " + offset + " limit = " + limit + " tableName = " + msgTableName + "  .list = " + result);
        return result;
    }

    public static long queryUnreceiptMtime(long serverMsgId, String msgTableName) {
        long result = 0L;
        if (tableExistSync(msgTableName)) {
            Cursor cursor = CRUDMethods.querySync(msgTableName, new String[]{UNRECEIPT_MTIME}, StringUtils.createSelectionWithAnd(SERVER_MESSAGE_ID),
                    new String[]{String.valueOf(serverMsgId)}, null, null, null, null);
            if (null != cursor && cursor.getCount() > 0) {
                try {
                    if (cursor.moveToNext()) {
                        result = cursor.getLong(cursor.getColumnIndex(UNRECEIPT_MTIME));
                    }
                } finally {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "queryUnreceiptMtime server msg id = " + serverMsgId + " mtime = " + result);
        return result;
    }

    /**
     * 批量查询serverMsgid所对应的unreceipt cnt
     *
     * @param msgIDMap     待查询的serverMsgId容器。key - serverMsgID, value - unreceipt cnt
     * @param msgTableName message表名
     */
    private static void queryUnreceiptCntInBatch(Map<Long, Integer> msgIDMap, String msgTableName) {
        Logger.d(TAG, "queryUnreceiptCnt in batch start . table name = " + msgTableName + " msgIDMap = " + msgIDMap);
        if (null == msgIDMap || msgIDMap.isEmpty()) {
            return;
        }
        if (tableExistSync(msgTableName)) {
            Cursor cursor = CRUDMethods.querySync(msgTableName, new String[]{SERVER_MESSAGE_ID, UNRECEIPT_COUNT},
                    StringUtils.createListSelection(SERVER_MESSAGE_ID, msgIDMap.keySet()), null, null, null, null, null);
            if (null != cursor && cursor.getCount() > 0) {
                try {
                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(cursor.getColumnIndex(SERVER_MESSAGE_ID));
                        msgIDMap.put(id, cursor.getInt(cursor.getColumnIndex(UNRECEIPT_COUNT)));
                    }
                } finally {
                    cursor.close();
                }
            } else if (cursor != null) {
                cursor.close();
            }
        }
        Logger.d(TAG, "queryUnreceiptCnt in batch result = " + msgIDMap);
    }

    public static InternalMessage queryLatestSync(final Object targetInfo, final ConversationType type,
                                                  final String msgTableName) {
        if (tableExistSync(msgTableName)) {
            return queryLatestInternal(CRUDMethods.querySync(msgTableName, null, null, null, null, null, CREATE_TIME + " desc", "1")
                    , msgTableName, targetInfo, type);
        } else {
            Logger.dd(TAG, "querySync latest failed. table not exist.");
            return null;
        }
    }

    private static InternalMessage queryLatestInternal(Cursor cursor, String msgTableName, Object targetInfo, ConversationType type) {
        InternalMessage msg = null;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToFirst()) {
                    msg = createMessage(msgTableName, cursor, targetInfo, type);
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return msg;
    }

    public static <T extends Message> Task<List<T>> queryAllInBackground(final ArrayList<T> list, final Object targetInfo, final ConversationType type,
                                                                         final String msgTableName) throws SQLiteException {
        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Cursor>>() {
            @Override
            public Task<Cursor> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.queryAsync(msgTableName, null, null, null, null, null, CREATE_TIME + " asc", null);
                } else {
                    Logger.dd(TAG, "queryAllInBackground failed. table not exist.");
                    return Task.forResult(null);
                }
            }
        }).onSuccess(new Continuation<Cursor, List<T>>() {
            @Override
            public List<T> then(Task<Cursor> task) throws Exception {
                return queryListInternal(task.getResult(), list, msgTableName, targetInfo, type);
            }
        });
    }

    public static <T extends Message> List<T> queryAllSync(final ArrayList<T> list, final Object targetInfo, final ConversationType type,
                                                           final String msgTableName) {
        if (tableExistSync(msgTableName)) {
            return queryListInternal(CRUDMethods.querySync(msgTableName, null, null, null, null, null, CREATE_TIME + " asc", null),
                    list, msgTableName, targetInfo, type);
        } else {
            Logger.dd(TAG, "queryAll failed. table not exist.");
            return list;
        }
    }

    public static Task<Boolean> deleteInBackground(final int messageId, final String msgTableName) throws SQLiteException {
        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.deleteAsync(msgTableName, StringUtils.createSelectionWithAnd(ID), new String[]{String.valueOf(messageId)});
                } else {
                    Logger.dd(TAG, "deleteInBackground failed. table not exist.");
                    return Task.forResult(false);
                }
            }
        });

    }

    public static Boolean deleteSync(final int messageId, final String msgTableName) {
        if (tableExistSync(msgTableName)) {
            return CRUDMethods.deleteSync(msgTableName, StringUtils.createSelectionWithAnd(ID), new String[]{String.valueOf(messageId)});
        } else {
            Logger.dd(TAG, "deleteSync failed. table not exist.");
            return false;
        }
    }

    public static Boolean deleteWithServerMsgIdSync(final long serverMsgId, final String msgTableName) {
        if (tableExistSync(msgTableName)) {
            return CRUDMethods.deleteSync(msgTableName, StringUtils.createSelectionWithAnd(SERVER_MESSAGE_ID), new String[]{String.valueOf(serverMsgId)});
        } else {
            Logger.dd(TAG, "deleteWithServerMsgIdSync failed. table not exist.");
            return false;
        }
    }

    public static Task<Boolean> deleteAllInBackground(final String msgTableName) {

        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.deleteAsync(msgTableName, null, null);
                } else {
                    Logger.dd(TAG, "deleteInBackground all failed. table not exist.");
                    return Task.forResult(false);
                }
            }
        });
    }

    public static Boolean deleteAllSync(String msgTableName) {
        if (tableExistSync(msgTableName)) {
            return CRUDMethods.deleteSync(msgTableName, null, null);
        } else {
            Logger.dd(TAG, "deleteAllSync failed. table not exist.");
            return false;
        }
    }

    public static Task<Void> dropTableInBackground(final String msgTableName) {
        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Void>>() {
            @Override
            public Task<Void> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    String sql = "DROP TABLE IF EXISTS " + msgTableName;
                    return CRUDMethods.execSQLAsync(sql, null);
                } else {
                    Logger.dd(TAG, "drop table failed. table not exist.");
                    return Task.forResult(null);
                }
            }
        });
    }

    public static void dropTableSync(String msgTableName) {
        if (tableExistSync(msgTableName)) {
            String sql = "DROP TABLE IF EXISTS " + msgTableName;
            CRUDMethods.execSQLSync(sql, null);
        } else {
            Logger.dd(TAG, "drop table failed. table not exist.");
        }
    }

    public static Task<Boolean> updateMessageContentInBackground(String msgTableName, int msgID, MessageContent content) {
        ContentValues values = new ContentValues();
        values.put(CONTENT, content.toJson());
        return updateInBackground(msgTableName, msgID, values);
    }

    public static boolean updateMessageContentSync(String msgTableName, int msgID, MessageContent content) {
        ContentValues values = new ContentValues();
        values.put(CONTENT, content.toJson());
        values.put(CONTENT_TYPE, String.valueOf(content.getContentType()));
        return updateSync(msgTableName, msgID, values);
    }

    public static Task<Boolean> updateMessageContentTypeInBackground(String msgTableName, int msgID, ContentType contentType) {
        ContentValues values = new ContentValues();
        values.put(CONTENT_TYPE, contentType.toString());
        return updateInBackground(msgTableName, msgID, values);
    }

    public static boolean updateMessageContentTypeSync(String msgTableName, int msgID, ContentType contentType) {
        ContentValues values = new ContentValues();
        values.put(CONTENT_TYPE, contentType.toString());
        return updateSync(msgTableName, msgID, values);
    }

    public static Task<Boolean> updateMessageStatusInBackground(String msgTableName, int msgID, MessageStatus status) {
        ContentValues values = new ContentValues();
        values.put(STATUS, status.toString());
        return updateInBackground(msgTableName, msgID, values);
    }

    public static boolean updateMessageStatusSync(String msgTableName, int msgID, MessageStatus status) {
        ContentValues values = new ContentValues();
        values.put(STATUS, status.toString());
        return updateSync(msgTableName, msgID, values);
    }

    public static Task<Boolean> updateMessageServerMsgIdInBackground(String msgTableName, int msgID, Long serverMsgId) {
        ContentValues values = new ContentValues();
        values.put(SERVER_MESSAGE_ID, serverMsgId);
        return updateInBackground(msgTableName, msgID, values);
    }

    public static boolean updateMessageServerMsgIdSync(String msgTableName, int msgID, Long serverMsgId) {
        ContentValues values = new ContentValues();
        values.put(SERVER_MESSAGE_ID, serverMsgId);
        return updateSync(msgTableName, msgID, values);
    }

    public static Task<Boolean> updateMessageTimestampInBackground(String msgTableName, int msgID, long creatTime) {
        ContentValues values = new ContentValues();
        values.put(CREATE_TIME, creatTime);
        return updateInBackground(msgTableName, msgID, values);
    }

    public static boolean updateMessageTimestampSync(String msgTableName, int msgID, long creatTime) {
        ContentValues values = new ContentValues();
        values.put(CREATE_TIME, creatTime);
        return updateSync(msgTableName, msgID, values);
    }

    //将目标serverMsgIds list中所有msg的have read状态置为 1（已读）。
    public static boolean setMessageHaveReadInBatch(String msgTableName, Collection<Long> serverMsgIds) {
        ContentValues values = new ContentValues();
        values.put(HAVE_READ, 1);//1表示消息已读
        boolean result = CRUDMethods.updateSync(msgTableName, values,
                StringUtils.createListSelection(SERVER_MESSAGE_ID, serverMsgIds), null);
        Logger.d(TAG, "[updateMessageHaveReadStateInBatch] server msg ids = " + serverMsgIds + " result = " + result);
        return result;
    }

    public static boolean updateMessageUnreceiptMtimeSync(String msgTableName, long serverMsgId, int unreceiptCnt, long unreceiptMtime) {
        long mtimeLocal = queryUnreceiptMtime(serverMsgId, msgTableName);
        if (unreceiptMtime > mtimeLocal) {//只有新的mtime大于本地已有的mtime时，才能更新本地unreceiptCnt
            ContentValues values = new ContentValues();
            values.put(UNRECEIPT_COUNT, unreceiptCnt);
            values.put(UNRECEIPT_MTIME, unreceiptMtime);
            return updateSyncWithServerMsgID(msgTableName, serverMsgId, values);
        }
        return false;
    }

    //批量更新消息的未回执数mtime
    public static Task<List<MessageReceiptStatusChangeEvent.MessageReceiptMeta>> updateMessageUnreceiptMtimeInBatch(final String msgTableName, final Collection<Receipt.MsgReceiptMeta> receiptMetas) {
        return CRUDMethods.execInTransactionAsync(new CRUDMethods.TransactionCallback<List<MessageReceiptStatusChangeEvent.MessageReceiptMeta>>() {
            @Override
            public List<MessageReceiptStatusChangeEvent.MessageReceiptMeta> execInTransaction() {
                //更新消息未回执数之前，先查询这些消息本地已有的未回执数unreceipt_cnt。只有待更新的未回执数小于本地已有的未回执数时，才将数据写入到数据库。
                Map<Long, Integer> msgIdList = new HashMap<Long, Integer>();//msgid到回执mtime之间的映射
                for (Receipt.MsgReceiptMeta meta : receiptMetas) {
                    msgIdList.put(meta.getMsgid(), 0);
                }
                queryUnreceiptCntInBatch(msgIdList, msgTableName);

                List<MessageReceiptStatusChangeEvent.MessageReceiptMeta> modifiedMetas = new ArrayList<MessageReceiptStatusChangeEvent.MessageReceiptMeta>();
                ContentValues values = new ContentValues();
                for (Receipt.MsgReceiptMeta meta : receiptMetas) {
                    Logger.d(TAG, "meta info .  id = " + meta.getMsgid() + " mtime = " + meta.getMtime() + " cnt = " + meta.getUnreadCount()
                            + " cnt in local = " + msgIdList.get(meta.getMsgid()));
                    if (meta.getUnreadCount() < msgIdList.get(meta.getMsgid())) {
                        values.put(UNRECEIPT_COUNT, meta.getUnreadCount());
                        values.put(UNRECEIPT_MTIME, meta.getMtime());
                        updateSyncWithServerMsgID(msgTableName, meta.getMsgid(), values);
                        //记录下未读数改变了的meta信息，之后需要把这部分数据通过MessageReceiptStatusChangeEvent事件抛给上层。
                        modifiedMetas.add(new MessageReceiptStatusChangeEvent.MessageReceiptMeta(meta.getMsgid(), meta.getUnreadCount(), meta.getMtime()));
                    }
                }
                return modifiedMetas;
            }
        });
    }

    private static Task<Boolean> updateInBackground(final String msgTableName, final int messageID,
                                                    final ContentValues values) {
        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.updateAsync(msgTableName, values, StringUtils.createSelectionWithAnd(ID), new String[]{String.valueOf(messageID)});
                } else {
                    Logger.dd(TAG, "updateInBackground specific column failed. table not exist.");
                    return Task.forResult(false);
                }
            }
        });
    }

    private static Boolean updateSync(final String msgTableName, final int messageID,
                                      final ContentValues values) {
        if (tableExistSync(msgTableName)) {
            return CRUDMethods.updateSync(msgTableName, values, StringUtils.createSelectionWithAnd(ID), new String[]{String.valueOf(messageID)});
        } else {
            Logger.dd(TAG, "updateSync specific column failed. table not exist.");
            return false;
        }
    }

    private static Boolean updateSyncWithServerMsgID(final String msgTableName, final long serverMsgId,
                                                     final ContentValues values) {
        if (tableExistSync(msgTableName)) {
            return CRUDMethods.updateSync(msgTableName, values, StringUtils.createSelectionWithAnd(SERVER_MESSAGE_ID), new String[]{String.valueOf(serverMsgId)});
        } else {
            Logger.dd(TAG, "updateSync specific column failed. table not exist.");
            return false;
        }
    }

    private static InternalMessage createMessage(String msgTableName, Cursor cursor, Object targetInfo, ConversationType type) {
        InternalMessage msg = cursorToMessage(msgTableName, cursor);
        if (null == msg) {
            return null;
        }
        //消息的targetInfo是从会话的targetInfo中拿过来的，所以消息的方向无论是发送或者接收
        //target始终是对方。
        msg.setTargetInfo(targetInfo);
        msg.setTargetType(type);
        return msg;
    }

    private static Task<Boolean> setDefaultAppkeyToDatabaseInBackground(final String msgTableName, final int msgId) {
        if (!CommonUtils.isInited("MessageStorage.setDefaultAppkeyToDatabaseInBackground")) {
            return Task.forResult(false);
        }

        final ContentValues contentValues = new ContentValues();
        contentValues.put(FROM_APPKEY, JCoreInterface.getAppKey());
        return tableExistsInBackground(msgTableName).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return CRUDMethods.updateAsync(msgTableName, contentValues,
                            StringUtils.createSelectionWithAnd(ID), new String[]{String.valueOf(msgId)});
                } else {
                    Logger.dd(TAG, "set default appkey failed. table not exist.");
                    return Task.forResult(false);
                }
            }
        });
    }

    public static Task<Boolean> tableExistsInBackground(String table) {
        String sql = "select * from sqlite_master where name=" + "'" + table + "'";
        return CRUDMethods.rawQueryAsync(sql, null).onSuccess(new Continuation<Cursor, Boolean>() {
            @Override
            public Boolean then(Task<Cursor> task) throws Exception {
                return tableExistInternal(task.getResult());
            }
        });
    }

    public static Boolean tableExistSync(String table) {
        String sql = "select * from sqlite_master where name=" + "'" + table + "'";
        return tableExistInternal(CRUDMethods.rawQuerySync(sql, null));
    }

    private static boolean tableExistInternal(Cursor cursor) {
        Boolean exist = false;
        if (null != cursor && cursor.getCount() > 0) {
            exist = true;
            cursor.close();
        } else if (null != cursor) {
            cursor.close();
        }
        return exist;
    }
}
