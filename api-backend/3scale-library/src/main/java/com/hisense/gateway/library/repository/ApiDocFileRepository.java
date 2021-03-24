package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.pojo.base.ApiDocFile;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ApiDocFileRepository extends CommonQueryRepository<ApiDocFile, Integer> {

    @Query("select d from ApiDocFile d where d.apiId = :apiId")
    List<ApiDocFile> findByApiId(@Param("apiId")Integer apiId);

    @Query("select p from ApiDocFile p where p.fileType=:fileType and p.apiId=:apiId order by p.createTime desc")
    List<ApiDocFile> findApiDocByTypeAndApiId(@Param("apiId")Integer apiId, @Param("fileType")String fileType);
}
