package cn.jpush.im.android.api.event;

import java.util.List;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;

/**
 * 命令透传事件。当收到对方发送透传消息过来时，sdk会抛出此事件通知上层。
 * <p>
 * 详见官方文档<a href="https://docs.jiguang.cn/jmessage/client/im_sdk_android/#_33">事件处理<a/>
 * 一节
 */
public class CommandNotificationEvent {
    private long senderUID;
    private long targetID;
    private Type type;
    private String cmd;

    public enum Type {
        single(3), group(4);

        private int value = 0;

        Type(int value) {
            this.value = value;
        }

        /**
         * 获取Type所对应的整型值
         *
         * @return 返回int整型值
         */
        public int getValue() {
            return value;
        }

        /**
         * 根据传入的整型值返回对应的Type
         *
         * @param value
         * @return 如果不存在对应的Type返回null
         */
        public static Type get(int value) {
            Type type = null;
            if (value == Type.single.getValue()) {
                type = Type.single;
            } else if (value == Type.group.getValue()) {
                type = Type.group;
            }
            return type;
        }
    }

    public CommandNotificationEvent(long senderUID, long targetID, Type type, String cmd) {
        this.senderUID = senderUID;
        this.targetID = targetID;
        this.type = type;
        this.cmd = cmd;
    }

    /**
     * 获取命令透传事件发送方的用户信息
     *
     * @param callback
     */
    public void getSenderUserInfo(final GetUserInfoCallback callback) {
        UserIDHelper.getUserInfo(senderUID, new GetUserInfoListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
                if (ErrorCode.NO_ERROR == responseCode) {
                    CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfoList.get(0));
                } else {
                    CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
                }
            }
        });
    }

    /**
     * 获取命令透传事件的targetInfo
     *
     * @param callback
     */
    public void getTargetInfo(final GetTargetInfoCallback callback) {
        if (type == Type.single) {
            UserIDHelper.getUserInfo(targetID, new GetUserInfoListCallback() {
                @Override
                public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfoList) {
                    if (ErrorCode.NO_ERROR == responseCode) {
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, userInfoList.get(0), type);
                    } else {
                        CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
                    }
                }
            });
        } else {
            GroupInfo groupInfo = GroupStorage.queryInfoSync(targetID);
            if (groupInfo == null) {
                JMessageClient.getGroupInfo(targetID, new GetGroupInfoCallback() {
                    @Override
                    public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                        if (ErrorCode.NO_ERROR == responseCode) {
                            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC,
                                    groupInfo, Type.group.ordinal());
                        } else {
                            CommonUtils.doCompleteCallBackToUser(callback, responseCode, responseMessage);
                        }
                    }
                });
            } else {
                CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC,
                        groupInfo, type);
            }
        }
    }

    /**
     * 获取命令透传事件的target type
     *
     * @return 返回enum Type{single, group}
     */
    public Type getType() {
        return type;
    }

    /**
     * 获取命令透传事件的msg
     *
     * @return
     */
    public String getMsg() {
        return cmd;
    }

    public static abstract class GetTargetInfoCallback extends BasicCallback {
        private static final String TAG = "GetTargetInfoCallback";

        protected GetTargetInfoCallback() {
        }

        protected GetTargetInfoCallback(boolean isRunInUIThread) {
            super(isRunInUIThread);
        }

        public abstract void gotResult(int responseCode, String responseMessage, Object targetInfo, Type type);

        @Override
        public void gotResult(int i, String s) {
            Logger.ee(TAG, "Should not reach here! ");
        }

        @Override
        public void gotResult(int responseCode, String responseMessage, Object... result) {
            Object targetInfo = null;
            Type type = null;
            if (null != result && result.length > 1 && null != result[0] && null != result[1]) {
                targetInfo = result[0];
                type = (Type) result[1];
            }
            gotResult(responseCode, responseMessage, targetInfo, type);
        }
    }
}
