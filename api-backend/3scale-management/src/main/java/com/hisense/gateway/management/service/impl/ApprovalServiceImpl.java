/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/2 @author peiyun
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.ApiInstanceDto;
import com.hisense.gateway.library.model.dto.web.ApprovalApiDto;
import com.hisense.gateway.library.model.dto.web.ApprovalApiResDto;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleDto;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.impl.PublishApiServiceImpl;
import com.hisense.gateway.management.service.ApprovalService;
import com.hisense.gateway.management.service.permission.PermissionService;
import com.hisense.gateway.library.stud.ServiceStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.SQLQuery;
import org.hibernate.transform.Transformers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ApprovalServiceImpl implements ApprovalService {
    @Resource
    ProcessRecordRepository processRecordRepository;

    @Resource
    PublishApiRepository publishApiRepository;

    @Resource
    PublishApiInstanceRelationshipRepository pairRepository;

    @Resource
    DataItemRepository dataItemRepository;

    @Autowired
    ServiceStud serviceStud;

    @PersistenceContext
    EntityManager entityManager;

    @Value("${api.paas-api}")
    public String paasApiUrl;

    private List<Map<String, String>> tenantList;

    private List<Map<String, String>> projectList;

    @Autowired
    private PermissionService permissionService;

    @Resource
    PublishApiGroupRepository publishApiGroupRepository;

    @Autowired
    PublishApplicationRepository publishApplicationRepository;

    @Override
    public Result<Page<ApprovalApiResDto>> searchReadyApprovalApi(ApprovalApiDto approvalApiDto, String authorization) {
        Result<Page<ApprovalApiResDto>> returnResult = new Result<>();
        String[] sort = approvalApiDto.getSort();
        PageRequest pageable = PageRequest.of(approvalApiDto.getPageNum() - 1, approvalApiDto.getPageSize());

        StringBuilder countSelectSql = new StringBuilder("");
        countSelectSql.append(" SELECT COUNT(1) ");

        StringBuilder selectHeadSql = new StringBuilder();
        selectHeadSql.append(" SELECT pr.id as id,pr.type as type,pr.status as status,pr.rel_id as relId, ");
        selectHeadSql.append(" pr.creator as createId, pr.create_time as createTime,pr.update_time as updateTime, ");
        selectHeadSql.append(" pr.remark as remark, pr.updater as updateId,pr.ext_var as extVar, pa.id as apiId, ");
        selectHeadSql.append(" pa.name as apiName, pag.id as groupId ,pag.tenant_id as tenantId , ");
        selectHeadSql.append(" pag.project_id as projectId ,di.item_name as systemName, pr.creator as createName ");

        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" FROM process_record pr ");
        selectSql.append(" LEFT JOIN publish_api pa ON pr.rel_id = pa.id ");
        selectSql.append(" LEFT JOIN publish_api_group pag ON pa.group_id = pag.id ");
        selectSql.append(" LEFT JOIN data_item di ON di.id = pag.system ");
        selectSql.append(" WHERE ");
        selectSql.append(" pr.type = 'publish_api' ");

        Map<String, Object> params = new HashMap<>();
        StringBuilder whereSql = new StringBuilder();
        if (null == approvalApiDto.getStatus() || 0 == approvalApiDto.getStatus()) {
            whereSql.append(" AND ( pr.status = 2 OR pr.status = 3 ) ");
        } else if (2 == approvalApiDto.getStatus()) {
            whereSql.append(" AND pr.status = 2 ");
        } else if (3 == approvalApiDto.getStatus()) {
            whereSql.append(" AND pr.status = 3 ");
        }

        if (StringUtils.isNotBlank(approvalApiDto.getTenantId())) {
            whereSql.append(" AND pag.tenant_id = :tenantId ");
            params.put("tenantId", approvalApiDto.getTenantId());
        }

        if (StringUtils.isNotBlank(approvalApiDto.getProjectId())) {
            whereSql.append(" AND pag.project_id = :projectId ");
            params.put("projectId", approvalApiDto.getProjectId());
        }

        if (StringUtils.isNotBlank(approvalApiDto.getSystem())) {
            //whereSql.append(" AND pag.system = :system ");
            whereSql.append(" AND JSON_EXTRACT(pr.ext_var,'$.apiSystem') = :system ");
            params.put("system", Integer.valueOf(approvalApiDto.getSystem()));
        }

        if (StringUtils.isNotBlank(approvalApiDto.getApiName())) {
            whereSql.append(" AND pa.name LIKE :apiName ");
            params.put("apiName", "%" + approvalApiDto.getApiName().trim() + "%");
        }

        String orderSql = " ORDER BY pr.update_time DESC ";
        if (sort != null && sort.length > 1) {//"sort":["ase","createTime"]
            orderSql = " ORDER BY pr.update_time" + sort[0];
        }

        String countSql = new StringBuilder().append(countSelectSql).append(selectSql).append(whereSql).toString();

        Query countQuery = entityManager.createNativeQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        List totalCountList = countQuery.getResultList();
        Long totalCount = 0L;
        if (null != totalCountList && totalCountList.size() > 0) {
            totalCount = Long.valueOf(String.valueOf(totalCountList.get(0)));
        }

        if (0L == totalCount) {
            returnResult.setCode(Result.OK);
            returnResult.setMsg("查询成功");
            returnResult.setData(new PageImpl<>(new ArrayList<ApprovalApiResDto>(1), pageable, totalCount));
            return returnResult;
        }

        String querySql =
                new StringBuilder().append(selectHeadSql).append(selectSql).append(whereSql).append(orderSql).toString();

        Query query = entityManager.createNativeQuery(querySql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Map<String, Object>> mapList = query.getResultList();
        List<ApprovalApiResDto> approvalApiResDtos = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Map<String, Object> map : mapList) {
            try {
                Date createTime = sdf.parse(String.valueOf(map.get("createTime")));
                ApprovalApiResDto approvalApiResDto = new ApprovalApiResDto();
                approvalApiResDto.setApiId(Integer.valueOf(String.valueOf(map.get("apiId"))));
                approvalApiResDto.setApiName(String.valueOf(map.get("apiName")));
                approvalApiResDto.setGroupId(Integer.valueOf(String.valueOf(map.get("groupId"))));
                approvalApiResDto.setSystemName(String.valueOf(map.get("systemName")));
                approvalApiResDto.setProcessRecordId(Integer.valueOf(String.valueOf(map.get("id"))));
                approvalApiResDto.setCreator(String.valueOf(map.get("createName")));
                approvalApiResDto.setProjectId(String.valueOf(map.get("projectId")));
                approvalApiResDto.setTenantId(String.valueOf(map.get("tenantId")));
                approvalApiResDto.setCreateTime(createTime);
                approvalApiResDto.setStatus(Integer.valueOf(String.valueOf(map.get("status"))));
                approvalApiResDto.setType(String.valueOf(map.get("type")));
                approvalApiResDto.setRemark(String.valueOf(map.get("remark")));

                String tenantId = String.valueOf(map.get("tenantId"));
                String tenantName = "hicloud";//permissionService.getTenantNameById(tenantId);
                approvalApiResDto.setTenantName(tenantName);

                String projectId = String.valueOf(map.get("projectId"));
                String projectName = "hicloud";//permissionService.getProjectNameById(projectId);
                approvalApiResDto.setProjectName(projectName);

                approvalApiResDtos.add(approvalApiResDto);
            } catch (ParseException e) {
                log.error("查询异常",e);
            }
        }

        Page<ApprovalApiResDto> page = new PageImpl<>(approvalApiResDtos, pageable, totalCount);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(page);
        return returnResult;
    }

    private String getProjectNameById(String tenantId, String projectId, String authorization) {
        if (null == projectList || projectList.size() == 0) {
            this.findProjectList(authorization, tenantId);
        }

        if (null != projectList && projectList.size() > 0) {
            Map<String, String> projectMap =
                    projectList.stream().filter(project -> projectId.equals(project.get("id"))).findAny().orElse(null);
            if (null == projectMap || projectMap.isEmpty()) {//再查一遍
                this.findProjectList(authorization, tenantId);
                projectMap =
                        projectList.stream().filter(project -> projectId.equals(project.get("id"))).findAny().orElse(null);
            }

            if (null != projectMap && !projectMap.isEmpty()) {
                return projectMap.get("name");
            }
        }
        return projectId;
    }

    private String getTenantNameById(String tenantId, String authorization) {
        if (null == tenantList || tenantList.size() == 0) {
            this.findTenantList(authorization);
        }

        if (null != tenantList && tenantList.size() > 0) {
            Map<String, String> tenantMap =
                    tenantList.stream().filter(group -> tenantId.equals(group.get("id"))).findAny().orElse(null);
            if (null == tenantMap || tenantMap.isEmpty()) {//再查一遍
                this.findTenantList(authorization);
                tenantMap =
                        tenantList.stream().filter(group -> tenantId.equals(group.get("id"))).findAny().orElse(null);
            }

            if (null != tenantMap && !tenantMap.isEmpty()) {
                return tenantMap.get("name");
            }
        }
        return tenantId;
    }

    @Override
    public Result<Page<ApprovalApiResDto>> searchWaitApprovalApi(ApprovalApiDto approvalApiDto, String authorization) {
        Result<Page<ApprovalApiResDto>> returnResult = new Result<>();
        String[] sort = approvalApiDto.getSort();
        PageRequest pageable = PageRequest.of(approvalApiDto.getPageNum() - 1, approvalApiDto.getPageSize());

        StringBuilder countSelectSql = new StringBuilder("");
        countSelectSql.append(" SELECT count(num) FROM (SELECT count(1) num ");

        StringBuilder selectHeadSql = new StringBuilder();
        selectHeadSql.append(" SELECT pr.id as id,pr.type as type,pr.status as status,pr.rel_id as relId, ");
        selectHeadSql.append(" pr.creator as createId, pr.create_time as createTime,pr.update_time as updateTime, ");
        selectHeadSql.append(" pr.remark as remark, pr.updater as updateId,pr.ext_var as extVar, pa.id as apiId, ");
        selectHeadSql.append(" pa.name as apiName, pag.id as groupId ,pag.tenant_id as tenantId , ");
        selectHeadSql.append(" pag.project_id as projectId ,di.item_name as systemName, pr.creator as createName ");

        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" FROM process_record pr ");
        selectSql.append(" LEFT JOIN publish_api pa ON pr.rel_id = pa.id ");
        selectSql.append(" LEFT JOIN publish_api_group pag ON pa.group_id = pag.id ");
        selectSql.append(" LEFT JOIN data_item di ON di.id = pag.system ");
        selectSql.append(" WHERE ");
        selectSql.append(" pr.type = 'publish_api' ");
        selectSql.append(" AND pr.status=1 ");

        Map<String, Object> params = new HashMap<>();
        StringBuilder whereSql = new StringBuilder();

        if (StringUtils.isNotBlank(approvalApiDto.getTenantId())) {
            whereSql.append(" AND pag.tenant_id = :tenantId ");
            params.put("tenantId", approvalApiDto.getTenantId());
        }

        if (StringUtils.isNotBlank(approvalApiDto.getProjectId())) {
            whereSql.append(" AND pag.project_id = :projectId ");
            params.put("projectId", approvalApiDto.getProjectId());
        }

        if (StringUtils.isNotBlank(approvalApiDto.getSystem())) {
            //whereSql.append(" AND pag.system = :system ");
            whereSql.append(" AND JSON_EXTRACT(pr.ext_var,'$.apiSystem') = :system ");
            params.put("system", Integer.valueOf(approvalApiDto.getSystem()));
        }

        if (StringUtils.isNotBlank(approvalApiDto.getApiName())) {
            whereSql.append(" AND pa.name LIKE :apiName ");
            params.put("apiName", "%" + approvalApiDto.getApiName().trim() + "%");
        }

        String groupSql = " GROUP BY pr.rel_id ";
        String orderSql = " ORDER BY pr.create_time DESC ";
        if (sort != null && sort.length > 1) {//"sort":["ase","createTime"]
            orderSql = " ORDER BY pr.create_time" + sort[0];
        }

        String countSql = new StringBuilder().append(countSelectSql).append(selectSql).append(whereSql).append(groupSql)
                .append(" ) b").toString();

        Query countQuery = entityManager.createNativeQuery(countSql);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            countQuery.setParameter(entry.getKey(), entry.getValue());
        }

        List totalCountList = countQuery.getResultList();
        Long totalCount = 0L;
        if (null != totalCountList && totalCountList.size() > 0) {
            totalCount = Long.valueOf(String.valueOf(totalCountList.get(0)));
        }

        if (0L == totalCount) {
            returnResult.setCode(Result.OK);
            returnResult.setMsg("查询成功");
            returnResult.setData(new PageImpl<>(new ArrayList<>(1), pageable, totalCount));
            return returnResult;
        }

        String querySql = new StringBuilder().append(selectHeadSql).append(selectSql).append(whereSql)
                .append(groupSql).append(orderSql).toString();

        Query query = entityManager.createNativeQuery(querySql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Map<String, Object>> mapList = query.getResultList();
        List<ApprovalApiResDto> approvalApiResDtos = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Map<String, Object> map : mapList) {
            try {
                Date createTime = sdf.parse(String.valueOf(map.get("createTime")));
                ApprovalApiResDto approvalApiResDto = new ApprovalApiResDto();
                approvalApiResDto.setApiId(Integer.valueOf(String.valueOf(map.get("apiId"))));
                approvalApiResDto.setApiName(String.valueOf(map.get("apiName")));
                approvalApiResDto.setGroupId(Integer.valueOf(String.valueOf(map.get("groupId"))));
                approvalApiResDto.setSystemName(String.valueOf(map.get("systemName")));
                approvalApiResDto.setProcessRecordId(Integer.valueOf(String.valueOf(map.get("id"))));
                approvalApiResDto.setCreator(String.valueOf(map.get("createName")));
                approvalApiResDto.setProjectId(String.valueOf(map.get("projectId")));
                approvalApiResDto.setTenantId(String.valueOf(map.get("tenantId")));
                approvalApiResDto.setCreateTime(createTime);
                approvalApiResDto.setStatus(Integer.valueOf(String.valueOf(map.get("status"))));
                approvalApiResDto.setType(String.valueOf(map.get("type")));
                approvalApiResDto.setRemark(String.valueOf(map.get("remark")));

                String tenantId = String.valueOf(map.get("tenantId"));
                String tenantName = "hicloud";//permissionService.getTenantNameById(tenantId);
                approvalApiResDto.setTenantName(tenantName);

                String projectId = String.valueOf(map.get("projectId"));
                String projectName = "hicloud";//permissionService.getProjectNameById(projectId);
                approvalApiResDto.setProjectName(projectName);

                approvalApiResDtos.add(approvalApiResDto);
            } catch (ParseException e) {
                log.error("context",e);
            }
        }

        Page<ApprovalApiResDto> page = new PageImpl<>(approvalApiResDtos, pageable, totalCount);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(page);
        return returnResult;
    }

    @Transactional
    @Override
    public Result<Boolean> approvalApi(ApprovalApiDto approvalApiDto) {
        Result<Boolean> returnResult = new Result<>();
        // 查询发布表信息
        ProcessRecord processRecord = processRecordRepository.findOne(approvalApiDto.getProcessRecordId());

        // 更新api表状态
        PublishApi publishApi = publishApiRepository.findOne(approvalApiDto.getAppId());
        if (ModelConstant.ALLOW_STATUS.equals(approvalApiDto.getStatus())) {//审批通过
            publishApi.setIsOnline(1);
            //状态: 0:删除, 1:创建完成, 2:初始流程中, 3:二次流程中, 4:审批通过, 5:初始审批不通过（不允许订阅2-5）6:二次审批不通过（允许订阅3-6）
            publishApi.setStatus(ModelConstant.API_COMPLETE);
        } else if (ModelConstant.NO_ALLOW_STATUS .equals(approvalApiDto.getStatus())) {//审批不通过
            if (ModelConstant.API_FIRST_PROMOTE .equals(publishApi.getStatus())) {//2->5
                //审批驳回还是上下线状态不变
                publishApi.setStatus(ModelConstant.API_FIRST_PROMOTE_REJECT);
            } else if (ModelConstant.API_FOLLOWUP_PROMOTE.equals(publishApi.getStatus())) {//3->6
                publishApi.setStatus(ModelConstant.API_FOLLOWUP_PROMOTE_REJECT);
            } else if (ModelConstant.API_COMPLETE.equals(publishApi.getStatus())) {//4->6
                publishApi.setStatus(ModelConstant.API_FOLLOWUP_PROMOTE_REJECT);
            } else {//异常暂时认为是不允许订阅
                log.info("发布审批拒绝异常: appId is {}, processRecordId is {}",
                        approvalApiDto.getAppId(), approvalApiDto.getProcessRecordId());
                publishApi.setStatus(ModelConstant.API_FIRST_PROMOTE_REJECT);
            }
        }

        publishApi.setUpdateTime(new Date());
        publishApiRepository.saveAndFlush(publishApi);

        // 更新发布记录表的状态
        processRecord.setStatus(approvalApiDto.getStatus());
        processRecord.setRemark(approvalApiDto.getRemark());
        processRecord.setExtVar2(approvalApiDto.getSupplementContent());
        processRecord.setUpdateTime(new Date());
//        processRecord.setUpdater(SecurityUtils.getLoginUser().getUsername());
        processRecordRepository.saveAndFlush(processRecord);

        List<String> partitions = new ArrayList<>();
        String partition = PublishApiServiceImpl.getPartition(publishApi.getPartition());
        partitions.add(partition);
        // 发布到3scale
        if (ModelConstant.ALLOW_STATUS.equals(approvalApiDto.getStatus())) {//审批通过
            List<ApiInstance> apiInstances = pairRepository.getApiInstances(publishApi.getId(),
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
                } else {
                    throw new OperationFailed("proxyConfig not exist");
                }
            }
        }

        // 发送邮件，查询邮箱
        String extVar = processRecord.getExtVar();
        JSONObject extVarJson = JSONObject.parseObject(extVar);
        Integer groupId = extVarJson.getInteger("groupId");
        PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(groupId);
        String projectId = publishApiGroup.getProjectId();
        List<Map<String, Object>> emails = permissionService.findAdminEmailByProjectId(projectId,
                ModelConstant.PROJECT_ADMIN_ROLE_ID);
        String sendNick = "hicloud";
        String systemName = dataItemRepository.findOne(publishApiGroup.getSystem()).getItemName();
        String projectName = "hicloud";//permissionService.getProjectNameById(projectId);
        if (null == emails || emails.size() == 0) {
            log.error("发布审批发送邮件查询邮箱为空, projectId: {}, processRecordId: {}", projectId,
                    approvalApiDto.getProcessRecordId());
        } else {
            // 构建邮件内容
            String subject = "审批结束：" + systemName + "的系统申请" + projectName + "的项目" + publishApi.getName() + "API发布";
            List<String> receiveMails = new ArrayList<>();
            for (Map<String, Object> map : emails) {
                receiveMails.add(String.valueOf(map.get("email")));
            }

            String resultMsg = "已拒绝。";
            if (ModelConstant.ALLOW_STATUS.equals(approvalApiDto.getStatus())) {
                resultMsg = "已通过。";
            }
            String contents = "该申请审批流程已结束，审批结果：" + resultMsg + " 详细审批结果请登录平台进行查询,http://hicloud.hisense.com";
            try {
//                MailUtils.sendNormalEmail(receiveMails, null, subject, sendNick, contents);
            } catch (Exception e) {
                log.error("context",e);
            }
        }

        Boolean isCompatible = extVarJson.getBoolean("isCompatible");
        if (null != isCompatible) {
            if (!isCompatible) {//不向下兼容，则需要发邮件
                // 查询服务的订阅者
                List<PublishApplication> publishApplications =
                        publishApplicationRepository.findByAppId(publishApi.getId());
                if (null != publishApplications && publishApplications.size() > 0) {
                    List<String> applicationsEmails =
                            publishApplications.stream().map(PublishApplication::getCreator)
                                    /*.map(User::getEmail)*/.distinct().collect(Collectors.toList());

                    if (applicationsEmails.size() > 0) {
                        String applicationSubject = "变更：订购" + projectName + "的项目" + publishApi.getName() + "API，请查看";
                        String applicationContents = "该订购的API，调用方法有变更，请登录平台查询详细信息！";

                        try {
/*                            MailUtils.sendNormalEmail(applicationsEmails, null,
                                    applicationSubject, sendNick, applicationContents);*/
                        } catch (Exception e) {
                            log.error("context",e);
                        }
                    }
                }
            }
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("审批结束");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<ApprovalApiResDto> getApprovalRecord(Integer processRecordId, Integer type, String authorization) {
        Result<ApprovalApiResDto> returnResult = new Result<>();
        ApprovalApiResDto approvalApiResDto = new ApprovalApiResDto();
        //查询processRecord基本信息
        ProcessRecord processRecord = processRecordRepository.findOne(processRecordId);
        if (null == processRecord) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("查询失败,记录不存在");
            returnResult.setData(null);
            return returnResult;
        }

        //添加申请基本信息
        approvalApiResDto.setCreateTime(processRecord.getCreateTime());
        //approvalApiResDto.setCreateName(userRepository.findOne(processRecord.getCreateUser().getId()).getName());
        approvalApiResDto.setCreator(processRecord.getCreator());

        //查询api基本信息
        PublishApi publishApi = publishApiRepository.findOne(processRecord.getRelId());

        //添加API基本信息
        approvalApiResDto.setApiId(publishApi.getId());
        approvalApiResDto.setApiName(publishApi.getName());

        //由于分组会更改，所以从ext_var中查
        String extVar = processRecord.getExtVar();
        JSONObject extVarJson = JSONObject.parseObject(extVar);
        Integer groupId = extVarJson.getInteger("groupId");
        PublishApiGroup publishApiGroup = publishApiGroupRepository.findOne(groupId);
        approvalApiResDto.setApiGroupName(publishApiGroup.getName());
        String tenantId = publishApiGroup.getTenantId();
        String tenantName = "hicloud";//permissionService.getTenantNameById(tenantId);
        approvalApiResDto.setTenantName(tenantName);
        String projectId = publishApiGroup.getProjectId();
        String projectName = "hicloud";//permissionService.getProjectNameById(projectId);
        approvalApiResDto.setProjectName(projectName);

        List<Integer> dataItemIds = new ArrayList<>(3);
        dataItemIds.add(publishApiGroup.getCategoryOne());
        dataItemIds.add(publishApiGroup.getCategoryTwo());
        dataItemIds.add(publishApiGroup.getSystem());

        List<DataItem> dataItemList = dataItemRepository.findByIds(dataItemIds);
        for (DataItem dataItem : dataItemList) {
            if (dataItem.getId().equals(publishApiGroup.getSystem())) {
                approvalApiResDto.setSystemName(dataItem.getItemName());
            } else if (dataItem.getId().equals(publishApiGroup.getCategoryOne())) {
                approvalApiResDto.setCategoryOneName(dataItem.getItemName());
            } else if (dataItem.getId().equals(publishApiGroup.getCategoryTwo())) {
                approvalApiResDto.setCategoryTwoName(dataItem.getItemName());
            }
        }

        if (1 == type) {// 已办理，发布审批
            approvalApiResDto.setStatus(processRecord.getStatus());
            approvalApiResDto.setUpdateTime(processRecord.getUpdateTime());
            approvalApiResDto.setUpdater(processRecord.getUpdater());
            approvalApiResDto.setRemark(processRecord.getRemark());
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(approvalApiResDto);
        return returnResult;
    }

    @Override
    public Result<PublishApiDto> getApprovalRecordDetails(Integer processRecordId) {
        Result<PublishApiDto> returnResult = new Result<>();
        PublishApiDto publishApiDto = new PublishApiDto();
        // 查询processRecord基本信息
        ProcessRecord processRecord = processRecordRepository.findOne(processRecordId);
        PublishApi publishApi = publishApiRepository.findOne(processRecord.getRelId());
        publishApiDto.setName(publishApi.getName());
        publishApiDto.setAuthType(ModelConstant.API_AUTH);

        //由于分组会更改，所以从ext_var中查
        String extVar = processRecord.getExtVar();
        JSONObject extVarJson = JSONObject.parseObject(extVar);
        String url = extVarJson.getString("url");
        String clusterPartitions = extVarJson.getString("clusterPartitions");
        publishApiDto.setUrl(url);
        publishApiDto.setAccessProtocol(ModelConstant.Protocol_HTTP);
        publishApiDto.setHost(publishApi.getHost());
        publishApiDto.setPort(publishApi.getPort());

        // 发布环境
        List<String> partitions = new ArrayList<>();
        List<ApiInstanceDto> apiInstanceDtos = pairRepository.getApiInstanceDtos(processRecord.getRelId());
        if (null != apiInstanceDtos && apiInstanceDtos.size() > 0) {
            for (ApiInstanceDto apiInstanceDto : apiInstanceDtos) {
                partitions.add(apiInstanceDto.getClusterPartition());
            }
            publishApiDto.setRequestProduction(apiInstanceDtos.get(0).getRequestSandbox());
        } else {
            if (StringUtils.isNotBlank(clusterPartitions)) {
                partitions.addAll(JSONArray.parseArray(clusterPartitions, String.class));
            }
        }

        publishApiDto.setPartitions(partitions);
        ProxyConfigDto proxyConfigDto = JSONObject.parseObject(processRecord.getData(), ProxyConfigDto.class);
        if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()
                && null != proxyConfigDto.getProxyConfig().getContent()
                && null != proxyConfigDto.getProxyConfig().getContent().getProxy()) {
            Proxy proxy = proxyConfigDto.getProxyConfig().getContent().getProxy();
            publishApiDto.setRealmName(proxy.getHostnameRewrite());
            String apiBackend = proxy.getApiBackend();
            if (apiBackend.contains("https")) {
                publishApiDto.setAccessProtocol(ModelConstant.Protocol_HTTPS);
            }

            String[] hostAndPort = apiBackend.split("//")[1].split(":");
            publishApiDto.setHost(hostAndPort[0]);
            publishApiDto.setPort(hostAndPort[1]);

            if (null == apiInstanceDtos && apiInstanceDtos.size() == 0) {
                publishApiDto.setRequestProduction(proxy.getEndpoint());
            }

            List<ProxyPolicy> proxyPolicies = proxy.getPolicyChain();
            for (ProxyPolicy proxyPolicy : proxyPolicies) {//是否鉴权
                if (ModelConstant.API_ANONYMOUS_POLICY_NAME.equals(proxyPolicy.getName())) {
                    publishApiDto.setAuthType(ModelConstant.API_NOAUTH);
                    break;
                }
            }
            publishApiDto.setProxy(proxyConfigDto.getProxyConfig().getContent().getProxy());
        }

        if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()
                && null != proxyConfigDto.getProxyConfig().getContent()
                && null != proxyConfigDto.getProxyConfig().getContent().getProxy()
                && null != proxyConfigDto.getProxyConfig().getContent().getProxy().getProxyRules()) {
            publishApiDto.setApiMappingRuleDtos(parseMappingRule(proxyConfigDto.getProxyConfig().
                    getContent().getProxy().getProxyRules(), url));
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(publishApiDto);
        return returnResult;
    }

    @Override
    public Result<List<Map<String, String>>> findTenantList(String authorization) {
        Result<List<Map<String, String>>> returnResult = new Result<>();
        List<Map<String, String>> returnList = new ArrayList<>();
        try {
            String rlt =
                    HttpUtil.sendGetAndAuthorization(paasApiUrl + "/api/v2/tenants?from=0&size=0", authorization);
            log.info("***end to invoke findTenantList, authorization is {}, rlt is {}", authorization, rlt);

            if (StringUtils.isBlank(rlt)) {
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("查询失败");
                returnResult.setData(returnList);
                return returnResult;
            } else {
                JSONObject rltObject = JSONObject.parseObject(rlt);
                if ("Success".equals(rltObject.getString("status"))) {
                    JSONArray dataArray = rltObject.getJSONObject("data").getJSONArray("data");
                    for (int i = 0, size = dataArray.size(); i < size; i++) {
                        Map<String, String> returnMap = new HashMap<>();
                        JSONObject tenantJSONObject = dataArray.getJSONObject(i);
                        returnMap.put("id", tenantJSONObject.getString("id"));
                        returnMap.put("name", tenantJSONObject.getString("name"));
                        returnList.add(returnMap);
                    }
                }
            }
        } catch (Exception e) {
            log.error("context",e);
        }

        tenantList = new ArrayList<>();
        tenantList.addAll(returnList);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(returnList);
        return returnResult;
    }

    @Override
    public Result<List<Map<String, String>>> findProjectList(String authorization, String tenantId) {
        Result<List<Map<String, String>>> returnResult = new Result<>();
        List<Map<String, String>> returnList = new ArrayList<>();
        try {
            String rlt =
                    HttpUtil.sendGetAndAuthorization(paasApiUrl + "/api/v2/tenants/" + tenantId +
                            "/projects?from=0&size=0", authorization);
            log.info("***end to invoke findProjectList, authorization is {},tenantId is {}, rlt is {}",
                    authorization, tenantId, rlt);

            if (StringUtils.isBlank(rlt)) {
                returnResult.setCode(Result.FAIL);
                returnResult.setMsg("查询失败");
                returnResult.setData(returnList);
                return returnResult;
            } else {
                JSONObject rltObject = JSONObject.parseObject(rlt);
                if ("Success".equals(rltObject.getString("status"))) {
                    JSONArray dataArray = rltObject.getJSONObject("data").getJSONArray("projects");
                    for (int i = 0, size = dataArray.size(); i < size; i++) {
                        Map<String, String> returnMap = new HashMap<>();
                        JSONObject tenantJSONObject = dataArray.getJSONObject(i);
                        returnMap.put("id", tenantJSONObject.getString("projectID"));
                        returnMap.put("name", tenantJSONObject.getString("projectName"));
                        returnList.add(returnMap);
                    }
                }
            }
        } catch (Exception e) {
            log.error("context",e);
        }

        projectList = new ArrayList<>();
        projectList.addAll(returnList);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(returnList);
        return returnResult;
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
}
