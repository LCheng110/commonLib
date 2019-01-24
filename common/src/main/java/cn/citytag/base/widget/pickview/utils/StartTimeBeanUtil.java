package cn.citytag.base.widget.pickview.utils;

import cn.citytag.base.widget.pickview.view.StartTimeBean;

/**
 * Created by liguangchun on 2018/2/1.
 */

public class StartTimeBeanUtil {

    public static int DEFAULT_INDEX = 8;

    private static volatile StartTimeBean instance = null;

    public static StartTimeBean getInstance() {
        if (instance == null) {
            synchronized (StartTimeBeanUtil.class) {
                if (instance == null) {
                    instance = new StartTimeBean();
                }
            }
        }
        return instance;
    }
}
