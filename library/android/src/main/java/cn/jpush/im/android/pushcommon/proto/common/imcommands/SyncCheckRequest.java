package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Jmconversation;

/**
 * Created by xiongtc on 16/7/29.
 */
public class SyncCheckRequest extends ImBaseRequest {
    private static final String TAG = "SyncCheckRequest";

    @Expose
    long syncConvKey;

    @Expose
    long syncEventKey;

    @Expose
    long syncReceiptKey;

    @Expose
    int syncType;

    public SyncCheckRequest(long syncConvKey, long syncEventKey, long syncReceiptKey, int syncType, long rid, long uid) {
        super(IMCommands.SyncCheck.CMD, uid, rid);

        this.syncConvKey = syncConvKey;
        this.syncEventKey = syncEventKey;
        this.syncReceiptKey = syncReceiptKey;
        this.syncType = syncType;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Jmconversation.SyncCheck syncCheck = Jmconversation.SyncCheck.newBuilder()
                .setSyncCon(Jmconversation.SyncConversation.
                        newBuilder().setSyncKey(syncConvKey).setSyncType(syncType))
                .setSyncEvent(Jmconversation.SyncEvent
                        .newBuilder().setSyncKey(syncEventKey))
                .setSyncMsgreceipt(Jmconversation.SyncMsgReceipt
                        .newBuilder().setSyncKey(syncReceiptKey)).build();

        return new IMProtocol(IMCommands.SyncCheck.CMD,
                IMCommands.SyncCheck.VERSION, imUid, appKey, syncCheck);
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
