package cn.citytag.base.vm;

import android.Manifest;
import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;

import org.json.JSONObject;

import java.util.concurrent.TimeUnit;

import cn.citytag.base.KickBackAnimator;
import cn.citytag.base.R;
import cn.citytag.base.callback.IPublishListener;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.constants.ExtraName;
import cn.citytag.base.databinding.CommonActivityPublishMenuBinding;
import cn.citytag.base.helpers.aroute.ARouteHandleType;
import cn.citytag.base.helpers.aroute.IntentRoute;
import cn.citytag.base.helpers.other_helper.ShortVideoStatusHelper;
import cn.citytag.base.helpers.permission.PermissionChecker;
import cn.citytag.base.utils.GuestJudgeUtils;
import cn.citytag.base.utils.SensorsDataUtils;
import cn.citytag.base.utils.UIUtils;
import cn.citytag.base.view.base.ComBaseActivity;
import cn.citytag.base.widget.dialog.OrderCancelDialog;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 作者：lnx. on 2018/12/17 14:53
 */
public class PublishMenuVM extends LceVM implements View.OnClickListener {

    private static final int SHORT_VIDEO_REQUEST_CODE = 502;

    private CommonActivityPublishMenuBinding binding;
    private ComBaseActivity activity;
    private IPublishListener listener;
    private OrderCancelDialog dialog;
    private String source;


    public PublishMenuVM(CommonActivityPublishMenuBinding binding, ComBaseActivity activity) {
        this.binding = binding;
        this.activity = activity;
        startBackRotation(true);
        showAnimation(binding.consContent);
        source = activity.getIntent().getStringExtra(ExtraName.EXTRA_SENSOR_SOURCE);
    }

    //点击返回按钮
    public void finish() {
        startBackRotation(false);
        closeAnimation(binding.consContent);
        binding.consContent.setBackgroundColor(Color.TRANSPARENT);
    }

    //设置回调接口
    public void setInterface(IPublishListener listener) {
        this.listener = listener;
    }

    //点击发布动态
    public void enterPublishDynamic() {

        if (GuestJudgeUtils.checkGuest(BaseConfig.getCurrentActivity())) {
            return;
        }
        IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_DYNAMIC_PUBLISH)
                .withExtra3("1")
                .navigation();

        if (listener != null) {
            listener.clickPublish();
        }


        //   IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_PUBLISH).navigation();

        finish();

    }

    //点击短视频
    public void enterSmallVideo() {

        if (GuestJudgeUtils.checkGuest(BaseConfig.getCurrentActivity())) {
            return;
        }
        if (checkPermission()) {

            switch (getCacheOrFailVideo()) {

                case ShortVideoStatusHelper.RECORD_STATE:
                case ShortVideoStatusHelper.CROP_STATE:
                case ShortVideoStatusHelper.PUBLISH_ALL_SUCCESS:
                    sensorClickPostShortVideos();
                    IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_MAIN).navigation();
                    finish();
                    break;
                case ShortVideoStatusHelper.EDITOR_STATE:
                    //去编辑
                    isFromEdit();
                    break;
                case ShortVideoStatusHelper.PUBLISH_DEFAULT_STATE:
                    hasNoPulishVideo();
                    break;
                case ShortVideoStatusHelper.PUBLISH_ALI_SUCCESS:
                case ShortVideoStatusHelper.PUBLISH_STATE_BTN_PRESS:
                    hasFailVideo();
                    break;
                default:
                    IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_MAIN).navigation();
                    finish();
                    break;
            }
        } else {
            PermissionChecker.requestPermissions(activity, SHORT_VIDEO_REQUEST_CODE, Manifest.permission.CAMERA,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }

    }


    //判断权限
    private boolean checkPermission() {

        boolean b = PermissionChecker.hasPermissions(activity, Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.READ_EXTERNAL_STORAGE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return b;
    }

    //获取是否有未发布 或 失败的视频
    private int getCacheOrFailVideo() {

        return ShortVideoStatusHelper.getParam();
    }


    //是否有上传失败的视频
    private boolean hasFailVideo() {

        if (dialog == null) {
            dialog = OrderCancelDialog.newInstance();
        }

        dialog.setStrContent("您有发布失败的短视频未处理,\n是否重新发布？");
        dialog.setStrCancel("放弃");
        dialog.setStrComfirm("去发布");
        dialog.setOnDialogClick(new OrderCancelDialog.OnDialogClick() {
            @Override
            public void onClick(OrderCancelDialog orderCancelDialog, int position) {
                switch (position) {
                    case 0:
                         ShortVideoStatusHelper.clearPublishModel();
                         ShortVideoStatusHelper.saveParam(ShortVideoStatusHelper.RECORD_STATE);
                         dialog.dismiss();
                        break;
                    case 1:
                        IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_PUBLISH).navigation();
                        ShortVideoStatusHelper.setIsCrash(true);
                        finish();
                        break;
                }
            }
        });
        dialog.show(activity.getSupportFragmentManager(), "publish");

        return true;
    }

    //判断是否有未发布的视频
    private boolean hasNoPulishVideo() {

        if (dialog == null) {
            dialog = OrderCancelDialog.newInstance();
        }
        dialog.setStrContent("上次视频发布未完成,\n是否继续？");
        dialog.setStrComfirm("继续");
        dialog.setStrCancel("重拍");

        dialog.setOnDialogClick(new OrderCancelDialog.OnDialogClick() {
            @Override
            public void onClick(OrderCancelDialog orderCancelDialog, int position) {
                switch (position) {
                    case 0:
                        dialog.dismiss();
                        ShortVideoStatusHelper.clearPublishModel();
                        ShortVideoStatusHelper.saveParam(ShortVideoStatusHelper.RECORD_STATE);
                        IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_MAIN).navigation();
                        finish();
                        break;
                    case 1:
                        IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_PUBLISH).navigation();
                        ShortVideoStatusHelper.setIsCrash(true);
                        finish();
                        break;
                }
            }
        });
        dialog.show(activity.getSupportFragmentManager(), "publish");
        return true;
    }

    //从剪辑页进入 弹窗
    private boolean isFromEdit() {
        if (dialog == null) {
            dialog = OrderCancelDialog.newInstance();
        }
        dialog.setStrContent("上次视频编辑未完成,\n是否继续？");
        dialog.setStrCancel("重拍");
        dialog.setStrComfirm("继续");

        dialog.setOnDialogClick(new OrderCancelDialog.OnDialogClick() {
            @Override
            public void onClick(OrderCancelDialog orderCancelDialog, int position) {
                switch (position) {
                    case 0:
                        dialog.dismiss();
                        ShortVideoStatusHelper.clearPublishModel();
                        ShortVideoStatusHelper.saveParam(ShortVideoStatusHelper.RECORD_STATE);
                        IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_MAIN).navigation();
                        break;
                    case 1:
                        dialog.dismiss();
                        IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_TO_SHORT_VIDEO_EDITOR).navigation();
                        ShortVideoStatusHelper.setIsCrash(true);
                        finish();
                        break;
                }
            }
        });
        dialog.show(activity.getSupportFragmentManager(), "edit");

        return true;
    }



    //开启动画
    private void startAnim() {

    }

    //停止动画
    private void stopAnim() {

    }


    @Override
    public void detach() {
        super.detach();
        stopAnim();
    }


    /**
     * 显示进入动画效果
     *
     * @param layout
     */
    private void showAnimation(ViewGroup layout) {
        //遍历根试图下的一级子试图
        for (int i = 0; i < layout.getChildCount(); i++) {
            final View child = layout.getChildAt(i);


            //忽略关闭组件
            if (child.getId() == R.id.iv_btn_back) {

                continue;
            }
            int offset = 0;
            if (child.getId() == R.id.iv_btn_dynamic || child.getId() == R.id.tv_dynamic) {
                offset = UIUtils.dip2px(80);
            }
            if (child.getId() == R.id.iv_btn_smallvideo || child.getId() == R.id.tv_video) {
                offset = UIUtils.dip2px(160);
            }

            //设置所有一级子试图的点击事件
            // child.setOnClickListener(this);
            child.setVisibility(View.INVISIBLE);
            //延迟显示每个子试图(主要动画就体现在这里)

            int finalOffset = offset;
            Observable.timer(i * 50, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            child.setVisibility(View.VISIBLE);

                            ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationY", finalOffset, 0);
                            fadeAnim.setDuration(300);
                            KickBackAnimator kickAnimator = new KickBackAnimator();
                            kickAnimator.setDuration(150);
                            fadeAnim.setEvaluator(kickAnimator);
                            fadeAnim.start();
                        }
                    });
        }
    }

    /**
     * 关闭动画效果
     *
     * @param layout
     */
    private void closeAnimation(ViewGroup layout) {

        for (int i = 0; i < layout.getChildCount(); i++) {
            final View child = layout.getChildAt(i);
            if (child.getId() == R.id.iv_btn_back) {

                continue;
            }
            int offset = 0;
            if (child.getId() == R.id.iv_btn_dynamic || child.getId() == R.id.tv_dynamic) {
                offset = UIUtils.dip2px(80);
            }
            if (child.getId() == R.id.iv_btn_smallvideo || child.getId() == R.id.tv_video) {
                offset = UIUtils.dip2px(160);
            }
            child.setVisibility(View.INVISIBLE);
            int finalOffset = offset;
            Observable.timer((layout.getChildCount() - i - 1) * 30, TimeUnit.MILLISECONDS)
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Long>() {
                        @Override
                        public void accept(Long aLong) throws Exception {
                            child.setVisibility(View.INVISIBLE);
                            ValueAnimator fadeAnim = ObjectAnimator.ofFloat(child, "translationY", 0, finalOffset);
                            fadeAnim.setDuration(200);
                            KickBackAnimator kickAnimator = new KickBackAnimator();
                            kickAnimator.setDuration(100);
                            fadeAnim.setEvaluator(kickAnimator);
                            fadeAnim.start();
                        }
                    });

//            if (child.getId() == R.id.video_window) {
//                Observable.timer((layout.getChildCount() - i) * 30 + 80, TimeUnit.MILLISECONDS)
//                        .subscribeOn(Schedulers.newThread())
//                        .observeOn(AndroidSchedulers.mainThread())
//                        .subscribe(new Action1<Long>() {
//                            @Override
//                            public void call(Long aLong) {
//                                dismiss();
//                            }
//                        });
//            }

        }
    }


    private void startBackRotation(boolean isStart) {
        float startDegrees = isStart ? 0f : 45f;
        float toDegrees = isStart ? 45f : 0f;
        ObjectAnimator rotation = ObjectAnimator.ofFloat(binding.ivBtnBack, "rotation", startDegrees, toDegrees).setDuration(300);
        rotation.start();
        rotation.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (isStart) return;
                activity.finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
//         RotateAnimation animation =new RotateAnimation(0f,toDegrees, Animation.RELATIVE_TO_SELF,
//                0.5f,Animation.RELATIVE_TO_SELF,0.5f);
//         animation.setDuration(500);
        //binding.ivBtnBack.startAnimation(animation);


    }


    public void onBackPress() {

        finish();

    }


    @Override
    public void onClick(View v) {

    }

    //神策埋点 - 点击发布+--短视频
    private void sensorClickPostShortVideos() {
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("source", source);
            SensorsDataUtils.track("clickPostShortVideos", jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
