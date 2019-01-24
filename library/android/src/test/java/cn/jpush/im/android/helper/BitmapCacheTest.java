package cn.jpush.im.android.helper;

import android.graphics.Bitmap;

import junit.framework.Assert;

import org.junit.Test;

import cn.jpush.im.android.helpers.BitmapCacheHelper;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by ${chenyn} on 16/5/27.
 *
 * @desc :
 * @parame :
 * @return :
 */

public class BitmapCacheTest {

    //Bitmap == null
    @Test
    public void addBitmapToCache() {
        String mediaID = "mediaID1";
        Bitmap mBitmap = null;

        Assert.assertEquals(false, BitmapCacheHelper.getInstance().addBitmapToCache(mediaID, mBitmap));
    }

    // mediaID != null Bitmap != null
    @Test
    public void addBitmapToCache_2() {
        String mediaID = "/mediaID2";
        Bitmap mBitmap = mock(Bitmap.class);

        Assert.assertEquals(true, BitmapCacheHelper.getInstance().addBitmapToCache(mediaID, mBitmap));
    }

    //mediaID != null
    public void getBitmapFromMemCache() {
        String mediaID = "/mediaID3";
        Bitmap bitmapFromMemCache = BitmapCacheHelper.getInstance().getBitmapFromMemCache(mediaID);
        Assert.assertEquals(null, bitmapFromMemCache);
    }

    //mediaID != null
    public void removeBitmapFromCache() {
        String mediaID = "/mediaID4";
        boolean b = BitmapCacheHelper.getInstance().removeBitmapFromCache(mediaID);
        Assert.assertEquals(false, b);
    }

}
