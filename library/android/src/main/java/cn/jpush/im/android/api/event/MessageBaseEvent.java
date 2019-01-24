package cn.jpush.im.android.api.event;


import cn.jpush.im.android.api.model.Message;

public abstract class MessageBaseEvent {
    private Message msg;

    private int responseCode;

    private String responseDesc;

    protected MessageBaseEvent(int responseCode, String responseDesc, Message msg) {
        this.responseCode = responseCode;
        this.responseDesc = responseDesc;
        this.msg = msg;
    }

    /**
     * 获取事件的响应码，0表示成功，其他表示失败，具体含义见错误码定义文档。
     *
     * @return
     */
    public int getResponseCode() {
        return responseCode;
    }

    /**
     * 获取事件响应描述，成功返回‘ok’,其他情况描述了具体错误的原因。
     *
     * @return
     */
    public String getResponseDesc() {
        return responseDesc;
    }

    /**
     * 获取事件中所包含的message对象
     *
     * @return
     */
    public Message getMessage() {
        return msg;
    }
}
