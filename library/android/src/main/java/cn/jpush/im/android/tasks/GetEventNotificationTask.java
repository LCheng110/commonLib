package cn.jpush.im.android.tasks;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;
import com.google.protobuf.jpush.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

/**
 * Created by hxhg on 2017/6/29.
 */

public class GetEventNotificationTask extends AbstractTask {
    private static final String TAG = GetEventNotificationTask.class.getSimpleName();

    private long uid;
    private Map<String, GetEventNotificationTaskMng.EventEntity> eventEntities;
    private GetEventNotificationInBatchCallback callback;

    public GetEventNotificationTask(long uid, Map<String, GetEventNotificationTaskMng.EventEntity> entities, GetEventNotificationInBatchCallback callback) {
        super(null, false);
        this.uid = uid;
        this.callback = callback;
        eventEntities = entities;
    }

    private String createUrl() {
        return JMessage.httpSyncPrefix + "/events";
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }

        String url = createUrl();
        if (null != url) {
            String authBase = StringUtils.getBasicAuthorization(
                    String.valueOf(IMConfigs.getUserID())
                    , IMConfigs.getToken());
            try {
                GetEventNotificationRequestEntity entity = new GetEventNotificationRequestEntity(JCoreInterface.getUid(), Consts.PLATFORM_ANDROID, eventEntities.values());
                Logger.d(TAG, "send get event content, event entity count = " + eventEntities.size() + " request entities = " + eventEntities.toString());
                response = mHttpClient.sendPost(url, JsonUtil.toJsonOnlyWithExpose(entity), authBase);
            } catch (APIRequestException e) {
                response = e.getResponseWrapper();
            } catch (APIConnectionException e) {
                response = null;
            }
            return response;
        } else {
            Logger.d(TAG, "created url is null!");
            return null;
        }
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        callback.gotResult(responseCode, responseMsg, eventEntities);
    }

    @Override
    protected void onSuccess(byte[] rawData) {
        super.onSuccess(rawData);
        int errorCode = ErrorCode.NO_ERROR;
        String errorDesc = ErrorCode.NO_ERROR_DESC;
        try {
            Message.EventPacket eventPacket = Message.EventPacket.parseFrom(rawData);
            List<Message.ConEventResponse> conSyncResponseList = eventPacket.getEventListList();

            //此时检查一次当前登录用户和发起请求时用户是否一致，如果不一致，则放弃解析,直接return。防止切换用户登陆后消息错乱。
            if (uid != IMConfigs.getUserID()) {
                Logger.ww(TAG, "current uid not match uid in protocol. abort this action.");
                return;
            }

            Logger.d(TAG, "cached entities = " + eventEntities.keySet());
            for (Message.ConEventResponse resp : conSyncResponseList) {
                Logger.d(TAG, "received entity = " + resp.getContId().toStringUtf8() + " event ids = " + resp.getEventList().size() + " expired ids = " + resp.getExpierdEidList());
                GetEventNotificationTaskMng.EventEntity eventEntity = eventEntities.get(resp.getContId().toStringUtf8());
                eventEntity.setConEventResponse(resp);
            }

            Logger.d(TAG, "result content length " + rawData.length);
        } catch (InvalidProtocolBufferException e) {
            Logger.ee(TAG, "jmessage occurs an error when parse protocol buffer.");
            errorCode = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR;
            errorDesc = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC;
            e.printStackTrace();
        }
        callback.gotResult(errorCode, errorDesc, eventEntities);
    }


    public interface GetEventNotificationInBatchCallback {
        void gotResult(int responseCode, String responseMessage, Map<String, GetEventNotificationTaskMng.EventEntity> entityMap);
    }

    private class GetEventNotificationRequestEntity {
        @Expose
        private long juid;
        @Expose
        private String platform;
        @Expose
        @SerializedName("sdk_version")
        private String sdkVersion = BuildConfig.SDK_VERSION;

        @Expose
        @SerializedName("event_list")
        private Collection<GetEventNotificationTaskMng.EventEntity> convEntities = new ArrayList<GetEventNotificationTaskMng.EventEntity>();

        GetEventNotificationRequestEntity(long juid, String platform, Collection<GetEventNotificationTaskMng.EventEntity> entities) {
            this.juid = juid;
            this.platform = platform;
            convEntities = entities;
        }

    }

}
