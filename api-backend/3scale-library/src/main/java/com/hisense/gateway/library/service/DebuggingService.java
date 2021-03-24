/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/11 @author peiyun
 */
package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.RequestInfo;

import java.util.Map;

public interface DebuggingService {
    Map<String, Object> debuggingRequest(Integer publishApiId, RequestInfo requestInfo);

    Result<Map<String, String>> getAppIdAndKeyParam(String partition, String environment, Integer publishApiId);

    Result<Map<String,String>> getApiByUserKey(String partition, String environment, Integer publishApiId,Integer systemId);

    Result<Map<String,String>> getApiByUserKey(String partition, String environment, Integer publishApiId);

    Result getPath( String partition, String environment, Integer publishApiId);
}
