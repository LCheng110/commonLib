package cn.citytag.base.widget.video;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import cn.citytag.base.R;

/**
 * 重播提示对话框。播放结束的时候会显示这个界面
 */
public class MpReplayView extends RelativeLayout {
    //重播按钮
    private ImageView mReplayBtn;
    //重播事件监听
    private OnReplayClickListener mOnReplayClickListener = null;

    public MpReplayView(Context context) {
        super(context);
        init();
    }

    public MpReplayView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MpReplayView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources resources = getContext().getResources();

        View view = inflater.inflate(R.layout.view_replay, null);
//        int viewWidth = resources.getDimensionPixelSize(R.dimen.video_player_view_err_width);
//        int viewHeight = resources.getDimensionPixelSize(R.dimen.video_player_view_err_height);


        LayoutParams params = new LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        addView(view, params);

        //设置监听
        mReplayBtn = view.findViewById(R.id.iv_replay);
        mReplayBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnReplayClickListener != null) {
                    mOnReplayClickListener.onReplay();
                }
            }
        });

    }

    /**
     * 重播点击事件
     */
    public interface OnReplayClickListener {
        /**
         * 重播事件
         */
        void onReplay();
    }

    /**
     * 设置重播事件监听
     *
     * @param l 重播事件
     */
    public void setOnReplayClickListener(OnReplayClickListener l) {
        mOnReplayClickListener = l;
    }

}
