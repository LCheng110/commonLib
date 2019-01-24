package cn.citytag.base.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.WindowManager;

import cn.citytag.base.R;
import cn.citytag.base.constants.ExtraName;
import cn.citytag.base.databinding.ActivityPreviewBinding;
import cn.citytag.base.view.base.ComBaseActivity;
import cn.citytag.base.vm.PreviewVM;

/**
 * Created by yangfeng01 on 2017/12/18.
 * 图片全屏预览页面
 */
public class PreviewActivity extends ComBaseActivity<ActivityPreviewBinding, PreviewVM> {

    private int index;

    private boolean isShowAlpha; //是否展示透明度动画

    @Override
    public String getStatName() {
        return "图片预览";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_preview;
    }

    @Override
    protected PreviewVM createViewModel() {
        Intent intent = getIntent();
        if (intent != null) {
            Bundle bundle = intent.getExtras();
            if (bundle != null && bundle.containsKey(ExtraName.INDEX)) {
                index = bundle.getInt(ExtraName.INDEX);
            }
            if (bundle != null && bundle.containsKey(ExtraName.EXTRA_COMMON_VAR)) {
                isShowAlpha = true;
            }

        }
        return new PreviewVM(this, cvb, index, isShowAlpha);
    }

    @Override
    protected void beforeOnCreate(@Nullable Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
    }

    @Override
    protected void afterOnCreate(@Nullable Bundle savedInstanceState) {

    }

    @Override
    public void onBackPressedSupport() {
        super.onBackPressedSupport();
        if (isShowAlpha) {
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        }
    }
}
