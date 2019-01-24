package jiguang.chat.utils;


import cn.citytag.base.network.HttpClient;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import jiguang.chat.api.IMApi;
import okhttp3.ResponseBody;

/**
 * Created by zhaoyuanchao on 2019/1/23.
 */
public class DownloadUtils {
    //文件下载
    public static void downloadFile(String fileUrl, Observer<ResponseBody> baseObserver) {
        HttpClient.getApi(IMApi.class)
                .downloadFile(fileUrl)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(baseObserver);
    }

}
