package cn.citytag.base.widget.video;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.citytag.base.R;

/**
 * 暂停提示框
 */
public class MpPauseView extends RelativeLayout {
    // 播放按钮
    private ImageView mPlayBtn;
    // 播放事件监听
    private OnPlayClickListener mOnPlayClickListener = null;

    public MpPauseView(Context context) {
        super(context);
        init();
    }

    public MpPauseView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MpPauseView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources resources = getContext().getResources();

        View view = inflater.inflate(R.layout.view_pause, null);
        int viewWidth = resources.getDimensionPixelSize(R.dimen.video_player_view_err_width);
        int viewHeight = resources.getDimensionPixelSize(R.dimen.video_player_view_err_height);


        LayoutParams params = new LayoutParams(viewWidth, viewHeight);
        addView(view, params);

        //设置监听
        mPlayBtn = view.findViewById(R.id.iv_play);
        mPlayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnPlayClickListener != null) {
                    mOnPlayClickListener.onPlay();
                }
            }
        });

    }

    /**
     * 点击播放事件
     */
    public interface OnPlayClickListener {
        /**
         * 点击播放事件
         */
        void onPlay();
    }

    /**
     * 设置重播事件监听
     *
     * @param l 重播事件
     */
    public void setOnPlayClickListener(OnPlayClickListener l) {
        mOnPlayClickListener = l;
    }

}
