package com.hisense.gateway.library.repository;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.model.pojo.base.ApiInvokeRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

/**
 * @ClassName: ApiInvokeRepository
 * @Description: TOOD
 * @Author: zhouguokai.ex
 * @createDate: 2020/11/20
 * @version: 1.0
 */
public interface ApiInvokeRecordRepository extends CommonQueryRepository<ApiInvokeRecord, Integer>  {

    @Modifying
    @Transactional
    @Query(value ="TRUNCATE TABLE API_INVOKE_RECORD", nativeQuery = true)
    void deleteInfo();

    @Modifying
    @Transactional
    @Query(value ="delete from API_INVOKE_RECORD ai where ai.INVOKE_DAY = :date  and ai.api_id = :apiId and ai.SCALE_APPLICATION_ID =:scaleApplicationId", nativeQuery = true)
    void deleteInfoByInvokeDay( @Param("date") String date, @Param("apiId") Integer apiId,@Param("scaleApplicationId") Integer scaleApplicationId);

    @Query(value = "SELECT \n" +
            "api.id as api_id,\n" +
            "pair.scale_api_id,\n" +
            "app.SCALE_APPLICATION_ID,\n" +
            "app.system,\n" +
            "gi.host,\n" +
            "gi.ACCESS_TOKEN \n" +
            "FROM publish_application app\n" +
            "INNER JOIN publish_api api ON app.api_id = api.id\n" +
            "INNER JOIN GW_INSTANCE gi on app.INSTANCE_ID = gi.ID\n" +
            "INNER JOIN PUBLISH_API_INSTANCE_RELATIONSHIP pair on api.id = pair.api_id and gi.id = pair.instance_id\n" +
            "WHERE app.type = 1\n" +
            "AND app.status =2 \n" +
            "AND api.status = 4 \n" +
            "GROUP BY api.id,pair.scale_api_id,app.SCALE_APPLICATION_ID,app.system,gi.host,gi.ACCESS_TOKEN", nativeQuery = true)
    List<JSONObject> queryApiInvokeHistiryList();

    @Query(value = "SELECT \n" +
            "api_id as api_id,\n" +
            "sum(ai.INVOKE_COUNT) as total\n" +
            "FROM API_INVOKE_RECORD ai \n" +
            "WHERE \n" +
            "ai.INVOKE_DAY =:date \n" +
            "and ai.api_id in (:apiIds) \n" +
            "GROUP BY api_id", nativeQuery = true)
    List<JSONObject> queryApiInvokeValue(@Param("date") String date, @Param("apiIds") List<Integer> apiIds);

    @Query(value = "SELECT \n" +
            "api_id as api_id,\n" +
            "sum(ai.INVOKE_COUNT) as total\n" +
            "FROM API_INVOKE_RECORD ai \n" +
            "WHERE \n" +
            " ai.api_id in (:apiIds) \n" +
            "GROUP BY api_id", nativeQuery = true)
    List<JSONObject> queryApiInvokeTotalValue(@Param("apiIds") List<Integer> apiIds);

    @Query(value = "SELECT \n" +
            "nvl(sum(invoke_count),0)\n" +
            "FROM API_INVOKE_RECORD ai \n" +
            "WHERE \n" +
            " ai.api_id =:apiId \n" +
            "and ai.SYSTEM =:system ", nativeQuery = true)
    int queryApiInvokeCountBySystem(@Param("apiId") String apiId,@Param("system") String system);

    @Query(value = "SELECT \n" +
            "nvl(sum(invoke_count),0)\n" +
            "FROM API_INVOKE_RECORD ai \n" +
            "WHERE \n" +
            " ai.api_id=:apiId \n" +
            "and ai.SYSTEM =:system \n" +
            "and ai.INVOKE_DAY =:date", nativeQuery = true)
    int queryApiInvokeCountBySystemAndInvokeDay( @Param("date") String date,@Param("apiId") String apiId,@Param("system") String system);

}
