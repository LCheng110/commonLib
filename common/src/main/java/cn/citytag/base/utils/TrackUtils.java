package cn.citytag.base.utils;

import com.reyun.tracking.sdk.Tracking;

/**
 * li
 * 热云Utils
 */
public class TrackUtils {

    //获取支付宝支付的流水号和money
    public static String[] getAliPayTrade_noAndMoney(String resultInfo) {

        com.alibaba.fastjson.JSONObject jsonObject = com.alibaba.fastjson.JSONObject.parseObject(resultInfo);
        String[] strings = new String[2];
        com.alibaba.fastjson.JSONObject var = (com.alibaba.fastjson.JSONObject) jsonObject.get("alipay_trade_app_pay_response");
        String trade_no = var.getString("trade_no");
        String money = var.getString("total_amount");
        strings[0] = trade_no;
        strings[1] = money;
        return strings;

    }

    //支付宝支付
    public static final String ALI_PAY = "alipay";

    //微信支付
    public static final String WEI_XIN_PAY = "weixinpay";

    //人民币
    public static final String CNY = "CNY";


    //热云注册统计
    public static void RegisterTrack(String accountId) {
        Tracking.setRegisterWithAccountID(accountId);
    }

    //热云登陆统计
    public static void loginTrack(String accountId) {
        Tracking.setLoginSuccessBusiness(accountId);

    }

    //热云支付统计
    public static void PaymentTrack(String transactionId, String paymentType, String currencyType, float currencyAmount) {
        Tracking.setPayment(transactionId, paymentType, currencyType, currencyAmount);

    }
}
