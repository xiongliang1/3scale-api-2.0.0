package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.PublishApiPolicyRelationship;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface PublishApiPolicyRelationshipRepository extends CommonQueryRepository<PublishApiPolicyRelationship,
        Integer> {
    @Modifying
    @Transactional
    @Query("delete from PublishApiPolicyRelationship p where p.publishApiId=:publishApiId")
    void deleteByApiId(@Param("publishApiId") Integer publishApiId);

    @Modifying
    @Transactional
    @Query("delete from PublishApiPolicyRelationship p where p.id=:id")
    void deleteRel(@Param("id") Integer id);

    @Query("select p from PublishApiPolicyRelationship p where p.publishPolicyId=:publishPolicyId")
    List<PublishApiPolicyRelationship> findAllByPoliciId(@Param("publishPolicyId") Integer id);

    @Query(nativeQuery = true, value = "select COUNT(num) from (select COUNT(p.id) as num from " +
            "publish_api_policy_relationship p where p.publish_policy_id=:publishPolicyId GROUP BY p.publish_api_id) b")
    Integer findNumByPolicyId(@Param("publishPolicyId") Integer id);

    @Query("select p from PublishApiPolicyRelationship p where p.publishApiId=:apiId and p.instanceId=:instanceId")
    PublishApiPolicyRelationship findAllByApiIdAndInstanceId(@Param("apiId") Integer apiId,
                                                             @Param("instanceId") Integer instanceId);

    @Modifying
    @Transactional
    @Query("update PublishApiPolicyRelationship set publishPolicyId=:publishPolicyId where id=:id")
    void updatePublishPolicyIdById(@Param("id") Integer id, @Param("publishPolicyId") Integer publishPolicyId);

    @Query("select p from PublishApiPolicyRelationship p where p.publishApiId=:apiId and p.instanceId=:instanceId")
    PublishApiPolicyRelationship findByApiIdAndInstanceId(@Param("apiId") Integer apiId,
                                                          @Param("instanceId") Integer instanceId);

    @Query("select p from PublishApiPolicyRelationship p where p.publishApiId=:apiId and p.instanceId=:instanceId and p.scalePolicyId=:serviceId")
    PublishApiPolicyRelationship findByApiIdAndInstanceIdAnscaleId(@Param("apiId") Integer apiId,
                                                          @Param("instanceId") Integer instanceId,@Param("serviceId") Long serviceId);
    @Query("select p from PublishApiPolicyRelationship p where p.publishApiId=:apiId")
    List<PublishApiPolicyRelationship> findByApiId(@Param("apiId") Integer apiId);

    @Query("select p from ApiPolicy a join PublishApiPolicyRelationship p on p.publishPolicyId=a.id" +
            " where p.publishApiId=:apiId and a.type='limit'and  p.publishApiId=:apiId")
    List<PublishApiPolicyRelationship> findPublishByApiId(@Param("apiId") Integer apiId);

}
