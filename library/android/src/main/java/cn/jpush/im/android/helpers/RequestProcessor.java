package cn.jpush.im.android.helpers;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;

import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.event.CommandNotificationEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.helpers.sync.SyncConvRespHandler;
import cn.jpush.im.android.helpers.sync.SyncEventRespHandler;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.pushcommon.helper.IMServiceHelper;
import cn.jpush.im.android.pushcommon.helper.PluginJCoreHelper;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.AddBlackListRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.AddFriendRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.AddGroupMemberRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.AddGroupToBlockRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.AddMsgnoDisturbGlobalRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.AddMsgnoDisturbGroupRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.AddMsgnoDisturbSingleRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.CreateGroupRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.DelBlackListRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.DelFriendRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.DelGroupFromBlockRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.DelGroupMemberRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.DeleteMsgnoDisturbGlobalRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.DeleteMsgnoDisturbGroupRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.DeleteMsgnoDisturbSingleRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ExitGroupRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.GroupMsgRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ImBaseRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ImLoginRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ImLogoutRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.MsgReceiptReportRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.MsgRetractRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ReportInfoRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.ResetUnreadCntRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.SingleMsgRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.SyncCheckRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.SyncConvACKRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.SyncEventACKRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.SyncReceiptACKRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.TransCommandRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.UpdateGroupInfoRequest;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.UpdateMemoRequest;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.EventIdListManager;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.storage.database.JMSQLiteDatabase;
import cn.jpush.im.android.tasks.AbstractTask;
import cn.jpush.im.android.tasks.GetGroupMembersTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.MessageProtocolParser;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.android.utils.filemng.FileDownloader;
import cn.jpush.im.api.BasicCallback;

/**
 *
 */
public class RequestProcessor {

    private static final String TAG = "RequestProcessor";

    public static Map<Long, ImBaseRequest> requestsCache = new HashMap<Long, ImBaseRequest>();

    private static final long REGISTERCODE_READ_INTERVAL = 2 * 1000;//2 seconds

    private static final long REGISTERCODE_READ_TIMEOUT = 30 * 1000;//30 seconds

    private static final int PLATFORM_ANDROID = 1;

    public static void imLogin(final Context context, final String username, final String password, final long rid,
                               final BasicCallback callback) {
        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                if (!needSendRequest(context, callback)) {
                    return null;
                }
                //如果当前登陆的账号和之前已登录的账号不同，则登录前清空之前已在队列中等待执行的任务。
                if (!username.equals(IMConfigs.getUserName())) {
                    clearQueueInThreadPool();
                }

                //清空缓存信息。
                clearCachedInfos();

                String pwd = StringUtils.toMD5(password);
                ImLoginRequest loginRequest = new ImLoginRequest(username, password, PLATFORM_ANDROID, JMessageClient.getSdkVersionString(), rid);
                loginRequest.setCallback(callback);
                requestsCache.put(rid, loginRequest);
                imRequest(context, loginRequest);
                return null;
            }
        });
    }

    public static boolean needSendRequest(Context context, BasicCallback callback) {
        int registerCode = PluginJCoreHelper.getRegisterCode(context);
        long waitTime = 0;
        while (-1 == registerCode && waitTime < REGISTERCODE_READ_TIMEOUT) {
            try {
                Thread.sleep(REGISTERCODE_READ_INTERVAL);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            registerCode = PluginJCoreHelper.getRegisterCode(context);
            waitTime += REGISTERCODE_READ_INTERVAL;
        }
        return !CommonUtils.handleRegCode(registerCode, callback);
    }

    public static void imLogout(Context context, String username, long rid) {
        ImLogoutRequest imLogoutRequest = new ImLogoutRequest(username, rid, IMConfigs.getUserID());
//        requestsCache.put(rid, imLogoutRequest);//对于logout请求，不需要处理对应的响应，所以这里不需要加入到requestsCache中去
        imRequest(context, imLogoutRequest);

        //将所有正在发送的消息状态置为send_fail.
        MessageSendingMaintainer.resetAllSendingMessageStatus();
        //登出时清空线程池中的等待队列。
        clearQueueInThreadPool();
        //清除本地缓存
        clearCachedInfos();
        //登出时候将总未读数置为 -1 登陆之后从数据库拿所有未读消息数
        JMessage.resetAllUnreadMsgCnt();
    }

    //用户 logout后的清理动作
    public static void clearCachedInfos() {

        //清掉通知栏上通知。
        ChatMsgManager.getInstance().reset();
        //清掉msgID缓存
        ResponseProcessor.msgIDCacheList.clear();
        //清掉Conversation缓存
        ConversationManager.getInstance().clearCache();
        UserInfoManager.getInstance().clearCache();
        UserIDHelper.clearCachedMaps();
        //停止SyncCheck
        RequestProcessor.stopSyncCheck();
        //将任务中flag状态重置。
        AbstractTask.resetFlag();
        //清掉缓存的general eventId list
        EventIdListManager.getInstance().clearCache();
        //清掉eventProcessor中还在队列中缓存的待处理的事件以及event id list.
        EventProcessor.getInstance().clearCache();
        //清掉缓存的正在获取群成员的gid列表
        GetGroupMembersTask.clearGidCache();

        //清理用户信息
        IMConfigs.setUserName("");
        IMConfigs.setUserPassword("");
        IMConfigs.setToken("");
        IMConfigs.setUserID(0);
    }

    public static MsgSendResponse imSingleMsgSend(Context context, long targetID, Message message, MessageSendingOptions options,
                                                  long rid) {
        MsgSendResponse msgSendResponse = new MsgSendResponse();
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imSingleMsgSend] send msg failed, uid = " + uid);
            return msgSendResponse.setResponseCode(ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN)
                    .setDesc(ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
        }
        InternalMessage internalMessage = (InternalMessage) message;
        //此处需要将message中的local content部分去掉。而且不能直接去掉用户传进来的message content，
        //所以需要使用clone对象的方式，去掉clone对象的local content并发送clone的对象。
        InternalMessage clonedMessage = (InternalMessage) internalMessage.clone();
        removeLocalContentInMessage(clonedMessage);

        String jsonString = MessageProtocolParser.messageToProtocol(clonedMessage);
        Logger.d(TAG, "json string after parse . length = " + jsonString.length());
        if (!jsonString.contains("msg_body")) {
            //如果解析出的json string中不包含msg_body，说明解析失败了，一般是由于用户混淆配置错误，
            //导致Message中的@Expose注解被去掉导致的。
            return msgSendResponse.setResponseCode(ErrorCode.LOCAL_ERROR.LOCAL_ILLEGAL_MSG_JSON)
                    .setDesc(ErrorCode.LOCAL_ERROR.LOCAL_ILLEGAL_MSG_JSON_DESC);
        }
        SingleMsgRequest singleMsgRequest = new SingleMsgRequest(targetID, jsonString
                , rid, uid, options, message);
        requestsCache.put(rid, singleMsgRequest);
        imRequest(context, singleMsgRequest);
        return msgSendResponse.setResponseCode(ErrorCode.NO_ERROR)
                .setDesc(ErrorCode.NO_ERROR_DESC);
    }

    public static MsgSendResponse imGroupMsgSend(Context context, long targetGID, MessageSendingOptions options, Message message, long rid) {
        MsgSendResponse msgSendResponse = new MsgSendResponse();
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imGroupMsgSend] send msg failed , uid = " + uid);
            return msgSendResponse.setResponseCode(ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN)
                    .setDesc(ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
        }
        InternalMessage internalMessage = (InternalMessage) message;
        InternalMessage clonedMessage = (InternalMessage) internalMessage.clone();
        removeLocalContentInMessage(clonedMessage);

        String jsonString = MessageProtocolParser.messageToProtocol(clonedMessage);
        Logger.d(TAG, "json string after parse . length = " + jsonString.length());
        if (!jsonString.contains("msg_body")) {
            //如果解析出的json string中不包含msg_body，说明解析失败了，一般是由于用户混淆配置错误，
            //导致Message中的@Expose注解被去掉导致的。
            return msgSendResponse.setResponseCode(ErrorCode.LOCAL_ERROR.LOCAL_ILLEGAL_MSG_JSON)
                    .setDesc(ErrorCode.LOCAL_ERROR.LOCAL_ILLEGAL_MSG_JSON_DESC);
        }

        GroupMsgRequest groupMsgRequest = new GroupMsgRequest(targetGID, jsonString,
                rid, uid, options, message);
        requestsCache.put(rid, groupMsgRequest);
        imRequest(context, groupMsgRequest);
        return msgSendResponse.setResponseCode(ErrorCode.NO_ERROR)
                .setDesc(ErrorCode.NO_ERROR_DESC);
    }

    public static class MsgSendResponse {
        private int responseCode;
        private String desc;

        public MsgSendResponse setResponseCode(int responseCode) {
            this.responseCode = responseCode;
            return this;
        }

        public MsgSendResponse setDesc(String desc) {
            this.desc = desc;
            return this;
        }

        public String getDesc() {
            return desc;
        }

        public int getResponseCode() {
            return responseCode;
        }
    }

    public static void imCreateGroup(Context context, String groupName, String groupDesc,
                                     int groupLevel, int flag, String avatarMediaID, long rid, CreateGroupCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imCreateGroup] create group failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        CreateGroupRequest createGroupRequest = new CreateGroupRequest(groupName, groupDesc, flag,
                groupLevel, avatarMediaID, rid, uid);
        createGroupRequest.setCallback(callback);
        requestsCache.put(rid, createGroupRequest);
        imRequest(context, createGroupRequest);
    }

    public static void imExitGroup(Context context, long groupID, long rid,
                                   BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imExitGroup] exit group failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        ExitGroupRequest exitGroupRequest = new ExitGroupRequest(groupID, rid, uid);
        exitGroupRequest.setCallback(callback);
        requestsCache.put(rid, exitGroupRequest);
        imRequest(context, exitGroupRequest);
    }

    public static void imAddGroupMember(Context context, long groupID,
                                        List<Long> uidList, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imAddGroupMember] add group member failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        AddGroupMemberRequest addGroupMemberRequest = new AddGroupMemberRequest(groupID, uidList, rid, uid);
        addGroupMemberRequest.setCallback(callback);
        requestsCache.put(rid, addGroupMemberRequest);
        imRequest(context, addGroupMemberRequest);
    }

    public static void imDelGroupMember(Context context, long groupID,
                                        List<Long> uidList, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imDelGroupMember] del group member failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        DelGroupMemberRequest delGroupMemberRequest = new DelGroupMemberRequest(groupID,
                uidList, rid, uid);
        delGroupMemberRequest.setCallback(callback);
        requestsCache.put(rid, delGroupMemberRequest);
        imRequest(context, delGroupMemberRequest);
    }

    public static void imUpdateGroupInfo(Context context, long groupID, String groupName,
                                         String groupDesc, String avatarMediaID, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imUpdateGroupInfo] update group info failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        UpdateGroupInfoRequest updateGroupInfoRequest = new UpdateGroupInfoRequest(groupID,
                groupName, groupDesc, avatarMediaID, rid, uid);
        updateGroupInfoRequest.setCallback(callback);
        requestsCache.put(rid, updateGroupInfoRequest);
        imRequest(context, updateGroupInfoRequest);
    }

    public static void imAddToBlackList(Context context, List<Long> userids, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imAddToBlackList] add users to blacklist failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        if (userids.contains(uid)) {
            Logger.d(TAG, "[imAddToBlackList] add users to blacklist failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_ERROR, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_DESC);
            return;
        }

        AddBlackListRequest addBlackListRequest = new AddBlackListRequest(userids, rid, uid);
        addBlackListRequest.setCallback(callback);
        requestsCache.put(rid, addBlackListRequest);
        imRequest(context, addBlackListRequest);
    }

    public static void imDelFromBlackList(Context context, List<Long> userids, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imDelFromBlackList] del users from blacklist failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        if (userids.contains(uid)) {
            Logger.d(TAG, "[imDelFromBlackList] del users from blacklist failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_ERROR, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_DESC);
            return;
        }

        DelBlackListRequest delBlackListRequest = new DelBlackListRequest(userids, rid, uid);
        delBlackListRequest.setCallback(callback);
        requestsCache.put(rid, delBlackListRequest);
        imRequest(context, delBlackListRequest);
    }

    public static void imAddGroupToNoDisturb(Context context, long targetGid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imAddGroupToNoDisturb] add group to nodisturb failed , targetGid = " + targetGid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        AddMsgnoDisturbGroupRequest addToNoDisturbGroupRequest = new AddMsgnoDisturbGroupRequest(targetGid, rid, uid);
        addToNoDisturbGroupRequest.setCallback(callback);
        requestsCache.put(rid, addToNoDisturbGroupRequest);
        imRequest(context, addToNoDisturbGroupRequest);
    }

    public static void imDelGroupFromNoDisturb(Context context, long targetGid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imDelGroupFromNoDisturb] del group to nodisturb failed , targetGid = " + targetGid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        DeleteMsgnoDisturbGroupRequest delFromNoDisturbGroupRequest = new DeleteMsgnoDisturbGroupRequest(targetGid, rid, uid);
        delFromNoDisturbGroupRequest.setCallback(callback);
        requestsCache.put(rid, delFromNoDisturbGroupRequest);
        imRequest(context, delFromNoDisturbGroupRequest);
    }

    public static void imAddGroupToBlock(Context context, long targetGid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imAddGroupToBlock] add group to block failed , targetGid = " + targetGid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        AddGroupToBlockRequest addGroupToBlockRequest = new AddGroupToBlockRequest(targetGid, rid, uid);
        addGroupToBlockRequest.setCallback(callback);
        requestsCache.put(rid, addGroupToBlockRequest);
        imRequest(context, addGroupToBlockRequest);
    }

    public static void imDelGroupFromBlock(Context context, long targetGid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imDelGroupFromBlock] delete group from block failed , targetGid = " + targetGid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        DelGroupFromBlockRequest delGroupFromBlockRequest = new DelGroupFromBlockRequest(targetGid, rid, uid);
        delGroupFromBlockRequest.setCallback(callback);
        requestsCache.put(rid, delGroupFromBlockRequest);
        imRequest(context, delGroupFromBlockRequest);
    }

    public static void msgRetract(Context context, long msgID, long rid, InternalConversation conversation, Message message, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        InternalMessage msg = (InternalMessage) message;
        MsgRetractRequest msgRetractRequest = new MsgRetractRequest(msgID, uid, rid, conversation, msg);
        msgRetractRequest.setCallback(callback);
        requestsCache.put(rid, msgRetractRequest);
        imRequest(context, msgRetractRequest);
    }

    public static void imAddUserToNoDisturb(Context context, long targetUid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imAddUserToNoDisturb] add user to nodisturb failed , targetUid = " + targetUid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        if (uid == targetUid) {
            Logger.d(TAG, "[imAddUserToNoDisturb] add user to nodisturb failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_ERROR, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_DESC);
            return;
        }

        AddMsgnoDisturbSingleRequest addToNoDisturbSingleRequest = new AddMsgnoDisturbSingleRequest(targetUid, rid, uid);
        addToNoDisturbSingleRequest.setCallback(callback);
        requestsCache.put(rid, addToNoDisturbSingleRequest);
        imRequest(context, addToNoDisturbSingleRequest);
    }

    public static void imDelUserFromNoDisturb(Context context, long targetUid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imDelUserFromNoDisturb] del users from nodisturb failed , targetUid = " + targetUid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        if (uid == targetUid) {
            Logger.d(TAG, "[imDelUserFromNoDisturb] del users from nodisturb failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_ERROR, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_DESC);
            return;
        }

        DeleteMsgnoDisturbSingleRequest delFromNoDisturbSingleRequest = new DeleteMsgnoDisturbSingleRequest(targetUid, rid, uid);
        delFromNoDisturbSingleRequest.setCallback(callback);
        requestsCache.put(rid, delFromNoDisturbSingleRequest);
        imRequest(context, delFromNoDisturbSingleRequest);
    }

    public static void imSetNoDisturbGlobal(Context context, int noDisturbGlobal, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imSetNoDisturbGlobal] set noDisturb global failed");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        ImBaseRequest request;
        if (noDisturbGlobal == 1) {
            request = new AddMsgnoDisturbGlobalRequest(rid, uid);
        } else {
            request = new DeleteMsgnoDisturbGlobalRequest(rid, uid);
        }
        request.setCallback(callback);
        requestsCache.put(rid, request);
        imRequest(context, request);
    }

    public static void imAddContact(Context context, long targetUid, int fromType, String why, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imAddContact] add user to contact failed");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        if (targetUid == uid) {
            Logger.d(TAG, "[imAddContact] add user to contact failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_ERROR, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_DESC);
            return;
        }

        AddFriendRequest addFriendRequest = new AddFriendRequest(targetUid, null, null, fromType, why, rid, uid);
        addFriendRequest.setCallback(callback);
        requestsCache.put(rid, addFriendRequest);
        imRequest(context, addFriendRequest);
    }

    public static void imRemoveContact(Context context, long targetUid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imRemoveContact] remove user from contact failed");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        if (targetUid == uid) {
            Logger.d(TAG, "[imRemoveContact] remove user from contact failed , uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_ERROR, ErrorCode.LOCAL_ERROR.TARGET_USER_CANNOT_BE_YOURSELF_DESC);
            return;
        }

        DelFriendRequest removeFriendRequest = new DelFriendRequest(targetUid, rid, uid);
        removeFriendRequest.setCallback(callback);
        requestsCache.put(rid, removeFriendRequest);
        imRequest(context, removeFriendRequest);
    }

    public static void imUpdateMemo(Context context, String memoName, String memoOthers, long targetUid, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[update memo]update memo failed");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        UpdateMemoRequest updateMemoRequest = new UpdateMemoRequest(targetUid, memoName, memoOthers, rid, uid);
        updateMemoRequest.setCallback(callback);
        requestsCache.put(rid, updateMemoRequest);
        imRequest(context, updateMemoRequest);
    }

    /**
     * 发送未读数重置请求，这个请求不需要后台响应。
     */
    public static void resetUnreadCnt(Conversation conversation, int readCnt, long rid) {
        long uid = IMConfigs.getUserID();
        if (uid == 0 || 0 == readCnt) {
            Logger.ww(TAG, "resetUnreadCnt failed, uid = " + uid + " readCnt = " + readCnt);
            return;
        }
        long targetUidOrGid = 0;
        int targetType = 0;
        Object targetInfo = conversation.getTargetInfo();
        switch (conversation.getType()) {
            case group:
                targetUidOrGid = ((GroupInfo) targetInfo).getGroupID();
                targetType = ResetUnreadCntRequest.TARGET_TYPE_GROUP;
                break;
            case single:
                targetUidOrGid = ((UserInfo) targetInfo).getUserID();
                targetType = ResetUnreadCntRequest.TARGET_TYPE_SINGLE;
                break;
        }
        ResetUnreadCntRequest updateUnreadCount = new ResetUnreadCntRequest(targetUidOrGid, targetType, readCnt, rid, uid);
        imRequest(JMessage.mContext, updateUnreadCount);
    }

    static boolean imReportInfo(Context context, String sdkVersion, long rid) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            //如果uid为0 说明im还未登陆，直接返回。
            return false;
        }

        //上报时的附加逻辑：如果im层是已登录状态，而push记录的im确实未登录，则将push的处的im登陆状态置为true
        boolean isImLoginInPush = PluginJCoreHelper.isImLoggedIn(context);
        if (CommonUtils.isLogin("ReportInfo") && !isImLoginInPush) {
            PluginJCoreHelper.setImLogStatus(context, true);
        }

        ReportInfoRequest reportInfoRequest = new ReportInfoRequest(sdkVersion, rid, uid);
        requestsCache.put(rid, reportInfoRequest);
        imRequest(context, reportInfoRequest);
        return true;
    }

    public static void imMsgReceiptReportRequest(MsgReceiptReportRequestPackager.RequestEntity requestEntity, BasicCallback callback, long rid) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            //如果uid为0 说明im还未登陆，直接返回。
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }

        if (null == requestEntity) {
            Logger.ww(TAG, "[imMsgReceiptReportRequest] request entity can not be null");
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_SET_HAVEREAD_ERROR, ErrorCode.LOCAL_ERROR.LOCAL_SET_HAVEREAD_ERROR_DESC);
            return;
        }
        MsgReceiptReportRequest request = new MsgReceiptReportRequest(uid, requestEntity, rid);
        request.setCallback(callback);
        requestsCache.put(rid, request);
        imRequest(JMessage.mContext, request);
    }

    public static void imSyncCheck(Context context, long syncKey, long syncEventKey, long syncReceiptKey, int syncType, long rid) {
        SyncCheckRequest syncCheckRequest = new SyncCheckRequest(syncKey, syncEventKey, syncReceiptKey, syncType, rid, IMConfigs.getUserID());
        imRequest(context, syncCheckRequest);
    }

    public static void imSyncConvACK(Context context, long syncKey, long rid) {
        SyncConvACKRequest syncConvACKRequest = new SyncConvACKRequest(syncKey, rid, IMConfigs.getUserID());
        imRequest(context, syncConvACKRequest);
    }

    public static void imSyncEventACK(Context context, long syncEventKey, long rid) {
        SyncEventACKRequest syncEventACKRequest = new SyncEventACKRequest(syncEventKey, rid, IMConfigs.getUserID());
        imRequest(context, syncEventACKRequest);
    }

    public static void imSyncReceiptACK(Context context, long syncReceiptKey, long rid) {
        SyncReceiptACKRequest syncEventACKRequest = new SyncReceiptACKRequest(syncReceiptKey, rid, IMConfigs.getUserID());
        imRequest(context, syncEventACKRequest);
    }

    public static void imTransCommandSend(Context context, long targetID, CommandNotificationEvent.Type type, String cmd, long rid, BasicCallback callback) {
        long uid = IMConfigs.getUserID();
        if (uid == 0) {
            Logger.d(TAG, "[imTransCommandSend] send transCommand failed, uid = " + uid);
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN,
                    ErrorCode.LOCAL_ERROR.LOCAL_NOT_LOGIN_DESC);
            return;
        }
        TransCommandRequest transCommandRequest = new TransCommandRequest(targetID, type.getValue(), cmd, rid, uid);
        transCommandRequest.setCallback(callback);
        requestsCache.put(rid, transCommandRequest);
        imRequest(context, transCommandRequest);
    }

    private static void imRequest(Context context, ImBaseRequest request) {
        IMServiceHelper.imRequest(context, request);
    }

    private static void removeLocalContentInMessage(Message message) {
        switch (message.getContentType()) {
            case image:
                // to clear local info from message content
                ImageContent imageContent = (ImageContent) message.getContent();
                imageContent.setLocalThumbnailPath(null);
                imageContent.setLocalPath(null);
                break;
            case voice:
                VoiceContent voiceContent = (VoiceContent) message.getContent();
                voiceContent.setLocalPath(null);
                break;
            case file:
                FileContent fileContent = (FileContent) message.getContent();
                fileContent.setLocalPath(null);
                break;
            default:
                break;
        }
    }

    //需要确保handler使用的是main looper,因为handler初始化有可能是从子进程进入的。
    private static Handler handler = new SyncCheckHandler(Looper.getMainLooper());
    private static final int COMMAND_SYNC_CHECK = 1;
    private static final int SYNC_CHECK_INTERVAL_IN_SEC = BuildConfig.DEFAULT_SYNC_INTERVAL_IN_SEC;

    /**
     * SyncCheck周期同步使用handler的原因：
     * <p>
     * SyncCheck不像heartbeat一样需要严格的频率保证每次请求都要到达后台，
     * 来维持tcp的长连接不被中断。
     * SyncCheck只是在应用的生命周期期间进行的定时任务。所有的SyncCheck应该只是在用户使用应用时才有意义。
     * 所以当应用处于未运行或者是设备休眠状态下，不需要保证SyncCheck的发送.
     * <p>
     * 基于以上这一点，SyncCheck使用handler来处理定时循环任务，而不使用AlarmManager,在执行任务
     * 过程中，也不会获取wakelock来保持设备唤醒，以节省电量。
     * <p>
     * NOTE :startSyncCheck这个操作应该只在上层应用进程调用，多进程环境下需要发送{@code IMResponseHelper.ACTION_IM_RESPONSE}
     * 广播来启动syncCheck。
     */
    public static void startSyncCheck() {
        if (!handler.hasMessages(COMMAND_SYNC_CHECK) && 0 != IMConfigs.getUserID()) {
            Logger.ii(TAG, "start SyncCheck");
            //首次sync check delay一段时间防止push login和im login同时到达时，可能会起两个sync check任务。
            handler.sendEmptyMessageDelayed(COMMAND_SYNC_CHECK, 2 * 1000);
        }
    }

    public static void resetAndStartSyncCheck(int delayMills) {
        if (0 != IMConfigs.getUserID()) {
            handler.removeMessages(COMMAND_SYNC_CHECK);
            Logger.ii(TAG, "reset and start SyncCheck");
            handler.sendEmptyMessageDelayed(COMMAND_SYNC_CHECK, delayMills);
        }
    }

    static void stopSyncCheck() {
        if (0 != IMConfigs.getUserID()) {
            Logger.ii(TAG, "SyncCheck stopped");
            handler.removeMessages(COMMAND_SYNC_CHECK);
            //停止SyncCheck时同时将缓存的数据清掉，防止下次SyncCheck数据出错
            SyncConvRespHandler.getInstance().clearCache();
            SyncEventRespHandler.getInstance().clearCache();
        }
    }

    private static class SyncCheckHandler extends Handler {
        SyncCheckHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case COMMAND_SYNC_CHECK:
                    Logger.d(TAG, "received a sync check broadcast");
                    Task.callInBackground(new Callable<Object>() {
                        @Override
                        public Object call() throws Exception {
                            long uid = IMConfigs.getUserID();
                            imSyncCheck(JMessage.mContext, IMConfigs.getConvSyncKey(uid), IMConfigs.getSyncEventKey(uid), IMConfigs.getSyncReceiptKey(uid),
                                    IMConfigs.getMsgRoaming(), CommonUtils.getSeqID());
                            sendEmptyMessageDelayed(COMMAND_SYNC_CHECK, SYNC_CHECK_INTERVAL_IN_SEC * 1000L);
                            return null;
                        }
                    });
                    break;
            }

            super.handleMessage(msg);
        }
    }

    //清空线程池中的等待队列。
    private static void clearQueueInThreadPool() {
        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Task.BACKGROUND_EXECUTOR;
        poolExecutor.getQueue().clear();
        JMSQLiteDatabase.clearQueue();
        ((ThreadPoolExecutor) FileDownloader.downloadExecutor).getQueue().clear();
        ((ThreadPoolExecutor) AbstractTask.httpTaskExecutor).getQueue().clear();
    }


}
