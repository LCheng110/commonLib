package cn.citytag.base.callback;

import java.io.Serializable;

/**
 * 作者：lnx. on 2018/12/20 17:12
 */
public interface IAddressPickCallBack extends Serializable{


    void sendAddress(String location);

}
