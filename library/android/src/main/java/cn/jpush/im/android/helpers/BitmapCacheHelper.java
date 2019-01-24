package cn.jpush.im.android.helpers;

import android.graphics.Bitmap;
import android.text.TextUtils;

import cn.jpush.im.android.helpers.cache.memory.MemoryCache;
import cn.jpush.im.android.helpers.cache.memory.impl.LruMemoryCache;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;


public class BitmapCacheHelper {
    private static final String TAG = "BitmapCacheHelper";

    private static BitmapCacheHelper sInstance;

    private MemoryCache memoryCache;


    private BitmapCacheHelper() {
        // Get max available VM memory, exceeding this amount will throw an
        // OutOfMemory exception. Stored in kilobytes as LruCache takes an
        // int in its constructor.
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        // Use 1/8th of the available memory for this memory cache.
        final int cacheSize = maxMemory / 16;
        memoryCache = new LruMemoryCache(cacheSize * 1024);
    }

    public static synchronized BitmapCacheHelper getInstance() {
        if (null == sInstance) {
            sInstance = new BitmapCacheHelper();
        }
        return sInstance;
    }

    public boolean addBitmapToCache(String mediaID, Bitmap bitmap) {
        if (TextUtils.isEmpty(mediaID) || null == bitmap) {
            Logger.ww(TAG, "add bitmap to cache failed! mediaID = " + mediaID + " bitmap = " + bitmap);
            return false;
        }
        return memoryCache.put(StringUtils.getResourceIDFromMediaID(mediaID), bitmap);
    }

    public Bitmap getBitmapFromMemCache(String mediaID) {
        if (TextUtils.isEmpty(mediaID)) {
            return null;
        }
        return memoryCache.get(StringUtils.getResourceIDFromMediaID(mediaID));
    }

    public boolean removeBitmapFromCache(String mediaID) {
        if (TextUtils.isEmpty(mediaID)) {
            Logger.ww(TAG, "remove bitmap from cache failed! mediaID is null ");
            return false;
        }
        return memoryCache.remove(StringUtils.getResourceIDFromMediaID(mediaID)) != null;
    }

    public void clearCache() {
        memoryCache.clear();
    }

}
