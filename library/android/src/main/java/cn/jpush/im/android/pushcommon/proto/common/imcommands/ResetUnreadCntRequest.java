package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Jmconversation;

/**
 * Created by hxhg on 2017/8/17.
 */

public class ResetUnreadCntRequest extends ImBaseRequest {

    public static final int TARGET_TYPE_SINGLE = 3;
    public static final int TARGET_TYPE_GROUP = 4;

    @Expose
    long targetUidOrGid;//会话对象id,单聊为对方uid,群聊为群的gid

    @Expose
    int targetType;//会话类型。3-单聊 4-群聊

    @Expose
    int readCnt;//本次需要重置的会话未读数

    public ResetUnreadCntRequest(long targetUidOrGid, int targetType, int readCnt, long rid, long uid) {
        super(IMCommands.UpdateUnreadCount.CMD, uid, rid);
        this.targetUidOrGid = targetUidOrGid;
        this.readCnt = readCnt;
        this.targetType = targetType;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Jmconversation.UpdateUnreadCount.Builder builder = Jmconversation.UpdateUnreadCount.newBuilder();
        builder.setReadCount(readCnt).setTarget(targetUidOrGid).setType(targetType);

        return new IMProtocol(IMCommands.UpdateUnreadCount.CMD, IMCommands.UpdateUnreadCount.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {

    }

    @Override
    public void onResponse(IMProtocol imProtocol) {

    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {

    }
}
