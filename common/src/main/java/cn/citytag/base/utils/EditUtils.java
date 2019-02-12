package cn.citytag.base.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.FrameLayout;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by yangfeng01 on 2017/11/30.
 */

public class EditUtils {

    private static final int DECIMAL_ONE = 1;
    private static final int DECIMAL_TWO = 2;
    private static final int DECIMAL_THREE = 3;
    private static final int DECIMAL_FOUR = 4;
    private static final int DECIMAL_FIVE = 5;

    //I can change my text using afterTextChanged while onTextChanged does not allow me to do that
    //onTextChanged gives me the offset of what changed where, while afterTextChanged does not
    private class TextWatcherAdapter implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            // This method is called to notify you that, within s, the count characters beginning at start are about
            // to be replaced by new text with length after.
            //这个方法被调用，说明在s字符串中，从start位置开始的count个字符即将被长度为after的新文本所取代。在这个方法里面改变s，会报错。
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            // This method is called to notify you that, within s, the count characters beginning at start have just
            // replaced old text that had length before.
            //这个方法被调用，说明在s字符串中，从start位置开始的count个字符刚刚取代了长度为before的旧文本。在这个方法里面改变s，会报错。
        }

        @Override
        public void afterTextChanged(Editable s) {
            // This method is called to notify you that, somewhere within s, the text has been changed.
            //这个方法被调用，那么说明s字符串的某个地方已经被改变。

        }
    }

    /**
     * 输入小数限制小数位数
     *
     * @param editText
     * @param digits
     */
    public static void decimals(final EditText editText, final int digits) {
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                if (s.toString().contains(".")) {
                    if (s.length() - 1 - s.toString().indexOf(".") > digits) { // 当输入小数位的长度比限制的最大小数位数digits还要长的时候，去截取成digits位小数。否则都不用管。
                        s = s.toString().subSequence(0, s.toString().indexOf(".") + digits + 1);
                        editText.setText(s);
                        editText.setSelection(s.length());
                    }
                }
                if (s.toString().trim().substring(0).equals(".")) {
                    s = "0" + s;
                    editText.setText(s);
                    editText.setSelection(2);
                }

                if (s.toString().startsWith("0") && s.toString().trim().length() > 1) {
                    if (!s.toString().substring(1, 2).equals(".")) {
                        editText.setText(s.subSequence(0, 1));
                        editText.setSelection(1);
                    }
                }
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    /**
     * xml里android:editable=false，完全不可编辑，做不到代码动态
     *
     * @param editText
     * @param editable 是否可编辑
     */
    public static void editable(EditText editText, boolean editable) {
        editText.setCursorVisible(editable);    // 输入框中光标不可见
        editText.setFocusable(editable);        // 无焦点
        editText.setFocusableInTouchMode(editable); // 触摸时也得不到焦点
    }

    public static void readOnly(EditText editText) {
        editText.setKeyListener(null);
    }

    /**
     * 检查所有的edits都不为空
     *
     * @param edits
     * @return
     */
    public static boolean checkNotEmpty(List<EditText> edits) {
        for (EditText edit : edits) {
            if (StringUtils.isEmpty(edit.getText().toString().trim())) {
                return false;
            }
        }
        return true;
    }

    /**
     * 检查密码不为空
     *
     * @param edits
     * @return
     */
    public static boolean checkPWDNotEmpty(EditText edits) {
        return !StringUtils.isEmpty(edits.getText().toString().trim()) && edits.getText().toString().length() >= 6;
    }

    /**
     * 检查邀请码不为空
     *
     * @param edits
     * @return
     */
    public static boolean checkIntroNotEmpty(EditText edits) {
        return !StringUtils.isEmpty(edits.getText().toString().trim());
    }

    /**
     * 检查验证码不为空
     *
     * @param edits
     * @return
     */
    public static boolean checkCodeNotEmpty(EditText edits) {
        return !StringUtils.isEmpty(edits.getText().toString().trim());
    }

    /**
     * 检查phone格式
     *
     * @param edits
     * @return
     */
    public static boolean checkPhoneNotEmpty(EditText edits) {
        return !StringUtils.isEmpty(edits.getText().toString().replace(" ","")) && edits.getText().toString().replace(" ","").length() >= 11;
    }

    /**
     * 动态显示软键盘
     */
    public static void showSoftInput(final Context context, final EditText editText) {
        if (editText == null) {
            return;
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        editText.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                // 这个方法的两个参数，showSoftInput(View view, int flags)
                // view是要在哪个view的基础上显示输入面板，同时再使用该方法之前，view需要获得焦点，可以通过requestFocus()方法来设定。
                if (inputMethodManager == null) {
                    return;
                }
                inputMethodManager.showSoftInput(editText, 0);
            }
        });
    }

    /**
     * 动U态隐藏软键盘
     */
    public static void hideSoftInput(Activity activity) {
        if (activity == null) {
            return;
        }
        View view = activity.getWindow().peekDecorView();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 动态隐藏软键盘
     */
    public static void hideSoftInput(View view) {
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) ActivityUtils.peek().getSystemService(Context
                    .INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    /**
     * 动态隐藏软键盘
     */
    public static void hideSoftInput(final Context context, final EditText editText) {
        if (editText == null) {
            return;
        }
        editText.post(new Runnable() {
            @Override
            public void run() {
                InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context
                        .INPUT_METHOD_SERVICE);
                if(inputMethodManager == null){
                    return;
                }
                inputMethodManager.hideSoftInputFromWindow(editText.getWindowToken(), 0);
                editText.clearFocus();
            }
        });
    }

    /**
     * 切换键盘显示与否
     */
    public static void toggleSoftInput(Context context, EditText editText) {
        if (editText == null) {
            return;
        }
        editText.setFocusable(true);
        editText.setFocusableInTouchMode(true);
        editText.requestFocus();
        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context
                .INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    /**
     * 点击屏幕空白区域隐藏软键盘（方法1）
     * <p>在onTouch中处理，未获焦点则隐藏</p>
     * <p>参照以下注释代码</p>
     */
    public static void clickBlankArea2HideSoftInput0() {
        Log.i("tips", "U should copy the following code.");
        /*
        @Override
        public boolean onTouchEvent (MotionEvent event){
            if (null != this.getCurrentFocus()) {
                InputMethodManager mInputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                return mInputMethodManager.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
            }
            return super.onTouchEvent(event);
        }
        */
    }

    public static boolean isSoftInputVisible(final Activity activity) {
        return getDecorViewInvisibleHeight(activity) > 0;
    }


    private static int sDecorViewDelta = 0;

    private static int getDecorViewInvisibleHeight(final Activity activity) {
        final View decorView = activity.getWindow().getDecorView();
        if (decorView == null) return 0;
        final Rect outRect = new Rect();
        decorView.getWindowVisibleDisplayFrame(outRect);
        Log.d("KeyboardUtils", "getDecorViewInvisibleHeight: "
                + (decorView.getBottom() - outRect.bottom));
        int delta = Math.abs(decorView.getBottom() - outRect.bottom);
        if (delta <= getNavBarHeight()) {
            sDecorViewDelta = delta;
            return 0;
        }
        return delta - sDecorViewDelta;
    }

    private static int getNavBarHeight() {
        Resources res = Resources.getSystem();
        int resourceId = res.getIdentifier("navigation_bar_height", "dimen", "android");
        if (resourceId != 0) {
            return res.getDimensionPixelSize(resourceId);
        } else {
            return 0;
        }
    }


    public interface OnSoftInputChangedListener {
        void onSoftInputChanged(int height);
    }
    private static int                        sDecorViewInvisibleHeightPre;
    private static ViewTreeObserver.OnGlobalLayoutListener onGlobalLayoutListener;
    private static OnSoftInputChangedListener onSoftInputChangedListener;

    public static void registerSoftInputChangedListener(final Activity activity,
                                                        final OnSoftInputChangedListener listener) {
        final int flags = activity.getWindow().getAttributes().flags;
        if ((flags & WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS) != 0) {
            activity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        final FrameLayout contentView = activity.findViewById(android.R.id.content);
        sDecorViewInvisibleHeightPre = getDecorViewInvisibleHeight(activity);
        onSoftInputChangedListener = listener;
        onGlobalLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (onSoftInputChangedListener != null) {
                    int height = getDecorViewInvisibleHeight(activity);
                    if (sDecorViewInvisibleHeightPre != height) {
                        onSoftInputChangedListener.onSoftInputChanged(height);
                        sDecorViewInvisibleHeightPre = height;
                    }
                }
            }
        };
        contentView.getViewTreeObserver()
                .addOnGlobalLayoutListener(onGlobalLayoutListener);
    }

    /**
     * Unregister soft input changed listener.
     *
     * @param activity The activity.
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    public static void unregisterSoftInputChangedListener(final Activity activity) {
        final View contentView = activity.findViewById(android.R.id.content);
        contentView.getViewTreeObserver().removeOnGlobalLayoutListener(onGlobalLayoutListener);
        onSoftInputChangedListener = null;
        onGlobalLayoutListener = null;
    }



    /**
     * Return whether soft input is visible.
     *
     * @param activity             The activity.
     * @param minHeightOfSoftInput The minimum height of soft input.
     * @return {@code true}: yes<br>{@code false}: no
     */
    public static boolean isSoftInputVisible(final Activity activity,
                                             final int minHeightOfSoftInput) {
        return getContentViewInvisibleHeight(activity) >= minHeightOfSoftInput;
    }

    private static int getContentViewInvisibleHeight(final Activity activity) {
        final View contentView = activity.findViewById(android.R.id.content);
        Rect r = new Rect();
        contentView.getWindowVisibleDisplayFrame(r);
        return contentView.getRootView().getHeight() - r.height();
    }

    /**
     * 验证手机格式
     */
    public static boolean isMobile(String number) {
    /*
    移动：134、135、136、137、138、139、150、151、152、157(TD)、158、159、178(新)、182、184、187、188
    联通：130、131、132、152、155、156、185、186
    电信：133、153、170、173、177、180、181、189、（1349卫通）
    总结起来就是第一位必定为1，第二位必定为3或5或8，其他位置的可以为0-9
    */
        String num = "[1][3456789]\\d{9}";//"[1]"代表第1位为数字1，"[34578]"代表第二位可以为3、4、5、7、8中的一个，"\\d{9}"代表后面是可以是0～9的数字，有9位。
        if (TextUtils.isEmpty(number)) {
            return false;
        } else {
            //matches():字符串是否在给定的正则表达式匹配
            return number.matches(num);
        }
    }

    /**
     * 验证密码格式
     */
    public static boolean isPwd(String number) {
        String str = "^(?![0-9]+$)(?![a-zA-Z]+$)[0-9A-Za-z]{6,12}$";
        Pattern p = Pattern.compile(str);
        Matcher m1 = p.matcher(number);
        return m1.matches();
    }

    /**
     * 验证输入的身份证号是否合法
     */
    public boolean isLegalId(String id) {
        if (id.toUpperCase().matches("(^[1-9]\\d{5}(18|19|([23]\\d))\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{3}[0-9Xx]$)|(^[1-9]\\d{5}\\d{2}((0[1-9])|(10|11|12))(([0-2][1-9])|10|20|30|31)\\d{2}$)")) {
            return true;
        } else {

            // UIUtils.toastMessage(R.string.verify_code_error);
            return false;
        }
    }

    //去除所有空格，换行，制表符\t\r
    public static String replaceBlank(String str) {
        String dest = "";
        if (str != null) {
            Pattern p = Pattern.compile("\\s*|\n");
            Matcher m = p.matcher(str);
            dest = m.replaceAll("");
        }
        return dest;
    }

    //去除所有空格，换行，制表符\t\r
    public static String replacehang(String str) {
        String dest = "";
        if (str != null) {
            dest = str.trim().replace("\n", "");
        }
        return dest;
    }

    public static String liveTitleFilter(String str) throws PatternSyntaxException { // 只允许字母、数字和汉字
        String regEx = "[^a-zA-Z0-9\u4E00-\u9FA5]";//正则表达式
        Pattern p = Pattern.compile(regEx);
        Matcher m = p.matcher(str);
        return m.replaceAll("").trim();
    }

    public static int calculateLength(String etstring) {
        char[] ch = etstring.toCharArray();
        int varlength = 0;
        for (int i = 0; i < ch.length; i++) {
            if ((ch[i] >= 0x2E80 && ch[i] <= 0xFE4F) || (ch[i] >= 0xA13F && ch[i] <= 0xAA40) || ch[i] >= 0x80) { // 中文字符范围0x4e00 0x9fbb
                varlength = varlength + 2;
            } else {
                varlength++;
            }

        }
        return varlength;
    }
}
