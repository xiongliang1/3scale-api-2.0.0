/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.library.service.impl;

import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.pojo.base.Domain;
import com.hisense.gateway.library.repository.DomainRepository;
import com.hisense.gateway.library.service.FeatureService;
import com.hisense.gateway.library.stud.FeatureStud;
import com.hisense.gateway.library.stud.model.FeatureDtos;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

@Slf4j
@Service
public class FeatureServiceImpl implements FeatureService {
    @Resource
    FeatureStud featureStud;

    @Resource
    DomainRepository domainRepository;

    @Override
    public FeatureDtos findFeatureByServiceId(String domainName, String serviceId) {
        Domain domain =
                domainRepository.searchDomainByName(domainName);
        if (domain == null) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        return featureStud.findFeatureByServiceId(domain.getHost(), domain.getAccessToken(), serviceId);
    }
}
