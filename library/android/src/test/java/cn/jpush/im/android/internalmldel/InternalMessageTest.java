package cn.jpush.im.android.internalmldel;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.CommonUtils;

/**
 * Created by ${chenyn} on 16/6/6.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonUtils.class})
public class InternalMessageTest {

    //fromUser = null  fromID+fromID.equals(fromName) = false
    @Test
    public void getFromUser_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getFromUser", null)).thenReturn(true);

        InternalMessage im = new InternalMessage(MessageDirect.send, new TextContent("1"), "fromID", "fromAppKey", "name", "targetId", "targetAppkey", "targetName", null, null);
        UserInfo fromUser = im.getFromUser();
        Assert.assertEquals("fromID", fromUser.getUserName());
        Assert.assertEquals(-1, fromUser.getStar());
        Assert.assertEquals("name", fromUser.getNickname());
    }

    //fromUser = null  fromID+fromID.equals(fromName) = true
    @Test
    public void getFromUser_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getFromUser", null)).thenReturn(true);

        InternalMessage im = new InternalMessage(MessageDirect.send, new TextContent("1"), "fromID", "fromAppKey", "fromID", "targetId", "targetAppkey", "targetName", null, null);
        UserInfo fromUser = im.getFromUser();
        Assert.assertEquals("fromID", fromUser.getUserName());
        Assert.assertEquals(-1, fromUser.getStar());
        Assert.assertEquals("", fromUser.getNickname());
    }


    //fromUser != null
    @Test
    public void getFromUser_3() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getFromUser", null)).thenReturn(true);
        InternalMessage im = new InternalMessage(MessageDirect.send, new TextContent("1"), "fromID", "fromAppKey", "name", "targetId", "targetAppkey", "targetName", null, null);

        UserInfo info = PowerMockito.mock(UserInfo.class);
        info.setNickname("infosetName");
        im.setFromUser(info);

        UserInfo fromUser = im.getFromUser();
        Assert.assertEquals("userInfo", fromUser.toString());
    }
}
