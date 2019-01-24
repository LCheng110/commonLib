package cn.citytag.base.utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by yangfeng01 on 2017/12/20.
 */

public class DateUtil {

	/**
	 * 获取时间戳
	 *
	 * @return
	 */
	public static String getTimestamp() {
		return System.currentTimeMillis() + "";
	}

	/**
	 * 输入：2018-05-14T07:40:02Z
	 * 输出：2018-05-14 15:40:02
	 *
	 * @param utcString
	 * @return
	 */
	public static Date formatUtc2Sdf(String utcString) {
		try {
			utcString = utcString.replace("Z", " UTC");
			SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
			SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = utcFormat.parse(utcString);
			return date;
			//return defaultFormat.format(date);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return null;
		}
	}

	/**
	 * 输入：String date = “2016-08-15T16:00:00.000Z”
	 * 输出：2016-08-16 00:00:00
	 *
	 * @param UTCString
	 * @return
	 */
	private static String UTCStringtODefaultString(String UTCString) {
		try {
			UTCString = UTCString.replace("Z", " UTC");
			SimpleDateFormat utcFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS Z");
			SimpleDateFormat defaultFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			Date date = utcFormat.parse(UTCString);
			return defaultFormat.format(date);
		} catch (ParseException pe) {
			pe.printStackTrace();
			return null;
		}
	}

	public static boolean before(Date date1, Date date2) {
		return date1.before(date2);
	}

	public static boolean before(String time1, String time2) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date a = sdf.parse(time1);
		Date b = sdf.parse(time2);
		return a.before(b);
	}
}
