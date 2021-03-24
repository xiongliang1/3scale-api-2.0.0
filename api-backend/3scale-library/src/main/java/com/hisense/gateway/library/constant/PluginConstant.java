/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.constant;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.hisense.gateway.library.model.pojo.buz.PluginFunctionality;

/**
 * @author wangjinshan
 * @date 2019-11-20
 */
public class PluginConstant {
    /*身份认证插件*/
    public static final String JWT = "jwt";
    public static final String OAUTH2 = "oauth2";
    public static final String KEY_AUTH = "key-auth";
    public static final String BASIC_AUTH = "basic-auth";
    public static final String AUTH_TYPE_NONE = "none";

    /*流量控制插件*/
    public static final String RATE_LIMITING = "rate-limiting"; //速率限制
    public static final String REQUEST_SIZE_LIMITING = "request-size-limiting"; // 大小限制
    public static final String REQUEST_TERMINATION = "request-termination"; // 请求终止

    /*权限安装插件*/
    public static final String ACL = "acl"; // ACL 鉴权
    public static final String IP_RESTRICTION = "ip-restriction"; // IP限制

    /*分析监控插件*/
    public static final String ZIPKIN = "zipkin";
    public static final String PROMETHEUS = "prometheus";

    /*日志插件*/

    /**
     * default created auth type.
     */
    protected static final List<String> DefaultCreatedAuthType = Arrays.asList(PluginConstant.BASIC_AUTH, PluginConstant.JWT,
            PluginConstant.OAUTH2);

    public static boolean hasAuthType(String type) {
        if (StringUtils.isBlank(type)) {
            return false;
        }
        return DefaultCreatedAuthType.contains(type);
    }

    public static PluginFunctionality getFunctionality(String authType) {
        switch (authType) {
            case OAUTH2:
                return PluginFunctionality.authentication;
            default:
                return null;
        }
    }
}
