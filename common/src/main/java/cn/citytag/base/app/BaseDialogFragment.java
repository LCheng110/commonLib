package cn.citytag.base.app;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.umeng.analytics.MobclickAgent;

import cn.citytag.base.command.ReplyCommand;

/**
 * Created by yangfeng01 on 2017/12/4.
 */

public abstract class BaseDialogFragment extends DialogFragment {

    protected View rootView;
    protected ReplyCommand sureCommand;

    protected abstract int getLayoutId();

    protected void beforeOnViewCreated() {

    }

    protected abstract void afterOnViewCreated();

    protected <T extends View> T findViewById(@IdRes int id) {
        return (T) rootView.findViewById(id);
    }

    protected void fullScreen() {
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.gravity = Gravity.CENTER;
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
//        window.setBackgroundDrawable(new ColorDrawable());
    }

    public <T> void setListener(ReplyCommand<T> replyCommand) {
        sureCommand = replyCommand;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle
            savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        beforeOnViewCreated();
        if (getLayoutId() <= 0) {
            return super.onCreateView(inflater, container, savedInstanceState);
        }
        if (rootView == null) {
            rootView = inflater.inflate(getLayoutId(), container, false);
        }
        return rootView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        afterOnViewCreated();
    }

    @Override
    public void onResume() {
        super.onResume();
        MobclickAgent.onPageStart(getClass().getName());
    }

    @Override
    public void onPause() {
        super.onPause();
        MobclickAgent.onPageEnd(getClass().getName());
    }

    /**
     * Version of {@link #show(FragmentManager, String)} that no-ops when an IllegalStateException
     * would otherwise occur.
     */
    public void showAllowingStateLoss(FragmentManager manager, String tag) {
        if (manager.isStateSaved()) {
            return;
        }
        show(manager, tag);
    }

}
