package cn.citytag.base.widget.facelib.util;

import android.content.Context;
import android.content.SharedPreferences;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by liguangchun on 2017/12/7.
 */

public class FileUtil {
    private  static SharedPreferences sharedPreferences;
    /**
     * 读取表情配置文件
     *
     * @param context
     * @return
     */
    public static List<String> getEmojiFile(Context context) {

        try {
            List<String> list = new ArrayList<>();
            InputStream in = context.getResources().getAssets().open("emoji");
            BufferedReader br = new BufferedReader(new InputStreamReader(in,
                    "UTF-8"));
            String str = null;
            while ((str = br.readLine()) != null) {
                list.add(str);
            }
            br.close();
            return list;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

	/**
	 * 读取表情配置文件
	 *
	 * @param context
	 * @return
	 */
	public static List<String> getEmojiFile(Context context, String fileName) {
		try {
			List<String> list = new ArrayList<>();
			InputStream in = context.getResources().getAssets().open(fileName);
			BufferedReader br = new BufferedReader(new InputStreamReader(in,
					"UTF-8"));
			String str = null;
			while ((str = br.readLine()) != null) {
				list.add(str);
			}
			br.close();
			return list;
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

}
