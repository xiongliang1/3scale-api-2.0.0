package com.hisense.gateway.developer.config;/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/9
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.datasource.secondary")
@Component
@Data
public class SecondaryProperties {
    private String url;

    private String username;

    private String password;

    private String driverClassName;
}
