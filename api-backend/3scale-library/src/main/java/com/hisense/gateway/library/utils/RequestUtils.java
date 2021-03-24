/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.library.utils;

import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.model.dto.web.LoginInfo;
import com.hisense.gateway.library.model.dto.web.Namespace;

import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * RequestUtils
 *
 * @author wangjinshan
 * @version v1.0
 * @date 2019-11-20 16:21
 */
public class RequestUtils {

    public static final String IngressClassHeader = "tenx-ingress-class";
    public static final String DefaultIngressClass = "kong";

/*    public static String ingressClass() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        String ingressClass = request.getHeader(IngressClassHeader);
        if (StringUtils.isBlank(ingressClass)) {
            return DefaultIngressClass;
        }
        return ingressClass;
    }*/

    /**
     *登陆的访客信息
     * @return
     */
    public static LoginInfo visitor() {
        /**
         * 根据ServletRequestAttributes 获取前端请求方法名、参数、路径等信息
         * attr 接收到请求，记录请求内容
         *
         */

        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        //接收到request
        HttpServletRequest request = attr.getRequest();
        // getAttribute(String name) 该方法用于获取指定的属性值，如果指定的属性值不存在，则会返回一个 null。
        LoginInfo info = (LoginInfo)request.getAttribute(GatewayConstants.LOGIN_INFO);
        return info;
    }

    public static String namespaceValue() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        Namespace namespace = (Namespace) request.getAttribute(GatewayConstants.NAMESPACE);
        return namespace.getNamespace();
    }

    public static String namespaceType() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        Namespace namespace = (Namespace) request.getAttribute(GatewayConstants.NAMESPACE);
        return namespace.getType().toString().toLowerCase();
    }

    public static Namespace namespace() {
        ServletRequestAttributes attr = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attr.getRequest();
        Namespace namespace = (Namespace) request.getAttribute(GatewayConstants.NAMESPACE);
        return namespace;
    }

}
