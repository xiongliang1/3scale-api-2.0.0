/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/26 @author peiyun
 */
package com.hisense.gateway.developer.web.interceptor;

import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.model.dto.web.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
public class HeaderModifierAdvice implements ResponseBodyAdvice<Object> {

    private static final Logger LOG = LoggerFactory.getLogger(HeaderModifierAdvice.class);

    @ResponseBody
    @ExceptionHandler(value = NumberFormatException.class)
    public ApiResponse exceptionHandler(NumberFormatException numberFormatEx) {
        LOG.error("error message: {}", numberFormatEx);
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setMessage("pageNum or pageSize is null");
        return apiResponse;
    }

    @ResponseBody
    @ExceptionHandler(value = NotExist.class)
    public ApiResponse exceptionHandler(NotExist notExist) {
        ApiResponse apiResponse = new ApiResponse();
        apiResponse.setCode(0);
        apiResponse.setMessage(notExist.getMessage());
        return apiResponse;
    }

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
                                  Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {
        ServletServerHttpRequest ssReq = (ServletServerHttpRequest)request;
        ServletServerHttpResponse ssResp = (ServletServerHttpResponse)response;
        if(ssReq == null || ssResp == null
                || ssReq.getServletRequest() == null
                || ssResp.getServletResponse() == null) {
            return body;
        }

        // 对于未添加跨域消息头的响应进行处理
        HttpServletRequest req = ssReq.getServletRequest();
        HttpServletResponse resp = ssResp.getServletResponse();
        String originHeader = "Access-Control-Allow-Origin";
        if(!resp.containsHeader(originHeader)) {
            String origin = req.getHeader("Origin");
            if(origin == null) {
                String referer = req.getHeader("Referer");
                if(referer != null) {
                    origin = referer.substring(0, referer.indexOf("/", 7));
                }
            }
            resp.setHeader("Access-Control-Allow-Origin", origin);
        }

        String credentialHeader = "Access-Control-Allow-Credentials";
        if(!resp.containsHeader(credentialHeader)) {
            resp.setHeader(credentialHeader, "true");
        }
        resp.setHeader("userName", UserNameHolder.get());
        UserNameHolder.release();
        return body;
    }
}
