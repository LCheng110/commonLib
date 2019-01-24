package cn.jpush.im.android.utils;

import cn.jiguang.ald.api.BaseLogger;

public class JMessageLogger  extends BaseLogger{

    private static final String COMMON_TAG = "JMessage";
    @Override
    public String getCommonTag() {
        return COMMON_TAG;
    }
}
