package jiguang.chat.contants;

/**
 * Created by zhaoyuanchao on 2018/9/12.
 */

public class IMContants {
    //存储 IM多媒体下载地址 ,区分环境
    public static String IM_MULTIMEDIA_URL;
    //自定义消息类型  0：订单消息  1：课程消息 2：音视频消息 3：群组提示性消息
    public static final String CUSTOM_MSG_TYPE = "customMsgType";

    /**
     * 订单消息相关：
     * isRemindBuyer, 0:只有达人可以看到  1：只有买家可以看到;
     * userId : 用户ID;
     * title : 订单标题;
     * content:订单内容;
     * serviceContent:服务内容;
     * appointTime:预约时间;
     * serviceRemark:服务备注;
     * orderId:订单ID;
     */

    public static final String ORDER_IS_REMIND_BUYER = "isRemindBuyer";
    public static final String ORDER_USERID = "userId";
    public static final String ORDER_TITLE = "title";
    public static final String ORDER_CONTENT = "content";
    public static final String ORDER_SERVICE_CONTENT = "serviceContent";
    public static final String ORDER_APPOINT_TIME = "appointTime";
    public static final String ORDER_SERVICE_REMARK = "serviceRemark";
    public static final String ORDER_ORDER_ID = "orderId";

    /**
     * 课程消息相关：
     * icon: 课程图标;
     * content: 课程内容;
     * nowPrice:当前课程价格;
     * userId:用户ID;
     * oldPrice: 之前的价格;
     * isPriceChange: 价格是否变化;
     * courseId: 课程ID;
     * typesId : 类型ID;
     */
    public static final String COURSE_ICON = "icon";
    public static final String COURSE_CONTENT = "content";
    public static final String COURSE_NOW_PRICE = "nowPrice";
    public static final String COURSE_USERID = "userId";
    public static final String COURSE_OLD_PRICE = "oldPrice";
    public static final String COURSE_IS_PRICE_CHANGE = "isPriceChange";
    public static final String COURSE_COURSE_ID = "courseId";
    public static final String COURSE_TYPES_ID = "typesId";

    /**
     * 音视频的自定义消息
     * CmdType: 0: 发起请求（通信搭桥作用） 1:取消请求（展示UI）  2:接受请求(通信搭桥作用) 3:结束请求(展示UI) 4:用户正忙
     * VideoChatSelectType: 0: 视频通话  1:语音通话
     * BubbleVideoChatDuration: 通话时长
     * SessionKey: 给IOS携带的时间戳
     */
    public static final String RTC_CMD_TYPE = "CmdType";
    public static final String RTC_VIDEO_CHAT_SELECT_TYPE = "VideoChatSelectType";
    public static final String RTC_BUBBLE_VIDEO_CHAT_DURATION = "BubbleVideoChatDuration";
    public static final String RTC_SESSION_KEY = "SessionKey";

    /**
     * 群组提示性消息
     * PromptText：提示性消息内容;
     */
    public static final String GROUP_PROMPT_TEXT = "promptText";
    public static final String GROUP_PROMPT_TEXT_BIG = "PromptText";
}
