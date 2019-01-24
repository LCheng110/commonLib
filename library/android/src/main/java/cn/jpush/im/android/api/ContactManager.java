package cn.jpush.im.android.api;

import android.text.TextUtils;

import java.util.List;
import java.util.concurrent.Callable;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.bolts.Task;
import cn.jpush.im.android.helpers.RequestProcessor;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.tasks.GetFriendListTask;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.UserIDHelper;
import cn.jpush.im.api.BasicCallback;


/**
 * 联系人管理接口入口类。提供联系人管理的大部分接口<br/>
 * <p/>
 * 需要注意的是，JMessage sdk本身是无好友模式的，也就是说JMessage中任意两个用户不需要建立好友关系
 * 也可以聊天。
 * JMessage仅仅提供好友关系和备注名的托管，任何基于好友关系之上的业务扩展，比如“仅仅好友之间才允许的聊天”等逻辑
 * 需要开发者的应用层自己去做。
 *
 * @since 1.4.0
 */
public class ContactManager {
    private static final String TAG = "ContactManager";

    //请求发送方
    private static final int FROM_TYPE_REQ = 1;
    //请求响应方
    private static final int FROM_TYPE_RESP = 2;

    /**
     * 获取好友列表，异步返回结果
     *
     * @param callback 结果回调
     */
    public static void getFriendList(final GetUserInfoListCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("getFriendList", callback)) {
            return;
        }

        Task.callInBackground(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                if (!IMConfigs.getNetworkConnected()) {
                    List<UserInfo> friendList = UserInfoManager.getInstance().getFriendList();
                    if (null == friendList) {
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED, ErrorCode.LOCAL_ERROR.LOCAL_NETWORK_DISCONNECTED_DESC);
                    } else {
                        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC);
                    }
                    return null;
                }

                new GetFriendListTask(callback, false).execute();
                return null;
            }
        });
    }

    /**
     * 发送添加好友请求。在对方未做回应的前提下，允许重复发送添加好友的请求。
     *
     * @param targetUsername 被邀请方用户名
     * @param appKey         被邀请方用户的appKey,如果为空则默认从本应用appKey下查找用户。
     * @param reason         申请理由
     * @param callback       结果回调
     */
    public static void sendInvitationRequest(final String targetUsername, String appKey, final String reason, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("sendInvitationRequest", callback)) {
            return;
        }

        if (!ExpressionValidateUtil.validUserName(targetUsername)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME_DESC);
            return;
        }

        if (!ExpressionValidateUtil.validNullableInput(reason)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }

        sendContactRequest(targetUsername, appKey, FROM_TYPE_REQ, reason, callback);
    }

    /**
     * 接受对方的好友请求，操作成功后，对方会出现在自己的好友列表中，双方建立起好友关系。
     *
     * @param targetUsername 邀请方用户名
     * @param appKey         邀请方用户的appKey,如果为空则默认从本应用appKey下查找用户。
     * @param callback       结果回调
     */
    public static void acceptInvitation(final String targetUsername, String appKey, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("acceptInvitation", callback)) {
            return;
        }

        if (!ExpressionValidateUtil.validUserName(targetUsername)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME_DESC);
            return;
        }

        //reason empty means accept invitation.
        sendContactRequest(targetUsername, appKey, FROM_TYPE_RESP, "", callback);
    }

    /**
     * 拒绝对方的好友请求
     *
     * @param targetUsername 邀请方用户名
     * @param appKey         邀请方用户的appKey,如果为空则默认从本应用appKey下查找用户。
     * @param reason         拒绝理由
     * @param callback       结果回调
     */
    public static void declineInvitation(final String targetUsername, String appKey, String reason, final BasicCallback callback) {
        if (!CommonUtils.doInitialCheckWithoutNetworkCheck("declineInvitation", callback)) {
            return;
        }

        if (!ExpressionValidateUtil.validUserName(targetUsername)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_USERNAME_DESC);
            return;
        }

        if (!ExpressionValidateUtil.validNullableInput(reason)) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT,
                    ErrorCode.LOCAL_ERROR.LOCAL_INVALID_INPUT_DESC);
            return;
        }

        if (null == reason || TextUtils.isEmpty(reason.trim())) {
            reason = "NO";//set default refuse reason.
        }
        sendContactRequest(targetUsername, appKey, FROM_TYPE_RESP, reason, callback);
    }

    private static void sendContactRequest(final String targetUsername, final String appKey, final int fromType, final String reason, final BasicCallback callback) {

        Task.callInBackground(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                String finalAppkey;
                if (TextUtils.isEmpty(appKey)) {
                    finalAppkey = JCoreInterface.getAppKey();
                } else {
                    finalAppkey = appKey;
                }
                UserIDHelper.getUserID(targetUsername, finalAppkey, new UserIDHelper.GetUseridsCallback() {
                    @Override
                    public void gotResult(int code, String msg, List<Long> userids) {
                        if (userids == null || userids.isEmpty()) {
                            CommonUtils.doCompleteCallBackToUser(callback, code, msg);
                        } else {
                            RequestProcessor.imAddContact(JMessage.mContext, userids.get(0), fromType, reason, CommonUtils.getSeqID(), callback);
                        }
                    }
                });
                return null;
            }
        });
    }


}
