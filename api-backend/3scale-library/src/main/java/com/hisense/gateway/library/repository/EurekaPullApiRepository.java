package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.EurekaPullApi;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface EurekaPullApiRepository extends CommonQueryRepository<EurekaPullApi, Integer>  {

    @Query("select e from EurekaPullApi e order by e.createTime desc")
    List<EurekaPullApi> findByTime();
}
