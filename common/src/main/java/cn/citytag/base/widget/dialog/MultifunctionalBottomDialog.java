package cn.citytag.base.widget.dialog;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

import cn.citytag.base.R;
import cn.citytag.base.app.BaseDialogFragment;
import cn.citytag.base.utils.UIUtils;

/**
 * 作者：Lgc
 * 创建时间：2018/12/21
 * 更改时间：2018/11/21
 */
public class MultifunctionalBottomDialog extends BaseDialogFragment {

    private LinearLayout mContainerLinearLayout; //容器布局

    private ArrayList<String> mArrayTitle;  //title集合

    private TextView mTvCancel; //取消

    private CallBack mCallBack; //回掉监听

    public CallBack getCallBack() {
        return mCallBack;
    }

    public void setCallBack(CallBack mCallBack) {
        this.mCallBack = mCallBack;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_multifunctional_bottom;
    }

    @Override
    protected void beforeOnViewCreated() {
        super.beforeOnViewCreated();
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.BOTTOM;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());
    }

    @Override
    protected void afterOnViewCreated() {
        initView();
    }

    private void initView() {
        mContainerLinearLayout = findViewById(R.id.ll_container);
        mTvCancel = findViewById(R.id.tv_cancel);
        mTvCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        addView();
    }

    /**
     * 添加View
     */
    private void addView() {
        int size = mArrayTitle.size();
        for (int i = 0; i < size; i++) {
            TextView textView = new TextView(getContext());
            textView.setGravity(Gravity.CENTER);
            textView.setText(mArrayTitle.get(0));
            textView.setTextColor(Color.parseColor("#4A4A4A"));
            textView.setTextSize(16);
            LinearLayout.LayoutParams textViewLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                    , UIUtils.dip2px(48));
            textView.setLayoutParams(textViewLayoutParams);
            int finalI = i;
            textView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCallBack != null) {
                        dismiss();
                        mCallBack.click(finalI);
                    }
                }
            });
            mContainerLinearLayout.addView(textView);
            View line = new View(getContext());
            LinearLayout.LayoutParams lineLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT
                    , UIUtils.dip2px((float) 0.5));
            lineLayoutParams.leftMargin = UIUtils.dip2px(16);
            lineLayoutParams.rightMargin = UIUtils.dip2px(16);
            line.setBackgroundColor(Color.parseColor("#EAEAEA"));
            line.setLayoutParams(lineLayoutParams);
            mContainerLinearLayout.addView(line);

        }
    }

    /**
     * @param titleArray
     */
    public void setTitleArray(ArrayList<String> titleArray) {
        this.mArrayTitle = titleArray;
    }

    public interface CallBack {
        void click(int index);
    }
}
