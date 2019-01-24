package cn.citytag.base.widget;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import cn.citytag.base.R;
import cn.citytag.base.image.ImageLoader;
import cn.citytag.base.utils.L;
import cn.citytag.base.utils.StringUtils;

/**
 * Created by yangfeng01 on 2018/4/11.
 * <p>a small util layout for processing content, empty and newtwork error state.</p>
 */
public class StateLayout extends FrameLayout implements View.OnClickListener {

    private static final String LOG_TAG = StateLayout.class.getSimpleName();
    private static final String TAG_LOADING = "tag_loading_layout";
    private static final String TAG_EMPTY = "tag_empty_layout";
    private static final String TAG_ERROR = "tag_error_layout";

    private final String CONTENT = "type_content";
    private final String LOADING = "type_loading";
    private final String EMPTY = "type_empty";
    private final String ERROR = "type_error";

    private View loadingView;
    private View emptyView;
    private View errorView;
    private List<View> contentViews = new ArrayList<>();
    private OnRetryClickListener onRetryClickListener;

    private int emptyImgId;
    private String emptyDesc;
    private int errorImdId;
    private String errorDesc;

    private String state = CONTENT;

    public StateLayout(Context context) {
        this(context, null);
    }

    public StateLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public StateLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        inflate(context, R.layout.layout_state, this);
    }

    @Override
    public void addView(@NonNull View child, int index, ViewGroup.LayoutParams params) {
        super.addView(child, index, params);
        if ((child.getId() != R.id.id_stub_error && child.getId() != R.id.id_stub_empty && child.getId() != R.id.id_stub_loading)) {
            if (child.getId() == R.id.id_layout_error) {
                return;
            }
            if (child.getId() == R.id.id_layout_empty) {
                return;
            }
            if (child.getId() == R.id.id_layout_loading) {
                return;
            }
            contentViews.add(child);
        }
    }

    public void showContent() {
        switchState(CONTENT, Collections.<Integer>emptyList());
    }

    public void showContent(List<Integer> skipIds) {
        switchState(CONTENT, skipIds);
    }

    public void showLoading() {
        switchState(LOADING, Collections.<Integer>emptyList());
    }

    public void showLoading(List<Integer> skipIds) {
        switchState(LOADING, skipIds);
    }

    public void showEmpty() {
        switchState(EMPTY, Collections.<Integer>emptyList());
    }

    public void showEmpty(List<Integer> skipIds) {
        switchState(EMPTY, skipIds);
    }

    public void showError() {
        switchState(ERROR, Collections.<Integer>emptyList());
    }

    public void showError(List<Integer> skipIds) {
        switchState(ERROR, skipIds);
    }

    public String getState() {
        return state;
    }

    public boolean isContent() {
        return state.equals(CONTENT);
    }

    public boolean isLoading() {
        return state.equals(LOADING);
    }

    public boolean isEmpty() {
        return state.equals(EMPTY);
    }

    public boolean isError() {
        return state.equals(ERROR);
    }

    private void switchState(String state, List<Integer> skipIds) {
        this.state = state;

        switch (state) {
            case CONTENT:
                hideLoadingView();
                hideEmptyView();
                hideErrorView();

                setContentVisibility(true, skipIds);
                break;

            case LOADING:
                hideEmptyView();
                hideErrorView();

                enSureLoading();
                loadingView.setVisibility(VISIBLE);
                setContentVisibility(false, skipIds);
                break;

            case EMPTY:
                hideLoadingView();
                hideErrorView();

                enSureEmpty();
                emptyView.setVisibility(VISIBLE);
                setContentVisibility(false, skipIds);
                break;

            case ERROR:
                hideLoadingView();
                hideEmptyView();

                enSureError();
                errorView.setVisibility(VISIBLE);
                setContentVisibility(false, skipIds);
                break;
        }
    }

    private void enSureLoading() {
        if (loadingView == null) {
            loadingView = ((ViewStub) findViewById(R.id.id_stub_loading)).inflate();
			ImageView progressHud = findViewById(R.id.spinnerImageView);
			ImageLoader.loadImage(progressHud, R.drawable.ic_loading);
        }
    }

    private void enSureEmpty() {
        if (emptyView == null) {
            emptyView = ((ViewStub) findViewById(R.id.id_stub_empty)).inflate();
            if (emptyImgId > 0) {
                ((ImageView) findViewById(R.id.empty_img)).setImageResource(emptyImgId);
            }
            if (!StringUtils.isEmpty(emptyDesc)) {
                ((TextView) findViewById(R.id.empty_desc)).setText(emptyDesc);
            }
        } else {
            if (emptyView.getParent() == null) {
                L.d("yangfeng", "emptyView.getParent() == " + emptyView.getParent());
                LayoutParams lp = new LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT);
                lp.gravity = Gravity.CENTER;
                addView(emptyView, lp);
            }
        }

    }

    private void enSureError() {
        if (errorView == null) {
            errorView = ((ViewStub) findViewById(R.id.id_stub_error)).inflate();
            if (errorImdId > 0) {
				((ImageView) errorView.findViewById(R.id.error_image)).setImageResource(errorImdId);
            }
            if (!StringUtils.isEmpty(errorDesc)) {
                ((TextView) errorView.findViewById(R.id.error_tv)).setText(errorDesc);
            }
            if (onRetryClickListener != null) {
                View retry = errorView.findViewById(R.id.error_retry);
                retry.setOnClickListener(this);
            }
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.error_retry) {
            if (onRetryClickListener != null) {
                onRetryClickListener.onRetryClick();
            }

        } else {
        }
    }

    private void setContentVisibility(boolean visible, List<Integer> skipIds) {
        for (View v : contentViews) {
            if (!skipIds.contains(v.getId())) {
                v.setVisibility(visible ? View.VISIBLE : View.GONE);
            }
        }
    }

    private void hideLoadingView() {
        if (loadingView != null) {
            loadingView.setVisibility(GONE);
        }
    }

    private void hideEmptyView() {
        if (emptyView != null) {
            emptyView.setVisibility(GONE);
        }
    }

    private void hideErrorView() {
        if (errorView != null) {
            errorView.setVisibility(GONE);
        }
    }

    public void setEmptyImgId(int imgId) {
        emptyImgId = imgId;
    }

    public void setEmptyDesc(String desc) {
        emptyDesc = desc;
    }

    public void setEmptyView(View emptyView) {
        this.emptyView = emptyView;
    }

    public void setErrorImdId(int imgId) {
        errorImdId = imgId;
    }

    public void setErrorDesc(String desc) {
        errorDesc = desc;
    }

    public void setOnRetryClickListener(OnRetryClickListener listener) {
        onRetryClickListener = listener;
    }

    public interface OnRetryClickListener {
        void onRetryClick();
    }
}
