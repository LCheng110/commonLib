package cn.jpush.im.android.internalmldel;

import android.database.sqlite.SQLiteException;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.IMConfigs;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageStatus;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.storage.MessageStorage;
import cn.jpush.im.android.utils.CommonUtils;

import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;

/**
 * Created by ${chenyn} on 16/5/30.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonUtils.class, MessageStorage.class, IMConfigs.class, MessageContent.class})
public class InternalConversationTest {

    //targetAppKey = ""
    @Test
    public void getTargetAppKey_1() {
        InternalConversation conversation = new InternalConversation();
        String targetAppKey = conversation.getTargetAppKey();
        Assert.assertEquals("", targetAppKey);
    }

    //targetAppKey = 123
    @Test
    public void getTargetAppKey_2() {
        InternalConversation t = new ForTestOne();
        String appKey = t.getTargetAppKey();
        Assert.assertEquals("123", appKey);
    }

    //targetAppKey = null && type = ConversationType.group
    @Test
    public void getTargetAppKey_3() {
        InternalConversation t = new ForTestTwo();
        String appKey = t.getTargetAppKey();
        Assert.assertEquals("", appKey);
    }

    //CommonUtils.doInitialCheckWithoutNetworkCheck("setUnReadMsgCnt", null, null) = false && conut >0
    @Test
    public void setUnReadMessageCnt_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("setUnReadMsgCnt", null)).thenReturn(false);
        InternalConversation conversation = new InternalConversation();
        boolean b = conversation.setUnReadMessageCnt(99);
        Assert.assertEquals(false, b);
    }

    //CommonUtils.doInitialCheckWithoutNetworkCheck("setUnReadMsgCnt", null, null) = true && conut < 0
    @Test
    public void setUnReadMessageCnt_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("setUnReadMsgCnt", null)).thenReturn(true);
        InternalConversation conversation = new InternalConversation();
        boolean b = conversation.setUnReadMessageCnt(-99);
        Assert.assertEquals(false, b);
    }

    //CommonUtils.doInitialCheckWithoutNetworkCheck("setUnReadMsgCnt", null, null) = true && conut >0
    @Test
    public void setUnReadMessageCnt_3() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("setUnReadMsgCnt", null)).thenReturn(true);
        InternalConversation conversation = new InternalConversation();
        boolean b = conversation.setUnReadMessageCnt(99);
        Assert.assertEquals(true, b);
    }

    //CommonUtils.doInitialCheckWithoutNetworkCheck("getMessage", null, null) == false
    @Test
    public void getMessage_1() {
        InternalConversation conversation = new InternalConversation();
        int messageId = 1;
        Message message = conversation.getMessage(messageId);
        Assert.assertEquals(null, message);
    }

    //查询信息不存在
    @Test
    public void getMessage_2() {
        InternalConversation conversation = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        int messageId = 1;
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getMessage", null)).thenReturn(true);
        Message message = conversation.getMessage(messageId);
        Assert.assertEquals(null, message);
    }

    //CommonUtils.doInitialCheckWithoutNetworkCheck("getLatestMessage", null, null) = false
    @Test
    public void getLatestMessage_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getLatestMessage", null)).thenReturn(false);
        InternalConversation conversation = new InternalConversation();
        Assert.assertNull(conversation.getLatestMessage());
    }

    //CommonUtils.doInitialCheckWithoutNetworkCheck("getLatestMessage", null, null) = true
    @Test
    public void getLatestMessage_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getLatestMessage", null)).thenReturn(true);
        InternalConversation conversation = new InternalConversation();
        Assert.assertNull(conversation.getLatestMessage());
    }

    //MessageStorage.delete(messageId, msgTableName) == false
    @Test
    public void deleteMessage_1() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("deleteMessage", null)).thenReturn(true);
        boolean b = info.deleteMessage(1);
        Assert.assertEquals(false, b);
    }

    //MessageStorage.delete(messageId, msgTableName) == true
    @Test
    public void deleteMessage_2() {
        PowerMockito.mockStatic(MessageStorage.class);
        PowerMockito.when(MessageStorage.deleteSync(1, null)).thenReturn(true);
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("deleteMessage", null)).thenReturn(true);
        boolean b = info.deleteMessage(1);
        Assert.assertEquals(true, b);
    }

    //主动抛出异常
    @Test
    public void deleteMessage_3() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("deleteMessage", null)).thenReturn(true);
        PowerMockito.mockStatic(MessageStorage.class);
        PowerMockito.when(MessageStorage.deleteSync(anyInt(), anyString())).thenThrow(new SQLiteException());
        InternalConversation info = new InternalConversation();
        boolean b = info.deleteMessage(1);
        Assert.assertEquals(false, b);
    }

    //MessageStorage.deleteAll(msgTableName) == false
    @Test
    public void deleteAllMessage_1() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("deleteAllMessage", null)).thenReturn(true);
        boolean b = info.deleteAllMessage();
        Assert.assertEquals(false, b);
    }

    //MessageStorage.deleteAll(msgTableName) == true
    @Test
    public void deleteAllMessage_2() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("deleteAllMessage", null)).thenReturn(true);

        PowerMockito.mockStatic(MessageStorage.class);
        PowerMockito.when(MessageStorage.deleteAllSync(anyString())).thenReturn(true);

        boolean b = info.deleteAllMessage();
        Assert.assertEquals(true, b);
    }

    //抛异常情况
    @Test
    public void deleteAllMessage_3() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("deleteAllMessage", null)).thenReturn(true);

        PowerMockito.mockStatic(MessageStorage.class);
        PowerMockito.when(MessageStorage.deleteAllSync(anyString())).thenThrow(new SQLiteException());

        boolean b = info.deleteAllMessage();
        Assert.assertEquals(false, b);
    }

    //msg = null
    @Test
    public void updateMessageExtra_String_1() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(null, "key", "value");
        Assert.assertEquals(false, extra);
    }

    //null != msg || null != msg.getContent() || null != key || null != value
    @Test
    public void updateMessageExtra_String_2() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);

        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        MessageContent content = PowerMockito.mock(MessageContent.class);
        PowerMockito.when(msg.getContent()).thenReturn(content);

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(msg, "key", "value");
        Assert.assertEquals(false, extra);
    }

    //抛异常情况
    @Test
    public void updateMessageExtra_String_3() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);

        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getContent()).thenThrow(new SQLiteException());

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(msg, "key", "value");
        Assert.assertEquals(false, extra);
    }

    //msg = null
    @Test
    public void updateMessageExtra_number_1() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(null, "key", 1);
        Assert.assertEquals(false, extra);
    }

    //null != msg || null != msg.getContent() || null != key || null != value
    @Test
    public void updateMessageExtra_number_2() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);

        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        MessageContent content = PowerMockito.mock(MessageContent.class);
        PowerMockito.when(msg.getContent()).thenReturn(content);

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(msg, "key", 2);
        Assert.assertEquals(false, extra);
    }

    //抛异常情况
    @Test
    public void updateMessageExtra_number_3() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);

        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getContent()).thenThrow(new SQLiteException());

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(msg, "key", 3);
        Assert.assertEquals(false, extra);
    }

    //msg = null
    @Test
    public void updateMessageExtra_boolean_1() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(null, "key", true);
        Assert.assertEquals(false, extra);
    }

    //null != msg || null != msg.getContent() || null != key || null != value
    @Test
    public void updateMessageExtra_boolean_2() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);

        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        MessageContent content = PowerMockito.mock(MessageContent.class);
        PowerMockito.when(msg.getContent()).thenReturn(content);

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(msg, "key", true);
        Assert.assertEquals(false, extra);
    }

    //抛异常情况
    @Test
    public void updateMessageExtra_boolean_3() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);

        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getContent()).thenThrow(new SQLiteException());

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtra", null)).thenReturn(true);
        boolean extra = info.updateMessageExtra(msg, "key", true);
        Assert.assertEquals(false, extra);
    }

    //msg = null
    @Test
    public void updateMessageExtra_map_1() {
        InternalConversation info = new InternalConversation();
        Map map = new HashMap();
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtras", null)).thenReturn(true);
        boolean extra = info.updateMessageExtras(null, map);
        Assert.assertEquals(false, extra);
    }

    //null != msg || null != msg.getContent() || null != key || null != value
    @Test
    public void updateMessageExtra_map_2() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        Map map = new HashMap();

        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        MessageContent content = PowerMockito.mock(MessageContent.class);
        PowerMockito.when(msg.getContent()).thenReturn(content);

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtras", null)).thenReturn(true);
        boolean extra = info.updateMessageExtras(msg, map);
        Assert.assertEquals(false, extra);
    }

    //抛异常情况
    @Test
    public void updateMessageExtra_map_3() {
        InternalConversation info = new InternalConversation();
        PowerMockito.mockStatic(CommonUtils.class);
        Map map = new HashMap();
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getContent()).thenThrow(new SQLiteException());

        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageExtras", null)).thenReturn(true);
        boolean extra = info.updateMessageExtras(msg, map);
        Assert.assertEquals(false, extra);
    }

    //正常情况
    @Test
    public void updateMessageContent_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageContent", null)).thenReturn(true);
        InternalConversation info = new InternalConversation();
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        MessageContent content = PowerMockito.mock(MessageContent.class);
        PowerMockito.when(content.getContentType()).thenReturn(ContentType.text);
        boolean b = info.updateMessageContent(msg, content);
        Assert.assertEquals(false, b);
    }

    //msg = null
    @Test
    public void updateMessageContent_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageContent", null)).thenReturn(true);
        InternalConversation info = new InternalConversation();
        InternalMessage msg = null;
        MessageContent content = PowerMockito.mock(MessageContent.class);
        boolean b = info.updateMessageContent(msg, content);
        Assert.assertEquals(false, b);
    }

    //抛异常情况
    @Test
    public void updateMessageContent_3() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageContent", null)).thenReturn(true);
        InternalConversation info = new InternalConversation();
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getId()).thenThrow(new SQLiteException());
        MessageContent content = PowerMockito.mock(MessageContent.class);
        PowerMockito.when(content.getContentType()).thenReturn(ContentType.text);
        boolean b = info.updateMessageContent(msg, content);
        Assert.assertEquals(false, b);
    }

    //正常情况
    @Test
    public void updateMessageStatus_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageStatus", null)).thenReturn(true);
        InternalConversation info = new InternalConversation();
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        MessageStatus status = MessageStatus.created;
        boolean b = info.updateMessageStatus(msg, status);
        Assert.assertEquals(false, b);
    }

    //msg = null
    @Test
    public void updateMessageStatus_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageStatus", null)).thenReturn(true);
        InternalConversation info = new InternalConversation();
        InternalMessage msg = null;
        MessageStatus status = MessageStatus.created;
        boolean b = info.updateMessageStatus(msg, status);
        Assert.assertEquals(false, b);
    }

    //抛异常情况
    @Test
    public void updateMessageStatus_3() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageStatus", null)).thenReturn(true);
        InternalConversation info = new InternalConversation();
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getId()).thenThrow(new SQLiteException());
        MessageStatus status = MessageStatus.created;
        boolean b = info.updateMessageStatus(msg, status);
        Assert.assertEquals(false, b);
    }

    //正常情况
    @Test
    public void updateMessageServerMsgId_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageServerMsgId", null)).thenReturn(true);
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        Long serverMsgId = 123L;
        InternalConversation info = new InternalConversation();
        boolean b = info.updateMessageServerMsgId(msg, serverMsgId);
        Assert.assertEquals(false, b);
    }

    //msg = null
    @Test
    public void updateMessageServerMsgId_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageServerMsgId", null)).thenReturn(true);
        InternalMessage msg = null;
        Long serverMsgId = 123L;
        InternalConversation info = new InternalConversation();
        boolean b = info.updateMessageServerMsgId(msg, serverMsgId);
        Assert.assertEquals(false, b);
    }

    //抛出异常情况
    @Test
    public void updateMessageServerMsgId_3() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageServerMsgId", null)).thenReturn(true);
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getId()).thenThrow(new SQLiteException());
        Long serverMsgId = 123L;
        InternalConversation info = new InternalConversation();
        boolean b = info.updateMessageServerMsgId(msg, serverMsgId);
        Assert.assertEquals(false, b);
    }

    //serverMsgId < 0
    @Test
    public void updateMessageServerMsgId_4() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("updateMessageServerMsgId", null)).thenReturn(true);
        InternalMessage msg = PowerMockito.mock(InternalMessage.class);
        PowerMockito.when(msg.getId()).thenThrow(new SQLiteException());
        Long serverMsgId = -123L;
        InternalConversation info = new InternalConversation();
        boolean b = info.updateMessageServerMsgId(msg, serverMsgId);
        Assert.assertEquals(false, b);
    }

}

class ForTestOne extends InternalConversation {
    public ForTestOne() {
        super();
        targetAppKey = "123";
    }
}

class ForTestTwo extends InternalConversation {
    public ForTestTwo() {
        super();
        targetAppKey = null;
        type = ConversationType.group;
    }
}

class ForTestThree extends InternalConversation {
    public ForTestThree() {
        super();
        targetAppKey = null;
        type = ConversationType.single;
    }
}
