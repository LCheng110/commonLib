package cn.jpush.im.android.api.content;

import com.google.gson.jpush.annotations.Expose;
import com.google.gson.jpush.annotations.SerializedName;

import cn.jpush.im.android.api.enums.ContentType;

/**
 * @since 1.4.0
 */
public class LocationContent extends MessageContent {
    private static final String TAG = "LocationContent";

    @Expose
    private Number latitude;
    @Expose
    private Number longitude;
    @Expose
    private Number scale;
    @Expose
    @SerializedName("label")
    private String address;

    //必须要有一个无参构造函数供json parse用
    protected LocationContent() {
        super();
    }

    public LocationContent(double latitude, double longitude, int scale, String address) {
        super();
        this.latitude = latitude;
        this.longitude = longitude;
        this.scale = scale;
        this.address = address;
        contentType = ContentType.location;
    }

    /**
     * 获取详细地址信息
     *
     * @return
     */
    public String getAddress() {
        return address;
    }

    /**
     * 获取纬度信息
     *
     * @return
     */
    public Number getLatitude() {
        return latitude;
    }

    /**
     * 获取经度信息
     *
     * @return
     */
    public Number getLongitude() {
        return longitude;
    }

    /**
     * 获取地图缩放比例信息
     *
     * @return
     */
    public Number getScale() {
        return scale;
    }
}
