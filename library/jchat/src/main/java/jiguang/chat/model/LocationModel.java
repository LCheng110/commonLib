package jiguang.chat.model;

import java.io.Serializable;

/**
 * Created by zhaoyuanchao on 2018/8/17.
 */

public class LocationModel implements Serializable {
    private double lat;
    private double lng;

    public double getLat() {
        return lat;
    }

    public void setLat(double lat) {
        this.lat = lat;
    }

    public double getLng() {
        return lng;
    }

    public void setLng(double lng) {
        this.lng = lng;
    }
}
