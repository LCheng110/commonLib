package cn.jpush.im.android;

import org.junit.Before;
import org.junit.Test;

import java.io.File;

import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.internalmodel.InternalMessage;
import cn.jpush.im.android.utils.filemng.FileDownloader;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


public class FileDownloadTest extends BaseTest {
    private static final String TAG = "FileDownloadTest";

    @Before
    public void prepare() {
    }

    @Test
    public void download_origin_image() {
        InternalMessage message = mock(InternalMessage.class);
        ImageContent imageContent = mock(ImageContent.class);

        when(message.getContent()).thenReturn(imageContent);
        when(imageContent.getMediaID()).thenReturn("foo/bar/filename");

        DownloadCompletionCallback completionCallback = new DownloadCompletionCallback() {
            @Override
            public void onComplete(int responseCode, String responseMessage, File file) {
                assertEquals(0, responseCode);
                assertEquals("Success", responseMessage);
                assertNotNull(file);
            }
        };
        FileDownloader fileDownloader = new FileDownloader();

        fileDownloader.downloadOriginImage(message, completionCallback, false);
    }

}
