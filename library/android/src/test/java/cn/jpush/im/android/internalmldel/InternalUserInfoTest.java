package cn.jpush.im.android.internalmldel;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.io.File;

import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.FileUtil;

/**
 * Created by ${chenyn} on 16/6/6.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({ExpressionValidateUtil.class, FileUtil.class, JMessage.class})
public class InternalUserInfoTest {

    //appKey != null
    @Test
    public void getAppKey_2() {
        InternalUserInfo info = new InternalUserInfo();
        info.setAppkey("1234");

        String appKey = info.getAppKey();
        Assert.assertEquals("1234", appKey);
    }
    
    //timeInMillis > 0
//    @Test
//    public void setBirthday_1() {
//        InternalUserInfo info = new InternalUserInfo();
//        long timeInMillis = 123456789;
//        info.setBirthday(timeInMillis);
//
//        long birthday = info.getBirthday();
//        Assert.assertEquals(57600000, birthday);
//    }

    //timeInMillis > 0
//    @Test
//    public void setBirthday_2() {
//        InternalUserInfo info = new InternalUserInfo();
//        long timeInMillis = -123456789;
//        info.setBirthday(timeInMillis);
//
//        long birthday = info.getBirthday();
//        Assert.assertEquals(-28800000, birthday);
//    }
    
    //ExpressionValidateUtil.validMediaID(avatarMediaID) = false
    @Test
    public void getAvatarFile() {
        InternalUserInfo info = new InternalUserInfo();
        info.setAvatarMediaID("path");
        File avatarFile = info.getAvatarFile();
        Assert.assertEquals(null, avatarFile);
    }

}
