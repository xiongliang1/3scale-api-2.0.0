/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/19
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.Instance;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface InstanceRepository extends CommonQueryRepository<Instance, Integer> {
    @Query("select a from Instance a where a.clusterId =:clusterId and a.clusterPartition =:partition order by a.createTime desc ")
    Instance searchInstanceByClusterIdAndPartition(@Param("clusterId") String clusterId, @Param("partition") String partition);

    @Query("select i from Instance i where i.status=1 and i.clusterId=:clusterId and i.clusterPartition in(:partitions)")
    List<Instance> findAllByClusterAndPartition(@Param("clusterId") String clusterId, @Param("partitions") List<String> partitions);

    @Query("select a from Instance a where a.instanceName = :name order by a.createTime desc")
    Instance searchInstanceByName(@Param("name") String name);

    // mingguilai.ex
    @Query("select i from Instance i where i.status=1 and i.tenantId=:tenantId")
    List<Instance> findAllByTenantId(@Param("tenantId") String tenantId);

    @Query("select i from Instance i where i.status=1 and i.clusterName=:environment and i.clusterPartition =:partition")
    Instance findAllByTenantIdAndPartition(@Param("environment") String environment, @Param("partition") String partition);

    @Query("select a from Instance a where a.clusterPartition=:partition  order by a.createTime desc")
    List<Instance> searchInstanceByPartition(@Param("partition") String partition);

    @Query(nativeQuery = true, value = "select a.* from gw_instance a where a.cluster_partition=:partition and a.cluster_name=:environment and rownum <=1 order by a.create_time")
    //@Query(nativeQuery = true, value = "select a.* from gw_instance a where a.cluster_partition=:partition and a.cluster_name=:environment order by a.create_time desc limit 1")
    Instance searchInstanceByPartitionEnvironment(@Param("partition") String partition,@Param("environment") String environment);
}
