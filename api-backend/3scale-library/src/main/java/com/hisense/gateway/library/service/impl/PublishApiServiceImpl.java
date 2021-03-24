/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/2/18 @author wangjinshan
 */
package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.*;
import com.hisense.gateway.library.exception.ListEmptyExist;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.PolicyHeader;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.portal.PublishApiBasicInfo;
import com.hisense.gateway.library.model.base.portal.PublishApiInfo;
import com.hisense.gateway.library.model.base.portal.PublishApiStat;
import com.hisense.gateway.library.model.base.portal.PublishApiVO;
import com.hisense.gateway.library.model.dto.buz.ApiInstanceDto;
import com.hisense.gateway.library.model.dto.buz.ProcessDataDto;
import com.hisense.gateway.library.model.dto.buz.WorkFlowDto;
import com.hisense.gateway.library.model.dto.web.*;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.model.pojo.base.ApiMappingRule;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.*;
import com.hisense.gateway.library.stud.*;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.CommonUtil;
import com.hisense.gateway.library.utils.IpUtils;
import com.hisense.gateway.library.utils.RequestUtils;
import com.hisense.gateway.library.utils.ScaleUtils;
import com.hisense.gateway.library.utils.api.MappingRuleUtil;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.utils.meta.GlobalSettings;
import com.hisense.gateway.library.utils.api.PublishApiUtil;
import com.hisense.gateway.library.web.form.ApplicationForm;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.persistence.*;
import javax.persistence.criteria.*;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.ApiConstant.*;
import static com.hisense.gateway.library.constant.ApiConstant.ApiBatchOPS.*;
import static com.hisense.gateway.library.constant.InstanceEnvironment.ENVIRONMENT_PRODUCTION;
import static com.hisense.gateway.library.model.ModelConstant.*;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.model.Result.FAIL;
import static com.hisense.gateway.library.model.Result.OK;

@Slf4j
@Service
public class PublishApiServiceImpl implements PublishApiService {

    @Resource
    WorkFlowService workFlowService;

    @Resource
    SystemInfoRepository systemInfoRepository;

    @Resource
    PublishApiRepository publishApiRepository;

    @Resource
    PublishApiGroupRepository publishApiGroupRepository;

    @Resource
    InstanceRepository instanceRepository;

    @Resource
    PublishApiInstanceRelationshipRepository pairRepository;

    @Resource
    PublishApiPlanRepository publishApiPlanRepository;

    @Resource
    ApiPolicyRepository apiPolicyRepository;

    @Resource
    PublishApiPolicyRelationshipRepository paprRepository;

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

    // ifdef mingguilai.ex
    @Autowired
    EurekaServiceRepository eurekaServiceRepository;

    @Autowired
    ApiDocFileRepository apiDocFileRepository;

    @Autowired
    MappingRuleService mappingRuleService;

    @Autowired
    MailService mailService;

    @Autowired
    MappingRuleRepository mappingRuleRepository;

    @Autowired
    AlertPolicyRepository alertPolicyRepository;

    @Autowired
    PublishApiTemporaryDataRepository publishApiTemporaryDataRepository;

    @Autowired
    OperationApiRepository operationApiRepository;

    @Autowired
    LimitStud limitStud;

    @Autowired
    ApiCastService apiCastService;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    ApiInvokeRecordService apiInvokeRecordService;

    @Value("${flow.app.link}")
    private String link;

    @Value("${flow.app.im-link}")
    private String imLink;

    private static String FILE_PATH="/tmp/apiDocFiles/";

    @Override
    public List<Integer> listAll3ScaleApiIds(String tenantId) {
        List<Instance> instances = instanceRepository.findAllByTenantId(tenantId);
        if (instances == null || instances.size() == 0) {
            log.error("查询失败，未查询到instance信息");
            return null;
        }

        List<Integer> serviceDtos = new ArrayList<>();
        for (Instance instance : instances) {
            serviceDtos.addAll(serviceStud.serviceDtoIdList(instance.getHost(), instance.getAccessToken()));
        }

        return serviceDtos;
    }

    @Override
    public PublishApiDto getPublishApi(Integer id) {
        if (id == null) {
            throw new NotExist(String.format("api id %d not exist", id));
        }

        PublishApi publishApi = publishApiRepository.findOne(id);
        if (publishApi == null) {
            throw new NotExist(String.format("api with id %d not exist", id));
        }
        List<String> partitions = new ArrayList<>();
        String partition = getPartition(publishApi.getPartition());
        partitions.add(partition);
        List<ApiInstance> apiInstances
                = publishApiInstanceRelationshipRepository.getApiInstances(id,
                InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName(), partitions);// guilai 2020/09/16
        if (CollectionUtils.isEmpty(apiInstances)) {
            throw new NotExist(String.format("api instance with apiid %d not exist", id));
        }

        PublishApiInstanceRelationship apiInstanceRelationship =
                publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(publishApi.getId(), Math.toIntExact(apiInstances.get(0).getId()));

        ProxyConfigDto proxyConfigDto = serviceStud.latestPromote(
                apiInstances.get(0).getHost(),
                apiInstances.get(0).getAccessToken(),
                apiInstances.get(0).getId(),
                ModelConstant.ENV_SANDBOX);

        log.info("{}proxyConfigDto={}", TAG, proxyConfigDto);

        publishApi.setApiMappingRules(mappingRuleService.findRuleByApiId(publishApi.getId()));

        PublishApiDto publishApiDto = PublishApiUtil.buildDtoWithApi(publishApi,
                ApiConstant.ApiDtoBuildType.API_DTO_BUILD_QUERY_DETAIL);

        String userName = CommonBeanUtil.getLoginUserName();
        //获取当前用户已经订阅过的系统
        List<Integer> systemId= publishApplicationRepository.findStatusByUserAndId(id,userName);
        publishApiDto.setSubscribeSystem(systemId);

        if (publishApi.getGroup() != null) {
            List<Integer> dataItemIds = new ArrayList<>(3);
            dataItemIds.add(publishApi.getGroup().getSystem());
            dataItemIds.add(publishApi.getGroup().getCategoryOne());
            dataItemIds.add(publishApi.getGroup().getCategoryTwo());

            List<DataItem> dataItemList = dataItemRepository.findByIds(dataItemIds);
            for (DataItem dataItem : dataItemList) {
                if (dataItem.getId().equals(publishApiDto.getPublishApiGroupDto().getSystem())) {
                    publishApiDto.getPublishApiGroupDto().setSystemName(dataItem.getItemName());
                } else if (dataItem.getId().equals(publishApiDto.getPublishApiGroupDto().getCategoryOne())) {
                    publishApiDto.getPublishApiGroupDto().setCategoryOneName(dataItem.getItemName());
                } else if (dataItem.getId().equals(publishApiDto.getPublishApiGroupDto().getCategoryTwo())) {
                    publishApiDto.getPublishApiGroupDto().setCategoryTwoName(dataItem.getItemName());
                }
            }
        } else {
            log.info("{}Automatically created api {},has not group info", TAG, publishApi.getUrl());
        }

        /*List<String> partitions = new ArrayList<>();
        List<ApiInstanceDto> apiInstanceDtos = publishApiInstanceRelationshipRepository.getApiInstanceDtos(id);
        for (ApiInstanceDto apiInstanceDto : apiInstanceDtos) {
            partitions.add(apiInstanceDto.getClusterPartition());
        }
        publishApiDto.setPartitions(partitions);*/

        if (apiInstances.size() > 0) {
            publishApiDto.setRequestProduction(apiInstances.get(0).getRequestSandbox());
        }

        if (proxyConfigDto != null && proxyConfigDto.getProxyConfig() != null
                && proxyConfigDto.getProxyConfig().getContent() != null
                && proxyConfigDto.getProxyConfig().getContent().getProxy() != null) {
            publishApiDto.setProxy(proxyConfigDto.getProxyConfig().getContent().getProxy());
        }

        /*if (proxyConfigDto != null && proxyConfigDto.getProxyConfig() != null
                && proxyConfigDto.getProxyConfig().getContent() != null
                && proxyConfigDto.getProxyConfig().getContent().getProxy() != null
                && proxyConfigDto.getProxyConfig().getContent().getProxy().getProxyRules() != null) {
            publishApiDto.setMappingRuleDtos(proxyConfigDto.getProxyConfig().getContent().getProxy().getProxyRules());
        }*/
        List<ApiDocFile> picFiles = apiDocFileRepository.findApiDocByTypeAndApiId(id, "1");
        List<ApiDocFile> attFiles = apiDocFileRepository.findApiDocByTypeAndApiId(id, "2");
        publishApiDto.setPicFiles(picFiles);
        publishApiDto.setAttFiles(attFiles);
        return publishApiDto;
    }

    public static String getPartition(Integer partitionCode) {
        String partition=null;
        if(InstancePartition.PARTITION_INNER.getCode()==partitionCode){
            partition = InstancePartition.PARTITION_INNER.getName();
        }else if(InstancePartition.PARTITION_OUTER.getCode()==partitionCode){
            partition = InstancePartition.PARTITION_OUTER.getName();
        }
        return partition;
    }

    private List<ApiMappingRuleDto> parseMappingRule(List<ApiMappingRuleDto> proxyRules, String url) {
        List<ApiMappingRuleDto> dtos = new ArrayList<>();
        ApiMappingRuleDto apiMappingRuleDto = null;

        for (ApiMappingRuleDto dto : proxyRules) {
            boolean flag = true;
            if (!CollectionUtils.isEmpty(dtos)) {
                for (ApiMappingRuleDto tmp : dtos) {
                    String pattern = dto.getPattern();
                    if (pattern.contains(url)) {
                        if (url.endsWith("/")) {
                            dto.setPattern(pattern.substring(url.length() - 1, pattern.length()));
                        } else {
                            dto.setPattern(pattern.substring(url.length(), pattern.length()));
                        }
                    }

                    if (dto.getPattern().equals(tmp.getPattern())) {
                        tmp.setHttpMethod(tmp.getHttpMethod() + "," + dto.getHttpMethod());
                        flag = false;
                        break;
                    }
                    dto.setPattern(pattern);
                }
            }

            if (flag) {
                apiMappingRuleDto = new ApiMappingRuleDto();
                apiMappingRuleDto.setHttpMethod(dto.getHttpMethod());
                String patternResult = "";
                String pattern = dto.getPattern();
                if (pattern.contains(url)) {
                    if (url.endsWith("/")) {
                        patternResult = pattern.substring(url.length() - 1, pattern.length());
                    } else {
                        patternResult = pattern.substring(url.length(), pattern.length());
                    }
                } else {
                    patternResult = pattern;
                }

                apiMappingRuleDto.setPattern(patternResult);
                dtos.add(apiMappingRuleDto);
            }
        }
        return dtos;
    }

    /**
     * 被动同步的API无(分组、分区), 有(systemname, 创建时间、名称), 创建者不重要
     * 主动拉取的API无(分区)，有(systemname, 分组及其他)
     * 手动创建的API均有
     */
    @Override
    public Page<PublishApiDto> findByPage(String tenantId, String projectId, String environment, PageRequest pageable,
                                          PublishApiQuery apiQuery) {
        log.info("{} apiQuery {}", TAG, apiQuery);
        List<PublishApiDto> list = new ArrayList<>();
        Set<Integer> systemIdSet = null;
        List<PublishApiGroup> defaultPublishApiGroups = null;
        List<PublishApiGroup> specialPublishApiGroups = null;
        // 1-查询分组\系统
        if (MiscUtil.isNotEmpty(apiQuery.getGroupIds())) {
            Specification<PublishApiGroup> groupSpec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();

                if (tenantId != null) {
                    andList.add(builder.equal(root.get("tenantId").as(String.class), tenantId));
                }

                if (projectId != null) {
                    andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
                }

                if (MiscUtil.isNotEmpty(apiQuery.getGroupIds())) {
                    CriteriaBuilder.In<Integer> in = builder.in(root.get("id"));
                    for (Integer groupId : apiQuery.getGroupIds()) {
                        in.value(groupId);
                    }
                    andList.add(in);
                }

                andList.add(builder.equal(root.get("status").as(Integer.class), 1));
                return builder.and(andList.toArray(new Predicate[0]));
            };

            specialPublishApiGroups = publishApiGroupRepository.findAll(groupSpec);
            if (CollectionUtils.isEmpty(specialPublishApiGroups)) {
                throw new NotExist("api group not exist");
            }
        } else {
            // 未指定分组时,未分组的查询systemId, 已分组的查询默认分组
            List<Integer> systemIdList = publishApiGroupRepository.findSystemByTenantProjectAndService(tenantId,
                    projectId);
            if (MiscUtil.isNotEmpty(systemIdList)) {
                systemIdSet = MiscUtil.list2Set(systemIdList);
            }

            defaultPublishApiGroups = publishApiGroupRepository.findByTenantAndProject(tenantId, projectId);
        }

        // 2-查询发布环境对应的Instance
        /*List<Integer> instanceIdList = null;
        if (MiscUtil.isNotEmpty(apiQuery.getPartitions())) {
            instanceIdList = publishApiInstanceRelationshipRepository.findAllByPartitions(apiQuery.getPartitions());
        }*/

        // 3-查询API
        Set<Integer> finalSystemIdSet = systemIdSet;
        //List<Integer> finalInstanceIdList = instanceIdList;
        List<PublishApiGroup> finalDefaultPublishApiGroups = defaultPublishApiGroups;
        List<PublishApiGroup> finalSpecialPublishApiGroups = specialPublishApiGroups;
        if(CollectionUtils.isEmpty(finalDefaultPublishApiGroups) && CollectionUtils.isEmpty(finalSpecialPublishApiGroups)){
            return new PageImpl<>(list, pageable, 0);
        }

        log.info("{}finalSystemIdSet={}", TAG, finalSystemIdSet != null ? finalSystemIdSet : 0);
        //log.info("{}finalInstanceIdList={}", TAG, finalInstanceIdList != null ? finalInstanceIdList : 0);
        log.info("{}finalDefaultPublishApiGroups={}", TAG, finalDefaultPublishApiGroups != null ?
                finalDefaultPublishApiGroups.stream().map(item -> String.format("[%d]: %s", item.getId(),
                        item.getName())).collect(Collectors.toList()) : 0);
        log.info("{}finalSpecialPublishApiGroups={}", TAG, finalSpecialPublishApiGroups != null ?
                finalSpecialPublishApiGroups.stream().map(item -> String.format("[%d]: %s", item.getId(),
                        item.getName())).collect(Collectors.toList()) : 0);

        // 全局条件
        Specification<PublishApi> publishApiSpec = (root, query, builder) -> {
            // and list
            List<Predicate> basicList = new LinkedList<>();

            // env
            basicList.add(builder.equal(root.get("environment").as(Integer.class),
                    InstanceEnvironment.fromCode(environment).getCode()));

            if (MiscUtil.isNotEmpty(apiQuery.getPartitions())) {
                CriteriaBuilder.In<Integer> partitionIn = builder.in(root.get("partition").as(Integer.class));
                apiQuery.getPartitions().forEach(partitionIn::value);
                basicList.add(partitionIn);
            }

            CriteriaBuilder.In<Integer> statusIn = builder.in(root.get("status").as(Integer.class));
            if (apiQuery.isPublished()) {
                statusIn.value(API_FOLLOWUP_PROMOTE);//3
                statusIn.value(API_COMPLETE);//4
                log.info("{}findByPage add IN statusIn Published", TAG);
            } else {
                statusIn.value(API_INIT);//1
                statusIn.value(API_FIRST_PROMOTE);//2
                statusIn.value(API_FIRST_PROMOTE_REJECT);//5
                statusIn.value(API_FOLLOWUP_PROMOTE_REJECT);//6
                log.info("{}findByPage add IN statusIn Unpublished", TAG);
            }
            basicList.add(statusIn);

            if (StringUtils.isNotBlank(apiQuery.getName())) {
                basicList.add(builder.like(root.get("name").as(String.class), "%" + apiQuery.getName() + "%",
                        GatewayConstants.ESCAPECHAR));
                log.info("{}findByPage add name like", TAG);
            }

            /*if (!CollectionUtils.isEmpty(finalInstanceIdList)) {
                CriteriaBuilder.In<Integer> inId = builder.in(root.get("id"));
                for (Integer id : finalInstanceIdList) {
                    inId.value(id);
                }
                basicList.add(inId);
                log.info("{}findByPage add IN finalInstanceIdList", TAG);
            }*/

            TimeQuery timeQuery = apiQuery.getTimeQuery();
            if (timeQuery != null) {
                if (timeQuery.getStart() != null && timeQuery.getEnd() != null) {
                    basicList.add(builder.between(root.get("createTime").as(Date.class), timeQuery.getStart(),
                            timeQuery.getEnd()));
                } else if (timeQuery.getStart() != null) {
                    basicList.add(builder.greaterThanOrEqualTo(root.get("createTime").as(Date.class),
                            timeQuery.getStart()));
                } else if (timeQuery.getEnd() != null) {
                    basicList.add(builder.lessThanOrEqualTo(root.get("createTime").as(Date.class), timeQuery.getEnd()));
                }
                log.info("{}findByPage add between TimeQuery", TAG);
            }

            if (!CollectionUtils.isEmpty(apiQuery.getUsers())) {
                CriteriaBuilder.In<String> creators = builder.in(root.get("creator"));
                for (String creator : apiQuery.getUsers()) {
                    creators.value(creator);
                }
                basicList.add(creators);
                log.info("{}findByPage add IN createId", TAG);
            }

            // or list
            List<Integer> groupIds = new ArrayList<>();

            Predicate orPredicate = null;
            if (MiscUtil.isNotEmpty(finalSpecialPublishApiGroups)) {
                CriteriaBuilder.In<PublishApiGroup> groupIn1 = builder.in(root.get("group"));
                for (PublishApiGroup pg : finalSpecialPublishApiGroups) {
                    groupIn1.value(pg);
                    groupIds.add(pg.getId());
                }
                basicList.add(groupIn1);
                log.info("{}findByPage add IN finalSpecialPublishApiGroups {}", TAG, groupIds);
            } else {
                CriteriaBuilder.In<Integer> groupIn = null;
                CriteriaBuilder.In<Integer> systemIn = null;

                if (MiscUtil.isNotEmpty(finalDefaultPublishApiGroups)) {
                    groupIn = builder.in(root.get("group").get("id"));
                    for (PublishApiGroup pg : finalDefaultPublishApiGroups) {
                        groupIn.value(pg.getId());
                        groupIds.add(pg.getId());
                    }
                    log.info("{}findByPage add IN finalDefaultPublishApiGroups {}", TAG, groupIds);
                }

                if (MiscUtil.isNotEmpty(finalSystemIdSet)) {
                    systemIn = builder.in(root/*.get("eurekaService")*/.get("systemId"));
                    for (Integer system : finalSystemIdSet) {
                        systemIn.value(system);
                    }

                    log.info("{}findByPage add IN systemAndList", TAG);
                }

                if (groupIn != null && systemIn != null) {
                    orPredicate = builder.or(groupIn, systemIn);
                } else if (groupIn != null) {
                    basicList.add(groupIn);
                } else if (systemIn != null) {
                    basicList.add(systemIn);
                }
            }

            if (orPredicate != null) {
                return query.where(builder.and(basicList.toArray(new Predicate[0])), orPredicate).getRestriction();
            } else {
                return builder.and(basicList.toArray(new Predicate[0]));
            }
        };

        log.info("{}publishApiSpec {}", TAG, publishApiSpec.toString());

        Page<PublishApi> p = publishApiRepository.findAll(publishApiSpec, pageable);
        List<PublishApi> publishApis = p.getContent();

        if (MiscUtil.isNotEmpty(publishApis)) {
            log.info("{}Get {} apis", TAG, publishApis.size());
        }

        for (PublishApi publishApi : publishApis) {
            publishApi.setApiInstanceDtos(
                    publishApiInstanceRelationshipRepository.getApiInstanceDtos(publishApi.getId())
            );
        }


        PublishApiDto publishApiDto = null;
        List<ApiInstanceDto> apiInstanceDtos = null;
        List<String> partitions2 = null;
        PublishApiPolicyRelationship publishApiPolicyRelationship = null;
        List<ApiPolicy> apiPolicies = null;

        for (PublishApi publishApi : p.getContent()) {
            //publishApiDto = new PublishApiDto(publishApi);
            /*Result<Boolean> result;
            if ((result = metaDataService.buildMetaDataForPublishApi(publishApi)).isFailure()) {
                log.info("{}result={}", TAG, result);
                continue;
            }*/

            publishApiDto = PublishApiUtil.buildDtoWithApi(publishApi,
                    ApiConstant.ApiDtoBuildType.API_DTO_BUILD_QUERY_LIST);

            partitions2 = new ArrayList<>();
            /*apiInstanceDtos = publishApiInstanceRelationshipRepository.getApiInstanceDtos(publishApi.getId());
            for (ApiInstanceDto apiInstanceDto : apiInstanceDtos) {
                partitions2.add(apiInstanceDto.getClusterPartition());
            }
            publishApiDto.setPartitions(partitions2);*/

            //流控策略
            apiPolicies = apiPolicyRepository.findByApiIdGroupByPublishLimitPolicyId(publishApi.getId());
            if (!CollectionUtils.isEmpty(apiPolicies)) {
                publishApiDto.setBindingPolicyId(apiPolicies.get(0).getId());
                publishApiDto.setBindingPolicyName(apiPolicies.get(0).getName());
                publishApiDto.setBindingPolicyEnabled(apiPolicies.get(0).getEnabled());
                JSONObject configJson = JSONObject.parseObject(apiPolicies.get(0).getConfig());
                publishApiDto.setBindingPolicyValue(Integer.parseInt(configJson.get("value").toString()));
                publishApiDto.setBindingPolicyPeriod(configJson.get("period").toString());
            }

            //告警策略
            log.info("{}lertPolicyId {}", TAG, publishApi.getAlertPolicyId());
            Integer alertPolicyId = publishApi.getAlertPolicyId();
            if(alertPolicyId !=null){
                AlertPolicy alertPolicy = alertPolicyRepository.getOne(alertPolicyId);
                if(alertPolicy!=null){
                    publishApiDto.setAlertPolicyId(alertPolicy.getId());
                    publishApiDto.setAlertPolicyName(alertPolicy.getName());
                }
            }

            List<Integer> dataItemIds = new ArrayList<>(3);
            if (publishApiDto.getPublishApiGroupDto() != null) {
                dataItemIds.add(publishApiDto.getPublishApiGroupDto().getSystem());
                dataItemIds.add(publishApiDto.getPublishApiGroupDto().getCategoryOne());
                dataItemIds.add(publishApiDto.getPublishApiGroupDto().getCategoryTwo());
            }

            if (MiscUtil.isNotEmpty(dataItemIds)) {
                List<DataItem> dataItemList = dataItemRepository.findByIds(dataItemIds);
                for (DataItem dataItem : dataItemList) {
                    if (dataItem.getId().equals(publishApiDto.getPublishApiGroupDto().getSystem())) {
                        publishApiDto.getPublishApiGroupDto().setSystemName(dataItem.getItemName());
                    } else if (dataItem.getId().equals(publishApiDto.getPublishApiGroupDto().getCategoryOne())) {
                        publishApiDto.getPublishApiGroupDto().setCategoryOneName(dataItem.getItemName());
                    } else if (dataItem.getId().equals(publishApiDto.getPublishApiGroupDto().getCategoryTwo())) {
                        publishApiDto.getPublishApiGroupDto().setCategoryTwoName(dataItem.getItemName());
                    }
                }
            }
            list.add(publishApiDto);
        }

        return new PageImpl<>(list, pageable, p.getTotalElements());
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Integer> createPublishApi(String tenantId, String projectId, String environment, PublishApiDto publishApiDto) throws Exception {
        Result<Integer> result = new Result<>(FAIL, "创建失败", null);
        result.setAlert(1);

        log.info("{}Manual,start to create Api {}", TAG, publishApiDto);

        PublishApiUtil.doPreProcessForDto(publishApiDto, true);

        Result<ApiValidateStatus> validateStatusResult = validateApiFields(publishApiDto, true,environment);
        if (validateStatusResult.isFailure()) {
            log.error("{}Fail to validate {}", TAG, validateStatusResult.getMsg());
            result.setError(validateStatusResult.getMsg());
            return result;
        }

        ApiValidateStatus validateStatus = validateStatusResult.getData();
        int envCode = InstanceEnvironment.fromCode(environment).getCode();
        // URL+mappingRule校验
        Result<List<PublishApi>> existsApis = mappingRuleService.existApisWithSameMappingRule(
                MappingRuleUtil.buildRuleWithDtos(publishApiDto.getApiMappingRuleDtos(), false),envCode);

        if (existsApis.isSuccess()) {
            result.setMsg(existsApis.getMsg());// guilai.ming
            return result;
        }

        validateStatus.setTenantId(tenantId);
        validateStatus.setProjectId(projectId);

        //跨系统不允许有相同的后端地址+端口
        /*if(publishApiDto.getHost()!=null && publishApiDto.getAccessProtocol()!=null){
            List<PublishApi> publishApis = publishApiRepository.findApiByHost(publishApiDto.getHost(),publishApiDto.getAccessProtocol(),envCode);
            if(publishApis!=null && publishApis.size()>0){
                for(PublishApi api : publishApis){
                    PublishApiGroup apiGroup = publishApiGroupRepository.getOne(api.getGroup().getId());
                    String project = apiGroup.getProjectId();
                    if(!projectId.equals(project)){
                        result.setMsg("后端服务地址在其他系统中已存在！");
                        return result;
                    }
                }
            }
        }*/


        //20200906 guilai.ming
        publishApiDto.setSystemId(validateStatus.getGroup().getSystem());


        result = createPublishApi(publishApiDto, validateStatus, environment, CommonBeanUtil.getLoginUserName());
        if (result.isSuccess()) {
            log.info("{}Manual,success to create {}", TAG, publishApiDto.getSimpleName());
            //创建操作记录
            String msg = OK.equals(result.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+result.getMsg();
            Integer apiId = result.getData();
            PublishApi api = publishApiRepository.getOne(apiId);
            String name = OperationType.fromCode(0).getName()+api.getName();
            OperationApi operationApi = new OperationApi(name,api.getCreator(),api.getCreateTime(),api.getCreateTime(),0,msg,api);
            operationApiRepository.save(operationApi);
        } else {
            log.error("{}Manual,fail to create {}", TAG, publishApiDto.getSimpleName());
        }

        return result;
    }

    @Override
    public Result<Boolean> createPublishApis(@NonNull Set<PublishApiDto> publishApiDtos, String user) {
        Result<Boolean> result = new Result<>(FAIL, "Fail", false);

        int successCount = 0;
        log.debug("{}Auto, start to create {} Apis", TAG, publishApiDtos.size());
        for (PublishApiDto publishApiDto : publishApiDtos) {
            log.info("{}Auto create {}", TAG, publishApiDto.getSimpleName());

            Result<ApiValidateStatus> validateStatusResult = validateApiFields(publishApiDto, true, publishApiDto.getEnvironment());
            if (validateStatusResult.isFailure()) {
                log.error("{}Fail to validate {}", TAG, validateStatusResult.getMsg());
                continue;
            }

            ApiValidateStatus apiValidateStatus = validateStatusResult.getData();
            apiValidateStatus.setTenantId(GlobalSettings.getDefaultTenant());
            apiValidateStatus.setProjectId(GlobalSettings.getDefaultProject());

            Result<Integer> result1;
            try{
                if ((result1 = createPublishApi(publishApiDto, apiValidateStatus,publishApiDto.getEnvironment(), user)).isFailure()) {
                    log.info("{} {} {}", TAG, publishApiDto.getSimpleName(), result1.getMsg());
                } else {
                    successCount++;
                }
            }catch (Exception e){
                log.error("{}Auto,fail to create {},error:{}", TAG, publishApiDto.getSimpleName(),e);
            }
            log.debug("{}Auto, success to auto create {}", TAG, publishApiDto.getSimpleName());
        }

        log.debug("{}Auto, success to auto create {} Apis", TAG, successCount);
        result.setCode(Result.OK);
        result.setMsg("Success");
        result.setData(true);
        return result;
    }

    /**
     * 用系统名去创建account, 一个用户登录
     */
    private String getAccountId(String user, Instance instance) {
        UserInstanceRelationship uir = new UserInstanceRelationship();
        if (user != null) {
            uir = userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
        }
        String accountId = "";
        if (uir == null) {
            // 存在漏创的情况
            uir = createAccount(instance.getHost(), instance.getAccessToken(), instance.getId(), user, uir);
        }else{
            //确认3scale用户是否真实存在
            Account account = new Account();
            account.setAccessToken(instance.getAccessToken());
            account.setUsername(user);
            AccountDto accountDtoFind = accountStud.accountFind(instance.getHost(), account);
            if (accountDtoFind == null || accountDtoFind.getAccount() == null || accountDtoFind.getAccount().getId() == null) {
                account.setEmail(user + "@hisense.com");
                account.setPassword("123456");
                account.setOrgName("hisense" + RandomStringUtils.random(6, true, true).toLowerCase());
                Result<AccountDto> accountDtoResult = accountStud.signUp(instance.getHost(), account);
                AccountDto accountDto = accountDtoResult.getData();
                log.info("{} accountDto {}", TAG, accountDto);
                uir.setAccountId(accountDto.getAccount().getId());
                userInstanceRelationshipRepository.saveAndFlush(uir);
            }
        }
        if (uir.getAccountId() == null) {
            uir = updateAccount(instance.getHost(), instance.getAccessToken(), user, uir);
        }
        accountId = String.valueOf(uir.getAccountId());
        return accountId;
    }

    /**
     * 更新账户
     */
    private UserInstanceRelationship updateAccount(String host, String accessToken,
                                                   String user, UserInstanceRelationship uir) {
        Account account = new Account();
        account.setAccessToken(accessToken);
        account.setUsername(user);
        AccountDto accountDtoFind = accountStud.accountFind(host, account);
        if (accountDtoFind == null || accountDtoFind.getAccount() == null || accountDtoFind.getAccount().getId() == null) {
            account.setEmail(user + "@hisense.com");
            account.setPassword("123456");
            account.setOrgName("hisense" + RandomStringUtils.random(6, true, true).toLowerCase());
            Result<AccountDto> accountDtoResult = accountStud.signUp(host, account);
            AccountDto accountDto = accountDtoResult.getData();
            uir.setAccountId(accountDto.getAccount().getId());
        } else {
            uir.setAccountId(accountDtoFind.getAccount().getId());
        }
        uir = userInstanceRelationshipRepository.saveAndFlush(uir);
        return uir;
    }

    /**
     * 创建账户
     */
    private UserInstanceRelationship createAccount(String host, String accessToken, Integer instanceId,
                                                   String user, UserInstanceRelationship uir) {
        UserInstanceRelationship uir1 = new UserInstanceRelationship();
        Account account = new Account();
        account.setAccessToken(accessToken);
        account.setUsername(user);
        AccountDto accountDtoFind = accountStud.accountFind(host, account);
        if (accountDtoFind == null || accountDtoFind.getAccount() == null || accountDtoFind.getAccount().getId() == null) {
            account.setEmail(user + "@hisense.com");
            account.setPassword("123456");
            account.setOrgName("hisense" + RandomStringUtils.random(6, true, true).toLowerCase());
            Result<AccountDto> accountDtoResult = accountStud.signUp(host, account);
            AccountDto accountDto = accountDtoResult.getData();
            log.info("{} accountDto {}", TAG, accountDto);
            uir1.setAccountId(accountDto.getAccount().getId());
        } else {
            uir1.setAccountId(accountDtoFind.getAccount().getId());
        }
        uir1.setInstanceId(instanceId);
        uir1.setUserName(user);

        UserInstanceRelationship userInstanceRelationship =
                userInstanceRelationshipRepository.findByUserAndInstanceId(user, instanceId);
        if (userInstanceRelationship == null) {
            userInstanceRelationship = userInstanceRelationshipRepository.saveAndFlush(uir1);
        }

        if (null == userInstanceRelationship.getAccountId()) {
            userInstanceRelationship.setAccountId(accountDtoFind.getAccount().getId());
            userInstanceRelationshipRepository.saveAndFlush(userInstanceRelationship);
        }

        return uir1;
    }

    @Override
    public Result<Boolean> deletePublishApi(Integer id) {
        Result<Boolean> returnResult = new Result<>();
        //查看api是否已经下线
        PublishApi publishApi = publishApiRepository.findOne(id);
        if (publishApi == null) {
            //throw new NotExist("api not exist");
            returnResult.setCode(FAIL);
            returnResult.setMsg("API不存在");
            returnResult.setData(false);
            return returnResult;
        }

        // 查看API是否已下线
        Integer isOnline = publishApi.getIsOnline();
        if (isOnline != null && isOnline.equals(1)) {
            //api属于上线状态,需先下线
            returnResult.setCode(FAIL);
            returnResult.setMsg("API未下线,不允许删除");
            returnResult.setData(false);
            return returnResult;
        }

        if (publishApi.getStatus() == 2 || publishApi.getStatus() == 3) {
            //Api属于上线状态需要先下线
            returnResult.setCode(FAIL);
            returnResult.setMsg("API处于审批处理中,不能删除");
            returnResult.setData(false);
            return returnResult;
        }

        PublishApiInstanceRelationship publishApiInstanceRelationship;
        //for (String partition : partitions) {
        List<Instance> instances =
                instanceRepository.searchInstanceByPartition(InstancePartition.fromCode(publishApi.getPartition()).getName());
        if (MiscUtil.isEmpty(instances)) {
            log.error("instance not found");
            throw new OperationFailed("instance not found");
        }

        for (Instance instance : instances) {
            publishApiInstanceRelationship = publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(id,
                    instance.getId());
            if (publishApiInstanceRelationship != null) {
                // 删除3scale上的service
                serviceStud.serviceDelete(instance.getHost(), instance.getAccessToken(),
                        publishApiInstanceRelationship.getScaleApiId().toString());
            }

            PublishApiPolicyRelationship papr = publishApiPolicyRelationshipRepository.findAllByApiIdAndInstanceId(id, instance.getId());
            if(papr!=null){
                Integer publishPolicyId = papr.getPublishPolicyId();
                ApiPolicy policy = apiPolicyRepository.getOne(publishPolicyId);
                if(policy!=null){
                    if(policy.getEnabled()){
                        PublishApiPlan publishApiPlan = publishApiPlanRepository.findByApiIdAndInstanceId(id, instance.getId());
                        Long scalePlanId = publishApiPlan.getScalePlanId();
                        Limit limit = new Limit();
                        limit.setMetricId(String.valueOf(papr.getMetricId()));
                        limit.setPlanId(String.valueOf(scalePlanId));
                        limit.setId(String.valueOf(papr.getScalePolicyId()));
                        limitStud.limitDelete(instance.getHost(),instance.getAccessToken(), limit);
                    }
                    apiPolicyRepository.deleteById(policy.getId());
                }
            }
        }

        publishApiPolicyRelationshipRepository.deleteByApiId(id);
        publishApiInstanceRelationshipRepository.deleteByApiId(id);
        publishApiRepository.updateStatus(id, API_DELETE);

        returnResult.setCode(OK);
        returnResult.setMsg("删除成功");
        returnResult.setData(true);
        //删除操作记录
        String msg = OK.equals(returnResult.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+returnResult.getMsg();
        PublishApi api = publishApiRepository.findApiByStatus(id);
        String name = OperationType.fromCode(3).getName()+api.getName();
        OperationApi operationApi = new OperationApi(name,CommonBeanUtil.getLoginUserName(),new Date(),new Date(),3,msg,api);
        operationApiRepository.save(operationApi);

        return returnResult;
    }

    @Override
    public Result<Boolean> offlinePublishApi(Integer apiId) {
        Result<Boolean> returnResult = new Result<>();
        PublishApi publishApi = publishApiRepository.findOne(apiId);

        if (publishApi.getStatus().equals(API_FIRST_PROMOTE) || publishApi.getStatus().equals(API_FOLLOWUP_PROMOTE)) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("API发布正在审批中");
            returnResult.setData(false);
            return returnResult;
        }

        // 查询Instance
        Instance instance = instanceRepository.searchInstanceByPartitionEnvironment(
                InstancePartition.fromCode(publishApi.getPartition()).getName(),
                InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName());
        if (instance == null) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("下线失败，未查询到instance信息");
            returnResult.setData(false);
            return returnResult;
        }

        log.info("{}api{},instance{}", TAG, apiId, instance.getId());

        //API订阅审批中不能下线
        List<PublishApplication> publishApplications = publishApplicationRepository.findByApiIdAndStatusAndType(apiId,Arrays.asList(1),1);
        if(publishApplications !=null && publishApplications.size()>0){
            returnResult.setCode(FAIL);
            returnResult.setMsg(String.format("【%s】正在订阅审批中，不允许下线",publishApi.getName()));
            returnResult.setData(false);
            return returnResult;
        }

        // update application plan
        String accessToken = instance.getAccessToken();
        String host = instance.getHost();
        PublishApiInstanceRelationship pair = pairRepository.getByAPIidAndInstanceId(apiId, instance.getId());
        if (pair == null) {
            log.error("{}Error Has no instance found", TAG);
            returnResult.setCode(FAIL);
            returnResult.setMsg("下线失败, 实例上无此API");
            returnResult.setData(false);
            return returnResult;
        }
        String serviceId = String.valueOf(pair.getScaleApiId());
        List<PublishApiPlan> publishApiPlans = publishApiPlanRepository.findAllByApiId(publishApi.getId());
        for (PublishApiPlan publishApiPlan : publishApiPlans) {
            applicationPlanStud.updateApplicationPlan(host, accessToken, serviceId,
                    publishApiPlan.getScalePlanId().toString(), 0);
            publishApiPlan.setStatus(0);
            publishApiPlan.setUpdateTime(new Date());
            publishApiPlanRepository.saveAndFlush(publishApiPlan);
        }

        // 下线后,回到未发布列表
        publishApi.setStatus(API_INIT);
        publishApi.setIsOnline(0);
        publishApiRepository.saveAndFlush(publishApi);
        returnResult.setCode(OK);
        returnResult.setMsg("下线成功");
        returnResult.setData(true);
        // 查询订阅流程
        List<PublishApplication> publishApplicationList = publishApplicationRepository.findByApiIdAndStatusAndType(apiId,Arrays.asList(1,2),1);
        if(publishApplicationList !=null && publishApplicationList.size()>0 && returnResult.getData()){
            //订阅中，已订阅的下线 ---- 发送邮件
            mailService.ApiOfflineSendMail(apiId);
        }
        //下线操作记录
        String msg = OK.equals(returnResult.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+returnResult.getMsg();
        String name = OperationType.fromCode(4).getName()+publishApi.getName();
        OperationApi operationApi = new OperationApi(name,CommonBeanUtil.getLoginUserName(),new Date(),new Date(),4,msg,publishApi);
        operationApiRepository.save(operationApi);

        return returnResult;
    }

    @Override
    public Result<Boolean> onlinePublishApi(Integer id) {
        Result<Boolean> returnResult = new Result<>();
        PublishApi publishApi = publishApiRepository.findOne(id);
        // 0，1，2，5 不允许上线, 3,4,6允许上线
        if (publishApi.getStatus().equals(API_DELETE) ||
                publishApi.getStatus().equals(API_INIT) ||
                publishApi.getStatus().equals(API_FIRST_PROMOTE) ||
                publishApi.getStatus() == 5) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("API在初次发布流程中或者未发布状态中不允许上线");
            returnResult.setData(false);
            return returnResult;
        }

        // 查询Instance
        List<Instance> instances =
                instanceRepository.searchInstanceByPartition(InstancePartition.fromCode(publishApi.getPartition()).getName());
        if (instances == null || instances.size() == 0) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("上线失败，未查询到instance信息");
            returnResult.setData(false);
            return returnResult;
        }

        // update application plan
        for (Instance instance : instances) {
            String accessToken = instance.getAccessToken();
            String host = instance.getHost();
            PublishApiInstanceRelationship pair = pairRepository.getByAPIidAndInstanceId(id, instance.getId());
            String serviceId = String.valueOf(pair.getScaleApiId());
            List<PublishApiPlan> publishApiPlans = publishApiPlanRepository.findAllByApiId(publishApi.getId());
            for (PublishApiPlan publishApiPlan : publishApiPlans) {
                applicationPlanStud.updateApplicationPlan(host, accessToken, serviceId,
                        publishApiPlan.getScalePlanId().toString(), 1);
                publishApiPlan.setStatus(1);
                publishApiPlan.setUpdateTime(new Date());
                publishApiPlanRepository.saveAndFlush(publishApiPlan);
            }
        }

        publishApi.setIsOnline(1);
        publishApiRepository.saveAndFlush(publishApi);
        returnResult.setCode(OK);
        returnResult.setMsg("上线成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Transactional
    @Override
    public Result<Boolean> updatePublishApi(Integer id, PublishApi savedApi, String projectId,
                                            PublishApiDto publishApiDto,String environment) {
        Result<Boolean> returnResult = new Result<>();
        returnResult.setAlert(1);

        PublishApiUtil.doPreProcessForDto(publishApiDto, false);

        log.info("{}Update Api {}", TAG, publishApiDto);
        publishApiDto.setId(id);
        Result<ApiValidateStatus> validateStatusResult = validateApiFields(publishApiDto, false,environment);
        if (validateStatusResult.isFailure()) {
            log.error("{}validate fail {}", TAG, validateStatusResult.getMsg());
            returnResult.setError(validateStatusResult.getMsg());
            return returnResult;
        }

        int envCode = InstanceEnvironment.fromCode(environment).getCode();
        PublishApi p = publishApiRepository.getOne(id);
        //跨系统不允许有相同的后端地址+端口
        /*if(publishApiDto.getHost()!=null && publishApiDto.getAccessProtocol()!=null){
            List<PublishApi> publishApis = publishApiRepository.findApiByHost(publishApiDto.getHost(),publishApiDto.getAccessProtocol(),envCode);
            if(publishApis!=null && publishApis.size()>0){
                for(PublishApi api : publishApis){
                    PublishApiGroup apiGroup = publishApiGroupRepository.getOne(api.getGroup().getId());
                    String project = apiGroup.getProjectId();
                    if(!api.getId().equals(p.getId())&&!projectId.equals(project)){
                        returnResult.setMsg("后端服务地址在其他系统中已存在！");
                        return returnResult;
                    }
                }
            }
        }*/


        ApiValidateStatus validateStatus = validateStatusResult.getData();
        List<Instance> instances = validateStatus.getInstances();

        PublishApi publishApiRes = id != null ? publishApiRepository.findOne(id) : savedApi;
        if (publishApiRes.getStatus().equals(API_FIRST_PROMOTE) ||
                publishApiRes.getStatus().equals(ModelConstant.API_FOLLOWUP_PROMOTE)) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("发布流程中不允许修改");
            returnResult.setData(false);
            return returnResult;
        }
        //API订阅审批中不能下线
        List<PublishApplication> publishApplications = publishApplicationRepository.findByApiIdAndStatusAndType(id,Arrays.asList(1),1);
        if(publishApplications !=null && publishApplications.size()>0){
            returnResult.setCode(FAIL);
            returnResult.setMsg("API正在订阅审批中，不允许修改");
            return returnResult;
        }

        //API修改发布审批中，不允许修改(临时表中有未处理数据)
        PublishApiTemporaryData tempDataByApiId = publishApiTemporaryDataRepository.findTempDataByApiId(id);
        if(null != tempDataByApiId && null != tempDataByApiId.getId()){
            returnResult.setCode(FAIL);
            returnResult.setMsg("API正在修改审批中，不允许重复修改");
            return returnResult;
        }
        //如果无需审批（测试环境和未发布的），就直接修改；否则先放入临时数据表
        if( InstanceEnvironment.fromCode(environment).getCode() == InstanceEnvironment.ENVIRONMENT_STAGING.getCode()
                || publishApiRes.getStatus().equals(API_INIT)){
            Result<Boolean> result = updatePublicApiData(instances,id,publishApiDto);
            if(FAIL.equals(result.getCode())){
                return  result;
            }
        }else{
            PublishApiTemporaryData tempData = new PublishApiTemporaryData();
            tempData.setCreator(CommonBeanUtil.getLoginUserName());
            tempData.setStatus(1);
            tempData.setUpdator(publishApiDto.getCreator());
            tempData.setUpdateTime(new Date());
            tempData.setCreateTime(new Date());
            tempData.setPublishApi(publishApiRes);
            tempData.setTempData(JSONObject.toJSONString(publishApiDto));
            publishApiTemporaryDataRepository.saveAndFlush(tempData);
        }
        returnResult.setCode(OK);
        returnResult.setMsg("更新成功");
        returnResult.setData(true);
        //修改操作记录
        String msg = OK.equals(returnResult.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+returnResult.getMsg();
        PublishApi api = publishApiRepository.getOne(id);
        String name = OperationType.fromCode(2).getName()+api.getName();
        OperationApi operationApi = new OperationApi(name,CommonBeanUtil.getLoginUserName(),new Date(),new Date(),2,msg,api);
        operationApiRepository.save(operationApi);


        return returnResult;
    }

    /**
     * 修改api数据
     * @param instances
     * @param ApiId
     * @param publishApiDto
     * @return
     */
    public Result<Boolean> updatePublicApiData(List<Instance> instances,Integer ApiId,PublishApiDto publishApiDto){
        Result<Boolean> returnResult = new Result<>(OK,"修改成功",null);
        PublishApi publishApiRes = publishApiRepository.findOne(ApiId);
        String user = publishApiRes.getCreator();
        for (Instance instance : instances) {
            String host = instance.getHost();
            String accessToken = instance.getAccessToken();
            PublishApiInstanceRelationship pair =
                    pairRepository.getByAPIidAndInstanceId(publishApiRes.getId(), instance.getId());

            if (pair==null || pair.getScaleApiId() == null) {
                log.error("{}Not found 3scale service id for api[{}] {}", publishApiRes.getId(),
                        publishApiRes.getSimpleName(), TAG);
                continue;
            }

            String serviceId = String.valueOf(pair.getScaleApiId());

            // 删除已有的MappingRule
            List<com.hisense.gateway.library.stud.model.MappingRuleDto> mappingRuleDtos =
                    mappingRuleStud.searchMappingRulesList(host, accessToken, serviceId);
            if (MiscUtil.isNotEmpty(mappingRuleDtos) && mappingRuleDtos.get(0).getMapping_rule() != null) {
                String metricId = mappingRuleDtos.get(0).getMapping_rule().getMetricId();
                for (com.hisense.gateway.library.stud.model.MappingRuleDto mappingRuleDto : mappingRuleDtos) {
                    mappingRuleStud.deleteMappingRules(host, accessToken, serviceId,
                            mappingRuleDto.getMapping_rule().getId());
                }

                // 新建MappingRule
                for (ApiMappingRuleDto apiMappingRuleDto : publishApiDto.getApiMappingRuleDtos()) {
                    String pattern = apiMappingRuleDto.getPattern();
                    String httpMethods = apiMappingRuleDto.getHttpMethod();

                    mappingRuleStud.createMapingRule(host, accessToken, serviceId, httpMethods, pattern, metricId);
                }
            }

            // String apiBackend = combinationApiBackend(publishApiDto);
            String apiBackend = GlobalSettings.wrapperHostPort(publishApiDto);
            Proxy proxy = publishApiDto.getProxy();
            String rewriteNme = publishApiDto.getRealmName();
            proxy.setHostnameRewrite(rewriteNme);

            String endpoint = instance.getProduction();//production
            String sandboxEndpoint = instance.getSandbox();//staging
            String secretToken = publishApiDto.getSecretToken();
            log.info("sandboxEndpoint={},endpoint={}", sandboxEndpoint, endpoint);//liyouzhi.ex-2021-01-18
            proxyPoliciesStud.editProxy(host, accessToken, serviceId, apiBackend, proxy,endpoint,sandboxEndpoint,secretToken);

            //考虑入参可能为空的情况
            String description = StringUtils.isBlank(publishApiRes.getDescription())?"":publishApiRes.getDescription();
            String name = StringUtils.isBlank(publishApiRes.getName())?"":publishApiRes.getName();
            if (!description.equals(publishApiDto.getDescription()) ||
                    !name.equals(publishApiDto.getName())) {
                //变更service
                serviceStud.updateServiceDesc(host, accessToken, serviceId, publishApiDto.getDescription(), publishApiDto.getName());
            }

            /*if (!urls.contains(publishApiRes.getUrl())) {// 前缀修改需要修改policy
                proxyPoliciesStud.changeUrlRewritingUrl(host, accessToken, serviceId, url);
            }*/

            proxyPoliciesStud.changeUrlRewritingUrl(host, accessToken, serviceId,
                    publishApiRes.getSourceType() == API_SRC_EUREKA_AUTO ?
                            String.format("/%s", publishApiDto.getSystemName()) :
                            publishApiDto.getUrl(),publishApiDto.getAccessProType());
            /*if (publishApiRes.getEnvironment() == 0) {

            }*/
            if (publishApiDto.isNeedLogging() != publishApiRes.isNeedLogging()) {
                proxyPoliciesStud.configHisenseLog(host, accessToken, serviceId, publishApiDto.isNeedLogging(),
                        publishApiDto.isNeedRecordRet(), publishApiRes.getId());
            }else{
                if(publishApiDto.isNeedLogging() && publishApiDto.isNeedRecordRet() != publishApiRes.isNeedRecordRet()){
                    proxyPoliciesStud.configHisenseLog(host, accessToken, serviceId, publishApiDto.isNeedLogging(),
                            publishApiDto.isNeedRecordRet(), publishApiRes.getId());
                }
            }
            // upstream 连接超时
            if (publishApiDto.getTimeout() != null && publishApiDto.getTimeout() > 0) {
                apiCastService.configConnectionTimeout(new ApiCastParam(host, accessToken, serviceId),
                        publishApiDto.getTimeout());
            }

            // ip黑白名单
            if (MiscUtil.isNotEmpty(publishApiDto.getIpWhiteList())) {
                List<String> ipWhiteList = PublishApiUtil.getAddressList(publishApiDto.getIpWhiteList());
                apiCastService.configIpBlackWhiteList(new ApiCastParam(host, accessToken, serviceId),
                        ipWhiteList, false);
            }

            if (MiscUtil.isNotEmpty(publishApiDto.getIpBlackList())) {
                List<String> ipBlackList = PublishApiUtil.getAddressList(publishApiDto.getIpBlackList());
                apiCastService.configIpBlackWhiteList(new ApiCastParam(host, accessToken, serviceId),
                        ipBlackList, true);
            }

            //Header Modification policy
            if(!CollectionUtils.isEmpty(publishApiDto.getRequestHeader()) || !CollectionUtils.isEmpty(publishApiDto.getResponseHeader())){
                apiCastService.configHeaderpolicy(new ApiCastParam(host, accessToken, serviceId),publishApiDto.getRequestHeader(),publishApiDto.getResponseHeader());
            }

            //策略修改
            if (publishApiRes.getAuthType().equals(publishApiDto.getAuthType())) {//策略没变
                continue;
            }

            if ("noauth".equals(publishApiDto.getAuthType())) {//从auth-->noauth
                //application查询
                ApplicationForm applicationForm = new ApplicationForm();
                applicationForm.setAccessToken(accessToken);
                //查询accountId，如果不存在则创建
                UserInstanceRelationship uir =
                        userInstanceRelationshipRepository.findByUserAndInstanceId(user,
                                instance.getId());
                if (null == uir) {//存在漏创建的情况
                    uir = createAccount(host, accessToken, instance.getId(), user, uir);
                }
                if (uir.getAccountId() == null) {
                    uir = updateAccount(host, accessToken, user, uir);
                }

                /*String appId = applicationXmlDtos.getApplication().get(0).getApplicationId();
                String appkey = applicationXmlDtos.getApplication().get(0).getKeys().getKey().get(0);
                PolicieConfigDto policieConfigDto = proxyPoliciesStud.updateAnonymousProxyPolicies(host, accessToken,
                        serviceId, appId, appkey);*/

                PublishApplication byApiIdAndSystemId = publishApplicationRepository.findByApiAndSystemId(ApiId, publishApiRes.getSystemId());

                log.info("instance:{},uir{},3scaleId{}",instance.getAccessToken(),uir.getAccountId(),byApiIdAndSystemId.getScaleApplicationId());
                ApplicationXml applicationXml = applicationStud.applicationRead(instance.getHost(), instance.getAccessToken(),
                        String.valueOf(uir.getAccountId()), byApiIdAndSystemId.getScaleApplicationId());

                PolicieConfigDto policieConfigDto1 = proxyPoliciesStud.proxyPoliciesChainShow(host, accessToken,
                        serviceId);
                List<PolicieConfig> policiesConfigs = new ArrayList<>();
                if ("default_credentials".equals(policieConfigDto1.getPolicies_config().get(0).getName())) {
                    for (PolicieConfig policieConfig : policieConfigDto1.getPolicies_config()) {
                        if ("default_credentials".equals(policieConfig.getName())) {
                            policieConfig.setEnabled(true);
                        }
                        policiesConfigs.add(policieConfig);
                    }
                    //将删除的策略修改恢复
                    proxyPoliciesStud.offAnonymousProxyPolicies(host, accessToken, serviceId, policiesConfigs);

                } else {
                    PolicieConfigDto policieConfigDto = null;
                    if (!GlobalSettings.isDefaultCredentialsWithUserKey()) {
                        String appId = applicationXml.getApplicationId();
                        String appKey = applicationXml.getKeys().getKey().get(0);

                        log.info("{}appId={}", TAG, appId);
                        log.info("{}appKey={}", TAG, appKey);

                        policieConfigDto = proxyPoliciesStud.updateAnonymousProxyPolicies(
                                host, accessToken, serviceId, appId, appKey, null);
                    } else {
                        String userKey = byApiIdAndSystemId.getUserKey();
                        policieConfigDto = proxyPoliciesStud.updateAnonymousProxyPolicies(
                                host, accessToken, serviceId, null, null, userKey);
                    }

                    //Policies 落库
                    Map<String, Object> configuration = new HashMap<>();
                    Boolean enabled = true;
                    String type = "";
                    for (PolicieConfig policieConfig : policieConfigDto.getPolicies_config()) {
                        if ("default_credentials".equals(policieConfig.getName())) {
                            configuration = policieConfig.getConfiguration();
                            enabled = policieConfig.getEnabled();
                            type = policieConfig.getName();
                            break;
                        }
                    }
                    ApiPolicy apiPolicy = new ApiPolicy(type, "description", JSONArray.toJSONString(configuration),
                            enabled, type, publishApiRes.getGroup().getProjectId(), user, new Date(), new Date());
                    ApiPolicy apiPolicyRes = apiPolicyRepository.save(apiPolicy);

                    PublishApiPolicyRelationship papr = paprRepository.findByApiIdAndInstanceIdAnscaleId(publishApiRes.getId(), instance.getId(), Long.valueOf(serviceId));
                    if(null == papr || null == papr.getId()){
                        papr = new PublishApiPolicyRelationship(publishApiRes.getId(),
                                apiPolicyRes.getId(),
                                instance.getId(), Long.valueOf(serviceId));
                    }else{
                        papr.setPublishPolicyId(apiPolicyRes.getId());
                    }
                    paprRepository.save(papr);
                }
            } else if ("auth".equals(publishApiDto.getAuthType())) {//noauth-auth
                PolicieConfigDto policieConfigDto = proxyPoliciesStud.proxyPoliciesChainShow(host, accessToken,
                        serviceId);
                List<PolicieConfig> policiesConfigs = new ArrayList<>();
                for (PolicieConfig policieConfig : policieConfigDto.getPolicies_config()) {
                    if ("default_credentials".equals(policieConfig.getName())) {
                        policieConfig.setEnabled(false);
                    }
                    policiesConfigs.add(policieConfig);
                }

                proxyPoliciesStud.offAnonymousProxyPolicies(host, accessToken, serviceId, policiesConfigs);
                PublishApiPolicyRelationship papr = paprRepository.findByApiIdAndInstanceId(publishApiRes.getId(),
                        instance.getId());
                if(null != papr){
                    paprRepository.deleteById(papr.getId());
                    apiPolicyRepository.deleteById(papr.getPublishPolicyId());
                }

            }
        }
        PublishApiUtil.updateApiWithDto(publishApiRes, publishApiDto, false,publishApiRes.getEnvironment());
        log.info("{}publishApi={}", TAG, publishApiRes);
        try {
            savePublishApi(publishApiRes, false);
            //更新文件
            List<Integer> fileDocIds = publishApiDto.getFileDocIds();
            if(!CollectionUtils.isEmpty(fileDocIds)){
                for(Integer fileId:fileDocIds){
                    ApiDocFile docFile = apiDocFileRepository.findOne(fileId);
                    if(null != docFile){
                        docFile.setApiId(publishApiRes.getId());
                        apiDocFileRepository.saveAndFlush(docFile);
                    }
                }
            }
        } catch (Exception e) {
            log.error("修改API数据异常",e);
            returnResult.setCode(FAIL);
            returnResult.setMsg("更新失败");
            returnResult.setData(false);
            return returnResult;
        }
        return returnResult;
    }

    /**
     * 发布API
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<Boolean> promotePublishApi(PromoteRequestInfo promoteRequestInfo,String environment) {
        Result<Boolean> rlt = new Result<>(Result.OK, "发布成功", true);
        if (promoteRequestInfo == null || promoteRequestInfo.getId() == null) {
            rlt.setMsg("参数错误未指定ApiId");
            return rlt;
        }

        PublishApi publishApi = publishApiRepository.findOne(promoteRequestInfo.getId());
        // guilai 2020/09/16,
        // 单个3scale实例默认,存在两个service: staging和production
        // 发布API,是指将api信息从单个3scale实例.staging sercie 提交到 其production service
        // 一键发生产时, clone api时设定为生产环境( 专用于生产环境的3scale实例)
        // 因此,环境字段在创建API时已设定,之后不再修改,此处需关闭此设定
        //publishApi.setEnvironment(environment);
        if (publishApi == null) {
            //throw new NotExist(String.format("api with id %s not exist", promoteRequestInfo.getId()));
            rlt.setMsg("指定的API不存在");
            return rlt;
        }

        if (publishApi.getGroup() == null) {
            rlt.setCode(FAIL);
            rlt.setMsg("当前API未设定分组,请设定分组及发布环境后,再执行发布");
            return rlt;
        }

        //当前api发布成功或正在审批中无需进入流程
        switch (publishApi.getStatus()) {
            case 0:// API_DELETE
                rlt.setError("API已经删除，不允许发布");
                return rlt;

            case 2:// API_FIRST_PROMOTE
                //api 第一发布流程中, 不允许订阅
                rlt.setMsg("API正在审批中，请勿重复发布");
                return rlt;

            case 3:// API_FOLLOWUP_PROMOTE
                // API发布流程中,允许订阅
                rlt.setMsg("API正在审批中，请勿重复发布");
                return rlt;

            case 4:// API_COMPLETE
                if(promoteRequestInfo.isCreate()){
                    rlt.setMsg("API已经发布成功，请勿重复发布");
                    return rlt;
                }
                break;
            default:
                break;
        }

        //审批中
        List<ProcessRecord> processRecords1 = processRecordRepository.findProcessRecordByRelId(publishApi.getId(),1);
        if(!CollectionUtils.isEmpty(processRecords1)){
            rlt.setMsg("API正在审批中，请勿重复发布");
            return rlt;
        }

        ProcessRecord pr = new ProcessRecord();
        String user = CommonBeanUtil.getLoginUserName();
        DataItem dataItem = dataItemRepository.findOne(publishApi.getGroup().getSystem());
        Integer systemId = Integer.parseInt(publishApi.getApiGroup().getProjectId());
        SystemInfo systemInfo = systemInfoRepository.findOne(systemId);
        if(systemInfo==null){
            log.error("systemInfo is not exit");
        }
        //点击发布  当前为生产环境 启动审批流程
        Map<String,Object> map =new HashMap<>();
        int status = 1;
        if( InstanceEnvironment.fromCode(environment).getCode()==1){
            map.put("publishEvn","production");
            if(systemInfo != null){
                if(systemInfo.getPrdApiAdminName()==null){
                    throw new NotExist("项目管理员为空，流程启动失败");
                }
                map.put("projectManager",systemInfo.getPrdApiAdminName());
            }
            //map.put("projectManager","xueyukun");
            map.put("link",link);
            map.put("im_link",imLink);
            WorkFlowDto flowDto = new WorkFlowDto();
            flowDto.setUserID(user);
            flowDto.setUserName(user);
            flowDto.setProcessDefName(BpmConstant.PUBLISH_API);
            flowDto.setProcessInstName("发布API");
            flowDto.setRelaDatas(map);
            Map map1 = new HashMap();
            String theme = String.format("%s-申请发布API-%s到%s",dataItem.getItemName(),publishApi.getName(),InstanceEnvironment.fromCode(environment).getName());
            map1.put("theme",theme);
            flowDto.setBizInfo(map1);
            Result<Object> result = workFlowService.startProcess(flowDto);
            if(Result.OK.equals(result.getCode()) && null != result.getData()){
                JSONObject resultObj = (JSONObject)result.getData();
                if(null != resultObj){
                    pr.setProcessInstID(String.valueOf(resultObj.get("processInstID")));
                    pr.setStatus(1);
                    processRecordRepository.saveAndFlush(pr);
                }
            }else {
                throw new NotExist("流程启动失败");
            }
            if(!API_COMPLETE.equals(publishApi.getStatus()) )publishApi.setStatus(API_FIRST_PROMOTE);
            result.setMsg("发布成功，已发起流程审批，请在【发布申请】菜单下查看审批详情");
        }else{
            //当前为测试环境无需审批
            status = 2;
            publishApi.setStatus(API_COMPLETE);
            publishApi.setIsOnline(1);
        }
        List<String> partitions = new ArrayList<>();
        String partition = PublishApiServiceImpl.getPartition(publishApi.getPartition());
        partitions.add(partition);
        // 发布到3scale // guilai 2020/09/16
        List<ApiInstance> apiInstances = pairRepository.getApiInstances(publishApi.getId(),
                InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName(),partitions);// guilai 2020/09/16
        ProxyConfigDto proxyConfigDto = null;
        for (ApiInstance apiInstance : apiInstances) {
            proxyConfigDto = serviceStud.latestPromote(apiInstance.getHost(),
                    apiInstance.getAccessToken(), apiInstance.getId(),
                    ModelConstant.ENV_SANDBOX);
            if( InstanceEnvironment.fromCode(environment).getCode()!=1){
                if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()
                        && null != proxyConfigDto.getProxyConfig().getVersion()) {
                    serviceStud.configPromote(apiInstance.getHost(),
                            apiInstance.getAccessToken(),
                            apiInstance.getId().toString(),
                            ModelConstant.ENV_SANDBOX, proxyConfigDto.getProxyConfig().getVersion(),
                            ModelConstant.ENV_PROD);
                } else {
                    log.error("proxyConfig not exist");
                    rlt.setMsg("3scale通信异常,请联系平台管理员");
                    return rlt;
                }
            }
        }

        publishApiRepository.saveAndFlush(publishApi);
        //处理记录
        pr.setType("publish_api");
        pr.setRelId(publishApi.getId());
        pr.setStatus(status);

        pr.setCreator(CommonBeanUtil.getLoginUserName());
        pr.setCreateTime(new Date());

        ProcessDataDto processDataDto = new ProcessDataDto();
        processDataDto.setGroupId(publishApi.getGroup().getId());
        processDataDto.setApiSystem(publishApi.getGroup().getSystem());
        processDataDto.setUrl(publishApi.getUrl());
        processDataDto.setIsCompatible(promoteRequestInfo.getIsCompatible());

        processRecordRepository.saveAndFlush(pr);

        //不兼容且当前为修改已发布的api，向订阅的用户发送邮件通知，异步方法
        if ("no".equals(promoteRequestInfo.getIsCompatible()) && !promoteRequestInfo.isCreate()){
            mailService.NotCompatibleSendMail(promoteRequestInfo);
        }
        //发布操作记录
        String msg = OK.equals(rlt.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+rlt.getMsg();
        String name = OperationType.fromCode(1).getName()+publishApi.getName();
        OperationApi operationApi = new OperationApi(name,user,new Date(),new Date(),1,msg,publishApi);
        operationApiRepository.save(operationApi);

        return rlt;
    }

    public static  String getEnv(String environment){
        String env  = environment;
        if(SystemNameConstant.TEST_ENV.equals(environment)){
            env = SystemNameConstant.SCANLE_TEST_ENV_NAME;
        }else if(SystemNameConstant.PROD_ENV.equals(environment)){
            env = SystemNameConstant.SCANLE_PROD_ENV_NAME;
        }
        return env;
    }

    @Override
    public Result<ProxyConfigDto> promotePublishApi(Integer apiId) {
        Result<Boolean> rlt = new Result<>();
        if (apiId == null) {
            throw new NotExist(String.format("api with id %s not exist", apiId));
        }

        PublishApi publishApi = publishApiRepository.findOne(apiId);
        if (publishApi == null) {
            throw new NotExist(String.format("api with id %s not exist", apiId));
        }
        List<String> partitions = new ArrayList<>();
        String partition = getPartition(publishApi.getPartition());
        partitions.add(partition);

        List<ApiInstance> apiInstances =
                publishApiInstanceRelationshipRepository.getApiInstances(publishApi.getId(),
                        InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName(),partitions);// guilai 2020/09/16
        ProxyConfigDto proxyConfigDto = null;
        for (ApiInstance apiInstance : apiInstances) {
            proxyConfigDto = serviceStud.latestPromote(apiInstance.getHost(),
                    apiInstance.getAccessToken(), apiInstance.getId(),
                    ModelConstant.ENV_SANDBOX);
            if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()
                    && null != proxyConfigDto.getProxyConfig().getVersion()) {
                serviceStud.configPromote(apiInstance.getHost(),
                        apiInstance.getAccessToken(),
                        apiInstance.getId().toString(),
                        ModelConstant.ENV_SANDBOX, proxyConfigDto.getProxyConfig().getVersion(),
                        ModelConstant.ENV_PROD);
            }
        }
        return null;
    }

    @Override
    public Result<List<PublishApiDto>> getConfigPromoteList(Integer apiId) {
        List<PublishApiDto> list = new ArrayList<>();
        Result<List<PublishApiDto>> rlt = new Result<>();
        if (apiId == null) {
            rlt.setCode(FAIL);
            rlt.setMsg(String.format("api with id %s not exist", apiId));
            rlt.setData(list);
            return rlt;
        }

        PublishApi publishApi = publishApiRepository.findOne(apiId);
        if (publishApi == null) {
            rlt.setCode(FAIL);
            rlt.setMsg(String.format("api with id %s not exist", apiId));
            rlt.setData(list);
            return rlt;
        }

        List<String> partitions = new ArrayList<>();
        String partition = getPartition(publishApi.getPartition());
        partitions.add(partition);
        List<ApiInstance> apiInstances =
                publishApiInstanceRelationshipRepository.getApiInstances(publishApi.getId(),
                        InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName(), partitions);// guilai 2020/09/16

        ProxyConfigDtos proxyConfigDtos = null;
        PublishApiDto publishApiDto = null;
        for (ApiInstance apiInstanceRel : apiInstances) {
            PublishApiInstanceRelationship apiInstanceRelationship =
                    publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(publishApi.getId(), Math.toIntExact(apiInstanceRel.getId()));
            if (ModelConstant.CLUSTER_PARTITION_INNER.equals(apiInstanceRel.getClusterPartition())) {
                proxyConfigDtos = serviceStud.listPromote(apiInstanceRel.getHost(), apiInstanceRel.getAccessToken(),
                        apiInstanceRel.getId(), ModelConstant.ENV_PROD);

                if (null != proxyConfigDtos && null != proxyConfigDtos.getProxyConfigs()
                        && !CollectionUtils.isEmpty(proxyConfigDtos.getProxyConfigs())) {

                    ProxyConfigDto lateproxyConfigDto = null;

                    lateproxyConfigDto = serviceStud.latestPromote(apiInstanceRel.getHost(),
                            apiInstanceRel.getAccessToken(), apiInstances.get(0).getId(),
                            ModelConstant.ENV_SANDBOX);

                    for (ProxyConfigDto proxyConfigDto : proxyConfigDtos.getProxyConfigs()) {
                        publishApiDto = new PublishApiDto();
                        publishApiDto.setVersion(proxyConfigDto.getProxyConfig().getVersion());
                        publishApiDto.setPartitions(Arrays.asList(ModelConstant.CLUSTER_PARTITION_INNER));
                        publishApiDto.setName(proxyConfigDto.getProxyConfig().getContent().getName());
                        publishApiDto.setDescription(proxyConfigDto.getProxyConfig().getContent().getDescription());
                        publishApiDto.setRealmName(proxyConfigDto.getProxyConfig().getContent().getProxy().getHostnameRewrite());
                        publishApiDto.setAuthType(ModelConstant.API_AUTH);
                        for (ProxyPolicy proxyPolicy :
                                proxyConfigDto.getProxyConfig().getContent().getProxy().getPolicyChain()) {
                            if (ModelConstant.API_ANONYMOUS_POLICY_NAME.equals(proxyPolicy.getName())) {
                                publishApiDto.setAuthType(ModelConstant.API_NOAUTH);
                            }
                        }

                        if (null != lateproxyConfigDto && null != lateproxyConfigDto.getProxyConfig()
                                && null != lateproxyConfigDto.getProxyConfig().getVersion()
                                && lateproxyConfigDto.getProxyConfig().getVersion().equals(publishApiDto.getVersion())) {
                            publishApiDto.setIsInUse(1);
                        }

                        publishApiDto.setApiMappingRuleDtos(parseMappingRule(proxyConfigDto.getProxyConfig().getContent().getProxy().getProxyRules(), publishApi.getUrl()));
                        publishApiDto.setProxy(proxyConfigDto.getProxyConfig().getContent().getProxy());
                        list.add(publishApiDto);
                    }
                }
            } else if (ModelConstant.CLUSTER_PARTITION_OUTER.equals(apiInstanceRel.getClusterPartition())) {
                proxyConfigDtos = serviceStud.listPromote(apiInstanceRel.getHost(), apiInstanceRel.getAccessToken(),
                        apiInstanceRel.getId(), ModelConstant.ENV_PROD);

                if (null != proxyConfigDtos && null != proxyConfigDtos.getProxyConfigs()
                        && !CollectionUtils.isEmpty(proxyConfigDtos.getProxyConfigs())) {
                    ProxyConfigDto lateproxyConfigDto = null;
                    lateproxyConfigDto = serviceStud.latestPromote(apiInstanceRel.getHost(),
                            apiInstanceRel.getAccessToken(), apiInstances.get(0).getId(),
                            ModelConstant.ENV_SANDBOX);

                    for (ProxyConfigDto proxyConfigDto : proxyConfigDtos.getProxyConfigs()) {
                        publishApiDto = new PublishApiDto();
                        publishApiDto.setVersion(proxyConfigDto.getProxyConfig().getVersion());
                        publishApiDto.setPartitions(Arrays.asList(CLUSTER_PARTITION_OUTER));
                        publishApiDto.setName(proxyConfigDto.getProxyConfig().getContent().getName());
                        publishApiDto.setDescription(proxyConfigDto.getProxyConfig().getContent().getDescription());
                        publishApiDto.setRealmName(proxyConfigDto.getProxyConfig().getContent().getProxy().getHostnameRewrite());
                        publishApiDto.setAuthType(ModelConstant.API_AUTH);
                        for (ProxyPolicy proxyPolicy :
                                proxyConfigDto.getProxyConfig().getContent().getProxy().getPolicyChain()) {
                            if (ModelConstant.API_ANONYMOUS_POLICY_NAME.equals(proxyPolicy.getName())) {
                                publishApiDto.setAuthType(ModelConstant.API_NOAUTH);
                            }
                        }

                        if (null != lateproxyConfigDto && null != lateproxyConfigDto.getProxyConfig()
                                && null != lateproxyConfigDto.getProxyConfig().getVersion()
                                && lateproxyConfigDto.getProxyConfig().getVersion().equals(publishApiDto.getVersion())) {
                            publishApiDto.setIsInUse(1);
                        }

                        publishApiDto.setApiMappingRuleDtos(parseMappingRule(proxyConfigDto.getProxyConfig().getContent().getProxy().getProxyRules(), publishApi.getUrl()));
                        publishApiDto.setProxy(proxyConfigDto.getProxyConfig().getContent().getProxy());
                        list.add(publishApiDto);
                    }
                }
            }
        }
        rlt.setCode(OK);
        rlt.setMsg("查询正常");
        rlt.setData(list);
        return rlt;
    }

    @Override
    public Result<Map<String, String>> findAppIdAndKey(Integer apiId) {
        Result<Map<String, String>> returnResult = new Result<>();
        Map<String, String> returnMap = new HashMap<>(2);
        //查询是否匿名访问
        PublishApi publishApi = publishApiRepository.findOne(apiId);
        if (ModelConstant.API_NOAUTH.equals(publishApi.getAuthType())) {
            // 不需要鉴权
            returnResult.setCode(FAIL);
            returnResult.setMsg("不需要鉴权");
            returnResult.setData(returnMap);
            return returnResult;
        }
        // 查询appid和appkey,由于创建的两个环境的appId和appkey相同，所以任取其一
        List<PublishApplication> publishApplications = publishApplicationRepository.findKeyByAppId(apiId);
        if (publishApplications == null || publishApplications.size() == 0) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("查询失败,没有订阅");
            returnResult.setData(returnMap);
            return returnResult;
        }

        // read application
        PublishApplication publishApplication = publishApplications.get(0);
        Instance instance = instanceRepository.findOne(publishApplication.getInstance().getId());
        UserInstanceRelationship uir =
                userInstanceRelationshipRepository.findByUserAndInstanceId(publishApplication.getCreator(), instance.getId());

        ApplicationXml applicationXml = applicationStud.applicationRead(instance.getHost(), instance.getAccessToken(),
                String.valueOf(uir.getAccountId()), publishApplication.getScaleApplicationId());

        if (StringUtils.isBlank(applicationXml.getApplicationId())) {
            returnMap.put("userKey", applicationXml.getUserKey());
            returnMap.put("backendVersion", "1");
        } else {
            returnMap.put("backendVersion", "2");
            returnMap.put("appId", applicationXml.getApplicationId());
            returnMap.put("appKey", applicationXml.getKeys().getKey().get(0));
        }

        returnResult.setCode(OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(returnMap);
        return returnResult;
    }

    @Override
    public Result<Boolean> checkApiName(String projectId, PublishApiDto publishApiDto) {
        Result<Boolean> returnResult = new Result<>();
        List<PublishApi> publishApis = publishApiRepository.findByNameAndProject(
                publishApiDto.getName().trim(),
                publishApiDto.getGroupId());
        if (publishApis != null && publishApis.size() > 0) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("创建失败，同组下名称重复");
            returnResult.setData(false);
            return returnResult;
        }

        returnResult.setCode(OK);
        returnResult.setMsg("名称可用");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<Boolean> checkUrl(String projectId, PublishApiDto publishApiDto) {
        Result<Boolean> returnResult = new Result<>();
        // 参数检验
        if (StringUtils.isBlank(publishApiDto.getUrl()) || "/".equals(publishApiDto.getUrl())) {
            returnResult.setCode(FAIL);
            returnResult.setMsg("API参数异常,url为空或异常'/'");
            returnResult.setData(false);
            return returnResult;
        }

        returnResult.setCode(OK);
        returnResult.setMsg("URL可用");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<List<String>> deletePublishApis(PublishApiBatch batch) {
        return batchOperation(batch, DELETE);
    }

    @Override
    public Result<List<String>> setGroupForPublishApis(PublishApiBatch batch) {
        return batchOperation(batch, SET_GROUP);
    }

    @Override
    public Result<List<String>> promotePublishApis(PublishApiBatch batch) {
        return batchOperation(batch, PROMOTE);
    }

    @Override
    public Result<List<String>> offlinePublishApis(PublishApiBatch batch) {
        return batchOperation(batch, OFF_LINE);
    }

    @Override
    public Result<Map<Integer, Boolean>> getSubscribeStatusForApis(PublishApiBatch batch) {
        Result<Map<Integer, Boolean>> result = new Result<>(FAIL, "Failure", null);
        if (batch == null || MiscUtil.isEmpty(batch.getIds())) {
            result.setMsg("未选择任何API");
            log.error("{}Please select api for query", TAG);
            return result;
        }

        Map<Integer, Boolean> resultMap = new HashMap<>();
        for (Integer apiId : batch.getIds()) {
            resultMap.put(apiId, getSubscribeStatusForApi(apiId));
        }

        result.setCode(OK);
        result.setMsg("success");
        result.setData(resultMap);
        return result;
    }

    @Override
    public Result<Page<SubscribeSystemInfo>> findApiSubscribeSystem(Integer apiId,
                                                                    SubscribeSystemQuery subscribeSystemQuery) {
        Result<Page<SubscribeSystemInfo>> result = new Result<>(FAIL,"查询失败！",null);
        if(null == apiId || apiId < 1){
            result.setError(FAIL,"请输入需要查询的apiId");
            return result;
        }

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if (StringUtils.isNotBlank(subscribeSystemQuery.getSort())) {
            direction = "d".equalsIgnoreCase(subscribeSystemQuery.getSort()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        }

        PageRequest pageable = PageRequest.of(
                0 != subscribeSystemQuery.getPage() ? subscribeSystemQuery.getPage() - 1 : 0, subscribeSystemQuery.getSize(),
                Sort.by(direction, property));
        List<SubscribeSystemInfo> subscribeInfos = new ArrayList<>();
        Page<SubscribeSystemInfo> subscribeds = new PageImpl<>(subscribeInfos,pageable,0);
        Specification<PublishApplication> applicationSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("publishApi").get("id").as(Integer.class), apiId));
            andList.add(builder.equal(root.get("status").as(Integer.class), 2));
            andList.add(builder.equal(root.get("type").as(Integer.class), 1));
            if (!CollectionUtils.isEmpty(subscribeSystemQuery.getSystem())) {
                CriteriaBuilder.In<Integer> in = builder.in(root.get("system"));
                for (Integer system: subscribeSystemQuery.getSystem()) {
                    in.value(system);
                }
                andList.add(in);
            }
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApplication> apps = publishApplicationRepository.findAll(applicationSpec);
        if (!CollectionUtils.isEmpty(apps)) {
            Specification<ProcessRecord> recordSpec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                andList.add(builder.equal(root.get("type").as(String.class), "application"));
                andList.add(builder.equal(root.get("status").as(Integer.class), 2));
                if (!CollectionUtils.isEmpty(apps)) {
                    CriteriaBuilder.In<Integer> in = builder.in(root.get("relId"));
                    for (PublishApplication pa: apps) {
                        in.value(pa.getId());
                    }
                    andList.add(in);
                }
                if (!CollectionUtils.isEmpty(subscribeSystemQuery.getUser())) {
                    CriteriaBuilder.In<String> in = builder.in(root.get("creator"));
                    for (String creator: subscribeSystemQuery.getUser()) {
                        in.value(creator);
                    }
                    andList.add(in);
                }
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            Page<ProcessRecord> subscribedRecords = processRecordRepository.findAll(recordSpec, pageable);
            List<ProcessRecord> contents = subscribedRecords.getContent();

            if(!CollectionUtils.isEmpty(contents)){
                for(ProcessRecord record:contents){
                    SubscribeSystemInfo subscribeSystemInfo = new SubscribeSystemInfo();
                    PublishApplication application = publishApplicationRepository.findOne(record.getRelId());
                    DataItem dataItem = dataItemRepository.findOne(application.getSystem());
                    subscribeSystemInfo.setSystem(dataItem.getItemName());
                    subscribeSystemInfo.setCreateTime(record.getCreateTime());
                    subscribeSystemInfo.setUser(record.getCreator());
                    subscribeInfos.add(subscribeSystemInfo);
                }
            }
            subscribeds = new PageImpl<>(subscribeInfos,pageable,subscribedRecords.getTotalElements());
        }
        result.setCode(OK);
        result.setMsg("查询成功！");
        result.setData(subscribeds);
        return result;
    }

    @Override
    public Result<Integer> uploadApiDoc(String type,MultipartFile uploadFile, HttpSession session) {
        Result<Integer> result = new Result<>(FAIL, "错误", 0);

        if (uploadFile == null || uploadFile.isEmpty()) {
            result.setMsg("错误, 未选择任何文件, 请重新选择");
            return result;
        }

        long size;
        if ((size = uploadFile.getSize()) <= 0) {
            result.setMsg("错误, 文件内容为空, 请重新选择");
            return result;
        }

        if (size > 200*1024*1024) {
            result.setMsg("错误, 上传文件过大超过200M, 请重新选择");
            return result;
        }

        String orgName = uploadFile.getOriginalFilename();
        String orgFirstName = orgName.lastIndexOf(".")==-1?orgName:orgName.substring(orgName.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + orgFirstName;
        String path = FILE_PATH + fileName;
        InputStream in = null;//定义一个输出流
        OutputStream out = null;//定义一个输入流
        try {
            File file = new File(path);
            if(!file.getParentFile().exists()) file.getParentFile().mkdirs();
            //输出流，读出数据
            in = uploadFile.getInputStream();
            //输入流，写入数据
            out = new FileOutputStream(file);
            byte[] b = new byte[1024];
            int len;
            //把读出的数据赋予len
            while ((len = in.read(b)) > 0) {
                //输出数据
                out.write(b, 0, len);
            }
        } catch (Exception e) {
            log.error("Exception",e);
            result.setMsg("错误, 文件存储异常, 请重试");
            return result;
        } finally {
            try {
                //关闭流
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            }catch (IOException e){
                log.error("in or out close exception",e);
            }

        }

        ApiDocFile docFile = new ApiDocFile();
        docFile.setCreateTime(new Date());
        docFile.setPath(path);
        docFile.setStatus(1);
        docFile.setFileType(type);
        docFile.setFileName(orgName);
        docFile.setCreator(GlobalSettings.getVisitor().getUsername());
        docFile.setSize(size);

        try {
            ApiDocFile apiDocFile = apiDocFileRepository.save(docFile);
            result.setData(apiDocFile.getId());
            result.setMsg("文件上传成功");
            result.setCode(OK);
        } catch (Exception e) {
            result.setMsg("文件存储失败");
        }
        return result;
    }

    @Transactional
    @Override
    public Result<List<String>> deleteApiDocs(PublishApiBatch apiBatch) {
        return batchOperation(apiBatch, DELETE_FILE);
    }

    @Override
    public Result<Boolean> updateMappingRules(PublishApi savedApi) {
        Result<Boolean> result = new Result<>(FAIL, "", false);
        List<Instance> instances =
                instanceRepository.searchInstanceByPartition(InstancePartition.fromCode(savedApi.getPartition()).getName());
        if (MiscUtil.isEmpty(instances)) {
            result.setMsg("未查询到scale实例");
            return result;
        }

        for (Instance instance : instances) {
            String host = instance.getHost();
            String accessToken = instance.getAccessToken();
            PublishApiInstanceRelationship pair =
                    pairRepository.getByAPIidAndInstanceId(savedApi.getId(), instance.getId());
            String serviceId = String.valueOf(pair.getScaleApiId());

            // 删除已有的MappingRule
            List<com.hisense.gateway.library.stud.model.MappingRuleDto> mappingRuleDtos =
                    mappingRuleStud.searchMappingRulesList(host, accessToken, serviceId);
            String metricId = mappingRuleDtos.get(0).getMapping_rule().getMetricId();
            for (com.hisense.gateway.library.stud.model.MappingRuleDto mappingRuleDto : mappingRuleDtos) {
                mappingRuleStud.deleteMappingRules(host, accessToken, serviceId,
                        mappingRuleDto.getMapping_rule().getId());
            }

            // 新建MappingRule
            for (ApiMappingRule apiMappingRule : savedApi.getApiMappingRules()) {
                String pattern = apiMappingRule.getPattern();
                String httpMethods = apiMappingRule.getHttpMethod();

                mappingRuleStud.createMapingRule(host, accessToken, serviceId, httpMethods, pattern, metricId);
            }
        }

        result.setCode(OK);
        return result;
    }

    /**
     * 组合API后端
     */
    private String combinationApiBackend(PublishApiDto publishApiDto) {
        String apiBackend = "";
        String suffix = "";//"".svc.cluster.local";
        if ("2".equals(publishApiDto.getTargetType())) {
            if (publishApiDto.getHost().contains("http://")) {
                apiBackend = publishApiDto.getHost() + suffix + ":" + publishApiDto.getPort();
            } else {
                apiBackend = "http://" + publishApiDto.getHost() + suffix + ":" + publishApiDto.getPort();
            }
        } else {
            if (publishApiDto.getHost().contains("https://")) {
                apiBackend = publishApiDto.getHost() + suffix + ":" + publishApiDto.getPort();
            } else {
                apiBackend = "https://" + publishApiDto.getHost() + suffix + ":" + publishApiDto.getPort();
            }
        }
        return apiBackend;
    }

    @Transactional(rollbackFor = Exception.class)
    private Result<Integer> createPublishApi(PublishApiDto publishApiDto, ApiValidateStatus validateStatus, String environment, String user) throws Exception {
        Result<Integer> returnResult = new Result<>(FAIL, "创建失败", null);
        String errorMsg = "";
        // API入库
        log.info("{}Start to saving {}", TAG, publishApiDto);

        Result<PublishApi> result;
        PublishApi publishApi = PublishApiUtil.buildApiWithDto(publishApiDto,
                InstanceEnvironment.fromCode(environment).getCode());// guilai 2020/09/16
        if ((result = savePublishApi(publishApi, true)).isFailure()) {
            log.error("{}{}", TAG, result.getMsg());
            errorMsg  = "创建失败，API 写入异常";
            returnResult.setMsg("创建失败，API 写入异常");
            throw new Exception(errorMsg);
//            return returnResult;
        }

        //更新文件
        List<Integer> fileDocIds = publishApiDto.getFileDocIds();
        if(!CollectionUtils.isEmpty(fileDocIds)){
            for(Integer fileId:fileDocIds){
                ApiDocFile docFile = apiDocFileRepository.findOne(fileId);
                if(null != docFile){
                    docFile.setApiId(publishApi.getId());
                    apiDocFileRepository.saveAndFlush(docFile);
                }
            }
        }

        String apiName = validateStatus.getApiName();
        PublishApiGroup publishApiGroup = validateStatus.getGroup();
        List<Instance> instances = validateStatus.getInstances();
        PublishApi publishApiRes = result.getData();

        // 查询systemId
        Integer systemId;
        /*if (publishApiDto.getSourceType().equals(API_SRC_EUREKA_AUTO)) {
            DataItem item = dataItemRepository.findSystemByItemKey(publishApiDto.getSystemName());
            // TODO 自动创建的API,其system未查询到时,应该显示在eureka全局列表中,而非未发布列表中,guilai.ming
            if (item == null) {
                log.info("{}No system found for this api {}", TAG, publishApiDto.getName());
                returnResult.setCode(OK);
                returnResult.setData(publishApiRes.getId());//不影响后续批量创建
                return returnResult;
            }
            systemId = item.getId();
        } else {
            if (publishApiGroup == null) {
                publishApiRepository.updateStatus(publishApiRes.getId(), API_DELETE);
                log.error("{}Created by user but with empty group in database, rollback", TAG);
                returnResult.setCode(OK);
                returnResult.setData(publishApiRes.getId());//不影响后续批量创建
                return returnResult;
            } else {
                systemId = publishApiGroup.getSystem();
            }
        }*/

        if (publishApiGroup == null) {
            publishApiRepository.updateStatus(publishApiRes.getId(), API_DELETE);
            errorMsg = String.format("%sCreated by user but with empty group in database, rollback", TAG);
            log.error(errorMsg);
            returnResult.setCode(OK);
            returnResult.setData(publishApiRes.getId());//不影响后续批量创建
            return returnResult;
        } else {
            systemId = publishApiGroup.getSystem();
        }

        log.info("{}Save done {}", TAG, result.getMsg());

        Integer publishApiId = publishApiRes.getId();
        String tenantId = validateStatus.getTenantId();
        String projectId = validateStatus.getProjectId();
        List<Map<String, String>> serviceInfos = new ArrayList<>();

        // 各个环境下创建3scale的信息
        try {
            String appId = "";
            String appKey = "";
            ApiPolicy apiPolicyRes = new ApiPolicy();
            boolean mark = true;

            // should avoid RequestContextHolder exception in async task,guilai.ming
            // Integer userId = publishApiDto.getCreateId() != null ? publishApiDto.getCreateId() : SecurityUtils.getLoginUser().getUserId();

            for (Instance instance : instances) {
                String host = instance.getHost();
                String accessToken = instance.getAccessToken();
                String description = publishApiDto.getDescription();
                DataItem dataItem = dataItemRepository.findOne(systemId);
                String groupSystemKey = "";
                if (dataItem != null) {
                    groupSystemKey = dataItem.getItemKey();
                }

                String nameSuffix = String.format("%s_%s_%s_%s", tenantId, groupSystemKey, projectId,
                        instance.getClusterPartition());
                String systemName = ScaleUtils.systemName(tenantId, nameSuffix);

                // TODO service create 调试暂时注释掉
                ServiceXmlDto serviceXmlDto = serviceStud.createXmlService(host, accessToken, apiName, description,
                        systemName);
                log.info("createXmlService serviceXmlDto:"+ JSON.toJSONString(serviceXmlDto));
                String serviceId = serviceXmlDto.getId();
                Map<String, String> serviceMap = new HashMap<>(3);
                serviceMap.put("host", host);
                serviceMap.put("accessToken", accessToken);
                serviceMap.put("serviceId", serviceId);
                serviceInfos.add(serviceMap);

                PublishApiInstanceRelationship publishApiInstanceRelationship =
                        new PublishApiInstanceRelationship(publishApiId, instance.getId(), Long.valueOf(serviceId));
                pairRepository.save(publishApiInstanceRelationship);

                // 组合apiBackend
                //String apiBackend = combinationApiBackend(publishApiDto);
                String apiBackend = GlobalSettings.wrapperHostPort(publishApiDto);
                log.info("{}apiBackend={}", TAG, apiBackend);

                String endpoint = instance.getProduction();//production
                String sandboxEndpoint = instance.getSandbox();//staging

                log.info("sandboxEndpoint={},endpoint={}", sandboxEndpoint, endpoint);//liyouzhi.ex-2020-10-15
                String rewriteNme = publishApiDto.getRealmName();
                publishApiDto.getProxy().setHostnameRewrite(rewriteNme);
                log.info("{}Start to proxyUpdate", TAG);
                Result<Object> serviceResult = serviceStud.proxyUpdate(host, accessToken, serviceId, endpoint, apiBackend, sandboxEndpoint,
                        publishApiDto.getProxy(), publishApiDto.getSecretToken());
                if(FAIL.equals(serviceResult.getCode())){
                    errorMsg = String.format("3scale初始化服务基本信息异常，请检查后端服务地址和路由规则！错误详情：%S",serviceResult.getMsg());
                    log.error(errorMsg);
                    returnResult.setError("3scale初始化服务基本信息异常，请检查并修改后端服务地址和路由规则！修改后重新提交发布！");
                    //删除服务
                    serviceStud.serviceDelete(host, accessToken,serviceId);
                    throw new Exception(errorMsg);
//                    return returnResult;
                }

                log.info("{}Start to create ApplicationPlan", TAG);
                AppPlanDto appPlanDto = applicationPlanStud.createApplicationPlan(host, accessToken, serviceId,
                        ScaleUtils.planName(apiName, nameSuffix),
                        ScaleUtils.planSystemName(MiscUtil.toHexString(apiName), nameSuffix));//liyouzhi.ex-systemname不能包含中文
                log.info("createApplicationPlan appPlanDto:"+ JSON.toJSONString(appPlanDto));
                PublishApiPlan publishApiPlan = new PublishApiPlan(publishApiId, instance.getId(),
                        Long.valueOf(appPlanDto.getApplication_plan().getId()), 1, user, new Date(), new Date(),
                        appPlanDto.getApplication_plan().getName());

                PublishApiPlan publishApiPlanRes = publishApiPlanRepository.save(publishApiPlan);

                Application application = new Application();
                application.setAccessToken(accessToken);
                application.setName(ScaleUtils.applicationName(apiName, nameSuffix));
                application.setDescription(ScaleUtils.applicationDesc(apiName, nameSuffix));
                application.setPlan(appPlanDto.getApplication_plan());
                if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(appKey)) {
                    application.setApplicationId(appId);
                    Keys keys = new Keys();
                    List<String> keyList = new ArrayList<>();
                    keyList.add(appKey);
                    keys.setKey(keyList);
                    application.setKeys(keys);
                }

                // 查询accountID,若不存在则创建
                String accountUser = CommonBeanUtil.getLoginUserName();
                log.info("current login user:"+accountUser);
                String accountId = getAccountId(accountUser, instance);
                Result<Application> applicationResult = accountStud.createApplication(host, accountId, application);
                log.info("createApplication applicationResult:"+ JSON.toJSONString(applicationResult.getData()));
                Application applicationRes = applicationResult.getData();
                if(FAIL.equals(applicationResult.getCode()) || null==applicationRes){
                    log.error("3scale create error:"+applicationResult.getMsg());
                    returnResult.setError(applicationResult.getMsg());
                    return returnResult;
                }

                log.debug("{}applicationRes={}", TAG, applicationRes);

                PublishApplication publishApplication = new PublishApplication(applicationRes.getName(), accountUser,
                        systemId, publishApiPlanRes, 1, new Date(), new Date(), publishApiRes,
                        applicationRes.getDescription(), applicationRes.getId());
                publishApplication.setInstance(instance);
                publishApplication.setUserKey(applicationRes.getUserKey());
                publishApplication.setType(APPLICATION_TYPE_CREATE_API);
                publishApplicationRepository.save(publishApplication);

                // mapping rule,先创建，再删除
                List<com.hisense.gateway.library.stud.model.MappingRuleDto> mappingRuleDtos =
                        mappingRuleStud.searchMappingRulesList(host, accessToken, serviceId);
                log.info("{}mappingRuleDtos.size={}", TAG, mappingRuleDtos.size());
                for (com.hisense.gateway.library.stud.model.MappingRuleDto mappingRuleDto : mappingRuleDtos) {
                    mappingRuleStud.deleteMappingRules(host, accessToken, serviceId,
                            mappingRuleDto.getMapping_rule().getId());
                    log.info("{}mappingRuleDto={}", TAG, mappingRuleDto);
                }

                for (ApiMappingRule apiMappingRule : publishApiRes.getApiMappingRules()) {
                    String pattern = apiMappingRule.getPattern();
                    String httpMethods = apiMappingRule.getHttpMethod();

                    mappingRuleStud.createMapingRule(host, accessToken, serviceId, httpMethods, pattern,
                            serviceXmlDto.getMetricXmls().getMetric().get(0).getId());
                }

                proxyPoliciesStud.updateBatcherProxyPolicies(host, accessToken, serviceId);
                proxyPoliciesStud.updateAuthCachingPolicies(host, accessToken, serviceId);

                if (publishApiDto.getSourceType() == API_SRC_EUREKA_AUTO) {
                    proxyPoliciesStud.updateUrlRewritingProxyPolicies(host, accessToken, serviceId,
                            String.format("/%s/", publishApiDto.getSystemName()),publishApi.getAccessProType());
                } else {
                    proxyPoliciesStud.updateUrlRewritingProxyPolicies(host, accessToken, serviceId,
                            String.format("%s/",publishApiDto.getUrl()),publishApi.getAccessProType());
                }

                // for staging environment
                if (publishApiDto.isNeedLogging()/* &&
                        InstanceEnvironment.fromCode(environment).equals(InstanceEnvironment.ENVIRONMENT_STAGING)*/) {
                    log.info("try to config HisenseLog");
                    proxyPoliciesStud.configHisenseLog(host, accessToken, serviceId, publishApiDto.isNeedLogging(),
                            publishApiDto.isNeedRecordRet(), publishApiId);
                }

                // ip黑白名单
                if (MiscUtil.isNotEmpty(publishApiDto.getIpWhiteList())) {
                    List<String> ipWhiteList = PublishApiUtil.getAddressList(publishApiDto.getIpWhiteList());
                    apiCastService.configIpBlackWhiteList(new ApiCastParam(host, accessToken, serviceId),
                            ipWhiteList, false);
                }

                if (MiscUtil.isNotEmpty(publishApiDto.getIpBlackList())) {
                    List<String> ipBlackList = PublishApiUtil.getAddressList(publishApiDto.getIpBlackList());
                    apiCastService.configIpBlackWhiteList(new ApiCastParam(host, accessToken, serviceId),
                            ipBlackList, true);
                }

                // upstream 连接超时
                if (publishApiDto.getTimeout() != null && publishApiDto.getTimeout() > 0) {
                    apiCastService.configConnectionTimeout(new ApiCastParam(host, accessToken, serviceId),
                            publishApiDto.getTimeout());
                }
                //Header Modification policy
                if(!CollectionUtils.isEmpty(publishApiDto.getRequestHeader()) || !CollectionUtils.isEmpty(publishApiDto.getResponseHeader())){
                    apiCastService.configHeaderpolicy(new ApiCastParam(host, accessToken, serviceId),publishApiDto.getRequestHeader(),publishApiDto.getResponseHeader());
                }

                ProxyConfigDto proxyConfigDto = serviceStud.latestPromote(host, accessToken, Long.valueOf(serviceId),
                        ModelConstant.ENV_SANDBOX);
                log.info("{}Create proxyConfigDto={}", TAG, proxyConfigDto);

                if ("noauth".equals(publishApiDto.getAuthType())) {
                    PolicieConfigDto policieConfigDto = null;
                    if (!GlobalSettings.isDefaultCredentialsWithUserKey()) {
                        appId = applicationRes.getApplicationId();
                        appKey = applicationRes.getKeys().getKey().get(0);

                        log.info("{}appId={}", TAG, appId);
                        log.info("{}appKey={}", TAG, appKey);

                        policieConfigDto = proxyPoliciesStud.updateAnonymousProxyPolicies(
                                host, accessToken, serviceId, appId, appKey, null);
                    } else {
                        policieConfigDto = proxyPoliciesStud.updateAnonymousProxyPolicies(
                                host, accessToken, serviceId, null, null, applicationRes.getUserKey());
                    }

                    Map<String, Object> configuration = new HashMap<>();
                    Boolean enabled = true;
                    String type = "";
                    for (PolicieConfig policieConfig : policieConfigDto.getPolicies_config()) {
                        if ("default_credentials".equals(policieConfig.getName())) {
                            configuration = policieConfig.getConfiguration();
                            enabled = policieConfig.getEnabled();
                            type = policieConfig.getName();
                            break;
                        }
                    }

                    ApiPolicy apiPolicy = new ApiPolicy(type, "description", JSONArray.toJSONString(configuration),
                            enabled, type, projectId, user, new Date(), new Date());
                    if (mark) {// 多种环境policy值保留一份
                        apiPolicyRes = apiPolicyRepository.save(apiPolicy);
                        mark = false;
                    }

                    PublishApiPolicyRelationship papr = new PublishApiPolicyRelationship(publishApiId,
                            apiPolicyRes.getId(), instance.getId(), Long.valueOf(serviceId));
                    paprRepository.save(papr);
                }
            }
        } catch (Exception e) {
            log.error("create api error:{}",e);
            for (Map<String, String> serviceInfo : serviceInfos) {
                serviceStud.serviceDelete(
                        serviceInfo.get("host"),
                        serviceInfo.get("accessToken"),
                        serviceInfo.get("serviceId")
                );
            }

            // 出现异常时,api应该标记为删除状态
            log.info("{}3scale实例创建阶段异常,暂时删除已落库的API", TAG);
            publishApiRepository.updateStatus(publishApiId, API_DELETE);

            returnResult.setCode(FAIL);
            returnResult.setMsg("创建失败"+e.getMessage());
            returnResult.setData(null);
            throw new Exception("创建失败!"+e.getMessage());
//            return returnResult;
        }

        returnResult.setCode(OK);
        returnResult.setMsg("创建成功");
        returnResult.setData(publishApiId);
        return returnResult;
    }

    private boolean getSubscribeStatusForApi(Integer apiId) {
        PublishApi publishApi = publishApiRepository.findOne(apiId);
        if (publishApi == null) {
            log.error("{}Api {} not exist", TAG, apiId);
            return false;
        }

        List<PublishApplication> publishApplications = publishApplicationRepository.findByAppId(apiId);
        List<Integer> applicationIds =
                publishApplications.stream().map(PublishApplication::getId).collect(Collectors.toList());
        if (applicationIds.size() > 0) {
            List<ProcessRecord> processRecords = processRecordRepository.findSubscribeByRelIdAndStatus(applicationIds,
                    Arrays.asList(1, 2));
            return MiscUtil.isNotEmpty(processRecords);
        }
        return false;
    }

    private Result<Boolean> updateGroupForPublishApi(Integer apiId, Integer groupId) {
        Result<Boolean> result = new Result<>(FAIL, "设置分组失败", false);
        try {
            if (publishApiRepository.findOne(apiId) == null) {
                result.setMsg("设置分组失败, API不存在");
                return result;
            }

            publishApiRepository.updateGroupId(apiId, groupId,API_INIT);
        } catch (Exception e) {
            log.error("context",e);
            result.setMsg("设置分组失败, 程序异常");
            return result;
        }

        result.setCode(OK);
        result.setMsg("设置分组成功");
        result.setData(true);
        return result;
    }

    /**
     * API批量操作
     */
    private Result<List<String>> batchOperation(PublishApiBatch batch, ApiConstant.ApiBatchOPS apiBatchOPS) {
        Result<List<String>> result = new Result<>(FAIL, String.format("批量【%s】,失败", apiBatchOPS.getName()), null);
        if (batch == null || MiscUtil.isEmpty(batch.getIds())) {
            result.setMsg(String.format("未选择任何API,请勾选待【%s】的API", apiBatchOPS.getName()));
            return result;
        }

        if (apiBatchOPS == SET_GROUP) {
            if (batch.getGroupId() == null || batch.getGroupId() <= 0) {
                result.setMsg("未指定分组");
                return result;
            }

            try {
                PublishApiGroup apiGroup = publishApiGroupRepository.findOne(batch.getGroupId());
                if (apiGroup == null) {
                    result.setMsg("错误, 指定的分组不存在");
                    return result;
                }
            } catch (Exception e) {
                log.error("context",e);
                result.setMsg("错误, 异常");
                return result;
            }
        }

        Result<Boolean> resultInner = new Result<>();
        List<String> failMessage = new ArrayList<>();
        for (Integer id : batch.getIds()) {
            switch (apiBatchOPS) {
                case DELETE:
                    resultInner = deletePublishApi(id);
                    break;

                case PROMOTE:
                    PromoteRequestInfo requestInfo = new PromoteRequestInfo();
                    requestInfo.setId(id);
                    requestInfo.setIsCompatible(GlobalSettings.isApiPromoteCompatible());
                    resultInner = promotePublishApi(requestInfo,batch.getEnvironment());
                    break;

                case SET_GROUP:
                    resultInner = updateGroupForPublishApi(id, batch.getGroupId());
                    break;

                case OFF_LINE:
                    resultInner = offlinePublishApi(id);
                    break;

                case DELETE_FILE:
                    resultInner = deleteApiDoc(id);
                    break;
            }

            if (resultInner.isFailure()) {
                String message = String.format("%s", resultInner.getMsg());
                log.info("{}Fail to {} {}", TAG, apiBatchOPS, message);
                failMessage.add(message);
            }
        }

        int failCount = failMessage.size();
        int successCount = batch.getIds().size() - failCount;

        String endMessage;
        if (failCount == 0 && successCount > 0) {
            endMessage = String.format("成功%s%d个API", apiBatchOPS.getName(), successCount);
        } else if (successCount > 0) {
            endMessage = String.format("成功%s%d个API, 未能%s%d个API,失败原因：%s", apiBatchOPS.getName(), successCount,
                    apiBatchOPS.getName(), failCount,JSONObject.toJSONString(failMessage));
        } else {
            endMessage = String.format("失败,未能%s%d个API,失败原因：%s", apiBatchOPS.getName(), failCount,JSONObject.toJSONString(failMessage));
        }

        result.setCode(successCount > 0 ? OK : FAIL);
        result.setMsg(endMessage);
        result.setAlert(1);// for pangea
        result.setData(null);// for pangea
        return result;
    }

    @Override
    public Result<Boolean> deleteApiDoc(Integer fileId) {
        Result<Boolean> result = new Result<>(FAIL, "错误", false);
        ApiDocFile apiDocFile = apiDocFileRepository.findOne(fileId);
        if (apiDocFile == null) {
            result.setCode(OK);
            result.setMsg("指定文件不存在");
            return result;
        }

        // 删除API中file列表项
        if(apiDocFile.getApiId()!=null){
            PublishApi publishApi = publishApiRepository.findOne(apiDocFile.getApiId());
            if (publishApi != null) {
                if (MiscUtil.isNotEmpty(publishApi.getFileDocIds())) {
                    List<Integer> list = MiscUtil.fromJson(publishApi.getFileDocIds(), PublishApiDto.class).getFileDocIds();
                    if (list != null) {
                        list.remove(fileId);
                        publishApiRepository.updateFileDocIds(publishApi.getId(), PublishApiUtil.buildFileDocIdStr(list));
                    }
                }
            }
        }


        // 清除文件
        String path = apiDocFile.getPath();
        try {
            Files.delete(Paths.get(new File(path).getPath()));
            //删除数据库记录
            apiDocFileRepository.deleteById(fileId);
        } catch (IOException e) {
            log.error("文件IO异常",e);
            result.setMsg("文件IO错误");
            result.setCode(OK);
            return result;
        }

        result.setMsg("Success");
        result.setCode(OK);
        return result;
    }

    @Override
    public  Result<String> getImageBase64Str(Integer docId){
        Result<String> result = new Result<>(OK,"文件预览查询成功！","");
        ApiDocFile docFile = apiDocFileRepository.findOne(docId);
        if(null == docFile || StringUtils.isBlank(docFile.getPath())){
            result.setCode(FAIL);
            result.setMsg("file or filepath not  exist!");
            return result;
        }
        String filePath = docFile.getPath();
        String fileType = filePath.substring(filePath.lastIndexOf(".")+1);
        String base64Str = "data:image/"+fileType+";base64,";
        String imageBase64Str = getImgStr(filePath);
        if(StringUtils.isBlank(imageBase64Str)){
            result.setError(FAIL,"文件不存在！");
            return result;
        }
        result.setData(base64Str+imageBase64Str);
        return result;
    }

    /**
     * 一件发生产 - 旧的api数据及编辑的数据写入新的api
     * @param oldApi
     * @param publishApiDto
     * @param user
     * @param group
     * @return
     */
    private PublishApi copyApi(PublishApi oldApi,PublishApiDto publishApiDto,String user,PublishApiGroup group){
        PublishApi newPublishApi = new PublishApi();
        BeanUtils.copyProperties(oldApi,newPublishApi);
        newPublishApi.setId(null);
        newPublishApi.setEnvironment(ENVIRONMENT_PRODUCTION.getCode());// guilai 2020/09/16
        newPublishApi.setCreateTime(new Date());
        newPublishApi.setUpdateTime(new Date());
        newPublishApi.setCreator(user);
        newPublishApi.setGroup(group);
        newPublishApi.setStatus(1);
        newPublishApi.setIsOnline(0);
        newPublishApi.setAlertPolicyId(null);//清除告警策略
        newPublishApi.setSystemId(oldApi.getSystemId());
        if(StringUtils.isNotBlank(publishApiDto.getAccessProtocol()))newPublishApi.setAccessProtocol(publishApiDto.getAccessProtocol());
        if(StringUtils.isNotBlank(publishApiDto.getHost()))newPublishApi.setHost(publishApiDto.getHost());
        if(StringUtils.isNotBlank(publishApiDto.getName()))newPublishApi.setName(publishApiDto.getName());
        newPublishApi.setSecretToken(publishApiDto.getSecretToken());
        newPublishApi.setNeedSubscribe(publishApiDto.isNeedSubscribe());
        newPublishApi.setNeedAuth(publishApiDto.isNeedAuth());
        newPublishApi.setAuthType(publishApiDto.isNeedAuth() ? "auth" : "noauth");
        newPublishApi.setNeedLogging(publishApiDto.isNeedLogging());
        if(StringUtils.isNotBlank(publishApiDto.getDescription()))newPublishApi.setDescription(publishApiDto.getDescription());
        newPublishApi.setSecretLevel(InstanceSecretLevel.fromName(publishApiDto.getSecretLevel()).getCode());
        newPublishApi.setTimeout(publishApiDto.getTimeout());
        newPublishApi.setUrl(publishApiDto.getUrl());
        if (MiscUtil.isNotEmpty(publishApiDto.getApiMappingRuleDtos())) {
            newPublishApi.setApiMappingRules(MappingRuleUtil.buildRuleWithDtos(publishApiDto.getApiMappingRuleDtos(),
                    true));
        }
        return newPublishApi;
    }

    /**
     * 一键发生产
     * @param id
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public  Result<Boolean> oneClickPromoteApiToProd(String environment,String tenantId, String projectId,Integer id,PromoteApiToProd promoteApiToProd) throws Exception {
        Result<Boolean> rlt = new Result<>(OK,"发布成功，已发起流程审批，请在【发布申请】菜单下查看审批详情！",true);
        if (id == null) {
            rlt.setMsg("参数错误未指定ApiId");
            return rlt;
        }

        PublishApi publishApi = publishApiRepository.findOne(id);
        Integer stagApppId = publishApi.getId();
        if (publishApi == null) {
            rlt.setMsg("指定的API不存在");
            return rlt;
        }

        String user = CommonBeanUtil.getLoginUserName();//获取当前登录用户信息
        Instance instance = instanceRepository.searchInstanceByPartitionEnvironment(InstancePartition.fromCode(publishApi.getPartition()).getName(),ENVIRONMENT_PRODUCTION.getName());
        if(null == instance){
            rlt.setError(FAIL,String.format("不存在%s的3scale实例",ENVIRONMENT_PRODUCTION.getName()));
            return rlt;
        }

        PublishApiGroup oldGroup = publishApiGroupRepository.findOne(publishApi.getGroup().getId());
        //确认生产环境有没有对应分组，没有就创建(环境,groupName,系统)

        Specification<PublishApiGroup> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("environment").as(Integer.class), ENVIRONMENT_PRODUCTION.getCode()));
            andList.add(builder.equal(root.get("name").as(String.class), oldGroup.getName()));
            andList.add(builder.equal(root.get("system").as(Integer.class), publishApi.getSystemId()));
            andList.add(builder.equal(root.get("projectId").as(String.class),String.valueOf(promoteApiToProd.getProjectId())));
            andList.add(builder.equal(root.get("tenantId").as(String.class),tenantId));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAll(spec);
        PublishApiGroup group = new PublishApiGroup();
        if(CollectionUtils.isEmpty(publishApiGroups)){
            BeanUtils.copyProperties(oldGroup,group);
            group.setEnvironment(ENVIRONMENT_PRODUCTION.getCode());
            group.setCreateTime(new Date());
            group.setCreator(user);
            group.setUpdateTime(new Date());
            group.setId(null);
            group.setStatus(1);
            group.setProjectId(String.valueOf(promoteApiToProd.getProjectId()));
            group.setSystem(publishApi.getSystemId());
            publishApiGroupRepository.saveAndFlush(group);
        }else{
            group =  publishApiGroups.get(0);
        }

        //判断当前AP是否在生产环境发布过
        PublishApiGroup finalGroup = group;
        Specification<PublishApi> Apispec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("environment").as(Integer.class), ENVIRONMENT_PRODUCTION.getCode()));
            andList.add(builder.equal(root.get("name").as(String.class), publishApi.getName()));
            andList.add(builder.equal(root.get("group").get("id").as(Integer.class), finalGroup.getId()));
            andList.add(builder.equal(root.get("systemId").as(Integer.class), publishApi.getSystemId()));
            CriteriaBuilder.In<Integer> in = builder.in(root.get("status"));
            in.value(1);
            in.value(4);
            andList.add(in);
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> publishApis = publishApiRepository.findAll(Apispec);
        if(!CollectionUtils.isEmpty(publishApis)){
            rlt.setError(FAIL,String.format("生产环境已发布或正在发布该Api-%s",publishApi.getName()));
            return rlt;
        }
        PublishApiDto publishApiDto = new PublishApiDto();
        BeanUtils.copyProperties(promoteApiToProd,publishApiDto);
        PublishApiUtil.doPreProcessForDto(publishApiDto, true);
        publishApiDto.setName(promoteApiToProd.getApiName());
        publishApiDto.setPartition(publishApi.getPartition());
        Result<ApiValidateStatus> validateStatusResult = validateApiFields(publishApiDto, true, ENVIRONMENT_PRODUCTION.PRODUCTION);
        if (validateStatusResult.isFailure()) {
            log.error("{}Fail to validate {}", TAG, validateStatusResult.getMsg());
            rlt.setError(validateStatusResult.getMsg());
            return rlt;
        }

        // URL+mappingRule校验
        Result<List<PublishApi>> existsApis = mappingRuleService.existApisWithSameMappingRule(
                MappingRuleUtil.buildRuleWithDtos(publishApiDto.getApiMappingRuleDtos(), false),ENVIRONMENT_PRODUCTION.getCode());

        if (existsApis.isSuccess()) {
            rlt.setError(existsApis.getMsg());
            return rlt;
        }
        //判断生产环境中其他系统是否存在相同后端地址+端口
        /*if(StringUtils.isNotBlank(promoteApiToProd.getAccessProtocol()) && StringUtils.isNotBlank(promoteApiToProd.getHost()) && promoteApiToProd.getProjectId()!=null){
            List<PublishApi> publishApiList = publishApiRepository.findApiByHost(promoteApiToProd.getHost(),promoteApiToProd.getAccessProtocol(),ENVIRONMENT_PRODUCTION.getCode());
            if(publishApiList!=null && publishApiList.size()>0){
                for(PublishApi api : publishApiList){
                    PublishApiGroup apiGroup = publishApiGroupRepository.getOne(api.getGroup().getId());
                    Integer project = Integer.valueOf(apiGroup.getProjectId());
                    if(!promoteApiToProd.getProjectId().equals(project)){
                        rlt.setError(FAIL,"后端服务地址在其他系统中已存在！");
                        return rlt;
                    }
                }
            }
        }*/

        //强制执行持久化
        PublishApi newPublishApi =  copyApi(publishApi, publishApiDto, user, group);
        publishApiRepository.saveAndFlush(newPublishApi);
        //创建关联数据对象
        Integer prodApiId = newPublishApi.getId();

        DataItem dataItem = dataItemRepository.findOne(promoteApiToProd.getProjectId());
        String groupSystemKey = "";
        if (dataItem != null) {
            groupSystemKey = dataItem.getItemKey();
        }
        String appId = "";
        String appKey = "";
        List<Map<String, String>> serviceInfos = new ArrayList<>();
        String host = instance.getHost();
        String accessToken = instance.getAccessToken();
        String description = newPublishApi.getDescription();
        String apiName = newPublishApi.getName();
        String nameSuffix = String.format("%s_%s_%s_%s", tenantId, groupSystemKey, projectId,
                instance.getClusterPartition());
        String systemName = ScaleUtils.systemName(tenantId, nameSuffix);



        // TODO service create 调试暂时注释掉
        ServiceXmlDto serviceXmlDto = serviceStud.createXmlService(host, accessToken, apiName, description,
                systemName);
        log.info("createXmlService serviceXmlDto:"+ JSON.toJSONString(serviceXmlDto));
        String serviceId = serviceXmlDto.getId();
        Map<String, String> serviceMap = new HashMap<>(3);
        serviceMap.put("host", host);
        serviceMap.put("accessToken", accessToken);
        serviceMap.put("serviceId", serviceId);
        serviceInfos.add(serviceMap);

        PublishApiInstanceRelationship publishApiInstanceRelationship =
                new PublishApiInstanceRelationship(prodApiId, instance.getId(), Long.valueOf(serviceId));
        pairRepository.save(publishApiInstanceRelationship);

        // 组合apiBackend
        //String apiBackend = combinationApiBackend(publishApiDto);
//        PublishApiDto publishApiDto = new PublishApiDto();
        publishApiDto.setAccessProtocol(newPublishApi.getAccessProtocol());
        publishApiDto.setHost(newPublishApi.getHost());
        publishApiDto.setPort(newPublishApi.getPort());
        publishApiDto.setRealmName(newPublishApi.getRealmName());
        publishApiDto.setProxy(GlobalSettings.getDefaultProxy());
        String apiBackend = GlobalSettings.wrapperHostPort(publishApiDto);

        log.info("{}apiBackend={}", TAG, apiBackend);

        String endpoint = instance.getProduction();//production
        String sandboxEndpoint = instance.getSandbox();//staging

        log.info("sandboxEndpoint={},endpoint={}", sandboxEndpoint, endpoint);//liyouzhi.ex-2020-10-15
        String rewriteNme = publishApiDto.getRealmName();
        publishApiDto.getProxy().setHostnameRewrite(rewriteNme);
        log.info("{}Start to proxyUpdate", TAG);
        Result<Object> serviceResult = serviceStud.proxyUpdate(host, accessToken, serviceId, endpoint, apiBackend, sandboxEndpoint,
                publishApiDto.getProxy(), newPublishApi.getSecretToken());
        if(FAIL.equals(serviceResult.getCode())){
            String errorMsg = String.format("3scale初始化服务基本信息异常，请检查后端服务地址和路由规则！错误详情：%S",serviceResult.getMsg());
            log.error("3scale初始化服务基本信息异常，请检查后端服务地址和路由规则！错误详情："+serviceResult.getMsg());
            rlt.setError("3scale初始化服务基本信息异常，请检查并修改后端服务地址和路由规则！修改后重新提交发布！");
            throw new Exception(errorMsg);
        }

        log.info("{}Start to create ApplicationPlan", TAG);
        AppPlanDto appPlanDto = applicationPlanStud.createApplicationPlan(host, accessToken, serviceId,
                ScaleUtils.planName(apiName, nameSuffix),
                ScaleUtils.planSystemName(MiscUtil.toHexString(apiName), nameSuffix));//liyouzhi.ex-systemname不能包含中文
        log.info("createApplicationPlan appPlanDto:"+ JSON.toJSONString(appPlanDto));
        PublishApiPlan publishApiPlan = new PublishApiPlan(prodApiId, instance.getId(),
                Long.valueOf(appPlanDto.getApplication_plan().getId()), 1, user, new Date(), new Date(),
                appPlanDto.getApplication_plan().getName());

        PublishApiPlan publishApiPlanRes = publishApiPlanRepository.save(publishApiPlan);

        Application application = new Application();
        application.setAccessToken(accessToken);
        application.setName(ScaleUtils.applicationName(apiName, nameSuffix));
        application.setDescription(ScaleUtils.applicationDesc(apiName, nameSuffix));
        application.setPlan(appPlanDto.getApplication_plan());
        if (StringUtils.isNotBlank(appId) && StringUtils.isNotBlank(appKey)) {
            application.setApplicationId(appId);
            Keys keys = new Keys();
            List<String> keyList = new ArrayList<>();
            keyList.add(appKey);
            keys.setKey(keyList);
            application.setKeys(keys);
        }

        // 查询accountID,若不存在则创建
        String accountUser = CommonBeanUtil.getLoginUserName();
        log.info("current login user:"+accountUser);
        String accountId = getAccountId(accountUser, instance);
        Result<Application> applicationResult = accountStud.createApplication(host, accountId, application);
        log.info("createApplication applicationResult:"+ JSON.toJSONString(applicationResult.getData()));
        Application applicationRes = applicationResult.getData();
        if(FAIL.equals(applicationResult.getCode()) || null==applicationRes){
            log.error("3scale create error:"+applicationResult.getMsg());
            rlt.setError(applicationResult.getMsg());
            return rlt;
        }

        log.debug("{}applicationRes={}", TAG, applicationRes);

        // mapping rule,先创建，再删除
        List<com.hisense.gateway.library.stud.model.MappingRuleDto> mappingRuleDtos =
                mappingRuleStud.searchMappingRulesList(host, accessToken, serviceId);
        log.info("{}mappingRuleDtos.size={}", TAG, mappingRuleDtos.size());
        for (com.hisense.gateway.library.stud.model.MappingRuleDto mappingRuleDto : mappingRuleDtos) {
            mappingRuleStud.deleteMappingRules(host, accessToken, serviceId,
                    mappingRuleDto.getMapping_rule().getId());
            log.info("{}mappingRuleDto={}", TAG, mappingRuleDto);
        }
        //创建mappingrules
        List<ApiMappingRule> mappingRules = newPublishApi.getApiMappingRules();
        for (ApiMappingRule apiMappingRule : mappingRules) {
            String pattern = apiMappingRule.getPattern();
            String httpMethods = apiMappingRule.getHttpMethod();
            mappingRuleStud.createMapingRule(host, accessToken, serviceId, httpMethods, pattern,
                    serviceXmlDto.getMetricXmls().getMetric().get(0).getId());
        }

        proxyPoliciesStud.updateBatcherProxyPolicies(host, accessToken, serviceId);
        proxyPoliciesStud.updateAuthCachingPolicies(host, accessToken, serviceId);

        if (newPublishApi.getSourceType() == API_SRC_EUREKA_AUTO) {
            proxyPoliciesStud.updateUrlRewritingProxyPolicies(host, accessToken, serviceId,
                    String.format("/%s/", dataItem.getItemName()),newPublishApi.getAccessProType());
        } else {
            proxyPoliciesStud.updateUrlRewritingProxyPolicies(host, accessToken, serviceId,
                    String.format("%s/",newPublishApi.getUrl()),newPublishApi.getAccessProType());
        }

        // for staging environment
        if (newPublishApi.isNeedLogging() /*&&
                InstanceEnvironment.fromCode(newPublishApi.getEnvironment()).equals(InstanceEnvironment.ENVIRONMENT_STAGING)*/) {
            log.info("try to config HisenseLog");
            proxyPoliciesStud.configHisenseLog(host, accessToken, serviceId, newPublishApi.isNeedLogging(),
                    newPublishApi.isNeedRecordRet(), prodApiId);
        }

        // ip黑白名单
        if (MiscUtil.isNotEmpty(newPublishApi.getIpWhiteList())) {
            List<String> ipWhiteListtrings = Arrays.asList(newPublishApi.getIpWhiteList().split(","));
            List<String> ipWhiteList = PublishApiUtil.getAddressList(ipWhiteListtrings);
            apiCastService.configIpBlackWhiteList(new ApiCastParam(host, accessToken, serviceId),
                    ipWhiteList, false);
        }

        if (MiscUtil.isNotEmpty(newPublishApi.getIpBlackList())) {
            List<String> ipBlackLists = Arrays.asList(newPublishApi.getIpBlackList().split(","));
            List<String> ipBlackList = PublishApiUtil.getAddressList(ipBlackLists);
            apiCastService.configIpBlackWhiteList(new ApiCastParam(host, accessToken, serviceId),
                    ipBlackList, true);
        }


        // upstream 连接超时
        if (newPublishApi.getTimeout() != null && newPublishApi.getTimeout() > 0) {
            apiCastService.configConnectionTimeout(new ApiCastParam(host, accessToken, serviceId),
                    publishApi.getTimeout());
        }
        //Header Modification policy
        if(MiscUtil.isNotEmpty(newPublishApi.getRequestHeader()) || MiscUtil.isNotEmpty(newPublishApi.getResponseHeader())){
            List<PolicyHeader> requestHeader = StringUtils.isBlank(publishApi.getRequestHeader())?null:JSONArray.parseArray(publishApi.getRequestHeader(), PolicyHeader.class);
            List<PolicyHeader> responseHeader = StringUtils.isBlank(publishApi.getResponseHeader())?null:JSONArray.parseArray(publishApi.getResponseHeader(), PolicyHeader.class);
            if(!CollectionUtils.isEmpty(requestHeader) || !CollectionUtils.isEmpty(responseHeader) ){
                apiCastService.configHeaderpolicy(new ApiCastParam(host, accessToken, serviceId),requestHeader,responseHeader);
            }
        }

        ProxyConfigDto proxyConfigDto = serviceStud.latestPromote(host, accessToken, Long.valueOf(serviceId),
                ModelConstant.ENV_SANDBOX);
        log.info("{}Create proxyConfigDto={}", TAG, proxyConfigDto);

        if ("noauth".equals(newPublishApi.getAuthType())) {
            PolicieConfigDto policieConfigDto = null;
            if (!GlobalSettings.isDefaultCredentialsWithUserKey()) {
                appId = applicationRes.getApplicationId();
                appKey = applicationRes.getKeys().getKey().get(0);

                log.info("{}appId={}", TAG, appId);
                log.info("{}appKey={}", TAG, appKey);

                policieConfigDto = proxyPoliciesStud.updateAnonymousProxyPolicies(
                        host, accessToken, serviceId, appId, appKey, null);
            } else {
                policieConfigDto = proxyPoliciesStud.updateAnonymousProxyPolicies(
                        host, accessToken, serviceId, null, null, applicationRes.getUserKey());
            }

            Map<String, Object> configuration = new HashMap<>();
            Boolean enabled = true;
            String type = "";
            for (PolicieConfig policieConfig : policieConfigDto.getPolicies_config()) {
                if ("default_credentials".equals(policieConfig.getName())) {
                    configuration = policieConfig.getConfiguration();
                    enabled = policieConfig.getEnabled();
                    type = policieConfig.getName();
                    break;
                }
            }

            ApiPolicy apiPolicy = new ApiPolicy(type, "description", JSONArray.toJSONString(configuration),
                    enabled, type, projectId, user, new Date(), new Date());
            ApiPolicy apiPolicyRes  = apiPolicyRepository.save(apiPolicy);

            PublishApiPolicyRelationship papr = new PublishApiPolicyRelationship(prodApiId,
                    apiPolicyRes.getId(), instance.getId(), Long.valueOf(serviceId));
            paprRepository.save(papr);
        }

        //创建mappingrules
        if(!CollectionUtils.isEmpty(mappingRules)){

            Result<List<Integer>>
                rules = mappingRuleService.saveRules(newPublishApi.getApiMappingRules(), newPublishApi.getId());
            if (rules == null || rules.getData() == null) {
                rlt.setMsg(String.format("%s,Mapping rule 存储失败", rlt.getMsg()));
                return rlt;
            }
            newPublishApi.setMappingRuleIds(MiscUtil.encodeListIds(rules.getData()));
            publishApiRepository.saveAndFlush(newPublishApi);
        }

        //创建processRecord
        ProcessRecord processRecord = new ProcessRecord();
        List<ProcessRecord> processRecords = processRecordRepository.findProcessRecordByRelId(stagApppId,2);
        if(!CollectionUtils.isEmpty(processRecords)){
            BeanUtils.copyProperties(processRecords.get(0),processRecord);
            processRecord.setRelId(prodApiId);
            processRecord.setCreateTime(new Date());
            processRecord.setId(null);
            processRecord.setUpdateTime(new Date());
            processRecord.setCreator(user);
            processRecord.setStatus(1);
            processRecordRepository.saveAndFlush(processRecord);
        }

        Specification<PublishApplication> appSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("publishApi").get("id").as(Integer.class), stagApppId));
            andList.add(builder.equal(root.get("publishApi").get("status").as(Integer.class), 4));
            andList.add(builder.equal(root.get("type").as(Integer.class), 0));
            andList.add(builder.equal(root.get("publishApi").get("environment").as(Integer.class), InstanceEnvironment.fromCode(environment).getCode()));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApplication> byApiIdAndStatusAndType = publishApplicationRepository.findAll(appSpec);
        if(!CollectionUtils.isEmpty(byApiIdAndStatusAndType)){
            PublishApplication publishApplication = new PublishApplication();
            BeanUtils.copyProperties(byApiIdAndStatusAndType.get(0),publishApplication);
            publishApplication.setPublishApi(newPublishApi);
            publishApplication.setCreateTime(new Date());
            publishApplication.setId(null);
            publishApplication.setUpdateTime(new Date());
            publishApplication.setCreator(user);
            publishApplication.setInstance(instance);
            publishApplication.setStatus(1);
            publishApplication.setUserKey(applicationRes.getUserKey());
            publishApplication.setSystem(publishApi.getSystemId());
            publishApplication.setApiPlan(publishApiPlan);
            PublishApplication publishApplication1 = publishApplicationRepository.saveAndFlush(publishApplication);
            apiInvokeRecordService.updateApiIdAndAppKey(publishApplication1.getId());
        }

        //文件对象
        List<ApiDocFile> docFiles = apiDocFileRepository.findByApiId(stagApppId);
        if(!CollectionUtils.isEmpty(docFiles)){
            List<ApiDocFile> newDocFiles =  new ArrayList<>();
            docFiles.forEach(docFile->{
                ApiDocFile file = new ApiDocFile();
                BeanUtils.copyProperties(docFile,file);
                file.setApiId(prodApiId);
                file.setId(null);
                file.setCreateTime(new Date());
                file.setCreator(user);
                newDocFiles.add(file);
            } );
            apiDocFileRepository.saveAll(newDocFiles);
        }

        if(rlt.getData() && rlt.getCode().equals(OK)){
            //启动流程
            SystemInfo systemInfo = systemInfoRepository.findOne(promoteApiToProd.getProjectId());
            if(systemInfo==null){
                log.error("systemInfo is not exit");
            }
            //点击发布  当前为生产环境 启动审批流程
            Map<String,Object> map =new HashMap<>();
            //此处就是发生产，必须起流程
            map.put("publishEvn","production");
            if(systemInfo!=null){
                if(systemInfo.getPrdApiAdminName()==null){
                    throw new NotExist("项目管理员为空,流程启动失败！");
                }
                map.put("projectManager",systemInfo.getPrdApiAdminName());
            }
            //map.put("projectManager","xueyukun");
            map.put("link",link);
            map.put("im_link",imLink);
            WorkFlowDto flowDto = new WorkFlowDto();
            flowDto.setUserID(user);
            flowDto.setUserName(user);
            flowDto.setProcessDefName(BpmConstant.PUBLISH_API);
            flowDto.setProcessInstName("发布API");
            flowDto.setRelaDatas(map);
            Map map1 = new HashMap();
            String theme = String.format("%s-申请发布API-%s到%s", dataItemRepository.findOne(publishApi.getSystemId()).getItemName(),publishApi.getName(),ENVIRONMENT_PRODUCTION.getName());
            map1.put("theme",theme);
            flowDto.setBizInfo(map1);
            Result<Object> result = workFlowService.startProcess(flowDto);
            if(Result.OK.equals(result.getCode()) && null != result.getData()){
                JSONObject resultObj = (JSONObject)result.getData();
                if(null != resultObj){
                    processRecord.setProcessInstID(String.valueOf(resultObj.get("processInstID")));
                    processRecordRepository.saveAndFlush(processRecord);
                }
            }else {
                throw new NotExist("流程启动失败");
            }
            rlt.setMsg("一键发生产成功，正在等待管理员审批");
        }
        //一键发生产操作记录
        String msg = OK.equals(rlt.getCode())?OperationType.fromCode(7).getName():OperationType.fromCode(8).getName()+rlt.getMsg();
        String name = OperationType.fromCode(6).getName()+publishApi.getName();
        OperationApi operationApi = new OperationApi(name,user,new Date(),new Date(),6,msg,publishApi);
        operationApiRepository.save(operationApi);

        return rlt;
    }

    /**
     * 将图片转换成Base64编码
     * @param imgFilePath 待处理图片
     * @return
     */
    private String getImgStr(String imgFilePath) {
        File file = new File(imgFilePath);
        if(!file.exists()){
            return null;
        }
        //将图片文件转换为字节数组字符串，并对其进行Base64编码处理
        InputStream in = null;
        byte[] data = null;
        ByteArrayOutputStream outStream = null;
        //读取图片字节数组
        try{
            in = new FileInputStream(imgFilePath);
            outStream = new ByteArrayOutputStream();
           /* data = new byte[in.available()];
            in.read(data);*/
            data = new byte[1024];
            int len = 0;
            // 使用一个输入流从buffer里把数据读取出来
            while ((len = in.read(data)) != -1) {
                // 用输出流往buffer里写入数据，中间参数代表从哪个位置开始读，len代表读取的长度
                outStream.write(data, 0, len);
            }
            return new String(Base64.encodeBase64(outStream.toByteArray()));
        }catch (Exception e){
            log.error("文件预览,文件转换字节数组异常：",e);
            return null;
        }finally {
            try {
                if(in!=null){
                    in.close();
                }
            } catch (IOException e) {
                log.error("in close exception",e);
            }
        }
    }


    public Result<ApiValidateStatus> validateApiFields(PublishApiDto publishApiDto, boolean create ,String environment) {
        String prefixTip = create ? "创建失败，" : "更新失败，";
        Result<ApiValidateStatus> result = new Result<>(FAIL, "", null);

        if (StringUtils.isBlank(publishApiDto.getUrl()) || "/".equals(publishApiDto.getUrl()) ||
                !publishApiDto.getUrl().startsWith("/")) {
            result.setError(String.format("%s%s", prefixTip, "API参数异常,url为空或异常'/'"));
            return result;
        }

        if (MiscUtil.containBlank(publishApiDto.getName())) {
            result.setError(String.format("%s%s", prefixTip, "API名称不能含有空格"));
            return result;
        }

        ApiValidateStatus validateStatus = new ApiValidateStatus();
        // 3scale-2.6 not allow set the name of api with GBK encoding.
        String apiName = publishApiDto.getName() ;//liyouzhi.ex-2020-10-15

        if (publishApiDto.getPartition() == null) {
            result.setError(String.format("%s%s", prefixTip, "请选择发布环境"));
            return result;
        }

        if (MiscUtil.isEmpty(publishApiDto.getApiMappingRuleDtos())) {
            result.setError(String.format("%s%s", prefixTip, "路由规则为空"));
            return result;
        }

        if (MappingRuleUtil.existDuplicateRules(publishApiDto.getApiMappingRuleDtos())) {
            result.setError(String.format("%s%s", prefixTip, "存在重复的路由规则"));
            return result;
        }

        //修改除去自己
        int envCode = InstanceEnvironment.fromCode(environment).getCode();
        List<ApiMappingRule>  mappingRules = mappingRuleRepository.findRuleByEvn(envCode);
        if(MappingRuleUtil.existSimilarRules(publishApiDto.getApiMappingRuleDtos(),mappingRules,publishApiDto.getId())){
            result.setError(String.format("%s%s", prefixTip, "存在类似的路由规则"));
            return result;
        }

        if (publishApiDto.getProxy() == null) {
            result.setError(String.format("%s%s", prefixTip, "Proxy信息不能为空"));
            return result;
        }

        if (MiscUtil.isNotEmpty(publishApiDto.getIpBlackList())) {
            Result<String> blkipCheckResult = IpUtils.ipValidationCheck(publishApiDto.getIpBlackList());
            if (Result.FAIL.equals(blkipCheckResult.getCode())) {
                result.setError(String.format("%s%s%s", prefixTip, "指定的黑名单中有无效的IP地址",blkipCheckResult.getMsg()));
                return result;
            }
        }

        if (MiscUtil.isNotEmpty(publishApiDto.getIpWhiteList())) {
            Result<String> whiteIpCheckResult = IpUtils.ipValidationCheck(publishApiDto.getIpWhiteList());
            if (Result.FAIL.equals(whiteIpCheckResult.getCode())) {
                result.setError(String.format("%s%s%s", prefixTip, "指定的白名单中有无效的IP地址",whiteIpCheckResult.getMsg()));
                return result;
            }
        }
        PublishApiGroup publishApiGroup = null;
        if (publishApiDto.getGroupId() != null) {
            try {
                if ((publishApiGroup = publishApiGroupRepository.findOne(publishApiDto.getGroupId())) == null) {
                    throw new NotExist();
                }
            } catch (Exception e) {
                result.setError(String.format("%s %s", prefixTip, "指定的分组不存在"));
                return result;
            }

            if (create) {
                List<PublishApi> publishApis = publishApiRepository.findByNameAndProject(
                        publishApiDto.getName().trim(), publishApiDto.getGroupId());
                if (publishApis != null && publishApis.size() > 0) {
                    result.setError(String.format("%s%s", prefixTip, "同组下名称重复"));
                    return result;
                }
            }
        }

        List<Instance> instances = new ArrayList<>();
        Instance allByTenantIdAndPartition = instanceRepository.findAllByTenantIdAndPartition(InstanceEnvironment.fromCode(environment).getName(), InstancePartition.fromCode(publishApiDto.getPartition()).getName());
        instances.add(allByTenantIdAndPartition);
        if (MiscUtil.isEmpty(instances)) {
            result.setError(FAIL,String.format("%s%s", prefixTip, "未查询到scale实例"));
            return result;
        }

        validateStatus.setApiName(apiName);
        validateStatus.setGroup(publishApiGroup);
        validateStatus.setInstances(instances);

        result.setCode(OK);
        result.setMsg("validate success");
        result.setData(validateStatus);
        return result;
    }

    /**
     * API落库
     *
     * @param publishApi 已初始化的API
     * @param create     创建or更新
     * @return 成功失败提示
     */
    private Result<PublishApi> savePublishApi(PublishApi publishApi, boolean create) {
        Result<PublishApi> result = new Result<>(FAIL, "保存失败", null);
        if (log.isTraceEnabled()) {
            log.trace("{}Save Api {}", TAG, publishApi);
        }

        // 更新时,须指定ApiId
        if (!create && publishApi.getId() == null) {
            result.setMsg("Id should not be empty when update");
            return result;
        }

        // 创建时,apiId须为空
        if (create && publishApi.getId() != null) {
            result.setMsg("Id should be empty when create");
            return result;
        }

        PublishApi resultEntity;
        if (create) {
            resultEntity = publishApiRepository.save(publishApi);
        } else {
            resultEntity = publishApiRepository.saveAndFlush(publishApi);
        }

        Result<List<Integer>> rules;
        if (create) {
            rules = mappingRuleService.saveRules(publishApi.getApiMappingRules(), resultEntity.getId());
        } else {
            rules = mappingRuleService.updateRules(publishApi.getApiMappingRules(), resultEntity.getId());
        }

        if (rules == null || rules.getData() == null) {
            result.setMsg(String.format("%s,Mapping rule 存储失败", result.getMsg()));
            return result;
        }

        resultEntity.setMappingRuleIds(MiscUtil.encodeListIds(rules.getData()));
        publishApiRepository.saveAndFlush(resultEntity);

        publishApi.setId(resultEntity.getId());
        publishApi.setMappingRuleIds(resultEntity.getMappingRuleIds());

        result.setData(publishApi);

        result.setCode(Result.OK);
        result.setMsg(String.format("Success to db with %s api %d %s",
                create ? "create" : "update", result.getData().getId(), result.getData().getUrl()));
        return result;
    }

    //-------------------------------------------- for portal

    @Override
    public Page<PublishApiDto> pagePublishApi(Integer page,Integer size,Integer partition,String name, Integer categoryOne, Integer categoryTwo, Integer system, String sort,String environment) {
        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
        Specification<PublishApiGroup> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("environment").as(Integer.class), envCode));
            if (categoryOne != null) {
                andList.add(builder.equal(root.get("categoryOne").as(Integer.class), categoryOne));
            }

            if (categoryTwo != null) {
                andList.add(builder.equal(root.get("categoryTwo").as(Integer.class), categoryTwo));
            }

            if (system != null) {
                andList.add(builder.equal(root.get("system").as(Integer.class), system));
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAll(spec);
        if (CollectionUtils.isEmpty(publishApiGroups)) {
            throw new NotExist("api not exist");
        }
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "updateTime";
        if("updateTimeDesc".equalsIgnoreCase(sort)){
            direction = Sort.Direction.DESC;
            property = "updateTime";
        }
        if("updateTimeAsc".equalsIgnoreCase(sort)){
            direction = Sort.Direction.ASC;
            property = "updateTime";
        }

        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size,Sort.by(direction, property,"id"));

        Page<PublishApi> p ;
        if("subscriptCountDesc".equalsIgnoreCase(sort) || "subscriptCountAsc".equalsIgnoreCase(sort)){
            StringBuilder querySql = new StringBuilder();
            querySql.append("select api.id apiId,count(app.id) countNum from PUBLISH_APPLICATION app ");
            querySql.append("right join PUBLISH_API api on app.api_id = api.id and app.type=1 and app.status=2 ");
            StringBuilder whereSql = new StringBuilder(" where api.status=4");
            Map<String, Object> paramMap = new HashMap<>();
            if (!CollectionUtils.isEmpty(publishApiGroups)) {
                List<Integer> groupIds = new ArrayList<>();
                publishApiGroups.forEach(publishApiGroup->{
                    groupIds.add(publishApiGroup.getId());
                });
                whereSql.append(" and api.group_id in (:groupIds) ");
                paramMap.put("groupIds",groupIds);
            }
            if(null !=envCode){
                whereSql.append(" AND api.environment=:envCode ");
                paramMap.put("envCode",envCode);
            }
            if(StringUtils.isNotEmpty(name)){
                whereSql.append(" AND  api.name like :name ");
                paramMap.put("name","%" +name+ "%");
            }
            if(null != partition){
                whereSql.append(" AND  api.publish_partition= :partition ");
                paramMap.put("partition",partition);
            }
            String groupByStr=" group by api.id ,api.name,api.publish_partition,api.status,api.environment,api.group_id";
            String sortStr=" order by count(app.id) asc,api.id asc";
            if("subscriptCountDesc".equalsIgnoreCase(sort)){
                sortStr =" order by count(app.id)  desc,api.id desc";//按照查询出来的数量进行排序
            }
            querySql.append(whereSql).append(groupByStr).append(sortStr);
            log.info("querySql => "+querySql.toString());
            Query dataQuery = entityManager.createNativeQuery(querySql.toString());
            dataQuery = dataQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
            this.setParameters(dataQuery,paramMap);
            List<Map> countList =  dataQuery.getResultList() ;
            dataQuery.setFirstResult(new Long(pageable.getOffset()).intValue());
            dataQuery.setMaxResults(pageable.getPageSize());
            List<Map> pageList =  dataQuery.getResultList() ;
            LinkedList<PublishApi> publishApiList = new LinkedList<>();
            if(!CollectionUtils.isEmpty(pageList)){
                pageList.forEach(item->{
                    String apiidStr = String.valueOf(item.get("APIID"));
                    Integer apiid = Integer.parseInt(apiidStr);
                    publishApiList.add(publishApiRepository.findOne(apiid));
                });
            }
            p = new PageImpl<>(publishApiList,pageable,countList.size());
        }else{
            Specification<PublishApi> spec2 = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                if (!CollectionUtils.isEmpty(publishApiGroups)) {
                    CriteriaBuilder.In<PublishApiGroup> in = builder.in(root.get("group"));

                    for (PublishApiGroup pg : publishApiGroups) {
                        in.value(pg);
                    }
                    andList.add(in);
                }

                if (StringUtils.isNotBlank(name)) {
                    andList.add(builder.like(root.get("name").as(String.class), "%" + name + "%"));
                }
                if(null != partition){
                    andList.add(builder.equal(root.get("partition").as(Integer.class), partition));
                }
                andList.add(builder.equal(root.get("environment").as(Integer.class), envCode));
                andList.add(builder.equal(root.get("status").as(Integer.class), 4));
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            List<PublishApi> publishApiList = publishApiRepository.findAll(spec2);
            if (CollectionUtils.isEmpty(publishApiList)) {
                throw new NotExist("api not exist");
            }
            p = publishApiRepository.findAll(spec2, pageable);
        }
        List<PublishApi> publishApis = p.getContent();

        LinkedList<PublishApiDto> list = new LinkedList<>();
        PublishApiDto publishApiDto = null;
        Map<Integer,DataItem> itemMap = new HashMap<>();
        List<DataItem> dataItemList = dataItemRepository.findAll();
        dataItemList.forEach(item->{
            itemMap.put(item.getId(),item);
        });

        for (PublishApi publishApi : p.getContent()) {
            publishApiDto = PublishApiUtil.buildDtoWithApi(publishApi,
                    ApiConstant.ApiDtoBuildType.API_DTO_BUILD_QUERY_LIST);
            publishApiDto.setSubscribeCount( publishApplicationRepository.subscribeCount(publishApi.getId()));
            publishApiDto.getPublishApiGroupDto().setSystemName(itemMap.get(publishApiDto.getPublishApiGroupDto().getSystem()).getItemName());
            publishApiDto.getPublishApiGroupDto().setCategoryOneName(itemMap.get(publishApiDto.getPublishApiGroupDto().getCategoryOne()).getItemName());
            publishApiDto.getPublishApiGroupDto().setCategoryTwoName(itemMap.get(publishApiDto.getPublishApiGroupDto().getCategoryTwo()).getItemName());
            List<ApiDocFile> picFiles = apiDocFileRepository.findApiDocByTypeAndApiId(publishApi.getId(), "1");
            List<ApiDocFile> attFiles = apiDocFileRepository.findApiDocByTypeAndApiId(publishApi.getId(), "2");
            publishApiDto.setPicFiles(picFiles);
            publishApiDto.setAttFiles(attFiles);
            list.add(publishApiDto);
        }

        Page<PublishApiDto> data = new PageImpl<>(list, pageable, p.getTotalElements());
        return data;
    }



    /**
     * 给hql参数设置值
     *
     * @param query  查询
     * @param params 参数
     */
    private void setParameters(Query query, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
    }


    @Override
    public Result<Integer> downloadApiDoc(Integer docId, HttpServletResponse response) throws IOException {
        Result<Integer> result = new Result<>(Result.FAIL, "错误", 0);

        //获取文件信息
        ApiDocFile docFile = apiDocFileRepository.findOne(docId);
        if (null == docFile || StringUtils.isBlank(docFile.getPath())) {
            result.setCode(Result.FAIL);
            result.setMsg("file or filepath not exist!");
            return result;
        }

        //文件路径
        String filePath = docFile.getPath();

        File file = new File(filePath);
        if (!file.exists()) {
            result.setCode(Result.FAIL);
            result.setMsg("file not exist!");
            return result;
        }

        BufferedInputStream br=null;
        OutputStream outputStream =null;
        try {
            br=new BufferedInputStream(new FileInputStream(file));
            byte[] bytes = new byte[1024];
            int len = 0;
            response.reset();
            response.setContentType("application/x-msdownload");
            response.setHeader("Content-Disposition", "attachment; filename=" + docFile.getFileName());
            outputStream = response.getOutputStream();
            while ((len = br.read(bytes)) > 0) {
                outputStream.write(bytes, 0, len);
            }
        }catch (Exception e){
            log.error("api文件下载异常",e);
            result.setError("api文件下载异常");
        }finally {
            try {
                if(br!=null) br.close();
                if(outputStream!=null) outputStream.close();
            }catch (Exception e){
                log.error("文件流关闭异常",e);
            }
        }
        return result;
    }

    @Override
    public Result<String> getFileBase64Str(Integer id) {
        Result<String> result = new Result<>(OK, "文件预览查询成功！", "");
        ApiDocFile docFile = apiDocFileRepository.findOne(id);
        if (null == docFile || StringUtils.isBlank(docFile.getPath())) {
            result.setCode(Result.FAIL);
            result.setMsg("file or filepath not exist!");
            return result;
        }
        String filePath = docFile.getPath();
        String fileType = filePath.substring(filePath.lastIndexOf(".") + 1);
        String base64Str = "data:image/" + fileType + ";base64,";
        String imageBase64Str = getImgStr(filePath);
        if(StringUtils.isBlank(imageBase64Str)){
            result.setError(FAIL,"文件不存在！");
            return result;
        }
        result.setData(base64Str + imageBase64Str);
        return result;
    }

    @Override
    public Result<List<PublishApiInfo>> findApiInfosByCategoryOne(String environment,Integer cateGoryOneId) {
        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
        Result<List<PublishApiInfo>> result = new Result<>(Result.FAIL, "", null);
        if (cateGoryOneId == null || cateGoryOneId == 0) {
            result.setMsg("一级类目参数不合法");
            return result;
        }

        if (dataItemRepository.findOne(cateGoryOneId) == null) {
            result.setMsg("一级类目不存在");
            return result;
        }

        // 1-find api group by cateGoryOneId
        Specification<PublishApiGroup> groupSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("categoryOne").as(String.class), cateGoryOneId));
            return builder.and(andList.toArray(new Predicate[0]));
        };
        List<PublishApiGroup> apiGroups = publishApiGroupRepository.findAll(groupSpec);
        if (MiscUtil.isEmpty(apiGroups)) {
            result.setMsg("一级类目下不存在分组");
            return result;
        }

        // 2-用分组去找API
        Specification<PublishApi> specification = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<Integer> groupIn = builder.in(root.get("group").get("id"));
            apiGroups.forEach(item -> groupIn.value(item.getId()));
            andList.add(groupIn);
            andList.add(builder.equal(root.get("status").as(Integer.class),  4 ));
            andList.add(builder.equal(root.get("isOnline").as(Integer.class),  1 ));
            andList.add(builder.equal(root.get("environment").as(Integer.class), envCode));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> publishApis = publishApiRepository.findAll(specification);
        if (MiscUtil.isEmpty(publishApis)) {
            result.setMsg("一级类目下不存在API");
            return result;
        }

        List<PublishApiInfo> infos = publishApis.stream().map(item -> new PublishApiInfo(
                item.getId(),
                dataItemRepository.findOne(item.getGroup().getSystem()).getItemName(),
                item.getName(), item.getDescription())).collect(Collectors.toList());
        result.setData(infos);
        result.setMsg("");
        result.setCode(MiscUtil.isNotEmpty(infos) ? OK : FAIL);
        return result;
    }

    @Override
    public Result<Page<ProcessRecord>> findApiPublishInfos(Integer apiId, PublishApiQuery publishApiQuery) {
        Result<Page<ProcessRecord>> result = new Result<>(FAIL,"查询失败！",null);
        if(null == apiId || apiId < 1){
            result.setError(FAIL,"请输入需要查询的apiId");
            return result;
        }

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if (!CollectionUtils.isEmpty(publishApiQuery.getSort())) {
            direction = "d".equalsIgnoreCase(publishApiQuery.getSort().get(0)) ? Sort.Direction.DESC : Sort.Direction.ASC;
        }

        PageRequest pageable = PageRequest.of(
                0 != publishApiQuery.getPageNum() ? publishApiQuery.getPageNum() - 1 : 0, publishApiQuery.getPageSize(),
                Sort.by(direction, property));

        Specification<ProcessRecord> recordSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("relId").as(Integer.class), apiId));
            andList.add(builder.equal(root.get("type").as(String.class), "publish_api"));
            if (!CollectionUtils.isEmpty(publishApiQuery.getUsers())) {
                CriteriaBuilder.In<String> in = builder.in(root.get("creator"));
                for (String creator: publishApiQuery.getUsers()) {
                    in.value(creator);
                }
                andList.add(in);
            }
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };

        Page<ProcessRecord> publishRecords = processRecordRepository.findAll(recordSpec, pageable);
        if(null != publishRecords && !CollectionUtils.isEmpty(publishRecords.getContent())){
            List<ProcessRecord> results = publishRecords.getContent();
            if( "d".equalsIgnoreCase(publishApiQuery.getSort().get(0))){
                for(int i=0;i<results.size();i++){
                    results.get(i).setVersion(results.size()-i);
                }
            }else{
                for(int i=0;i<results.size();i++){
                    results.get(i).setVersion(i+1);
                }
            }
        }

        result.setCode(OK);
        result.setMsg("查询成功！");
        result.setData(publishRecords);
        return result;
    }

    /**
     * 流程中心获取流程对应的基本信息（api信息或者订阅信息）
     * @param processId
     * @return
     */
    @Override
    public Result<PublishApiBasicInfo> getApiBasicInfo(String processId) {
        PublishApiBasicInfo publishApiBasicInfo = new PublishApiBasicInfo();
        Result<PublishApiBasicInfo> result = new Result<>(OK,"查询成功",publishApiBasicInfo);
        if(StringUtils.isBlank(processId)){
            result.setError(FAIL,"ProcessId  is null!");
            return result;
        }
        //根据流程ID查询到对应的流程记录
        Specification<ProcessRecord> pcSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("processInstID").as(String.class), processId));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<ProcessRecord> processRecords=processRecordRepository.findAll(pcSpec);
        if(CollectionUtils.isEmpty(processRecords)){
            return result;
        }
        ProcessRecord pr = processRecords.get(0);
        Integer apiId = pr.getRelId();
        if("application".equals(pr.getType())){
            PublishApplication application = publishApplicationRepository.findOne(pr.getRelId());
            publishApiBasicInfo.setSubscriber(application.getCreator());
            publishApiBasicInfo.setSubscribeDesc(processRecords.get(0).getRemark());
            publishApiBasicInfo.setSubscribeSystem(dataItemRepository.findOne(application.getSystem()).getItemName());
            publishApiBasicInfo.setSubscribeTime(new SimpleDateFormat(BaseConstants.DATE_TIME_FORMAT).format(application.getCreateTime()));
            apiId = application.getPublishApi().getId();
        }
        PublishApi api = publishApiRepository.findOne(apiId);
        publishApiBasicInfo.setApiName(api.getName());
        publishApiBasicInfo.setApiGroup(api.getApiGroup().getName());
        publishApiBasicInfo.setCatageoryOne(dataItemRepository.findOne(api.getApiGroup().getCategoryOne()).getItemName());
        publishApiBasicInfo.setCatageoryTwo(dataItemRepository.findOne(api.getApiGroup().getCategoryTwo()).getItemName());
        publishApiBasicInfo.setEnv(InstanceEnvironment.fromCode(api.getEnvironment()).getName());
        publishApiBasicInfo.setPublishSystem(dataItemRepository.findOne(api.getSystemId()).getItemName());
        result.setData(publishApiBasicInfo);
        return result;
    }

    /**
     * 根据路由查询对应API信息
     * @param rule
     * @param pageable
     * @return
     */
    @Override
    public Result<Page<PublishApiRuleInfo>> findApiByRule(String rule, PageRequest pageable) {
        Result<Page<PublishApiRuleInfo>> returnResult = new Result<>();
        //判断传入的rule是url还是路由
        String pattern ;
        if(StringUtils.startsWith(rule,"http")){
            String str = rule.substring(0,rule.indexOf("?"));
            for(int i = 0; i < 3; i++){
                str = str.substring(str.indexOf("/")+1 );
            }
            pattern = "/"+str;
        }else {
            pattern=rule;
        }
        Specification<ApiMappingRule> ruleSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.like(root.get("pattern").as(String.class), "%"+pattern+"%"));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<ApiMappingRule> apiMappingRuleList =mappingRuleRepository.findAll(ruleSpec);
        if (CollectionUtils.isEmpty(apiMappingRuleList)) {
            return returnResult.setError(FAIL,"mapplingRule not exist");
        }
        //查询API
        Specification<PublishApi> apiSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<Integer> in = builder.in(root.get("id"));
            for (ApiMappingRule apiMappingRule : apiMappingRuleList) {
                in.value(apiMappingRule.getPublishApi().getId());
            }
            andList.add(in);

            andList.add(builder.equal(root.get("status").as(Integer.class), 4));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        Page<PublishApi> p = publishApiRepository.findAll(apiSpec, pageable);
        List<PublishApi> publishApis = p.getContent();
        if (CollectionUtils.isEmpty(publishApis)) {
            return returnResult.setError(FAIL,"PublishApi not exist");
        }

        List<PublishApiRuleInfo> publishApiRuleInfoList = new ArrayList<>();
        for (PublishApi publishApi : publishApis) {
            PublishApiRuleInfo publishApiRuleInfo = new PublishApiRuleInfo();
            publishApiRuleInfo.setId(publishApi.getId());
            publishApiRuleInfo.setName(publishApi.getName());
            publishApiRuleInfo.setApiMappingRules(mappingRuleService.findRuleByApi(publishApi.getId(),rule));
            String env = InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName();
            publishApiRuleInfo.setEnvironment(env);
            publishApiRuleInfo.setSystemName(systemInfoRepository.getOne(Integer.parseInt(publishApi.getGroup().getProjectId())).getName());
            publishApiRuleInfoList.add(publishApiRuleInfo);
        }

        Page<PublishApiRuleInfo> data = new PageImpl<>(publishApiRuleInfoList, pageable, p.getTotalElements());
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(data);

        return returnResult;
    }

    /**
     * for debug on scale
     * 2020/10/15
     */
    @Override
    public Result<Boolean> deletePublishApisByScaleIds(List<Long> scaleIds) {
        if (MiscUtil.isEmpty(scaleIds)) {
            return new Result<>(FAIL, "Scale id invalid", false);
        }

        List<Integer> apiIds = publishApiInstanceRelationshipRepository.findApiIdsByScaleServiceIds(scaleIds);
        if (MiscUtil.isEmpty(apiIds)) {
            return new Result<>(FAIL, "none api found", false);
        }

        log.info("Try to delete service[{}]", scaleIds);
        log.info("Try to delete api    [{}]", apiIds);
        for (Integer apiId : apiIds) {
            deletePublishApi(apiId);
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                log.error("context",e);
                Thread.currentThread().interrupt();
            }
        }
        return new Result<>(OK, "", true);
    }
}
