/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/20
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.LimitBingApiPolicy;
import com.hisense.gateway.library.model.dto.web.LimitPolicyDto;
import com.hisense.gateway.library.model.pojo.base.ApiPolicy;
import com.hisense.gateway.management.service.LimitService;
import com.hisense.gateway.library.web.form.BindingForm;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_LIMIT;

@RequestMapping(URL_LIMIT)
@RestController
public class LimitController {
    @Resource
    LimitService limitService;

    @ApiOperation("流控策略解绑API")
    @PostMapping("/{policyId}/unbindApi")
    public Result<Boolean> unbindApi(@PathVariable String tenantId, @PathVariable String policyId,
                                     @RequestBody BindingForm bindingForm) {
        return limitService.unbindApi(tenantId, bindingForm, policyId);
    }

    @ApiOperation("流控策略绑定API")
    @PostMapping("/{policyId}/bindingApi")
    public Result<Boolean> bindingApi(@PathVariable String tenantId, @PathVariable String policyId,
                                      @RequestBody List<BindingForm> bindingFormList) {
        return limitService.bindingApi(tenantId, bindingFormList, policyId);
    }

    @ApiOperation("创建策略")
    @RequestMapping(value = "/createLimitPolicy", method = RequestMethod.POST)
    public Result<Boolean> createLimitPolicy(@PathVariable String projectId,
                                             @RequestBody LimitPolicyDto limitPolicyDto) {
        return limitService.createLimitPolicy(projectId, limitPolicyDto);
    }

    @RequestMapping(value = "/checkLimitPolicyName", method = RequestMethod.POST)
    public Result<Boolean> checkLimitPolicyName(@PathVariable String projectId,
                                                @RequestBody LimitPolicyDto limitPolicyDto) {
        return limitService.checkLimitPolicyName(projectId, limitPolicyDto);
    }

    @ApiOperation("更新策略")
    @RequestMapping(value = "/updateLimitPolicy/{id}", method = RequestMethod.POST)
    public Result<Boolean> updateLimitPolicy(@PathVariable String tenantId,
                                             @PathVariable String projectId,
                                             @PathVariable Integer id,
                                             @RequestBody LimitPolicyDto limitPolicyDto) {
        return limitService.updateLimitPolicy(id, tenantId, projectId, limitPolicyDto);
    }

    @RequestMapping(value = "/searchLimitPolicy", method = RequestMethod.POST)
    public Result<Page<ApiPolicy>> searchLimitPolicy(@PathVariable String tenantId, @PathVariable String projectId,
                                                     @RequestBody LimitPolicyDto limitPolicyDto) {
        return limitService.searchLimitPolicy(tenantId, projectId, limitPolicyDto);
    }

    @RequestMapping(value = "/searchLimitBingApiPolicy", method = RequestMethod.POST)
    public Result<Page<LimitBingApiPolicy>> searchLimitBingApiPolicy(@PathVariable String tenantId,
                                                                     @PathVariable String projectId,
                                                                     @RequestBody LimitPolicyDto limitPolicyDto) {
        return limitService.searchLimitBingApiPolicy(tenantId, projectId, limitPolicyDto);
    }

    @RequestMapping(value = "/getLimitPolicyById", method = RequestMethod.GET)
    public Result<LimitPolicyDto> getLimitPolicyById(@RequestParam Integer id) {
        return limitService.getLimitPolicyById(id);
    }

    @RequestMapping(value = "/deleteLimitPolicy/{id}", method = RequestMethod.DELETE)
    public Result<Boolean> deleteLimitPolicy(@PathVariable String tenantId,
                                             @PathVariable Integer id) {
        return limitService.deleteLimitPolicy(id, tenantId);
    }
}
