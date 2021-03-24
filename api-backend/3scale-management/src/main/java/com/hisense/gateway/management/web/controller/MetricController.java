/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author wangjinshan
 * @date 2019-11-25
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.service.MetricService;
import com.hisense.gateway.library.stud.model.MetricsDto;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.hisense.gateway.library.constant.BaseConstants.URL_METRIC;

@RequestMapping(URL_METRIC)
@RestController
public class MetricController {
    @Autowired
    MetricService metricService;

    @GetMapping("/")
    public MetricsDto getMetricByServiceId(@PathVariable String domain, @PathVariable String serviceId) {
        return metricService.getMetricByServiceId(domain, serviceId);
    }
}
