package cn.jpush.im.android.utils;

import android.text.TextUtils;

import java.io.File;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.im.android.BuildConfig;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.model.Message;

public class ExpressionValidateUtil {

    private static final String TAG = ExpressionValidateUtil.class.getSimpleName();

    private static final int MIN_PASSWORD_LENGTH = 4;

    private static final int MAX_PASSWORD_LENGTH = 128;

    private static final int MAX_OTHER_NAME_LENGTH = 64;

    private static final int MAX_INPUT_LENGTH = 250;

    private static final int MAX_USER_EXTRAS = 512;

    private static final int MAX_MESSAGE_LENGTH = BuildConfig.MAX_MSG_LENGTH_IN_BYTE;

    private static final int MAX_TRANS_CMD = 7168;

    private static final String PROVIDER_QINIU = "qiniu";

    private static final String PROVIDER_UPYUN = "upyun";

    private static final String PROVIDER_FASTDFS = "fastdfs";

    private static final String MEDIA_TYPE_IMAGE = "image";

    private static Pattern pattern = null;

    private static Matcher matcher = null;

    public static boolean validUserName(String userName) {
        if (TextUtils.isEmpty(userName)) {
            return false;
        }
        userName = userName.trim();
        pattern = Pattern.compile("^[0-9a-zA-Z][a-zA-Z0-9_\\-@\\.]{3,127}$");
        matcher = pattern.matcher(userName);
        return matcher.find();
    }

    public static boolean validPassword(String password) {
        return !TextUtils.isEmpty(password)
                && password.getBytes().length >= MIN_PASSWORD_LENGTH
                && password.getBytes().length <= MAX_PASSWORD_LENGTH;
    }

    public static boolean validOtherNames(String name) {
        return null != name && validNullableName(name);
    }

    public static boolean validOthers(String text) {
        return null != text && validNullableInput(text);
    }

    public static boolean validMessageLength(Message message) {
        if (null != message) {
            String msgJson = message.getContent().toJson();
            Logger.d(TAG, "[validMessageLength] msgJson = " + msgJson);
            if (msgJson.getBytes().length > MAX_MESSAGE_LENGTH) {
                return false;
            }
        } else {
            return false;
        }
        return true;
    }

    public static boolean validCurrentUserPassword(String originPassword) {
        originPassword = StringUtils.toMD5(originPassword.trim());
        String password = IMConfigs.getUserPassword();
        return null != password && password.equals(originPassword);
    }

    public static boolean validMediaID(String mediaID) {
        return !TextUtils.isEmpty(mediaID)
                && (mediaID.startsWith(PROVIDER_QINIU) || mediaID.startsWith(PROVIDER_UPYUN) || mediaID.startsWith(PROVIDER_FASTDFS))
                && mediaID.split(File.separator).length >= 3;
    }

    public static boolean validImageMediaID(String mediaID) {
        return !TextUtils.isEmpty(mediaID)
                && String.valueOf(MEDIA_TYPE_IMAGE).equals(StringUtils.getTypeFromMediaID(mediaID))
                && mediaID.split(File.separator).length >= 3 ;
    }

    public static boolean validNullableInput(String inputString) {
        if (null != inputString && inputString.getBytes().length > MAX_INPUT_LENGTH) {
            //仅当string超长时返回false
            Logger.ee(TAG, "invalid input, input string can not more than " + MAX_INPUT_LENGTH + " bytes.");
            return false;
        }
        return true;
    }

    public static boolean validNullableName(String name) {
        if (null == name) {
            return true;
        }

        boolean invalidLength = false;
        boolean haveInvalidMark;
        if (name.getBytes().length > MAX_OTHER_NAME_LENGTH) {
            invalidLength = true;
        }
        pattern = Pattern.compile("[\\r\\n]");
        matcher = pattern.matcher(name);
        haveInvalidMark = matcher.find();
        return !invalidLength && !haveInvalidMark;
    }

    public static boolean validExtras(Map<String, String> extras) {
        if (null != extras && JsonUtil.toJson(extras).getBytes().length <= MAX_USER_EXTRAS) {
            return true;
        }
        Logger.ee(TAG, "invalid user extras, extras = " + extras);
        return false;
    }
}
