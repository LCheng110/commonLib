package cn.jpush.im.android.tasks;

import android.content.ContentValues;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;
import com.google.protobuf.jpush.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.Consts;
import cn.jpush.im.android.ErrorCode;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.pushcommon.proto.Group;
import cn.jpush.im.android.storage.CRUDMethods;
import cn.jpush.im.android.storage.GroupStorage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.UserIDHelper;

/**
 * Created by hxhg on 2017/7/6.
 */

public class GetGroupInfoListTask extends AbstractTask {
    private static final String TAG = GetGroupInfoListTask.class.getSimpleName();

    private long uid;
    private Map<Long, GetGroupInfoListTaskMng.GroupEntity> entities;
    private GetGroupInfoInBatchCallback callback;

    public GetGroupInfoListTask(long uid, Map<Long, GetGroupInfoListTaskMng.GroupEntity> entities, GetGroupInfoInBatchCallback callback, boolean waitForCompletion) {
        super(null, waitForCompletion);
        this.uid = uid;
        this.callback = callback;
        this.entities = entities;
    }

    private String createUrl() {
        return JMessage.httpSyncPrefix + "/groups";
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
                GetGroupInfoInBatchRequestEntity entity = new GetGroupInfoInBatchRequestEntity(JCoreInterface.getUid(), Consts.PLATFORM_ANDROID, entities.keySet());
                Logger.d(TAG, "send get msg content, conv count = " + entities.size() + " request entities = " + entities.toString());
                response = mHttpClient.sendPost(url, JsonUtil.toJsonOnlyWithExpose(entity), authBase);
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
    protected void onSuccess(final byte[] rawData) {
        super.onSuccess(rawData);

        //此时检查一次当前登录用户和发起请求时用户是否一致，如果不一致，则放弃解析,直接return。
        if (uid != IMConfigs.getUserID()) {
            Logger.ww(TAG, "current uid not match uid in protocol. abort this action.");
            return;
        }

        CRUDMethods.execInTransactionAsync(new CRUDMethods.TransactionCallback<Void>() {
            @Override
            public Void execInTransaction() {
                try {
                    final int errorCode = ErrorCode.NO_ERROR;
                    final String errorDesc = ErrorCode.NO_ERROR_DESC;
                    Group.GroupPacket packet = Group.GroupPacket.parseFrom(rawData);
                    List<Group.GroupMeta> groupMetas = packet.getGroupListList();

                    List<Long> allUids = new ArrayList<Long>();
                    List<Long> memberIds = new ArrayList<Long>();
                    long gid;
                    long ownerId = 0;
                    for (Group.GroupMeta groupMeta : groupMetas) {//遍历每个群组
                        memberIds.clear();
                        InternalGroupInfo groupInfo = JsonUtil.fromJsonOnlyWithExpose(groupMeta.getGroupInfo().toStringUtf8(), InternalGroupInfo.class);
                        gid = groupInfo.getGroupID();
                        GroupStorage.insertOrUpdateWhenExistsSync(groupInfo, false, false);//将群信息写到数据库

                        List<Group.GroupMemberMeta> membermetas = groupMeta.getMemberListList();
                        for (Group.GroupMemberMeta meta : membermetas) {//遍历群组中每个群成员
                            if (1 == meta.getFlag()) {
                                ownerId = meta.getUid();
                            }
                            memberIds.add(meta.getUid());
                            allUids.add(meta.getUid());
                        }
                        //将群成员uid和ownerid写到到数据库
                        ContentValues values = new ContentValues();
                        values.put(GroupStorage.GROUP_OWNER_ID, ownerId);
                        values.put(GroupStorage.GROUP_MEMBERS, JsonUtil.toJson(memberIds));
                        GroupStorage.updateValuesSync(gid, values);
                        Logger.d(TAG, "update group info and members . info = " + groupInfo + " owner = " + ownerId + " members = " + memberIds);
                    }

                    //整体对于所有uid取一次userinfo，确保每个uid在本地都能拿到userinfo，防止上层展示出问题。
                    // TODO: 2017/8/1 这里如果uid数量非常多（比如有多个大群切群成员上限是20000个的情况下），会有效率隐患
                    UserIDHelper.getUserNames(allUids, new UserIDHelper.GetUsernamesCallback() {
                        @Override
                        public void gotResult(int code, String msg, List<String> usernames) {
                            callback.gotResult(errorCode, errorDesc, entities);
                        }
                    });
                } catch (InvalidProtocolBufferException e) {
                    Logger.ee(TAG, "jmessage occurs an error when parse protocol buffer.");
                    callback.gotResult(ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR,
                            ErrorCode.LOCAL_ERROR.LOCAL_MESSAGE_PARSE_ERROR_DESC, entities);
                    e.printStackTrace();
                }
                return null;
            }
        });
    }

    @Override
    protected void onError(int responseCode, String responseMsg) {
        super.onError(responseCode, responseMsg);
    }

    private class GetGroupInfoInBatchRequestEntity {
        @Expose
        private long juid;
        @Expose
        private String platform;
        @Expose
        @SerializedName("sdk_version")
        private String sdkVersion = BuildConfig.SDK_VERSION;

        @Expose
        @SerializedName("gid_list")
        private Collection<Long> gids = new ArrayList<Long>();

        GetGroupInfoInBatchRequestEntity(long juid, String platform, Collection<Long> gidList) {
            this.juid = juid;
            this.platform = platform;
            gids = gidList;
        }

    }

    public interface GetGroupInfoInBatchCallback {
        void gotResult(int responseCode, String responseMessage, Map<Long, GetGroupInfoListTaskMng.GroupEntity> entityMap);
    }
}
