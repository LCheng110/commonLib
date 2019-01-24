package cn.citytag.base.view;


import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import java.util.List;

import cn.citytag.base.R;
import cn.citytag.base.databinding.CommonActivityPublishMenuBinding;
import cn.citytag.base.utils.statusbarutil.StatusBarCompat;
import cn.citytag.base.view.base.ComBaseActivity;
import cn.citytag.base.vm.PublishMenuVM;

public class PublishMenuActivity extends ComBaseActivity<CommonActivityPublishMenuBinding, PublishMenuVM> {

    @Override
    public String getStatName() {
        return "发布菜单";
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.common_activity_publish_menu;
    }

    @Override
    protected PublishMenuVM createViewModel() {
        return new PublishMenuVM(cvb, this);
    }

    @Override
    protected void beforeOnCreate(@Nullable Bundle savedInstanceState) {


        // setCurrentStatusBar();
//        supportRequestWindowFeature(Window.FEATURE_NO_TITLE);//去掉标题头
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            //透明状态栏
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
//        }


    }


    @Override
    protected void afterOnCreate(@Nullable Bundle savedInstanceState) {

        // setCurrentStatusBar();
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            // 设置根布局的参数
//            ViewGroup rootView = (ViewGroup) ((ViewGroup) findViewById(android.R.id.content)).getChildAt(0);
//            rootView.setFitsSystemWindows(true);
//            rootView.setClipToPadding(true);
//        }


        //窗口对齐屏幕宽度
        Window win = this.getWindow();
        win.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams layoutParams = win.getAttributes();
        //layoutParams.alpha = 0.6f ;
        layoutParams.width = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.height = WindowManager.LayoutParams.MATCH_PARENT;
        layoutParams.gravity = Gravity.BOTTOM;//设置对话框在底部显示
        win.setAttributes(layoutParams);


    }


    private void setCurrentStatusBar() {

        if (Build.VERSION.SDK_INT >= 21 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            View decorView = getWindow().getDecorView();
            int option = View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE;
            decorView.setSystemUiVisibility(option);
            getWindow().setNavigationBarColor(Color.TRANSPARENT);
            getWindow().setStatusBarColor(Color.TRANSPARENT);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            StatusBarCompat.setStatusBarColor(this, Color.TRANSPARENT, true);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        super.onPermissionsGranted(requestCode, perms);
        Log.e("TAG", "onPermissionsGranted: ");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        super.onPermissionsDenied(requestCode, perms);
        Log.e("TAG", "onPermissionsGranted: +deny");
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.e("TAG", "onPermissionsGranted:  + result");
    }


    @Override
    public void onBackPressedSupport() {
        if (viewModel != null) {
            viewModel.onBackPress();
        }
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransition(0, 0);
    }
}
