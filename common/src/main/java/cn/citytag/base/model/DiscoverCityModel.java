package cn.citytag.base.model;

/**
 * 作者：Lgc
 * 创建时间：2018/11/29
 * 更改时间：2018/11/29
 */
public class DiscoverCityModel {

    private int code;
    private CityModel data;
    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public CityModel getData() {
        return data;
    }

    public void setData(CityModel data) {
        this.data = data;
    }

    public static class CityModel {

        public CityModel(String cityName, String msg) {
            this.cityName = cityName;
            this.msg = msg;
        }

        private String cityName;
        private String msg;

        public String getCityName() {
            return cityName;
        }

        public void setCityName(String cityName) {
            this.cityName = cityName;
        }

        public String getMsg() {
            return msg;
        }

        public void setMsg(String msg) {
            this.msg = msg;
        }
    }

}
