/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/19
 */
package com.hisense.gateway.management.service.impl;

import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.repository.InstanceRepository;
import com.hisense.gateway.management.service.InstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class InstanceServiceImpl implements InstanceService {
    @Autowired
    InstanceRepository instanceRepository;

    @Override
    public Instance getInstanceByclusterIdAndPartition(String clusterId, String partition) {
        return instanceRepository.searchInstanceByClusterIdAndPartition(clusterId, partition);
    }
}
