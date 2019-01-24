package cn.jpush.im.android;


import android.database.sqlite.SQLiteDatabase;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.jpush.im.android.utils.UserIDHelper;

import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by ${chenyn} on 16/5/26.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(UserIDHelper.class)
public class UserIDHelperTestTwo {

    //getUidInCache(userName, appkey) != 0
    @Test
    public void getUserIDFromLocal_1() throws Exception {
        PowerMockito.mockStatic(UserIDHelper.class);
        UserIDHelper spy = PowerMockito.spy(new UserIDHelper());
        PowerMockito.when(spy, PowerMockito.method(UserIDHelper.class, "getUidInCache", String.class, String.class))
                .withArguments("userName", "appkey").thenReturn(1L);
        PowerMockito.when(UserIDHelper.getUserIDFromLocal("userName", "appkey")).thenCallRealMethod();
        Assert.assertEquals(1, UserIDHelper.getUserIDFromLocal("userName", "appkey").intValue());
    }

    //getUidInCache(userName, appkey) != 0
    @Test
    public void getUserIDFromLocal_2() throws Exception {
        SQLiteDatabase helper = mock(SQLiteDatabase.class);
        PowerMockito.mockStatic(UserIDHelper.class);
        UserIDHelper spy = PowerMockito.spy(new UserIDHelper());
        PowerMockito.when(spy, PowerMockito.method(UserIDHelper.class, "getUidInCache", String.class, String.class))
                .withArguments("userName", "appkey").thenReturn(1L);
        PowerMockito.when(UserIDHelper.getUserIDFromLocal(helper, "userName", "appkey")).thenCallRealMethod();
        Assert.assertEquals(1, UserIDHelper.getUserIDFromLocal(helper, "userName", "appkey").intValue());
    }


}
