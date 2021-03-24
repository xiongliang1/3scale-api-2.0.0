/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/19
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.TemporaryApplication;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface TemporaryApplicationRepository extends CommonQueryRepository<TemporaryApplication, Integer> {
    @Modifying
    @Transactional
    @Query("delete from TemporaryApplication p where p.apiId=:apiId")
    void deleteByApiId(@Param("apiId") Integer apiId);

    @Query("select p from TemporaryApplication p where p.status=1 and p.apiId=:apiId")
    List<TemporaryApplication> findByAppId(@Param("apiId") Integer apiId);
}
