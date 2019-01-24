package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.pushcommon.proto.Message.EventSync;
import cn.jpush.im.android.utils.JsonUtil;


// Actually it is response
public class EventSyncRequest extends ImBaseRequest {

    @Expose
    long eventId;

    public EventSyncRequest(long eventId, long rid, long uid) {
        super(IMCommands.ChatMsgSync.CMD, uid, rid);

        this.eventId = eventId;
    }

    public static EventSyncRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, EventSyncRequest.class);
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        EventSync event = EventSync.newBuilder()
                .setEventId(eventId)
                .build();

        return new IMProtocol(IMCommands.GroupMsg.CMD,
                IMCommands.GroupMsg.VERSION,
                imUid, appKey, event);
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
