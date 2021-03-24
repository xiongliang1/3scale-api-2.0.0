/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.library.stud;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.stud.model.*;

import java.util.List;

public interface ServiceStud {
    List<Integer> serviceDtoIdList(String host, String accessToken);

    List<ServiceDto> serviceDtoList(String host, String accessToken);

    ServiceDto serviceDtoRead(String host, String accessToken, String id);

    ServiceXmlDto serviceXmlDtoRead(String host, String accessToken, String serviceId);

    List<AppPlanDto> appPlanDtoList(String host, String accessToken, String serviceId);

    List<AppPlanDto> allServicePlanDtoList(String host, String accessToken);

    List<ApplicationCount> countPlanApps(String host, String accessToken, String serviceId, Long accountId);

    AppPlanDto appPlanDtoRead(String host, String accessToken, String serviceId, String planId);

    List<ServiceDto> searchServiceByName(String host, String accessToken, String serviceName);

    MetricsDto getServiceMetricList(String host, String accessToken, String serviceId);

    MetricXmls getServiceMetricXmlList(String host, String accessToken, String serviceId);

    ServiceDto createService(String host, String accessToken, String name, String description, String systemName);

    void serviceDelete(String host, String accessToken, String serviceId);

    Result<Object> proxyUpdate(String host, String accessToken, String serviceId, String endpoint, String apiBackend,
                                   String sandboxEndPoint, Proxy proxy, String secretToken);

    void configPromote(String host, String accessToken, String serviceId, String env, String version, String to);

    ProxyConfigDto latestPromote(String host, String accessToken, Long id, String env);

    ProxyConfigDtos listPromote(String host, String accessToken, Long id, String env);

    ProxyConfigDto getConfigByVersion(String host, String accessToken, Long id, String env, String version);

    ServiceXmlDto createXmlService(String host, String accessToken, String name, String description, String systemName);

    void updateServiceDesc(String host, String accessToken, String serviceId, String description, String name);
}
