package cn.citytag.base.widget.video;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

/**
 * 作者：M. on 2018/12/19 13:46
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 */
public class MpTipsView extends RelativeLayout {

    private static final String TAG = MpTipsView.class.getSimpleName();
    //错误提示
    private MpErrorView mErrorView = null;
    //重播提示
    private MpReplayView mReplayView = null;
    //缓冲加载提示
    private MpLoadingView mNetLoadingView = null;
    //网络请求加载提示
    private MpLoadingView mBufferLoadingView = null;
    //提示点击事件
    private OnTipClickListener mOnTipClickListener = null;

    //错误提示的重试点击事件
    private MpErrorView.OnRetryClickListener onRetryClickListener = new MpErrorView.OnRetryClickListener() {
        @Override
        public void onRetryClick() {
            if (mOnTipClickListener != null) {
                mOnTipClickListener.onRetryPlay();
            }
        }
    };

    //重播点击事件
    private MpReplayView.OnReplayClickListener onReplayClickListener = new MpReplayView.OnReplayClickListener() {
        @Override
        public void onReplay() {
            if (mOnTipClickListener != null) {
                mOnTipClickListener.onReplay();
            }
        }
    };

    public MpTipsView(Context context) {
        super(context);
    }

    public MpTipsView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MpTipsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    /**
     * 显示错误提示
     *
     * @param errorMsg   错误消息
     */
    public void showErrorTipView(String errorMsg) {
        if (mErrorView == null) {
            mErrorView = new MpErrorView(getContext());
            mErrorView.setOnRetryClickListener(onRetryClickListener);
            addSubView(mErrorView);
        }
        mErrorView.setVisibility(VISIBLE);
    }

    /**
     * 显示重播view
     */
    public void showReplayTipView() {
        if (mReplayView == null) {
            mReplayView = new MpReplayView(getContext());
            mReplayView.setOnReplayClickListener(onReplayClickListener);
            addSubView(mReplayView);
        }

        if (mReplayView.getVisibility() != VISIBLE) {
            mReplayView.setVisibility(VISIBLE);
        }
    }


    /**
     * 显示缓冲加载view
     */
    public void showBufferLoadingTipView() {
        if (mBufferLoadingView == null) {
            mBufferLoadingView = new MpLoadingView(getContext());
            addSubView(mBufferLoadingView);
        }
        if (mBufferLoadingView.getVisibility() != VISIBLE) {
            mBufferLoadingView.setVisibility(VISIBLE);
        }
    }

    /**
     * 更新缓冲加载的进度
     *
     * @param percent 进度百分比
     */
    public void updateLoadingPercent(int percent) {
        showBufferLoadingTipView();
        mBufferLoadingView.updateLoadingPercent(percent);
    }

    /**
     * 显示网络加载view
     */
    public void showNetLoadingTipView() {
        if (mNetLoadingView == null) {
            mNetLoadingView = new MpLoadingView(getContext());
            mNetLoadingView.setOnlyLoading();
            addSubView(mNetLoadingView);
        }

        if (mNetLoadingView.getVisibility() != VISIBLE) {
            mNetLoadingView.setVisibility(VISIBLE);
        }
    }


    /**
     * 把新增的view添加进来，居中添加
     *
     * @param subView 子view
     */
    private void addSubView(View subView) {
        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.addRule(CENTER_IN_PARENT);
        addView(subView, params);

        //同时需要更新新加的view的主题
//        if (subView instanceof ITheme) {
//            ((ITheme) subView).setTheme(mCurrentTheme);
//        }
    }


    /**
     * 隐藏所有的tip
     */
    public void hideAll() {
//        hideNetChangeTipView();
        hideErrorTipView();
        hideReplayTipView();
        hideBufferLoadingTipView();
        hideNetLoadingTipView();
    }

    /**
     * 隐藏缓冲加载的tip
     */
    public void hideBufferLoadingTipView() {
        if (mBufferLoadingView != null && mBufferLoadingView.getVisibility() == VISIBLE) {
            mBufferLoadingView.setVisibility(INVISIBLE);
        }
    }

    /**
     * 隐藏网络加载的tip
     */
    public void hideNetLoadingTipView() {
        if (mNetLoadingView != null && mNetLoadingView.getVisibility() == VISIBLE) {
            mNetLoadingView.setVisibility(INVISIBLE);
        }
    }

    /**
     * 隐藏重播的tip
     */
    public void hideReplayTipView() {
        if (mReplayView != null && mReplayView.getVisibility() == VISIBLE) {
            mReplayView.setVisibility(INVISIBLE);
        }
    }

    /**
     * 隐藏网络变化的tip
     */
//    public void hideNetChangeTipView() {
//        if (mNetChangeView != null && mNetChangeView.getVisibility() == VISIBLE) {
//            mNetChangeView.setVisibility(INVISIBLE);
//        }
//    }

    /**
     * 隐藏错误的tip
     */
    public void hideErrorTipView() {
        if (mErrorView != null && mErrorView.getVisibility() == VISIBLE) {
            mErrorView.setVisibility(INVISIBLE);
        }
    }

    /**
     * 错误的tip是否在显示，如果在显示的话，其他的tip就不提示了。
     *
     * @return true：是
     */
    public boolean isErrorShow() {
        if (mErrorView != null) {
            return mErrorView.getVisibility() == VISIBLE;
        } else {
            return false;
        }
    }

    /**
     * 隐藏网络错误tip
     */
    public void hideNetErrorTipView() {
        if (mErrorView != null && mErrorView.getVisibility() == VISIBLE) {
            mErrorView.setVisibility(INVISIBLE);
        }
    }

//    @Override
//    public void setTheme(AliyunVodPlayerView.Theme theme) {
//
//        mCurrentTheme = theme;
//        //判断子view是不是实现了ITheme的接口，从而达到更新主题的目的
//        int childCount = getChildCount();
//        for (int i = 0; i < childCount; i++) {
//            View child = getChildAt(i);
//            if (child instanceof ITheme) {
//                ((ITheme) child).setTheme(theme);
//            }
//        }
//    }

    /**
     * 提示view中的点击操作
     */
    public interface OnTipClickListener {
        /**
         * 重试播放
         */
        void onRetryPlay();

        /**
         * 重播
         */
        void onReplay();
    }

    /**
     * 设置提示view中的点击操作 监听
     *
     * @param l 监听事件
     */
    public void setOnTipClickListener(OnTipClickListener l) {
        mOnTipClickListener = l;
    }
}