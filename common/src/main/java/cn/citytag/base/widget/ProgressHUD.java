package cn.citytag.base.widget;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StyleRes;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.TextView;

import java.lang.ref.WeakReference;

import cn.citytag.base.R;
import cn.citytag.base.image.ImageLoader;

/**
 * Created by yangfeng01 on 2017/11/28.
 */

public class ProgressHUD extends Dialog {

    private static WeakReference<Context> sContextRef;
    private static WeakReference<ProgressHUD> sHudRef;
    private static ProgressHUD mDialog;

    public ProgressHUD(@NonNull Context context) {
        super(context);
    }

    public ProgressHUD(@NonNull Context context, @StyleRes int themeResId) {
        super(context, themeResId);
    }

    public static void show(Context context, String message, boolean cancelable, OnCancelListener onCancelListener) {
        //message = context.getString(R.string.loading);
        if (sHudRef != null && sHudRef.get() != null && sHudRef.get().isShowing()) {
            sHudRef.get().dismiss();
        }

        if (context == null || !(context instanceof Activity) || ((Activity) context).isFinishing()) {
            return;
        }
        sContextRef = new WeakReference<>(context);
        sHudRef = new WeakReference<>(new ProgressHUD(sContextRef.get(), R.style.ProgressHUD));
        final ProgressHUD dialog = sHudRef.get();
        if (dialog == null) {
            return;
        }
        dialog.setContentView(R.layout.progress_hud);
        TextView messageText = dialog.findViewById(R.id.message);
        /*if (message == null || message.length() == 0) {
            messageText.setVisibility(View.GONE);
        } else {
            messageText.setText(message);
        }*/
        ImageView progressHud = dialog.findViewById(R.id.spinnerImageView);
        ImageLoader.loadImage(progressHud, R.drawable.ic_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(onCancelListener);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        //WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        //lp.dimAmount = 0.2f;
        //dialog.getWindow().setAttributes(lp);
        if (!dialog.isShowing()) {
            dialog.show();
        }
    }

    public static ProgressHUD showAlways(Context context, boolean cancelable, OnCancelListener onCancelListener) {
        ProgressHUD dialog = new ProgressHUD(context, R.style.ProgressHUD);
        dialog.setContentView(R.layout.progress_hud);
        TextView messageText = dialog.findViewById(R.id.message);
        ImageView progressHud = dialog.findViewById(R.id.spinnerImageView);
        ImageLoader.loadImage(progressHud, R.drawable.ic_loading);
        dialog.setCanceledOnTouchOutside(false);
        dialog.setCancelable(cancelable);
        dialog.setOnCancelListener(onCancelListener);
        dialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        //WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        //lp.dimAmount = 0.2f;
        //dialog.getWindow().setAttributes(lp);
        if (!dialog.isShowing()) {
            dialog.show();
        }
        return dialog;
    }

    public static ProgressHUD showIMLogin(Context context, boolean cancelable, OnCancelListener onCancelListener) {
        mDialog = new ProgressHUD(context, R.style.ProgressHUD);
        mDialog.setContentView(R.layout.progress_hud);
        TextView messageText = mDialog.findViewById(R.id.message);
        ImageView progressHud = mDialog.findViewById(R.id.spinnerImageView);
        ImageLoader.loadImage(progressHud, R.drawable.ic_loading);
        mDialog.setCanceledOnTouchOutside(false);
        mDialog.setCancelable(cancelable);
        mDialog.setOnCancelListener(onCancelListener);
        mDialog.getWindow().getAttributes().gravity = Gravity.CENTER;
        //WindowManager.LayoutParams lp = dialog.getWindow().getAttributes();
        //lp.dimAmount = 0.2f;
        //dialog.getWindow().setAttributes(lp);
        if (!mDialog.isShowing()) {
            mDialog.show();
        }
        return mDialog;
    }

    public static void dismissHUD() {
        if (sHudRef != null && sHudRef.get() != null && sHudRef.get().isShowing()) {
            sHudRef.get().dismiss();
        }
    }

    public static void dismissIMHUD() {
        if (mDialog != null) {
            mDialog.dismiss();
        }
    }

}
