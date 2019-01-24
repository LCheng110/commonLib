package cn.citytag.base.vm;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.alivc.player.VcPlayerLog;
import com.aliyun.vodplayer.media.IAliyunVodPlayer;

import cn.citytag.base.constants.ExtraName;
import cn.citytag.base.databinding.ActivityVideoPlayerBinding;
import cn.citytag.base.utils.ActivityUtils;
import cn.citytag.base.utils.UIUtils;
import cn.citytag.base.utils.manager.PlayerProcessManager;
import cn.citytag.base.view.VideoPlayerActivity;
import cn.citytag.base.widget.video.VideoPlayerManager;

/**
 * 作者：M. on 2018/12/20 13:45
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 */
public class VideoPlayerVM extends BaseVM {
    private VideoPlayerActivity activity;
    private ActivityVideoPlayerBinding cvb;
    private String url;
    private double width, height;
    private boolean isBack = false;          //是否点击了返回

    public VideoPlayerVM(VideoPlayerActivity activity, ActivityVideoPlayerBinding cvb) {
        this.activity = activity;
        this.cvb = cvb;
        this.url = activity.getIntent().getStringExtra(ExtraName.VIDEO_PLAYER_URL);
        if (UIUtils.hasNotchInScreen(activity)) {
            cvb.ivBack.setPadding(0, UIUtils.getNotchHeight(activity), 0, 0);
        }
        initView();
    }

    private void initView() {
        cvb.videoView.setKeepScreenOn(true);
        VideoPlayerManager.get().reBindVideoPlayerView(cvb.videoView);
//        if (VideoPlayerManager.get().getPlayerState() == IAliyunVodPlayer.PlayerState.Paused) {
//            cvb.videoView.refreshByPause();
//        }
    }

    public void onResume() {
        updatePlayerViewMode();
        isBack = false;
        if (cvb.videoView != null) {
            cvb.videoView.onResume(true);
        }
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        //解决某些手机上锁屏之后会出现标题栏的问题。
        updatePlayerViewMode();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        updatePlayerViewMode();
    }

    public void onDestroy() {
        if (cvb.videoView != null) {
            cvb.videoView.onDestroy();
        }
    }

    public void onStop() {
        if (cvb.videoView != null) {
            if (!isBack)
            cvb.videoView.onStop();

        }
    }

    public void onBack() {
        isBack = true;
        activity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        Intent intent = new Intent();
        boolean isPlaying;
        if (VideoPlayerManager.get().getPlayerState() == IAliyunVodPlayer.PlayerState.Paused) {
            isPlaying = false;
        } else {
            isPlaying = true;
        }
        intent.putExtra(ExtraName.EXTRA_VIDEOPLAY_STATE, isPlaying);
        activity.setResult(ExtraName.EXTRA_CODE_VIDEO_RETURN_RESULT,intent);
        PlayerProcessManager.getInstance().savePlayerProcess(url,PlayerProcessManager.getInstance().getPlayerProcess(url)); //存储进度
        VideoPlayerManager.get().unBindVideoPlayerView();
        ActivityUtils.pop();
    }

    public void startVideoPlay() {
        cvb.videoView.switchPlayerState();
    }

    private void updatePlayerViewMode() {

        if (cvb.videoView != null) {
            int orientation = activity.getResources().getConfiguration().orientation;
            if (orientation == Configuration.ORIENTATION_PORTRAIT) {
                //转为竖屏了。
                //显示状态栏
                activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                cvb.videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
                //设置view的布局，宽高之类
                FrameLayout.LayoutParams aliVcVideoViewLayoutParams = (FrameLayout.LayoutParams) cvb.videoView
                        .getLayoutParams();
                if (width < height) {
                    aliVcVideoViewLayoutParams.height = (int) (UIUtils.getScreenWidth(activity) * 9.0f / 16);
                    aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                } else {
                    aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                    aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
                }
            } else if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
                //转到横屏了。
                //隐藏状态栏
                if (!isStrangePhone()) {
                    activity.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                            WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    cvb.videoView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                }

                //设置view的布局，宽高
                FrameLayout.LayoutParams aliVcVideoViewLayoutParams = (FrameLayout.LayoutParams) cvb.videoView
                        .getLayoutParams();
                aliVcVideoViewLayoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                aliVcVideoViewLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            }
        }
    }

    protected boolean isStrangePhone() {
        boolean strangePhone = "mx5".equalsIgnoreCase(Build.DEVICE)
                || "Redmi Note2".equalsIgnoreCase(Build.DEVICE)
                || "Z00A_1".equalsIgnoreCase(Build.DEVICE)
                || "hwH60-L02".equalsIgnoreCase(Build.DEVICE)
                || "hermes".equalsIgnoreCase(Build.DEVICE)
                || ("V4".equalsIgnoreCase(Build.DEVICE) && "Meitu".equalsIgnoreCase(Build.MANUFACTURER))
                || ("m1metal".equalsIgnoreCase(Build.DEVICE) && "Meizu".equalsIgnoreCase(Build.MANUFACTURER));

        VcPlayerLog.e("lfj1115 ", " Build.Device = " + Build.DEVICE + " , isStrange = " + strangePhone);
        return strangePhone;
    }
}
