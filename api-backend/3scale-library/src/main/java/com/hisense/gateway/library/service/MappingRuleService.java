package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApiMappingRuleInfo;
import com.hisense.gateway.library.model.pojo.base.ApiMappingRule;
import com.hisense.gateway.library.model.pojo.base.PublishApi;

import java.util.List;

public interface MappingRuleService {
    /**
     * 查询api对应的MappingRule;
     */
    List<ApiMappingRule> findRuleByApiId(Integer apiId);

    List<ApiMappingRuleInfo> findRuleByApi(Integer apiId,String rule);

    Result<List<Integer>> saveRules(List<ApiMappingRule> rules, Integer apiId);

    Result<List<Integer>> updateRules(List<ApiMappingRule> rules, Integer apiId);

    /**
     * 检查是否存在相同MappingRule的、未删除的API
     *
     * @param apiMappingRules 路由规则
     * @return OK:存在相同rule的API, FAIL: 不存在
     */
    Result<List<PublishApi>> existApisWithSameMappingRule(List<ApiMappingRule> apiMappingRules,Integer envCode);
}
