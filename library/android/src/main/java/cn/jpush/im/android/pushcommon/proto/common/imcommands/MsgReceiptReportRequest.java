package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.MsgReceiptReportRequestPackager;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.pushcommon.proto.Receipt;
import cn.jpush.im.android.storage.ConversationManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;

/**
 * Created by hxhg on 2017/8/24.
 */

public class MsgReceiptReportRequest extends ImBaseRequest {

    private static final String TAG = MsgReceiptReportRequest.class.getSimpleName();

    public static final int MSG_TYPE_SINGLE = 3;//单聊消息类型，由后台定义
    public static final int MSG_TYPE_GROUP = 4;//群聊消息类型，由后台定义

    @Expose
    private long targetId;//消息会话目标id， 单聊为请求发起方uid，群聊则为gid
    @Expose
    private int msgType;//消息类型
    @Expose
    private Set<Long> msgIDList;//消息的serverMsgID list

    private MsgReceiptReportRequestPackager.RequestEntity requestEntity;


    public MsgReceiptReportRequest(long uid, MsgReceiptReportRequestPackager.RequestEntity requestEntity, long rid) {
        super(IMCommands.MsgReceiptReport.CMD, uid, rid);

        this.requestEntity = requestEntity;
        this.targetId = requestEntity.targetId;
        this.msgType = requestEntity.msgType;
        this.msgIDList = requestEntity.serverMsgIds;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        Receipt.MsgReceiptReport.Builder builder = Receipt.MsgReceiptReport.newBuilder().setConTarget(targetId)
                .setMsgType(msgType).addAllMsgidList(msgIDList);
        return new IMProtocol(IMCommands.MsgReceiptReport.CMD, IMCommands.MsgReceiptReport.VERSION,
                imUid, appKey, builder.build());
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(IMProtocol imProtocol) {
        final int responseCode = imProtocol.getResponse().getCode();
        final String responseMsg = imProtocol.getResponse().getMessage().toStringUtf8();
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                Logger.d(TAG, "send receipt request onResponse. code = " + responseCode + " msg = " + responseMsg + " request entity = " + requestEntity);
                if (0 != targetId && ErrorCode.NO_ERROR == responseCode) {
                    if (MSG_TYPE_SINGLE == msgType) {
                        UserIDHelper.getUsername(targetId, new UserIDHelper.GetUsernamesCallback() {
                            @Override
                            public void gotResult(int code, String msg, List<String> usernames) {
                                if (ErrorCode.NO_ERROR == code) {
                                    String username = usernames.get(0);
                                    String appkey = UserIDHelper.getUserAppkeyFromLocal(targetId);
                                    InternalConversation internalConv = ConversationManager.getInstance().getSingleConversation(username, appkey);
                                    if (null != internalConv) {
                                        //更新消息的haveRead状态
                                        internalConv.updateMessageHaveReadStateInBatch(msgIDList);
                                    }
                                }
                                CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                                RequestProcessor.requestsCache.remove(rid);
                            }
                        });
                    } else if (MSG_TYPE_GROUP == msgType) {
                        InternalConversation internalConv = ConversationManager.getInstance().getGroupConversation(targetId);
                        if (null != internalConv) {
                            internalConv.updateMessageHaveReadStateInBatch(msgIDList);
                        }
                        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMsg);
                        RequestProcessor.requestsCache.remove(rid);
                    }
                }
                return null;
            }
        });
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }
}
