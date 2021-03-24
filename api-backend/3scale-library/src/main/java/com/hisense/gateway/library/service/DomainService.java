/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.pojo.base.Domain;

/**
 * DomainService
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:34
 */
public interface DomainService {
    Domain getDomainByName(String name);
}
