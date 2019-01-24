package cn.jpush.im.android;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.utils.SendingMsgCallbackManager;
import cn.jpush.im.api.BasicCallback;

/**
 * Created by ${chenyn} on 16/5/25.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(SendingMsgCallbackManager.class)
public class SendMsgCallBackManagerTest {

    //targetToHashMap.get(targetKey) == null
    @Test
    public void getUploadProgressCallbackFromTarget() {
        String target = "target";
        String appkey = "appkey";
        int msgID = 123;

        ProgressUpdateCallback upload = SendingMsgCallbackManager.getUploadProgressCallbackFromTarget(target, appkey, msgID);
        Assert.assertEquals(null, upload);
    }

    //callbacksMap.get(hashcode) == null
    @Test
    public void getUploadProgressCallbackFromHash() {
       int hashCode = 1;
        ProgressUpdateCallback fromHash = SendingMsgCallbackManager.getUploadProgressCallbackFromHash(hashCode);

        Assert.assertEquals(null, fromHash);
    }

    //targetToHashMap.get(targetKey) == null
    @Test
    public void getDownloadProgressCallbackFromTarget() {
        String target = "target";
        String appkey = "appkey";
        int msgID = 123;

        ProgressUpdateCallback upload = SendingMsgCallbackManager.getDownloadProgressCallbackFromTarget(target, appkey, msgID);
        Assert.assertEquals(null, upload);
    }

    //callbacksMap.get(hashcode) == null
    @Test
    public void getDownloadProgressCallbackFromHash() {
        int hashCode = 1;
        ProgressUpdateCallback fromHash = SendingMsgCallbackManager.getDownloadProgressCallbackFromHash(hashCode);

        Assert.assertEquals(null, fromHash);
    }

    //targetToHashMap.get(targetKey) == null
    @Test
    public void getCompleteCallbackFromTarget() {
        String target = "target";
        String appkey = "appkey";
        int msgID = 123;

        BasicCallback upload = SendingMsgCallbackManager.getCompleteCallbackFromTarget(target, appkey, msgID);
        Assert.assertEquals(null, upload);
    }

    //callbacksMap.get(hashcode) == null
    @Test
    public void getCompleteCallbackFromHash() {
        int hashCode = 1;
        BasicCallback fromHash = SendingMsgCallbackManager.getCompleteCallbackFromHash(hashCode);

        Assert.assertEquals(null, fromHash);
    }
}















