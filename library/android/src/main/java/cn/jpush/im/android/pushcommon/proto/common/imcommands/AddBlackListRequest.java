package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import com.google.gson.jpush.annotations.Expose;

import java.util.List;
import java.util.concurrent.Callable;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.pushcommon.proto.Friend.AddBlackList;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.JsonUtil;


public class AddBlackListRequest extends ImBaseRequest {
    @Expose
    List<Long> blackList;

    public AddBlackListRequest(List<Long> blackList, long rid, long uid) {
        super(IMCommands.AddBlackList.CMD,uid,rid);

        this.blackList = blackList;
    }

    @Override
    IMProtocol toProtocolBuffer(long imUid, String appKey) {
        AddBlackList addBlackList = AddBlackList.newBuilder()
                .addAllTargetList(blackList)
                .build();
        return new IMProtocol(IMCommands.AddBlackList.CMD,
                IMCommands.AddBlackList.VERSION,
                imUid, appKey, addBlackList);
    }

    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }

    @Override
    public void onResponse(final IMProtocol imProtocol) {
        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                int respCode = imProtocol.getResponse().getCode();
                if (respCode == 0 && null != blackList) {
                    for (long userid : blackList) {
                        UserInfoManager.getInstance().updateBlacklist(userid, true);
                    }
                }
                return null;
            }
        }).continueWith(new BasicCallbackContinuation(this, imProtocol.getResponse().getCode(),
                imProtocol.getResponse().getMessage().toStringUtf8()), getExecutor());
    }

    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }

    public static AddBlackListRequest fromJson(String json) {
        return JsonUtil.fromJsonOnlyWithExpose(json, AddBlackListRequest.class);
    }
}
