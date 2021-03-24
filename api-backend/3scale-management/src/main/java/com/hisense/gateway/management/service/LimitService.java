/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/20
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.LimitBingApiPolicy;
import com.hisense.gateway.library.model.dto.web.LimitPolicyDto;
import com.hisense.gateway.library.model.pojo.base.ApiInstance;
import com.hisense.gateway.library.model.pojo.base.ApiPolicy;
import com.hisense.gateway.library.web.form.BindingForm;
import org.springframework.data.domain.Page;

import java.util.List;

public interface LimitService {
    Result<Boolean> bindingApi(String clusterId, List<BindingForm> bindingFormList, String policyId);

    Result<Boolean> unbindApi(String clusterId, BindingForm bindingForm, String policyId);

    Result<Boolean> createLimitPolicy(String projectId, LimitPolicyDto limitPolicyDto);

    Result<Boolean> updateLimitPolicy(Integer id, String clusterId, String projectId, LimitPolicyDto limitPolicyDto);

    Result<Page<ApiPolicy>> searchLimitPolicy(String clusterId, String projectId, LimitPolicyDto limitPolicyDto);

    Result<Boolean> deleteLimitPolicy(Integer id, String clusterId);

    Result<LimitPolicyDto> getLimitPolicyById(Integer id);

    List<ApiInstance> getInstances(Integer policyid);

    Result<Boolean> checkLimitPolicyName(String projectId, LimitPolicyDto limitPolicyDto);

    Result<Page<LimitBingApiPolicy>> searchLimitBingApiPolicy(String clusterId, String projectId, LimitPolicyDto limitPolicyDto);
}
