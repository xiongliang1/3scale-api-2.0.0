/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2017 TenxCloud. All Rights Reserved.
 */

package com.hisense.gateway.developer.web.response;

import com.hisense.gateway.library.model.dto.web.ApiResponse;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.hisense.gateway.library.model.dto.web.WrapResponseBody;

import java.util.Map;

//@ControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        WrapResponseBody wrapResponseBody = returnType.getParameterAnnotation(WrapResponseBody.class);
        return wrapResponseBody == null || !wrapResponseBody.skip();
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        if (notSupport(request)) return body;
        Integer statusCode = getStatusCode(response);
        if (statusCode == null) return body;
        if (statusCode / 100 == 2) {
            return success(body, statusCode);
        } else {
            return failure(body, statusCode);
        }
    }

    private Object success(Object body, int statusCode) {
        ApiResponse response = new ApiResponse();
        response.setCode(statusCode);
        response.setStatus("success");
        if (body != null) {
            response.setData(body);
        }
        return response;
    }

    private Object failure(Object body, int statusCode) {
        if (!(body instanceof Map)) return body;
        Map struct = (Map) body;
        ApiResponse response = new ApiResponse();
        response.setCode(statusCode);
        response.setStatus("failure");
        response.setMessage((String) struct.get("message"));
        response.setReason((String) struct.get("error"));
        response.setData(null);
        return response;
    }

    private Integer getStatusCode(ServerHttpResponse response) {
        if (!(response instanceof ServletServerHttpResponse)) return null;
        ServletServerHttpResponse realResponse = (ServletServerHttpResponse) response;
        return realResponse.getServletResponse().getStatus();
    }

    private static boolean notSupport(ServerHttpRequest request) {
        return !request.getURI().getPath().startsWith("/api/v1");
    }
}