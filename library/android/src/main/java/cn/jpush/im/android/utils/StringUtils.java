package cn.jpush.im.android.utils;

import android.text.TextUtils;
import android.util.Base64;

import com.upyun.jpush.api.utils.Base64Coder;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.jpush.im.android.Consts;
import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.storage.ConversationStorage;
import cn.jpush.im.android.storage.table.ConversationTable;
import cn.jpush.im.android.utils.filemng.PrivateCloudUploadManager;

public class StringUtils {
    private final static String TAG = "StringUtils";
    private final static String BASIC_PREFIX = "Basic";

    private StringUtils() {
    }

    /**
     * will trim the string
     *
     * @param s
     * @return
     */
    public static boolean isEmpty(String s) {
        if (null == s)
            return true;
        if (s.length() == 0)
            return true;
        if (s.trim().length() == 0)
            return true;
        return false;
    }

    public static boolean equals(String paramString1, String paramString2) {
        return paramString1 == null ? false : paramString2 == null ? false : paramString1.equals(paramString2);
    }

    private static String hexString = "0123456789ABCDEF";

    public static String encode(String str) {
        byte[] bytes = str.getBytes();
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (int i = 0; i < bytes.length; i++) {
            sb.append(hexString.charAt((bytes[i] & 0xf0) >> 4));
            sb.append(hexString.charAt((bytes[i] & 0x0f) >> 0));
        }
        return sb.toString();
    }

    public static String decode(String bytes) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream(
                bytes.length() / 2);
        for (int i = 0; i < bytes.length(); i += 2)
            baos.write((hexString.indexOf(bytes.charAt(i)) << 4 | hexString
                    .indexOf(bytes.charAt(i + 1))));
        return new String(baos.toByteArray());
    }

    public static void main(String[] args) {

    }

    public static String toMD5(String source) {
        if (null == source || "".equals(source))
            return null;
        try {
            MessageDigest digest = MessageDigest
                    .getInstance("MD5");
            digest.update(source.getBytes());
            return toHex(digest.digest());
        } catch (NoSuchAlgorithmException e) {
        }
        return null;
    }

    public static String toHex(byte[] buf) {
        if (buf == null)
            return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
        }
        return result.toString();
    }

    private static void appendHex(StringBuffer sb, byte b) {
        sb.append(hexString.charAt((b >> 4) & 0x0f)).append(hexString.charAt(b & 0x0f));
    }

    public static String fixedLengthString(String s, int expectedLength) {
        int l = s.length();
        if (l >= expectedLength) {
            return s;
            // return s.substring(0, expectedLength);
        }
        for (int i = 0; i < expectedLength - l; i++) {
            s = s + " ";
        }
        return s;
    }

    public static boolean isIPAddress(String ipaddr) {
        boolean flag = false;
        Pattern pattern = Pattern
                .compile("\\b((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\.((?!\\d\\d\\d)\\d+|1\\d\\d|2[0-4]\\d|25[0-5])\\b");
        Matcher m = pattern.matcher(ipaddr);
        flag = m.matches();
        return flag;
    }

    /**
     * 过滤掉特殊字符，只取字母数字下划线中文
     */
    public static String filterSpecialCharacter(String s) {
        if (isEmpty(s)) {
            return "";
        }
        String regex = "[^\\w#$@\\-\u4e00-\u9fa5]+";
        return Pattern.compile(regex).matcher(s).replaceAll("");
    }

    public static String toHex(String txt) {
        return toHex(txt.getBytes());
    }

    public static String fromHex(String hex) {
        return new String(toByte(hex));
    }

    public static byte[] toByte(String hexString) {
        int len = hexString.length() / 2;
        byte[] result = new byte[len];
        for (int i = 0; i < len; i++)
            result[i] = Integer.valueOf(hexString.substring(2 * i, 2 * i + 2), 16).byteValue();
        return result;
    }

    public static String toHexLog(byte[] buf) {
        if (buf == null) return "";
        StringBuffer result = new StringBuffer(2 * buf.length);
        for (int i = 0; i < buf.length; i++) {
            appendHex(result, buf[i]);
            result.append(' ');
        }
        return result.toString();
    }

    public static String toMD5(byte[] bytes) {
        String resultString = null;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            resultString = byteArrayToHexString(md.digest(bytes));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return resultString;
    }

    public static String createResourceID(String format) {
        String result = toMD5(String.valueOf(
                IMConfigs.getUserID() + System.currentTimeMillis() + new Random().nextInt(1000)));
        if (null != format && !TextUtils.isEmpty(format.trim())) {
            result = result + "." + format;
        }
        return result;
    }

    public static String getProviderFromMediaID(String mediaID) {
        if (TextUtils.isEmpty(mediaID)) {
            return null;
        }
        return mediaID.split(File.separator)[0];
    }

    public static String getTypeFromMediaID(String mediaID) {
        if (TextUtils.isEmpty(mediaID)) {
            return null;
        }
        return mediaID.split(File.separator)[1];
    }

    public static String getFormatFromFileName(String fileName) {
        int index = fileName.lastIndexOf(".");
        return index == -1 ? null : fileName.substring(index + 1);//返回的format不包括"."
    }

    public static String getDownloadUrlForQiniu(String mediaID, String... params) {
        String baseUrl = Consts.DOWNLOAD_HOST_QINIU + mediaID;
        if (null != params) {
            for (int i = 0; i <= params.length - 1; i++) {
                if (i == 0) {
                    baseUrl += "?" + params[i];
                } else {
                    baseUrl += File.separator + params[i];
                }
            }
        }
        return baseUrl;
    }

    public static String getDownloadUrlForUpyun(String mediaID, String bucket, String thumbSuffix) {
        String baseUrl = "http://" + bucket + Consts.DOWNLOAD_HOST_UPYUN + mediaID;
        if (null != thumbSuffix) {
            baseUrl += Consts.UPYUN_THUMB_SPERATOR + thumbSuffix;
        }
        return baseUrl;
    }

    public static String getDownloadUrlForFastDFS(String mediaID, boolean isThumb) {
        if (TextUtils.isEmpty(mediaID)) {
            return null;
        }
        String url = null;
        String[] parts = mediaID.split(File.separator);
        String groupName = parts[3];
        String remoteFileName = mediaID.substring(mediaID.indexOf(parts[4]));
        String suffixName = "";
        if (isThumb) {
            float density = BitmapUtils.mDisplayMetrics.density;

            float deltaToHDPI = Math.abs(PrivateCloudUploadManager.DENSITY_HDPI - density);
            float deltaToXHDPI = Math.abs(PrivateCloudUploadManager.DENSITY_XHDPI - density);
            float deltaToXXHDPI = Math.abs(PrivateCloudUploadManager.DENSITY_XXHDPI - density);
            float minDelta = Math.min(Math.min(deltaToHDPI, deltaToXHDPI), deltaToXXHDPI);
            Logger.d(TAG, "device density is " + density + " close to " + minDelta);
            if (minDelta == deltaToXHDPI) {
                suffixName = PrivateCloudUploadManager.THUMB_XHDPI;
            } else if (minDelta == deltaToXXHDPI) {
                suffixName = PrivateCloudUploadManager.THUMB_XXHDPI;
            } else {
                suffixName = PrivateCloudUploadManager.THUMB_HDPI;
            }
        }
        if (remoteFileName.contains(".")) {
            //文件名中包含"."，则说明上传时指定了后缀名，这时我们自定义的suffixName需要加在真正文件名之后， 后缀名之前。
            remoteFileName = remoteFileName.substring(0, remoteFileName.lastIndexOf(".")) + suffixName
                    + remoteFileName.substring(remoteFileName.lastIndexOf("."));
        } else {
            remoteFileName += suffixName;
        }

        Logger.d(TAG, "getDownloadUrlForFastDFS -- mediaId is " + mediaID + " groupName = " + groupName + " remote file name = " + remoteFileName);
        try {
            Class fastDFSUtilCls = Class.forName("cn.jiguang.im.fastdfs.FastDFSUtil");
            Object fastDFSUtil = fastDFSUtilCls.getConstructor(String.class, int.class, int.class, String.class, int.class, String.class, int.class, String.class)
                    .newInstance(JMessage.fastDfsTrackerHost, JMessage.fastDfsTrackerPort, JMessage.fastDfsTrackerHttpPort,
                            JMessage.customStorageHostForUpload, JMessage.customStoragePortForUpload,
                            JMessage.customStorageHostForDownload, JMessage.customStoragePortForDownload, JMessage.customStoragePrefixForDownload);
            Object arg = new String[]{groupName, remoteFileName};
            url = (String) fastDFSUtilCls.getDeclaredMethod("getFastDFSDownUrl", String[].class)
                    .invoke(fastDFSUtil, arg);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return url;
    }

    public static String getResourceIDFromMediaID(String mediaID) {
        if (TextUtils.isEmpty(mediaID)) {
            return null;
        }
        String[] strings = mediaID.split(File.separator);
        if (strings.length >= 1) {
            return strings[strings.length - 1];
        } else {
            return null;
        }
    }

    public static String getBasicAuthorization(String key, String value) {
        String encodeKey = key + ":" + value;
        return BASIC_PREFIX + " " + String.valueOf(Base64Coder.encode(encodeKey.getBytes()));
    }

    public static String base64(String ciphertext) {
        byte[] bytes = ciphertext.getBytes();
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    private final static String[] hexDigits = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9",
            "A", "B", "C", "D", "E", "F"};

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer resultSb = new StringBuffer();
        for (int i = 0; i < b.length; i++) {
            resultSb.append(byteToHexString(b[i]));
        }
        return resultSb.toString();
    }

    private static String byteToHexString(byte b) {
        int n = b;
        if (n < 0) {
            n = 256 + n;
        }
        int d1 = n / 16;
        int d2 = n % 16;
        return hexDigits[d1] + hexDigits[d2];
    }

    public static boolean isSSL(String url) {
        String host = url.indexOf(":") <= 0 ? null : url.substring(0, url.indexOf(":"));
        if (host != null && host.equals("https"))
            return true;
        else
            return false;
    }

    public static String createSelectionWithAnd(String... keys) {
        return createSelection("and", keys);
    }


    public static String createListSelection(String columnName, Collection<Long> ids) {
        if (null == ids || 0 >= ids.size()) {
            return null;
        }
        StringBuilder whereClause = new StringBuilder();
        whereClause.append(columnName).append(" IN (");
        Iterator<Long> iterator = ids.iterator();
        while (iterator.hasNext()) {
            whereClause.append(iterator.next());
            if (iterator.hasNext()) {
                whereClause.append(",");
            } else {
                whereClause.append(")");
            }
        }
        return whereClause.toString();
    }

    private static String createSelection(String type, String... keys) {
        if (null == keys) {
            return null;
        }
        StringBuilder selection = new StringBuilder();
        int size = keys.length;
        for (int i = 0; i < size; i++) {
            if (i != size - 1) {
                selection.append(keys[i]).append("=? ").append(type).append(" ");
            } else {
                selection.append(keys[i]).append("=?");
            }
        }
        return selection.toString();
    }

    public static boolean isTextEmpty(CharSequence str) {
        if (str == null || str.length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    public static void createSQLStringFromIterator(StringBuffer sqlString, Iterator<InternalConversation> iterator) {
        if (null == sqlString || null == iterator || !iterator.hasNext()) {
            Logger.d(TAG, "invalid param ,return from create sql string.");
            return;
        }
        //第一个元素，添加上select前缀
        sqlString.append("select * from " + ConversationTable.CONVERSATION_TABLE_NAME + " where ");

        while (iterator.hasNext()) {
            InternalConversation conversation = iterator.next();
            ConversationType type = conversation.getType();
            String targetId = conversation.getTargetId();
            String targetAppkey = conversation.getTargetAppKey();
            //加上条件子句
            sqlString.append("not (" + ConversationStorage.TYPE + "='" + type + "' and " + ConversationStorage.TARGET_ID + "='" +
                    targetId);

            //如果是单聊，再加上appkey的查询条件
            if (type == ConversationType.single) {
                sqlString.append("' and " + ConversationStorage.TARGET_APPKEY + "='" + targetAppkey + "')");
            } else {
                sqlString.append("')");
            }

            //如果不是最后一个元素，加上and连接符
            if (iterator.hasNext()) {
                sqlString.append(" and ");
            }
        }
        Logger.d(TAG, "created sql string is " + sqlString);
    }

    public static long getNewOwnerFromDescription(String description) {
        if (!TextUtils.isEmpty(description)) {
            NewOwnerDescription newOwnerDescription = JsonUtil.fromJson(description, NewOwnerDescription.class);
            Logger.d(TAG, "[getNewOwnerFromDescription] - desc = " + description + " new owner = " + newOwnerDescription.new_owner);
            return newOwnerDescription.new_owner == null ? 0L : newOwnerDescription.new_owner.longValue();
        }
        return 0L;
    }

    private class NewOwnerDescription {
        Number new_owner;
    }
}
