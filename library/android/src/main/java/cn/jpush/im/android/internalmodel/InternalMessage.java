package cn.jpush.im.android.internalmodel;

import android.text.TextUtils;

import com.google.gson.jpush.JsonElement;
import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetReceiptDetailsCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.MediaContent;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.android.helpers.MessageSendingMaintainer;
import cn.jpush.im.android.helpers.MsgReceiptReportRequestPackager;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.MsgReceiptReportRequest;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetMessageReceiptDetailTask;
import cn.jpush.im.android.tasks.GetUserInfoListTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.SendingMsgCallbackManager;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.android.utils.filemng.FileUploader;
import cn.jpush.im.api.BasicCallback;


public class InternalMessage extends Message implements Cloneable {

    private static final String TAG = "InternalMessage";

    public String originMeta;

    private MessageSendingOptions messageSendingOptions;

    @Expose
    @SerializedName("set_from_name")
    private Number isSetFromName = 0;

    private int haveRead = 0;

    private int unreceiptCnt = 0;

    private long unreceiptMtime = 0L;

    public InternalMessage(MessageDirect direct, MessageContent content, String fromID, String fromAppkey,
                           String fromName, String targetId, String targetAppkey, String targetName,
                           ConversationType targetType, List<Long> atList) {
        super();
        this.direct = direct;
        this.content = content;
        this.contentType = content.getContentType();
        msgTypeString = contentType.toString();
        this.fromName = fromName;
        this.fromID = fromID;
        this.fromAppkey = fromAppkey;
        this.targetID = targetId;
        this.targetAppkey = targetAppkey;
        this.targetName = targetName;
        this.targetType = targetType;
        this.atList = atList;
        UserInfo myInfo = JMessageClient.getMyInfo();
        suiMTime = null == myInfo ? 0 : myInfo.getmTime();
        if (TextUtils.isEmpty(targetAppkey)) {
            //消息发送时，msg的targetAppkey是必填项，但是sdk群聊会话中定义的targetAppkey是空字符串，这里传过来的appkey也是空，所以这里给appkey给一个默认值，保证消息在tojson之后targetAppkey是有值的。
            this.targetAppkey = JCoreInterface.getAppKey();
        }
    }

    public InternalMessage() {
        super();
    }

    @Override
    public void getAtUserList(GetUserInfoListCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getAtUserList", callback)) {
            return;
        }

        List<InternalUserInfo> membersInfo = UserInfoManager.getInstance().getUserInfoList(atList);
        if (membersInfo != null && null != atList && membersInfo.size() == atList.size()) {
            //这里要判断数量和atList一致，不能只判断size > 0。否则有可能返回的userinfo只有一部分，这样callback上去就有问题
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, membersInfo);
        } else {
            if (!IMConfigs.getNetworkConnected()) {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED
                        , ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                return;
            }
            if (atList != null && atList.size() > 0 && atList.get(0) != 1) {
                ArrayList<Object> ids = new ArrayList<Object>(atList);
                GetUserInfoListTask task = new GetUserInfoListTask(ids, GetUserInfoListTask.IDType.uid, callback, false);
                task.execute();
            } else {
                Logger.ii(TAG, "atList is empty. ");
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
            }
        }
    }

    @Override
    public boolean isAtMe() {
        long myUid = IMConfigs.getUserID();
        return atList != null && atList.contains(myUid);
    }

    public boolean isAtAll() {
        return atList != null && atList.get(0) == 1L;
    }

    public void setId(int id) {
        this._id = id;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public void setFromID(String fromID) {
        this.fromID = fromID;
    }

    public void setNotification(JsonElement notification) {
        this.notification = notification;
    }

    public void setTargetName(String targetName) {
        this.targetName = targetName;
    }

    public void setFromType(String fromType) {
        this.fromType = fromType;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public void setAtList(List<Long> atList) {
        this.atList = atList;
    }

    public List<Long> getAtList() {
        return atList;
    }

    public void setContent(MessageContent content) {
        this.content = content;
    }

    public void setContentType(ContentType type) {
        this.contentType = type;
    }

    public void setStatus(MessageStatus status) {
        this.status = status;
    }

    public void setDirect(MessageDirect direct) {
        this.direct = direct;
    }

    public void setTargetType(ConversationType targetType) {
        this.targetType = targetType;
    }

    public void setTargetID(String targetID) {
        this.targetID = targetID;
    }

    public void setTargetInfo(Object targetInfo) {
        this.targetInfo = targetInfo;
    }

    public void setFromUser(UserInfo fromUser) {
        this.fromUser = fromUser;
    }

    public void setServerMessageId(Long serverMessageId) {
        this.serverMessageId = serverMessageId;
    }

    public String getOriginMeta() {
        return originMeta;
    }

    public void setOriginMeta(String originMeta) {
        this.originMeta = originMeta;
    }

    public void setMsgType(String msgType) {
        this.msgTypeString = msgType;
    }

    public String getMsgTypeString() {
        return msgTypeString;
    }

    public Number getIsSetFromName() {
        return isSetFromName;
    }

    public void setIsSetFromName(Number isSetFromName) {
        this.isSetFromName = isSetFromName;
    }

    @Override
    public boolean haveRead() {
        return haveRead != 0;
    }

    public void setHaveRead(int haveRead) {
        this.haveRead = haveRead;
    }

    @Override
    public int getUnreceiptCnt() {
        return unreceiptCnt;
    }

    @Override
    public long getUnreceiptMtime() {
        return unreceiptMtime;
    }

    @Override
    public void setUnreceiptCnt(int unreceipteCnt) {
        this.unreceiptCnt = unreceipteCnt;
    }

    @Override
    public void setUnreceiptMtime(long unreceipteMtime) {
        this.unreceiptMtime = unreceipteMtime;
    }

    @Override
    public void getReceiptDetails(GetReceiptDetailsCallback callback) {
        if (direct != MessageDirect.send) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_GET_RECEIPT_DETAIL_PERMISSION_ERROR, ErrorCode.LOCAL_ERROR.LOCAL_GET_RECEIPT_DETAIL_PERMISSION_ERROR_DESC);
            return;
        }

        if (status != MessageStatus.send_success || 0 == serverMessageId) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_GET_RECEIPT_DETAIL_STATUS_ERROR, ErrorCode.LOCAL_ERROR.LOCAL_GET_RECEIPT_DETAIL_STATUS_ERROR_DESC);
        }
        new GetMessageReceiptDetailTask(Collections.singletonList(serverMessageId), callback, false).execute();
    }

    public int getCreateTimeInSeconds() {
        return null != createTimeInSeconds ? createTimeInSeconds.intValue() : 0;
    }

    public String getFromAppKey() {
        if (null == fromAppkey) {
            Logger.w(TAG, "from appkey is null ,return default value.");
            return JCoreInterface.getAppKey();
        }
        return fromAppkey;
    }

    public void setCreateTime(long createTimeInMills) {
        this.createTimeInMillis = createTimeInMills;
        this.createTimeInSeconds = (int) (createTimeInMills / 1000);
    }

    public void setMsgBody(JsonElement msgBody) {
        this.msgBody = msgBody;
    }

    public int getVersion() {
        return null != version ? version.intValue() : 0;
    }

    public JsonElement getNotification() {
        return notification;
    }

    public JsonElement getMsgBody() {
        return msgBody;
    }

    @Override
    public UserInfo getFromUser() {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getFromUser", null)) {
            return null;
        }

        //使用lazy load加载fromUser信息。
        if (null == fromUser) {
            //首先从本地获取fromUser的信息。
            if (targetType == ConversationType.single) {
                UserInfo userInfo = (UserInfo) targetInfo;
                if (direct == MessageDirect.receive) {
                    fromUser = userInfo;
                } else if (direct == MessageDirect.send) {
                    fromUser = JMessageClient.getMyInfo();
                }
            } else if (targetType == ConversationType.group) {
                GroupInfo groupInfo = (GroupInfo) targetInfo;
                if (direct == MessageDirect.receive) {
                    if (contentType == ContentType.eventNotification) {
                        //如果这条消息是event notification类型的消息（也就是群成员变化的消息）
                        //则不触发获取group member info的逻辑，因为fromID 是 "系统消息"，获取fromUser没有意义。
                        fromUser = null;
                    } else {
                        fromUser = groupInfo.getGroupMemberInfo(fromID, fromAppkey);
                    }
                } else if (direct == MessageDirect.send) {
                    fromUser = JMessageClient.getMyInfo();
                }
            }

            //如果本地获取到的是空，则返回一个临时的userinfo上去，同时背后通过网络去获取userinfo后再更新。
            if (null == fromUser) {
                InternalUserInfo tempUser = new InternalUserInfo();
                tempUser.setUserName(fromID);
                if (null != fromID && !fromID.equals(fromName)) {
                    tempUser.setNickname(fromName);
                }
                if (contentType != ContentType.eventNotification) {
                    //只有当消息类型不是eventNotification时，才去重新请求fromUser的userinfo。
                    //如果是 eventNotification(群成员变化事件消息),targetID被固定为"系统消息"，此时获取fromUser是没有意义的。
                    Logger.dd(TAG, "fromUser not exists! return a temp userInfo and get userInfo from Server");
                    getUserInfoAndUpdate(fromID, fromAppkey);
                }
                fromUser = tempUser;
            }
        }
        return fromUser;
    }

    @Override
    public String getTargetName() {
        if (null == targetName && null != targetInfo) {
            if (targetType == ConversationType.single) {
                InternalUserInfo userInfo = (InternalUserInfo) targetInfo;
                //不能使用displayName作为消息的targetName,因为备注名只是发送者才可见
                targetName = userInfo.getDisplayName(false);
            } else if (targetType == ConversationType.group) {
                try {
                    GroupInfo targetGroupInfo = (GroupInfo) targetInfo;
                    if (!TextUtils.isEmpty(targetGroupInfo.getGroupName())) {
                        targetName = targetGroupInfo.getGroupName();
                    } else {
                        targetName = targetID;
                    }
                } catch (NumberFormatException nfe) {
                    Logger.ee(TAG, "targetID parse failed! ");
                    targetName = targetID;
                }
            }
        }
        return targetName;
    }

    @Override
    public String getTargetID() {
        if (null == targetID && null != targetInfo) {
            if (targetType == ConversationType.single) {
                UserInfo userInfo = (UserInfo) targetInfo;
                targetID = userInfo.getUserName();
            } else if (targetType == ConversationType.group) {
                GroupInfo groupInfo = (GroupInfo) targetInfo;
                targetID = String.valueOf(groupInfo.getGroupID());
            }
        }
        return targetID;
    }

    @Override
    public String getTargetAppKey() {
        if (targetType == ConversationType.group) {
            return "";//如果是群聊消息，从这里给出去appkey统一为空字符串，因为包括会话缓存和通知栏id等sdk内部用到的群聊targetAppkey都是空字符串。
        } else if (null == targetAppkey && null != targetInfo) {
            UserInfo userInfo = (UserInfo) targetInfo;
            targetAppkey = userInfo.getAppKey();
        }
        return targetAppkey;
    }

    public void setFromAppkey(String fromAppkey) {
        this.fromAppkey = fromAppkey;
    }

    public void setTargetAppkey(String targetAppkey) {
        this.targetAppkey = targetAppkey;
    }

    public MessageSendingOptions getMessageSendingOptions() {
        return messageSendingOptions;
    }

    public void setMessageSendingOptions(MessageSendingOptions messageSendingOptions) {
        this.messageSendingOptions = messageSendingOptions;
    }

    public void setSenderUserInfoMTime(int suiMTime) {
        this.suiMTime = suiMTime;
    }

    public String toJson() {
        if (contentType == ContentType.custom) {
            CustomContent customContent = (CustomContent) content;
            this.msgBody = customContent.toJsonElement();
        } else {
            this.msgBody = content.toJsonElement();
        }
        return JsonUtil.toJsonOnlyWithExpose(this);
    }

    @Override
    public void setOnContentUploadProgressCallback(ProgressUpdateCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("setOnContentUploadProgressCallback", null)) {
            return;
        }
        SendingMsgCallbackManager
                .saveCallbacks(getTargetID(), getTargetAppKey(), _id, this.hashCode(), callback, null, null);
        if (null != FileUploader.getProgressInCache(getTargetID(), getTargetAppKey(), _id)) {
            double progress = FileUploader.getProgressInCache(getTargetID(), getTargetAppKey(), _id);
            Logger.d(TAG, "onProgressCallback set ! progress = " + progress);
            if (0d != progress) {
                CommonUtils.doProgressCallbackToUser(getTargetID(), getTargetAppKey(), _id, progress);
            }
        }
    }

    @Override
    public boolean isContentUploadProgressCallbackExists() {
        return SendingMsgCallbackManager.getUploadProgressCallbackFromHash(this.hashCode()) != null;
    }

    @Override
    public void setOnContentDownloadProgressCallback(ProgressUpdateCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("setOnContentDownloadProgressCallback", null)) {
            return;
        }
        SendingMsgCallbackManager
                .saveCallbacks(getTargetID(), getTargetAppKey(), _id, this.hashCode(), null, callback, null);
    }

    @Override
    public boolean isContentDownloadProgressCallbackExists() {
        return SendingMsgCallbackManager.getDownloadProgressCallbackFromHash(this.hashCode())
                != null;
    }

    @Override
    public boolean isSendCompleteCallbackExists() {
        return SendingMsgCallbackManager.getCompleteCallbackFromHash(this.hashCode()) != null;
    }

    @Override
    public void setHaveRead(final BasicCallback callback) {
        if (0 != haveRead || MessageDirect.receive != direct) {
            //当本地haveRead状态不为0，或者不是接受到的消息，此时不触发已读回执上报。
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_SET_HAVEREAD_ERROR, ErrorCode.LOCAL_ERROR.LOCAL_SET_HAVEREAD_ERROR_DESC);
            return;
        }
        switch (targetType) {
            case single:
                UserIDHelper.getUserID(getTargetID(), getTargetAppKey(), new UserIDHelper.GetUseridsCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<Long> userids) {
                        if (null != userids && !userids.isEmpty()) {
                            MsgReceiptReportRequestPackager.getInstance().addServerMsgId(userids.get(0), MsgReceiptReportRequest.MSG_TYPE_SINGLE, serverMessageId, new ReceiptReportCallback(callback));
                        } else {
                            CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                        }
                    }
                });
                break;
            case group:
                MsgReceiptReportRequestPackager.getInstance().addServerMsgId(Long.valueOf(getTargetID()), MsgReceiptReportRequest.MSG_TYPE_GROUP, serverMessageId, new ReceiptReportCallback(callback));
                break;
        }
    }

    private class ReceiptReportCallback extends BasicCallback {
        private BasicCallback userCallback;

        ReceiptReportCallback(BasicCallback userCallback) {
            this.userCallback = userCallback;
        }

        @Override
        public void gotResult(int responseCode, String responseMessage) {
            if (ErrorCode.NO_ERROR == responseCode) {
                setHaveRead(1);
            }
            CommonUtils.doCompleteCallBackToUser(userCallback, responseCode, responseMessage);
        }
    }

    @Override
    public void setOnSendCompleteCallback(BasicCallback sendCompleteCallback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("setOnSendCompleteCallback", null)) {
            return;
        }
        SendingMsgCallbackManager.saveCallbacks(getTargetID(), getTargetAppKey(), _id, this.hashCode(), null, null,
                sendCompleteCallback);
    }

    public synchronized void send(final MessageSendingOptions options) {
        final InternalConversation conv = ConversationManager.getInstance().getConversation(targetType,
                getTargetID(), getTargetAppKey());
        if (status != MessageStatus.send_going) {
            Logger.d(TAG, "send msg start ! time = " + System.currentTimeMillis());
            //将此条消息加入到发送状态的列表中
            MessageSendingMaintainer.addIdentifier(getTargetID(), getTargetAppKey(), _id);
            conv.updateMessageStatus(this, MessageStatus.send_going); //将消息状态改为send_going

            if (content instanceof MediaContent) {
                boolean isFileUploaded = ((MediaContent) content).isFileUploaded();
                if (!isFileUploaded) {
                    FileUploader manager = new FileUploader();
                    manager.doUploadMsgAttachFile(this, new BasicCallback(false) {
                        @Override
                        public void gotResult(final int responseCode, final String s) {
                            if (responseCode == 0) {
                                internalSendMessage(options);
                            } else {
                                CommonUtils.doMessageCompleteCallbackToUser(getTargetID(), getTargetAppKey(), _id, responseCode, s);
                            }
                        }
                    });
                } else {
                    CommonUtils.doProgressCallbackToUser(getTargetID(), getTargetAppKey(), _id, 1.0);
                    internalSendMessage(options);
                }
            } else if (null != content) {
                internalSendMessage(options);
            }
        } else {
            Logger.ww(TAG, "[sendMessage] this message is in sending progress.");
        }
    }

    private void internalSendMessage(final MessageSendingOptions options) {
        if (getTargetType().equals(ConversationType.single)) {
            //use targetID to get userID, then send message
            InternalUserInfo targetUserInfo = ((InternalUserInfo) getTargetInfo());
            if (null != targetUserInfo) {
                final String targetUsername = targetUserInfo.getUserName();
                String targetAppkey = targetUserInfo.getAppKey();
                UserIDHelper.getUserID(targetUsername, targetAppkey, new UserIDHelper.GetUseridsCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<Long> userids) {
                        if (code == 0) {
                            Logger.d(TAG, "got user info when send message! username = " +
                                    targetUsername + " userid = " + userids);
                            long uid = userids.get(0);
                            RequestProcessor.MsgSendResponse response = RequestProcessor.imSingleMsgSend(JMessage.mContext, uid, InternalMessage.this,
                                    options, CommonUtils.getSeqID());
                            if (0 != response.getResponseCode()) {
                                sendMessageFailPostExecute(response.getResponseCode(), response.getDesc());
                            }
                        } else {
                            sendMessageFailPostExecute(code, msg);
                        }
                    }
                });
            } else {
                Logger.ww(TAG, "target info is null,can not send message!");
                sendMessageFailPostExecute(ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
            }
        } else {
            //since targetID in group message is groupID already,we send message directly
            long groupID;
            try {
                groupID = Long.parseLong(getTargetID());
            } catch (NumberFormatException e) {
                Logger.ee(TAG, "JMessage catch a number formate exception,maybe your conversation's target_id is 'String' while conversation_type is 'group'.");
                sendMessageFailPostExecute(ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_PARAMETERS_DESC);
                return;
            }
            RequestProcessor.MsgSendResponse response = RequestProcessor
                    .imGroupMsgSend(JMessage.mContext, groupID, options, this, CommonUtils.getSeqID());
            if (0 != response.getResponseCode()) {
                sendMessageFailPostExecute(response.getResponseCode(), response.getDesc());
            }
        }

    }

    private void sendMessageFailPostExecute(int code, String msg) {
        InternalConversation conv = ConversationManager.getInstance().getConversation(getTargetType(),
                getTargetID(), getTargetAppKey());
        if (null != conv) {
            conv.updateMessageStatus(this, MessageStatus.send_fail);
            CommonUtils.doMessageCompleteCallbackToUser(getTargetID(), getTargetAppKey(), getId(), code, msg);
        } else {
            Logger.w(TAG, "conversation is null! return from internal send message.");
        }
    }

    public Object clone() {
        InternalMessage o = null;
        try {
            o = (InternalMessage) super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.ww(TAG, "clone message failed!");
            e.printStackTrace();
        }
        o.content = (MessageContent) content.clone();
        return o;
    }


    private void getUserInfoAndUpdate(final String userName, String fromAppkey) {
        JMessageClient.getUserInfo(userName, fromAppkey, new GetUserInfoCallback(false) {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                Logger.d(TAG, "get user info finished ! response code = " + responseCode);
                if (0 == responseCode) {
                    InternalUserInfo internalUserInfo = (InternalUserInfo) fromUser;
                    internalUserInfo.copyUserInfo((InternalUserInfo) info, false, false, false);
                }
            }
        });
    }

    @Override
    public String toString() {
        return "Message{" +
                "_id=" + _id +
                ", messageId=" + serverMessageId +
                ", createTimeInMillis=" + createTimeInMillis +
                ", direct=" + direct +
                ", status=" + status +
                ", content=" + (content == null ? null : content.toJson()) +
                ", version=" + version +
                ", fromName='" + fromName + '\'' +
                ", contentType=" + contentType +
                ", contentTypesString='" + msgTypeString + '\'' +
                ", targetType=" + targetType +
                ", targetID='" + targetID + '\'' +
                ", targetName='" + targetName + '\'' +
                ", fromType='" + fromType + '\'' +
                ", atList=" + atList +
                ", fromID=" + fromID +
                ", notification=" + notification +
                ", isSetFromName=" + isSetFromName +
                ", suiMTime=" + suiMTime +
                ", haveRead=" + haveRead +
                ", unreceipt cnt=" + unreceiptCnt +
                ", unreceipt mtime=" + unreceiptMtime +
                '}';
    }
}
