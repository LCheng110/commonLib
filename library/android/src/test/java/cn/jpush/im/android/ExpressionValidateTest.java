package cn.jpush.im.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import cn.jpush.im.android.api.content.MessageContent;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.utils.ExpressionValidateUtil;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class ExpressionValidateTest {
    private static final String TAG = "ExpressionValidateTest";

    @Test
    public void usernameTest() {
        assertTrue(ExpressionValidateUtil.validUserName("aaaa"));
        assertTrue(ExpressionValidateUtil.validUserName("33223"));
        assertTrue(ExpressionValidateUtil.validUserName("33rrs_@.--__@@"));

        assertFalse(ExpressionValidateUtil.validUserName("a"));
        assertFalse(ExpressionValidateUtil.validUserName("@fre"));
        assertFalse(ExpressionValidateUtil.validUserName(".sfff"));
        assertFalse(ExpressionValidateUtil.validUserName("_fewfwe"));
        assertFalse(ExpressionValidateUtil.validUserName("-frerf"));
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            stringBuilder.append(i);
        }
        assertFalse(ExpressionValidateUtil.validUserName(stringBuilder.toString()));
    }

    @Test
    public void passwordTest() {
        //TextUtils.isEmpty(password) == true
        assertFalse(ExpressionValidateUtil.validPassword(""));
        assertFalse(ExpressionValidateUtil.validPassword(null));

        //TextUtils.isEmpty(password) == false ; password长度小于4
        assertFalse(ExpressionValidateUtil.validPassword(" "));

        //TextUtils.isEmpty(password) == false ; password长度大于128
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 200; i++) {
            stringBuilder.append(i);
        }
        assertFalse(ExpressionValidateUtil.validPassword(stringBuilder.toString()));

        //TextUtils.isEmpty(password) == false ; password长度大于4 小于128
        assertTrue(ExpressionValidateUtil.validPassword("    "));


    }

    @Test
    public void otherNamesTest() {
        //name == null
        assertFalse(ExpressionValidateUtil.validOtherNames(null));

        //name length > 64
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 70; i++) {
            stringBuilder.append(i);
        }
        assertFalse(ExpressionValidateUtil.validOtherNames(stringBuilder.toString()));

        //name length < 64
        assertTrue(ExpressionValidateUtil.validOtherNames(" "));
        assertTrue(ExpressionValidateUtil.validOtherNames(""));
        assertTrue(ExpressionValidateUtil.validOtherNames("abc"));
    }

    @Test
    public void othersTest() {
        //text == null
        assertFalse(ExpressionValidateUtil.validOthers(null));

        //text != null ; text.length = 0 ; text.length < 250
        assertTrue(ExpressionValidateUtil.validOthers(""));

        //text != null ; text.length != 0 ; text.length > 250
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 300; i++) {
            stringBuilder.append(i);
        }
        assertFalse(ExpressionValidateUtil.validOthers(stringBuilder.toString()));
    }

    @Test
    public void messageLengthTest_1() {
        //message == null
        assertFalse(ExpressionValidateUtil.validMessageLength(null));

        //message != null ; message.getContent.toJson.getBytes.length > 1024*4
        Message message = mock(Message.class);
        MessageContent content = mock(MessageContent.class);
        when(message.getContent()).thenReturn(content);
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 1024 * 5; i++) {
            stringBuilder.append(i);
        }
        when(message.getContent().toJson()).thenReturn(stringBuilder.toString());
        assertFalse(ExpressionValidateUtil.validMessageLength(message));
    }

    @Test
    public void messageLengthTest_2() {
        //message == null
        assertFalse(ExpressionValidateUtil.validMessageLength(null));

        //message != null ; message.getContent.toJson.getBytes.length < 1024*4
        Message message = mock(Message.class);
        MessageContent content = mock(MessageContent.class);
        when(message.getContent()).thenReturn(content);
        when(message.getContent().toJson()).thenReturn(" ");
        assertTrue(ExpressionValidateUtil.validMessageLength(message));
    }

    @Test
    public void mediaIDTest() {
        //media == null
        assertFalse(ExpressionValidateUtil.validMediaID(null));

        //media = ""
        assertFalse(ExpressionValidateUtil.validMediaID(""));

        //media == "qiniu"
        assertFalse(ExpressionValidateUtil.validMediaID("qiniu"));

        //media == "qiniu/aaa"
        assertFalse(ExpressionValidateUtil.validMediaID("qiniu/aaa"));

        //media = "qiniu/aaa/dd"
        assertTrue(ExpressionValidateUtil.validMediaID("qiniu/aaa/dd"));

        assertTrue(ExpressionValidateUtil.validMediaID("qiniu/aaa/dd/sss"));

        assertTrue(ExpressionValidateUtil.validMediaID("qiniu/aaa/dd/sss/ds32d"));

        //media == "upyun"
        assertFalse(ExpressionValidateUtil.validMediaID("upyun"));

        //media == "upyun/aaa"
        assertFalse(ExpressionValidateUtil.validMediaID("upyun/aaa"));

        //media = "upyun/aaa/dd"
        assertTrue(ExpressionValidateUtil.validMediaID("upyun/aaa/dd"));

        assertTrue(ExpressionValidateUtil.validMediaID("upyun/aaa/dd/sss"));

        assertTrue(ExpressionValidateUtil.validMediaID("upyun/aaa/dd/sss/ds32d"));
    }
}
