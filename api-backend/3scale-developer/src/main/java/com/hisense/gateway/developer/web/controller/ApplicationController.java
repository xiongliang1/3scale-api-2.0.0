package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.developer.service.ApplicationService;
import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.dto.web.PublishApiBatch;
import com.hisense.gateway.library.model.dto.web.SubscribedApi;
import com.hisense.gateway.library.service.ProcessRecordService;
import com.hisense.gateway.library.stud.model.Application;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;


@Api
@RequestMapping("/api/v1/{environment}/applications")
@RestController
public class ApplicationController {

    @Resource
    ApplicationService applicationService;

    @Resource
    ProcessRecordService processRecordService;

    @ApiOperation("已订阅列表")
    @GetMapping("/recommend")
    public Result<Page<ProcessRecordDto>> applicationList(@PathVariable(value = "environment")String environment,
                                                          @RequestParam(name = "page", defaultValue = "1", required = false)Integer page,
                                                          @RequestParam(name = "size",defaultValue = "10",required = false)Integer size,
                                                          @RequestParam(name = "apiName",required = false)String apiName,
                                                          @RequestParam(name = "system",required = false)List<Integer> system,
                                                          @RequestParam(name = "sort",required = false) String sort){
        Sort.Direction direction = Sort.Direction.DESC;
        String property = "createTime";
        if("createTimeDesc".equalsIgnoreCase(sort)){
            direction = Sort.Direction.DESC;
            property = "createTime";
        }
        if("createTimeAsc".equalsIgnoreCase(sort)){
            direction = Sort.Direction.ASC;
            property = "createTime";
        }
        PageRequest pageable = PageRequest.of(0 != page ? page - 1 : 0, size, Sort.by(direction, property));
        Page<ProcessRecordDto> p = applicationService.applicationList(environment,apiName,system,pageable);
        Result<Page<ProcessRecordDto>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(p);
        return returnResult;
    }


    @ApiOperation("单个订阅/多个订阅")
    @PostMapping("/subscribeApi")
    public Result<List<Object>> subscribeApi(@PathVariable(value = "environment")String environment,@RequestBody List<SubscribedApi> applications){
        return applicationService.subscribeApi(environment,applications);
    }

    @PostMapping("/subscribe")
    public Result<List<Object>> subscribe(@PathVariable(value = "environment")String environment,@RequestBody List<SubscribedApi> applications){
        return applicationService.subscribeApi(environment,applications);
    }


    @ApiOperation("取消订阅")
    @PostMapping("/unSubscribeApi")
    public Result<List<String>> unSubscribeApi(@RequestBody PublishApiBatch publishApiBatch){
        return applicationService.unSubscribeApi(publishApiBatch);
    }

    @ApiOperation("重置密匙")
    @GetMapping("/modifyAuthSecret/{prId}")
    public Result<Object> modifyAuthSecret(@PathVariable Integer prId){
        return applicationService.modifyAuthSecret(prId);
    }
}