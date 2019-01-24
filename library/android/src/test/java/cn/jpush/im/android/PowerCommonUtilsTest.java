package cn.jpush.im.android;

import org.powermock.api.mockito.PowerMockito;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * Created by ${chenyn} on 16/5/17.
 *
 * @desc : 使用powerMockito进行测试CommonUtils－translateUserIdToDisplaynames
 * @parame :
 * @return :
 */
//@RunWith(PowerMockRunner.class)
//@PrepareForTest(UserInfoManager.class)
public class PowerCommonUtilsTest {

    // FIXME: 16/7/8 如果不使用robolectric框架，方法内所有的android相关api全部返回默认值这样导致结果不正确
    public void translateUserIdToDisplaynames_2() {

        final List<Long> userIds = new ArrayList<Long>();
        userIds.add(888l);

        List<UserInfo> userInfo = new ArrayList<UserInfo>();
        final InternalUserInfo info = new InternalUserInfo();
        info.setNickname("testNickName");
        info.setUserName("testUserName");
        info.setUserID(888l);
        userInfo.add(info);

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = mock(UserInfoManager.class);

        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
//        PowerMockito.when(manager.getUserInfoList(userIds, -1)).thenReturn(userInfo);

        assertEquals("[" + "testNickName" + "]", CommonUtils.translateUserIdToDisplaynames(userIds, true).toString());
    }


}
