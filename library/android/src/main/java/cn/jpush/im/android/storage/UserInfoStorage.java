package cn.jpush.im.android.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.table.UserTable;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.UserIDHelper;


/**
 * 直接操作UserInfo数据库的类。<br/>
 * <p/>
 * Note:此类中的方法都为protected，必须通过{@link UserInfoManager}类来操作和管理UserInfo数据库。
 */
public class UserInfoStorage {

    private static final String TAG = "UserInfoStorage";

    public static final String KEY_UID = "uid";

    public static final String KEY_USERNAME = "username";

    public static final String KEY_NICKNAME = "nickname";

    public static final String KEY_NOTENAME = "note_name";

    public static final String KEY_NOTETEXT = "note_text";

    public static final String KEY_STAR = "star";

    public static final String KEY_BLACKLIST = "blacklist";

    public static final String KEY_AVATAR = "avatar";

    public static final String KEY_BIRTHDAY = "birthday";

    public static final String KEY_SIGNATURE = "signature";

    public static final String KEY_GENDER = "gender";

    public static final String KEY_REGION = "region";

    public static final String KEY_ADDRESS = "address";

    public static final String KEY_APPKEY = "appkey";

    public static final String KEY_NODISTURB = "nodisturb";

    public static final String KEY_ISFRIEND = "friend";

    public static final String KEY_MTIME = "mtime";

    public static final String KEY_EXTRAS = "extras";

    public static Task<Boolean> insertOrUpdateWhenExistsInBackground(final InternalUserInfo userInfo,
                                                                     final boolean needUpdateConversation,
                                                                     final boolean needUpdateBlackList,
                                                                     final boolean needUpdateNoDisturb,
                                                                     final boolean needUpdateMemo) {
        if (null == userInfo) {
            Logger.ee(TAG, "[insertOrUpdateWhenExistsInBackground] invalid parameters! userInfo is null");
            return Task.forResult(false);
        }

        fillGenderInUserInfo(userInfo);

        return queryExistInBackground(userInfo.getUserName(), userInfo.getAppKey()).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    //userID 已存在，则更新userInfo
                    return updateInBackground(userInfo, needUpdateConversation, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
                } else {
                    return insertInBackground(userInfo).onSuccess(new Continuation<Long, Boolean>() {
                        @Override
                        public Boolean then(Task<Long> task) throws Exception {
                            return task.getResult() > 0;
                        }
                    });
                }
            }
        });
    }

    static boolean insertOrUpdateWhenExistsSync(InternalUserInfo userInfo,
                                                boolean needUpdateConversation,
                                                boolean needUpdateBlackList,
                                                boolean needUpdateNoDisturb,
                                                boolean needUpdateMemo) {
        if (null == userInfo) {
            Logger.ee(TAG, "[insertOrUpdateWhenExistsSync] invalid parameters! userInfo is null");
            return false;
        }
        fillGenderInUserInfo(userInfo);

        boolean result;
        if (queryExistSync(userInfo.getUserName(), userInfo.getAppKey())) {
            //userID 已存在，则更新userInfo
            result = updateSync(userInfo, needUpdateConversation, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
        } else {
            result = insertSync(userInfo) > 0;
        }
        return result;
    }

    static Task<Void> insertOrUpdateWhenExistsInBackground(final List<InternalUserInfo> userInfos,
                                                           final boolean needUpdateConversation,
                                                           final boolean needUpdateBlackList,
                                                           final boolean needUpdateNoDisturb,
                                                           final boolean needUpdateMemo) {

        return CRUDMethods.execInTransactionAsync(new CRUDMethods.TransactionCallback<Void>() {
            @Override
            public Void execInTransaction() {
                Logger.d(TAG, "execInTransactionAsync start !!!");
                for (InternalUserInfo userInfo : userInfos) {
                    insertOrUpdateWhenExistsSync(userInfo, needUpdateConversation, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
                }
                Logger.d(TAG, "execInTransactionAsync end !!!");
                return null;
            }
        });
    }

    private static void fillGenderInUserInfo(InternalUserInfo userInfo) {
        String genderString = userInfo.getGenderString();
        if (genderString.equals("0")) {
            userInfo.setGender(UserInfo.Gender.unknown);
        } else if (genderString.equals("1")) {
            userInfo.setGender(UserInfo.Gender.male);
        } else if (genderString.equals("2")) {
            userInfo.setGender(UserInfo.Gender.female);
        }
    }

    protected static Task<Long> insertInBackground(final InternalUserInfo info) {
        final ContentValues contentValues = infoToContentValues(info, true, true, true);

        return CRUDMethods.insertAsync(UserTable.USERS_TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_IGNORE).onSuccessTask(new Continuation<Long, Task<Long>>() {
            @Override
            public Task<Long> then(Task<Long> task) throws Exception {
                if (task.getResult() > 0) {
                    afterUpdateSuccess(info, contentValues);
                }
                return task;
            }
        });

    }

    protected static long insertSync(final InternalUserInfo info) {
        final ContentValues contentValues = infoToContentValues(info, true, true, true);
        long rowId = CRUDMethods.insertSync(UserTable.USERS_TABLE_NAME, contentValues, SQLiteDatabase.CONFLICT_IGNORE);
        if (rowId > 0) {
            afterUpdateSuccess(info, contentValues);
        }
        return rowId;
    }

    private static ContentValues infoToContentValues(InternalUserInfo info, boolean needUpdateBlackList, boolean needUpdateNoDisturb, boolean needFriendInfo) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_UID, info.getUserID());
        contentValues.put(KEY_USERNAME, info.getUserName());
        contentValues.put(KEY_NICKNAME, info.getNickname());
        contentValues.put(KEY_SIGNATURE, info.getSignature());
        contentValues.put(KEY_GENDER, info.getGenderString());
        contentValues.put(KEY_BIRTHDAY, info.getBirthdayString());
        contentValues.put(KEY_REGION, info.getRegion());
        contentValues.put(KEY_AVATAR, info.getAvatar());
        contentValues.put(KEY_APPKEY, info.getAppKey());
        contentValues.put(KEY_STAR, info.getStar());
        contentValues.put(KEY_ADDRESS, info.getAddress());
        contentValues.put(KEY_MTIME, info.getmTime());
        contentValues.put(KEY_EXTRAS, info.getExtrasJson());
        //needUpdateLocalFields用来标示是否需要更新本地字段，通过服务器拿到的userinfo中不包含
        //这些本地字段，处理服务器端get userinfo返回的数据时，不应更新这些字段。
        if (needUpdateBlackList) {
            contentValues.put(KEY_BLACKLIST, info.getBlacklist());
        }

        if (needUpdateNoDisturb) {
            contentValues.put(KEY_NODISTURB, info.getNoDisturb());
        }

        if (needFriendInfo) {
            contentValues.put(KEY_ISFRIEND, info.isFriend() ? 1 : 0);
            contentValues.put(KEY_NOTENAME, info.getNotename());
            contentValues.put(KEY_NOTETEXT, info.getNoteText());
        }
        return contentValues;
    }


    static Task<Boolean> updateNickNameInBackground(long userID, String nickname) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NICKNAME, nickname);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateNoteNameInBackground(long userID, String notename) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NOTENAME, notename);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateNoteTextInBackground(long userID, String noteText) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NOTETEXT, noteText);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateStarInBackground(long userID, boolean isStar) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_STAR, isStar ? 1 : 0);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateBlackListInBackground(long userID, boolean isInBlackList) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_BLACKLIST, isInBlackList ? 1 : 0);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateNoDisturbInBackground(long userID, boolean isInNoDisturb) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_NODISTURB, isInNoDisturb ? 1 : 0);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateSignatureInBackground(long userID, String signature) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_SIGNATURE, signature);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateIsFriendInBackground(long userID, int isFriend) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ISFRIEND, isFriend);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateMTimeInBackground(long userID, int mTime) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_MTIME, mTime);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateGenderInBackground(long userID, UserInfo.Gender gender) {
        String genderString = String.valueOf(gender.ordinal());
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_GENDER, genderString);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateBirthdayInBackground(long userID, String birthday) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_BIRTHDAY, birthday);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateRegionInBackground(long userID, String region) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_REGION, region);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateAddressInBackground(long userID, String address) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_ADDRESS, address);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateAvatarInBackground(long userID, String mediaID) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_AVATAR, mediaID);
        return updateInBackground(contentValues, userID);
    }

    static Task<Boolean> updateExtrasInBackground(long userID, String extrasJson) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_EXTRAS, extrasJson);
        return updateInBackground(contentValues, userID);
    }

    /**
     * 更新所有uid下对应的值
     *
     * @param uids   被更新的gid集合
     * @param values 被更新的值
     * @return
     */
    static Task<Boolean> updateAllUidsWithValue(final Collection<Long> uids, final ContentValues values) {
        if (!CommonUtils.isInited("UserInfo.updateInBackground") || null == uids || uids.isEmpty()) {
            return Task.forResult(false);
        }
        String selection = StringUtils.createListSelection(KEY_UID, uids);
        return CRUDMethods.updateAsync(UserTable.USERS_TABLE_NAME, values, selection, null).onSuccess(new Continuation<Boolean, Boolean>() {
            @Override
            public Boolean then(Task<Boolean> task) throws Exception {
                boolean result = task.getResult();
                if (result) {
                    for (Long uid : uids) {
                        afterUpdateSuccess(uid, values);
                    }
                }
                return task.getResult();
            }
        });
    }

    static Task<Boolean> updateInBackground(final ContentValues contentValues, final long userID) {
        if (!CommonUtils.isInited("UserInfo.updateInBackground")) {
            return Task.forResult(false);
        }

        return CRUDMethods.updateAsync(UserTable.USERS_TABLE_NAME, contentValues, StringUtils.createSelectionWithAnd(KEY_UID),
                new String[]{String.valueOf(userID)}).onSuccess(new Continuation<Boolean, Boolean>() {
            @Override
            public Boolean then(Task<Boolean> task) throws Exception {
                boolean result = task.getResult();
                if (result) {
                    afterUpdateSuccess(userID, contentValues);
                }
                return result;
            }
        });
    }

    protected static Task<Boolean> updateInBackground(final InternalUserInfo userInfo,
                                                      final boolean needUpdateConversation,
                                                      boolean needUpdateBlackList, boolean needUpdateNoDisturb, boolean needUpdateMemo) {
        if (!CommonUtils.isInited("UserInfo.updateInBackground") || null == userInfo) {
            return Task.forResult(false);
        }
        final ContentValues contentValues = infoToContentValues(userInfo, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
        return CRUDMethods.updateAsync(UserTable.USERS_TABLE_NAME, contentValues, StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY),
                new String[]{userInfo.getUserName(), userInfo.getAppKey()}).onSuccess(new Continuation<Boolean, Boolean>() {
            @Override
            public Boolean then(Task<Boolean> task) throws Exception {
                boolean result = task.getResult();
                if (result && needUpdateConversation) {
                    afterUpdateSuccess(userInfo, contentValues);
                }
                return result;
            }
        });
    }


    /**
     * 直接在当前线程同步执行数据库更新操作
     */
    protected static boolean updateSync(InternalUserInfo userInfo,
                                        boolean needUpdateConversation, boolean needUpdateBlackList, boolean needUpdateNoDisturb, boolean needUpdateMemo) {
        if (!CommonUtils.isInited("UserInfo.updateInBackground") || null == userInfo) {
            return false;
        }
        ContentValues contentValues = infoToContentValues(userInfo, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
        boolean result = CRUDMethods.updateSync(UserTable.USERS_TABLE_NAME, contentValues,
                StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY), new String[]{userInfo.getUserName(), userInfo.getAppKey()});
        if (result && needUpdateConversation) {
            afterUpdateSuccess(userInfo, contentValues);
        }
        return result;
    }

    //本地数据库中用户信息更新完成之后，需要同时更新缓存中会话的相关信息，以及缓存的会话中targetInfo相关的信息
    private static void afterUpdateSuccess(InternalUserInfo info, ContentValues contentValues) {
        if (null == info) {
            return;
        }

        String username = info.getUserName();
        String appkey = info.getAppKey();
        long uid = info.getUserID();

        updateConversationTitle(uid, username, appkey, contentValues);

        if (null != info.getAvatarFile()) {
            Logger.d(TAG, "updateInBackground conversation when user info updated ! avatar = " + info.getAvatarFile());
            ConversationManager.getInstance().updateConvsersationAvatar(ConversationType.single,
                    username, appkey, info.getAvatarFile().getAbsolutePath());
        }
        //更新conversation缓存中targetinfo
        ConversationManager.getInstance().updateTargetInfoInCache(username, appkey, contentValues);

        if (contentValues.containsKey(KEY_NODISTURB)) {
            //用户免打扰状态改变，直接将全局未读数重置。保证之后获取到的全局未读数是正确的。
            JMessage.resetAllUnreadMsgCnt();
        }
        UserIDHelper.updateIDInCache(username, appkey, uid);
    }

    //本地数据库中用户信息更新完成之后，需要同时更新缓存中会话的相关信息，以及缓存的会话中targetInfo相关的信息
    private static void afterUpdateSuccess(long uid, ContentValues contentValues) {
        afterUpdateSuccess(UserInfoManager.getInstance().getUserInfo(uid), contentValues);
    }

    private static void updateConversationTitle(long uid, String username, String appkey, ContentValues contentValues) {
        String nickname = contentValues.getAsString(KEY_NICKNAME);
        String noteName = contentValues.getAsString(KEY_NOTENAME);

        boolean updateConvTitle = false;
        String newTitle = "";
        if (!TextUtils.isEmpty(noteName)) {
            //更新的userinfo中包含了notename,此时需要同步更新会话title
            Logger.d(TAG, "contains notename, update conversation title");
            updateConvTitle = true;
            newTitle = noteName;
        } else if (!TextUtils.isEmpty(nickname)) {
            String notenameInDB = queryStringValueSync(KEY_UID, new String[]{String.valueOf(uid)}, KEY_NOTENAME);
            if (TextUtils.isEmpty(notenameInDB)) {
                //被更新的user本身没有备注名，同时更新信息中包含了nickname,则使用nickname来更新会话title.
                Logger.d(TAG, "contains nickname and user do NOT have a notename, update conversation title");
                updateConvTitle = true;
                newTitle = nickname;
            }
        } else if (null != noteName) {
            String nicknameInDB = queryStringValueSync(KEY_UID, new String[]{String.valueOf(uid)}, KEY_NICKNAME);
            if (!TextUtils.isEmpty(nicknameInDB)) {
                //待更新的noteName为空字符串，说明这是一个重置noteName的请求，此时检查用户的nickname，如果存在，则将nickname更新到会话title
                Logger.d(TAG, "reset noteName when there is a nickname, update conversation title with nickname");
                updateConvTitle = true;
                newTitle = nicknameInDB;
            }
        }

        if (updateConvTitle) {
            Logger.d(TAG, "updateInBackground conversation when user info updated new title is " + newTitle);
            // FIXME: 2017/7/6 此处只更新了单聊会话的title，之后还需要考虑更新用户所在群聊的title
            ConversationManager.getInstance().updateConvsersationTitle(ConversationType.single,
                    username, appkey, newTitle);
        }
    }

    private static InternalUserInfo cursorToUserInfo(Cursor cursor) {
        if (null == cursor) {
            return null;
        }
        InternalUserInfo info = new InternalUserInfo();
        info.setUserID(cursor.getLong(cursor.getColumnIndex(KEY_UID)));
        info.setUserName(cursor.getString(cursor.getColumnIndex(KEY_USERNAME)));
        info.setNickname(cursor.getString(cursor.getColumnIndex(KEY_NICKNAME)));
        info.setNotename(cursor.getString(cursor.getColumnIndex(KEY_NOTENAME)));
        info.setNoteText(cursor.getString(cursor.getColumnIndex(KEY_NOTETEXT)));
        info.setSignature(cursor.getString(cursor.getColumnIndex(KEY_SIGNATURE)));
        info.setBirthdayString(cursor.getString(cursor.getColumnIndex(KEY_BIRTHDAY)));
        info.setRegion(cursor.getString(cursor.getColumnIndex(KEY_REGION)));
        info.setAddress(cursor.getString(cursor.getColumnIndex(KEY_ADDRESS)));
        info.setStar(cursor.getInt(cursor.getColumnIndex(KEY_STAR)));
        info.setBlacklist(cursor.getInt(cursor.getColumnIndex(KEY_BLACKLIST)));
        info.setNoDisturbInLocal(cursor.getInt(cursor.getColumnIndex(KEY_NODISTURB)));
        String genderString = cursor.getString(cursor.getColumnIndex(KEY_GENDER));
        info.setIsFriend(cursor.getInt(cursor.getColumnIndex(KEY_ISFRIEND)) == 1);
        info.setExtrasFromJson(cursor.getString(cursor.getColumnIndex(KEY_EXTRAS)));
        info.setGenderString(genderString);
        fillGenderInUserInfo(info);
        info.setAvatarMediaID(cursor.getString(cursor.getColumnIndex(KEY_AVATAR)));
        info.setmTime(cursor.getInt(cursor.getColumnIndex(KEY_MTIME)));

        //设置user的appkey，若为空则给一个默认值
        String appkey = cursor.getString(cursor.getColumnIndex(KEY_APPKEY));
        if (TextUtils.isEmpty(appkey)) {
            info.setAppkey(JCoreInterface.getAppKey());
            setDefaultAppkeyToDatabaseInBackground(info.getUserName());
        } else {
            info.setAppkey(appkey);
        }
        return info;
    }

    protected static Task<InternalUserInfo> queryInfoInBackground(String userName, String appkey) {
        return queryInfoInBackground(StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY), new String[]{userName, appkey});
    }

    protected static InternalUserInfo queryInfoSync(String userName, String appkey) {
        return queryInfoSync(StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY), new String[]{userName, appkey});
    }

    protected static Task<InternalUserInfo> queryInfoInBackground(long userID) {
        return queryInfoInBackground(StringUtils.createSelectionWithAnd(KEY_UID), new String[]{String.valueOf(userID)});
    }

    protected static InternalUserInfo queryInfoSync(long userID) {
        return queryInfoSync(StringUtils.createSelectionWithAnd(KEY_UID), new String[]{String.valueOf(userID)});
    }

    private static Task<InternalUserInfo> queryInfoInBackground(String selections, String[] selectionArgs) {
        if (!CommonUtils.isInited("UserInfo.queryInfoInBackground")) {
            return Task.forResult(null);
        }

        return CRUDMethods.queryAsync(UserTable.USERS_TABLE_NAME, null, selections, selectionArgs, null,
                null, null, null).onSuccess(new Continuation<Cursor, InternalUserInfo>() {
            @Override
            public InternalUserInfo then(Task<Cursor> task) throws Exception {
                return queryInfoInternal(task.getResult());
            }
        });
    }

    private static InternalUserInfo queryInfoSync(String selections, String[] selectionArgs) {
        if (!CommonUtils.isInited("UserInfo.queryInfoInBackground")) {
            return null;
        }

        return queryInfoInternal(CRUDMethods.querySync(UserTable.USERS_TABLE_NAME, null, selections, selectionArgs, null,
                null, null, null));
    }

    private static InternalUserInfo queryInfoInternal(Cursor cursor) {
        InternalUserInfo info = null;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    info = cursorToUserInfo(cursor);
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return info;
    }

    private static Task<Boolean> queryExistInBackground(String username, String appkey) {
        if (!CommonUtils.isInited("UserInfo.queryInfoInBackground")) {
            return Task.forResult(false);
        }

        String sql = "select count(*) as count from " + UserTable.USERS_TABLE_NAME + " where "
                + StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY);
        String[] args = new String[]{username, appkey};
        return CRUDMethods.rawQueryAsync(sql, args).onSuccess(new Continuation<Cursor, Boolean>() {
            @Override
            public Boolean then(Task<Cursor> task) throws Exception {
                return queryExistInternal(task.getResult());
            }
        });
    }

    static boolean queryExistSync(String username, String appkey) {
        if (!CommonUtils.isInited("UserInfo.queryInfoInBackground")) {
            return false;
        }

        String sql = "select count(*) as count from " + UserTable.USERS_TABLE_NAME + " where "
                + StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY);
        String[] args = new String[]{username, appkey};
        Cursor cursor = CRUDMethods.rawQuerySync(sql, args);
        return queryExistInternal(cursor);
    }

    private static boolean queryExistInternal(Cursor cursor) {
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
        return rowCount > 0;
    }

    static void queryInfosSync(Map<Long, InternalUserInfo> userInfoMap) {
        if (!CommonUtils.isInited("UserInfo.queryInfosSync") || null == userInfoMap) {
            return;
        }

        String sqlString = createQueryInfosSQLString(userInfoMap.keySet());
        if (null == sqlString) {
            Logger.w(TAG, "create query info sql string failed . return empty collection");
            return;
        }

        queryInfoInternal(CRUDMethods.rawQuerySync(sqlString, null), userInfoMap);
    }

    private static String createQueryInfosSQLString(Collection<Long> uidList) {
        //Note:此处使用IN() 作为查询条件有两个好处：1 使索引生效。 2 条件太多时，用OR连接会报错。
        String whereClause = StringUtils.createListSelection(KEY_UID, uidList);
        if (TextUtils.isEmpty(whereClause)) {
            Logger.d(TAG, "queryInBackground infos failed. username is empty");
            return null;
        }

        return "select * from " + UserTable.USERS_TABLE_NAME + " where " + whereClause;
    }

    private static void queryInfoInternal(Cursor cursor, Map<Long, InternalUserInfo> userInfoMap) {
        if (null != cursor && cursor.getCount() > 0) {
            try {
                while (cursor.moveToNext()) {
                    InternalUserInfo info = cursorToUserInfo(cursor);
                    userInfoMap.put(info.getUserID(), info);
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        userInfoMap.values();
    }

    protected static Task<String> queryUserNameInBackground(long userID) {
        if (!CommonUtils.isInited("UserInfo.queryUserNameInBackground")) {
            return Task.forResult(null);
        }

        return CRUDMethods.queryAsync(UserTable.USERS_TABLE_NAME, new String[]{KEY_USERNAME}, StringUtils.createSelectionWithAnd(KEY_UID),
                new String[]{String.valueOf(userID)}, null, null, null, null).onSuccess(new Continuation<Cursor, String>() {
            @Override
            public String then(Task<Cursor> task) throws Exception {
                return queryUsernameInternal(task.getResult());
            }
        });
    }

    protected static String queryUserNameSync(long userID) {
        if (!CommonUtils.isInited("UserInfo.queryUserNameInBackground")) {
            return null;
        }

        return queryUsernameInternal(CRUDMethods.querySync(UserTable.USERS_TABLE_NAME, new String[]{KEY_USERNAME}, StringUtils.createSelectionWithAnd(KEY_UID),
                new String[]{String.valueOf(userID)}, null, null, null, null));
    }

    private static String queryUsernameInternal(Cursor cursor) {
        String userName = null;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    userName = cursor.getString(cursor.getColumnIndex(KEY_USERNAME));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return userName;
    }

    //根据uid 查询username 和 appkey,查询结果中:
    // string[0] -- username
    // string[1] -- appkey
    protected static Task<String[]> queryUsernameAndAppkeyInBackground(long uid) {
        if (!CommonUtils.isInited("UserInfo.queryUsernameAndAppkeyInBackground")) {
            return Task.forResult(null);
        }

        return CRUDMethods.queryAsync(UserTable.USERS_TABLE_NAME, new String[]{KEY_USERNAME, KEY_APPKEY}, StringUtils.createSelectionWithAnd(KEY_UID),
                new String[]{String.valueOf(uid)}, null, null, null, null).onSuccess(new Continuation<Cursor, String[]>() {
            @Override
            public String[] then(Task<Cursor> task) throws Exception {
                return queryUsernameAndAppkeyInternal(task.getResult());
            }
        });
    }

    static String[] queryUsernameAndAppkeySync(long uid) {
        if (!CommonUtils.isInited("UserInfo.queryUsernameAndAppkeySync")) {
            return null;
        }

        return queryUsernameAndAppkeyInternal(CRUDMethods.querySync(UserTable.USERS_TABLE_NAME, new String[]{KEY_USERNAME, KEY_APPKEY}, StringUtils.createSelectionWithAnd(KEY_UID),
                new String[]{String.valueOf(uid)}, null, null, null, null));
    }

    private static String[] queryUsernameAndAppkeyInternal(Cursor cursor) {
        String[] results = new String[2];
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    results[0] = cursor.getString(cursor.getColumnIndex(KEY_USERNAME));
                    results[1] = cursor.getString(cursor.getColumnIndex(KEY_APPKEY));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return results;
    }

    /**
     * 此方法仅用于数据库从低版本升上来时 GroupTable upgrade的逻辑。因为低版本user表中不含appkey，
     * 所以查询条件中不带有appkey
     *
     * @param db
     * @param userName
     * @return
     */
    static long queryUserIDInBackground(SQLiteDatabase db, String userName) {
        if (!CommonUtils.isInited("UserInfo.queryUserIDInBackground")) {
            return 0;
        }
        Cursor cursor = db
                .query(UserTable.USERS_TABLE_NAME, new String[]{KEY_UID}, StringUtils.createSelectionWithAnd(KEY_USERNAME),
                        new String[]{userName}, null, null, null);
        long userID = 0;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    userID = cursor.getLong(cursor.getColumnIndex(KEY_UID));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return userID;
    }


    protected static Task<Long> queryUserIDInBackground(String userName, String appkey) {
        if (!CommonUtils.isInited("UserInfo.queryUserIDInBackground")) {
            return Task.forResult(0L);
        }

        return CRUDMethods.queryAsync(UserTable.USERS_TABLE_NAME, new String[]{KEY_UID}, StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY),
                new String[]{userName, appkey}, null, null, null, null).onSuccess(new Continuation<Cursor, Long>() {
            @Override
            public Long then(Task<Cursor> task) throws Exception {
                return queryUserIDInternal(task.getResult());
            }
        });
    }

    static long queryUserIDSync(String userName, String appkey) {
        if (!CommonUtils.isInited("UserInfo.queryUserIDSync")) {
            return 0L;
        }

        return queryUserIDInternal(CRUDMethods.querySync(UserTable.USERS_TABLE_NAME, new String[]{KEY_UID}, StringUtils.createSelectionWithAnd(KEY_USERNAME, KEY_APPKEY),
                new String[]{userName, appkey}, null, null, null, null));
    }

    private static long queryUserIDInternal(Cursor cursor) {
        long uid = 0;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    uid = cursor.getLong(cursor.getColumnIndex(KEY_UID));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return uid;
    }

    protected static Task<Integer> isUserInBlacklistInBackground(long userID) {
        return queryIntValueInBackground(KEY_UID, new String[]{String.valueOf(userID)}, KEY_BLACKLIST);
    }

    static int isUserInBlacklistSync(long userID) {
        return queryIntValueSync(KEY_UID, new String[]{String.valueOf(userID)}, KEY_BLACKLIST);
    }

    protected static Task<Integer> isUserInNoDisturbInBackground(long uid) {
        return queryIntValueInBackground(KEY_UID, new String[]{String.valueOf(uid)}, KEY_NODISTURB);
    }

    static int isUserInNoDisturbSync(long uid) {
        return queryIntValueSync(KEY_UID, new String[]{String.valueOf(uid)}, KEY_NODISTURB);
    }

    static int isUserYourFriend(long uid) {
        return queryIntValueSync(KEY_UID, new String[]{String.valueOf(uid)}, KEY_ISFRIEND);
    }

    static String queryUserNotename(long uid) {
        return queryStringValueSync(KEY_UID, new String[]{String.valueOf(uid)}, KEY_NOTENAME);
    }

    static String queryUserNoteText(long uid) {
        return queryStringValueSync(KEY_UID, new String[]{String.valueOf(uid)}, KEY_NOTETEXT);
    }

    private static Task<Integer> queryIntValueInBackground(String key, String[] args, final String columnName) {

        return CRUDMethods.queryAsync(UserTable.USERS_TABLE_NAME, new String[]{columnName}, key + "=?",
                args, null, null, null, null).onSuccess(new Continuation<Cursor, Integer>() {
            @Override
            public Integer then(Task<Cursor> task) throws Exception {
                return queryIntValueInternal(task.getResult(), columnName);
            }
        });
    }

    private static int queryIntValueSync(String key, String[] args, final String columnName) {
        return queryIntValueInternal(CRUDMethods.querySync(UserTable.USERS_TABLE_NAME, new String[]{columnName}, key + "=?",
                args, null, null, null, null), columnName);
    }

    private static int queryIntValueInternal(Cursor cursor, String columnName) {
        int intValue = 0;
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    intValue = cursor.getInt(cursor.getColumnIndex(columnName));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return intValue;
    }


    protected static Task<String> queryStringValueInBackground(String key, String[] args, final String columnName) {

        return CRUDMethods.queryAsync(UserTable.USERS_TABLE_NAME, new String[]{columnName}, key + "=?",
                args, null, null, null, null).onSuccess(new Continuation<Cursor, String>() {
            @Override
            public String then(Task<Cursor> task) throws Exception {
                return queryStringValueInternal(task.getResult(), columnName);
            }
        });
    }

    private static String queryStringValueSync(String key, String[] args, final String columnName) {
        return queryStringValueInternal(CRUDMethods.querySync(UserTable.USERS_TABLE_NAME, new String[]{columnName}, key + "=?",
                args, null, null, null, null), columnName);
    }

    private static String queryStringValueInternal(Cursor cursor, String columnName) {
        String stringValue = "";
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    stringValue = cursor.getString(cursor.getColumnIndex(columnName));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return stringValue;
    }

    static List<UserInfo> queryFriendListSync() {
        if (!CommonUtils.isInited("UserInfo.queryFriendListSync")) {
            return null;
        }

        return queryFriendListInternal(CRUDMethods.querySync(
                UserTable.USERS_TABLE_NAME, null, KEY_ISFRIEND + "=1", null, null, null, null, null));
    }

    private static List<UserInfo> queryFriendListInternal(Cursor cursor) {
        List<UserInfo> result = new ArrayList<UserInfo>();
        if (null != cursor && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    result.add(cursorToUserInfo(cursor));
                }
            } finally {
                cursor.close();
            }
        } else if (null != cursor) {
            cursor.close();
        }
        return result;
    }

    static boolean resetBlacklistStatusSync() {
        ContentValues values = new ContentValues();
        values.put(KEY_BLACKLIST, 0);
        return CRUDMethods.updateSync(UserTable.USERS_TABLE_NAME, values, KEY_BLACKLIST + "=1", null);
    }

    static boolean resetNodisturbStatusSync() {
        ContentValues values = new ContentValues();
        values.put(KEY_NODISTURB, 0);
        return CRUDMethods.updateSync(UserTable.USERS_TABLE_NAME, values, KEY_NODISTURB + "=1", null);
    }

    static boolean resetFriendRelatedSync() {
        ContentValues values = new ContentValues();
        values.put(KEY_ISFRIEND, 0);
        values.put(KEY_NOTENAME, "");
        values.put(KEY_NOTETEXT, "");
        return CRUDMethods.updateSync(UserTable.USERS_TABLE_NAME, values, KEY_ISFRIEND + "=1", null);
    }

    private static Task<Boolean> setDefaultAppkeyToDatabaseInBackground(String username) {
        if (!CommonUtils.isInited("UserInfoStorage.setDefaultAppkeyToDatabaseInBackground")) {
            return Task.forResult(false);
        }
        if (null == username) {
            Logger.ww(TAG, "set default appkey to database conversation failed . username = null");
            return Task.forResult(false);
        }

        ContentValues contentValues = new ContentValues();
        contentValues.put(KEY_APPKEY, JCoreInterface.getAppKey());
        return CRUDMethods.updateAsync(UserTable.USERS_TABLE_NAME, contentValues,
                StringUtils.createSelectionWithAnd(KEY_USERNAME), new String[]{username});
    }

}
