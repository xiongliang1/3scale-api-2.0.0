/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020-02-19 @author jinshan
 */
package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.base.alert.AlertApiInfo;
import com.hisense.gateway.library.model.pojo.base.PublishApi;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface PublishApiRepository extends CommonQueryRepository<PublishApi, Integer> {
    // portal
    @Query("select pa.id from DataItem di left join PublishApiGroup pag on pag.categoryTwo=di.id left join PublishApi pa on pa.group=pag.id where pa.environment=:envCode and pa.status=4 and pa.partition=:partition and di.id =:categoryTwo")
    List<Integer> findApiByCategoryTwo(@Param("categoryTwo") Integer categoryTwo,@Param("envCode")Integer envCode,@Param("partition")Integer partition);

    @Query("select p from PublishApi p where p.environment=:envCode and p.status=4 and p.partition=:partition")
    List<PublishApi> findApiList(@Param("envCode")Integer envCode,@Param("partition")Integer partition);

    // management
    @Query("select p from PublishApi p where p.status>0 and p.name=:name and p.group.id =:groupId")
    List<PublishApi> findByNameAndProject(@Param("name") String name, @Param("groupId") Integer groupId);

    @Query("select count(p.id) from PublishApi p where p.status>0 and p.group.id =:groupId")
    Integer getNumByGroupId(@Param("groupId") Integer groupId);

    @Query("select p from PublishApi p where p.status>0 and p.group.id =:groupId")
    List<PublishApi> findByGroupId(@Param("groupId") Integer groupId);

    @Modifying
    @Transactional
    @Query("update PublishApi set status=:status where id=:id")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    @Query("select p from PublishApi p where p.status=0 and p.id=:apiId")
    PublishApi findApiByStatus(@Param("apiId") Integer apiId);

    @Modifying
    @Transactional
    @Query("update PublishApi set fileDocIds=:fileDocIds where id=:id")
    void updateFileDocIds(@Param("id") Integer id, @Param("fileDocIds") String fileDocIds);

    @Modifying
    @Transactional
    @Query(nativeQuery = true, value = "update publish_api set group_id =:groupId,status =:status where id =:id")
    void updateGroupId(@Param("id") Integer id, @Param("groupId") Integer groupId,@Param("status") Integer status);

    @Query("SELECT pa from PublishApi pa LEFT JOIN PublishApiInstanceRelationship pair ON pa.id=pair.apiId where pair" +
            ".instanceId in (:instanceId) AND pa.status>0 AND pa.url in (:url)")
    List<PublishApi> findByInstanceAndUrl(@Param("instanceId") List<Integer> instanceId,
                                          @Param("url") List<String> url);

    @Query("select p from PublishApi p where p.eurekaService.id =:serviceId and p.isOnline =0")
    List<PublishApi> findByServiceIdAndOffline(@Param("serviceId") Integer serviceId);

    @Query("select p from PublishApi p where p.eurekaService.id =:serviceId and p.status<>0")
    List<PublishApi> findByServiceId(@Param("serviceId") Integer serviceId);

    @Query("select p from PublishApi p where p.partition =:partition and p.sourceType = 1 and p.status<>0")
    List<PublishApi> findManualApiByPartition(@Param("partition") Integer partition);

    //@Query(nativeQuery = true, value = "select * from publish_api p where p.hash=:hash  and rownum <=1 order by p.hash desc")
    @Query(nativeQuery = true, value = "select * from publish_api p where p.hash=:hash order by p.hash desc limit 1")
    PublishApi findByHash(@Param("hash") String hash);

    @Modifying
    @Transactional
    @Query("update PublishApi set method=:method where id=:id")
    void updateMethod(@Param("id") Integer id, @Param("method") String method);

    @Query(nativeQuery = true,value = "SELECT pa.* FROM PROCESS_RECORD pr " +
            "INNER JOIN PUBLISH_APPLICATION app on pr.rel_id = app.id " +
            "INNER JOIN PUBLISH_API pa on app.api_id = pa.id " +
            "WHERE pa.STATUS=0 and pr.id in (:prId) ")
    List<PublishApi> findApiByProcessRecordId(@Param("prId") List<Integer> prId);

    // @author guilai.ming 2020/09/10
    @Query("select new com.hisense.gateway.library.model.base.alert.AlertApiInfo(p.id,p.name,p.group.name,p" +
            ".partition,p.needLogging) from PublishApi p where p.id in (:apiIds)")
    List<AlertApiInfo> findByApiIds(@Param("apiIds") List<Integer> apiIds);

    @Query("select p from PublishApi p where p.host=:host and p.accessProtocol=:port and p.environment=:envCode ")
    List<PublishApi> findApiByHost(@Param("host")String host,@Param("port")String port,@Param("envCode")Integer envCode);
}
