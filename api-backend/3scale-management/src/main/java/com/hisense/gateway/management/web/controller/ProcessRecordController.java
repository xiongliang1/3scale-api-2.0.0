/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/3
 */
package com.hisense.gateway.management.web.controller;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.buz.ProcessHandleDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.ProcessRecordQuery;
import com.hisense.gateway.library.service.ProcessRecordService;
import com.hisense.gateway.library.web.form.ApplicationSearchForm;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.hisense.gateway.library.constant.BaseConstants.URL_PROCESS_RECORD;

@Api("API发布申请, API订阅历史, 审批订阅")
@RequestMapping(URL_PROCESS_RECORD)
@RestController
@Slf4j
public class ProcessRecordController {
    @Resource
    ProcessRecordService processRecordService;

    @ApiOperation("发布申请 分页查询")
    @GetMapping("/api/applyList")
    public Result<Page<ProcessRecordDto>> applyList(
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "isApproved", required = true) int isApproved,
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = true) String projectId,
            @PathVariable String environment,
            @RequestParam(name = "sort", required = false) String[] sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        String type = "publish_api";
        if (sort != null && sort.length > 1) {
            direction = "d".equalsIgnoreCase(sort[0]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = sort[1];
        }

        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size, Sort.by(direction, property));
        Page<ProcessRecordDto> p = processRecordService.findApiApplyList(tenantId, projectId, isApproved, name, type,
                pageable,environment);
        Result<Page<ProcessRecordDto>> returnResult = new Result<>();

        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("发布申请 查看 指定(API发布申请)的详情")
    @GetMapping("/api/{id}")
    public Result<ProcessRecordDto> getApplyDetail(@PathVariable(name = "id", required = true) Integer id) {
        ProcessRecordDto processRecordDto = processRecordService.findApiApplyDetail(id);
        Result<ProcessRecordDto> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(processRecordDto);
        return returnResult;
    }

    @ApiOperation("订阅管理 审批 一条订阅")
    @PostMapping("/application/approve")
    public Result<Boolean> approve(@RequestBody ProcessRecordDto processRecordDto) {
        return processRecordService.approveApplication(processRecordDto);
    }

    @ApiOperation("订阅管理 待办 分页查看")
    @GetMapping("/application/applyList")
    public Result<Page<ProcessRecordDto>> applicationApplyList(
            @RequestParam(name = "page", defaultValue = "0", required = false) Integer page,
            @RequestParam(name = "size", defaultValue = "10", required = false) Integer size,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "system", required = false) Integer system,
            @RequestParam(name = "apiSystem", required = false) Integer apiSystem,
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = false) String projectId,
            @RequestParam(name = "sort", required = false) String[] sort) {
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        String type = "application";
        if (sort != null && sort.length > 1) {
            direction = "d".equalsIgnoreCase(sort[0]) ? Sort.Direction.DESC : Sort.Direction.ASC;
            property = sort[1];
        }

        ApplicationSearchForm applicationSearchForm = new ApplicationSearchForm();
        applicationSearchForm.setTenantId(tenantId);
        applicationSearchForm.setProjectId(projectId);
        applicationSearchForm.setApiName(name);
        applicationSearchForm.setApiSystem(apiSystem);
        applicationSearchForm.setAppSystem(system);
        applicationSearchForm.setSystem(system);
        applicationSearchForm.setName(name);

        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size, Sort.by(direction, property));
        Page<ProcessRecordDto> p = processRecordService.findApplicationApplyList(applicationSearchForm, pageable);

        Result<Page<ProcessRecordDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("订阅管理 查看 订阅的详情(PRID)")
    @GetMapping("/application/{id}")
    public Result<ProcessRecordDto> getApplicationDetail(@PathVariable(name = "id", required = true) Integer id) {
        ProcessRecordDto p = processRecordService.findApplicationApplyDetail(id);
        Result<ProcessRecordDto> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("订阅管理 批量审批")
    @PostMapping("/application/approveList")
    public Result<Boolean> approveList(@RequestBody List<ProcessRecordDto> processRecordDto) {
        return processRecordService.approveListApplication(processRecordDto);
    }

    @ApiOperation("订阅管理 待审批 分页")
    @PostMapping("/application/applyList")
    public Result<Page<ProcessRecordDto>> applicationApplyList(
            @PathVariable(name = "tenantId", required = true) String tenantId,
            @PathVariable(name = "projectId", required = true) String projectId,
            @PathVariable(name = "environment", required = true) String environment,
            @RequestBody ProcessRecordQuery processRecordQuery) {

        Page<ProcessRecordDto> p = processRecordService.findApplicationApplyList(tenantId, projectId,environment, processRecordQuery);

        Result<Page<ProcessRecordDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("订阅管理 订阅人、审批人")
    @GetMapping("/application/subscribers")
    public Result<Map> getSubscribers(@PathVariable(name = "tenantId", required = true) String tenantId,
                                      @PathVariable(name = "projectId", required = false) String projectId) {
        Map<String, Set<String>> subscribers = processRecordService.findSubscribers(tenantId, projectId);
        Result<Map> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(subscribers);
        return returnResult;
    }


    @ApiOperation("流程审批处理")
    @PostMapping("/processHandle")
    public Result<Object> processHandle(@RequestBody ProcessHandleDto processHandleDto){
        log.info("流程审批处理开始，入参："+ JSONObject.toJSONString(processHandleDto));
        return processRecordService.processHandle(processHandleDto.getProcessInstID(),
                processHandleDto.getApproveType(),processHandleDto.getRemark());
    }
}
