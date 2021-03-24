/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/2/19
 */
package com.hisense.gateway.library.repository;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.model.base.portal.PublishApiStat;
import com.hisense.gateway.library.model.pojo.base.DataItem;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.Arrays;
import java.util.List;

public interface PublishApplicationRepository extends CommonQueryRepository<PublishApplication, Integer> {

    @Query("select p.system from PublishApplication  p join ProcessRecord pr on pr.relId=p.id and pr.type='application' and pr.status=2 where p.publishApi.id=:id and p.type=1 and pr.creator=:userName")
    List<Integer> findStatusByUserAndId(@Param("id") Integer id, @Param("userName") String userName);

    @Query("select p from PublishApplication p where p.publishApi.id=:publishApiId and p.instance.id=:instancesId and p.type=0")
    PublishApplication findByPublishApiId(@Param("publishApiId") Integer publishApiId ,@Param("instancesId")Integer instancesId);

    @Query("select p from PublishApplication p where p.instance.id=:instancesId and p.type=1 and p.system=:systemId and p.publishApi.id=:apiId and p.status=2")
    List<PublishApplication> findByPublishApiIdAndSystemId(@Param("instancesId")Integer instancesId, @Param("systemId")Integer systemId, @Param("apiId")Integer apiId);

    // portal

    @Query("select new com.hisense.gateway.library.model.base.portal.PublishApiStat (p.publishApi.id,count(p),pa" +
            ".systemId) " +
            "from PublishApplication p join PublishApi pa on p.publishApi.id = pa.id where p.type=1 and p.status=2 and pa.status=4 and pa.environment=:env group by p.publishApi.id ,pa.systemId  order by count(p) desc")
    List<PublishApiStat> subscribeApi(@Param("env")Integer env);

    @Query("select count(p) from PublishApi p join PublishApplication  pa on pa.publishApi.id=p.id where p.status=4 and pa.type=1 and pa.status=2 and p.id=:apiId")
    Integer subscribeCount(Integer apiId);

    @Query("select p from PublishApplication p where p.publishApi.id=:apiId and p.creator=:creator")
    PublishApplication findByUserNameAndApiId(String creator, Integer apiId);

    @Query("select u from PublishApplication u where u.publishApi.id=:id")
    List<PublishApplication> findApplicationByApiId(@Param("id")Integer id);

    @Modifying
    @Transactional
    @Query("delete from PublishApplication p where p.publishApi.id=:apiId")
    void deleteByApiId(@Param("apiId") Integer apiId);

    @Query("select u from PublishApplication u where u.creator=:creator and u.instance.id=:instanceId and u.apiPlan.id =:apiPlanId")
    PublishApplication findByUserNameAndInstanceIdAndPlanId(@Param("creator") String creator,
                                                         @Param("instanceId") Integer instanceId, @Param("apiPlanId") Integer apiPlanId);

    @Modifying
    @Transactional
    @Query("update PublishApplication set status=:status where id=:id")
    void updateStatus(@Param("id") Integer id, @Param("status") Integer status);

    @Query("select u from PublishApplication u where u.creator=:creator and u.instance.id=:instanceId and u.apiPlan.id =:apiPlanId and u.status =:status")
    List<PublishApplication> findByUserNameAndInstanceIdAndPlanIdAndStatus (@Param("creator") String creator, @Param("instanceId") Integer instanceId, @Param("apiPlanId") Integer apiPlanId, @Param("status") Integer status);
    
    @Query("select u from PublishApplication u where u.system=:systemId and u.instance.id=:instanceId and u.apiPlan.id =:apiPlanId and u.status =:status")
    List<PublishApplication> findBySystemAndInstanceIdAndPlanIdAndStatus(@Param("systemId") Integer systemId, @Param("instanceId") Integer instanceId,
    		@Param("apiPlanId") Integer apiPlanId,
    		@Param("status") Integer status);
    
    //@Query("select u from PublishApplication u where u.createUser.id <>:createId and u.instance.id=:instanceId and
    // u.apiPlan.id =:apiPlanId and u.status >0")
    @Query("select u from PublishApplication u where u.creator =:creator and u.instance.id=:instanceId and u.apiPlan" +
            ".id =:apiPlanId and u.status >0")
    List<PublishApplication> findByInstanceIdAndPlanIdAndUserName(@Param("instanceId")Integer instanceId, @Param(
            "apiPlanId") Integer apiPlanId, @Param("creator")String creator);

    // management
    @Modifying
    @Transactional
    @Query("update PublishApplication p set state=:state where p.id=:id")
    void updateState(@Param("id") Integer id, @Param("state") String state);

    @Query("select p from PublishApplication p where p.status=1 and p.publishApi.id =:apiId")
    List<PublishApplication> findByAppId(@Param("apiId") Integer apiId);

    @Query("select p from PublishApplication p where p.publishApi.id =:apiId and p.status in(:status) and p.type =:type")
    List<PublishApplication> findByApiIdAndStatusAndType(@Param("apiId")Integer apiId, @Param("status") List<Integer> status, @Param("type")Integer type);

    @Query("select p from PublishApplication p where p.status>=1 and p.publishApi.id =:apiId")
    List<PublishApplication> findAllByAppId(@Param("apiId") Integer apiId);

    @Query("select p from PublishApplication p where p.status>0 and p.publishApi.id =:apiId")
    List<PublishApplication> findKeyByAppId(@Param("apiId") Integer apiId);

    @Query("select p from PublishApplication p where p.scaleApplicationId =:scaleApplicationId and p.instance.id " +
            "=:instanceId")
    List<PublishApplication> findByScaleAppId(@Param("scaleApplicationId") String scaleApplicationId,
                                              @Param("instanceId") Integer instanceId);

    @Query("select u from PublishApplication u where u.instance.id=:instanceId and u.apiPlan.id =:apiPlanId")
    List<PublishApplication> findByInstanceIdAndPlanId(@Param("instanceId") Integer instanceId, @Param("apiPlanId") Integer apiPlanId);

    @Query("select p from PublishApplication p where p.status=:status and p.publishApi.id =:apiId")
    List<PublishApplication> findByApiIdAndStatus(@Param("apiId") Integer apiId, @Param("status") Integer status);

    @Query("select p from PublishApplication p JOIN  ProcessRecord pr " +
            "on pr.relId=p.id and pr.type='application' and pr.status=:status " +
            "where p.system =:systemId and p.publishApi.id=:apiId and p.type=:type")
    List<PublishApplication> findSubscribedApiBySystem(@Param("systemId") Integer systemId,@Param("apiId") Integer apiId,@Param("status") Integer status,@Param("type") Integer type);

    @Query("select p from PublishApplication p JOIN  ProcessRecord pr " +
            "on pr.relId=p.id and pr.type='application' and pr.status in (:status) " +
            "where p.system =:systemId and p.publishApi.id=:apiId and p.type=:type")
    List<PublishApplication> findSubscribedApiBySystemAndStatus(@Param("systemId") Integer systemId,@Param("apiId") Integer apiId,@Param("status") List<Integer> status,@Param("type") Integer type);

    @Query("select p from PublishApplication p JOIN  ProcessRecord pr " +
            "on pr.relId=p.id and pr.type='application' and p.userKey is not null " +
            "where p.system =:systemId  and p.type=:type and p.publishApi.environment=:env")
    List<PublishApplication> findSubscribedsBySystem(@Param("systemId") Integer systemId,@Param("type") Integer type,@Param("env") Integer env);


    @Query("select p from PublishApplication p JOIN  ProcessRecord pr " +
            "on pr.relId=p.id and pr.type='application' and pr.status in (1,2) " +
            "where p.system =:systemId and p.publishApi.id=:apiId and p.type=1 and pr.creator=:userName")
    List<PublishApplication> findSubRecordsBySystemAndPer(@Param("systemId") Integer systemId,@Param("apiId") Integer apiId,@Param("userName") String userName);

    @Query("select p from PublishApplication p where p.publishApi.id=:apiId and p.system =:systemId  and p.type=0 and p.publishApi.status=4")
    PublishApplication findByApiIdAndSystemId(@Param("apiId") Integer apiId, @Param("systemId") Integer systemId);

    @Query("select p from PublishApplication p where p.publishApi.id=:apiId and p.system =:systemId  and p.type=0 and p.publishApi.status<>0")
    PublishApplication findByApiAndSystemId(@Param("apiId") Integer apiId, @Param("systemId") Integer systemId);

    @Query("select p from PublishApplication p where p.status=2 and p.publishApi.id =:apiId and p.type=1")
    List<PublishApplication> findSubscribedAppByApiId(@Param("apiId") Integer apiId);

    // 2020/10/20
    @Query("SELECT new com.hisense.gateway.library.model.pojo.base.PublishApplication(app.system,app" +
            ".scaleApplicationId) FROM PublishApplication app WHERE app.publishApi.id =:apiId and " +
            "app.type=1 and app.userKey is not null GROUP BY app.system,app.scaleApplicationId")
    List<PublishApplication> findSubscribedScaleAppByApiId(@Param("apiId") Integer apiId);

    @Query("select p from PublishApplication p where p.status=:status and p.publishApi.id =:apiId and p.userKey is not null")
    List<PublishApplication> findSubscribedAppByApiIdAndStatus(@Param("apiId") Integer apiId, @Param("status") Integer status);

    @Modifying
    @Transactional
    @Query("update PublishApplication p set state=:state where p.system =:systemId and p.publishApi.id=:apiId")
    void updateStateByApiIdAndSystem(@Param("state") String state, @Param("apiId") Integer apiId, @Param("systemId") Integer systemId);

    // for es query
    @Query("select d from DataItem d where d.id =(select p.system from PublishApplication p where (p.status=2 or p.status=4) and p.userKey=:userKey and p.type=1 group by p.system)")
    DataItem findSubscribedSystemNameByUserKey(@Param("userKey") String userKey);

    // for es query
    @Query("select d from DataItem d where d.id =(select p.system from PublishApplication p where p.status=1 and p.publishApi.id=:apiId and p.type=0 group by p.system)")
    DataItem findCreateSystemNameByApiId(@Param("apiId") Integer apiId);

    // for es query
    @Query("select  p from PublishApplication p where p.type=1 and p.system=:systemId  and p.publishApi.id=:apiId and p.status=:status")
    List<PublishApplication> findAppByApiIdAndSystem(@Param("systemId") Integer systemId,@Param("apiId") Integer apiId,@Param("status") Integer status);

    @Query(value ="SELECT \n" +
            "app.id,\n"+
            "app.api_id,\n" +
            "app.SCALE_APPLICATION_ID,\n" +
            "app.INSTANCE_ID,\n" +
            "app.CREATOR,\n" +
            "uir.ACCOUNT_ID,\n" +
            "gi.host,\n" +
            "gi.ACCESS_TOKEN,\n" +
            "app.app_id,\n" +
            "app.app_key\n" +
            "FROM PUBLISH_APPLICATION app \n" +
            "INNER JOIN PUBLISH_API api on api.id = app.api_id and api.status<>0 \n" +
            "INNER JOIN GW_INSTANCE gi on app.INSTANCE_ID = gi.ID \n" +
            "INNER JOIN USER_INSTANCE_RELATIONSHIP uir on uir.INSTANCE_ID = app.INSTANCE_ID and app.CREATOR = uir.USER_NAME\n" +
            "WHERE ((app.type=0 and app.STATUS=1 ) or(app.type=1 and app.STATUS=2)) and app.USER_KEY is null and app.SCALE_APPLICATION_ID is not null", nativeQuery = true)
    List<JSONObject> queryDataList();

    @Modifying
    @Transactional
    @Query(value ="update publish_application p set p.app_id=:appId,p.app_key=:appKey where  p.id=:id ",nativeQuery = true)
    int updateAppIdAndAppKey(@Param("id") Integer id,@Param("appId") String appId,@Param("appKey") String appKey);

    @Query(value = "SELECT\n" +
            "app.SCALE_APPLICATION_ID,\n" +
            "uir.ACCOUNT_ID,\n" +
            "gi.host,\n" +
            "gi.ACCESS_TOKEN,\n" +
            "app.app_id,\n" +
            "app.app_key\n" +
            "FROM PUBLISH_APPLICATION app \n" +
            "INNER JOIN PUBLISH_API api on api.id = app.api_id and api.status<>0 \n" +
            "INNER JOIN GW_INSTANCE gi on app.INSTANCE_ID = gi.ID \n" +
            "INNER JOIN USER_INSTANCE_RELATIONSHIP uir on uir.INSTANCE_ID = app.INSTANCE_ID and app.CREATOR = uir.USER_NAME\n" +
            "WHERE app.id=:id and app.USER_KEY is null and app.SCALE_APPLICATION_ID is not null", nativeQuery = true)
   JSONObject queryApplicationById(@Param("id") Integer id);

    @Query(value = "SELECT\n" +
            "app.SCALE_APPLICATION_ID,\n" +
            "uir.ACCOUNT_ID,\n" +
            "gi.host,\n" +
            "gi.ACCESS_TOKEN,\n" +
            "app.app_id,\n" +
            "app.app_key\n" +
            "FROM PUBLISH_APPLICATION app \n" +
            "INNER JOIN PUBLISH_API api on api.id = app.api_id and api.status<>0\n" +
            "INNER JOIN GW_INSTANCE gi on app.INSTANCE_ID = gi.ID \n" +
            "INNER JOIN USER_INSTANCE_RELATIONSHIP uir on uir.INSTANCE_ID = app.INSTANCE_ID and app.CREATOR = uir.USER_NAME\n" +
            "WHERE app.api_id=:apiId and app.type=0 and app.USER_KEY is null and app.SCALE_APPLICATION_ID is not null", nativeQuery = true)
    List<JSONObject> queryApplicationByApiId(@Param("apiId") Integer apiId);

    @Query("select  p from PublishApplication p where p.type=1 and p.system=:systemId  and p.publishApi.id=:apiId and p.status in (2,4)")
    List<PublishApplication> queryAppByApiIdAndSystem(@Param("systemId") Integer systemId,@Param("apiId") Integer apiId);

    @Query("select  p from PublishApplication p where p.type=0 and p.publishApi.id in (:apiId) and p.status = 1")
    List<PublishApplication> queryAppByApiIds(@Param("apiId") List<Integer> apiId);
}
