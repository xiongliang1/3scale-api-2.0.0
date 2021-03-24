/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */

package com.hisense.gateway.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * BadRequest
 *
 * @Date 2019-01-17 14:59
 * @Author huhu
 * @Version v1.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequest extends RuntimeException {
    public BadRequest(String message, Exception exception) {
        super(message, exception);
    }

    public BadRequest(String message) {
        super(message);
    }

    public BadRequest() {
        super("Bad Request: please check your request body.");
    }
}
