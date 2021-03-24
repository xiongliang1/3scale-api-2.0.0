/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.service.impl;

import com.hisense.gateway.library.model.pojo.base.Domain;
import com.hisense.gateway.library.repository.DomainRepository;
import com.hisense.gateway.library.service.DomainService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class DomainServiceImpl implements DomainService {
    @Autowired
    DomainRepository domainRepository;

    @Override
    public Domain getDomainByName(String name) {
        return domainRepository.searchDomainByName(name);
    }
}
