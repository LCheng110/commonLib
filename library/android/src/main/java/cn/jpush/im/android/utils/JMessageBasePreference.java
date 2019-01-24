package cn.jpush.im.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Random;


public class JMessageBasePreference {
    private static final String JMESSAGE_PREF = "cn.jmessage.preferences";
    private static final String AES_ENCRYPTION_SEED = "jmessage";
    private static SharedPreferences mSharedPreferences = null;
    private static final String KEY_IM_NEXT_RID = "im_next_rid";

    public JMessageBasePreference() {
    }

    public static void init(Context context) {
        if(null == mSharedPreferences) {
            mSharedPreferences = context.getSharedPreferences("cn.jmessage.preferences", 0);
        }
    }

    public static void removeKey(String key) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(key);
        editor.commit();
    }

    public static void removeAll() {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.clear();
        editor.commit();
    }

    protected static void commitString(String key, String value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    protected static String getString(String key, String failValue) {
        return mSharedPreferences.getString(key, failValue);
    }

    protected static void commitInt(String key, int value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    protected static int getInt(String key, int failValue) {
        return mSharedPreferences.getInt(key, failValue);
    }

    protected static void commitLong(String key, long value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    protected static long getLong(String key, long failValue) {
        return mSharedPreferences.getLong(key, failValue);
    }

    protected static void commitBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    protected static Boolean getBoolean(String key, boolean failValue) {
        return Boolean.valueOf(mSharedPreferences.getBoolean(key, failValue));
    }

    protected static void commitString(Context context, String key, String value) {
        init(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    protected static String getString(Context context, String key, String faillValue) {
        init(context);
        return mSharedPreferences.getString(key, faillValue);
    }

    protected static void commitInt(Context context, String key, int value) {
        init(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    protected static int getInt(Context context, String key, int failValue) {
        init(context);
        return mSharedPreferences.getInt(key, failValue);
    }

    protected static void commitLong(Context context, String key, long value) {
        init(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putLong(key, value);
        editor.apply();
    }

    protected static long getLong(Context context, String key, long failValue) {
        init(context);
        return mSharedPreferences.getLong(key, failValue);
    }

    protected static void commitBoolean(Context context, String key, boolean value) {
        init(context);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    protected static Boolean getBoolean(Context context, String key, boolean failValue) {
        init(context);
        return Boolean.valueOf(mSharedPreferences.getBoolean(key, failValue));
    }

    public static synchronized long getNextRid() {
        long oldRid = getLong("im_next_rid", getStartRid()) % 32767L;
        commitLong("im_next_rid", oldRid + 2L);
        return oldRid + 2L;
    }

    private static long getStartRid() {
        long rid = (long)Math.abs((new Random()).nextInt(32767));
        rid = rid % 2L == 0L?rid:rid + 1L;
        return rid;
    }
}

