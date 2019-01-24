package cn.jpush.im.android.pushcommon.helper;

import android.content.Context;
import android.os.Bundle;

import java.nio.ByteBuffer;

import cn.jiguang.ald.api.JResponse;
import cn.jpush.im.android.pushcommon.proto.JMessageCommands;
import cn.jpush.im.android.pushcommon.proto.common.commands.IMResponse;
import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.utils.Logger;


public class IMResponseHelper {
    private static final String TAG = "IMResponseHelper";

    // push
    public static final String EXTRA_PUSH_TYPE = "push_type";
    public static final String EXTRA_PUSH_TYPE_LOGIN = "push_login";
    public static final String EXTRA_PUSH_TYPE_LOGOUT = "push_logout";


    // response
    public static final String ACTION_IM_RESPONSE = "cn.jpush.im.android.action.IM_RESPONSE";

    public static final String EXTRA_IM_RESPONSE = "im_response";   // Value: buf[]
    public static final String DATA_MSG_BODY = "data_body";   // Value: buf[]
    public static final String DATA_MSG_HEAD = "data_head";   // Value: buf[]
    public static final String DATA_MSG_CMD = "data_cmd";   // Value: int
    public static final String EXTRA_IM_TIMEOUT = "im_timeout"; // Value: true if timeout

    public static final String EXTRA_PUSH2IM_DATA = "push_to_im_data";


    // msg what
    public static final int MSG_ON_RESPONSE = 7500;
    public static final int MSG_EVENT_NOTIFICATION_BACK = 7501;
    public static final int MSG_CHAT_MSG_SYNC_BACK = 7502;
    public static final int MSG_PUSH_LOGIN = 7601;
    public static final int MSG_PUSH_LOGOUT = 7602;


    public static void handlePushLogin(Context context, Bundle bundle) {
        Logger.v(TAG, "action - handlePushLogin");
        IMServiceHelper.handlePushLogin(context, bundle);
    }

    public static void handlePushLogout(Context context, Bundle bundle) {
        Logger.v(TAG, "action - handlePushLogout");
        IMServiceHelper.handlePushLogout(context, bundle);
    }

    public static void handleImResponsePreExecute(Context context, JResponse response) {
        Logger.v(TAG, "action - handleImResponsePreExecute");
        if (null == context || null == response) {
            Logger.e(TAG, "unexpected! im response is " + response + " context is " + context);
            return;
        }
        IMServiceHelper.handleImResponsePreExecute(context, response);

    }

    /**
     * 仅当response是eventNotification类型时需要调用此接口，给服务器发送一个响应。证明sdk已收到并成功处理event.
     * <p>
     * 注意此接口会改变imProtocol的内容，sdk需要确保调用此接口之后，不在需要处理imProtocol中的数据，
     *
     * @param context
     * @param rid
     * @param imProtocol
     */
    public static void handleImResponseSendEventBack(Context context, long rid, IMProtocol imProtocol) {
        Logger.v(TAG, "action - handleImResponseSendEventBack");
        if (null == context || null == imProtocol) {
            Logger.e(TAG, "unexpected! im imProtocol is " + imProtocol + " context is " + context);
            return;
        }
        IMServiceHelper.sendEventBack(context, rid, imProtocol);
    }

    public static JResponse parseResponseInbound(int cmd, byte[] headBytes, byte[] bodyBytes) {
        switch (cmd) {
            case JMessageCommands.IM.CMD:
                return new IMResponse(headBytes, ByteBuffer.wrap(bodyBytes));

            default:
                Logger.dd(TAG, "Unknown command for parsing inbound.");
                return null;
        }
    }


}
