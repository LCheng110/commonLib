package cn.jpush.im.android.api.content;

import android.text.TextUtils;

import com.google.gson.jpush.JsonElement;
import com.google.gson.jpush.JsonObject;
import com.google.gson.jpush.JsonPrimitive;

import java.util.HashMap;
import java.util.Map;

import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;

public class CustomContent extends MessageContent {

    private static final String TAG = "CustomContent";

    protected Map<String, String> contentStringMap = new HashMap<String, String>();

    protected Map<String, Number> contentNumMap = new HashMap<String, Number>();

    protected Map<String, Boolean> contentBooleanMap = new HashMap<String, Boolean>();




    public CustomContent() {
        super();
        this.contentType = ContentType.custom;

    }

    /**
     * 设置自定义消息体中的值
     *
     * @param key
     * @param value
     */
    public void setStringValue(String key, String value) {
        contentStringMap.put(key, value);
    }

    /**
     * 设置自定义消息体中的值
     *
     * @param key
     * @param number
     */
    public void setNumberValue(String key, Number number) {
        contentNumMap.put(key, number);
    }

    /**
     * 设置自定义消息体中的值
     *
     * @param key
     * @param value
     */
    public void setBooleanValue(String key, Boolean value) {
        contentBooleanMap.put(key, value);
    }

    /**
     * 将map中所有键值对放入消息体，
     *
     * @param map
     */

    public void setAllValues(Map<? extends String, ? extends String> map) {
        if (null != map) {
            contentStringMap.putAll(map);
        } else {
            Logger.ee(TAG, "map should not be null !");
        }
    }

    /**
     * 获取指定key对应的值
     *
     * @param key
     * @return 指定key对应的值，如果key为空或不存在则返回null
     */
    public String getStringValue(String key) {
        if (!TextUtils.isEmpty(key)) {
            return contentStringMap.get(key);
        } else {
            return null;
        }
    }

    /**
     * 获取指定key对应的值
     *
     * @param key
     * @return 指定key对应的值，如果key为空或不存在则返回null
     */
    public Number getNumberValue(String key) {
        if (!TextUtils.isEmpty(key)) {
            return contentNumMap.get(key);
        } else {
            return null;
        }
    }

    /**
     * 获取指定key对应的值
     *
     * @param key
     * @return 指定key对应的值，如果key为空或不存在则返回null
     */
    public Boolean getBooleanValue(String key) {
        if (!TextUtils.isEmpty(key)) {
            return contentBooleanMap.get(key);
        } else {
            return null;
        }
    }



    /**
     * 获取消息体中所有值为字符串类型的键值对
     *
     * @return 一个包含消息体中所有键值对的map
     */
    public Map getAllStringValues() {
        return contentStringMap;
    }

    /**
     * 获取消息体中所有值为数字类型的键值对
     *
     * @return 一个包含消息体中所有键值对的map
     */
    public Map getAllNumberValues() {
        return contentNumMap;
    }

    /**
     * 获取消息体中所有值为布尔类型的键值对
     *
     * @return 一个包含消息体中所有键值对的map
     */
    public Map getAllBooleanValues() {
        return contentBooleanMap;
    }

    public String toJson() {
        return JsonUtil.toJson(toJsonElement());
    }

    public JsonElement toJsonElement() {
        JsonObject contentObj = new JsonObject();
        for (String key : contentStringMap.keySet()) {
            contentObj.add(key, new JsonPrimitive(contentStringMap.get(key)));
        }

        for (String key : contentNumMap.keySet()) {
            contentObj.add(key, new JsonPrimitive(contentNumMap.get(key)));
        }

        for (String key : contentBooleanMap.keySet()) {
            contentObj.add(key, new JsonPrimitive(contentBooleanMap.get(key)));
        }


        JsonObject extrasObj = new JsonObject();
        for (String key : stringExtras.keySet()) {
            extrasObj.add(key, new JsonPrimitive(stringExtras.get(key)));
        }

        for (String key : numExtras.keySet()) {
            extrasObj.add(key, new JsonPrimitive(numExtras.get(key)));
        }

        for (String key : booleanExtras.keySet()) {
            extrasObj.add(key, new JsonPrimitive(booleanExtras.get(key)));
        }
        extras = extrasObj;
        contentObj.add("extras", extrasObj);
        return contentObj;
    }
}
