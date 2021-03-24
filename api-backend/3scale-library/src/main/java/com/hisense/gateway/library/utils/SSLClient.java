/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.library.utils;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.apache.http.conn.ClientConnectionManager;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.apache.http.impl.client.DefaultHttpClient;


public class SSLClient extends DefaultHttpClient {
    public SSLClient() throws Exception {
        super();
        //传输协议需要根据自己的判断
        SSLContext ctx = SSLContext.getInstance("TLS");
        TrustManager[] tm = { new MyX509TrustManager() };
        ctx.init(null, tm, null);
        SSLSocketFactory ssf = new SSLSocketFactory(ctx, SSLSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER);
        ClientConnectionManager ccm = this.getConnectionManager();
        SchemeRegistry sr = ccm.getSchemeRegistry();
        sr.register(new Scheme("https", 443, ssf));
    }

    class MyX509TrustManager implements X509TrustManager {
        // 检查客户端证书
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        // 检查服务器端证书
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
        }
        // 返回受信任的X509证书数组
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }
    }
}
