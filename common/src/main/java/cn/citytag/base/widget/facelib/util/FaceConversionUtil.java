package cn.citytag.base.widget.facelib.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.Log;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import cn.citytag.base.R;
import cn.citytag.base.utils.StringUtils;
import cn.citytag.base.widget.facelib.model.ChatFaceModel;

import static cn.citytag.base.widget.facelib.util.FileUtil.getEmojiFile;


/**
 * 表情转换工具
 */
public class FaceConversionUtil {

    /**
     * 每一页表情的个数
     */
    private static final int pageSize = 20;

    /**
     * 缓存的bitmap，防止内存溢出
     */
    private static SparseArray<Bitmap> mBitmap = new SparseArray<>();

    private static SparseArray<ImageSpan> mImageSpan = new SparseArray<>();

    private static FaceConversionUtil mFaceConversionUtil;

    /**
     * 保存于内存中的表情HashMap
     */
    private HashMap<String, String> emojiMap = new HashMap<>();

    /**
     * 保存于内存中的表情集合
     */
    private List<ChatFaceModel> emojis = new ArrayList<>();

    /**
     * 表情分页的结果集合
     */
    public List<List<ChatFaceModel>> emojiLists = new ArrayList<>();

    private FaceConversionUtil() {

    }

    public static FaceConversionUtil getInstace() {
        if (mFaceConversionUtil == null) {
            mFaceConversionUtil = new FaceConversionUtil();
        }
        return mFaceConversionUtil;
    }

    /**
     * 得到一个SpanableString对象，通过传入的字符串,并进行正则判断
     */
    public SpannableString getExpressionString(Context context, String str) {
        SpannableString spannableString = new SpannableString(str);

        /**
         *  简单点可以是  \\[(.+)\\]
         * 	因为[ 是正则里的关键字（我只能这么说了）,所以要用\\[ 接着你会看到 [^\]\[] 这里[^] 表示不包含里面的
         * 	字符,你没猜错,[****]表示包含里面的某个字符,这里是不包含] 和 [ ,是的,要用\\[ 或者 \\],接着的{2,3}
         * 	表示这个条件满足几次,最后以]结尾。整个正则的含义是,以[ 开头, 然后接着的字符不能是[或者]且满足2或者3次
         * 	最后以]结尾!
         * */
        String regex = "\\[[^\\]\\[]{2,20}\\]";
        // 通过传入的正则表达式来生成一个pattern
        Pattern sinaPatten = Pattern.compile(regex);
        try {
            dealExpression(context, spannableString, sinaPatten);
        } catch (Exception e) {
            Log.e("dealExpression", e.getMessage());
        }
        return spannableString;
    }


    /**
     * 添加表情  输入框~
     */
    public SpannableString addFace(Context context, int imgId,
                                   String spannableString) {
        if (TextUtils.isEmpty(spannableString)) {
            return null;
        }

        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(),
                imgId);
        bitmap = Bitmap.createScaledBitmap(bitmap, 56, 56, true);
        ImageSpan imageSpan = new ImageSpan(context, bitmap);

        SpannableString spannable = new SpannableString(spannableString);
        spannable.setSpan(imageSpan, 0, spannableString.length(),
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    @SuppressWarnings("deprecation")
    /**
     * 对spanableString进行正则判断，如果符合要求，则以表情图片代替  聊天内容~
     *
     * @param context
     * @param spannableString
     * @param patten
     * @param start
     * @throws Exception
     */
    private void dealExpression(Context context,
                                SpannableString spannableString, Pattern patten) {
        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            String value = emojiMap.get(key);
            value = StringUtils.isEmpty(value) ? emojiMap.get("defaultIconName") : value;
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            int resId = context.getResources().getIdentifier(value, "drawable",
                    context.getPackageName());
            if (resId != 0) {

                // 计算该图片名字的长度，也就是要替换的字符串的长度
                int end = matcher.start() + key.length();

                // 通过图片资源id来得到bitmap，用一个ImageSpan来包装
                //   ImageSpan span = new ImageSpan(context, getBitmap(resId, context));
                ImageSpan span = getImageSpan(context, resId);
                spannableString.setSpan(span, matcher.start(), end,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
    }

    /**
     * 根据表情编码的值获取到对应图片的resId
     *
     * @param context
     * @param str
     * @return
     */
    public int getResIdByFaceCode(Context context, String str) {
        SpannableString spannableString = new SpannableString(str);
        String regex = "\\[[^\\]\\[]{2,20}\\]";
        // 通过传入的正则表达式来生成一个pattern
        Pattern patten = Pattern.compile(regex);

        Matcher matcher = patten.matcher(spannableString);
        while (matcher.find()) {
            String key = matcher.group();
            String value = emojiMap.get(key);
            value = StringUtils.isEmpty(value) ? emojiMap.get("defaultIconName") : value;
            if (StringUtils.isEmpty(value)) {
                continue;
            }
            int resId = context.getResources().getIdentifier(value, "drawable",
                    context.getPackageName());
            return resId;
        }
        return 0;
    }

    private ImageSpan getImageSpan(Context context, int id) {
        ImageSpan imageSpan = mImageSpan.get(id);
        if (imageSpan == null) {
            imageSpan = new ImageSpan(context, getBitmap(id, context));
        }
        return imageSpan;
    }

    /**
     * 缓存bitmap，防止内存溢出
     *
     * @param id      resourcesId
     * @param context context
     * @return bitmap
     */
    private Bitmap getBitmap(int id, Context context) {

        Bitmap bitmap = mBitmap.get(id);
        if (bitmap == null) {

            bitmap = BitmapFactory.decodeResource(
                    context.getResources(), id);
            bitmap = Bitmap.createScaledBitmap(bitmap, 56, 56, true);
            mBitmap.put(id, bitmap);
        }
        return bitmap;
    }

    public void getFileText(Context context, String defaultIconName) {
        ParseData(getEmojiFile(context), context, defaultIconName, false);
        ParseData(getEmojiFile(context, "emoji_mood"), context, defaultIconName, true);
    }

    /**
     * 根据assets下的文件转换成表情model list
     *
     * @param context
     * @param fileName
     * @return
     */
    public List<ChatFaceModel> getChatFaceModelByFile(Context context, String fileName) {
        List<ChatFaceModel> list = new ArrayList<>();
        List<String> lineList = getEmojiFile(context, fileName);
        ChatFaceModel faceModel;
        for (String line : lineList) {
            String[] text = line.split(",");
            String imageName = text[0].substring(0, text[0].lastIndexOf("."));
            int resID = context.getResources().getIdentifier(imageName,
                    "drawable", context.getPackageName());

            if (resID != 0) {
                faceModel = new ChatFaceModel();
                faceModel.setId(resID);
                faceModel.setCharacter(text[1]);
                faceModel.setFaceName(imageName);
                list.add(faceModel);
            }
        }
        return list;
    }

    /**
     * 解析字符
     */
    private void ParseData(List<String> data, Context context, String defaultIconName, boolean isMood) {
        if (data == null) {
            return;
        }
        ChatFaceModel emojEentry;
        try {
            for (String str : data) {
                String[] text = str.split(",");
                String fileName = text[0]
                        .substring(0, text[0].lastIndexOf("."));
                emojiMap.put(text[1], fileName);
                int resID = context.getResources().getIdentifier(fileName,
                        "drawable", context.getPackageName());

                if (resID != 0) {
                    emojEentry = new ChatFaceModel();
                    emojEentry.setId(resID);
                    emojEentry.setCharacter(text[1]);
                    emojEentry.setFaceName(fileName);
                    emojis.add(emojEentry);
                }
            }
            emojiMap.put("defaultIconName", defaultIconName);
            int size = emojis.size();
            int pageCount = size / pageSize + (size % pageSize == 0 ? 0 : 1);

            for (int i = 0; i < pageCount; i++) {
                if(!isMood){
                    emojiLists.add(getData(i));
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取分页数据
     */
    private List<ChatFaceModel> getData(int page) {
        int startIndex = page * pageSize;
        int endIndex = startIndex + pageSize;

        if (endIndex > emojis.size()) {
            endIndex = emojis.size();
        }

        List<ChatFaceModel> list = new ArrayList<>();
        list.addAll(emojis.subList(startIndex, endIndex));
        if (list.size() < pageSize) {
            for (int i = list.size(); i < pageSize; i++) {
                ChatFaceModel object = new ChatFaceModel();
                list.add(object);
            }
        }
        if (list.size() == pageSize) {
            ChatFaceModel object = new ChatFaceModel();
            //那个删除按钮  。。。。
            object.setId(R.drawable.face_del_ico_dafeult);
            list.add(object);
        }
        return list;
    }
}