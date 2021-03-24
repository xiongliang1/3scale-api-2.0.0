/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */

package com.hisense.gateway.library.exception;

import com.alibaba.fastjson.JSON;

import com.hisense.gateway.library.web.response.OriginalError;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.client.RestClientResponseException;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
public class HttpStatusForwarder {

    private static final Map<Integer, ExceptionFactory> factories;

    static {
        factories = new HashMap<>();
        factories.put(HttpStatus.BAD_REQUEST.value(), BadRequest::new);
        factories.put(HttpStatus.NOT_FOUND.value(), NotFound::new);
        factories.put(HttpStatus.CONFLICT.value(), Conflict::new);
        factories.put(HttpStatus.SERVICE_UNAVAILABLE.value(), ServiceUnavailable::new);
        factories.put(HttpStatus.UNAUTHORIZED.value(), UnAuthorized::new);
    }

    public RuntimeException forward(RestClientResponseException exception) {
        int statusCode = exception.getRawStatusCode();
        ExceptionFactory factory = factories.get(statusCode);
        if (factory == null) {
            OriginalError oe = null;
            try {
                // 获取原始错误信息
                oe = JSON.parseObject(exception.getResponseBodyAsString(), OriginalError.class);
            } catch (Exception e) {
                log.error(ExceptionUtils.getRootCauseMessage(e));
            }
            if (oe != null) {
                log.error("## originalError info: {}", oe);
                return new RuntimeException(oe.getMessage(), exception);
            }
        }

        return factory.create(exception.getMessage(), exception);
    }
}

interface ExceptionFactory {
    RuntimeException create(String message, Exception exception);
}

@ResponseStatus(HttpStatus.NOT_FOUND)
final class NotFound extends RuntimeException {

    NotFound(String message, Exception exception) {
        super(message, exception);
    }
}

@ResponseStatus(HttpStatus.CONFLICT)
final class Conflict extends RuntimeException {

    Conflict(String message, Exception exception) {
        super(message, exception);
    }
}

@ResponseStatus(HttpStatus.SERVICE_UNAVAILABLE)
final class ServiceUnavailable extends RuntimeException {

    ServiceUnavailable(String message, Exception exception) {
        super(message, exception);
    }
}
