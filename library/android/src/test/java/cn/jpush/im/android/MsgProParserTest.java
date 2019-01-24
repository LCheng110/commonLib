package cn.jpush.im.android;

import com.google.gson.jpush.reflect.TypeToken;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.MessageProtocolParser;

/**
 * Created by ${chenyn} on 16/5/26.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(JsonUtil.class)
public class MsgProParserTest {

    //internalMessage == null
    @Test
    public void protocolToInternalMessage() {
        String protocol = "test";
        long createTime = 123L;
        long msgId = 456L;
        boolean sendEvent = false;

        PowerMockito.mockStatic(JsonUtil.class);
        PowerMockito.when(JsonUtil.formatToGivenTypeOnlyWithExpose(protocol, new TypeToken<Class<InternalMessage>>() {
        })).thenReturn(null);

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, createTime, msgId, 0, 0, null, sendEvent);
        Assert.assertEquals(null, internalMessage);
    }
}
