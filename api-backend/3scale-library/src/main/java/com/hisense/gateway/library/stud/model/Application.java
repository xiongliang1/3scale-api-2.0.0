package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Application {
    private String id;
    private String accessToken;
    private String createdAt;
    private String state;
    private String userAccountId;
    private String serviceId;
    private String serviceName;
    private String userKey;
    private String applicationId;
    private Keys keys;
    private AppPlan plan;
    private String providerVerificationKey;
    private String name;
    private String description;
	// portal
    private Integer system;
    private String systemName;
    private Integer status;

}
