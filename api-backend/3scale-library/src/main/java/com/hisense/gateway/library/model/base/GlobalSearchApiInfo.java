package com.hisense.gateway.library.model.base;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

/**
 * API全局查询信息
 * <p>
 * 2020/10/20 guilai.ming
 */
@Data
public class GlobalSearchApiInfo {
    Integer apiId;
    String apiName;
    String projectId;
    String systemName;
    @JsonIgnore
    Integer systemId;

    public GlobalSearchApiInfo(Integer apiId, String apiName, Integer systemId, String projectId) {
        this.apiId = apiId;
        this.apiName = apiName;
        this.projectId = projectId;
        this.systemId = systemId;
    }
}
