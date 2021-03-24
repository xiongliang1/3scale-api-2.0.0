/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/19 @author peiyun
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.management.service.DealDataService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.hisense.gateway.library.constant.BaseConstants.URL_DEALDATA;

@Slf4j
@RequestMapping(URL_DEALDATA)
@RestController
public class DealDataController {
    @Resource
    DealDataService dealDataService;

    @RequestMapping(value = "{instanceName}/initApiData", method = RequestMethod.GET)
    public Result<Boolean> initApiData(@PathVariable String tenantId,
                                       @PathVariable String projectId,
                                       @PathVariable String instanceName,
                                       @RequestParam Integer groupId,
                                       HttpServletRequest request) {
        return dealDataService.initApiData(tenantId, instanceName, groupId);
    }

    @RequestMapping(value = "{instanceName}/initApiDataById", method = RequestMethod.GET)
    public Result<Boolean> initApiDataById(@PathVariable String tenantId,
                                           @PathVariable String instanceName,
                                           @RequestParam String scaleId,
                                           @RequestParam Integer apiId,
                                           @RequestParam Integer groupId,
                                           HttpServletRequest request) {
        return dealDataService.initApiDataById(tenantId, instanceName, scaleId, apiId, groupId);
    }
}
