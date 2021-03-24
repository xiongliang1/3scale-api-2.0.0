/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/26
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.UserInstanceRelationship;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserInstanceRelationshipRepository extends CommonQueryRepository<UserInstanceRelationship, Integer> {
    @Query("select u from UserInstanceRelationship u where u.userName=:userName and u.instanceId=:instanceId")
    UserInstanceRelationship findByUserAndInstanceId(@Param("userName") String userName,
                                                     @Param("instanceId") Integer instanceId);

    @Query("select u from UserInstanceRelationship u where u.accountId=:accountId and u.instanceId=:instanceId")
    UserInstanceRelationship findByAccountIdAndInstanceId(@Param("accountId") Long accountId,
                                                          @Param("instanceId") Integer instanceId);

    @Query("select u from UserInstanceRelationship u where u.userName=:userName and u.instanceId=:instanceId")
    List<UserInstanceRelationship> findListByUserAndInstanceId(@Param("userName") String userName,
                                                           @Param("instanceId") Integer instanceId);
}
