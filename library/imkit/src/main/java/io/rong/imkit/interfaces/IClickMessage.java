package io.rong.imkit.interfaces;

/**
 * Created by liguangchun on 2018/1/23.
 */

public interface IClickMessage {

    /*
    *点击消息模块的群组进行群组
    *
    * */
    void clickMessageGroup();

    /*
    * 活动消息
    * */
    void clickMessageActivity();

    /**
     * 互动消息
     */
    void clickMessageInteraction();

    /**
     * 系统消息
     */
    void clickMessageSystem();
}
