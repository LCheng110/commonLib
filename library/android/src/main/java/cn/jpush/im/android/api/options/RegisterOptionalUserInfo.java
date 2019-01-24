package cn.jpush.im.android.api.options;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.storage.UserInfoStorage;
import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.api.BasicCallback;

/**
 * 该类用来设置注册时的用户其他信息
 * 配合{@link JMessageClient#register(String, String, RegisterOptionalUserInfo, BasicCallback)}使用
 *
 */

public class RegisterOptionalUserInfo {

    private static final String TAG = "RegisterOptionalParameters";

    private Map<String, Object> optionalParameters = new HashMap<String, Object>();

    /**
     * 设置注册时的用户nickname信息
     *
     * @param nickname
     * @return 设置成功返回true, 失败返回false
     * @since 2.3.0
     */
    public boolean setNickname(String nickname) {
        if (!ExpressionValidateUtil.validOtherNames(nickname)) {
            Logger.ee(TAG, "set nickname failed. nickname is invalid.");
            return false;
        }
        optionalParameters.put(UserInfo.Field.nickname.toString(), nickname);
        return true;
    }

    /**
     * 设置注册时的用户birthday信息,单位毫秒.
     *
     * @param timeInMillis
     * @since 2.3.0
     */
    public void setBirthday(long timeInMillis) {
        optionalParameters.put(UserInfo.Field.birthday.toString(), timeInMillis);
    }

    /**
     * 设置注册时的用户signature信息
     *
     * @param signature
     * @return 设置成功返回true, 失败返回false
     * @since 2.3.0
     */
    public boolean setSignature(String signature) {
        if (!ExpressionValidateUtil.validOthers(signature)) {
            Logger.ee(TAG, "set signature failed. signature is invalid.");
            return false;
        }
        optionalParameters.put(UserInfo.Field.signature.toString(), signature);
        return true;
    }

    /**
     * 设置注册时的用户gender信息
     *
     * @param gender
     * @return 设置成功返回true, 失败返回false
     * @since 2.3.0
     */
    public boolean setGender(UserInfo.Gender gender) {
        if (null == gender) {
            Logger.ee(TAG, "set gender failed.gender should not be null.");
            return false;
        }
        optionalParameters.put(UserInfo.Field.gender.toString(), gender.ordinal());
        return true;
    }

    /**
     * 设置注册时的用户region信息
     *
     * @param region
     * @return 设置成功返回true, 失败返回false
     * @since 2.3.0
     */
    public boolean setRegion(String region) {
        if (!ExpressionValidateUtil.validOthers(region)) {
            Logger.ee(TAG, "set region failed.region is invalid.");
            return false;
        }
        optionalParameters.put(UserInfo.Field.region.toString(), region);
        return true;
    }

    /**
     * 设置注册时的用户address信息
     *
     * @param address
     * @return 设置成功返回true, 失败返回false
     * @since 2.3.0
     */
    public boolean setAddress(String address) {
        if (!ExpressionValidateUtil.validOthers(address)) {
            Logger.ee(TAG, "set address failed.address is invalid.");
            return false;
        }
        optionalParameters.put(UserInfo.Field.address.toString(), address);
        return true;
    }

    /**
     * 设置注册时的用户头像信息
     *
     * @param avatar 头像的mediaId
     * @return 置成功返回true, 失败返回false
     * @since 2.3.0
     */
    public boolean setAvatar(String avatar) {
        if (!ExpressionValidateUtil.validImageMediaID(avatar)) {
            Logger.ee(TAG, "set avatar failed.avatar mediaID is invalid.");
            return false;
        }
        optionalParameters.put(UserInfoStorage.KEY_AVATAR, avatar);
        return true;
    }

    /**
     * 设置注册时的用户extras信息
     *
     * @param extras
     * @return
     * @since 2.3.0
     */
    public boolean setExtras(Map<String, String> extras) {
        if (!ExpressionValidateUtil.validExtras(extras)) {
            Logger.ee(TAG, "set extras failed.extras is invalid.");
            return false;
        }
        optionalParameters.put(UserInfo.Field.extras.toString(), extras);
        return true;
    }

    /**
     * 获取注册时的用户nickname信息
     *
     * @return 已设置返回设置值，否则返回null
     * @since 2.3.0
     */
    public String getNickname() {
        return (String) optionalParameters.get(UserInfo.Field.nickname.toString());
    }

    /**
     * 获取注册时的用户birthday信息
     *
     * @return 已设置返回设置值long的包装类Long，否则返回null
     * @since 2.3.0
     */
    public Long getBirthday() {
        return (Long) optionalParameters.get(UserInfo.Field.birthday.toString());
    }

    /**
     * 获取注册时的用户signature信息
     *
     * @return 已设置返回设置值，否则返回null
     * @since 2.3.0
     */
    public String getSignature() {
        return (String) optionalParameters.get(UserInfo.Field.signature.toString());
    }

    /**
     *获取注册时的用户gender信息
     *
     * @return 已设置返回设置值，否则返回null
     * @since 2.3.0
     */
    public UserInfo.Gender getGender() {
        Integer gender = (Integer) optionalParameters.get(UserInfo.Field.gender.toString());
        if (gender != null) {
            return UserInfo.Gender.get(gender);
        } else {
            return null;
        }
    }

    /**
     * 获取注册时的用户region信息
     *
     * @return 已设置返回设置值，否则返回null
     * @since 2.3.0
     */
    public String getRegion() {
        return (String) optionalParameters.get(UserInfo.Field.region.toString());
    }

    /**
     * 获取注册时的用户address信息
     *
     * @return 已设置返回设置值，否则返回null
     * @since 2.3.0
     */
    public String getAddress() {
        return (String) optionalParameters.get(UserInfo.Field.address.toString());
    }

    /**
     * 获取注册时的用户头像信息
     *
     * @return 已设置返回设置的mediaID，否则返回null
     * @since 2.3.0
     */
    public String getAvatar() {
        return (String) optionalParameters.get(UserInfoStorage.KEY_AVATAR);
    }

    /**
     * 获取注册时的用户extras信息
     *
     * @return 已设置返回设置值，否则返回null
     * @since 2.3.0
     */
    public Map<String, String> getExtras() {
        return (Map<String, String>) optionalParameters.get(UserInfo.Field.extras.toString());
    }


    /**
     * 此接口为内部调用， 开发者可忽略
     *
     * @return
     */
    public Map<String, Object> getRequestMap() {
        Map<String, Object> requestMap = new HashMap<String, Object>();
        requestMap.putAll(optionalParameters);
        Long birthday = (Long) requestMap.get(UserInfo.Field.birthday.toString());
        if (birthday != null) {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            String birthdayString = simpleDateFormat.format(new Date(birthday));
            requestMap.put(UserInfo.Field.birthday.toString(), birthdayString);
        }
        return requestMap;
    }

}
