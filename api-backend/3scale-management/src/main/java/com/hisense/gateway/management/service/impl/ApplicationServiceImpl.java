/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.exception.BadScaleRequest;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.ProcessDataDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import com.hisense.gateway.library.model.dto.web.PublishApplicationDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.management.service.ApplicationService;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.ApplicationStud;
import com.hisense.gateway.library.stud.model.ApplicationDtos;
import com.hisense.gateway.library.stud.model.ApplicationXml;
import com.hisense.gateway.library.stud.model.ApplicationXmlDtos;
import com.hisense.gateway.library.web.form.ApplicationForm;
import com.hisense.gateway.library.web.form.ApplicationSearchForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Clob;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * ApplicationServiceImpl
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:35
 */
@Slf4j
@Service
public class ApplicationServiceImpl implements ApplicationService {
    @Autowired
    DomainRepository domainRepository;

    @Autowired
    ApplicationStud applicationStud;

    @Autowired
    TemporaryApplicationRepository temporaryApplicationRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Autowired
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    AccountStud accountStud;

    @Autowired
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;

    @Autowired
    ProcessRecordRepository processRecordRepository;

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    DataItemRepository dataItemRepository;

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public ApplicationDtos getApplicationListByAccount(String domainName,
                                                       ApplicationForm applicationForm) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        applicationForm.setAccessToken(domain.getAccessToken());
        ApplicationDtos applicationDtos = null;
        applicationDtos = applicationStud.getApplicationListByAccount(domain.getHost(), applicationForm);
        return applicationDtos;
    }

    @Override
    public ApplicationXmlDtos findApplicationListByAccount(String domainName, ApplicationForm applicationForm) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        applicationForm.setAccessToken(domain.getAccessToken());
        return applicationStud.findApplicationListByAccount(domain.getHost(), applicationForm);
    }

    @Override
    public List<TemporaryApplication> findAllTemporaryApplication(ApplicationSearchForm applicationSearchForm) {
        Integer groupId = applicationSearchForm.getGroupId();
        String applicationName = applicationSearchForm.getApplicationName();
        Integer categoryOne = applicationSearchForm.getCategoryOne();
        Integer categoryTwo = applicationSearchForm.getCategoryTwo();
        Integer system = applicationSearchForm.getSystem();
        Integer status = applicationSearchForm.getStatus();
        Integer pageNum = applicationSearchForm.getPageNum();
        Integer pageSize = applicationSearchForm.getPageSize();
        if (groupId != null) {
            //todo 判断查询条件联合查询
            //todo 分页
        }
        return temporaryApplicationRepository.findAll();
    }

    @Override
    public Page<PublishApplication> findBackLogApplication(ApplicationSearchForm applicationSearchForm,
                                                           PageRequest pageable) {
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

            if (applicationSearchForm.getApiSystem() != null) {
                andList.add(builder.equal(root.get("system").as(Integer.class), applicationSearchForm.getApiSystem()));
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };

        List<PublishApiGroup> publishApiGroups = publishApiGroupRepository.findAll(spec);
        if (CollectionUtils.isEmpty(publishApiGroups)) {
            throw new NotExist("app not exist");
        }

        Specification<PublishApi> spec2 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            if (!CollectionUtils.isEmpty(publishApiGroups)) {
                CriteriaBuilder.In<PublishApiGroup> in = builder.in(root.get("group"));
                for (PublishApiGroup pg : publishApiGroups) {
                    in.value(pg);
                }
                andList.add(in);
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> apis = publishApiRepository.findAll(spec2);
        if (CollectionUtils.isEmpty(apis)) {
            throw new NotExist("api not exist");
        }

        Specification<PublishApplication> spec3 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<PublishApi> in = builder.in(root.get("publishApi"));
            for (PublishApi api : apis) {
                in.value(api);
            }
            andList.add(in);

            if (applicationSearchForm.getAppSystem() != null) {
                andList.add(builder.equal(root.get("system").as(Integer.class), applicationSearchForm.getAppSystem()));
            }

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };

        return publishApplicationRepository.findAll(spec3, pageable);
    }

    @Override
    public PublishApplicationDto getPublishApplication(Integer id) {
        if (id == null) {
            throw new NotExist("app_id is null");
        }

        PublishApplication db = publishApplicationRepository.findOne(id);
        if (db == null) {
            throw new NotExist(String.format("application with id %s not exist", id));
        }

        // guilai.ming 2020/09/10
        PublishApplicationDto dto = new PublishApplicationDto(db);
        DataItem item = dataItemRepository.findOne(db.getSystem());
        if (item != null) {
            dto.setSystem(item.getId());
        }

        return dto;
    }

    @Override
    public Page<ProcessRecordDto> findApprovalCompleteApp(ApplicationSearchForm applicationSearchForm,
                                                          PageRequest pageable) {
//      Integer userId = SecurityUtils.getLoginUser().getUsername();
//      User createUser = new User();
//      createUser.setId(userId);
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
            throw new NotExist("app not exist");
        }

        Specification<PublishApi> spec2 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<PublishApiGroup> in = builder.in(root.get("group"));
            for (PublishApiGroup pg : publishApiGroups) {
                in.value(pg);
            }
            andList.add(in);
            if (StringUtils.isNotBlank(applicationSearchForm.getApiName())) {
                andList.add(builder.like(root.get("name").as(String.class),
                        "%" + applicationSearchForm.getApiName() + "%", GatewayConstants.ESCAPECHAR));
            }

            //if (null != createUser) {
            //    andList.add(builder.equal(root.get("createUser").as(User.class), createUser));
            //}

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApi> apis = publishApiRepository.findAll(spec2);
        if (CollectionUtils.isEmpty(apis)) {
            throw new NotExist("app not exist");
        }

        Specification<PublishApplication> spec3 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<PublishApi> in = builder.in(root.get("publishApi"));
            for (PublishApi api : apis) {
                in.value(api);
            }
            andList.add(in);

            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        List<PublishApplication> publishApplicationList = publishApplicationRepository.findAll(spec3);
        if (CollectionUtils.isEmpty(publishApplicationList)) {
            throw new NotExist("app not exist");
        }

        Specification<ProcessRecord> spec4 = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            CriteriaBuilder.In<Integer> in = builder.in(root.get("relId"));
            for (PublishApplication publishApplication : publishApplicationList) {
                in.value(publishApplication.getId());
            }
            andList.add(in);
            andList.add(builder.equal(root.get("type").as(String.class), "application"));
            if (applicationSearchForm.getStatus() != null) {
                andList.add(builder.equal(root.get("status").as(String.class), applicationSearchForm.getStatus()));
            }

            if (applicationSearchForm.getGroupId() != null) {
                andList.add(builder.like(root.get("extVar").as(String.class),
                        "%\"groupId\":" + applicationSearchForm.getGroupId() + "}%", GatewayConstants.ESCAPECHAR));
            }

            if (applicationSearchForm.getAppSystem() != null) {
                andList.add(builder.like(root.get("extVar").as(String.class),
                        "%\"appSystem\":" + applicationSearchForm.getAppSystem() + ",%", GatewayConstants.ESCAPECHAR));
            }

            andList.add(builder.notEqual(root.get("status").as(Integer.class), 1));//待审批的不展示
            andList.add(builder.notEqual(root.get("status").as(Integer.class), 0));//删除的不展示
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        Page<ProcessRecord> p = processRecordRepository.findAll(spec4, pageable);
        List<ProcessRecord> processRecords = p.getContent();
        if (CollectionUtils.isEmpty(processRecords)) {
            throw new NotExist("ProcessRecord not exist");
        }

        List<ProcessRecordDto> processRecordDtoList = new ArrayList<>();
        for (ProcessRecord processRecord : processRecords) {
            ProcessRecordDto processRecordDto = new ProcessRecordDto(processRecord);
            PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
            ProcessDataDto processDataDto = JSONObject.parseObject(processRecord.getExtVar(), ProcessDataDto.class);
            if (null != processDataDto) {
                PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(processDataDto.getGroupId());
                DataItem dataItem = dataItemRepository.findOne(processDataDto.getAppSystem());
                if (dataItem != null) {
                    processRecordDto.setAppSystemName(dataItem.getItemName());
                }

                processRecordDto.setGroupName(publishApiGroup.getName());
            }

            processRecordDto.setState(publishApplication.getState());
            processRecordDto.setRemark(processRecord.getRemark());
            processRecordDto.setApiName(publishApplication.getPublishApi().getName());
            processRecordDtoList.add(processRecordDto);
        }

        Page<ProcessRecordDto> data = new PageImpl<>(processRecordDtoList, pageable, p.getTotalElements());
        return data;
    }

    @Override
    public Result<Boolean> applicationSuspend(Integer id) {
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
        Result<Boolean> returnResult = new Result<>();
        if (publishApplication.getPublishApi().getStatus() == 0) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("所属api已删除,订购已失效");
            returnResult.setData(false);
            return returnResult;
        }

        Instance instance = publishApplication.getInstance();
        if (instance == null) {
            log.error("can not found instance");
            throw new OperationFailed("instance not exist");
        }

        String user = publishApplication.getCreator();
        if (user == null) {
            log.error("can not found user");
            throw new OperationFailed("user not exist");
        }

        UserInstanceRelationship userInstanceRelationship =
                userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
        if (publishApplication.getState().equals("live")) {
            ApplicationXml applicationXml = applicationStud.applicationSuspend(instance.getHost(),
                    instance.getAccessToken(), userInstanceRelationship.getAccountId().toString(),
                    publishApplication.getScaleApplicationId());

            if (null == applicationXml) {
                throw new BadScaleRequest("fail to invoke createApplication of 3scale");
            }

            publishApplicationRepository.updateState(processRecord.getRelId(), applicationXml.getState());
        }

        //发送邮件通知
        DataItem dataItem = dataItemRepository.findOne(publishApplication.getPublishApi().getGroup().getSystem());
        String name = String.format("%s项目%sAPI", dataItem.getItemName(), publishApplication.getPublishApi().getName());
        String subject = String.format("禁用：订购%s, 请查看", name);
        String contents = String.format("该订购记录已被禁用, 您暂时无法调用%s, 请耐心等待!", name);
        try {
//            MailUtils.sendNormalEmail(Collections.singletonList(user/*.getEmail()*/), null, subject, "hicloud",
//                    contents);
        } catch (Exception e) {
            log.error("context",e);
            log.error("发送邮件失败");
        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("禁用成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<Boolean> applicationResume(Integer id) {
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
        Result<Boolean> returnResult = new Result<>();
        if (publishApplication.getPublishApi().getStatus() == 0) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("所属api已删除,订购已失效");
            returnResult.setData(false);
            return returnResult;
        }

        Instance instance = publishApplication.getInstance();
        if (instance == null) {
            log.error("can not found instance");
            throw new OperationFailed("instance not exist");
        }

        String user = publishApplication.getCreator();
        if (user == null) {
            log.error("can not found user");
            throw new OperationFailed("user not exist");
        }

        UserInstanceRelationship userInstanceRelationship =
                userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
        if (publishApplication.getState().equals("suspended")) {
            ApplicationXml applicationXml = applicationStud.applicationResume(instance.getHost(),
                    instance.getAccessToken(), userInstanceRelationship.getAccountId().toString(),
                    publishApplication.getScaleApplicationId());

            if (null == applicationXml) {
                throw new BadScaleRequest("fail to invoke createApplication of 3scale");
            }

            publishApplicationRepository.updateState(processRecord.getRelId(), applicationXml.getState());
        }

        //发送邮件通知
        DataItem dataItem = dataItemRepository.findOne(publishApplication.getPublishApi().getGroup().getSystem());
        String name = String.format("%s项目%sAPI", dataItem.getItemName(), publishApplication.getPublishApi().getName());
        String subject = String.format("启用：订购%s，请查看", name);
        String contents = String.format("该订购记录已被启用，您可以正常使用%s！", name);
        try {
/*            MailUtils.sendNormalEmail(Collections.singletonList(user*//*.getEmail()*//*), null, subject, "hicloud",
                    contents);*/
        } catch (Exception e) {
            log.error("发送邮件失败");
            log.error("context",e);
        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("启用成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    @Transactional
    public Boolean applicationDelete(Integer id) {
        return delete(id);
    }

    @Override
    public Page<ProcessRecordDto> findApprovalCompleteApp(String tenantId, String projectId,String environment,
                                                          ProcessRecordQuery processRecordQuery) {
        int envCode = InstanceEnvironment.fromCode(environment).getCode();
        StringBuilder querySql = new StringBuilder(
                "SELECT\n" +
                        "\tpr.id,\n" +
                        "\tpr.ext_var,\n" +
                        "\tto_char(pr.create_time,'yyyy-mm-dd hh24:mi:ss') create_time,\n" +
                        "\tto_char(pr.update_time,'yyyy-mm-dd hh24:mi:ss') update_time,\n" +
                        "\tpr.status,\n" +
                        "\tpr.remark as remark,\n" +
                        "\tapp.state,\n" +
                        "\tpr.rel_id,\n" +
                        "\tpr.creator,\n" +
                        "\tpr.updater,\n" +
                        "\tpa.id AS api_id,\n" +
                        "\tpa.NAME AS api_name\n" +
                        "FROM ");
        StringBuilder countSql = new StringBuilder("SELECT count(1) FROM ");
        StringBuilder whereSql = new StringBuilder(
                " PROCESS_RECORD pr\n" +
                        "\tINNER JOIN PUBLISH_APPLICATION app ON pr.rel_id = app.id\n" +
                        "\tINNER JOIN PUBLISH_API pa ON app.api_id = pa.id\n" +
                        "\tINNER JOIN PUBLISH_API_GROUP pag ON pa.group_id = pag.id\n" +
                        " WHERE 1=1 AND pr.STATUS not in(1) AND pr.type = 'application' AND pa.environment ="+envCode);


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

        if (MiscUtil.isNotEmpty(processRecordQuery.getUpdaters())) {
            whereSql.append(" AND pr.updater IN (:updaters) ");//审批人
            paramMap.put("updaters", processRecordQuery.getUpdaters());
        }

        if (MiscUtil.isNotEmpty(processRecordQuery.getStatus())) {
            whereSql.append(" AND pr.status IN (:status) ");//审批状态
            paramMap.put("status", processRecordQuery.getStatus());
        }

        if (StringUtils.isNotEmpty(processRecordQuery.getApiName())) {
            whereSql.append(" AND pa.NAME LIKE :apiName ");//api名称
            paramMap.put("apiName", "%" + processRecordQuery.getApiName() + "%");
        }

        if (MiscUtil.isNotEmpty(processRecordQuery.getState())) {
            whereSql.append(" AND app.state IN (:state) ");//禁用/启用
            paramMap.put("state", processRecordQuery.getState());
        }

        List<Integer> apiSystem = processRecordQuery.getApiSystem();
        if (MiscUtil.isNotEmpty(apiSystem)) {
            List<String> str = new ArrayList<>();
            for (Integer id : apiSystem) {
                str.add(String.format("\"application\":%d", id));//TODO:application
            }

            String join = "(" + String.join("|", str) + ")";
            paramMap.put("appSystem", join);
            whereSql.append(" AND REGEXP_LIKE ( pr.ext_var, :appSystem ) ");// 订阅系统
        }

        String sortStr = "ORDER BY pr.create_time desc";
        String[] sort = processRecordQuery.getSort();
        if (sort != null && sort.length > 1) {
            if ("updateTime".equalsIgnoreCase(sort[1])) {
                sortStr = "ORDER BY pr.update_time " + sort[0];
            } else {
                sortStr = "ORDER BY pr.create_time " + sort[0];
            }
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
            if (map.get("STATE") != null) {
                dto.setState(map.get("STATE").toString());
            }

            if (map.get("CREATE_TIME") != null) {
                dto.setCreateTime(parseDate(map.get("CREATE_TIME").toString(), timeDtf));
            }

            if (map.get("UPDATE_TIME") != null) {
                dto.setUpdateTime(parseDate(map.get("UPDATE_TIME").toString(), timeDtf));
            }

            dto.setRelId(Integer.parseInt(map.get("REL_ID").toString()));

            if (map.get("CREATOR") != null) {
                dto.setCreator(map.get("CREATOR").toString());
            }

            if (map.get("UPDATER") != null) {
                dto.setUpdater(map.get("UPDATER").toString());
            }

            dto.setApiName(map.get("API_NAME")+"");//api名称
            String ext_var = oracleClob2Str((Clob) map.get("EXT_VAR"));
            dto.setExtVar(ext_var);

            if (map.get("REMARK") != null) {
                dto.setRemark(map.get("REMARK").toString());
            }

            if (StringUtils.isNotEmpty(ext_var)) {
                ProcessDataDto processDataDto = JSON.parseObject(ext_var, ProcessDataDto.class);
                if (null != processDataDto && null != processDataDto.getAppSystem()) {//TODO:application
                    List<DataItem> appSystem = collect.get(processDataDto.getAppSystem());
                    dto.setAppSystemName(appSystem != null ? appSystem.get(0).getItemName() : "");//订阅系统
                }
            }
            resultList.add(dto);
        }
        return new PageImpl(resultList, pageable, total);
    }

    @Override
    @Transactional
    public Result<List<String>> applicationSuspendList(List<Integer> ids) {
        Result<List<String>> returnResult = new Result<>();
        List<PublishApi> apiByProcessRecordId = publishApiRepository.findApiByProcessRecordId(ids);
        if (apiByProcessRecordId.size() > 0) {
            List<String> collect = apiByProcessRecordId.stream().map(m -> m.getName()).collect(Collectors.toList());
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("所属api已删除,订购已失效");
            returnResult.setData(collect);
            return returnResult;
        }

        for (Integer id : ids) {
            ProcessRecord processRecord = processRecordRepository.findOne(id);
            PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
            Instance instance = publishApplication.getInstance();
            if (instance == null) {
                log.error("can not found instance");
                throw new OperationFailed("instance not exist");
            }

            String user = publishApplication.getCreator();
            if (user == null) {
                log.error("can not found user");
                throw new OperationFailed("user not exist");
            }

            UserInstanceRelationship userInstanceRelationship =
                    userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
            if ("live".equals(publishApplication.getState())) {
                ApplicationXml applicationXml = applicationStud.applicationSuspend(instance.getHost(),
                        instance.getAccessToken(), userInstanceRelationship.getAccountId().toString(),
                        publishApplication.getScaleApplicationId());

                if (null == applicationXml) {
                    throw new BadScaleRequest("fail to invoke createApplication of 3scale");
                }

                publishApplicationRepository.updateStateByApiIdAndSystem(applicationXml.getState(), publishApplication.getPublishApi().getId(), publishApplication.getSystem());

                //发送邮件通知
                DataItem dataItem = dataItemRepository.findOne(publishApplication.getPublishApi().getGroup().getSystem());
                String name = String.format("%s项目%sAPI", dataItem.getItemName(), publishApplication.getPublishApi().getName());
                String subject = String.format("禁用：订购%s, 请查看", name);
                String contents = String.format("该订购记录已被禁用, 您暂时无法调用%s, 请耐心等待!", name);
                try {
//                    MailUtils.sendNormalEmail(Collections.singletonList(user/*.getEmail()*/), null, subject, "hicloud", contents);
                } catch (Exception e) {
                    log.error("发送邮件失败");
                    log.error("context",e);
                }
            }
        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("禁用成功");
        returnResult.setData(null);
        return returnResult;
    }

    @Override
    public Result<List<String>> applicationResumeList(List<Integer> ids) {
        Result<List<String>> returnResult = new Result<>();
        List<PublishApi> apiByProcessRecordId = publishApiRepository.findApiByProcessRecordId(ids);
        if (apiByProcessRecordId.size() > 0) {
            List<String> collect = apiByProcessRecordId.stream().map(m -> m.getName()).collect(Collectors.toList());
            returnResult.setMsg("所属api已删除,订购已失效");
            returnResult.setCode(Result.FAIL);
            returnResult.setData(collect);
            return returnResult;
        }

        for (Integer id : ids) {
            ProcessRecord processRecord = processRecordRepository.findOne(id);
            PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());
            Instance instance = publishApplication.getInstance();
            if (instance == null) {
                log.error("can not found instance");
                throw new OperationFailed("instance not exist");
            }

            String user = publishApplication.getCreator();
            if (user == null) {
                log.error("can not found user");
                throw new OperationFailed("user not exist");
            }

            UserInstanceRelationship userInstanceRelationship =
                    userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
            if ("suspended".equals(publishApplication.getState())) {
                ApplicationXml applicationXml = applicationStud.applicationResume(instance.getHost(),
                        instance.getAccessToken(), userInstanceRelationship.getAccountId().toString(),
                        publishApplication.getScaleApplicationId());

                if (null == applicationXml) {
                    throw new BadScaleRequest("fail to invoke createApplication of 3scale");
                }

                publishApplicationRepository.updateStateByApiIdAndSystem(applicationXml.getState(), publishApplication.getPublishApi().getId(), publishApplication.getSystem());

                //发送邮件通知
                DataItem dataItem = dataItemRepository.findOne(publishApplication.getPublishApi().getGroup().getSystem());
                String name = String.format("%s项目%sAPI", dataItem.getItemName(), publishApplication.getPublishApi().getName());
                String subject = String.format("启用：订购%s，请查看", name);
                String contents = String.format("该订购记录已被启用，您可以正常使用%s！", name);
                try {
//                    MailUtils.sendNormalEmail(Collections.singletonList(user/*.getEmail()*/), null, subject, "hicloud", contents);
                } catch (Exception e) {
                    log.error("发送邮件失败");
                    log.error("context",e);
                }
            }
        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("启用成功");
        returnResult.setData(null);
        return returnResult;
    }

    @Override
    @Transactional
    public Boolean applicationDeleteList(List<Integer> ids) {
        for (Integer id : ids) {
            delete(id);
        }
        return true;
    }

    private boolean delete(Integer id) {
        ProcessRecord processRecord = processRecordRepository.findOne(id);
        PublishApplication publishApplication = publishApplicationRepository.findOne(processRecord.getRelId());

        if (processRecord.getStatus() == 2) {
            //如果审批通过
            if (publishApplication.getPublishApi().getStatus() != 0) {
                // 同时所属api没有被删除，此时才需要到3scale上去删除数据
                Instance instance = publishApplication.getInstance();
                if (instance == null) {
                    log.error("can not found instance");
                    throw new OperationFailed("instance not exist");
                }

                String user = publishApplication.getCreator();
                if (user == null) {
                    log.error("can not found user");
                    throw new OperationFailed("user not exist");
                }

                UserInstanceRelationship userInstanceRelationship =
                        userInstanceRelationshipRepository.findByUserAndInstanceId(user, instance.getId());
                applicationStud.applicationDelete(instance.getHost(), instance.getAccessToken(),
                        userInstanceRelationship.getAccountId().toString(), publishApplication.getScaleApplicationId());
            }
            // 将本地app状态修改为删除,并解除与scale的关联
            publishApplication.setStatus(0);
            publishApplicationRepository.saveAndFlush(publishApplication);
        }
        // 设置所选APP记录为i删除状态
        processRecord.setStatus(0);
        processRecordRepository.saveAndFlush(processRecord);
        return true;
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
        ZoneId zone = ZoneId.systemDefault();
        Instant instant = localDateTime.atZone(zone).toInstant();
        return Date.from(instant);
    }
}
