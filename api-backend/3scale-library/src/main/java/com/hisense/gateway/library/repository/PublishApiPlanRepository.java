/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/24
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.PublishApiPlan;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface PublishApiPlanRepository extends CommonQueryRepository<PublishApiPlan, Integer> {
    @Query("select p from PublishApiPlan p where p.apiId=:apiId and p.instanceId=:instanceId")
    PublishApiPlan findByApiIdAndInstanceId(@Param("apiId") Integer apiId, @Param("instanceId") Integer instanceId);

    @Query("select p from PublishApiPlan p where p.apiId=:apiId order by create_Time")
    List<PublishApiPlan> findAllByApiId(@Param("apiId") Integer apiId);

    @Modifying
    @Transactional
    @Query("delete from PublishApiPlan p where p.apiId=:apiId")
    void deleteByApiId(@Param("apiId") Integer apiId);
	
    @Query("select p from PublishApiPlan p where p.apiId=:apiId and p.scalePlanId = :scalePlanId ")
    PublishApiPlan findAllByApiIdAndScalePlanId(@Param("apiId")Integer apiId, @Param("scalePlanId")Long scalePlanId);

    @Query( "select p from PublishApiPlan p where p.scalePlanId=:scalePlanId and p.instanceId=:instanceId")
    PublishApiPlan findByScalePlanIdAndInstanceId(@Param("scalePlanId") Long scalePlanId,
                                                  @Param("instanceId") Integer instanceId);

    @Query("select p from PublishApiPlan p where p.apiId=:apiId and p.instanceId=:instanceId  order by create_Time ")
    List<PublishApiPlan> findAllByInstanceIdAndApiId(@Param("apiId") Integer apiId, @Param("instanceId") Integer instanceId);
}
