package io.rong.imkit.interfaces;

/**
 * Created by liguangchun on 2018/1/23.
 */

public class CommonInterfaces {

    /*消息模块群组的点击监听*/
    private static IClickMessage iClickMessage;

    public static IClickMessage getIClickMessageGroup() {
        return iClickMessage;
    }

    public static void setIClickMessageGroup(IClickMessage iClickMessage) {
        CommonInterfaces.iClickMessage = iClickMessage;
    }


}
