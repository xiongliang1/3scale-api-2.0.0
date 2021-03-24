/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/20
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.ApiPolicy;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApiPolicyRepository extends CommonQueryRepository<ApiPolicy, Integer> {
    /*@Query(value = "SELECT a.id as id,a.name as name,a.description as description,a.enabled as enabled," +
            "a.config as config, a.create_id as createId,a.project_id as projectId,a.type as type," +
            "a.update_time as updateTime,a.create_time as createTime, count(pa.id) as bindApiNum " +
            "from api_policy a,publish_api_policy_relationship pa " +
            "where pa.publish_policy_id = a.id and a.name like :name group by a.id",
            countQuery = "select count(*) " +
                    "from api_policy a,publish_api_policy_relationship pa " +
                    "where pa.publish_policy_id = a.id and a.name like :name  group by a.id"
            , nativeQuery = true)
    Page<Map<String, Object>> findPageByName(@Param("name") String name, Pageable pageable);

    @Query(value = "SELECT a.id as id,a.name as name,a.description as description,a.enabled as enabled," +
            "a.config as config, a.create_id as createId,a.project_id as projectId,a.type as type," +
            "a.update_time as updateTime,a.create_time as createTime, count(pa.id) as bindApiNum " +
            "from api_policy a,publish_api_policy_relationship pa " +
            "where pa.publish_policy_id = a.id group by a.id",
            countQuery = "select count(*) " +
                    "from api_policy a,publish_api_policy_relationship pa " +
                    "where pa.publish_policy_id = a.id group by a.id"
            , nativeQuery = true)
    Page<Map<String, Object>> findPage(Pageable pageable);*/

    @Query(value = "select a.id,a.name from ApiPolicy a join PublishApiPolicyRelationship p on p.publishPolicyId=a.id" +
            " where p.publishApiId=:apiId group by a.id,a.name")
    ApiPolicy findByApiIdGroupByPublishPolicyId(@Param("apiId") Integer apiId);

    @Query(value = "select a from ApiPolicy a where a.projectId=:projectId and a.name=:name")
    List<ApiPolicy> findByProjectAndName(@Param("projectId") String projectId, @Param("name") String name);

    @Query(value = "select a from ApiPolicy a join PublishApiPolicyRelationship p on p.publishPolicyId=a.id" +
            " where p.publishApiId=:apiId and a.type='limit'  order by a.id desc ")
    List<ApiPolicy> findByApiIdGroupByPublishLimitPolicyId(@Param("apiId") Integer apiId);
}
