/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */

package com.hisense.gateway.library.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

//@ResponseStatus(HttpStatus.FORBIDDEN)
@ResponseStatus(HttpStatus.UNAUTHORIZED)
public class UnAuthorized extends RuntimeException {

    public UnAuthorized(String message) {
        super(message);
    }

    public UnAuthorized(String message, Exception exception) {
        super(message, exception);
    }
}
