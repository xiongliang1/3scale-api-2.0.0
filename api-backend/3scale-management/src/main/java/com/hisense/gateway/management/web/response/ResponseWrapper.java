/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2017 TenxCloud. All Rights Reserved.
 */
package com.hisense.gateway.management.web.response;

import com.alibaba.fastjson.JSONPObject;
import com.google.gson.JsonObject;
import com.hisense.gateway.library.exception.ListEmptyExist;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.ApiResponse;
import com.hisense.gateway.library.model.dto.web.WrapResponseBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.ArrayList;
import java.util.Map;

@Slf4j
@ControllerAdvice
public class ResponseWrapper implements ResponseBodyAdvice<Object> {
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        WrapResponseBody wrapResponseBody = returnType.getParameterAnnotation(WrapResponseBody.class);
        return wrapResponseBody == null || !wrapResponseBody.skip();
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<?
            extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request,
                                  ServerHttpResponse response) {

        if (!(body instanceof Result)) {
            return body;
        }
        Result result = (Result) body;
        if (result.getData() != null && "true".equals(result.getData().toString())){
            result.setAlert(1);
            return result;
        }
        return body;
//        if (notSupport(request)) {
//            return body;
//        }

//        Integer statusCode = getStatusCode(response);
//        if (statusCode == null)
//            return body;
//
//        if (statusCode / 100 == 2) {
//            return success(body, statusCode);
//        } else {
//            return failure(body, statusCode);
//        }
    }

//    @ResponseBody
//    @ExceptionHandler(value = NotExist.class)
//    public Result exceptionHandler(NotExist notExist) {
//        Result result = new Result<>();
//        result.setCode(Result.OK);
//        result.setMsg(notExist.getMessage());
//        return result;
//    }
//
//    @ResponseBody
//    @ExceptionHandler(value = ListEmptyExist.class)
//    public Result listExceptionHandler(ListEmptyExist notExist) {
//        Result result = new Result<>();
//        result.setCode(Result.FAIL);
//        result.setMsg(notExist.getMessage());
//        result.setData(new ArrayList<>());
//        return result;
//    }

    /*private Object success(Object body, int statusCode) {
        ApiResponse response = new ApiResponse();
        response.setCode(statusCode);
        response.setStatus("success");
        if (body != null) {
            response.setData(body);
        }
        return response;
    }

    private Object failure(Object body, int statusCode) {
        if (!(body instanceof Map)) {
            return body;
        }

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
        if (!(response instanceof ServletServerHttpResponse)) {
            return null;
        }

        ServletServerHttpResponse response1 = (ServletServerHttpResponse) response;
        return response1.getServletResponse().getStatus();
    }

    private static boolean notSupport(ServerHttpRequest request) {
        return !request.getURI().getPath().startsWith("/api/v1");
    }*/
}