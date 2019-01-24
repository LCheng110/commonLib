package cn.citytag.base.widget.dialog;

import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.HorizontalScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.socialize.bean.SHARE_MEDIA;

import java.util.HashMap;
import java.util.Map;

import cn.citytag.base.R;
import cn.citytag.base.app.BaseDialogFragment;
import cn.citytag.base.command.ReplyCommand;
import cn.citytag.base.helpers.other_helper.UMShareHelper;

/**
 * Created by yangfeng01 on 2017/12/6.
 */

public class BottomShareDialog extends BaseDialogFragment implements View.OnClickListener {

    private static final String TAG = "BottomShareDialog";
    private TextView weixinFriend;
    private TextView weixinCircle;
    private TextView qq;
    private TextView sinaWeibo;
    private TextView maoapo;
    private TextView qzone;

    //	private TextView follow;
    private TextView report;
    private TextView delete;
    private TextView collection;
    private TextView shield;
    private TextView remark;
    private TextView tvCancel;
    private HorizontalScrollView hsv;
    private HorizontalScrollView hsvSecond;
    private View line;

    private boolean isShowFollow = true;
    private boolean isShowReport = true;
    private boolean isShowDelete = true;
    private boolean isShowCollection = true;
    private boolean isShowShield = true;
    private boolean isShowRemark = true;

    private boolean isShowPaoPao = true;
    private boolean isBlackStatus = false; //拉黑状态    false:  未拉黑   true: 拉黑

    /**
     * 是否展示分享
     */
    private boolean isShowShare = true;

    /**
     * 是否展示删除举报等工具按钮
     */
    private boolean isShowTool = true;

    /**
     * 是否已收藏
     */
    private boolean isCollected = false;

    private ReplyCommand<BottomShareEnum> replyCommand;

    public enum BottomShareEnum {

        WEIXIN_FRIEND,
        WEIXIN_CIRCLE,
        QQ,
        SINA,
        MAOPAO,
        QZONE,
        FOLLOW,
        DELETE,
        REPORT,
        SHIELD,
        REMARK,
        COLLECTION;

        BottomShareEnum() {

        }

    }

    public static BottomShareDialog newInstance(ReplyCommand<BottomShareEnum> replyCommand) {
        BottomShareDialog fragment = new BottomShareDialog();
        fragment.setBottomClickListener(replyCommand);
        return fragment;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_bottom_share;
    }

    @Override
    protected void beforeOnViewCreated() {
        Window window = getDialog().getWindow();
        window.getDecorView().setPadding(0, 0, 0, 0);
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        lp.gravity = Gravity.BOTTOM;
        lp.windowAnimations = R.style.BottomDialog;
        window.setAttributes(lp);
        window.setBackgroundDrawable(new ColorDrawable());
    }

    @Override
    protected void afterOnViewCreated() {
        weixinFriend = findViewById(R.id.tv_weixin_friend);
        weixinCircle = findViewById(R.id.tv_weixin_circle);
        qq = findViewById(R.id.tv_qq);
        sinaWeibo = findViewById(R.id.tv_sina_weibo);
        maoapo = findViewById(R.id.tv_maopao);
        qzone = findViewById(R.id.tv_qq_qzone);
        report = findViewById(R.id.tv_report);
        delete = findViewById(R.id.tv_delete);
        collection = findViewById(R.id.tv_collection);
        shield = findViewById(R.id.tv_shield);
        remark = findViewById(R.id.tv_note);
        tvCancel = findViewById(R.id.tv_cancel);
        hsv = findViewById(R.id.hsv_first);
        hsvSecond = findViewById(R.id.hsv_second);
        line = findViewById(R.id.view_line);
        report.setVisibility(isShowReport ? View.VISIBLE : View.GONE);
        delete.setVisibility(isShowDelete ? View.VISIBLE : View.GONE);
        collection.setVisibility(View.GONE);
        collection.setSelected(isCollected);
        if (isCollected) {
            collection.setText("已收藏");
        } else {
            collection.setText("收藏");
        }
        shield.setVisibility(isShowShield ? View.VISIBLE : View.GONE);
        if (isBlackStatus) {
            shield.setText("已拉黑");
        } else {
            shield.setText("拉黑");
        }
        remark.setVisibility(isShowRemark ? View.VISIBLE : View.GONE);
        hsv.setVisibility(isShowShare ? View.VISIBLE : View.GONE);
        hsvSecond.setVisibility(isShowTool ? View.VISIBLE : View.GONE);
//        maoapo.setVisibility(View.GONE);
        if (!isShowShare || !isShowTool) {
            line.setVisibility(View.GONE);
        }

        weixinFriend.setOnClickListener(this);
        weixinCircle.setOnClickListener(this);
        qq.setOnClickListener(this);
        sinaWeibo.setOnClickListener(this);
        qzone.setOnClickListener(this);
        maoapo.setOnClickListener(this);

        report.setOnClickListener(this);
        delete.setOnClickListener(this);
        collection.setOnClickListener(this);
        shield.setOnClickListener(this);
        remark.setOnClickListener(this);
        tvCancel.setOnClickListener(this);
    }

    public void setBottomClickListener(ReplyCommand<BottomShareEnum> replyCommand) {
        this.replyCommand = replyCommand;
    }

    @Override
    public void onClick(View v) {

        try {
            int i = v.getId();
            if (i == R.id.tv_weixin_friend) {
                if (UMShareHelper.newInstance(getActivity()).isInstall(SHARE_MEDIA.WEIXIN)) {
                    Map<String, String> map_wf = new HashMap<String, String>();
                    map_wf.put("share_type", "weixin_friend");
                    //  MobclickAgent.onEvent(AppConfig.getContext(), BuryingPointUtil.kEVENT_CLICK_SHARE, map_wf);
                    replyCommand.execute(BottomShareEnum.WEIXIN_FRIEND);
                } else {
                    Toast.makeText(getActivity(), "请先安装微信", Toast.LENGTH_SHORT).show();
                }


            } else if (i == R.id.tv_weixin_circle) {
                if (UMShareHelper.newInstance(getActivity()).isInstall(SHARE_MEDIA.WEIXIN)) {
                    Map<String, String> map_wc = new HashMap<String, String>();
                    map_wc.put("share_type", "weixin_circle");
                    //MobclickAgent.onEvent(AppConfig.getContext(), BuryingPointUtil.kEVENT_CLICK_SHARE, map_wc);
                    replyCommand.execute(BottomShareEnum.WEIXIN_CIRCLE);
                } else {
                    Toast.makeText(getActivity(), "请先安装微信", Toast.LENGTH_SHORT).show();
                }

            } else if (i == R.id.tv_qq) {
                if (UMShareHelper.newInstance(getActivity()).isInstall(SHARE_MEDIA.QQ)) {
                    Map<String, String> map_qq = new HashMap<String, String>();
                    map_qq.put("share_type", "qq");
                    // MobclickAgent.onEvent(AppConfig.getContext(), BuryingPointUtil.kEVENT_CLICK_SHARE, map_qq);
                    replyCommand.execute(BottomShareEnum.QQ);
                } else {
                    Toast.makeText(getActivity(), "请先安装QQ", Toast.LENGTH_SHORT).show();
                }

            } else if (i == R.id.tv_qq_qzone) {
                if (UMShareHelper.newInstance(getActivity()).isInstall(SHARE_MEDIA.QQ)) {
                    Map<String, String> map_qzone = new HashMap<String, String>();
                    map_qzone.put("share_type", "qzone");
                    //MobclickAgent.onEvent(AppConfig.getContext(), BuryingPointUtil.kEVENT_CLICK_SHARE, map_qzone);
                    replyCommand.execute(BottomShareEnum.QZONE);
                } else {
                    Toast.makeText(getActivity(), "请先安装QQ", Toast.LENGTH_SHORT).show();
                }

            } else if (i == R.id.tv_sina_weibo) {
                if (UMShareHelper.newInstance(getActivity()).isInstall(SHARE_MEDIA.SINA)) {
                    Map<String, String> map_sina = new HashMap<String, String>();
                    map_sina.put("share_type", "sina");
                    // MobclickAgent.onEvent(AppConfig.getContext(), BuryingPointUtil.kEVENT_CLICK_SHARE, map_sina);
                    replyCommand.execute(BottomShareEnum.SINA);
                } else {
                    Toast.makeText(getActivity(), "请先安装微博客户端", Toast.LENGTH_SHORT).show();
                }

            } else if (i == R.id.tv_maopao) {
                replyCommand.execute(BottomShareEnum.MAOPAO);

            } else if (i == R.id.tv_report) {
                replyCommand.execute(BottomShareEnum.REPORT);

            } else if (i == R.id.tv_delete) {
                replyCommand.execute(BottomShareEnum.DELETE);

            } else if (i == R.id.tv_collection) {
                replyCommand.execute(BottomShareEnum.COLLECTION);

            } else if (i == R.id.tv_shield) {
                replyCommand.execute(BottomShareEnum.SHIELD);

            } else if (i == R.id.tv_note) {
                replyCommand.execute(BottomShareEnum.REMARK);

            } else if (i == R.id.tv_cancel) {
            } else {
            }
            dismiss();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * @param isShowCollection 收藏
     * @param isShowRemark     备注
     * @param isShowReport     举报
     * @param isShowShield     屏蔽
     * @param isShowDelete     删除
     */
    public void setBottomItemVisiable(boolean isShowCollection, boolean isShowRemark, boolean isShowReport, boolean
            isShowShield, boolean isShowDelete) {
        this.isShowCollection = isShowCollection;
        this.isShowRemark = isShowRemark;
        this.isShowReport = isShowReport;
        this.isShowShield = isShowShield;
        this.isShowDelete = isShowDelete;
    }

    /**
     * 是否已收藏
     */
    public void setIsCollected(boolean isCollected) {
        this.isCollected = isCollected;
    }

    /**
     * 设置是否展示分享模块
     *
     * @param isShowShare
     */
    public void setShareVisiable(boolean isShowShare) {
        this.isShowShare = isShowShare;
    }

    /**
     * 设置是否展示工具栏模块
     *
     * @param isShowTool
     */
    public void setToolVisible(boolean isShowTool) {
        this.isShowTool = isShowTool;
    }

    /**
     * 是否展示泡泡内部分享
     *
     * @param isShow
     */
    public void setIsVisiblePaoPao(boolean isShow) {
        this.isShowPaoPao = isShow;
    }

    /**
     * 是否已经拉黑
     */
    public boolean isBlackStatus() {
        return isBlackStatus;
    }

    public void setBlackStatus(boolean blackStatus) {
        isBlackStatus = blackStatus;
    }
}
