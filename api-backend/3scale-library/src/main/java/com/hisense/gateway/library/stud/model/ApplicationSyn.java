/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * @author douyanwei
 * 2019/11/25
 */
package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApplicationSyn {
    private String id;
    private String accessToken;
    private String createdAt;
    private String state;
    private String accountId;
    private String serviceId;
    private String serviceName;
    private String userKey;
    private String applicationId;
    private Keys keys;
    private AppPlan plan;
    private String providerVerificationKey;
    private String name;
    private String description;
    private String planId;
}
