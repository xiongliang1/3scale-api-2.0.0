/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.developer.config;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.nio.client.HttpAsyncClientBuilder;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;


@Configuration
public class BeanConfig {

    @Value("${spring.data.elasticsearch.username}")
    private String userName;

    @Value("${spring.data.elasticsearch.password}")
    private String password;

    @Value("${api.config.elasticSearch.host}")
    private String httpHost;

    @Value("${api.config.elasticSearch.port}")
    private int httpPort;

    public RestTemplate restTemplate(){
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        return new RestTemplate(requestFactory);
    }



    @Bean
    public RestHighLevelClient client(){
        /*用户认证对象*/
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        考虑有账号密码de情况
        if(StringUtils.isNoneBlank(userName) && StringUtils.isNoneBlank(password)){
            /*设置账号密码*/
            credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(userName, password));
        }
        HttpHost[] esHosts = null;
        if(httpHost.indexOf(",")>-1){
            String[] hosts = httpHost.split(",");
            esHosts = new HttpHost[hosts.length];
            for (int i=0;i< hosts.length;i++){
                esHosts[i] = new HttpHost(hosts[i], httpPort);
            }
        }else{
            esHosts = new HttpHost[]{new HttpHost(httpHost, httpPort)};
        }
        /*创建rest client对象*/
        RestClientBuilder builder = RestClient.builder(esHosts)
                .setHttpClientConfigCallback(new RestClientBuilder.HttpClientConfigCallback() {
                    @Override
                    public HttpAsyncClientBuilder customizeHttpClient(HttpAsyncClientBuilder httpAsyncClientBuilder) {
                        return httpAsyncClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
                    }
                });
        RestHighLevelClient client = new RestHighLevelClient(builder);
        return client;
    }
}
