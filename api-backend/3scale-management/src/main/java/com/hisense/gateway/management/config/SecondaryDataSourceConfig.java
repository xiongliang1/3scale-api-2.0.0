package com.hisense.gateway.management.config;/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/9
 */

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
public class SecondaryDataSourceConfig {

    @Autowired
    SecondaryProperties secondaryProperties;

    @Bean(name = "secondaryDataSource")
    public DataSource secondaryDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(secondaryProperties.getUrl());
        dataSource.setUsername(secondaryProperties.getUsername());
        dataSource.setPassword(secondaryProperties.getPassword());
        dataSource.setDriverClassName(secondaryProperties.getDriverClassName());
        return dataSource;
    }
}
