package cn.jpush.im.android.pushcommon.proto.common.utils;

import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

import cn.jiguang.ald.api.JResponse;
import cn.jpush.im.android.utils.Logger;


public class ByteBufferUtils {
    private final static String TAG = "ByteBufferUtils";

    public static int getInt(ByteBuffer byteBuffer, JResponse jResponse) {
        try {
            return byteBuffer.getInt();
        } catch (BufferUnderflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (BufferOverflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (Exception e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        }
        if (jResponse != null) {
        }
        return -1;
    }

    public static short getShort(ByteBuffer byteBuffer, JResponse jResponse) {
        try {
            return byteBuffer.getShort();
        } catch (BufferUnderflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (BufferOverflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (Exception e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        }
        if (jResponse != null) {
        }
        return -1;
    }

    public static ByteBuffer get(ByteBuffer byteBuffer, byte[] bytes, JResponse jResponse) {
        try {
            return byteBuffer.get(bytes);
        } catch (BufferUnderflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (BufferOverflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (Exception e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        }
        if (jResponse != null) {
        }
        return null;
    }

    public static Byte get(ByteBuffer byteBuffer, JResponse jResponse) {
        try {
            return byteBuffer.get();
        } catch (BufferUnderflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (BufferOverflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (Exception e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        }
        if (jResponse != null) {
        }
        return null;
    }

    public static long getLong(ByteBuffer byteBuffer, JResponse jResponse) {
        try {
            return byteBuffer.getLong();
        } catch (BufferUnderflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (BufferOverflowException e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        } catch (Exception e) {
            onException(e.fillInStackTrace(), jResponse, byteBuffer);
        }

        if (jResponse != null) {
        }
        return 0;
    }

    /**
     * 将错误信息添加到crash log
     */
    private static void onException(Throwable throwable, JResponse jResponse, ByteBuffer byteBuffer) {
        Logger.e(TAG,"#unexecpted - cause by:"+throwable);
    }

    private static String generalExtraStr(Throwable throwable, JResponse jResponse, ByteBuffer byteBuffer) {
        StringBuilder stringBuilder = new StringBuilder();
        if (jResponse != null) {
            stringBuilder.append((jResponse == null ? "jResponse is null" : jResponse.toString()));
            stringBuilder.append("|bytebuffer:" + (byteBuffer == null ? "byteBuffer is null" : byteBuffer.toString()));
        }
        return stringBuilder.toString();
    }
}
