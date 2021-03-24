package com.hisense.gateway.management.web.interceptor;

import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.model.dto.web.FromToken;
import com.hisense.gateway.library.model.dto.web.LoginInfo;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;

public class LoginInfoResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        FromToken fromTokenAnnotation = parameter.getParameterAnnotation(FromToken.class);
        return fromTokenAnnotation != null && parameter.getParameterType() == LoginInfo.class;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        Object request = webRequest.getNativeRequest();
        if (request == null || !(request instanceof HttpServletRequest)) {
            return null;
        }

        HttpServletRequest hasAttributes = (HttpServletRequest) request;
        return hasAttributes.getAttribute(GatewayConstants.LOGIN_INFO);
    }
}