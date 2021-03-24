/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.Domain;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DomainRepository extends CommonQueryRepository<Domain, Integer> {
    /**
     * 根据租户查询
     *
     * @param name
     * @return
     */
    @Query("select a from Domain a where a.name = :name order by a.createTime desc")
    Domain searchDomainByName(@Param("name") String name);
}
