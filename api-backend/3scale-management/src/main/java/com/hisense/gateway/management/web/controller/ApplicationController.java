/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/19
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import com.hisense.gateway.library.model.dto.web.PublishApplicationDto;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import com.hisense.gateway.management.service.ApplicationService;
import com.hisense.gateway.library.service.ProcessRecordService;
import com.hisense.gateway.library.web.form.ApplicationSearchForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_APPLICATIONS;

@Api("查看我发起的API订购(待办,已办,列表,详情)")
@RequestMapping(URL_APPLICATIONS)
@RestController
public class ApplicationController {
    @Resource
    ApplicationService applicationService;

    @Autowired
    ProcessRecordService processRecordService;

    @ApiOperation("订阅管理 已审批(审批完成,审批不通过) 分页")
    @PostMapping("/approvalComplete")
    public Result<Page<ProcessRecordDto>> approvalComplete(
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = true) String projectId,
            @PathVariable(name = "environment", required = true) String environment,
            @RequestBody ProcessRecordQuery processRecordQuery) {
        Page<ProcessRecordDto> p = applicationService.findApprovalCompleteApp(tenantId, projectId, environment,processRecordQuery);
        Result<Page<ProcessRecordDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("我订阅的 已办(审批完成,审批不通过) 分页查看")
    @GetMapping("/approvalComplete")
    public Result<Page<ProcessRecordDto>> getApplications(
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(name = "apiName", required = false) String apiName,
            @RequestParam(name = "groupId", required = false) Integer groupId,
            @RequestParam(name = "status", required = false) Integer status,
            @RequestParam(name = "apiSystem", required = false) Integer apiSystem,
            @RequestParam(name = "appSystem", required = false) Integer appSystem,
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = true) String projectId,
            @RequestParam(name = "sort", required = false) String[] sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if (sort != null && sort.length > 1) {
            direction = "d".equalsIgnoreCase(sort[0]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = sort[1];
        }

        ApplicationSearchForm applicationSearchForm = new ApplicationSearchForm();
        applicationSearchForm.setTenantId(tenantId);
        applicationSearchForm.setGroupId(groupId);
        applicationSearchForm.setStatus(status);
        applicationSearchForm.setProjectId(projectId);
        applicationSearchForm.setApiName(apiName);
        applicationSearchForm.setApiSystem(apiSystem);
        applicationSearchForm.setAppSystem(appSystem);
        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size, Sort.by(direction, property));
        Page<ProcessRecordDto> p =
                applicationService.findApprovalCompleteApp(applicationSearchForm, pageable);

        Result<Page<ProcessRecordDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("我订阅的 查看 某个已办订阅的详情(PRID)")
    @GetMapping("/approvalComplete/{id}")
    public Result<ProcessRecordDto> getApplyDetail(@PathVariable(name = "id", required = true) Integer id) {
        ProcessRecordDto processRecordDto = processRecordService.findApplicationApplyDetail(id);
        Result<ProcessRecordDto> returnResult = new Result<>();
        returnResult.setMsg("查询成功");
        returnResult.setCode(Result.OK);
        returnResult.setData(processRecordDto);
        return returnResult;
    }

    @ApiOperation("我订阅的-已审批 分页查询 PublishApplication")
    @GetMapping("/backlogs")
    public Result<Page<PublishApplication>> listByPage(
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(name = "apiName", required = false) String apiName,
            @RequestParam(name = "apiSystem", required = false) Integer apiSystem,
            @RequestParam(name = "appSystem", required = false) Integer appSystem,
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = true) String projectId,
            @RequestParam(name = "sort", required = false) String[] sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if (sort != null && sort.length > 1) {
            direction = "d".equalsIgnoreCase(sort[0]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = sort[1];
        }

        ApplicationSearchForm applicationSearchForm = new ApplicationSearchForm();
        applicationSearchForm.setTenantId(tenantId);
        applicationSearchForm.setProjectId(projectId);
        applicationSearchForm.setApiName(apiName);
        applicationSearchForm.setApiSystem(apiSystem);
        applicationSearchForm.setAppSystem(appSystem);
        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size, Sort.by(direction, property));
        Page<PublishApplication> p =
                applicationService.findBackLogApplication(applicationSearchForm, pageable);

        return new Result(Result.OK,"",p);
    }

    @ApiOperation("我订阅的 查看 PublishApplication详情(AppId)")
    @GetMapping("/{id}")
    public PublishApplicationDto getApplication(@PathVariable Integer id, HttpServletRequest request) {
        return applicationService.getPublishApplication(id);
    }

    @ApiOperation("订阅管理 禁用 指定的(PRID)一条订阅")
    @PutMapping("/{id}/suspend")
    public Result<Boolean> applicationSuspend(@PathVariable(value = "id", required = true) Integer id) {
        // 操作PR表
        return applicationService.applicationSuspend(id);
    }

    @ApiOperation("订阅管理 启用 指定的(PRID)一条订阅")
    @PutMapping("/{id}/resume")
    public Result<Boolean> applicationResume(@PathVariable(value = "id", required = true) Integer id) {
        return applicationService.applicationResume(id);
    }

    @ApiOperation("订阅管理 删除 指定的(PRID)一条订阅")
    @DeleteMapping("/{id}")
    public Result<Boolean> applicationDelete(@PathVariable(value = "id", required = true) Integer id) {
        applicationService.applicationDelete(id);
        Result<Boolean> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("删除成功");
        returnResult.setData(true);
        returnResult.setAlert(1);
        return returnResult;
    }

    @ApiOperation("订阅管理 批量禁用")
    @PutMapping("/suspendList")
    public Result<List<String>> applicationSuspendList(@RequestBody List<Integer> ids) {
        return applicationService.applicationSuspendList(ids);
    }

    @ApiOperation("订阅管理 批量启用")
    @PutMapping("/resumeList")
    public Result<List<String>> applicationResumeList(@RequestBody List<Integer> ids) {
        return applicationService.applicationResumeList(ids);
    }

    @ApiOperation("订阅管理 批量删除")
    @DeleteMapping("/deleteList")
    public Result<Boolean> applicationDeleteList(@RequestBody List<Integer> ids) {
        applicationService.applicationDeleteList(ids);
        Result<Boolean> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("删除成功");
        returnResult.setData(true);
        returnResult.setAlert(1);
        return returnResult;
    }
}
