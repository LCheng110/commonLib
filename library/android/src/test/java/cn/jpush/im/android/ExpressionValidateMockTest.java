package cn.jpush.im.android;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.jpush.im.android.utils.ExpressionValidateUtil;
import cn.jpush.im.android.utils.StringUtils;

/**
 * Created by ${chenyn} on 16/5/20.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(IMConfigs.class)
public class ExpressionValidateMockTest {

    //null != password && password.equals(originPassword) == true
    @Test
    public void currentUserPasswordTest_1() {
        PowerMockito.mockStatic(IMConfigs.class);
        PowerMockito.when(IMConfigs.getUserPassword()).thenReturn(StringUtils.toMD5("abc"));
        String originPassword = "abc";

        Assert.assertTrue(ExpressionValidateUtil.validCurrentUserPassword(originPassword));
    }

    //null == password && password.equals(originPassword) == true
    @Test
    public void currentUserPasswordTest_2() {
        PowerMockito.mockStatic(IMConfigs.class);
        PowerMockito.when(IMConfigs.getUserPassword()).thenReturn(null);
        String originPassword = "abc";

        Assert.assertFalse(ExpressionValidateUtil.validCurrentUserPassword(originPassword));
    }

    //null != password && password.equals(originPassword) == false
    @Test
    public void currentUserPasswordTest_3() {
        PowerMockito.mockStatic(IMConfigs.class);
        PowerMockito.when(IMConfigs.getUserPassword()).thenReturn("abc");
        String originPassword = "abc";

        Assert.assertFalse(ExpressionValidateUtil.validCurrentUserPassword(originPassword));
    }

}
