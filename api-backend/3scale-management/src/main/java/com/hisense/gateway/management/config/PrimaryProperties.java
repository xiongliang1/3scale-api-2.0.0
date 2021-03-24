package com.hisense.gateway.management.config;/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/9
 */

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "spring.datasource.primary")
@Component
@Data
public class PrimaryProperties {
    private String url;
    private String username;
    private String password;
    private String driverClassName;
}
