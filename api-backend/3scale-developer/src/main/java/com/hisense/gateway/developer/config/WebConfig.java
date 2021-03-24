/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.developer.config;

import com.google.common.collect.ImmutableMap;
import com.hisense.gateway.developer.service.UserService;
import com.hisense.gateway.developer.service.LdapHisenseService;
import com.hisense.gateway.developer.web.interceptor.SsoCookieValidator;
import com.thetransactioncompany.cors.CORSFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextListener;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.mvc.WebContentInterceptor;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import javax.servlet.DispatcherType;
import javax.servlet.MultipartConfigElement;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * WebConfig
 * @Date 2019-11-20 18:37
 * @Author wangjinshan
 * @version v1.0
 */
@Configuration
@EnableWebMvc
@EnableSwagger2
@ComponentScan(
        basePackages = {"com.hisense.gateway.developer"},
        useDefaultFilters = false,
        includeFilters = {
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = RestController.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class),
                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ControllerAdvice.class)
        })
public class WebConfig implements WebMvcConfigurer {

    /**
     * properties
     */
    private final Properties properties;
    /**
     * restTemplate
     */
    private final RestTemplate restTemplate;

    /**
     * ldapHisenseService
     */
    private final LdapHisenseService ldapHisenseService;
    /**
     * userService
     */
    private final UserService userService;

    public WebConfig(Properties properties,
                     RestTemplate restTemplate,
                     LdapHisenseService ldapHisenseService,
                     UserService userService) {
        this.properties = properties;
        this.restTemplate = restTemplate;
        this.ldapHisenseService = ldapHisenseService;
        this.userService = userService;

    }

    @Bean
    public InternalResourceViewResolver internalResourceViewResolver() {
        InternalResourceViewResolver internalResourceViewResolver = new InternalResourceViewResolver();
        internalResourceViewResolver.setPrefix("/");
        internalResourceViewResolver.setSuffix(".html");
        return internalResourceViewResolver;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        //registry.addResourceHandler("swagger-ui.html").addResourceLocations("classpath:/META-INF/resources/");
        registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
    }

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        registry.addViewController("/").setViewName("index");
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
        webContentInterceptor.setCacheSeconds(0);
        registry.addInterceptor(webContentInterceptor);
        registry.addInterceptor(new SsoCookieValidator(properties, restTemplate,ldapHisenseService,userService))
                .addPathPatterns("/api/v1/**").excludePathPatterns("/api/v1/login/login",
                "/api/v1/login/loginNew","/api/v1/login/ssologin","/api/v1/*/publishApi/downloadApiDocFile/**","/default/**",
                "/api/v1/*/applications/subscribe","/api/v1/*/dataItems/getAllSystems");
    }

    @Override
    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
        configurer.enable();
    }

    @Bean
    public MultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }


    @Bean
    public FilterRegistrationBean CORSFilterRegistration() {
        CORSFilter filter = new CORSFilter();
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        ImmutableMap<String, String> initParameters = ImmutableMap.<String, String>builder()
                .put("cors.supportedMethods", "GET,POST,PUT,DELETE")
                .build();
        registration.setInitParameters(initParameters);
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE));
        registration.setMatchAfter(false);
        registration.setUrlPatterns(Arrays.asList("/*"));
        return registration;
    }


    @Bean
    public FilterRegistrationBean characterEncodingFilterRegistration() {
        CharacterEncodingFilter filter = new CharacterEncodingFilter();
        filter.setEncoding("UTF-8");
        filter.setForceEncoding(true);
        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(filter);
        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE));
        registration.setMatchAfter(false);
        registration.setUrlPatterns(Arrays.asList("/*"));
        return registration;
    }

    /**
     * @param dispatcherServlet
     * @return
     */
    @Bean
    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
        MultipartConfigElement multipartConfig = new MultipartConfigElement(null, (long) 1024 * 1024 * 100, (long) 1024 * 1024 * 5 * 100, 1024 * 1024 * 4);
        registration.setLoadOnStartup(1);
        registration.setUrlMappings(Arrays.asList("/*"));
        registration.setMultipartConfig(multipartConfig);
        return registration;
    }

    @Bean
    public ServletListenerRegistrationBean<RequestContextListener> servletListenerRegistrationBean() {
        ServletListenerRegistrationBean<RequestContextListener> servletListenerRegistrationBean = new ServletListenerRegistrationBean<>();
        servletListenerRegistrationBean.setListener(new RequestContextListener());
        return servletListenerRegistrationBean;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
//        argumentResolvers.add(new LoginInfoResolver());
//        argumentResolvers.add(new NamespaceResolver());
    }


    @Bean
    public Docket createRestApi() {
        ApiInfo info = new ApiInfoBuilder()
                .title("3scale RESTful APIs")
                .description("3scale Swagger RESTful APIs for Micro Service")
                .version("1.0")
                .build();
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(info)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.hisense.gateway"))
                .paths(PathSelectors.any())
                .build();
    }
}
