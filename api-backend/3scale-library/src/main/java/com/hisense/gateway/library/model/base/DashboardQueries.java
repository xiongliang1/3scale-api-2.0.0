package com.hisense.gateway.library.model.base;

import lombok.Data;

/**
 * 仪表盘查询
 * <p>
 * 2020/10/20 guilai.ming
 */
public class DashboardQueries {
    @Data
    public static class ApiInvokeQuery{
        Integer system;//订阅系统
        TimeQuery timeQuery;// 查询时间区间
    }
}
