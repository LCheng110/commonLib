package cn.citytag.base.widget.video;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.aliyun.vodplayer.media.AliyunMediaInfo;
import com.aliyun.vodplayer.media.IAliyunVodPlayer.PlayerState;

import java.lang.ref.WeakReference;

import cn.citytag.base.R;

/**
 * 作者：M. on 2019/1/10 14:12
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 * <p>
 * 视频播放主体的页面
 * <p>
 * 当前采用 播放引擎 + 视频播放页面相互绑定的形势
 */
public class MpVideoPlayerView extends RelativeLayout {

    public static final int ERROR_CODE_NO_INPUTFILE = 4004;

    private static final String TAG = "MpVideoPlayerView";
    // 视频画面
    private SurfaceView mSurfaceView;
    // 封面view
    private ImageView mCoverView;
    // 手势操作view
    private MpGestureView mGestureView;
    // 控制view
    private MpVideoControl mControlView;
    // Tips view
    private MpTipsView mTipsView;
    // 屏幕方向监听
    private MpOrientationWatchDog mMpOrientationWatchDog;
    // 视频暂停 播放按钮
    private ImageView mPlayView;
    // 当前是否绑定
    private boolean isBind;
    // 是不是在seek中
    private boolean inSeek = false;
    // 播放是否完成
    private boolean isCompleted = false;
    // 当前屏幕模式
    private MpScreenMode mCurrentScreenMode = MpScreenMode.Small;

    // 播放器相关callback回调事件
    // 双击回调 监听
    private OnDoubleClickListener mOnDoubleClickListener;
    // 播放按钮点击监听
    private OnPlayStateBtnClickListener mOnPlayStateBtnClickListener;
    // seek 开始的监听
    private OnSeekStartListener mOnSeekStartListener;
    // 屏幕方向改变监听
    private OnOrientationChangeListener mOrientationChangeListener;
    // 视频准备结束回调
    private OnPreparedListener mOutPreparedListener;
    // 播放器出错回调
    private OnErrorListener mOutErrorListener = null;
    // 播放结束 回调
    private OnCompletionListener mOutCompletionListener = null;
    // 重播 重试 成功的回调
    private OnRePlayListener mOutRePlayListener = null;
    // 播放器自动开始播放 回调
    private OnAutoPlayListener mOutAutoPlayListener = null;
    // 播放器 seek 结束回调
    private OnSeekCompleteListener mOuterSeekCompleteListener = null;
    // 播放器第一帧回调
    private OnFirstFrameStartListener mOutFirstFrameStartListener = null;
    // 全屏按钮点击不切换大小屏自定义的点击事件
    private OnScreenJumpClickListener mOnScreenJumpClickListener = null;
    // 播放器当前进度的回调
    private OnCurrentPositionListener mOnCurrentPositionListener = null;


    //进度更新计时器
    private ProgressUpdateTimer mProgressUpdateTimer = new ProgressUpdateTimer(this);
    //解决bug,进入播放界面快速切换到其他界面,播放器仍然播放视频问题
    private VodPlayerLoadEndHandler vodPlayerLoadEndHandler = new VodPlayerLoadEndHandler(this);

    public MpVideoPlayerView(Context context) {
        super(context);
        initVideoPlayerView();
    }

    public MpVideoPlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initVideoPlayerView();
    }

    public MpVideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initVideoPlayerView();
    }

    /**
     * 初始化View
     */
    private void initVideoPlayerView() {
        //初始化播放用的surfaceView
        initSurfaceView();
        // TODO: 2019/1/10 播放器初始化
        //初始化封面
        initCoverView();
        //初始化手势view
        initGestureView();
        //初始化控制栏
        initControlView();
        //初始化提示view
        initTipsView();
        //初始化播放view
        initPlayView();
        //初始化屏幕方向监听
        initOrientationWatchdog();
        //先隐藏手势和控制栏，防止在没有prepare的时候做操作。
        hideGestureAndControlViews();
    }

    /**
     * 初始化播放器显示SurfaceView
     */
    private void initSurfaceView() {
        mSurfaceView = new SurfaceView(getContext().getApplicationContext());
        addSubView(mSurfaceView);
    }

    /**
     * 初始化封面
     */
    private void initCoverView() {
        mCoverView = new ImageView(getContext());
        addSubView(mCoverView);
    }

    /**
     * 初始化手势view
     */
    private void initGestureView() {
        mGestureView = new MpGestureView(getContext());
        addSubView(mGestureView);
        //设置手势监听
        mGestureView.setOnGestureListener(new MpGestureView.GestureListener() {
            @Override
            public void onSingleTap() {
                /**
                 * 逻辑：
                 * 1.当前播放中，控制栏未展示则展示控制栏。控制栏已展示则 隐藏控制栏
                 * 2.暂停中，控制栏未展示则展示控制栏。控制栏已展示则隐藏控制栏，同时开始播放
                 */
                if (mControlView == null) {
                    return;
                }
                if (mControlView.getVisibility() != VISIBLE) {
                    mControlView.show();
                    if (mControlView.getPlayState() == MpVideoControl.PlayState.NotPlaying) {
                        VideoPlayerManager.get().start();
                    }
                } else {
                    mControlView.hide(MpVideoControl.HideType.Normal);
                    if (mControlView.getPlayState() == MpVideoControl.PlayState.NotPlaying) {
                        VideoPlayerManager.get().start();
                    }
                }
            }

            @Override
            public void onDoubleTap() {
                //双击事件，控制暂停播放
                if (mOnDoubleClickListener != null) {
                    mOnDoubleClickListener.onDoubleClick();
                }
            }
        });
    }

    /**
     * 初始化控制栏view
     */
    private void initControlView() {
        mControlView = new MpVideoControl(getContext());
        addSubView(mControlView);
        //设置播放按钮点击
        mControlView.setOnPlayStateClickListener(new MpVideoControl.OnPlayStateClickListener() {
            @Override
            public void onPlayStateClick() {
                switchPlayerState();
            }
        });
        //设置进度条的seek监听
        mControlView.setOnSeekListener(new MpVideoControl.OnSeekListener() {
            @Override
            public void onSeekEnd(int position) {
                mControlView.setVideoPosition(position);
                if (isCompleted) {
                    //播放完成了，不能seek了
                    inSeek = false;
                } else {
                    //拖动结束后，开始seek
                    VideoPlayerManager.get().seekTo(position);
                    inSeek = true;
                    if (mOnSeekStartListener != null) {
                        mOnSeekStartListener.onSeekStart();
                    }
                    if (mControlView != null) {
                        mControlView.hide(MpVideoViewAction.HideType.Normal);
                    }
                }
            }

            @Override
            public void onSeekStart() {
                //拖动开始
                inSeek = true;
            }
        });
        //点击全屏/小屏按钮
        mControlView.setOnScreenModeClickListener(new MpVideoControl.OnScreenModeClickListener() {
            @Override
            public void onClick() {
                MpScreenMode targetMode;
                if (mCurrentScreenMode == MpScreenMode.Small) {
                    targetMode = MpScreenMode.Full;
                } else {
                    targetMode = MpScreenMode.Small;
                }
                changeScreenMode(targetMode);
                if (mControlView != null) {
                    mControlView.hide(MpVideoViewAction.HideType.Normal);
                }
            }
        });
    }

    /**
     * 初始化提示view
     */
    private void initTipsView() {
        mTipsView = new MpTipsView(getContext());
        //设置tip中的点击监听事件
        mTipsView.setOnTipClickListener(new MpTipsView.OnTipClickListener() {
            @Override
            public void onRetryPlay() {
                //重试
                reTry();
            }

            @Override
            public void onReplay() {
                //重播
                rePlay();
            }
        });
        addSubView(mTipsView);
    }

    /**
     * 初始化播放的view
     */
    private void initPlayView() {
        mPlayView = new ImageView(getContext());
        mPlayView.setImageResource(R.drawable.ic_video_play);
        LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
        addView(mPlayView, params);//添加到布局中
    }

    /**
     * 初始化屏幕方向旋转。用来监听屏幕方向。结果通过OrientationListener回调出去。
     */
    private void initOrientationWatchdog() {
        final Context context = getContext();
        mMpOrientationWatchDog = new MpOrientationWatchDog(context);
        mMpOrientationWatchDog.setOnOrientationListener(new InnerOrientationListener(this));
    }

    /**
     * 隐藏手势和控制栏
     */
    private void hideGestureAndControlViews() {
        if (mGestureView != null) {
            mGestureView.hide(MpVideoViewAction.HideType.Normal);
        }
        if (mControlView != null) {
            mControlView.hide(MpVideoViewAction.HideType.Normal);
        }
        if (mPlayView != null) {
            mPlayView.setVisibility(GONE);
        }
    }

    /**
     * addSubView 添加子view到布局中
     *
     * @param view 子view
     */
    private void addSubView(View view) {
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        addView(view, params);//添加到布局中
    }

    /**
     * 切换播放状态。点播播放按钮之后的操作
     */
    public void switchPlayerState() {
        PlayerState playerState = VideoPlayerManager.get().getAliyunVodPlayer().getPlayerState();
        if (playerState == PlayerState.Started) {
            VideoPlayerManager.get().pause();
        } else if (playerState == PlayerState.Paused || playerState == PlayerState.Prepared) {
            VideoPlayerManager.get().start();
        }
        if (mOnPlayStateBtnClickListener != null) {
            mOnPlayStateBtnClickListener.onPlayBtnClick(playerState);
        }
    }

    /**
     * 改变屏幕模式：小屏或者全屏。
     *
     * @param targetMode {@link MpScreenMode}
     */
    public void changeScreenMode(MpScreenMode targetMode) {
        //这里可能会对模式做一些修改
        if (targetMode != mCurrentScreenMode) {
            mCurrentScreenMode = targetMode;
        }
        Context context = getContext();
        if (context instanceof Activity) {
            if (targetMode == MpScreenMode.Full) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            } else if (targetMode == MpScreenMode.Small) {
                ((Activity) context).setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            }
        }

        if (mControlView != null) {
            mControlView.setScreenModeStatus(targetMode);
        }
    }

    /**
     * 重试播放，会从当前位置开始播放
     */
    private void reTry() {
        isCompleted = false;
        inSeek = false;

        int currentPosition = mControlView.getVideoPosition();
        Log.d(TAG, " currentPosition = " + currentPosition);
        if (mTipsView != null) {
            mTipsView.hideAll();
        }
        if (mControlView != null) {
            mControlView.reset();
            //防止被reset掉，下次还可以获取到这些值
            mControlView.setVideoPosition(currentPosition);
        }
        if (mGestureView != null) {
            mGestureView.reset();
        }
        if (VideoPlayerManager.get().getAliyunVodPlayer() != null) {
            if (mTipsView != null) {
                mTipsView.showNetLoadingTipView();
            }
            VideoPlayerManager.get().prepareLocalSource();
            VideoPlayerManager.get().seekTo(currentPosition);
        }
    }

    /**
     * 重播，将会从头开始播放
     */
    private void rePlay() {
        isCompleted = false;
        inSeek = false;
        if (mTipsView != null) {
            mTipsView.hideAll();
        }
        if (mControlView != null) {
            mControlView.reset();
        }
        if (mGestureView != null) {
            mGestureView.reset();
        }
        if (VideoPlayerManager.get().getAliyunVodPlayer() != null) {
            if (mTipsView != null) {
                mTipsView.showNetLoadingTipView();
            }
            VideoPlayerManager.get().prepareLocalSource();
        }
    }

    /**
     * 屏幕方向变为横屏。
     *
     * @param fromPort 是否从竖屏变过来
     */
    private void changedToLandScape(boolean fromPort) {
        //如果不是从竖屏变过来，也就是一直是横屏的时候，就不用操作了
        if (!fromPort) {
            return;
        }
        //屏幕由竖屏转为横屏
        if (mCurrentScreenMode == MpScreenMode.Full) {
            //全屏情况转到了横屏
        } else if (mCurrentScreenMode == MpScreenMode.Small) {
            changeScreenMode(MpScreenMode.Full);
        }

        if (mOrientationChangeListener != null) {
            mOrientationChangeListener.orientationChange(fromPort, mCurrentScreenMode);
        }
    }

    /**
     * 屏幕方向变为竖屏
     *
     * @param fromLand 是否从横屏转过来
     */
    private void changedToPortrait(boolean fromLand) {
        if (mCurrentScreenMode == MpScreenMode.Full) {
            //全屏情况转到了竖屏
            if (fromLand) {
                changeScreenMode(MpScreenMode.Small);
            } else {
                //如果没有转到过横屏，就不让他转了。防止竖屏的时候点横屏之后，又立即转回来的现象
            }
        } else if (mCurrentScreenMode == MpScreenMode.Small) {
            //竖屏的情况转到了竖屏
        }

        if (mOrientationChangeListener != null) {
            mOrientationChangeListener.orientationChange(fromLand, mCurrentScreenMode);
        }
    }

    /**
     * 重新绑定刷新UI
     *
     * @param mAliyunMediaInfo
     */
    public void refreshByReBind(AliyunMediaInfo mAliyunMediaInfo) {
        mControlView.setMediaInfo(mAliyunMediaInfo);
        mControlView.setHideType(MpVideoViewAction.HideType.Normal);
        mGestureView.setHideType(MpVideoViewAction.HideType.Normal);
//        mControlView.show();
        mGestureView.show();
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.Playing);
        }
        if (mTipsView != null) {
            mTipsView.hideNetLoadingTipView();
            mTipsView.hideReplayTipView();
        }
        startProgressUpdateTimer();
    }

    /**
     * 视频prepare 刷新UI
     *
     * @param mAliyunMediaInfo
     */
    public void refreshByPrepare(AliyunMediaInfo mAliyunMediaInfo) {
        mControlView.setMediaInfo(mAliyunMediaInfo);
        mControlView.setHideType(MpVideoViewAction.HideType.Normal);
        mGestureView.setHideType(MpVideoViewAction.HideType.Normal);
//        mControlView.show();
        mGestureView.show();
        if (mTipsView != null) {
            mTipsView.hideNetLoadingTipView();
            mTipsView.hideNetErrorTipView();
        }
        //准备成功之后可以调用start方法开始播放
        if (mOutPreparedListener != null) {
            mOutPreparedListener.onPrepared();
        }
    }

    /**
     * 视频播放出错 刷新UI
     */
    public void refreshByError(int errorCode, String errorMsg) {
        //关闭定时器
        stopProgressUpdateTimer();
        if (mTipsView != null) {
            mTipsView.hideAll();
        }
        showErrorTipView(errorMsg);
        if (mOutErrorListener != null) {
            mOutErrorListener.onError(errorCode, errorMsg);
        }
    }

    /**
     * 播放器加载开始 刷新UI
     */
    public void refreshByLoadStart() {
        if (mTipsView != null) {
            mTipsView.showBufferLoadingTipView();
        }
    }

    /**
     * 播放器加载结束 刷新UI
     */
    public void refreshByLoadEnd() {
        if (mTipsView != null) {
            mTipsView.hideBufferLoadingTipView();
        }
        if (VideoPlayerManager.get().getAliyunVodPlayer() != null
                && VideoPlayerManager.get().getAliyunVodPlayer().isPlaying()) {
            mTipsView.hideErrorTipView();
        }
    }

    /**
     * 播放器加载中 刷新UI
     *
     * @param percent
     */
    public void refreshByLoadProgress(int percent) {

    }

    /**
     * 播放器播放结束 回调
     */
    public void refreshByCompletion() {
        inSeek = false;
        //关闭定时器
        stopProgressUpdateTimer();
        //如果当前播放资源是本地资源时, 再显示replay
        if (mTipsView != null && isBind) {
            //隐藏其他的动作,防止点击界面去进行其他操作
            mGestureView.hide(MpVideoViewAction.HideType.End);
            mControlView.hide(MpVideoViewAction.HideType.End);
            mTipsView.showReplayTipView();
        }
        if (mOutCompletionListener != null) {
            mOutCompletionListener.onCompletion();
        }
    }

    /**
     * 播放器重播成功回调
     *
     * @param mAliyunMediaInfo
     */
    public void refreshByReplaySuccess(AliyunMediaInfo mAliyunMediaInfo) {
        //重播、重试成功
        mTipsView.hideAll();
        mGestureView.show();
//        mControlView.show();
        mControlView.setMediaInfo(mAliyunMediaInfo);
        //重播自动开始播放,需要设置播放状态
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.Playing);
        }
        //开始启动更新进度的定时器
        startProgressUpdateTimer();
        if (mOutRePlayListener != null) {
            mOutRePlayListener.onReplaySuccess();
        }
    }

    /**
     * 播放器自动播放开始回调
     */
    public void refreshByAutoPlay() {
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.Playing);
        }
        if (mOutAutoPlayListener != null) {
            mOutAutoPlayListener.onAutoPlayStarted();
        }
    }

    /**
     * 播放器 seek 结束时间
     */
    public void refreshBySeekComplete() {
        inSeek = false;
        if (mOuterSeekCompleteListener != null) {
            mOuterSeekCompleteListener.onSeekComplete();
        }
    }

    /**
     * 播放器第一帧展示回调
     */
    public void refreshByFirstFrameStart() {
        //开始启动更新进度的定时器
        startProgressUpdateTimer();
        mCoverView.setVisibility(GONE);
        if (mOutFirstFrameStartListener != null) {
            mOutFirstFrameStartListener.onFirstFrameStart();
        }
    }

    /**
     * 视频开始播放 刷新UI
     */
    public void refreshByStart() {
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.Playing);
        }
        mControlView.setHideType(MpVideoViewAction.HideType.Normal);
        mGestureView.setHideType(MpVideoViewAction.HideType.Normal);
//        mControlView.show();
        mGestureView.show();
        mPlayView.setVisibility(GONE);
    }

    /**
     * 视频暂停播放 刷新UI
     */
    public void refreshByPause() {
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.NotPlaying);
        }
        mPlayView.setVisibility(VISIBLE);
    }

    /**
     * 视频停止播放 刷新UI
     */
    public void refreshByStop() {
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.NotPlaying);
        }
        stopProgressUpdateTimer();
    }

    /**
     * 视频进度拖拉 刷新UI
     */
    public void refreshBySeekTo() {
        inSeek = true;
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.Playing);
            if (mPlayView != null) {
                mPlayView.setVisibility(mControlView.getPlayState() == MpVideoControl.PlayState.Playing ? View.GONE : View.VISIBLE);
            }
        }
    }

    /**
     * 与播放引擎解绑 刷新UI
     */
    public void refreshByUnBind() {
        stopProgressUpdateTimer();
        if (mControlView != null) {
            mControlView.refreshByUnBind();
        }
        setBind(false);
    }

    /**
     * 开始进度条更新计时器
     */
    private void startProgressUpdateTimer() {
        if (mProgressUpdateTimer != null) {
            mProgressUpdateTimer.removeMessages(0);
            mProgressUpdateTimer.sendEmptyMessageDelayed(0, 1000);
        }
    }

    /**
     * 停止进度条更新计时器
     */
    private void stopProgressUpdateTimer() {
        if (mProgressUpdateTimer != null) {
            mProgressUpdateTimer.removeMessages(0);
        }
    }

    /**
     * 处理进度更新消息
     *
     * @param msg
     */
    private void handleProgressUpdateMessage(Message msg) {
        if (VideoPlayerManager.get().getAliyunVodPlayer() != null && !inSeek) {
            if (mControlView != null) {
                mControlView.setVideoBufferPosition(VideoPlayerManager.get().getAliyunVodPlayer().getBufferingPosition());
                mControlView.setVideoPosition((int) VideoPlayerManager.get().getCurrentPosition());
            }
            if (mOnCurrentPositionListener != null && VideoPlayerManager.get().isPlayerPlaying()) {
                mOnCurrentPositionListener.onCurrentPosition((int) VideoPlayerManager.get().getCurrentPosition());
                mPlayView.setVisibility(mControlView.getPlayState() == MpVideoControl.PlayState.Playing ? View.GONE : View.VISIBLE);
            }
        }
        //解决bug：在Prepare中开始更新的时候，不会发送更新消息。
        startProgressUpdateTimer();
    }

    /**
     * 显示错误提示
     *
     * @param errorMsg 错误描述
     */
    public void showErrorTipView(String errorMsg) {
        VideoPlayerManager.get().pause();
        VideoPlayerManager.get().stop();
        if (mControlView != null) {
            mControlView.setPlayState(MpVideoControl.PlayState.NotPlaying);
        }
        if (mTipsView != null) {
            //隐藏其他的动作,防止点击界面去进行其他操作
            mGestureView.hide(MpVideoViewAction.HideType.End);
            mControlView.hide(MpVideoViewAction.HideType.End);
            mCoverView.setVisibility(GONE);
            mTipsView.showErrorTipView(errorMsg);
        }
    }

    /**
     * 切换播放是否展示
     *
     * @param show true 展示 false  隐藏
     */
    public void togglePlayView(boolean show) {
        if (mPlayView != null) {
            mPlayView.setVisibility(show ? VISIBLE : GONE);
        }
    }

    /**
     * 暂停播放器的操作
     */
    public void onStop() {
        if (!(VideoPlayerManager.get().getHasLoadEnd() != null
                && VideoPlayerManager.get().getHasLoadEnd().size() > 0)) {
            vodPlayerLoadEndHandler.sendEmptyMessage(0);
            return;
        }
        if (mMpOrientationWatchDog != null) {
            mMpOrientationWatchDog.stopWatch();
        }
        //保存播放器的状态，供resume恢复使用。
        VideoPlayerManager.get().savePlayerState();
    }

    /**
     * 在activity调用onResume的时候调用。 解决home回来后，画面方向不对的问题
     */
    public void onResume() {
        if (mMpOrientationWatchDog != null) {
            mMpOrientationWatchDog.startWatch();
        }
        //从其他界面过来的话，也要show。
        if (mControlView != null) {
//            mControlView.show();
        }
        //onStop中记录下来的状态，在这里恢复使用
        VideoPlayerManager.get().resumePlayerState();
    }

    public void onResume(boolean isNeedWatch) {
        if (mMpOrientationWatchDog != null && isNeedWatch) {
            mMpOrientationWatchDog.startWatch();
        }
        //从其他界面过来的话，也要show。
        if (mControlView != null) {
//            mControlView.show();
        }
        //onStop中记录下来的状态，在这里恢复使用
        VideoPlayerManager.get().resumePlayerState();
    }

    /**
     * 活动销毁，释放
     */
    public void onDestroy() {
        //移除队列中的消息
        if (mProgressUpdateTimer != null) {
            mProgressUpdateTimer.removeCallbacksAndMessages(null);
            mProgressUpdateTimer = null;
        }
        mSurfaceView = null;
        mGestureView = null;
        mControlView = null;
        mCoverView = null;
        mTipsView = null;
        if (mMpOrientationWatchDog != null) {
            mMpOrientationWatchDog.destroy();
        }
        mMpOrientationWatchDog = null;
    }

    public SurfaceView getSurfaceView() {
        return mSurfaceView;
    }

    public boolean isBind() {
        return isBind;
    }

    public void setBind(boolean bind) {
        isBind = bind;
    }

    public void setOnDoubleClickListener(OnDoubleClickListener mOnDoubleClickListener) {
        this.mOnDoubleClickListener = mOnDoubleClickListener;
    }

    /**
     * 双击屏幕的回调
     */
    public interface OnDoubleClickListener {
        void onDoubleClick();
    }

    public void setOnPlayStateBtnClickListener(OnPlayStateBtnClickListener mOnPlayStateBtnClickListener) {
        this.mOnPlayStateBtnClickListener = mOnPlayStateBtnClickListener;
    }

    /**
     * 播放按钮点击listener
     */
    public interface OnPlayStateBtnClickListener {
        void onPlayBtnClick(PlayerState playerState);
    }

    public void setOnSeekStartListener(OnSeekStartListener mOnSeekStartListener) {
        this.mOnSeekStartListener = mOnSeekStartListener;
    }

    /**
     * seek开始监听
     */
    public interface OnSeekStartListener {
        void onSeekStart();
    }

    public void setOrientationChangeListener(OnOrientationChangeListener mOrientationChangeListener) {
        this.mOrientationChangeListener = mOrientationChangeListener;
    }

    /**
     * 屏幕方向改变监听接口
     */
    public interface OnOrientationChangeListener {
        /**
         * 屏幕方向改变
         *
         * @param from        从横屏切换为竖屏, 从竖屏切换为横屏
         * @param currentMode 当前屏幕类型
         */
        void orientationChange(boolean from, MpScreenMode currentMode);
    }

    public void setPreparedListener(OnPreparedListener mOutPreparedListener) {
        this.mOutPreparedListener = mOutPreparedListener;
    }

    /**
     * 视频准备的回调
     */
    public interface OnPreparedListener {
        void onPrepared();
    }

    public void setErrorListener(OnErrorListener mOutErrorListener) {
        this.mOutErrorListener = mOutErrorListener;
    }

    /**
     * 播放器播放出错 回调
     */
    public interface OnErrorListener {
        void onError(int errorCode, String errorMsg);
    }

    public void setCompletionListener(OnCompletionListener mOutCompletionListener) {
        this.mOutCompletionListener = mOutCompletionListener;
    }

    /**
     * 播放结束的回调
     */
    public interface OnCompletionListener {
        void onCompletion();
    }

    public void setRePlayListener(OnRePlayListener mOutRePlayListener) {
        this.mOutRePlayListener = mOutRePlayListener;
    }

    public interface OnRePlayListener {
        void onReplaySuccess();
    }

    public void setAutoPlayListener(OnAutoPlayListener mOutAutoPlayListener) {
        this.mOutAutoPlayListener = mOutAutoPlayListener;
    }

    /**
     * 自动播放回调
     */
    public interface OnAutoPlayListener {
        void onAutoPlayStarted();
    }

    public void setSeekCompleteListener(OnSeekCompleteListener mOuterSeekCompleteListener) {
        this.mOuterSeekCompleteListener = mOuterSeekCompleteListener;
    }

    /**
     * 播放器 seek 结束回调
     */
    public interface OnSeekCompleteListener {
        void onSeekComplete();
    }

    public void setFirstFrameStartListener(OnFirstFrameStartListener mOutFirstFrameStartListener) {
        this.mOutFirstFrameStartListener = mOutFirstFrameStartListener;
    }

    /**
     * 播放器第一帧回调
     */
    public interface OnFirstFrameStartListener {
        void onFirstFrameStart();
    }

    public void setOnScreenJumpClickListener(OnScreenJumpClickListener mOnScreenJumpClickListener) {
        this.mOnScreenJumpClickListener = mOnScreenJumpClickListener;
        // 点击大小屏 不切换屏幕的点击事件
        if (mOnScreenJumpClickListener != null && mControlView != null) {
            mControlView.setOnScreenJumpListener(new MpVideoControl.OnScreenJumpListener() {
                @Override
                public void onScreenJump() {
                    if (mOnScreenJumpClickListener != null) {
                        mOnScreenJumpClickListener.onScreenJumpClick();
                    }
                }
            });
        }
    }

    /**
     * 大小屏点击跳转的
     */
    public interface OnScreenJumpClickListener {
        void onScreenJumpClick();
    }

    public void setOnCurrentPositionListener(OnCurrentPositionListener mOnCurrentPositionListener) {
        this.mOnCurrentPositionListener = mOnCurrentPositionListener;
    }

    /**
     * 播放器当前进度的回调
     */
    public interface OnCurrentPositionListener {
        void onCurrentPosition(int position);
    }

    private static class InnerOrientationListener implements MpOrientationWatchDog.OnOrientationListener {

        private WeakReference<MpVideoPlayerView> playerViewWeakReference;

        public InnerOrientationListener(MpVideoPlayerView playerView) {
            playerViewWeakReference = new WeakReference<MpVideoPlayerView>(playerView);
        }

        @Override
        public void changedToLandScape(boolean fromPort) {
            MpVideoPlayerView playerView = playerViewWeakReference.get();
            if (playerView != null) {
                playerView.changedToLandScape(fromPort);
            }
        }

        @Override
        public void changedToPortrait(boolean fromLand) {
            MpVideoPlayerView playerView = playerViewWeakReference.get();
            if (playerView != null) {
                playerView.changedToPortrait(fromLand);
            }
        }
    }

    /**
     * 进度更新计时器
     */
    private static class ProgressUpdateTimer extends Handler {

        private WeakReference<MpVideoPlayerView> viewWeakReference;

        ProgressUpdateTimer(MpVideoPlayerView mpVideoPlayerView) {
            viewWeakReference = new WeakReference<MpVideoPlayerView>(mpVideoPlayerView);
        }

        @Override
        public void handleMessage(Message msg) {
            MpVideoPlayerView mpVideoPlayerView = viewWeakReference.get();
            if (mpVideoPlayerView != null) {
                mpVideoPlayerView.handleProgressUpdateMessage(msg);
            }
            super.handleMessage(msg);
        }
    }

    /**
     * 当VodPlayer 没有加载完成的时候,调用onStop 去暂停视频,
     * 会出现暂停失败的问题。
     */
    private static class VodPlayerLoadEndHandler extends Handler {

        private WeakReference<MpVideoPlayerView> weakReference;

        private boolean intentPause;

        public VodPlayerLoadEndHandler(MpVideoPlayerView mpVideoPlayerView) {
            weakReference = new WeakReference<>(mpVideoPlayerView);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 0) {
                intentPause = true;
            }
            if (msg.what == 1) {
                MpVideoPlayerView mpVideoPlayerView = weakReference.get();
                if (mpVideoPlayerView != null && intentPause) {
                    mpVideoPlayerView.onStop();
                    intentPause = false;
                }
            }
        }
    }

}
