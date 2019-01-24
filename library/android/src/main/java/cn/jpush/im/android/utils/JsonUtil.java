package cn.jpush.im.android.utils;

import com.google.gson.jpush.Gson;
import com.google.gson.jpush.GsonBuilder;
import com.google.gson.jpush.JsonElement;
import com.google.gson.jpush.JsonSyntaxException;
import com.google.gson.jpush.reflect.TypeToken;

import java.util.Map;

public final class JsonUtil {

    private static Gson gson = new GsonBuilder().create();

    private static Gson gson2 = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
    

    public static Map<String, String> formatToMap(String data) throws JsonSyntaxException {
        Map<String, String> map = gson.fromJson(data, new TypeToken<Map<String, String>>() {
        }.getType());
        return map;
    }

    public static Map<String, Object> formatToObjectMap(String data) throws JsonSyntaxException {
        Map<String, Object> map = gson.fromJson(data, new TypeToken<Map<String, Object>>() {
        }.getType());
        return map;
    }

    public static <T extends Object> T formatToGivenType(String data, TypeToken<T> typeToken) {
        T object = gson.fromJson(data, typeToken.getType());
        return object;
    }

    public static <T extends Object> T formatToGivenType(JsonElement element,
                                                         TypeToken<T> typeToken) {
        T object = gson.fromJson(element, typeToken.getType());
        return object;
    }

    public static <T extends Object> T formatToGivenTypeOnlyWithExpose(String data,
                                                                       TypeToken<T> typeToken) {
        T object = gson2.fromJson(data, typeToken.getType());
        return object;
    }

    public static <T extends Object> T fromJson(String json, Class<T> cls) {
        return gson.fromJson(json, cls);
    }

    public static <T extends Object> T fromJson(JsonElement jsonElement, Class<T> cls) {
        return gson.fromJson(jsonElement, cls);
    }

    public static <T extends Object> T fromJsonOnlyWithExpose(String json, Class<T> cls) {
        return gson2.fromJson(json, cls);
    }

    public static JsonElement toJsonTreeWithoutExpose(Object src) {
        return gson2.toJsonTree(src);
    }

    public static String toJsonOnlyWithExpose(Object data) {
        return JsonUtil.gson2.toJson(data);
    }

    public static String toJson(Object data) {
        return JsonUtil.gson.toJson(data);
    }

}
