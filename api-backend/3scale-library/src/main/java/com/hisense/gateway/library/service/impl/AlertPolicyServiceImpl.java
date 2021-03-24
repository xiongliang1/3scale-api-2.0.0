package com.hisense.gateway.library.service.impl;

import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.config.SystemConfigProperties;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.alert.*;
import com.hisense.gateway.library.model.dto.web.AlertPolicyDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.AlertPolicyService;
import com.hisense.gateway.library.utils.CommonUtil;
import com.hisense.gateway.library.utils.TaskBroker;
import com.hisense.gateway.library.utils.api.AlertUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.concurrent.ListenableFuture;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.constant.AlertConstant.STATUS_DELETE;
import static com.hisense.gateway.library.constant.AlertConstant.STATUS_INIT;
import static com.hisense.gateway.library.constant.BaseConstants.TAG;

/**
 * @author guilai.ming 2020/09/10
 */
@Slf4j
@Service
public class AlertPolicyServiceImpl implements AlertPolicyService {
/*    @Resource
    KafkaTemplate<String, Object> kafkaTemplate;*/

    @Resource
    SystemConfigProperties configProperties;


    @Autowired
    AlertPolicyRepository alertPolicyRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    SystemInfoRepository systemInfoRepository;

    private boolean taskScheduled = false;

    private final Object taskLock = new Object();

    private ExecutorService executorService = TaskBroker.buildSingleExecutorService("AlertSync");

    @Transactional
    @Override
    public Result<Boolean> saveAlertPolicy(String projectId, Integer environment, AlertPolicyDto policyDto) {
        log.info("{}SaveAlertPolicy {}", TAG, policyDto);
        Result<Boolean> result;
        if ((result = AlertUtil.isAlertPolicyValid(policyDto)).isFailure()) {
            return result;
        }
        if (MiscUtil.isNotEmpty(alertPolicyRepository.findByProjectAndName(projectId, policyDto.getName(),environment))) {
            result.setMsg("同project下存在相同名称的策略");
            result.setCode(Result.FAIL);
            result.setData(false);
            return result;
        }

        List<String> list = new ArrayList<>();
        if(CollectionUtils.isEmpty(policyDto.getMsgReceivers())){
            Integer systemId = Integer.parseInt(projectId);
            SystemInfo systemInfo = systemInfoRepository.findOne(systemId);
            if(InstanceEnvironment.fromCode(environment).getCode()==0){  //当前为测试环境
                if(StringUtils.isEmpty(systemInfo.getApiAdminName())){
                    throw new NotExist("MsgReceivers not exit");
                }
                String adminName = systemInfo.getApiAdminName();
                if(adminName.indexOf(",")>=0){
                    list = Arrays.asList(adminName.split(","));
                }else {
                    list.add(adminName);
                }
                policyDto.setMsgReceivers(list);
            }else {
                if(StringUtils.isEmpty(systemInfo.getPrdApiAdminName())){
                    throw new NotExist("MsgReceivers not exit");
                }
                String adminName = systemInfo.getPrdApiAdminName();
                if(adminName.indexOf(",")>=0){
                    list = Arrays.asList(adminName.split(","));
                }else {
                    list.add(adminName);
                }
                policyDto.setMsgReceivers(list);
            }
        }



        AlertPolicy policy = AlertUtil.buildAlertPolicy(policyDto);
        policy.setProjectId(projectId);
        // closed for wrong scenario,should be done after bind when no receiver found
        /*if (MiscUtil.isEmpty(policy.getMsgReceivers())) {
            policy.setMsgReceivers(CommonUtil.encodeStrListWithComma(systemInfoService.findSystemAdminName(projectId)));
        }*/

        policy.setEnvironment(environment);
        AlertPolicy policyRes = alertPolicyRepository.save(policy);

        log.info("{}SaveAlertPolicy done {}", TAG, policy);

        syncAlertPolicyToKafkaAsync();

        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }

    @Transactional
    @Override
    public Result<Boolean> deleteAlertPolicies(List<Integer> policyIds) {
        Result<Boolean> result = new Result<>(Result.FAIL, "", false);
        if (MiscUtil.isEmpty(policyIds)) {
            result.setMsg("错误, 指定的Policy 列表为空");
            return result;
        }

        try {
            unbindApiForPolicies(policyIds);
            alertPolicyRepository.deleteByLogic(policyIds, new Date());
            syncAlertPolicyToKafkaAsync();
        } catch (Exception e) {
            log.error("{}Error to delete {}", TAG, policyIds);
            result.setData(true);
            result.setCode(Result.OK);
        }

        result.setData(true);
        result.setCode(Result.OK);
        return result;
    }

    private void unbindApiForPolicies(List<Integer> policyIds) {
        if (MiscUtil.isEmpty(policyIds)) {
            return;
        }

        Specification<PublishApi> apiSpecification = (Specification<PublishApi>) (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<Integer> in = builder.in(root.get("alertPolicyId"));
            policyIds.forEach(in::value);
            andList.add(in);
            return builder.and(andList.toArray(new Predicate[0]));
        };

        List<PublishApi> publishApis = publishApiRepository.findAll(apiSpecification);
        if (MiscUtil.isNotEmpty(publishApis)) {
            publishApis.forEach(item -> item.setAlertPolicyId(null));
        }
        log.info("{} try to unbind policy fot api {}", TAG,
                publishApis.stream().map(PublishApi::getName).collect(Collectors.toList()));
        publishApiRepository.saveAll(publishApis);
    }

    @Transactional
    @Override
    public Result<Boolean> updateAlertPolicy(String projectId, Integer environment, Integer id, AlertPolicyDto policyDto) {
        Result<Boolean> result;
        if ((result = AlertUtil.isAlertPolicyValid(policyDto)).isFailure()) {
            return result;
        }

        AlertPolicy policyRes = alertPolicyRepository.findOne(id);
        if (policyRes == null || policyRes.getStatus() == STATUS_DELETE) {
            result.setMsg("指定的policy不存在,或者已删除");
            return result;
        }

        List<String> list = new ArrayList<>();

        if(CollectionUtils.isEmpty(policyDto.getMsgReceivers())){
            Integer systemId = Integer.parseInt(projectId);
            SystemInfo systemInfo = systemInfoRepository.findOne(systemId);
            if(InstanceEnvironment.fromCode(environment).getCode()==0){  //当前为测试环境
                if(StringUtils.isEmpty(systemInfo.getApiAdminName())){
                    throw new NotExist("MsgReceivers not exit");
                }
                String adminName = systemInfo.getApiAdminName();
                if(adminName.indexOf(",")>=0){
                    list = Arrays.asList(adminName.split(","));
                }else {
                    list.add(adminName);
                }
                policyDto.setMsgReceivers(list);
            }else {
                if(StringUtils.isEmpty(systemInfo.getPrdApiAdminName())){
                    throw new NotExist("MsgReceivers not exit");
                }
                String adminName = systemInfo.getPrdApiAdminName();
                if(adminName.indexOf(",")>=0){
                    list = Arrays.asList(adminName.split(","));
                }else {
                    list.add(adminName);
                }
                policyDto.setMsgReceivers(list);
            }
        }

        AlertPolicy policy = AlertUtil.buildAlertPolicy(policyDto);
        policy.setProjectId(projectId);
        policy.setEnvironment(environment);
        policy.setId(id);
        policy.setCreateTime(policyRes.getCreateTime());
        policy.setApiIds(policyRes.getApiIds());//编辑的时候需要把之前绑定的api信息关联进去-liyouzhi.ex2020/11/11
        log.info("{}UpdateAlertPolicy={}", TAG, policy);
        alertPolicyRepository.saveAndFlush(policy);

        syncAlertPolicyToKafkaAsync();

        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }

    @Transactional
    @Override
    public Result<Boolean> bindToPublishApi(Integer policyId,
                                            List<Integer> bindApiIds,
                                            List<Integer> unBindApiIds) {
        Result<Boolean> result = new Result<>(Result.FAIL, "", false);
        if (MiscUtil.isEmpty(bindApiIds) && MiscUtil.isEmpty(unBindApiIds)) {
            result.setMsg("错误, 未选定任何API");
            return result;
        }

        AlertPolicy policyRes = alertPolicyRepository.findOne(policyId);
        if (policyRes == null || policyRes.getStatus() == STATUS_DELETE) {
            result.setMsg("指定的policy不存在,或者已删除");
            return result;
        }

        // unbind
        if (MiscUtil.isNotEmpty(unBindApiIds)) {
            List<Integer> oldBindApiIds = CommonUtil.decodeIntListWithComma(policyRes.getApiIds());
            if (MiscUtil.isNotEmpty(oldBindApiIds)) {
                oldBindApiIds.removeIf(item -> !unBindApiIds.contains(item));
                log.info("{} try to unbind {} from policy", TAG, oldBindApiIds);
                for (Integer apiId : oldBindApiIds) {
                    PublishApi publishApi = publishApiRepository.findOne(apiId);
                    if (publishApi == null) {
                        continue;
                    }

                    publishApi.setAlertPolicyId(null);
                    publishApiRepository.saveAndFlush(publishApi);
                }
            }
        }

        // bind
        if (MiscUtil.isNotEmpty(bindApiIds)) {
            log.info("{} try to bind policy to apis: {}", TAG, bindApiIds);
            for (Integer apiId : bindApiIds) {
                PublishApi publishApi = publishApiRepository.findOne(apiId);
                if (publishApi == null) {
                    continue;
                }

                publishApi.setAlertPolicyId(policyId);
                publishApiRepository.saveAndFlush(publishApi);
            }
        }

        // update api ids
        policyRes.setApiIds(CommonUtil.encodeIntListWithComma(bindApiIds));
        alertPolicyRepository.saveAndFlush(policyRes);

        syncAlertPolicyToKafkaAsync();

        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }

    @Transactional
    @Override
    public Result<Boolean> enableAlertPolicy(Integer policyId, boolean enable) {
        Result<Boolean> result = new Result<>(Result.FAIL, "", false);

        AlertPolicy policyRes = alertPolicyRepository.findOne(policyId);
        if (policyRes == null || policyRes.getStatus() == STATUS_DELETE) {
            result.setMsg("指定的policy不存在,或者已删除");
            return result;
        }

        policyRes.setEnable(enable ? 1 : 0);
        log.info("{}EnableAlertPolicy 1 {} {}", TAG, enable, policyRes);
        alertPolicyRepository.saveAndFlush(policyRes);

        syncAlertPolicyToKafkaAsync();

        result.setCode(Result.OK);
        result.setData(true);
        return result;
    }

    @Override
    public Page<AlertPolicyDto> findAlertPoliciesByPage(String projectId, Integer environment,
                                                        AlertPolicyQuery policyQuery) {
        log.info("{}FindAlertPoliciesByPage {}", TAG, policyQuery);
        Specification<AlertPolicy> specification = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (projectId != null) {
                andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
            }

            if (environment != null) {
                andList.add(builder.equal(root.get("environment").as(Integer.class), environment));
            }

            andList.add(builder.equal(root.get("status").as(Integer.class), STATUS_INIT));

            if (StringUtils.isNotBlank(policyQuery.getName())) {
                andList.add(builder.like(root.get("name").as(String.class), "%" + policyQuery.getName() + "%",
                        GatewayConstants.ESCAPECHAR));
                log.info("{}FindAlertPoliciesByPage add name like", TAG);
            }

            if (MiscUtil.isNotEmpty(policyQuery.getStatusList())) {
                CriteriaBuilder.In<Integer> in = builder.in(root.get("enable"));
                policyQuery.getStatusList().forEach(in::value);
                andList.add(in);
            }

            TimeQuery timeQuery = policyQuery.getTimeQuery();
            if (timeQuery != null) {
                if (timeQuery.getStart() != null && timeQuery.getEnd() != null) {
                    andList.add(builder.between(root.get("createTime").as(Date.class), timeQuery.getStart(),
                            timeQuery.getEnd()));
                } else if (timeQuery.getStart() != null) {
                    andList.add(builder.greaterThanOrEqualTo(root.get("createTime").as(Date.class),
                            timeQuery.getStart()));
                } else if (timeQuery.getEnd() != null) {
                    andList.add(builder.lessThanOrEqualTo(root.get("createTime").as(Date.class), timeQuery.getEnd()));
                }
                log.info("{}findByPage add between TimeQuery", TAG);
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };

        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";//status
        if (policyQuery.getSort() != null && policyQuery.getSort().size() > 1) {
            direction = "d".equalsIgnoreCase(policyQuery.getSort().get(0)) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = policyQuery.getSort().get(1);
        }

        PageRequest pageable = PageRequest.of(
                0 != policyQuery.getPage() ? policyQuery.getPage() - 1 : 0, policyQuery.getSize(),
                Sort.by(direction, property));

        Page<AlertPolicy> p = alertPolicyRepository.findAll(specification, pageable);
        List<AlertPolicy> policies = p.getContent();
        List<AlertPolicyDto> list = new ArrayList<>();
        if (MiscUtil.isNotEmpty(policies)) {
            list.addAll(policies.stream().map(item -> buildAlertPolicyDto(item, true)).collect(Collectors.toList()));
        }

        return new PageImpl<>(list, pageable, p.getTotalElements());
    }

    @Override
    public List<AlertApiInfo> findBindUnBindApiList(Integer policyId, String tenantId, String projectId,
                                                    Integer environment,
                                                    AlertApiInfoQuery apiInfoQuery) {
        log.info("{}FindBindApiList {}", TAG, apiInfoQuery);
        Set<Integer> systemIdSet = null;
        List<PublishApiGroup> defaultPublishApiGroups = null;
        List<PublishApiGroup> specialPublishApiGroups = null;
        // 1-查询分组\系统
        if (MiscUtil.isNotEmpty(apiInfoQuery.getGroupIds())) {
            Specification<PublishApiGroup> groupSpec = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();

                if (tenantId != null) {
                    andList.add(builder.equal(root.get("tenantId").as(String.class), tenantId));
                }

                if (projectId != null) {
                    andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
                }

                if (MiscUtil.isNotEmpty(apiInfoQuery.getGroupIds())) {
                    CriteriaBuilder.In<Integer> in = builder.in(root.get("id"));
                    for (Integer groupId : apiInfoQuery.getGroupIds()) {
                        in.value(groupId);
                    }
                    andList.add(in);
                }

                andList.add(builder.equal(root.get("status").as(Integer.class), 1));
                return builder.and(andList.toArray(new Predicate[0]));
            };

            specialPublishApiGroups = publishApiGroupRepository.findAll(groupSpec);
            if (CollectionUtils.isEmpty(specialPublishApiGroups)) {
                log.error("{}api group not exist", TAG);
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

        // 2-查询API
        Set<Integer> finalSystemIdSet = systemIdSet;
        List<PublishApiGroup> finalDefaultPublishApiGroups = defaultPublishApiGroups;
        List<PublishApiGroup> finalSpecialPublishApiGroups = specialPublishApiGroups;

        log.info("{}FindBindApiList SystemIdSet={}", TAG, finalSystemIdSet != null ? finalSystemIdSet : 0);
        log.info("{}FindBindApiList DefaultPublishApiGroups={}", TAG, finalDefaultPublishApiGroups != null ?
                finalDefaultPublishApiGroups.stream().map(item -> String.format("[%d]: %s", item.getId(),
                        item.getName())).collect(Collectors.toList()) : 0);
        log.info("{}FindBindApiList SpecialPublishApiGroups={}", TAG, finalSpecialPublishApiGroups != null ?
                finalSpecialPublishApiGroups.stream().map(item -> String.format("[%d]: %s", item.getId(),
                        item.getName())).collect(Collectors.toList()) : 0);

        // 全局条件
        Specification<PublishApi> publishApiSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();

            // api name
            if (StringUtils.isNotBlank(apiInfoQuery.getName())) {
                andList.add(builder.like(root.get("name").as(String.class), "%" + apiInfoQuery.getName() + "%",
                        GatewayConstants.ESCAPECHAR));
                log.info("{}FindBindApiList add name like", TAG);
            }

            // partition
            if (MiscUtil.isNotEmpty(apiInfoQuery.getPartitions())) {
                if (MiscUtil.isNotEmpty(apiInfoQuery.getPartitions())) {
                    CriteriaBuilder.In<Integer> partitionIn = builder.in(root.get("partition"));
                    for (Integer groupId : apiInfoQuery.getPartitions()) {
                        partitionIn.value(groupId);
                    }
                    andList.add(partitionIn);
                }
            }

            // group
            List<Integer> groupIds = new ArrayList<>();
            andList.add(builder.equal(root.get("environment").as(Integer.class), environment));
            andList.add(builder.equal(root.get("status").as(Integer.class), 4));//只查询发布成功的api
            Predicate orPredicate = null;
            if (MiscUtil.isNotEmpty(finalSpecialPublishApiGroups)) {
                CriteriaBuilder.In<PublishApiGroup> groupIn1 = builder.in(root.get("group"));
                for (PublishApiGroup pg : finalSpecialPublishApiGroups) {
                    groupIn1.value(pg);
                    groupIds.add(pg.getId());
                }
                andList.add(groupIn1);
                log.info("{}FindBindApiList add IN finalSpecialPublishApiGroups {}", TAG, groupIds);
            } else {
                CriteriaBuilder.In<Integer> groupIn = null;
                CriteriaBuilder.In<Integer> systemIn = null;

                if (MiscUtil.isNotEmpty(finalDefaultPublishApiGroups)) {
                    groupIn = builder.in(root.get("group").get("id"));
                    for (PublishApiGroup pg : finalDefaultPublishApiGroups) {
                        groupIn.value(pg.getId());
                        groupIds.add(pg.getId());
                    }
                    log.info("{}FindBindApiList add IN finalDefaultPublishApiGroups {}", TAG, groupIds);
                }

                if (MiscUtil.isNotEmpty(finalSystemIdSet)) {
                    systemIn = builder.in(root/*.get("eurekaService")*/.get("systemId"));
                    for (Integer system : finalSystemIdSet) {
                        systemIn.value(system);
                    }

                    log.info("{}FindBindApiList add IN systemAndList", TAG);
                }

                if (groupIn != null && systemIn != null) {
                    orPredicate = builder.or(groupIn, systemIn);
                } else if (groupIn != null) {
                    andList.add(groupIn);
                } else if (systemIn != null) {
                    andList.add(systemIn);
                }
            }

            if (orPredicate != null) {
                return query.where(builder.and(andList.toArray(new Predicate[0])), orPredicate).getRestriction();
            } else {
                return builder.and(andList.toArray(new Predicate[0]));
            }
        };

        List<PublishApi> publishApis = publishApiRepository.findAll(publishApiSpec);
        if (MiscUtil.isNotEmpty(publishApis)) {
            publishApis.removeIf(item ->
                    (apiInfoQuery.isBind() ?
                            (item.getAlertPolicyId() == null || !item.getAlertPolicyId().equals(policyId)) :
                            (item.getAlertPolicyId() != null)));
        }

        log.info("{}FindBindApiList publishApis.size={}", TAG, publishApis.size());

        return publishApis.stream().map(item -> new AlertApiInfo(item.getId(), item.getName(),
                item.getGroup() != null ? item.getGroup().getName() : "hicloud", item.getPartition(),item.isNeedLogging())).collect(Collectors.toList());
    }

    @Override
    public Result<AlertPolicyDto> getAlertPolicy(Integer policyId) {
        Result<AlertPolicyDto> result = new Result<>(Result.FAIL, "", null);

        AlertPolicy policyRes = alertPolicyRepository.findOne(policyId);
        if (policyRes == null || policyRes.getStatus() == STATUS_DELETE) {
            result.setMsg("指定的policy不存在,或者已删除");
            return result;
        }

        result.setData(buildAlertPolicyDto(policyRes, true));
        result.setCode(Result.OK);
        return result;
    }

    private AlertPolicyDto buildAlertPolicyDto(AlertPolicy policy, boolean detail) {
        AlertPolicyDto dto = new AlertPolicyDto();
        dto.setId(policy.getId());
        dto.setName(policy.getName());

        dto.setCreateTime(policy.getCreateTime());
        if (MiscUtil.isNotEmpty(policy.getApiIds())) {
            List<Integer> apiIds = CommonUtil.decodeIntListWithComma(policy.getApiIds());
            if (MiscUtil.isNotEmpty(apiIds)) {
                List<AlertApiInfo> apiInfos = publishApiRepository.findByApiIds(apiIds);
                if (MiscUtil.isNotEmpty(apiInfos)) {
                    List<String> apiNames = apiInfos.stream().map(AlertApiInfo::getName).collect(Collectors.toList());
                    dto.setBindApis(CommonUtil.encodeStrListWithComma(apiNames));
                }
            }
        }
        dto.setMsgSendTypes(CommonUtil.decodeIntListWithComma(policy.getMsgSendTypes()));
        dto.setStatus(policy.getStatus());
        dto.setEnable(policy.getEnable() == 1);

        if (detail) {
            dto.setCreator(policy.getCreator());
            if (MiscUtil.isNotEmpty(policy.getTriggerMethods())) {
                dto.setTriggerMethods(MiscUtil.fromJson(policy.getTriggerMethods(), AlertPolicyDto.class).getTriggerMethods());
            }

            dto.setMsgReceivers(CommonUtil.decodeStrListWithComma(policy.getMsgReceivers()));
            dto.setMsgSendInterval(policy.getMsgSendInterval());
        }

        return dto;
    }

    @Override
    public String syncAlertPolicyToKafka() {
        log.info("{}SyncAlertPolicyToKafka {}", TAG, configProperties);

        StringBuilder sb = new StringBuilder();
        Specification<AlertPolicy> specification = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("status").as(Integer.class), STATUS_INIT));
            andList.add(builder.equal(root.get("enable").as(Integer.class), 1));
            andList.add(builder.isNotNull(root.get("apiIds").as(String.class)));
            return builder.and(andList.toArray(new Predicate[0]));
        };

        List<AlertPolicy> policies = null;
        try {
            policies = alertPolicyRepository.findAll(specification);
        } catch (Exception e) {
            log.error("告警策略数据同步-查询策略数据异常！",e);
            return null;
        }

        log.info("{} SyncAlertPolicyToKafka policies {}", TAG, policies.size());
        if (MiscUtil.isEmpty(policies)) {
            sb.append("No alert policy fond,exit now");
            return sb.toString();
        }

        List<AlertSyncRecord> syncRecords = new ArrayList<>();
        for (AlertPolicy policy : policies) {
            List<Integer> apiIds = CommonUtil.decodeIntListWithComma(policy.getApiIds());
            if (MiscUtil.isEmpty(apiIds)) {
                continue;
            }

            List<AlertTriggerMethod> alertTriggerMethods = MiscUtil.fromJson(policy.getTriggerMethods(),
                    AlertPolicyDto.class).getTriggerMethods();
            if (MiscUtil.isEmpty(alertTriggerMethods)) {
                continue;
            }

            for (Integer apiId : apiIds) {
                syncRecords.addAll(findSubscribeSystems(policy, apiId, alertTriggerMethods));
            }
        }

        String message = String.format("{\"alertPolicies\":%s}", MiscUtil.toJson(syncRecords));
        log.info("{}syncAlertPolicyToKafka={}", TAG, message);

        if (configProperties.getAlert().isKafkaEnable()) {
            String topic = configProperties.getAlert().getKafkaTopic();
            try {
     /*           ListenableFuture<SendResult<String, Object>> send =
                        kafkaTemplate.send(MiscUtil.isNotEmpty(topic) ? topic : "alertPolicySync", message);
                log.info(send.toString());*/
            } catch (Exception e) {
                log.error("告警策略数据同步-数据推送异常！",e);
                log.error("{} fail to send to kafka", TAG);
            }
        }

        sb.append(String.format("%s records", syncRecords.size()));
        return sb.toString();
    }

    private Collection<AlertSyncRecord> findSubscribeSystems(AlertPolicy policy, Integer apiId,
                                                             List<AlertTriggerMethod> alertTriggerMethods) {
        Map<String, AlertSyncRecord> alertSyncRecordMap = new HashMap<>();

        PublishApi publishApi = publishApiRepository.findOne(apiId);
        if (publishApi == null) {
            log.error("{}api {} not exits", TAG, apiId);
            return alertSyncRecordMap.values();
        }

        List<PublishApplication> subscribedAppsForApi =
                publishApplicationRepository.findSubscribedAppByApiId(apiId);
        if (MiscUtil.isEmpty(subscribedAppsForApi)) {
            log.error("{}api {} had not been subscribed", TAG, apiId);
            return alertSyncRecordMap.values();
        }

        log.info("{} subscribedAppsForApi {}", TAG, subscribedAppsForApi.size());
        DataItem publishSystemItem = dataItemRepository.findOne(publishApi.getSystemId());
        for (PublishApplication pa : subscribedAppsForApi) {
            if (pa.getSystem() == null || pa.getUserKey() == null) {
                log.error("pa {} has invalid system", pa);
                continue;
            }

            if (pa.getInstance() == null || pa.getInstance().getId() == null) {
                log.error("pa {} has invalid instance", pa);
                continue;
            }

            DataItem item = dataItemRepository.findOne(pa.getSystem());
            if (item == null) {
                log.error("pa {} has no found system", pa);
                continue;
            }

            PublishApiInstanceRelationship relationship =
                    publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(apiId, pa.getInstance().getId());

            if (relationship == null) {
                log.error("pa {} has no found valid instance", pa);
                continue;
            }

            String key = String.format("%s.%s.%s", pa.getInstance().getId(), relationship.getScaleApiId(),
                    pa.getUserKey());

            AlertSyncRecord syncRecord;
            log.info("{} key {}", TAG, key);
            if ((syncRecord = alertSyncRecordMap.get(key)) == null) {
                syncRecord = new AlertSyncRecord();
                alertSyncRecordMap.put(key, syncRecord);
                syncRecord.setApiPublishSystem(publishSystemItem==null?"":publishSystemItem.getItemName());
                syncRecord.setApiSubscribeSystem(item.getItemName());
                syncRecord.setInstanceId(pa.getInstance().getId());
                syncRecord.setScaleServiceId(relationship.getScaleApiId());
                syncRecord.setSubscribeSystemUserKey(pa.getUserKey());
                syncRecord.setMsgSendTypes(CommonUtil.decodeIntListWithComma(policy.getMsgSendTypes()));
                String users = String.format("%s,%s", policy.getMsgReceivers(), pa.getCreator());
                Set<String> userList = MiscUtil.list2Set(CommonUtil.decodeStrListWithComma(users));
                syncRecord.setMsgReceivers(CommonUtil.encodeStrListWithComma(MiscUtil.set2List(userList)));
                syncRecord.setPolicyName(policy.getName());
                if (policy.getUpdateTime() != policy.getCreateTime()) {
                    syncRecord.setPolicyUpdateTime(policy.getUpdateTime());
                } else {
                    syncRecord.setPolicyCreateTime(policy.getCreateTime());
                }
                syncRecord.setMsgSendInterval(policy.getMsgSendInterval());

                List<AlertSyncRecord.TriggerMethod> triggerMethods = new ArrayList<>();
                for (AlertTriggerMethod alertTriggerMethod : alertTriggerMethods) {
                    triggerMethods.add(new AlertSyncRecord.TriggerMethod(alertTriggerMethod));
                }
                syncRecord.setTriggerMethods(triggerMethods);
            } else {
                // 同个系统 + api, 的所有订阅者PA, 需取出订阅者字段, 并增加到告警同步记录中
                String users = String.format("%s,%s", syncRecord.getMsgReceivers(), pa.getCreator());
                Set<String> userList = MiscUtil.list2Set(CommonUtil.decodeStrListWithComma(users));
                syncRecord.setMsgReceivers(CommonUtil.encodeStrListWithComma(MiscUtil.set2List(userList)));
            }
        }
        return alertSyncRecordMap.values();
    }

    public void syncAlertPolicyToKafkaAsync() {
        synchronized (taskLock) {
            if (taskScheduled && configProperties.getAlert().isSerialSync()) {
                log.info("{}SerialSync enable, Sync task had been triggered", TAG);
                return;
            }
        }

        executorService.submit(() -> {
            log.info("{}Append sync task to queue", TAG);
            synchronized (taskLock) {
                taskScheduled = true;
            }

            log.info("Start to async alertPolicy to kafka");
            syncAlertPolicyToKafka();
            synchronized (taskLock) {
                taskScheduled = false;
                log.info("End to async alertPolicy to kafka");
            }
        });
    }
}
