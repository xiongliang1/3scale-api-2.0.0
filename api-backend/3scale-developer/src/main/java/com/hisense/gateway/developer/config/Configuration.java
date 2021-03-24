/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.developer.config;

import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.ssl.SSLContexts;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

import com.hisense.gateway.developer.web.response.DefaultErrorHandlerWrapper;

import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.MultipartConfigElement;
import java.io.File;

/**
 * Configuration
 *
 * @Date 2019-11-20 18:37
 * @Author wangjinshan
 * @Version v1.0
 */
@EntityScan(basePackages = {
        "com.hisense.gateway",
})
@EnableJpaRepositories(basePackages = {
        "com.hisense.gateway"
})
@org.springframework.context.annotation.Configuration
//@EnableSwagger2
@ComponentScan({"com.hisense.gateway"})
@EnableConfigurationProperties(Properties.class)
public class Configuration {

    private final DefaultErrorHandlerWrapper wrapper;

    @Autowired
    public Configuration(DefaultErrorHandlerWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Bean(name = "restTemplate")
    public RestTemplate restTemplate() throws Exception {
        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(HttpClients.custom().setSSLSocketFactory(
                new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null,
                        (x509Certificates, s) -> true).build(), new NoopHostnameVerifier())).build());
        RestTemplate restTemplate = new RestTemplate(requestFactory);
        restTemplate.setErrorHandler(wrapper);
        return restTemplate;
    }

    /**
     *@multipartConfigElement 文件上传临时路径
     *@return javax.servlet.MultipartConfigElement
     *@Author wanjinshan
     *@Date 2019/11/20 18:40
     *@version 1.0
     */
    @Bean
    MultipartConfigElement multipartConfigElement() {
        MultipartConfigFactory factory = new MultipartConfigFactory();
        String location = "/tmp";
        File tmpFile = new File(location);
        if (!tmpFile.exists()) {
            tmpFile.mkdirs();
        }
        factory.setLocation(location);
        return factory.createMultipartConfig();
    }

    /**
     *@createRestApi integrate with swagger2
     *@return springfox.documentation.spring.web.plugins.Docket
     *@Author wanjinshan
     *@Date 2019/11/20 18:40
     *@Version 1.0
     */
    //@Bean
    public Docket createRestApi() {
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .select()
                //为当前包路径
                .apis(RequestHandlerSelectors.basePackage("com.tenxcloud.gateway.developer"))
                .paths(PathSelectors.any())
                .build();
    }

    /**
     *@apiInfo 构建 api文档的详细信息函数,注意这里的注解引用的是哪个
     *@return springfox.documentation.service.ApiInfo
     *@Author wanjinshan
     *@Date 2019/11/20 18:40
     *@Version 1.0
     */
    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                //页面标题
                .title("3scale-Rest-Api")
                //创建人
                .contact(new Contact("wangjinshan", "http://txgw.tenxcloud.com", "jinshan.wang@tenxcloud.com"))
                //版本号
                .version("1.0")
                //描述
                .description("Rest Apis For 3scale")
                .build();
    }
}
