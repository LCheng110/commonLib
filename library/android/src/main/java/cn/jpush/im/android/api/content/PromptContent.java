package cn.jpush.im.android.api.content;


import com.google.gson.jpush.annotations.Expose;

import cn.jpush.im.android.api.enums.ContentType;

/**
 * 提示性消息内容。
 * 此MessageContent类型仅由sdk主动创建，上层做展示用，不能当做发送的消息体。
 */

public class PromptContent extends MessageContent {

    @Expose
    private String promptText;

    public PromptContent(String promptText) {
        this.contentType = ContentType.prompt;
        this.promptText = promptText;
    }

    /**
     * 获取提示信息
     *
     * @return 消息提示文字
     */
    public String getPromptText() {
        return promptText;
    }
}
