/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/2 @author peiyun
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApprovalApiDto;
import com.hisense.gateway.library.model.dto.web.ApprovalApiResDto;
import com.hisense.gateway.library.model.dto.web.PublishApiDto;
import com.hisense.gateway.management.service.ApprovalService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

import static com.hisense.gateway.library.constant.BaseConstants.URL_APPROVAL;

@Api("发布审批")
@RequestMapping(URL_APPROVAL)
@RestController
public class ApprovalController {
    @Resource
    ApprovalService approvalService;

    @ApiOperation("发布审批 分页查询 指定API的 发布审批(待办,已办)列表")
    @RequestMapping(value = "/searchApprovalApi", method = RequestMethod.POST)
    public Result<Page<ApprovalApiResDto>> searchApprovalApi(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @RequestBody ApprovalApiDto approvalApiDto,
            HttpServletRequest request) {
        if (approvalApiDto.getMark() == 1) {// 查询已办
            return approvalService.searchReadyApprovalApi(approvalApiDto, request.getHeader("Authorization"));
        }
        return approvalService.searchWaitApprovalApi(approvalApiDto, request.getHeader("Authorization"));
    }

    @ApiOperation("发布审批 从cloud获取租户列表,header里需携带Authorization")
    @RequestMapping(value = "/findTenantList", method = RequestMethod.GET)
    public Result<List<Map<String, String>>> findTenantList(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            HttpServletRequest request) {
        return approvalService.findTenantList(request.getHeader("Authorization"));
    }

    @ApiOperation("发布审批 从cloud获取Project列表,header里需携带Authorization")
    @RequestMapping(value = "/findProjectList", method = RequestMethod.GET)
    public Result<List<Map<String, String>>> findProjectList(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            HttpServletRequest request) {
        return approvalService.findProjectList(request.getHeader("Authorization"), tenantId);
    }

    @ApiOperation("发布审批 管理员审批通过 API发布申请")
    @RequestMapping(value = "/approvalApi", method = RequestMethod.POST)
    public Result<Boolean> approvalApi(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @RequestBody ApprovalApiDto approvalApiDto) {
        return approvalService.approvalApi(approvalApiDto);
    }

    @ApiOperation("发布审批 查看指定API的 发布审批详情")
    @RequestMapping(value = "/getApprovalRecord/{id}", method = RequestMethod.GET)
    public Result<ApprovalApiResDto> getApprovalRecord(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable Integer id,
            @RequestParam Integer type,
            HttpServletRequest request) {
        // 1 已办,2-代办
        return approvalService.getApprovalRecord(id, type, request.getHeader("Authorization"));
    }

    @ApiOperation("发布审批 查看指定API的详情")
    @RequestMapping(value = "/getApprovalRecordDetails/{id}", method = RequestMethod.GET)
    public Result<PublishApiDto> getApprovalRecordDetails(
            @PathVariable String tenantId,
            @PathVariable String projectId,
            @PathVariable Integer id) {
        return approvalService.getApprovalRecordDetails(id);
    }
}
