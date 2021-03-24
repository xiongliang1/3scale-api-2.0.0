/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.service.impl;

import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.pojo.base.Domain;
import com.hisense.gateway.library.repository.DomainRepository;
import com.hisense.gateway.library.service.MetricService;
import com.hisense.gateway.library.stud.MetricStud;
import com.hisense.gateway.library.stud.model.MetricsDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * UserServiceImp
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:35
 */
@Slf4j
@Service
public class MetricServiceImpl implements MetricService {
    @Autowired
    DomainRepository domainRepository;

    @Autowired
    MetricStud metricStud;

    @Override
    public MetricsDto getMetricByServiceId(String domainName, String serviceId) {
        MetricsDto metrics = null;
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        metrics = metricStud.getMetricByServiceId(domain.getHost(),
                domain.getAccessToken(), serviceId);
        return metrics;
    }
}
