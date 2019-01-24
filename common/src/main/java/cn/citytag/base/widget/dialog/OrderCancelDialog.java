package cn.citytag.base.widget.dialog;

import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import cn.citytag.base.R;
import cn.citytag.base.app.BaseDialogFragment;
import cn.citytag.base.image.ImageLoader;

/**
 * Created by yuhuizhong on 2018/6/21.
 */

public class OrderCancelDialog extends BaseDialogFragment implements View.OnClickListener {
    private TextView tv_title;
    private TextView tv_cancel;
    private TextView tv_comfirm;
    private TextView tv_content;
    private ImageView iv_logo;
    private String title;
    private String strCancel;
    private String strComfirm;
    private String strContent;
    private String url;
    private OnDialogClick onDialogClick;

    public static OrderCancelDialog newInstance() {
        OrderCancelDialog dialog = new OrderCancelDialog();
        return dialog;
    }


    @Override
    protected void beforeOnViewCreated() {
        super.beforeOnViewCreated();
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());
    }

    private void initView() {
        tv_title = findViewById(R.id.tv_title);
        tv_content = findViewById(R.id.tv_content);
        tv_cancel = findViewById(R.id.tv_cancel);
        tv_comfirm = findViewById(R.id.tv_comfirm);
        iv_logo = findViewById(R.id.iv_logo);

        tv_title.setText(title);
        tv_content.setVisibility(TextUtils.isEmpty(strContent) ? View.GONE : View.VISIBLE);
        tv_cancel.setVisibility(TextUtils.isEmpty(strCancel) ? View.GONE : View.VISIBLE);
        tv_comfirm.setVisibility(TextUtils.isEmpty(strComfirm) ? View.GONE : View.VISIBLE);
        iv_logo.setVisibility(TextUtils.isEmpty(url) ? View.GONE : View.VISIBLE);
        tv_cancel.setText(strCancel);
        tv_comfirm.setText(strComfirm);
        tv_content.setText(strContent);
        ImageLoader.loadRoundImage(iv_logo, url, 4);
        tv_cancel.setOnClickListener(this);
        tv_comfirm.setOnClickListener(this);
    }


    public interface OnDialogClick {
        void onClick(OrderCancelDialog orderCancelDialog, int position);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_ordercancel;
    }

    @Override
    protected void afterOnViewCreated() {
        initView();
    }

    @Override
    public void onClick(View v) {
        if (onDialogClick == null) {
            return;
        }
        int i = v.getId();
        if (i == R.id.tv_cancel) {
            onDialogClick.onClick(this,0);

        } else if (i == R.id.tv_comfirm) {
            onDialogClick.onClick(this,1);

        }
    }

    public String getTitle() {
        return title == null ? "" : title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStrCancel() {
        return strCancel == null ? "" : strCancel;
    }

    public void setStrCancel(String strCancel) {
        this.strCancel = strCancel;
    }

    public String getStrComfirm() {
        return strComfirm == null ? "" : strComfirm;
    }

    public void setStrComfirm(String strComfirm) {
        this.strComfirm = strComfirm;
    }

    public void setOnDialogClick(OnDialogClick onDialogClick) {
        this.onDialogClick = onDialogClick;
    }

    public String getStrContent() {
        return strContent;
    }

    public void setStrContent(String strContent) {
        this.strContent = strContent;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
