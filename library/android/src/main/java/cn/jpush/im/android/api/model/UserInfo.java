package cn.jpush.im.android.api.model;


import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import java.io.File;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

public abstract class UserInfo {
    private static final String TAG = UserInfo.class.getSimpleName();

    public enum Gender {
        unknown, male, female;

        public static Gender get(int ordinal) {
            return Gender.values()[ordinal];
        }
    }

    public enum Field {
        nickname, birthday, signature, gender, region, address, extras, all
    }

    @Expose
    @SerializedName("uid")
    protected long userID;

    @Expose
    @SerializedName("username")
    protected String userName;

    @Expose
    protected String nickname = "";

    @Expose
    @SerializedName("avatar")
    protected String avatarMediaID;

    @Expose
    protected String birthday = "";

    @Expose
    protected String signature = "";

    @Expose
    protected String gender = "";

    @Expose
    protected String region = "";

    @Expose
    protected String address = "";

    @Expose
    protected String appkey = null;

    @Expose
    @SerializedName("memo_name")
    protected String notename = null;//默认为null,上层在获取这个字段时，需要使用lazyLoad.

    @Expose
    @SerializedName("memo_others")
    protected String noteText = null;//默认为null,上层在获取字段时，需要使用lazyLoad.

    @Expose
    @SerializedName("mtime")
    //2.2.0版本新增，用来实现用户信息自动更新。http://jira.jpushoa.com/browse/IM-1843
    protected int mTime;

    @Expose
    //2.3.0版本新增，用户信息新增一个自定义字段extras。http://jira.jpushoa.com/browse/IM-2988
    protected Map<String, String> extras = new ConcurrentHashMap<String, String>();

    protected int isFriend = -1;

    protected int star = -1;

    protected int blacklist = -1;

    protected int noDisturb = -1;

    protected Gender mGender;

    /**
     * 获取用户的userID
     *
     * @return
     */
    public long getUserID() {
        return userID;
    }

    /**
     * 获取用户的username
     *
     * @return
     */
    public String getUserName() {
        return userName;
    }

    /**
     * 获取用户的昵称
     *
     * @return
     */
    public String getNickname() {
        return nickname;
    }

    /**
     * 获取用户的备注名
     *
     * @return
     */
    public abstract String getNotename();

    /**
     * 获取用户的备注信息
     *
     * @return
     */
    public abstract String getNoteText();

    /**
     * 获取用户生日，单位毫秒
     *
     * @return
     */
    public abstract long getBirthday();

    /**
     * 获取用户签名
     *
     * @return
     */
    public String getSignature() {
        return signature;
    }

    /**
     * 获取用户性别信息
     *
     * @return
     */
    public Gender getGender() {
        return mGender;
    }

    /**
     * 获取用户地区信息
     *
     * @return
     */
    public String getRegion() {
        return region;
    }

    /**
     * 获取用户地址信息
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * 获取用户所有扩展字段
     *
     * @return 无法修改的map对象
     * @since 2.3.0
     */
    public Map<String, String> getExtras() {
        return Collections.unmodifiableMap(extras);
    }

    /**
     * 获取指定扩展字段的内容
     *
     * @param key
     * @return 若extras map中存在该键值，则返回对应key的value，否则返回null
     * @since 2.3.0
     */
    public String getExtra(String key) {
        if (extras != null) {
            return extras.get(key);
        } else {
            return null;
        }
    }

    /**
     * 获取用户头像的mediaID
     *
     * @return 用户头像的mediaID，若未设置头像则返回null
     */
    public String getAvatar() {
        return avatarMediaID;
    }

    /**
     * 从本地获取用户头像缩略图文件，头像缩略图会在调用{@link JMessageClient#getUserInfo(String, GetUserInfoCallback)}
     * 时自动下载。当用户未设置头像，或者自动下载失败时此接口返回Null。<br/>
     *
     * @return 用户缩略头像文件对象，若未设置头像或者未下载完成则返回null
     */
    public abstract File getAvatarFile();

    /**
     * 异步获取用户缩略头像文件对象，如果本地存在头像缩略图文件，直接返回；若不存在，会异步从服务器拉取。
     * 下载完成后 会将头像保存至本地并返回。当用户未设置头像，或者下载失败时回调返回Null。<br/>
     *
     * @param callback 下载头像的回调接口
     * @deprecated deprecated in sdk version 1.1.5,use {@link UserInfo#getAvatarBitmap(GetAvatarBitmapCallback)} instead
     */
    @Deprecated
    public abstract void getAvatarFileAsync(DownloadAvatarCallback callback);


    /**
     * 从本地获取用户头像的缩略头像bitmap，如果本地存在头像缩略图文件，直接返回；若不存在，会异步从服务器拉取。
     * 下载完成后 会将头像保存至本地并返回。当用户未设置头像，或者下载失败时回调返回Null。<br/>
     * 所有的缩略头像bitmap在sdk内都会缓存，
     * 并且会有清理机制，所以上层不需要对缩略头像bitmap做缓存。
     */
    public abstract void getAvatarBitmap(GetAvatarBitmapCallback callback);


    /**
     * 从本地获取用户头像原图文件，当用户未设置头像，或者本地不存在头像原图文件时此接口返回Null。<br/>
     *
     * @return 用户头像原图文件对象，若未设置头像或者本地不存在头像原图文件则返回null
     * @since 2.3.0
     */
    public abstract File getBigAvatarFile();

    /**
     * 从本地获取用户头像的原图的bitmap，如果本地存在头像原图文件，直接返回；若不存在，会异步从服务器拉取。
     * 下载完成后 会将头像保存至本地并返回。当用户未设置头像，或者下载失败时回调返回Null。<br/>
     * 注意：在调用{@link JMessageClient#updateUserAvatar(File, BasicCallback)}上传头像时，若
     * 上传的头像未经压缩处理，调此接口则有可能出现OOM
     */
    public abstract void getBigAvatarBitmap(GetAvatarBitmapCallback callback);

    /**
     * 此用户是否在黑名单当中
     *
     * @return 1表示存在，0表示不存在
     */
    public abstract int getBlacklist();

    /**
     * 用户是否在免打扰名单中
     *
     * @return 1表示存在，0表示不存在
     */
    public abstract int getNoDisturb();

    public int getStar() {
        return star;
    }

    /**
     * 用户是否是你的好友
     *
     * @return true 表示是好友,false表示其他情况。
     */
    public abstract boolean isFriend();

    /**
     * 获取用户所属appkey
     *
     * @return
     */
    public abstract String getAppKey();

    /**
     * 设置用户的所有扩展字段，原来的扩展字段内容都会移除替换成传进来的map对象内容
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的。
     *
     * @param extras
     * @since 2.3.0
     */
    public abstract void setUserExtras(Map<String, String> extras);

    /**
     * 设置用户的扩展字段 value为null时删除key对应的键值
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的
     *
     * @param key
     * @param value
     * @since 2.3.0
     */
    public abstract void setUserExtras(String key, String value);

    /**
     * 设置用户的nickname.
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用。
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的。
     *
     * @param nickname 待更新的nickname
     */
    public void setNickname(String nickname) {
        if (!ExpressionValidateUtil.validOtherNames(nickname)) {
            Logger.ee(TAG, "set nickname failed. nickname is invalid.");
            return;
        }
        this.nickname = nickname;
    }

    /**
     * 设置用户的birthday,单位毫秒.
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用。
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的。
     *
     * @param timeInMillis 待更新的birthday信息
     */
    public abstract void setBirthday(long timeInMillis);

    /**
     * 设置用户的signature.
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用。
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的。
     *
     * @param signature 待更新的signature信息
     */
    public void setSignature(String signature) {
        if (!ExpressionValidateUtil.validOthers(signature)) {
            Logger.ee(TAG, "set signature failed. signature is invalid.");
            return;
        }
        this.signature = signature;
    }

    /**
     * 设置用户的gender.
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用。
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的。
     *
     * @param gender 待更新的gender信息
     */
    public void setGender(Gender gender) {
        if (null == gender) {
            Logger.ee(TAG, "set gender failed.gender should not be null.");
            return;
        }
        this.mGender = gender;
    }

    /**
     * 设置用户的region.
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用。
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的。
     *
     * @param region 待更新的region信息
     */
    public void setRegion(String region) {
        if (!ExpressionValidateUtil.validOthers(region)) {
            Logger.ee(TAG, "set region failed.region is invalid.");
            return;
        }
        this.region = region;
    }

    /**
     * 设置用户的address.
     * 此接口仅在需要更新当前登录用户的信息时需要
     * 配合{@link JMessageClient#updateMyInfo(Field, UserInfo, BasicCallback)}使用。
     * <p>
     * 其他情况下单独调用此接口仅会更新内存中的值，是无意义的。
     *
     * @param address 待更新的gender信息
     */
    public void setAddress(String address) {
        if (!ExpressionValidateUtil.validOthers(address)) {
            Logger.ee(TAG, "set address failed.address is invalid.");
            return;
        }
        this.address = address;
    }

    /**
     * 将此用户设置为免打扰。
     *
     * @param noDisturb 1 -- 免打扰，其他 -- 非免打扰
     * @param callback  回调接口
     */
    public abstract void setNoDisturb(int noDisturb, BasicCallback callback);

    /**
     * 将用户从你的好友列表中移出
     *
     * @param callback 结果回调
     * @since 1.4.0
     */
    public abstract void removeFromFriendList(BasicCallback callback);

    /**
     * 设置用户的notename.
     *
     * @param notename notename.
     * @deprecated deprecated in 2.2.0,use {@link UserInfo#updateNoteName(String, BasicCallback)} instead.
     */
    public void setNotename(String notename) {
        if (!ExpressionValidateUtil.validNullableName(notename)) {
            Logger.ee(TAG, "set notename failed. notename is invalid.");
            return;
        }
        this.notename = notename;
    }

    /**
     * 设置用户的note text.
     *
     * @param noteText noteText.
     * @deprecated deprecated in 2.2.0,use {@link UserInfo#updateNoteText(String, BasicCallback)} instead.
     */
    public void setNoteText(String noteText) {
        if (!ExpressionValidateUtil.validNullableInput(noteText)) {
            Logger.ee(TAG, "set noteText failed. NoteText is invalid.");
            return;
        }
        this.noteText = noteText;
    }

    /**
     * 更新用户的备注名。仅当用户存在于当前用户的好友列表中时可以更新。
     *
     * @param noteName 新的备注名
     * @param callback 结果回调
     * @since 1.4.0
     */
    public abstract void updateNoteName(String noteName, BasicCallback callback);

    /**
     * 更新用户的备注信息。仅当用户存在于当前用户的好友列表中时可以更新。
     *
     * @param noteText 新的备注信息
     * @param callback 结果回调
     * @since 1.4.0
     */
    public abstract void updateNoteText(String noteText, BasicCallback callback);

    /**
     * 获取用户信息最后一次被更新的时间，单位-秒
     *
     * @return 用户信息最后一次被更新的时间
     */
    public int getmTime() {
        return mTime;
    }

    /**
     * 获取该用户的展示名.
     * 展示名返回优先级为： 备注名 > 用户昵称 > 用户名。即：
     * 有备注名则优先返回备注，没有备注如果用户有昵称则返回昵称，没有昵称则返回用户注册时的用户名。
     *
     * @return 用户的展示名
     * @since 2.2.1
     */
    public abstract String getDisplayName();
}
