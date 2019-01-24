package cn.jpush.im.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.internalmodel.InternalConversation;
import cn.jpush.im.android.utils.StringUtils;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNotSame;

/**
 * Created by ${chenyn} on 16/5/25.
 *
 * @desc :
 * @parame :
 * @return :
 */
public class StringUtilTest extends BaseTest {

    //null != params && string[].length == 2
    @Test
    public void getDownloadUrlForQiniu_1() {
        String mediaID = "test";
        String[] params = {"1", "2"};

        String url = StringUtils.getDownloadUrlForQiniu(mediaID, params);
        assertEquals("http://fmedia.im.jiguang.cn/test?1/2", url);
    }

    //null != params && string[].length == 0
    @Test
    public void getDownloadUrlForQiniu_2() {
        String mediaID = "test";
        String[] params = new String[0];

        String url = StringUtils.getDownloadUrlForQiniu(mediaID, params);
        assertEquals("http://fmedia.im.jiguang.cn/test", url);
    }

    //null != params && string[].length == 1
    @Test
    public void getDownloadUrlForQiniu_3() {
        String mediaID = "test";
        String[] params = new String[]{"1"};

        String url = StringUtils.getDownloadUrlForQiniu(mediaID, params);
        assertEquals("http://fmedia.im.jiguang.cn/test?1", url);
    }

    //null != params && string[] == null
    @Test
    public void getDownloadUrlForQiniu_4() {
        String mediaID = "test";
        String[] params = null;

        String url = StringUtils.getDownloadUrlForQiniu(mediaID, params);
        assertEquals("http://fmedia.im.jiguang.cn/test", url);
    }

    //thumbSuffix != null
    @Test
    public void getDownloadUrlForUpyun_1() {
        String mediaId = "mediaId";
        String bucket = "bucket";
        String thumbSuffix = "thumbSuffix";

        String url = StringUtils.getDownloadUrlForUpyun(mediaId, bucket, thumbSuffix);
        assertEquals("http://bucket.b0.upaiyun.com/mediaId!thumbSuffix", url);
    }

    //thumbSuffix == null
    @Test
    public void getDownloadUrlForUpyun_2() {
        String mediaId = "mediaId";
        String bucket = "bucket";
        String thumbSuffix = null;

        String url = StringUtils.getDownloadUrlForUpyun(mediaId, bucket, thumbSuffix);
        assertEquals("http://bucket.b0.upaiyun.com/mediaId", url);
    }

    //strings.length >= 1
    @Test
    public void getResourceIDFromMediaID_1() {
        String mediaId = "mediaId";
        String id = StringUtils.getResourceIDFromMediaID(mediaId);
        assertEquals("mediaId", id);
    }

    //strings.length == 0
    @Test
    public void getResourceIDFromMediaID_2() {
        String mediaId = "/";
        String id = StringUtils.getResourceIDFromMediaID(mediaId);
        assertEquals(null, id);
    }

    //host != null && host.equals("https")==false
    @Test
    public void isSSL_1() {
        String url = "testurl:url";
        boolean b = StringUtils.isSSL(url);
        assertEquals(false, b);
    }

    //host != null && host.equals("https")==true
    @Test
    public void isSSL_2() {
        String url = "https:url";
        boolean b = StringUtils.isSSL(url);
        assertEquals(true, b);
    }

    //str != null
    @Test
    public void isTextEmpty_1() {
        CharSequence str = "test";
        boolean b = StringUtils.isTextEmpty(str);
        assertEquals(false, b);
    }

    //str == null
    @Test
    public void isTextEmpty_2() {
        CharSequence str = null;
        boolean b = StringUtils.isTextEmpty(str);
        assertEquals(true, b);
    }

    //str.length = 0
    @Test
    public void isTextEmpty_3() {
        CharSequence str = "";
        boolean b = StringUtils.isTextEmpty(str);
        assertEquals(true, b);
    }

    //keys == null
    @Test
    public void createSelectionWithAnd_1() {
        String[] key = null;
        String selection = StringUtils.createSelectionWithAnd(key);
        assertEquals(null, selection);
    }

    //keys.length = 1
    @Test
    public void createSelectionWithAnd_2() {
        String[] key = {"test"};
        String selection = StringUtils.createSelectionWithAnd(key);
        assertEquals("test=?", selection);
    }

    //keys.length = 2
    @Test
    public void createSelectionWithAnd_3() {
        String[] key = {"test1", "test2"};
        String selection = StringUtils.createSelectionWithAnd(key);
        assertEquals("test1=? and test2=?", selection);
    }

    @Test
    public void createSQLStringFromIterator_normal() {
        List<InternalConversation> list = new ArrayList<InternalConversation>();
        for (int i = 0; i < 5; i++) {
            InternalConversation singleConversation = new InternalConversation();
            singleConversation.setType(ConversationType.single);
            singleConversation.setTargetId("single " + i);
            singleConversation.setTargetAppKey("appkey " + i);
            list.add(singleConversation);

            InternalConversation groupConversation = new InternalConversation();
            groupConversation.setType(ConversationType.group);
            groupConversation.setTargetId("group " + i);
            groupConversation.setTargetAppKey("");
            list.add(groupConversation);
        }
        StringBuffer stringBuffer = new StringBuffer();
        StringUtils.createSQLStringFromIterator(stringBuffer, list.iterator());

        assertNotNull(stringBuffer.toString());
        assertNotSame("", stringBuffer.toString());
    }

    @Test
    public void createSQLStringFromIterator_invalidParam() {
        //sqlString is null
        StringUtils.createSQLStringFromIterator(null, new ArrayList<InternalConversation>().iterator());

        //iterator is null
        StringBuffer stringBuffer = new StringBuffer();
        StringUtils.createSQLStringFromIterator(stringBuffer, null);

        //iterator is empty
        StringUtils.createSQLStringFromIterator(stringBuffer, new ArrayList<InternalConversation>().iterator());

    }
}
