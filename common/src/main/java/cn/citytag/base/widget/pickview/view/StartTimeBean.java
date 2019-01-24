package cn.citytag.base.widget.pickview.view;

import cn.citytag.base.widget.pickview.utils.StartTimeBeanUtil;

/**
 * Created by liguangchun on 2018/1/31.
 */

public class StartTimeBean {

    private int index = StartTimeBeanUtil.DEFAULT_INDEX;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public String getHours() {
        return hours;
    }

    public void setHours(String hours) {
        this.hours = hours;
    }

    public String getMinutes() {
        return minutes;
    }

    public void setMinutes(String minutes) {
        this.minutes = minutes;
    }

    private String hours;
    private String minutes;
}
