package cn.citytag.base.utils;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.TimeZone;

/**
 * Created by yangfeng01 on 2017/10/16.
 */

public class FormatUtils {

    public static String getAsteriskPhone(String phone) {
        String result = null;
        if (TextUtils.isEmpty(phone)) {
            return result;
        }
        result = phone.replaceAll("(?<=\\d{3})\\d(?=\\d{4})", "*");
        return result;
    }

    public static String getEncryptName(String name) {
        if (TextUtils.isEmpty(name)) {
            return "";
        }
        String result = name;
        try {
            if (name.length() == 2) {
                result = name.substring(0, 1) + "*";
                //                result = name.replace(name.charAt(name.length() - 1) + "", "*");
            } else if (name.length() > 2) {
                int len = name.length() - 2;
                StringBuilder builder = new StringBuilder("$1");
                for (int i = 0; i < len; i++) {
                    builder.append("*");
                }
                builder.append("$2");
                result = name.replaceAll("^(.).*(.)$", builder.toString());
                L.d("yf", "getEncryptName == " + builder.toString());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return name;
        }
        return result;
    }

    public static String toDateStr(Date date) {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        String dateStr = format.format(date);
        return dateStr;
    }

    /**
     * 将秒转换为时分秒的格式输出
     *
     * @param sec 秒
     * @return
     */
    public static String secToHMS(int sec) {
        int lastSec = sec % 60;
        int min = sec / 60;
        int lastMin = min % 60;
        int hour = min / 60;
        return String.format("%02d:%02d:%02d", hour, lastMin, lastSec);
    }

    /**
     * 将毫秒秒转换为时分秒的格式输出
     *
     * @param sec 秒
     * @return
     */
    public static String HaosecToHMS(long sec) {
        SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");//这里想要只保留分秒可以写成"mm:ss"
        formatter.setTimeZone(TimeZone.getTimeZone("GMT+00:00"));
        String hms = formatter.format(sec);
        return hms;
    }

    //点赞、评论数大于99 显示99+
    public static String toFormatNum(int num) {
        if (num > 999) {
            return "999+";
        } else if (num == 0) {
            return "";
        }
        return num + "";
    }

    //点赞、评论数大于99 显示99+
    public static String toFormatNum99(int num) {
        if (num > 99) {
            return "99+";
        } else if (num <= 0) {
            return "0";
        }
        return String.valueOf(num);
    }

    /**
     * 1.0显示1;1.5显示1.5
     *
     * @param d
     * @return
     */
    public static String formatShort(double d) {
        if (Math.round(d) - d == 0) {
            return (String.valueOf((int) d));
        }
        return String.valueOf(d);
    }

    /**
     * 截取到小数点后两位，用于计算可用余额
     */
    public static String getPoint2(double x) {
        String result = x + "";
        try {
            BigDecimal bigDecimal = new BigDecimal(result);
            result = bigDecimal.toString();
            if (result.contains(".")) {
                String temp = result.substring(result.indexOf(".") + 1, result.length());
                if (temp.length() > 2) {
                    result = result.substring(0, result.indexOf(".") + 3);
                }
                if (temp.length() == 1) {
                    result = result + "0";
                }
            }
        } catch (Exception e) {
            // TODO: handle exception
        }
        return result;
    }

    /**
     * 将String类型的数字转换成小数点两位
     * 1.若该string本身小数点后大于两位，则截取两位
     * 2.若该string本身小数只有一位，补一位0
     * <p>
     * 问题：1. 2./BigDecimal当数据特别大时会显示科学计数法
     */
    public static String getPoint2(String d) {
        BigDecimal bigDecimal = new BigDecimal(d);
        String result = bigDecimal.toString();
        try {
            if (result.contains(".")) {
                String temp = result.substring(result.indexOf(".") + 1, result.length());
                if (temp.length() > 2) {
                    result = result.substring(0, result.indexOf(".") + 3);
                }
                if (temp.length() == 1) {
                    result = result + "0";
                }
            }
        } catch (Exception e) {
        }
        return result;
    }


    /**
     * 数值 保留1位小数
     */
    public static String getWanPoint(double d) {
        if (Math.abs(d) < 10000) {

            return formatShort(d);

        } else if (Math.abs(d) < 100000000) {

            return getDecimaleFormat(d).format(d / 10000.0) + "w";

        } else {

            return getDecimaleFormat(d).format(d / 100000000.0) + "e";

        }
    }

    /**
     * 将每三个数字加上逗号处理（通常使用金额方面的编辑）
     *
     * @param str 需要处理的字符串
     * @return 处理完之后的字符串
     */
    public static String addComma(String str) {
        DecimalFormat decimalFormat = new DecimalFormat(",###");
        return decimalFormat.format(Double.parseDouble(str));
    }


    /**
     * 保留两个小数点
     *
     * @param d
     * @return
     */
    public static String getPoint2New(double d) {

        DecimalFormat df = new DecimalFormat("0.00");
        df.setRoundingMode(RoundingMode.FLOOR);
        String number;
        number = df.format(d);
        return number;
    }


    /**
     * 直播数值规则
     *
     * @param d
     * @return
     */
    public static String getLivePoint(double d) {
        DecimalFormat df = new DecimalFormat("#.0");
        df.setRoundingMode(RoundingMode.FLOOR);
        String number;
        if (d < 10000) {
            number = formatShort(d);
        } else if (d < 1000000) {
            df = new DecimalFormat("#.00");
            number = df.format(d / 10000.00);
        } else if (d < 10000000) {
            number = df.format(d / 10000.0);
        } else if (d < 100000000) {
            df = new DecimalFormat("#");
            number = df.format(d / 10000.0);
        } else if (d < 100000000000.0) {
            df = new DecimalFormat("#.00");
            number = df.format(d / 100000000.0);
        } else if (d < 1000000000000.0) {
            number = df.format(d / 100000000.0);
        } else {
            df = new DecimalFormat("#");
            number = df.format(d / 100000000.0);
        }
        StringBuilder stringBuilder = new StringBuilder("");
        if (number.endsWith(".0")) {
            stringBuilder.append(number.replace(".0", ""));
        } else if (number.endsWith(".00")) {
            stringBuilder.append(number.replace(".00", ""));
        } else {
            stringBuilder.append(number);
        }
        if (10000 <= d && d < 100000000) {
            stringBuilder.append("w");
        } else if (d >= 100000000) {
            stringBuilder.append("E");
        }
        return stringBuilder.toString();
    }

    /**
     * 数值 保留1位小数
     */
    public static String getWanPoint(String str) {
        if (str == null || TextUtils.isEmpty(str)) {
            return str;
        }
        double d = Double.parseDouble(str);
        if (Math.abs(d) < 10000) {

            return str;

        } else if (Math.abs(d) < 100000000) {

            return getDecimaleFormat(d).format(d / 10000.0) + "w";

        } else {

            return getDecimaleFormat(d).format(d / 100000000.0) + "e";

        }
    }

    /**
     * 数值 保留1位小数
     */
    public static String getWanPoint(int d) {
        if (Math.abs(d) < 10000) {

            return d + "";

        } else if (Math.abs(d) < 100000000) {

            return getDecimaleFormat(d).format(d / 10000.0) + "w";

        } else {

            return getDecimaleFormat(d).format(d / 100000000.0) + "e";

        }
    }

    public static DecimalFormat getDecimaleFormat(double d) {
        DecimalFormat df = new DecimalFormat("#.0");
        if (d >= 0) {
            df.setRoundingMode(RoundingMode.FLOOR);
            return df;
        } else {
            df.setRoundingMode(RoundingMode.CEILING);
            return df;
        }
    }

    public static DecimalFormat getDecimaleFormat(int d) {
        DecimalFormat df = new DecimalFormat("#.0");
        if (d >= 0) {
            df.setRoundingMode(RoundingMode.FLOOR);
            return df;
        } else {
            df.setRoundingMode(RoundingMode.CEILING);
            return df;
        }
    }

    /**
     * format 时间
     *
     * @param time
     * @return
     */
    public static String getFormatMinTime(long time) {
        long left = time / (60 * 1000);
        long right = time % (60 * 1000) / 1000;

        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(String.format("%02d", left));
        stringBuilder.append(":");
        stringBuilder.append(String.format("%02d", right));
        return stringBuilder.toString();
    }

    /**
     * 补齐两个0，不管整数与否
     *
     * @return
     */
    public static String addPoint2(String x) {
        if (!x.contains(".")) {
            return x = x + ".00";
        }
        return getPoint2(x);
    }

    /**
     * 加密手机号或邮箱，中间位用*代替
     *
     * @param account
     * @return
     */
    public static String encrypt(String account) {
        if (StringUtils.isEmpty(account)) {
            return account;
        }
        String encryptStr = account;
        try {
            if (account.contains("@")) {
                encryptStr = account.replaceAll("(\\w?)(\\w+)(\\w)(@\\w+\\.[a-z]+(\\.[a-z]+)?)", "$1****$3$4");
            } else {
                encryptStr = account.replaceAll("(\\d{3})\\d{4}(\\d{4})", "$1****$2");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return encryptStr;
    }

    public static String getPresentCountFormat(String str) {
        DecimalFormat df = new DecimalFormat("###,###");
        return df.format(Double.parseDouble(str));
    }


    //数字转字母 1-26 ： A-Z
    public static String numberToLetter(int num) {
        if (num <= 0) {
            return null;
        }
        String letter = "";
        num--;
        do {
            if (letter.length() > 0) {
                num--;
            }
            letter = ((char) (num % 26 + (int) 'A')) + letter;
            num = (int) ((num - num % 26) / 26);
        } while (num > 0);

        return letter;
    }

    public static int getLivePKHeight(int width) {
        return (int) (width / 9d * 16d / 2d) - UIUtils.dip2px(8);
    }

    /**
     * 打印json字符串格式化打印
     */
    private static final String LINE_SEPARATOR = System.getProperty("line.separator");

    private static void printLine(String tag, boolean isTop) {
        if (isTop) {
            Log.d(tag, "╔═══════════════════════════════════════════════════════════════════════════════════════");
        } else {
            Log.d(tag, "╚═══════════════════════════════════════════════════════════════════════════════════════");
        }
    }

    public static void printJson(String tag, String msg, String headString) {
        String message;
        try {
            if (msg.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(msg);
                message = jsonObject.toString(4);//最重要的方法，就一行，返回格式化的json字符串，其中的数字4是缩进字符数
            } else if (msg.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(msg);
                message = jsonArray.toString(4);
            } else {
                message = msg;
            }
        } catch (JSONException e) {
            message = msg;
        }

        printLine(tag, true);
        message = headString + LINE_SEPARATOR + message;
        String[] lines = message.split(LINE_SEPARATOR);
        for (String line : lines) {
            Log.d(tag, "║ " + line);
        }
        printLine(tag, false);
    }

    //减少重复数据
    public static ArrayList getSingleArrayList(ArrayList list) {
        ArrayList newList = new ArrayList();
        Iterator it = list.iterator();
        while (it.hasNext()) {
            Object obj = it.next();
            if (!newList.contains(obj)) {
                newList.add(obj);
            }
        }
        return newList;
    }

}
