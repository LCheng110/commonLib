package cn.jpush.im.android.helper;

import junit.framework.Assert;

import org.junit.Test;

import java.io.File;
import java.lang.reflect.Method;

import cn.jpush.im.android.helpers.MessageSendingMaintainer;

/**
 * Created by ${chenyn} on 16/5/27.
 *
 * @desc :
 * @parame :
 * @return :
 */
public class MessageSendingMaintainerTest {

    //appkey != null && TextUtils.isEmpty(targetID) = true
    private static final String USERKEY_SEPERATOR = File.separator;

    @Test
    public void createMsgIdentifier_1() throws Exception {
        String targetID = "test";
        String appkey = "test";
        int msgID = 2;
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("createMsgIdentifier", String.class, String.class, int.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, targetID, appkey, msgID);
        Assert.assertEquals("test" + USERKEY_SEPERATOR + "test" + USERKEY_SEPERATOR + "2", object);
    }

    //appkey == null && TextUtils.isEmpty(targetID) = true
    @Test
    public void createMsgIdentifier_2() throws Exception {
        String targetID = "test";
        String appkey = null;
        int msgID = 2;
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("createMsgIdentifier", String.class, String.class, int.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, targetID, appkey, msgID);
        Assert.assertEquals("", object);
    }

    //appkey == "" && TextUtils.isEmpty(targetID) = true
    @Test
    public void createMsgIdentifier_3() throws Exception {
        String targetID = "test";
        String appkey = "";
        int msgID = 2;
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("createMsgIdentifier", String.class, String.class, int.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, targetID, appkey, msgID);
        Assert.assertEquals("test" + USERKEY_SEPERATOR + "group" + USERKEY_SEPERATOR + "2", object);
    }

    //messageIdentifier = null
    @Test
    public void getTargetIDFromIdentifier_1() throws Exception {
        String messageIdentifier = null;
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getTargetIDFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals(null, object);
    }


    //messageIdentifier = null
    @Test
    public void getTargetIDFromIdentifier_2() throws Exception {
        String messageIdentifier = "test" + USERKEY_SEPERATOR + "messageIdentifier";
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getTargetIDFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals("test", object);
    }

    //messageIdentifier != null
    @Test
    public void getAppkeyFromIdentifier_1() throws Exception {
        String messageIdentifier = "test" + USERKEY_SEPERATOR + "getAppkeyFromIdentifier";
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getAppkeyFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals("getAppkeyFromIdentifier", object);
    }

    //messageIdentifier == null
    @Test
    public void getAppkeyFromIdentifier_2() throws Exception {
        String messageIdentifier = null;
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getAppkeyFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals("", object);
    }

    //messageIdentifier == "group"
    @Test
    public void getAppkeyFromIdentifier_3() throws Exception {
        String messageIdentifier = "getAppkeyFromIdentifier" + USERKEY_SEPERATOR + "group";
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getAppkeyFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals("", object);
    }

    //messageIdentifier == null
    @Test
    public void getMessageIDFromIdentifier_1() throws Exception {
        String messageIdentifier = null;
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getMessageIDFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals(0, object);
    }

    //messageIdentifier != null
    @Test
    public void getMessageIDFromIdentifier_2() throws Exception {
        String messageIdentifier = "test1" + USERKEY_SEPERATOR + "test2" + USERKEY_SEPERATOR + "3" + USERKEY_SEPERATOR + "test4";
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getMessageIDFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals(3, object);
    }

    //messageIdentifier != null
    @Test
    public void getMessageIDFromIdentifier_3() throws Exception {
        String messageIdentifier = "test1" + USERKEY_SEPERATOR + "test2" + USERKEY_SEPERATOR + "test3" + USERKEY_SEPERATOR + "test4";
        Method method = MessageSendingMaintainer.class.getDeclaredMethod("getMessageIDFromIdentifier", String.class);
        method.setAccessible(true);
        Object object = method.invoke(MessageSendingMaintainer.class, messageIdentifier);
        Assert.assertEquals(0, object);
    }
}
