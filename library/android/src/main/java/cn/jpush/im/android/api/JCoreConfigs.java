package cn.jpush.im.android.api;

import android.content.Context;

import java.util.List;

import cn.jiguang.ald.api.JCoreInterface;

/**
 * Created by hxhg on 2018/3/9.
 */

public class JCoreConfigs {
    /**
     * 自定义sis域名，这里和sisIps都传空集合表示不做sis操作，都传null表示使用默认域名和ip
     **/
    public List<String> sisHosts;
    /**
     * 自定义sis ip列表，这里和sisHosts都传空集合表示不做sis操作，都传null表示使用默认ip
     **/
    public List<String> sisIps;
    /**
     * 自定义sis端口号
     **/
    public int sisPort;
    /**
     * 自定义接入域名,传空串表示不使用默认值，传null表示使用默认域名,host优先级高于ip
     **/
    public String defaultHost = "";
    /**
     * 自定义接入ip，传空串表示不使用默认值，传null表示使用默认ip
     **/
    public String defaultIp = "";
    /**
     * 自定义接入端口号
     **/
    public int defaultPort;
    /**
     * 自定义上报url，传空串表示不使用默认值，传null表示使用默认url
     **/
    public String reportUrl;

    public void apply(Context context, boolean reconnect) {
        JCoreInterface.setConnectionConfig(context, sisHosts, sisIps, sisPort, defaultHost, defaultIp, defaultPort, reportUrl, reconnect);
    }
}
