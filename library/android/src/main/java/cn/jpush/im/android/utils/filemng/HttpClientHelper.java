package cn.jpush.im.android.utils.filemng;

import org.apache.http.HttpVersion;
import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRouteBean;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpProtocolParams;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

/**
 * 注意所导入的包，故将引入的包也贴上，防止错误。
 */
public class HttpClientHelper {

    private static DefaultHttpClient httpClient;
    private static final String VERSION = "1.1";
    /**
     * http请求最大并发连接数
     */
    private static final int DEFAULT_MAX_CONNECTIONS = 10;
    /**
     * 超时时间，默认10秒
     */
    private static final int DEFAULT_SOCKET_TIMEOUT = 30 * 1000;
    /**
     * 默认的套接字缓冲区大小
     */
    private static final int DEFAULT_SOCKET_BUFFER_SIZE = 8192;
    private static int maxConnections = DEFAULT_MAX_CONNECTIONS;
    private static int socketTimeout = DEFAULT_SOCKET_TIMEOUT;

    private HttpClientHelper() {
    }

    public static synchronized DefaultHttpClient getHttpClient() {
        if (null == httpClient) {
            // 初始化工作
            try {
                KeyStore trustStore = KeyStore.getInstance(KeyStore
                        .getDefaultType());
                trustStore.load(null, null);
                BasicHttpParams httpParams = new BasicHttpParams();
                ConnManagerParams.setTimeout(httpParams, socketTimeout);
                ConnManagerParams.setMaxConnectionsPerRoute(httpParams,
                        new ConnPerRouteBean(maxConnections));
                ConnManagerParams.setMaxTotalConnections(httpParams,
                        DEFAULT_MAX_CONNECTIONS);

                HttpConnectionParams.setSoTimeout(httpParams, socketTimeout);
                HttpConnectionParams.setConnectionTimeout(httpParams,
                        socketTimeout);
                HttpConnectionParams.setTcpNoDelay(httpParams, true);
                HttpConnectionParams.setSocketBufferSize(httpParams,
                        DEFAULT_SOCKET_BUFFER_SIZE);

                HttpProtocolParams.setVersion(httpParams, HttpVersion.HTTP_1_1);
                HttpProtocolParams.setUserAgent(httpParams, String
                        .format("thinkandroid/%s (http://www.thinkandroid.cn)",
                                VERSION));

                // 设置 https支持
                SSLSocketFactory sf = new SSLSocketFactoryEx(trustStore);
                sf.setHostnameVerifier(SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER); // 允许所有主机的验证
                SchemeRegistry schReg = new SchemeRegistry();
                schReg.register(new Scheme("http", PlainSocketFactory
                        .getSocketFactory(), 80));
                schReg.register(new Scheme("https", sf, 443));
                schReg.register(new Scheme("https", sf, 8443));

                ClientConnectionManager conManager = new ThreadSafeClientConnManager(
                        httpParams, schReg);
                httpClient = new DefaultHttpClient(conManager, httpParams);
            } catch (Exception e) {
                e.printStackTrace();
                return new DefaultHttpClient();
            }
        }
        return httpClient;
    }
}

class SSLSocketFactoryEx extends SSLSocketFactory {
    SSLContext sslContext = SSLContext.getInstance("TLS");

    public SSLSocketFactoryEx(KeyStore truststore)
            throws NoSuchAlgorithmException, KeyManagementException,
            KeyStoreException, UnrecoverableKeyException {
        super(truststore);
        TrustManager tm = new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return null;
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] chain, String authType)
                    throws java.security.cert.CertificateException {

            }
        };

        sslContext.init(null, new TrustManager[]{tm}, null);
    }

    @Override
    public Socket createSocket(Socket socket, String host, int port,
                               boolean autoClose) throws IOException, UnknownHostException {
        return sslContext.getSocketFactory().createSocket(socket, host, port,
                autoClose);
    }

    @Override
    public Socket createSocket() throws IOException {
        return sslContext.getSocketFactory().createSocket();
    }
}