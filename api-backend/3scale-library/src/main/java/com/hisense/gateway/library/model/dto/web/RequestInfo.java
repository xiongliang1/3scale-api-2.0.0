/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/11 @author peiyun
 */
package com.hisense.gateway.library.model.dto.web;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class RequestInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    String path;
    Map<String, Object> parameterMap = new HashMap<>();
    String parameterStr;
    /**
     * 参数类型,json,form
     */
    String parameterContentType;
    /**
     * 请求类型Get,post...
     */
    String type;
    /**
     * 鉴权类型,auth和noauth
     */
    String authType;
    /**
     * 秘钥方式:1,user_key, 2.app_id&app_key
     */
    Integer backendVersion;

    String appId;

    String appKey;

    String userKey;

    List<Map<String, String>> header;
}
