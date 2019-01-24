package cn.jpush.im.android.tasks;

import com.google.gson.jpush.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.StringUtils;

public class GetGroupIDListTask extends AbstractTask {

    private long userID;

    private GetGroupIDListCallback callback;

    public GetGroupIDListTask(long userID, GetGroupIDListCallback callback,
                              boolean waitForCompletion) {
        super(callback, waitForCompletion);
        this.userID = userID;
        this.callback = callback;
    }

    private String createGetGroupListUrl() {
        return JMessage.httpUserCenterPrefix + "/users/" + userID + "/groups";
    }

    @Override
    protected ResponseWrapper doExecute() throws Exception {
        ResponseWrapper response = super.doExecute();
        if (null != response) {
            return response;
        }

        String url = createGetGroupListUrl();
        String authBase = StringUtils
                .getBasicAuthorization(String.valueOf(IMConfigs.getUserID()),
                        IMConfigs.getToken());
        try {
            response = mHttpClient.sendGet(url, authBase);
        } catch (APIRequestException e) {
            response = e.getResponseWrapper();
        } catch (APIConnectionException e) {
            response = null;
        }
        return response;
    }

    private void getLocalInfoAndDoCallback(int errorCode, String errorMsg) {
        List<Long> idList = GroupStorage.queryIDListSync();
        if (null != idList) {
            CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, idList);
        } else {
            CommonUtils.doCompleteCallBackToUser(callback, errorCode, errorMsg);
        }
    }

    @Override
    protected void onSuccess(String resultContent) {
        super.onSuccess(resultContent);
        List<Long> list = new ArrayList<Long>();
        Set<Long> idsSet = JsonUtil
                .formatToGivenType(resultContent, new TypeToken<Set<Long>>() {
                });
        for (long id : idsSet) {
            InternalGroupInfo groupInfo = new InternalGroupInfo();
            groupInfo.setGroupID(id);
            if (!GroupStorage.isExistSync(id)) {
                GroupStorage.insertSync(groupInfo);
            }
            list.add(id);
        }

        CommonUtils.doCompleteCallBackToUser(callback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, list);
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        getLocalInfoAndDoCallback(responseCode, responseMsg);
    }
}
