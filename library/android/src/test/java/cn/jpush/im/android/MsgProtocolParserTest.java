package cn.jpush.im.android;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.MessageProtocolParser;

import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Created by ${chenyn} on 16/5/25.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class MsgProtocolParserTest {

    //InternalMessage == null
    @Test
    public void messageToProtocol_1() {
        InternalMessage mMessage = null;

        String protocol = MessageProtocolParser.messageToProtocol(mMessage);
        Assert.assertEquals(null, protocol);
    }

    //InternalMessage != null
    @Test
    public void messageToProtocol_2() {
        InternalMessage mMessage = mock(InternalMessage.class);
        when(mMessage.toJson()).thenReturn("test");
        String protocol = MessageProtocolParser.messageToProtocol(mMessage);
        Assert.assertEquals("test", protocol);
    }

    //TextUtils.isEmpty(protocol) == true
//    @Test
//    public void protocolToMessage_1() {
//        String protocol = "";
//        long createTime = 123L;
//        long msgId = 456L;
//
//        Message message = MessageProtocolParser.protocolToMessage(protocol, MessageDirect.send, createTime, msgId);
//        Assert.assertEquals(null, message);
//    }

    //TextUtils.isEmpty(protocol) == false  Message = null
//    @Test
//    public void protocolToMessage() {
//        String protocol = "test";
//        long createTime = 123L;
//        long msgId = 456L;
//
//        Message message = MessageProtocolParser.protocolToMessage(protocol, MessageDirect.send, createTime, msgId);
//        Assert.assertEquals(null, message);
//    }

    //InternalMessage == null
    @Test
    public void saveMsgToLocal() {
        InternalMessage internalMessage = null;
        Message message = MessageProtocolParser.saveMsgToLocal(internalMessage);
        Assert.assertEquals(null, message);
    }
}


