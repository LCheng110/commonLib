package cn.jpush.im.android.api;

import android.content.Context;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.JMessage;
import cn.jpush.im.android.api.event.LoginStateChangeEvent;
import cn.jpush.im.android.eventbus.EventBus;

/**
 * Created by hxhg on 2018/3/9.
 */

public class JMessageConfigs {

    /**
     * 自定义im http api host&port
     */
    @Expose
    public String httpIp;
    @Expose
    public int httpPort;
    @Expose
    public int syncHttpPort;
    @Expose
    public String sdkApiPathPrefix;
    @Expose
    public String syncApiPathPrefix;

    /**
     * fastDfs tracker配置
     **/
    @Expose
    public String fastDfsTrackerHost;
    @Expose
    public int fastDfsTrackerPort;
    @Expose
    public int fastDfsTackerHttpPort;

    /**
     * fastDFS 自定义storage for upload ,host port配置
     **/
    @Expose
    public String fastDfsStorageHostForUpload;
    @Expose
    public int fastDfsStoragePortForUpload;

    /**
     * fastDFS 自定义storage for download ,host port配置
     **/
    @Expose
    public String fastDfsStorageHostForDownload;
    @Expose
    public int fastDfsStoragePortForDownload;
    @Expose
    public String fastDfsStoragePrefixForDownload;


    public void apply(Context context) {
        if (null != JMessageClient.getMyInfo()) {
            EventBus.getDefault().post(new LoginStateChangeEvent(JMessageClient.getMyInfo(), LoginStateChangeEvent.Reason.user_logout));
            JMessageClient.logout();
        }
        JMessage.configHttpUrl(context, this, true);
    }

    @Override
    public String toString() {
        return "JMessageConfigs{" +
                "httpIp='" + httpIp + '\'' +
                ", httpPort=" + httpPort +
                ", syncHttpPort=" + syncHttpPort +
                ", sdkApiPathPrefix='" + sdkApiPathPrefix + '\'' +
                ", syncApiPathPrefix='" + syncApiPathPrefix + '\'' +
                ", fastDfsTrackerHost='" + fastDfsTrackerHost + '\'' +
                ", fastDfsTrackerPort=" + fastDfsTrackerPort +
                ", fastDfsTackerHttpPort=" + fastDfsTackerHttpPort +
                ", fastDfsStorageHostForUpload='" + fastDfsStorageHostForUpload + '\'' +
                ", fastDfsStoragePortForUpload=" + fastDfsStoragePortForUpload +
                ", fastDfsStorageHostForDownload='" + fastDfsStorageHostForDownload + '\'' +
                ", fastDfsStoragePortForDownload=" + fastDfsStoragePortForDownload +
                ", fastDfsStoragePrefixForDownload='" + fastDfsStoragePrefixForDownload + '\'' +
                '}';
    }
}
