package cn.citytag.base.model;

/**
 * 作者：lnx. on 2018/12/7 16:32
 */
public class JsBridgeModel {


    /**
     * id : gh_0efddaf13254  小程序id
     * path : ald006         小程序 path
     *  packageName: 'com.alddin.asj'  app包名
     *  url        浏览器跳转url
     *  schemeUrl  跳转协议
     */

    private String id;
    private String path;
    private String packageName ;
    private String schemeUrl;
    private String url ;


    public String getSchemeUrl() {
        return schemeUrl;
    }

    public void setSchemeUrl(String schemeUrl) {
        this.schemeUrl = schemeUrl;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
