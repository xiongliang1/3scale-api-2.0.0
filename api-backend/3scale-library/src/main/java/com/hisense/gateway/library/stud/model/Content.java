package com.hisense.gateway.library.stud.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.Date;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Content {
    private String id;
    private String accountId;
    private String name;
    private String onelineDescription;
    private String description;
    private String txt_api;
    private String txtSupport;
    private String txtFeatures;
    private Date createdAt;
    private Date updatedAt;
    private String logoFileName;
    private String logoContentType;
    private String logoFileSize;
    private String state;
    private String intentionsRequired;
    private String draftName;
    private String infobar;
    private String terms;
    private String displayProviderKeys;
    private String creditCardSupportEmail;
    private String buyersManageApps;
    private String buyersManageKeys;
    private String customKeysEnabled;
    private String buyerPlanChangePermission;
    private String buyerCanSelectPlan;
    private String notificationSettings;
    private String defaultApplicationPlanId;
    private String defaultServicePlanId;
    private String defaultEndUserPlanId;
    private String endUserRegistrationRequired;
    private String tenantId;
    private String systemName;
    private String backendVersion;
    private String mandatoryAppKey;
    private String buyerKeyRegenerateEnabled;
    private String supportEmail;
    private String referrerFiltersRequired;
    private String deploymentOption;
    private String kubernetesServiceLink;
    private String techSupportEmail;
    private String adminSupportEmail;
    private String proxiable;
    private String backendAuthenticationType;
    private String backendAuthenticationValue;
    private Proxy proxy;
}
