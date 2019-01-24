package cn.citytag.base.model;

import com.aliyun.svideo.sdk.external.struct.common.AliyunVideoParam;

/**
 * 作者：lnx. on 2018/12/20 10:59
 */
public class ShortVideoPublishModel {

    private String vId ; // oss 视频id
    private String videoUrl ; //视频url
    private String coverUrl ; //封面url

    private double height;
    private double width ;

    private long duration ; //时长（单位：s）
    private long musicId;  //背景音乐id
    private long themeId ; //主题id

    private String introduce; //描述
    private long categoryId ; //分类id

    private double longitude ; //经度
    private double latitude ;  //纬度

    private String location ; //地址


    private AliyunVideoParam videoParam;

    private String projectJson;

    private String videoLocalPath;


    public String getvId() {
        return vId;
    }

    public void setvId(String vId) {
        this.vId = vId;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public String getCoverUrl() {
        return coverUrl;
    }

    public void setCoverUrl(String coverUrl) {
        this.coverUrl = coverUrl;
    }

    public double getHeight() {
        return height;
    }

    public void setHeight(double height) {
        this.height = height;
    }

    public double getWidth() {
        return width;
    }

    public void setWidth(double width) {
        this.width = width;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getMusicId() {
        return musicId;
    }

    public void setMusicId(long musicId) {
        this.musicId = musicId;
    }

    public long getThemeId() {
        return themeId;
    }

    public void setThemeId(long themeId) {
        this.themeId = themeId;
    }

    public String getIntroduce() {
        return introduce;
    }

    public void setIntroduce(String introduce) {
        this.introduce = introduce;
    }

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }



    public AliyunVideoParam getVideoParam() {
        return videoParam;
    }

    public void setVideoParam(AliyunVideoParam videoParam) {
        this.videoParam = videoParam;
    }

    public String getProjectJson() {
        return projectJson;
    }

    public void setProjectJson(String projectJson) {
        this.projectJson = projectJson;
    }

    public String getVideoLocalPath() {
        return videoLocalPath;
    }

    public void setVideoLocalPath(String videoLocalPath) {
        this.videoLocalPath = videoLocalPath;
    }
}
