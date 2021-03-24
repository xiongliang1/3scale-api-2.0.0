package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.eureka.EurekaPullConfig;
import com.hisense.gateway.library.model.pojo.base.EurekaPullApi;

public interface EurekaPullApiService {

    void createEurekaConfig(EurekaPullConfig config);

    Result<EurekaPullApi> findEurekaConfig();
}
