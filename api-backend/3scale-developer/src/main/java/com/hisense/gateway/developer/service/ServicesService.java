/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.developer.service;

import com.hisense.gateway.library.model.dto.portal.ApplicationCountRes;
import com.hisense.gateway.library.model.dto.portal.ServiceResDto;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.model.dto.portal.InstanceDto;
import com.hisense.gateway.library.model.dto.portal.ServiceResDtos;

import java.util.List;
import java.util.Map;

public interface ServicesService {

    ServiceDtos getPageServiceDtoList(String domain, int pageNum, int pageSize);

    ServiceDto getServiceDto(String domain, String id);

    List<AppPlanDto> getAppPlanDtoList(String domain, String serviceId);

    List<ApplicationCount> getPlanAppCount(String domainName, String serviceId, String userName);

    ServiceDtos searchPageServiceDtoByName(String domain, String serviceName, int pageNum, int pageSize);

    MetricsDto getServiceMetricList(String domainName, String serviceId);

    ServiceDtos getServiceClassifies(String domain, int pageNum, int pageSize);

    ServiceDtos getPageServiceDtoBySysNameList(String domain, String target, String secTarget, int pageNum, int pageSize);

    ServiceDtos searchPageServiceDtoBySysNameAndName(String domain, String serviceName, String target, String secTarget, int pageNum, int pageSize);

    ServiceResDtos findDataItem(String instanceName, int pageNum, int pageSize);

    ServiceResDtos getPageServiceDtoByCategory(String domain, String target, String secTarget, Integer system, int pageNum, int pageSize);

    ServiceResDtos searchPageServiceDtoByNameAndSystem(String domain, String serviceName, String target, String secTarget,
                                                       Integer system, int pageNum, int pageSize);

    ServiceResDtos searchPageServiceResDtoByName(String domain, String serviceName, int pageNum, int pageSize);

    ServiceResDto getServiceResDtos(String domain, Integer id);

    List<AppPlanDto> getAppPlanDtoListNew(String domain, Integer serviceId);

    List<ApplicationCountRes> getPlanAppCountNew(String domain, Integer serviceId, String userName);

    List<Map<String, Object>> findSystem(String systemName);
    
    InstanceDto getInstanceDto(String domain);
}
