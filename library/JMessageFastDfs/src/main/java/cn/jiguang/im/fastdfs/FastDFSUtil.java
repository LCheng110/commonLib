package cn.jiguang.im.fastdfs;

import org.csource.jiguang.common.MyException;
import org.csource.jiguang.fastdfs.ClientGlobal;
import org.csource.jiguang.fastdfs.FileInfo;
import org.csource.jiguang.fastdfs.ProtoCommon;
import org.csource.jiguang.fastdfs.ServerInfo;
import org.csource.jiguang.fastdfs.StorageClient;
import org.csource.jiguang.fastdfs.StorageClient1;
import org.csource.jiguang.fastdfs.StorageServer;
import org.csource.jiguang.fastdfs.TrackerClient;
import org.csource.jiguang.fastdfs.TrackerServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

public class FastDFSUtil {
    private static ExecutorService executor = Executors.newSingleThreadExecutor();
    private TrackerServer trackerServer = null;
    private StorageServer storageServer = null;
    private TrackerClient fastClient = null;
    private StorageClient storageClient = null;
    public static org.csource.jiguang.common.NameValuePair[] meta_list;


    private FastDFSUtil() {
        try {
            ClientGlobal.init();
        } catch (IOException e) {
            throw new RuntimeException("FastDFS 异常");
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    public FastDFSUtil(String trackerHost, int trackerPort, int httpPort) {
        try {
            ClientGlobal.init(trackerHost, trackerPort, httpPort, null, -1, null, -1, null);
        } catch (IOException e) {
            throw new RuntimeException("FastDFS 异常");
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    public FastDFSUtil(String trackerHost, int trackerPort, int httpPort, String customUploadStorageHost, int customUploadStoragePort) {
        try {
            ClientGlobal.init(trackerHost, trackerPort, httpPort, customUploadStorageHost, customUploadStoragePort, null, -1, null);
        } catch (IOException e) {
            throw new RuntimeException("FastDFS 异常");
        } catch (MyException e) {
            e.printStackTrace();
        }
    }

    public FastDFSUtil(String trackerHost, int trackerPort, int httpPort, String customUploadStorageHost, int customUploadStoragePort
            , String customDownloadStorageHost, int customDownloadStoragePort, String customStoragePrefixForDownload) {
        try {
            ClientGlobal.init(trackerHost, trackerPort, httpPort, customUploadStorageHost, customUploadStoragePort,
                    customDownloadStorageHost, customDownloadStoragePort, customStoragePrefixForDownload);
        } catch (IOException e) {
            throw new RuntimeException("FastDFS 异常");
        } catch (MyException e) {
            e.printStackTrace();
        }
    }


    // Get download file ip
    StorageClient client = null;

    public String[] upFastdfsFileWithNewClient(byte[] data, String fileExt) throws Exception {
        try {
            client = getStorageClient();
        } catch (IOException e) {
            System.out.println("Connection FastDFS fail!");
            e.printStackTrace();
        }

        String[] results = null;
        String fileDownUrl = null;
        try {
            results = client.upload_file(data, fileExt, meta_list);
            fileDownUrl = getFastDFSDownUrl(results);
            System.out.println(String.format("fastDFS upload file success!ext:%s downloadurl:%s", fileExt, fileDownUrl));
        } catch (Exception e) {
            System.out.println("FastDFS: Upload file fail!");
            e.printStackTrace();
        }
        return results;
    }


    public String[] upFastdfsFileWithNewClient(String group_name, String master_filename, String prefix_name, byte[] data, String fileExt) throws Exception {
        try {
            client = getStorageClient();
        } catch (IOException e) {
            System.out.println("Connection FastDFS fail!");
            e.printStackTrace();
        }

        String[] results = null;
        String fileDownUrl = null;
        try {
            results = client.upload_file(group_name, master_filename, prefix_name, data, fileExt, null);
            if (null != results && 2 == Integer.valueOf(results[2])) {
                //upload error code is 2, try to upload again
                System.out.println("upload failed. error code is 2,try to upload again");
                Thread.sleep(10000L);
                client = getStorageClient();
                results = client.upload_file(group_name, master_filename, prefix_name, data, fileExt, null);
            }
            fileDownUrl = getFastDFSDownUrl(results);
            System.out.println(String.format("fastDFS upload file success!ext:%s downloadurl:%s", fileExt, fileDownUrl));
        } catch (Exception e) {
            System.out.println("FastDFS: Upload file fail!");
            e.printStackTrace();
        }
        return results;
    }

    public String[] upFastdfsFile(String group_name, String master_filename, String prefix_name, byte[] data, String fileExt) throws Exception {
        if (null == client) {
            System.out.println("client is null.failed to upload file");
            return null;
        }

        String[] results = null;
        String fileDownUrl = null;
        try {
            results = client.upload_file(group_name, master_filename, prefix_name, data, fileExt, null);
            if (null != results && 2 == Integer.valueOf(results[2])) {
                //upload error code is 2, try to upload again
                System.out.println("upload failed. error code is 2,try to upload again");
                Thread.sleep(10000L);
                client = getStorageClient();
                results = client.upload_file(group_name, master_filename, prefix_name, data, fileExt, null);
            }
            fileDownUrl = getFastDFSDownUrl(results);
            System.out.println(String.format("fastDFS upload file success!ext:%s downloadurl:%s", fileExt, fileDownUrl));
        } catch (Exception e) {
            System.out.println("FastDFS: Upload file fail!");
            e.printStackTrace();
        }
        return results;
    }

    // delete file
    public int deleteFastdfsFile(String groupName, String fileName) {
        StorageClient client = null;
        int result = 0;
        try {
            client = getStorageClient();
            try {
                result = client.delete_file(groupName, fileName);
            } catch (MyException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            System.out.println("Connection FastDFS fail!");
            e.printStackTrace();
        }
        System.out.println("delete fastdfs file result = " + result);
        return result;
    }

    public String[] getGroupFileName(String url) {
        String[] results = new String[2];
        url = getRootUrl(url, false);
        results[0] = url.substring(0, url.indexOf("/"));
        results[1] = url.substring(url.indexOf("/") + 1);
        return results;
    }

    /**
     * @param url
     * @param rootUrl true:顶级域名,false:获取参数
     * @return
     * @date 2012-12-20
     * @desc 获取顶级域名或者参数
     */
    public static String getRootUrl(String url, boolean rootUrl) {
        if (url == null || url.equals(""))
            return null;
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < url.length(); i++) {
            if (i > 2)
                break;
            int index = url.indexOf("/");
            if (index == -1 && i == 2) {
                buffer.append(url).append("/");
            } else {
                buffer.append(url.substring(0, index + 1));
            }
            String netxUrl = url.substring(index + 1);
            url = netxUrl;
        }
        if (rootUrl)
            return buffer.toString();
        else
            return url;
    }

    private StorageClient getStorageClient() throws IOException {
        fastClient = new TrackerClient();
        try {
            trackerServer = fastClient.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        storageServer = fastClient.getStoreStorage(trackerServer);
        return new StorageClient(trackerServer, storageServer);
    }

    public String getFastDFSDownUrl(String[] results) {
        if (null == results) {
            System.out.println("getFastDFSDownUrl failed,results is null");
            return null;
        }
        String group_name = results[0];
        String remote_filename = results[1];
        System.out.println("group name = " + group_name + " remote file name = " + remote_filename);
        String file_id = group_name + StorageClient1.SPLIT_GROUP_NAME_AND_FILENAME_SEPERATOR + remote_filename;
        StringBuilder file_url = new StringBuilder("http://");
        InetSocketAddress inetSockAddr;
        String hostAddress;
        int port;
        if (ClientGlobal.g_custom_storage_download) {
            //下载时走自定义的storage.
            inetSockAddr = new InetSocketAddress(ClientGlobal.g_custom_storage_download_host, ClientGlobal.g_custom_storage_download_port);
            port = inetSockAddr.getPort();
        } else {
            if (null == trackerServer) {
                fastClient = new TrackerClient();
                try {
                    trackerServer = fastClient.getConnection();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            inetSockAddr = this.trackerServer.getInetSocketAddress();
            port = ClientGlobal.g_tracker_http_port;//下载走tracker的http服务端口
        }

        hostAddress = inetSockAddr.getAddress().getHostAddress();
        file_url.append(hostAddress);
        if (port != 80) {
            file_url.append(":").append(port);
        }
        if (null != ClientGlobal.g_custom_storage_download_prefix) {//自定义下载url前缀
            file_url.append(ClientGlobal.g_custom_storage_download_prefix);
        }
        file_url.append("/" + file_id);
        if (ClientGlobal.g_anti_steal_token) {
            int ts = (int) (System.currentTimeMillis() / 1000);
            String token = null;
            try {
                token = ProtoCommon.getToken(file_id, ts, ClientGlobal.g_secret_key);
            } catch (Exception e) {
                e.printStackTrace();
            }
            file_url.append("?token=" + token + "&ts=" + ts);
        }
        return file_url.toString();
    }

    public static void getFetchS(String downloadUrl, String groupName, String fileName) {
        FastDFSUtil f = new FastDFSUtil();
        TrackerClient client = new TrackerClient();
        try {
            ServerInfo[] servers = client.getFetchStorages(f.trackerServer, groupName, fileName);
            client.getConnection().close();
            System.out.println("servers count = " + servers.length);
            for (int k = 0; k < servers.length; k++) {
                System.out.println(k + 1 + ". " + servers[k].getIpAddr() + ":" + servers[k].getPort());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int executorFileSync(String downloadUrl) {
        List<String> downloadUrlList = new ArrayList<String>();
        downloadUrlList.add(downloadUrl);
        return executorFilesSync(downloadUrlList);
    }

    public int executorFilesSync(List<String> downloadUrlList) {
        long startTime = System.currentTimeMillis();
        List<FutureTask<Integer>> futureTasks = new ArrayList<FutureTask<Integer>>();
        executor = Executors.newFixedThreadPool(downloadUrlList.size());
        for (int i = 0; i < downloadUrlList.size(); i++) {
            FutureTask<Integer> future = new FutureTask<Integer>(new FileSyncService(downloadUrlList.get(i)));
            futureTasks.add(future);
            executor.submit(future);
        }
        int errCode = FastDFSConfigure.RichPush.STATE_CODE.SYNC_TIMEOUT_ERROR;
        for (FutureTask<Integer> future : futureTasks) {
            try {
                errCode = future.get(FastDFSConfigure.RichPush.STATE_CODE.SYNC_TIMEOUT_DEFAULT, TimeUnit.SECONDS);
            } catch (Exception e) {
                errCode = FastDFSConfigure.RichPush.STATE_CODE.SYNC_TIMEOUT_ERROR;
            } finally {
                if (FastDFSConfigure.RichPush.STATE_CODE.SYNC_TIMEOUT_ERROR == errCode) {
                    executor.shutdown();
                    break;
                }
            }
        }
        executor.shutdownNow();
        System.out.println(String.format("executorFilesSync file count[%s],errCode[%s],Time[%s]ms", downloadUrlList.size(),
                errCode, System.currentTimeMillis() - startTime));
        return errCode;
    }

    class FileSyncService implements Callable<Integer> {
        private String downloadUrl;

        public FileSyncService(String downloadUrl) {
            this.downloadUrl = downloadUrl;
        }

        @Override
        public Integer call() throws Exception {
            int errCode = FastDFSConfigure.RichPush.STATE_CODE.SYNC_TIMEOUT_ERROR;
            String path = downloadUrl.replace("http://", "");
            path = path.substring(path.indexOf("/") + 1);
            String group_name = path.substring(0, path.indexOf("/"));
            String remote_filename = path.substring(path.indexOf("/") + 1);
            System.out.println("group_name: " + group_name + ", remote_filename: " + remote_filename);
            TrackerClient trackerClient = new TrackerClient();
            TrackerServer trackerServer = null;
            List<StorageServer> storageServers = null;
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    if (trackerServer == null)
                        trackerServer = trackerClient.getConnection();
                    if (storageServers == null)
                        storageServers = new ArrayList<StorageServer>(Arrays.asList(trackerClient.getStoreStorages(
                                trackerServer, group_name)));
                    for (int i = 0; i < storageServers.size(); i++) {
                        StorageServer tempStorageServer = storageServers.get(i);
                        storageClient = new StorageClient(trackerServer, tempStorageServer);
                        FileInfo fileInfo = null;
                        for (int repeat = 5; repeat > 0; repeat--) {
                            fileInfo = storageClient.query_file_info(group_name, remote_filename);
                            if (fileInfo != null) {
                                System.out.println("store storage server: " + tempStorageServer.getInetSocketAddress()
                                        + " fileinfo: " + fileInfo);
                                storageServers.remove(i);
                                break;
                            }
                        }
                    }
                    if (storageServers.isEmpty()) {
                        errCode = FastDFSConfigure.RichPush.STATE_CODE.SYNC_SUCCESS;
                        System.out.println(String.format("file [%s] sync success.", remote_filename));
                        break;
                    }
                } catch (Exception e) {
                    System.out.println(String.format("sync storage server file exception:Filepath[%s],Exception[%s]",
                            downloadUrl, e));
                }
            }
            return errCode;
        }
    }

    public static void main(String[] args) {
        //ios qiniu sdk url:http://richpush.qiniudn.com/sdk-JPush-iOS-SDK-1.6.3.zip
        //ios fastdfs sdk url:http://fastdfs.jpush.cn:8080/push01/M00/00/58/bw0wIVLfhxiAeuquABv3Ruvbcr0071.zip
        try {
            FastDFSUtil fastDFSUtil = new FastDFSUtil();
            byte[] data = "master file".getBytes();
            byte[] subData1 = "sub file1".getBytes();
            byte[] subData2 = "sub file2".getBytes();
            long startTime = System.currentTimeMillis();
            System.out.println("upload start ");
            String[] results = fastDFSUtil.upFastdfsFileWithNewClient(data, "txt");
            String groupName = results[0];
            String masterFileName = results[1];
            System.out.println("masterFileName = " + masterFileName);
            fastDFSUtil.upFastdfsFile(groupName, masterFileName, "sub_1",
                    subData1, "txt");
            fastDFSUtil.upFastdfsFile(groupName, masterFileName, "sub_2",
                    subData2, "txt");
            System.out.println("upload finish . lasts " + (System.currentTimeMillis() - startTime));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}