package cn.jpush.im.android;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;

import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.utils.filemng.AvatarDownloader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Created by ${chenyn} on 16/5/18.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class RoboAvatarDownloadTest extends BaseTest {
    /**
     * #################    测试AvatarDownloader－downloadSmallAvatar    #################
     */
    //mediaID == null
    @Test
    public void downloadSmallAvatar_5() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn(null);

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.LOCAL_ERROR.LOCAL_USER_AVATAR_NOT_SPECIFIED, responseCode);
                assertEquals(ErrorCode.LOCAL_ERROR.LOCAL_USER_AVATAR_NOT_SPECIFIED_DESC, responseMessage);
                assertNull(avatar);
            }
        };
        AvatarDownloader avatarDownloader = new AvatarDownloader();
        avatarDownloader.downloadSmallAvatar(info.getAvatar(), callback);
    }

    /**
     * #################    测试AvatarDownloader－downloadBigAvatar    #################
     */
    //mediaID == null
    @Test
    public void downloadBigAvatar_5() {
        InternalUserInfo info = mock(InternalUserInfo.class);
        when(info.getAvatar()).thenReturn(null);

        final DownloadAvatarCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(ErrorCode.LOCAL_ERROR.LOCAL_USER_AVATAR_NOT_SPECIFIED, responseCode);
                assertEquals(ErrorCode.LOCAL_ERROR.LOCAL_USER_AVATAR_NOT_SPECIFIED_DESC, responseMessage);
                assertNull(avatar);
            }
        };
        AvatarDownloader avatarDownloader = new AvatarDownloader();
        avatarDownloader.downloadBigAvatar(info.getAvatar(), callback);
    }
}
