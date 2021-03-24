/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */

package com.hisense.gateway.management.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import static com.hisense.gateway.management.config.Properties.PREFIX;

@Data
@ConfigurationProperties(PREFIX)
public class Properties {

    public static final String PREFIX = "api";

    private String paasApi = "http://192.168.2.29:8080";
    private String systemCallSignature = "8e059c94-f760-4f85-8910-f94c27cf0ff5";

    private String kongServer = "http://kong-server:8001";

}
