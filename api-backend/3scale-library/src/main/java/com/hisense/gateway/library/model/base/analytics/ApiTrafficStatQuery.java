package com.hisense.gateway.library.model.base.analytics;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.TimeQuery;
import lombok.Data;
import lombok.Setter;

import java.util.List;

import static com.hisense.gateway.library.constant.AnalyticsConstant.*;
import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.INVOKE_BY_SINGLE_SUB_SYSTEM;
import static com.hisense.gateway.library.constant.AnalyticsConstant.StatType.RESPONSE_TIME_PERCENTILE_FOR_APP;

/**
 * 接口调用量统计查询
 */
@Data
public class ApiTrafficStatQuery {
    /**
     * 租户
     */
    @JsonIgnore
    private String tenantId;

    /**
     * 集群环境
     */
    @JsonIgnore
    private String environment;
    /**
     * 当前所在的API ID
     */
    private Integer apiId;
    /**
     * 当前指定的系统对应的3scale-application的ID, 使用getSubscribeSystems查询得到,
     * 点击具体某个订阅系统的调用量时, 需传入
     * 2020/10/20 guilai.ming
     */
    private Integer appId;

    /**
     * 统计类型:
     */
    private StatType statType;

    /**
     * 统计粒度
     */
    private StatGranularity granularity;

    /**
     * 统计区间
     */
    private TimeQuery timeQuery;

    @JsonIgnore
    private Long scaleServiceId;

    @JsonIgnore
    private String appUserKey;

    @JsonIgnore
    private List<String> indexList;

    public void setStatType(int statType) {
        this.statType = StatType.fromCode(statType);
    }

    public void setGranularity(int granularity) {
        this.granularity = StatGranularity.fromCode(granularity);
    }

    /**
     * 参数有效性检查
     *
     * @return 有效: "1", 无效: 错误提示
     */
    @JsonIgnore
    public String isValid() {
        if (apiId == null) {
            return ANA_QUERY_PARAM_ERROR_NULL_APIID;
        }

        if (statType == null) {
            return ANA_QUERY_PARAM_ERROR_NONE_EXIST_STAT_TYPE;
        }

        if (granularity == null) {
            return ANA_QUERY_PARAM_ERROR_NONE_EXIST_STAT_GRANULARITY;
        }

        if (statType == INVOKE_BY_SINGLE_SUB_SYSTEM && appId == null) {
            return ANA_QUERY_PARAM_ERROR_INVOKE_BY_SYSTEM_NEED_APPID;
        }

        if (statType == RESPONSE_TIME_PERCENTILE_FOR_APP && appId == null) {
            return ANA_QUERY_PARAM_ERROR_RSPSTAT_BY_SYSTEM_NEED_APPID;
        }

        switch (granularity) {
            case DAYS_7:
            case DAYS_30:
            case HOURS_24:
            case MONTHS_12:
                if (timeQuery != null && Result.OK.equals(timeQuery.isValid())) {
                    return ANA_QUERY_PARAM_ERROR_STAT_GRANULARITY_SPECIAL_DAY_HOUR_MONTH_TIMEQUERY;
                }
                break;

            case EXACT_DURATION_HOURS:
            case EXACT_DURATION_DAYS:
            case EXACT_DURATION_MONTHS:
                if (timeQuery == null || !Result.OK.equals(timeQuery.isValid())) {
                    return ANA_QUERY_PARAM_ERROR_STAT_GRANULARITY_EXACT_DURATION_INVALID_TIMEQUERY;
                }
                break;
        }

        return Result.OK;
    }
}
