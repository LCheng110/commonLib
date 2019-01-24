package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.protobuf.jpush.ByteString;
import com.google.protobuf.jpush.InvalidProtocolBufferException;

import cn.jpush.im.android.pushcommon.proto.Event;
import cn.jpush.im.android.pushcommon.proto.Friend;
import cn.jpush.im.android.pushcommon.proto.Friend.AddFriend;
import cn.jpush.im.android.pushcommon.proto.Friend.DelFriend;
import cn.jpush.im.android.pushcommon.proto.Group;
import cn.jpush.im.android.pushcommon.proto.Group.AddGroupMember;
import cn.jpush.im.android.pushcommon.proto.Group.CreateGroup;
import cn.jpush.im.android.pushcommon.proto.Group.DelGroupMember;
import cn.jpush.im.android.pushcommon.proto.Group.ExitGroup;
import cn.jpush.im.android.pushcommon.proto.Group.UpdateGroupInfo;
import cn.jpush.im.android.pushcommon.proto.Im.Packet;
import cn.jpush.im.android.pushcommon.proto.Im.ProtocolBody;
import cn.jpush.im.android.pushcommon.proto.Im.ProtocolHead;
import cn.jpush.im.android.pushcommon.proto.Im.Response;
import cn.jpush.im.android.pushcommon.proto.Jmconversation;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.Message.ChatMsgSync;
import cn.jpush.im.android.pushcommon.proto.Message.EventAnswer;
import cn.jpush.im.android.pushcommon.proto.Message.EventNotification;
import cn.jpush.im.android.pushcommon.proto.Message.EventSync;
import cn.jpush.im.android.pushcommon.proto.Message.GroupMsg;
import cn.jpush.im.android.pushcommon.proto.Message.SingleMsg;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.pushcommon.proto.User;
import cn.jpush.im.android.pushcommon.proto.User.Login;
import cn.jpush.im.android.pushcommon.proto.User.Logout;
import cn.jpush.im.android.utils.Logger;

public class IMProtocol {
    private static final String TAG = "IMProtocol";
    private static final int platform = 1;//1 -- android,2 -- iOS,4 -- WinPhone,8 -- API,16 -- WebIM
    private int command;
    int version;
    long uid;
    String appKey;
    private Object entity;
    private ProtocolHead protocolHead;

    Response response;

    int responseCode = -1;
    private String responseMessage;

    // outbound
    public IMProtocol(int command, int version, long uid, String appKey, Object entity) {
        this.command = command;
        this.version = version;
        this.uid = uid;
        this.appKey = appKey;
        this.entity = entity;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public void clearEntity() {
        this.entity = null;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public void setResponse(int code, String error) {
        this.responseCode = code;
        this.responseMessage = error;

        Response.Builder builder = Response.newBuilder().setCode(code);
        if (null != error) {
            builder.setMessage(ByteString.copyFromUtf8(error));
        }
        this.response = builder.build();
    }

    public Packet toProtocolBuffer() {
        ProtocolHead.Builder head = ProtocolHead.newBuilder()
                .setCmd(command)
                .setVer(version)
                .setPlatform(platform)
                .setUid(uid);
        if (null != appKey && !"".equalsIgnoreCase(appKey.trim())) {
            head.setAppkey(ByteString.copyFromUtf8(appKey));
        }

        return Packet.newBuilder()
                .setHead(head.build())
                .setBody(buildBody(command, entity, response))
                .build();
    }

    private static ProtocolBody buildBody(int command, Object entity, Response response) {
        ProtocolBody.Builder body = ProtocolBody.newBuilder();
        if (null != response) {
            body.setCommonRep(response);
        }

        if (null == entity) return body.build();

        switch (command) {
            case IMCommands.Login.CMD:
                body.setLogin((Login) entity);
                break;
            case IMCommands.Logout.CMD:
                body.setLogout((Logout) entity);
                break;

            case IMCommands.SingleMsg.CMD:
                body.setSingleMsg((SingleMsg) entity);
                break;
            case IMCommands.GroupMsg.CMD:
                body.setGroupMsg((GroupMsg) entity);
                break;

            case IMCommands.AddFriend.CMD:
                body.setAddFriend((AddFriend) entity);
                break;
            case IMCommands.DelFriend.CMD:
                body.setDelFriend((DelFriend) entity);
                break;
            case IMCommands.UpdateMemo.CMD:
                body.setUpdateMemo((Friend.UpdateMemo) entity);
                break;

            case IMCommands.CreateGroup.CMD:
                body.setCreateGroup((CreateGroup) entity);
                break;
            case IMCommands.ExitGroup.CMD:
                body.setExitGroup((ExitGroup) entity);
                break;
            case IMCommands.AddGroupMember.CMD:
                body.setAddGroupMember((AddGroupMember) entity);
                break;
            case IMCommands.DelGroupMember.CMD:
                body.setDelGroupMember((DelGroupMember) entity);
                break;
            case IMCommands.UpdateGroupInfo.CMD:
                body.setUpdateGroupInfo((UpdateGroupInfo) entity);
                break;

            case IMCommands.EventNotification.CMD:
                body.setEventNotification((EventNotification) entity);
                break;
            case IMCommands.ChatMsgSync.CMD:
                body.setChatMsg((ChatMsgSync) entity);
                break;
            case IMCommands.EventSync.CMD:
                body.setEventSync((EventSync) entity);
                break;
            case IMCommands.EventAnswer.CMD:
                body.setEventAnswer((EventAnswer) entity);
                break;
            case IMCommands.AddBlackList.CMD:
                body.setAddBlacklist((Friend.AddBlackList) entity);
                break;
            case IMCommands.DelBlackList.CMD:
                body.setDelBlacklist((Friend.DelBlackList) entity);
                break;

            case IMCommands.ReportInformation.CMD:
                body.setReportInfo((User.ReportInformation) entity);
                break;

            case IMCommands.AddMsgnoDisturbSingle.CMD:
                body.setAddMsgNoDisturbSingle((Message.AddMsgnoDisturbSingle) entity);
                break;
            case IMCommands.DeleteMsgnoDisturbSingle.CMD:
                body.setDeleteMsgNoDisturbSingle((Message.DeleteMsgnoDisturbSingle) entity);
                break;
            case IMCommands.AddMsgnoDisturbGroup.CMD:
                body.setAddMsgNoDisturbGroup((Message.AddMsgnoDisturbGroup) entity);
                break;
            case IMCommands.DeleteMsgnoDisturbGroup.CMD:
                body.setDeleteMsgNoDisturbGroup((Message.DeleteMsgnoDisturbGroup) entity);
                break;
            case IMCommands.AddMsgnoDisturbGlobal.CMD:
                body.setAddMsgNoDisturbGlobal((Message.AddMsgnoDisturbGlobal) entity);
                break;
            case IMCommands.DeleteMsgnoDisturbGlobal.CMD:
                body.setDeleteMsgNoDisturbGlobal((Message.DeleteMsgnoDisturbGlobal) entity);
                break;
            case IMCommands.AddGroupToBlock.CMD:
                body.setAddGroupToShielding((Group.AddMsgshieldGroup) entity);
                break;
            case IMCommands.DelGroupFromBlock.CMD:
                body.setDelGroupFromShielding((Group.DelMsgshieldGroup) entity);
                break;
            case IMCommands.AddMsgReceipt.CMD:
                body.setAddMsgReceipt((Message.AddMsgReceipt) entity);
                break;
            case IMCommands.DelMsgReceipt.CMD:
                body.setDelMsgReceipt((Message.DelMsgReceipt) entity);
                break;
            case IMCommands.SyncCheck.CMD:
                body.setSyncCheck((Jmconversation.SyncCheck) entity);
                break;
            case IMCommands.SyncConversationResp.CMD:
                body.setSyncConResp((Jmconversation.SyncConversationResp) entity);
                break;
            case IMCommands.SyncConversationACK.CMD:
                body.setSyncConAck((Jmconversation.SyncConversationACK) entity);
                break;
            case IMCommands.SyncEventACK.CMD:
                body.setSyncEventAck((Event.SyncEventACK) entity);
                break;
            case IMCommands.MsgRetract.CMD:
                body.setMsgRetract((Message.MsgRetract) entity);
                break;
            case IMCommands.UpdateUnreadCount.CMD:
                body.setUpdateUnreadCnt((Jmconversation.UpdateUnreadCount) entity);
                break;
            case IMCommands.SyncMsgReceiptResp.CMD:
                body.setSyncMsgreceiptResp((Receipt.SyncMsgReceiptResp) entity);
                break;
            case IMCommands.SyncMsgReceiptACK.CMD:
                body.setSyncMsgreceiptAck((Receipt.SyncMsgReceiptACK) entity);
                break;
            case IMCommands.MsgReceiptReport.CMD:
                body.setMsgReceiptReport((Receipt.MsgReceiptReport) entity);
                break;
            case IMCommands.MsgReceiptChange.CMD:
                body.setMsgReceiptChange((Receipt.MsgReceiptChange) entity);
                break;
            case IMCommands.TransCommand.CMD:
                body.setTransCommand((Message.TransCommand) entity);
                break;
            case IMCommands.CommandNotification.CMD:
                body.setCommandNotification((Message.CommandNotification) entity);
                break;

            default:
                Logger.w(TAG, "Unhandled cmd - " + command + " maybe you forgot add a case in this method to handle it");
        }
        return body.build();
    }

    // inbound
    public IMProtocol(byte[] data) throws InvalidProtocolBufferException {
        Packet protocol = Packet.parseFrom(data);

        protocolHead = protocol.getHead();
        ProtocolBody body = protocol.getBody();

        this.command = protocolHead.getCmd();
        this.version = protocolHead.getVer();
        this.uid = protocolHead.getUid();

        if (null != protocolHead.getAppkey()) {
            this.appKey = protocolHead.getAppkey().toStringUtf8();
        }

        this.response = body.getCommonRep();

        switch (this.command) {
            case IMCommands.Login.CMD:
                this.entity = body.getLogin();
                break;
            case IMCommands.Logout.CMD:
                this.entity = body.getLogout();
                break;

            case IMCommands.SingleMsg.CMD:
                this.entity = body.getSingleMsg();
                break;
            case IMCommands.GroupMsg.CMD:
                this.entity = body.getGroupMsg();
                break;

            case IMCommands.AddFriend.CMD:
                this.entity = body.getAddFriend();
                break;
            case IMCommands.DelFriend.CMD:
                this.entity = body.getDelFriend();
                break;
            case IMCommands.UpdateMemo.CMD:
                this.entity = body.getUpdateMemo();
                break;

            case IMCommands.CreateGroup.CMD:
                this.entity = body.getCreateGroup();
                break;
            case IMCommands.ExitGroup.CMD:
                this.entity = body.getExitGroup();
                break;
            case IMCommands.AddGroupMember.CMD:
                this.entity = body.getAddGroupMember();
                break;
            case IMCommands.DelGroupMember.CMD:
                this.entity = body.getDelGroupMember();
                break;
            case IMCommands.UpdateGroupInfo.CMD:
                this.entity = body.getUpdateGroupInfo();
                break;


            case IMCommands.EventNotification.CMD:
                this.entity = body.getEventNotification();
                break;
            case IMCommands.ChatMsgSync.CMD:
                this.entity = body.getChatMsg();
                break;
            case IMCommands.EventSync.CMD:
                this.entity = body.getEventSync();
                break;
            case IMCommands.EventAnswer.CMD:
                this.entity = body.getEventAnswer();
                break;

            case IMCommands.AddBlackList.CMD:
                this.entity = body.getAddBlacklist();
                break;
            case IMCommands.DelBlackList.CMD:
                this.entity = body.getDelBlacklist();
                break;

            case IMCommands.ReportInformation.CMD:
                this.entity = body.getReportInfo();
                break;

            case IMCommands.AddMsgnoDisturbSingle.CMD:
                this.entity = body.getAddMsgNoDisturbSingle();
                break;
            case IMCommands.DeleteMsgnoDisturbSingle.CMD:
                this.entity = body.getDeleteMsgNoDisturbSingle();
                break;
            case IMCommands.AddMsgnoDisturbGroup.CMD:
                this.entity = body.getAddMsgNoDisturbGroup();
                break;
            case IMCommands.DeleteMsgnoDisturbGroup.CMD:
                this.entity = body.getDeleteMsgNoDisturbGroup();
                break;
            case IMCommands.AddMsgnoDisturbGlobal.CMD:
                this.entity = body.getAddMsgNoDisturbGlobal();
                break;
            case IMCommands.DeleteMsgnoDisturbGlobal.CMD:
                this.entity = body.getDeleteMsgNoDisturbGlobal();
                break;
            case IMCommands.AddGroupToBlock.CMD:
                this.entity = body.getAddGroupToShielding();
                break;
            case IMCommands.DelGroupFromBlock.CMD:
                this.entity = body.getDelGroupFromShielding();
            case IMCommands.AddMsgReceipt.CMD:
                this.entity = body.getAddMsgReceipt();
                break;
            case IMCommands.DelMsgReceipt.CMD:
                this.entity = body.getDelMsgReceipt();
                break;
            case IMCommands.SyncCheck.CMD:
                this.entity = body.getSyncCheck();
                break;
            case IMCommands.SyncConversationResp.CMD:
                this.entity = body.getSyncConResp();
                break;
            case IMCommands.SyncConversationACK.CMD:
                this.entity = body.getSyncConAck();
                break;
            case IMCommands.MsgRetract.CMD:
                this.entity = body.getMsgRetract();
                break;
            case IMCommands.SyncEventResp.CMD:
                this.entity = body.getSyncEventResp();
                break;
            case IMCommands.SyncEventACK.CMD:
                this.entity = body.getSyncEventAck();
                break;
            case IMCommands.SyncMsgReceiptResp.CMD:
                this.entity = body.getSyncMsgreceiptResp();
                break;
            case IMCommands.SyncMsgReceiptACK.CMD:
                this.entity = body.getSyncMsgreceiptAck();
                break;
            case IMCommands.MsgReceiptReport.CMD:
                this.entity = body.getMsgReceiptReport();
                break;
            case IMCommands.MsgReceiptChange.CMD:
                this.entity = body.getMsgReceiptChange();
                break;
            case IMCommands.UpdateUnreadCount.CMD:
                this.entity = body.getUpdateUnreadCnt();
            case IMCommands.TransCommand.CMD:
                this.entity = body.getTransCommand();
                break;
            case IMCommands.CommandNotification.CMD:
                this.entity = body.getCommandNotification();
                break;

            default:
                Logger.w(TAG, "Unhandled IM cmd yet - " + command);
                this.entity = null;
        }

    }

    public IMProtocol resetProtocol(String appKey) {
        this.appKey = appKey;
        return this;
    }

    public int getCommand() {
        return command;
    }

    public int getVersion() {
        return version;
    }

    public long getUid() {
        return uid;
    }

    public Response getResponse() {
        return response;
    }

    public Object getEntity() {
        return entity;
    }

    public String getAppKey() {
        return this.appKey;
    }

    public ProtocolHead getProtocolHead() {
        return protocolHead;
    }

    public String toString() {
        return "[IMProtocol] - command:" + this.command
                + ", version:" + this.version
                + ", uid:" + this.uid
                + ", appkey:" + this.appKey
                + ((this.responseCode >= 0) ? (", responseCode:" + this.responseCode) : "")
                + ((this.responseCode >= 0 && this.responseMessage != null) ? (", responseMessage:" + this.responseMessage) : "")
                + ((null == this.entity) ? "" : (", entity:" + this.entity.toString()));
    }
}
