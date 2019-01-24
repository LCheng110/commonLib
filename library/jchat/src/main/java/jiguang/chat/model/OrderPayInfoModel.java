package jiguang.chat.model;

import cn.citytag.base.app.BaseModel;

/**
 * Created by zhaoyuanchao on 2018/8/21.
 */

public class OrderPayInfoModel extends BaseModel{
    private double money;                 //支付金额
    private long couponId;              //优惠券id
    private long timeRemaining;         //剩余时间
    private int orderStatus;            //订单状态

    public double getMoney() {
        return money;
    }

    public void setMoney(double money) {
        this.money = money;
    }

    public long getCouponId() {
        return couponId;
    }

    public void setCouponId(long couponId) {
        this.couponId = couponId;
    }

    public long getTimeRemaining() {
        return timeRemaining;
    }

    public void setTimeRemaining(long timeRemaining) {
        this.timeRemaining = timeRemaining;
    }

    public int getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(int orderStatus) {
        this.orderStatus = orderStatus;
    }
}
