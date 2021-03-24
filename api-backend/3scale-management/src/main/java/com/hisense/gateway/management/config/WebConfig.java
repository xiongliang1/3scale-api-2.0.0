//package com.hisense.gateway.management.config;
//
//import com.google.common.collect.ImmutableMap;
//import com.hisense.gateway.library.repository.InstanceRepository;
//import com.hisense.gateway.library.repository.UserInstanceRelationshipRepository;
//import com.hisense.gateway.management.web.interceptor.BearerTokenValidator;
//import com.thetransactioncompany.cors.CORSFilter;
//import org.springframework.boot.web.servlet.FilterRegistrationBean;
//import org.springframework.boot.web.servlet.ServletListenerRegistrationBean;
//import org.springframework.boot.web.servlet.ServletRegistrationBean;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.ComponentScan;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.context.annotation.FilterType;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.ControllerAdvice;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.context.request.RequestContextListener;
//import org.springframework.web.filter.CharacterEncodingFilter;
//import org.springframework.web.method.support.HandlerMethodArgumentResolver;
//import org.springframework.web.multipart.MultipartResolver;
//import org.springframework.web.multipart.support.StandardServletMultipartResolver;
//import org.springframework.web.servlet.DispatcherServlet;
//import org.springframework.web.servlet.config.annotation.*;
//import org.springframework.web.servlet.mvc.WebContentInterceptor;
//import org.springframework.web.servlet.view.InternalResourceViewResolver;
//import springfox.documentation.builders.ApiInfoBuilder;
//import springfox.documentation.builders.PathSelectors;
//import springfox.documentation.builders.RequestHandlerSelectors;
//import springfox.documentation.service.ApiInfo;
//import springfox.documentation.spi.DocumentationType;
//import springfox.documentation.spring.web.plugins.Docket;
//import springfox.documentation.swagger2.annotations.EnableSwagger2;
//
//import javax.servlet.DispatcherType;
//import javax.servlet.MultipartConfigElement;
//import java.util.Arrays;
//import java.util.EnumSet;
//import java.util.List;
//
//@Configuration
//@EnableSwagger2
//@EnableWebMvc
//@ComponentScan(basePackages = {"com.hisense.gateway"},
//        useDefaultFilters = false,
//        includeFilters = {
//                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = RestController.class),
//                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = Controller.class),
//                @ComponentScan.Filter(type = FilterType.ANNOTATION, value = ControllerAdvice.class)
//        }
//)
//public class WebConfig implements WebMvcConfigurer {
//    private final Properties properties;
//    private final UserInstanceRelationshipRepository userInstanceRelationshipRepository;
//    private final InstanceRepository instanceRepository;
//
//    public WebConfig(Properties properties,
//                     UserInstanceRelationshipRepository userInstanceRelationshipRepository,
//                     InstanceRepository instanceRepository) {
//        this.properties = properties;
//        this.userInstanceRelationshipRepository = userInstanceRelationshipRepository;
//        this.instanceRepository = instanceRepository;
//    }
//
//    @Bean
//    public InternalResourceViewResolver internalResourceViewResolver() {
//        InternalResourceViewResolver viewResolver = new InternalResourceViewResolver();
//        viewResolver.setPrefix("/");
//        viewResolver.setSuffix(".html");
//        return viewResolver;
//    }
//
//    @Override
//    public void addResourceHandlers(ResourceHandlerRegistry registry) {
//        /*registry.addResourceHandler("swagger-ui.html")
//                .addResourceLocations("classpath:/META-INF/resources/");*/
//        registry.addResourceHandler("/webjars/**")
//                .addResourceLocations("classpath:/META-INF/resources/webjars/");
//    }
//
//    @Override
//    public void addViewControllers(ViewControllerRegistry registry) {
//        registry.addViewController("/").setViewName("index");
//    }
//
//    @Override
//    public void addInterceptors(InterceptorRegistry registry) {
//        WebContentInterceptor webContentInterceptor = new WebContentInterceptor();
//        webContentInterceptor.setCacheSeconds(0);
//        registry.addInterceptor(webContentInterceptor);
//        registry.addInterceptor(new BearerTokenValidator(properties, userInstanceRelationshipRepository,
//                instanceRepository)).addPathPatterns("/api/v1/**");
//    }
//
//    @Override
//    public void configureDefaultServletHandling(DefaultServletHandlerConfigurer configurer) {
//        configurer.enable();
//    }
//
//    @Bean
//    public MultipartResolver multipartResolver() {
//        return new StandardServletMultipartResolver();
//    }
//
//    @Bean
//    public FilterRegistrationBean CORSFilterRegistration() {
//        CORSFilter filter = new CORSFilter();
//        FilterRegistrationBean registrationBean = new FilterRegistrationBean();
//        registrationBean.setFilter(filter);
//        ImmutableMap<String, String> initParameters = ImmutableMap.<String, String>builder()
//                .put("cors.supportedMethods", "GET,POST,PUT,DELETE")
//                .build();
//
//        registrationBean.setInitParameters(initParameters);
//        registrationBean.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST,
//                DispatcherType.FORWARD,
//                DispatcherType.INCLUDE));
//        registrationBean.setMatchAfter(false);
//        registrationBean.setUrlPatterns(Arrays.asList("/*"));
//        return registrationBean;
//    }
//
//    @Bean
//    public FilterRegistrationBean characterEncodingFilterRegistration() {
//        CharacterEncodingFilter filter = new CharacterEncodingFilter();
//        filter.setEncoding("UTF-8");
//        filter.setForceEncoding(true);
//        FilterRegistrationBean registration = new FilterRegistrationBean();
//        registration.setFilter(filter);
//        registration.setDispatcherTypes(EnumSet.of(DispatcherType.REQUEST,
//                DispatcherType.FORWARD,
//                DispatcherType.INCLUDE));
//        registration.setMatchAfter(false);
//        registration.setUrlPatterns(Arrays.asList("/*"));
//        return registration;
//    }
//
//    @Bean
//    public ServletRegistrationBean dispatcherRegistration(DispatcherServlet dispatcherServlet) {
//        ServletRegistrationBean registration = new ServletRegistrationBean(dispatcherServlet);
//        MultipartConfigElement multipartConfig = new MultipartConfigElement(null, 1024 * 1024 * 200,
//                1024 * 1024 * 5 * 100,
//                1024 * 1024 * 4);
//        registration.setLoadOnStartup(1);
//        registration.setUrlMappings(Arrays.asList("/*"));
//        registration.setMultipartConfig(multipartConfig);
//        return registration;
//    }
//
//    @Bean
//    public ServletListenerRegistrationBean<RequestContextListener> servletListenerRegistrationBean() {
//        ServletListenerRegistrationBean<RequestContextListener> registration = new ServletListenerRegistrationBean<>();
//        registration.setListener(new RequestContextListener());
//        return registration;
//    }
//
//    @Override
//    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
//        // resolvers.add(new LoginInfoResolver());
//        // resolvers.add(new NamespaceResolver());
//    }
//
//    @Bean
//    public Docket createRestApi() {
//        ApiInfo info = new ApiInfoBuilder()
//                .title("3scale RESTful APIS")
//                .description("3scale swagger API for micro service")
//                .version("1.0")
//                .build();
//
//        return new Docket(DocumentationType.SWAGGER_2)
//                .apiInfo(info)
//                .select()
//                .apis(RequestHandlerSelectors.basePackage("com.hisense.gateway"))
//                .paths(PathSelectors.any())
//                .build();
//    }
//}
