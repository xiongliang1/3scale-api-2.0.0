/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.developer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * ApplicationStarter
 *
 * @author wangjinshan
 * @version v1.0c
 * @date 2019-11-20 09:12
 */
@SpringBootApplication(scanBasePackages = {"com.hisense.gateway"})
@EnableScheduling
//@EnableEurekaClient
public class ApiDeveloperApp {
    public static void main(String[] args) {
        System.setProperty("java.security.egd","file：/// dev / urandom");
        SpringApplication.run(ApiDeveloperApp.class, args);
    }
}
