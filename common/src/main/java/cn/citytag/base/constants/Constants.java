package cn.citytag.base.constants;

/**
 * Created by yangfeng01 on 2017/11/16.
 */

public interface Constants {


    //emptyProtect
    String EMPTY_PROTECT = "android";

	/** 微信appId */
	String WX_APPID = "wx4902420834013457";

    //0普通动态点赞 1普通动态评论点赞 2技能动态点赞数 3技能动态评论点赞数
    int PRAISE_TYPE_NORMAL_MOMENT = 0;
    int PRAISE_TYPE_NORMAL_MOMENT_COMMENT = 1;
    int PRAISE_TYPE_SKILL_MOMENT = 2;
    int PRAISE_TYPE_SKILL_MOMENT_COMMENT = 3;
    int REQUEST_CODE_AUTH = 100;
    int REQUEST_CODE_PERMISSION_CAMERA_STORAGE = 101;
    int REQUEST_CODE_PERMISSION_SETTING = 102;

    int REQUEST_CODE_TAKE_PHOTO = 110;
    int REQUEST_CODE_SELECT_IMAGE = 111;
    int REQUEST_CODE_SELECT_IMAGE_VIDEO = 112;
    int REQUEST_CODE_SELECT_MORE_IMAGE = 113;
    int REQUEST_CODE_SELECT_MULTI_IMAGES = 114;

    int REQUEST_CODE_SELECT_TOPIC_IMAGE = 115;

    int REQUEST_CODE_PERMISSION_ADDRESS = 102;

    int REQUEST_CODE_PUBLISH_ADDRESS = 200;

    interface Sex {
        int CONSTANT_SEX_ALL = 0;
        int CONSTANT_SEX_MALE = 1;
        int CONSTANT_SEX_FEMALE = 2;
    }

    //web scheme 跳转    1 泡泡详情  2动态详情  3地图 4 冒泡授权  5约单详情 6约单列表 7个人主页 8达人认证
    //9充值界面 type = 1余额充值  type = 2 泡泡币充值  10发布动态  type = 1 发布普通动态；type = 2 发布技能动态 11跳转到地图
    String WEB_SCHEME_TYPE_ONE = "1";
    String WEB_SCHEME_TYPE_TWO = "2";
    String WEB_SCHEME_TYPE_THREE = "3";
    String WEB_SCHEME_TYPE_FOUR = "4";
    String WEB_SCHEME_TYPE_FIVE = "5";
    String WEB_SCHEME_TYPE_SIX = "6";
    String WEB_SCHEME_TYPE_SEVEN = "7";
    String WEB_SCHEME_TYPE_EIGHT = "8";
    String WEB_SCHEME_TYPE_NINE = "9";
    String WEB_SCHEME_TYPE_TEN = "10";
    String WEB_SCHEME_TYPE_ELEVEN = "11";

}
