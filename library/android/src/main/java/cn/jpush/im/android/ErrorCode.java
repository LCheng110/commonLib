package cn.jpush.im.android;


public class ErrorCode {

    public static final int ERROR_USER_OFFLINE = 800013;
    public static final String ERROR_USER_OFFLINE_DESC = "user is offline,please login again";

    public static final int ERROR_USER_DEVICE_NOT_MATCH = 800016;
    public static final String ERROR_USER_DEVICE_NOT_MATCH_DESC = "device not match,please login again.";

    public static final int ERROR_NO_SUCH_USER = 898002;

    public static final int NO_ERROR = 0;
    public static final String NO_ERROR_DESC = "Success";

    public class HTTP_ERROR {
        public static final int HTTP_INVALID_PARAMETERS = 871101;
        public static final String HTTP_INVALID_PARAMETERS_DESC = "Invalid request parameters.";

        public static final int HTTP_RETRY_REACH_LIMIT = 871102;
        public static final String HTTP_RETRY_REACH_LIMIT_DESC = "Request failed.please check your network connection.";

        public static final int HTTP_UNEXPECTED_ERROR = 871103;
        public static final String HTTP_UNEXPECTED_ERROR_DESC = "Server returned an unexpected error code.";

        public static final int HTTP_SERVER_INTERNAL_ERROR = 871104;
        public static final String HTTP_SERVER_INTERNAL_ERROR_DESC = "Server internal error.";

        public static final int HTTP_SERVER_USER_INFO_NOT_FOUND_ERROR = 871105;
        public static final String HTTP_SERVER_USER_INFO_NOT_FOUND_ERROR_DESC = "User info not found";
    }

    public class TCP_ERROR {
        public static final int TCP_RESPONSE_TIMEOUT = 871201;
        public static final String TCP_RESPONSE_TIMEOUT_DESC = "Get response timeout,please try again later.";
    }

    public class LOCAL_ERROR {
        public static final int LOCAL_NOT_LOGIN = 871300;
        public static final String LOCAL_NOT_LOGIN_DESC = "Have not logged in.";

        public static final int LOCAL_INVALID_PARAMETERS = 871301;
        public static final String LOCAL_INVALID_PARAMETERS_DESC = "Invalid parameters.";

        public static final int LOCAL_INVALID_MESSAGE_CONTENT_LENGTH = 871302;
        public static final String LOCAL_INVALID_MESSAGE_CONTENT_LENGTH_DESC = "Message content exceeds its max length.";

        public static final int LOCAL_INVALID_USERNAME = 871303;
        public static final String LOCAL_INVALID_USERNAME_DESC = "Invalid username.";

        public static final int LOCAL_INVALID_PASSWORD = 871304;
        public static final String LOCAL_INVALID_PASSWORD_DESC = "Invalid password.";

        public static final int LOCAL_INVALID_NAME = 871305;
        public static final String LOCAL_INVALID_NAME_DESC = "Invalid name.";

        public static final int LOCAL_INVALID_INPUT = 871306;
        public static final String LOCAL_INVALID_INPUT_DESC = "Invalid input.";

        public static final int LOCAL_USER_NOT_EXISTS = 871307;
        public static final String LOCAL_USER_NOT_EXISTS_DESC = "Some user not exists,operate failed.";

        public static final int LOCAL_HAVE_NOT_INIT = 871308;
        public static final String LOCAL_HAVE_NOT_INIT_DESC = "SDK have not init yet.";

        public static final int LOCAL_FILE_NOT_FOUND = 871309;
        public static final String LOCAL_FILE_NOT_FOUND_DESC = "Attached file not found.";

        public static final int LOCAL_NETWORK_DISCONNECTED = 871310;
        public static final String LOCAL_NETWORK_DISCONNECTED_DESC = "Network not available,please check your network connection.";

        public static final int LOCAL_USER_AVATAR_NOT_SPECIFIED = 871311;
        public static final String LOCAL_USER_AVATAR_NOT_SPECIFIED_DESC = "Avatar not specified. download avatar failed.";

        public static final int LOCAL_CREATE_IMAGE_CONTENT_FAIL = 871312;
        public static final String LOCAL_CREATE_IMAGE_CONTENT_FAIL_DESC = "Create image content failed.";

        public static final int LOCAL_MESSAGE_PARSE_VERSION_NOT_MATCH = 871313;
        public static final String LOCAL_MESSAGE_PARSE_VERSION_NOT_MATCH_DESC = "Message parse error,version not match.";

        public static final int LOCAL_MESSAGE_PARSE_INVALID_KEY_VALUE = 871314;
        public static final String LOCAL_MESSAGE_PARSE_INVALID_KEY_VALUE_DESC = "Message parse error,lack of key parameter.";

        public static final int LOCAL_MESSAGE_PARSE_ERROR = 871315;
        public static final String LOCAL_MESSAGE_PARSE_ERROR_DESC = "Message parse error,check logcat for more information.";

        public static final int LOCAL_DATABASE_ERROR = 871316;
        public static final String LOCAL_DATABASE_ERROR_DESC = "Error occurs in database operation.";

        public static final int TARGET_USER_CANNOT_BE_YOURSELF_ERROR = 871317;
        public static final String TARGET_USER_CANNOT_BE_YOURSELF_DESC = "Target user cannot be yourself.";

        public static final int LOCAL_ILLEGAL_MSG_JSON = 871318;
        public static final String LOCAL_ILLEGAL_MSG_JSON_DESC = "Illegal message content. maybe it`s caused by proguard,please follow the instruction on docs.jiguang.cn when use proguard.";

        public static final int LOCAL_CREATE_FORWARD_MESSAGE_ERROR = 871319;
        public static final String LOCAL_CREATE_FORWARD_MESSAGE_ERROR_DESC = "create forwardMessage failed, check logcat for more information.";

        public static final int LOCAL_SET_HAVEREAD_ERROR = 871320;
        public static final String LOCAL_SET_HAVEREAD_ERROR_DESC = "set message HaveRead status failed. maybe this message is already in HaveRead status, or this is not a received message.";

        public static final int LOCAL_GET_RECEIPT_DETAIL_PERMISSION_ERROR = 871321;
        public static final String LOCAL_GET_RECEIPT_DETAIL_PERMISSION_ERROR_DESC = "get receipt details failed. this message is not sent by you,you don`t have the permission to check the receipt details";

        public static final int LOCAL_GET_RECEIPT_DETAIL_STATUS_ERROR = 871322;
        public static final String LOCAL_GET_RECEIPT_DETAIL_STATUS_ERROR_DESC = "get receipt details failed. message is not successfully sent yet, should send message first before you get receipt details";
    }

    public class OTHERS_ERROR {
        public static final int OTHERS_HTTP_ERROR = 871401;
        public static final String OTHERS_HTTP_ERROR_DESC = "Upload file failed.";

        public static final int OTHERS_AUTH_ERROR = 871402;
        public static final String OTHERS_AUTH_ERROR_DESC = "Upload file failed.";

        public static final int OTHERS_UPLOAD_ERROR = 871403;
        public static final String OTHERS_UPLOAD_ERROR_DESC = "Upload file failed.";

        public static final int OTHERS_DOWNLOAD_ERROR = 871404;
        public static final String OTHERS_DOWNLOAD_ERROR_DESC = "Download file failed.";
    }

    public class PUSH_REGISTER_ERROR {
        //Equal to push register error code 1005
        public static final int PUSH_REGISTER_ERROR_APPKEY_APPID_NOT_MATCH = 871501;
        public static final String PUSH_REGISTER_ERROR_APPKEY_APPID_NOT_MATCH_DESC = "Push register error,appkey and appid not match.";

        //Equal to push register error code 1008
        public static final int PUSH_REGISTER_ERROR_WRONG_APPKEY = 871502;
        public static final String PUSH_REGISTER_ERROR_WRONG_APPKEY_DESC = "Push register error,invalid appkey.";

        //Equal to push register error code 1009
        public static final int PUSH_REGISTER_ERROR_APPKEY_PLATFORM_NOT_MATCH = 871503;
        public static final String PUSH_REGISTER_ERROR_APPKEY_PLATFORM_NOT_MATCH_DESC = "Push register error,appkey not matches platform";

        public static final int PUSH_REGISTER_NOT_FINISHED = 871504;
        public static final String PUSH_REGISTER_NOT_FINISHED_DESC = "Push register not finished. ";

        //Equal to push register error code 1006
        public static final int PUSH_REGISTER_ERROR_PACKAGE_NOT_EXIST = 871505;
        public static final String PUSH_REGISTER_ERROR_PACKAGE_NOT_EXIST_DESC = "Push register error,package not exists.";

        //Equal to push register error code 1007
        public static final int PUSH_REGISTER_ERROR_INVALID_IMEI = 871506;
        public static final String PUSH_REGISTER_ERROR_INVALID_IMEI_DESC = "Push register error,invalid IMEI.";
    }

}
