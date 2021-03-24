package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.TimeQuery;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.base.monitor.*;
import com.hisense.gateway.library.model.pojo.base.PublishApplication;
import org.springframework.data.domain.Page;

import java.util.LinkedList;
import java.util.List;

/**
 * @author guilai.ming 2020/09/28
 */
public interface ElasticSearchService {
    /**
     * API调用日志-ES定向查询
     *
     * @param specialQuery 查询请求
     * @param clazz        其所代表的类的成员,必须是ApiLogRecord的子集
     */
    <T> Result<List<T>> queryForSpecial(ApiLogQuerySpecial specialQuery, Class<T> clazz);

    /**
     * 查询单个API的接口响应时间
     */
    Result<List<ApiResponseTime>> queryForList(ApiTrafficStatQuery invokeQuery);

    /**
     * 分页查询调用记录
     */
    Result<Page<ApiLogRecord>> queryForPage(ApiLogQueryFull invokeQuery);

    /**
     * 查询单个API的接口响应时间百分比
     */
    Result<ApiResponseStatistics> queryForStatistics(ApiTrafficStatQuery trafficStatQuery);

    /**
     * 查询单个调用日志详情
     */
    Result<ApiCastLog> queryForSingle(ApiLogQuerySingle apiLogQuerySingle);

    /**
     *根据订阅系统查询单个api的调用日志
     */
    public Result<LinkedList<ApiCastLog>> queryApiLogForSubSystem(Integer apiId, List<String> userKeys);

    Result<Long> queryCount(List<String> indexList,TimeQuery timeQuery, List<PublishApplication> params, int statType, Boolean removeSelf);


    /**
     * 接口异常日志查询
     * @param specialQuery
     * @return
     */
    Result<List<ApiCastLog>> queryForApiAndRescode(ApiLogQuerySpecial specialQuery);

    /**
     * 查询索引
     * @param timeQuery 时间
     * @return 索引
     */
    Result<List<String>> queryIndexList(TimeQuery timeQuery);
}
