/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/19
 */
package com.hisense.gateway.management.service;

import com.hisense.gateway.library.model.pojo.base.Instance;

public interface InstanceService {
    Instance getInstanceByclusterIdAndPartition(String clusterId, String partition);
}
