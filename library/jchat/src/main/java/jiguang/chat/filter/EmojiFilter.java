package jiguang.chat.filter;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.widget.EditText;

import com.sj.emoji.EmojiDisplay;
import com.sj.emoji.EmojiDisplayListener;
import com.sj.emoji.EmojiSpan;

import java.util.regex.Matcher;

import jiguang.chat.utils.keyboard.interfaces.EmoticonFilter;
import jiguang.chat.utils.keyboard.utils.EmoticonsKeyboardUtils;


public class EmojiFilter extends EmoticonFilter {

    private int emojiSize = -1;

    @Override
    public void filter(EditText editText, CharSequence text, int start, int lengthBefore, int lengthAfter) {
        emojiSize = emojiSize == -1 ? EmoticonsKeyboardUtils.getFontHeight(editText) : emojiSize;
        clearSpan(editText.getText(), start, text.toString().length());
        Matcher m = EmojiDisplay.getMatcher(text.toString().substring(start, text.toString().length()));
        if (m != null) {
            while (m.find()) {
                String emojiHex = Integer.toHexString(Character.codePointAt(m.group(), 0));
                EmojiDisplay.emojiDisplay(editText.getContext(), editText.getText(), emojiHex, emojiSize, start + m.start(), start + m.end());
            }
        }
    }

	public static Spannable spannableFilter(Context context, Spannable spannable, CharSequence text, int fontSize, EmojiDisplayListener emojiDisplayListener) {
		Matcher m = EmojiDisplay.getMatcher(text);
		if (m != null) {
			while(m.find()) {
				String emojiHex = Integer.toHexString(Character.codePointAt(m.group(), 0));
				if (emojiDisplayListener == null) {
					emojiDisplay(context, spannable, emojiHex, fontSize, m.start(), m.end());
				} else {
					emojiDisplayListener.onEmojiDisplay(context, spannable, emojiHex, fontSize, m.start(), m.end());
				}
			}
		}

		return spannable;
	}

	public static void emojiDisplay(Context context, Spannable spannable, String emojiHex, int fontSize, int start, int end) {
		Drawable drawable = getDrawable(context, "emoji_0x" + emojiHex);
		if (drawable != null) {
			int itemHeight;
			int itemWidth;
			if (fontSize == -1) {
				itemHeight = drawable.getIntrinsicHeight();
				itemWidth = drawable.getIntrinsicWidth();
			} else {
				itemHeight = fontSize;
				itemWidth = fontSize;
			}

			drawable.setBounds(0, 0, itemHeight, itemWidth);
			EmojiSpan imageSpan = new EmojiSpan(drawable);
			spannable.setSpan(imageSpan, start, end, 17);
		}

	}

    private void clearSpan(Spannable spannable, int start, int end) {
        if (start == end) {
            return;
        }
        EmojiSpan[] oldSpans = spannable.getSpans(start, end, EmojiSpan.class);
        for (int i = 0; i < oldSpans.length; i++) {
            spannable.removeSpan(oldSpans[i]);
        }
    }
}
