package cn.jpush.im.android;

import com.loopj.android.jpush.http.ResponseHandlerInterface;
import com.loopj.android.jpush.http.SyncHttpClient;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.powermock.reflect.Whitebox;

import java.io.File;

import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.android.utils.StringUtils;
import cn.jpush.im.android.utils.filemng.AvatarDownloader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest(StringUtils.class)
public class AvatarDownloadTest extends BaseTest {

    private static final String TAG = "AvatarDownloadTest";

    @Before
    public void prepare() {

    }

    /**
     * #################    测试AvatarDownloader－downloadSmallAvatar    #################
     */
    //qiniu下载small图片
    @Test
    public void downloadSmallAvatar_1() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn("qiniu/image/B8C68B55D34B3BC9");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(0, responseCode);
                assertEquals("Success", responseMessage);
                assertNotNull(avatar);
            }
        };

        SyncHttpClient httpClient = mock(SyncHttpClient.class);
        when(httpClient.get(anyString(), any(ResponseHandlerInterface.class))).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                callback.gotResult(0, "Success", new File(""));
                return null;
            }
        });

        AvatarDownloader avatarDownloader = new AvatarDownloader();
        CommonUtils.setSyncHttpClient(httpClient);
        avatarDownloader.downloadSmallAvatar(info.getAvatar(), callback);
    }

    //随意给mediaID
    @Test
    public void downloadSmallAvatar_2() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn("asd/ff");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS, responseCode);
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC, responseMessage);
                assertNull(avatar);
            }
        };

        AvatarDownloader avatarDownloader = new AvatarDownloader();
        avatarDownloader.downloadSmallAvatar(info.getAvatar(), callback);
    }

    //mediaID给一个空字符串
    @Test
    public void downloadSmallAvatar_3() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn(" ");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS, responseCode);
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC, responseMessage);
                assertNull(avatar);
            }
        };

        AvatarDownloader avatarDownloader = new AvatarDownloader();
        avatarDownloader.downloadSmallAvatar(info.getAvatar(), callback);
    }

    //provider == null && mediaID != null
    @Test
    public void downloadSmallAvatar_4() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn(" ");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS, responseCode);
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC, responseMessage);
                assertNull(avatar);
            }
        };
        PowerMockito.mockStatic(StringUtils.class);
        AvatarDownloader avatarDownloader = new AvatarDownloader();
        when(StringUtils.getProviderFromMediaID(" ")).thenReturn(null);
        avatarDownloader.downloadSmallAvatar(info.getAvatar(), callback);
    }

    /**
     * #################    测试AvatarDownloader－downloadBigAvatar    #################
     */
    //qiniu下载大图片
    @Test
    public void downloadBigAvatar_1() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn("qiniu/image/B8C68B55D34B3BC9");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(0, responseCode);
                assertEquals("Success", responseMessage);
                assertNotNull(avatar);
            }
        };

        SyncHttpClient httpClient = mock(SyncHttpClient.class);
        when(httpClient.get(anyString(), any(ResponseHandlerInterface.class))).then(new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                callback.gotResult(0, "Success", new File(""));
                return null;
            }
        });

        AvatarDownloader avatarDownloader = new AvatarDownloader();
        CommonUtils.setSyncHttpClient(httpClient);
        avatarDownloader.downloadBigAvatar(info.getAvatar(), callback);
    }

    //随意给mediaID下载大图片
    @Test
    public void downloadBigAvatar_2() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn("asd/ff");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS, responseCode);
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC, responseMessage);
                assertNull(avatar);
            }
        };

        AvatarDownloader avatarDownloader = new AvatarDownloader();
        avatarDownloader.downloadBigAvatar(info.getAvatar(), callback);
    }

    //mediaID给一个空字符串
    @Test
    public void downloadBigAvatar_3() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn(" ");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS, responseCode);
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC, responseMessage);
                assertNull(avatar);
            }
        };

        AvatarDownloader avatarDownloader = new AvatarDownloader();
        avatarDownloader.downloadBigAvatar(info.getAvatar(), callback);
    }

    //provider == null && mediaID != null
    @Test
    public void downloadBigAvatar_4() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn(" ");

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS, responseCode);
                assertEquals(ErrorCode.HTTP_ERROR.HTTP_INVALID_PARAMETERS_DESC, responseMessage);
                assertNull(avatar);
            }
        };
        PowerMockito.mockStatic(StringUtils.class);
        AvatarDownloader avatarDownloader = new AvatarDownloader();
        when(StringUtils.getProviderFromMediaID(" ")).thenReturn(null);
        avatarDownloader.downloadBigAvatar(info.getAvatar(), callback);
    }

    /**
     * #################    测试AvatarDownloader－needDownload    #################
     */
    //isSmallAvatar == true ; avatarFile.exists() = false
    @Test
    public void needDownload_1() throws Exception {
        AvatarDownloader downloader = new AvatarDownloader();
        boolean flag = Whitebox.<Boolean>invokeMethod(downloader, "needDownload", "path", true);
        assertEquals(true, flag);
    }

    //isSmallAvatar == false ; bigAvatarFile.exists() = false
    @Test
    public void needDownload_2() throws Exception {
        AvatarDownloader downloader = new AvatarDownloader();
        boolean flag = Whitebox.<Boolean>invokeMethod(downloader, "needDownload", "path", false);
        assertEquals(true, flag);
    }

    /**
     * #################    测试AvatarDownloader－duplicateDownloadCheck    #################
     */
    //containsMediaId(mediaID, isSmallAvatar) == false
    @Test
    public void duplicateDownloadCheck() throws Exception {
        AvatarDownloader downloader = new AvatarDownloader();
        boolean flag = Whitebox.<Boolean>invokeMethod(downloader, "duplicateDownloadCheck", "path", true);
        assertEquals(false, flag);
    }
}
