package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Jmconversation;

/**
 * Created by xiongtc on 16/8/1.
 */
public class SyncConvACKRequest extends ImBaseRequest {

    @Expose
    long syncKey;

    public SyncConvACKRequest(long syncKey, long rid, long uid) {
        super(IMCommands.SyncConversationACK.CMD, uid, rid);

        this.syncKey = syncKey;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Jmconversation.SyncConversationACK syncConversationACK =
                Jmconversation.SyncConversationACK.newBuilder().setSyncKey(syncKey).build();

        return new IMProtocol(cmd, IMCommands.SyncConversationACK.VERSION,
                imUid, appKey, syncConversationACK);
    }

    @Override
    public void onResponseTimeout() {
        //do nothing...
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        //do nothing...
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        //do nothing...
    }

}
