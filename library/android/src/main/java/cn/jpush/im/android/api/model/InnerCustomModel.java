package cn.jpush.im.android.api.model;

/**
 * Created by zhaoyuanchao on 2018/8/21.
 */

public class InnerCustomModel {
    private int isRemindBuyer;
    private String title;
    private String serviceContent;
    private String appointTime;
    private String serviceRemark;

    public int getIsRemindBuyer() {
        return isRemindBuyer;
    }

    public void setIsRemindBuyer(int isRemindBuyer) {
        this.isRemindBuyer = isRemindBuyer;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getServiceContent() {
        return serviceContent;
    }

    public void setServiceContent(String serviceContent) {
        this.serviceContent = serviceContent;
    }

    public String getAppointTime() {
        return appointTime;
    }

    public void setAppointTime(String appointTime) {
        this.appointTime = appointTime;
    }

    public String getServiceRemark() {
        return serviceRemark;
    }

    public void setServiceRemark(String serviceRemark) {
        this.serviceRemark = serviceRemark;
    }
}
