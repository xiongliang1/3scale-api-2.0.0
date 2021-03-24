/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * @date 2019/11/25
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.management.service.ServicesService;
import com.hisense.gateway.library.stud.model.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

import static com.hisense.gateway.library.constant.BaseConstants.URL_SERVICES;

@Slf4j
@RequestMapping(URL_SERVICES)
@RestController
public class ServicesController {
    @Autowired
    ServicesService services;

    @GetMapping
    public ServiceDtos getPageServiceDtoList(@PathVariable String domain, HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        return services.getPageServiceDtoList(domain, pageNum, pageSize);
    }

    @GetMapping("/find")
    public ServiceDtos getPageServiceDtoBySysNameList(@PathVariable String domain, HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        String target = request.getParameter("target");
        String secTarget = request.getParameter("secTarget");
        return services.getPageServiceDtoBySysNameList(domain, target, secTarget, pageNum, pageSize);
    }

    @GetMapping("/{id}")
    public ServiceDto getServiceDto(@PathVariable String domain, @PathVariable String id) {
        return services.getServiceDto(domain, id);
    }

    @GetMapping("/{serviceId}/appPlans")
    public List<AppPlanDto> getAppPlanDtoList(@PathVariable String domain, @PathVariable String serviceId) {
        return services.getAppPlanDtoList(domain, serviceId);
    }

    @GetMapping("/{serviceId}/{userName}/applications")
    public List<ApplicationCount> getPlanAppCount(@PathVariable String domain, @PathVariable String serviceId, @PathVariable String userName) {
        return services.getPlanAppCount(domain, serviceId, userName);
    }

    @GetMapping("/{serviceName}/search")
    public ServiceDtos searchPageServiceDtoByName(@PathVariable String domain, @PathVariable String serviceName,
                                                  HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        return services.searchPageServiceDtoByName(domain, serviceName, pageNum, pageSize);
    }

    @GetMapping("/{serviceName}/searchByDesc")
    public ServiceDtos searchPageServiceDtoBySysNameAndName(@PathVariable String domain, @PathVariable String serviceName,
                                                            HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        String target = request.getParameter("target");
        String secTarget = request.getParameter("secTarget");
        return services.searchPageServiceDtoBySysNameAndName(domain, serviceName, target, secTarget, pageNum, pageSize);
    }

    @GetMapping("/{serviceId}/metrics")
    public MetricsDto getServiceMetricList(@PathVariable String domain, @PathVariable String serviceId) {
        return services.getServiceMetricList(domain, serviceId);
    }

    @GetMapping("/classifies")
    public ServiceDtos getServiceClassifies(@PathVariable String domain, HttpServletRequest request) {
        int pageNum = Integer.parseInt(request.getParameter("pageNum"));
        int pageSize = Integer.parseInt(request.getParameter("pageSize"));
        return services.getServiceClassifies(domain, pageNum, pageSize);
    }
}
