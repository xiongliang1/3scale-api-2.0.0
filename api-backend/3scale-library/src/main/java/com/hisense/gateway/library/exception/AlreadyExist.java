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
 * AccessDeny  自定义异常类
 *
 * @author wangmingyue
 * @date 2018-07-27 12:15:00
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.CONFLICT)
public class AlreadyExist extends RuntimeException {
    /**
     * AlreadyExist 无惨构造方法
     *
     * @date: 2018-07-27 12:16:00
     * @author: weiwei
     */
    public AlreadyExist() {
        super();
    }

    /**
     * AlreadyExist 构造方法
     *
     * @param message 异常信息
     * @date: 2018-07-27 12:17:00
     * @author: weiwei
     */
    public AlreadyExist(String message) {
        super(message);
    }
}
