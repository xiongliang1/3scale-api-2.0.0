package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.GlobalSearchApiInfo;

/**
 * 2020/10/20 guilai.ming
 */
public interface GlobalSearchService {
    /**
     * 使用url前缀/mappingRule 搜索API信息,返回API所属的project和API名称
     * @param environment 实例环境
     * @param path url前缀/mappingRule
     */
    Result<GlobalSearchApiInfo> findApiInfoByEndpointUrl(String environment, String path);
}
