/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020-02-19 @author jinshan
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.PublishApiTemporaryData;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PublishApiTemporaryDataRepository extends CommonQueryRepository<PublishApiTemporaryData, Integer> {

    @Query("select p from PublishApiTemporaryData  p where p.publishApi.id=:apiId and p.status=1  order by p.createTime desc")
    PublishApiTemporaryData findTempDataByApiId(@Param("apiId") Integer apiId);
}
