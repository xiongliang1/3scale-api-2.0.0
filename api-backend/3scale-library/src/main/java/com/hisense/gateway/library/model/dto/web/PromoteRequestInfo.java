/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/12/2 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class PromoteRequestInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    Integer id;
    /**
     * 是否兼容
     */
    String isCompatible;
    /**
     * 环境信息
     */
    private List<String> partitions;

    /**
     * 是否创建(true:创建,false:非创建)
     */
     private boolean create;
}