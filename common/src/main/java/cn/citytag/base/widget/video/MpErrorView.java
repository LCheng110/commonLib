package cn.citytag.base.widget.video;

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import cn.citytag.base.R;

/**
 * 作者：M. on 2018/12/19 13:47
 * <p>
 * 邮箱：qiuhuanming@maopp.cn
 */
public class MpErrorView extends RelativeLayout {
    private static final String TAG = MpErrorView.class.getSimpleName();
    //错误信息
    private TextView mMsgView;
    //重试的图片
    private View mRetryView;
    //重试的按钮
    private TextView mRetryBtn;


    private OnRetryClickListener mOnRetryClickListener = null;//重试点击事件

    public MpErrorView(Context context) {
        super(context);
        init();
    }


    public MpErrorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public MpErrorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources resources = getContext().getResources();

        View view = inflater.inflate(R.layout.view_error, null);

        int viewWidth = resources.getDimensionPixelSize(R.dimen.video_player_view_err_width);
        int viewHeight = resources.getDimensionPixelSize(R.dimen.video_player_view_err_height);

        LayoutParams params = new LayoutParams(viewWidth, viewHeight);
        addView(view, params);

        mRetryBtn = (TextView) view.findViewById(R.id.retry_btn);
        mMsgView = (TextView) view.findViewById(R.id.msg);
        mRetryView = view.findViewById(R.id.retry);
        //重试的点击监听
        mRetryView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mOnRetryClickListener != null) {
                    mOnRetryClickListener.onRetryClick();
                }
            }
        });

    }

    /**
     * 更新提示文字
     * @param errMsg 错误码
     */
    public void updateTips(String errMsg) {
        mMsgView.setText(errMsg);
    }

    /**
     * 重试的点击事件
     */
    public interface OnRetryClickListener {
        /**
         * 重试按钮点击
         */
        void onRetryClick();
    }

    /**
     * 设置重试点击事件
     * @param l 重试的点击事件
     */
    public void setOnRetryClickListener(OnRetryClickListener l) {
        mOnRetryClickListener = l;
    }
}
