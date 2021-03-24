package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Service {
    private String id;
    private String accessToken;
    private String name;
    private String systemName;
    private String description;
    private String backendVersion;
    private String target;
    private String secTarget;
    private String accountId;
    private List<Metric> metric;
}
