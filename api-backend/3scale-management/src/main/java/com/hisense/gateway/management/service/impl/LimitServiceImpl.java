/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/20
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.LimitBingApiPolicy;
import com.hisense.gateway.library.model.dto.web.LimitPolicyDto;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.stud.*;
import com.hisense.gateway.library.stud.model.ProxyConfigDto;
import com.hisense.gateway.management.service.LimitService;
import com.hisense.gateway.library.stud.model.Limit;
import com.hisense.gateway.library.stud.model.MetricsDto;
import com.hisense.gateway.library.utils.RequestUtils;
import com.hisense.gateway.library.web.form.BindingForm;
import lombok.extern.slf4j.Slf4j;
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

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.criteria.Predicate;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@Service
public class LimitServiceImpl implements LimitService {
    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    ApiPolicyRepository apiPolicyRepository;

    @Autowired
    LimitStud limitStud;

    @Autowired
    PublishApiPlanRepository publishApiPlanRepository;

    @Autowired
    PublishApiPolicyRelationshipRepository paprRepository;

    @Autowired
    ApplicationPlanStud applicationPlanStud;

    @Autowired
    MetricStud metricStud;

    @Autowired
    PublishApiRepository publishApiRepository;

    @Autowired
    PublishApiPolicyRelationshipRepository publishApiPolicyRelationshipRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository pairRepository;

    @Autowired
    ProxyPoliciesStud proxyPoliciesStud;

    @Autowired
    ServiceStud serviceStud;

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Result<Boolean> bindingApi(String clusterId, List<BindingForm> bindingFormList, String policyId) {
        Result<Boolean> returnResult = new Result<>();
        ApiPolicy apiPolicy = apiPolicyRepository.getOne(Integer.valueOf(policyId));

        if (!apiPolicy.getEnabled()) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("策略关闭,禁止绑定");
            returnResult.setData(false);
            return returnResult;
        }

        JSONObject configJson = JSONObject.parseObject(apiPolicy.getConfig());
        for (BindingForm bindingForm : bindingFormList) {
            List<String> partitionList = bindingForm.getPartitions();
            for (String partition : partitionList) {
                Instance instance = instanceRepository.searchInstanceByClusterIdAndPartition(clusterId, partition);
                if (null == instance) {
                    log.error("can not found instance, clusterId is {}, partition is {}", clusterId, partition);
                    throw new OperationFailed("instance not exist");
                }

                String host = instance.getHost();
                String accessToken = instance.getAccessToken();
                Limit limit = new Limit();
                PublishApiPlan publishApiPlan =
                        publishApiPlanRepository.findByApiIdAndInstanceId(bindingForm.getApiId(), instance.getId());
                if (null == publishApiPlan) {
                    throw new NotExist(String.format("apiPlan with apiId %s not exist", bindingForm.getApiId()));
                }

                Long scalePlanId = publishApiPlan.getScalePlanId();
                if (bindingForm.getBindingPolicyId() == null || bindingForm.getBindingPolicyId().equals("")) {
                    //如果Api没有绑定过limit,则新建限流规则
                    PublishApiInstanceRelationship publishApiInstanceRelationship =
                            publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(bindingForm.getApiId(),
                                    instance.getId());
                    MetricsDto metricsDto = metricStud.getMetricByServiceId(host, accessToken,
                            publishApiInstanceRelationship.getScaleApiId().toString());
                    limit.setPlanId(scalePlanId.toString());
                    limit.setMetricId(metricsDto.getMetrics().get(0).getMetric().getId());
                    limit.setPeriod(configJson.getString("period"));
                    limit.setValue(configJson.getString("value"));
                    limit = limitStud.limitCreate(host, accessToken, limit);
                    if (limit == null) {
                        throw new NotExist(String.format("api 绑定失败, apiId is %s", bindingForm.getApiId()));
                    }

                    //每次新建将api和policy的关系入库
                    PublishApiPolicyRelationship publishApiPolicyRelationship = new PublishApiPolicyRelationship();
                    publishApiPolicyRelationship.setPublishApiId(bindingForm.getApiId());
                    publishApiPolicyRelationship.setPublishPolicyId(Integer.valueOf(policyId));
                    publishApiPolicyRelationship.setScalePolicyId(Long.valueOf(limit.getId()));
                    publishApiPolicyRelationship.setInstanceId(instance.getId());
                    publishApiPolicyRelationship.setMetricId(Long.valueOf(limit.getMetricId()));
                    publishApiPolicyRelationship.setCreateTime(new Date());
                    publishApiPolicyRelationshipRepository.save(publishApiPolicyRelationship);
                } else if (!bindingForm.getBindingPolicyId().equals(policyId)) {
                    PublishApiPolicyRelationship publishApiPolicyRelationship =
                            publishApiPolicyRelationshipRepository.findAllByApiIdAndInstanceId(bindingForm.getApiId()
                                    , instance.getId());
                    limit.setMetricId(publishApiPolicyRelationship.getMetricId().toString());
                    limit.setPlanId(scalePlanId.toString());
                    limit.setId(publishApiPolicyRelationship.getScalePolicyId().toString());
                    limit.setPeriod(configJson.getString("period"));
                    limit.setValue(configJson.getInteger("value").toString());

                    // 每个API只能由一个限流规则,若一个API已经绑定了一个限流规则,那么再次绑定的时候实际上是更新原有限流规则
                    limitStud.limitUpdate(host, accessToken, limit);
                    // 更新关系表
                    publishApiPolicyRelationshipRepository.updatePublishPolicyIdById(publishApiPolicyRelationship.getId(), Integer.valueOf(policyId));
                }
            }
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("绑定成功");
        returnResult.setData(true);
        returnResult.setAlert(1);
        return returnResult;
    }

    @Override
    public Result<Boolean> unbindApi(String clusterId, BindingForm bindingForm, String policyId) {
        Result<Boolean> returnResult = new Result<>();
        ApiPolicy apiPolicy = apiPolicyRepository.getOne(Integer.valueOf(policyId));

        if (!apiPolicy.getEnabled()) {
            publishApiPolicyRelationshipRepository.deleteByApiId(bindingForm.getApiId());
            returnResult.setCode(Result.OK);
            returnResult.setMsg("解绑成功");
            returnResult.setData(true);
            returnResult.setAlert(1);
            return returnResult;
        }

        List<String> partitionList = bindingForm.getPartitions();
        for (String partition : partitionList) {
            Instance instance = instanceRepository.searchInstanceByClusterIdAndPartition(clusterId, partition);
            if (instance == null) {
                log.error("can not found instance clusterId is {},partition is {}", clusterId, partition);
                throw new OperationFailed("instance not exist");
            }

            PublishApiPolicyRelationship publishApiPolicyRelationship =
                    publishApiPolicyRelationshipRepository.findAllByApiIdAndInstanceId(bindingForm.getApiId(),
                            instance.getId());
            String host = instance.getHost();
            String accessToken = instance.getAccessToken();
            Limit limit = new Limit();
            PublishApiPlan publishApiPlan = publishApiPlanRepository.findByApiIdAndInstanceId(bindingForm.getApiId(),
                    instance.getId());
            if (publishApiPlan == null) {
                throw new NotExist(String.format("apiPlan with apiId %s not exist", bindingForm.getApiId()));
            }

            limit.setMetricId(publishApiPolicyRelationship.getMetricId().toString());
            limit.setPlanId(publishApiPlan.getScalePlanId().toString());
            limit.setId(publishApiPolicyRelationship.getScalePolicyId().toString());
            // 删除3scale的limit数据
            limitStud.limitDelete(host, accessToken, limit);
            // 删除关系表数据
            publishApiPolicyRelationshipRepository.deleteRel(publishApiPolicyRelationship.getId());
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("解绑成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    @Transactional
    public Result<Boolean> createLimitPolicy(String projectId, LimitPolicyDto limitPolicyDto) {
        Result<Boolean> returnResult = new Result<>();
        if(!limitPolicyDto.getEnabled()){
            returnResult.setCode(Result.OK);
            returnResult.setData(false);
            return returnResult;
        }
        Integer apiId = limitPolicyDto.getApiId();
        if (limitPolicyDto.getApiId() == null) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("apiId不能为空");
            returnResult.setData(false);
            return returnResult;
        }

        List<PublishApiPolicyRelationship> apiPolicies = paprRepository.findPublishByApiId(apiId);
        if (apiPolicies != null && apiPolicies.size() > 0) {
            for(PublishApiPolicyRelationship ship:apiPolicies){
                Integer publishPolicyId = ship.getPublishPolicyId();
                updateLimitPolicy(publishPolicyId,null,projectId,limitPolicyDto);
            }
        } else {
            List<PublishApiInstanceRelationship > publishApiInstance = publishApiInstanceRelationshipRepository.getByAPIid(limitPolicyDto.getApiId());
            if (null == publishApiInstance||publishApiInstance.size()==0){
                throw new NotExist(String.format("apiPlan with apiId %s not exist", limitPolicyDto.getApiId()));
            }

            JSONObject config = new JSONObject();
            config.put("value", limitPolicyDto.getValue());
            config.put("period", limitPolicyDto.getPeriod());
            String period = limitPolicyDto.getPeriod();
            long window =getSecond(period);
            int count = Integer.parseInt(limitPolicyDto.getValue());
      /*      String user = SecurityUtils.getLoginUser().getUsername();
            ApiPolicy apiPolicy = new ApiPolicy(limitPolicyDto.getName(), limitPolicyDto.getDescription(),
                    JSONObject.toJSONString(config), limitPolicyDto.getEnabled(), "limit", projectId, user,
                    new Date(), new Date());
            ApiPolicy policy = apiPolicyRepository.save(apiPolicy);*/

            for(PublishApiInstanceRelationship plan: publishApiInstance){
                Instance instance = instanceRepository.getOne(plan.getInstanceId());
                String host =instance.getHost();
                String accessToken = instance.getAccessToken();
                PublishApiPolicyRelationship publishApiPolicyRelationship = new PublishApiPolicyRelationship();
                publishApiPolicyRelationship.setPublishApiId(limitPolicyDto.getApiId());
//                publishApiPolicyRelationship.setPublishPolicyId(policy.getId());
                publishApiPolicyRelationship.setInstanceId(instance.getId());
                publishApiPolicyRelationship.setCreateTime(new Date());
                publishApiPolicyRelationshipRepository.save(publishApiPolicyRelationship);
                if(limitPolicyDto.getEnabled()){
                    //生成策略
                    proxyPoliciesStud.openEdgeLimiting(host,accessToken,String.valueOf(plan.getScaleApiId()),window,count);
                    ProxyConfigDto proxyConfigDto = serviceStud.latestPromote(host, accessToken,plan.getScaleApiId(), ModelConstant.ENV_SANDBOX);
                    if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()&& null != proxyConfigDto.getProxyConfig().getVersion()) {
                        serviceStud.configPromote(host, accessToken, String.valueOf(plan.getScaleApiId()),
                                ModelConstant.ENV_SANDBOX, proxyConfigDto.getProxyConfig().getVersion(),
                                ModelConstant.ENV_PROD);
                    } else {
                        log.error("proxyConfig not exist");
                    }
                }

//                if(limitPolicyDto.getEnabled()){
//                    MetricsDto metricsDto = metricStud.getMetricByServiceId(host, accessToken,plan.getScaleApiId().toString());
//                    Limit limit = new Limit();
//                    limit.setPlanId(scalePlanId.toString());
//                    limit.setMetricId(metricsDto.getMetrics().get(0).getMetric().getId());
//                    limit.setPeriod(config.getString("period"));
//                    limit.setValue(config.getString("value"));
//                    limit = limitStud.limitCreate(host,accessToken,limit);
//                    if(limit==null){
//                        throw new NotExist(String.format("api 绑定失败, apiId is %s", limitPolicyDto.getApiId()));
//                    }
//                    applicationPlanStud.updateApplicationPlan(host,accessToken,String.valueOf(plan.getScaleApiId()),String.valueOf(scalePlanId),1);
//                    PublishApiPolicyRelationship publishApiPolicyRelationship = new PublishApiPolicyRelationship();
//                    publishApiPolicyRelationship.setPublishApiId(limitPolicyDto.getApiId());
//                    publishApiPolicyRelationship.setPublishPolicyId(Integer.valueOf(policy.getId()));
//                    publishApiPolicyRelationship.setScalePolicyId(Long.valueOf(limit.getId()));
//                    publishApiPolicyRelationship.setInstanceId(instance.getId());
//                    publishApiPolicyRelationship.setMetricId(Long.valueOf(limit.getMetricId()));
//                    publishApiPolicyRelationship.setCreateTime(new Date());
//                    publishApiPolicyRelationshipRepository.save(publishApiPolicyRelationship);
//                } else {
//                    applicationPlanStud.updateApplicationPlan(host,accessToken,String.valueOf(plan.getScaleApiId()),String.valueOf(scalePlanId),1);
//                    PublishApiPolicyRelationship publishApiPolicyRelationship = new PublishApiPolicyRelationship();
//                    publishApiPolicyRelationship.setPublishApiId(limitPolicyDto.getApiId());
//                    publishApiPolicyRelationship.setPublishPolicyId(Integer.valueOf(policy.getId()));
//                    publishApiPolicyRelationship.setInstanceId(instance.getId());
//                    publishApiPolicyRelationship.setCreateTime(new Date());
//                    publishApiPolicyRelationshipRepository.save(publishApiPolicyRelationship);
//                }
            }
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("创建成功");
        returnResult.setData(true);
        returnResult.setAlert(1);
        return returnResult;
    }

    @Override
    @Transactional
    public Result<Boolean> updateLimitPolicy(Integer id, String clusterId, String projectId,
                                             LimitPolicyDto limitPolicyDto) {
        Result<Boolean> returnResult = new Result<>();
        ApiPolicy apiPolicy = apiPolicyRepository.findOne(id);
        JSONObject configJson = JSONObject.parseObject(apiPolicy.getConfig());
        String period = configJson.getString("period");
        String value = configJson.getString("value");
        long window =getSecond(limitPolicyDto.getPeriod());
        int count = Integer.parseInt(limitPolicyDto.getValue());
        List<PublishApiPolicyRelationship> paprs = paprRepository.findAllByPoliciId(id);
        if (paprs != null && paprs.size() > 0) {//已绑定
            for (PublishApiPolicyRelationship papr : paprs) {
                // 确定Instance
                Instance instance = instanceRepository.getOne(papr.getInstanceId());
                String host = instance.getHost();
                String accessToken = instance.getAccessToken();
                PublishApiPlan publishApiPlan = publishApiPlanRepository.findByApiIdAndInstanceId(papr.getPublishApiId(), instance.getId());
                PublishApiInstanceRelationship apiInstanceRelationship = publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(papr.getPublishApiId(), instance.getId());
                Long scalePlanId = publishApiPlan.getScalePlanId();
//                Limit limit = new Limit();
//                limit.setMetricId(String.valueOf(papr.getMetricId()));
//                limit.setPlanId(String.valueOf(scalePlanId));
                if (apiPolicy.getEnabled() && limitPolicyDto.getEnabled()&& (!period.equals(limitPolicyDto.getPeriod()) || !value.equals(limitPolicyDto.getValue()))) {
//                    // 状态始终为开启,而且策略有变动, 更新所有绑定的3scale的数据
//                    limit.setId(String.valueOf(papr.getScalePolicyId()));
//                    limit.setPeriod(limitPolicyDto.getPeriod());
//                    limit.setValue(limitPolicyDto.getValue());
//                    limitStud.limitUpdate(host, accessToken, limit);
//                    applicationPlanStud.updateApplicationPlan(host,accessToken,String.valueOf(apiInstanceRelationship.getScaleApiId()),String.valueOf(scalePlanId),1);
                    proxyPoliciesStud.openEdgeLimiting(host,accessToken,String.valueOf(apiInstanceRelationship.getScaleApiId()),window,count);

                    ProxyConfigDto proxyConfigDto = serviceStud.latestPromote(host, accessToken, apiInstanceRelationship.getScaleApiId(), ModelConstant.ENV_SANDBOX);
                    if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()&& null != proxyConfigDto.getProxyConfig().getVersion()) {
                        serviceStud.configPromote(host, accessToken, String.valueOf(apiInstanceRelationship.getScaleApiId()),
                                ModelConstant.ENV_SANDBOX, proxyConfigDto.getProxyConfig().getVersion(),
                                ModelConstant.ENV_PROD);
                    } else {
                        log.error("proxyConfig not exist");
                    }
//                    serviceStud.latestPromote(host, accessToken, new Long(instance.getId()), ModelConstant.ENV_PROD);
                } else if (apiPolicy.getEnabled() && !limitPolicyDto.getEnabled()) {
                    // 状态由开启变为关闭,删除所有3scale的limit,同时将关系表中scalePolicyId置为Null;
//                    limit.setId(String.valueOf(papr.getScalePolicyId()));
//                    limitStud.limitDelete(host, accessToken, limit);
//                    applicationPlanStud.updateApplicationPlan(host,accessToken,String.valueOf(apiInstanceRelationship.getScaleApiId()),String.valueOf(scalePlanId),0);
                    papr.setScalePolicyId(null);
                    publishApiPolicyRelationshipRepository.saveAndFlush(papr);
                    proxyPoliciesStud.closeEdgeLimiting(host,accessToken,String.valueOf(apiInstanceRelationship.getScaleApiId()));
                    ProxyConfigDto proxyConfigDto = serviceStud.latestPromote(host, accessToken, apiInstanceRelationship.getScaleApiId(), ModelConstant.ENV_SANDBOX);
                    if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()&& null != proxyConfigDto.getProxyConfig().getVersion()) {
                        serviceStud.configPromote(host, accessToken, String.valueOf(apiInstanceRelationship.getScaleApiId()),
                                ModelConstant.ENV_SANDBOX, proxyConfigDto.getProxyConfig().getVersion(),
                                ModelConstant.ENV_PROD);
                    } else {
                        log.error("proxyConfig not exist");
                    }
//                    serviceStud.latestPromote(host, accessToken, new Long(instance.getId()), ModelConstant.ENV_PROD);
                } else if (!apiPolicy.getEnabled() && limitPolicyDto.getEnabled()) {
                    // 状态从关闭变为开启,创建所有已绑定的3scale的limit，同时将关系表scalePolicyId填值
//                    limit.setPeriod(limitPolicyDto.getPeriod());
//                    limit.setValue(limitPolicyDto.getValue());
//                    limit = limitStud.limitCreate(host, accessToken, limit);
//                    papr.setScalePolicyId(Long.valueOf(limit.getId()));
                    publishApiPolicyRelationshipRepository.saveAndFlush(papr);
                    proxyPoliciesStud.openEdgeLimiting(host,accessToken,String.valueOf(apiInstanceRelationship.getScaleApiId()),window,count);
                    ProxyConfigDto proxyConfigDto = serviceStud.latestPromote(host, accessToken, apiInstanceRelationship.getScaleApiId(), ModelConstant.ENV_SANDBOX);
                    if (null != proxyConfigDto && null != proxyConfigDto.getProxyConfig()&& null != proxyConfigDto.getProxyConfig().getVersion()) {
                        serviceStud.configPromote(host, accessToken, String.valueOf(apiInstanceRelationship.getScaleApiId()),
                                ModelConstant.ENV_SANDBOX, proxyConfigDto.getProxyConfig().getVersion(),
                                ModelConstant.ENV_PROD);
                    } else {
                        log.error("proxyConfig not exist");
                    }
//                    serviceStud.latestPromote(host, accessToken, new Long(instance.getId()), ModelConstant.ENV_PROD);
//                    applicationPlanStud.updateApplicationPlan(host,accessToken,String.valueOf(apiInstanceRelationship.getScaleApiId()),String.valueOf(scalePlanId),1);
                }
            }
        }
        // 状态始终为关闭, 或者状态始终未开启,但policy值未改变,不做任何特殊处理
        JSONObject config = new JSONObject();
        config.put("value", limitPolicyDto.getValue());
        config.put("period", limitPolicyDto.getPeriod());
        apiPolicy.setConfig(JSONObject.toJSONString(config));
        apiPolicy.setName(limitPolicyDto.getName());
        apiPolicy.setDescription(limitPolicyDto.getDescription());
        apiPolicy.setEnabled(limitPolicyDto.getEnabled());
        apiPolicy.setUpdateTime(new Date());
        apiPolicyRepository.saveAndFlush(apiPolicy);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("更新成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public List<ApiInstance> getInstances(Integer policyid) {
        List<ApiInstance> instances = new ArrayList<>();
        List<PublishApiPolicyRelationship> paprs = paprRepository.findAllByPoliciId(policyid);
        if (paprs != null && paprs.size() > 0) {// 已绑定
            Set<Integer> apiIdSet = new HashSet<>();
            for (PublishApiPolicyRelationship papr : paprs) {
                apiIdSet.add(papr.getPublishApiId());
            }
            // 确定Instance
            instances = pairRepository.findInstancesByApiIds(apiIdSet);
        }
        return instances;
    }

    @Override
    public Result<Boolean> checkLimitPolicyName(String projectId, LimitPolicyDto limitPolicyDto) {
        Result<Boolean> returnResult = new Result<>();
        List<ApiPolicy> apiPolicies = apiPolicyRepository.findByProjectAndName(projectId, limitPolicyDto.getName());
        if (apiPolicies != null && apiPolicies.size() > 0) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("同一个项目下名字不允许重复:" + apiPolicies.get(0).getName());
            returnResult.setData(false);
            return returnResult;
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("名称可用");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<Page<LimitBingApiPolicy>> searchLimitBingApiPolicy(String clusterId, String projectId,
                                                                     LimitPolicyDto limitPolicyDto) {
        Result<Page<LimitBingApiPolicy>> returnResult = new Result<>();
        String[] sort = limitPolicyDto.getSort();
        PageRequest pageable = PageRequest.of(limitPolicyDto.getPageNum() - 1, limitPolicyDto.getPageSize());
        StringBuilder countSelectSql = new StringBuilder("");
        countSelectSql.append(" SELECT count(num) FROM (SELECT count(1) num ");

        StringBuilder selectHeadSql = new StringBuilder();
        selectHeadSql.append(" SELECT papr.instance_id as instanceId,papr.publish_api_id as apiId,papr" +
                ".publish_policy_id as policyId, ");
        selectHeadSql.append(" papr.scale_policy_id as scalePolicyId,pa.name AS apiName, pa.group_id AS groupId,pag" +
                ".name AS groupName, ");
        selectHeadSql.append(" group_concat(gi.cluster_partition) as clusterPartition,papr.create_time as createTime ");

        StringBuilder selectSql = new StringBuilder();
        selectSql.append(" FROM publish_api_policy_relationship papr ");
        selectSql.append(" LEFT JOIN publish_api pa ON papr.publish_api_id = pa.id ");
        selectSql.append(" LEFT JOIN publish_api_group pag ON pa.group_id = pag.id ");
        selectSql.append(" LEFT JOIN gw_instance gi ON gi.id = papr.instance_id ");
        selectSql.append(" WHERE ");
        selectSql.append(" papr.publish_policy_id =:policyId ");

        Map<String, Object> params = new HashMap<>();
        params.put("policyId", limitPolicyDto.getPolicyId());

        StringBuilder whereSql = new StringBuilder();
        if (StringUtils.isNotBlank(limitPolicyDto.getName())) {
            whereSql.append(" AND pa.name LIKE :apiName ");
            String limitPolicyName = limitPolicyDto.getName();
            try {
                limitPolicyName = URLDecoder.decode(limitPolicyDto.getName().trim(), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                log.error("context",e);
            }

            params.put("apiName", "%" + limitPolicyName + "%");
        }

        String groupSql = " GROUP BY pa.id ";
        String orderSql = " ORDER BY papr.create_time DESC ";

        if (sort != null && sort.length > 1) {
            orderSql = " ORDER BY papr.create_time " + sort[0];
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
            returnResult.setData(new PageImpl<>(new ArrayList<LimitBingApiPolicy>(1), pageable, totalCount));
            return returnResult;
        }

        String querySql = new StringBuilder().append(selectHeadSql).append(selectSql).append(whereSql).
                append(groupSql).append(orderSql).toString();
        Query query = entityManager.createNativeQuery(querySql);
        query.unwrap(SQLQuery.class).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP);
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            query.setParameter(entry.getKey(), entry.getValue());
        }
        query.setFirstResult((int) pageable.getOffset());
        query.setMaxResults(pageable.getPageSize());
        List<Map<String, Object>> mapList = query.getResultList();
        List<LimitBingApiPolicy> limitBingApiPolicies = new ArrayList<>();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (Map<String, Object> map : mapList) {
            try {
                Date createTime = null;
                if (null != map.get("createTime")) {
                    createTime = sdf.parse(String.valueOf(map.get("createTime")));
                }

                LimitBingApiPolicy limitBingApiPolicy =
                        new LimitBingApiPolicy(Integer.valueOf(String.valueOf(map.get("instanceId"))),
                                Integer.valueOf(String.valueOf(map.get("apiId"))),
                                Integer.valueOf(String.valueOf(map.get("policyId"))),
                                String.valueOf(map.get("scalePolicyId")), String.valueOf(map.get("apiName")),
                                Integer.valueOf(String.valueOf(map.get("groupId"))), String.valueOf(map.get(
                                "groupName")),
                                String.valueOf(map.get("clusterPartition")), createTime);
                limitBingApiPolicies.add(limitBingApiPolicy);
            } catch (ParseException e) {
                log.error("context",e);
            }
        }

        Page<LimitBingApiPolicy> page = new PageImpl<>(limitBingApiPolicies, pageable, totalCount);

        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(page);
        return returnResult;
    }

    @Override
    public Result<Page<ApiPolicy>> searchLimitPolicy(String clusterId, String projectId,
                                                     LimitPolicyDto limitPolicyDto) {
        Result<Page<ApiPolicy>> returnResult = new Result<>();
        String[] sort = limitPolicyDto.getSort();
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if (sort != null && sort.length > 1) {
            direction = "d".equalsIgnoreCase(sort[0]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = sort[1];
        }

        PageRequest pageable = PageRequest.of(limitPolicyDto.getPageNum() - 1, limitPolicyDto.getPageSize(),
                Sort.by(direction, property));
        Specification<ApiPolicy> spec = (root, query, builder) -> {
            List<Predicate> andList = new LinkedList<>();
            andList.add(builder.equal(root.get("projectId").as(String.class), projectId));
            if (StringUtils.isNotBlank(limitPolicyDto.getName())) {
                String limitPolicyName = limitPolicyDto.getName();
                try {
                    limitPolicyName = URLDecoder.decode(limitPolicyDto.getName().trim(), "UTF-8");
                } catch (UnsupportedEncodingException e) {
                    log.error("context",e);
                }
                andList.add(builder.like(root.get("name").as(String.class), "%" + limitPolicyName + "%",
                        GatewayConstants.ESCAPECHAR));
            }
            return builder.and(andList.toArray(new Predicate[andList.size()]));
        };
        Page<ApiPolicy> apiPolicies = apiPolicyRepository.findAll(spec, pageable);
        List<ApiPolicy> apiPolicyList = apiPolicies.getContent();
        List<ApiPolicy> apiPoliciesRes = new ArrayList<>(apiPolicyList.size());
        for (ApiPolicy apiPolicy : apiPolicyList) {//查询绑定的API数量
            Integer bindApiNum = paprRepository.findNumByPolicyId(apiPolicy.getId());
            apiPolicy.setBindApiNum(bindApiNum);
            apiPoliciesRes.add(apiPolicy);
        }
        Page<ApiPolicy> data = new PageImpl<>(apiPoliciesRes, pageable, apiPolicies.getTotalElements());
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(data);
        return returnResult;
    }

    @Override
    public Result<Boolean> deleteLimitPolicy(Integer id, String clusterId) {
        Result<Boolean> returnResult = new Result<>();
        List<PublishApiPolicyRelationship> paprs = paprRepository.findAllByPoliciId(id);
        if (null != paprs && paprs.size() > 0) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("已绑定API，请先解绑");
            returnResult.setData(false);
            return returnResult;
        }

        // 策略表删除,关系表删除
        apiPolicyRepository.deleteById(id);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("删除成功");
        returnResult.setData(true);
        return returnResult;
    }

    @Override
    public Result<LimitPolicyDto> getLimitPolicyById(Integer id) {
        Result<LimitPolicyDto> returnResult = new Result<>();
        LimitPolicyDto limitPolicyDto = new LimitPolicyDto();
        ApiPolicy apiPolicy = apiPolicyRepository.findOne(id);
        JSONObject configJson = JSONObject.parseObject(apiPolicy.getConfig());
        limitPolicyDto.setName(apiPolicy.getName());
        limitPolicyDto.setDescription(apiPolicy.getDescription());
        limitPolicyDto.setEnabled(apiPolicy.getEnabled());
        limitPolicyDto.setPeriod(configJson.getString("period"));
        limitPolicyDto.setValue(configJson.getString("value"));
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(limitPolicyDto);
        return returnResult;
    }

    private long getSecond(String period){
        long window=1l;
        if("minute".equals(period)){
            window=60l;
        }else if("hour".equals(period)){
            window=60l*60;
        }else if("day".equals(period)){
            window=60l*60*24;
        }else if("week".equals(period)){
            window=60l*60*24*7;
        }else if("month".equals(period)){
            window=60l*60*24*7*30;
        }else if("year".equals(period)){
            window=60l*60*24*7*30*365;
        }
        return window;
    }
}
