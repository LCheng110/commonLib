package cn.jpush.im.android.storage;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.bolts.Continuation;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.table.GroupTable;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.FileUtil;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

public class GroupStorage {

    private static final String TAG = "GroupStorage";

    public static final String GROUP_ID = "group_id";

    @Deprecated
    //use GROUP_OWNER_ID instead.
    public static final String GROUP_OWNER = "group_owner";

    public static final String GROUP_OWNER_ID = "group_owner_id";

    public static final String GROUP_NAME = "group_name";

    public static final String GROUP_DESC = "group_desc";

    public static final String GROUP_LEVEL = "group_level";

    public static final String GROUP_FLAG = "group_flag";

    public static final String GROUP_MEMBERS = "group_members";

    public static final String GROUP_NODISTURB = "nodisturb";

    public static final String MAX_MEMBER_COUNT = "max_member_count";

    public static final String GROUP_BLOCKED = "group_blocked";

    public static final String GROUP_AVATAR = "avatar";


    public static Task<Boolean> insertOrUpdateWhenExistsInBackground(final InternalGroupInfo groupInfo, final boolean needUpdateNoDisturb, final boolean needUpdateBlock) {
        if (null == groupInfo) {
            Logger.ww(TAG, "insertInBackground or updateInBackground group info failed. group info is null");
            return Task.forResult(false);
        }

        return isExistInBackground(groupInfo.getGroupID()).onSuccessTask(new Continuation<Boolean, Task<Boolean>>() {
            @Override
            public Task<Boolean> then(Task<Boolean> task) throws Exception {
                if (task.getResult()) {
                    return updateAllValuesInBackground(groupInfo, needUpdateNoDisturb, needUpdateBlock);
                } else {
                    return insertInBackground(groupInfo).onSuccess(new Continuation<Long, Boolean>() {
                        @Override
                        public Boolean then(Task<Long> task) throws Exception {
                            return task.getResult() > 0;
                        }
                    });
                }
            }
        });

    }

    public static boolean insertOrUpdateWhenExistsSync(final InternalGroupInfo groupInfo, final boolean needUpdateNoDisturb, final boolean needUpdateBlock) {
        if (null == groupInfo) {
            Logger.ww(TAG, "insertInBackground or updateInBackground group info failed. group info is null");
            return false;
        }

        if (isExistSync(groupInfo.getGroupID())) {
            return updateAllValuesSync(groupInfo, needUpdateNoDisturb, needUpdateBlock);
        } else {
            return insertSync(groupInfo) > 0;
        }
    }

    //此处insert group info时,不包含group的member和owner属性
    private static Task<Long> insertInBackground(final InternalGroupInfo groupInfo) {
        final ContentValues values = infoToValues(groupInfo, true, true);
        return CRUDMethods.insertAsync(GroupTable.GROUP_TABLE_NAME, values, SQLiteDatabase.CONFLICT_IGNORE).onSuccess(new Continuation<Long, Long>() {
            @Override
            public Long then(Task<Long> task) throws Exception {
                long result = task.getResult();
                if (result > 0) {
                    updateAttrsInConversation(groupInfo.getGroupID(), values);
                }
                return task.getResult();
            }
        });
    }

    public static long insertSync(final InternalGroupInfo groupInfo) {

        final ContentValues values = infoToValues(groupInfo, true, true);
        long id = CRUDMethods.insertSync(GroupTable.GROUP_TABLE_NAME, values, SQLiteDatabase.CONFLICT_IGNORE);

        if (id > 0) {
            updateAttrsInConversation(groupInfo.getGroupID(), values);
        }
        return id;
    }

    private static InternalGroupInfo cursorToGroupInfo(Cursor cursor) {
        if (null == cursor) {
            return null;
        }
        InternalGroupInfo groupInfo = new InternalGroupInfo();
        groupInfo.set_id(cursor.getInt(cursor.getColumnIndex("_id")));
        groupInfo.setGroupID(cursor.getLong(cursor.getColumnIndex(GROUP_ID)));
        groupInfo.setGroupOwner(cursor.getString(cursor.getColumnIndex(GROUP_OWNER)));
        groupInfo.setGroupName(cursor.getString(cursor.getColumnIndex(GROUP_NAME)));
        groupInfo.setGroupDescription(cursor.getString(cursor.getColumnIndex(GROUP_DESC)));
        groupInfo.setGroupLevel(cursor.getInt(cursor.getColumnIndex(GROUP_LEVEL)));
        groupInfo.setGroupFlag(cursor.getInt(cursor.getColumnIndex(GROUP_FLAG)));
        groupInfo.setOwnerId(cursor.getLong(cursor.getColumnIndex(GROUP_OWNER_ID)));
        groupInfo.setNoDisturbInLocal(cursor.getInt(cursor.getColumnIndex(GROUP_NODISTURB)));
        groupInfo.setMaxMemberCount(cursor.getInt(cursor.getColumnIndex(MAX_MEMBER_COUNT)));
        groupInfo.setBlockGroupInLocal(cursor.getInt(cursor.getColumnIndex(GROUP_BLOCKED)));
        groupInfo.setGroupMemberUserIds(cursorToGroupMembers(cursor));
        groupInfo.setAvatarMediaID(cursor.getString(cursor.getColumnIndex(GROUP_AVATAR)));
        return groupInfo;
    }

    private static Set<Long> cursorToGroupMembers(Cursor cursor) {
        if (null == cursor) {
            return null;
        }
        Set<Long> members;
        String membersString = cursor.getString(cursor.getColumnIndex(GROUP_MEMBERS));
        members = JsonUtil.formatToGivenType(membersString, new TypeToken<Set<Long>>() {
        });
        return members == null ? new LinkedHashSet<Long>() : members;//确保给到上层的member集合不是null
    }

    public static Task<List<Long>> queryIDListInBackground() {
        if (!CommonUtils.isInited("GroupInfo.queryIDListInBackground")) {
            return Task.forResult(null);
        }

        String sql = "select _id," + GROUP_ID + " from " + GroupTable.GROUP_TABLE_NAME;
        return CRUDMethods.rawQueryAsync(sql, null).onSuccess(new Continuation<Cursor, List<Long>>() {
            @Override
            public List<Long> then(Task<Cursor> task) throws Exception {
                return queryIDListInternal(task.getResult());
            }
        });
    }

    public static List<Long> queryIDListSync() {
        if (!CommonUtils.isInited("GroupInfo.queryIDListInBackground")) {
            return null;
        }

        String sql = "select _id," + GROUP_ID + " from " + GroupTable.GROUP_TABLE_NAME;
        return queryIDListInternal(CRUDMethods.rawQuerySync(sql, null));
    }

    private static List<Long> queryIDListInternal(Cursor cursor) {
        List<Long> groupIDList = null;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                groupIDList = new ArrayList<Long>();
                while (cursor.moveToNext()) {
                    groupIDList.add(cursor.getLong(cursor.getColumnIndex(GROUP_ID)));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return groupIDList;
    }

    public static Task<InternalGroupInfo> queryInfoInBackground(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.queryInfoInBackground")) {
            return Task.forResult(null);
        }
        String sql = "select * from " + GroupTable.GROUP_TABLE_NAME + " where " + GROUP_ID + "=?";
        String[] arg = new String[]{String.valueOf(groupID)};
        return CRUDMethods.rawQueryAsync(sql, arg).onSuccess(new Continuation<Cursor, InternalGroupInfo>() {
            @Override
            public InternalGroupInfo then(Task<Cursor> task) throws Exception {
                return queryInfoInternal(task.getResult());
            }
        });
    }

    public static InternalGroupInfo queryInfoSync(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.queryInfoInBackground")) {
            return null;
        }
        String sql = "select * from " + GroupTable.GROUP_TABLE_NAME + " where " + GROUP_ID + "=?";
        String[] arg = new String[]{String.valueOf(groupID)};

        Cursor cursor = CRUDMethods.rawQuerySync(sql, arg);
        return queryInfoInternal(cursor);
    }

    private static InternalGroupInfo queryInfoInternal(Cursor cursor) {
        InternalGroupInfo groupInfo = null;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    groupInfo = cursorToGroupInfo(cursor);
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return groupInfo;
    }

    public static Task<List<Long>> queryMemberUserIdsInBackground(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.queryMemberUserIdsInBackground")) {
            return Task.forResult(null);
        }
        String sql = "select " + GROUP_MEMBERS + " from " + GroupTable.GROUP_TABLE_NAME + " where "
                + GROUP_ID + "=?";
        String[] args = new String[]{String.valueOf(groupID)};
        return CRUDMethods.rawQueryAsync(sql, args).onSuccess(new Continuation<Cursor, List<Long>>() {
            @Override
            public List<Long> then(Task<Cursor> task) throws Exception {
                return queryMemberUserIdsInternal(task.getResult());
            }
        });
    }

    public static List<Long> queryMemberUserIdsSync(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.queryMemberUserIdsSync")) {
            return null;
        }
        String sql = "select " + GROUP_MEMBERS + " from " + GroupTable.GROUP_TABLE_NAME + " where "
                + GROUP_ID + "=?";
        String[] args = new String[]{String.valueOf(groupID)};
        Cursor cursor = CRUDMethods.rawQuerySync(sql, args);
        return queryMemberUserIdsInternal(cursor);
    }

    private static List<Long> queryMemberUserIdsInternal(Cursor cursor) {
        List<Long> members = null;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                members = new ArrayList<Long>();
                if (cursor.moveToNext()) {
                    Set<Long> localMembers = cursorToGroupMembers(cursor);
                    if (null != localMembers) {
                        members.addAll(localMembers);
                    }
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return members;
    }

    public static Task<Long> queryOwnerIdInBackground(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.queryOwnerIdInBackground")) {
            return Task.forResult(0L);
        }

        String sql = "select " + GROUP_OWNER_ID + " from " + GroupTable.GROUP_TABLE_NAME + " where "
                + GROUP_ID + "=?";
        String[] args = new String[]{String.valueOf(groupID)};
        return CRUDMethods.rawQueryAsync(sql, args).onSuccess(new Continuation<Cursor, Long>() {
            @Override
            public Long then(Task<Cursor> task) throws Exception {
                return queryOwnerIDInternal(task.getResult());
            }
        });
    }

    public static long queryOwnerIdSync(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.queryOwnerIdSync")) {
            return 0L;
        }

        String sql = "select " + GROUP_OWNER_ID + " from " + GroupTable.GROUP_TABLE_NAME + " where "
                + GROUP_ID + "=?";
        String[] args = new String[]{String.valueOf(groupID)};
        return queryOwnerIDInternal(CRUDMethods.rawQuerySync(sql, args));
    }

    private static long queryOwnerIDInternal(Cursor cursor) {
        long ownerId = 0L;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    ownerId = cursor.getLong(cursor.getColumnIndex(GROUP_OWNER_ID));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return ownerId;
    }

    public static Task<Integer> queryIntValueInBackground(long groupID, final String columnName) {
        if (!CommonUtils.isInited("GroupInfo.queryOwnerIdInBackground")) {
            return Task.forResult(0);
        }

        String sql = "select " + columnName + " from " + GroupTable.GROUP_TABLE_NAME + " where "
                + GROUP_ID + "=?";
        String[] args = new String[]{String.valueOf(groupID)};
        return CRUDMethods.rawQueryAsync(sql, args).onSuccess(new Continuation<Cursor, Integer>() {
            @Override
            public Integer then(Task<Cursor> task) throws Exception {
                return queryIntValueInternal(task.getResult(), columnName);
            }
        });
    }

    public static int queryIntValueSync(long groupID, final String columnName) {
        if (!CommonUtils.isInited("GroupInfo.queryOwnerIdSync")) {
            return 0;
        }

        String sql = "select " + columnName + " from " + GroupTable.GROUP_TABLE_NAME + " where "
                + GROUP_ID + "=?";
        String[] args = new String[]{String.valueOf(groupID)};
        return queryIntValueInternal(CRUDMethods.rawQuerySync(sql, args), columnName);
    }

    private static int queryIntValueInternal(Cursor cursor, String columnName) {
        int intValue = 0;
        if (cursor != null && cursor.getCount() > 0) {
            try {
                if (cursor.moveToNext()) {
                    intValue = cursor.getInt(cursor.getColumnIndex(columnName));
                }
            } finally {
                cursor.close();
            }
        } else if (cursor != null) {
            cursor.close();
        }
        return intValue;
    }

    public static Task<Boolean> isExistInBackground(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.isExistInBackground")) {
            return Task.forResult(false);
        }

        String sql = "select count(*) as count from " + GroupTable.GROUP_TABLE_NAME + " where " + GROUP_ID + " = ?";
        String groupIDString = String.valueOf(groupID);
        String[] args = new String[]{groupIDString};
        return CRUDMethods.rawQueryAsync(sql, args).onSuccess(new Continuation<Cursor, Boolean>() {
            @Override
            public Boolean then(Task<Cursor> task) throws Exception {
                return isExistInternal(task.getResult());
            }
        });
    }

    public static boolean isExistSync(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.queryExistSync")) {
            return false;
        }

        String sql = "select count(*) as count from " + GroupTable.GROUP_TABLE_NAME + " where " + GROUP_ID + " = ?";
        String groupIDString = String.valueOf(groupID);
        String[] args = new String[]{groupIDString};
        return isExistInternal(CRUDMethods.rawQuerySync(sql, args));
    }

    private static boolean isExistInternal(Cursor cursor) {
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
        Logger.i(TAG, "GroupStorage.queryExistInBackground  rowCount = " + rowCount);
        return rowCount > 0;
    }

    public static Task<Boolean> updateValuesInBackground(final long groupID, final ContentValues values) {
        if (!CommonUtils.isInited("GroupInfo.updateValuesInBackground")) {
            return Task.forResult(false);
        }

        if (null == values) {
            Logger.ww(TAG, "[updateValuesInBackground] invalid parameters! contentValues is null");
            return Task.forResult(false);
        }

        return CRUDMethods.updateAsync(GroupTable.GROUP_TABLE_NAME, values, GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)}).onSuccess(new Continuation<Boolean, Boolean>() {
            @Override
            public Boolean then(Task<Boolean> task) throws Exception {
                boolean result = task.getResult();
                Logger.d(TAG, "update group info result is " + result + " values = " + values);
                if (result) {
                    updateAttrsInConversation(groupID, values);
                }
                return result;
            }
        });
    }

    public static boolean updateValuesSync(long groupID, ContentValues values) {
        if (!CommonUtils.isInited("GroupInfo.updateValuesInBackground")) {
            return false;
        }

        if (null == values) {
            Logger.ww(TAG, "[updateValuesInBackground] invalid parameters! contentValues is null");
            return false;
        }

        boolean result = CRUDMethods.updateSync(GroupTable.GROUP_TABLE_NAME, values, GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)});
        if (result) {
            updateAttrsInConversation(groupID, values);
        }
        return result;
    }

    /**
     * 更新所有gid下对应的值
     *
     * @param gids   被更新的gid集合
     * @param values 被更新的值
     * @return
     */
    public static boolean updateAllGidsWithValue(Collection<Long> gids, ContentValues values) {
        if (null == gids || gids.isEmpty() || null == values) {
            Logger.i(TAG, "[updateValuesInBackground] invalid parameters!");
            return false;
        }

        if (!CommonUtils.isInited("GroupInfo.updateValuesInBackground")) {
            return false;
        }

        boolean result = CRUDMethods.updateSync(GroupTable.GROUP_TABLE_NAME, values, StringUtils.createListSelection(GROUP_ID, gids), null);
        if (result) {
            for (long gid : gids) {
                updateAttrsInConversation(gid, values);
            }
        }
        return result;
    }

    private static Task<Boolean> updateAllValuesInBackground(InternalGroupInfo groupInfo, boolean needUpdateNoDisturb, boolean needUpdateBlock) {
        if (null == groupInfo) {
            Logger.ww(TAG, "updateInBackground all values failed. group info is null");
            return Task.forResult(false);
        }
        return updateValuesInBackground(groupInfo.getGroupID(), infoToValues(groupInfo, needUpdateNoDisturb, needUpdateBlock));
    }

    private static boolean updateAllValuesSync(InternalGroupInfo groupInfo, boolean needUpdateNoDisturb, boolean needUpdateBlock) {
        if (null == groupInfo) {
            Logger.ww(TAG, "updateInBackground all values failed. group info is null");
            return false;
        }
        return updateValuesSync(groupInfo.getGroupID(), infoToValues(groupInfo, needUpdateNoDisturb, needUpdateBlock));
    }

    private static ContentValues infoToValues(InternalGroupInfo groupInfo, boolean needUpdateNoDisturb, boolean needUpdateBlock) {
        ContentValues values = new ContentValues();

        //将groupinfo中字段转为values. 其中不包括groupmember 和group owner。因为两个字段请求groupinfo时
        //不会返回，只有在单独请求group member时才会单独更新。
        values.put(GroupStorage.GROUP_ID, groupInfo.getGroupID());
        values.put(GroupStorage.GROUP_NAME, groupInfo.getGroupName());
        values.put(GroupStorage.GROUP_DESC, groupInfo.getGroupDescription());
        values.put(GroupStorage.GROUP_LEVEL, groupInfo.getGroupLevel());
        values.put(GroupStorage.GROUP_FLAG, groupInfo.getGroupFlag());
        values.put(GroupStorage.MAX_MEMBER_COUNT, groupInfo.getMaxMemberCount());
        values.put(GROUP_AVATAR, groupInfo.getAvatar());
        //needUpdateLocalFields用来标示是否需要更新本地字段，通过服务器拿到的groupinfo中不包含
        //这些本地字段，处理服务器端get groupinfo返回的数据时，不应更新这些字段。
        if (needUpdateNoDisturb) {
            values.put(GROUP_NODISTURB, groupInfo.getNoDisturb());
        }
        if (needUpdateBlock) {
            values.put(GROUP_BLOCKED, groupInfo.isGroupBlocked());
        }
        return values;
    }

    //本地数据库中用户信息更新完成之后，需要同时更新缓存中会话的相关信息，以及缓存的会话中targetInfo相关的信息
    private static void updateAttrsInConversation(long groupID, ContentValues values) {
        if (values.containsKey(GROUP_NAME) && !TextUtils.isEmpty(values.getAsString(GROUP_NAME))) {
            //更新群信息中包含了groupName，则conversation中title也要更新
            Logger.d(TAG, "updateInBackground group name . conversation display name need be updated too!");
            ConversationManager.getInstance().updateConvsersationTitle(ConversationType.group,
                    String.valueOf(groupID), "", values.getAsString(GROUP_NAME));
        }

        if (values.containsKey(GROUP_MEMBERS)) {
            GroupInfo groupInfo = queryInfoSync(groupID);
            Conversation gConv = ConversationManager.getInstance().getGroupConversation(groupID);
            if (null != groupInfo
                    && TextUtils.isEmpty(groupInfo.getGroupName())
                    && null != gConv
                    && (TextUtils.isEmpty(gConv.getTitle())
                    || gConv.getTitle().equalsIgnoreCase(gConv.getTargetId())//如果会话title是群组的gid,说明创建会话时，群成员信息还没获取到，此时也要更新会话的title。
                    || groupInfo.getGroupMembers().size() <= 5)) {// TODO: 2017/8/7 这里应该通过群组的groupMemberIds数量来判断群成员数，而不是通过memberInfo的数量
                Logger.d(TAG, "group name not set, updateInBackground default conversation display name!");
                //如果没设置群名，而且群成员数不足5人，则需要更新Conversation的title
                ConversationManager.getInstance().updateConvsersationTitle(ConversationType.group,
                        String.valueOf(groupID), "", getGroupDefaultTitle(groupID, gConv.getTitle()));
            }
        }

        if (values.containsKey(GROUP_AVATAR)) {
            ConversationManager.getInstance().updateConvsersationAvatar(ConversationType.group, String.valueOf(groupID),
                    "", FileUtil.getBigAvatarFilePath(values.getAsString(GROUP_AVATAR)));
        }

        if (values.containsKey(GROUP_NODISTURB)) {
            //群组免打扰状态改变，直接将全局未读数重置。保证之后获取到的全局未读数是正确的。
            JMessage.resetAllUnreadMsgCnt();
        }
        ConversationManager.getInstance().updateGroupInfoInCache(groupID, values);//更新conv缓存里的targetInfo群信息
    }

    public static Task<Boolean> deleteInBackground(long groupID) {
        if (!CommonUtils.isInited("GroupInfo.deleteInBackground")) {
            return Task.forResult(false);
        }

        return CRUDMethods.deleteAsync(GroupTable.GROUP_TABLE_NAME, GROUP_ID + "=?",
                new String[]{String.valueOf(groupID)});
    }

    public static Task<Boolean> resetNodisturbStatusInBackground() {
        if (!CommonUtils.isInited("GroupInfo.resetBlacklistStatusInBackground")) {
            return Task.forResult(false);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(GROUP_NODISTURB, 0);
        return CRUDMethods.updateAsync(GroupTable.GROUP_TABLE_NAME, contentValues, GROUP_NODISTURB + "=1", null)
                .onSuccess(new Continuation<Boolean, Boolean>() {
                    @Override
                    public Boolean then(Task<Boolean> task) throws Exception {
                        boolean result = task.getResult();
                        if (result) {
                            ConversationManager.getInstance().resetNodisturbFlagsInCache();
                        }
                        return result;
                    }
                });
    }

    public static Task<Boolean> resetShieldingStatusInBackground() {
        if (!CommonUtils.isInited("resetShieldingStatusInBackground")) {
            return Task.forResult(false);
        }
        ContentValues contentValues = new ContentValues();
        contentValues.put(GROUP_BLOCKED, 0);
        return CRUDMethods.updateAsync(GroupTable.GROUP_TABLE_NAME, contentValues, GROUP_BLOCKED + "=1", null)
                .onSuccess(new Continuation<Boolean, Boolean>() {
                    @Override
                    public Boolean then(Task<Boolean> task) throws Exception {
                        boolean result = task.getResult();
                        if (result) {
                            ConversationManager.getInstance().resetShieldingFlagsInCache();
                        }
                        return result;
                    }
                });
    }


    /**
     * 获取群组默认的title，
     *
     * @param groupID      群组id
     * @param defaultTitle 默认title
     * @return 群组title
     */
    public static String getGroupDefaultTitle(long groupID, String defaultTitle) {
        StringBuilder displayName = new StringBuilder();
        List<Long> groupMemberUids = queryMemberUserIdsSync(groupID);
        if (null != groupMemberUids && groupMemberUids.size() > 5) {
            groupMemberUids = groupMemberUids.subList(0, 5);//只拿群组中前五个群成员的DisplayName
        }
        List<InternalUserInfo> selectedMembers = UserInfoManager.getInstance().getUserInfoList(groupMemberUids);
        if (null != selectedMembers && !selectedMembers.isEmpty() && !selectedMembers.contains(null)) {
            Iterator<InternalUserInfo> iterator = selectedMembers.iterator();
            while (iterator.hasNext()) {
                //群聊的title中展示备注名，
                displayName.append(iterator.next().getDisplayName(true));
                if (iterator.hasNext()) {
                    displayName.append(",");
                }
            }
        } else if (!TextUtils.isEmpty(defaultTitle)) {
            Logger.d(TAG, "do not have members yet , use default title.");
            displayName.append(defaultTitle);
        } else {
            Logger.d(TAG, "do not have members yet , use groupID as default title.");
            displayName.append(groupID);
        }
        return displayName.toString();
    }
}
