package cn.citytag.base.live;

import android.app.Application;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import cn.citytag.base.config.BaseConfig;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.agora.rtc.mediaio.IVideoFrameConsumer;
import io.agora.rtc.mediaio.IVideoSource;
import io.agora.rtc.mediaio.MediaIO;
import io.agora.rtc.video.AgoraVideoFrame;
import io.agora.rtc.video.VideoCanvas;
import io.agora.rtc.video.VideoEncoderConfiguration;

/**
 * 作者：M. on 2018/11/16 14:36
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 * <p>
 * 即时通讯Manager
 * 当前支持业务
 * 1.电台
 * 2.1V1语音闪聊
 * 3.1V1视频闪聊
 * 当前默认三种方式均引用 创建一个直播间的，分别以不同的身份加入直播间的场景
 */
public class LiveManager {

    public static final String AGORA_APP_ID = "656ed1b4e76a44ea83a89a16c9f00dea";

    // 1V1 语音聊天场景
    public static final int LIVE_MODE_AUDIO = Constants.CHANNEL_PROFILE_COMMUNICATION;
    // 1v1 视频视频场景  1vN  N V N 电台场景 PS：由于需要TX的美颜结合声网的推流，涉及texture 必须使用 视频模式
    public static final int LIVE_MODE_VIDEO = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING;

    // 主播角色 即音视频会被采集，并推送
    public static final int LIVE_USER_ROLE_BROADCASTER = Constants.CLIENT_ROLE_BROADCASTER;
    // 观众角色 即音视频均不会会被采集且推送
    public static final int LIVE_USER_ROLE_AUDIENCE = Constants.CLIENT_ROLE_AUDIENCE;

    // 优先保证视窗被填满。视频尺寸等比缩放，直至整个视窗被视频填满。如果视频长宽与显示窗口不同，多出的视频将被截掉。
    public static final int LIVE_RENDER_MODE_HIDDEN = VideoCanvas.RENDER_MODE_HIDDEN;
    // 优先保证视频内容全部显示。视频尺寸等比缩放，直至视频窗口的一边与视窗边框对齐。如果视频长宽与显示窗口不同，视窗上未被填满的区域将被涂黑。
    public static final int LIVE_RENDER_MODE_FIT = VideoCanvas.RENDER_MODE_FIT;

    public static final int USER_OFFLINE_QUIR = Constants.USER_OFFLINE_QUIT;                            //用户主动退出
    public static final int USER_OFFLINE_DROPPED = Constants.USER_OFFLINE_DROPPED;                      //异常断线
    public static final int USER_OFFLINE_BECOME_AUDIENCE = Constants.USER_OFFLINE_BECOME_AUDIENCE;      //变为观众

    private Context mContext;
    private RtcEngine mRtcEngine;               //声网 RtcEngine 对象
    private MpRtcEngineEventHandler mpRtcEngineEventHandler;        //RtcEventHandler 管理类
    private IVideoFrameConsumer mIVideoFrameConsumer;
    private boolean mVideoFrameConsumerReady;
    private int currentPushWidth;           //当前上传视频流宽度
    private int currentPushHeight;          //当前上传视频流高度
    private int currentMode = -1;            //当前场景
    private int currentUserRole = -1;        //当前用户角色
    private final float[] mtx = new float[]{
            1.0f, 0.0f, 0.0f, 0.0f,
            0.0f, 1.0f, 0.0f, 0.0f,
            0.0f, 0.0f, 1.0f, 0.0f,
            0.0f, 0.0f, 0.0f, 1.0f,
    };

    private long anchorId;
    private String anchorName;
    private long host;

    public LiveManager() {
    }

    private static class SingletonHolder {
        private static LiveManager instance = new LiveManager();
    }

    public static LiveManager get() {
        return SingletonHolder.instance;
    }

    public void init(Application application) {
        mContext = application.getApplicationContext();
    }

    /**
     * 预制声网需要的SDK
     */
    public void prepare() {
        mpRtcEngineEventHandler = new MpRtcEngineEventHandler(mContext);
        try {
            mRtcEngine = RtcEngine.create(mContext, AGORA_APP_ID, mpRtcEngineEventHandler.mRtcEventHandler);
        } catch (Exception e) {
            Log.e("qhm", Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }
        //开启完整的 声网的log日志
        //mRtcEngine.setParameters("{\"rtc.log_filter\":65535}");
    }

    /**
     * 初始化自定义视频流需要的 Consumer
     */
    public void prepareConsumer() {
        IVideoSource source = new IVideoSource() {
            @Override
            public boolean onInitialize(IVideoFrameConsumer iVideoFrameConsumer) {
                mIVideoFrameConsumer = iVideoFrameConsumer;
                Log.d("qhm", "onInitialize");
                return true;
            }

            @Override
            public boolean onStart() {
                mVideoFrameConsumerReady = true;
                Log.d("qhm", "onStart");
                return true;
            }

            @Override
            public void onStop() {
                mVideoFrameConsumerReady = false;
                Log.d("qhm", "onStop");
            }

            @Override
            public void onDispose() {
                mVideoFrameConsumerReady = false;
                Log.d("qhm", "onDispose");
            }

            @Override
            public int getBufferType() {
                Log.d("qhm", "getBufferType");
                return MediaIO.BufferType.TEXTURE.intValue();
            }
        };
        // 将输出流切换到刚创建的VideoSource实例
        mRtcEngine.setVideoSource(source);
    }

    /**
     * 设置当前所需的场景
     *
     * @param mode
     */
    public void setLiveMode(int mode) {
        if (currentMode != mode) {
            mRtcEngine.setChannelProfile(mode);
            currentMode = mode;
        }
    }

    /**
     * 设置用户角色
     */
    public void setUserRole(int userRole) {
        if (currentUserRole != userRole) {
            currentUserRole = userRole;
            mRtcEngine.setClientRole(userRole);
        }
    }

    /**
     * 加入房间
     *
     * @param token  token 校验
     * @param roomId 房间频道
     * @param msg    需要传递的消息
     * @param uid    用户Uid
     */
    public void joinRoom(String token, String roomId, String msg, int uid) {
        mRtcEngine.joinChannel(token, roomId, msg, uid);
    }

    /**
     *
     */
    public void renewToken(String token) {
        if (!TextUtils.isEmpty(token)) {
            mRtcEngine.renewToken(token);
        }
    }

    /**
     * 离开房间
     */
    public void leaveRoom() {
        mRtcEngine.leaveChannel();
    }

    /**
     * 开始推流
     */
    public void startPush(int textureId, int width, int height) {
        if (mIVideoFrameConsumer != null && mVideoFrameConsumerReady) {
            if (currentPushWidth != width || currentPushHeight != height) {
                currentPushWidth = width;
                currentPushHeight = height;
                setVideoDimens(currentPushWidth, currentPushHeight);
            }
            mIVideoFrameConsumer.consumeTextureFrame(textureId, AgoraVideoFrame.FORMAT_TEXTURE_2D,
                    width, height, 180, System.currentTimeMillis(), mtx);
        }
    }

    /***************************** 声网音频相关控制方法 ***********************************/

    /**
     * 启用音频模块（默认为开启状态）。
     */
    public void enableAudio() {
        mRtcEngine.enableAudio();
    }

    /**
     * 关闭音频模块（默认为开启状态）。
     */
    public void disableAudio() {
        mRtcEngine.disableAudio();
    }

    /**
     * 开/关本地音频采集。
     *
     * @param enabled true：重新开启本地语音功能，即开启本地语音采集或处理（默认）false：关闭本地语音功能，即停止本地语音采集或处理
     */
    public void enableLocalAudio(boolean enabled) {
        mRtcEngine.enableLocalAudio(enabled);
    }

    /**
     * 开/关本地音频发送。该方法用于允许/禁止往网络发送本地音频流。
     *
     * @param muted true：麦克风静音 false：取消静音（默认）
     */
    public void muteLocalAudioStream(boolean muted) {
        mRtcEngine.muteLocalAudioStream(muted);
    }

    /**
     * 接收/停止接收指定音频流。
     *
     * @param uid   指定的用户 ID
     * @param muted true：停止接收指定用户的音频流 false：继续接收指定用户的音频流（默认）
     *              <p>
     *              tips:如果之前有调用过 muteAllRemoteAudioStreams (true) 停止接收所有远端音频流，
     *              在调用本 API 之前请确保你已调用 muteAllRemoteAudioStreams (false)。
     *              muteAllRemoteAudioStreams 是全局控制，muteRemoteAudioStream 是精细控制。
     */
    public void muteRemoteAudioStream(int uid, boolean muted) {
        mRtcEngine.muteRemoteAudioStream(uid, muted);
    }

    /**
     * 接收/停止接收所有音频流。
     *
     * @param muted true：停止接收所有远端音频流 false：继续接收所有远端音频流（默认）
     */
    public void muteAllRemoteAudioStreams(boolean muted) {
        mRtcEngine.muteAllRemoteAudioStreams(false);
    }

    /**
     * 检查扬声器状态启用状态。
     *
     * @return true：扬声器已开启，语音会输出到扬声器 false：扬声器未开启，语音会输出到非扬声器（听筒，耳机等）
     */
    public boolean isSpeakerphoneEnabled() {
        return mRtcEngine.isSpeakerphoneEnabled();
    }

    /**
     * 开启耳返功能
     *
     * @param enabled true：开启耳返功能 false：关闭耳返功能(默认)
     */
    public void enableInEarMonitoring(boolean enabled) {
        mRtcEngine.enableInEarMonitoring(enabled);
    }

    /**
     * 设置耳返音量
     *
     * @param volume 取值范围在 0 到 100 间。默认值为 100。
     */
    public void setInEarMonitoringVolume(int volume) {
        mRtcEngine.setInEarMonitoringVolume(volume);
    }

    /**
     * 启用/关闭扬声器播放
     *
     * @param enabled true：切换到外放 false：切换到听筒
     */
    public void setEnableSpeakerphone(boolean enabled) {
        mRtcEngine.setEnableSpeakerphone(enabled);
    }

    /**
     * 启用说话者音量提示。
     *
     * @param interval 音量提示的时间间隔：
     * @param smooth   平滑系数：
     */
    public void enableAudioVolumeIndication(int interval, int smooth) {
        mRtcEngine.enableAudioVolumeIndication(interval, smooth);
    }

    /***************************** 声网音频相关控制方法 ***********************************/

    /***************************** 声网视频相关控制方法 ***********************************/

    /**
     * 设置 视频的编码属性 宽高
     *
     * @param width
     * @param height
     */
    public void setVideoDimens(int width, int height) {
        if (currentPushWidth == width && currentPushHeight == height) {
            return;
        }
        currentPushWidth = width;
        currentPushHeight = height;
        VideoEncoderConfiguration.VideoDimensions dimensions = new VideoEncoderConfiguration.VideoDimensions(width, height);
        mRtcEngine.setVideoEncoderConfiguration(new VideoEncoderConfiguration(dimensions,
                VideoEncoderConfiguration.FRAME_RATE.FRAME_RATE_FPS_15,
                VideoEncoderConfiguration.STANDARD_BITRATE,
                VideoEncoderConfiguration.ORIENTATION_MODE.ORIENTATION_MODE_FIXED_PORTRAIT));
    }

    /**
     * 启用视频模块。（默认为开启状态）。
     */
    public void enableVideo() {
        mRtcEngine.enableVideo();
    }

    /**
     * 启用视频模块。（默认为开启状态）。
     */
    public void disableVideo() {
        mRtcEngine.disableVideo();
    }

    /**
     * 开/关本地视频采集。
     *
     * @param enabled true：开启本地视频采集和渲染（默认）false：关闭使用本地摄像头设备。关闭后，远端用户会接收不到本地用户的视频流；
     *                但本地用户依然可以接收远端用户的视频流。设置为 false 时，该方法不需要本地有摄像头。
     */
    public void enableLocalVideo(boolean enabled) {
        mRtcEngine.enableLocalVideo(enabled);
    }

    /**
     * 开/关本地视频发送。该方法用于允许/禁止往网络发送本地音频流。
     * 该方法不影响本地视频流获取，没有禁用摄像头。
     *
     * @param muted true：不发送本地视频流 false：发送本地视频流（默认）
     */
    public void muteLocalVideoStream(boolean muted) {
        mRtcEngine.muteLocalVideoStream(muted);
    }

    /**
     * 接收/停止接收指定视频流。
     *
     * @param uid   指定的用户 ID
     * @param muted true：停止接收指定用户的视频流 false：继续接收指定用户的音视频流（默认）
     *              <p>
     *              tips:如果之前有调用过 muteAllRemoteVideoStreams (true) 停止接收所有远端视频流，
     *              在调用本 API 之前请确保你已调用 muteAllRemoteVideoStreams (false)。
     *              muteAllRemoteVideoStreams 是全局控制，muteRemoteVideoStream 是精细控制。
     */
    public void muteRemoteVideoStream(int uid, boolean muted) {
        mRtcEngine.muteRemoteVideoStream(uid, muted);
    }

    /**
     * 接收/停止接收所有shipi你流。
     *
     * @param muted true：停止接收所有远端视频流 false：继续接收所有远端视频流（默认）
     */
    public void muteAllRemoteVideoStreams(boolean muted) {
        mRtcEngine.muteAllRemoteVideoStreams(false);
    }

    /**
     * 设置接收到的视频画面
     */
    public void setupRemoteVideo(int uid, int renderMode, FrameLayout frameLayout) {
        if (frameLayout == null) {
            return;
        }
        if (frameLayout.getChildCount() >= 1) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(mContext);
        frameLayout.addView(surfaceView);
        getRtcEngine().setupRemoteVideo(new VideoCanvas(surfaceView, renderMode, uid));
    }

    /**
     * 设置本地的画面
     */
    public void setupLocalVideo(int uid, int renderMode, FrameLayout frameLayout) {
        if (frameLayout == null) {
            return;
        }
        if (frameLayout.getChildCount() >= 1) {
            return;
        }
        SurfaceView surfaceView = RtcEngine.CreateRendererView(mContext);
        frameLayout.addView(surfaceView);
        getRtcEngine().setupLocalVideo(new VideoCanvas(surfaceView, renderMode, uid));
    }

    /***************************** 声网视频相关控制方法 ***********************************/

    /**
     * 声网管理类
     *
     * @return
     */
    public RtcEngine getRtcEngine() {
        return mRtcEngine;
    }

    public MpRtcEngineEventHandler getMpRtcEngineEventHandler() {
        return mpRtcEngineEventHandler;
    }

    public int getCurrentPushWidth() {
        return currentPushWidth;
    }

    public int getCurrentPushHeight() {
        return currentPushHeight;
    }

    public int getCurrentMode() {
        return currentMode;
    }

    public int getCurrentUserRole() {
        return currentUserRole;
    }

    public long getAnchorId() {
        return anchorId;
    }

    public void setAnchorId(long anchorId) {
        this.anchorId = anchorId;
    }

    public String getAnchorName() {
        return anchorName;
    }

    public void setAnchorName(String anchorName) {
        this.anchorName = anchorName;
    }

    public long getHost() {
        return host;
    }

    public void setHost(long host) {
        this.host = host;
    }

    public boolean isAnchor() {
        return anchorId == BaseConfig.getUserId();
    }

    public boolean isHost() {
        return host == BaseConfig.getUserId();
    }
}
