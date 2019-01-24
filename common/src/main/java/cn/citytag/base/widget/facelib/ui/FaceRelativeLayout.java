package cn.citytag.base.widget.facelib.ui;

/**
 * Created by liguangchun on 2017/12/7.
 */

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.view.ViewPager;
import android.text.Editable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import cn.citytag.base.R;
import cn.citytag.base.utils.EditUtils;
import cn.citytag.base.utils.StringUtils;
import cn.citytag.base.utils.UIUtils;
import cn.citytag.base.widget.facelib.adapter.FaceAdapter;
import cn.citytag.base.widget.facelib.adapter.ViewPagerAdapter;
import cn.citytag.base.widget.facelib.model.ChatFaceModel;
import cn.citytag.base.widget.facelib.util.FaceConversionUtil;

/**
 * 带表情的自定义输入框
 */
public class FaceRelativeLayout extends RelativeLayout implements
        AdapterView.OnItemClickListener, View.OnClickListener {

    private Context context;

    private Activity activity;

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }


    private IFaceVisibleListener iFaceVisibleListener;  //face表情库是否可见

    public IFaceVisibleListener getFaceVisibleListener() {
        return iFaceVisibleListener;
    }

    public void setFaceVisibleListener(IFaceVisibleListener iFaceVisibleListener) {
        this.iFaceVisibleListener = iFaceVisibleListener;
    }

    private IClickBottomEdit iClickBottomEdit;  //点击下部分bottomEditText的回调

    public IClickBottomEdit getiClickBottomEdit() {
        return iClickBottomEdit;
    }

    public void setiClickBottomEdit(IClickBottomEdit iClickBottomEdit) {
        this.iClickBottomEdit = iClickBottomEdit;
    }

    private boolean isChangeFaceIcon = true;

    public void setIsChangeFaceIcon(boolean b) {
        isChangeFaceIcon = b;
    }

    /**
     * 表情页的监听事件
     */
    private OnCorpusSelectedListener mListener;

    /**
     * 显示表情页的viewpager
     */
    private ViewPager vp_face;

    /**
     * 表情页界面集合
     */
    private ArrayList<View> pageViews;

    /**
     * 游标显示布局
     */
    private LinearLayout layout_point;

    /**
     * 游标点集合
     */
    private ArrayList<ImageView> pointViews;

    /**
     * 表情集合
     */
    private List<List<ChatFaceModel>> emojis;

    /**
     * 表情区域
     */
    private View faceIconList;

    /**
     * 输入框
     */
    private EditText et_sendmessage;


    /*对于软键盘不出现时下面的bottom*/

    private EditText mEditTextBottom;

    //private ImageView mImageButtonBottom;

    //private LinearLayout mLlEditBottom;

    private RelativeLayout mRlEdit;

    public boolean isGoneFaceView = true;

    public boolean isClickEdit = false;/*是否点击底部的输入框*/

    /**
     * 表情数据填充器
     */
    private List<FaceAdapter> faceAdapters;

    /**
     * 当前表情页
     */
    private int current = 0;

    private LinearLayout selectPicAndSend;


    private OnClickListener onClickListener;

    private CloseImeInterface mcloseIme;

    private LinearLayout mLlEditBottom;

    public FaceRelativeLayout(Context context) {
        super(context);
        this.context = context;
    }

    public FaceRelativeLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
    }

    public FaceRelativeLayout(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
    }

    public void addSendMsgListener(OnClickListener listener) {
        onClickListener = listener;
    }

    public void unImeInterface() {

        if (mcloseIme != null) {
            mcloseIme = null;
        }
    }

    public void setOnCorpusSelectedListener(OnCorpusSelectedListener listener) {
        mListener = listener;
    }

    public int isGone = 0;

    public int getIsGone() {
        return isGone;
    }

    public void setIsGone(int isGone) {
        this.isGone = isGone;
    }

    /**
     * 表情选择监听
     */
    public interface OnCorpusSelectedListener {

        void onCorpusSelected(ChatFaceModel emoji);

        void onCorpusDeleted();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        emojis = FaceConversionUtil.getInstace().emojiLists;
        onCreate();
    }

    private void onCreate() {
        initView();
        initViewPager();
        initPoint();
        initData();
    }

    @Override
    public void onClick(View v) {
        if (v == null) return;
        int id = v.getId();
        if (id == R.id.btn_face_bottom) {
            if (faceIconList.getVisibility() == View.VISIBLE) {
                faceIconList.setVisibility(View.GONE);
                mRlEdit.setVisibility(GONE);
                if (mLlEditBottom != null)
                    mLlEditBottom.setVisibility(VISIBLE);
                isGoneFaceView = true;
            } else {
                faceIconList.setVisibility(View.VISIBLE);
                et_sendmessage.setVisibility(VISIBLE);
                mRlEdit.setVisibility(VISIBLE);
                if (mLlEditBottom != null)
                    mLlEditBottom.setVisibility(GONE);
                isGoneFaceView = false;
                //if (selectPic != null) selectPic.setVisibility(GONE);
            }
            if (mcloseIme != null) {
                mcloseIme.closeIme();
            }
        } else if (id == R.id.btn_face) {
            // case R.id.btn_face_bottom:
            // 隐藏表情选择框
            if (faceIconList.getVisibility() == View.VISIBLE) {
                faceIconList.setVisibility(View.GONE);
                if (isGone == 0) {
                    if (mLlEditBottom != null) {
                        mRlEdit.setVisibility(GONE);
                    }

                } else {
                    mRlEdit.setVisibility(VISIBLE);
                }
                if (mLlEditBottom != null)
                    mLlEditBottom.setVisibility(VISIBLE);
                isGoneFaceView = true;
                EditUtils.showSoftInput(context, et_sendmessage);
                // if (isChangeFaceIcon) {
                mFaceImageView.setBackgroundResource(R.drawable.ic_emoji_reply);
                // }
                if (iFaceVisibleListener != null) {
                    iFaceVisibleListener.isFaceVisible(false);
                }

            } else {
                //  if (isChangeFaceIcon) {
                mFaceImageView.setBackgroundResource(R.drawable.ic_keyboard);
                //   }
                if (activity != null) {
                    activity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING); //  不改变布局，隐藏键盘，emojiView弹出
                }

                InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);

                imm.hideSoftInputFromWindow(mFaceImageView.getWindowToken(), 0); //强制隐藏键盘
                faceIconList.setVisibility(View.VISIBLE);
                if (et_sendmessage != null) {
                    et_sendmessage.setVisibility(VISIBLE);
                }

                mRlEdit.setVisibility(VISIBLE);
                if (mLlEditBottom != null)
                    mLlEditBottom.setVisibility(GONE);
                isGoneFaceView = false;
                //if (selectPic != null) selectPic.setVisibility(GONE);
                if (iFaceVisibleListener != null) {
                    iFaceVisibleListener.isFaceVisible(true);
                }
            }
            if (mcloseIme != null) {
                mcloseIme.closeIme();
            }
        } else if (id == R.id.edit_comment) {
            // 隐藏表情选择框
            if (faceIconList.getVisibility() == View.VISIBLE) {
                faceIconList.setVisibility(View.GONE);
                isGoneFaceView = true;
                mFaceImageView.setBackgroundResource(R.drawable.ic_emoji_reply);
            }
        }

    }

    public void setImeListener(CloseImeInterface ime) {
        mcloseIme = ime;
    }

    public interface CloseImeInterface {
        void closeIme();
    }

    /**
     * 隐藏表情选择框
     */
    public void hideFaceView() {
        // 隐藏表情选择框
        if (faceIconList != null) {
            isGoneFaceView = true;
            faceIconList.setVisibility(View.GONE);
            //  if (selectPic != null) selectPic.setVisibility(GONE);
        }

    }

    private ImageView mBottomFaceImageView;
    private ImageView mFaceImageView;

    public ImageView getmFaceImageView() {
        return mFaceImageView;
    }

    public void setmFaceImageView(ImageButton mFaceImageView) {
        this.mFaceImageView = mFaceImageView;
    }

    /**
     * 初始化控件
     */
    private void initView() {
        mLlEditBottom = findViewById(R.id.ll_edit_bottom);
        vp_face = findViewById(R.id.vp_contains);
        et_sendmessage = findViewById(R.id.edit_comment);
        layout_point = findViewById(R.id.iv_image);

        // mLlEditBottom = findViewById(R.id.ll_edit_bottom);
        mRlEdit = findViewById(R.id.rl_edit);
        if (et_sendmessage != null) {
            et_sendmessage.setOnClickListener(this);
        }
        mFaceImageView = findViewById(R.id.btn_face);
        if (mFaceImageView != null) {
            mFaceImageView.setOnClickListener(this);
        }
        mBottomFaceImageView = findViewById(R.id.btn_face_bottom);
        if (mBottomFaceImageView != null) {
            mBottomFaceImageView.setOnClickListener(this);
        }
        faceIconList = findViewById(R.id.ll_face);

        mEditTextBottom = findViewById(R.id.et_send_bottom);

        /*mEditTextBottom.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                // 隐藏表情选择框
                if (faceIconList.getVisibility() == View.VISIBLE) {
                    faceIconList.setVisibility(View.GONE);
                }
            }
        });*/
        //mEditTextBottom.setOnClickListener(this);
        // mImageButtonBottom = findViewById(R.id.btn_face_bottom);
        //mImageButtonBottom.setOnClickListener(this);

        //  leftIconBt.setOnClickListener(this);

        //  selectPicAndSend = (LinearLayout) findViewById(R.id.activity_chat_button_send);
        if (selectPicAndSend != null) {
            selectPicAndSend.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (StringUtils.isEmpty(getText(et_sendmessage))) {
                        if (mcloseIme != null) {
                            mcloseIme.closeIme();
                        }
                      /*if (selectPic == null) return;
                        if (selectPic.getVisibility() == VISIBLE) selectPic.setVisibility(GONE);
                        else {
                            if (faceIconList != null) faceIconList.setVisibility(GONE);
                            selectPic.setVisibility(VISIBLE);
                        }*/
                    } else if (onClickListener != null) {
                        onClickListener.onClick(v);
                    }
                }
            });
        }

        if (et_sendmessage != null) {
            et_sendmessage.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

                }

                @Override
                public void afterTextChanged(Editable editable) {
                    if (selectPicAndSend == null) return;
                    String text = getText(et_sendmessage);
                    boolean hasText = !StringUtils.isEmpty(text);
                    // selectPicAndSend.setBackgroundDrawable(getResources().getDrawable(hasText ? R.mipmap.chat_send : R.mipmap.chat_more));
                }
            });
        }
        if (mEditTextBottom != null) {
            mEditTextBottom.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean b) {
                    if (b) {
                        mFaceImageView.setBackgroundResource(R.drawable.ic_emoji_reply);
                        // mFaceImageView.setBackgroundResource(R.drawable.ic_keyboard);
                        hideFaceView();
                        isClickEdit = true;
                        if (iClickBottomEdit != null) {
                            iClickBottomEdit.click();

                        }
                    }
                }
            });
        }
        if (et_sendmessage != null) {
            et_sendmessage.setOnFocusChangeListener(new OnFocusChangeListener() {
                @Override
                public void onFocusChange(View view, boolean hasFocus) {

                    if (hasFocus) {
                        hideFaceView();
                        mFaceImageView.setBackgroundResource(R.drawable.ic_emoji_reply);

                    }
                }
            });
        }
    }


    /**
     * 初始化显示表情的viewpager
     */
    private void initViewPager() {
        pageViews = new ArrayList<>();
        // 左侧添加空页
        View nullView1 = new View(context);
        // 设置透明背景
        nullView1.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView1);

        // 中间添加表情页

        faceAdapters = new ArrayList<>();
        for (int i = 0; i < emojis.size(); i++) {
            GridView view = new GridView(context);
            FaceAdapter adapter = new FaceAdapter(context, emojis.get(i));
            view.setAdapter(adapter);
            faceAdapters.add(adapter);
            view.setOnItemClickListener(this);
            view.setNumColumns(7);
            view.setBackgroundColor(Color.TRANSPARENT);
            view.setHorizontalSpacing(1);
            view.setVerticalSpacing(1);
            view.setStretchMode(GridView.STRETCH_COLUMN_WIDTH);
            view.setCacheColorHint(0);
            view.setPadding(5, 0, 5, 0);
            view.setSelector(new ColorDrawable(Color.TRANSPARENT));
            LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT,
                    LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(0, 0, 0, 0);
            view.setLayoutParams(layoutParams);
            view.setGravity(Gravity.CENTER);
            pageViews.add(view);
        }

        // 右侧添加空页面
        View nullView2 = new View(context);
        // 设置透明背景
        nullView2.setBackgroundColor(Color.TRANSPARENT);
        pageViews.add(nullView2);
    }

    /**
     * 初始化游标
     */
    private void initPoint() {

        pointViews = new ArrayList<>();
        ImageView imageView;
        for (int i = 0; i < pageViews.size(); i++) {
            imageView = new ImageView(context);
            imageView.setBackgroundResource(R.drawable.ic_cus_face_page_no_select);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                    new ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT,
                            LayoutParams.WRAP_CONTENT));
            layoutParams.leftMargin = 10;
            layoutParams.rightMargin = 10;
            layoutParams.width = UIUtils.dip2px(7);
            layoutParams.height = UIUtils.dip2px(7);
            layout_point.addView(imageView, layoutParams);
            if (i == 0 || i == pageViews.size() - 1) {
                imageView.setVisibility(View.GONE);
            }
            if (i == 1) {
                imageView.setBackgroundResource(R.drawable.ic_cus_face_page_select);
            }
            pointViews.add(imageView);

        }
    }

    /**
     * 填充数据
     */
    private void initData() {
        vp_face.setAdapter(new ViewPagerAdapter(pageViews));

        vp_face.setCurrentItem(1);
        current = 0;
        vp_face.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageSelected(int arg0) {
                current = arg0 - 1;
                // 描绘分页点
                drawPoint(arg0);
                // 如果是第一屏或者是最后一屏禁止滑动，其实这里实现的是如果滑动的是第一屏则跳转至第二屏，
                // 如果是最后一屏则跳转到倒数第二屏.
                if (arg0 == pointViews.size() - 1 || arg0 == 0) {
                    if (arg0 == 0) {
                        vp_face.setCurrentItem(arg0 + 1);// 第二屏 会再次实现该回调方法实现跳转.
                        pointViews.get(1).setBackgroundResource(R.drawable.ic_cus_face_page_select);
                    } else {
                        vp_face.setCurrentItem(arg0 - 1);// 倒数第二屏
                        pointViews.get(arg0 - 1).setBackgroundResource(
                                R.drawable.ic_cus_face_page_select);
                    }
                }
            }

            @Override
            public void onPageScrolled(int arg0, float arg1, int arg2) {

            }

            @Override
            public void onPageScrollStateChanged(int arg0) {

            }
        });

    }

    /**
     * 绘制游标背景
     */
    public void drawPoint(int index) {
        for (int i = 1; i < pointViews.size(); i++) {
            if (index == i) {
                pointViews.get(i).setBackgroundResource(R.drawable.ic_cus_face_page_select);
            } else {
                pointViews.get(i).setBackgroundResource(R.drawable.ic_cus_face_page_no_select);
            }
        }
    }

    @Override
    public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
        ChatFaceModel emoji = (ChatFaceModel) faceAdapters.get(current).getItem(arg2);
        if (emoji.getId() == R.drawable.face_del_ico_dafeult) {
            int selection = et_sendmessage.getSelectionStart();
            String text = et_sendmessage.getText().toString();
            if (selection > 0) {
                String text2 = text.substring(selection - 1);
                if ("]".equals(text2)) {
                    int start = text.lastIndexOf("[");
                    et_sendmessage.getText().delete(start, selection);
                    return;
                }
                et_sendmessage.getText().delete(selection - 1, selection);
            }
        }
        int len = et_sendmessage.getText() == null ? 0 : et_sendmessage.getText().length();
        if (!TextUtils.isEmpty(emoji.getCharacter()) && len + emoji.getCharacter().length() < 300) {
            if (mListener != null)
                mListener.onCorpusSelected(emoji);
            if (et_sendmessage == null) return;
            SpannableString spannableString = FaceConversionUtil.getInstace()
                    .addFace(getContext(), emoji.getId(), emoji.getCharacter());
            int selectionIndex = et_sendmessage.getSelectionStart();
            Editable editable = et_sendmessage.getText();
            editable.insert(selectionIndex, spannableString);
            //et_sendmessage.append(spannableString);
            //mEditTextBottom.append(spannableString);
        }

    }

    public String getText(TextView view) {

        return view == null ? null : view.getText().toString();
    }


    public interface IClickBottomEdit {
        void click();
    }

    public interface IFaceVisibleListener {
        void isFaceVisible(boolean b);
    }

    /**
     * 手动设置输入框
     *
     * @param editText
     */
    public void setEditText(EditText editText) {
        et_sendmessage = editText;
    }

}
