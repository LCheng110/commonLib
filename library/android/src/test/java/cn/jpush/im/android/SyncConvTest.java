package cn.jpush.im.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cn.jpush.im.android.pushcommon.proto.common.imcommands.IMProtocol;
import cn.jpush.im.android.utils.Logger;

public class SyncConvTest extends BaseTest {
    private static final String TAG = "SyncConvTest";

    List<IMProtocol> protocolList = new ArrayList<IMProtocol>();

    public SyncConvTest() {
    }


    @Test
    public void convPageReceivedTest(){
        Map<String ,Integer> map = new HashMap<String, Integer>();

        map.put("a",1);
        map.put("b",1);
        map.put("c",1);
        map.put("d",1);
        map.put("e",1);

        Set<String> keySet = map.keySet();

        List<String> list = new ArrayList<String>();
        list.add("c");
        list.add("d");
        list.add("g");
        list.add("h");

        keySet.removeAll(list);

        Logger.d(TAG,"now keySet is . " + keySet);
        Logger.d(TAG,"now map is . " + map);
    }

}
