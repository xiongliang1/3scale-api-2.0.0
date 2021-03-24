package com.hisense.gateway.library.constant;

/**
 * @author guilai.ming 2020/09/06
 */
public class AnalyticsConstant {
    /**
     * 统计类型对应的3scale-api地址
     */
    // 108-
    private static final String SERVICE_STAT_BY_USAGE = "/stats/services/%s/usage.json" +
            "?access_token=%s&metric_name=%s&since=%s&until=%s&granularity=%s&skip_change=true&timezone=%s";

    // 108
    public static final String SERVICE_STAT_BY_APPLICATION = "/stats/applications/%s/usage.json" +
            "?access_token=%s&metric_name=%s&since=%s&until=%s&granularity=%s&skip_change=true&timezone=%s";

    // 107
    private static final String SERVICE_STAT_BY_RESPONSE_CODE = "/stats/services/%s/usage_response_code.json?" +
            "?access_token=%s&response_code=%s&since=%s&until=%s&granularity=%s&skip_change=true&timezone=%s";

    public static final String ANA_QUERY_PARAM_ERROR_NULL_APIID = "指定的API ID为空";
    public static final String ANA_QUERY_PARAM_ERROR_NONE_EXIST_API = "指定的API不存在";
    public static final String ANA_QUERY_PARAM_ERROR_NONE_EXIST_STAT_TYPE = "指定的统计类型不存在";
    public static final String ANA_QUERY_PARAM_ERROR_NONE_EXIST_STAT_GRANULARITY = "指定的统计粒度不存在";
    public static final String ANA_QUERY_PARAM_ERROR_INVOKE_BY_SYSTEM_NEED_APPID = "订阅系统调用量查询,必须指定appId";
    public static final String ANA_QUERY_PARAM_ERROR_RSPSTAT_BY_SYSTEM_NEED_APPID = "查询某个订阅系统时,必须指定appId";
    public static final String ANA_QUERY_PARAM_ERROR_INVOKE_BY_SYSTEM_NONE_EXIST_APP = "订阅系统调用量查询,指定的App不存在";
    public static final String ANA_QUERY_PARAM_ERROR_STAT_GRANULARITY_EXACT_DURATION_INVALID_TIMEQUERY
            = "指定起始终止时间区间时,必须指定有效的时间格式";
    public static final String ANA_QUERY_PARAM_ERROR_STAT_GRANULARITY_SPECIAL_DAY_HOUR_MONTH_TIMEQUERY
            = "选择当前24小时,或7天,或3天,或12月内时,无须指定起始和终止时间";

    /**
     * 统计类型
     * <p>
     * 0-调用总量
     * 1-所有订阅系统总调用量 = API调用总量 - 默认APP对应的调用量
     * 2-单个订阅系统调用量
     * 3-错误码-2XX-对应次数
     * 4-错误码-4XX-对应次数
     * 5-错误码-5XX-对应次数
     * 6-接口网络延迟百分比
     */
    public enum StatType {
        INVOKE_ALL(0, "调用总量", "hits", SERVICE_STAT_BY_USAGE),
        INVOKE_BY_ALL_SUB_SYSTEM(1, "所有订阅系统总调用量", "hits", null),
        INVOKE_BY_SINGLE_SUB_SYSTEM(2, "单个订阅系统调用量", "hits", SERVICE_STAT_BY_APPLICATION),
        ERROR_CODE_2XX(3, "错误码-2XX-对应次数", "2XX", SERVICE_STAT_BY_RESPONSE_CODE),
        ERROR_CODE_4XX(4, "错误码-4XX-对应次数", "4XX", SERVICE_STAT_BY_RESPONSE_CODE),
        ERROR_CODE_5XX(5, "错误码-5XX-对应次数", "5XX", SERVICE_STAT_BY_RESPONSE_CODE),
        RESPONSE_TIME_PERCENTILE_FOR_ALL(6, "(含所有系统)接口网络延迟百分比", "rsp", null),
        RESPONSE_TIME_PERCENTILE_FOR_APP(7, "(仅含指定的订阅系统)接口网络延迟百分比", "rsp", null);

        private final int code;
        private final String name;
        private final String metricName;
        private final String scaleApiPath;

        StatType(int code, String name, String metricName, String scaleApiPath) {
            this.code = code;
            this.name = name;
            this.metricName = metricName;
            this.scaleApiPath = scaleApiPath;
        }

        public int getCode() {
            return code;
        }

        public String getName() {
            return name;
        }

        public String getMetricName() {
            return metricName;
        }

        public String getScaleApiPath() {
            return scaleApiPath;
        }

        public static StatType fromCode(int code) {
            if (code == INVOKE_ALL.getCode()) {
                return INVOKE_ALL;
            } else if (code == INVOKE_BY_ALL_SUB_SYSTEM.getCode()) {
                return INVOKE_BY_ALL_SUB_SYSTEM;
            } else if (code == INVOKE_BY_SINGLE_SUB_SYSTEM.getCode()) {
                return INVOKE_BY_SINGLE_SUB_SYSTEM;
            } else if (code == ERROR_CODE_2XX.getCode()) {
                return ERROR_CODE_2XX;
            } else if (code == ERROR_CODE_4XX.getCode()) {
                return ERROR_CODE_4XX;
            } else if (code == ERROR_CODE_5XX.getCode()) {
                return ERROR_CODE_5XX;
            } else if (code == RESPONSE_TIME_PERCENTILE_FOR_ALL.getCode()) {
                return RESPONSE_TIME_PERCENTILE_FOR_ALL;
            } else if (code == RESPONSE_TIME_PERCENTILE_FOR_APP.getCode()) {
                return RESPONSE_TIME_PERCENTILE_FOR_APP;
            } else {
                return INVOKE_ALL;
            }
        }
    }

    /**
     * 统计粒度
     * <p>
     * 0: 24小时
     * 1: 7天
     * 2: 30天
     * 3: 12月
     * 4: 指定起始终止区间,以小时为单位
     * 5: 指定起始终止区间,以天为单位
     * 6: 指定起始终止区间,以月为单位
     */
    public enum StatGranularity {
        HOURS_24(0, 24, "hour", 24, "24小时"),
        DAYS_7(1, 24 * 7, "day", 7, "7天"),
        DAYS_30(2, 24 * 30, "day", 30, "30天"),
        MONTHS_12(3, 365 * 24, "month", 12, "12月"),
        EXACT_DURATION_HOURS(4, 0, "hour", 30, "指定起始终止区间,以小时为单位"),
        EXACT_DURATION_DAYS(5, 0, "day", 6, "指定起始终止区间,以天为单位"),
        EXACT_DURATION_MONTHS(6, 0, "month", 5, "指定起始终止区间,以月为单位");

        private final int code;
        private final int hours;
        private final String granularity;
        private final int defaultItemCount;
        private final String description;

        StatGranularity(int code, int hours, String granularity, int defaultItemCount, String description) {
            this.code = code;
            this.hours = hours;
            this.granularity = granularity;
            this.defaultItemCount = defaultItemCount;
            this.description = description;
        }

        public int getCode() {
            return code;
        }

        public int getHours() {
            return hours;
        }

        public String getGranularity() {
            return granularity;
        }

        public int getDefaultItemCount() {
            return defaultItemCount;
        }

        public String getDescription() {
            return description;
        }

        public static StatGranularity fromCode(int code) {
            if (HOURS_24.getCode() == code) {
                return HOURS_24;
            } else if (DAYS_7.getCode() == code) {
                return DAYS_7;
            } else if (DAYS_30.getCode() == code) {
                return DAYS_30;
            } else if (MONTHS_12.getCode() == code) {
                return MONTHS_12;
            } else if (EXACT_DURATION_HOURS.getCode() == code) {
                return EXACT_DURATION_HOURS;
            } else if (EXACT_DURATION_DAYS.getCode() == code) {
                return EXACT_DURATION_DAYS;
            } else if (EXACT_DURATION_MONTHS.getCode() == code) {
                return EXACT_DURATION_MONTHS;
            } else {
                return HOURS_24;
            }
        }
    }
}
