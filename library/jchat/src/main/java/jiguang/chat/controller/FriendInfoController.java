package jiguang.chat.controller;

import android.content.Intent;
import android.view.View;

import cn.jpush.im.android.api.model.UserInfo;
import jiguang.chat.R;
import jiguang.chat.activity.FriendInfoActivity;
import jiguang.chat.activity.FriendSettingActivity;
import jiguang.chat.view.FriendInfoView;

/**
 * Created by ${chenyn} on 2017/3/24.
 */

public class FriendInfoController implements View.OnClickListener {
    private FriendInfoActivity mContext;
    private UserInfo friendInfo;

    public FriendInfoController(FriendInfoView friendInfoView, FriendInfoActivity context) {
        this.mContext = context;
    }

    @Override
    public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.btn_goToChat) {
			mContext.startChatActivity();

		} else if (i == R.id.iv_friendPhoto) {
			mContext.startBrowserAvatar();

		} else if (i == R.id.jmui_commit_btn) {
			Intent intent = new Intent(mContext, FriendSettingActivity.class);
			intent.putExtra("userName", friendInfo.getUserName());
			intent.putExtra("noteName", friendInfo.getNotename());
			mContext.startActivity(intent);

		} else if (i == R.id.return_btn) {
			mContext.finish();

		} else {
		}
    }

    public void setFriendInfo(UserInfo info) {
        friendInfo = info;
    }

}
