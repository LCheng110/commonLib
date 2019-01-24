package cn.jpush.im.android.utils;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Environment;
import android.text.TextUtils;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.enums.ConversationType;

import static android.os.Environment.MEDIA_MOUNTED;


public final class FileUtil {

    private final static String TAG = "FileUtil";

    public static final String STORAGE_DIR_NAME = "jPush-IM";

    public static final String AVATAR_DIR_NAME = "avatar";
    public static final String AVATAR_SMALL_DIR_NAME = "small-avatar";

    public static final String JPUSHIM_OUTPUT_DIR = JMessage.mContext.getFilesDir()
            .getAbsolutePath();

    public static final String OUTPUT_CATEGORY_MEDIA = "media";

    public static final String OUTPUT_CATEGORY_IMAGE = "images";

    public static final String OUTPUT_CATEGORY_ORIGINS = "origins";

    public static final String OUTPUT_CATEGORY_THUMBNAILS = "thumbnails";

    public static final String OUTPUT_CATEGORY_VOICE = "voice";

    public static final String OUTPUT_CATEGORY_FILE = "file";

    private static final String EXTERNAL_STORAGE_PERMISSION = "android.permission.WRITE_EXTERNAL_STORAGE";
    private static final String INDIVIDUAL_DIR_NAME = "jmessage-images";

    private static final String AVATAR_PATH_PREFIX = FileUtil.JPUSHIM_OUTPUT_DIR + File.separator
            + FileUtil.OUTPUT_CATEGORY_IMAGE + File.separator + FileUtil.AVATAR_DIR_NAME;

    private static final String AVATAR_SMALL_PATH_PREFIX = FileUtil.JPUSHIM_OUTPUT_DIR + File.separator
            + FileUtil.OUTPUT_CATEGORY_IMAGE + File.separator + FileUtil.AVATAR_SMALL_DIR_NAME;

    public static byte[] File2byte(String filePath) {
        byte[] buffer = null;
        try {
            File file = new File(filePath);
            FileInputStream fis = new FileInputStream(file);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            byte[] b = new byte[1024];
            int n;
            while ((n = fis.read(b)) != -1) {
                bos.write(b, 0, n);
            }
            fis.close();
            bos.close();
            buffer = bos.toByteArray();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return buffer;
    }

    public static File byte2File(byte[] buf, String dirPath, String fileName) {
        BufferedOutputStream bos = null;
        FileOutputStream fos = null;
        File file = null;
        try {
            File dir = new File(dirPath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            file = new File(dirPath, fileName);
            fos = new FileOutputStream(file);
            bos = new BufferedOutputStream(fos);
            bos.write(buf);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (bos != null) {
                try {
                    bos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return file;
    }

    public static void copyFileUsingStream(File source, File dest) throws IOException {
        Logger.d(TAG, "copy file . source = " + source + " dest = " + dest);
        if (null != dest && !dest.exists()) {
            dest.getParentFile().mkdirs();
        }
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(source);
            os = new FileOutputStream(dest);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
        } finally {
            if (null != is) {
                is.close();
            }
            if (null != os) {
                os.close();
            }
        }
    }

    public static File saveBitmapToFile(Bitmap bitmap, String filePath) {
        if (null == bitmap || null == filePath) {
            return null;
        }
        File file = new File(filePath);
        BufferedOutputStream os = null;
        try {
            if (!file.exists()) {
                file.getParentFile().mkdirs();
            }
            os = new BufferedOutputStream(new FileOutputStream(file));
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, os);
        } catch (IOException e) {
            Logger.w(TAG, "save bitmap to file failed. ", e);
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    Logger.e(TAG, e.getMessage(), e);
                }
            }
        }
        return file;
    }

    public static String SDPATH;

    static {
        SDPATH = Environment.getExternalStorageDirectory() + "/";
    }

    /**
     * 在SD卡上创建文件
     */
    public static File createSDFile(String fileName) throws IOException {
        File file = new File(SDPATH + fileName);
        if (!file.createNewFile()) {
            Logger.e(TAG, "[createSDFile] create sd file fail");
        }
        return file;
    }

    /**
     * 在SD卡上创建目录
     */
    public static File createSDDir(String dirName) {
        File dir = new File(SDPATH + dirName);
        dir.mkdirs();
        return dir;
    }

    /**
     * 判断SD卡上的文件夹是否存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(SDPATH + fileName);
        return file.exists();
    }

    /**
     * 将一个InputStream里面的数据写入到SD卡中
     */
    public static File write2SDFromInput(String path, String fileName, InputStream input) {
        File file = null;
        OutputStream output = null;
        try {
            createSDDir(path);
            file = createSDFile(path + fileName);
            output = new FileOutputStream(file);
            byte buffer[] = new byte[4 * 1024];
            while ((input.read(buffer)) != -1) {
                output.write(buffer);
            }
            output.flush();
        } catch (Exception e) {
            Logger.e(TAG, "[write2SDFromInput] catch a exception, e:" + e.getMessage());
            e.printStackTrace();
        } finally {
            try {
                if (input != null) {
                    input.close();
                }
                if (output != null) {
                    output.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public static boolean hasSDcard() {
        boolean isSDPresent = Environment.getExternalStorageState()
                .equals(Environment.MEDIA_MOUNTED);
        return isSDPresent;
    }

    public static String getFilePath(Context context, String fileName) {
        if (hasSDcard()) {
            File sdCard = Environment.getExternalStorageDirectory();
            File dir = new File(sdCard.getAbsolutePath() + File.separator + STORAGE_DIR_NAME);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File file = new File(dir, fileName);
//            Logger.d(TAG, "[getFilePath] 获取到的路径名为：" + file.getPath());
            return file.getPath();
        } else {
            File dir = context.getFilesDir();
            File file = new File(dir, fileName);
//            Logger.d(TAG, "[getFilePath] 获取到的路径名为：" + file.getPath());
            return file.getPath();
        }

    }

    public static String getAvatarDirPath() {
        return FileUtil.AVATAR_SMALL_PATH_PREFIX;
    }

    public static String getAvatarFilePath(String mediaID) {
        return getAvatarDirPath() + File.separator + StringUtils.getResourceIDFromMediaID(mediaID);
    }

    public static String getBigAvatarDirPath() {
        return FileUtil.AVATAR_PATH_PREFIX;
    }

    public static String getBigAvatarFilePath(String mediaID) {
        return getBigAvatarDirPath() + File.separator + StringUtils.getResourceIDFromMediaID(mediaID);
    }

    public static String getImageOriginDirPath(ConversationType type, String targetID, String appkey) {
        String absPath = "";
        if (!TextUtils.isEmpty(targetID) && null != type) {
            String convString = type + "_" + targetID;
            absPath = getMediaFilePath(appkey, convString, OUTPUT_CATEGORY_IMAGE, OUTPUT_CATEGORY_ORIGINS);
        }
        return absPath;
    }

    public static String getImageThumbnailDirPath(ConversationType type, String targetID, String appkey) {
        String absPath = "";
        if (!TextUtils.isEmpty(targetID) && null != type) {
            String convString = type + "_" + targetID;
            absPath = getMediaFilePath(appkey, convString, OUTPUT_CATEGORY_IMAGE, OUTPUT_CATEGORY_THUMBNAILS);
        }
        return absPath;
    }

    public static String getVoiceDirPath(ConversationType type, String targetID, String appkey) {
        String absPath = "";
        if (!TextUtils.isEmpty(targetID) && null != type) {
            String convString = type + "_" + targetID;
            absPath = getMediaFilePath(appkey, convString, OUTPUT_CATEGORY_VOICE);
        }
        return absPath;
    }

    public static String getFileDirPath(ConversationType type, String targetID, String appkey) {
        String absPath = "";
        if (!TextUtils.isEmpty(targetID) && null != type) {
            String convString = type + "_" + targetID;
            absPath = getMediaFilePath(appkey, convString, OUTPUT_CATEGORY_FILE);
        }
        return absPath;
    }

    private static String getMediaFilePath(String... subDirs) {
        String absPath = JPUSHIM_OUTPUT_DIR
                + File.separator + OUTPUT_CATEGORY_MEDIA;
        for (String subDir : subDirs) {
            if (!TextUtils.isEmpty(subDir)) {
                absPath += File.separator + subDir;
            }
        }
        return absPath;
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context) {
        return getIndividualCacheDirectory(context, INDIVIDUAL_DIR_NAME);
    }

    /**
     * Returns individual application cache directory (for only image caching from ImageLoader). Cache directory will be
     * created on SD card <i>("/Android/data/[app_package_name]/cache/uil-images")</i> if card is mounted and app has
     * appropriate permission. Else - Android defines cache directory on device's file system.
     *
     * @param context  Application context
     * @param cacheDir Cache directory path (e.g.: "AppCacheDir", "AppDir/cache/images")
     * @return Cache {@link File directory}
     */
    public static File getIndividualCacheDirectory(Context context, String cacheDir) {
        File appCacheDir = getCacheDirectory(context);
        File individualCacheDir = new File(appCacheDir, cacheDir);
        if (!individualCacheDir.exists()) {
            if (!individualCacheDir.mkdir()) {
                individualCacheDir = appCacheDir;
            }
        }
        return individualCacheDir;
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> if card is mounted and app has appropriate permission. Else -
     * Android defines cache directory on device's file system.
     *
     * @param context Application context
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link android.content.Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context) {
        return getCacheDirectory(context, true);
    }

    /**
     * Returns application cache directory. Cache directory will be created on SD card
     * <i>("/Android/data/[app_package_name]/cache")</i> (if card is mounted and app has appropriate permission) or
     * on device's file system depending incoming parameters.
     *
     * @param context        Application context
     * @param preferExternal Whether prefer external location for cache
     * @return Cache {@link File directory}.<br />
     * <b>NOTE:</b> Can be null in some unpredictable cases (if SD card is unmounted and
     * {@link android.content.Context#getCacheDir() Context.getCacheDir()} returns null).
     */
    public static File getCacheDirectory(Context context, boolean preferExternal) {
        File appCacheDir = null;
        String externalStorageState;
        try {
            externalStorageState = Environment.getExternalStorageState();
        } catch (NullPointerException e) { // (sh)it happens (Issue #660)
            externalStorageState = "";
        } catch (IncompatibleClassChangeError e) { // (sh)it happens too (Issue #989)
            externalStorageState = "";
        }
        if (preferExternal && MEDIA_MOUNTED.equals(externalStorageState) && hasExternalStoragePermission(context)) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/cache/";
            Logger.ww(TAG, "Can't define system cache directory! '%s' will be used." + cacheDirPath);
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    private static boolean hasExternalStoragePermission(Context context) {
        int perm = context.checkCallingOrSelfPermission(EXTERNAL_STORAGE_PERMISSION);
        return perm == PackageManager.PERMISSION_GRANTED;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "cache");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                Logger.ww(TAG, "Unable to create external cache directory");
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {
                Logger.ii(TAG, "Can't create \".nomedia\" file in application external cache directory");
            }
        }
        return appCacheDir;
    }

    private FileUtil() {
    }
}
