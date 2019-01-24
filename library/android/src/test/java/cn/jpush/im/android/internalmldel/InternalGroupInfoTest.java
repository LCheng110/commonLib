package cn.jpush.im.android.internalmldel;

import junit.framework.Assert;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.internalmodel.InternalGroupInfo;
import cn.jpush.im.android.internalmodel.InternalUserInfo;
import cn.jpush.im.android.storage.UserInfoManager;
import cn.jpush.im.android.utils.CommonUtils;

/**
 * Created by ${chenyn} on 16/6/2.
 *
 * @desc :
 * @parame :
 * @return :
 */
@RunWith(PowerMockRunner.class)
@PrepareForTest({CommonUtils.class, UserInfoManager.class})
public class InternalGroupInfoTest {

    //groupMemberInfos.size() = 0
    @Test
    public void getGroupMembers_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMembers", null)).thenReturn(true);
        InternalGroupInfo info = new InternalGroupInfo();

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = PowerMockito.mock(UserInfoManager.class);
        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.getUserInfoList(null)).thenReturn(new ArrayList<InternalUserInfo>());

        List<UserInfo> groupMembers = info.getGroupMembers();
        Assert.assertEquals("[]", groupMembers.toString());
    }

    //groupMemberInfos.size() = 0
    @Test
    public void getGroupMembers_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMembers", null)).thenReturn(true);
        InternalGroupInfo info = new ForGroupInfo();

        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = PowerMockito.mock(UserInfoManager.class);
        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.getUserInfoList(null)).thenReturn(new ArrayList<InternalUserInfo>());

        List<UserInfo> groupMembers = info.getGroupMembers();
        Assert.assertEquals("[userInfo]", groupMembers.toString());
    }

    //memberlist != null
    public void getGroupMemberInfo_1() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMemberInfo", null)).thenReturn(true);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMembers", null)).thenReturn(true);

        InternalGroupInfo info = new ForGroupInfo();
        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = PowerMockito.mock(UserInfoManager.class);
        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.getUserInfoList(null)).thenReturn(new ArrayList<InternalUserInfo>());

        UserInfo username = info.getGroupMemberInfo("username", "appKey");
        Assert.assertEquals("userInfo", username.toString());
    }

    //memberlist == null
    @Test
    public void getGroupMemberInfo_2() {
        PowerMockito.mockStatic(CommonUtils.class);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMemberInfo", null)).thenReturn(true);
        PowerMockito.when(CommonUtils.doInitialCheckWithoutNetworkCheck("getGroupMembers", null)).thenReturn(true);

        InternalGroupInfo info = new ForGroupInfoTwo();
        PowerMockito.mockStatic(UserInfoManager.class);
        UserInfoManager manager = PowerMockito.mock(UserInfoManager.class);
        PowerMockito.when(UserInfoManager.getInstance()).thenReturn(manager);
        PowerMockito.when(manager.getUserInfoList(null)).thenReturn(new ArrayList<InternalUserInfo>());

        UserInfo username = info.getGroupMemberInfo("username", "testAppkey");
        Assert.assertEquals(null, username);
    }

    //memberInfo = null
    @Test
    public void addMemberToList_1() {
        InternalGroupInfo info = new InternalGroupInfo();
        boolean b = info.addMemberToList(null);
        Assert.assertEquals(false, b);
    }

    //memberInfo != null
    @Test
    public void addMemberToList_2() {
        InternalGroupInfo info = new ForGroupInfoTwo();
        UserInfo userInfo = PowerMockito.mock(UserInfo.class);
        boolean b = info.addMemberToList(userInfo);
        Assert.assertEquals(true, b);
    }

    //memberInfo = null
    @Test
    public void addMemberIdToNameList_1() {
        InternalGroupInfo info = new InternalGroupInfo();
        boolean b = info.addMemberIdToNameList(null);
        Assert.assertEquals(false, b);
    }

    //memberInfo != null
    @Test
    public void addMemberIdToNameList_2() {
        InternalGroupInfo info = new ForGroupInfoFour();
        List<Long> userId = new ArrayList<Long>();
        userId.add(12L);
        boolean b = info.addMemberIdToNameList(userId);
        Assert.assertEquals(true, b);
    }

    //groupMemberInfos = null
    @Test
    public void removeMemberFromList_1() {
        InternalGroupInfo info = new InternalGroupInfo();
        boolean b = info.removeMemberFromList(12L);
        Assert.assertEquals(false, b);
    }

    //groupMemberInfos != null
    @Test
    public void removeMemberFromList_2() {
        InternalGroupInfo info = new ForGroupInfoTwo();
        boolean b = info.removeMemberFromList(12L);
        Assert.assertEquals(true, b);
    }

    //groupMemberUserIds = null
    @Test
    public void removeMemberIdFromIdList_1() {
        InternalGroupInfo info = new InternalGroupInfo();
        List<Long> userId = new ArrayList<Long>();
        userId.add(88L);
        boolean b = info.removeMemberIdFromIdList(userId);
        Assert.assertEquals(true, b);
    }

    //groupMemberUserIds != null
    @Test
    public void removeMemberIdFromIdList_2() {
        InternalGroupInfo info = new ForGroupInfoFour();
        List<Long> userId = new ArrayList<Long>();
        userId.add(88L);
        boolean b = info.removeMemberIdFromIdList(userId);
        Assert.assertEquals(true, b);
    }
}

class ForGroupInfo extends InternalGroupInfo {

    public ForGroupInfo() {
        groupMemberInfos = new Vector<UserInfo>();
        UserInfo info = PowerMockito.mock(UserInfo.class);
        PowerMockito.when(info.getUserName()).thenReturn("username");
        PowerMockito.when(info.getAppKey()).thenReturn("appKey");
        groupMemberInfos.add(info);
    }
}

class ForGroupInfoTwo extends InternalGroupInfo {

    public ForGroupInfoTwo() {
        groupMemberInfos = new Vector<UserInfo>();
        UserInfo info = PowerMockito.mock(UserInfo.class);
        PowerMockito.when(info.getUserID()).thenReturn(12L);
        groupMemberInfos.add(info);
    }
}

class ForGroupInfoFour extends InternalGroupInfo {
    public ForGroupInfoFour() {
        groupMemberUserIds = new HashSet<Long>();
        groupMemberUserIds.add(12L);
    }
}