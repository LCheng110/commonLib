package cn.jpush.im.android.pushcommon.proto.common.imcommands;

import cn.jpush.im.android.ErrorCode;

/**
 * Created by xiongtc on 2016/10/10.
 */
public abstract class GroupBlockRequest extends ImBaseRequest {
    private static final String TAG = "GroupBlockRequest";

    public static final int BLOCK_GROUP_ALREADY_SET = 840003;
    public static final int BLOCK_GROUP_NEVER_SET = 841001;

    public GroupBlockRequest(int cmd, long uid, long rid) {
        super(cmd, uid, rid);
    }


    @Override
    public void onResponseTimeout() {
        basicCallbackToUser(ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT, ErrorCode.TCP_ERROR.TCP_RESPONSE_TIMEOUT_DESC);
    }


    @Override
    public void onErrorResponse(int responseCode, String responseMsg) {
        basicCallbackToUser(responseCode, responseMsg);
    }

}
