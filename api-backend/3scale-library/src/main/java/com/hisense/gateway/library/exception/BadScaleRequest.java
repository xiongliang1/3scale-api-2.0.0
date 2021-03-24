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
 * BadScaleRequest
 *
 * @Date 2020-03-04 14:59
 * @Author wangjinshan
 * @Version v1.0
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadScaleRequest extends RuntimeException {
    public BadScaleRequest(String message, Exception exception) {
        super(message, exception);
    }

    public BadScaleRequest(String message) {
        super(message);
    }

    public BadScaleRequest() {
        super("Bad Request to 3scale: please check your request body.");
    }
}
