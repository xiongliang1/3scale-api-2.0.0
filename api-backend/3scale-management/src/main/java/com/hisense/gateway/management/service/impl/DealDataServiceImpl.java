/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/19 @author peiyun
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.management.service.DealDataService;
import com.hisense.gateway.library.stud.*;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.RequestUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DealDataServiceImpl implements DealDataService {
    @Resource
    PublishApiRepository publishApiRepository;

    @Resource
    InstanceRepository instanceRepository;

    @Resource
    PublishApiInstanceRelationshipRepository pairRepository;

    @Resource
    PublishApiPlanRepository publishApiPlanRepository;

    @Resource
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    ServiceStud serviceStud;

    @Autowired
    ApplicationStud applicationStud;

    @Autowired
    ApplicationPlanStud applicationPlanStud;

    @Autowired
    AccountStud accountStud;

    @Autowired
    MappingRuleStud mappingRuleStud;

    @Autowired
    ProxyPoliciesStud proxyPoliciesStud;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    PublishApiPolicyRelationshipRepository publishApiPolicyRelationshipRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;

    @Autowired
    ProcessRecordRepository processRecordRepository;

    @Override
    public Result<Boolean> initApiData(String tenantId, String instanceName, Integer groupId) {
        Result<Boolean> returnResult = new Result<>();

        // 查询环境下的api的信息
        Instance instance = instanceRepository.searchInstanceByName(instanceName);
        if (null == instance) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("操作失败，未查询到instance信息");
            returnResult.setData(false);
            return returnResult;
        }

//        String username = SecurityUtils.getLoginUser().getUsername();
        //初始化api数据
        String host = instance.getHost();
        String accessToken = instance.getAccessToken();
        String partition = instance.getClusterPartition();
        // 查询service
        List<ServiceDto> serviceDtos = serviceStud.serviceDtoList(host, accessToken);
        List<com.hisense.gateway.library.stud.model.Service> services = serviceDtos.stream()
                .map(serviceDto -> serviceDto.getService()).collect(Collectors.toList());
        List<Long> scaleApiIds = publishApiInstanceRelationshipRepository.findAllScaleApiId(tenantId, partition);
        //未存在于库中的服务
        List<com.hisense.gateway.library.stud.model.Service> serviceList = services.stream()
                .filter(service1 -> !scaleApiIds.contains(Long.valueOf(service1.getId()))).collect(Collectors.toList());
        //数据创建
        for (com.hisense.gateway.library.stud.model.Service service1 : serviceList) {
        /*    PublishApi publishApiRes = savePublishApi(host, accessToken, service1, username, groupId);
            PublishApiInstanceRelationship pari = new PublishApiInstanceRelationship(publishApiRes.getId(),
                    instance.getId(),
                    Long.valueOf(service1.getId()));
            pairRepository.save(pari);*/
            //查询plan
            List<AppPlanDto> appPlanDtos = serviceStud.appPlanDtoList(host, accessToken, service1.getId());
            //PublishApiPlan落库
            for (AppPlanDto appPlanDto : appPlanDtos) {
                AppPlan appPlan = appPlanDto.getApplication_plan();
               /* PublishApiPlan publishApiPlan = new PublishApiPlan(publishApiRes.getId(), instance.getId(),
                        Long.valueOf(appPlan.getId()), 1,
                        username, new Date(), new Date(), appPlan.getName());
                PublishApiPlan publishApiPlanRes = publishApiPlanRepository.save(publishApiPlan);*/
            }
        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("操作成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<Boolean> initApiDataById(String tenantId, String instanceName, String scaleId, Integer apiId,
                                           Integer groupId) {
        Result<Boolean> returnResult = new Result<>();
        // 查询环境下的api的信息
        Instance instance = instanceRepository.searchInstanceByName(instanceName);
        if (null == instance) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("操作失败，未查询到instance信息");
            returnResult.setData(false);
            return returnResult;
        }

        String host = instance.getHost();
        String accessToken = instance.getAccessToken();

        if (StringUtils.isNotBlank(scaleId)) {
//            String userName = SecurityUtils.getLoginUser().getUsername();
            // 查询
            ServiceDto serviceDto = serviceStud.serviceDtoRead(host, accessToken, scaleId);
            if (null == serviceDto) {
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("操作失败，该instance下未查到api");
                returnResult.setData(false);
                return returnResult;
            }

            // 从scale中落库
            com.hisense.gateway.library.stud.model.Service service = serviceDto.getService();

        /*    PublishApi publishApiRes = savePublishApi(host, accessToken, service, userName, groupId);
            PublishApiInstanceRelationship pari = new PublishApiInstanceRelationship(publishApiRes.getId(),
                    instance.getId(),
                    Long.valueOf(service.getId()));
            pairRepository.save(pari);*/
            //查询plan
            List<AppPlanDto> appPlanDtos = serviceStud.appPlanDtoList(host, accessToken, service.getId());
            //PublishApiPlan落库
            for (AppPlanDto appPlanDto : appPlanDtos) {
               /* AppPlan appPlan = appPlanDto.getApplication_plan();
                PublishApiPlan publishApiPlan = new PublishApiPlan(publishApiRes.getId(), instance.getId(),
                        Long.valueOf(appPlan.getId()), 1,
                        userName, new Date(), new Date(), appPlan.getName());
                PublishApiPlan publishApiPlanRes = publishApiPlanRepository.save(publishApiPlan);*/
            }
        }

        if (StringUtils.isBlank(scaleId) && null != apiId) {//库中有数据，但scale没有,则删除库
            publishApiInstanceRelationshipRepository.deleteByApiId(apiId);
            publishApiPlanRepository.deleteByApiId(apiId);
            publishApiRepository.deleteById(apiId);
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("操作成功");
        returnResult.setData(true);
        return returnResult;
    }

    private PublishApi savePublishApi(String host, String accessToken,
                                      com.hisense.gateway.library.stud.model.Service service1, String creator,
                                      Integer groupId) {
        ProxyDto proxyDto = proxyPoliciesStud.readProxy(host, accessToken, service1.getId());
        Proxy proxy = proxyDto.getProxy();
        String realmName = proxy.getHostnameRewrite();
        String apiBackend = proxy.getApiBackend();
        String[] str1 = apiBackend.split("//");
        String[] str2 = str1[1].split(":");
        String apiHhost = str2[0];
        String port = str2[1];
        if (port.contains("/")) {
            String portStr[] = port.split("/");
            port = portStr[0];
        }

        String accessProtocol = "1";//1:http，2:https
        if (apiBackend.contains("https")) {
            accessProtocol = "2";
        }

        PolicieConfigDto policieConfigDto = proxyPoliciesStud.proxyPoliciesChainShow(host, accessToken,
                service1.getId());
        String authType = "auth";
        String url = "";
        for (PolicieConfig policieConfig : policieConfigDto.getPolicies_config()) {
            if ("default_credentials".equals(policieConfig.getName())) {
                if (policieConfig.getEnabled()) {
                    authType = "noauth";
                }
            }

            if ("url_rewriting".equals(policieConfig.getName())) {
                Map<String, Object> configuration = policieConfig.getConfiguration();
                JSONArray commands = JSONArray.parseArray(String.valueOf(configuration.get("commands")));
                url = commands.getJSONObject(0).getString("regex");
            }
        }

        PublishApi publishApi = new PublishApi(service1.getName(), service1.getDescription(),
                authType, realmName, url, accessProtocol, "2", apiHhost,
                port, new Date(), new Date(), 1);
        PublishApiGroup publishApiGroup = new PublishApiGroup();
        publishApiGroup.setId(groupId);
        publishApi.setGroup(publishApiGroup);
        publishApi.setCreator(creator);
        publishApi.setStatus(4);
        return publishApiRepository.save(publishApi);
    }
}
