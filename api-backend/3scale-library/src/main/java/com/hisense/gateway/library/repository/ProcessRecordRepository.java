package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.dto.web.ProcessRecordDto;
import com.hisense.gateway.library.model.pojo.base.ProcessRecord;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

public interface ProcessRecordRepository extends CommonQueryRepository<ProcessRecord, Integer> {
    @Query(value = "SELECT * from process_record a where 1=1 and (:system IS NULL OR :system = '' or json_contains(a" +
            ".ext_var, JSON_OBJECT('appSystem',:system)))", nativeQuery = true)
    List<ProcessRecord> findApplyApplication(@Param("system") Integer system);

    @Query("select pr from ProcessRecord pr where pr.status=:status and pr.relId in(:applicationIds)")
    List<ProcessRecord> findByRelIds(@Param("applicationIds") List<Integer> applicationIds,
                                     @Param("status") Integer status);

    @Query("select pr from ProcessRecord pr where pr.type ='application' and pr.status in(:status) and pr.relId in(:applicationIds)")
    List<ProcessRecord> findSubscribeByRelIdAndStatus(@Param("applicationIds") List<Integer> applicationIds,
                                             @Param("status") List<Integer> status);

    @Query("select pr from ProcessRecord pr where pr.type ='publish_api' and pr.status=:status and pr.relId =:appId order by pr.createTime desc")
    List<ProcessRecord> findProcessRecordByRelId(@Param("appId") Integer appId, @Param("status") Integer status);

    // 2020/09/18 guilai.ming,remove user table
    /*@Query(value = "SELECT " +
            "pr.create_id, " +
            "(SELECT gu.name from GW_USER gu WHERE gu.id = pr.create_id ) as create_user, " +
            "pr.update_id, " +
            "(SELECT gu.name from GW_USER gu WHERE gu.id = pr.update_id ) as update_user " +
            " FROM PROCESS_RECORD pr " +
            "INNER JOIN PUBLISH_APPLICATION app ON pr.rel_id = app.id " +
            "INNER JOIN PUBLISH_API pa ON app.api_id = pa.id " +
            "INNER JOIN PUBLISH_API_GROUP pag ON pa.group_id = pag.id " +
            "WHERE pag.tenant_id = :tenantId AND pag.project_id = :projectId AND pr.type = 'application' GROUP BY pr.create_id,pr.update_id order by pr.create_id,pr.update_id ", nativeQuery = true)//TODO:application
    List<Map> findSubscribers(@Param("tenantId") String tenantId, @Param("projectId") String projectId);
    */

    @Query(value = "SELECT " +
            "pr.creator, pr.updater " +
            "FROM PROCESS_RECORD pr " +
            "INNER JOIN PUBLISH_APPLICATION app ON pr.rel_id = app.id " +
            "INNER JOIN PUBLISH_API pa ON app.api_id = pa.id " +
            "INNER JOIN PUBLISH_API_GROUP pag ON pa.group_id = pag.id " +
            "WHERE pag.tenant_id = :tenantId AND pag.project_id = :projectId AND pr.type = 'application' GROUP BY pr.creator,pr.updater order by pr.creator,pr.updater ", nativeQuery = true)//TODO:application
    List<Map> findSubscribers(@Param("tenantId") String tenantId, @Param("projectId") String projectId);
    // portal

    @Modifying
    @Transactional
    @Query("update ProcessRecord p set p.status=:status where p.relId=:relId")
    void updateByRelId(@Param("relId")Integer relId, @Param("status")Integer status);

    @Query("select p from ProcessRecord p where p.relId=:id ")
    ProcessRecord findByApiId(Integer id);

    @Query("select p from ProcessRecord p where p.relId=:id ")
    List<ProcessRecordDto> findMy(Integer id);
}
