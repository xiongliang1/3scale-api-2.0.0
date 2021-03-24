/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author peiyun
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.ProcessDataDto;
import com.hisense.gateway.library.model.dto.web.AppSynDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.impl.PublishApiServiceImpl;
import com.hisense.gateway.management.service.AppSynService;
import com.hisense.gateway.management.service.permission.PermissionService;
import com.hisense.gateway.library.stud.*;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.web.form.ApplicationForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class AppSynServiceImpl implements AppSynService {
    @Resource
    PublishApiGroupRepository publishApiGroupRepository;

    @Resource
    PublishApiRepository publishApiRepository;

    @Resource
    DataItemRepository dataItemRepository;

    @Resource
    ApplicationStud applicationStud;

    @Resource
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;

    @Resource
    PublishApplicationRepository publishApplicationRepository;

    @Resource
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Resource
    PublishApiPlanRepository publishApiPlanRepository;

    @Resource
    AccountStud accountStud;

    @Resource
    PermissionService permissionService;

    @Resource
    InstanceRepository instanceRepository;

    @Resource
    ServiceStud serviceStud;

    @Resource
    ProcessRecordRepository processRecordRepository;

    @Override
    public Result<Boolean> synApplication(AppSynDto appSynDto) {
        ApplicationSynDtos apps = null;

        if (null != appSynDto && null != appSynDto.getInstanceId()
                && 0 != appSynDto.getInstanceId()) {
            Instance ins = instanceRepository.getOne(appSynDto.getInstanceId());

            if (StringUtils.isEmpty(appSynDto.getAccountId())) {
                apps = applicationStud.getAllApplicationList2(ins.getHost(), ins.getAccessToken());
                if (null == apps || CollectionUtils.isEmpty(apps.getApplications())
                        || null == apps.getApplications().get(0)
                        || CollectionUtils.isEmpty(apps.getApplications().get(0).getApplication()))
                    return new Result<>(false);

                synApp(ins, apps.getApplications());
            } else {
                ApplicationForm applicationForm = new ApplicationForm();
                applicationForm.setAccessToken(ins.getAccessToken());
                applicationForm.setUserAccountId(appSynDto.getAccountId());

                if (null == apps || CollectionUtils.isEmpty(apps.getApplications())
                        || null == apps.getApplications().get(0)
                        || CollectionUtils.isEmpty(apps.getApplications().get(0).getApplication()))
                    return new Result<>(false);

                synApp(ins, apps.getApplications());
            }
            return new Result<>(true);
        }

        List<Instance> inslist = instanceRepository.findAll();
        for (Instance ins : inslist) {
            apps = applicationStud.getAllApplicationList2(ins.getHost(), ins.getAccessToken());
            if (null == apps || CollectionUtils.isEmpty(apps.getApplications())
                    || null == apps.getApplications().get(0)
                    || CollectionUtils.isEmpty(apps.getApplications().get(0).getApplication()))
                return new Result<>(false);

            synApp(ins, apps.getApplications());
        }
        return new Result<>(true);
    }

    private void synApp(Instance ins, String appId) {
        ApplicationSynDtos apps =
                applicationStud.getAllApplicationList2(ins.getHost(), ins.getAccessToken());

        if (null != apps && CollectionUtils.isEmpty(apps.getApplications())) {
            for (ApplicationSyn application : apps.getApplications().get(0).getApplication()) {
                // 校验account
                //User user = validateUser(ins, Long.valueOf(application.getAccountId()));
              /*  String user = SecurityUtils.getLoginUser().getUsername();
                if (null == user) continue;*/

                // 校验是否已经存在订阅
                List<PublishApplication> applist =
                        publishApplicationRepository.findByScaleAppId(
                                String.valueOf(application.getId()), ins.getId());
                if (!CollectionUtils.isEmpty(applist)) continue;

                // 校验是否已经存在API
                List<PublishApiInstanceRelationship> apiRels =
                        publishApiInstanceRelationshipRepository.getApiInstanceRelByInsAndScaleApiId(ins.getId(),
                                Long.valueOf(application.getServiceId()));
                if (CollectionUtils.isEmpty(apiRels)) continue;

                PublishApi publishApi =
                        publishApiRepository.getOne(apiRels.get(0).getApiId());
                if (null == publishApi.getId()) continue;

                //校验是否已经存在API计划
                PublishApiPlan plan = publishApiPlanRepository.findByScalePlanIdAndInstanceId(
                        Long.valueOf(application.getPlanId()), ins.getId());
                if (null == plan) continue;

                PublishApplication publishApplication = new PublishApplication();

                publishApplication.setName(application.getName());
                //publishApplication.setSystem("16"); guilai.ming 2020/09/10
                publishApplication.setPublishApi(publishApi);
                publishApplication.setInstance(ins);
                publishApplication.setScaleApplicationId(application.getId());
                publishApplication.setApiPlan(plan);
                publishApplication.setStatus(2);
//                publishApplication.setCreator(user);
                publishApplication.setCreateTime(new Date());
                publishApplication.setState(application.getState());
                publishApplicationRepository.saveAndFlush(publishApplication);
            }
        }
    }

    private void synApp(Instance ins, List<ApplicationSynDto> apps) {
        if (null != apps && !CollectionUtils.isEmpty(apps)) {
            for (ApplicationSynDto application : apps) {
                //校验account
                //User user = validateUser(ins, Long.valueOf(application.getApplication().get(0).getAccountId()));
               /* String user = SecurityUtils.getLoginUser().getUsername();
                if (null == user) {
                    log.error("没有查询到用户，account is {}", application.getApplication().get(0).getAccountId());
                    continue;
                }*/

                //校验是否已经存在订阅
                List<PublishApplication> applist =
                        publishApplicationRepository.findByScaleAppId(
                                String.valueOf(application.getApplication().get(0).getId()), ins.getId());
                if (!CollectionUtils.isEmpty(applist)) {
                    log.error("已经订阅，account is {}", applist);
                    continue;
                }

                //校验是否已经存在API
                List<PublishApiInstanceRelationship> apiRels =
                        publishApiInstanceRelationshipRepository.getApiInstanceRelByInsAndScaleApiId(ins.getId(),
                                Long.valueOf(application.getApplication().get(0).getServiceId()));
                if (CollectionUtils.isEmpty(apiRels)) {
                    log.error("不存在此API已经订阅，apiRels is {}", apiRels);
                    continue;
                }

                PublishApi publishApi =
                        publishApiRepository.getOne(apiRels.get(0).getApiId());
                if (null == publishApi) {
                    log.error("不存在此API ，apiRels is {}", apiRels);
                    continue;
                }

                //校验是否已经存在API计划
                PublishApiPlan plan = publishApiPlanRepository.findByScalePlanIdAndInstanceId(
                        Long.valueOf(application.getApplication().get(0).getPlanId()), ins.getId());
                if (null == plan) {
                    log.error("不存在此API PLAN ，plan is {}", plan);
                    continue;
                }

                PublishApplication publishApplication = new PublishApplication();

                publishApplication.setName(application.getApplication().get(0).getName());
                publishApplication.setSystem(4);
                publishApplication.setPublishApi(publishApi);
                publishApplication.setInstance(ins);
                publishApplication.setScaleApplicationId(application.getApplication().get(0).getId());
                publishApplication.setApiPlan(plan);
                publishApplication.setStatus(2);
//                publishApplication.setCreator(user);
                publishApplication.setCreateTime(new Date());
                publishApplication.setState(application.getApplication().get(0).getState());
                publishApplicationRepository.saveAndFlush(publishApplication);

                ProcessRecord pr = new ProcessRecord();
                pr.setCreateTime(new Date());
//                pr.setCreator(user);
                pr.setUpdateTime(new Date());
                pr.setType("application");
                pr.setStatus(2);
                pr.setRemark("数据清洗");
                pr.setRelId(publishApplication.getId());

                ProcessDataDto processDataDto = new ProcessDataDto();
                processDataDto.setGroupId(publishApi.getApiGroup().getId());
                processDataDto.setApiSystem(publishApi.getGroup().getSystem());
                processDataDto.setAppSystem(publishApplication.getSystem());
                List<String> clusterPartitions = new ArrayList<>();
                clusterPartitions.add(ins.getClusterPartition());
                processDataDto.setClusterPartitions(clusterPartitions);
                pr.setExtVar(JSONObject.toJSONString(processDataDto));

                List<String> partitions = new ArrayList<>();
                String partition = PublishApiServiceImpl.getPartition(publishApi.getPartition());
                partitions.add(partition);
                List<ApiInstance> apiInstances =
                        publishApiInstanceRelationshipRepository.getApiInstances(publishApi.getId(),
                                InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName(),partitions);// guilai 2020/09/16

                ProxyConfigDto proxyConfigDto = null;
                for (ApiInstance apiInstance : apiInstances) {
                    if(proxyConfigDto == null){
                        PublishApiInstanceRelationship apiInstanceRelationship =
                                publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(publishApi.getId(), Math.toIntExact(apiInstance.getId()));
                        proxyConfigDto = serviceStud.latestPromote(apiInstance.getHost(), apiInstance.getAccessToken(),
                                apiInstance.getId(), ModelConstant.ENV_SANDBOX);

                        log.info("*********proxyConfigDto,{}", proxyConfigDto);

                        if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()
                                && null != proxyConfigDto.getProxyConfig().getVersion()) {
                            serviceStud.configPromote(apiInstance.getHost(), apiInstance.getAccessToken(),
                                    apiInstance.getId().toString(), ModelConstant.ENV_SANDBOX,
                                    proxyConfigDto.getProxyConfig().getVersion(), ModelConstant.ENV_PROD);
                        }

                        pr.setData(JSONObject.toJSON(proxyConfigDto).toString());
                    }
                }

                processRecordRepository.saveAndFlush(pr);
            }
        }
    }

    /*private User validateUser(Instance ins, Long accountId) {
        Account account = new Account();
        account.setAccessToken(ins.getAccessToken());
        account.setId(accountId);

        AccountSynDto accDto =
                accountStud.accountFindById(ins.getHost(), account);
        if (null == accDto || null == accDto.getAccount()) return null;

        AccountSyn accountSyn = accountStud.accountUserFindById(ins.getHost(), account);
        if (null == accountSyn ||
                CollectionUtils.isEmpty(accountSyn.getUsers())) return null;

        User user =
                userRepository.searchUserByName(accountSyn.getUsers().get(0).getUser().getUsername());
        if (null == user) {
            user = userRepository.save(User.builder()
                    .name(accountSyn.getUsers().get(0).getUser().getUsername())
                    .email(accountSyn.getUsers().get(0).getUser().getEmail())
                    .status(1)//1,可见, 2不可见
                    .createTime(new Date())
                    .build());

            UserDto userDto =
                    permissionService.getPaasUser(accountSyn.getUsers().get(0).getUser().getUsername());
            if (null != userDto) {
                user.setPaasUserId(userDto.getId());
                user.setPhone(userDto.getPhone());
                user.setEmail(userDto.getEmail());
                userRepository.saveAndFlush(user);
            }
        }

        UserInstanceRelationship userRel =
                userInstanceRelationshipRepository.findByAccountIdAndInstanceId(
                        accountId, ins.getId());

        if (null == userRel) {
            userRel = userInstanceRelationshipRepository.save(UserInstanceRelationship.builder()
                    .userName(user.getName())
                    .instanceId(ins.getId())
                    .accountId(accDto.getAccount().getId())
                    .build());
        }
        return user;
    }*/
}
