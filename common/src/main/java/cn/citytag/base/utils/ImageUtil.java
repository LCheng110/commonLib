package cn.citytag.base.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.view.WindowManager;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by qbm on 2016/4/18.
 */
public class ImageUtil {

    private static final String TAG = ImageUtil.class.getSimpleName();

    private ImageUtil() {

    }

    /**
     * 将filePath的图片路径转成bitmap
     *
     * @param filePath
     * @param options
     * @return
     */
    public static Bitmap getBitmapByPath(String filePath, BitmapFactory.Options options) {
        FileInputStream fis = null;
        Bitmap bitmap = null;
        try {
            File file = new File(filePath);
            fis = new FileInputStream(file);
            bitmap = BitmapFactory.decodeStream(fis, null, options);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            L.e(TAG, "getBitmapByPath, file not found! path = " + filePath);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return bitmap;
    }

    /**
     * 把bitmap存储到本地文件图片
     *
     * @param bitmap
     * @param filePath
     */
    private static void saveBitmap2SD(Bitmap bitmap, String filePath) {
        if (bitmap == null) {
            return;
        }
        File parentDir = new File(filePath.substring(0, filePath.lastIndexOf(File.separator)));
        if (!parentDir.exists()) {
            if (!parentDir.mkdirs()) {
                L.d(TAG, "pic upload directory created failed!");
            }
        }
        BufferedOutputStream bufferedOutputStream = null;
        try {
            FileOutputStream fos = new FileOutputStream(filePath);
            bufferedOutputStream = new BufferedOutputStream(fos);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bufferedOutputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.flush();
                    bufferedOutputStream.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * targetWidth和targetHeight可以是同一路径，覆盖掉之前存储的数据。
     *
     * @param fromPath
     * @param toPath
     * @param targetWidth
     * @param targetHeight
     */
    public static void compressImage(String fromPath, String toPath, int targetWidth, int targetHeight) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(fromPath, options);
        options.inSampleSize = calculateInSampleSize(options, targetWidth, targetHeight);
        L.d(TAG, "option.inSampleSize == " + options.inSampleSize);
        options.inJustDecodeBounds = false;
        Bitmap bitmap = BitmapFactory.decodeFile(fromPath, options);
        final int shrinkWidth = bitmap.getWidth();
        final int shrinkHeight = bitmap.getHeight();
        L.d(TAG, "after shrink bitmap width == " + shrinkWidth + ", height == " + shrinkHeight);
        int degree = readPicDegree(fromPath);
        if (degree == 0) {
            saveBitmap2SD(bitmap, toPath);
        } else {
            Bitmap rotatedBitmap = getRotatedBitmap(bitmap, degree);
            if (bitmap != null && !bitmap.isRecycled()) {
                bitmap.recycle();
            }
            saveBitmap2SD(rotatedBitmap, toPath);
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int targetWidth, int targetHeight) {
        final int originalWidth = options.outWidth;
        final int originalHeight = options.outHeight;
        L.d(TAG, "original width == " + originalWidth + ", original height == " + originalHeight);
        int inSampleSize = 1;
        if (targetWidth < originalWidth && targetHeight < originalHeight) {
            return inSampleSize;
        }
        final int widthRatio = Math.round((float) originalWidth / targetWidth);
        final int heightRatio = Math.round((float) originalHeight / targetHeight);
        inSampleSize = widthRatio > heightRatio ? widthRatio : heightRatio; // 根据场景取压缩比大的或者小的，这里取大的
        inSampleSize = Math.max(1, inSampleSize);
        if (inSampleSize == 1) {
            return inSampleSize;
        }
        int roundedSize = 1;
        if (inSampleSize <= 8) {
            while (roundedSize < inSampleSize) { // 2,4,8
                roundedSize <<= 1;
            }
        } else {
            roundedSize = (inSampleSize + 7) / 8 * 8;
        }
        return roundedSize;
    }

    //对图片进行压缩处理
    public static Bitmap scale(String srcPath, Context context) {
        BitmapFactory.Options newOpts = new BitmapFactory.Options();
        //开始读入图片，此时把options.inJustDecodeBounds 设回true了
        newOpts.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(srcPath, newOpts);//此时返回bm为空

        newOpts.inJustDecodeBounds = false;
        int w = newOpts.outWidth;
        int h = newOpts.outHeight;
        //高和宽我们设置为
        WindowManager wm = (WindowManager) context
                .getSystemService(Context.WINDOW_SERVICE);
        float hh = wm.getDefaultDisplay().getHeight();//这里设置高度为屏幕高度
        float ww = wm.getDefaultDisplay().getWidth();//这里设置宽度为屏幕宽度
        //        hh = 600;
        //        ww = 600;
        //缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        float temp = 0;
        if (w > h && w > ww) { //如果宽度大的话根据宽度固定大小缩放
            temp = w / ww;
        } else if (w < h && h > hh) { //如果高度高的话根据宽度固定大小缩放
            temp = h / hh;
        }
        be = (int) Math.ceil(temp);
        if (be <= 0) {
            be = 1;
        }


        newOpts.inSampleSize = be;//设置缩放比例
        //重新读入图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(srcPath, newOpts);
        return bitmap;//压缩好比例大小后再进行质量压缩
    }

    //将图片保存到本地
    public static String saveImageToGallery(Bitmap bmp, String url) {
        String path;
        // 首先保存图片
        File appDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "photo");
        if (!appDir.exists()) {
            appDir.mkdir();
        }
        String fileName = EncryptUtil.md5(url) + ".jpg";
        File file = new File(appDir, fileName);
        path = file.getAbsolutePath();
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 30, fos);
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            //ToastUtil.toastFail("保存失败了");
        } catch (IOException e) {
            e.printStackTrace();
            //ToastUtil.toastFail("保存失败了");
        }
        return path;
    }

    public static int readPicDegree(String picPath) {
        int degree = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(picPath);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {

                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;

                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;

                default:
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    private static Bitmap getRotatedBitmap(Bitmap bitmap, int degree) {
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        Bitmap rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        return rotatedBitmap;
    }


    //oss 转换工具类
    public static String getSpecificUrl(String url, int width, int height) {
        return url + "?x-oss-process=image/resize,m_fill,w_"
                + UIUtils.dip2px(width) + ",h_" + UIUtils.dip2px(height);
    }

    //oss 新的转换
    public static String getSpecificNewUrl(String url, int width, int height) {
        return url + "?x-oss-process=image/resize,m_fill,w_"
                + width + ",h_" + height;
    }

    /**
     * 正则判断url是否是gif
     */
    public static boolean urlIsGif(String url) {
        boolean isGif = false;
        if (url == null || url.length() == 0) {
            return isGif;
        }
        String suffixes = "gif";
        Pattern pat = Pattern.compile("[\\w]+[\\.](" + suffixes + ")");//正则判断
        Matcher mc = pat.matcher(url);//条件匹配
        while (mc.find()) {
            isGif = true;
        }
        return isGif;
    }

}
