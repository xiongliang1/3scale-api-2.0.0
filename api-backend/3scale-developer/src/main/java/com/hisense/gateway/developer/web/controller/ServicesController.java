/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.library.model.dto.portal.ApplicationCountRes;
import com.hisense.gateway.library.model.dto.portal.ServiceResDto;
import com.hisense.gateway.library.model.dto.portal.ServiceResDtos;
import com.hisense.gateway.developer.service.ServicesService;
import com.hisense.gateway.library.stud.model.AppPlanDto;
import com.hisense.gateway.library.stud.model.MetricsDto;
import com.hisense.gateway.library.stud.model.ServiceDtos;
import com.hisense.gateway.library.model.dto.portal.InstanceDto;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

@Api
@RequestMapping("/api/v1/domains/{domain}/services")
@RestController
public class ServicesController {

    @Autowired
    ServicesService services;

    @ApiOperation("getPageServiceDtoList....")
    @GetMapping
    public ServiceDtos getPageServiceDtoList(@PathVariable String domain, HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        return services.getPageServiceDtoList(domain, pageNum, pageSize);
    }

    @GetMapping("/find")
    public ServiceResDtos getPageServiceDtoBySysNameList(@PathVariable String domain, @RequestParam Integer system, HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        //categoryOne
        String target = request.getParameter("target");
        //categoryTwo
        String secTarget = request.getParameter("secTarget");
        //return services.getPageServiceDtoBySysNameList(domain,target,secTarget, pageNum, pageSize);
        return services.getPageServiceDtoByCategory(domain,target,secTarget, system, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public ServiceResDto getServiceDto(@PathVariable String domain, @PathVariable Integer id) {
        //return services.getServiceDto(domain, id);
        return services.getServiceResDtos(domain, id);
    }

    @GetMapping("/{serviceId}/appPlans")
    public List<AppPlanDto> getAppPlanDtoList(@PathVariable String domain, @PathVariable Integer serviceId) {
        //return services.getAppPlanDtoList(domain, serviceId);
        return services.getAppPlanDtoListNew(domain, serviceId);
    }

    @GetMapping("/{serviceId}/{userName}/applications")
    public List<ApplicationCountRes> getPlanAppCount(@PathVariable String domain, @PathVariable Integer serviceId, @PathVariable String userName) {
        //return services.getPlanAppCount(domain, serviceId, userName);
        return services.getPlanAppCountNew(domain, serviceId, userName);
    }

    @GetMapping("/{serviceName}/search")
    public ServiceResDtos searchPageServiceDtoByName(@PathVariable String domain, @PathVariable String serviceName,
                                                  HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        //return services.searchPageServiceDtoByName(domain,serviceName, pageNum, pageSize);
        return services.searchPageServiceResDtoByName(domain,serviceName, pageNum, pageSize);
    }

    @GetMapping("/{serviceName}/searchByDesc")
    public ServiceResDtos searchPageServiceDtoBySysNameAndName(@PathVariable String domain, @PathVariable String serviceName,
                                                            @RequestParam Integer system,
                                                            HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        String target = request.getParameter("target");
        String secTarget = request.getParameter("secTarget");
        //return services.searchPageServiceDtoBySysNameAndName(domain, serviceName, target, secTarget,pageNum, pageSize);
        return services.searchPageServiceDtoByNameAndSystem(domain, serviceName, target, secTarget, system, pageNum, pageSize);
    }

    @GetMapping("/{serviceId}/metrics")
    public MetricsDto getServiceMetricList(@PathVariable String domain, @PathVariable String serviceId) {
        return services.getServiceMetricList(domain,serviceId);
    }

    @GetMapping("/classifies")
    public ServiceResDtos getServiceClassifies(@PathVariable String domain, HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        return services.findDataItem(domain, pageNum, pageSize);
        //return services.getServiceClassifies(domain, pageNum, pageSize);
    }

    @GetMapping("/findSystem")
    public List<Map<String,Object>> findSystem(@RequestParam String systemName, HttpServletRequest request) {
        return services.findSystem(systemName);
    }
    
    @GetMapping("/instance")
    public InstanceDto getInstanceDto(@PathVariable String domain) {
        return services.getInstanceDto(domain);
    }
}
