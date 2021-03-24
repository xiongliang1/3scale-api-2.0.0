package com.hisense.gateway.library.repository;

import com.hisense.gateway.library.model.base.GlobalSearchApiInfo;
import com.hisense.gateway.library.model.pojo.base.ApiMappingRule;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.transaction.Transactional;
import java.util.List;

public interface MappingRuleRepository extends CommonQueryRepository<ApiMappingRule, Integer> {
    @Query("select p from ApiMappingRule p where p.partition=:partition and p.httpMethod=:method and p.pattern=:pattern and p.publishApi.environment=:environment and p.publishApi.status <> 0")
    List<ApiMappingRule> findByPartitionMethodPattern(@Param("partition") Integer partition,
                                                      @Param("method") String method, @Param("pattern") String pattern,@Param("environment") Integer environment);

    @Query("select p from ApiMappingRule p where p.publishApi.id =:apiId")
    List<ApiMappingRule> findRuleByApiId(@Param("apiId") Integer apiId);

    @Query("select p from ApiMappingRule p where p.publishApi.environment=:evnCode and  p.publishApi.id <>:apiId or p.publishApi.id is null")
    List<ApiMappingRule> findRuleByNotApiId(@Param("apiId") Integer apiId,@Param("evnCode") Integer evnCode);

    @Query("select p from ApiMappingRule p where p.publishApi.environment=:evnCode and p.publishApi.status <> 0")
    List<ApiMappingRule> findRuleByEvn(@Param("evnCode") Integer evnCode);

    @Modifying
    @Transactional
    @Query("delete from ApiMappingRule p where p.publishApi.id=:apiId")
    void deleteByApiId(@Param("apiId") Integer apiId);

    // 2020/10/20 guilai.ming
    @Query("select new com.hisense.gateway.library.model.base.GlobalSearchApiInfo(a.publishApi.id, a.publishApi.name," +
            " a.publishApi.systemId, g.projectId) from ApiMappingRule a left join PublishApiGroup g on a.publishApi" +
            ".group.id = g.id where a.pattern =:path and a.publishApi.environment =:environment group by a" +
            ".publishApi.id, a.publishApi.name, a.publishApi" +
            ".systemId, g.projectId")
    List<GlobalSearchApiInfo> findApiInfoByUrlRulePath(@Param("environment") Integer environment,
                                                       @Param("path") String path);
}
