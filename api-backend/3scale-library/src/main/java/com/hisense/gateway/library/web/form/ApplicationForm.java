package com.hisense.gateway.library.web.form;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApplicationForm implements Serializable {
    private static final long serialVersionUID = 5105223900137785597L;

    private String id;
    private String accessToken;
    private String userAccountId;
    private String name;
    private String description;
    private String userKey;
    private String serviceId;
    private String serviceName;
    private String planId;
    private String planName;
    private String providerVerificationKey;
    private String state;
    private Integer page;
    private Integer perPage;
}
