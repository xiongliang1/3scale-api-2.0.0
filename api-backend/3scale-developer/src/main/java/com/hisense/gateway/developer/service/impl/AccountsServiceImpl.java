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
import com.hisense.gateway.developer.config.GatewayConstants;
import com.hisense.gateway.developer.service.AccountsService;
import com.hisense.gateway.developer.service.permission.PermissionService;

import com.hisense.gateway.developer.constant.ApiDefaultPlanConstant;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.ProcessDataDto;
import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.model.pojo.base.UserInstanceRelationship;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.stud.*;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.Pagination;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.*;

@Slf4j
@Service
public class AccountsServiceImpl implements AccountsService {

    @Autowired
    DomainRepository domainRepository;

    @Autowired
    AccountStud accountStud;

    @Autowired
    ServiceStud serviceStud;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    ProcessRecordRepository processRecordRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    PublishApiPlanRepository publishApiPlanRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;
    
    @Autowired
    PermissionService permissionService;

    @Autowired
    DataItemRepository dataItemRepository;
    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    public static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Override
    public ApplicationDtos getApplicationList(String domainName, String userName, int pageNum, int pageSize) {
        ApplicationDtos applicationDtos = getApplicationList(domainName, userName);
        List<Application> applicationList = applicationDtos.getApplication();
        int totalRecord = applicationList.size();
        Pagination pagination = new Pagination(pageNum, pageSize, totalRecord);
        applicationDtos.setApplication(applicationList.subList(pagination.getFromIndex(), pagination.getToIndex()));
        applicationDtos.setTotalRecord(applicationList.size());
        return applicationDtos;
    }

    @Override
    public ApplicationDtos getApplicationListBySearch(String domain, String userName, ApplicationSearchForm form) throws ParseException {
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.HOUR, 8);
        format.setTimeZone(TimeZone.getTimeZone("Asia/Shanghai"));
        String serviceName = form.getServiceName();
        String planName = form.getPlanName();
        String sinceStr = form.getSince();
        Date since = StringUtils.isNotEmpty(sinceStr) ? format.parse(sinceStr) : null;
        String untilStr = form.getUntil();
        Date until = StringUtils.isNotEmpty(untilStr) ? format.parse(untilStr) : null;
        Integer status = form.getStatus();
        String state = form.getState();
        Integer system = form.getSystem();
        int pageNum = Integer.parseInt(form.getPageNum());
        int pageSize = Integer.parseInt(form.getPageSize());

        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found Instance,Instance is {}", domain);
            throw new OperationFailed("Instance not exist");
        }

        UserInstanceRelationship uir = userInstanceRelationshipRepository.findByUserAndInstanceId(userName, instance.getId());
        if (null == uir) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }

        Specification<PublishApi> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (StringUtils.isNotEmpty(serviceName)) {
                andList.add(builder.like(root.get("name").as(String.class), "%" + serviceName + "%", GatewayConstants.ESCAPECHAR));
            }
            if (status != null && status == 4){
                //app失效即api状态为删除
                andList.add(builder.equal(root.get("status").as(Integer.class), 0));
            }
            if (status != null && status != 4){
                //app有效即api状态不为0
                andList.add(builder.notEqual(root.get("status").as(Integer.class), 0));
            }
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> publishApis = publishApiRepository.findAll(spec);
        if (CollectionUtils.isEmpty(publishApis)) {
            throw new NotExist(String.format("api not exist"));
        }

        Specification<PublishApiPlan> spec2 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
//            if (StringUtils.isNotEmpty(planName)) {
//                andList.add(builder.like(root.get("name").as(String.class), "%" + planName + "%", GatewayConstants.ESCAPECHAR));
//            }
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApiPlan> publishApiPlans = publishApiPlanRepository.findAll(spec2);
        if (CollectionUtils.isEmpty(publishApiPlans)) {
            throw new NotExist(String.format("api plan not exist"));
        }

        Specification<PublishApplication> spec3 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<PublishApi> inPublishApi = builder.in(root.get("publishApi"));
            for (PublishApi pa : publishApis) {
                inPublishApi.value(pa);
            }
            andList.add(inPublishApi);

            CriteriaBuilder.In<PublishApiPlan> inPlan = builder.in(root.get("apiPlan"));
            for (PublishApiPlan pap : publishApiPlans) {
                inPlan.value(pap);
            }
            andList.add(inPlan);

            if (null != system) {
                andList.add(builder.equal(root.get("system").as(Integer.class), system));
            }

            if (null != status && status == 1) {
                andList.add(builder.equal(root.get("status").as(Integer.class), status));
            }
            if (null != status && status == 2) {
                andList.add(builder.equal(root.get("status").as(Integer.class), status));
                if (StringUtils.isNotBlank(state)) {
                    andList.add(builder.equal(root.get("state").as(String.class), state));
                }
            }
            if (null != since) {
                andList.add(builder.greaterThanOrEqualTo(root.get("createTime").as(Date.class), since));
            }
            if (null != until) {
                andList.add(builder.lessThanOrEqualTo(root.get("createTime").as(Date.class), until));
            }
            andList.add(builder.equal(root.get("creator").as(String.class), uir.getUserName()));
            andList.add(builder.equal(root.get("instance").as(Instance.class), instance));
            andList.add(builder.notEqual(root.get("status").as(Integer.class), 0));//已删除的也不展示
            andList.add(builder.notEqual(root.get("status").as(Integer.class), 3));//审批未通过不展示
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        PageRequest pageable = PageRequest.of(0!=pageNum?pageNum-1:0, pageSize, Sort.by(direction, property));
        Page<PublishApplication> p = publishApplicationRepository.findAll(spec3, pageable);
        List<PublishApplication> publishApplications = p.getContent();
        if (CollectionUtils.isEmpty(publishApplications)) {
            throw new NotExist(String.format("application not exist"));
        }

        //TODO
        ApplicationDtos applicationDtos = new ApplicationDtos();
        if(null!=form &&
        		!StringUtils.isEmpty(form.getPlanName())
        		&& !ApiDefaultPlanConstant.PLAN_NAME.contains(form.getPlanName())) {
        	return applicationDtos;
        }
        ApplicationXmlDtos applicationXmlDtos = accountStud.appXmlDtoList(instance.getHost(), instance.getAccessToken(), uir.getAccountId().toString());
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String jsonStr = mapper.writeValueAsString(applicationXmlDtos);
            applicationDtos = mapper.readValue(jsonStr, ApplicationDtos.class);
        } catch (JsonProcessingException e) {
            log.error("exception",e);
        } catch (IOException e) {
            log.error("IOException",e);
        }
        List<Application> applications = applicationDtos.getApplication();
        List<Application> applicationList = new ArrayList<>();
        for (PublishApplication publishApplication : publishApplications) {
            if (publishApplication.getPublishApi().getStatus() == 0 || publishApplication.getStatus() == 1) {
                //当前app已失效(即所属api被删除) 或 待审批
                Application app = new Application();
                AppPlan appPlan = new AppPlan();
                app.setStatus(publishApplication.getPublishApi().getStatus() == 0 ? 4 : 1);//失效或待审批都没有3scale相关的数据
                app.setId(publishApplication.getId().toString());
                app.setServiceId(publishApplication.getPublishApi().getId().toString());
                app.setServiceName(publishApplication.getPublishApi().getName());
                app.setCreatedAt(format.format(publishApplication.getCreateTime()));
                app.setServiceId(publishApplication.getPublishApi().getId().toString());
//                appPlan.setName(publishApplication.getApiPlan().getName());
                appPlan.setName(ApiDefaultPlanConstant.PLAN_NAME);
                appPlan.setId(publishApplication.getApiPlan().getId().toString());
                appPlan.setServiceId(publishApplication.getPublishApi().getId().toString());
                app.setPlan(appPlan);
                DataItem dataItem = dataItemRepository.findOne(publishApplication.getSystem());
                if(null != dataItem) {
                    app.setSystemName(dataItem.getItemName());
                }
                applicationList.add(app);
                continue;
            }
            for (Application application : applications) {
                if (publishApplication.getScaleApplicationId().equals(application.getId())){
                    application.setId(publishApplication.getId().toString());
                    application.setServiceId(publishApplication.getPublishApi().getId().toString());
                    application.setServiceName(publishApplication.getPublishApi().getName());
                    application.setCreatedAt(format.format(publishApplication.getCreateTime()));
                    application.getPlan().setId(publishApplication.getApiPlan().getId().toString());
                    application.getPlan().setServiceId(publishApplication.getPublishApi().getId().toString());          
                    application.getPlan().setName(ApiDefaultPlanConstant.PLAN_NAME);
                    application.setStatus(publishApplication.getStatus());
                    DataItem dataItem = dataItemRepository.findOne(publishApplication.getSystem());
                    if(null != dataItem) {
                        application.setSystemName(dataItem.getItemName());
                    }
                    applicationList.add(application);
                }
            }
        }
        applicationDtos.setApplication(applicationList);
        applicationDtos.setTotalRecord((int)p.getTotalElements());
        return applicationDtos;

//        ApplicationDtos applicationDtos = getApplicationList(domain, userName);
//        List<Application> applicationList = applicationDtos.getApplication();
//        Iterator<Application> iterator = applicationList.iterator();
//        if (serviceName != null) {
//            while(iterator.hasNext()) {
//                Application application = iterator.next();
//                if (!application.getServiceName().equals(serviceName)) {
//                    iterator.remove();
//                }
//            }
//            iterator = applicationList.iterator();
//        }
//        if (planName != null) {
//            while(iterator.hasNext()) {
//                Application application = iterator.next();
//                if (!application.getPlan().getName().equals(planName)) {
//                    iterator.remove();
//                }
//            }
//            iterator = applicationList.iterator();
//        }
//        if (since != null) {
//            Date sinceAt = format.parse(since);
//            cal.setTime(sinceAt);
//            cal.add(Calendar.HOUR, -8);
//            sinceAt = cal.getTime();
//            while(iterator.hasNext()) {
//                Application application = iterator.next();
//                String createdAtStr = application.getCreatedAt();
//                Date createdAt = format.parse(createdAtStr);
//                if (!(createdAt.getTime() >= sinceAt.getTime())) {
//                    iterator.remove();
//                }
//            }
//            iterator = applicationList.iterator();
//        }
//        if (until != null) {
//            Date untilAt = format.parse(until);
//            cal.setTime(untilAt);
//            cal.add(Calendar.HOUR, -8);
//            untilAt = cal.getTime();
//            while(iterator.hasNext()) {
//                Application application = iterator.next();
//                String createdAtStr = application.getCreatedAt();
//                Date createdAt = format.parse(createdAtStr);
//                if (!(createdAt.getTime() <= untilAt.getTime())) {
//                    iterator.remove();
//                }
//            }
//            iterator = applicationList.iterator();
//        }
//        if (state != null) {
//            while(iterator.hasNext()) {
//                Application application = iterator.next();
//                if (!application.getState().equals(state)) {
//                    iterator.remove();
//                }
//            }
//        }
//        int totalRecord = applicationList.size();
//        Pagination pagination = new Pagination(pageNum, pageSize, totalRecord);
//        applicationDtos.setApplication(applicationList.subList(pagination.getFromIndex(), pagination.getToIndex()));
//        applicationDtos.setTotalRecord(applicationList.size());

    }

    @Override
    public Application getApplication(String instanceName, String userName, String id) {
    	/*User user = userRepository.searchUserByName(userName);
    	if (null == user) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }*/
    	
        Instance ins =
                instanceRepository.searchInstanceByName(instanceName);

        if (null == ins) {
            log.error("can not found Instance,InstanceName is {}",instanceName);
            throw new OperationFailed("Instance not exist");
        }
        
        UserInstanceRelationship useRel = 
        		userInstanceRelationshipRepository.findByUserAndInstanceId(userName, ins.getId());
        if (null == useRel) {
            log.error("can not found UserInstanceRelationship,InstanceName is {}",instanceName);
            throw new OperationFailed("UserInstanceRelationship not exist");
        }
        PublishApplication app =
                publishApplicationRepository.getOne(Integer.parseInt(id));
        if (null == app.getId()) {
            log.error("can not found app");
            throw new OperationFailed("app not exist");
        }
        Application application = null;
        ApplicationXml applicationXml = accountStud.appXml(ins.getHost(), ins.getAccessToken(), useRel.getAccountId().toString(), app.getScaleApplicationId());
        applicationXml.getPlan().setName("0元/次数不限");
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String jsonStr = mapper.writeValueAsString(applicationXml);
            application = mapper.readValue(jsonStr, Application.class);
            application.setSystem(app.getSystem());
            application.setId(app.getId().toString());
        } catch (JsonProcessingException e) {
            log.error("exception",e);
        } catch (IOException e) {
            log.error("exception",e);
        }
        return application;
    }

    @Override
    public String deleteApplication(String instanceName, String userName, String id) {
        Instance instance = instanceRepository.searchInstanceByName(instanceName);
        if (null == instance) {
            log.error("can not found Instance,InstanceName is {}",instanceName);
            throw new OperationFailed("Instance not exist");
        }
        UserInstanceRelationship uir = userInstanceRelationshipRepository.findByUserAndInstanceId(userName, instance.getId());
        if (null == uir) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }

        PublishApplication publishApplication = publishApplicationRepository.findOne(Integer.valueOf(id));
        String res = accountStud.delAppDto(instance.getHost(), instance.getAccessToken(), uir.getAccountId().toString(), publishApplication.getScaleApplicationId());
        processRecordRepository.updateByRelId(Integer.valueOf(id), 0);
        publishApplicationRepository.updateStatus(Integer.valueOf(id), 0);
        return res;
    }

    @Override
    public Result<Application> updateApplication(String domainName, String userName, Application application) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}",domainName);
            throw new OperationFailed("domain not exist");
        }
        //User user = userRepository.searchUserByNameAndDomain(userName, domain.getId());
        if (null == userName) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }
        application.setAccessToken(domain.getAccessToken());
        return accountStud.updateApplication(domain.getHost(), null, application);
    }

    @Override
    public Result<Application> creatApplication(String instanceName, String userName, Application application) {

    	if (null == userName) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }
        Instance ins =
                instanceRepository.searchInstanceByName(instanceName);

        if (null == ins) {
            log.error("can not found Instance,InstanceName is {}",instanceName);
            throw new OperationFailed("Instance not exist");
        }

        PublishApiPlan publishApiPlan =
                publishApiPlanRepository.getOne(Integer.parseInt(application.getPlan().getId()));
        if (null == publishApiPlan.getId()) {
            log.error("can not found publishApiPlan");
            throw new OperationFailed("publishApiPlan not exist");
        }

        PublishApi api =
                publishApiRepository.getOne(publishApiPlan.getApiId());
        if (null == api.getId()) {
            log.error("can not found api");
            throw new OperationFailed("api not exist");
        }

        List<PublishApplication> apps =
                publishApplicationRepository.findByUserNameAndInstanceIdAndPlanIdAndStatus(userName,
                ins.getId(),Integer.parseInt(application.getPlan().getId()),1);
        if(!CollectionUtils.isEmpty(apps)) {
            throw new OperationFailed("有已经申请的订阅，还在流程中");
        }
        
        List<PublishApplication> app2s =
                publishApplicationRepository.findByUserNameAndInstanceIdAndPlanIdAndStatus(userName,
                ins.getId(),Integer.parseInt(application.getPlan().getId()),2);
        if(!CollectionUtils.isEmpty(app2s)) {
            throw new OperationFailed("API已经订阅，不能重复订阅");
        }
        
        PublishApplication app =publishApplicationRepository.findByUserNameAndInstanceIdAndPlanId(userName,
                ins.getId(),Integer.parseInt(application.getPlan().getId()));
        if(null==app) {
        	app = new PublishApplication();
        	app.setStatus(1);
            app.setInstance(ins);
            app.setApiPlan(publishApiPlan);
            app.setCreateTime(new Date());
            app.setCreator(userName);
            app.setDescription(application.getDescription());
            app.setName(application.getName());
            app.setPublishApi(api);
            app.setSystem(application.getSystem());
            app.setStatus(1);
        } 
        app.setStatus(1);
        app.setDescription(application.getDescription());
        app.setName(application.getName());
        app.setSystem( application.getSystem());
        app = publishApplicationRepository.saveAndFlush(app);

        ProcessRecord pr = new ProcessRecord();
        pr.setCreateTime(new Date());
        pr.setType("application");
        pr.setStatus(1);
        pr.setRelId(app.getId());
        ProcessDataDto processDataDto = new ProcessDataDto();
        processDataDto.setGroupId(api.getApiGroup().getId());
        processDataDto.setApiSystem(api.getGroup().getSystem());
        processDataDto.setAppSystem(application.getSystem());
        List<String> clusterPartitions = new ArrayList<>();
        clusterPartitions.add(ins.getClusterPartition());
        processDataDto.setClusterPartitions(clusterPartitions);
        pr.setExtVar(JSONObject.toJSONString(processDataDto));
        
        List<ApiInstance> apiInstances =
                publishApiInstanceRelationshipRepository.getApiInstancesPortal(api.getId());

        ProxyConfigDto proxyConfigDto = null;
        for (ApiInstance apiInstance:
                apiInstances) {
            if(proxyConfigDto == null){
                proxyConfigDto = serviceStud.latestPromote(apiInstance.getHost(),
                        apiInstance.getAccessToken(),apiInstance.getId(),
                        ModelConstant.ENV_SANDBOX);
                log.info("*********proxyConfigDto,{}",proxyConfigDto);
                if(null!=proxyConfigDto && null!=proxyConfigDto.getProxyConfig()
                        && null!=proxyConfigDto.getProxyConfig().getVersion()){
                    serviceStud.configPromote(apiInstance.getHost(),
                            apiInstance.getAccessToken(),
                            apiInstance.getId().toString(),
                            ModelConstant.ENV_SANDBOX,proxyConfigDto.getProxyConfig().getVersion(),
                            ModelConstant.ENV_PROD);
                }
                pr.setData(JSONObject.toJSON(proxyConfigDto).toString());
            }
        }
        
        processRecordRepository.saveAndFlush(pr);
        
        List<String> admins =
        		permissionService.listProjectAdmin(api.getGroup().getProjectId());
        String tenName = permissionService.getTenantName(api.getGroup().getTenantId());
        String pjName = permissionService.getProjectName(api.getGroup().getProjectId());
        String subject = String.format("待审批：%s用户申请订购%s项目%sAPI",
        		userName, pjName,api.getName());
        String content = String.format("请审批，申请人：%s，申请类别：%s租户申请订购%sAPI，详细申请内容请登录平台进行查询",
        		userName, tenName,api.getName());
        try {
//			MailUtils.sendNormalEmail(admins, null, subject, "hicloud", content);
		} catch (Exception e) {
			log.error("发送邮件失败");
		}
        return new Result<Application>();
    }

    @Override
    public Result<Application> creatNewKey(String instanceName, String userName, String applicationId) {
        Instance instance = instanceRepository.searchInstanceByName(instanceName);
        if (null == instance) {
            log.error("can not found Instance,InstanceName is {}",instanceName);
            throw new OperationFailed("Instance not exist");
        }
        UserInstanceRelationship uir = userInstanceRelationshipRepository.findByUserAndInstanceId(userName, instance.getId());
        if (null == uir) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }
        PublishApplication publishApplication = publishApplicationRepository.findOne(Integer.valueOf(applicationId));
        String key = UUID.randomUUID().toString().replaceAll("-", "");
        return accountStud.createNewKey(instance.getHost(), instance.getAccessToken(), uir.getAccountId().toString(),
                publishApplication.getScaleApplicationId(), key, executorService);
    }

    @Override
    public String deleteKey(String instanceName, String userName, String applicationId, String key) {
        Instance instance = instanceRepository.searchInstanceByName(instanceName);
        if (null == instance) {
            log.error("can not found Instance,InstanceName is {}",instanceName);
            throw new OperationFailed("Instance not exist");
        }
        UserInstanceRelationship uir = userInstanceRelationshipRepository.findByUserAndInstanceId(userName, instance.getId());
        if (null == uir) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }
        PublishApplication publishApplication = publishApplicationRepository.findOne(Integer.valueOf(applicationId));
        return accountStud.deleteKey(instance.getHost(), instance.getAccessToken(), uir.getAccountId().toString(), publishApplication.getScaleApplicationId(), key);
    }

    public ApplicationDtos getApplicationList(String domainName, String userName) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        ApplicationDtos applicationDtos = null;
        if (null == domain) {
            log.error("can not found domain,domain is {}",domainName);
            throw new OperationFailed("domain not exist");
        }
        if (null == userName) {
            log.error("can not found user,userName is {}", userName);
            throw new OperationFailed("user not exist");
        }
        Future<List<ServiceDto>> serviceFeature = executorService.submit(new Callable<List<ServiceDto>>() {
            @Override
            public List<ServiceDto> call() {
                return serviceStud.serviceDtoList(domain.getHost(), domain.getAccessToken());
            }
        });
        ApplicationXmlDtos applicationXmlDtos = accountStud.appXmlDtoList(domain.getHost(), domain.getAccessToken(), null);
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String jsonStr = mapper.writeValueAsString(applicationXmlDtos);
            applicationDtos = mapper.readValue(jsonStr, ApplicationDtos.class);
        } catch (JsonProcessingException e) {
            log.error("Exception:",e);
        } catch (IOException e) {
            log.error("Exception:",e);
        }
        try {
            List<ServiceDto> serviceDtos = serviceFeature.get();
            if(applicationDtos != null){
                for (Application application : applicationDtos.getApplication()) {
                    for (ServiceDto serviceDto : serviceDtos) {
                        if (application.getServiceId().equals(serviceDto.getService().getId())) {
                            application.setServiceName(serviceDto.getService().getName());
                        }
                    }
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException e) {
            log.error("Exception:",e);
        }
        return applicationDtos;
    }
}
