/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/11 @author peiyun
 */
package com.hisense.gateway.management.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.InstanceEnvironment;
import com.hisense.gateway.library.constant.InstancePartition;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.RequestInfo;
import com.hisense.gateway.library.service.DebuggingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

import static com.hisense.gateway.library.constant.BaseConstants.URL_DEBUGGING;

@Api
@Slf4j
@RequestMapping(URL_DEBUGGING)
@RestController
public class DebuggingController {
    @Resource
    DebuggingService debuggingService;

    @RequestMapping(value = "/{publishApiId}", method = RequestMethod.POST)
    public Result<Object> debuggingRequest(
            @PathVariable Integer publishApiId,
            @RequestBody RequestInfo requestInfo,
            HttpServletRequest request) {
        Map<String, Object> result = debuggingService.debuggingRequest(publishApiId, requestInfo);
        log.info("debuggingRequest {} ", result);
        String contentType = String.valueOf(result.get("contentType"));
        result.remove("contentType");

        Result<Object> returnResult = new Result<>();
        if (!Boolean.parseBoolean(String.valueOf(result.get("state")))) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg(String.valueOf(result.get("msg")));
            returnResult.setData(null);
            return returnResult;
        }

        returnResult.setData(result.get("data"));
        if (contentType.contains("application/json")) {
            if (String.valueOf(result.get("data")).startsWith("[")) {
                JSONArray dataJsonArray = JSONArray.parseArray(String.valueOf(result.get("data")));
                returnResult.setData(dataJsonArray);
            } else {
                JSONObject dataJsonObject = JSONObject.parseObject(String.valueOf(result.get("data")));
                returnResult.setData(dataJsonObject);
            }
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("请求成功");
        return returnResult;
    }

    @RequestMapping(value = "/{publishApiId}/getAppIdAndKeyParam/{partition}", method = RequestMethod.GET)
    public Result<Map<String, String>> getAppIdAndKeyParam(
            @PathVariable Integer publishApiId,
            @PathVariable Integer partition,
            @PathVariable String environment,
            HttpServletRequest request) {
        String partitionStr = InstancePartition.fromCode(partition).getName();
        String env = InstanceEnvironment.fromCode(environment).getName();
        return debuggingService.getAppIdAndKeyParam(partitionStr, env, publishApiId);
    }

    @ApiOperation("get api userKey or appId and appKey")
    @RequestMapping(value = "/{publishApiId}/getUserKey/{partition}", method = RequestMethod.GET)
    public Result<Map<String,String>> getApiByUserKey(@PathVariable Integer publishApiId,
                             @PathVariable Integer partition,
                             @PathVariable String environment){
        String partitionStr = InstancePartition.fromCode(partition).getName();
        String env = InstanceEnvironment.fromCode(environment).getName();
        return debuggingService.getApiByUserKey(partitionStr, env ,publishApiId);
    }

    @ApiOperation("get api userKey or appId and appKey")
    @RequestMapping(value = "/{publishApiId}/getUserKey/{partition}/{systemId}", method = RequestMethod.GET)
    public Result<Map<String,String>> getApiByUserKey(@PathVariable Integer systemId,@PathVariable Integer publishApiId,
                                                      @PathVariable Integer partition,
                                                      @PathVariable String environment){
        String partitionStr = InstancePartition.fromCode(partition).getName();
        String env = InstanceEnvironment.fromCode(environment).getName();
        return debuggingService.getApiByUserKey(partitionStr, env ,publishApiId,systemId);
    }

    @ApiOperation("get path")
    @RequestMapping(value = "/{publishApiId}/getPath/{partition}", method = RequestMethod.GET)
    public Result getPath(@PathVariable Integer publishApiId,
                          @PathVariable Integer partition,
                          @PathVariable String environment){
        String partitionStr = InstancePartition.fromCode(partition).getName();
        String env = InstanceEnvironment.fromCode(environment).getName();
        return debuggingService.getPath(partitionStr, env ,publishApiId);
    }


}
