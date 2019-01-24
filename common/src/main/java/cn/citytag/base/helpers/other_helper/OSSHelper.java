package cn.citytag.base.helpers.other_helper;

import com.alibaba.sdk.android.oss.ClientConfiguration;
import com.alibaba.sdk.android.oss.OSS;
import com.alibaba.sdk.android.oss.OSSClient;
import com.alibaba.sdk.android.oss.common.OSSLog;
import com.alibaba.sdk.android.oss.common.auth.OSSCredentialProvider;
import com.alibaba.sdk.android.oss.common.auth.OSSStsTokenCredentialProvider;
import com.alibaba.sdk.android.oss.model.ObjectMetadata;
import com.alibaba.sdk.android.oss.model.PutObjectRequest;
import com.alibaba.sdk.android.oss.model.PutObjectResult;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import cn.citytag.base.app.BaseModel;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.model.OSSModel;
import cn.citytag.base.network.HttpClient;
import cn.citytag.base.utils.DateUtil;
import cn.citytag.base.utils.L;
import cn.citytag.base.utils.StringUtils;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.ObservableSource;
import io.reactivex.functions.Function;

/**
 * Created by yangfeng01 on 2017/12/5.
 * 阿里云OSS上传下载文件
 * 日志文件位置在内置sd卡路径\OSSLog\logs.csv
 * bucket-name:bubble-video,bubble-img
 * https://help.aliyun.com/document_detail/32047.html?spm=5176.10695662.1996646101.searchclickresult.7d12a7bb0a7nZr
 */

public class OSSHelper {

    private static volatile OSSHelper sOSSHelper;

    private static final String TAG = "OSSHelper";
    public static final String END_POINT = "http://oss-cn-hangzhou.aliyuncs.com";
    public static final String END_PIINT_AUDIO = "http://oss-cn-shanghai.aliyuncs.com";
    public static final String OSS_TYPE_IMAGE = "image";
    public static final String OSS_TYPE_VIDEO = "video";
    public static final String BUCKET_NAME_IMG = "bubble-img";
    public static final String BUCKET_NAME_VIDEO = "bubble-video";
    public static final String BUCKET_NAME_AUDIO = "bubble-audio";

    private OSSCredentialProvider credentialProvider;
    private ClientConfiguration clientConfiguration;
    private OSS oss;

    private OSSModel ossModel;

    private String accessKeyId;
    private String secretKeyId;
    private String securityToken;
    private String Expiration;
    private boolean isUpAudio = false;

    public static OSSHelper getInstance() {
        return getInstance(null);
    }

    public static OSSHelper getInstance(OSSModel ossModel) {

//		if (sOSSHelper == null) {
//			synchronized (OSSHelper.class) {
//				if (sOSSHelper == null) {
        sOSSHelper = new OSSHelper(ossModel);
//				}
//			}
//		}
//
        return sOSSHelper;
    }

    private OSSHelper(OSSModel ossModel) {
        ensureOss(ossModel);
    }

    private void ensureOss(OSSModel ossModel) {
        if (ossModel == null)
            return;
        this.ossModel = ossModel;
        this.accessKeyId = ossModel.getAccessKeyId();
        this.secretKeyId = ossModel.getAccessKeySecret();
        this.securityToken = ossModel.getSecurityToken();
        this.Expiration = ossModel.getExpiration();

        credentialProvider = new OSSStsTokenCredentialProvider(accessKeyId, secretKeyId, securityToken);
        clientConfiguration = new ClientConfiguration();    //该配置类如果不设置，会有默认配置，具体可看该类
        clientConfiguration.setConnectionTimeout(60 * 1000);    // 连接超时，默认15秒
        clientConfiguration.setSocketTimeout(30 * 1000);    // socket超时，默认15秒
        clientConfiguration.setMaxConcurrentRequest(5);    // 最大并发请求数，默认5个
        clientConfiguration.setMaxErrorRetry(2);    // 失败后最大重试次数，默认2次
        if (BaseConfig.isDebug()) {
            OSSLog.enableLog();  //调用此方法即可开启日志，默认不开启
        }


    }

    /**
     * OSS同步上传多张图片文件
     *
     * @param filePaths
     * @return objectKey    2017-12/bubble-img/1513860005360.jpg
     * @throws Exception
     */
    public Observable<List<String>> uploadImageSync(List<String> filePaths) throws Exception {
        oss = new OSSClient(BaseConfig.getContext(), END_POINT, credentialProvider, clientConfiguration);
        List<String> imageKeys = new ArrayList<>();
        for (String filePath : filePaths) {
            String imageKey = uploadImageSync(filePath);
            imageKeys.add(imageKey);
        }
        return Observable.just(imageKeys);
    }


    public Observable<String> uploadImage(String filePath) {
        oss = new OSSClient(BaseConfig.getContext(), END_POINT, credentialProvider, clientConfiguration);
        if (isTokenInvalid()) {
            return HttpClient.getApi(ComApi.class)
                    .getStsToken()
                    .concatMap((Function<BaseModel<OSSModel>, ObservableSource<String>>) model -> {
                        OSSModel ossModel = model.getData();
                        OSSHelper.this.ossModel = ossModel;
                        ensureOss(ossModel);

                        String imageKey = uploadImageSync(filePath);
                        return Observable.just(imageKey);
                    });
        } else {
            return Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> e) throws Exception {
                    String imageKey = uploadImageSync(filePath);
                    e.onNext(imageKey);
                    e.onComplete();
                }
            });
        }
    }

    /**
     * OSS同步上传单张图片文件
     *
     * @param uploadFilePath
     * @return objectKey    2017-12/bubble-img/1513860005360.jpg
     * @throws Exception
     */
    public String uploadImageSync(String uploadFilePath) throws Exception {
        oss = new OSSClient(BaseConfig.getContext(), END_POINT, credentialProvider, clientConfiguration);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        String[] strings = uploadFilePath.split("\\.");
        String suffix = ".jpg";
        if (strings.length > 0) {
            suffix = "." + strings[strings.length - 1];
        }

        final String objectKey = year + "-" + month + "/" + BUCKET_NAME_IMG + "/" + DateUtil.getTimestamp() + suffix;
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME_IMG, objectKey, uploadFilePath);    //
        // 构造上传请求

        PutObjectResult putObjectResult = oss.putObject(putObjectRequest);
        L.d("PutObject", "UploadSuccess");
        L.d(TAG, putObjectResult.getETag());
        L.d(TAG, putObjectResult.getRequestId());
        return objectKey;
    }

    public Observable<String> uploadVideo(String filePath) {
        oss = new OSSClient(BaseConfig.getContext(), END_POINT, credentialProvider, clientConfiguration);
        if (isTokenInvalid()) {
            return HttpClient.getApi(ComApi.class)
                    .getStsToken()
                    .concatMap((Function<BaseModel<OSSModel>, ObservableSource<String>>) model -> {
                        OSSModel ossModel = model.getData();
                        OSSHelper.this.ossModel = ossModel;
                        ensureOss(ossModel);

                        String videoKey = uploadVideoSync(filePath);
                        return Observable.just(videoKey);
                    });
        } else {
            return Observable.create(new ObservableOnSubscribe<String>() {
                @Override
                public void subscribe(ObservableEmitter<String> e) throws Exception {
                    String videoKey = uploadVideoSync(filePath);
                    e.onNext(videoKey);
                    e.onComplete();
                }
            });
        }
    }

    /**
     * OSS同步上传单个视频
     *
     * @param uploadFilePath
     * @return
     * @throws Exception
     */
    public String uploadVideoSync(String uploadFilePath) throws Exception {
        oss = new OSSClient(BaseConfig.getContext(), END_POINT, credentialProvider, clientConfiguration);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;

        final String objectKey = year + "-" + month + "/" + BUCKET_NAME_VIDEO + "/" + DateUtil.getTimestamp() + ".mp4";
        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME_VIDEO, objectKey, uploadFilePath);    //
        // 构造上传请求
        PutObjectResult putObjectResult = oss.putObject(putObjectRequest);
        //L.d("PutObject", "UploadSuccess");
        //L.d(TAG, putObjectResult.getETag());
        //L.d(TAG, putObjectResult.getRequestId());
        return objectKey;
    }

    /**
     * OSS 同步上传音频
     */
    public String uploadAudioSync(String uploadFilePath) throws Exception {
        oss = new OSSClient(BaseConfig.getContext(), END_PIINT_AUDIO, credentialProvider, clientConfiguration);
        File file = new File(uploadFilePath);
        Calendar cal = Calendar.getInstance();
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH)+1;

        final String objectKey = year + "-" + month + "/" + BUCKET_NAME_AUDIO + "/" + DateUtil.getTimestamp() + ".mp3";
        ObjectMetadata metadata = new ObjectMetadata();
        // 指定Content-Type
        metadata.setContentType("audio/mp3");

        PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME_AUDIO, objectKey, uploadFilePath);
//        putObjectRequest.setMetadata(metadata);
        // 构造上传请求
        PutObjectResult putObjectResult = oss.putObject(putObjectRequest);
        //L.d("PutObject", "UploadSuccess");
        //L.d(TAG, putObjectResult.getETag());
        //L.d(TAG, putObjectResult.getRequestId());
        return objectKey;
    }

    /**
     * 临时凭证是否失效
     */
    private boolean isTokenInvalid() {
        try {
            if (ossModel == null)
                return true;
            if (oss == null)
                return true;
            String expirationUtc = ossModel.getExpiration();
            if (StringUtils.isEmpty(expirationUtc))
                return true;
            Date expiration = DateUtil.formatUtc2Sdf(expirationUtc);
            return DateUtil.before(expiration, new Date());
        } catch (Exception e) {
            e.printStackTrace();
            return true;
        }
    }

}
