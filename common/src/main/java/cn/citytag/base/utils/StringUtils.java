package cn.citytag.base.utils;

import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.AbsoluteSizeSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.widget.facelib.util.FaceConversionUtil;

/**
 * Created by yangfeng01 on 2017/11/9.
 */
public class StringUtils {
    /**
     * 判断给定字符串是否空白串。 空白串是指由空格、制表符、回车符、换行符组成的字符串 若输入字符串为null或空字符串，返回true
     *
     * @param input
     * @return boolean
     */
    public static boolean isEmpty(String input) {
        if (input == null || "".equals(input)) {
            return true;
        }

        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            if (c != ' ' && c != '\t' && c != '\r' && c != '\n') {
                return false;
            }
        }
        return true;
    }

    public static boolean isNotEmpty(String input) {
        return !isEmpty(input);
    }

    /**
     * 判断一个对象是否为空；
     */
    public final static boolean isEmpty(Object o) {
        return (o == null);
    }

    public final static boolean isEmpty(String[] array) {
        return array == null || array.length == 0;
    }

    public final static boolean isEmpty(int[] array) {
        return array == null || array.length == 0;
    }

    public final static boolean isEmpty(StringBuffer sb) {
        return sb == null || sb.length() == 0;
    }

    public final static boolean isEmpty(List list) {
        return list == null || list.size() == 0;
    }

    public final static boolean isEmpty(Set set) {
        return set == null || set.size() == 0;
    }

    public final static boolean isEmpty(Map map) {
        return map == null || map.size() == 0;
    }

    /**
     * 检测两个字符串是否相同；
     */
    public final static boolean isSame(String value1, String value2) {
        if (isEmpty(value1) && isEmpty(value2)) {
            return true;
        } else if (!isEmpty(value1) && !isEmpty(value2)) {
            return (value1.trim().equalsIgnoreCase(value2.trim()));
        } else {
            return false;
        }
    }

    /**
     * 字符串转整数
     *
     * @param str
     * @param defValue
     * @return
     */
    public static int toInt(String str, int defValue) {
        try {
            return Integer.parseInt(str);
        } catch (Exception e) {
        }
        return defValue;
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static int toInt(Object obj) {
        if (obj == null)
            return 0;
        return toInt(obj.toString(), 0);
    }

    /**
     * 检测变量的值是否为一个整型数据；
     */
    public final static boolean isInt(String value) {
        if (isEmpty(value))
            return false;

        try {
            Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * 判断变量的值是否为double类型
     */
    public final static boolean isDouble(String value) {
        if (isEmpty(value))
            return false;
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException e) {
            return false;
        }
        return true;
    }

    /**
     * 解析一个字符串为整数；
     */
    public final static int parseInt(String value) {
        if (isInt(value))
            return Integer.parseInt(value);
        return 0;
    }

    public final static int parseInt(String value, int defaultValue) {
        if (isInt(value))
            return Integer.parseInt(value);
        return defaultValue;
    }


    public final static boolean isFloat(String value) {
        if (isEmpty(value))
            return false;

        try {
            Float.parseFloat(value);
        } catch (NumberFormatException e) {
            return false;
        }

        return true;
    }

    /**
     * 解析为float
     *
     * @param value
     * @return
     */
    public final static float parseFloat(String value) {
        if (isFloat(value))
            return Float.parseFloat(value);
        return 0f;
    }

    /**
     * 解析一个字符串为double
     */
    public final static double parseDouble(String value) {
        if (isDouble(value))
            return Double.parseDouble(value);
        return 0;
    }

    public final static double parseDouble(String value, double defaultValue) {
        if (isDouble(value))
            return Double.parseDouble(value);
        return defaultValue;
    }

    /**
     * 解析 boolean 值；
     */
    public final static boolean parseBoolean(String value) {
        return parseBoolean(value, false);
    }

    public final static boolean parseBoolean(String value, boolean defaultValue) {
        if (isEmpty(value))
            return defaultValue;

        return StringUtils.isSame(value, "true");
    }

    /**
     * num为实数，n为要保留的小数位数。
     *
     * @param num
     * @param n
     * @return
     */
    public static double round(double num, int n) {
        return Math.round(num * Math.pow(10, n)) / Math.pow(10, n);
    }

    public static double round(int num, int n) {
        return Math.round(1.0 * num * Math.pow(10, n)) / Math.pow(10, n);
    }

    /**
     * 对象转整数
     *
     * @param obj
     * @return 转换异常返回 0
     */
    public static long toLong(String obj) {
        try {
            return Long.parseLong(obj);
        } catch (Exception e) {
        }
        return 0;
    }

    /**
     * 字符串转布尔值
     *
     * @param b
     * @return 转换异常返回 false
     */
    public static boolean toBool(String b) {
        try {
            return Boolean.parseBoolean(b);
        } catch (Exception e) {
        }
        return false;
    }

    /**
     * 格式化字符串显示；
     */
    public final static String format(String value) {
        return format(value, "");
    }

    public final static String format(String value, String defaultValue) {
        if (isEmpty(value))
            return defaultValue;
        else
            return value.trim();
    }

    /**
     * 判断指定数据是否存在于指定的数组中；
     */
    public final static boolean isContain(String[] array, String value) {
        if (isEmpty(array) || isEmpty(value))
            return false;

        int size = size(array);
        for (int i = 0; i < size; i++) {
            if (isSame(array[i], value))
                return true;
        }

        return false;
    }

    public final static boolean isContain(String content, String value) {
        if (isEmpty(content) || isEmpty(value))
            return false;

        return (content.indexOf(value) != -1);
    }

    public final static boolean isContain(List list, Object object) {
        if (isEmpty(list))
            return false;

        return list.contains(object);
    }

    /**
     * 获取指定集合的大小；
     */
    public final static int size(List list) {
        if (isEmpty(list))
            return 0;
        else
            return list.size();
    }

    public final static int size(Map map) {
        if (isEmpty(map))
            return 0;
        else
            return map.size();
    }

    public final static int size(String[] array) {
        if (isEmpty(array))
            return 0;
        else
            return array.length;
    }

    public final static int size(Object[] array) {
        if (isEmpty(array))
            return 0;
        else
            return array.length;
    }

    /***************************************************************************
     * 判断一个时间和目前时间的比较 (<0，date2<date1) (=0， date2=date1) (>0， date2>date1)
     *
     * @param date1
     * @param date2
     * @return
     */
    public final static int getDaysBetween(Date date1, Date date2) {
        if (StringUtils.isEmpty(date1) || StringUtils.isEmpty(date2)) {
            return 1;
        }
        Calendar d1 = Calendar.getInstance();
        d1.setTime(date1);
        Calendar d2 = Calendar.getInstance();
        d2.setTime(date2);
        int days = d2.get(Calendar.DAY_OF_YEAR)
                - d1.get(Calendar.DAY_OF_YEAR);
        if (d1.get(Calendar.YEAR) > d2.get(Calendar.YEAR)) {
            d1 = (Calendar) d1.clone();
            do {
                days -= d1.getActualMaximum(Calendar.DAY_OF_YEAR);
                // System.out.println("-----"+days);
                d2.add(Calendar.YEAR, 1);
            } while (d1.get(Calendar.YEAR) != d2
                    .get(Calendar.YEAR));
        }
        if (d1.get(Calendar.YEAR) < d2.get(Calendar.YEAR)) {
            d1 = (Calendar) d1.clone();
            do {
                days += d1.getActualMaximum(Calendar.DAY_OF_YEAR);
                // System.out.println("+++++"+days);
                d1.add(Calendar.YEAR, 1);
            } while (d1.get(Calendar.YEAR) != d2
                    .get(Calendar.YEAR));
        }
        return days;
    }

    /**
     * 格式化时间
     *
     * @param date
     * @return
     */
    public final static String formatDateTime(Date date) {
        return formatDateTime("yyyy-MM-dd HH:mm:ss", date, null);
    }

    public final static String formatDateTime(String style, Date date,
                                              String defaultValue) {
        if (isEmpty(style) || isEmpty(date))
            return defaultValue;

        SimpleDateFormat sdf = new SimpleDateFormat(style);
        return StringUtils.format(sdf.format(date), defaultValue);
    }

    /**
     * 判断一个时间和目前时间的比较 <0， 指定时间早于今天 ==0， 指定日期等于今天 >0， 指定日期晚于今天
     */
    public final static int timeCompareWithNow(Date date) {
        if (isEmpty(date))
            return 1;
        Date now = new Date();
        return (int) ((date.getTime() - now.getTime()) / (24 * 3600 * 1000));
    }

    /**
     * 计算两个calendar的天数差
     */
    public final static int dateBetween(Date c1, Date c2) {
        if (isEmpty(c1) || isEmpty(c2))
            return 0;

        Calendar calendar1 = Calendar.getInstance();
        calendar1.setTime(c1);
        Calendar calendar2 = Calendar.getInstance();
        calendar2.setTime(c2);

        return (int) ((calendar1.getTime().getTime() - calendar2.getTime().getTime()));
    }

    /**
     * 解析日期；
     */
    public final static Date parseDate(long milliSeconds) {
        return new Date(milliSeconds);
    }

    public final static Date parseDate(String date) {
        return parseDate("yyyy-MM-dd HH:mm:ss", date);
    }

    public final static Date parseDate(String date, Date defaultValue) {
        if (isEmpty(date))
            return defaultValue;

        return parseDate("yyyy-MM-dd", date);
    }

    public final static Date parseDate(String style, String date) {
        if (isEmpty(style) || isEmpty(date))
            return new Date();

        try {
            SimpleDateFormat sdf = new SimpleDateFormat(style);
            return sdf.parse(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return new Date();
        }
    }

    /**
     * 获取月份
     *
     * @param date
     * @return
     */
    public static int parseMonth(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(parseDate("yyyy-MM-dd", date));
        int month = cal.get(Calendar.MONTH) + 1;
        return month;
    }

    /**
     * 获取年份
     *
     * @param date
     * @return
     */
    public static int parseYear(String date) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(parseDate("yyyy-MM-dd", date));
        int month = cal.get(Calendar.YEAR);
        return month;
    }

    private final static ThreadLocal<SimpleDateFormat> dateFormater = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        }
    };

    private final static ThreadLocal<SimpleDateFormat> dateFormater2 = new ThreadLocal<SimpleDateFormat>() {
        @Override
        protected SimpleDateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd");
        }
    };


    /**
     * 以友好的方式显示时间
     *
     * @param sdate
     * @return
     */
    public static String friendly_time(String sdate) {

        Date time = parseDate("yyyy-MM-dd", sdate);

        if (time == null) {
            return "Unknown";
        }
        String ftime = "";
        Calendar cal = Calendar.getInstance();

        // 判断是否是同一天
        String curDate = dateFormater2.get().format(cal.getTime());
        String paramDate = dateFormater2.get().format(time);
        if (curDate.equals(paramDate)) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
            return ftime;
        }

        long lt = time.getTime() / 86400000;
        long ct = cal.getTimeInMillis() / 86400000;
        int days = (int) (ct - lt);
        if (days == 0) {
            int hour = (int) ((cal.getTimeInMillis() - time.getTime()) / 3600000);
            if (hour == 0)
                ftime = Math.max(
                        (cal.getTimeInMillis() - time.getTime()) / 60000, 1)
                        + "分钟前";
            else
                ftime = hour + "小时前";
        } else if (days == 1) {
            ftime = "昨天";
        } else if (days == 2) {
            ftime = "前天";
        } else if (days > 2 && days < 31) {
            ftime = days + "天前";
        } else if (days >= 31 && days <= 2 * 31) {
            ftime = "一个月前";
        } else if (days > 2 * 31 && days <= 3 * 31) {
            ftime = "2个月前";
        } else if (days > 3 * 31 && days <= 4 * 31) {
            ftime = "3个月前";
        } else {
//            ftime = "3个月前";
            ftime = dateFormater2.get().format(time);
        }
        return ftime;
    }

    /**
     * 以友好的方式显示时间
     *
     * @param date
     * @return
     */
    public static String friendly_time(Date date) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return friendly_time(f.format(date));
    }

    /**
     * 验证是否邮箱
     *
     * @param email
     * @return
     */
    public final static boolean isEmail(String email) {
        //电子邮件
        String check = "^([a-z0-9A-Z]+[-|\\.]?)+[a-z0-9A-Z]@([a-z0-9A-Z]+(-[a-z0-9A-Z]+)?\\.)+[a-zA-Z]{2,}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(email);
        return matcher.matches();
    }

    /**
     * 验证身份证
     *
     * @param card
     * @return
     */
    public final static boolean isCardId(String card) {
        String check = "\\d{17}[0-9a-zA-Z]|\\d{14}[0-9a-zA-Z]";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(card);
        return matcher.matches();
    }

    /**
     * 验证手机号码
     *
     * @param phone
     * @return
     */
    public final static boolean isPhone(String phone) {
        String check = "^((13[0-9])|(14[0-9])|(15[0-9])|(17[0-8])|(18[0-9]))\\d{8}$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(phone);
        return matcher.matches();
    }

    /**
     * 判断是否为手机号码(qbm)
     */
    public static boolean isPhoneNumberValid(String phoneNumber) {
        // String regExp = "^((13[0-9])|(15[^4,\\D])|(18[0,5-9]))\\d{8}$";
        // String regExp = "^(1)[0-9]{10}$";
        // Pattern p = Pattern.compile(regExp);
        // Matcher m = p.matcher(phoneNumber);
        // return m.matches();
        String reg = "^(\\d{10})|(\\d{11})|(\\d{12})|^((\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})|(\\d{4}|\\d{3})-(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1})|(\\d{7,8})-(\\d{4}|\\d{3}|\\d{2}|\\d{1}))$";
        Pattern p = Pattern.compile(reg);
        Matcher m = p.matcher(phoneNumber);
        return m.matches();
    }

    /**
     * 验证是否中文
     *
     * @param str
     * @return
     */
    public final static boolean isChinese(String str) {
        String check = "^[\\u4e00-\\u9fa5]+$";
        Pattern regex = Pattern.compile(check);
        Matcher matcher = regex.matcher(str);
        return matcher.matches();
    }

    public final static boolean isPassWord(String password) {
        boolean result = false;
        String[] regex = {"(?=.*[A-Z])(?=.*[0-9])[A-Z0-9]{6,20}"
                , "(?=.*[a-z])(?=.*[0-9])[a-z0-9]{6,20}"
                , "(?=.*[A-Z])(?=.*[a-z])[a-zA-Z]{6,20}"
                , "(?=.*[a-z])(?=.*[!#$%^&*.~])[a-z!#$%^&*.~]{6,20}"
                , "(?=.*[A-Z])(?=.*[!#$%^&*.~])[A-Z!#$%^&*.~]{6,20}"
                , "(?=.*[0-9])(?=.*[!#$%^&*.~])[0-9!#$%^&*.~]{6,20}"};
        for (String rg : regex) {
            Matcher matcher = Pattern.compile(rg).matcher(password);
            if (matcher.matches()) {
                result = true;
                break;
            }
        }
        return result;
    }


    public static String formatDuring(long mss) {
        long days = mss / (1000 * 60 * 60 * 24);
        long hours = (mss % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60);
        long minutes = (mss % (1000 * 60 * 60)) / (1000 * 60);
        String result = "";
        if (days != 0) result += days + "天";
        if (hours != 0) result += hours + "小时";
        if (minutes != 0) result += minutes + "分";
        return result;
    }

    /**
     * 创建｛文字内容、字体颜色、字体大小｝分段文字集合体
     *
     * @param text
     * @param color
     * @param textSize
     * @return
     */
    public static SpannableStringBuilder creSpanString(String[] text,
                                                       int[] color, int[] textSize, int[] textStyle) {
        if (text == null || color == null || textSize == null || textStyle == null)
            throw new IllegalArgumentException("参数不能为空");
        if (text.length != color.length || text.length != textSize.length || text.length != textStyle.length)
            throw new IllegalArgumentException("参数数组长度不一致");
        SpannableStringBuilder sb = new SpannableStringBuilder();

        try {
            for (int i = 0; i < text.length; i++) {
                SpannableString sp = FaceConversionUtil.getInstace().getExpressionString(BaseConfig.getContext(), text[i]);
                sp.setSpan(new ForegroundColorSpan(color[i]), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sp.setSpan(new AbsoluteSizeSpan(textSize[i], true), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sp.setSpan(new StyleSpan(textStyle[i]), 0, sp.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                sb.append(sp);
            }
            return sb;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb;
    }

}
