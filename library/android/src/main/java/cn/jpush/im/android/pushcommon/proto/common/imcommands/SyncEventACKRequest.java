package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Event;

/**
 * Created by hxhg on 2017/6/29.
 */

public class SyncEventACKRequest extends ImBaseRequest {

    @Expose
    long syncKey;

    public SyncEventACKRequest(long syncKey, long rid, long uid) {
        super(IMCommands.SyncEventACK.CMD, uid, rid);

        this.syncKey = syncKey;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Event.SyncEventACK syncEventACK =
                Event.SyncEventACK.newBuilder().setSyncKey(syncKey).build();
        return new IMProtocol(cmd, IMCommands.SyncEventACK.VERSION, imUid, appKey, syncEventACK);
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
