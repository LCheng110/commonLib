package jiguang.chat.controller;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.view.WindowManager;

import jiguang.chat.R;
import jiguang.chat.activity.AboutJChatActivity;
import jiguang.chat.activity.FeedbackActivity;
import jiguang.chat.activity.PersonalActivity;
import jiguang.chat.activity.ResetPasswordActivity;
import jiguang.chat.activity.fragment.MeFragment;
import jiguang.chat.utils.DialogCreator;

/**
 * Created by ${chenyn} on 2017/2/21.
 */

public class MeController implements View.OnClickListener {
    public static final String PERSONAL_PHOTO = "personal_photo";
    private MeFragment mContext;
    private Dialog mDialog;
    private int mWidth;
    private Bitmap mBitmap;

    public MeController(MeFragment context, int width) {
        this.mContext = context;
        this.mWidth = width;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    @Override
    public void onClick(View v) {
		int i = v.getId();
		if (i == R.id.setPassword) {
			mContext.startActivity(new Intent(mContext.getContext(), ResetPasswordActivity.class));

		} else if (i == R.id.opinion) {
			mContext.startActivity(new Intent(mContext.getContext(), FeedbackActivity.class));

		} else if (i == R.id.about) {
			mContext.startActivity(new Intent(mContext.getContext(), AboutJChatActivity.class));

		} else if (i == R.id.exit) {
			View.OnClickListener listener = new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					int i1 = v.getId();
					if (i1 == R.id.jmui_cancel_btn) {
						mDialog.cancel();

					} else if (i1 == R.id.jmui_commit_btn) {
						mContext.Logout();
						mContext.cancelNotification();
						mContext.getActivity().finish();
						mDialog.cancel();

					}
				}
			};
			mDialog = DialogCreator.createLogoutDialog(mContext.getActivity(), listener);
			mDialog.getWindow().setLayout((int) (0.8 * mWidth), WindowManager.LayoutParams.WRAP_CONTENT);
			mDialog.show();

		} else if (i == R.id.rl_personal) {
			Intent intent = new Intent(mContext.getContext(), PersonalActivity.class);
			intent.putExtra(PERSONAL_PHOTO, mBitmap);
			mContext.startActivity(intent);

		}
    }
}
