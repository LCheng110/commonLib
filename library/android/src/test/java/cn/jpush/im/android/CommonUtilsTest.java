package cn.jpush.im.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import cn.jpush.im.android.api.callback.CreateGroupCallback;
import cn.jpush.im.android.api.callback.DownloadAvatarCallback;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.GetAvatarBitmapCallback;
import cn.jpush.im.android.api.callback.GetBlacklistCallback;
import cn.jpush.im.android.api.callback.GetGroupIDListCallback;
import cn.jpush.im.android.api.callback.GetGroupInfoCallback;
import cn.jpush.im.android.api.callback.GetGroupMembersCallback;
import cn.jpush.im.android.api.callback.GetNoDisurbListCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.GetUserInfoListCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.utils.CommonUtils;
import cn.jpush.im.api.BasicCallback;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static junit.framework.TestCase.assertEquals;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;


/**
 * Created by ${chenyn} on 16/5/12.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(RobolectricTestRunner.class)
@Config(manifest = Config.NONE)
public class CommonUtilsTest extends BaseTest {
    /**
     * #################    测试CommonUtils－isNetworkConnected     #################
     */
    //正常情况
    @Test
    public void testIsNetWorkConnected_1() {
        Context context = mock(Context.class);
        NetworkInfo info = mock(NetworkInfo.class);
        ConnectivityManager manager = mock(ConnectivityManager.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetworkInfo()).thenReturn(info);
        when(info.isAvailable()).thenReturn(true);

        assertTrue(CommonUtils.isNetworkConnected(context));
    }

    //NetworkInfo.isAvailable()为null的情况
    @Test
    public void testIsNetWorkConnected_2() {
        Context context = mock(Context.class);
        NetworkInfo info = mock(NetworkInfo.class);
        ConnectivityManager manager = mock(ConnectivityManager.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetworkInfo()).thenReturn(info);
        when(info.isAvailable()).thenReturn(false);

        assertFalse(CommonUtils.isNetworkConnected(context));
    }

    //ConnectivityManager.getActiveNetworkInfo()为null情况
    @Test
    public void testIsNetWorkConnected_3() {
        Context context = mock(Context.class);
        ConnectivityManager manager = mock(ConnectivityManager.class);

        when(context.getSystemService(Context.CONNECTIVITY_SERVICE)).thenReturn(manager);
        when(manager.getActiveNetworkInfo()).thenReturn(null);

        assertFalse(CommonUtils.isNetworkConnected(context));
    }

    /**
     * #################    测试CommonUtils－testValidateStrings    #################
     */

    @Test
    public void testValidateStrings() {
        assertTrue(CommonUtils.validateStrings("test", "test", "test"));
        assertTrue(CommonUtils.validateStrings(null, "test"));
        assertFalse(CommonUtils.validateStrings("test", " ", " "));
        assertFalse(CommonUtils.validateStrings("test", "test", " "));
        assertFalse(CommonUtils.validateStrings(null, " "));

        assertFalse(CommonUtils.validateStrings("test", "", ""));
        assertFalse(CommonUtils.validateStrings("test", "test", null));
        assertFalse(CommonUtils.validateStrings(null));
        assertFalse(CommonUtils.validateStrings(null, ""));
    }

    /**
     * #################    测试CommonUtils－isLogin     #################
     */
    //IMConfigs.getUserID()返回值不为0
    @Test
    public void testIsLogin_1() {
        when(IMConfigs.getUserID()).thenReturn((long) 1);
        assertTrue(CommonUtils.isLogin("test"));
    }

    //IMConfigs.getUserID()返回值为0
    @Test
    public void testIsLogin_2() {
        when(IMConfigs.getUserID()).thenReturn((long) 0);
        assertEquals(false, CommonUtils.isLogin("test"));
    }

    /**
     * #################    测试CommonUtils－isInited    #################
     */

    //JMessage.mContext为null的情况
    @Test
    public void testIsInited_1() {
        Context flag = JMessage.mContext;
        Context context = null;
        JMessage.mContext = context;
        assertFalse(CommonUtils.isInited("test"));
        JMessage.mContext = flag;
    }

    //JMessage.mContext不为null的情况
    @Test
    public void testIsInited_2() {
        assertTrue(CommonUtils.isInited("test"));
    }

    /**
     * #################    测试CommonUtils－validateObjects    #################
     */
    @Test
    public void testValidateObjects() {
        assertTrue(CommonUtils.validateObjects("test", new Object()));
        assertTrue(CommonUtils.validateObjects(null, new Object()));
        assertTrue(CommonUtils.validateObjects(" ", new Object()));
        assertTrue(CommonUtils.validateObjects("", " "));

        assertFalse(CommonUtils.validateObjects(" ", null, ""));
        assertFalse(CommonUtils.validateObjects("test"));

    }

    /**
     * #################    测试CommonUtils－validateCollectionSize    #################
     */
    @Test
    public void testValidateListSize() {
        assertFalse(CommonUtils.validateCollectionSize("test", new ArrayList(), new ArrayList()));

        List list = new ArrayList();
        for (int i = 0; i < 501; i++) {
            list.add(i);
        }
        assertFalse(CommonUtils.validateCollectionSize("test", list));
        assertFalse(CommonUtils.validateCollectionSize(null, list));

        assertFalse(CommonUtils.validateCollectionSize("test"));
    }

    /**
     * #################    测试CommonUtils－doInitialCheck    #################
     */

    //三种if都进不去，直接返回true情况
    @Test
    public void testDoInitialCheck_1() {
        BasicCallback callBack = mock(BasicCallback.class);
        when(IMConfigs.getNetworkConnected()).thenReturn(true);
        when(IMConfigs.getUserID()).thenReturn((long) 1);
        assertTrue(CommonUtils.doInitialCheck("test", null));
        assertTrue(CommonUtils.doInitialCheck("test", null));
        assertTrue(CommonUtils.doInitialCheck("test", callBack));
    }

    //isInited(methodName)==false情况
    @Test
    public void testDoInitialCheck_2() {
        Context flag = JMessage.mContext;
        BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871308, i);
                assertEquals("SDK have not init yet.", s);

            }
        };
        Context context = null;
        JMessage.mContext = context;
        assertFalse(CommonUtils.doInitialCheck("test", callBack));
        assertFalse(CommonUtils.doInitialCheck("test", null));
        JMessage.mContext = flag;
    }

    //    isLogin(methodName)==false情况
    @Test
    public void testDoInitialCheck_3() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871300, i);
                assertEquals("Have not logged in.", s);
                countDownLatch.countDown();
            }
        };
        when(IMConfigs.getNetworkConnected()).thenReturn(true);
        when(IMConfigs.getUserID()).thenReturn((long) 0);
        assertFalse(CommonUtils.doInitialCheck("test", callBack));
        assertFalse(CommonUtils.doInitialCheck("test", null));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //IMConfigs.getNetworkConnected()==false情况
    @Test
    public void testDoInitialCheck_4() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871310, i);
                assertEquals("Network not available,please check your network connection.", s);
                countDownLatch.countDown();
            }
        };
        when(IMConfigs.getNetworkConnected()).thenReturn(false);
        assertFalse(CommonUtils.doInitialCheck("test", callBack));
        assertFalse(CommonUtils.doInitialCheck("test", null));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    /**
     * #################    测试CommonUtils－doInitialCheckWithoutNetworkCheck    #################
     */
    //两种if都进不去直接返回true情况
    @Test
    public void testDoInitialCheckWithoutNetworkCheck_1() {
        BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
            }
        };
        assertTrue(CommonUtils.doInitialCheckWithoutNetworkCheck("test", callBack));
        assertTrue(CommonUtils.doInitialCheckWithoutNetworkCheck("test", callBack));
        assertTrue(CommonUtils.doInitialCheckWithoutNetworkCheck("test", null));
        assertTrue(CommonUtils.doInitialCheckWithoutNetworkCheck("test", null));
    }

    //isInited(methodName)==false情况
    @Test
    public void testDoInitialCheckWithoutNetworkCheck_2() {
        final Context flag = JMessage.mContext;
        BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871308, i);
                assertEquals("SDK have not init yet.", s);
            }
        };
        Context context = null;
        JMessage.mContext = context;
        assertFalse(CommonUtils.doInitialCheckWithoutNetworkCheck("test", callBack));
        assertFalse(CommonUtils.doInitialCheckWithoutNetworkCheck("test", null));
        JMessage.mContext = flag;
    }

    //isLogin(methodName)==false情况
    @Test
    public void testDoInitialCheckWithoutNetworkCheck_3() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871300, i);
                assertEquals("Have not logged in.", s);
                countDownLatch.countDown();
            }
        };

        when(IMConfigs.getUserID()).thenReturn((long) 0);
        assertFalse(CommonUtils.doInitialCheckWithoutNetworkCheck("test", callBack));
        assertFalse(CommonUtils.doInitialCheckWithoutNetworkCheck("test", null));
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    /**
     * #################    测试CommonUtils－handleRegCode    #################
     */
    //case 1005:
    @Test
    public void testHandleRegCode_1() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int registerCode = 1005;
        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871501, i);
                assertEquals("Push register error,appkey and appid not match.", s);
                countDownLatch.countDown();
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //case 1006:
    @Test
    public void testHandleRegCode_2() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int registerCode = 1006;
        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871505, i);
                assertEquals("Push register error,package not exists.", s);
                countDownLatch.countDown();
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //case 1007:
    @Test
    public void testHandleRegCode_3() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int registerCode = 1007;
        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871506, i);
                assertEquals("Push register error,invalid IMEI.", s);
                countDownLatch.countDown();
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //case 1008:
    @Test
    public void testHandleRegCode_4() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int registerCode = 1008;
        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871502, i);
                assertEquals("Push register error,invalid appkey.", s);
                countDownLatch.countDown();
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //case 1009:
    @Test
    public void testHandleRegCode_5() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int registerCode = 1009;

        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871503, i);
                assertEquals("Push register error,appkey not matches platform", s);
                countDownLatch.countDown();
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //case 0:
    @Test
    public void testHandleRegCode_6() {
        final int registerCode = 0;
        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(0, i);
                assertEquals("Success", s);
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
    }

    //case -1:
    @Test
    public void testHandleRegCode_7() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int registerCode = -1;
        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(871504, i);
                assertEquals("Push register not finished. ", s);
                countDownLatch.countDown();
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //case 666:registerCode不存在的情况
    @Test
    public void testHandleRegCode_8() {
        final CountDownLatch countDownLatch = new CountDownLatch(1);
        final int registerCode = 666;
        final BasicCallback callBack = new BasicCallback() {
            @Override
            public void gotResult(int i, String s) {
                assertEquals(666, i);
                assertEquals("push 注册失败", s);
                countDownLatch.countDown();
            }
        };
        CommonUtils.handleRegCode(registerCode, callBack);
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    //callback＝＝null情况
    @Test
    public void testHandleRegCode_9() {
        assertTrue(CommonUtils.handleRegCode(44, null));
        assertTrue(CommonUtils.handleRegCode(1006, null));
        assertTrue(CommonUtils.handleRegCode(144, null));
    }

    /**
     * #################    测试CommonUtils－doCompleteCallBackToUser    #################
     */
    //case BasicCallback:inCallerThread == true
    public void testDoCompleteCallBackToUser_1() {
        BasicCallback callBack = mock(BasicCallback.class);
        when(callBack.isRunInUIThread()).thenReturn(true);

        CommonUtils.doCompleteCallBackToUser(true, callBack, 1, "desc");
        Mockito.verify(callBack).gotResult(1, "desc");
    }

    //case BasicCallback:inCallerThread == false
    public void testDoCompleteCallBackToUser_2() {
        BasicCallback callBack = mock(BasicCallback.class);
        when(callBack.isRunInUIThread()).thenReturn(true);

        CommonUtils.doCompleteCallBackToUser(false, callBack, 1, "desc");
        Mockito.verify(callBack).gotResult(1, "desc");
    }

    //case CreateGroupCallback:param[0] != null && param[1] != null inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_3() {
        BasicCallback callBack = new CreateGroupCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, long groupId) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMsg);
                assertEquals(1l, groupId);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callBack, 1, "desc", 1l, 2l);
    }

    //case CreateGroupCallback:param[0] != null && param[1] != null inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_4() {
        BasicCallback callBack = new CreateGroupCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, long groupId) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMsg);
                assertEquals(1l, groupId);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callBack, 1, "desc", 1l, 2l);
    }

    //case CreateGroupCallback:param[0] == null inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_5() {
        BasicCallback callBack = new CreateGroupCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, long groupId) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMsg);
                assertEquals(0l, groupId);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callBack, 1, "desc", null, 2l);
    }

    //case CreateGroupCallback:param[0] == null  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_6() {
        BasicCallback callBack = new CreateGroupCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, long groupId) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMsg);
                assertEquals(0l, groupId);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callBack, 1, "desc", null, 2l);
    }

    //case CreateGroupCallback:param[0] != null && param[1] == null inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_7() {
        BasicCallback callBack = new CreateGroupCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, long groupId) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMsg);
                assertEquals(1l, groupId);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callBack, 1, "desc", 1l, null);
    }

    //case CreateGroupCallback:param[0] != null && param[1] == null inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_8() {
        BasicCallback callBack = new CreateGroupCallback() {
            @Override
            public void gotResult(int responseCode, String responseMsg, long groupId) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMsg);
                assertEquals(1l, groupId);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callBack, 1, "desc", 1l, null);
    }

    //case GetGroupIDListCallback:param[0]为null的List && param[1]==2l  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_9() {
        List<Long> arrayList = new ArrayList<Long>();
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[]", groupIDList.toString());
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", arrayList, 2l);
    }

    //case GetGroupIDListCallback:param[0]为null的List && param[1]==2l  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_10() {
        List<Long> arrayList = new ArrayList<Long>();
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[]", groupIDList.toString());
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", arrayList, 2l);
    }

    //case GetGroupIDListCallback:param[0]为有参数的List && param[1]==2l  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_11() {
        List<Long> arrayList = new ArrayList<Long>();
        arrayList.add(3l);
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[3]", groupIDList.toString());
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", arrayList, 2l);
    }

    //case GetGroupIDListCallback:param[0]为有参数的List && param[1]==2l  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_12() {
        List<Long> arrayList = new ArrayList<Long>();
        arrayList.add(3l);
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[3]", groupIDList.toString());
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", arrayList, 2l);
    }

    //case GetGroupIDListCallback:param[0]==null && param[1]==3l  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_13() {
        List<Long> arrayList = new ArrayList<Long>();
        arrayList.add(3l);
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, groupIDList);
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, arrayList);
    }

    //case GetGroupIDListCallback:param[0]==null && param[1]==3l  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_14() {
        List<Long> arrayList = new ArrayList<Long>();
        arrayList.add(3l);
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, groupIDList);
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, arrayList);
    }

    //case GetGroupIDListCallback:param[0]==3 && param[1]==null  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_15() {
        List<Long> arrayList = new ArrayList<Long>();
        arrayList.add(3l);
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[3]", groupIDList.toString());
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", arrayList, null);
    }

    //case GetGroupIDListCallback:param[0]==3 && param[1]==null  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_16() {
        List<Long> arrayList = new ArrayList<Long>();
        arrayList.add(3l);
        BasicCallback callback = new GetGroupIDListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<Long> groupIDList) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[3]", groupIDList.toString());
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", arrayList, null);
    }

    //case GetGroupInfoCallback: param[0]为创建的InternalGroupInfo并没赋值，param[1]为赋值id的InternalGroupInfo inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_17() {
        InternalGroupInfo info = new InternalGroupInfo();
        info.setGroupID(99l);
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(-1, groupInfo.getGroupID());
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", new InternalGroupInfo(), info);
    }

    //case GetGroupInfoCallback: param[0]为创建的InternalGroupInfo并没赋值，param[1]为赋值id的InternalGroupInfo  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_18() {
        InternalGroupInfo info = new InternalGroupInfo();
        info.setGroupID(99l);
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(-1, groupInfo.getGroupID());
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", new InternalGroupInfo(), info);
    }

    //case GetGroupInfoCallback: param[0]为创建的InternalGroupInfo赋值id=99l，param[1]==null  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_19() {
        InternalGroupInfo info = new InternalGroupInfo();
        info.setGroupID(99l);
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(99, groupInfo.getGroupID());
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", info, null);
    }

    //case GetGroupInfoCallback: param[0]为创建的InternalGroupInfo赋值id=99l，param[1]==null  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_20() {
        InternalGroupInfo info = new InternalGroupInfo();
        info.setGroupID(99l);
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(99, groupInfo.getGroupID());
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", info, null);
    }

    //case GetGroupInfoCallback: param[0]==null，param[1]==null  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_21() {
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, groupInfo);
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, null);
    }

    //case GetGroupInfoCallback: param[0]==null，param[1]==null  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_22() {
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, groupInfo);
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, null);
    }

    //case GetGroupInfoCallback: param[0]==null，param[1]的ID赋值99l  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_23() {
        InternalGroupInfo info = new InternalGroupInfo();
        info.setGroupID(99l);
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, groupInfo);
            }
        };
        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, info);
    }

    //case GetGroupInfoCallback: param[0]==null，param[1]的ID赋值99l  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_24() {
        InternalGroupInfo info = new InternalGroupInfo();
        info.setGroupID(99l);
        BasicCallback callback = new GetGroupInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, GroupInfo groupInfo) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, groupInfo);
            }
        };
        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, info);
    }

    //case GetGroupMembersCallback: param[0]创建list不赋值，param［1]==null  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_25() {
        List<UserInfo> list = new ArrayList<UserInfo>();

        BasicCallback callback = new GetGroupMembersCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[]", members.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", list, null);
    }

    //case GetGroupMembersCallback: param[0]创建list不赋值，param［1]==null  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_26() {
        List<UserInfo> list = new ArrayList<UserInfo>();

        BasicCallback callback = new GetGroupMembersCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[]", members.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", list, null);
    }

    //case GetGroupMembersCallback: param[0]创建list并赋值，param［1]==null  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_27() {
        List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetGroupMembersCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[" + info.toString() + "]", members.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", list, null);
    }

    //case GetGroupMembersCallback: param[0]创建list并赋值，param［1]==null  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_28() {
        List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetGroupMembersCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[" + info.toString() + "]", members.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", list, null);
    }

    //case GetGroupMembersCallback: param[0]==null，param［1]创建list并赋值  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_29() {
        List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetGroupMembersCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, members);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, list);
    }

    //case GetGroupMembersCallback: param[0]==null，param［1]创建list并赋值  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_30() {
        List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetGroupMembersCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> members) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, members);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, list);
    }

    //case GetUserInfoCallback: param[0]==null，param［1]创建InternalUserInfo不赋值  inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_31() {
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");

        BasicCallback callback = new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, info);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, info);
    }

    //case GetUserInfoCallback: param[0]==null，param［1]创建InternalUserInfo不赋值  inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_32() {
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");

        BasicCallback callback = new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, info);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, info);
    }

    //case GetUserInfoCallback: param［0]创建InternalUserInfo不赋值 , param[1]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_33() {
        final InternalUserInfo userInfo = new InternalUserInfo();
        userInfo.setNickname("testNickName");

        BasicCallback callback = new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(userInfo, info);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", userInfo, null);
    }

    //case GetUserInfoCallback: param［0]创建InternalUserInfo不赋值 , param[1]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_34() {
        final InternalUserInfo userInfo = new InternalUserInfo();
        userInfo.setNickname("testNickName");

        BasicCallback callback = new GetUserInfoCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, UserInfo info) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(userInfo, info);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", userInfo, null);
    }

    //case GetUserInfoListCallback: param［0]创建list赋值 , param[1]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_35() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetUserInfoListCallback() {
            @Override
            public void gotResult(int code, String msg, List<UserInfo> userList) {
                assertEquals(1, code);
                assertEquals("desc", msg);
                assertEquals(list, userList);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", list, null);
    }

    //case GetUserInfoListCallback: param［0]创建list赋值 , param[1]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_36() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetUserInfoListCallback() {
            @Override
            public void gotResult(int code, String msg, List<UserInfo> userList) {
                assertEquals(1, code);
                assertEquals("desc", msg);
                assertEquals(list, userList);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", list, null);
    }

    //case GetUserInfoListCallback: param［1]创建list赋值 , param[0]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_37() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetUserInfoListCallback() {
            @Override
            public void gotResult(int code, String msg, List<UserInfo> userList) {
                assertEquals(1, code);
                assertEquals("desc", msg);
                assertEquals(null, userList);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, list);
    }

    //case GetUserInfoListCallback: param［1]创建list赋值 , param[0]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_38() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);

        BasicCallback callback = new GetUserInfoListCallback() {
            @Override
            public void gotResult(int code, String msg, List<UserInfo> userList) {
                assertEquals(1, code);
                assertEquals("desc", msg);
                assertEquals(null, userList);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, list);
    }

    //case ProgressUpdateCallback: param［0]=1.1 , param[1]==2.2， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_39() {
        BasicCallback callback = new ProgressUpdateCallback() {
            @Override
            public void onProgressUpdate(double percent) {
                assertEquals(1.1, percent);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", 1.1, 2.2);
    }

    //case ProgressUpdateCallback: param［0]=1.1 , param[1]==2.2， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_40() {
        BasicCallback callback = new ProgressUpdateCallback() {
            @Override
            public void onProgressUpdate(double percent) {
                assertEquals(1.1, percent);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", 1.1, 2.2);
    }

    //case ProgressUpdateCallback: param［0]=null , param[1]==2.2， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_41() {
        BasicCallback callback = new ProgressUpdateCallback() {
            @Override
            public void onProgressUpdate(double percent) {
                assertEquals(0.0, percent);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, 2.2);
    }

    //case ProgressUpdateCallback: param［0]=null , param[1]==2.2， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_42() {
        BasicCallback callback = new ProgressUpdateCallback() {
            @Override
            public void onProgressUpdate(double percent) {
                assertEquals(0.0, percent);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, 2.2);
    }

    //case DownloadCompletionCallback: param［0]=null , param[1]==2.2， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_43() {
        BasicCallback callback = new DownloadCompletionCallback() {
            @Override
            public void onComplete(int responseCode, String responseMessage, File file) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, file);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, 2.2);
    }

    //case DownloadCompletionCallback: param［0]=null , param[1]==2.2， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_44() {
        BasicCallback callback = new DownloadCompletionCallback() {
            @Override
            public void onComplete(int responseCode, String responseMessage, File file) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, file);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, 2.2);
    }

    //case DownloadCompletionCallback: param［0]=new File("testPath") , param[1]==2.2， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_45() {
        BasicCallback callback = new DownloadCompletionCallback() {
            @Override
            public void onComplete(int responseCode, String responseMessage, File file) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("testPath", file.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", new File("testPath"), 2.2);
    }

    //case DownloadCompletionCallback: param［0]=new File("testPath") , param[1]==2.2， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_46() {
        BasicCallback callback = new DownloadCompletionCallback() {
            @Override
            public void onComplete(int responseCode, String responseMessage, File file) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("testPath", file.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", new File("testPath"), 2.2);
    }

    //case DownloadAvatarCallback: param［0]=new File("testPath") , param[1]==2.2， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_47() {
        BasicCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("testPath", avatar.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", new File("testPath"), 2.2);
    }

    //case DownloadAvatarCallback: param［0]=new File("testPath") , param[1]==2.2， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_48() {
        BasicCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("testPath", avatar.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", new File("testPath"), 2.2);
    }

    //case DownloadAvatarCallback: param［1]=new File("testPath") , param[0]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_49() {
        BasicCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, avatar);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, new File("testPath"));
    }

    //case DownloadAvatarCallback: param［1]=new File("testPath") , param[0]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_50() {
        BasicCallback callback = new DownloadAvatarCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, File avatar) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, avatar);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, new File("testPath"));
    }

    //case GetAvatarBitmapCallback: param［0]=bitmap , param[1]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_51() {
        final Bitmap bitmap = BitmapFactory.decodeFile("");
        BasicCallback callback = new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(bitmap, avatarBitmap);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", bitmap, null);
    }

    //case GetAvatarBitmapCallback: param［0]=bitmap , param[1]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_52() {
        final Bitmap bitmap = BitmapFactory.decodeFile("");
        BasicCallback callback = new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(bitmap, avatarBitmap);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", bitmap, null);
    }

    //case GetAvatarBitmapCallback: param［1]=bitmap , param[0]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_53() {
        final Bitmap bitmap = BitmapFactory.decodeFile("");
        BasicCallback callback = new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, avatarBitmap);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, bitmap);
    }

    //case GetAvatarBitmapCallback: param［1]=bitmap , param[0]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_54() {
        final Bitmap bitmap = BitmapFactory.decodeFile("");
        BasicCallback callback = new GetAvatarBitmapCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, Bitmap avatarBitmap) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, avatarBitmap);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, bitmap);
    }

    //case GetBlacklistCallback: param［0]=list , param[1]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_55() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);
        BasicCallback callback = new GetBlacklistCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[" + info + "]", userInfos.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", list, null);
    }

    //case GetBlacklistCallback: param［0]=list , param[1]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_56() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);
        BasicCallback callback = new GetBlacklistCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("[" + info + "]", userInfos.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", list, null);
    }

    //case GetBlacklistCallback: param［1]=list , param[0]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_57() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);
        BasicCallback callback = new GetBlacklistCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, userInfos);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, list);
    }

    //case GetBlacklistCallback: param［1]=list , param[0]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_58() {
        final List<UserInfo> list = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        list.add(info);
        BasicCallback callback = new GetBlacklistCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, userInfos);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, list);
    }

    //case CreateImageContentCallback: param［1]=content , param[0]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_59() {
        ImageContent content = mock(ImageContent.class);

        BasicCallback callback = new ImageContent.CreateImageContentCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, ImageContent imageContent) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, imageContent);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, content);
    }

    //case CreateImageContentCallback: param［1]=content , param[0]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_60() {
        ImageContent content = mock(ImageContent.class);

        BasicCallback callback = new ImageContent.CreateImageContentCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, ImageContent imageContent) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, imageContent);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, content);
    }

    //case CreateImageContentCallback: param［0]=content , param[1]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_61() {
        ImageContent content = mock(ImageContent.class);

        BasicCallback callback = new ImageContent.CreateImageContentCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, ImageContent imageContent) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("imageContent", imageContent.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", content, null);
    }

    //case CreateImageContentCallback: param［0]=content , param[1]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_62() {
        ImageContent content = mock(ImageContent.class);

        BasicCallback callback = new ImageContent.CreateImageContentCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, ImageContent imageContent) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("imageContent", imageContent.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", content, null);
    }

    //case GetNoDisturbListCallback: param［0]=null , param[1]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_63() {
        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, userInfos);
                assertEquals(null, groupInfos);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, null);
    }

    //case GetNoDisturbListCallback: param［0]=null , param[1]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_64() {
        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, userInfos);
                assertEquals(null, groupInfos);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, null);
    }

    //case GetNoDisturbListCallback: param［0]=userInfo , param[1]==null， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_65() {
        final List<UserInfo> userInfo = mock(List.class);

        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("list", userInfos.toString());
                assertEquals(null, groupInfos);
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", userInfo, null);
    }

    //case GetNoDisturbListCallback: param［0]=userInfo , param[1]==null， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_66() {
        final List<UserInfo> userInfo = mock(List.class);

        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("list", userInfos.toString());
                assertEquals(null, groupInfos);
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", userInfo, null);
    }

    //case GetNoDisturbListCallback: param［0]=userInfo , param[1]==groupInfo， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_67() {
        final List<UserInfo> userInfo = mock(List.class);
        final List<GroupInfo> groupInfo = mock(List.class);

        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("list", userInfos.toString());
                assertEquals("list", groupInfos.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", userInfo, groupInfo);
    }

    //case GetNoDisturbListCallback: param［0]=userInfo , param[1]==groupInfo， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_68() {
        final List<UserInfo> userInfo = mock(List.class);
        final List<GroupInfo> groupInfo = mock(List.class);

        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals("list", userInfos.toString());
                assertEquals("list", groupInfos.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", userInfo, groupInfo);
    }

    //case GetNoDisturbListCallback: param［0]=null , param[1]==groupInfo， inCallerThread == true
    @Test
    public void testDoCompleteCallBackToUser_69() {
        final List<GroupInfo> groupInfo = mock(List.class);

        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, userInfos);
                assertEquals("list", groupInfos.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(true, callback, 1, "desc", null, groupInfo);
    }

    //case GetNoDisturbListCallback: param［0]=null , param[1]==groupInfo， inCallerThread == false
    @Test
    public void testDoCompleteCallBackToUser_70() {
        final List<GroupInfo> groupInfo = mock(List.class);

        BasicCallback callback = new GetNoDisurbListCallback() {
            @Override
            public void gotResult(int responseCode, String responseMessage, List<UserInfo> userInfos, List<GroupInfo> groupInfos) {
                assertEquals(1, responseCode);
                assertEquals("desc", responseMessage);
                assertEquals(null, userInfos);
                assertEquals("list", groupInfos.toString());
            }
        };

        CommonUtils.doCompleteCallBackToUser(false, callback, 1, "desc", null, groupInfo);
    }

    /**
     * #################    测试CommonUtils－translateUserIdToDisplaynames    #################
     */
    //userIds == null
    @Test
    public void testTranslateUserIdToDisplaynames_1() {
        List<Long> userIds = null;
        assertEquals("[]", CommonUtils.translateUserIdToDisplaynames(userIds, true).toString());
    }
}


