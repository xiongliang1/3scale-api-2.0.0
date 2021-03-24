/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.stud.model.*;

import java.util.List;

public interface ServicesService {
    ServiceDtos getPageServiceDtoList(String domain, int pageNum, int pageSize);

    ServiceDto getServiceDto(String domain, String serviceId);

    List<AppPlanDto> getAppPlanDtoList(String domain, String serviceId);

    List<ApplicationCount> getPlanAppCount(String domainName, String serviceId, String userName);

    ServiceDtos searchPageServiceDtoByName(String domain, String serviceName, int pageNum, int pageSize);

    MetricsDto getServiceMetricList(String domainName, String serviceId);

    ServiceDtos getServiceClassifies(String domain, int pageNum, int pageSize);

    ServiceDtos getPageServiceDtoBySysNameList(String domain, String target, String secTarget, int pageNum, int pageSize);

    ServiceDtos searchPageServiceDtoBySysNameAndName(String domain, String serviceName, String target, String secTarget, int pageNum, int pageSize);
}
