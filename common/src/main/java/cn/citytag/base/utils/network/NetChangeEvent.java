package cn.citytag.base.utils.network;

/**
 * Author: Lusheast
 * E-mail：zhangxiaoyu@maopp.cn
 * Date: on 2019/1/14 09:48
 * Desc:跟网络相关的工具类
 */
public class NetChangeEvent {
    /**
     * 没有连接网络
     */
    public static final int NETWORK_NONE = -1;
    /**
     * 移动网络
     */
    public static final int NETWORK_MOBILE = 0;
    /**
     * 无线网络
     */
    public static final int NETWORK_WIFI = 1;


    private int netWorkState = NETWORK_NONE;

    public NetChangeEvent(int networkWifi) {
        this.netWorkState = networkWifi;
    }

    public int getNetWorkState() {
        return netWorkState;
    }

    public void setNetWorkState(int netWorkState) {
        this.netWorkState = netWorkState;
    }
}
