/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.buz.CreatorBase;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.Optional;

@NoRepositoryBean
public interface CreatorJpaExecutor<T extends CreatorBase, ID> extends MyJpaExecutor<T, ID>,
                                                                       JpaSpecificationExecutor<T> {
    default T findOne(ID id, Integer creatorId) {
        Optional<T> option = findById(id);
        if (!option.isPresent()) {
            return null;
        }
        T t = option.get();

        if (t.getCreatorId().intValue() != creatorId.intValue()) {
            return null;
        }
        return t;
    }
}
