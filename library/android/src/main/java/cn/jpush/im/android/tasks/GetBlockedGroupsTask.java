package cn.jpush.im.android.tasks;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.reflect.TypeToken;

import java.util.List;

import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.callback.GetGroupInfoListCallback;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;

/**
 * Created by ${chenyn} on 16/8/12.
 *
 * @desc :
 */
public class GetBlockedGroupsTask extends AbstractTask {
    private static final String TAG = "GetShieldingGroupTask";
    private static final int SHIELDING_VERSION = 0;

    public GetBlockedGroupsTask(GetGroupInfoListCallback callback, boolean waitForCompletion) {
        super(callback, waitForCompletion);
    }

    private String createUrl() {
        long userID = IMConfigs.getUserID();
        if (0 != userID) {
            return JMessage.httpUserCenterPrefix + "/users/" + userID + "/groupsShield?version=" + SHIELDING_VERSION;
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
                response = mHttpClient.sendGet(url, authBase);
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
        Logger.d(TAG, "on success .result = " + resultContent);
        ShieldingEntity entity = JsonUtil.formatToGivenType(resultContent, new TypeToken<ShieldingEntity>() {
        });

        if (null == entity) {
            Logger.ww(TAG, "failed to parse response data. entity is null");
            return;
        }

        List<InternalGroupInfo> resultGroups = null;

        GroupStorage.resetShieldingStatusInBackground();
        if (null != entity.getGroups()) {
            resultGroups = entity.getGroups();
            for (InternalGroupInfo groupInfo : entity.getGroups()) {
                groupInfo.setBlockGroupInLocal(1);
                GroupStorage.insertOrUpdateWhenExistsInBackground(groupInfo, false, true);
            }
        }
        CommonUtils.doCompleteCallBackToUser(mCallback, ErrorCode.NO_ERROR, ErrorCode.NO_ERROR_DESC, resultGroups);
    }


    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
        Logger.d(TAG, "on error . result code = " + responseCode + " result = " + responseMsg);
        CommonUtils.doCompleteCallBackToUser(mCallback, responseCode, responseMsg);
    }


    class ShieldingEntity {
        @Expose
        private List<InternalGroupInfo> groups;

        public List<InternalGroupInfo> getGroups() {
            return groups;
        }
    }
}
