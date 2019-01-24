package cn.jpush.im.android;


import android.test.AndroidTestCase;

import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.api.BasicCallback;

public class BasicTestCase extends AndroidTestCase {
    private static final String TAG = "BasicTestCase";

    public static final int ERROR_CODE_NO_ERROR = 0;
    public static final int ERROR_CODE_LOCAL_INVALID_PARAM = 871301;
    public static final int ERROR_CODE_LOCAL_INVALID_USERNAME = 871303;
    public static final int ERROR_CODE_LOCAL_INVALID_PASSWORD = 871304;
    public static final int ERROR_CODE_HTTP_USER_EXIST = 898001;
    public static final int ERROR_CODE_HTTP_USER_NOT_EXIST = 898002;
    public static final int ERROR_CODE_HTTP_INVALID_PARAM = 898003;

    protected boolean isTestEnv = false;

    protected boolean needLogin = true;

    protected String test_username = "nnnnn";

    protected String test_password = "nnnnn";
    protected String appkey = "4f7aef34fb361292c566a1cd";

    public BasicTestCase(String test_username, String test_password, boolean isTestEnv) {
        this.test_username = test_username;
        this.test_password = test_password;
        this.isTestEnv = isTestEnv;
    }

    public BasicTestCase(boolean needLogin) {
        this.needLogin = needLogin;
    }

    public void setUp() throws Exception {
        super.setUp();
        JMessageClient.init(getContext());
        if (needLogin) {
            final CountDownLatch loginLatch = new CountDownLatch(1);
            JMessageClient.swapEnvironment(getContext(), isTestEnv);
            JMessageClient.login(test_username, test_password, new BasicCallback() {
                @Override
                public void gotResult(int i, String s) {
                    assertEquals(0, i);
                    loginLatch.countDown();
                }
            });
            loginLatch.await(30, TimeUnit.SECONDS);
            assertEquals(0, loginLatch.getCount());
        }
    }

    public void tearDown() throws Exception {
        super.tearDown();
    }

    protected String createRandomString() {
        return UUID.randomUUID().toString();
    }
}
