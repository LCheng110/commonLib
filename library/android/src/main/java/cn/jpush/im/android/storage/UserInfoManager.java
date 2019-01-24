package cn.jpush.im.android.storage;

import android.content.ContentValues;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.helpers.eventsync.Kind7EventsWrapper;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;

/**
 * userinfo管理类，
 * userinfo对象在内存中会有一份缓存，上层获取userinfo时，首先会从缓存中找。写入时也会先写缓存，再写入数据库
 * <p>
 * 因为有缓存机制，所以需要注意当userinfo的信息更新时，数据库和缓存这两个地方都需要同步更新
 */
public class UserInfoManager {
    private static final String TAG = "UserInfoManager";

    private static Map<Long, InternalUserInfo> userInfoCache = new ConcurrentHashMap<Long, InternalUserInfo>();
    private static UserInfoManager sInstance = null;

    private UserInfoManager() {

    }

    public synchronized static UserInfoManager getInstance() {
        if (null == sInstance) {
            sInstance = new UserInfoManager();
        }
        return sInstance;
    }

    public InternalUserInfo getUserInfo(String username, String appkey) {
        long uid = UserIDHelper.getUserIDFromLocal(username, appkey);
        InternalUserInfo info = getInfoFromCache(uid);
        if (null == info) {
            info = UserInfoStorage.queryInfoSync(uid);
            if (null != info) {
                putInfoToCache(info);
            }
        }
        return info;
    }

    public InternalUserInfo getUserInfo(long uid) {
        InternalUserInfo info = getInfoFromCache(uid);
        if (null == info) {
            info = UserInfoStorage.queryInfoSync(uid);
            if (null != info) {
                putInfoToCache(info);
            }
        }

        return info;
    }

    public List<InternalUserInfo> getUserInfoList(Collection<Long> uidList) {
        if (null != uidList) {
            //为了保证返回的userinfo的顺序是严格按照uidList的顺序，这里用一个LinkedHashMap来存储所有的uid和userinfo。
            Map<Long, InternalUserInfo> userInfoMap = new LinkedHashMap<Long, InternalUserInfo>();
            List<InternalUserInfo> userInfoList = new ArrayList<InternalUserInfo>();

            for (long uid : uidList) {
                InternalUserInfo userInfo = getInfoFromCache(uid);
                userInfoMap.put(uid, userInfo);
            }

            if (userInfoMap.containsValue(null)) {
                UserInfoStorage.queryInfosSync(userInfoMap);
                putInfoListToCache(userInfoMap);
            }

            userInfoList.addAll(userInfoMap.values());
            userInfoList.remove(null);//将其中为null的值remove掉。防止调用者出问题
            return userInfoList;
        }
        return null;
    }

    public boolean insertOrUpdateUserInfo(InternalUserInfo info, boolean needUpdateConversation, boolean needUpdateBlackList, boolean needUpdateNoDisturb, boolean needUpdateMemo) {
        if (null != info) {
            UserInfoStorage.insertOrUpdateWhenExistsInBackground(info, needUpdateConversation, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
            InternalUserInfo cachedInfo = getInfoFromCache(info.getUserID());
            if (null != cachedInfo) {
                cachedInfo.copyUserInfo(info, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
            }
            return true;
        }
        return false;
    }

    public boolean insertOrUpdateUserInfoSync(InternalUserInfo info, boolean needUpdateConversation, boolean needUpdateBlackList, boolean needUpdateNoDisturb, boolean needUpdateMemo) {
        boolean result = false;
        if (null != info) {
            result = UserInfoStorage.insertOrUpdateWhenExistsSync(info, needUpdateConversation, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
            if (result) {
                InternalUserInfo cachedInfo = getInfoFromCache(info.getUserID());
                if (null != cachedInfo) {
                    cachedInfo.copyUserInfo(info, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
                }
            }
        }
        return result;
    }

    public boolean insertOrUpdateUserInfo(List<InternalUserInfo> infos, boolean needUpdateConversation, boolean needUpdateBlackList, boolean needUpdateNoDisturb, boolean needUpdateMemo) {
        if (null != infos) {
            UserInfoStorage.insertOrUpdateWhenExistsInBackground(infos, needUpdateConversation, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
            for (InternalUserInfo userInfo : infos) {
                InternalUserInfo cachedInfo = getInfoFromCache(userInfo.getUserID());
                if (null != cachedInfo) {
                    cachedInfo.copyUserInfo(userInfo, needUpdateBlackList, needUpdateNoDisturb, needUpdateMemo);
                }
            }
            return true;
        }
        return false;
    }

    public List<UserInfo> getFriendList() {
        return UserInfoStorage.queryFriendListSync();
    }

    public boolean resetBlacklistStatus() {
        UserInfoStorage.resetBlacklistStatusSync();
        //将缓存中userinfo的blacklist属性重置。
        for (InternalUserInfo info : userInfoCache.values()) {
            if (null != info) {
                info.setBlacklist(0);
            }
        }
        return true;
    }

    public boolean resetNodisturbStatus() {
        UserInfoStorage.resetNodisturbStatusSync();
        //将缓存中userinfo的blacklist属性重置。
        for (InternalUserInfo info : userInfoCache.values()) {
            if (null != info) {
                info.setNoDisturbInLocal(0);
            }
        }
        return true;
    }

    public boolean resetFriendRelated() {
        UserInfoStorage.resetFriendRelatedSync();
        //将缓存中userinfo的isFriend属性重置。
        for (InternalUserInfo info : userInfoCache.values()) {
            if (null != info) {
                info.setIsFriend(false);
                info.setNotename("");
                info.setNoteText("");
            }
        }
        return true;
    }

    public boolean updateNickName(long uid, String nickname) {
        UserInfoStorage.updateNickNameInBackground(uid, nickname);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setNickname(nickname);
        }
        return true;
    }

    public boolean updateNoteName(long uid, String notename) {
        UserInfoStorage.updateNoteNameInBackground(uid, notename);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setNotename(notename);
        }
        return true;
    }

    public boolean updateNoteText(long uid, String notetext) {
        UserInfoStorage.updateNoteTextInBackground(uid, notetext);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setNoteText(notetext);
        }
        return true;
    }

    public boolean updateStar(long uid, boolean isStar) {
        UserInfoStorage.updateStarInBackground(uid, isStar);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setStar(isStar ? 1 : 0);
        }
        return true;
    }

    public boolean updateBlacklist(long uid, boolean isInBlacklist) {
        UserInfoStorage.updateBlackListInBackground(uid, isInBlacklist);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setBlacklist(isInBlacklist ? 1 : 0);
        }
        return true;
    }

    public boolean updateAllUidsBlacklistFlag(Collection<Long> uids, boolean isInBlacklist) {
        ContentValues values = new ContentValues();
        values.put(UserInfoStorage.KEY_BLACKLIST, isInBlacklist ? 1 : 0);
        UserInfoStorage.updateAllUidsWithValue(uids, values);
        for (long uid : uids) {
            InternalUserInfo cachedInfo = getInfoFromCache(uid);
            if (null != cachedInfo) {
                cachedInfo.setBlacklist(isInBlacklist ? 1 : 0);
            }
        }
        return true;
    }

    public boolean updateNoDisturb(long uid, boolean isInNoDisturb) {
        UserInfoStorage.updateNoDisturbInBackground(uid, isInNoDisturb);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setNoDisturbInLocal(isInNoDisturb ? 1 : 0);
        }
        return true;
    }

    public boolean updateAllUidsNoDisturbFlag(Collection<Long> uids, boolean isInNoDisturb) {
        ContentValues values = new ContentValues();
        values.put(UserInfoStorage.KEY_NODISTURB, isInNoDisturb ? 1 : 0);
        UserInfoStorage.updateAllUidsWithValue(uids, values);
        for (long uid : uids) {
            InternalUserInfo cachedInfo = getInfoFromCache(uid);
            if (null != cachedInfo) {
                cachedInfo.setNoDisturbInLocal(isInNoDisturb ? 1 : 0);
            }
        }
        return true;
    }

    public boolean updateSignature(long uid, String signature) {
        UserInfoStorage.updateSignatureInBackground(uid, signature);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setSignature(signature);
        }
        return true;
    }

    public boolean updateGender(long uid, UserInfo.Gender gender) {
        UserInfoStorage.updateGenderInBackground(uid, gender);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setGender(gender);
        }
        return true;
    }

    public boolean updateBirthday(long uid, String birthday) {
        UserInfoStorage.updateBirthdayInBackground(uid, birthday);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setBirthdayString(birthday);
        }
        return true;
    }

    public boolean updateRegion(long uid, String region) {
        UserInfoStorage.updateRegionInBackground(uid, region);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setRegion(region);
        }
        return true;
    }

    public boolean updateAddress(long uid, String address) {
        UserInfoStorage.updateAddressInBackground(uid, address);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setAddress(address);
        }
        return true;
    }

    public void updateAllPublicInfos(long uid, Map<String, Object> map) {
        ContentValues values = new ContentValues();
        for (Map.Entry<String, Object> entry :
                map.entrySet()) {
            if (entry.getKey() == UserInfo.Field.extras.toString()) {
                values.put(entry.getKey(), JsonUtil.toJson(entry.getValue()));
            } else {
                values.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
        }
        UserInfoStorage.updateInBackground(values, uid);

        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setAddress((String) map.get(UserInfo.Field.address.toString()));
            cachedInfo.setBirthdayString((String) map.get(UserInfo.Field.birthday.toString()));
            cachedInfo.setGender(UserInfo.Gender.get((Integer) map.get(UserInfo.Field.gender.toString())));
            cachedInfo.setRegion((String) map.get(UserInfo.Field.region.toString()));
            cachedInfo.setNickname((String) map.get(UserInfo.Field.nickname.toString()));
            cachedInfo.setSignature((String) map.get(UserInfo.Field.signature.toString()));
            cachedInfo.setUserExtras((Map<String, String>) map.get(UserInfo.Field.extras.toString()));
        }

    }

    public boolean updateAvatar(long uid, String mediaID) {
        UserInfoStorage.updateAvatarInBackground(uid, mediaID);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setAvatarMediaID(mediaID);
        }
        return true;
    }

    public boolean updateExtras(long uid, String extrasJson) {
        UserInfoStorage.updateExtrasInBackground(uid, extrasJson);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setExtrasFromJson(extrasJson);
        }
        return true;
    }

    public boolean updateIsFriendFlag(long uid, boolean isFriend) {
        UserInfoStorage.updateIsFriendInBackground(uid, isFriend ? 1 : 0);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setIsFriend(isFriend);
        }
        return true;
    }

    /**
     * 将给定map中所有用户加入到好友关联的信息中去，包括friendFlag,noteName,NoteText。
     *
     * @param contactsInfoMap key - uid,value - ContactInfoEntity 是对kind 7 description的解析结果
     * @return
     */
    public boolean addAllUidsFriendRelatedInfo(final Map<Long, Kind7EventsWrapper.ContactInfoEntity> contactsInfoMap) {
        if (null == contactsInfoMap) {
            return false;
        }
        final ContentValues values = new ContentValues();
        values.put(UserInfoStorage.KEY_ISFRIEND, 1);
        CRUDMethods.execInTransactionAsync(new CRUDMethods.TransactionCallback<Void>() {
            @Override
            public Void execInTransaction() {
                Set<Map.Entry<Long, Kind7EventsWrapper.ContactInfoEntity>> entries = contactsInfoMap.entrySet();
                for (Map.Entry<Long, Kind7EventsWrapper.ContactInfoEntity> entry : entries) {
                    long uid = entry.getKey();
                    Kind7EventsWrapper.ContactInfoEntity contactInfo = entry.getValue();
                    if (null != contactInfo && null != contactInfo.memo_name) {
                        values.put(UserInfoStorage.KEY_NOTENAME, contactInfo.memo_name);
                    }
                    if (null != contactInfo && null != contactInfo.memo_others) {

                        values.put(UserInfoStorage.KEY_NOTETEXT, contactInfo.memo_others);
                    }
                    UserInfoStorage.updateInBackground(values, uid);

                    //更新缓存中userinfo相关信息
                    InternalUserInfo cachedInfo = getInfoFromCache(uid);
                    if (null != cachedInfo) {
                        cachedInfo.setIsFriend(true);
                        if (null != contactInfo && null != contactInfo.memo_name) {
                            cachedInfo.setNotename(contactInfo.memo_name);
                        }
                        if (null != contactInfo && null != contactInfo.memo_others) {
                            cachedInfo.setNoteText(contactInfo.memo_others);
                        }
                    }
                    Logger.d(TAG, "update user friend related info, uid = " + uid + " values = " + values);
                }
                return null;
            }
        });
        return true;
    }

    public boolean removeAllUidsFriendRelatedInfo(Collection<Long> uids) {
        ContentValues values = new ContentValues();
        values.put(UserInfoStorage.KEY_ISFRIEND, 0);
        values.put(UserInfoStorage.KEY_NOTENAME, "");
        values.put(UserInfoStorage.KEY_NOTETEXT, "");
        UserInfoStorage.updateAllUidsWithValue(uids, values);
        for (long uid : uids) {
            InternalUserInfo cachedInfo = getInfoFromCache(uid);
            if (null != cachedInfo) {
                cachedInfo.setIsFriend(false);
                cachedInfo.setNotename("");
                cachedInfo.setNoteText("");
            }
        }
        return true;
    }

    public boolean updateMTime(long uid, int mTime) {
        UserInfoStorage.updateMTimeInBackground(uid, mTime);
        InternalUserInfo cachedInfo = getInfoFromCache(uid);
        if (null != cachedInfo) {
            cachedInfo.setmTime(mTime);
        }
        return true;
    }

    public int isUserInBlackList(long userID) {
        return UserInfoStorage.isUserInBlacklistSync(userID);
    }

    public int isUserInNoDisturb(long userID) {
        return UserInfoStorage.isUserInNoDisturbSync(userID);
    }

    public int isUserYourFriend(long userID) {
        return UserInfoStorage.isUserYourFriend(userID);
    }

    public String queryUserNotename(long userID) {
        return UserInfoStorage.queryUserNotename(userID);
    }

    public String queryUserNoteText(long userID) {
        return UserInfoStorage.queryUserNoteText(userID);
    }

    public long queryUserID(String username, String appkey) {
        return UserInfoStorage.queryUserIDSync(username, appkey);
    }

    public long queryUserID(SQLiteDatabase db, String username) {
        return UserInfoStorage.queryUserIDInBackground(db, username);
    }

    /**
     * 根据uid 查询username 和 appkey.
     *
     * @param uid
     * @return string[0] -- username,string[1] -- appkey
     */
    public String[] queryUsernameAndAppkey(long uid) {
        return UserInfoStorage.queryUsernameAndAppkeySync(uid);
    }

    public void clearCache() {
        userInfoCache.clear();
    }

    private InternalUserInfo getInfoFromCache(long uid) {
        return userInfoCache.get(uid);
    }

    private boolean putInfoToCache(InternalUserInfo info) {
        if (null != info) {
            userInfoCache.put(info.getUserID(), info);
            return true;
        }
        return false;
    }

    private boolean putInfoListToCache(Map<Long, InternalUserInfo> infos) {
        if (null != infos) {
            Set<Map.Entry<Long, InternalUserInfo>> entrySet = infos.entrySet();
            for (Map.Entry<Long, InternalUserInfo> entry : entrySet) {
                if (null != entry.getKey() && null != entry.getValue()) {//加入cache之前先做判断，因为ConcurrentHashMap中key和value不允许为null
                    userInfoCache.put(entry.getKey(), entry.getValue());
                }
            }
            return true;
        }
        return false;
    }

    private boolean removeInfoFromCache(long uid) {
        return userInfoCache.remove(uid) != null;
    }

}
