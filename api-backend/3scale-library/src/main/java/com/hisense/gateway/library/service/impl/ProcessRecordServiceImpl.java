/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/3
 */
package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.*;
import com.hisense.gateway.library.exception.BadRequest;
import com.hisense.gateway.library.exception.BadScaleRequest;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.portal.PublishApiBasicInfo;
import com.hisense.gateway.library.model.dto.buz.ApiInstanceDto;
import com.hisense.gateway.library.model.dto.buz.ProcessDataDto;
import com.hisense.gateway.library.model.dto.portal.PortalProcessRecordQuery;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.ApiInvokeRecordService;
import com.hisense.gateway.library.service.MailService;
import com.hisense.gateway.library.service.ProcessRecordService;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.ServiceStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.web.form.ApplicationSearchForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.hisense.gateway.library.model.ModelConstant.*;
import static com.hisense.gateway.library.model.Result.FAIL;
import static com.hisense.gateway.library.model.Result.OK;

@Slf4j
@Service
public class ProcessRecordServiceImpl implements ProcessRecordService {
    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    ProcessRecordRepository processRecordRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Resource
    PublishApiInstanceRelationshipRepository pairRepository;


    @Autowired
    ServiceStud serviceStud;

    @Autowired
    AccountStud accountStud;

    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;

    @Autowired
    PublishApiTemporaryDataRepository publishApiTemporaryDataRepository;

    @Resource
    InstanceRepository instanceRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    MailService mailService;

    @Autowired
    AlertPolicyServiceImpl alertPolicyService;

    @Autowired
    PublishApiServiceImpl publishApiService;

    @Autowired
    ApiInvokeRecordService apiInvokeRecordService;

    @Override
    public Page<ProcessRecordDto> findApiApplyList(String tenantId, String projectId, int isApproved,
                                                   String name, String type, PageRequest pageable,String environment) {
        List<ProcessRecordDto> processRecordDtoList = new ArrayList<>();
        int envCode = InstanceEnvironment.fromCode(environment).getCode();
        Page<ProcessRecordDto> data = new PageImpl<>(processRecordDtoList, pageable, 0);
        Specification<PublishApiGroup> groupSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.like(root.get("tenantId").as(String.class), tenantId));
            andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
            andList.add(builder.equal(root.get("environment").as(Integer.class), envCode));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };

        List<PublishApiGroup> groupList = publishApiGroupRepository.findAll(groupSpec);
        if (CollectionUtils.isEmpty(groupList)) {
            log.error("PublishApiGroup is null!");
            return data;
        }

        Specification<PublishApi> spec2 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (StringUtils.isNotBlank(name) && !"null".equals(name)) {
                andList.add(builder.like(root.get("name").as(String.class), "%" + name + "%"));
            }
            if (!CollectionUtils.isEmpty(groupList)) {
                CriteriaBuilder.In<PublishApiGroup> in = builder.in(root.get("group"));
                for (PublishApiGroup pg : groupList) {
                    in.value(pg);
                }
                andList.add(in);
            }
            andList.add(builder.equal(root.get("environment").as(Integer.class), envCode));
            andList.add(builder.notEqual(root.get("status").as(Integer.class), API_DELETE));//不显示已删除API的数据-liyouzhi.ex
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> publishApiList = publishApiRepository.findAll(spec2);
        if (CollectionUtils.isEmpty(publishApiList)) {
            return data;
        }
        Specification<ProcessRecord> spec3 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (!CollectionUtils.isEmpty(publishApiList)) {
                CriteriaBuilder.In<Integer> in = builder.in(root.get("relId"));
                for (PublishApi publishApi : publishApiList) {
                    in.value(publishApi.getId());
                }
                andList.add(in);
            }
            if(isApproved==0){
                andList.add(builder.equal(root.get("status").as(Integer.class), 1));
            }else{
                andList.add(builder.notEqual(root.get("status").as(Integer.class), 1));
            }
            andList.add(builder.equal(root.get("type").as(String.class), type));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        Page<ProcessRecord> p = processRecordRepository.findAll(spec3, pageable);
        List<ProcessRecord> processRecords = p.getContent();
        if (processRecords == null || processRecords.size() == 0) {
            return data;
        }

        PublishApi publishApi;
        for (ProcessRecord processRecord : processRecords) {
            ProcessRecordDto processRecordDto = new ProcessRecordDto(processRecord);
            publishApi = publishApiRepository.findOne(processRecord.getRelId());
            ProcessDataDto processDataDto = JSONObject.parseObject(processRecordDto.getExtVar(), ProcessDataDto.class);
            if (processDataDto != null) {
                DataItem dataItem = dataItemRepository.findOne(publishApi.getSystemId());
                processRecordDto.setApiSystemName(dataItem.getItemName());
            }
            processRecordDto.setApiName(publishApi.getName());
            processRecordDto.setApiSystemName(dataItemRepository.findOne(publishApi.getSystemId()).getItemName());
            processRecordDto.setProcessInstID(processRecord.getProcessInstID());
            processRecordDto.setCreateTime(processRecord.getCreateTime());
            processRecordDto.setUpdateTime(processRecord.getUpdateTime());
            processRecordDtoList.add(processRecordDto);
        }
        data = new PageImpl<>(processRecordDtoList, pageable, p.getTotalElements());
        return data;
    }

    @Override
    public ProcessRecordDto findApiApplyDetail(Integer id) {
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        if (processRecord == null) {
            throw new NotExist("processRecord not exist");
        }

        ProxyConfigDto proxyConfigDto = JSONObject.parseObject(processRecord.getData(), ProxyConfigDto.class);
        ProcessRecordDto processRecordDto = new ProcessRecordDto(processRecord);
        ProcessDataDto processDataDto = JSONObject.parseObject(processRecordDto.getExtVar(), ProcessDataDto.class);
        if (processDataDto.getGroupId() != null) {
            PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(processDataDto.getGroupId());
            processRecordDto.setGroupName(publishApiGroup.getName());
        }

        processRecordDto.setApiName(proxyConfigDto.getProxyConfig().getContent().getName());
        PublishApiDto publishApiDto = new PublishApiDto();
        List<String> partitions = new ArrayList<>();
        String extVar = processRecord.getExtVar();
        JSONObject extVarJson = JSONObject.parseObject(extVar);
        String clusterPartitions = extVarJson.getString("clusterPartitions");
        List<ApiInstanceDto> apiInstanceDtos =
                publishApiInstanceRelationshipRepository.getApiInstanceDtos(processRecord.getRelId());
        if (null != apiInstanceDtos && apiInstanceDtos.size() > 0) {
            for (ApiInstanceDto apiInstanceDto : apiInstanceDtos) {
                partitions.add(apiInstanceDto.getClusterPartition());
            }
            publishApiDto.setRequestProduction(apiInstanceDtos.get(0).getRequestSandbox());//TODO
        } else {
            if (StringUtils.isNotBlank(clusterPartitions)) {
                partitions.addAll(JSONArray.parseArray(clusterPartitions, String.class));
            }
        }

        publishApiDto.setPartitions(partitions);
        publishApiDto.setName(proxyConfigDto.getProxyConfig().getContent().getName());
        publishApiDto.setAuthType(ModelConstant.API_AUTH);
        if(processDataDto != null){
            publishApiDto.setUrl(processDataDto.getUrl());
        }
        publishApiDto.setRealmName(proxyConfigDto.getProxyConfig().getContent().getProxy().getHostnameRewrite());

        String endPoint = proxyConfigDto.getProxyConfig().getContent().getProxy().getEndpoint();
        publishApiDto.setAccessProtocol(endPoint.substring(0, endPoint.indexOf("://")));
        for (ProxyPolicy proxyPolicy : proxyConfigDto.getProxyConfig().getContent().getProxy().getPolicyChain()) {
            if (ModelConstant.API_ANONYMOUS_POLICY_NAME.equals(proxyPolicy.getName())) {
                publishApiDto.setAuthType(ModelConstant.API_NOAUTH);
            }
        }

        publishApiDto.setApiMappingRuleDtos(parseMappingRule(proxyConfigDto.getProxyConfig().
                getContent().getProxy().getProxyRules(), processDataDto.getUrl()));
        publishApiDto.setProxy(proxyConfigDto.getProxyConfig().getContent().getProxy());
        processRecordDto.setPublishApiDto(publishApiDto);
        return processRecordDto;
    }

    @Override
    public ProcessRecordDto findApplicationApplyDetail(Integer id) {
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        if (processRecord == null) {
            throw new NotExist("processRecord not exist");
        }
        processRecord.setRemark(processRecord.getRemark());
        PublishApplication publishApplication = publishApplicationRepository.getOne(processRecord.getRelId());
        ProcessRecordDto processRecordDto = new ProcessRecordDto(processRecord);
        ProcessDataDto processDataDto = JSONObject.parseObject(processRecord.getExtVar(), ProcessDataDto.class);
        if (null != processDataDto) {
            DataItem dataItem = dataItemRepository.findOne(processDataDto.getAppSystem());//TODO:appSystem
            if (dataItem != null) {
                processRecordDto.setAppSystemName(dataItem.getItemName());
            }
            PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(processDataDto.getGroupId());
            processRecordDto.setGroupName(publishApiGroup.getName());
            processRecordDto.setApiSystemName(dataItemRepository.findOne(processDataDto.getApiSystem()).getItemName());
            processRecordDto.setCategoryOneName(dataItemRepository.findOne(publishApiGroup.getCategoryOne()).getItemName());
            processRecordDto.setCategoryTwoName(dataItemRepository.findOne(publishApiGroup.getCategoryTwo()).getItemName());
        }

        processRecordDto.setApiName(publishApplication.getPublishApi().getName());
        PublishApi publishApi = publishApplication.getPublishApi();
        if(!publishApi.isNeedSubscribe()){
            processRecordDto.setUpdateTime(processRecordDto.getCreateTime());
        }
        return processRecordDto;
    }

    @Override
    @Transactional
    public Result<Boolean> approveApplication(ProcessRecordDto processRecordDto) {
        Result<Boolean> rlt = new Result<>();
        approve(processRecordDto);
        return rlt;
    }

    /**
     * 查询 待办的 订阅(group\api\app\pr)
     * @param applicationSearchForm
     * @param pageable
     * @return
     */
    @Override
    public Page<ProcessRecordDto> findApplicationApplyList(ApplicationSearchForm applicationSearchForm,
                                                           PageRequest pageable) {
        // find group
        Specification<PublishApiGroup> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (!StringUtils.isEmpty(applicationSearchForm.getTenantId())) {
                andList.add(builder.equal(root.get("tenantId").as(String.class),
                        applicationSearchForm.getTenantId()));
            }

            if (!StringUtils.isEmpty(applicationSearchForm.getProjectId())) {
                andList.add(builder.equal(root.get("projectId").as(String.class),
                        applicationSearchForm.getProjectId()));
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAll(spec);
        if (CollectionUtils.isEmpty(publishApiGroups)) {
            throw new NotExist("api group not exist");
        }

        // find api
        Specification<PublishApi> spec2 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (!CollectionUtils.isEmpty(publishApiGroups)) {
                CriteriaBuilder.In<PublishApiGroup> in = builder.in(root.get("group"));
                for (PublishApiGroup pg : publishApiGroups) {
                    in.value(pg);
                }
                andList.add(in);
            }

            if (!StringUtils.isEmpty(applicationSearchForm.getName())) {
                andList.add(builder.like(root.get("name").as(String.class),
                        "%" + applicationSearchForm.getName() + "%", GatewayConstants.ESCAPECHAR));
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> apis = publishApiRepository.findAll(spec2);
        if (CollectionUtils.isEmpty(apis)) {
            throw new NotExist("api not exist");
        }

        // find app
        Specification<PublishApplication> spec3 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (!CollectionUtils.isEmpty(apis)) {
                CriteriaBuilder.In<PublishApi> in = builder.in(root.get("publishApi"));
                for (PublishApi api : apis) {
                    in.value(api);
                }
                andList.add(in);
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApplication> apps = publishApplicationRepository.findAll(spec3);
        if (CollectionUtils.isEmpty(apps)) {
            throw new NotExist("app not exist");
        }

        // 未指定system时,仅查relId
        // 否则,group->api->app->pr(system)
        Page<ProcessRecord> p = null;
        if (null != applicationSearchForm.getSystem()) {
            List<ProcessRecord> temp = processRecordRepository.findApplyApplication(
                    applicationSearchForm.getSystem());

            if (CollectionUtils.isEmpty(temp)) {
                throw new NotExist("processrecord not exist");
            }

            Specification<ProcessRecord> spec4 = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                CriteriaBuilder.In<Integer> in = builder.in(root.get("relId"));
                for (PublishApplication app : apps) {
                    in.value(app.getId());
                }
                andList.add(in);

                CriteriaBuilder.In<Integer> idin = builder.in(root.get("id"));
                for (ProcessRecord pro : temp) {
                    idin.value(pro.getId());
                }
                andList.add(idin);

                andList.add(builder.equal(root.get("status").as(Integer.class), 1));
                andList.add(builder.equal(root.get("type").as(String.class), "application"));
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };

            p = processRecordRepository.findAll(spec4, pageable);
        } else {
            Specification<ProcessRecord> spec4 = (root, query, builder) -> {
                List<Predicate> andList = new LinkedList<>();
                CriteriaBuilder.In<Integer> in = builder.in(root.get("relId"));
                for (PublishApplication app : apps) {
                    in.value(app.getId());
                }
                andList.add(in);

                andList.add(builder.equal(root.get("status").as(Integer.class), 1));
                andList.add(builder.equal(root.get("type").as(String.class), "application"));
                return builder.and(andList.toArray(new Predicate[andList.size()]));
            };
            p = processRecordRepository.findAll(spec4, pageable);
        }

        if (CollectionUtils.isEmpty(p.getContent())) {
            throw new NotExist("app process not exist");
        }

        PublishApi publishApi = null;
        PublishApplication app = null;
        List<ProcessRecordDto> processRecordDtoList = new ArrayList<>();

        for (ProcessRecord processRecord : p.getContent()) {
            ProcessRecordDto processRecordDto = new ProcessRecordDto(processRecord);
            app = publishApplicationRepository.findOne(processRecord.getRelId());
            publishApi = app.getPublishApi();
            DataItem dataItem = dataItemRepository.findOne(publishApi.getApiGroup().getSystem());
            processRecordDto.setApiSystemName(dataItem.getItemName());
            processRecordDto.setApiName(publishApi.getName());

            if (!StringUtils.isEmpty(processRecordDto.getExtVar())) {
                ProcessDataDto processDataDto =
                        JSON.parseObject(processRecordDto.getExtVar(), ProcessDataDto.class);
                if (null != processDataDto && null != processDataDto.getAppSystem()) {
                    DataItem dataItem2 = dataItemRepository.findOne(processDataDto.getAppSystem());
                    processRecordDto.setAppSystemName(null == dataItem2 ? "" : dataItem2.getItemName());
                }
            }

            processRecordDto.setPublishApiDto(new PublishApiDto(publishApi));
            processRecordDtoList.add(processRecordDto);
        }
        Page<ProcessRecordDto> data = new PageImpl<>(processRecordDtoList, pageable, p.getTotalElements());
        return data;
    }

    @Override
    public Page<ProcessRecordDto> findApplicationApplyList(String tenantId, String projectId,String environment, ProcessRecordQuery processRecordQuery) {
        int envCode = InstanceEnvironment.fromCode(environment).getCode();
        String[] sort = processRecordQuery.getSort();
        StringBuilder querySql = new StringBuilder(
                "SELECT\n" +
                        "\tpr.id,\n" +
                        "\tpr.ext_var,\n" +
                        "\tto_char(pr.create_time,'yyyy-mm-dd hh24:mi:ss') create_time,\n" +
                        "\tpr.status,\n" +
                        "\tpr.rel_id,\n" +
                        "\tpr.creator,\n" +
                        "\tpa.id AS api_id,\n" +
                        "\tpa.NAME AS api_name\n" +
                        "FROM ");
        StringBuilder countSql = new StringBuilder("SELECT count(1) FROM ");
        StringBuilder whereSql = new StringBuilder(
                " PROCESS_RECORD pr\n" +
                        "\tINNER JOIN PUBLISH_APPLICATION app ON pr.rel_id = app.id\n" +
                        "\tINNER JOIN PUBLISH_API pa ON app.api_id = pa.id\n" +
                        "\tINNER JOIN PUBLISH_API_GROUP pag ON pa.group_id = pag.id\n" +
                        " WHERE 1=1 AND pr.STATUS = 1 AND pr.type = 'application' AND pa.environment ="+envCode);
        Map<String, Object> paramMap = new HashMap<>();
        if (StringUtils.isNotEmpty(tenantId)) {
            whereSql.append(" AND pag.tenant_id = :tenantId ");
            paramMap.put("tenantId", tenantId);
        }

        if (StringUtils.isNotEmpty(projectId)) {
            whereSql.append(" AND pag.project_id = :projectId ");
            paramMap.put("projectId", projectId);
        }

        if (StringUtils.isNotEmpty(processRecordQuery.getStartDate()) && StringUtils.isNotEmpty(processRecordQuery.getEndDate())) {
            whereSql.append(" AND pr.create_time BETWEEN to_date (:start,'yyyy-mm-dd hh24:mi:ss') and to_date (:end,'yyyy-mm-dd hh24:mi:ss') ");
            paramMap.put("start", processRecordQuery.getStartDate());//开始时间
            paramMap.put("end", processRecordQuery.getEndDate());//结束时间
        }

        if (MiscUtil.isNotEmpty(processRecordQuery.getCreators())) {
            whereSql.append(" AND pr.creator IN (:creators) ");//订阅人
            paramMap.put("creators", processRecordQuery.getCreators());
        }

        if (StringUtils.isNotEmpty(processRecordQuery.getApiName())) {
            whereSql.append(" AND pa.NAME LIKE :apiName ");//api名称
            paramMap.put("apiName", "%" + processRecordQuery.getApiName() + "%");
        }

        List<Integer> apiSystem = processRecordQuery.getApiSystem();
        if (MiscUtil.isNotEmpty(apiSystem)) {
            whereSql.append(" AND REGEXP_LIKE ( pr.ext_var, :appSystem ) ");// 订阅系统
            List<String> str = new ArrayList<>();
            for (Integer id : apiSystem) {
                str.add(String.format("\"appSystem\":%d", id));//TODO:appSystem
            }

            String join = "(" + String.join("|", str) + ")";
            paramMap.put("appSystem", join);
        }

        String sortStr = " ORDER BY pr.create_time desc";
        if (sort != null && sort.length > 1) {
            sortStr = " ORDER BY pr.create_time " + sort[0];
        }
        querySql.append(whereSql).append(sortStr);
        countSql.append(whereSql);

        Query dataQuery = entityManager.createNativeQuery(querySql.toString());
        dataQuery = dataQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.setParameters(dataQuery, paramMap);

        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        this.setParameters(countQuery, paramMap);

        PageRequest pageable = PageRequest.of(processRecordQuery.getPage() - 1, processRecordQuery.getSize());
        dataQuery.setFirstResult(new Long(pageable.getOffset()).intValue());
        dataQuery.setMaxResults(pageable.getPageSize());

        BigDecimal count = (BigDecimal) countQuery.getSingleResult();
        long total = count.longValue();
        List<Map> list = total > pageable.getOffset() ? dataQuery.getResultList() : Collections.emptyList();
        List<DataItem> system = dataItemRepository.findAllByGroupKey("system");
        Map<Integer, List<DataItem>> collect = system.stream().collect(Collectors.groupingBy(DataItem::getId));

        List<ProcessRecordDto> resultList = new ArrayList<>();
        DateTimeFormatter timeDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        for (Map map : list) {
            ProcessRecordDto dto = new ProcessRecordDto();
            dto.setId(Integer.parseInt(map.get("ID").toString()));
            dto.setStatus(Integer.parseInt(map.get("STATUS").toString()));
            dto.setRelId(Integer.parseInt(map.get("REL_ID").toString()));

            if (map.get("CREATE_TIME") != null) {
                dto.setCreateTime(parseDate(map.get("CREATE_TIME").toString(), timeDtf));
            }

            if (map.get("CREATOR") != null) {
                dto.setCreator(map.get("CREATOR").toString());
            }

            dto.setApiName(map.get("API_NAME")+"");//api名称
            String ext_var = oracleClob2Str((Clob) map.get("EXT_VAR"));
            dto.setExtVar(ext_var);

            if (StringUtils.isNotEmpty(ext_var)) {
                ProcessDataDto processDataDto = JSON.parseObject(ext_var, ProcessDataDto.class);
                if (null != processDataDto && null != processDataDto.getAppSystem()) {//TODO:application
                    List<DataItem> appSystem = collect.get(processDataDto.getAppSystem());
                    dto.setAppSystemName(appSystem == null ?"": appSystem.get(0).getItemName());//订阅系统
                }
            }
            resultList.add(dto);
        }
        return new PageImpl(resultList, pageable, total);
    }

    @Override
    public Map<String, Set<String>> findSubscribers(String tenantId, String projectId) {
        List<Map> subscribers = processRecordRepository.findSubscribers(tenantId, projectId);

        Map<String, Set<String>> map = new HashMap<>();
        map.put("createUser", subscribers.stream().map(item -> String.valueOf(item.containsKey("OPERATOR") ?
                item.get("OPERATOR") : "")).collect(Collectors.toCollection(LinkedHashSet::new)));
        map.put("updateUser", subscribers.stream().map(item -> String.valueOf(item.containsKey("UPDATER") ?
                item.get("UPDATER") : "")).collect(Collectors.toCollection(LinkedHashSet::new)));

        return map;
    }

    @Override
    public Result<Boolean> approveListApplication(List<ProcessRecordDto> processRecordDto) {
        Result<Boolean> result = new Result<>();
        for (ProcessRecordDto dto : processRecordDto) {
            approve(dto);
        }
        return result;
    }

    /**
     * 审批
     * @param processRecordDto
     */
    private Result<Object> approve(ProcessRecordDto processRecordDto) {
        Result<Object> result = new Result<>(OK,"审批处理成功",null);
        if (processRecordDto == null || processRecordDto.getId() == null ||
                processRecordDto.getStatus() == null) {
            throw new BadRequest("app_id is null");
        }

        ProcessRecord pr = processRecordRepository.getOne(processRecordDto.getId());
        if (pr.getId() == null) {
            throw new NotExist(String.format("process with id %s not exist",
                    processRecordDto.getId()));
        }

        PublishApplication pa = publishApplicationRepository.findOne(pr.getRelId());
        if (pa == null) {// TODO
            throw new NotExist(String.format("application with id %s not exist",
                    processRecordDto.getRelId()));
        }

        if (pa.getInstance() == null) {
            throw new NotExist(String.format("application with id %s not exist with instance", pa.getId()));
        }

        UserInstanceRelationship userInsRel =
                userInstanceRelationshipRepository.findByUserAndInstanceId(pa.getCreator(), pa.getInstance().getId());
        if (userInsRel == null) {
            userInsRel = new UserInstanceRelationship();
            userInsRel.setInstanceId(pa.getInstance().getId());
            userInsRel.setUserName(pa.getCreator());
            //判断3scale是否创建成功
            Long accountId = getAccountId(pa.getCreator(), pa.getInstance());
            if(accountId != null){
                userInsRel.setAccountId(accountId);
                userInsRel=userInstanceRelationshipRepository.saveAndFlush(userInsRel);
            }else {
                throw new NotExist("用户在3scale中对应的accountId获取失败！");
            }
        }

        if (null == userInsRel.getAccountId()) {
            //判断3scale是否创建成功
            Long accountId = getAccountId(pa.getCreator(), pa.getInstance());
            if(accountId != null){
                userInsRel.setAccountId(getAccountId(pa.getCreator(), pa.getInstance()));
                userInsRel=userInstanceRelationshipRepository.saveAndFlush(userInsRel);
            }else {
                throw new NotExist("用户在3scale中对应的accountId创建失败！");
            }
        }

        pr.setExtVar2(processRecordDto.getRemark());
        String aproveUser = CommonBeanUtil.getLoginUserName();
        if (processRecordDto.getStatus() == 0) {
            pr.setStatus(3);
            pr.setUpdater(aproveUser);
            pr.setUpdateTime(new Date());
            processRecordRepository.saveAndFlush(pr);
            pa.setStatus(3);
            pa.setUpdateTime(new Date());
            publishApplicationRepository.saveAndFlush(pa);
        } else {
            Application application = new Application();
            AppPlan plan = new AppPlan();
            application.setName(pa.getName());
            application.setDescription(pa.getDescription());
            plan.setId(pa.getApiPlan().getScalePlanId().toString());
            application.setPlan(plan);
            application.setAccessToken(pa.getInstance().getAccessToken());
            /**
             * 以系统为单位，一个系统可以通过一个userKey访问所有API
             * 根据订阅者所在系统查询
             */
            String scaleApplicationId = "";
            String userKey = "";
            List<PublishApplication> subscribedApis
                    = publishApplicationRepository.findSubscribedApiBySystem(pa.getSystem(),pa.getPublishApi().getId(), 2,ApiConstant.APPLICATION_TYPE_SUBSCRIBE_API);
            if(!CollectionUtils.isEmpty(subscribedApis)){
                scaleApplicationId = subscribedApis.get(0).getScaleApplicationId();
                userKey = subscribedApis.get(0).getUserKey();
                Application applicationNew = new Application();
                applicationNew.setId(scaleApplicationId);
                applicationNew.setUserKey(userKey);
                applicationNew.setAccessToken(pa.getInstance().getAccessToken());
                Result<Application> applicationResult = accountStud.updateApplication(pa.getInstance().getHost(), userInsRel.getAccountId().toString(),
                        applicationNew);
                if (applicationResult == null || "1".equals(applicationResult.getCode()) || null == applicationResult.getData()) {
                    throw new BadScaleRequest("fail to update userkey of 3scale");
                }
            }else{
                List<PublishApplication> subscribedsBySystem =
                        publishApplicationRepository.findSubscribedsBySystem(pa.getSystem(), ApiConstant.APPLICATION_TYPE_SUBSCRIBE_API,pa.getPublishApi().getEnvironment());

                //如果该系统有订阅过api（包含成功失败），则就用原来的useray
                if(!CollectionUtils.isEmpty(subscribedsBySystem)){
                    userKey = subscribedsBySystem.get(0).getUserKey();
                    application.setUserKey(userKey);

                }
                Result<Application> appRlt =
                        accountStud.createApplication(pa.getInstance().getHost(), userInsRel.getAccountId().toString(),
                                application);
                log.info("********appRlt,{}", JSONObject.toJSON(appRlt).toString());

                if (appRlt == null || "1".equals(appRlt.getCode()) || null == appRlt.getData()) {
                    throw new BadScaleRequest("fail to invoke createApplication of 3scale");
                }
                Application applicationNew = appRlt.getData();
                scaleApplicationId = applicationNew.getId();
                userKey = applicationNew.getUserKey();
            }
            pa.setScaleApplicationId(scaleApplicationId);
            pa.setUserKey(userKey);
            pa.setState("live");
            pa.setStatus(2);
            pa.setUpdateTime(new Date());
            publishApplicationRepository.saveAndFlush(pa);
            pr.setStatus(2);
            pr.setUpdater(aproveUser);
            pr.setUpdateTime(new Date());
            processRecordRepository.saveAndFlush(pr);
            //如果有其他待审批的记录也同时处理
            syncApproveHandle( pa, pr);
        }
        //审批通过，给订阅者发送邮件
        if(pr.getStatus()==2){
            Thread sendMail = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("审批通过，发送邮件,prId"+pr.getId());
                    mailService.ApiApprovalSendMail(pr.getId());
                }
            });
            sendMail.start();
        }
        //审批拒绝，给订阅者发送邮件
        if(pr.getStatus()==3){
            Thread sendMail = new Thread(new Runnable() {
                @Override
                public void run() {
                    log.info("审批拒绝，发送邮件,prId"+pr.getId());
                    mailService.ApiRefuseSendMail(pr.getId());
                }
            });
            sendMail.start();
        }
        return result;
    }


    public void syncApproveHandle(PublishApplication pa,ProcessRecord pr){
        //查询其他待处理的订阅申请
        Specification<PublishApplication> appSpec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("system").as(String.class), pa.getSystem()));
            andList.add(builder.equal(root.get("publishApi").get("id").as(Integer.class), pa.getPublishApi().getId()));
            andList.add(builder.equal(root.get("type").as(Integer.class), pa.getType()));
            andList.add(builder.equal(root.get("status").as(Integer.class), 1));
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApplication> handleApps = publishApplicationRepository.findAll(appSpec);
        if(!CollectionUtils.isEmpty(handleApps)){
            for(PublishApplication application:handleApps){
                application.setScaleApplicationId(pa.getScaleApplicationId());
                application.setUserKey(pa.getUserKey());
                application.setState("live");
                application.setStatus(2);
                application.setUpdateTime(new Date());
                publishApplicationRepository.saveAndFlush(application);
                //根据流程ID查询到对应的流程记录
                Specification<ProcessRecord> pcSpec = (root, query, builder) -> {
                    List<Predicate> andList = new LinkedList<>();
                    andList.add(builder.equal(root.get("relId").as(String.class), application.getId()));
                    andList.add(builder.equal(root.get("type").as(String.class), "application"));
                    andList.add(builder.equal(root.get("status").as(Integer.class), 1));
                    return builder.and(andList.toArray(new Predicate[andList.size()]));
                };
                List<ProcessRecord> processRecords=processRecordRepository.findAll(pcSpec);
                if(!CollectionUtils.isEmpty(processRecords)){
                    for(ProcessRecord record:processRecords){
                        record.setStatus(2);
                        record.setUpdater(pr.getUpdater());
                        record.setUpdateTime(new Date());
                        processRecordRepository.saveAndFlush(record);
                    }
                }
            }
        }
    }

    private Long getAccountId(String user, Instance ins) {
        Account account = new Account();
        account.setAccessToken(ins.getAccessToken());
        account.setUsername(user);
        AccountDto accountFind =
                accountStud.accountFind(ins.getHost(), account);
        if (null == accountFind) {
            account.setEmail(user + "@hisense.com");
            account.setPassword("123456");
            account.setOrgName("hisense" + RandomStringUtils.random(6, true, true).toLowerCase());
            Result<AccountDto> accountDtoResult = accountStud.signUp(ins.getHost(), account);
            AccountDto accountDto = accountDtoResult.getData();
            return accountDto.getAccount().getId();
        }
        return accountFind.getAccount().getId();
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

    public String oracleClob2Str(Clob clob) {
        try {
            return (clob != null ? clob.getSubString(1, (int) clob.length()) : null);
        } catch (SQLException e) {
            log.error("SQLException",e);
    }
        return "";
    }

    private Date parseDate(String timeStr, DateTimeFormatter timeDtf) {
        LocalDateTime localDateTime = LocalDateTime.parse(timeStr, timeDtf);
        ZoneId zone = ZoneId.of("Asia/Shanghai");
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }

    // portal
    /**
     * 查询我的申请列表
     * @param portalProcessRecordQuery
     * @return
     */
    @Override
    public Page<ProcessRecordDto> findMyApplicationas(String environment,PortalProcessRecordQuery portalProcessRecordQuery) {
        List<ProcessRecordDto> processRecordDtoList = new ArrayList<>();
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if(StringUtils.isNoneBlank(portalProcessRecordQuery.getSort())){
            direction = "d".equalsIgnoreCase(portalProcessRecordQuery.getSort()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        }
        Integer envCode = InstanceEnvironment.fromCode(environment).getCode();
        String loginUser = CommonBeanUtil.getLoginUserName();
        //使用动态sql的形式查询
        StringBuilder querySql = new StringBuilder(
                "select api.name apiName,api.id apiId,pr.id recordId,pr.PROCESS_INSTID processInstID,\n" +
                        "pr.status approvalStatus,pr.remark applicationlRemark,\n" +
                        "pr.ext_var2 approvalRemark,pr.updater updater,pr.create_time+0 createTime\n" +
                        ",di.item_name appName,di.id systemId\n" +
                        "from ");
        StringBuilder countSql = new StringBuilder("SELECT count(1) FROM ");
        StringBuilder whereSql = new StringBuilder("PROCESS_RECORD pr\n" +
                "join PUBLISH_APPLICATION pa on pr.rel_id = pa.id  and pr.type='application'\n" +
                "join PUBLISH_API api on pa.api_id = api.id\n" +
                "left join DATA_ITEM di on di.id = pa.system where 1=1 and api.environment="+envCode);
        Map<String, Object> paramMap = new HashMap<>();
        if(StringUtils.isNotEmpty(loginUser)){
            log.info("current login user is " + loginUser);
            whereSql.append(" AND  pr.creator= :loginUser ");
            paramMap.put("loginUser",loginUser);
        }
        if(portalProcessRecordQuery.isApprovalComplete()){
            if(!CollectionUtils.isEmpty(portalProcessRecordQuery.getApprovalStatus())){
                List<Integer> approvalStatus = portalProcessRecordQuery.getApprovalStatus();
                StringBuffer bf = new StringBuffer();
                approvalStatus.forEach(status->bf.append(status+","));
                String statusStr = bf.toString();
                statusStr = statusStr.substring(0,statusStr.length()-1);
                whereSql.append(" AND pr.status in ("+statusStr+") ");
            }else{
                whereSql.append(" AND pr.status in (0,2,3) ");
            }
        }else{
            whereSql.append(" AND pr.status =1 ");
        }
        if(!CollectionUtils.isEmpty(portalProcessRecordQuery.getApplicationSystem())){
            List<Integer> systems = portalProcessRecordQuery.getApplicationSystem();
            StringBuffer bf = new StringBuffer();
            systems.forEach(system->bf.append(system+","));
            String systemStr = bf.toString();
            systemStr = systemStr.substring(0,systemStr.length()-1);
            whereSql.append(" AND pa.system in ("+systemStr+") ");
        }

        if(StringUtils.isNotEmpty(portalProcessRecordQuery.getApiName())){
            whereSql.append("and api.name like :apiName ");
            paramMap.put("apiName","%"+ portalProcessRecordQuery.getApiName()+"%");
        }
        String sortStr=" ORDER BY pr.create_time "+direction;

        querySql.append(whereSql).append(sortStr);
        countSql.append(whereSql);
        Query dataQuery = entityManager.createNativeQuery(querySql.toString());
        dataQuery = dataQuery.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        this.setParameters(dataQuery,paramMap);
        Query countQuery = entityManager.createNativeQuery(countSql.toString());
        this.setParameters(countQuery,paramMap);
        PageRequest pageable = PageRequest.of(portalProcessRecordQuery.getPage()- 1, portalProcessRecordQuery.getSize());
        dataQuery.setFirstResult(new Long(pageable.getOffset()).intValue());
        dataQuery.setMaxResults(pageable.getPageSize());
        BigDecimal count = (BigDecimal)countQuery.getSingleResult();
        long total = count.longValue();
        List<Map> list = total > pageable.getOffset() ? dataQuery.getResultList() : Collections.emptyList();

        List<ProcessRecordDto> resultList = new ArrayList<>();
        DateTimeFormatter timeDtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        for(Map map : list){
            ProcessRecordDto dto = new ProcessRecordDto();
            if(null != map.get("RECORDID"))dto.setId(Integer.parseInt(map.get("RECORDID").toString()));
            if(null != map.get("APIID"))dto.setApiId(Integer.parseInt(map.get("APIID").toString()));
            if(null != map.get("APINAME"))dto.setApiName(map.get("APINAME").toString());
            if(null != map.get("APPROVALSTATUS"))dto.setStatus(Integer.parseInt(map.get("APPROVALSTATUS").toString()));
            if(null != map.get("APPLICATIONLREMARK"))dto.setRemark(map.get("APPLICATIONLREMARK").toString());
            if(null != map.get("APPROVALREMARK"))dto.setExtVar2(map.get("APPROVALREMARK").toString());
            if(null != map.get("CREATETIME"))dto.setCreateTime(parseDate(map.get("CREATETIME").toString(),timeDtf));
            if(null != map.get("APPNAME"))dto.setAppSystemName(map.get("APPNAME").toString());
            if(null != map.get("UPDATER"))dto.setUpdater(map.get("UPDATER").toString());
            if(null != map.get("PROCESSINSTID"))dto.setProcessInstID(map.get("PROCESSINSTID").toString());
            if(null != map.get("SYSTEMID"))dto.setSystemId(Integer.parseInt(map.get("SYSTEMID").toString()));
            resultList.add(dto);
        }
        return new PageImpl(resultList, pageable, total);
    }

    @Override
    public Result<Object> processHandle(String processId,String approveType,String remark) {
        Result<Object> result = new Result<>(OK,"流程处理成功",null);
        try{
            if(StringUtils.isBlank(processId) || StringUtils.isBlank(approveType)){
                result.setError(FAIL,"ProcessInsId or approveType is null!");
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
            //一个流程Id对应一条记录
            ProcessRecord pr = processRecords.get(0);
            pr.setRemark(remark);
            if("application".equals(pr.getType())){
                //订阅逻辑
                ProcessRecordDto processRecordDto = new ProcessRecordDto();
                processRecordDto.setRelId(pr.getRelId());
                processRecordDto.setId(pr.getId());
                if("pass".equals(approveType)){
                    processRecordDto.setStatus(1);
                    processRecordDto.setRemark(remark);
                }else{
                    processRecordDto.setStatus(0);
                    processRecordDto.setRemark(remark);
                }
                approve(processRecordDto);
                result.setMsg("订阅流程处理称成功！");
                apiInvokeRecordService.updateApiIdAndAppKey(pr.getRelId());
            }else if("publish_api".equals(pr.getType())){
                result = publishApi(approveType,pr);
                apiInvokeRecordService.updateApiIdAndAppKeyByApiId(pr.getRelId());
            }
        }catch (Exception e){
            log.error("流程处理异常！",e);
            result.setError(FAIL,String.format("流程处理异常:%s",e.getMessage()));
            return result;
        }
        return result;
    }


    /**
     * 发布流程的处理
     * @param approveType
     * @param pr
     * @return
     */
    public Result<Object> publishApi(String approveType,ProcessRecord pr){
        Result<Object> result = new Result<>(OK,"流程处理成功",null);
        //发布逻辑
        PublishApi publishApi = publishApiRepository.findOne(pr.getRelId());
        int status = 3;
        Integer OldStatus = publishApi.getStatus();
        switch (OldStatus) {
            case 1:// API_DELETE
                if("pass".equals(approveType)){
                    publishApi.setStatus(API_COMPLETE);
                    publishApi.setIsOnline(1);
                }else{
                    publishApi.setStatus(API_FIRST_PROMOTE_REJECT);
                }
                break;
            case 5:// API_INIT
                if("pass".equals(approveType)){
                    publishApi.setStatus(API_COMPLETE);
                    publishApi.setIsOnline(1);
                }else{
                    publishApi.setStatus(API_FOLLOWUP_PROMOTE_REJECT);
                }
                break;
            case 6:// API_INIT
                if("pass".equals(approveType)){
                    publishApi.setStatus(API_COMPLETE);
                    publishApi.setIsOnline(1);
                }
                break;
            default:
                if("pass".equals(approveType)){
                    publishApi.setStatus(API_COMPLETE);
                    publishApi.setIsOnline(1);
                }else{
                    if(OldStatus!=4){
                        publishApi.setStatus(API_FIRST_PROMOTE_REJECT);
                    }
                }
                break;
        }
        PublishApiTemporaryData tempDataByApiId = publishApiTemporaryDataRepository.findTempDataByApiId(publishApi.getId());
        if("pass".equals(approveType)){
            //修改api数据（欻性能临时表，更新对应数据）
            if(null != tempDataByApiId && StringUtils.isNotBlank(tempDataByApiId.getTempData())){
                PublishApiDto publishApiDto = JSONObject.parseObject(tempDataByApiId.getTempData(), PublishApiDto.class);
                List<Instance> instances = new ArrayList<>();
                Instance allByTenantIdAndPartition = instanceRepository.findAllByTenantIdAndPartition(InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName(), InstancePartition.fromCode(publishApiDto.getPartition()).getName());
                instances.add(allByTenantIdAndPartition);
                if (MiscUtil.isEmpty(instances)) {
                    result.setError(FAIL,String.format("更新失败，未查询到scale实例"));
                    return result;
                }
                publishApiService.updatePublicApiData( instances,publishApi.getId(),publishApiDto);
            }
            List<String> partitions = new ArrayList<>();
            String partition = PublishApiServiceImpl.getPartition(publishApi.getPartition());
            partitions.add(partition);
            // 发布到3scale // guilai 2020/09/16
            List<ApiInstance> apiInstances = pairRepository.getApiInstances(publishApi.getId(),
                    InstanceEnvironment.fromCode(publishApi.getEnvironment()).getName(),partitions);// guilai 2020/09/16
            ProxyConfigDto proxyConfigDto = null;
            for (ApiInstance apiInstance : apiInstances) {
                log.info(String.format("latestPromote params:host-%S,token-%S,3scaleId-%S",apiInstance.getHost(),
                        apiInstance.getAccessToken(), apiInstance.getId()));
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
                } else {
                    log.error("proxyConfig not exist");
                    result.setMsg("3scale通信异常,请联系平台管理员");
                    return result;
                }
            }
            status = 2;
        }else{
            status = 3;
        }
        Date date = new Date();
        publishApi.setUpdateTime(date);
        publishApiRepository.saveAndFlush(publishApi);
        String aproveUser = CommonBeanUtil.getLoginUserName();
        pr.setStatus(status);
        pr.setUpdater(aproveUser);
        pr.setUpdateTime(date);
        processRecordRepository.saveAndFlush(pr);
        //修改发布时，更新临时表数据状态
        if(null != tempDataByApiId && StringUtils.isNotBlank(tempDataByApiId.getTempData())){
            tempDataByApiId.setUpdator(aproveUser);
            tempDataByApiId.setUpdateTime(date);
            tempDataByApiId.setStatus(status);
            publishApiTemporaryDataRepository.saveAndFlush(tempDataByApiId);
        }
        result.setMsg("发布流程处理成功！");

        //发布拒绝发送邮件
        if((publishApi.getStatus()==5 || publishApi.getStatus()==6) && pr.getStatus()==3){
            mailService.PromoteRejectSendMail(pr.getId());
        }
        return result;
    }
}
