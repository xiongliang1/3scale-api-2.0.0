package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.alert.AlertApiInfo;
import com.hisense.gateway.library.model.base.alert.AlertApiInfoQuery;
import com.hisense.gateway.library.model.dto.web.AlertPolicyDto;
import com.hisense.gateway.library.model.base.alert.AlertPolicyQuery;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * @author guilai.ming 2020/09/10
 */
public interface AlertPolicyService {
    /**
     * 存储告警策略
     */
    Result<Boolean> saveAlertPolicy(String projectId, Integer environment, AlertPolicyDto policyDto);

    /**
     * 批量删除告警策略
     */
    Result<Boolean> deleteAlertPolicies(List<Integer> policyIds);

    /**
     * 更新告警策略
     */
    Result<Boolean> updateAlertPolicy(String projectId, Integer environment, Integer policyId, AlertPolicyDto policyDto);

    /**
     * 查询告警策略
     * 排序方式: 策略名称、策略状态、策略创建时间
     */
    Page<AlertPolicyDto> findAlertPoliciesByPage(String projectId, Integer environment, AlertPolicyQuery policyQuery);

    /**
     * 查询 API绑定列表, 或者未绑定列表
     */
    List<AlertApiInfo> findBindUnBindApiList(Integer policyId, String tenantId, String projectId, Integer environment,
                                       AlertApiInfoQuery apiInfoQuery);

    /**
     * 为多个API绑定\解绑某个指定的告警策略
     *
     * @param policyId     指定的alertPolicy的id
     * @param bindApiIds   绑定列表
     * @param unBindApiIds 解绑列表
     */
    Result<Boolean> bindToPublishApi(Integer policyId, List<Integer> bindApiIds, List<Integer> unBindApiIds);

    /**
     * 开启\关闭 告警策略
     *
     * @param policyId 指定的alertPolicy的id
     * @param enable   false为关闭,true为开启
     */
    Result<Boolean> enableAlertPolicy(Integer policyId, boolean enable);

    /**
     * 获取Policy详情
     */
    Result<AlertPolicyDto> getAlertPolicy(Integer policyId);

    /**
     * 使用kafka将告警策略信息同步到Kafka
     */
    String syncAlertPolicyToKafka();
}
