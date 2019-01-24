package cn.citytag.base.adapter.holder;

import android.support.annotation.ColorInt;
import android.support.annotation.StringRes;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import cn.citytag.base.R;
import cn.citytag.base.widget.SlideEditRecycleView;

/**
 * Created by liucheng on 2018/9/20.
 * item的根布局需要设置为FrameLayout
 */
public abstract class SlideEditViewHolder extends BaseViewHolder {
    private LinearLayout ll_item_root;
    private TextView tv_item_edit, tv_item_delete;
    private FrameLayout.LayoutParams layoutParams;
    private SlideEditRecycleView.OnItemClickListener onItemClickListener;

    public SlideEditViewHolder(View itemView) {
        super(itemView);
        View editView = LayoutInflater.from(itemView.getContext()).inflate(R.layout.include_item_edit, (ViewGroup) itemView, false);
        ((ViewGroup) itemView).addView(editView);
        ll_item_root = editView.findViewById(R.id.ll_item_root);
        tv_item_edit = editView.findViewById(R.id.tv_item_edit);
        tv_item_delete = editView.findViewById(R.id.tv_item_delete);
        layoutParams = (FrameLayout.LayoutParams) ll_item_root.getLayoutParams();
    }

    public void setEditText(String text) {
        tv_item_edit.setText(text);
        showEditItem();
    }

    public void setEditText(@StringRes int stringId) {
        tv_item_edit.setText(stringId);
        showEditItem();
    }

    public void setEditBackgroundColor(@ColorInt int color) {
        tv_item_edit.setBackgroundColor(color);
        showEditItem();
    }

    public void setDeleteText(String text) {
        tv_item_delete.setText(text);
        showDeleteItem();
    }

    public void setDeleteText(@StringRes int stringId) {
        tv_item_delete.setText(stringId);
        showDeleteItem();
    }

    public void setDeleteBackgroundColor(@ColorInt int color) {
        tv_item_delete.setBackgroundColor(color);
        showDeleteItem();
    }

    public void showEditItem() {
        if (tv_item_edit.getVisibility() == View.VISIBLE) {
            return;
        }
        tv_item_edit.setVisibility(View.VISIBLE);
        if (tv_item_delete.getVisibility() == View.VISIBLE) {
            layoutParams.setMarginEnd(tv_item_edit.getResources().getDimensionPixelSize(R.dimen.item_edit_hide_width));
        } else {
            layoutParams.setMarginEnd(tv_item_edit.getResources().getDimensionPixelSize(R.dimen.item_edit_hide_width_single));
        }
    }

    public void showDeleteItem() {
        if (tv_item_delete.getVisibility() == View.VISIBLE) {
            return;
        }
        tv_item_delete.setVisibility(View.VISIBLE);
        if (tv_item_edit.getVisibility() == View.VISIBLE) {
            layoutParams.setMarginEnd(tv_item_edit.getResources().getDimensionPixelSize(R.dimen.item_edit_hide_width));
        } else {
            layoutParams.setMarginEnd(tv_item_edit.getResources().getDimensionPixelSize(R.dimen.item_edit_hide_width_single));
        }
    }

    public void hideEditItem() {
        tv_item_edit.setVisibility(View.GONE);
        if (tv_item_delete.getVisibility() == View.VISIBLE) {
            layoutParams.setMarginEnd(tv_item_edit.getResources().getDimensionPixelSize(R.dimen.item_edit_hide_width_single));
        } else {
            layoutParams.setMarginEnd(0);
        }
    }

    public void hideDeleteItem() {
        tv_item_delete.setVisibility(View.GONE);
        if (tv_item_edit.getVisibility() == View.VISIBLE) {
            layoutParams.setMarginEnd(tv_item_edit.getResources().getDimensionPixelSize(R.dimen.item_edit_hide_width_single));
        } else {
            layoutParams.setMarginEnd(0);
        }
    }

    public void setOnEditListener(final View.OnClickListener clickListener) {
        tv_item_edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick();
                }
                clickListener.onClick(v);
            }
        });
    }

    public void setOnDeleteListener(final View.OnClickListener clickListener) {
        tv_item_delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    onItemClickListener.onItemClick();
                }
                clickListener.onClick(v);
            }
        });
    }

    public void setOnItemClickListener(SlideEditRecycleView.OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public int getVisibilityCount() {
        int count = 0;
        if (tv_item_edit.getVisibility() == View.VISIBLE) {
            count++;
        }
        if (tv_item_delete.getVisibility() == View.VISIBLE) {
            count++;
        }
        return count;
    }
}
