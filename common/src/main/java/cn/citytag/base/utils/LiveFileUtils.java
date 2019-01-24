package cn.citytag.base.utils;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import okhttp3.ResponseBody;

public class LiveFileUtils {
    public static final String TAG = "LiveFileUtils";
    public static String LIVE_GIFT_999 = "999";
    private static String LIVE_CACHE_PATH;
    private static String LIVE_GIFT_PATH;

    public static void init(String path) {
        if (!TextUtils.isEmpty(LIVE_CACHE_PATH)) {
            return;
        }
        LIVE_CACHE_PATH = path + File.separator + "live";
        File dir = new File(LIVE_CACHE_PATH);
        if (!dir.exists()) {
            Log.i(TAG, "init LIVE_CACHE_PATH: " + dir.mkdirs());
        }
        initGift();
    }

    private static void initGift() {
        if (TextUtils.isEmpty(LIVE_CACHE_PATH)) {
            return;
        }
        if (!TextUtils.isEmpty(LIVE_GIFT_PATH)) {
            return;
        }
        LIVE_GIFT_PATH = LIVE_CACHE_PATH + File.separator + "gift";
        File dir = new File(LIVE_GIFT_PATH);
        if (!dir.exists()) {
            Log.i(TAG, "init LIVE_GIFT_PATH: " + dir.mkdirs());
        }
        File dir999 = new File(LIVE_GIFT_PATH + File.separator + LIVE_GIFT_999);
        if (!dir999.exists()) {
            Log.i(TAG, "init LIVE_GIFT_PATH: " + dir999.mkdirs());
        }
    }

    public static String getLiveCachePath() {
        return LIVE_CACHE_PATH;
    }

    public static String getLiveGiftPath() {
        return LIVE_GIFT_PATH;
    }

    public static File getCacheDir(Context context, String uniqueName) {
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() != null) {
                cachePath = context.getExternalCacheDir().getPath();
            } else {
                cachePath = context.getCacheDir().getPath();
            }
        } else {
            cachePath = context.getCacheDir().getPath();
        }
        return new File(cachePath + File.separator + uniqueName);
    }

    public static void unZip(final String outDir) {
        File[] files = new File(outDir).listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name != null && name.endsWith(".zip");
            }
        });
        if (files.length == 0) {
            return;
        }
        try {
            for (final File file : files) {
                new AsyncTask() {
                    @Override
                    protected Object doInBackground(Object[] objects) {
                        try {
                            int len = file.getAbsolutePath().length();
                            //判断解压后的文件是否存在,截取.zip之前的字符串
                            File unzipDir = new File(file.getAbsolutePath().substring(0, len - 4));
                            unzipDir.mkdirs();
                            unZipFolder(file.getAbsolutePath(), outDir);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        return null;
                    }

                    @Override
                    protected void onPostExecute(Object o) {
                        if (file.exists()) {
                            file.delete();
                        }
                    }
                }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void unZipFolder(String zipFileString, String outPathString) throws Exception {
        ZipInputStream inZip = new ZipInputStream(new FileInputStream(zipFileString));
        ZipEntry zipEntry;
        String szName;
        while ((zipEntry = inZip.getNextEntry()) != null) {
            szName = zipEntry.getName();
            if (zipEntry.isDirectory()) {
                // get the folder name of the widget
                szName = szName.substring(0, szName.length() - 1);
                File folder = new File(outPathString + File.separator + szName);
                Log.i(TAG, "unZipDir: " + outPathString + File.separator + szName);
                folder.mkdirs();
            } else {
                File file = new File(outPathString + File.separator + szName);
                Log.i(TAG, "unZipFolder: " + outPathString + File.separator + szName);
                file.createNewFile();
                FileOutputStream out = new FileOutputStream(file);
                int len;
                byte[] buffer = new byte[1024];
                while ((len = inZip.read(buffer)) != -1) {
                    out.write(buffer, 0, len);
                    out.flush();
                }
                out.close();
            }
        }
        inZip.close();
    }

    public static boolean writeResponseBodyToDisk(ResponseBody body, String fileName) {
        Log.d(TAG, "writeResponseBodyToDisk: " + body + " of " + fileName);
        try {
            File futureStudioIconFile = new File(LIVE_GIFT_PATH + File.separator + fileName);
            InputStream inputStream = null;
            OutputStream outputStream = null;
            try {
                byte[] fileReader = new byte[4096];
                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;
                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);
                while (true) {
                    int read = inputStream.read(fileReader);
                    if (read == -1) {
                        break;
                    }
                    outputStream.write(fileReader, 0, read);
                    fileSizeDownloaded += read;
                    Log.d(TAG, "file download: " + fileSizeDownloaded + " of " + fileSize);
                }
                outputStream.flush();
                return true;
            } catch (IOException e) {
                return false;
            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {
            return false;
        }
    }
}
