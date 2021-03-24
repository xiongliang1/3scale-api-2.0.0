/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisense.gateway.library.constant.SystemNameConstant;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.pojo.base.Domain;
import com.hisense.gateway.library.repository.DomainRepository;
import com.hisense.gateway.management.service.ServicesService;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.ServiceStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@org.springframework.stereotype.Service
public class ServicesServiceImpl implements ServicesService {
    @Autowired
    DomainRepository domainRepository;

    @Autowired
    ServiceStud serviceStud;

    @Autowired
    AccountStud accountStud;

    private List<ServiceDto> serviceDtoList;

    @Override
    public ServiceDtos getPageServiceDtoList(String domainName, int pageNum, int pageSize) {
        ServiceDtos serviceDtos = new ServiceDtos();
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        serviceDtoList = serviceStud.serviceDtoList(domain.getHost(), domain.getAccessToken());
        Pagination pagination = new Pagination(pageNum, pageSize, serviceDtoList.size());
        serviceDtos.setServices(serviceDtoList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        serviceDtos.setTotalRecord(serviceDtoList.size());
        return serviceDtos;
    }

    @Override
    public ServiceDto getServiceDto(String domainName, String id) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        ServiceDto serviceDto = serviceStud.serviceDtoRead(domain.getHost(), domain.getAccessToken(), id);
        if (StringUtils.isBlank(serviceDto.getService().getDescription())) {
            serviceDto.getService().setTarget("未分类");
            serviceDto.getService().setSecTarget("未分类");
            serviceDto.getService().setDescription("");
        } else {
            String originalDescription = serviceDto.getService().getDescription();
            int index = originalDescription.indexOf("|");
            if (index == -1) {
                serviceDto.getService().setTarget("未分类");
                serviceDto.getService().setSecTarget("未分类");
                serviceDto.getService().setDescription(originalDescription);
            } else {
                serviceDto.getService().setTarget(originalDescription.substring(0, index));// 一级菜单
                String originalDescription2 = originalDescription.substring((index + 1), originalDescription.length());
                int index2 = originalDescription2.indexOf("|");
                if (index2 == -1) {//没有二级菜单
                    serviceDto.getService().setSecTarget("未分类");
                    serviceDto.getService().setDescription(originalDescription2);
                } else {// 有二级菜单
                    serviceDto.getService().setSecTarget(originalDescription2.substring(0, index2));
                    serviceDto.getService().setDescription(originalDescription2.substring((index2 + 1), originalDescription2.length()));
                }
            }
        }
        return serviceDto;
    }

    @Override
    public List<AppPlanDto> getAppPlanDtoList(String domainName, String serviceId) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }
        return serviceStud.appPlanDtoList(domain.getHost(), domain.getAccessToken(), serviceId);
    }

    @Override
    public List<ApplicationCount> getPlanAppCount(String domainName, String serviceId, String userName) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

      /*  String user = SecurityUtils.getLoginUser().getUsername();
        if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/

        return serviceStud.countPlanApps(domain.getHost(), domain.getAccessToken(), serviceId, null);
    }

    @Override
    public ServiceDtos searchPageServiceDtoByName(String domainName, String serviceName, int pageNum, int pageSize) {
        ServiceDtos serviceDtos = new ServiceDtos();
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        serviceDtoList = serviceStud.searchServiceByName(domain.getHost(), domain.getAccessToken(), serviceName);
        int totalRecord = serviceDtoList.size();
        Pagination pagination = new Pagination(pageNum, pageSize, totalRecord);
        serviceDtos.setServices(serviceDtoList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        serviceDtos.setTotalRecord(serviceDtoList.size());
        return serviceDtos;
    }

    @Override
    public MetricsDto getServiceMetricList(String domainName, String serviceId) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        MetricsDto metricsDto = new MetricsDto();
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        MetricXmls metricXmls = serviceStud.getServiceMetricXmlList(domain.getHost(), domain.getAccessToken(), serviceId);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        try {
            String jsonStr = mapper.writeValueAsString(metricXmls);
            metricsDto = mapper.readValue(jsonStr, MetricsDto.class);
        } catch (JsonProcessingException e) {
            log.error("context",e);
        } catch (IOException e) {
            log.error("context",e);
        }
        return metricsDto;
    }

    @Override
    public ServiceDtos getServiceClassifies(String domainName, int pageNum, int pageSize) {
        ServiceDtos serviceDtos = new ServiceDtos();
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        serviceDtoList = serviceStud.serviceDtoList(domain.getHost(), domain.getAccessToken());
        // 分组
        rebuildServiceDtoList(serviceDtoList);
        Map<String, List<ServiceDto>> resultMap = serviceDtoList.stream()
                .collect(Collectors.groupingBy(serviceDto -> serviceDto.getService().getTarget()));
        log.info("getServiceClassifies info resultMap,resultMap is {}", JSONObject.toJSONString(resultMap));

        List<ServiceDto> services = new ArrayList<>();
        for (Map.Entry<String, List<ServiceDto>> entry : resultMap.entrySet()) {
            ServiceDto serviceDto = new ServiceDto();
            serviceDto.setTarget(entry.getKey());

            Map<String, List<ServiceDto>> resultMap2 = entry.getValue().stream()
                    .collect(Collectors.groupingBy(item -> item.getService().getSecTarget()));
            List<String> secTarget = new ArrayList<>();
            for (Map.Entry<String, List<ServiceDto>> entry2 : resultMap2.entrySet()) {
                secTarget.add(entry2.getKey());
            }
            serviceDto.setSecTarget(secTarget);
            services.add(serviceDto);
        }
        log.info("getServiceClassifies info ,services is {}", JSONObject.toJSONString(services));
        Pagination pagination = new Pagination(pageNum, pageSize, services.size());
        serviceDtos.setServices(services.subList(pagination.getFromIndex(), pagination.getToIndex()));
        serviceDtos.setTotalRecord(services.size());

        return serviceDtos;
    }

    private void rebuildServiceDtoList(List<ServiceDto> serviceDtoList) {
        serviceDtoList.stream().forEach(serviceDto2 -> {
            if (StringUtils.isBlank(serviceDto2.getService().getDescription())) {
                serviceDto2.getService().setTarget("未分类");
                serviceDto2.getService().setSecTarget("未分类");
            } else {
                String originalDescription = serviceDto2.getService().getDescription();
                int index = originalDescription.indexOf("|");
                if (index == -1) {
                    serviceDto2.getService().setTarget("未分类");
                    serviceDto2.getService().setSecTarget("未分类");
                    serviceDto2.getService().setDescription(originalDescription);
                } else {
                    serviceDto2.getService().setTarget(originalDescription.substring(0, index));// 一级菜单
                    String originalDescription2 = originalDescription.substring((index + 1), originalDescription.length());
                    int index2 = originalDescription2.indexOf("|");
                    if (index2 == -1) {//没有二级菜单
                        serviceDto2.getService().setSecTarget("未分类");
                        serviceDto2.getService().setDescription(originalDescription2);
                    } else {// 有二级菜单
                        serviceDto2.getService().setSecTarget(originalDescription2.substring(0, index2));
                        serviceDto2.getService().setDescription(originalDescription2.substring((index2 + 1), originalDescription2.length()));
                    }
                }
            }
        });
    }

    @Override
    public ServiceDtos getPageServiceDtoBySysNameList(String domainName, String target, String secTarget, int pageNum, int pageSize) {
        ServiceDtos serviceDtos = new ServiceDtos();
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        serviceDtoList = serviceStud.serviceDtoList(domain.getHost(), domain.getAccessToken());
        rebuildServiceDtoList(serviceDtoList);
        if (SystemNameConstant.ALL.equals(target)) {
            // 查全部的分页
            Pagination pagination = new Pagination(pageNum, pageSize, serviceDtoList.size());
            serviceDtoList.stream().forEach(serviceDto2 -> serviceDto2.setTarget(target));
            serviceDtos.setServices(serviceDtoList.subList(pagination.getFromIndex(), pagination.getToIndex()));
            serviceDtos.setTotalRecord(serviceDtoList.size());
            return serviceDtos;
        }

        // 过滤分页
        List<ServiceDto> resultList = new ArrayList<>();
        if (SystemNameConstant.ALL.equals(secTarget)) {
            resultList = serviceDtoList.stream()
                    .filter(serviceDto -> serviceDto.getService().getTarget().equals(target)).collect(Collectors.toList());
        } else {
            resultList = serviceDtoList.stream()
                    .filter(serviceDto -> (serviceDto.getService().getTarget().equals(target) &&
                            serviceDto.getService().getSecTarget().equals(secTarget))).collect(Collectors.toList());
        }

        resultList.stream().forEach(serviceDto2 -> serviceDto2.setTarget(target));
        Pagination pagination = new Pagination(pageNum, pageSize, resultList.size());
        serviceDtos.setServices(resultList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        serviceDtos.setTotalRecord(resultList.size());
        return serviceDtos;
    }

    @Override
    public ServiceDtos searchPageServiceDtoBySysNameAndName(String domainName, String serviceName, String target,
                                                            String secTarget, int pageNum, int pageSize) {
        ServiceDtos serviceDtos = new ServiceDtos();
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        serviceDtoList = serviceStud.searchServiceByName(domain.getHost(), domain.getAccessToken(), serviceName);
        rebuildServiceDtoList(serviceDtoList);
        if (SystemNameConstant.ALL.equals(target)) {
            // 查全部的分页
            serviceDtoList.stream().forEach(serviceDto2 -> serviceDto2.setTarget(target));
            Pagination pagination = new Pagination(pageNum, pageSize, serviceDtoList.size());
            serviceDtos.setServices(serviceDtoList.subList(pagination.getFromIndex(), pagination.getToIndex()));
            serviceDtos.setTotalRecord(serviceDtoList.size());
            return serviceDtos;
        }

        // 过滤分页
        List<ServiceDto> resultList = new ArrayList<>();
        if (SystemNameConstant.ALL.equals(secTarget)) {
            resultList = serviceDtoList.stream()
                    .filter(serviceDto -> serviceDto.getService().getTarget().equals(target)).collect(Collectors.toList());
        } else {
            resultList = serviceDtoList.stream()
                    .filter(serviceDto -> (serviceDto.getService().getTarget().equals(target) &&
                            serviceDto.getService().getSecTarget().equals(secTarget))).collect(Collectors.toList());
        }

        resultList.stream().forEach(serviceDto2 -> serviceDto2.setTarget(target));
        Pagination pagination = new Pagination(pageNum, pageSize, resultList.size());
        serviceDtos.setServices(resultList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        serviceDtos.setTotalRecord(resultList.size());
        return serviceDtos;
    }
}
