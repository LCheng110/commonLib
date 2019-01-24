package cn.jpush.im.android.api.callback;

import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.api.BasicCallback;

/**
 * 获取消息回执详细信息{@link ReceiptDetails}的回调接口。
 */
public abstract class GetReceiptDetailsCallback extends BasicCallback {
    protected GetReceiptDetailsCallback() {

    }

    protected GetReceiptDetailsCallback(boolean isRunInUIThread) {
        super(isRunInUIThread);
    }

    @Override
    public void gotResult(int responseCode, String responseMessage) {

    }

    public abstract void gotResult(int responseCode, String responseMessage, List<ReceiptDetails> receiptDetails);

    @Override
    public void gotResult(int responseCode, String responseMessage, Object... result) {
        List<ReceiptDetails> receiptDetails = null;
        if (null != result && result.length > 0) {
            if (null != result[0]) {
                receiptDetails = (List<ReceiptDetails>) result[0];
            }
        }
        gotResult(responseCode, responseMessage, receiptDetails);
    }


    /**
     * 消息回执详细信息
     * <p>
     * 具体包括: 消息的serviceMessageId、已发送已读回执的用户的UserInfo List、未发送已读回执的用户的UserInfo List
     *
     * @since 2.3.0
     */
    public static class ReceiptDetails {
        private long serverMsgID;
        private List<? extends UserInfo> receiptList;
        private List<? extends UserInfo> unreceiptList;

        public ReceiptDetails(long serverMsgID, List<? extends UserInfo> receiptList, List<? extends UserInfo> unreceiptList) {
            this.serverMsgID = serverMsgID;
            this.receiptList = receiptList;
            this.unreceiptList = unreceiptList;
        }

        /**
         * 获取这条消息的serverMsgID
         *
         * @return
         */
        public long getServerMsgID() {
            return serverMsgID;
        }

        /**
         * 获取这条消息已发送已读回执的用户的UserInfo List
         *
         * @return 已发送已读回执的用户的UserInfo List
         */
        public List<UserInfo> getReceiptList() {
            return (List<UserInfo>) receiptList;
        }


        /**
         * 获取这条消息尚未发送已读回执的用户的UserInfo List
         *
         * @return 尚未发送已读回执的用户的UserInfo
         */
        public List<UserInfo> getUnreceiptList() {
            return (List<UserInfo>) unreceiptList;
        }

    }
}
