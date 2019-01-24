package cn.citytag.base.event;

public class RTCEvent extends BaseEvent {
    public static final int TYPE_REQUEST = 0;       //发起方发起请求
    public static final int TYPE_CANCEL = 1;        //发起方取消请求
    public static final int TYPE_ACCEPT = 2;        //接收方接受语音请求
    public static final int TYPE_BUSY = 3;          //对方忙

    // 0 对方发起的请求   1：对方挂断的请求 2：接受请求
    private int type_RTC;
    //接收到事件后 将时间戳取出来  传入确认或者接受的自定义消息里面
    private String timestamp;
    // 定义的MSG
    private String msg;

    public RTCEvent(int type, int type_RTC, String timestamp, String msg) {
        super(type);
        this.type_RTC = type_RTC;
        this.timestamp = timestamp;
        this.msg = msg;
    }

    @Override
    public int getType() {
        return type_RTC;
    }

    @Override
    public void setType(int type) {
        this.type_RTC = type;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
