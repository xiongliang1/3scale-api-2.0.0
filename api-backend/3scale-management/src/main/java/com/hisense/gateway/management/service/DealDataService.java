package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;

public interface DealDataService {
    Result<Boolean> initApiData(String tenantId, String instanceName, Integer groupId);

    Result<Boolean> initApiDataById(String tenantId, String instanceName, String scaleId, Integer apiId, Integer groupId);
}
