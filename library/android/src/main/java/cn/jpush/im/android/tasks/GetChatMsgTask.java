package cn.jpush.im.android.tasks;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;
import com.google.protobuf.jpush.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.common.ChatMsgManager;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.pushcommon.proto.Message;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

/**
 * Created by xiongtc on 16/8/17.
 */
public class GetChatMsgTask extends AbstractTask {
    private static final String TAG = "GetChatMsgTask";

    private long uid;
    private Map<String, GetChatMsgTaskMng.ConvEntity> convEntities;
    private GetChatMsgCallback callback;

    public GetChatMsgTask(long uid, Map<String, GetChatMsgTaskMng.ConvEntity> entities, GetChatMsgCallback callback) {
        super(null, false);
        this.uid = uid;
        this.callback = callback;
        convEntities = entities;
    }

    private String createUrl() {
        return JMessage.httpSyncPrefix + "/messages";
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
                GetChatMsgRequestEntity entity = new GetChatMsgRequestEntity(JCoreInterface.getUid(), Consts.PLATFORM_ANDROID, convEntities.values());
                Logger.d(TAG, "send get msg content, conv count = " + convEntities.size() + " request entities = " + convEntities.toString());
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
        callback.gotResult(responseCode, responseMsg, convEntities);
    }

    @Override
    protected void onSuccess(byte[] rawData) {
        super.onSuccess(rawData);
        int errorCode = ErrorCode.NO_ERROR;
        String errorDesc = ErrorCode.NO_ERROR_DESC;
        try {
            Message.MsgPacket msgPacket = Message.MsgPacket.parseFrom(rawData);
            List<Message.ConSyncResponse> conSyncResponseList = msgPacket.getCsrListList();

            //此时检查一次当前登录用户和发起请求时用户是否一致，如果不一致，则放弃解析,直接return。防止切换用户登陆后消息错乱。
            if (uid != IMConfigs.getUserID()) {
                Logger.ww(TAG, "current uid not match uid in protocol. abort this action.");
                return;
            }

            //将所有消息本地化
            Logger.d(TAG, "start to save chat msg..  received conv count = " + conSyncResponseList.size() + "entities = " + convEntities.toString());
            final CountDownLatch latch = new CountDownLatch(conSyncResponseList.size());
            for (final Message.ConSyncResponse conSyncResp : conSyncResponseList) {
                Task.callInBackground(new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Map<Long, Receipt.MsgReceiptMeta> metaMap = new HashMap<Long, Receipt.MsgReceiptMeta>();
                        for (Receipt.MsgReceiptMeta meta : conSyncResp.getReceiptMsglistList()) {
                            metaMap.put(meta.getMsgid(), meta);
                        }
                        Logger.d(TAG, " msg ids size = " + conSyncResp.getChatMsgList().size() + " expired ids = " + conSyncResp.getExpierdMsgidList().size() + "conv id = " + conSyncResp.getConId().toStringUtf8());
                        if (0 != conSyncResp.getChatMsgCount()) {
                            GetChatMsgTaskMng.ConvEntity entity = convEntities.get(conSyncResp.getConId().toStringUtf8());
                            Logger.d(TAG, "ready to parseSyncInBatch. conv id " + entity.getConvId() + " entity = " + entity);
                            Collection<InternalMessage> msgs = ChatMsgManager.getInstance().parseSyncInBatch(uid, conSyncResp.getChatMsgList(), metaMap, entity.getNewList(), entity.getOldList());
                            entity.setMessages(msgs);
                        }
                        latch.countDown();
                        return null;
                    }
                });
            }
            latch.await(30, TimeUnit.SECONDS);
            //latch 等待完成后，检查count是否归0，也就是所有会话是否都已经被成功入库，如果不为0，说明很可能同步过程被中断，
            //此时放弃回调,直接return。防止上层切换用户登陆后消息错乱。
            if (0 != latch.getCount()) {
                Logger.ww(TAG, "count down timeout. just return,and don`t invoke callback");
                return;
            }
            Logger.d(TAG, "result content length " + rawData.length);
        } catch (InvalidProtocolBufferException e) {
            Logger.ee(TAG, "jmessage occurs an error when parse protocol buffer.");
            errorCode = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR;
            errorDesc = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC;
            e.printStackTrace();
        } catch (InterruptedException e) {
            errorCode = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR;
            errorDesc = ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC;
            e.printStackTrace();
        }
        callback.gotResult(errorCode, errorDesc, convEntities);
    }


    interface GetChatMsgCallback {
        void gotResult(int responseCode, String responseMessage, Map<String, GetChatMsgTaskMng.ConvEntity> entityMap);
    }

    private class GetChatMsgRequestEntity {
        @Expose
        private long juid;
        @Expose
        private String platform;
        @Expose
        @SerializedName("sdk_version")
        private String sdkVersion = BuildConfig.SDK_VERSION;

        @Expose
        @SerializedName("con_list")
        private Collection<GetChatMsgTaskMng.ConvEntity> convEntities = new ArrayList<GetChatMsgTaskMng.ConvEntity>();

        GetChatMsgRequestEntity(long juid, String platform, Collection<GetChatMsgTaskMng.ConvEntity> entities) {
            this.juid = juid;
            this.platform = platform;
            convEntities = entities;
        }

        @Override
        public String toString() {
            return "GetChatMsgRequestEntity{" +
                    "juid=" + juid +
                    ", platform='" + platform + '\'' +
                    ", sdkVersion='" + sdkVersion + '\'' +
                    ", convEntities=" + convEntities +
                    '}';
        }
    }

}
