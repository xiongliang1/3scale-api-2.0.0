package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.dto.buz.ApiInstanceDto;
import com.hisense.gateway.library.model.pojo.base.ApiInstance;
import com.hisense.gateway.library.model.pojo.base.PublishApiInstanceRelationship;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Set;

public interface PublishApiInstanceRelationshipRepository extends CommonQueryRepository<PublishApiInstanceRelationship, Integer> {
    @Query(value = "select a from PublishApiInstanceRelationship a where a.apiId=:apiId")
    List<PublishApiInstanceRelationship> getApiInstanceRels(@Param("apiId") Integer apiId);

    @Modifying
    @Transactional
    @Query("delete from PublishApiInstanceRelationship p where p.apiId=:apiId")
    void deleteByApiId(@Param("apiId") Integer apiId);
	
    @Query(value = "select new com.hisense.gateway.library.model.dto.buz.ApiInstanceDto(b.clusterId,b.clusterPartition) from PublishApiInstanceRelationship a left join Instance b  on a.instanceId=b.id   where a.apiId =:apiId")
    public List<ApiInstanceDto> getApiInstanceDtosPortal(@Param("apiId")Integer apiId);

    @Query(value = "select new com.hisense.gateway.library.model.pojo.base.ApiInstance(a.scaleApiId,b.clusterId,b.clusterPartition,b.host,b.accessToken) from PublishApiInstanceRelationship a left join Instance b  on a.instanceId=b.id   where a.apiId =:apiId")
    public List<ApiInstance> getApiInstancesPortal(@Param("apiId")Integer apiId);

    @Query(value = "select new com.hisense.gateway.library.model.dto.buz.ApiInstanceDto(b.clusterId,b.clusterPartition,b.requestProduction,b.requestSandbox) from PublishApiInstanceRelationship  a left join Instance b on a.instanceId=b.id where a.apiId=:apiId")
    List<ApiInstanceDto> getApiInstanceDtos(@Param("apiId") Integer apiId);

    @Query(value = "select new com.hisense.gateway.library.model.pojo.base.ApiInstance(a.scaleApiId,b.clusterId,b.clusterPartition,b.host,b.accessToken,b.requestProduction,b.requestSandbox) from PublishApiInstanceRelationship  a left join Instance b on a.instanceId=b.id where a.apiId=:apiId and b.clusterName=:env and b.clusterPartition in (:partitions)")
    List<ApiInstance> getApiInstances(@Param("apiId") Integer apiId,@Param("env") String env,@Param("partitions") List<String> partitions);

    @Query(value = "select a from PublishApiInstanceRelationship a where a.apiId=:apiId and a.instanceId=:instanceId")
    PublishApiInstanceRelationship getByAPIidAndInstanceId(@Param("apiId") Integer apiId, @Param("instanceId") Integer instanceId);

    @Query(value = "select a from PublishApiInstanceRelationship a where a.apiId=:apiId")
    List<PublishApiInstanceRelationship> getByAPIid(@Param("apiId")Integer apiId);

    @Query(value = "select new com.hisense.gateway.library.model.pojo.base.ApiInstance(a.scaleApiId,b.clusterId,b.clusterPartition,b.host,b.accessToken) from PublishApiInstanceRelationship a left join Instance b  on a.instanceId=b.id   where a.apiId in (:apiIdSet)")
    List<ApiInstance> findInstancesByApiIdsPortal(@Param("apiIdSet") Set<Integer> apiIdSet);

    @Query(value = "select new com.hisense.gateway.library.model.pojo.base.ApiInstance(a.scaleApiId,b.clusterId,b.clusterPartition,b.host,b.accessToken,b.requestProduction,b.requestSandbox) from PublishApiInstanceRelationship a left join Instance b on a.instanceId=b.id where a.apiId in (:apiIdSet)")
    List<ApiInstance> findInstancesByApiIds(@Param("apiIdSet") Set<Integer> apiIdSet);

    @Query(value = "select p.apiId from PublishApiInstanceRelationship p join Instance i on p.instanceId=i.id where i.clusterId=:clusterId and i.clusterPartition=:partition")
    List<Integer> findAllApiId(@Param("clusterId") String clusterId, @Param("partition") String partition);

    @Query(value = "select p.apiId from PublishApiInstanceRelationship p join Instance i on p.instanceId=i.id where i" +
            ".clusterPartition=:partition")
    List<Integer> findAllApiId(@Param("partition") String partition);

    @Query(value = "select p.apiId from PublishApiInstanceRelationship p join Instance i on p.instanceId=i.id where i.clusterPartition in(:partitions)")
    List<Integer> findAllByPartitions(@Param("partitions") List<String> partitions);

    @Query(value = "select a from PublishApiInstanceRelationship  a where a.instanceId=:instanceId and a" +
            ".scaleApiId=:scaleApiId")
    List<PublishApiInstanceRelationship> getApiInstanceRelByInsAndScaleApiId(@Param("instanceId") Integer instanceId,
                                                                             @Param("scaleApiId") Long scaleApiId);

    @Query(value = "select p.scaleApiId from PublishApiInstanceRelationship p join Instance i on p.instanceId=i.id " +
            "where i.clusterId=:clusterId and i.clusterPartition=:partition")
    List<Long> findAllScaleApiId(@Param("clusterId") String clusterId, @Param("partition") String partition);

    // guilai 2020/10/15
    @Query(value = "select p.apiId from PublishApiInstanceRelationship p where p.scaleApiId in(:scaleApiIds)")
    List<Integer> findApiIdsByScaleServiceIds(@Param("scaleApiIds") List<Long> scaleApiIds);
}
