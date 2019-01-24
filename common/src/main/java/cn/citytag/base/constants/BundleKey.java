package cn.citytag.base.constants;

/**
 * Created by yangfeng01 on 2017/11/1.
 * <p>
 * bundle.putString("key", "value");;
 * bundle.putInt("key", 1);
 */

public interface BundleKey {

    String USER_NAME = "user_name";
    String USER_PWD = "user_pwd";
    String KEY_TITLE = "key_title";
    String KEY_CONTENT = "key_content";    // dialog content
    String KEY_MAIN_TAB = "key_main_tab";
    String KEY_BUBBLE_ID_LIST = "key_bubble_id_list";
    String KEY_BUBBLE_ADDRESS = "key_bubble_address";
    String KEY_AUTHED_TECHS = "key_authed_techs";    // 已认证的二级技能

    String KEY_ORDER_STATE = "key_order_state";    // 订单状态

    String KEY_PIC_SELECT_CARRY_CATALOG = "key_pic_select_carry_catalog";//图片选择器携带目录
}
