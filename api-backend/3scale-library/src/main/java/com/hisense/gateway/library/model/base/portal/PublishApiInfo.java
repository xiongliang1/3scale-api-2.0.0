package com.hisense.gateway.library.model.base.portal;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;

@Data
public class PublishApiInfo {
    private Integer apiId;
    private String systemName;
    private String apiName;
    private String description;

    public PublishApiInfo(Integer apiId, String systemName, String apiName, String description) {
        this.apiId = apiId;
        this.systemName = systemName;
        this.apiName = apiName;
        this.description = description;
    }
}
