/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.repository;

import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface CommonQueryRepository<T, ID> extends MyJpaExecutor<T, ID> {
}
