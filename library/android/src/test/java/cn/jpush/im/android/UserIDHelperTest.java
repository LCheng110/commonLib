package cn.jpush.im.android;

import android.database.sqlite.SQLiteDatabase;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.UserIDHelper;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;
import static org.powermock.api.mockito.PowerMockito.mock;

/**
 * Created by ${chenyn} on 16/5/23.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest(UserInfoManager.class)
public class UserIDHelperTest {
    //getUidInCache(userName, appkey) == 0  userID == 0
    public void getUserIDFromLocal_1() {
        long userID = UserIDHelper.getUserIDFromLocal("username", "appkey");
        assertEquals(0, userID);
    }

    //getUidInCache(userName, appkey) == 0  userID != 0
    @Test
    public void getUserIDFromLocal_2() {
        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.queryUserID("userName", "appkey")).thenReturn(6L);

        long userID = UserIDHelper.getUserIDFromLocal("userName", "appkey");

        assertEquals(6, userID);
    }

    //getUidInCache(userName, appkey) == 0  userID == 0
    @Test
    public void getUserIDFromLocal_3() {
        SQLiteDatabase helper = mock(SQLiteDatabase.class);
        long userID = UserIDHelper.getUserIDFromLocal(helper, "testName", "testAppkey");
        assertEquals(0, userID);
    }

    //getUidInCache(userName, appkey) == 0  userID != 0
    @Test
    public void getUserIDFromLocal_4() {
        SQLiteDatabase helper = mock(SQLiteDatabase.class);
        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.queryUserID(helper, "userName")).thenReturn(6L);

        long userID = UserIDHelper.getUserIDFromLocal(helper, "userName", "appkey");

        assertEquals(6, userID);
    }

    //null != usernames
    public void getUserIDsFromLocal_1() {
        List<String> userName = new ArrayList<String>();
        userName.add("testName");

        List list = UserIDHelper.getUserIDsFromLocal(userName, "appkey");
        assertEquals("[0]", list.toString());
    }

    //null == usernames
    @Test
    public void getUserIDsFromLocal_2() {
        List<String> userName = null;
        List list = UserIDHelper.getUserIDsFromLocal(userName, "appkey");
        assertEquals(null, list);
    }

    //userName != null 
    @Test
    public void getUserIDsFromLocal_3() {
        SQLiteDatabase helper = mock(SQLiteDatabase.class);
        List<String> userName = new ArrayList<String>();
        userName.add("testName");

        List list = UserIDHelper.getUserIDsFromLocal(helper, userName, "appkey");
        assertEquals("[0]", list.toString());
    }

    //userName == null
    @Test
    public void getUserIDsFromLocal_4() {
        SQLiteDatabase helper = mock(SQLiteDatabase.class);
        List<String> userName = null;

        List list = UserIDHelper.getUserIDsFromLocal(helper, userName, "appkey");
        assertEquals(null, list);
    }

    //null == userName && null != appkey
    public void getUserNameFromLocal_1() {
        long userID = 88888;
        String[] str = new String[]{null, "test2"};

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);

        PowerMockito.when(manager.queryUsernameAndAppkey(userID)).thenReturn(str);
        String userName = UserIDHelper.getUserNameFromLocal(userID);
        assertEquals(null, userName);
    }

    //null != userName && null == appkey
    @Test
    public void getUserNameFromLocal_2() {
        long userID = 123;
        String[] str = new String[]{"test1", null};

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);

        PowerMockito.when(manager.queryUsernameAndAppkey(userID)).thenReturn(str);
        String userName = UserIDHelper.getUserNameFromLocal(userID);
        assertEquals("test1", userName);
    }

    //null != userName && null != appkey
    @Test
    public void getUserNameFromLocal_3() {
        long userID = 123;
        String[] str = new String[]{"test1", "test2"};

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.queryUsernameAndAppkey(userID)).thenReturn(str);

        String userName = UserIDHelper.getUserNameFromLocal(userID);
        assertEquals("test1", userName);
    }

    //null == userName && null != appkey
    @Test
    public void getUserAppkeyFromLocal_1() {
        long userID = 1234;
        String[] string = new String[]{null, "test2"};

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);

        PowerMockito.when(manager.queryUsernameAndAppkey(userID)).thenReturn(string);
        String userName = UserIDHelper.getUserAppkeyFromLocal(userID);
        assertEquals("test2", userName);
    }


    //null != userName && null != appkey
    @Test
    public void getUserAppkeyFromLocal_2() {
        long userID = 1234;
        String[] string = new String[]{"test1", "test2"};

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.queryUsernameAndAppkey(userID)).thenReturn(string);

        String userName = UserIDHelper.getUserAppkeyFromLocal(userID);
        assertEquals("test2", userName);
    }

    //null != userName && null == appkey
    public void getUserAppkeyFromLocal_3() {
        long userID = 12345;
        String[] username = new String[]{"test1", null};

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);
        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.queryUsernameAndAppkey(userID)).thenReturn(username);

        String userName = UserIDHelper.getUserAppkeyFromLocal(userID);
        assertEquals(null, userName);
    }

    //userName == null
    @Test
    public void getUserIDs_1() {
        List<String> userNames = null;
        String appkey = "appkey";
        UserIDHelper.GetUseridsCallback callback = new UserIDHelper.GetUseridsCallback() {
            @Override
            public void gotResult(int code, String msg, List<Long> userids) {
                assertEquals(871301, code);
                assertEquals("Invalid parameters.", msg);
                assertNull(userids);
            }
        };
        UserIDHelper.getUserIDs(userNames, appkey, callback);
    }

    //userName != null
    @Test
    public void getUserIDs_2() {
        List<String> userNames = new ArrayList<String>();
        String appkey = "appkey";

        UserIDHelper.GetUseridsCallback callback = new UserIDHelper.GetUseridsCallback() {
            @Override
            public void gotResult(int code, String msg, List<Long> userids) {
                assertEquals(0, code);
                assertEquals("Success", msg);
                assertEquals("[]", userids.toString());
            }
        };
        UserIDHelper.getUserIDs(userNames, appkey, callback);
    }

    //userIDs == null
    @Test
    public void getUserNames_1() {
        final List<Long> userIDs = null;
        UserIDHelper.GetUsernamesCallback callback = new UserIDHelper.GetUsernamesCallback() {
            @Override
            public void gotResult(int code, String msg, List<String> usernames) {
                assertEquals(871301, code);
                assertEquals("Invalid parameters.", msg);
                assertNull(usernames);
            }
        };
        UserIDHelper.getUserNames(userIDs, callback);
    }

    //userName != null
    @Test
    public void getUserNames_2() {
        final List<Long> userIDs = new ArrayList<Long>();

        UserIDHelper.GetUsernamesCallback callback = new UserIDHelper.GetUsernamesCallback() {
            @Override
            public void gotResult(int code, String msg, List<String> usernames) {
                assertEquals(0, code);
                assertEquals("Success", msg);
                assertEquals("[]", usernames.toString());
            }
        };
        UserIDHelper.getUserNames(userIDs, callback);
    }
}
