package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class PublishApiRuleInfo {

    private Integer id;
    private String name;
    private String systemName;
    private String environment;
    private List<ApiMappingRuleInfo> apiMappingRules;
}
