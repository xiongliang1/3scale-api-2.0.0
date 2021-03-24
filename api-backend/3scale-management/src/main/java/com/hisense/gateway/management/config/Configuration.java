/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.management.config;

import com.hisense.gateway.management.web.response.DefaultErrorHandlerWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.MultipartConfigFactory;
import org.springframework.context.annotation.Bean;

import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import javax.servlet.MultipartConfigElement;
import java.io.File;

/**
 * Configuration
 *
 * @Date 2019-11-20 18:37
 * @Author wangjinshan
 * @Version v1.0
 */
@org.springframework.context.annotation.Configuration
@EnableConfigurationProperties(Properties.class)
@EntityScan(basePackages = {
        "com.hisense.gateway",
})
@EnableJpaRepositories(basePackages = {
        "com.hisense.gateway"
})
public class Configuration {

    private final DefaultErrorHandlerWrapper wrapper;

    @Autowired
    public Configuration(DefaultErrorHandlerWrapper wrapper) {
        this.wrapper = wrapper;
    }

//    @Bean(name = "restTemplate")
//    public RestTemplate restTemplate() throws Exception {
//        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
//        requestFactory.setHttpClient(HttpClients.custom().setSSLSocketFactory(
//                new SSLConnectionSocketFactory(SSLContexts.custom().loadTrustMaterial(null,
//                        (x509Certificates, s) -> true).build(), new NoopHostnameVerifier())).build());
//        RestTemplate restTemplate = new RestTemplate(requestFactory);
////        restTemplate.getMessageConverters().add(new MappingJackson2XmlHttpMessageConverter());
//
////      //先获取到converter列表
////        List<HttpMessageConverter<?>> converters = restTemplate.getMessageConverters();
////        for(HttpMessageConverter<?> converter : converters){
////        //因为我们只想要jsonConverter支持对text/html的解析
////            if(converter instanceof MappingJackson2XmlHttpMessageConverter){
////                try{
////                //先将原先支持的MediaType列表拷出
////                    List<MediaType> mediaTypeList = new ArrayList<>(converter.getSupportedMediaTypes());
////                    //加入对text/html的支持
////                    mediaTypeList.add(MediaType.APPLICATION_XML);
////                   //将已经加入了text/html的MediaType支持列表设置为其支持的媒体类型列表
////                    ((MappingJackson2HttpMessageConverter) converter).setSupportedMediaTypes(mediaTypeList);
////                }catch(Exception e){
////                }
////            }
////        }
//
//        restTemplate.setErrorHandler(wrapper);
//        return restTemplate;
//    }

    /**
     * @return javax.servlet.MultipartConfigElement
     * @multipartConfigElement 文件上传临时路径
     * @Author wanjinshan
     * @Date 2019/11/20 18:40
     * @version 1.0
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
     * @return springfox.documentation.spring.web.plugins.Docket
     * @createRestApi integrate with swagger2
     * @Author wanjinshan
     * @Date 2019/11/20 18:40
     * @Version 1.0
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
     * @return springfox.documentation.service.ApiInfo
     * @apiInfo 构建 api文档的详细信息函数,注意这里的注解引用的是哪个
     * @Author wanjinshan
     * @Date 2019/11/20 18:40
     * @Version 1.0
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
