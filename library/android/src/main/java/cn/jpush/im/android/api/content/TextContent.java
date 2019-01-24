package cn.jpush.im.android.api.content;

import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.api.enums.ContentType;

public class TextContent extends MessageContent {

    private static final String TAG = "TextContent";

    @Expose
    private String text;

    protected TextContent(){
        super();
    }

    public TextContent(String text) {
        super();
        this.text = text;
        this.contentType = ContentType.text;
    }

    /**
     * 获取消息的文本内容
     *
     * @return 消息文本内容
     */
    public String getText() {
        return text;
    }

}
