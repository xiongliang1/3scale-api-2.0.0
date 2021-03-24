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
 * NotExist  404 异常处理
 *
 * @author wangjinshan
 * @date 2020-03-18 13:29:00
 */
@SuppressWarnings("serial")
@ResponseStatus(code = HttpStatus.NOT_FOUND)
public class ListEmptyExist extends RuntimeException {

    /**
     * NotExist 构造方法
     *
     * @date 2018-07-27 13:29:00
     * @author weiwei
     */
    public ListEmptyExist() {
        super();
    }

    /**
     * NotExist 构造方法
     *
     * @param message 异常信息
     * @date 2018-07-27 13:29:00
     * @author weiwei
     */
    public ListEmptyExist(String message) {
        super(message);
    }
}