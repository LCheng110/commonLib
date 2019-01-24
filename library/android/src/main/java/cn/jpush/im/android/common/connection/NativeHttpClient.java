package cn.jpush.im.android.common.connection;


import android.text.TextUtils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import cn.jiguang.ald.api.JCoreInterface;
import cn.jpush.im.android.common.resp.APIConnectionException;
import cn.jpush.im.android.common.resp.APIRequestException;
import cn.jpush.im.android.common.resp.ResponseWrapper;
import cn.jpush.im.android.utils.Logger;
import cn.jpush.im.android.utils.StringUtils;


/**
 * The implementation has no connection pool mechanism, used origin java connection.
 * <p/>
 * 本实现没有连接池机制，基于 Java 原始的 HTTP 连接实现。
 * <p/>
 * 遇到连接超时，会自动重连指定的次数（默认为 3）；如果是读取超时，则不会自动重连。
 * <p/>
 * 可选支持 HTTP 代理，同时支持 2 种方式：1) HTTP 头上加上 Proxy-Authorization 信息；2）全局配置 Authenticator.setDefault；
 */
public class NativeHttpClient {

    private static final String TAG = "NativeHttpClient";

    public static final String CHARSET = "UTF-8";

    public static final String CONTENT_TYPE_JSON = "application/json";

    public static final String CONTENT_TYPE_FORM = "application/x-www-form-urlencoded";

    public static final String RATE_LIMIT_QUOTA = "X-Rate-Limit-Limit";

    public static final String RATE_LIMIT_Remaining = "X-Rate-Limit-Remaining";

    public static final String RATE_LIMIT_Reset = "X-Rate-Limit-Reset";

    public static final String PROTOBUF_MESSAGE = "X-Protobuf-Message";

    public static final String JPUSH_USER_AGENT = "JPush-IM-Android-Client";

    public static final int RESPONSE_OK = 200;

    public enum RequestMethod {
        GET,
        POST,
        DELETE,
        PUT
    }

    public static final String IO_ERROR_MESSAGE = "Connection IO error. \n"
            + "Can not connect to JPush Server. "
            + "Please ensure your internet connection is ok. \n"
            + "If the problem persists, please let us know at support@jpush.cn.";

    public static final String CONNECT_TIMED_OUT_MESSAGE = "connect timed out. \n"
            + "Connect to JPush Server timed out, and already retried some times. \n"
            + "Please ensure your internet connection is ok. \n"
            + "If the problem persists, please let us know at support@jpush.cn.";

    public static final String READ_TIMED_OUT_MESSAGE = "Read timed out. \n"
            + "Read response from JPush Server timed out. \n"
            + "If this is a Push action, you may not want to retry. \n"
            + "It may be due to slowly response from JPush server, or unstable connection. \n"
            + "If the problem persists, please let us know at support@jpush.cn.";

    public static final String NO_AUTH_ERROR_MESSAGE = "No authentication challenges found";

    //设置连接超时时间
    public static final int DEFAULT_CONNECTION_TIMEOUT = (10 * 1000); // milliseconds

    //设置读取超时时间
    public static final int DEFAULT_READ_TIMEOUT = (30 * 1000); // milliseconds

    public static final int DEFAULT_MAX_RETRY_TIMES = 3;

    private static final String KEYWORDS_CONNECT_TIMED_OUT = "connect timed out";

    private static final String KEYWORDS_READ_TIMED_OUT = "Read timed out";

    private int _maxRetryTimes = 0;

    private String _authCode;



    /**
     * 默认的重连次数是 3
     */
    public NativeHttpClient(String authCode) {
        this(authCode, DEFAULT_MAX_RETRY_TIMES);
    }

    public NativeHttpClient(String authCode, int maxRetryTimes) {
        this._maxRetryTimes = maxRetryTimes;
        Logger.i(TAG, "Created instance with _maxRetryTimes = " + _maxRetryTimes);
        this._authCode = authCode;
//        initSSL();
    }

    public ResponseWrapper sendGet(String url, String authCode)
            throws APIConnectionException, APIRequestException {
        return doRequest(url, null, RequestMethod.GET, authCode);
    }

    public ResponseWrapper sendDelete(String url, String authCode)
            throws APIConnectionException, APIRequestException {
        return doRequest(url, null, RequestMethod.DELETE, authCode);
    }

    public ResponseWrapper sendPost(String url, String content, String authCode)
            throws APIConnectionException, APIRequestException {
        return doRequest(url, content, RequestMethod.POST, authCode);
    }

    public ResponseWrapper sendPut(String url, String content, String authCode)
            throws APIConnectionException, APIRequestException {
        return doRequest(url, content, RequestMethod.PUT, authCode);
    }

    public ResponseWrapper doRequest(String url, String content, RequestMethod method,
                                     String authCode) throws APIConnectionException, APIRequestException {
        ResponseWrapper response = null;
        for (int retryTimes = 0; ; retryTimes++) {
            try {
                response = _doRequest(url, content, method, authCode);
                break;
            } catch (SocketTimeoutException e) {
                if (KEYWORDS_READ_TIMED_OUT.equals(e.getMessage())) {
                    // Read timed out.  For push, maybe should not re-send.
                    throw new APIConnectionException(READ_TIMED_OUT_MESSAGE, e, true);
                } else {    // connect timed out
                    if (retryTimes >= _maxRetryTimes) {
                        throw new APIConnectionException(CONNECT_TIMED_OUT_MESSAGE, e, retryTimes);
                    } else {
                        Logger.d(TAG, "connect timed out - retry again - " + (retryTimes + 1));
                    }
                }
            }
        }
        return response;
    }

    private ResponseWrapper _doRequest(String url, String content, RequestMethod method,
                                       String authCode)
            throws APIConnectionException, APIRequestException, SocketTimeoutException {

        Logger.d(TAG, "Send request - " + method.toString() + " " + url);
        if (null != content) {
            Logger.d(TAG, "Request Content - " + content);
        }

        HttpURLConnection conn = null;
        OutputStream out = null;
        StringBuffer sb = new StringBuffer();
        ResponseWrapper wrapper = new ResponseWrapper();

        try {
            if (StringUtils.isSSL(url)) {
                initSSL();
            }
            URL aUrl = new URL(url);
            conn = (HttpURLConnection) aUrl.openConnection();
            conn.setConnectTimeout(DEFAULT_CONNECTION_TIMEOUT);
            conn.setReadTimeout(DEFAULT_READ_TIMEOUT);
            conn.setUseCaches(false);
            conn.setRequestMethod(method.name());
            conn.setRequestProperty("User-Agent", JPUSH_USER_AGENT);
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept-Charset", CHARSET);
            conn.setRequestProperty("Charset", CHARSET);
            conn.setRequestProperty("Authorization", authCode);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE_JSON);
            conn.setRequestProperty("X-App-Key", JCoreInterface.getAppKey());
            conn.setRequestProperty("jm-channel", "m");//支持多通道，要求所有请求都带上通道类型，移动端固定带"m"
            if (RequestMethod.GET == method || RequestMethod.DELETE == method) {
                conn.setDoOutput(false);
            } else if (RequestMethod.PUT == method || RequestMethod.POST == method) {
                conn.setDoOutput(true);
                byte[] data = content.getBytes(CHARSET);
                conn.setRequestProperty("Content-Length", String.valueOf(data.length));
                out = conn.getOutputStream();
                out.write(data);
                out.flush();
            }
            int status;
            try {
                // Will throw IOException if server responds with 401.
                status = conn.getResponseCode();
            } catch (IOException e) {
                // Will return 401, because now connection has the correct internal state.
                status = conn.getResponseCode();
            }
            InputStream in = null;
            if (status >= 200 && status < 300) {
                in = conn.getInputStream();
            } else {
                in = conn.getErrorStream();
            }

            if (null != in) {
                String message = conn.getHeaderField(PROTOBUF_MESSAGE);
                if (!TextUtils.isEmpty(message)) {
                    ByteArrayOutputStream swapStream = new ByteArrayOutputStream();
                    byte[] buff = new byte[1024];
                    int rc;
                    while ((rc = in.read(buff, 0, buff.length)) > 0) {
                        swapStream.write(buff, 0, rc);
                    }
                    swapStream.close();
                    wrapper.rawData = swapStream.toByteArray();
                } else {
                    InputStreamReader reader = new InputStreamReader(in, CHARSET);
                    char[] buff = new char[1024];
                    int len;
                    while ((len = reader.read(buff)) > 0) {
                        sb.append(buff, 0, len);
                    }
                }
                in.close();
            }

            String responseContent = sb.toString();
            wrapper.responseCode = status;
            wrapper.responseContent = responseContent;

            String quota = conn.getHeaderField(RATE_LIMIT_QUOTA);
            String remaining = conn.getHeaderField(RATE_LIMIT_Remaining);
            String reset = conn.getHeaderField(RATE_LIMIT_Reset);
            wrapper.setRateLimit(quota, remaining, reset);

            if (status >= 200 && status < 300) {
                Logger.d(TAG, "Succeed to get response - " + status);
                Logger.d(TAG, "Response Content - " + responseContent);

            } else if (status >= 300 && status < 400) {
                Logger.w(TAG, "Normal response but unexpected - responseCode:" + status
                        + ", responseContent:" + responseContent);

            } else {
                Logger.w(TAG, "Got error response - responseCode:" + status + ", responseContent:"
                        + responseContent);

                switch (status) {
                    case 400:
                        wrapper.setErrorObject();
                        break;
                    case 401:
                        wrapper.setErrorObject();
                        break;
                    case 403:
                        wrapper.setErrorObject();
                        break;
                    case 410:
                        wrapper.setErrorObject();
                    case 429:
                        wrapper.setErrorObject();
                        break;
                    case 500:
                    case 502:
                    case 503:
                    case 504:
                        break;
                    default:
                }

                throw new APIRequestException(wrapper);
            }

        } catch (SocketTimeoutException e) {
            if (null != e.getMessage() && e.getMessage().contains(KEYWORDS_CONNECT_TIMED_OUT)) {
                throw e;
            } else if (null != e.getMessage() && e.getMessage().contains(KEYWORDS_READ_TIMED_OUT)) {
                throw new SocketTimeoutException(KEYWORDS_READ_TIMED_OUT);
            }
            Logger.d(TAG, IO_ERROR_MESSAGE, e);
            throw new APIConnectionException(IO_ERROR_MESSAGE, e);

        } catch (IOException e) {
            Logger.d(TAG, IO_ERROR_MESSAGE, e);
            throw new APIConnectionException(IO_ERROR_MESSAGE, e);

        } finally {
            if (null != out) {
                try {
                    out.close();
                } catch (IOException e) {
                    Logger.e(TAG, "Failed to close stream.", e);
                }
            }
            if (null != conn) {
                conn.disconnect();
            }
        }

        return wrapper;
    }


    protected void initSSL() {
        TrustManager[] tmCerts = new TrustManager[1];
        tmCerts[0] = new SimpleTrustManager();
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            sslContext.init(null, tmCerts, null);
            HttpsURLConnection.setDefaultSSLSocketFactory(sslContext.getSocketFactory());

            HostnameVerifier hostnameVerifier = new SimpleHostnameVerifier();
            HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifier);
        } catch (Exception e) {
            Logger.e(TAG, "Init SSL error", e);
        }
    }


    private static class SimpleHostnameVerifier implements HostnameVerifier {

        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }

    }

    private static class SimpleTrustManager implements TrustManager, X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return;
        }

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            return;
        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }

    private static class SimpleProxyAuthenticator extends Authenticator {

        private String username;

        private String password;

        public SimpleProxyAuthenticator(String username, String password) {
            this.username = username;
            this.password = password;
        }

        protected PasswordAuthentication getPasswordAuthentication() {
            return new PasswordAuthentication(this.username, this.password.toCharArray());
        }
    }
}
