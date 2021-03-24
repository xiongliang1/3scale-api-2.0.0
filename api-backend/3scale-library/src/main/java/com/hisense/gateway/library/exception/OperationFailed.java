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
 * OperationFailed
 *
 * @author huhu
 * @version v1.0
 * @date 2019-03-11 19:11
 */
@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class OperationFailed extends RuntimeException {

    public OperationFailed(String message) {
        super(message);
    }
}
