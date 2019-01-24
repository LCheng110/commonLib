package com.luck.picture.lib.compress;

import android.text.TextUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

class Checker {
    private static List<String> format = new ArrayList<>();
    private static final String JPG = "jpg";
    private static final String JPEG = "jpeg";
    private static final String PNG = "png";
    private static final String WEBP = "webp";
    private static final String GIF = "gif";
    private static final String BMP = "bmp";

    static {
        format.add(JPG);
        format.add(JPEG);
        format.add(PNG);
        format.add(WEBP);
        format.add(GIF);
        format.add(BMP);
    }

    static boolean isImage(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        String suffix = path.substring(path.lastIndexOf(".") + 1, path.length());
        return format.contains(suffix.toLowerCase());
    }

    static boolean isJPG(String path) {
        if (TextUtils.isEmpty(path)) {
            return false;
        }

        String suffix = path.substring(path.lastIndexOf("."), path.length()).toLowerCase();
        return suffix.contains(JPG) || suffix.contains(JPEG);
    }

    static String checkSuffix(String path) {
        if (TextUtils.isEmpty(path)) {
            return ".jpg";
        }

        return path.substring(path.lastIndexOf("."), path.length());
    }

    static boolean isNeedCompress(int leastCompressSize, String path) {
        if (urlIsGif(path)) {
            return false;
        }

        if (leastCompressSize > 0) {
            File source = new File(path);
            if (!source.exists()) {
                return false;
            }

            return source.length() > (leastCompressSize << 10);
        }
        return true;
    }

    private static boolean urlIsGif(String url) {
        boolean isGif = false;
        if (url == null || url.length() == 0) {
            return isGif;
        }
        String suffixes = "";
        String[] array = url.split("\\.");
        if (array.length > 1) {
            suffixes = array[array.length - 1];
        }
        //Pattern pat = Pattern.compile("[\\w]+[.](" + suffixes + ")");//正则判断
        //Matcher mc = pat.matcher(url);//条件匹配
        //while (mc.find()) {
        //isGif = true;
        // }
        if (suffixes != null && suffixes.equals("gif")) {
            isGif = true;
        }
        return isGif;
    }
}