package cn.citytag.base.view;

import android.content.res.Configuration;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.KeyEvent;

import cn.citytag.base.R;
import cn.citytag.base.databinding.ActivityVideoPlayerBinding;
import cn.citytag.base.utils.statusbarutil.StatusBarCompat;
import cn.citytag.base.view.base.ComBaseActivity;
import cn.citytag.base.vm.VideoPlayerVM;

/**
 * 作者：M. on 2018/12/20 14:07
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 */
public class VideoPlayerActivity extends ComBaseActivity<ActivityVideoPlayerBinding, VideoPlayerVM> {

    @Override
    public String getStatName() {
        return "视频播放页面";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_video_player;
    }

    @Override
    protected VideoPlayerVM createViewModel() {
        return new VideoPlayerVM(this, cvb);
    }

    @Override
    protected void afterOnCreate(@Nullable Bundle savedInstanceState) {
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.onResume();
    }

    @Override
    protected void onStop() {
        super.onStop();
        viewModel.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        viewModel.onDestroy();
    }

    @Override
    protected void setStatusBar() {
        StatusBarCompat.setStatusBarColor(this, Color.TRANSPARENT, false);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        viewModel.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (cvb.videoView != null) {
            boolean handler = cvb.videoView.onKeyDown(keyCode, event);
            if (!handler) {
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        viewModel.onWindowFocusChanged(hasFocus);
    }
}
