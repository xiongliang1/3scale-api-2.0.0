package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

@Data
public class ApiMappingRuleInfo {

    private String httpMethod;
    private  String pattern;
}
