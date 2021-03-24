/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.service;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.analytics.ApiTrafficStatQuery;
import com.hisense.gateway.library.model.base.SubscribeSystem;
import com.hisense.gateway.library.stud.model.AnalyticsDto;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * AnalyticsService
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 10:34
 */
public interface AnalyticsService {
    Result<AnalyticsDto> getServiceAnalytics(ApiTrafficStatQuery statQuery);

    /**
     * 查询结果导出
     *
     * @param statQuery
     * @return
     */
    XSSFWorkbook downloadApiStatisticsData(ApiTrafficStatQuery statQuery);

    /**
     * 返回当前API的所有订阅系统
     *
     * @param apiId apid id
     * @return 订阅系统名称
     */
    Result<List<SubscribeSystem>> getSubscribeSystems(Integer apiId);

    /**
     * 返回当前订阅系统
     * @param apiId
     * @param recordId
     * @return
     */
    Result<SubscribeSystem> getSubscribeSystems(Integer apiId,Integer recordId);
}
