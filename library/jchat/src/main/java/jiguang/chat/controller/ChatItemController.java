package jiguang.chat.controller;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.AnimationDrawable;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSONObject;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import cn.citytag.base.app.observer.BaseObserver;
import cn.citytag.base.config.BaseConfig;
import cn.citytag.base.helpers.aroute.ARouteHandleType;
import cn.citytag.base.helpers.aroute.IntentRoute;
import cn.citytag.base.image.ImageLoader;
import cn.citytag.base.network.HttpClient;
import cn.citytag.base.utils.StringUtils;
import cn.citytag.base.utils.UIUtils;
import cn.jpush.im.android.api.JMessageClient;
import cn.jpush.im.android.api.callback.DownloadCompletionCallback;
import cn.jpush.im.android.api.callback.GetUserInfoCallback;
import cn.jpush.im.android.api.callback.ProgressUpdateCallback;
import cn.jpush.im.android.api.content.CustomContent;
import cn.jpush.im.android.api.content.EventNotificationContent;
import cn.jpush.im.android.api.content.FileContent;
import cn.jpush.im.android.api.content.ImageContent;
import cn.jpush.im.android.api.content.LocationContent;
import cn.jpush.im.android.api.content.PromptContent;
import cn.jpush.im.android.api.content.TextContent;
import cn.jpush.im.android.api.content.VoiceContent;
import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.enums.ConversationType;
import cn.jpush.im.android.api.enums.MessageDirect;
import cn.jpush.im.android.api.event.DeleteCustomMessageEvent;
import cn.jpush.im.android.api.event.IMOderMessageEvent;
import cn.jpush.im.android.api.model.Conversation;
import cn.jpush.im.android.api.model.GroupInfo;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.api.model.UserInfo;
import cn.jpush.im.android.api.options.MessageSendingOptions;
import cn.jpush.im.android.eventbus.EventBus;
import cn.jpush.im.api.BasicCallback;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.schedulers.Schedulers;
import jiguang.chat.R;
import jiguang.chat.activity.BrowserViewPagerActivity;
import jiguang.chat.activity.ChatActivity;
import jiguang.chat.activity.DownLoadActivity;
import jiguang.chat.activity.FriendInfoActivity;
import jiguang.chat.activity.GroupNotFriendActivity;
import jiguang.chat.adapter.ChattingListAdapter;
import jiguang.chat.adapter.ChattingListAdapter.ViewHolder;
import jiguang.chat.api.IMApi;
import jiguang.chat.application.JGApplication;
import jiguang.chat.model.OrderPayInfoModel;
import jiguang.chat.pickerimage.utils.BitmapDecoder;
import jiguang.chat.utils.CommonUtils;
import jiguang.chat.utils.FileHelper;
import jiguang.chat.utils.FileUtils;
import jiguang.chat.utils.HandleResponseCode;
import jiguang.chat.utils.SimpleCommonUtils;
import jiguang.chat.utils.ToastUtil;

import static cn.citytag.base.helpers.aroute.ARouteHandleType.IM_TYPE_ORDER_PAY;
import static jiguang.chat.contants.IMContants.COURSE_CONTENT;
import static jiguang.chat.contants.IMContants.COURSE_COURSE_ID;
import static jiguang.chat.contants.IMContants.COURSE_ICON;
import static jiguang.chat.contants.IMContants.COURSE_IS_PRICE_CHANGE;
import static jiguang.chat.contants.IMContants.COURSE_NOW_PRICE;
import static jiguang.chat.contants.IMContants.COURSE_OLD_PRICE;
import static jiguang.chat.contants.IMContants.COURSE_TYPES_ID;
import static jiguang.chat.contants.IMContants.COURSE_USERID;
import static jiguang.chat.contants.IMContants.CUSTOM_MSG_TYPE;
import static jiguang.chat.contants.IMContants.GROUP_PROMPT_TEXT;
import static jiguang.chat.contants.IMContants.GROUP_PROMPT_TEXT_BIG;
import static jiguang.chat.contants.IMContants.IM_MULTIMEDIA_URL;
import static jiguang.chat.contants.IMContants.ORDER_APPOINT_TIME;
import static jiguang.chat.contants.IMContants.ORDER_CONTENT;
import static jiguang.chat.contants.IMContants.ORDER_IS_REMIND_BUYER;
import static jiguang.chat.contants.IMContants.ORDER_ORDER_ID;
import static jiguang.chat.contants.IMContants.ORDER_SERVICE_CONTENT;
import static jiguang.chat.contants.IMContants.ORDER_SERVICE_REMARK;
import static jiguang.chat.contants.IMContants.ORDER_TITLE;
import static jiguang.chat.contants.IMContants.ORDER_USERID;
import static jiguang.chat.contants.IMContants.RTC_BUBBLE_VIDEO_CHAT_DURATION;
import static jiguang.chat.contants.IMContants.RTC_CMD_TYPE;
import static jiguang.chat.contants.IMContants.RTC_VIDEO_CHAT_SELECT_TYPE;


public class ChatItemController {

    private ChattingListAdapter mAdapter;
    private Activity mContext;
    private Conversation mConv;
    private List<Message> mMsgList;
    private ChattingListAdapter.ContentLongClickListener mLongClickListener;
    private float mDensity;
    public Animation mSendingAnim;
    private boolean mSetData = false;
    private final MediaPlayer mp = new MediaPlayer();
    private AnimationDrawable mVoiceAnimation;
    private int mPosition = -1;// 和mSetData一起组成判断播放哪条录音的依据
    private List<Integer> mIndexList = new ArrayList<Integer>();//语音索引
    private FileInputStream mFIS;
    private FileDescriptor mFD;
    private boolean autoPlay = false;
    private int nextPlayPosition = 0;
    private boolean mIsEarPhoneOn;
    private int mSendMsgId;
    private Queue<Message> mMsgQueue = new LinkedList<Message>();
    private UserInfo mUserInfo;
    private Map<Integer, UserInfo> mUserInfoMap = new HashMap<>();
    //存放所有图片的路径
    private List<String> mPathList = new ArrayList<String>();

    public ChatItemController(ChattingListAdapter adapter, Activity context, Conversation conv, List<Message> msgList,
                              float density, ChattingListAdapter.ContentLongClickListener longClickListener) {
        this.mAdapter = adapter;
        this.mContext = context;
        this.mConv = conv;
        if (mConv.getType() == ConversationType.single) {
            mUserInfo = (UserInfo) mConv.getTargetInfo();
        }
        this.mMsgList = msgList;
        this.mLongClickListener = longClickListener;
        this.mDensity = density;
        mSendingAnim = AnimationUtils.loadAnimation(mContext, R.anim.jmui_rotate);
        LinearInterpolator lin = new LinearInterpolator();
        mSendingAnim.setInterpolator(lin);

        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        audioManager.setMode(AudioManager.MODE_NORMAL);
        if (audioManager.isSpeakerphoneOn()) {
            audioManager.setSpeakerphoneOn(true);
        } else {
            audioManager.setSpeakerphoneOn(false);
        }
        mp.setAudioStreamType(AudioManager.STREAM_RING);
        mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {

            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return false;
            }
        });
    }

    public void handleBusinessCard(final Message msg, final ViewHolder holder, int position) {
        final TextContent[] textContent = {(TextContent) msg.getContent()};
        final String[] mUserName = {textContent[0].getStringExtra("userName")};
        final String mAppKey = textContent[0].getStringExtra("appKey");
        holder.ll_businessCard.setTag(position);
        int key = (mUserName[0] + mAppKey).hashCode();
        UserInfo userInfo = mUserInfoMap.get(key);
        if (userInfo != null) {
            String name = userInfo.getNickname();
            //如果没有昵称,名片上面的位置显示用户名
            //如果有昵称,上面显示昵称,下面显示用户名
            if (TextUtils.isEmpty(name)) {
                holder.tv_userName.setText("");
                holder.tv_nickUser.setText(mUserName[0]);
            } else {
                holder.tv_nickUser.setText(name);
                holder.tv_userName.setText("用户名: " + mUserName[0]);
            }
            if (userInfo.getAvatarFile() != null) {
                holder.business_head.setImageBitmap(BitmapFactory.decodeFile(userInfo.getAvatarFile().getAbsolutePath
                        ()));
            } else {
                holder.business_head.setImageResource(R.drawable.jmui_head_icon);
            }
        } else {
            JMessageClient.getUserInfo(mUserName[0], mAppKey, new GetUserInfoCallback() {
                @Override
                public void gotResult(int i, String s, UserInfo userInfo) {
                    if (i == 0) {
                        mUserInfoMap.put((mUserName[0] + mAppKey).hashCode(), userInfo);
                        String name = userInfo.getNickname();
                        //如果没有昵称,名片上面的位置显示用户名
                        //如果有昵称,上面显示昵称,下面显示用户名
                        if (TextUtils.isEmpty(name)) {
                            holder.tv_userName.setText("");
                            holder.tv_nickUser.setText(mUserName[0]);
                        } else {
                            holder.tv_nickUser.setText(name);
                            holder.tv_userName.setText("用户名: " + mUserName[0]);
                        }
                        if (userInfo.getAvatarFile() != null) {
                            holder.business_head.setImageBitmap(BitmapFactory.decodeFile(userInfo.getAvatarFile()
                                    .getAbsolutePath()));
                        } else {
                            holder.business_head.setImageResource(R.drawable.jmui_head_icon);
                        }
                    } else {
                        HandleResponseCode.onHandle(mContext, i, false,s);
                    }
                }
            });
        }

        holder.ll_businessCard.setOnLongClickListener(mLongClickListener);
        holder.ll_businessCard.setOnClickListener(new BusinessCard(mUserName[0], mAppKey, holder));
        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case created:
                    if (null != mUserInfo) {
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                        holder.text_receipt.setVisibility(View.GONE);
                    }
                    break;
                case send_success:
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
            }
        } else {
            if (mConv.getType() == ConversationType.group) {
                if (msg.isAtMe()) {
                    mConv.updateMessageExtra(msg, "isRead", true);
                }
                if (msg.isAtAll()) {
                    mConv.updateMessageExtra(msg, "isReadAtAll", true);
                }
                holder.displayName.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                    holder.displayName.setText(msg.getFromUser().getUserName());
                } else {
                    holder.displayName.setText(msg.getFromUser().getNickname());
                }
            }
        }
        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });
        }
    }

    private class BusinessCard implements View.OnClickListener {
        private String userName;
        private String appKey;
        private ViewHolder mHolder;

        public BusinessCard(String name, String appKey, ViewHolder holder) {
            this.userName = name;
            this.appKey = appKey;
            this.mHolder = holder;
        }

        @Override
        public void onClick(View v) {
            if (mHolder.ll_businessCard != null && v.getId() == mHolder.ll_businessCard.getId()) {
                JMessageClient.getUserInfo(userName, new GetUserInfoCallback() {
                    @Override
                    public void gotResult(int i, String s, UserInfo userInfo) {
                        Intent intent = new Intent();
                        if (i == 0) {
                            if (userInfo.isFriend()) {
                                intent.setClass(mContext, FriendInfoActivity.class);
                            } else {
                                intent.setClass(mContext, GroupNotFriendActivity.class);
                            }
                            intent.putExtra(JGApplication.TARGET_APP_KEY, appKey);
                            intent.putExtra(JGApplication.TARGET_ID, userName);
                            intent.putExtra("fromSearch", true);
                            mContext.startActivity(intent);
                        } else {
                            ToastUtil.shortToast(mContext, "获取信息失败,稍后重试");
                        }
                    }
                });
            }

        }
    }

    public void handleTextMsg(final Message msg, final ViewHolder holder, int position) {
        final String content = ((TextContent) msg.getContent()).getText();
        SimpleCommonUtils.spannableEmoticonFilter(holder.txtContent, content);
        //holder.txtContent.setText(content);
        holder.txtContent.setTag(position);
        holder.txtContent.setOnLongClickListener(mLongClickListener);
        // 检查发送状态，发送方有重发机制
        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case created:
                    if (null != mUserInfo) {
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                        holder.text_receipt.setVisibility(View.GONE);
                    }
                    break;
                case send_success:
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
                default:
            }

        } else {
            if (mConv.getType() == ConversationType.group) {
                if (msg.isAtMe()) {
                    mConv.updateMessageExtra(msg, "isRead", true);
                }
                if (msg.isAtAll()) {
                    mConv.updateMessageExtra(msg, "isReadAtAll", true);
                }
                holder.displayName.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                    holder.displayName.setText(msg.getFromUser().getUserName());
                } else {
                    holder.displayName.setText(msg.getFromUser().getNickname());
                }
            }
        }
        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });
        }
    }

    // 处理图片
    public void handleImgMsg(final Message msg, final ViewHolder holder, final int position) {
        final ImageContent imgContent = (ImageContent) msg.getContent();
        final String jiguang = imgContent.getStringExtra("jiguang");
        // 先拿本地缩略图
        final String path = imgContent.getLocalThumbnailPath();
        /**
         * 截取图片
         */
        String mediaID = imgContent.getMediaID();
        if (!StringUtils.isEmpty(mediaID)) {
            if (mediaID.contains("group1")) {
                String[] split = mediaID.split("group1");
                String imgUrl = IM_MULTIMEDIA_URL + split[1];
                ImageView imageView = setPictureScaleNew(jiguang, msg, path, holder.picture);
                RequestOptions requestOptions = new RequestOptions();
                requestOptions.placeholder(R.drawable.img_placeholder);
                Glide.with(mContext).load(imgUrl).apply(requestOptions).into(imageView);
            } else {
                if (path == null) {
                    //从服务器上拿缩略图
                    imgContent.downloadThumbnailImage(msg, new DownloadCompletionCallback() {
                        @Override
                        public void onComplete(int status, String desc, File file) {
                            if (status == 0) {
                                ImageView imageView = setPictureScale(jiguang, msg, file.getPath(), holder.picture);
                                Glide.with(mContext).load(file).into(imageView);
                            }
                        }
                    });
                } else {
                    ImageView imageView = setPictureScale(jiguang, msg, path, holder.picture);
                    Glide.with(mContext).load(new File(path)).into(imageView);
                }
            }

        } else {
            if (path == null) {
                //从服务器上拿缩略图
                imgContent.downloadThumbnailImage(msg, new DownloadCompletionCallback() {
                    @Override
                    public void onComplete(int status, String desc, File file) {
                        if (status == 0) {
                            ImageView imageView = setPictureScale(jiguang, msg, file.getPath(), holder.picture);
                            Glide.with(mContext).load(file).into(imageView);
                        }
                    }
                });
            } else {
                ImageView imageView = setPictureScale(jiguang, msg, path, holder.picture);
                Glide.with(mContext).load(new File(path)).into(imageView);
            }
        }


        // 接收图片
        if (msg.getDirect() == MessageDirect.receive) {
            //群聊中显示昵称
            if (mConv.getType() == ConversationType.group) {
                holder.displayName.setVisibility(View.VISIBLE);
                if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                    holder.displayName.setText(msg.getFromUser().getUserName());
                } else {
                    holder.displayName.setText(msg.getFromUser().getNickname());
                }
            }

            switch (msg.getStatus()) {
                case receive_fail:
                    holder.picture.setImageResource(R.drawable.img_placeholder);
                    holder.resend.setVisibility(View.VISIBLE);
                    holder.resend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            imgContent.downloadOriginImage(msg, new DownloadCompletionCallback() {
                                @Override
                                public void onComplete(int i, String s, File file) {
                                    if (i == 0) {
                                        ToastUtil.shortToast(mContext, "下载成功");
                                        holder.sendingIv.setVisibility(View.GONE);
                                        mAdapter.notifyDataSetChanged();
                                    } else {
                                        ToastUtil.shortToast(mContext, "下载失败" + s);
                                    }
                                }
                            });
                        }
                    });
                    break;
                default:
            }
            /**
             * 修改接受图片 无失败
             */
            holder.resend.setVisibility(View.GONE);
            // 发送图片方，直接加载缩略图
        } else {
            //            try {
            //                setPictureScale(path, holder.picture);
            //                Picasso.with(mContext).load(new File(path)).into(holder.picture);
            //            } catch (NullPointerException e) {
            //                Picasso.with(mContext).load(IdHelper.getDrawable(mContext, "jmui_picture_not_found"))
            //                        .into(holder.picture);
            //            }
            //检查状态
            switch (msg.getStatus()) {
                case created:
                    holder.picture.setEnabled(false);
                    holder.resend.setEnabled(false);
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.sendingIv.setVisibility(View.VISIBLE);
                    holder.resend.setVisibility(View.GONE);
                    holder.progressTv.setText("0%");
                    break;
                case send_success:
                    holder.picture.setEnabled(true);
                    holder.sendingIv.clearAnimation();
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.picture.setAlpha(1.0f);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.resend.setEnabled(true);
                    holder.picture.setEnabled(true);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.picture.setAlpha(1.0f);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    holder.picture.setEnabled(false);
                    holder.resend.setEnabled(false);
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    sendingImage(msg, holder);
                    break;
                default:
                    holder.picture.setAlpha(0.75f);
                    holder.sendingIv.setVisibility(View.VISIBLE);
                    holder.sendingIv.startAnimation(mSendingAnim);
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.progressTv.setText("0%");
                    holder.resend.setVisibility(View.GONE);
                    //从别的界面返回聊天界面，继续发送
                    if (!mMsgQueue.isEmpty()) {
                        Message message = mMsgQueue.element();
                        if (message.getId() == msg.getId()) {
                            MessageSendingOptions options = new MessageSendingOptions();
                            options.setNeedReadReceipt(true);
                            JMessageClient.sendMessage(message, options, true);
                            mSendMsgId = message.getId();
                            sendingImage(message, holder);
                        }
                    }
            }
        }
        if (holder.picture != null) {
            // 点击预览图片
            holder.picture.setOnClickListener(new BtnOrTxtListener(position, holder));
            holder.picture.setTag(position);
            holder.picture.setOnLongClickListener(mLongClickListener);

        }
        if (msg.getDirect().equals(MessageDirect.send) && holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });
        }
    }

    private void sendingImage(final Message msg, final ViewHolder holder) {
        holder.picture.setAlpha(0.75f);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(mSendingAnim);
        holder.progressTv.setVisibility(View.VISIBLE);
        holder.progressTv.setText("0%");
        holder.resend.setVisibility(View.GONE);
        //如果图片正在发送，重新注册上传进度Callback
        if (!msg.isContentUploadProgressCallbackExists()) {
            msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                @Override
                public void onProgressUpdate(double v) {
                    String progressStr = (int) (v * 100) + "%";
                    holder.progressTv.setText(progressStr);
                }
            });
        }
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, String desc) {
                    if (!mMsgQueue.isEmpty() && mMsgQueue.element().getId() == mSendMsgId) {
                        mMsgQueue.poll();
                        if (!mMsgQueue.isEmpty()) {
                            Message nextMsg = mMsgQueue.element();
                            MessageSendingOptions options = new MessageSendingOptions();
                            options.setNeedReadReceipt(true);
                            JMessageClient.sendMessage(nextMsg, options, true);
                            mSendMsgId = nextMsg.getId();
                        }
                    }
                    holder.picture.setAlpha(1.0f);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.progressTv.setVisibility(View.GONE);
                    if (status == 803008) {
                        CustomContent customContent = new CustomContent();
                        customContent.setBooleanValue("blackList", true);
                        Message customMsg = mConv.createSendMessage(customContent);
                        mAdapter.addMsgToList(customMsg);
                    } else if (status != 0) {
                        holder.resend.setVisibility(View.VISIBLE);
                    }

                    Message message = mConv.getMessage(msg.getId());
                    mMsgList.set(mMsgList.indexOf(msg), message);
                    //                    notifyDataSetChanged();
                }
            });

        }
    }

    public void handleVoiceMsg(final Message msg, final ViewHolder holder, final int position) {
        final VoiceContent content = (VoiceContent) msg.getContent();
        final MessageDirect msgDirect = msg.getDirect();
        int length = content.getDuration();
        String lengthStr = length + mContext.getString(R.string.jmui_symbol_second);
        holder.voiceLength.setText(lengthStr);
        //控制语音长度显示，长度增幅随语音长度逐渐缩小
        int width = (int) (-0.04 * length * length + 4.526 * length + 75.214);
        holder.txtContent.setWidth((int) (width * mDensity));
        //要设置这个position
        holder.txtContent.setTag(position);
        holder.txtContent.setOnLongClickListener(mLongClickListener);
        if (msgDirect == MessageDirect.send) {
            holder.voice.setImageResource(R.drawable.auto_not_send);
            switch (msg.getStatus()) {
                case created:
                    holder.sendingIv.setVisibility(View.VISIBLE);
                    holder.resend.setVisibility(View.GONE);
                    holder.text_receipt.setVisibility(View.GONE);
                    break;
                case send_success:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    break;
                case send_fail:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
                default:
            }
        } else
            switch (msg.getStatus()) {
                case receive_success:
                    if (mConv.getType() == ConversationType.group) {
                        holder.displayName.setVisibility(View.VISIBLE);
                        if (TextUtils.isEmpty(msg.getFromUser().getNickname())) {
                            holder.displayName.setText(msg.getFromUser().getUserName());
                        } else {
                            holder.displayName.setText(msg.getFromUser().getNickname());
                        }
                    }
                    holder.voice.setImageResource(R.drawable.auto_not_paly_revice);
                    // 收到语音，设置未读
                    if (msg.getContent().getBooleanExtra("isRead") == null
                            || !msg.getContent().getBooleanExtra("isRead")) {
                        mConv.updateMessageExtra(msg, "isRead", false);
                        holder.readStatus.setVisibility(View.VISIBLE);
                        if (mIndexList.size() > 0) {
                            if (!mIndexList.contains(position)) {
                                addToListAndSort(position);
                            }
                        } else {
                            addToListAndSort(position);
                        }
                        if (nextPlayPosition == position && autoPlay) {
                            playVoice(position, holder, false);
                        }
                    } else if (msg.getContent().getBooleanExtra("isRead")) {
                        holder.readStatus.setVisibility(View.GONE);
                    }
                    break;
                case receive_fail:
                    holder.voice.setImageResource(R.drawable.auto_revice_three);
                    // 接收失败，从服务器上下载
                    content.downloadVoiceFile(msg,
                            new DownloadCompletionCallback() {
                                @Override
                                public void onComplete(int status, String desc, File file) {

                                }
                            });
                    break;
                case receive_going:
                    break;
                default:
            }

        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (msg.getContent() != null) {
                        mAdapter.showResendDialog(holder, msg);
                    } else {
                        Toast.makeText(mContext, R.string.jmui_sdcard_not_exist_toast, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
        holder.txtContent.setOnClickListener(new BtnOrTxtListener(position, holder));
    }

    public void handleLocationMsg(final Message msg, final ViewHolder holder, int position) {
        final LocationContent content = (LocationContent) msg.getContent();
        Number lat = content.getLatitude();
        Number lgt = content.getLongitude();
        String address = content.getAddress();
        String mapUrl = createThumbMap(lgt, lat);
        String path = content.getStringExtra("path");

        Map<String, String> map = content.getStringExtras();
        String poi = map.get("poi");
        holder.location.setText(address);
        if (msg.getDirect() == MessageDirect.receive) {
            switch (msg.getStatus()) {
                case receive_going:
                    break;
                case receive_success:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            final Bitmap locationBitmap = createLocationBitmap(content.getLongitude(), content
                                    .getLatitude());
                            if (locationBitmap != null) {
                                mContext.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        holder.locationView.setVisibility(View.VISIBLE);
                                        holder.picture.setImageBitmap(locationBitmap);
                                    }
                                });
                            }
                        }
                    }).start();
                    break;
                case receive_fail:
                    break;
            }
        } else {
            if (mapUrl != null && holder.picture != null) {
                try {
                    Glide.with(mContext).load(mapUrl).into(holder.picture);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            switch (msg.getStatus()) {
                case created:
                    holder.text_receipt.setVisibility(View.GONE);
                    if (null != mUserInfo/* && !mUserInfo.isFriend()*/) {
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                    } else {
                        holder.sendingIv.setVisibility(View.VISIBLE);
                        holder.resend.setVisibility(View.GONE);
                    }
                    break;
                case send_going:
                    sendingTextOrVoice(holder, msg);
                    break;
                case send_success:
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.sendingIv.clearAnimation();
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
            }
        }
        if (holder.picture != null) {
            holder.picture.setOnClickListener(new BtnOrTxtListener(position, holder));
            holder.picture.setTag(position);
            holder.picture.setOnLongClickListener(mLongClickListener);

        }

        if (holder.resend != null) {
            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View arg0) {
                    if (msg.getContent() != null) {
                        mAdapter.showResendDialog(holder, msg);
                    } else {
                        Toast.makeText(mContext, R.string.jmui_sdcard_not_exist_toast, Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    private String createThumbMap(Number longitude, Number latitude) {
        String mapUrl = "https://restapi.amap.com/v3/staticmap?location=" + longitude + "," + latitude +
                "&zoom=17&size=750*300&markers=mid,,:" + longitude + "," + latitude +
                "&key=5ed7bccab25b740873fbfc4853113b8c";
        Log.d("yf", "mapUrl == " + mapUrl);
        return mapUrl;
    }

    //位置消息接收方根据百度api生成位置周围图片
    private Bitmap createLocationBitmap(Number longitude, Number latitude) {
        //d56cb8a2886f4f612677dd1c8fa3870c
        String mapUrl = createThumbMap(longitude, latitude);

        //String mapUrl = "http://api.map.baidu.com/staticimage?width=160&height=90&center="
        //        + longitude + "," + latitude + "&zoom=18";
        try {
            URL url = new URL(mapUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setDoOutput(false);
            conn.setDoInput(true);
            conn.setConnectTimeout(5000);
            conn.connect();
            if (conn.getResponseCode() == 200) {
                InputStream inputStream = conn.getInputStream();
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    //正在发送文字或语音
    private void sendingTextOrVoice(final ViewHolder holder, final Message msg) {
        holder.text_receipt.setVisibility(View.GONE);
        holder.resend.setVisibility(View.GONE);
        holder.sendingIv.setVisibility(View.VISIBLE);
        holder.sendingIv.startAnimation(mSendingAnim);
        //消息正在发送，重新注册一个监听消息发送完成的Callback
        if (!msg.isSendCompleteCallbackExists()) {
            msg.setOnSendCompleteCallback(new BasicCallback() {
                @Override
                public void gotResult(final int status, final String desc) {
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.sendingIv.clearAnimation();
                    if (status == 803008) {
                        CustomContent customContent = new CustomContent();
                        customContent.setBooleanValue("blackList", true);
                        Message customMsg = mConv.createSendMessage(customContent);
                        mAdapter.addMsgToList(customMsg);
                    } else if (status == 803005) {
                        holder.resend.setVisibility(View.VISIBLE);
                        ToastUtil.shortToast(mContext, "发送失败, 你不在该群组中");
                    } else if (status != 0) {
                        holder.resend.setVisibility(View.VISIBLE);
                        HandleResponseCode.onHandle(mContext, status, false,desc);
                    }
                }
            });
        }
    }

    //小视频
    public void handleVideo(final Message msg, final ViewHolder holder, int position) {
        FileContent fileContent = (FileContent) msg.getContent();
        String videoPath = fileContent.getLocalPath();
        if (videoPath != null) {
            //            String absolutePath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" +
            // msg.getServerMessageId();
            String thumbPath = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + msg
                    .getServerMessageId();
            String path = BitmapDecoder.extractThumbnail(videoPath, thumbPath);
            setPictureScale(null, msg, path, holder.picture);
            Glide.with(mContext).load(new File(path)).into(holder.picture);
        } else {
            Glide.with(mContext).load(R.drawable.video_not_found).into(holder.picture);
        }

        //        if (videoPath != null && new File(videoPath).exists()) {
        //            Bitmap bitmap = BitmapDecoder.extractThumbnail(videoPath);
        //            holder.picture.setImageBitmap(bitmap);
        //        } else {
        //            holder.picture.setImageResource(R.drawable.video_not_found);
        //        }

        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case created:
                    holder.videoPlay.setVisibility(View.GONE);
                    holder.text_receipt.setVisibility(View.GONE);
                    if (null != mUserInfo) {
                        holder.sendingIv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                    } else {
                        holder.sendingIv.setVisibility(View.VISIBLE);
                        holder.resend.setVisibility(View.GONE);
                    }
                    break;
                case send_success:
                    holder.sendingIv.clearAnimation();
                    holder.picture.setAlpha(1.0f);
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    holder.videoPlay.setVisibility(View.VISIBLE);
                    break;
                case send_fail:
                    holder.sendingIv.clearAnimation();
                    holder.sendingIv.setVisibility(View.GONE);
                    holder.picture.setAlpha(1.0f);
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    holder.videoPlay.setVisibility(View.VISIBLE);
                    break;
                case send_going:
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.videoPlay.setVisibility(View.GONE);
                    sendingImage(msg, holder);
                    break;
                default:
                    holder.picture.setAlpha(0.75f);
                    holder.sendingIv.setVisibility(View.VISIBLE);
                    holder.sendingIv.startAnimation(mSendingAnim);
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.videoPlay.setVisibility(View.GONE);
                    holder.progressTv.setText("0%");
                    holder.resend.setVisibility(View.GONE);
                    //从别的界面返回聊天界面，继续发送
                    if (!mMsgQueue.isEmpty()) {
                        Message message = mMsgQueue.element();
                        if (message.getId() == msg.getId()) {
                            MessageSendingOptions options = new MessageSendingOptions();
                            options.setNeedReadReceipt(true);
                            JMessageClient.sendMessage(message, options, true);
                            mSendMsgId = message.getId();
                            sendingImage(message, holder);
                        }
                    }
                    break;
            }

            holder.resend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mAdapter.showResendDialog(holder, msg);
                }
            });

        } else {
            switch (msg.getStatus()) {
                case receive_going:
                    holder.videoPlay.setVisibility(View.VISIBLE);
                    break;
                case receive_fail:
                    holder.videoPlay.setVisibility(View.VISIBLE);
                    break;
                case receive_success:
                    holder.videoPlay.setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }

        }
        holder.picture.setOnClickListener(new BtnOrTxtListener(position, holder));
        holder.picture.setTag(position);
        holder.picture.setOnLongClickListener(mLongClickListener);
    }


    public void handleFileMsg(final Message msg, final ViewHolder holder, int position) {
        final FileContent content = (FileContent) msg.getContent();
        if (holder.txtContent != null) {
            holder.txtContent.setText(content.getFileName());
        }
        Number fileSize = content.getNumberExtra("fileSize");
        if (fileSize != null && holder.sizeTv != null) {
            String size = FileUtils.getFileSize(fileSize);
            holder.sizeTv.setText(size);
        }
        String fileType = content.getStringExtra("fileType");
        Drawable drawable;
        if (fileType != null && (fileType.equals("mp4") || fileType.equals("mov") || fileType.equals("rm") ||
                fileType.equals("rmvb") || fileType.equals("wmv") || fileType.equals("avi") ||
                fileType.equals("3gp") || fileType.equals("mkv"))) {
            drawable = mContext.getResources().getDrawable(R.drawable.jmui_video);
        } else if (fileType != null && (fileType.equals("wav") || fileType.equals("mp3") || fileType.equals("wma") ||
                fileType.equals("midi"))) {
            drawable = mContext.getResources().getDrawable(R.drawable.jmui_audio);
        } else if (fileType != null && (fileType.equals("ppt") || fileType.equals("pptx") || fileType.equals("doc") ||
                fileType.equals("docx") || fileType.equals("pdf") || fileType.equals("xls") ||
                fileType.equals("xlsx") || fileType.equals("txt") || fileType.equals("wps"))) {
            drawable = mContext.getResources().getDrawable(R.drawable.jmui_document);
            //.jpeg .jpg .png .bmp .gif
        } else if (fileType != null && (fileType.equals("jpeg") || fileType.equals("jpg") || fileType.equals("png") ||
                fileType.equals("bmp") || fileType.equals("gif"))) {
            drawable = mContext.getResources().getDrawable(R.drawable.image_file);
        } else {
            drawable = mContext.getResources().getDrawable(R.drawable.jmui_other);
        }
        BitmapDrawable bitmapDrawable = (BitmapDrawable) drawable;
        if (holder.ivDocument != null)
            holder.ivDocument.setImageBitmap(bitmapDrawable.getBitmap());
        //        drawable.setBounds(0, 0, drawable.getMinimumWidth(), drawable.getMinimumHeight());
        //        holder.txtContent.setCompoundDrawables(drawable, null, null, null);

        if (msg.getDirect() == MessageDirect.send) {
            switch (msg.getStatus()) {
                case created:
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.progressTv.setText("0%");
                    holder.resend.setVisibility(View.GONE);
                    holder.text_receipt.setVisibility(View.GONE);
                    if (null != mUserInfo) {
                        holder.progressTv.setVisibility(View.GONE);
                        holder.resend.setVisibility(View.VISIBLE);
                    } else {
                        holder.progressTv.setVisibility(View.VISIBLE);
                        holder.progressTv.setText("0%");
                        holder.resend.setVisibility(View.GONE);
                    }
                    break;
                case send_going:
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.resend.setVisibility(View.GONE);
                    if (!msg.isContentUploadProgressCallbackExists()) {
                        msg.setOnContentUploadProgressCallback(new ProgressUpdateCallback() {
                            @Override
                            public void onProgressUpdate(double v) {
                                String progressStr = (int) (v * 100) + "%";
                                holder.progressTv.setText(progressStr);
                            }
                        });
                    }
                    if (!msg.isSendCompleteCallbackExists()) {
                        msg.setOnSendCompleteCallback(new BasicCallback() {
                            @Override
                            public void gotResult(int status, String desc) {
                                holder.contentLl.setBackground(mContext.getDrawable(R.drawable.jmui_msg_send_bg));
                                holder.progressTv.setVisibility(View.GONE);
                                if (status == 803008) {
                                    CustomContent customContent = new CustomContent();
                                    customContent.setBooleanValue("blackList", true);
                                    Message customMsg = mConv.createSendMessage(customContent);
                                    mAdapter.addMsgToList(customMsg);
                                } else if (status != 0) {
                                    holder.resend.setVisibility(View.VISIBLE);
                                }
                            }
                        });
                    }
                    break;
                case send_success:
                    holder.text_receipt.setVisibility(View.VISIBLE);
                    holder.contentLl.setBackground(mContext.getDrawable(R.drawable.jmui_msg_send_bg));
                    holder.alreadySend.setVisibility(View.VISIBLE);
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.GONE);
                    break;
                case send_fail:
                    holder.alreadySend.setVisibility(View.VISIBLE);
                    holder.alreadySend.setText("发送失败");
                    holder.text_receipt.setVisibility(View.GONE);
                    holder.contentLl.setBackground(mContext.getDrawable(R.drawable.jmui_msg_send_bg));
                    holder.progressTv.setVisibility(View.GONE);
                    holder.resend.setVisibility(View.VISIBLE);
                    break;
            }
        } else {
            switch (msg.getStatus()) {
                case receive_going:
                    holder.contentLl.setBackgroundColor(Color.parseColor("#86222222"));
                    holder.progressTv.setVisibility(View.VISIBLE);
                    holder.fileLoad.setText("");
                    if (!msg.isContentDownloadProgressCallbackExists()) {
                        msg.setOnContentDownloadProgressCallback(new ProgressUpdateCallback() {
                            @Override
                            public void onProgressUpdate(double v) {
                                if (v < 1) {
                                    String progressStr = (int) (v * 100) + "%";
                                    holder.progressTv.setText(progressStr);
                                } else {
                                    holder.progressTv.setVisibility(View.GONE);
                                    holder.contentLl.setBackground(mContext.getDrawable(R.drawable
                                            .jmui_msg_receive_bg));
                                }

                            }
                        });
                    }
                    break;
                case receive_fail://收到文件没下载也是这个状态
                    holder.progressTv.setVisibility(View.GONE);
                    //开始是用的下面这行设置但是部分手机会崩溃
                    //mContext.getDrawable(R.drawable.jmui_msg_receive_bg)
                    //如果用上面的报错 NoSuchMethodError 就把setBackground后面参数换成下面的
                    //ContextCompat.getDrawable(mContext, R.drawable.jmui_msg_receive_bg)
                    holder.contentLl.setBackground(ContextCompat.getDrawable(mContext, R.drawable
                            .jmui_msg_receive_bg));
                    holder.fileLoad.setText("未下载");
                    break;
                case receive_success:
                    holder.progressTv.setVisibility(View.GONE);
                    holder.contentLl.setBackground(mContext.getDrawable(R.drawable.jmui_msg_receive_bg));
                    holder.fileLoad.setText("已下载");
                    break;
            }
        }

        if (holder.fileLoad != null) {
            holder.fileLoad.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (msg.getDirect() == MessageDirect.send) {
                        mAdapter.showResendDialog(holder, msg);
                    } else {
                        holder.contentLl.setBackgroundColor(Color.parseColor("#86222222"));
                        holder.progressTv.setText("0%");
                        holder.progressTv.setVisibility(View.VISIBLE);
                        if (!msg.isContentDownloadProgressCallbackExists()) {
                            msg.setOnContentDownloadProgressCallback(new ProgressUpdateCallback() {
                                @Override
                                public void onProgressUpdate(double v) {
                                    String progressStr = (int) (v * 100) + "%";
                                    holder.progressTv.setText(progressStr);
                                }
                            });
                        }
                        content.downloadFile(msg, new DownloadCompletionCallback() {
                            @Override
                            public void onComplete(int status, String desc, File file) {
                                holder.progressTv.setVisibility(View.GONE);
                                holder.contentLl.setBackground(mContext.getDrawable(R.drawable.jmui_msg_receive_bg));
                                if (status != 0) {
                                    holder.fileLoad.setText("未下载");
                                    Toast.makeText(mContext, R.string.download_file_failed,
                                            Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(mContext, R.string.download_file_succeed, Toast.LENGTH_SHORT)
                                            .show();
                                }
                            }
                        });
                    }
                }
            });
        }
        holder.contentLl.setTag(position);
        holder.contentLl.setOnLongClickListener(mLongClickListener);
        holder.contentLl.setOnClickListener(new BtnOrTxtListener(position, holder));
    }


    public void handleGroupChangeMsg(Message msg, ViewHolder holder) {
        //部落的提示性消息
        List<String> userNames = ((EventNotificationContent) msg.getContent()).getUserNames();
        List<String> userDisplayNames = ((EventNotificationContent) msg.getContent()).getUserDisplayNames();
        if (null == userDisplayNames) {
            userDisplayNames = userNames;
        }
        StringBuffer eventText = new StringBuffer();
        String content = ((EventNotificationContent) msg.getContent()).getEventText();
        EventNotificationContent.EventNotificationType type = ((EventNotificationContent) msg
                .getContent()).getEventNotificationType();
        switch (type) {
            case group_member_added:
                eventText.append(userDisplayNames).append("加入部落");
                String add = eventText.toString();
                holder.groupChange.setText(add);
                holder.groupChange.setVisibility(View.GONE);
                holder.msgTime.setVisibility(View.GONE);
                break;
            case group_member_exit:
                eventText.append(userDisplayNames).append(" 退出部落");
                String exit = eventText.toString();
                holder.groupChange.setText(exit);
                holder.groupChange.setVisibility(View.GONE);
                holder.msgTime.setVisibility(View.GONE);
                break;
            case group_member_removed:
                eventText.append(userDisplayNames).append(" 被请出了部落");
                String removed = eventText.toString();
                holder.groupChange.setText(removed);
                holder.groupChange.setVisibility(View.GONE);
                holder.msgTime.setVisibility(View.GONE);
                break;
            case group_info_updated:
                holder.groupChange.setText(content);
                holder.groupChange.setVisibility(View.GONE);
                holder.msgTime.setVisibility(View.GONE);
                break;
        }
    }

    public void handlePromptMsg(Message msg, ViewHolder holder) {
        String promptText = ((PromptContent) msg.getContent()).getPromptText();
        holder.groupChange.setText(promptText);
        holder.groupChange.setVisibility(View.VISIBLE);
        holder.msgTime.setVisibility(View.GONE);
    }

    public void handleCustomMsg(final Message msg, ViewHolder holder) {
        CustomContent content = (CustomContent) msg.getContent();
        Boolean isBlackListHint = content.getBooleanValue("blackList");
        Boolean notFriendFlag = content.getBooleanValue("notFriend");
        if (isBlackListHint != null && isBlackListHint) {
            holder.groupChange.setText(R.string.jmui_server_803008);
            holder.groupChange.setVisibility(View.VISIBLE);
        } else {
            holder.groupChange.setVisibility(View.GONE);
        }

        Number customMsgTypes = content.getNumberValue(CUSTOM_MSG_TYPE);


        if (customMsgTypes != null) {
            //订单消息范畴 0
            if (customMsgTypes.intValue() == 0) {
                Number isRemindBuyer = content.getNumberValue(ORDER_IS_REMIND_BUYER);
                //只有达人看到
                if (isRemindBuyer.intValue() == 0) {
                    if (content.getStringValue(ORDER_USERID).equals(String.valueOf(BaseConfig.getUserId()))) {
                        String title = content.getStringValue(ORDER_TITLE);
                        String serviceContent = content.getStringValue(ORDER_SERVICE_CONTENT);
                        String appointTime = content.getStringValue(ORDER_APPOINT_TIME);
                        String serviceRemark = content.getStringValue(ORDER_SERVICE_REMARK);
                        if (holder.tv_message_title != null) {
                            holder.tv_message_title.setText(title);
                        }

                        if (holder.tv_message_time != null) {
                            if (TextUtils.isEmpty(appointTime)) {
                                holder.tv_message_time.setVisibility(View.GONE);
                            } else {
                                holder.tv_message_time.setText("预约时间:" + appointTime);
                            }
                        }
                        if (holder.tv_message_content != null) {
                            if (TextUtils.isEmpty(serviceContent)) {
                                holder.tv_message_content.setVisibility(View.GONE);
                            } else {
                                holder.tv_message_content.setText("服务内容:" + serviceContent);
                            }
                        }
                        if (holder.tv_message_introduce != null) {
                            if (TextUtils.isEmpty(serviceRemark)) {
                                holder.tv_message_introduce.setText("暂无需求描述~");
                            } else {
                                holder.tv_message_introduce.setText(serviceRemark);
                            }
                        }

                        if (holder.iv_message_back != null) {
                            holder.iv_message_back.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
//							int id = msg.getId();
                                    EventBus.getDefault().post(new DeleteCustomMessageEvent(msg));

                                }
                            });
                        }

                    } else {
                        if (holder.msgTime != null) {
                            holder.msgTime.setVisibility(View.GONE);
                        }
                    }


                }
                //只有买家看到
                else if (isRemindBuyer.intValue() == 1) {
                    String userId = content.getStringValue(ORDER_USERID);
                    final String info = content.getStringValue(ORDER_CONTENT);
                    Number orderId1 = content.getNumberValue(ORDER_ORDER_ID);
                    final long orderId = content.getNumberValue(ORDER_ORDER_ID).longValue();
                    if (content.getStringValue(ORDER_USERID).equals(String.valueOf(BaseConfig.getUserId()))) {
                        if (holder.tv_order_conetent != null) {
                            holder.tv_order_conetent.setText(info);
                        }
                        if (holder.tv_order_button != null) {
                            holder.tv_order_button.setOnClickListener(new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    EventBus.getDefault().post(new IMOderMessageEvent(orderId));
//									getOrderDetail(orderId);
                                }
                            });
                        }

                    } else {
                        if (holder.msgTime != null) {
                            holder.msgTime.setVisibility(View.GONE);
                        }
                    }


                }
            }
            //修改价格
            else if (customMsgTypes.intValue() == 1) {
                if (holder.iv_icon != null) {
                    ImageLoader.loadImage(holder.iv_icon, content.getStringValue(COURSE_ICON));
                }
                if (holder.tv_order_info != null) {
                    holder.tv_order_info.setText(content.getStringValue(COURSE_CONTENT));
                }

                if (holder.tv_newprice != null) {
                    holder.tv_newprice.setText(content.getStringValue(COURSE_NOW_PRICE));
                }

                if (holder.tv_oldprice != null) {
                    holder.tv_oldprice.setText(content.getStringValue(COURSE_OLD_PRICE));
                    holder.tv_oldprice.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }

                if (holder.tv_oldunit != null) {
                    holder.tv_oldunit.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                }

                if (holder.ll_updata != null) {
                    if (content.getStringValue(COURSE_IS_PRICE_CHANGE) != null) {
                        holder.ll_updata.setVisibility(content.getStringValue(COURSE_IS_PRICE_CHANGE).equals("1") ? View.VISIBLE : View.GONE);
                    }

                }

                if (holder.ll_not_updata != null) {
                    if (content.getStringValue(COURSE_IS_PRICE_CHANGE) != null) {
                        holder.ll_not_updata.setVisibility(content.getStringValue(COURSE_IS_PRICE_CHANGE).equals("1") ? View.GONE : View.VISIBLE);
                    }

                }
                if (holder.tv_newprice_not != null) {
                    holder.tv_newprice_not.setText(content.getStringValue(COURSE_NOW_PRICE));
                }
                if (holder.tv_oldprice_not != null) {
                    holder.tv_oldprice_not.setText(content.getStringValue(COURSE_OLD_PRICE));
                }
                final String courseId = content.getStringValue(COURSE_COURSE_ID);
                final String typesId = content.getStringValue(COURSE_TYPES_ID);
                final String userId = content.getStringValue(COURSE_USERID);
                String isPriceChange = content.getStringValue(COURSE_IS_PRICE_CHANGE);
                final String upData = String.valueOf(isPriceChange);

                if (holder.rl_click != null) {
                    holder.rl_click.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            IntentRoute.getIntentRoute().withType(ARouteHandleType.IM_TYPE_CHANGE_PRICE_DETAILS).withExtra(courseId).withExtra2(userId).withExtra3(upData).withExtra4(typesId).navigation();
                        }
                    });
                }

            }
            //音视频的自定义消息
            else if (customMsgTypes.intValue() == 2) {
                if (content.getNumberValue(RTC_CMD_TYPE) != null) {
                    if (content.getNumberValue(RTC_CMD_TYPE).intValue() == 1) {
                        if (content.getNumberValue(RTC_VIDEO_CHAT_SELECT_TYPE) != null) {
                            if (content.getNumberValue(RTC_VIDEO_CHAT_SELECT_TYPE).intValue() == 0) {
                                if (msg.getDirect() == MessageDirect.send) {
                                    holder.txtContent.setText("你已取消视频通话");
                                } else {
                                    holder.txtContent.setText("对方已取消视频通话");
                                }
                            } else {
                                if (msg.getDirect() == MessageDirect.send) {
                                    holder.txtContent.setText("你已取消语音通话");
                                } else {
                                    holder.txtContent.setText("对方已取消语音通话");
                                }
                            }
                        } else {
                            if (msg.getDirect() == MessageDirect.send) {
                                holder.txtContent.setText("你已主动挂断");
                            } else {
                                holder.txtContent.setText("对方已取消通话");
                            }
                        }
                        holder.txtContent.setVisibility(View.VISIBLE);
                    } else if (content.getNumberValue(RTC_CMD_TYPE).intValue() == 3) {
                        if (content.getStringValue(RTC_BUBBLE_VIDEO_CHAT_DURATION) != null) {
                            holder.txtContent.setText(content.getStringValue(RTC_BUBBLE_VIDEO_CHAT_DURATION));
                        }
                        holder.txtContent.setVisibility(View.VISIBLE);
                        if (holder.sendingIv != null) {
                            holder.sendingIv.setVisibility(View.GONE);
                        }
                        if (holder.resend != null) {
                            holder.resend.setVisibility(View.GONE);
                        }
                    }
                    //隐藏消息途径的时间
                    else if (content.getNumberValue(RTC_CMD_TYPE).intValue() == 0 || content.getNumberValue(RTC_CMD_TYPE).intValue() == 2) {
                        if (holder.msgTime != null) {
                            holder.msgTime.setVisibility(View.GONE);
                        }
                    }
                }
            }
            //群组的提示性消息
            else if (customMsgTypes.intValue() == 3) {
                if (content.getStringValue(GROUP_PROMPT_TEXT) != null) {
                    holder.groupChange.setText(content.getStringValue(GROUP_PROMPT_TEXT));
                    holder.groupChange.setVisibility(View.VISIBLE);
                    holder.msgTime.setVisibility(View.GONE);
                }
                //兼容老数据
                if (content.getStringValue(GROUP_PROMPT_TEXT_BIG) != null) {
                    holder.groupChange.setText(content.getStringValue(GROUP_PROMPT_TEXT_BIG));
                    holder.groupChange.setVisibility(View.VISIBLE);
                    holder.msgTime.setVisibility(View.GONE);
                }
            }

        }
        if (customMsgTypes != null && customMsgTypes.intValue() == 3) {
            holder.groupChange.setVisibility(View.VISIBLE);
        } else {
            holder.groupChange.setVisibility(View.GONE);
        }
    }

    /**
     * 获取订单状态
     */
    private void getOrderDetail(final long orderId) {
        JSONObject object = new JSONObject();
        object.put("orderId", orderId);
        HttpClient.getApi(IMApi.class)
                .getOrderById(object)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new BaseObserver<OrderPayInfoModel>() {
                    @Override
                    public void onNext2(@NonNull OrderPayInfoModel orderPayInfoModel) {
                        if (orderPayInfoModel.getOrderStatus() == -3) {
                            UIUtils.toastMessage("该订单已关闭");
                        } else {
                            IntentRoute.getIntentRoute().withType(IM_TYPE_ORDER_PAY).withExtra(String.valueOf(orderId)).navigation();
                        }
                    }

                    @Override
                    public void onError2(@NonNull Throwable t) {

                    }
                });

    }


    public class BtnOrTxtListener implements View.OnClickListener {

        private int position;
        private ViewHolder holder;

        public BtnOrTxtListener(int index, ViewHolder viewHolder) {
            this.position = index;
            this.holder = viewHolder;
        }

        @Override
        public void onClick(View v) {
            Message msg = mMsgList.get(position);
            MessageDirect msgDirect = msg.getDirect();
            switch (msg.getContentType()) {
                case voice:
                    if (!FileHelper.isSdCardExist()) {
                        Toast.makeText(mContext, R.string.jmui_sdcard_not_exist_toast,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    // 如果之前存在播放动画，无论这次点击触发的是暂停还是播放，停止上次播放的动画
                    if (mVoiceAnimation != null) {
                        mVoiceAnimation.stop();
                    }
                    // 播放中点击了正在播放的Item 则暂停播放
                    if (mp.isPlaying() && mPosition == position) {
                        if (msgDirect == MessageDirect.send) {
                            holder.voice.setImageResource(R.drawable.jmui_voice_send);
                        } else {
                            holder.voice.setImageResource(R.drawable.jmui_voice_receive);
                        }
                        mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
                        pauseVoice(msgDirect, holder.voice);
                        // 开始播放录音
                    } else if (msgDirect == MessageDirect.send) {
                        holder.voice.setImageResource(R.drawable.jmui_voice_send);
                        mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();

                        // 继续播放之前暂停的录音
                        if (mSetData && mPosition == position) {
                            mVoiceAnimation.start();
                            mp.start();
                            // 否则重新播放该录音或者其他录音
                        } else {
                            playVoice(position, holder, true);
                        }
                        // 语音接收方特殊处理，自动连续播放未读语音
                    } else {
                        try {
                            // 继续播放之前暂停的录音
                            if (mSetData && mPosition == position) {
                                if (mVoiceAnimation != null) {
                                    mVoiceAnimation.start();
                                }
                                mp.start();
                                // 否则开始播放另一条录音
                            } else {
                                // 选中的录音是否已经播放过，如果未播放，自动连续播放这条语音之后未播放的语音
                                if (msg.getContent().getBooleanExtra("isRead") == null
                                        || !msg.getContent().getBooleanExtra("isRead")) {
                                    autoPlay = true;
                                    playVoice(position, holder, false);
                                    // 否则直接播放选中的语音
                                } else {
                                    holder.voice.setImageResource(R.drawable.jmui_voice_receive);
                                    mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
                                    playVoice(position, holder, false);
                                }
                            }
                        } catch (IllegalArgumentException e) {
                            e.printStackTrace();
                        } catch (SecurityException e) {
                            e.printStackTrace();
                        } catch (IllegalStateException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case image:
                    if (holder.picture != null && v.getId() == holder.picture.getId()) {
                        //原先Demo的逻辑
                        Intent intent = new Intent();
                        intent.putExtra(JGApplication.TARGET_ID, mConv.getTargetId());
                        intent.putExtra("msgId", msg.getId());
                        if (mConv.getType() == ConversationType.group) {
                            GroupInfo groupInfo = (GroupInfo) mConv.getTargetInfo();
                            intent.putExtra(JGApplication.GROUP_ID, groupInfo.getGroupID());
                        }
                        intent.putExtra(JGApplication.TARGET_APP_KEY, mConv.getTargetAppKey());
                        intent.putExtra("msgCount", mMsgList.size());
                        intent.putIntegerArrayListExtra(JGApplication.MsgIDs, getImgMsgIDList());
                        intent.putExtra("fromChatActivity", true);
                        intent.setClass(mContext, BrowserViewPagerActivity.class);
                        mContext.startActivity(intent);
//						//跳转到本地的图片列表
//						ArrayList<Integer> imgMsgIDList = getImgMsgIDList();
//						Message msgs;
//						ImageContent ic;
//						for (int msgID : imgMsgIDList) {
//							msgs = mConv.getMessage(msgID);
//							if (msgs.getContentType().equals(ContentType.image)) {
//								ic = (ImageContent) msgs.getContent();
//								if (!TextUtils.isEmpty(ic.getLocalPath())) {
//									mPathList.add(ic.getLocalPath());
//								} else {
//									mPathList.add(ic.getLocalThumbnailPath());
//								}
//							}
//						}
//						IntentRoute
                    }
                    break;
                case location:
                    if (holder.picture != null && v.getId() == holder.picture.getId()) {
                        //Intent intent = new Intent(mContext, MapPickerActivity.class);
                        LocationContent locationContent = (LocationContent) msg.getContent();

                        Number lat_num = locationContent.getLatitude();
                        Number lng_num = locationContent.getLongitude();
                        String address = locationContent.getAddress();
                        String lat = String.valueOf(lat_num);
                        String lng = String.valueOf(lng_num);
//                        Map<String, String> extras = locationContent.getStringExtras();
//						String poi = extras.get("poi");
//						String address = extras.get("address");
//						String lat = extras.get("lat");
//						String lng = extras.get("lng");
                        IntentRoute.getIntentRoute().withType(ARouteHandleType.IM_TYPE_MAP).withExtra(lat)
                                .withExtra2(lng).withExtra3(address).navigation();
//
//						EventBus.getDefault().post(new LocationEvent.Builder()
//								.setType(EventType.located)
//								.setLatitude(locationContent.getLatitude().doubleValue())
//								.setLongitude(locationContent.getLongitude().doubleValue())
//								.setPoi(poi)
//								.setLocDesc(poi)
//								.setSendLocation(false)
//								.build());

                        //intent.putExtra("latitude", locationContent.getLatitude().doubleValue());
                        //intent.putExtra("longitude", locationContent.getLongitude().doubleValue());
                        //intent.putExtra("locDesc", locationContent.getAddress());
                        //intent.putExtra("sendLocation", false);
                        //mContext.startActivity(intent);
                    }
                    break;
                case file:
                    FileContent content = (FileContent) msg.getContent();
                    String fileName = content.getFileName();
                    String extra = content.getStringExtra("video");
                    if (extra != null) {
                        fileName = msg.getServerMessageId() + "." + extra;
                    }
                    final String path = content.getLocalPath();
                    if (path != null && new File(path).exists()) {
                        final String newPath = JGApplication.FILE_DIR + fileName;
                        File file = new File(newPath);
                        if (file.exists() && file.isFile()) {
                            browseDocument(fileName, newPath);
                        } else {
                            final String finalFileName = fileName;
                            FileHelper.getInstance().copyFile(fileName, path, (Activity) mContext,
                                    new FileHelper.CopyFileCallback() {
                                        @Override
                                        public void copyCallback(Uri uri) {
                                            browseDocument(finalFileName, newPath);
                                        }
                                    });
                        }
                    } else {
                        org.greenrobot.eventbus.EventBus.getDefault().postSticky(msg);
                        Intent intent = new Intent(mContext, DownLoadActivity.class);
                        mContext.startActivity(intent);
                    }
                    break;
            }

        }
    }


    public void playVoice(final int position, final ViewHolder holder, final boolean isSender) {
        // 记录播放录音的位置
        mPosition = position;
        Message msg = mMsgList.get(position);
        if (autoPlay) {
            mConv.updateMessageExtra(msg, "isRead", true);
            holder.readStatus.setVisibility(View.GONE);
            if (mVoiceAnimation != null) {
                mVoiceAnimation.stop();
                mVoiceAnimation = null;
            }
            holder.voice.setImageResource(R.drawable.jmui_voice_receive);
            mVoiceAnimation = (AnimationDrawable) holder.voice.getDrawable();
        }
        try {
            mp.reset();
            VoiceContent vc = (VoiceContent) msg.getContent();
            mFIS = new FileInputStream(vc.getLocalPath());
            mFD = mFIS.getFD();
            mp.setDataSource(mFD);
            if (mIsEarPhoneOn) {
                mp.setAudioStreamType(AudioManager.STREAM_VOICE_CALL);
            } else {
                mp.setAudioStreamType(AudioManager.STREAM_MUSIC);
            }
            mp.prepare();
            mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mVoiceAnimation.start();
                    mp.start();
                }
            });
            mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mVoiceAnimation.stop();
                    mp.reset();
                    mSetData = false;
                    if (isSender) {
                        holder.voice.setImageResource(R.drawable.auto_not_send);
                    } else {
                        holder.voice.setImageResource(R.drawable.auto_not_paly_revice);
                    }
                    if (autoPlay) {
                        int curCount = mIndexList.indexOf(position);
                        if (curCount + 1 >= mIndexList.size()) {
                            nextPlayPosition = -1;
                            autoPlay = false;
                        } else {
                            nextPlayPosition = mIndexList.get(curCount + 1);
                            mAdapter.notifyDataSetChanged();
                        }
                        if (curCount != -1 && mIndexList.size() >= curCount) {
                            mIndexList.remove(curCount);
                        }

                    }
                }
            });
        } catch (Exception e) {
            Toast.makeText(mContext, R.string.jmui_file_not_found_toast,
                    Toast.LENGTH_SHORT).show();
            VoiceContent vc = (VoiceContent) msg.getContent();
            vc.downloadVoiceFile(msg, new DownloadCompletionCallback() {
                @Override
                public void onComplete(int status, String desc, File file) {
                    if (status == 0) {
                        Toast.makeText(mContext, R.string.download_completed_toast,
                                Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(mContext, R.string.file_fetch_failed,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } finally {
            try {
                if (mFIS != null) {
                    mFIS.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 设置图片最小宽高
     *
     * @param path      图片路径
     * @param imageView 显示图片的View
     */
    private ImageView setPictureScale(String extra, Message message, String path, final ImageView imageView) {

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(path, opts);


        //计算图片缩放比例
        double imageWidth = opts.outWidth;
        double imageHeight = opts.outHeight;
        return setDensity(extra, message, imageWidth, imageHeight, imageView);
    }

    private ImageView setPictureScaleNew(String extra, Message message, String path, final ImageView imageView) {


        ImageContent imgContent = (ImageContent) message.getContent();
        int height = imgContent.getHeight();
        int width = imgContent.getWidth();

        return setDensityNew(message, width, height, imageView);
    }

    private ImageView setDensityNew(Message message, int bitmapWidth, int bitmapHeight, ImageView
            imageView) {
        int width;
        int height;
        Context context = imageView.getContext();
        if (bitmapWidth > bitmapHeight) {
            width = CommonUtils.getScreenWidth(context) / 2;
            height = (int) Math.ceil(width * bitmapHeight / bitmapWidth);
        } else {
            width = CommonUtils.getScreenWidth(context) / 3;
            height = (int) Math.ceil(width * bitmapHeight / bitmapWidth);
        }

        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) width;
        params.height = (int) height;
        imageView.setLayoutParams(params);

        return imageView;
    }

    private ImageView setDensity(String extra, Message message, double bitmapWidth, double bitmapHeight, ImageView
            imageView) {
        int width;
        int height;
        Context context = imageView.getContext();
        if (extra != null) {
            width = 200;
            height = 200;
        } else {
            //int height = (int) Math.ceil((float) width * (float) d.getIntrinsicHeight() / (float) d.getIntrinsicWidth());

            if (Math.abs(bitmapWidth - bitmapHeight) < 80) {
                return imageView;
            } else {
                if (bitmapWidth > bitmapHeight) {
                    width = CommonUtils.getScreenWidth(context) / 2;
                    height = (int) Math.ceil(width * bitmapHeight / bitmapWidth);
                } else {
                    width = CommonUtils.getScreenWidth(context) / 3;
                    height = (int) Math.ceil(width * bitmapHeight / bitmapWidth);
                }
            }


            //if (bitmapWidth > 350) {
            //	bitmapWidth = 550;
            //	bitmapHeight = 250;
            //} else if (bitmapHeight > 450) {
            //	bitmapWidth = 300;
            //	bitmapHeight = 450;
            //} else if ((bitmapWidth < 50 && bitmapWidth > 20) || (bitmapHeight < 50 && bitmapHeight > 20)) {
            //	bitmapWidth = 200;
            //	bitmapHeight = 300;
            //} else if (bitmapWidth < 20 || bitmapHeight < 20) {
            //	bitmapWidth = 100;
            //	bitmapHeight = 150;
            //} else {
            //	bitmapWidth = 300;
            //	bitmapHeight = 450;
            //}
        }

        ViewGroup.LayoutParams params = imageView.getLayoutParams();
        params.width = (int) width;
        params.height = (int) height;
        imageView.setLayoutParams(params);

        return imageView;
    }


    private DisplayImageOptions options = createImageOptions();

    private boolean hasLoaded = false;

    private static final DisplayImageOptions createImageOptions() {
        return new DisplayImageOptions.Builder()
                .cacheInMemory(true)
                .cacheOnDisk(true)
                .bitmapConfig(Bitmap.Config.RGB_565)
                .build();
    }

    public void setAudioPlayByEarPhone(int state) {
        AudioManager audioManager = (AudioManager) mContext
                .getSystemService(Context.AUDIO_SERVICE);
        int currVolume = audioManager.getStreamVolume(AudioManager.STREAM_VOICE_CALL);
        audioManager.setMode(AudioManager.MODE_IN_CALL);
        if (state == 0) {
            mIsEarPhoneOn = false;
            audioManager.setSpeakerphoneOn(true);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL,
                    audioManager.getStreamMaxVolume(AudioManager.STREAM_VOICE_CALL),
                    AudioManager.STREAM_VOICE_CALL);
        } else {
            mIsEarPhoneOn = true;
            audioManager.setSpeakerphoneOn(false);
            audioManager.setStreamVolume(AudioManager.STREAM_VOICE_CALL, currVolume,
                    AudioManager.STREAM_VOICE_CALL);
        }
    }

    public void releaseMediaPlayer() {
        if (mp != null)
            mp.release();
    }

    public void initMediaPlayer() {
        mp.reset();
    }

    public void stopMediaPlayer() {
        if (mp.isPlaying())
            mp.stop();
    }

    private void pauseVoice(MessageDirect msgDirect, ImageView voice) {
        if (msgDirect == MessageDirect.send) {
            voice.setImageResource(R.drawable.auto_not_send);
        } else {
            voice.setImageResource(R.drawable.auto_not_paly_revice);
        }
        mp.pause();
        mSetData = true;
    }


    private void addToListAndSort(int position) {
        mIndexList.add(position);
        Collections.sort(mIndexList);
    }

    private ArrayList<Integer> getImgMsgIDList() {
        ArrayList<Integer> imgMsgIDList = new ArrayList<Integer>();
        for (Message msg : mMsgList) {
            if (msg.getContentType() == ContentType.image) {
                imgMsgIDList.add(msg.getId());
            }
        }
        return imgMsgIDList;
    }

    private void browseDocument(String fileName, String path) {
        try {
            String ext = fileName.substring(fileName.lastIndexOf('.') + 1).toLowerCase();
            MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
            String mime = mimeTypeMap.getMimeTypeFromExtension(ext);
            File file = new File(path);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.fromFile(file), mime);
            mContext.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, R.string.file_not_support_hint, Toast.LENGTH_SHORT).show();
        }
    }

}
