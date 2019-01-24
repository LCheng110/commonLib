package cn.jpush.im.android.utils;

import android.database.sqlite.SQLiteDatabase;
import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetUserInfoListTask;
import cn.jpush.im.api.BasicCallback;


public class UserIDHelper {

    public static final String TAG = "UserIDHelper";

    //用于分隔username和appkey的符号
    private static final String USERKEY_SEPERATOR = File.separator;

    //UserKey组成： username + USERKEY_SEPERATOR + appkey。
    private static Map<String, Long> userKeyToIDMap = new HashMap<String, Long>();

    private static Map<Long, String> idToUserKeyMap = new HashMap<Long, String>();

    /**
     * 通过userName来查找获取userID，首先从内存map中查，如果查不到则从数据库中查。
     *
     * @return 若内存和数据库中均查不到则返回0
     */
    public static Long getUserIDFromLocal(String userName, String appkey) {
        long userID;
        if (getUidInCache(userName, appkey) == 0) {
            Logger.d(TAG, "can not find userID from map,query from database! username = " + userName + " appkey = " + appkey);
            userID = UserInfoManager.getInstance().queryUserID(userName, appkey);
            if (userID != 0) { //数据库中存在，则将id保存至map
                updateIDInCache(userName, appkey, userID);
            }
        } else {
            userID = getUidInCache(userName, appkey);
        }
        return userID;
    }

    public static Long getUserIDFromLocal(SQLiteDatabase db, String userName, String appkey) {
        long userID;
        if (getUidInCache(userName, appkey) == 0) {
            Logger.d(TAG, "can not find userID from map,query from database!");
            userID = UserInfoManager.getInstance().queryUserID(db, userName);
            if (userID != 0) { //数据库中存在，则将id保存至map
                updateIDInCache(userName, appkey, userID);
            }
        } else {
            userID = getUidInCache(userName, appkey);
        }
        return userID;
    }

    public static List<Long> getUserIDsFromLocal(List<String> usernames, String appkey) {
        List<Long> userIds = null;
        if (null != usernames) {
            userIds = new ArrayList<Long>();
            for (String username : usernames) {
                userIds.add(getUserIDFromLocal(username, appkey));
            }
        }
        return userIds;
    }

    /*
    数据库升级时
     */
    public static List<Long> getUserIDsFromLocal(SQLiteDatabase database, List<String> usernames, String appkey) {
        List<Long> userIds = null;
        if (null != usernames) {
            userIds = new ArrayList<Long>();
            for (String username : usernames) {
                Logger.d(TAG, "getUserIDsFromLocal username = " + username);
                userIds.add(getUserIDFromLocal(database, username, appkey));
                Logger.d(TAG, "getUserIDsFromLocal username = " + username);
            }
        }
        return userIds;
    }

    public static String getUserNameFromLocal(long userID) {
        String userName = getUsernameInCache(userID);
        String appkey;
        if (userName == null) {
            Logger.d(TAG, "can not find userName from map,query from database! uid = " + userID);
            String results[] = UserInfoManager.getInstance().queryUsernameAndAppkey(userID);
            userName = results[0];
            appkey = results[1];
            if (null != userName && null != appkey) {
                updateUserKeyInCache(userID, userName, appkey);
            }
        }
        return userName;
    }

    public static List<String> getUserNamesFromLocal(List<Long> userIDs) {
        List<String> userNames = null;
        if (null != userIDs) {
            userNames = new ArrayList<String>();
            for (Long userId : userIDs) {
                userNames.add(getUserNameFromLocal(userId));
            }
        }
        return userNames;
    }

    public static String getUserAppkeyFromLocal(long userID) {
        String userName;
        String appkey = getUserAppkeyInCache(userID);
        if (appkey == null) {
            Logger.d(TAG, "can not find appkey from map,query from database! uid = " + userID);
            String results[] = UserInfoManager.getInstance().queryUsernameAndAppkey(userID);
            userName = results[0];
            appkey = results[1];
            if (null != userName && null != appkey) {
                updateUserKeyInCache(userID, userName, appkey);
            }
        }
        return appkey;
    }

    public static void getUserID(String username, String appkey, GetUseridsCallback callback) {
        List<String> usernames = new ArrayList<String>();
        usernames.add(username);
        getUserIDs(usernames, appkey, callback);
    }

    public static void getUserIDs(final Collection<String> userNames, String appkey, final GetUseridsCallback callback) {
        if (null == userNames) {
            Logger.ww(TAG, "userNames list is null . can not get usernames.");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }
        //若传入的appkey为空则使用本应用appkey
        if (TextUtils.isEmpty(appkey)) {
            appkey = JCoreInterface.getAppKey();
        }

        final Map<String, Long> usernameToIDMap = new LinkedHashMap<String, Long>();
        final List<Long> userIDs = new ArrayList<Long>();
        ArrayList<Object> usersNotFoundInLocal = new ArrayList<Object>();
        for (final String userName : userNames) {
            long userID = getUserIDFromLocal(userName, appkey);
            if (userID == 0) {
                Logger.d(TAG, "can not find " + userName + " from local,get user info from server!");
                usernameToIDMap.put(userName, 0l);
                usersNotFoundInLocal.add(userName);
            } else {
                usernameToIDMap.put(userName, userID);
            }
        }

        if (usersNotFoundInLocal.size() != 0) {
            new GetUserInfoListTask(usersNotFoundInLocal, appkey, new GetUserInfoListCallback() {
                @Override
                public void gotResult(int code, String msg, List<UserInfo> userList) {
                    if (code == 0 && null != userList) {
                        for (UserInfo userInfo : userList) {
                            usernameToIDMap.put(userInfo.getUserName(), userInfo.getUserID());
                        }
                        Collection<Long> values = usernameToIDMap.values();
                        if (values.contains(0L)) {
                            //如果有0，则表示username中有用户名不存在，直接返回null
                            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS_DESC);
                        }
                        userIDs.addAll(values);
                        Logger.d(TAG, "getUserIDs finished,userIDs = " + userIDs + " map = " + usernameToIDMap);
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userIDs);
                    } else {
                        Logger.ww(TAG, "get userInfo list failed ! code = " + code + " msg = " + msg);
                        CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                    }
                }
            }, false).execute();
        } else {
            Collection<Long> values = usernameToIDMap.values();
            if (values.contains(0l)) {
                //如果有0，则表示username中有用户名不存在，直接返回null
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS_DESC);
                return;
            }
            userIDs.addAll(values);
            Logger.d(TAG, "getUserIDs finished,userIDs = " + userIDs + " map = " + usernameToIDMap);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userIDs);
        }
    }


    public static void getUsername(long uid, GetUsernamesCallback callback) {
        List<Long> uids = new ArrayList<Long>();
        uids.add(uid);
        getUserNames(uids, callback);
    }

    public static void getUserNames(final Collection<Long> userIDs, final GetUsernamesCallback callback) {
        if (null == userIDs) {
            Logger.ww(TAG, "userIDs list is null . can not get usernames.");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            return;
        }

        final Map<Long, String> idToUsernameMap = new LinkedHashMap<Long, String>();
        final List<String> userNames = new ArrayList<String>();
        ArrayList<Object> usersNotFoundInLocal = new ArrayList<Object>();
        for (final long userID : userIDs) {
            String userName = getUserNameFromLocal(userID);
            if (null == userName) {
                Logger.d(TAG, "can not find " + userID + " from local,get user info from server!");
                idToUsernameMap.put(userID, "");//本地未找到匹配id的name，放一个空字符串到map中以保证顺序
                usersNotFoundInLocal.add(userID);
            } else {
                idToUsernameMap.put(userID, userName);
            }
        }

        if (usersNotFoundInLocal.size() != 0) {
            new GetUserInfoListTask(usersNotFoundInLocal, GetUserInfoListTask.IDType.uid, new GetUserInfoListCallback() {
                @Override
                public void gotResult(int code, String msg, List<UserInfo> userList) {
                    if (code == 0 && null != userList) {
                        for (UserInfo userInfo : userList) {
                            idToUsernameMap.put(userInfo.getUserID(), userInfo.getUserName());
                        }
                        Collection<String> values = idToUsernameMap.values();
                        if (values.contains("")) {
                            //如果包含了空字符串，表示有用户id不存在,
                            Logger.ww(TAG, "some user not find with given uid.");
                            //这里如果包含了没有找到对应username的uid,则说明是uid在服务器端被删除了，服务器调过了这个username的返回，此时也应该将这个uid从idToUsernameMap中删除
                            values.remove("");
//                            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS, ErrorCode.LOCAL_ERROR.LOCAL_USER_NOT_EXISTS_DESC);
                        }
                        userNames.addAll(values);
                        Logger.d(TAG, "getUserNames finished,userNames = " + userNames);
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userNames);
                    } else {
                        Logger.ww(TAG, "get userInfo list failed ! code = " + code + " msg = " + msg);
                        CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                    }
                }
            }, false).execute();
        } else {
            userNames.addAll(idToUsernameMap.values());
            Logger.d(TAG, "getUserNames finished,userNames = " + userNames);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userNames);
        }
    }

    public static void getUserInfo(long uid, GetUserInfoListCallback callback) {
        List<Long> uids = new ArrayList<Long>();
        uids.add(uid);
        getUserInfos(uids, callback);
    }

    public static void getUserInfos(final Collection<Long> userIDs, final GetUserInfoListCallback callback) {
        if (null == userIDs) {
            Logger.ww(TAG, "userIDs list is null. can not get usernames");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }

        final Map<Long, UserInfo> idToUserInfoMap = new LinkedHashMap<Long, UserInfo>();
        final List<UserInfo> userInfos = new ArrayList<UserInfo>();
        ArrayList<Object> usersNotFoundInLocal = new ArrayList<Object>();
        for (final long userID : userIDs) {
            UserInfo userInfo = UserInfoManager.getInstance().getUserInfo(userID);
            if (null == userInfo) {
                Logger.d(TAG, "can not find " + userID + " from local,get user info from server!");
                idToUserInfoMap.put(userID, null);//本地未找到匹配id的userInfo，放一个null到map中以保证顺序
                usersNotFoundInLocal.add(userID);
            } else {
                idToUserInfoMap.put(userID, userInfo);
            }
        }

        if (usersNotFoundInLocal.size() != 0) {
            new GetUserInfoListTask(usersNotFoundInLocal, GetUserInfoListTask.IDType.uid, new GetUserInfoListCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
                    if (responseCode == 0 && null != userInfoList) {
                        for (UserInfo userInfo : userInfoList) {
                            idToUserInfoMap.put(userInfo.getUserID(), userInfo);
                        }
                        Collection<UserInfo> values = idToUserInfoMap.values();
                        if (values.contains(null)) {
                            //如果包含了null，表示有用户id不存在
                            Logger.ww(TAG, "some user not find with given uid.");
                            //这里如果包含了没有找到对应userInfo的uid,则说明是uid在服务器端被删除了，服务器调过了这个userInfo的返回，此时也应该将这个uid从idToUserInfoMap中删除
                            values.remove(null);
                        }
                        userInfos.addAll(values);

                        if (userInfos.size() == 0) {
                            Logger.ww(TAG, "userInfo not found");
                            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.HTTP_ERROR.HTTP_SERVER_USER_INFO_NOT_FOUND_ERROR,
                                    ErrorCode.HTTP_ERROR.HTTP_SERVER_USER_INFO_NOT_FOUND_ERROR_DESC);
                        } else {
                            Logger.d(TAG, "getUserInfos finshed, userInfos = " + userInfos);
                            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfos);
                        }
                    } else {
                        Logger.ww(TAG, "get userInfo list failed ! code = " + responseCode + " msg = " + responseMessage);
                        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
                    }
                }
            }, false).execute();
        } else {
            userInfos.addAll(idToUserInfoMap.values());
            Logger.d(TAG, "getUserInfos finshed, userInfos = " + userInfos);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfos);
        }
    }

    public static long getUidInCache(String username, String appkey) {
        if (null == userKeyToIDMap.get(createUserKey(username, appkey))) {
            return 0;
        } else {
            return userKeyToIDMap.get(createUserKey(username, appkey));
        }
    }

    public static String getUsernameInCache(long uid) {
        String userKey = idToUserKeyMap.get(uid);
        if (null != userKey) {
            return getUsernameFromUserKey(userKey);
        }
        return null;
    }

    public static String getUserAppkeyInCache(long uid) {
        String userKey = idToUserKeyMap.get(uid);
        if (null != userKey) {
            return getAppkeyFromUserKey(userKey);
        }
        return null;
    }

    public static void updateIDInCache(String userName, String appkey, long userID) {
        String userKey = createUserKey(userName, appkey);
        userKeyToIDMap.put(userKey, userID);
    }

    public static void updateUserKeyInCache(long userID, String userName, String appkey) {
        String userKey = createUserKey(userName, appkey);
        idToUserKeyMap.put(userID, userKey);
    }

    public static void clearCachedMaps() {
        userKeyToIDMap.clear();
        idToUserKeyMap.clear();
    }

    private static String createUserKey(String username, String appkey) {
        return username + USERKEY_SEPERATOR + appkey;
    }

    private static String getUsernameFromUserKey(String userKey) {
        return userKey != null ? userKey.split(USERKEY_SEPERATOR)[0] : null;
    }

    private static String getAppkeyFromUserKey(String userKey) {
        return userKey != null ? userKey.split(USERKEY_SEPERATOR)[1] : null;
    }

    public static abstract class GetUsernamesCallback extends BasicCallback {
        protected GetUsernamesCallback() {
        }

        protected GetUsernamesCallback(boolean isRunInUIThread) {
            super(isRunInUIThread);
        }

        @Override
        public void gotResult(int i, String s) {
            Logger.ee(TAG, "should not reach here!");
        }

        public abstract void gotResult(int code, String msg, List<String> usernames);

        @Override
        public void gotResult(int responseCode, String responseMessage, Object... result) {
            List<String> usernames = null;
            if (null != result && result.length > 0 && null != result[0]) {
                usernames = (List<String>) result[0];
            }
            gotResult(responseCode, responseMessage, usernames);
        }
    }

    public static abstract class GetUseridsCallback extends BasicCallback {
        protected GetUseridsCallback() {
        }

        protected GetUseridsCallback(boolean isRunInUIThread) {
            super(isRunInUIThread);
        }

        @Override
        public void gotResult(int i, String s) {
            Logger.ee(TAG, "should not reach here!");
        }

        public abstract void gotResult(int code, String msg, List<Long> userids);

        @Override
        public void gotResult(int responseCode, String responseMessage, Object... result) {
            List<Long> userids = null;
            if (null != result && result.length > 0 && null != result[0]) {
                userids = (List<Long>) result[0];
            }
            gotResult(responseCode, responseMessage, userids);
        }
    }
}
