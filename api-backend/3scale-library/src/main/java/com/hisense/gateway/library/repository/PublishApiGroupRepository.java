package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.PublishApiGroup;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

public interface PublishApiGroupRepository extends CommonQueryRepository<PublishApiGroup, Integer> {
    @Query("select p from PublishApiGroup p where p.status=1 and p.categoryOne=:categoryOne and p.categoryTwo=:categoryTwo and p.system=:system and p.environment=:environment and p.projectId=:projectId order by p.createTime asc")
    List<PublishApiGroup> findByParam(@Param("categoryOne") Integer categoryOne,@Param("categoryTwo") Integer categoryTwo,
                                      @Param("system") Integer system, @Param("projectId") String projectId,@Param("environment") Integer environment);

    @Query("select p from PublishApiGroup p where p.status=1 and p.projectId=:projectId order by p.updateTime desc")
    List<PublishApiGroup> findAllByProjectId(@Param("projectId") String projectId);

    @Query("select p from PublishApiGroup p where p.status=1 and p.categoryOne=:categoryOne")
    List<PublishApiGroup> getCategoryOne(@Param("categoryOne") Integer categoryOne);

    @Query("select p from PublishApiGroup p where p.status=1 and p.categoryTwo=:categoryTwo")
    List<PublishApiGroup> getCategoryTwo(@Param("categoryTwo") Integer categoryTwo);

    @Modifying
    @Transactional
    @Query("update PublishApiGroup set status=0 , updateTime=:updateTime where id=:id")
    void logicDeleteById(@Param("id") Integer id, @Param("updateTime") Date updateTime);

    @Query("select p from PublishApiGroup p where p.status=1 and p.system=:system")
    List<PublishApiGroup> findBySystem(@Param("system") Integer system);

    @Query("select p from PublishApiGroup p where p.status=1 and p.projectId=:projectId and p.name=:name and p.environment=:environment")
    List<PublishApiGroup> findByProjectAndName(@Param("projectId") String projectId, @Param("name") String name, @Param("environment") Integer environment);

    @Query("select p.system from PublishApiGroup p where p.tenantId=:tenantId and p.projectId=:projectId and p.system" +
            " in(select e.systemId from EurekaService e where e.status = 1 group by e.systemId)")
    List<Integer> findSystemByTenantProjectAndService(@Param("tenantId") String tenantId,
                                                      @Param("projectId") String projectId);

    @Query("select p from PublishApiGroup p where p.tenantId=:tenantId and p.projectId=:projectId order by p.id asc ")
    List<PublishApiGroup> findByTenantAndProject(@Param("tenantId") String tenantId,
                                                 @Param("projectId") String projectId);
}
