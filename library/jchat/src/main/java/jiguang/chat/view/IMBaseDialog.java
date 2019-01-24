package jiguang.chat.view;

import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;

/**
 * Created by zhaoyuanchao on 2018/8/15.
 */

public abstract class IMBaseDialog extends DialogFragment{
    protected View rootView;
    protected abstract int getLayoutId();
    protected void beforeOnViewCreated(){}
    protected abstract void afterOnViewCreated();

    protected <T extends View> T findViewById(@IdRes int id){
        return (T)rootView.findViewById(id);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
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

    }

    @Override
    public void onPause() {
        super.onPause();

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
