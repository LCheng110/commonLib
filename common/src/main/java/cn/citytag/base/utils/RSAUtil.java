package cn.citytag.base.utils;

import android.util.Base64;

import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.security.Key;
import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Security;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Properties;

import javax.crypto.Cipher;

/**
 * Created by baoyiwei on 2018/7/6.
 */

public class RSAUtil {

    public static final String KEY_ALGORITHM = "RSA";// RSA/ECB/PKCS1Padding

    /**
     * String to hold name of the encryption padding.
     */
    public static final String PADDING = "RSA/NONE/PKCS1Padding";// RSA/NONE/NoPadding

    /**
     * String to hold name of the security provider.
     */
    public static final String PROVIDER = "BC";

    /** */
    /**
     * 签名算法
     */
    public static final String SIGNATURE_ALGORITHM = "MD5withRSA";

    /** */
    /**
     * 获取公钥的key
     */
    private static final String PUBLIC_KEY = "MDwwDQYJKoZIhvcNAQEBBQADKwAwKAIhAMMzrxrRJAkmG9VD+TXOjbOoMRsraOqDn/PXt3ry0MibAgMBAAE=";

    /** */
    /**
     * 获取私钥的key
     */
    private static final String PRIVATE_KEY = "MIIBVQIBADANBgkqhkiG9w0BAQEFAASCAT8wggE7AgEAAkEAoygEv6CZTv12YCpvXkh/Cn4b5He/LC34dQMfvrYHjJ3AN2q0y8hYeqBdEBTFzHUfAWw/UZYA1DJWhVxv74X+sQIDAQABAkBzZlDFgUAuYJQjvJ/83eYNc9kipZfwFavQh58icvmv+Jrnly4x4YEz2sJ/VcxEPIBhiFb90dEQbPxkkjb0sLSBAiEAzjjiJmwq5QZFsPrz1Vz9zcBQ8j1XHD/YpN1K6xkYSdkCIQDKifWetC43rh88GnkYUf4cyc/ecnhE30sZMuf6yWs8mQIhAMAk4hoYuNLRyEeBW4WWcmit4v4Bx1Nr7aiXA8IPjMDpAiEAk0Av6d03iNW38buRdMQPyqBS13hDxlbNQ5nGHIeZxmkCIDIj6WDBXNH9nOYDHR9LSWIhPfT9kXu3vQt4c1LLuIHE";

    /** */
    /**
     * RSA最大加密明文大小
     */
    private static final int MAX_ENCRYPT_BLOCK = 117;

    /** */
    /**
     * RSA最大解密密文大小
     */
    private static final int MAX_DECRYPT_BLOCK = 128;
    private static int sBase64Mode = Base64.DEFAULT;
    /*
     * 公钥加密
     */
    public static String encryptByPublicKey(String str) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);

        // 获得公钥
        Key publicKey = getPublicKey();

        // 用公钥加密
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        // 读数据源
        byte[] data = str.getBytes("UTF-8");

        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();

        return Base64Util.encode(encryptedData);
    }

    /**
     * 私钥加密
     *
     * @param str
     * @return
     * @throws Exception
     * @author kokJuis
     * @date 2016-4-7 下午12:53:15
     * @comment
     */
    public static String encryptByPrivateKey(String str) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);

        // 获得私钥
        Key privateKey = getPrivateKey();

        // 用私钥加密
        cipher.init(Cipher.ENCRYPT_MODE, privateKey);
        // 读数据源
        byte[] data = str.getBytes("UTF-8");

        int inputLen = data.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段加密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_ENCRYPT_BLOCK) {
                cache = cipher.doFinal(data, offSet, MAX_ENCRYPT_BLOCK);
            } else {
                cache = cipher.doFinal(data, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_ENCRYPT_BLOCK;
        }
        byte[] encryptedData = out.toByteArray();
        out.close();

        return Base64Util.encode(encryptedData);
    }

    /*
     * 公钥解密
     */
    public static String decryptByPublicKey(String str) throws Exception {
        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);

        // 获得公钥
        Key publicKey = getPublicKey();

        // 用公钥解密
        cipher.init(Cipher.DECRYPT_MODE, publicKey);

        // 读数据源
        byte[] encryptedData = Base64Util.decode(str);

        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher
                        .doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher
                        .doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();

        return new String(decryptedData, "UTF-8");
    }

    /*
     * 私钥解密
     */
    public static String decryptByPrivateKey(String str) throws Exception {

        Security.addProvider(new BouncyCastleProvider());

        Cipher cipher = Cipher.getInstance(PADDING, PROVIDER);
        // 得到Key
        Key privateKey = getPrivateKey();
        // 用私钥去解密
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        // 读数据源
        byte[] encryptedData = Base64Util.decode(str);

        int inputLen = encryptedData.length;
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int offSet = 0;
        byte[] cache;
        int i = 0;
        // 对数据分段解密
        while (inputLen - offSet > 0) {
            if (inputLen - offSet > MAX_DECRYPT_BLOCK) {
                cache = cipher
                        .doFinal(encryptedData, offSet, MAX_DECRYPT_BLOCK);
            } else {
                cache = cipher
                        .doFinal(encryptedData, offSet, inputLen - offSet);
            }
            out.write(cache, 0, cache.length);
            i++;
            offSet = i * MAX_DECRYPT_BLOCK;
        }
        byte[] decryptedData = out.toByteArray();
        out.close();

        // 二进制数据要变成字符串需解码
        return new String(decryptedData, "UTF-8");
    }

    /**
     * 从文件中读取公钥
     *
     * @return
     * @throws Exception
     * @author kokJuis
     * @date 2016-4-6 下午4:38:22
     * @comment
     */
    private static Key getPublicKey() throws Exception {



        byte[] keyBytes;
        keyBytes = Base64Util.decode(PUBLIC_KEY);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PublicKey publicKey = keyFactory.generatePublic(keySpec);
        return publicKey;
    }

    /**
     * 从文件中读取公钥String
     *
     * @return
     * @throws Exception
     * @author kokJuis
     * @date 2016-4-6 下午4:38:22
     * @comment
     */
    public static String getStringPublicKey() throws Exception {
        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("rsa_key.properties");
        Properties properties = new Properties();
        properties.load(stream);

        String key = properties.getProperty(PUBLIC_KEY);

        return key;
    }

    /**
     * 获取私钥
     *
     * @return
     * @throws Exception
     * @author kokJuis
     * @date 2016-4-7 上午11:46:12
     * @comment
     */
    private static Key getPrivateKey() throws Exception {

        InputStream stream = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("rsa_key.properties");
        Properties properties = new Properties();
        properties.load(stream);

        String key = properties.getProperty(PRIVATE_KEY);

        byte[] keyBytes;
        keyBytes = Base64Util.decode(key);
        PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
        return privateKey;


    }
}