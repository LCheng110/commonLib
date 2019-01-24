package cn.jpush.im.android.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.DisplayMetrics;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import cn.jpush.im.android.JMessage;

public class BitmapUtils {

    private static final String TAG = "BitmapUtils";

    public static final int TUMBNAIL_ORIGIN_EDGE = 150;

    public static final int SMALL_AVATAR_EDGE = 75;

    private static final float THUMBNAIL_WIDTH_RATE = 0.422f;
    private static final float THUMBNAIL_HEIGHT_RATE = 0.238f;

    private static final String IMG_OUTPUT_DIR = JMessage.mContext.getFilesDir()
            + "/images";

    private static final String THUMBNAILS_DIR = "thumbnails";

    private static final String ORIGINS_DIR = "origins";

    public static DisplayMetrics mDisplayMetrics = JMessage.mContext
            .getApplicationContext().getResources().getDisplayMetrics();

    private final static Object createThumbLock = new Object();

    public static String createThumbnailAndSave(Bitmap bitmap, BitmapFactory.Options opts, int edge, String resourceID) {
        String filePath = saveOriginToLocal(bitmap, resourceID);
        if (null != filePath) {
            return createThumbnailAndSave(filePath, opts, edge, resourceID);
        }
        return null;
    }

    public static String createThumbnailAndSave(String filePath, BitmapFactory.Options originOpts, int edge, String resourceID) {
        String thumbPath = null;
        //根据传入的文件生成缩略图，并保存到本地
        Bitmap thumbBmp = createThumbnail(filePath, originOpts, edge);
        if (null != thumbBmp) {
            thumbPath = BitmapUtils.saveThumbnailToLocal(thumbBmp, resourceID);
            thumbBmp.recycle();
        }
        return thumbPath;
    }

    public synchronized static Bitmap createThumbnail(String filePath, BitmapFactory.Options originOpts, int edge) {
        if (null == originOpts) {
            originOpts = new BitmapFactory.Options();
        }
        //根据传入的文件生成缩略图
        originOpts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, originOpts);
        BitmapFactory.Options thumbOpts = new BitmapFactory.Options();
        thumbOpts.inSampleSize = BitmapUtils.computeSampleSize(originOpts, edge);
        thumbOpts.outWidth = originOpts.outWidth;
        thumbOpts.outHeight = originOpts.outHeight;
        thumbOpts.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, thumbOpts);
    }

    public synchronized static String saveOriginToLocal(Bitmap bitmap, String resourceID) {
        return saveBitmapToLocal(bitmap, ORIGINS_DIR, resourceID);
    }

    public synchronized static String saveThumbnailToLocal(Bitmap bitmap, String resourceID) {
        return saveBitmapToLocal(bitmap, THUMBNAILS_DIR, resourceID);
    }

    public synchronized static String saveBitmapToLocal(Bitmap bitmap, String directory, String resourceID) {
        if (null == bitmap) {
            return null;
        }
        String filePath;
        FileOutputStream fileOutput = null;
        File imgFile = null;
        try {
            File dir = new File(IMG_OUTPUT_DIR + "/" + directory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            imgFile = new File(dir.getAbsoluteFile() + "/" + resourceID);
            imgFile.createNewFile();
            fileOutput = new FileOutputStream(imgFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 80, fileOutput);
            fileOutput.flush();
            filePath = imgFile.getAbsolutePath();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            filePath = null;
        } catch (IOException e) {
            e.printStackTrace();
            filePath = null;
        } finally {
            if (null != fileOutput) {
                try {
                    fileOutput.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return filePath;
    }

    public static BitmapFactory.Options computeThumbSize(BitmapFactory.Options options) {
        int originW = options.outWidth;
        int originH = options.outHeight;
        int targetW;
        int targetH;
        float originR;
        if (originW >= originH) {
            originR = originH / originW;
            if (originR < 0.4f) {
                originR = 0.4f;
            }
            targetW = (int) (mDisplayMetrics.widthPixels * THUMBNAIL_WIDTH_RATE);
            targetH = (int) (targetW * originR);
        } else {
            originR = originW / originH;
            if (originR < 0.4f) {
                originR = 0.4f;
            }
            targetH = (int) (mDisplayMetrics.heightPixels * THUMBNAIL_HEIGHT_RATE);
            targetW = (int) (targetH * originR);
        }
        options.outWidth = targetW;
        options.outHeight = targetH;
        return options;
    }

    /**
     * 根据屏幕密度计算缩小倍数
     *
     * @param options option对象
     * @return int 缩小的倍数
     */
    public static int computeSampleSize(BitmapFactory.Options options, int edge) {
        return computeSampleSize(options, mDisplayMetrics.density, edge);
    }

    /**
     * 根据屏幕密度计算缩小倍数
     *
     * @param options option对象
     * @param density 指定的屏幕密度
     * @return int 缩小的倍数
     */
    public static int computeSampleSize(BitmapFactory.Options options, float density, int edge) {
        Logger.d(TAG, "display density is = " + density + " edge is = " + edge);
        int minSideLength = (int) (edge * density);//短边的最大长度
        int maxNumOfPixels = (int) Math.pow(minSideLength, 2);
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        int roundedSize;
        if (initialSize <= 8) {
            roundedSize = 1;
            while (roundedSize < initialSize) {
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (initialSize + 7) / 8 * 8;
        }
        Logger.d(TAG, "image width = " + options.outWidth + " image height = " + options.outHeight);
        Logger.d(TAG, "roundedSize is " + roundedSize);
        return roundedSize;
    }

    private static int computeInitialSampleSize(BitmapFactory.Options options, int minSideLength,
                                                int maxNumOfPixels) {
        double w = options.outWidth;
        double h = options.outHeight;

        int lowerBound = (maxNumOfPixels == -1) ? 1
                : (int) Math.ceil(Math.sqrt(w * h / maxNumOfPixels));
        int upperBound = (minSideLength == -1) ? 128
                : (int) Math.min(Math.floor(w / minSideLength), Math.floor(h / minSideLength));

        if (upperBound < lowerBound) {
            // return the larger one when there is no overlapping zone.
            return lowerBound;
        }

        if ((maxNumOfPixels == -1) && (minSideLength == -1)) {
            return 1;
        } else if (minSideLength == -1) {
            return lowerBound;
        } else {
            return upperBound;
        }
    }
}
