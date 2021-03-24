package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.portal.PortalProcessRecordQuery;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.service.MailService;
import com.hisense.gateway.library.service.ProcessRecordService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import static com.hisense.gateway.library.constant.BaseConstants.URL_PROCESS_RECORD;

@Api
@RequestMapping("/api/v1/{environment}/processRecord")
@RestController
public class ProcessRecordController {

    @Resource
    ProcessRecordService processRecordService;
    @Resource
    MailService mailService;

    @ApiOperation("我的订阅 订阅的详情(PRID)")
    @GetMapping("/application/{id}")
    public Result<ProcessRecordDto> getApplicationDetail(@PathVariable(name = "id", required = true) Integer id) {
        ProcessRecordDto p = processRecordService.findApplicationApplyDetail(id);
        Result<ProcessRecordDto> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("我的申请列表")
    @PostMapping("/myApplication")
    public Result<Page<ProcessRecordDto>> getMyApplications(@PathVariable String environment, @RequestBody PortalProcessRecordQuery portalProcessRecordQuery) {
        //查询我的申请列表
        Page<ProcessRecordDto> p = processRecordService.findMyApplicationas(environment,portalProcessRecordQuery);
        Result<Page<ProcessRecordDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }

    @ApiOperation("我的申请列表-催办")
    @GetMapping("/urge/{recordId}")
    public Result<String> approvalUrge( @PathVariable Integer recordId) {
        return mailService.ApiApprovalUrgeSendMail(recordId);
    }

}
