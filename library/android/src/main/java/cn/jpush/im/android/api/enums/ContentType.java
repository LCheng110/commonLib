package cn.jpush.im.android.api.enums;


public enum ContentType {
    text, image, voice, location, video, eventNotification, custom, unknown, file, prompt;

    public static ContentType get(int index) {
        return ContentType.values()[index];
    }

}
