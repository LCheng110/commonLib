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
 * 加载提示对话框。加载过程中，缓冲过程中会显示。
 */
public class MpLoadingView extends RelativeLayout {
    private static final String TAG = MpLoadingView.class.getSimpleName();
    //加载提示文本框
    private TextView mLoadPercentView;

    public MpLoadingView(Context context) {
        super(context);
        init();
    }

    public MpLoadingView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }


    public MpLoadingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        LayoutInflater inflater = (LayoutInflater) getContext()
                .getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        Resources resources = getContext().getResources();

        View view = inflater.inflate(R.layout.view_loading, null);

        int viewWidth = resources.getDimensionPixelSize(R.dimen.video_player_dialog_loading_width);
        int viewHeight = resources.getDimensionPixelSize(R.dimen.video_player_dialog_loading_width);

        LayoutParams params = new LayoutParams(viewWidth, viewHeight);
        addView(view, params);

        mLoadPercentView = view.findViewById(R.id.tv_speed);
        mLoadPercentView.setText(getContext().getString(R.string.video_player_loading) + " 0%");
    }

    /**
     * 更新加载进度
     *
     * @param percent 百分比
     */
    public void updateLoadingPercent(int percent) {
        mLoadPercentView.setText(getContext().getString(R.string.video_player_loading) + percent + "%");
    }

    /**
     * 只显示loading，不显示进度提示
     */
    public void setOnlyLoading() {
        mLoadPercentView.setVisibility(GONE);
    }

}
