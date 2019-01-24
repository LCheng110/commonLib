package cn.citytag.base.constants;

/**
 * Created by yangfeng01 on 2017/11/1.
 * <p>
 * intent.putExtra("name", "value");
 * intent.putExtra("age", 23);
 */

public interface ExtraName {

    String EXTRA_FROM = "extra_from";
    String EXTRA_SEARCH_FROM = "extra_from";

    String EXTRA_USER_NAME = "extra_user_name";
    String EXTRA_USER_PWD = "extra_user_pwd";
    String EXTRA_IMAGE_PATH = "extra_image_path";
    String EXTRA_MEDIA_INFO = "extra_media_info";
    String EXTRA_PREVIEW_IMAGES = "extra_preview_images";
    String EXTRA_PREVIEW_THUMB_IMAGES = "extra_preview_thumb_images";
    String EXTRA_SELECT_POSITION = "extra_select_position";
    String EXTRA_USER_MODEL = "extra_user_model";    // UserModel
    String EXTRA_THIRD_LOGIN_MODEL = "extra_third_login_model";    // ThirdLoginModel
    String EXTRA_SEARCH_TYPE = "extra_search_type";    // 搜索类型：泡泡、地址
    String EXTRA_BUBBLE_ID = "extra_bubble_id";        // 泡泡活动id
    String EXTRA_BEHAVIOR_STATE = "extra_behavior_state";    // 泡泡详情展现状态
    String EXTRA_BUBBLE_COMMENT_COUNT = "extra_bubble_comment_count";    // 泡泡评论数
    String EXTRA_BUBBLE_TYPE = "extra_bubble_map_type";    // 泡泡地图类型
    String EXTRA_BUBBLE_MODEL = "extra_bubble_model";    // 泡泡model
    String EXTRA_BUBBLE_APPLY_TYEPE = "extra_bubble_apply_type";    // 报名/选人
    String EXTRA_BUBBLE_NEED_COUNT = "extra_bubble_need_count";        // 招募人数
    String EXTRA_BUBBLE_TITLE = "extra_bubble_title";

    String INDEX = "index";

    String EXTRA_FILE_PATH = "extra_file_path";    // 文件，图片或视频的本地路径
    String EXTRA_VIDEO_TYPE = "extra_video_type";    // 消失类型
    String EXTRA_MOMENT_ID = "extra_moment_item";    // 动态id
    String EXTRA_MOMENT_DISTANCE = "extra_moment_distance";    // 动态距离
    String EXTRA_MOMENT_TYPE = "extra_moment_type"; //动态类型
    String EXTRA_VIDEO_PATH = "extra_video_path";    // 视频路径
    String EXTRA_COMMENT_ENTER_TYPE = "extra_video_enter_type";    // 视频路径
    String EXTRA_COMMENT_HEAD = "extra_comment_head";    // 评论头部对象

    String EXTRA_PREVIEW_SHOW_DELETE = "extra_preview_show_delete";        // 是否显示删除
    String EXTRA_PREVIEW_SHOW_DOWNLOAD = "extra_preview_show_download";    // 是否显示下载

    //弹弹
    String EXTRA_TANTAN_FROM = "extra_tantan_from";
    String EXTRA_OTHER_USERID = "extra_other_userid";
    String EXTRA_OTHER_USERNAME = "extra_other_username";
    String EXTRA_OTHER_TYPE = "extra_other_type";

    String EXTRA_WEBVIEW = "extra_webview";

    String EXTRA_TYPE = "extra_type";
    String EXTRA_MAIN_TAB = "extra_main_tab";    // 主页的第几个tab
    String EXTRA_MAIN_NAV_TYPE = "extra_main_nav_type";    // 先进入MainActivity然后跳转其他页面
    String EXTRA_MAIN_DEATILID = "extra_main_detailid";    // 先进入MainActivity然后跳转其他详情页面的id
    String EXTRA_BOOLEAN = "extra_boolean";
    String EXTRA_CROP = "extra_crop";
    String EXTRA_CITY_MODEL = "extra_city_model";    // 定位城市model


    //动态举报
    int EXTRA_REPORT_MOMENT = 2;
    int EXTRA_REPORT_GROUP = 0;
    int EXTRA_REPORT_PRIVATE = 0;
    int EXTRA_REPORT_OTHER = 4;
    String EXTRA_CONVERSATION_TITLE = "title";
    String EXTRA_CONVERSATION_TARGET_ID = "targetId";

    /*自己动态*/
    int EXTRA_SELF_MOMENT = 2;

    //泡泡圈
    int EXTRA_OTHER_INFO = 2;

    //视频列表
    String EXTRA_VIDEO_LIST_NOTIFICATION_VIDEO_ID = "video_id";
    String EXTRA_VIDEO_LIST_NOTIFICATION_VIDEO_BE_USER_ID = "beUserId";
    String EXTRA_VIDEO_LIST_NOTIFICATION_VIDEO_TYPE = "type";
    String EXTRA_VIDEO_LIST_NOTIFICATION_VIDEO_DATA = "data";
    String EXTRA_VIDEO_LIST_NOTIFICATION_VIDEO_IS_DETAILS = "is_details";
    //群组添加成员的小灰条消息通知
    String EXTRA_GROUP_NOTIFICATION_TITLE = "notification_title";
    String EXTRA_GROUP_NOTIFICATION_TEXT = "notification_text";
    String EXTRA_GROUP_NOTIFICATION_ID = "id";
    String EXTRA_GROUP_NOTIFICATION_TARGET_ID = "target_id";

    String EXTRA_NOTE_NAME = "note_name";
    String EXTRA_NOTE_USERID = "note_userid";
    String EXTRA_NOTE_USERNAME = "note_username";

    //R_IM 融云推送 extra
    String EXTRA_R_IM_PUSH = "r_push";

    String EXTRA_IS_BUBBLE_SET_GROUP = "extra_is_bubble_set_group";
    /*是否是泡泡好友内部分享*/
    String EXTRA_IS_MAO_PAO_INSIDE_SHARE = "extra_is_inside_share";
    String EXTRA_IS_MAO_PAO_INSIDE_SHARE_OBJECT = "extra_is_inside_share_object";
    String EXTRA_IS_OTHER_FRIENDS = "extra_is_other_frineds";

    //公共的int值状态变量类型
    String EXTRA_COMMON_VAR = "extra_common_var";
    int EXTRA_COMMON_VAR0 = 0;
    int EXTRA_COMMON_VAR1 = 1;
    int EXTRA_COMMON_VAR2 = 2;
    int EXTRA_COMMON_VAR3 = 3;
    int EXTRA_COMMON_VAR4 = 4;//map地图动态详情评论的点击类型
    String EXTRA_REQUEST_CODE = "extra_request_code";
    //编辑类型
    String EDIT_TYPE = "edit_type";
    String EDIT_CONTENT = "edit_content";


    //他人泡泡
    String OTHERS_PAOPAO = "others_paopao";

    String EDIT_TITLE = "edit_title";
    //
    String PROVINCE_ID = "province_id";
    String PROVINCE_NAME = "province_name";

    String EXTRA_INTENT_ADDRESS = "intent_address";
    String EXTRA_INTENT_LNG = "extra_intent_lng";
    String EXTRA_INTENT_LAT = "extra_intent_lat";
    //
    String PHONE = "phone";
    String PHONE_CODE = "phone_code";

    String USER_FANS = "user_fans";
    String USER_FOCUS = "user_focus";
    String USER_FRIEND = "user_focus";
    String USER_FANS_AMOUNT = "user_fans_amount";
    String USER_FANS_TYPE = "user_fans_type";

    String TAG_LAMBL = "tag_lambl";
    String TAG_CHOOSE = "tag_choose";
    String TAG_MODIFY = "tag_modify";

    //广场跳转到publish
    String TAG_FROM_SQUARE = "tag_from_square";

    String EXTRA_LNG = "extra_lng";
    String EXTRA_LAT = "extra_lat";
    String EXTRA_POI = "extra_poi";
    String EXTRA_ADS = "extra_address";
    String EXTRA_IS_CHAT_LOCATION = "extra_is_chat_location";
    String EXTRA_IS_PIN_LOCATION = "extra_is_pin_location";

    String EXTRA_SKILL_ID = "extra_skill_id";
    String EXTRA_AUTHEN_ID = "extra_authenticationid";
    String EXTRA_SKILL_NAME = "extra_skill_name";
    String EXTRA_SKILL_MODEL = "extra_skill_model";

    String EXTRA_REJECT_FROM = "extra_from";   //拒绝／取消来源
    String EXTRA_ORDER_ID = "extra_order_id";    // 订单id
    String EXTRA_REFUND_AMOUNT = "extra_refund_amount";        // 退款额
    String EXTRA_SCORE_TYPE = "extra_score_type"; //评论入口区别
    String EXTRA_SKILLS_ID = "extra_skills_id"; //技能id
    String EXTRA_COMMENDED_ID = "extra_commend_id";//被评价人ID
    String EXTRA_CURRENT_COUNT = "extra_current_count";//当前金额
    String EXTRA_ORDER_COUPONID = "extra_order_couponid"; //优惠券
    String EXTRA_ORDER_TOTALCOUNT = "extra_order_totalCount"; //付款
    String EXTRA_ORDER_LEFTTIME = "extra_order_lefttime"; //付款剩余时间
    String EXTRA_COUPON_TIME = "extra_coupon_time";    //优惠券购买时长
    String EXTRA_COUPON_UNIT = "extra_coupon_unit";    //时间单位


    String EXTRA_CONTRACT_SKILLNAME = "extra_contract_skillname"; //约Ta来源类型

    String EXTRA_ORDER_TYPE = "extra_order_type"; //订单页来源
    String EXTRA_ORDER_POSITION = "extra_order_position"; //订单type position

    //达人
    String EXTRA_DO_YEN = "extra_do_yen";
    String EXTRA_DO_YEN_NAME = "extra_do_yen_name";

    String EXTRA_FOR_SEARCH = "extra_for_search";

    //
    String SKILL_IMAGE_TEXT_ID = "skill_image_text_id";
    String SKILL_IMAGE_TEXT_BEUSERID = "skill_image_text_beuserid";


    String MULTI_MODEL = "multi_model";
    String MULTI_TITLE = "multi_title";
    String MULTI_ANNOUCE = "multi_annouce";
    String MULTI_CHATGROUP_ID = "multi_chatgroupid";

    String MULTI_TYPE = "multi_type";
    String MULTI_ROOM_ID = "multi_room_id";
    String MULTI_ROOM_OLDGROUPID = "multi_room_oldgroupid";


    String MULTI_EXIT_TITLE = "multi_exit_title";
    String MULTI_EXIT_ROOM_ID = "multi_exit_room_id";
    String MULTI_EXIT_AVATER = "multi_exit_avater";
    String MULTI_EXIT_TIME = "multi_exit_time";
    //搜索的ResulCode
    int CODE = 1;

    //神策-跳转至个人主页专用，无特殊用途,勿删
    String OTHER_ENTRY_SC_1 = "排行榜";
    String OTHER_ENTRY_SC_2 = "首页卡片";
    String OTHER_ENTRY_SC_3 = "卡片详情页";
    String OTHER_ENRTY_SC_4 = "聊天窗口";
    String OTHER_ENTRY_SC_5 = "地图";
    String OTHER_ENTRY_SC_6 = "评论";
    String OTHER_ENTRY_SC_7 = "直播间名片";
    String OTHER_ENTRY_SC_8 = "直播开播推送";

    //    String OTHER_ENTRY_SC_8="直播间名片的主页Btn";
    String OTHER_ENTRY_SC_9 = "其它";
    String OTHER_ENTRY_SC_10 = "Sayhi";
    String OTHER_ENTRY_SC_11 = "声优专区";
    String OTHER_ENTRY_SC_12 = "派单厅";


    //情感
    String EXTRA_EMOTION_FROMTYPE = "fromTYPE";
    String EXTRA_EMOTION_PRICE = "price";
    String EXTRA_EMOTION_CLASSID = "classId";
    String EXTRA_EMOTION_COURSEID = "courseId";
    String EXTRA_EMOTION_UPDATA = "updata";
    String EXTRA_EMOTION_MODEL = "demol";
    String EXTRA_EMOTION_TITLE = "title";
    String EXTRA_CLASS_ROOM_MODEL = "roomModel";
    String EXTRA_CLASS_ROOM_ID = "roomId";
    String EXTRA_EDIT_INFO = "editInfo";
    String EXTRA_GROUP_ID = "group_id";
    String EXTRA_TEACHER_NAME = "extra_teacher_name";
    String EXTRA_COURSR_NAME = "extra_course_name";

    //约单
    String EXTRA_CONTRACT_HOME_TAB = "contract_tab";

    //发现
    String EXTRA_TOPIC_ID = "topicId";
    String EXTRA_TOPIC_CARD_ID = "topic_card_Id";//话题帖子Id
    String EXTRA_TOPIC_SOURCE = "topic_source";
    String EXTRA_DISCOVER_SENSORS = "discover_sensors";//话题帖子Id
    int EXTRA_REPLY_DIALOG_TOPIC = 0;//话题回复
    int EXTRA_REPLY_DIALOG_TOPIC_CARD = 1;//话题帖子回复
    int EXTRA_REPLY_DIALOG_TOPIC_CARD_COMMENT = 2;//话题帖子的评论回复
    int EXTRA_REPLY_DIALOG_TOPIC_COMMENT = 4;//话题评论的回复
    int EXTRA_REPLY_MAP_DYNAMIC_DETAILS_COMMENT = 5;//动态详情的回复
    int EXTRA_TOPIC_AND_TOPIC_CARD_BOTTOM_DIALOG_SELF_IS_hS = 1;//自己的评论或帖子 匿名
    int EXTRA_TOPIC_AND_TOPIC_CARD_BOTTOM_DIALOG_SELF_NO_hS = 2;//自己的评论或帖子 无匿名
    int EXTRA_TOPIC_AND_TOPIC_CARD_BOTTOM_DIALOG_OTHER_REPLY = 3;//他人的评论或帖子 能回复
    int EXTRA_TOPIC_AND_TOPIC_CARD_BOTTOM_DIALOG_OTHER_NO_REPLY = 4;//自己的评论或帖子 不能回复

    //map宠物动态
    int EXTRA_MAP_DYNAMIC_DETAILS_BOTTOM_DIALOG_SELF = 6;//自己的动态
    int EXTRA_MAP_DYNAMIC_DETAILS_BOTTOM_DIALOG_OTHER = 7;//他人的动态或者他人信息
    int EXTRA_MAP_DYNAMIC_DETAILS_COMMENT_BOTTOM_DIALOG_SELF = 8;//自己的评论
    int EXTRA_MAP_DYNAMIC_DETAILS_COMMENT_BOTTOM_DIALOG_N0_SELF = 9;//不是自己的评论

    //map宠物中的Extra
    String EXTRA_MAP_PET_MES_GO_MAP = "map_pet_msg_go_map"; //点击宠物消息进入地图
    String EXTRA_MAP_OTHER_INFO_USER_ID = "map_other_info_userId";//userId
    String EXTRA_MAP_ID = "map_id";//id
    String EXTRA_MAP_SOURCE_ID = "map_type";//type当是1的时候为未读进入详情
    String EXTRA_MAP_FIND_EVENT = "map_find_event";//id
    String EXTRA_MAP_TESTS_ID = "map_test_id";//id
    String EXTRA_MAP_TESTS_INTERFACES = "map_test_interfaces";//id
    String EXTRA_MAP_UNREAD_EVENT = "map_un_read_event";//是否是未读事件进来的详情
    String EXTRA_MAP_PET_NAME = "map_pet_name";//宠物名字
    String EXTRA_MAP_PET_MESSAGE_EVENT = "map_pet_message_event";//宠物消息点击携带事件Event
    String EXTRA_MAP_ORIGIN_TYPE = "map_origin_type"; //入口起源type
    String EXTRA_MAP_WARE_HOUSE_TYPE = "map_ware_house_type";
    String EXTRA_MAP_STORY_INFO = "map_story_info";
    //直播跳转roomId
    public static final String EXTRA_ENTER_LIVEROOMID = "extra_enter_liveroom_roomid";
    public static final String EXTRA_ENTER_LIVEROOM = "extra_enter_liveroom";

    //登录
    public static final String EXTRA_ENTER_INCOMEBILL_DETAIL = "extra_enter_incomebill_detail";
    public static final String EXTRA_VCode_FROM = "extra_vcode_from";
    public static final String EXTRA_VCode_PHONE = "extra_vcode_phone";
    public static final String EXTRA_VCode_THIRDMODEL = "extra_vcode_thirdmodel";

    public static final String EXTRA_LOGINOUT_PASSWORD = "extra_loginout_password";
    public static final String EXTRA_MODIFY_PASSWORD = "extra_modify_password";
    public static final String MODIFY_PASSWORD = "modify_password";

    public static final String EXTRA_SOAP_DETAILS = "extra_soap_details";
    public static final String EXTRA_TALENTSKILL_USERID = "extra_talentskill_userid";


    //视频播放器url
    String VIDEO_PLAYER_URL = "videoPlayerUrl";
    String VIDEO_PLAYER_WIDTH = "videoPlayerWidth";
    String VIDEO_PLAYER_HEIGHT = "videoPlayerHeight";
    public static final String EXTRA_VIDEOPLAY_STATE="extra_videoplay_state";
    public static final int EXTRA_CODE_VIDEO_RETURN_RESULT = 1006;
    public static final int EXTRA_CODE_THEME_RETURN = 1007;

    public static final String EXTRA_SENSOR_SOURCE = "extra_sensor_source"; //神策埋点 - 点击短视频



}
