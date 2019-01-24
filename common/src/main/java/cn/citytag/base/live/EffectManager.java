package cn.citytag.base.live;

import android.app.Application;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;

import com.tencent.rtmp.ITXLivePushListener;
import com.tencent.rtmp.TXLiveConstants;
import com.tencent.rtmp.TXLivePushConfig;
import com.tencent.rtmp.TXLivePusher;
import com.tencent.rtmp.ui.TXCloudVideoView;

/**
 * 作者：M. on 2018/11/16 14:36
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 * <p>
 * 美颜 滤镜 动效 管理Manager
 */
public class EffectManager implements TXLivePusher.VideoCustomProcessListener, ITXLivePushListener {
    private static final int EFFECT_BEAUTY_DEFAULT = 5;
    private static final int VIDEO_QUALITY_DEFAULT = TXLiveConstants.VIDEO_QUALITY_HIGH_DEFINITION;     //高清
    private static final boolean VIDEO_ADJUSTBITRATE_DEFAULT = false;     //动态码率开关
    private static final boolean VIDEO_ADJUSTRESOLUTION_DEFAULT = false;     //动态切分辨率开关
    private Context mContext;
    private TXLivePusher mTXLivePusher;
    private TXLivePushConfig mTXLivePushConfig;

    private int mBeautyLevel = 5;           //默认美颜等级
    private int mWhiteningLevel = 5;        //默认美白等级

    private OnProcessCallback mOnProcessCallback;


    public EffectManager() {
    }

    private static class SingletonHolder {
        private static EffectManager instance = new EffectManager();
    }

    public static EffectManager get() {
        return SingletonHolder.instance;
    }

    public void init(Application application) {
        mContext = application.getApplicationContext();
    }

    /**
     * 开始前调用
     */
    public void prepare() {
        prepare(new TXLivePusher(mContext));
    }

    /**
     * 开始前调用
     */
    public void prepare(TXLivePusher pusher) {
        mTXLivePusher = pusher;
        initDefaultPushConfig(pusher);
        mTXLivePusher.setVideoQuality(VIDEO_QUALITY_DEFAULT, VIDEO_ADJUSTBITRATE_DEFAULT, VIDEO_ADJUSTRESOLUTION_DEFAULT);
        mTXLivePusher.setVideoProcessListener(this);
        mTXLivePusher.setPushListener(this);
    }

    private void initDefaultPushConfig(TXLivePusher pusher) {
        mTXLivePushConfig = new TXLivePushConfig();
        mTXLivePushConfig.setTouchFocus(false);
        pusher.setConfig(mTXLivePushConfig);
    }

    /**
     * 开启预览界面
     *
     * @param videoView 当前采用 腾讯云的美颜 配合声网的数据传输，所以在本地展示美颜效果的时候必须使用腾讯的TXCloudVideoView
     */
    public void startPreView(@NonNull TXCloudVideoView videoView) {
        if (mTXLivePusher != null) {
            mTXLivePusher.startCameraPreview(videoView);
            //开启默认美颜
            onEyeScaleLevel(EFFECT_BEAUTY_DEFAULT);
            onFaceSlimLevel(EFFECT_BEAUTY_DEFAULT);
            onFaceVLevel(EFFECT_BEAUTY_DEFAULT);
            onChinLevel(EFFECT_BEAUTY_DEFAULT);
            onFaceShortLevel(EFFECT_BEAUTY_DEFAULT);
            onNoseSlimLevel(EFFECT_BEAUTY_DEFAULT);
            onBeautyLevel(EFFECT_BEAUTY_DEFAULT);
            onWhiteningLevel(EFFECT_BEAUTY_DEFAULT);
        }
    }

    /**
     * setFilterImage 设置指定素材滤镜特效
     *
     * @param bmp: 指定素材，即颜色查找表图片。注意：一定要用png图片格式！！！
     *             demo用到的滤镜查找表图片位于RTMPAndroidDemo/app/src/main/res/drawable-xxhdpi/目录下。
     */
    public void setFilter(Bitmap bmp) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFilter(bmp);
        }
    }

    /**
     * 大眼
     *
     * @param eyeScaleLevel
     */
    public void onEyeScaleLevel(int eyeScaleLevel) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setEyeScaleLevel(eyeScaleLevel);
        }
    }

    /**
     * 瘦脸
     *
     * @param faceSlimLevel
     */
    public void onFaceSlimLevel(int faceSlimLevel) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFaceSlimLevel(faceSlimLevel);
        }
    }

    /**
     * V脸
     *
     * @param level
     */
    public void onFaceVLevel(int level) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFaceVLevel(level);
        }
    }

    /**
     * 下巴拉伸或收缩效果
     *
     * @param scale
     */
    public void onChinLevel(int scale) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setChinLevel(scale);
        }
    }

    /**
     * 缩脸效果
     *
     * @param level
     */
    public void onFaceShortLevel(int level) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setFaceShortLevel(level);
        }
    }

    /**
     * 瘦鼻效果
     *
     * @param scale
     */
    public void onNoseSlimLevel(int scale) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setNoseSlimLevel(scale);
        }
    }

    /**
     * 设置美颜等级
     *
     * @param beautyLevel 美颜等级.美颜等级即 beautyLevel 取值为0-9.取值为0时代表关闭美颜效果.默认值:0,即关闭美颜效果.
     */
    public void onBeautyLevel(int beautyLevel) {
        if (mTXLivePusher != null) {
            mBeautyLevel = beautyLevel;
            mTXLivePusher.setBeautyFilter(0, beautyLevel, mWhiteningLevel, 0);
        }
    }

    /**
     * 设置美白等级
     *
     * @param whiteningLevel 美白等级.美白等级即 whiteningLevel 取值为0-9.取值为0时代表关闭美白效果.默认值:0,即关闭美白效果.
     */
    public void onWhiteningLevel(int whiteningLevel) {
        if (mTXLivePusher != null) {
            mWhiteningLevel = whiteningLevel;
            mTXLivePusher.setBeautyFilter(0, mBeautyLevel, whiteningLevel, 0);
        }
    }

    /**
     * 设置动效
     *
     * @param path //动效 文件 路径
     */
    public void setMotionTmpl(String path) {
        if (mTXLivePusher != null) {
            mTXLivePusher.setMotionTmpl(path);
        }
    }

    /**
     * 关闭预览 并销毁数据
     */
    public void stopPreView() {
        if (mTXLivePusher != null) {
            mTXLivePusher.stopCameraPreview(true);//停止摄像头预览
            mTXLivePusher.setPushListener(null);    //解绑 listener
            mTXLivePusher.stopPusher();
            mTXLivePusher = null;
        }
    }

    public OnProcessCallback getOnProcessCallback() {
        return mOnProcessCallback;
    }

    public void setOnProcessCallback(OnProcessCallback mOnProcessCallback) {
        this.mOnProcessCallback = mOnProcessCallback;
    }

    @Override
    public void onPushEvent(int event, Bundle bundle) {
        String pushEventLog = "receive onPushEvent: " + event + ", " + bundle.getString(TXLiveConstants.EVT_DESCRIPTION);
        Log.d("qhm", pushEventLog);
    }

    @Override
    public void onNetStatus(Bundle bundle) {

    }

    @Override
    public int onTextureCustomProcess(int textureId, int width, int height) {
        if (mOnProcessCallback != null) {
            mOnProcessCallback.onTextureCustomProcess(textureId, width, height);
        }
        return textureId;
    }

    @Override
    public void onDetectFacePoints(float[] floats) {

    }

    @Override
    public void onTextureDestoryed() {

    }

    public interface OnProcessCallback {
        void onTextureCustomProcess(int textureId, int width, int height);
    }

}
