package cn.jpush.im.android.api.model;


import java.io.Serializable;

/**
 * Created by zhaoyuanchao on 2018/11/14.
 */

public  class UserStatus implements Serializable{
    private static final String TAG = UserStatus.class.getSimpleName();
    private String code;
    private String msg;
    private Data data;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Data getData() {
        return data;
    }

    public void setData(Data data) {
        this.data = data;
    }

    public class Data implements Serializable{
        private int result;
        private String desc;

        public int getResult() {
            return result;
        }

        public void setResult(int result) {
            this.result = result;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }
    }
}
