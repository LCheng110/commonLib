package cn.jpush.im.android.storage;

import android.content.ContentValues;
import android.text.TextUtils;

import com.google.gson.jpush.reflect.TypeToken;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

/**
 * 会话管理类，
 * 会话对象在内存中会有一份缓存，上层获取会话对象时，首先会从缓存中找。写入时也会先写缓存，再写入数据库
 * <p>
 * 因为有缓存机制，所以需要注意当会话的信息更新时，数据库和缓存这两个地方都需要同步更新
 */
public class ConversationManager {
    private static final String TAG = "ConversationManager";

    private static final String CONVERSATION_KEY_SEPERATOR = File.separator;
    /**
     * 缓存conversation的map，如果是单聊会话，key为"targetId_appkey",例如"userabc_appkey11111"
     * 如果是群聊会话，则直接使用targetId(gid)作为key,例如“10002938”
     */
    private Map<String, InternalConversation> conversationCache = new ConcurrentHashMap<String, InternalConversation>();
    private static ConversationManager sInstance;


    private ConversationManager() {
    }

    public synchronized static ConversationManager getInstance() {
        if (null == sInstance) {
            sInstance = new ConversationManager();
        }
        return sInstance;
    }

    public InternalConversation createSingleConversation(String targetID, String appkey) {
        return createConversation(ConversationType.single, targetID, appkey);
    }

    public InternalConversation createGroupConversation(long groupId) {
        return createConversation(ConversationType.group, String.valueOf(groupId), "");
    }

    public InternalConversation createConversation(ConversationType type, String targetID, String appkey) {
        InternalConversation conversation = ConversationStorage.createConversation(type, targetID, appkey);
        putConversationToCache(conversation);
        return conversation;
    }

    public InternalConversation createConversation(ConversationType type, String targetID, String appkey, String expectedTitle, long expectedLatestMsgDate, boolean saveInBackground) {
        InternalConversation conversation = ConversationStorage.createConversation(type, targetID, appkey, expectedTitle, expectedLatestMsgDate, saveInBackground);
        putConversationToCache(conversation);
        return conversation;
    }

    public InternalConversation getSingleConversation(String targetID, String appkey) {
        return getConversation(ConversationType.single, targetID, appkey);
    }

    public InternalConversation getGroupConversation(long groupId) {
        //
        return getConversation(ConversationType.group, String.valueOf(groupId), "");
    }


    public InternalConversation getConversation(ConversationType type, String targetID, String appkey) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getConversation", null)) {
            return null;
        }
        if (ConversationType.single == type && TextUtils.isEmpty(appkey)) {
            appkey = JCoreInterface.getAppKey();
        }
        InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
        if (null == conversation) {
            //缓存中没有，则从数据库拿并放入缓存。
            conversation = ConversationStorage.getConversationSync(type, targetID, appkey);
            if (null != conversation) {
                putConversationToCache(conversation);
            }
        }
        return conversation;
    }

    public List<InternalConversation> getAllConversation(boolean needSort) {
        List<InternalConversation> allConversationList = new ArrayList<InternalConversation>();
        Collection<InternalConversation> cachedConversationList = conversationCache.values();

        int countInDB = ConversationStorage.queryCountSync(null, null);
        Logger.d(TAG, "conv in db = " + countInDB + " conv in cache = " + cachedConversationList.size());
        if (countInDB != cachedConversationList.size()) {
            ConversationStorage.getConversationListSync(allConversationList);
            for (InternalConversation conversation : allConversationList) {
                putConversationToCache(conversation);
            }
        } else {
            allConversationList.addAll(cachedConversationList);
        }
        Logger.d(TAG, "after get all. conv list size = " + allConversationList.size());
        if (needSort) {
            Collections.sort(allConversationList, new ConvComparator());
        }
        return allConversationList;
    }

    /**
     * 按照会话的latest msg date 降序排列。
     */
    private class ConvComparator implements Comparator<InternalConversation> {
        @Override
        public int compare(InternalConversation lhs, InternalConversation rhs) {
            if (lhs.getLastMsgDate() > rhs.getLastMsgDate()) {
                return -1;
            } else if (lhs.getLastMsgDate() < rhs.getLastMsgDate()) {
                return 1;
            }
            return 0;
        }
    }

    public int queryUnreadCnt(ConversationType type, String targetID, String appkey) {
        return ConversationStorage.queryUnreadCntSync(type, targetID, appkey);
    }

    public boolean deleteSingleConversation(final String targetID, final String appkey) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("deleteSingleConversation", null)) {
            return false;
        }
        return deleteConversation(ConversationType.single, targetID, appkey);
    }

    public boolean deleteGroupConversation(long groupId) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("deleteGroupConversation", null)) {
            return false;
        }
        String targetId = String.valueOf(groupId);
        String appkey = "";
        return deleteConversation(ConversationType.group, targetId, appkey);
    }

    private synchronized boolean deleteConversation(ConversationType type, final String targetId, final String appkey) {
        InternalConversation conv = getConversation(type, targetId, appkey);
        if (null != conv) {
            ConversationStorage.deleteConversationInBackground(type, targetId, appkey, conv.getMsgTableName());
            //会话删除后，还要更新内存中记录的消息未读总数。
            if (!conv.isTargetInNoDisturb()) {//改变全局未读数之前，判断target是否是免打扰状态
                JMessage.addAllUnreadMsgCntBy(-(conv.getUnReadMsgCnt()));
            }
            //清除通知栏的通知
            ChatMsgManager.getInstance().cancelNotification(targetId, appkey);
            //将会话从缓存中删除。
            deleteConversationFromCache(type, targetId, appkey);
        }
        return true;
    }

    public boolean updateConversationUnreadCnt(ConversationType type, String targetID, String appkey, int unreadCnt, boolean updateInBackground) {
        boolean result;
        ContentValues values = new ContentValues();
        values.put(ConversationStorage.UNREAD_CNT, unreadCnt);
        if (updateInBackground) {
            ConversationStorage.updateInBackground(type, targetID, appkey, values);//数据库更新放到后台，不再阻塞主线程
            result = true;
        } else {
            result = ConversationStorage.updateSync(type, targetID, appkey, values);
        }

        if (result) {
            InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
            if (null != conversation) {
                conversation.setUnReadMsgCnt(unreadCnt);
            } else {
                Logger.d(TAG, "conversation not in cache . do not need to update");
            }
        }
        return result;
    }

    public boolean updateConversationUnreadCntMtime(ConversationType type, String targetID, String appkey, long unreadCntMtime) {
        ContentValues values = new ContentValues();
        values.put(ConversationStorage.UNREAD_CNT_MTIME, unreadCntMtime);
        ConversationStorage.updateInBackground(type, targetID, appkey, values);//数据库更新放到后台，不再阻塞主线程

        InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
        if (null != conversation) {
            conversation.setUnreadCntMtime(unreadCntMtime);
            conversation.recalculateUnreadCnt();//会话未读数最后一次清空的mtime更新，需要通知会话重新计算未读数。
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
        }
        return true;
    }

    public boolean updateConversationExtra(ConversationType type, String targetID, String appkey, String extra) {
        boolean result = ConversationStorage.updateConversationExtraSync(type, targetID, appkey, extra);
        if (result) {
            InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
            if (null != conversation) {
                conversation.setExtra(extra);
            } else {
                Logger.d(TAG, "conversation not in cache . do not need to update");
            }
        }
        return result;
    }

//    public boolean updateConversationUnreadCntMtime(ConversationType type, String targetID, String appkey, long unreadCntMtime) {
//        ContentValues values = new ContentValues();
//        values.put(ConversationStorage.UNREAD_CNT_MTIME, unreadCntMtime);
//        ConversationStorage.updateInBackground(type, targetID, appkey, values);//数据库更新放到后台，不再阻塞主线程
//
//        InternalConversation conversation = getConversationFromCache(targetID, appkey);
//        if (null != conversation) {
//            conversation.setUnreadCntMtime(unreadCntMtime);
//            conversation.recalculateUnreadCnt();//会话未读数最后一次清空的mtime更新，需要通知会话重新计算未读数。
//        } else {
//            Logger.d(TAG, "conversation not in cache . do not need to update");
//        }
//        return true;
//    }

    public boolean updateConvsersationTitle(ConversationType type, String targetID, String appkey, String title) {
        ConversationStorage.updateTitleInBackground(type, targetID, appkey, title);//数据库更新放到后台，不再阻塞主线程

        InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
        if (null != conversation) {
            conversation.setTitle(title);
            //更新title时，需要把targetName也更新下,因为targetName本质上等于title
            conversation.setTargetName(title);
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
        }
        return true;
    }

    public boolean updateConvsersationAvatar(ConversationType type, String targetID, String appkey, String avatarPath) {
        ConversationStorage.updateAvatarInBackground(type, targetID, appkey, avatarPath);//数据库更新放到后台，不再阻塞主线程
        InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
        if (null != conversation) {
            conversation.setAvatarPath(avatarPath);
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
        }
        return true;
    }

    //当消息内任何字段发生更新时，都需要检查消息是否是会话中最后一条消息，进而通过此接口更新会话中的latestMsg相关信息，因为考虑到上层对于会话的latestMsg访问的频繁性，sdk将latestMsg这个字段也缓存了。
    public boolean updateLatestMsg(ConversationType type, String targetID, String appkey, InternalMessage msg) {
        ConversationStorage.updateLatestMsgInBackground(type, targetID, appkey, msg);//数据库更新放到后台，不再阻塞主线程
        InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
        if (null != conversation) {
            //更新conv缓存
            if (null != msg) {
                conversation.setLatestMsg(msg);
                conversation.setLatestType(msg.getContentType());
                conversation.setLastMsgDate(msg.getCreateTime());
                if (ContentType.text == msg.getContentType()) {
                    TextContent content = (TextContent) msg.getContent();
                    conversation.setLatestText(content.getText());
                } else if (ContentType.prompt == msg.getContentType()) {
                    PromptContent content = (PromptContent) msg.getContent();
                    conversation.setLatestText(content.getPromptText());
                }
            } else {
                conversation.setLatestMsg(null);
                conversation.setLatestType(ContentType.text);
                conversation.setLatestText("");
            }
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
        }
        return true;
    }

    public boolean resetUnreadCount(ConversationType type, String targetID, String appkey) {
        ConversationStorage.resetUnreadCountInBackground(type, targetID, appkey);//数据库更新放到后台，不再阻塞主线程
        InternalConversation conversation = getConversationFromCache(type, targetID, appkey);
        if (null != conversation) {
            conversation.setUnReadMsgCnt(0);
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update unread count");
        }
        return true;
    }

    //更新缓存会话对象中包含的userinfo的信息。当userinfo有更新时需要注意同时也要调用此方法更新userinfo
    public boolean updateTargetInfoInCache(String username, String appkey, ContentValues values) {
        if (null == values) {
            Logger.ww(TAG, "values is null! updateTargetInfoInCache failed !");
            return false;
        }
        InternalConversation conversation = getConversationFromCache(ConversationType.single, username, appkey);

        if (null != conversation) {
            InternalUserInfo userInfo = (InternalUserInfo) conversation.getTargetInfo();
            if (null != userInfo) {
                Logger.d(TAG, "update target info in conv . values = " + values.toString());
                if (values.containsKey(UserInfoStorage.KEY_EXTRAS)) {
                    userInfo.setExtrasFromJson(values.getAsString(UserInfoStorage.KEY_EXTRAS));
                }
                if (values.containsKey(UserInfoStorage.KEY_AVATAR)) {
                    userInfo.setAvatarMediaID(values.getAsString(UserInfoStorage.KEY_AVATAR));
                }
                if (values.containsKey(UserInfoStorage.KEY_BIRTHDAY)) {
                    userInfo.setBirthdayString(values.getAsString(UserInfoStorage.KEY_BIRTHDAY));
                }
                if (values.containsKey(UserInfoStorage.KEY_BLACKLIST)) {
                    userInfo.setBlacklist(values.getAsInteger(UserInfoStorage.KEY_BLACKLIST));
                }
                if (values.containsKey(UserInfoStorage.KEY_GENDER)) {
                    userInfo.setGenderString(values.getAsString(UserInfoStorage.KEY_GENDER));
                }
                if (values.containsKey(UserInfoStorage.KEY_NICKNAME)) {
                    userInfo.setNickname(values.getAsString(UserInfoStorage.KEY_NICKNAME));
                }
                if (values.containsKey(UserInfoStorage.KEY_SIGNATURE)) {
                    userInfo.setSignature(values.getAsString(UserInfoStorage.KEY_SIGNATURE));
                }
                if (values.containsKey(UserInfoStorage.KEY_REGION)) {
                    userInfo.setRegion(values.getAsString(UserInfoStorage.KEY_REGION));
                }
                if (values.containsKey(UserInfoStorage.KEY_STAR)) {
                    userInfo.setStar(values.getAsInteger(UserInfoStorage.KEY_STAR));
                }
                if (values.containsKey(UserInfoStorage.KEY_NOTENAME)) {
                    userInfo.setNotename(values.getAsString(UserInfoStorage.KEY_NOTENAME));
                }
                if (values.containsKey(UserInfoStorage.KEY_NOTETEXT)) {
                    userInfo.setNoteText(values.getAsString(UserInfoStorage.KEY_NOTETEXT));
                }
                if (values.containsKey(UserInfoStorage.KEY_UID)) {
                    userInfo.setUserID(values.getAsLong(UserInfoStorage.KEY_UID));
                }
                if (values.containsKey(UserInfoStorage.KEY_NODISTURB)) {
                    Logger.d(TAG, "update no disturb = " + values.getAsInteger(UserInfoStorage.KEY_NODISTURB));
                    userInfo.setNoDisturbInLocal(values.getAsInteger(UserInfoStorage.KEY_NODISTURB));
                }
                if (values.containsKey(UserInfoStorage.KEY_ISFRIEND)) {
                    userInfo.setIsFriend(values.getAsInteger(UserInfoStorage.KEY_ISFRIEND) == 1);
                }
                if (values.containsKey(UserInfoStorage.KEY_ADDRESS)) {
                    userInfo.setAddress(values.getAsString(UserInfoStorage.KEY_ADDRESS));
                }
            } else {
                Logger.d(TAG, "conversation not in cache . do not need to update");
                return false;
            }
        }
        return true;
    }

    //更新缓存会话对象中包含的groupinfo的信息。当groupinfo有更新时需要注意同时也要调用此方法更新groupinfo
    public boolean updateGroupInfoInCache(long groupID, ContentValues values) {
        if (null == values) {
            Logger.ww(TAG, "values is null! updateGroupInfoInCache failed !");
            return false;
        }
        String targetID = String.valueOf(groupID);
        InternalConversation conversation = getConversationFromCache(ConversationType.group, targetID, null);
        if (null != conversation) {
            InternalGroupInfo groupInfo = (InternalGroupInfo) conversation.getTargetInfo();
            if (values.containsKey(GroupStorage.GROUP_NAME)) {
                groupInfo.setGroupName(values.getAsString(GroupStorage.GROUP_NAME));
            }
            if (values.containsKey(GroupStorage.GROUP_DESC)) {
                groupInfo.setGroupDescription(values.getAsString(GroupStorage.GROUP_DESC));
            }
            if (values.containsKey(GroupStorage.GROUP_OWNER_ID)) {
                groupInfo.setOwnerId(values.getAsLong(GroupStorage.GROUP_OWNER_ID));
            }
            if (values.containsKey(GroupStorage.GROUP_MEMBERS)) {
                String memberString = values.getAsString(GroupStorage.GROUP_MEMBERS);
                List<Long> memberUserIds = JsonUtil.formatToGivenType(memberString, new TypeToken<List<Long>>() {
                });
                groupInfo.setGroupMemberUserIds(memberUserIds);
            }
            if (values.containsKey(GroupStorage.GROUP_NODISTURB)) {
                int noDisturb = values.getAsInteger(GroupStorage.GROUP_NODISTURB);
                groupInfo.setNoDisturbInLocal(noDisturb);
            }
            if (values.containsKey(GroupStorage.GROUP_BLOCKED)) {
                int shielding = values.getAsInteger(GroupStorage.GROUP_BLOCKED);
                groupInfo.setBlockGroupInLocal(shielding);
            }

            if (values.containsKey(GroupStorage.GROUP_AVATAR)) {
                String avatar = values.getAsString(GroupStorage.GROUP_AVATAR);
                groupInfo.setAvatarMediaID(avatar);
            }
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
            return false;
        }
        return true;
    }

    public boolean addGroupMemberInCacheWithIds(long groupID, List<Long> memberUserIds) {
        if (null == memberUserIds) {
            Logger.ww(TAG, "memberInfo is null! addGroupMemberInCache failed !");
            return false;
        }
        List<InternalUserInfo> userInfoList = UserInfoManager.getInstance().getUserInfoList(memberUserIds);
        return addGroupMemberInCache(groupID, memberUserIds, userInfoList);
    }

    public boolean removeGroupMemberFromCache(long groupID, List<Long> memberUserIds) {
        if (null == memberUserIds) {
            Logger.ww(TAG, "memberInfo is null! removeGroupMemberFromCache failed !");
            return false;
        }

        String targetID = String.valueOf(groupID);
        InternalConversation conversation = getConversationFromCache(ConversationType.group, targetID, "");
        if (null != conversation) {
            InternalGroupInfo groupInfo = (InternalGroupInfo) conversation.getTargetInfo();

            groupInfo.removeMemberIdFromIdList(memberUserIds);
            for (long userId : memberUserIds) {
                groupInfo.removeMemberFromList(userId);
            }
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
            return false;
        }
        return true;
    }

    public boolean updateGroupMemberNamesAndReload(long groupID, List<Long> memberUserIds) {
        if (null == memberUserIds) {
            Logger.ww(TAG, "memberInfo is null! removeGroupMemberFromCache failed !");
            return false;
        }

        String targetID = String.valueOf(groupID);
        InternalConversation conversation = getConversationFromCache(ConversationType.group, targetID, "");
        if (null != conversation) {
            InternalGroupInfo groupInfo = (InternalGroupInfo) conversation.getTargetInfo();
            groupInfo.setGroupMemberUserIds(memberUserIds);
            groupInfo.loadMemberList();
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
            return false;
        }
        return true;
    }

    public void resetNodisturbFlagsInCache() {
        Collection<InternalConversation> conversations = conversationCache.values();
        for (InternalConversation conversation : conversations) {
            if (ConversationType.group == conversation.getType()) {
                ((InternalGroupInfo) conversation.getTargetInfo()).setNoDisturbInLocal(0);
            }
        }
    }

    public void resetShieldingFlagsInCache() {
        Collection<InternalConversation> conversations = conversationCache.values();
        for (InternalConversation conversation : conversations) {
            if (ConversationType.group == conversation.getType()) {
                ((InternalGroupInfo) conversation.getTargetInfo()).setBlockGroupInLocal(0);
            }
        }
    }

    public void clearCache() {
        conversationCache.clear();
    }

    private boolean addGroupMemberInCache(long groupID, List<Long> memberIds, List<InternalUserInfo> memberInfos) {
        if (null == memberInfos) {
            Logger.ww(TAG, "memberInfo is null! addGroupMemberInCache failed !");
            return false;
        }
        String targetID = String.valueOf(groupID);
        InternalConversation conversation = getConversationFromCache(ConversationType.group, targetID, "");
        if (null != conversation) {
            InternalGroupInfo groupInfo = (InternalGroupInfo) conversation.getTargetInfo();

            groupInfo.addMemberIdToNameList(memberIds);
            for (UserInfo userInfo : memberInfos) {
                groupInfo.addMemberToList(userInfo);
            }
        } else {
            Logger.d(TAG, "conversation not in cache . do not need to update");
            return false;
        }
        return true;
    }

    private boolean putConversationToCache(InternalConversation conversation) {
        if (null == conversation) {
            Logger.ww(TAG, "parameter invalid! put to map failed!");
            return false;
        }
        String conversationKey = createConversationKey(conversation.getType(), conversation.getTargetId(), conversation.getTargetAppKey());
        conversationCache.put(conversationKey, conversation);
        return true;
    }

    private InternalConversation getConversationFromCache(ConversationType type, String targetID, String appkey) {
        if (TextUtils.isEmpty(targetID)) {
            Logger.ww(TAG, "parameter invalid! get conversation from cache failed!");
            return null;
        }
        String conversationKey = createConversationKey(type, targetID, appkey);
        return getConversationFromCache(conversationKey);
    }

    private InternalConversation getConversationFromCache(String conversationKey) {
        if (TextUtils.isEmpty(conversationKey)) {
            Logger.ww(TAG, "parameter invalid! get conversation from cache failed!");
            return null;
        }
        return conversationCache.get(conversationKey);
    }

    private boolean deleteConversationFromCache(ConversationType type, String targetID, String appkey) {
        if (TextUtils.isEmpty(targetID)) {
            Logger.ww(TAG, "parameter invalid! delete conversation from cache failed!");
            return false;
        }
        String conversationKey = createConversationKey(type, targetID, appkey);
        conversationCache.remove(conversationKey);
        return true;
    }

    private String createConversationKey(ConversationType type, String targetId, String appkey) {
        if (type == ConversationType.single) {
            //单聊需要用targetId和appkey联合做key
            return targetId + CONVERSATION_KEY_SEPERATOR + appkey;
        } else {
            //群聊直接使用groupId做key
            return targetId;
        }

    }

    public void initAllUnReadCount(AtomicInteger allUnreadMsgCnt) {
        int unReadMsgCnt = 0;
        List<InternalConversation> allConversation = ConversationManager.getInstance().getAllConversation(false);
        for (InternalConversation conversation : allConversation) {
            if (!conversation.isTargetInNoDisturb()) {//全局未读数初始化时，只计算不在免打扰名单中的会话的未读数。
                unReadMsgCnt += conversation.getUnReadMsgCnt();
            }
        }
        allUnreadMsgCnt.set(unReadMsgCnt);
    }

}
