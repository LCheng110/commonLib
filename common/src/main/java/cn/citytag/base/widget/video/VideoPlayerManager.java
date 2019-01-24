package cn.citytag.base.widget.video;

import android.app.Application;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.SurfaceHolder;

import com.aliyun.vodplayer.media.AliyunLocalSource;
import com.aliyun.vodplayer.media.AliyunMediaInfo;
import com.aliyun.vodplayer.media.AliyunVodPlayer;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;
import com.aliyun.vodplayer.media.IAliyunVodPlayer.PlayerState;

import java.util.HashMap;
import java.util.Map;

import cn.citytag.base.config.BaseConfig;

/**
 * 作者：M. on 2019/1/10 10:52
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 * <p>
 * 视频播放器单例类
 */
public class VideoPlayerManager {

    private static final String TAG = "VideoPlayerManager";
    private Context mContext;
    private AliyunVodPlayer mAliyunVodPlayer;
    private boolean isPlayerPrepare;                        // 播放器是否准备就绪
    private String mPlayUrl;                                // 播放地址
    private AliyunLocalSource mAliyunLocalSource;           // 播放器播放资源
    private MpVideoPlayerView mMpVideoPlayerView;           // 视频播放绑定的控件
    private AliyunMediaInfo mAliyunMediaInfo;               // 媒体信息
    private PlayerState mPlayerState;                       // 用来记录前后台切换时的状态，以供恢复。

    private boolean needSeekTo;                             // 是否需要seekTo
    private int seekToPosition;                             // seekTo的位置
    private SurfaceHolder currentSurfaceHolder;             // 当前SurfaceHolder
    private boolean isPrepareing = false;                   // 当前是否处于准备视频中，连续切换url 不会prepare新的
    private boolean rePrepare = false;                      // 需要重新prepare 当前URL

    /**
     * 判断VodePlayer 是否加载完成
     */
    private Map<AliyunMediaInfo, Boolean> hasLoadEnd = new HashMap<>();

    public VideoPlayerManager() {
    }

    private static class SingletonHolder {
        private static VideoPlayerManager instance = new VideoPlayerManager();
    }

    public static VideoPlayerManager get() {
        return SingletonHolder.instance;
    }

    public void init(Application application) {
        mContext = application.getApplicationContext();
    }

    /**
     * 初始化播放器引擎
     */
    public void prepare() {
        if (isPlayerPrepare) {
            return;
        } else {
            mAliyunVodPlayer = new AliyunVodPlayer(mContext);
            mAliyunVodPlayer.enableNativeLog();
            setPlayerPrepare(true);
            preparePlayerListener();
            isPrepareing = false;
        }
    }

    public void prepareIfNot(){
        if (mAliyunVodPlayer == null) {
            if (isPlayerPrepare) {
                return;
            } else {
                mAliyunVodPlayer = new AliyunVodPlayer(mContext);
                mAliyunVodPlayer.enableNativeLog();
                setPlayerPrepare(true);
                preparePlayerListener();
                isPrepareing = false;
            }
        }

    }

    /**
     * 初始化播放器各种监听
     */
    private void preparePlayerListener() {
        initPrePareListener();
        initErrorListener();
        initLoadingListener();
        initCompletionListener();
        initReplayListener();
        initAutoPlayListener();
        initSeekCompleteListener();
        initFirstFrameStartListener();
    }

    /**
     * 设置准备回调
     */
    private void initPrePareListener() {
        mAliyunVodPlayer.setOnPreparedListener(new IAliyunVodPlayer.OnPreparedListener() {
            @Override
            public void onPrepared() {

                if (mAliyunVodPlayer == null) {
                    return;
                }
                isPrepareing = false;
                if (rePrepare) {
                    preparePlay();
                    rePrepare = false;
                    return;
                }
                mAliyunMediaInfo = mAliyunVodPlayer.getMediaInfo();
                if (mAliyunMediaInfo == null) {
                    return;
                }
                //防止服务器信息和实际不一致
                mAliyunMediaInfo.setDuration((int) mAliyunVodPlayer.getDuration());
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByPrepare(mAliyunMediaInfo);
                }
            }
        });
    }

    /**
     * 播放器出错监听
     */
    private void initErrorListener() {
        //播放器出错监听
        mAliyunVodPlayer.setOnErrorListener(new IAliyunVodPlayer.OnErrorListener() {
            @Override
            public void onError(int errorCode, int errorEvent, String errorMsg) {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByError(errorCode, errorMsg);
                }
            }
        });
    }

    /**
     * 播放器加载回调
     */
    private void initLoadingListener() {
        mAliyunVodPlayer.setOnLoadingListener(new IAliyunVodPlayer.OnLoadingListener() {
            @Override
            public void onLoadStart() {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByLoadStart();
                }
            }

            @Override
            public void onLoadEnd() {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByLoadEnd();
                }
                hasLoadEnd.put(mAliyunMediaInfo, true);
            }

            @Override
            public void onLoadProgress(int percent) {

                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByLoadProgress(percent);
                }
            }
        });
    }

    /**
     * 播放结束的回调
     */
    private void initCompletionListener() {
        mAliyunVodPlayer.setOnCompletionListener(new IAliyunVodPlayer.OnCompletionListener() {
            @Override
            public void onCompletion() {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByCompletion();
                }
            }
        });
    }

    /**
     * 重播监听
     */
    private void initReplayListener() {
        mAliyunVodPlayer.setOnRePlayListener(new IAliyunVodPlayer.OnRePlayListener() {
            @Override
            public void onReplaySuccess() {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByReplaySuccess(mAliyunMediaInfo);
                }
            }
        });
    }

    /**
     * 播放器自动播放的回调
     */
    private void initAutoPlayListener() {
        mAliyunVodPlayer.setOnAutoPlayListener(new IAliyunVodPlayer.OnAutoPlayListener() {
            @Override
            public void onAutoPlayStarted() {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByAutoPlay();
                }
            }
        });
    }

    /**
     * 播放器 seek 结束
     */
    private void initSeekCompleteListener() {
        mAliyunVodPlayer.setOnSeekCompleteListener(new IAliyunVodPlayer.OnSeekCompleteListener() {
            @Override
            public void onSeekComplete() {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshBySeekComplete();
                }
            }
        });
    }

    /**
     * 第一帧 展示
     */
    private void initFirstFrameStartListener() {
        mAliyunVodPlayer.setOnFirstFrameStartListener(new IAliyunVodPlayer.OnFirstFrameStartListener() {
            @Override
            public void onFirstFrameStart() {
                if (mMpVideoPlayerView != null) {
                    mMpVideoPlayerView.refreshByFirstFrameStart();
                }
                if (needSeekTo) {
                    seekTo(seekToPosition);
                    seekToPosition = 0;
                    needSeekTo = !needSeekTo;
                }
            }
        });
    }

    /**
     * 开始播放视频
     *
     * @param url
     */
    public void startPlay(String url) {
        this.seekToPosition = 0;
        this.needSeekTo = false;
        setPlayUrl(url);
        if (isPrepareing) {
            if (mAliyunVodPlayer != null) {
                mAliyunVodPlayer.stop();
            }
        }
        if(isPlayerPlaying() && mAliyunVodPlayer != null) {
            mAliyunVodPlayer.stop();
        }
        preparePlay();
    }

    /**
     * 播放同时移动到制定Position
     *
     * @param url
     */
    public void startPlay(String url, int seekToPosition) {
        setPlayUrl(url);
        this.seekToPosition = seekToPosition;
        this.needSeekTo = true;
        preparePlay();
    }

    /**
     * 播放器准备视频
     */
    private void preparePlay() {
        AliyunLocalSource.AliyunLocalSourceBuilder builder = new AliyunLocalSource.AliyunLocalSourceBuilder();
        builder.setSource(getPlayUrl());
        AliyunLocalSource localSource = builder.build();
        if (mAliyunVodPlayer == null) {
            return;
        }
        setAliyunLocalSource(localSource);
        if (mAliyunVodPlayer.isPlaying()) {
            stop();
        }
        mAliyunVodPlayer.prepareAsync(localSource);
        isPrepareing = true;
    }

    /**
     * 设置循环播放
     */
    public void setCirclePlay(boolean circlePlay) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setCirclePlay(circlePlay);
        }
    }

    /**
     * 自动播放 prepare结束之后自动开始播放
     *
     * @param autoPlay
     */
    public void setAutoPlay(boolean autoPlay) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setAutoPlay(autoPlay);
        }
    }

    /**
     * 绑定VideoPlayerView
     */
    public void bindVideoPlayerView(@NonNull MpVideoPlayerView videoPlayerView) {
        mMpVideoPlayerView = videoPlayerView;
        mMpVideoPlayerView.setBind(true);
        bindSurface(videoPlayerView);
    }

    /**
     * 重新绑定
     *
     * @param videoPlayerView
     */
    public void reBindVideoPlayerView(@NonNull MpVideoPlayerView videoPlayerView) {
        unBindVideoPlayerView();
        mMpVideoPlayerView = videoPlayerView;
        mMpVideoPlayerView.setBind(true);
        reBindSurface(videoPlayerView);
        mMpVideoPlayerView.refreshByReBind(mAliyunMediaInfo);
    }

    private void reBindSurface(MpVideoPlayerView videoPlayerView) {
        SurfaceHolder holder = videoPlayerView.getSurfaceView().getHolder();
        currentSurfaceHolder = holder;
        //增加surfaceView的监听
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                if (surfaceHolder == currentSurfaceHolder) {
                    mAliyunVodPlayer.setDisplay(surfaceHolder);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                mAliyunVodPlayer.surfaceChanged();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
        mAliyunVodPlayer.setDisplay(holder);
    }

    /**
     * 解绑 MpVideoPlayerView
     */
    public void unBindVideoPlayerView() {
        if (mMpVideoPlayerView != null) {
            mMpVideoPlayerView.refreshByUnBind();
        }
    }

    /**
     * 以SurfaceView 作为视频画面载体进行绑定
     */
    private void bindSurface(@NonNull MpVideoPlayerView videoPlayerView) {
        SurfaceHolder holder = videoPlayerView.getSurfaceView().getHolder();
        //增加surfaceView的监听
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder surfaceHolder) {
                mAliyunVodPlayer.setDisplay(surfaceHolder);
            }

            @Override
            public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {
                mAliyunVodPlayer.surfaceChanged();
            }

            @Override
            public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
            }
        });
        mAliyunVodPlayer.setDisplay(holder);
    }

    /**
     * 开始播放
     */
    public void start() {
        if (mAliyunVodPlayer == null) {
            return;
        }
        PlayerState playerState = mAliyunVodPlayer.getPlayerState();
        if (playerState == PlayerState.Paused || playerState == PlayerState.Prepared || mAliyunVodPlayer.isPlaying()) {
            mAliyunVodPlayer.start();
            if (mMpVideoPlayerView != null) {
                mMpVideoPlayerView.refreshByStart();
            }
        }
    }

    /**
     * 暂停播放
     */
    public void pause() {
        if (mAliyunVodPlayer == null) {
            return;
        }
        PlayerState playerState = mAliyunVodPlayer.getPlayerState();
        if (playerState == PlayerState.Started || mAliyunVodPlayer.isPlaying()) {
            mAliyunVodPlayer.pause();
            if (mMpVideoPlayerView != null) {
                mMpVideoPlayerView.refreshByPause();
            }
        }
    }

    /**
     * 停止播放
     */
    public void stop() {
        Boolean hasLoadedEnd = null;
        AliyunMediaInfo mediaInfo = null;
        if (mAliyunVodPlayer != null && hasLoadEnd != null) {
            mediaInfo = mAliyunVodPlayer.getMediaInfo();
            hasLoadedEnd = hasLoadEnd.get(mediaInfo);
        }
        if (mAliyunVodPlayer != null && hasLoadedEnd != null) {
            mAliyunVodPlayer.stop();

        }
        if (mMpVideoPlayerView != null) {
            mMpVideoPlayerView.refreshByStop();
        }
        if (hasLoadEnd != null) {
            hasLoadEnd.remove(mediaInfo);
        }
    }

    /**
     * seek 操作
     *
     * @param position 制定位置
     */
    public void seekTo(int position) {
        if (mAliyunVodPlayer == null) {
            return;
        }
        mAliyunVodPlayer.seekTo(position);
        mAliyunVodPlayer.start();
        if (mMpVideoPlayerView != null) {
            mMpVideoPlayerView.refreshBySeekTo();
        }
    }

    /**
     * 结束播放释放资源。
     * 1.播放器置空
     */
    public void release() {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.release();
            mAliyunVodPlayer = null;
        }
        setPlayerPrepare(false);
        if (hasLoadEnd != null) {
            hasLoadEnd.clear();
        }
        mPlayUrl = null;
        mAliyunLocalSource = null;
        //销毁关于播放器VIEW
        if (mMpVideoPlayerView != null) {
//            mMpVideoPlayerView.onDestroy();
            mMpVideoPlayerView = null;
        }
        isPrepareing = false;
    }

    public PlayerState getPlayerState() {
        if (mAliyunVodPlayer != null) {
            return mAliyunVodPlayer.getPlayerState();
        } else {
            return null;
        }
    }

    /**
     * 重新prepare
     */
    public void prepareLocalSource() {
        if (mAliyunLocalSource != null) {
            mAliyunVodPlayer.prepareAsync(mAliyunLocalSource);
        }
    }

    /**
     * 保存当前的状态，供恢复使用
     */
    public void savePlayerState() {
        if (mAliyunVodPlayer == null || mMpVideoPlayerView == null) {
            return;
        }
        mPlayerState = mAliyunVodPlayer.getPlayerState();
        //然后再暂停播放器
        //如果希望后台继续播放，不需要暂停的话，可以注释掉pause调用。
        pause();
    }

    /**
     * 恢复之前的状态
     */
    public void resumePlayerState() {
        if (mAliyunVodPlayer == null) {
            return;
        }
        if (mPlayerState == PlayerState.Paused) {
            pause();
        } else if (mPlayerState == PlayerState.Started) {
            // TODO: 2019/1/11 需要梳理
//            if (isLocalSource()) {
//                reTry();
//            } else {
//                start();
//            }
            start();
        }
    }

    /**
     * 获取当前播放进度
     */
    public long getCurrentPosition() {
        if (mAliyunVodPlayer != null) {
            return mAliyunVodPlayer.getCurrentPosition();
        }
        return 0;
    }

    /**
     * 设置是否静音
     *
     * @param mode
     */
    public void setMuteMode(boolean mode) {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setMuteMode(mode);
        }

    }

    /**
     * 获取当前播放器引擎
     *
     * @return
     */
    public AliyunVodPlayer getAliyunVodPlayer() {
        if (mAliyunVodPlayer != null) {
            return mAliyunVodPlayer;
        }
        return null;
    }

    public MpVideoPlayerView getMpVideoPlayerView() {
        return mMpVideoPlayerView;
    }

    /**
     * 生命周期resume
     */
    public void onVideoPlayerResume() {
        if (mMpVideoPlayerView != null) {
            mMpVideoPlayerView.onResume();
        }
    }

    /**
     * @param isNeedWatch 是否注册
     */
    public void onVideoPlayerResume(boolean isNeedWatch) {
        if (mMpVideoPlayerView != null) {
            mMpVideoPlayerView.onResume(isNeedWatch);
        }
    }

    /**
     * 生命周期stop
     */
    public void onVideoPlayerStop() {
        if (mMpVideoPlayerView != null) {
            mMpVideoPlayerView.onStop();
        }
    }

    /**
     * 正在播放中
     *
     * @return
     */
    public boolean isPlaying() {
        if (mAliyunVodPlayer != null) {
            return mAliyunVodPlayer.isPlaying();
        } else {
            return false;
        }
    }

    /**
     * 正在播放中
     *
     * @return
     */
    public boolean isPlayerPlaying() {
        return getPlayerState() == PlayerState.Started;
    }

    /**
     * 获取当前播放器引擎是否准备好
     *
     * @return
     */
    public boolean isPlayerPrepare() {
        return isPlayerPrepare;
    }

    /**
     * 清理surface
     */
    public void clearSurface() {
        if (mAliyunVodPlayer != null) {
            mAliyunVodPlayer.setSurface(null);
        }
    }

    /**
     * 设置当前播放器殷勤是否准备好
     *
     * @param playerPrepare
     */
    public void setPlayerPrepare(boolean playerPrepare) {
        isPlayerPrepare = playerPrepare;
    }

    /**
     * 获取当前播放URL
     *
     * @return
     */
    public String getPlayUrl() {
        return mPlayUrl;
    }

    /**
     * 设置当前播放URL
     *
     * @param mPlayUrl
     */
    public void setPlayUrl(String mPlayUrl) {
        this.mPlayUrl = mPlayUrl;
    }

    /**
     * 获取当前播放资源
     *
     * @return
     */
    private AliyunLocalSource getAliyunLocalSource() {
        return mAliyunLocalSource;
    }

    /**
     * 设置当前播放资源
     *
     * @param mAliyunLocalSource
     */
    private void setAliyunLocalSource(AliyunLocalSource mAliyunLocalSource) {
        this.mAliyunLocalSource = mAliyunLocalSource;
    }

    public Map<AliyunMediaInfo, Boolean> getHasLoadEnd() {
        return hasLoadEnd;
    }

    public void setHasLoadEnd(Map<AliyunMediaInfo, Boolean> hasLoadEnd) {
        this.hasLoadEnd = hasLoadEnd;
    }
}
