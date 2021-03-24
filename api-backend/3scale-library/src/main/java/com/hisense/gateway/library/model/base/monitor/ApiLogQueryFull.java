package com.hisense.gateway.library.model.base.monitor;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.base.TimeQuery;
import lombok.Data;

import java.util.List;
import java.util.Map;

/**
 * @author guilai.ming 2020/09/22
 * <p>
 * Api调用记录全量查询, 不指定API\或订阅系统
 */
@Data
public class ApiLogQueryFull {
    private Integer pageNum;
    private Integer pageSize;
    private String apiName;
    private List<String> sort;
    private TimeQuery timeQuery;
    private Integer statType;
    private String requestBody;
    private String requestHeader;
    private String responseBody;

    @JsonIgnore
    private String tenantId;

    @JsonIgnore
    private String projectId;

    @JsonIgnore
    private String environment;

    @JsonIgnore
    private Map<Integer, ApiInfo> apiInfos;

    @JsonIgnore
    private List<String> indexList;

    public StatType getStatType() {
        return StatType.from(this.statType);
    }

    @Data
    public static class ApiInfo {
        private Integer apiId;
        private Long serviceId;
        private String apiName;
        private String systemName;

        public ApiInfo(Integer apiId, Long serviceId, String apiName, String systemName) {
            this.apiId = apiId;
            this.serviceId = serviceId;
            this.apiName = apiName;
            this.systemName = systemName;
        }
    }

    public enum StatType {
        STAT_ALL(0),
        STAT_OK(1),
        STAT_FAIL(2);

        private final Integer code;

        StatType(Integer code) {
            this.code = code;
        }

        public Integer getCode() {
            return code;
        }

        public static StatType from(int code) {
            if (STAT_ALL.getCode().equals(code)) {
                return STAT_ALL;
            } else if (STAT_FAIL.getCode().equals(code)) {
                return STAT_FAIL;
            } else {
                return STAT_OK;
            }
        }
    }
}
