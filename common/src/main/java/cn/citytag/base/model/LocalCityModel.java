package cn.citytag.base.model;

import java.util.List;

import cn.citytag.base.widget.pickview.model.IPickerViewData;

/**
 * Created by yuhuizhong on 2018/9/12.
 */

public class LocalCityModel implements IPickerViewData {
    /**
     * province : 北京
     * city_list : ["北京"]
     */

    private String province;
    private List<String> city_list;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public List<String> getCity_list() {
        return city_list;
    }

    public void setCity_list(List<String> city_list) {
        this.city_list = city_list;
    }

    @Override
    public String getPickerViewText() {
        return this.province;
    }
}
