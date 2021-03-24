/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2017 TenxCloud. All Rights Reserved.
 *
 */

package com.hisense.gateway.developer.web.interceptor;

import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.exception.UnAuthorized;
import com.hisense.gateway.library.model.dto.web.FromHeader;
import com.hisense.gateway.library.model.dto.web.Namespace;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

/**
 * NamespaceResolver
 * @Date 2019/11/20 09:14
 * @Author wangjinshan
 * @Version v1.0
 */
public class NamespaceResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        FromHeader fromHeaderAnnotation = parameter.getParameterAnnotation(FromHeader.class);
        Class<?> type = parameter.getParameterType();
        return fromHeaderAnnotation != null && (type == Namespace.class || type == String.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object request = webRequest.getNativeRequest();
        if (request == null || !(request instanceof HttpServletRequest)) return null;
        HttpServletRequest hasAttributes = (HttpServletRequest) request;
        Namespace namespace = (Namespace) hasAttributes.getAttribute(GatewayConstants.NAMESPACE);
        if (namespace == null) {
            throw new UnAuthorized("No namespace found in the header");
        }
        Class<?> type = parameter.getParameterType();
        if (type == Namespace.class) {
            return namespace;
        } else if (type == String.class) {
            return namespace.getNamespace();
        }
        return null;
    }
}
