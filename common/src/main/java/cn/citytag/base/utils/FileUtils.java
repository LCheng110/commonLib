package cn.citytag.base.utils;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import cn.citytag.base.config.BaseConfig;

/**
 * Created by yangfeng01 on 2017/12/19.
 */

public class FileUtils {

    private static final String TAG = "LiveFileUtils";

    public static void createFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            if (!file.mkdirs()) {
                if (!file.mkdir()) {
                    L.e(TAG, "create file failed! file path == " + filePath);
                }
            }
        }
    }

    public static void createFile(File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                if (!file.mkdir()) {
                    L.e(TAG, "create file failed! file path == " + file.getAbsolutePath());
                }
            }
        }
    }

    /**
     * 确保目录已创建
     *
     * @param file
     */
    public static void ensureFile(@NonNull File file) {
        if (!file.exists()) {
            if (!file.mkdirs()) {
                L.e(TAG, "file mkdirs failed! file == " + file);
            }
        }
    }

    /**
     * 复制文件
     *
     * @param oldPath
     * @param newPath
     */
    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(oldPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1444];
                int length;
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();

        }
    }

    //
    public static void deleteFile(String path, Activity activity) {
        if (path != null || !TextUtils.isEmpty(path)) {
            new File(path).delete();
        }
//
//        Uri uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
//        ContentResolver mContentResolver = activity.getContentResolver();
//        String where = MediaStore.Images.Media.DATA + "='" + path + "'";
////删除图片
//        mContentResolver.delete(uri, where, null);
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
////            final Intent scanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
////            final Uri contentUri = Uri.fromFile(new File(path));
////            scanIntent.setData(contentUri);
////            activity.sendBroadcast(scanIntent);
////        } else {
//            final Intent intent = new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory()));
//            activity.sendBroadcast(intent);
//        }
//		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//			new MediaScanner(activity,path);
//		} else {
//        activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED, Uri.parse("file://" + Environment.getExternalStorageDirectory())));
//		}
    }

    /**
     * 将json文件转为字符串
     *
     * @param filename
     * @return
     */
    public static String getJson(String filename) {
        StringBuilder builder = new StringBuilder();
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(openAssetFile(BaseConfig.getContext(), filename)));
            String line;
            while ((line = reader.readLine()) != null) {
                builder.append(line);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return builder.toString();
    }

    /**
     * 打开Asset下的文件
     *
     * @param context
     * @param fileName
     * @return
     */
    public static InputStream openAssetFile(Context context, String fileName) {
        AssetManager am = context.getAssets();
        InputStream is = null;
        try {
            is = am.open(fileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return is;
    }
}
