/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2018 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.library.utils;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

/**
 * 2019/11/20 @author wangjinshan
 */
@Component
public class SpringBeanUtil implements ApplicationContextAware {

    private static ApplicationContext applicationContext = null;

    /***
     * 根据一个bean的id获取配置文件中相应的bean
     */
    public static Object getBean(String beanId) throws BeansException {
        if (applicationContext.containsBean(beanId)) {
            applicationContext.getBean(beanId);
        }
        return null;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        if (SpringBeanUtil.applicationContext == null) {
            SpringBeanUtil.applicationContext = applicationContext;
        }
    }

    /***
     * 根据一个bean的类型获取配置文件中相应的bean
     */
    public static <T> T getBean(Class<T> requiredType) throws BeansException {
        return applicationContext.getBean(requiredType);
    }

    /**
     * 如果BeanFactory包含一个与所给名称匹配的bean定义，则返回true
     */
    public static boolean containsBean(String name) {
        return applicationContext.containsBean(name);
    }

    public static ApplicationContext getApplicationContext() {
        return SpringBeanUtil.applicationContext;
    }
}
