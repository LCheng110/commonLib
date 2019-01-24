package cn.jpush.im.android.tasks;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;
import com.google.gson.jpush.reflect.TypeToken;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetReceiptDetailsCallback;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.UserIDHelper;

/**
 * Created by hxhg on 2017/8/25.
 */

public class GetMessageReceiptDetailTask extends AbstractTask {
    private static final String TAG = GetMessageReceiptDetailTask.class.getSimpleName();

    private List<Long> msgIdList;

    public GetMessageReceiptDetailTask(List<Long> msgIdList, GetReceiptDetailsCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.msgIdList = msgIdList;
    }

    private String createUrl() {
        long userID = IMConfigs.getUserID();
        if (0 != userID) {
            return JMessage.httpUserCenterPrefix + "/msgreceipt";
        }
        return null;
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
                response = mHttpClient.sendPost(url, JsonUtil.toJson(msgIdList), authBase);
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
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        final List<ResponseEntity> responseEntities = JsonUtil.formatToGivenType(resultContent, new TypeToken<List<ResponseEntity>>() {
        });
        Set<Long> uidSet = new HashSet<Long>();
        for (ResponseEntity entity : responseEntities) {
            uidSet.addAll(entity.receiptUidList);
            uidSet.addAll(entity.unReceiptUidList);
        }
        UserIDHelper.getUserNames(uidSet, new UserIDHelper.GetUsernamesCallback() {
            @Override
            public void gotResult(int code, String msg, List<String> usernames) {
                List<GetReceiptDetailsCallback.ReceiptDetails> detailses = null;
                if (ErrorCode.NO_ERROR == code) {
                    detailses = new ArrayList<GetReceiptDetailsCallback.ReceiptDetails>();
                    for (ResponseEntity entity : responseEntities) {
                        List<InternalUserInfo> receiptUsers = UserInfoManager.getInstance().getUserInfoList(entity.receiptUidList);
                        List<InternalUserInfo> unreceiptUsers = UserInfoManager.getInstance().getUserInfoList(entity.unReceiptUidList);
                        GetReceiptDetailsCallback.ReceiptDetails details = new GetReceiptDetailsCallback.ReceiptDetails(entity.msgid, receiptUsers, unreceiptUsers);
                        detailses.add(details);
                    }
                }
                CommonUtils.doCompleteCallBackToUser(mCallback, code, msg, detailses);
            }
        });
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        Logger.d(TAG, "on error . result code = " + responseCode + " result = " + responseMsg);
        CommonUtils.doCompleteCallBackToUser(mCallback, responseCode, responseMsg);
    }

    private class ResponseEntity {
        @Expose
        long msgid;
        @Expose
        @SerializedName("read_list")
        List<Long> receiptUidList;
        @Expose
        @SerializedName("unread_list")
        List<Long> unReceiptUidList;
    }
}
