/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.developer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisense.gateway.library.constant.SystemNameConstant;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.developer.service.ServicesService;
import com.hisense.gateway.library.stud.ServiceStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.Pagination;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.model.dto.portal.*;
import com.hisense.gateway.library.model.pojo.portal.User;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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

    @Autowired
    InstanceRepository instanceRepository;

    @Resource
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    PublishApiPlanRepository publishApiPlanRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;

    @PersistenceContext
    EntityManager entityManager;

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
                serviceDto.getService().setTarget(originalDescription.substring(0, index));//一级菜单
                String originalDescription2 = originalDescription.substring((index + 1), originalDescription.length());
                int index2 = originalDescription2.indexOf("|");
                if (index2 == -1) {//没有二级菜单
                    serviceDto.getService().setSecTarget("未分类");
                    serviceDto.getService().setDescription(originalDescription2);
                } else {//有二级菜单
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
        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
        if (null == userName) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }
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
//        Domain domain = domainRepository.searchDomainByName(domainName);
        Instance ins = instanceRepository.searchInstanceByName(domainName);
        MetricsDto metricsDto = new MetricsDto();
        if (null == ins) {
            log.error("can not found Instance,Instance is {}", domainName);
            throw new OperationFailed("Instance not exist");
        }
        PublishApi api =
                publishApiRepository.getOne(Integer.parseInt(serviceId));
        if (null == api.getId()) {
            log.error("can not found api");
            throw new OperationFailed("api not exist");
        }
        PublishApiInstanceRelationship publishApiInstanceRelationship =
        		publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(api.getId(), ins.getId());

        if (null == publishApiInstanceRelationship || null == publishApiInstanceRelationship.getScaleApiId()) {
            log.error("can not found api");
            throw new OperationFailed("api not exist");
        }
        MetricXmls metricXmls = serviceStud.getServiceMetricXmlList(ins.getHost(),
        		ins.getAccessToken(), publishApiInstanceRelationship.getScaleApiId().toString());
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
//        return serviceStud.getServiceMetricList(domain.getHost(), domain.getAccessToken(), serviceId);
    }

    @Override
    public ServiceDtos getServiceClassifies(String domainName, int pageNum, int pageSize) {
        ServiceDtos serviceDtos = new ServiceDtos();
        Instance instance = instanceRepository.searchInstanceByName(domainName);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }
//        Domain domain = domainRepository.searchDomainByName(domainName);
//        if (null == domain) {
//            log.error("can not found domain,domain is {}", domainName);
//            throw new OperationFailed("domain not exist");
//        }
        serviceDtoList = serviceStud.serviceDtoList(instance.getHost(), instance.getAccessToken());
        //分组
        rebuildServiceDtoList(serviceDtoList);
        Map<String, List<ServiceDto>> resultMap = serviceDtoList.stream()
                .collect(Collectors.groupingBy(serviceDto -> serviceDto.getService().getTarget()));
        log.info("getServiceClassifies info resultMap,resultMap is {}", JSONObject.toJSONString(resultMap));
        List<ServiceDto> services = new ArrayList<>();
        for (Map.Entry<String, List<ServiceDto>> entry : resultMap.entrySet()) {
            ServiceDto serviceDto = new ServiceDto();
            //serviceDto.setSystemName(entry.getKey());
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
        serviceDtoList.stream().forEach(serviceDto2 ->
        {
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
                    serviceDto2.getService().setTarget(originalDescription.substring(0, index));//一级菜单
                    String originalDescription2 = originalDescription.substring((index + 1), originalDescription.length());
                    int index2 = originalDescription2.indexOf("|");
                    if (index2 == -1) {//没有二级菜单
                        serviceDto2.getService().setSecTarget("未分类");
                        serviceDto2.getService().setDescription(originalDescription2);
                    } else {//有二级菜单
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
            serviceDtoList.stream().forEach(serviceDto2->serviceDto2.setTarget(target));
            serviceDtos.setServices(serviceDtoList.subList(pagination.getFromIndex(), pagination.getToIndex()));
            serviceDtos.setTotalRecord(serviceDtoList.size());
            return serviceDtos;
        }
        //过滤分页
        List<ServiceDto> resultList = serviceDtoList.stream()
                .filter(serviceDto->{
                    if (SystemNameConstant.ALL.equals(secTarget)) {
                        if (serviceDto.getService().getTarget().equals(target)){
                            return true;
                        }
                    } else {
                        if (serviceDto.getService().getTarget().equals(target) &&
                                serviceDto.getService().getSecTarget().equals(secTarget)) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
        resultList.stream().forEach(serviceDto2->serviceDto2.setTarget(target));
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
            serviceDtoList.stream().forEach(serviceDto2->serviceDto2.setTarget(target));
            Pagination pagination = new Pagination(pageNum, pageSize, serviceDtoList.size());
            serviceDtos.setServices(serviceDtoList.subList(pagination.getFromIndex(), pagination.getToIndex()));
            serviceDtos.setTotalRecord(serviceDtoList.size());
            return serviceDtos;
        }
        List<ServiceDto> resultList = serviceDtoList.stream()
                .filter(serviceDto->{
                    if (SystemNameConstant.ALL.equals(secTarget)) {
                        if (serviceDto.getService().getTarget().equals(target)){
                            return true;
                        }
                    } else {
                        if (serviceDto.getService().getTarget().equals(target) &&
                                serviceDto.getService().getSecTarget().equals(secTarget)) {
                            return true;
                        }
                    }
                    return false;
                }).collect(Collectors.toList());
        resultList.stream().forEach(serviceDto2->serviceDto2.setTarget(target));
        Pagination pagination = new Pagination(pageNum, pageSize, resultList.size());
        serviceDtos.setServices(resultList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        serviceDtos.setTotalRecord(resultList.size());
        return serviceDtos;
    }

    @Override
    public ServiceResDtos findDataItem(String instanceName, int pageNum, int pageSize) {
        ServiceResDtos serviceResDtos = new ServiceResDtos();
        List<DataItem> dataItems = dataItemRepository.findAllByGroupKey("categoryOne");
        List<ServiceResDto> serviceResDtoList = new ArrayList<>();
        for (int i = 0, size = dataItems.size(); i < size; i++) {
            DataItem dataItem = dataItems.get(i);
            ServiceResDto serviceResDto = new ServiceResDto();
            TargetRes targetRes = new TargetRes();
            targetRes.setId(dataItem.getId());
            targetRes.setItemName(dataItem.getItemName());
            serviceResDto.setTarget(targetRes);
            if (0 == dataItem.getParentId()) {//包含子集
                List<DataItem> dataItemList = dataItemRepository.findAllByParentId(dataItem.getId());
                List<TargetRes> secTargetResList = new ArrayList<>(dataItemList.size());
                dataItem.setDataItemList(dataItemList);
                for (DataItem secDataItem : dataItemList) {
                    TargetRes secTargetRes = new TargetRes();
                    secTargetRes.setId(secDataItem.getId());
                    secTargetRes.setItemName(secDataItem.getItemName());
                    secTargetResList.add(secTargetRes);
                }
                serviceResDto.setSecTarget(secTargetResList);
            }
            serviceResDtoList.add(serviceResDto);
        }
        serviceResDtos.setServices(serviceResDtoList);
        serviceResDtos.setTotalRecord(serviceResDtoList.size());
        return serviceResDtos;
    }

    @Override
    public ServiceResDtos getPageServiceDtoByCategory(String domain, String target, String secTarget, Integer system, int pageNum, int pageSize) {
        ServiceResDtos serviceResDtos = new ServiceResDtos();
        List<ServiceResDto> services = new ArrayList<>();
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        PageRequest pageable = PageRequest.of(pageNum-1, pageSize);
        StringBuilder countSelectSql = new StringBuilder("");
        countSelectSql.append(" SELECT count(1) ");
        StringBuilder selectHeadSql = new StringBuilder();
        selectHeadSql.append(" SELECT pa.id as id, pa.name as name ,pa.description as description, ");
        selectHeadSql.append(" pag.category_one as categoryOne, pag.category_two as categoryTwo, ");
        selectHeadSql.append(" di.item_name as targetRes, di2.item_name as secTargetRes, di3.item_name as systemName ");
        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" FROM publish_api pa ");
        selectSql.append(" LEFT JOIN publish_api_instance_relationship pair ON pa.id = pair.api_id ");
        selectSql.append(" LEFT JOIN publish_api_group pag ON pa.group_id = pag.id ");
        selectSql.append(" LEFT JOIN data_item di ON di.id = pag.category_one ");
        selectSql.append(" LEFT JOIN data_item di2 ON di2.id = pag.category_two ");
        selectSql.append(" LEFT JOIN data_item di3 ON di3.id = pag.system ");
        selectSql.append(" WHERE ");
        selectSql.append(" pa.STATUS > 0 AND pa.is_online = 1 AND pair.instance_id = :instanceId");
        Map<String,Object> params = new HashMap<>();
        params.put("instanceId", instance.getId());
        StringBuilder whereSql = new StringBuilder();
        if (StringUtils.isNotBlank(target) && !"ALL".equals(target)) {
            whereSql.append(" AND pag.category_one = :categoryOne ");
            params.put("categoryOne", Integer.valueOf(target));
        }
        if (StringUtils.isNotBlank(secTarget) && !"ALL".equals(secTarget)) {
            whereSql.append(" AND pag.category_two = :categoryTwo ");
            params.put("categoryTwo", Integer.valueOf(secTarget));
        }
        if (null != system) {
            whereSql.append(" AND pag.system= :system ");
            params.put("system", system);
        }
        String orderSql = " ORDER BY pa.create_time DESC ";
        String countSql = new StringBuilder().append(countSelectSql).append(selectSql).append(whereSql).toString();
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        List totalCountList = countQuery.getResultList();
        Integer totalCount = 0;
        if (null != totalCountList && totalCountList.size() > 0) {
            totalCount = Integer.valueOf(String.valueOf(totalCountList.get(0)));
        }
        if (0 == totalCount) {
            serviceResDtos.setTotalRecord(0);
            serviceResDtos.setServices(services);
            return serviceResDtos;
        }
        String querySql = new StringBuilder().append(selectHeadSql).append(selectSql).append(whereSql).append(orderSql).toString();
        Query query = entityManager.createNativeQuery(querySql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Map<String,Object>> mapList = query.getResultList();
        for (Map<String, Object> map : mapList) {
            ServiceResDto serviceResDto = new ServiceResDto();
            ServiceRes serviceRes = new ServiceRes();
            serviceRes.setId(String.valueOf(map.get("id")));
            serviceRes.setName(String.valueOf(map.get("name")));
            serviceRes.setDescription(String.valueOf(map.get("description")));
            serviceRes.setTarget(String.valueOf(map.get("targetRes")));
            serviceRes.setSecTarget(String.valueOf(map.get("secTargetRes")));
            serviceRes.setSystemName(String.valueOf(map.get("systemName")));
            serviceRes.setBackendVersion("2");
            serviceResDto.setService(serviceRes);
            services.add(serviceResDto);
        }
        serviceResDtos.setServices(services);
        serviceResDtos.setTotalRecord(totalCount);
        return serviceResDtos;
    }

    @Override
    public ServiceResDtos searchPageServiceDtoByNameAndSystem(String domain, String serviceName, String target,
                                                              String secTarget, Integer system, int pageNum, int pageSize) {
        ServiceResDtos serviceResDtos = new ServiceResDtos();
        List<ServiceResDto> services = new ArrayList<>();
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        PageRequest pageable = PageRequest.of(pageNum-1, pageSize);
        StringBuilder countSelectSql = new StringBuilder("");
        countSelectSql.append(" SELECT count(1) ");
        StringBuilder selectHeadSql = new StringBuilder();
        selectHeadSql.append(" SELECT pa.id as id, pa.name as name ,pa.description as description, ");
        selectHeadSql.append(" pag.category_one as categoryOne, pag.category_two as categoryTwo, ");
        selectHeadSql.append(" di.item_name as targetRes, di2.item_name as secTargetRes, di3.item_name as systemName ");
        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" FROM publish_api pa ");
        selectSql.append(" LEFT JOIN publish_api_instance_relationship pair ON pa.id = pair.api_id ");
        selectSql.append(" LEFT JOIN publish_api_group pag ON pa.group_id = pag.id ");
        selectSql.append(" LEFT JOIN data_item di ON di.id = pag.category_one ");
        selectSql.append(" LEFT JOIN data_item di2 ON di2.id = pag.category_two ");
        selectSql.append(" LEFT JOIN data_item di3 ON di3.id = pag.system ");
        selectSql.append(" WHERE ");
        selectSql.append(" pa.STATUS > 0 AND pa.is_online = 1 AND pair.instance_id = :instanceId");
        Map<String,Object> params = new HashMap<>();
        params.put("instanceId", instance.getId());
        StringBuilder whereSql = new StringBuilder();
        if (StringUtils.isNotBlank(target) && !"ALL".equals(target)) {
            whereSql.append(" AND pag.category_one = :categoryOne ");
            params.put("categoryOne", Integer.valueOf(target));
        }
        if (StringUtils.isNotBlank(secTarget) && !"ALL".equals(secTarget)) {
            whereSql.append(" AND pag.category_two = :categoryTwo ");
            params.put("categoryTwo", Integer.valueOf(secTarget));
        }
        if (StringUtils.isNotBlank(serviceName)) {
            whereSql.append(" AND pa.name LIKE :name ");
            params.put("name", "%" + serviceName.trim() + "%");
        }
        if (null != system) {
            whereSql.append(" AND pag.system= :system ");
            params.put("system", system);
        }
        String orderSql = " ORDER BY pa.create_time DESC ";
        String countSql = new StringBuilder().append(countSelectSql).append(selectSql).append(whereSql).toString();
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        List totalCountList = countQuery.getResultList();
        Integer totalCount = 0;
        if (null != totalCountList && totalCountList.size() > 0) {
            totalCount = Integer.valueOf(String.valueOf(totalCountList.get(0)));
        }
        if (0 == totalCount) {
            serviceResDtos.setTotalRecord(0);
            serviceResDtos.setServices(services);
            return serviceResDtos;
        }
        String querySql = new StringBuilder().append(selectHeadSql).append(selectSql).append(whereSql).append(orderSql).toString();
        Query query = entityManager.createNativeQuery(querySql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Map<String,Object>> mapList = query.getResultList();
        for (Map<String, Object> map : mapList) {
            ServiceResDto serviceResDto = new ServiceResDto();
            ServiceRes serviceRes = new ServiceRes();
            serviceRes.setId(String.valueOf(map.get("id")));
            serviceRes.setName(String.valueOf(map.get("name")));
            serviceRes.setDescription(String.valueOf(map.get("description")));
            serviceRes.setTarget(String.valueOf(map.get("targetRes")));
            serviceRes.setSecTarget(String.valueOf(map.get("secTargetRes")));
            serviceRes.setSystemName(String.valueOf(map.get("systemName")));
            serviceRes.setBackendVersion("2");
            serviceResDto.setService(serviceRes);
            services.add(serviceResDto);
        }
        serviceResDtos.setServices(services);
        serviceResDtos.setTotalRecord(totalCount);
        return serviceResDtos;
    }

    @Override
    public ServiceResDtos searchPageServiceResDtoByName(String domain, String serviceName, int pageNum, int pageSize) {
        ServiceResDtos serviceResDtos = new ServiceResDtos();
        List<ServiceResDto> services = new ArrayList<>();
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        PageRequest pageable = PageRequest.of(pageNum-1, pageSize);
        StringBuilder countSelectSql = new StringBuilder("");
        countSelectSql.append(" SELECT count(1) ");
        StringBuilder selectHeadSql = new StringBuilder();
        selectHeadSql.append(" SELECT pa.id as id, pa.name as name ,pa.description as description, ");
        selectHeadSql.append(" pag.category_one as categoryOne, pag.category_two as categoryTwo, ");
        selectHeadSql.append(" di.item_name as targetRes, di2.item_name as secTargetRes, di3.item_name as systemName ");
        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" FROM publish_api pa ");
        selectSql.append(" LEFT JOIN publish_api_instance_relationship pair ON pa.id = pair.api_id ");
        selectSql.append(" LEFT JOIN publish_api_group pag ON pa.group_id = pag.id ");
        selectSql.append(" LEFT JOIN data_item di ON di.id = pag.category_one ");
        selectSql.append(" LEFT JOIN data_item di2 ON di2.id = pag.category_two ");
        selectSql.append(" LEFT JOIN data_item di3 ON di3.id = pag.system ");
        selectSql.append(" WHERE ");
        selectSql.append(" pa.STATUS > 0 AND pa.is_online = 1 AND pair.instance_id = :instanceId");
        Map<String,Object> params = new HashMap<>();
        params.put("instanceId", instance.getId());
        StringBuilder whereSql = new StringBuilder();
        if (StringUtils.isNotBlank(serviceName)) {
            whereSql.append(" AND pa.name LIKE :name ");
            params.put("name", "%" + serviceName.trim() + "%");
        }
        String orderSql = " ORDER BY pa.create_time DESC ";
        String countSql = new StringBuilder().append(countSelectSql).append(selectSql).append(whereSql).toString();
        Query countQuery = entityManager.createNativeQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }
        List totalCountList = countQuery.getResultList();
        Integer totalCount = 0;
        if (null != totalCountList && totalCountList.size() > 0) {
            totalCount = Integer.valueOf(String.valueOf(totalCountList.get(0)));
        }
        if (0 == totalCount) {
            serviceResDtos.setTotalRecord(0);
            serviceResDtos.setServices(services);
            return serviceResDtos;
        }
        String querySql = new StringBuilder().append(selectHeadSql).append(selectSql).append(whereSql).append(orderSql).toString();
        Query query = entityManager.createNativeQuery(querySql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Map<String,Object>> mapList = query.getResultList();
        for (Map<String, Object> map : mapList) {
            ServiceResDto serviceResDto = new ServiceResDto();
            ServiceRes serviceRes = new ServiceRes();
            serviceRes.setId(String.valueOf(map.get("id")));
            serviceRes.setName(String.valueOf(map.get("name")));
            serviceRes.setDescription(String.valueOf(map.get("description")));
            serviceRes.setTarget(String.valueOf(map.get("targetRes")));
            serviceRes.setSecTarget(String.valueOf(map.get("secTargetRes")));
            serviceRes.setSystemName(String.valueOf(map.get("systemName")));
            serviceRes.setBackendVersion("2");
            serviceResDto.setService(serviceRes);
            services.add(serviceResDto);
        }
        serviceResDtos.setServices(services);
        serviceResDtos.setTotalRecord(totalCount);
        return serviceResDtos;
    }

    @Override
    public ServiceResDto getServiceResDtos(String domain, Integer id) {
        ServiceResDto serviceResDto = new ServiceResDto();
        PublishApi publishApi = publishApiRepository.findOne(id);
        PublishApiGroup publishApiGroup = publishApi.getApiGroup();
        List<Integer> dataItemIds = new ArrayList<>(3);
        dataItemIds.add(publishApiGroup.getSystem());
        dataItemIds.add(publishApiGroup.getCategoryOne());
        dataItemIds.add(publishApiGroup.getCategoryTwo());
        List<DataItem> dataItemList = dataItemRepository.findByIds(dataItemIds);
        for (DataItem dataItem : dataItemList) {
            if (dataItem.getId().equals(publishApiGroup.getSystem())) {
                publishApiGroup.setSystemName(dataItem.getItemName());
            } else if (dataItem.getId().equals(publishApiGroup.getCategoryOne())) {
                publishApiGroup.setCategoryOneName(dataItem.getItemName());
            } else if (dataItem.getId().equals(publishApiGroup.getCategoryTwo())) {
                publishApiGroup.setCategoryTwoName(dataItem.getItemName());
            }
        }
        ServiceRes serviceRes = new ServiceRes();
        serviceRes.setId(String.valueOf(publishApi.getId()));
        serviceRes.setName(publishApi.getName());
        serviceRes.setDescription(publishApi.getDescription());
        serviceRes.setTarget(publishApiGroup.getCategoryOneName());
        serviceRes.setSecTarget(publishApiGroup.getCategoryTwoName());
        serviceRes.setSystemName(publishApiGroup.getSystemName());
        serviceRes.setCreateTime(publishApi.getCreateTime());
        serviceRes.setUpdateTime(publishApi.getUpdateTime());
        serviceResDto.setService(serviceRes);
        return serviceResDto;
    }

    @Override
    public List<AppPlanDto> getAppPlanDtoListNew(String domain, Integer serviceId) {
        List<AppPlanDto> appPlanDtos = new ArrayList<>();
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        //查询3scaleId
        PublishApiInstanceRelationship pair = publishApiInstanceRelationshipRepository
                .getByAPIidAndInstanceId(serviceId,instance.getId());
        List<PublishApiPlan> publishApiPlans = publishApiPlanRepository.findAllByInstanceIdAndApiId(serviceId,instance.getId());
        for (PublishApiPlan publishApiPlan : publishApiPlans) {
            AppPlanDto appPlanDto = new AppPlanDto();
            AppPlan applicationPlan = new AppPlan();
            applicationPlan.setId(String.valueOf(publishApiPlan.getId()));
            //暂时写死
            applicationPlan.setName("0元/次数不限");
            AppPlanDto appPlanDto1 = serviceStud.appPlanDtoRead(instance.getHost(), instance.getAccessToken(),String.valueOf(pair.getScaleApiId()),
                    String.valueOf(publishApiPlan.getScalePlanId()));
            if (null != appPlanDto1) {
                AppPlan appPlanRes = appPlanDto1.getApplication_plan();
                if (null != appPlanRes) {
                    applicationPlan.setAccessToken(appPlanRes.getAccessToken());
                    applicationPlan.setCostPerMonth(appPlanRes.getCostPerMonth());
                    applicationPlan.setSetupFee(appPlanRes.getSetupFee());
                    applicationPlan.setTrialPeriodDays(appPlanRes.getTrialPeriodDays());
                    applicationPlan.setState(appPlanRes.getState());
                    applicationPlan.setServiceId(appPlanRes.getServiceId());
                }
            }
            appPlanDto.setApplication_plan(applicationPlan);
            appPlanDtos.add(appPlanDto);
        }
        return appPlanDtos;
    }

    @Override
    public List<ApplicationCountRes> getPlanAppCountNew(String domain, Integer serviceId, String userName) {
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        List<UserInstanceRelationship> uirs = userInstanceRelationshipRepository.findListByUserAndInstanceId(userName, instance.getId());
        if (null == uirs || uirs.size() == 0) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }
        UserInstanceRelationship uir = uirs.get(0);
        List<ApplicationCountRes> applicationCountResList = new ArrayList<>();
        List<PublishApiPlan> publishApiPlans = publishApiPlanRepository.findAllByInstanceIdAndApiId(serviceId,instance.getId());
        if (null == publishApiPlans || publishApiPlans.size() == 0) {
            return applicationCountResList;
        }
        //查询默认账户
        String defaultUser = "houpeiyun.ex";
        for (PublishApiPlan publishApiPlan : publishApiPlans) {
            ApplicationCountRes applicationCountRes = new ApplicationCountRes();
            applicationCountRes.setPlanId(String.valueOf(publishApiPlan.getId()));
            applicationCountRes.setScalePlanId(String.valueOf(publishApiPlan.getScalePlanId()));
            PublishApplication publishApplication =
                    publishApplicationRepository.findByUserNameAndInstanceIdAndPlanId(uir.getUserName(),
                    instance.getId(),publishApiPlan.getId());
            if (null == publishApplication) {
                applicationCountRes.setStatus(4);
            } else {
                applicationCountRes.setStatus(publishApplication.getStatus());
                applicationCountRes.setSystem(publishApplication.getSystem());
                DataItem dataItem = dataItemRepository.findOne(publishApplication.getSystem());
                if (null != dataItem) {
                    applicationCountRes.setAppSystem(dataItem.getItemName());
                }
            }
            List<PublishApplication> publishApplications = publishApplicationRepository.findByInstanceIdAndPlanIdAndUserName(
                        instance.getId(),publishApiPlan.getId(),defaultUser);
            applicationCountRes.setApplications(publishApplications.size());
            applicationCountResList.add(applicationCountRes);
        }

        return applicationCountResList;
    }

    @Override
    public List<Map<String, Object>> findSystem(String systemName) {
        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" SELECT di.id,di.item_name as systemName from data_item di where di.`status`=1 AND di.group_key=:groupKey ");
        Map<String,Object> params = new HashMap<>();
        params.put("groupKey", "system");
        StringBuilder whereSql = new StringBuilder();
        if (StringUtils.isNotBlank(systemName)) {
            whereSql.append(" AND di.item_name LIKE :systemName ");
            params.put("systemName", "%"+systemName+"%");
        }
        String orderSql = " ORDER BY di.item_name ";
        String querySql = new StringBuilder().append(selectSql).append(whereSql).append(orderSql).toString();
        Query query = entityManager.createNativeQuery(querySql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);

        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        List<Map<String,Object>> mapList = query.getResultList();
        return mapList;
    }

	@Override
	public InstanceDto getInstanceDto(String domain) {
		InstanceDto instanceDto = new InstanceDto();
		Instance ins = instanceRepository.searchInstanceByName(domain);
		if(null!=ins) {
			instanceDto.setProduction(ins.getRequestProduction());
		}
		return instanceDto;
	}
}
