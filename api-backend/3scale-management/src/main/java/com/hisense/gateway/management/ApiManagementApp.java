/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ApplicationStarter
 *
 * @author
 * @version v1.0
 * @date 2019-11-20 09:12
 */
@SpringBootApplication(scanBasePackages = {"com.hisense.gateway"},
        exclude = {DataSourceAutoConfiguration.class})
@EnableScheduling
@EnableDiscoveryClient
public class ApiManagementApp {
    public static void main(String[] args) {

        SpringApplication.run(ApiManagementApp.class, args);
    }
}
