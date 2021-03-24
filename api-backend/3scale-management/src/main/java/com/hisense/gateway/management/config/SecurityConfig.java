/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.management.config;

import org.springframework.context.annotation.Configuration;
/*@Configuration
@EnableResourceServer
public class SecurityConfig extends ResourceServerConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.csrf().ignoringAntMatchers("/user/info/**")
                .disable().httpBasic()
                .and()
                .authorizeRequests()
                .antMatchers("/user/info/**").permitAll()
                .antMatchers("/user/get/**").permitAll()
                .antMatchers("/user/find/**").permitAll()
//                .antMatchers("/monitor/online/**").permitAll()
                .antMatchers("/user/userNameLike/**").permitAll()
                .antMatchers("/group/findbyGroupName/**").permitAll()
                .antMatchers("/user/save").permitAll()
                .antMatchers("/role/checkRoleCode/**").permitAll()
                .antMatchers("/role/save").permitAll()
                .antMatchers("/operLog/save").permitAll()
                .antMatchers("/logininfor").permitAll()
                .antMatchers("/ruleGroups/save").permitAll()
                .antMatchers("/rules/getAll/**").permitAll();
    }
}*/
