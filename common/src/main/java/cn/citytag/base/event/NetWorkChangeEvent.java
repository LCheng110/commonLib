package cn.citytag.base.event;

/**
 * Created by baoyiwei on 2018/1/12.
 */

public class NetWorkChangeEvent extends BaseEvent{

    /*是否有网络*/
    private boolean isHasNet;

    public boolean isHasNet() {
        return isHasNet;
    }

    public void setHasNet(boolean hasNet) {
        isHasNet = hasNet;
    }

    public NetWorkChangeEvent(boolean isHasNet){
        this.isHasNet = isHasNet;
    }
}
