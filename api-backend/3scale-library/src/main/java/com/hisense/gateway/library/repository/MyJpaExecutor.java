/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface MyJpaExecutor<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    default T findOne(ID id) {
        Optional<T> option = findById(id);
        if (!option.isPresent()) {
            return null;
        }
        return option.get();
    }
}
