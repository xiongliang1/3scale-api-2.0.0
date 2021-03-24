package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.AlertPolicy;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

/**
 * @author guilai.ming 2020/09/10
 */
public interface AlertPolicyRepository extends CommonQueryRepository<AlertPolicy, Integer> {
    @Modifying
    @Transactional
    @Query("update AlertPolicy set status=0,updateTime=:updateTime where id=(:policyIds)")
    void deleteByLogic(@Param("policyIds") List<Integer> policyIds, @Param("updateTime") Date updateTime);

    @Query(value = "select a from AlertPolicy a where a.status =1 and a.projectId=:projectId and a.name=:name and a.environment=:environment")
    List<AlertPolicy> findByProjectAndName(@Param("projectId") String projectId, @Param("name") String name,@Param("environment") Integer environment);

    @Query(value = "select a from AlertPolicy a where a.status =1 and a.enable=1 and a.apiIds is not null")
    List<AlertPolicy> findEnabledPolicies();
}
