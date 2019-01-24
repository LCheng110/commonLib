package cn.citytag.base.model;

import cn.citytag.base.widget.jsBridge.CallBackFunction;

/**
 * 作者：lnx. on 2018/12/7 16:26
 */
public class WebviewHandlerModel {

    private String data ;
    private CallBackFunction function;

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public CallBackFunction getFunction() {
        return function;
    }

    public void setFunction(CallBackFunction function) {
        this.function = function;
    }
}
