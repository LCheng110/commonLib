package jiguang.chat.view;


import android.graphics.drawable.ColorDrawable;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;

import cn.citytag.base.helpers.aroute.ARouteHandleType;
import cn.citytag.base.helpers.aroute.IntentRoute;
import jiguang.chat.R;

/**
 * Created by zhaoyuanchao on 2018/8/15.
 */

public class ReportBottomDialog extends IMBaseDialog  {
    private RelativeLayout mReportRelative;
    private String userId;

    @Override
    protected int getLayoutId() {
        return R.layout.im_dialog_conversation_bottom;
    }

    @Override
    protected void afterOnViewCreated() {
        mReportRelative = findViewById(R.id.rl_report);
        mReportRelative.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                IntentRoute.getIntentRoute().withType(ARouteHandleType.TYPE_REPORT).withExtra(userId).navigation();
//                Intent intent = new Intent();
//                Bundle bundle = new Bundle();
//                intent.putExtra("reportId", targetId);
//                intent.putExtra("type", type);
//                Navigation.startReport(intent);
                Log.e("toast", "onClick: ");
                dismiss();
            }
        });
    }

    @Override
    protected void beforeOnViewCreated() {
        super.beforeOnViewCreated();
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
    public void show(FragmentManager manager, String tag) {
        super.show(manager, tag);
        userId = tag;
    }
}
