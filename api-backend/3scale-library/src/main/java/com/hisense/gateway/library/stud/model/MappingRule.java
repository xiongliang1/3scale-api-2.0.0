package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MappingRule {
    private String id;
    private String metricId;
    private String pattern;
    private String httpMethod;
    private String delta;
    private String last;
    private String createdAt;
    private String updatedAt;
}
