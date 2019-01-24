package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Receipt;

/**
 * Created by hxhg on 2017/8/22.
 */

public class SyncReceiptACKRequest extends ImBaseRequest {

    @Expose
    long syncKey;

    public SyncReceiptACKRequest(long syncKey, long rid, long uid) {
        super(IMCommands.SyncMsgReceiptACK.CMD, uid, rid);

        this.syncKey = syncKey;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Receipt.SyncMsgReceiptACK syncEventACK =
                Receipt.SyncMsgReceiptACK.newBuilder().setSyncKey(syncKey).build();
        return new IMProtocol(cmd, IMCommands.SyncMsgReceiptACK.CMD, imUid, appKey, syncEventACK);
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
