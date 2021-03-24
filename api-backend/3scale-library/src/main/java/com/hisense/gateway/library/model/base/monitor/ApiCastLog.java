package com.hisense.gateway.library.model.base.monitor;

import lombok.Data;

import java.util.Map;

/**
 * @author guilai.ming 2020/09/22
 * <p>
 * API网关日志
 */
@Data
public class ApiCastLog {
    int apiId;
    String apiName;
    long bytesSent;
    long bytesReceived;
    long costAll;
    String clientId;
    String clientIp;

    String domain;

    String httpVersion;
    String httpReferer;
    String httpXForwardedFor;

    String id;

    String phase;

    Map<String, String> reqHeaders;
    String requestMethod;
    String requestUri;
    String requestBody;
    long requestBodyReadCost;
    String requestTime;
    double responseTime;

    String responseBody;
    String respContentType;
    Map<String, String> respHeader;
    String responseCode;
    long responseBodyReadCost;
    String respConnection;

    long serviceId;
    String startTime;
    long timestamp;

    String userAgent;
    String userKey;

    String upstreamAddr;
}
