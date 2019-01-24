package cn.jpush.im.android;

import com.google.gson.jpush.JsonElement;
import com.google.gson.jpush.JsonSyntaxException;

import junit.framework.Assert;

import org.junit.AfterClass;
import org.junit.Test;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.event.MessageEvent;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.MessageProtocolParser;

public class MessageParseTest extends BaseTest {

    private static final String TAG = "MessageParseTest";

    int mVersion = 1;
    String mTextMsgType = ContentType.text.toString();
    String mVoiceMsgType = ContentType.voice.toString();
    String mTargetType = ConversationType.single.toString();
    int mCreateTimeInSec = 1451013888;
    String mFromAppkey = "4f7aef34fb361292c566a1cd";
    String mFromId = "nnnnn";
    String mFromName = "custom_name";
    String mFromType = "user";
    String mFromPlatform = "a";
    String mTextMsgBody = "{\"text\":\"hello ! this message is from nnnnn\",\"extras\":{}}";
    String mVoiceMsgBody = "{\"media_id\":11111111,\"media_crc32\":2222222.11222,\"duration\":11.223" +
            ",\"format\":\"mp3\",\"fsize\":2233.442,\"extras\":{}}";
    String mTargetAppkey = "426251cac0146ce0a08ca38f";
    String mTargetId = "kkkkk";
    String mTargetName = "\"\"";
    long mServerMsgId = 111;

    static int expectedErrorCount;
    static int actualErrorCount;

    @Test
    public void test_normal_parse() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        fullAssertMessage(internalMessage, mTextMsgType);
        MessageContent content = internalMessage.getContent();
        TextContent textContent = ((TextContent) content);
        Assert.assertEquals("hello ! this message is from nnnnn", textContent.getText());

    }

    @Test
    public void test_error_parse_null_protocol() {
        String protocol = null;

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
    }

    @Test
    public void test_error_parse_empty_protocol() {
        String protocol = "";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
    }

    @Test
    public void test_normal_unknown_msg_type_parse() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + "aaa" + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        simpleAssertMessage(internalMessage, ContentType.unknown.name());
        MessageContent content = internalMessage.getContent();
        Assert.assertNull(content);
        Assert.assertEquals(ContentType.unknown, internalMessage.getContentType());

    }

    @Test
    public void test_normal_parse_voice_message() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mVoiceMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mVoiceMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        fullAssertMessage(internalMessage, mVoiceMsgType);

        MessageContent content = internalMessage.getContent();
        VoiceContent voiceContent = ((VoiceContent) content);
        Assert.assertEquals(11, voiceContent.getDuration());
        Assert.assertEquals(2222222, voiceContent.getCrc().longValue());
        Assert.assertEquals(2233, voiceContent.getFileSize());

    }

    @Test
    public void test_normal_parse_float_version() {
        String protocol =
                "{\"version\":" + "1.9" + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        fullAssertMessage(internalMessage, mTextMsgType);

        MessageContent content = MessageContent.fromJson(internalMessage.getMsgBody(),
                internalMessage.getContentType());
        TextContent textContent = ((TextContent) content);
        Assert.assertEquals("hello ! this message is from nnnnn", textContent.getText());

    }

    @Test
    public void test_normal_parse_no_target_type() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        simpleAssertMessage(internalMessage, mTextMsgType);

        MessageContent content = MessageContent.fromJson(internalMessage.getMsgBody(),
                internalMessage.getContentType());
        TextContent textContent = ((TextContent) content);
        Assert.assertEquals("hello ! this message is from nnnnn", textContent.getText());

    }

    @Test
    public void test_normal_parse_no_from_type() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        simpleAssertMessage(internalMessage, mTextMsgType);

        MessageContent content = MessageContent.fromJson(internalMessage.getMsgBody(),
                internalMessage.getContentType());
        TextContent textContent = ((TextContent) content);
        Assert.assertEquals("hello ! this message is from nnnnn", textContent.getText());

    }

    @Test
    public void test_normal_parse_no_create_time() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        simpleAssertMessage(internalMessage, mTextMsgType);

        MessageContent content = MessageContent.fromJson(internalMessage.getMsgBody(),
                internalMessage.getContentType());
        TextContent textContent = ((TextContent) content);
        Assert.assertEquals("hello ! this message is from nnnnn", textContent.getText());

    }

    @Test
    public void test_normal_parse_no_target_name() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        simpleAssertMessage(internalMessage, mTextMsgType);

        MessageContent content = MessageContent.fromJson(internalMessage.getMsgBody(),
                internalMessage.getContentType());
        TextContent textContent = ((TextContent) content);
        Assert.assertEquals("hello ! this message is from nnnnn", textContent.getText());

    }

    @Test
    public void test_normal_parse_no_from_name() {
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";

        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        simpleAssertMessage(internalMessage, mTextMsgType);

        MessageContent content = MessageContent.fromJson(internalMessage.getMsgBody(),
                internalMessage.getContentType());
        TextContent textContent = ((TextContent) content);
        Assert.assertEquals("hello ! this message is from nnnnn", textContent.getText());

    }


    @Test
    public void test_error_version_not_match() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol =
                "{\"version\":" + "100" + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_error_negative_version() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol =
                "{\"version\":" + "-1" + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_error_version_with_string() {
        JMessageClient.registerEventReceiver(this);
        String protocol =
                "{\"version\":" + "sdbsj" + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";
        try {
            MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);
        } catch (JsonSyntaxException exception) {
            JMessageClient.unRegisterEventReceiver(this);
        }
    }


    @Test
    public void test_error_no_version() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol =
                "{\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_error_no_target_id() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + "," +
                        "\"target_name\":" + mTargetName + "}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_error_no_from_id() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey +
                        ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_error_no_msg_type() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol =
                "{\"version\":" + mVersion + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + ",\"msg_body\":" + mTextMsgBody + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_error_no_msg_body() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol =
                "{\"version\":" + mVersion + ",\"msg_type\":" + mTextMsgType + ",\"target_type\":" + mTargetType + "," +
                        "\"create_time\":" + mCreateTimeInSec + ",\"from_appkey\":" + mFromAppkey + "," +
                        "\"from_id\":" + mFromId + ",\"from_name\":" + mFromName + ",\"from_type\":" + mFromType + "," +
                        "\"from_platform\":" + mFromPlatform + "," +
                        "\"target_appkey\":" + mTargetAppkey + ",\"target_id\":" + mTargetId + "," +
                        "\"target_name\":" + mTargetName + "}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, mCreateTimeInSec * 1000, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_custom_msg() {
        JMessageClient.registerEventReceiver(this);
        String protocol = "{\"version\":1,\"target_type\":\"single\",\"from_platform\":\"web\",\"target_id\":\"tianding_116618945\",\"from_id\":\"tianding_907851886\",\"create_time\":1480401577866,\"msg_type\":\"custom\",\"msg_body\":{\"k1\":\"v1\",\"k2\":\"v2\"},\"target_appkey\":\"355e56d8e18ef23b9e493ffe\",\"from_appkey\":\"355e56d8e18ef23b9e493ffe\",\"from_name\":\"tianding_907851886\",\"set_from_name\":0,\"from_type\":\"user\"}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, 0l, mServerMsgId, 0, 0, null, true);

        Assert.assertNotNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_invalid_target_msg() {
        JMessageClient.registerEventReceiver(this);
        expectedErrorCount++;
        String protocol = "{\"version\":1,\"target_type\":\"group\",\"from_platform\":\"web\",\"target_id\":\"tianding_116618945\",\"from_id\":\"tianding_907851886\",\"create_time\":1480401577866,\"msg_type\":\"custom\",\"msg_body\":{\"k1\":\"v1\",\"k2\":\"v2\"},\"target_appkey\":\"355e56d8e18ef23b9e493ffe\",\"from_appkey\":\"355e56d8e18ef23b9e493ffe\",\"from_name\":\"tianding_907851886\",\"set_from_name\":0,\"from_type\":\"user\"}";
        InternalMessage internalMessage = (InternalMessage) MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, 0l, mServerMsgId, 0, 0, null, true);

        Assert.assertNull(internalMessage);
        JMessageClient.unRegisterEventReceiver(this);
    }

    @Test
    public void test_nested_jsonObject_in_msgbody_protocol() {
        String protocol = "{\"version\":1,\"msg_type\":\"custom\",\"from_type\":\"admin\",\"from_id\":\"admin100\",\"target_type\":\"group\",\"target_id\":\"1000256\",\"msg_body\":{\"type\":1,\"version\":1,\"msg_body\":{\"msg_type\":3,\"title\":\"这是一个分享消息\",\"src_param\":{\"share_id\":\"110\",\"share_name\":\"警察\",\"share_source\":\"icenter.zte.com.cn\",\"share_time\":1503914859541},\"forward\":1,\"service_id\":\"zte-km-icenter-address\"}}}";
        InternalMessage internalMessage = MessageProtocolParser.protocolToInternalMessage(protocol, MessageDirect.receive, 0l, mServerMsgId, 0, 0, null, true);
        Assert.assertNotNull(internalMessage);
    }


    @AfterClass
    public static void error_event_count() {
        Assert.assertEquals(expectedErrorCount, actualErrorCount);
    }


    private void fullAssertMessage(InternalMessage internalMessage, String expectedMsgType) {

        simpleAssertMessage(internalMessage, expectedMsgType);

        JsonElement noti = internalMessage.getNotification();
        String fromType = internalMessage.getFromType();
        ConversationType type = internalMessage.getTargetType();
        String fromName = internalMessage.getFromName();

        Assert.assertNull(noti);
        Assert.assertEquals(mTargetType, type.toString());
        Assert.assertEquals(mFromType, fromType);
        Assert.assertEquals(mFromName, fromName);
    }

    private void simpleAssertMessage(InternalMessage internalMessage, String expectedMsgType) {
        int version = internalMessage.getVersion();
        ContentType contentType = internalMessage.getContentType();
        JsonElement body = internalMessage.getMsgBody();
        String fromAppkey = internalMessage.getFromAppKey();
        String fromId = internalMessage.getFromID();
        long serverMsgId = internalMessage.getServerMessageId();


        Assert.assertNotNull(internalMessage);
        Assert.assertEquals(mVersion, version);
        Assert.assertEquals(expectedMsgType, contentType.toString());
        Assert.assertNotNull(body);
        Assert.assertEquals(mFromId, fromId);
        Assert.assertEquals(mFromAppkey, fromAppkey);
        Assert.assertEquals(mServerMsgId, serverMsgId);
    }


    public void onEvent(MessageEvent messageEvent) {
        Logger.d(TAG, "error code = " + messageEvent.getResponseCode() + " error desc = " + messageEvent.getResponseDesc() + " message = " + messageEvent.getMessage());
        actualErrorCount++;
    }

}
