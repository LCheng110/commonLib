package cn.citytag.base.interfaces;

/**
 * Created by liguangchun on 2018/1/28.
 */

public interface IMomentDetailPlayVideo {

    /*同时也包含了弹出软键盘*/
    void showSoftInput();

    void playVideo(String url);
}
