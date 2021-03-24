package com.hisense.gateway.management.config;/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2020 TenxCloud. All Rights Reserved.
 *
 * @auther douyanwei
 * @date 2020/3/9
 */

import com.alibaba.druid.pool.DruidDataSource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.sql.DataSource;
@Configuration
@Slf4j
public class PrimaryDataSourceConfig {

//    @Bean
//    @ConfigurationProperties("spring.datasource.durid.primary")
//    public DataSource primaryDataSource() {
//        return DataSourceBuilder.create().build();
//    }

    @Autowired
    PrimaryProperties primaryProperties;

    @Primary
    @Bean(name = "primaryDataSource")
    public DataSource secondaryDataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(primaryProperties.getUrl());
        dataSource.setUsername(primaryProperties.getUsername());
        dataSource.setPassword(primaryProperties.getPassword());
        dataSource.setDriverClassName(primaryProperties.getDriverClassName());
        return dataSource;
    }

}
