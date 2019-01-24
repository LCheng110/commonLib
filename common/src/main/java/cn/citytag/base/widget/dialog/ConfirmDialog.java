package cn.citytag.base.widget.dialog;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import cn.citytag.base.R;
import cn.citytag.base.app.BaseDialogFragment;
import cn.citytag.base.command.ReplyCommand;
import cn.citytag.base.constants.BundleKey;
import cn.citytag.base.utils.StringUtils;

/**
 * Created by yangfeng01 on 2018/1/2.
 */

public class ConfirmDialog extends BaseDialogFragment implements View.OnClickListener {

    public static final String TAG = "ConfirmDialog";
    private String title;
    private String content;
    private String okStr;
    private String cancelStr;
    private ReplyCommand confirmListener;
    private ReplyCommand cancelListener;
    private boolean isClickOutsideNoCancel = false;

    public static ConfirmDialog newInstance(String title, String content) {
        ConfirmDialog dialog = new ConfirmDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.KEY_TITLE, title);
        bundle.putString(BundleKey.KEY_CONTENT, content);
        dialog.setArguments(bundle);
        return dialog;
    }


    public static ConfirmDialog newInstance(String content) {
        ConfirmDialog dialog = new ConfirmDialog();
        Bundle bundle = new Bundle();
        bundle.putString(BundleKey.KEY_CONTENT, content);
        dialog.setArguments(bundle);
        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Bundle bundle = getArguments();
        if (bundle != null) {
            String title = bundle.getString(BundleKey.KEY_TITLE);
            String content = bundle.getString(BundleKey.KEY_CONTENT);
            if (StringUtils.isNotEmpty(title)) {
                this.title = title;
            }
            if (StringUtils.isNotEmpty(content)) {
                this.content = content;
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_confirm;
    }

    @Override
    protected void afterOnViewCreated() {
        initDialogSize();
        if (isClickOutsideNoCancel) {
            getDialog().setCanceledOnTouchOutside(false);
        }
        TextView tvFirst = findViewById(R.id.tv_first);
        TextView tvSecond = findViewById(R.id.tv_second);
        TextView tvConfirm = findViewById(R.id.tv_sure);
        TextView tvCancel = findViewById(R.id.tv_cancel);
        if (StringUtils.isNotEmpty(okStr)) {
            tvConfirm.setText(okStr);
        }
        if (StringUtils.isNotEmpty(cancelStr)) {
            tvCancel.setText(cancelStr);
        }
        if (StringUtils.isNotEmpty(title) && StringUtils.isNotEmpty(content)) {
            tvFirst.setVisibility(View.VISIBLE);
            tvSecond.setVisibility(View.VISIBLE);
            tvFirst.setText(title);
            tvSecond.setText(content);
        } else if (StringUtils.isEmpty(title) && StringUtils.isNotEmpty(content)) {
            tvFirst.setVisibility(View.VISIBLE);
            tvSecond.setVisibility(View.GONE);
            tvFirst.setText(content);
        }
        tvConfirm.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    protected void initDialogSize() {

    }

    public ConfirmDialog setTitle(String title) {
        this.title = title;
        return this;
    }

    public ConfirmDialog setContent(String content) {
        this.content = content;
        return this;
    }

    public ConfirmDialog setContent(String content, boolean isClickOutsideNoCancel) {
        this.content = content;
        this.isClickOutsideNoCancel = isClickOutsideNoCancel;
        return this;
    }

    public ConfirmDialog confirm(ReplyCommand replyCommand) {
        this.confirmListener = replyCommand;
        return this;
    }

    public ConfirmDialog confirm(String okStr, ReplyCommand replyCommand) {
        this.okStr = okStr;
        this.confirmListener = replyCommand;
        return this;
    }

    public ConfirmDialog cancel(ReplyCommand replyCommand) {
        this.cancelListener = replyCommand;
        return this;
    }

    public ConfirmDialog cancel(String cancelStr, ReplyCommand replyCommand) {
        this.cancelStr = cancelStr;
        this.cancelListener = replyCommand;
        return this;
    }

    @Override
    public void onClick(View view) {

        int i = view.getId();
        if (i == R.id.tv_sure) {
            try {
                confirmListener.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else if (i == R.id.tv_cancel) {
            try {
                cancelListener.execute();
            } catch (Exception e) {
                e.printStackTrace();
            }

        }
        dismiss();
    }

}
