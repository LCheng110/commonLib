package cn.jpush.im.android;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import cn.jpush.im.android.utils.Logger;

import static junit.framework.Assert.assertEquals;

public class SeperateListTest extends BaseTest {
    private static final String TAG = "SeperateListTest";

    private static final int PAGE_SIZE = 50;
    private List<List<Integer>> parts = new ArrayList<List<Integer>>();
    private List<Integer> uids = new ArrayList<Integer>();


    @Test
    public void testSeperate() {
        int size = 501;
        for (int i = 0; i < size; i++) {
            uids.add(i);
        }
        seperateUidList();
        int expectedParts;
        if (0 == size % PAGE_SIZE) {
            expectedParts = size / PAGE_SIZE;
        } else {
            expectedParts = size / PAGE_SIZE + 1;
        }
        assertEquals(expectedParts, parts.size());
    }

    private void seperateUidList() {
        int listSize = uids.size();
        if (PAGE_SIZE >= listSize) {
            parts.add(uids);
            return;
        }

        int curIndex = 0;
        int curPage = 0;
        int end;
        do {
            if ((curPage + 1) * PAGE_SIZE >= listSize) {
                end = listSize;
            } else {
                end = (curPage + 1) * PAGE_SIZE;
            }
            parts.add(uids.subList(curIndex, end));
            Logger.d(TAG, "part = " + uids.subList(curIndex, end));
            curIndex = end;
            curPage++;
        } while (curIndex < listSize);

    }
}
