/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/12/2 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class DocRquestInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    String path;
    Map<String, Object> parameters = new HashMap<>();

    String parameterContentType;
    String type;
}
