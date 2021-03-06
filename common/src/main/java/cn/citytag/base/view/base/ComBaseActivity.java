package cn.citytag.base.view.base;

import android.content.Intent;
import android.content.IntentFilter;
import android.databinding.DataBindingUtil;
import android.databinding.ViewDataBinding;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.inputmethod.InputMethodManager;
import android.widget.RelativeLayout;

import com.bqs.risk.df.android.BqsDF;
import com.bqs.risk.df.android.BqsParams;
import com.bqs.risk.df.android.OnBqsDFListener;
import com.sensorsdata.analytics.android.sdk.ScreenAutoTracker;
import com.trello.rxlifecycle2.components.support.RxAppCompatActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.util.List;

import cn.citytag.base.BR;
import cn.citytag.base.R;
import cn.citytag.base.utils.network.NetChangeEvent;
import cn.citytag.base.utils.network.NetStateReceiver;
import cn.citytag.base.view.delegate.ComBaseActivityDelegate;
import cn.citytag.base.vm.BaseVM;
import cn.citytag.base.app.IStatLabel;
import cn.citytag.base.app.delegate.ActivityDelegate;
import cn.citytag.base.command.ReplyCommand;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.helpers.permission.PermissionChecker;
import cn.citytag.base.utils.AppUtils;
import cn.citytag.base.utils.L;
import cn.citytag.base.utils.StringUtils;
import cn.citytag.base.utils.statusbarutil.StatusBarCompat;
import cn.citytag.base.widget.dialog.ConfirmDialog;
import io.reactivex.functions.Action;

import static cn.citytag.base.constants.Constants.REQUEST_CODE_PERMISSION_SETTING;


/**
 * Created by yangfeng01 on 2017/11/8.
 */
public abstract class ComBaseActivity<CVB extends ViewDataBinding, VM extends BaseVM> extends RxAppCompatActivity
        implements IStatLabel, PermissionChecker.PermissionCallbacks, OnBqsDFListener, ScreenAutoTracker {

    protected String tag = getClass().getSimpleName();
    protected CVB cvb;
    protected VM viewModel;

    private ComBaseActivityDelegate delegate;
    private cn.citytag.base.databinding.ActivityBaseComBinding activityBaseBinding;
    private BaseVM baseVM;
    private RelativeLayout content;
    private ComBaseLceToolbarActivity.ToolbarStyle toolbarStyle = ToolbarStyle.LIGHT;


    private NetStateReceiver netStateReceiver;/*网络状态变化的广播接收器*/


    public enum ToolbarStyle {
        LIGHT,
        DARK;

        ToolbarStyle() {

        }
    }

    public abstract String getStatName();

    protected abstract int getLayoutResId();

    protected abstract VM createViewModel();

    protected void beforeOnCreate(@Nullable Bundle savedInstanceState) {

    }


    protected void getBundle() {

    }

    protected abstract void afterOnCreate(@Nullable Bundle savedInstanceState);

    /**
     * todo 可以是一个delegate list
     * 创建Activity生命周期代理类
     *
     * @return
     */
    public ComBaseActivityDelegate createActivityDelegate() {
        return new ComBaseActivityDelegate(this, tag, hasFragment());
    }

    /**
     * Activity中是否有Fragment
     */
    protected boolean hasFragment() {
        return false;
    }

    public ComBaseActivity() {
        delegate = createActivityDelegate();
    }

    public void setupToolbar(Toolbar toolbar, String title) {
        toolbarStyle = toolbarStyle();
        switch (toolbarStyle) {
            case LIGHT:
                StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.color_white));
                toolbar.setBackgroundResource(R.drawable.shape_divider_textview_bg);
                toolbar.setTitleTextColor(getResources().getColor(R.color.textColorSecondary));
                toolbar.setNavigationIcon(R.drawable.ic_back_drak);
                break;

            case DARK:
                StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.color_393939));
                toolbar.setBackgroundColor(getResources().getColor(R.color.color_393939));
                toolbar.setTitleTextColor(getResources().getColor(R.color.color_white));
                toolbar.setNavigationIcon(R.drawable.ic_back_white);
                break;
            default:
                break;
        }
        toolbar.setTitle(title);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);    // 返回监听，android.R.id.home
    }

    protected ToolbarStyle toolbarStyle() {
        return toolbarStyle;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        beforeOnCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        delegate.onCreate(savedInstanceState);
        setStatusBar();
        initView(savedInstanceState);
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
        if (viewModel != null)
            viewModel.onViewModelCreated();
        //注册网络状态监听广播
        netStateReceiver = new NetStateReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netStateReceiver, filter);

    }

    protected void initView(@Nullable Bundle savedInstanceState) {
        activityBaseBinding = DataBindingUtil.setContentView(this, R.layout.activity_base_com);
        baseVM = new BaseVM();
        activityBaseBinding.setBaseVM(baseVM);
        content = activityBaseBinding.content;
        int layoutResId = getLayoutResId();
        if (layoutResId > 0) {
            cvb = DataBindingUtil.inflate(LayoutInflater.from(this), layoutResId, content, true);
            content.removeAllViews();
            content.addView(cvb.getRoot());
            getBundle();
            viewModel = createViewModel();
            cvb.setVariable(BR.baseVM, viewModel);
            afterOnCreate(savedInstanceState);
        } else {
            throw new IllegalArgumentException("layout is not a inflate");
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        delegate.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onDestroy() {
        delegate.onDestroy();
        super.onDestroy();
        if (viewModel != null) {
            viewModel.detach();    // 释放viewModel的资源
        }
        unregisterReceiver(netStateReceiver);
        EventBus.getDefault().unregister(this);
        // MainApp.getRefWatcher().watch(this);

    }

    @Override
    public final void onBackPressed() {
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        delegate.onNewIntent(intent);
    }

    @Override
    protected void onStart() {
        super.onStart();
        delegate.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        delegate.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        delegate.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        delegate.onStop();
    }

    protected void setStatusBar() {
        StatusBarCompat.setStatusBarColor(this, getResources().getColor(R.color.color_white));

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        delegate.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        delegate.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
    }

    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        L.d(tag, "onPermissionsGranted == " + requestCode + ", granted permissions == " + perms);
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        L.d(tag, "onPermissionsDenied == " + requestCode + ", denied permissions == " + perms);
        if (PermissionChecker.somePermissionPermanentlyDenied(this, perms)) {
            L.d(tag, "somePermissionPermanentlyDenied == " + requestCode + ", denied permissions == " + perms);
            // 永久拒绝权限引导设置页
            // TODO: 2018/1/10 具体需要打开啥权限提示下
            StringBuilder builder = new StringBuilder();
            String permissionName;
            if (perms.size() > 0) {
                for (String perm : perms) {
                    permissionName = PermissionChecker.getPermissionName(perm);
                    if (StringUtils.isNotEmpty(permissionName)) {
                        builder.append(permissionName);
                        builder.append("，");
                    }
                }
            }
            String tip = builder.toString();
            tip = tip.substring(0, tip.length() - 1);
            ConfirmDialog.newInstance(getString(R.string.permission_permanently_denied_setting, tip))
                    .confirm(new ReplyCommand(new Action() {
                        @Override
                        public void run() {
                            AppUtils.startAppSettings(ComBaseActivity.this, REQUEST_CODE_PERMISSION_SETTING);
                        }
                    }))
                    .cancel(new ReplyCommand(new Action() {
                        @Override
                        public void run() {

                        }
                    }))
                    .showAllowingStateLoss(getSupportFragmentManager(), "PermissionSettingDialog");
        }
    }

    // 点击键盘外空白区域关闭键盘
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (null != this.getCurrentFocus()) {
            InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ActivityDelegate getActivityDelegate() {
        return delegate;
    }

    /***************************************(Optional methods)******************************************************/

    /**
     * 5.0直接设置true
     */
    protected void setAuthRuntimePermissions() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        }
    }

    public void initBqsDFSDK() {
        //1、添加设备信息采集回调
        BqsDF.setOnBqsDFListener(this);
        /*BqsDF.setOnBqsDFContactsListener(new OnBqsDFContactsListener() {
            @Override
            public void onGatherResult(boolean gatherStatus) {
                L.e("通讯录采集状态 gatherStatus=" + gatherStatus);
            }

            @Override
            public void onSuccess(String tokenKey) {
                L.e("通讯录采集成功");
            }

            @Override
            public void onFailure(String resultCode, String resultDesc) {
                L.e("通讯录采集失败 resultCode=" + resultCode + " resultDesc=" + resultDesc);
                Global.isInitBqsDF = false;
            }
        });*/
        //BqsDF.setOnBqsDFCallRecordListener(...);

        //2、配置初始化参数
        BqsParams params = new BqsParams();
        params.setPartnerId("fanbei");//商户编号
        params.setTestingEnv(false);//false是生产环境 true是测试环境
        params.setGatherGps(true);
        params.setGatherContact(true);
        params.setGatherCallRecord(true);

        //3、执行初始化
        BqsDF.initialize(this, params);
        //采集通讯录,第一个参数：是否采集通讯录，第二个参数：是否采集通话记录
        BqsDF.commitContactsAndCallRecords(false, false);
        BqsDF.commitLocation();
        //BqsDF.commitLocation(longitude, latitude);
        //String tokenkey = BqsDF.getTokenKey();

        //注意：上传tokenkey时最好再停留几百毫秒的时间（给SDK预留上传设备信息的时间）
        new CountDownTimer(500, 500) {
            @Override
            public void onTick(long l) {
            }

            @Override
            public void onFinish() {
                submitTokenkey();
            }
        }.start();
    }

    /**
     * 提交tokenkey
     */
    private void submitTokenkey() {
        String tokenkey = BqsDF.getTokenKey();

        L.e("提交tokenkey:" + tokenkey);
        //AppConfig.getAppConfig().saveBqsDF(tokenkey);
        BaseConfig.setBqsDF(tokenkey);

    }

    //白骑士成功回掉
    @Override
    public void onSuccess(String s) {
        L.e("bqsDF", s);
    }

    //白骑士失败回掉
    @Override
    public void onFailure(String s, String s1) {
        L.e("bqsDF", s + "-" + s1);
    }

    //内存不足的时候会调用
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        L.d("onTrimMemory", "level->" + level);
        switch (level) {
            case TRIM_MEMORY_RUNNING_MODERATE:
                System.gc();
                break;
        }
    }

    @Override
    public String getScreenUrl() {
        return this.getClass().getCanonicalName();
    }

    @Override
    public JSONObject getTrackProperties() {
        return new JSONObject();
    }

    /**
     * 网络状态发生变化时的处理
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onNetworkChangeEvent(NetChangeEvent event) {
        if (event != null) {
            if (BaseConfig.getCurrentActivity() == this) {
                netStateChangedUI(event.getNetWorkState());
            }
        }
    }


    protected void netStateChangedUI(int state) {
    }

}
