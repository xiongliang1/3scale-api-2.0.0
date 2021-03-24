package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApprovalApiResDto;
import com.hisense.gateway.developer.service.ApprovalService;
import com.hisense.gateway.library.model.dto.web.FlowResponseDto;
import com.hisense.gateway.library.model.dto.web.QueryParamsDto;
import com.hisense.gateway.library.service.WorkFlowService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import oracle.jdbc.proxy.annotation.Post;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@Api
@RequestMapping("/api/v1/{environment}/approval")
@RestController
public class ApprovalController {

    @Resource
    ApprovalService approvalService;

    @Resource
    WorkFlowService workFlowService;

    @ApiOperation("查看指定API的审批详情")
    @RequestMapping(value = "getApprovalRecord/{id}", method = RequestMethod.GET)
    public Result<ApprovalApiResDto> getApprovalRecord(@PathVariable Integer id) {

        return approvalService.getApprovalRecord(id);
    }

    @ApiOperation("查看某人发起的流程")
    @GetMapping("/queryPersonStartProcess")
    public Result<FlowResponseDto> queryPersonStartProcessInstWithBizInfo(Integer pageNum,Integer pageSize){
        QueryParamsDto queryParamsDto = new QueryParamsDto();
        queryParamsDto.setIndex(pageNum);
        queryParamsDto.setSize(pageSize);
        return approvalService.queryPersonStartProcessInstWithBizInfo(queryParamsDto);
    }

    /**
     * 流程图相关数据信息
     */
    @PostMapping(value = "/getProcessGraph")
    @ApiOperation("流程图相关数据信息")
    public  Result<Object> getProcessGraph(@RequestBody QueryParamsDto queryParamsDto){
        return approvalService.getProcessGraph(queryParamsDto);
    }
}
