/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.model.dto.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;

@Data
public class ClusterInfo {
    @JsonProperty("apiProtocol")
    @Setter(AccessLevel.NONE)
    private String protocol;

    @JsonProperty("apiHost")
    @Setter(AccessLevel.NONE)
    private String host;

    @JsonProperty("apiToken")
    @Setter(AccessLevel.NONE)
    private String token;

    private String clusterName;
    private String clusterID;
    private String description;
    private String apiVersion;
}
