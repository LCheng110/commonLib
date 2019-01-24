package cn.citytag.base.widget.video;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.aliyun.vodplayer.media.AliyunMediaInfo;

import java.lang.ref.WeakReference;

import cn.citytag.base.R;

/**
 * 控制条界面。包括了底部的控制栏等等。是界面的主要组成部分。
 */
public class MpVideoControl extends RelativeLayout implements MpVideoViewAction {

    private static final String TAG = MpVideoControl.class.getSimpleName();

    private static final int WHAT_HIDE = 0;
    private static final int DELAY_TIME = 3 * 1000; //3秒后隐藏

    //标题，控制条单独控制是否可显示
    private boolean mControlBarCanShow = true;
    private View mControlBar;

    //这些是大小屏都有的==========START========
    //视频播放状态
    private PlayState mPlayState = PlayState.NotPlaying;
    //播放按钮
    private ImageView mPlayStateBtn;

    //切换大小屏相关
    private MpScreenMode mAliyunScreenMode = MpScreenMode.Small;
    //全屏/小屏按钮
    private ImageView mScreenModeBtn;

    //大小屏公用的信息
    //视频信息，info显示用。
    private AliyunMediaInfo mAliyunMediaInfo;
    //播放的进度
    private int mVideoPosition = 0;
    //seekbar拖动状态
    private boolean isSeekbarTouching = false;
    //视频缓冲进度
    private int mVideoBufferPosition;
    //这些是大小屏都有的==========END========

    //这些是大屏时显示的
    //大屏的底部控制栏
    private View mLargeInfoBar;
    //当前位置文字
    private TextView mLargePositionText;
    //时长文字
    private TextView mLargeDurationText;
    //进度条
    private SeekBar mLargeSeekbar;
    //这些是小屏时显示的
    //底部控制栏
    private View mSmallInfoBar;
    //当前位置文字
    private TextView mSmallPositionText;
    //时长文字
    private TextView mSmallDurationText;
    //seek进度条
    private SeekBar mSmallSeekbar;

    //整个view的显示控制：
    //不显示的原因。如果是错误的，那么view就都不显示了。
    private HideType mHideType = null;

    //各种监听
    // 进度拖动监听
    private OnSeekListener mOnSeekListener;
    //播放按钮点击监听
    private OnPlayStateClickListener mOnPlayStateClickListener;
    //大小屏按钮点击监听
    private OnScreenModeClickListener mOnScreenModeClickListener;
    // 大小屏幕切换
    private OnScreenJumpListener mOnScreenJumpListener;

    private HideHandler mHideHandler = new HideHandler(this);

    public MpVideoControl(Context context) {
        super(context);
        init();
    }

    public MpVideoControl(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MpVideoControl(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        //Inflate布局
        LayoutInflater.from(getContext()).inflate(R.layout.view_control, this, true);
        findAllViews(); //找到所有的view
        setViewListener(); //设置view的监听事件
        updateAllViews(); //更新view的显示
    }

    private void findAllViews() {
        mControlBar = findViewById(R.id.ll_control);
        mScreenModeBtn = findViewById(R.id.iv_screen_mode);
        mPlayStateBtn = findViewById(R.id.iv_player_state);
        mLargeInfoBar = findViewById(R.id.ll_info_large_bar);
        mLargePositionText = findViewById(R.id.tv_info_large_position);
        mLargeDurationText = findViewById(R.id.tv_info_large_duration);
        mLargeSeekbar = findViewById(R.id.sb_info_large_seekbar);
        mSmallInfoBar = findViewById(R.id.ll_info_small_bar);
        mSmallPositionText = findViewById(R.id.tv_info_small_position);
        mSmallDurationText = findViewById(R.id.tv_info_small_duration);
        mSmallSeekbar = findViewById(R.id.sb_info_small_seekbar);
    }

    private void setViewListener() {
        //控制栏的播放按钮监听
        mPlayStateBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPlayStateClickListener != null) {
                    mOnPlayStateClickListener.onPlayStateClick();
                }
            }
        });
        //大小屏按钮监听
        mScreenModeBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnScreenJumpListener != null) {
                    mOnScreenJumpListener.onScreenJump();
                    return;
                }
                if (mOnScreenModeClickListener != null) {
                    mOnScreenModeClickListener.onClick();
                }
            }
        });
        //seekbar的滑动监听
        SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    //这里是用户拖动，直接设置文字进度就行，
                    // 无需去updateAllViews() ， 因为不影响其他的界面。
                    if (mAliyunScreenMode == MpScreenMode.Full) {
                        //全屏状态.
                        mLargePositionText.setText(formatMs(progress));
                    } else if (mAliyunScreenMode == MpScreenMode.Small) {
                        //小屏状态
                        mSmallPositionText.setText(formatMs(progress));
                    }
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                isSeekbarTouching = true;
                mHideHandler.removeMessages(WHAT_HIDE);
                if (mOnSeekListener != null) {
                    mOnSeekListener.onSeekStart();
                }
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                if (mOnSeekListener != null) {
                    mOnSeekListener.onSeekEnd(seekBar.getProgress());
                }

                isSeekbarTouching = false;
                mHideHandler.removeMessages(WHAT_HIDE);
                mHideHandler.sendEmptyMessageDelayed(WHAT_HIDE, DELAY_TIME);
            }
        };
        //seekbar的滑动监听
        mLargeSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
        mSmallSeekbar.setOnSeekBarChangeListener(seekBarChangeListener);
    }

    /**
     * 设置是否显示控制栏
     *
     * @param show fase：不显示
     */
    public void setControlBarCanShow(boolean show) {
        mControlBarCanShow = show;
        updateAllControlBar();
    }

    /**
     * 设置当前的播放状态
     *
     * @param playState 播放状态
     */
    public void setPlayState(PlayState playState) {
        mPlayState = playState;
        updatePlayStateBtn();
    }

    /**
     * 获取当前播放状态
     *
     * @return
     */
    public PlayState getPlayState() {
        return mPlayState;
    }

    /**
     * 设置视频信息
     *
     * @param aliyunMediaInfo 媒体信息
     */
    public void setMediaInfo(AliyunMediaInfo aliyunMediaInfo) {
        mAliyunMediaInfo = aliyunMediaInfo;
        updateLargeInfoBar();
    }

    /**
     * 更新视频进度
     *
     * @param position 位置，ms
     */
    public void setVideoPosition(int position) {
        mVideoPosition = position;
        updateSmallInfoBar();
        updateLargeInfoBar();
    }

    /**
     * 获取视频进度
     *
     * @return 视频进度
     */
    public int getVideoPosition() {
        return mVideoPosition;
    }

    private void updateAllViews() {
        updatePlayStateBtn();//更新播放状态
        updateLargeInfoBar();//更新大屏的显示信息
        updateSmallInfoBar();//更新小屏的显示信息
        updateScreenModeBtn();//更新大小屏信息
        updateAllControlBar();//更新控制栏显示
    }

    /**
     * 更新控制条的显示
     */
    private void updateAllControlBar() {
        if (mControlBar != null) {
            mControlBar.setVisibility(mControlBarCanShow ? VISIBLE : INVISIBLE);
        }
    }

    /**
     * 更新小屏下的控制条信息
     */
    private void updateSmallInfoBar() {
        if (mAliyunScreenMode == MpScreenMode.Full) {
            mSmallInfoBar.setVisibility(INVISIBLE);
        } else if (mAliyunScreenMode == MpScreenMode.Small) {
            //先设置小屏的info数据
            if (mAliyunMediaInfo != null) {
                mSmallDurationText.setText(formatMs(mAliyunMediaInfo.getDuration()));
                mSmallSeekbar.setMax((int) mAliyunMediaInfo.getDuration());
            } else {
                mSmallDurationText.setText(formatMs(0));
                mSmallSeekbar.setMax(0);
            }

            if (isSeekbarTouching) {
                //用户拖动的时候，不去更新进度值，防止跳动。
            } else {
                mSmallSeekbar.setSecondaryProgress(mVideoBufferPosition);
                mSmallSeekbar.setProgress(mVideoPosition);
                mSmallPositionText.setText(formatMs(mVideoPosition));
            }
            //然后再显示出来。
            mSmallInfoBar.setVisibility(VISIBLE);
        }
    }

    /**
     * 更新大屏下的控制条信息
     */
    private void updateLargeInfoBar() {
        if (mAliyunScreenMode == MpScreenMode.Small) {
            //里面包含了很多按钮，比如切换清晰度的按钮之类的
            mLargeInfoBar.setVisibility(INVISIBLE);
        } else if (mAliyunScreenMode == MpScreenMode.Full) {

            //先更新大屏的info数据
            if (mAliyunMediaInfo != null) {
                mLargeDurationText.setText(formatMs(mAliyunMediaInfo.getDuration()));
                mLargeSeekbar.setMax((int) mAliyunMediaInfo.getDuration());
            } else {
                mLargeDurationText.setText(formatMs(0));
                mLargeSeekbar.setMax(0);
            }

            if (isSeekbarTouching) {
                //用户拖动的时候，不去更新进度值，防止跳动。
            } else {
                mLargeSeekbar.setSecondaryProgress(mVideoBufferPosition);
                mLargeSeekbar.setProgress(mVideoPosition);
                mLargePositionText.setText(formatMs(mVideoPosition));
            }
            //然后再显示出来。
            mLargeInfoBar.setVisibility(VISIBLE);
        }


    }

    /**
     * 更新切换大小屏按钮的信息
     */
    private void updateScreenModeBtn() {
        if (mAliyunScreenMode == MpScreenMode.Full) {
            mScreenModeBtn.setImageResource(R.drawable.ic_video_screen_mode_small);
        } else {
            mScreenModeBtn.setImageResource(R.drawable.ic_video_screen_mode_large);
        }
    }

    /**
     * 更新播放按钮的状态
     */
    private void updatePlayStateBtn() {
        if (mPlayState == PlayState.NotPlaying) {
            mPlayStateBtn.setImageResource(R.drawable.ic_video_playstate_play);
        } else if (mPlayState == PlayState.Playing) {
            mPlayStateBtn.setImageResource(R.drawable.ic_video_playstate_pause);
        }
    }

    /**
     * 监听view是否可见。从而实现5秒隐藏的功能
     *
     * @param changedView
     * @param visibility
     */
    @Override
    protected void onVisibilityChanged(@Nullable View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (visibility == VISIBLE) {
            //如果变为可见了。启动五秒隐藏。
            hideDelayed();
        }
    }

    /**
     * 设置当前屏幕模式：全屏还是小屏
     *
     * @param mode {@link MpScreenMode#Small}：小屏. {@link MpScreenMode#Full}:全屏
     */
    @Override
    public void setScreenModeStatus(MpScreenMode mode) {
        mAliyunScreenMode = mode;
        updateLargeInfoBar();
        updateSmallInfoBar();
        updateScreenModeBtn();
    }

    /**
     * 重置状态
     */
    @Override
    public void reset() {
        mHideType = null;
        mAliyunMediaInfo = null;
        mVideoPosition = 0;
        mPlayState = PlayState.NotPlaying;
        isSeekbarTouching = false;
        updateAllViews();
    }

    /**
     * 显示画面
     */
    @Override
    public void show() {
        if (mHideType == HideType.End) {
            //如果是由于错误引起的隐藏，那就不能再展现了
            setVisibility(GONE);
        } else {
            updateAllViews();
            setVisibility(VISIBLE);
        }
    }

    /**
     * 隐藏画面
     */
    @Override
    public void hide(HideType hideType) {
        if (mHideType != HideType.End) {
            mHideType = hideType;
        }
        setVisibility(GONE);
    }

    public void setHideType(HideType hideType) {
        this.mHideType = hideType;
    }

    private void hideDelayed() {
        mHideHandler.removeMessages(WHAT_HIDE);
        mHideHandler.sendEmptyMessageDelayed(WHAT_HIDE, DELAY_TIME);
    }

    /**
     * 格式化毫秒数为 xx:xx:xx这样的时间格式。
     *
     * @param ms 毫秒数
     * @return 格式化后的字符串
     */
    public static String formatMs(long ms) {
        int seconds = (int) (ms / 1000);
        int finalSec = seconds % 60;
        int finalMin = seconds / 60 % 60;
        int finalHour = seconds / 3600;

        StringBuilder msBuilder = new StringBuilder("");
        if (finalHour > 9) {
            msBuilder.append(finalHour).append(":");
        } else if (finalHour > 0) {
            msBuilder.append("0").append(finalHour).append(":");
        }

        if (finalMin > 9) {
            msBuilder.append(finalMin).append(":");
        } else if (finalMin > 0) {
            msBuilder.append("0").append(finalMin).append(":");
        } else {
            msBuilder.append("00").append(":");
        }

        if (finalSec > 9) {
            msBuilder.append(finalSec);
        } else if (finalSec > 0) {
            msBuilder.append("0").append(finalSec);
        } else {
            msBuilder.append("00");
        }

        return msBuilder.toString();
    }

    /**
     * 设置当前缓存的进度，给seekbar显示
     *
     * @param mVideoBufferPosition 进度，ms
     */
    public void setVideoBufferPosition(int mVideoBufferPosition) {
        this.mVideoBufferPosition = mVideoBufferPosition;
        updateSmallInfoBar();
        updateLargeInfoBar();
    }

    /**
     * 解绑刷新页面
     */
    public void refreshByUnBind() {
        mSmallSeekbar.setProgress(0);
        mLargeSeekbar.setProgress(0);

    }

    public interface OnScreenModeClickListener {
        /**
         * 大小屏按钮点击事件
         */
        void onClick();
    }

    public void setOnScreenModeClickListener(OnScreenModeClickListener l) {
        mOnScreenModeClickListener = l;
    }

    public interface OnSeekListener {
        /**
         * seek结束事件
         */
        void onSeekEnd(int position);

        /**
         * seek开始事件
         */
        void onSeekStart();
    }

    public void setOnSeekListener(OnSeekListener onSeekListener) {
        mOnSeekListener = onSeekListener;
    }

    public interface OnPlayStateClickListener {
        /**
         * 播放按钮点击事件
         */
        void onPlayStateClick();
    }

    public void setOnPlayStateClickListener(OnPlayStateClickListener onPlayStateClickListener) {
        mOnPlayStateClickListener = onPlayStateClickListener;
    }

    /**
     * 点击切换mode 跳转页面的点击事件
     */
    public interface OnScreenJumpListener {
        void onScreenJump();
    }

    public void setOnScreenJumpListener(OnScreenJumpListener mOnScreenJumpListener) {
        this.mOnScreenJumpListener = mOnScreenJumpListener;
    }

    /**
     * 播放状态
     */
    public static enum PlayState {
        /**
         * Playing:正在播放
         * NotPlaying: 停止播放
         */
        Playing, NotPlaying
    }

    /**
     * 隐藏类
     */
    private static class HideHandler extends Handler {
        private WeakReference<MpVideoControl> controlViewWeakReference;

        public HideHandler(MpVideoControl controlView) {
            controlViewWeakReference = new WeakReference<MpVideoControl>(controlView);
        }

        @Override
        public void handleMessage(Message msg) {

            MpVideoControl controlView = controlViewWeakReference.get();
            if (controlView != null) {
                if (!controlView.isSeekbarTouching) {
                    controlView.hide(HideType.Normal);
                }
            }

            super.handleMessage(msg);
        }
    }
}
