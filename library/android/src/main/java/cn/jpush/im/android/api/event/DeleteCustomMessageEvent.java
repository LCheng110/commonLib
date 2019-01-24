package cn.jpush.im.android.api.event;

import cn.jpush.im.android.api.model.Message;

/**
 * Created by zhaoyuanchao on 2018/8/21.
 */

public class DeleteCustomMessageEvent {
    private Message msg;

    public DeleteCustomMessageEvent(Message msg) {
        this.msg = msg;
    }

    public Message getMsg() {
        return msg;
    }

    public void setMsg(Message msg) {
        this.msg = msg;
    }
}
