package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.EurekaService;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface EurekaServiceRepository extends CommonQueryRepository<EurekaService, Integer> {
    @Modifying
    @Transactional
    @Query("delete from EurekaService p where p.systemName=:systemName")
    void deleteBySystemName(@Param("systemName") String systemName);

    @Query(value = "select * from meta_eureka_service p where p.system_name=:systemName and rownum <=1", nativeQuery = true)
    //@Query(value = "select * from meta_eureka_service p where p.system_name=:systemName limit 1", nativeQuery = true)
    EurekaService findBySystemName(@Param("systemName") String systemName);

    @Query("select p from EurekaService p where p.eurekaZone =:eurekaZone and p.status = 1")
    List<EurekaService> findByEurekaZone(@Param("eurekaZone") String eurekaZone);
}
