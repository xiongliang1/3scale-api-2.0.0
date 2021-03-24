/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/20 @author peiyun
 */
package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleDto;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Proxy {

    private String serviceId;
    /**production base url */
    private String endpoint;
    /** "api_backend": "https://echo-api.3scale.net:443" */
    private String apiBackend;
    /** "credentials_location": "query" */
    private String credentialsLocation;

    /** "error_auth_failed": "Authentication failed" */
    private String errorAuthFailed;
    /** "error_status_auth_failed": 403 */
    private String errorStatusAuthFailed;
    /** "text/plain; charset=us-ascii" */
    private String errorHeadersAuthFailed;

    /** "error_auth_missing": "Authentication parameters missing" */
    private String errorAuthMissing;
    /** "error_status_auth_missing": 403 */
    private String errorStatusAuthMissing;
    /** "error_headers_auth_missing": "text/plain; charset=us-ascii" */
    private String errorHeadersAuthMissing;

    /** "error_no_match": "No Mapping Rule matched" */
    private String errorNoMatch;
    /** "error_status_no_match": 404 */
    private String errorStatusNoMatch;
    /**  "error_headers_no_match": "text/plain; charset=us-ascii" */
    private String errorHeadersNoMatch;

    /** "error_limits_exceeded": "Usage limit exceeded" */
    private String errorLimitsExceeded;
    /** "error_status_limits_exceeded": 429 */
    private String errorStatusLimitsExceeded;
    /** "error_headers_limits_exceeded": "text/plain; charset=us-ascii" */
    private String errorHeadersLimitsExceeded;

    //staging base url
    private String sandboxEndpoint;
    //测试的路径
    private String apiTestPath;
    private String createdAt;
    private String updatedAt;
    private String hostnameRewrite;

	private String id;

    private String tenantId;

    private String authAppId;

    private String authAppKey;

    private List<ApiMappingRuleDto> proxyRules;

    private List<ProxyPolicy> policyChain;
}
