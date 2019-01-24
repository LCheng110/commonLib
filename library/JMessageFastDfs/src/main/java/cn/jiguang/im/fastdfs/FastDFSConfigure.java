package cn.jiguang.im.fastdfs;

/**
 * Created by xiongtc on 2017/3/21.
 */

public interface FastDFSConfigure {

    interface DevConfig {
        int connect_timeout = 10;
        int network_timeout = 30;
        String charset = "UTF-8";
        int tracker_http_port = 8080;
        boolean anti_steal_token = false;
        String secret_key = "FastDFS1234567890";
        //        String tracker_server = "121.46.25.204:22122";
        String tracker_server_host = "121.46.25.204";
        int tracker_server_port = 22122;

        String storage_for_upload_host = null;
        int storage_for_upload_port = -1;

        String storage_for_download_host = null;
        int storage_for_download_port = -1;
        String storage_for_download_prefix = null;

//        String storage_for_upload_host = "121.46.25.204";
//        int storage_for_upload_port = 23000;

//        String storage_for_download_host = "www.baidu.com";
//        int storage_for_download_port = 8080;
    }

    interface RichPush {
        interface STATE_CODE {
            int SYNC_TIMEOUT_DEFAULT = 15;
            int SYNC_TIMEOUT_ERROR = -1;
            int SYNC_SUCCESS = 0;
        }
    }

}
