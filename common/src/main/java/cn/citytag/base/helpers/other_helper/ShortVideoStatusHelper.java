package cn.citytag.base.helpers.other_helper;

import com.aliyun.svideo.sdk.external.struct.common.AliyunVideoParam;
import com.google.gson.Gson;

import cn.citytag.base.model.ShortVideoPublishModel;

/**
 * 作者：lnx. on 2018/12/26 16:42
 */
public class ShortVideoStatusHelper {

    //短视频状态
    public static final String VIDEO_STATUS = "video_status";
    public static final int RECORD_STATE = 0;
    public static final int CROP_STATE = 2;
    public static final int EDITOR_STATE = 3;
    //发布的默认状态 即未发到阿里云 也未发到自己的后台
    public static final int PUBLISH_DEFAULT_STATE = 4;
    //用户点击了发布按钮状态 用于判断文案提示
    public static final int PUBLISH_STATE_BTN_PRESS = 5;
    //发布到阿里是成功的 发布到服务器是失败的
    public static final int PUBLISH_ALI_SUCCESS = 6;
    //都成功了
    public static final int PUBLISH_ALL_SUCCESS = 7;

    private static boolean isCrash ;


    public static final String SHORT_VIDEO_DURATION = "short_video_duration";
    public static final String SHORT_VIDEO_HEIGHT = "short_video_height";
    public static final String SHORT_VIDEO_WIDTH = "short_video_width";
    public static final String SHORT_VIDEO_MUSIC_ID = "short_video_music_id";
    public static final String SHORT_VIDEO_CATEGORY_ID = "short_video_category_id";
    public static final String SHORT_VIDEO_THEME_ID = "short_video_theme_id";
    public static final String SHORT_VIDEO_INTRODUCE = "short_video_introduce";
    public static final String SHORT_VIDEO_LOCATION = "short_video_location";
    public static final String SHORT_VIDEO_LONGITUDE = "short_video_longitude";
    public static final String SHORT_VIDEO_LATITUDE = "short_video_latutude";

    public static final String SHORT_VIDEO_COVER_URL = "short_video_cover_url";
    public static final String SHORT_VIDEO_VID = "short_video_vid";
    public static final String SHORT_VIDEO_VIDEO_URL = "short_video_video_url";
    public static final String SHORT_VIDEO_ALI_PARAMS = "short_video_ali_params";
    public static final String SHORT_VIDEO_PROJECT_JSON = "short_video_project_json";
    public static final String SHORT_VIDEO_LOCAL_PATH = "short_video_local_path";



    public static void saveParam(int status){

        SPUtil.setInt(VIDEO_STATUS,status);

    }


    public static int getParam(){

        int anInt = SPUtil.getInt(VIDEO_STATUS);

        return anInt;
    }


    public static boolean isIsCrash() {
        return isCrash;
    }

    public static void setIsCrash(boolean isCrash) {
        ShortVideoStatusHelper.isCrash = isCrash;
    }

    public static void savePublishMsg(ShortVideoPublishModel model){

        SPUtil.setLong(SHORT_VIDEO_DURATION,model.getDuration()); ;
        SPUtil.setFloat(SHORT_VIDEO_HEIGHT,(float) model.getHeight()); ;
        SPUtil.setFloat(SHORT_VIDEO_WIDTH,(float) model.getWidth());

        SPUtil.setLong(SHORT_VIDEO_MUSIC_ID,model.getMusicId());
        SPUtil.setLong(SHORT_VIDEO_CATEGORY_ID,model.getCategoryId());
        SPUtil.setLong(SHORT_VIDEO_THEME_ID ,model.getThemeId());
        SPUtil.setString(SHORT_VIDEO_INTRODUCE ,model.getIntroduce());

        SPUtil.setString(SHORT_VIDEO_LOCATION,model.getLocation());
        SPUtil.setFloat(SHORT_VIDEO_LONGITUDE ,(long)model.getLongitude());
        SPUtil.setFloat(SHORT_VIDEO_LATITUDE ,(long)model.getLatitude());

        SPUtil.setString(SHORT_VIDEO_COVER_URL ,model.getCoverUrl());
        SPUtil.setString(SHORT_VIDEO_VID,model.getvId());
        SPUtil.setString(SHORT_VIDEO_VIDEO_URL,model.getVideoUrl());
        SPUtil.setString(SHORT_VIDEO_PROJECT_JSON,model.getProjectJson());
        SPUtil.setString(SHORT_VIDEO_LOCAL_PATH,model.getVideoLocalPath());
        Gson gson = new Gson();
        String videoParams = gson.toJson(model.getVideoParam());
        SPUtil.setString(SHORT_VIDEO_ALI_PARAMS,videoParams);

    }

    public static ShortVideoPublishModel getPublishMsg(){

        ShortVideoPublishModel videoPublishModel = new ShortVideoPublishModel();
        videoPublishModel.setDuration((int) SPUtil.getLong(SHORT_VIDEO_DURATION));
        videoPublishModel.setHeight(SPUtil.getFloat(SHORT_VIDEO_HEIGHT));
        videoPublishModel.setWidth(SPUtil.getFloat(SHORT_VIDEO_WIDTH));

        videoPublishModel.setMusicId(SPUtil.getLong(SHORT_VIDEO_MUSIC_ID));
        videoPublishModel.setCategoryId(SPUtil.getLong(SHORT_VIDEO_CATEGORY_ID));
        videoPublishModel.setThemeId(SPUtil.getLong(SHORT_VIDEO_THEME_ID));
        videoPublishModel.setIntroduce(SPUtil.getString(SHORT_VIDEO_INTRODUCE));

        videoPublishModel.setLocation(SPUtil.getString(SHORT_VIDEO_LOCATION));
        videoPublishModel.setLongitude(SPUtil.getFloat(SHORT_VIDEO_LONGITUDE));
        videoPublishModel.setLatitude(SPUtil.getFloat(SHORT_VIDEO_LATITUDE));

        videoPublishModel.setCoverUrl(SPUtil.getString(SHORT_VIDEO_COVER_URL));
        videoPublishModel.setvId(SPUtil.getString(SHORT_VIDEO_VID));
        videoPublishModel.setVideoUrl(SPUtil.getString(SHORT_VIDEO_VIDEO_URL));
        videoPublishModel.setProjectJson(SPUtil.getString(SHORT_VIDEO_PROJECT_JSON));
        videoPublishModel.setVideoLocalPath(SPUtil.getString(SHORT_VIDEO_LOCAL_PATH));

        Gson gson = new Gson();
        String paramsString = SPUtil.getString(SHORT_VIDEO_ALI_PARAMS);
        AliyunVideoParam videoParams = gson.fromJson(paramsString,AliyunVideoParam.class);
        videoPublishModel.setVideoParam(videoParams);


        return videoPublishModel;
    }



    public static void clearPublishModel(){

        SPUtil.setLong(SHORT_VIDEO_DURATION,0); ;
        SPUtil.setFloat(SHORT_VIDEO_HEIGHT,0); ;
        SPUtil.setFloat(SHORT_VIDEO_WIDTH ,0);

        SPUtil.setLong(SHORT_VIDEO_MUSIC_ID,0);
        SPUtil.setLong(SHORT_VIDEO_CATEGORY_ID,0);
        SPUtil.setLong(SHORT_VIDEO_THEME_ID ,0);
        SPUtil.setString(SHORT_VIDEO_INTRODUCE ,"");

        SPUtil.setString(SHORT_VIDEO_LOCATION,"");
        SPUtil.setFloat(SHORT_VIDEO_LONGITUDE ,0);
        SPUtil.setFloat(SHORT_VIDEO_LATITUDE ,0);

        SPUtil.setString(SHORT_VIDEO_COVER_URL ,"");
        SPUtil.setString(SHORT_VIDEO_VID,"");
        SPUtil.setString(SHORT_VIDEO_VIDEO_URL,"");
        SPUtil.setString(SHORT_VIDEO_PROJECT_JSON,"");
        SPUtil.setString(SHORT_VIDEO_ALI_PARAMS,"");
        SPUtil.setString(SHORT_VIDEO_LOCAL_PATH,"");

        setIsCrash(false);
    }



}
