package cn.jpush.im.android.api.event;

/**
 * Created by zhaoyuanchao on 2018/8/21.
 */

public class IMOderMessageEvent {
    private long orderId;

    public long getOrderId() {
        return orderId;
    }

    public void setOrderId(long orderId) {
        this.orderId = orderId;
    }

    public IMOderMessageEvent(long orderId) {
        this.orderId = orderId;
    }
}
