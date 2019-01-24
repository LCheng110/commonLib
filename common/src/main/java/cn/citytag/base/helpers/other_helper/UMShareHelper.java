package cn.citytag.base.helpers.other_helper;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.widget.Toast;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.ShareContent;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;
import com.umeng.socialize.shareboard.SnsPlatform;
import com.umeng.socialize.utils.ShareBoardlistener;

import java.util.Map;

import cn.citytag.base.R;
import cn.citytag.base.model.ShareModel;
import cn.citytag.base.utils.L;
import cn.citytag.base.utils.UIUtils;

/**
 * Created by yangfeng01 on 2017/11/30.
 * <p>
 * 友盟分享及授权第三方登录
 */

public class UMShareHelper {
    /*type
     * 0带有url分享（默认是0）
     * 1文本
     * 2大图
     *
     *
     * */

    public static final String TAG = "UMShareHelper";

    public Activity activity;

    private UMShareAPI umShareAPI;

    public final int SHARE_URL = 0;

    public final int SHARE_TITLE = 1;

    public final int SHARE_ONLY_IMAGE = 2;

    private OnSharedSuccessListener sharedSuccessListener;

    private OnShareErrorListener shareErrorListener;

    private OnShareCancelListener shareCancelListener;


    private UMShareListener umShareListener = new UMShareListener() {
        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {
            if (sharedSuccessListener != null) {
                sharedSuccessListener.onSharedSuccess();
            } else {
                Toast.makeText(activity, "分享成功", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {
            if (shareErrorListener != null) {
                shareErrorListener.onShareError();
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {
            if(shareCancelListener != null){
                shareCancelListener.onShareCancel();
            }
        }
    };

    private UMShareHelper(Activity activity) {
        this.activity = activity;
        umShareAPI = UMShareAPI.get(activity);
        UMShareConfig config = new UMShareConfig();
        config.isNeedAuthOnGetUserInfo(true);                           //
        config.setSinaAuthType(UMShareConfig.AUTH_TYPE_SSO);            //每次都需要授权
        umShareAPI.setShareConfig(config);

    }

    public static UMShareHelper newInstance(Activity activity) {
        return new UMShareHelper(activity);
    }

    /**
     * 友盟默认的带面板分享
     *
     * @param text            分享的文字
     * @param umShareListener 分享回调
     */
    public void shareByBoard(String text, final UMShareListener umShareListener) {
        new ShareAction(activity)
                .withText(text)
                .setDisplayList(SHARE_MEDIA.SINA, SHARE_MEDIA.QQ, SHARE_MEDIA.WEIXIN)
                // 自定义按钮
                .addButton("umeng_sharebutton_custom", "umeng_sharebutton_custom", "info_icon_1", "info_icon_1")
                //面板点击监听器
                .setShareboardclickCallback(new ShareBoardlistener() {    //面板点击监听器
                    @Override
                    public void onclick(SnsPlatform snsPlatform, SHARE_MEDIA share_media) {
                        if (share_media == null) {
                            //根据key来区分自定义按钮的类型，并进行对应的操作
                            if (snsPlatform.mKeyword.equals("umeng_sharebutton_custom")) {
                                Toast.makeText(activity, "add button                                       " +
                                        "success", Toast.LENGTH_LONG).show();
                            }

                        } else {//社交平台的分享行为
                            new ShareAction(activity)
                                    .setPlatform(share_media)
                                    .setCallback(umShareListener)
                                    .withText("多平台分享")
                                    .share();
                        }
                    }
                })
                .open();
    }

    /**
     * 自定义界面分享，仅调用分享功能
     *
     * @param text            分享文字
     * @param umShareListener 分享回调
     */
    public void shareCustom(String text, UMShareListener umShareListener) {

    }

    /**
     * 友盟分享实现类
     */
    public static class UMShareListenerAdapter implements UMShareListener {

        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onResult(SHARE_MEDIA share_media) {

        }

        @Override
        public void onError(SHARE_MEDIA share_media, Throwable throwable) {

        }

        @Override
        public void onCancel(SHARE_MEDIA share_media) {

        }
    }

    /**
     * 在使用分享或者授权的Activity中，重写onDestory()方法，调用这个方法释放内存
     */
    public static void releaseShare(Activity activity) {
        UMShareAPI.get(activity).release();
    }

    /**
     * 判断app是否安装客户端
     *
     * @param share_media
     * @return
     */
    public boolean isInstall(SHARE_MEDIA share_media) {
        return umShareAPI.isInstall(activity, share_media);
    }

    /**
     * 第三方登录
     *
     * @param share_media   第三方平台
     * @param umAuthAdapter UMAuthListener的实现类，方便onComplete成功时回调
     */
    public void loginOauth(SHARE_MEDIA share_media, UMAuthListenerAdapter umAuthAdapter) {
        L.i(TAG, "loginOauth, share_media == " + share_media);
        if (!umShareAPI.isInstall(activity, share_media)) {
            if (share_media == SHARE_MEDIA.WEIXIN || share_media == SHARE_MEDIA.WEIXIN_CIRCLE) {
                UIUtils.toastMessage(R.string.please_install_weixin_first);
            } else if (share_media == SHARE_MEDIA.QQ || share_media == SHARE_MEDIA.QZONE) {
                UIUtils.toastMessage(R.string.please_install_qq_first);
            } else if (share_media == SHARE_MEDIA.SINA) {
                UIUtils.toastMessage(R.string.please_install_weibo_first);
            }
        }
        umShareAPI.getPlatformInfo(activity, share_media, new UMAuthListenerWrapper(umAuthAdapter));
    }

    /**
     * 1.QQ与新浪不需要添加Activity，但需要在使用QQ分享或者授权的Activity中
     * 2.QQ与新浪不需要添加Activity，但需要在使用QQ分享或者授权的Activity中，添加：
     * 注意onActivityResult不可在fragment中实现，如果在fragment中调用登录或分享，需要在fragment依赖的Activity中实现
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        UMShareAPI.get(activity).onActivityResult(requestCode, resultCode, data);
    }

    /**
     * 包装接口，在回调前后处理一些业务逻辑
     */
    public class UMAuthListenerWrapper implements UMAuthListener {

        private UMAuthListenerAdapter umAuthListenerAdapter;

        public UMAuthListenerWrapper(UMAuthListenerAdapter umAuthListenerAdapter) {
            this.umAuthListenerAdapter = umAuthListenerAdapter;
        }

        public void onStart(SHARE_MEDIA share_media) {
            L.i(TAG, "UMAuthListener onStart, share_media == " + share_media);
            umAuthListenerAdapter.onStart(share_media);
        }

        /**
         * 登录成功后，第三方平台会将用户资料传回， 全部会在Map data中返回
         * ，由于各个平台对于用户资料的标识不同，因此为了便于开发者使用，我们将一些常用的字段做了统一封装，
         * 开发者可以直接获取，不再需要对不同平台的不同字段名做转换，这里列出我们封装的字段及含义
         * <p>
         * UShare封装后字段名 QQ原始字段名 	  微信原始字段名 	    新浪原始字段名 	   字段含义 	         备注
         * uid 				  openid 	        unionid 	        id 	         用户唯一标识
         * 如果需要做跨APP用户打通，QQ需要使用unionID实现
         * name 	        screen_name 	  screen_name 	    screen_name 	   用户昵称
         * gender 	          gender 	        gender 	           gender 	       用户性别 	    该字段会直接返回男女
         * iconurl 	      profile_image_url   profile_image_url	profile_image_url  用户头像
         *
         * @param share_media
         * @param i
         * @param map
         */
        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
            L.i(TAG, "UMAuthListener onComplete, share_media == " + share_media);
            L.i(TAG, "map data == " + map);
            //map.get("uid");
            umAuthListenerAdapter.onComplete(share_media, i, map);
        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
//            UIUtils.toastMessage(R.string.auth_error_try_again);
            umAuthListenerAdapter.onError(share_media, i, throwable);
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {
            String msg = null;
            if (share_media == SHARE_MEDIA.SINA) {
                msg = activity.getResources().getString(R.string.auth_cancel_weibo_login);
            } else if (share_media == SHARE_MEDIA.WEIXIN) {
                msg = activity.getResources().getString(R.string.auth_cancel_weixin_login);
            } else if (share_media == SHARE_MEDIA.QQ) {
                msg = activity.getResources().getString(R.string.auth_cancel_qq_login);
            }
            UIUtils.toastMessage(msg);
            umAuthListenerAdapter.onCancel(share_media, i);
        }
    }

    /**
     * 实现接口，方便只回调个别方法
     */
    public static class UMAuthListenerAdapter implements UMAuthListener {

        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {

        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {

        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {

        }
    }


    //简单测了下
    // 朋友圈 wxmoment    微信好友 wxmessage  新浪微博 weibo  qq空间 qqzone     qq好友 qqmessage
    public void performShare(int type, ShareModel shareModel) {
        switch (type) {
            case 0:
                doShare(SHARE_MEDIA.WEIXIN, shareModel);
                break;
            case 1:
                doShare(SHARE_MEDIA.QQ, shareModel);
                break;
            case 2:
                doShare(SHARE_MEDIA.SINA, shareModel);
                break;

        }

    }

    //图文分享 简单测了下

    public void doShare(SHARE_MEDIA platform, ShareModel shareModel) {
        if (platform == SHARE_MEDIA.WEIXIN || platform == SHARE_MEDIA.WEIXIN_CIRCLE) {
            if (!umShareAPI.isInstall(activity, SHARE_MEDIA.WEIXIN)) {
                UIUtils.toastMessage(R.string.please_install_weixin_first);
                return;
            }
        } else if (platform == SHARE_MEDIA.QQ) {
            if (!umShareAPI.isInstall(activity, SHARE_MEDIA.QQ)) {
                UIUtils.toastMessage(R.string.please_install_qq_first);
                return;
            }
        } else if (platform == SHARE_MEDIA.QZONE) {
            if (!umShareAPI.isInstall(activity, SHARE_MEDIA.QQ) && !umShareAPI.isInstall(activity, SHARE_MEDIA.QZONE)) {
                UIUtils.toastMessage(R.string.please_install_qq_qzone_first);
                return;
            }
        } else if (platform == SHARE_MEDIA.SINA) {
            if (!umShareAPI.isInstall(activity, SHARE_MEDIA.SINA)) {
                UIUtils.toastMessage(R.string.please_install_weibo_first);
                return;
            }
        }
        if (shareModel == null) return;
        ShareAction share = new ShareAction(activity);
        ShareContent mShareContent = new ShareContent();
        switch (shareModel.getType()) {
            case SHARE_URL:
                UMWeb shareType = null;
                shareType = new UMWeb(shareModel.getUrl());
                shareType.setDescription(shareModel.getDescription());
                shareType.setTitle(shareModel.getTitle());
                if (!TextUtils.isEmpty(shareModel.getImageUrl())) {
                    shareType.setThumb(new UMImage(activity, shareModel.getImageUrl()));
                } else {
                    if (shareModel.getBitmap() != null) {
                        shareType.setThumb(new UMImage(activity, shareModel.getBitmap()));
                    } else {
                        //R.mipmap.ic_launcher
                        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_share_logo);
                        shareType.setThumb(new UMImage(activity, bitmap));
                    }
                }
                mShareContent.mMedia = shareType;
                break;
            case SHARE_ONLY_IMAGE:
                UMImage shareImageType ;
                if (!TextUtils.isEmpty(shareModel.getDataUrl())) {
                    shareImageType = new UMImage(activity, shareModel.getDataUrl());
                    shareImageType.setThumb(new UMImage(activity, shareModel.getDataUrl()));
                } else {
                    if (shareModel.getBitmap() != null) {
                        shareImageType = new UMImage(activity, shareModel.getBitmap());
                        shareImageType.setThumb(new UMImage(activity, shareModel.getBitmap()));
                    } else {
                        //R.mipmap.ic_launcher
                        Bitmap bitmap = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_share_logo);
                        shareImageType = new UMImage(activity, bitmap);
                        shareImageType.setThumb(new UMImage(activity, bitmap));
                    }
                }
                mShareContent.mMedia = shareImageType;
                break;

        }
        share.setPlatform(platform);
        share.setCallback(umShareListener);
        share.setShareContent(mShareContent).share();
    }

    public void setSharedSuccessListener(OnSharedSuccessListener sharedSuccessListener) {
        this.sharedSuccessListener = sharedSuccessListener;
    }

    public static interface OnSharedSuccessListener {
        void onSharedSuccess();
    }

    public OnShareErrorListener getShareErrorListener() {
        return shareErrorListener;
    }

    public void setShareErrorListener(OnShareErrorListener shareErrorListener) {
        this.shareErrorListener = shareErrorListener;
    }

    public OnShareCancelListener getShareCancelListener() {
        return shareCancelListener;
    }

    public void setShareCancelListener(OnShareCancelListener shareCancelListener) {
        this.shareCancelListener = shareCancelListener;
    }

    //为了不改变原有接口只能加一个失败的接口，没有连用
    public static interface OnShareErrorListener {
        void onShareError();
    }
    //为了不改变原有接口只能加一个失败的接口，没有连用
    public static interface OnShareCancelListener {
        void onShareCancel();
    }

}
