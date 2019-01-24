package cn.jpush.im.android.api.model;


import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import java.io.File;
import java.util.List;
import java.util.Vector;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.api.BasicCallback;

public abstract class GroupInfo {

    private static final String TAG = "GroupInfo";

    protected int _id;

    @Expose
    @SerializedName("gid")
    protected long groupID = -1;

    @Expose
    @SerializedName("name")
    protected String groupName = "";

    @Expose
    @SerializedName("desc")
    protected String groupDescription = "";

    @Expose
    @SerializedName("level")
    protected int groupLevel;

    @Expose
    @SerializedName("flag")
    protected int groupFlag;

    @Expose
    @SerializedName("max_member_count")
    protected int maxMemberCount;

    protected String groupOwner = "";

    protected int noDisturb = -1;

    protected int isGroupBlocked = -1;

    @Expose
    @SerializedName("avatar")
    protected String avatarMediaID;

    protected Vector<UserInfo> groupMemberInfos;

    protected int get_id() {
        return _id;
    }

    /**
     * 获取群组id
     *
     * @return
     */
    public long getGroupID() {
        return groupID;
    }

    /**
     * 获取群组名称
     *
     * @return
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * 获取群组描述信息
     *
     * @return
     */
    public String getGroupDescription() {
        return groupDescription;
    }

    public int getGroupLevel() {
        return groupLevel;
    }

    public int getGroupFlag() {
        return groupFlag;
    }

    /**
     * 群组是否在免打扰名单中
     *
     * @return 1表示存在，0表示不存在
     */
    public abstract int getNoDisturb();

    /**
     * 获取群主的username
     *
     * @return
     */
    public abstract String getGroupOwner();

    /**
     * 获取群主所属appkey
     *
     * @since 1.4.0
     */
    public abstract String getOwnerAppkey();

    /**
     * 获取群成员userinfo列表。
     *
     * @return
     */
    public abstract List<UserInfo> getGroupMembers();

    /**
     * 获取单个群成员Userinfo，默认获取本应用appkey下用户信息,如果需要获取跨应用
     * 下其他用户的用户信息，请使用{@link GroupInfo#getGroupMemberInfo(String, String)}
     *
     * @param username 指定群成员的username
     * @return 指定username的用户信息，如果本地未找到则返回null
     * @deprecated deprecated in jmessage 1.3.0 use {@link GroupInfo#getGroupMemberInfo(String, String)} instead.
     */
    @Deprecated
    public abstract UserInfo getGroupMemberInfo(String username);

    /**
     * 根据appKey跨应用获取单个群成员UserInfo
     *
     * @param username 指定群成员的username
     * @param appKey   群成员所属的appKey
     * @return 指定username的用户信息，如果本地未找到则返回null
     */
    public abstract UserInfo getGroupMemberInfo(String username, String appKey);

    /**
     * 获取群成员数的最大上限
     *
     * @return 群成员数上限
     */
    public int getMaxMemberCount() {
        return maxMemberCount;
    }

    /**
     * 将此群组设置为免打扰。设置之后收到此群消息时将不会在通知栏展示通知
     *
     * @param noDisturb 1 -- 免打扰，其他 -- 非免打扰
     * @param callback  回调接口
     */
    public abstract void setNoDisturb(int noDisturb, BasicCallback callback);

    /**
     * 设置屏蔽群消息,设置后将不会再收到此群的消息
     *
     * @param blocked  1 -- 设置屏蔽; 其他(int) -- 取消屏蔽
     * @param callback 回调接口
     * @since 2.1.0
     */
    public abstract void setBlockGroupMessage(int blocked, BasicCallback callback);

    /**
     * 群组是否在被屏蔽的名单中
     *
     * @return 1 -- 存在; 0 -- 不存在
     * @since 2.1.0
     */
    public abstract int isGroupBlocked();

    /**
     * 获取群头像的mediaID
     *
     * @return 用户头像的mediaID，若未设置头像则返回null
     * @since 2.3.0
     */
    public String getAvatar() {
        return avatarMediaID;
    }

    /**
     * 从本地获取群头像缩略图文件，头像缩略图会在调用{@link JMessageClient#getGroupInfo(long, GetGroupInfoCallback)}
     * 时自动下载。当此群未设置头像，或者自动下载失败时此接口返回Null。<br/>
     *
     * @return 群组缩略头像文件对象，若未设置头像或者未下载完成则返回null
     * @since 2.3.0
     */
    public abstract File getAvatarFile();

    /**
     * 从本地获取群组头像的缩略图bitmap，如果本地存在头像缩略图文件，直接返回；若不存在，会异步从服务器拉取。
     * 下载完成后 会将头像保存至本地并返回。当群组未设置头像，或者下载失败时回调返回Null。<br/>
     * 所有的缩略头像bitmap在sdk内都会缓存，
     * 并且会有清理机制，所以上层不需要对缩略头像bitmap做缓存。
     *
     * @since 2.3.0
     */
    public abstract void getAvatarBitmap(GetAvatarBitmapCallback callback);

    /**
     * 从本地获取群组头像原图文件，当群组未设置头像，或者本地不存在头像原图文件时此接口返回Null。<br/>
     *
     * @return 群组头像原图文件对象，若未设置头像或者本地不存在头像原图文件则返回null
     * @since 2.3.0
     */
    public abstract File getBigAvatarFile();

    /**
     * 从本地获取群组头像的原图bitmap，如果本地存在头像原图文件，直接返回；若不存在，会异步从服务器拉取。
     * 下载完成后 会将头像保存至本地并返回。当群组未设置头像，或者下载失败时回调返回Null。<br/>
     * 注意：在调用{@link GroupInfo#updateAvatar(File, String, BasicCallback)} 上传头像时，若
     * 上传的头像未经压缩处理，调此接口则有可能出现OOM
     *
     * @since 2.3.0
     */
    public abstract void getBigAvatarBitmap(GetAvatarBitmapCallback callback);

    /**
     * 更新群组头像，建议用户在上传头像前先对头像先进行压缩，否则在调用{@link GroupInfo#getBigAvatarBitmap(GetAvatarBitmapCallback)}
     * 接口拿头像的原图时，有可能会抛出OOM异常。
     * <p>
     * 此接口可以指定头像文件在后台存储时的扩展名，如果填空或者不填，则后台存储文件时将没有扩展名。
     *
     * @param avatar   头像文件
     * @param format   文件扩展名，注意名称中不要包括"."
     * @param callback 回调对象
     * @since 2.3.0
     */
    public abstract void updateAvatar(File avatar, final String format, final BasicCallback callback);

    /**
     * 修改群组名称,新的群名称不允许为空
     *
     * @param groupName 群名称
     * @param callback  回调接口
     * @since 2.3.0
     */
    public abstract void updateName(String groupName, BasicCallback callback);

    /**
     * 修改群组描述,新的描述不允许为空
     *
     * @param groupDesc 群组描述
     * @param callback  回调接口
     * @since 2.3.0
     */
    public abstract void updateDescription(String groupDesc, BasicCallback callback);
}
