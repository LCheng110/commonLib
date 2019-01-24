package cn.jpush.im.android.api.content;

import com.google.gson.jpush.JsonElement;
import com.google.gson.jpush.JsonObject;
import com.google.gson.jpush.JsonPrimitive;
import com.google.gson.jpush.annotations.Expose;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import cn.jpush.im.android.api.enums.ContentType;
import cn.jpush.im.android.api.model.Message;
import cn.jpush.im.android.utils.JsonUtil;
import cn.jpush.im.android.utils.Logger;


public abstract class MessageContent implements Cloneable {

    private static final String TAG = "MessageContent";

    /**
     * 消息内容类型
     */
    protected ContentType contentType;

    @Expose
    protected JsonElement extras;

    Map<String, String> stringExtras = new HashMap<String, String>();
    Map<String, Number> numExtras = new HashMap<String, Number>();
    Map<String, Boolean> booleanExtras = new HashMap<String, Boolean>();

    protected MessageContent() {
    }

    public ContentType getContentType() {
        return this.contentType;
    }

    /**
     * 设置消息内容的类型。
     *
     * @param contentType
     */
    protected void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }

    /**
     * 获取消息体中所有的附加字段
     *
     * @return
     */
    public Map<String, String> getStringExtras() {
        return stringExtras;
    }

    /**
     * 获取消息体中所有的附加字段
     *
     * @return
     */
    public Map<String, Number> getNumberExtras() {
        return numExtras;
    }

    /**
     * 获取消息体中所有的附加字段
     *
     * @return
     */
    public Map<String, Boolean> getBooleanExtras() {
        return booleanExtras;
    }

    /**
     * 设置消息体中的附加字段
     *
     * @param stringExtras
     */
    public void setExtras(Map<String, String> stringExtras) {
        if (null != stringExtras) {
            this.stringExtras = stringExtras;
        }
    }

    /**
     * 获取消息体中附加字段
     *
     * @param key
     * @return
     */
    public String getStringExtra(String key) {
        if (null != stringExtras) {
            return stringExtras.get(key);
        } else {
            return null;
        }
    }

    /**
     * 获取消息体中附加字段
     *
     * @param key
     * @return
     */
    public Number getNumberExtra(String key) {
        if (null != numExtras) {
            return numExtras.get(key);
        } else {
            return null;
        }
    }

    /**
     * 获取消息体中附加字段
     *
     * @param key
     * @return
     */
    public Boolean getBooleanExtra(String key) {
        if (null != booleanExtras) {
            return booleanExtras.get(key);
        } else {
            return null;
        }
    }

    /**
     * 设置消息体中的附加字段.
     * 注意此接口设置的值只是写到内存，数据不会固化到数据库。如果需要将数据固化，
     * 需要使用{@link cn.jpush.im.android.api.model.Conversation#updateMessageExtra(Message, String, String)}
     * 接口更新消息的extra。
     *
     * @param key
     * @param value
     */
    public void setStringExtra(String key, String value) {
        if (null != stringExtras && null == value) {
            stringExtras.remove(key);
        } else if (null != stringExtras) {
            stringExtras.put(key, value);
        }
    }

    /**
     * 设置消息体中的附加字段.
     * 注意此接口设置的值只是写到内存，数据不会固化到数据库。如果需要将数据固化，
     * 需要使用{@link cn.jpush.im.android.api.model.Conversation#updateMessageExtra(Message, String, Number)}
     * 接口更新消息的extra。
     *
     * @param key
     * @param value
     */
    public void setNumberExtra(String key, Number value) {
        if (null != numExtras && null == value) {
            numExtras.remove(key);
        } else if (null != numExtras) {
            numExtras.put(key, value);
        }
    }

    /**
     * 设置消息体中的附加字段
     * 注意此接口设置的值只是写到内存，数据不会固化到数据库。如果需要将数据固化，
     * 需要使用{@link cn.jpush.im.android.api.model.Conversation#updateMessageExtra(Message, String, Boolean)}
     * 接口更新消息的extra。
     *
     * @param key
     * @param value
     */
    public void setBooleanExtra(String key, Boolean value) {
        if (null != booleanExtras && null == value) {
            booleanExtras.remove(key);
        } else if (null != booleanExtras) {
            booleanExtras.put(key, value);
        }
    }

    public String toJson() {
        return JsonUtil.toJson(toJsonElement());
    }

    public JsonElement toJsonElement() {
        JsonObject jsonObject = new JsonObject();
        if (null != stringExtras) {// TODO: 2017/8/3 这里stringExtras有可能为null?
            for (String key : stringExtras.keySet()) {
                jsonObject.add(key, new JsonPrimitive(stringExtras.get(key)));
            }
        }

        if (null != numExtras) {
            for (String key : numExtras.keySet()) {
                jsonObject.add(key, new JsonPrimitive(numExtras.get(key)));
            }
        }

        if (null != booleanExtras) {
            for (String key : booleanExtras.keySet()) {
                jsonObject.add(key, new JsonPrimitive(booleanExtras.get(key)));
            }
        }
        extras = jsonObject;
        return JsonUtil.toJsonTreeWithoutExpose(this);
    }

    public static MessageContent fromJson(JsonElement jsonElement, ContentType type) {
        return fromJson(jsonElement.toString(), type);
    }

    public static MessageContent fromJson(String contentJson, ContentType type) {
        MessageContent messageContent = null;
        switch (type) {
            case text:
                messageContent = JsonUtil.fromJson(contentJson, TextContent.class);
                break;
            case image:
                messageContent = JsonUtil.fromJson(contentJson, ImageContent.class);
                break;
            case location:
                messageContent = JsonUtil.fromJson(contentJson, LocationContent.class);
                break;
            case voice:
                messageContent = JsonUtil.fromJson(contentJson, VoiceContent.class);
                break;
            case file:
                messageContent = JsonUtil.fromJson(contentJson, FileContent.class);
                break;
            case custom:
                CustomContent customContent = new CustomContent();
                JsonObject contentJsonObject = JsonUtil.fromJson(contentJson, JsonObject.class);
                moveObjectsToGivenMaps(contentJsonObject, customContent.contentStringMap, customContent.contentNumMap, customContent.contentBooleanMap);
                JsonObject extrasJsonObject = JsonUtil.fromJson(contentJsonObject.get("extras"), JsonObject.class);
                moveObjectsToGivenMaps(extrasJsonObject, customContent.stringExtras, customContent.numExtras, customContent.booleanExtras);
                customContent.setContentType(ContentType.custom);
                return customContent;
            case eventNotification:
                messageContent = JsonUtil.fromJson(contentJson, EventNotificationContent.class);
                break;
            case prompt:
                messageContent = JsonUtil.fromJson(contentJson, PromptContent.class);
                break;
            case unknown:
                Logger.ww(TAG, "unknown message content type.");
                break;
        }
        if (null != messageContent) {
            messageContent.setContentType(type);
            JsonObject extrasObj = JsonUtil.fromJson(messageContent.extras, JsonObject.class);
            moveObjectsToGivenMaps(extrasObj, messageContent.stringExtras, messageContent.numExtras, messageContent.booleanExtras);
        }
        return messageContent;
    }

    private static void moveObjectsToGivenMaps(JsonObject jsonObject, Map<String, String> stringMap, Map<String, Number> numberMap, Map<String, Boolean> booleanMap) {
        if (null != jsonObject) {
            Set<Map.Entry<String, JsonElement>> entrySet = jsonObject.entrySet();
            for (Map.Entry<String, JsonElement> entry : entrySet) {
                if (entry.getValue().isJsonPrimitive()) {
                    JsonPrimitive value = entry.getValue().getAsJsonPrimitive();
                    if (value.isString() && null != stringMap) {
                        stringMap.put(entry.getKey(), value.getAsString());
                    } else if (value.isNumber() && null != numberMap) {
                        numberMap.put(entry.getKey(), value.getAsNumber());
                    } else if (value.isBoolean() && null != booleanMap) {
                        booleanMap.put(entry.getKey(), value.getAsBoolean());
                    } else {
                        Logger.ww(TAG, "unsupported type of value. key = " + entry.getKey() + " value = " + value);
                    }
                }
            }
        }
    }

    @Override
    public Object clone() {
        MessageContent o = null;
        try {
            o = (MessageContent) super.clone();
        } catch (CloneNotSupportedException e) {
            Logger.ww(TAG, "clone message content failed!");
            e.printStackTrace();
        }
        return o;
    }
}
