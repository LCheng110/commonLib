package cn.jpush.im.android.pushcommon.proto.common.utils;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;

import cn.jiguang.ald.api.JResponse;

public class ProtocolUtil {
    private static final String TAG = "ProtocolUtil";
	private static final String ENCODING_UTF_8 = "UTF-8";
		
	public static byte[] getBytesConsumed(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		buffer.get(bytes);
		return bytes;
	}
	
	public static byte[] getBytes(ByteBuffer buffer) {
		byte[] bytes = new byte[buffer.remaining()];
		ByteBuffer copied = buffer.asReadOnlyBuffer();
		copied.flip();
		buffer.get(bytes);
		return bytes;
	}

	
	public static byte[] tlv2ToByteArray(String value) {
		if (null == value || "".equals(value)) {
			return new byte[] {0, 0};
		}
		
    	byte[] valueBytes = null;
		try {
			valueBytes = value.getBytes(ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		
		short len = (short) valueBytes.length;
		byte[] result = new byte[len + 2];
		
		System.arraycopy(short2ByteArray(len), 0, result, 0, 2);
		System.arraycopy(valueBytes, 0, result, 2, len);
		return result;
	}
	
	public static byte[] fixedStringToBytes(String value, int length) {
		if (null == value || "".equals(value)) {
			return new byte[] {0, 0, 0, 0};
		}
		
		byte[] valueBytes = null;
		try {
			valueBytes = value.getBytes(ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		byte[] results = ProtocolUtil.getDefaultByte(length);
		int readLength = valueBytes.length > length ? length : valueBytes.length;
		System.arraycopy(valueBytes, 0, results, 0, readLength);
		return results;
	}

    public static void fillIntData(byte[] data, int intValue, int pos) {
        System.arraycopy(int2ByteArray(intValue), 0, data, pos, 4);
    }

    public static void fillStringData(byte[] data, String str, int pos) {
        byte[] tmp = str.getBytes();
        System.arraycopy(tmp, 0, data, pos, tmp.length);
    }


    public static void main(String[] args) {
		String s = "ab";
		byte[] b = fixedStringToBytes(s,4);
		System.out.println(StringUtils.toHexLog(b));
	}


	public static byte[] getDefaultByte(int byteSize) {
		byte[] data = new byte[byteSize];
		for (int i = 0; i < data.length; i++) {
			data[0] = 0;
		}
		return data;
	}
	
	public static String getTlv2(ByteBuffer buffer, JResponse jResponse) {
    	int len = ByteBufferUtils.getShort(buffer,jResponse);//buffer.getShort();
    	byte[] bytes = new byte[len];
		ByteBufferUtils.get(buffer,bytes,jResponse);//buffer.get(bytes);
		try {
			return new String(bytes, ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}
	public static String getTlv2(ByteBuffer buffer) {
    	int len = buffer.getShort();
    	byte[] bytes = new byte[len];
    	buffer.get(bytes);
		try {
			return new String(bytes, ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	public static String getString(ByteBuffer buffer, int length) {
    	byte[] bytes = new byte[length];
    	buffer.get(bytes);
		try {
			return new String(bytes, ENCODING_UTF_8);
		} catch (UnsupportedEncodingException e) {
		}
		return null;
	}

	
	//=======================================================
	// from http://cobweb.cs.uga.edu/~budak/4370_S14/Conversions.java
	
	public static byte [] short2ByteArray (short value) {
        return new byte [] { (byte) (value >>> 8),
                             (byte) value };
    }

    public static byte [] int2ByteArray (int value) {
        return new byte [] { (byte) (value >>> 24),
                             (byte) (value >>> 16),
                             (byte) (value >>> 8),
                             (byte) value };
    }
    
    public static byte [] long2ByteArray (long value) {
        return new byte [] { (byte) (value >>> 56),
                             (byte) (value >>> 48),
                             (byte) (value >>> 40),
                             (byte) (value >>> 32),
                             (byte) (value >>> 24),
                             (byte) (value >>> 16),
                             (byte) (value >>> 8),
                             (byte) value };
    } 
    
}
