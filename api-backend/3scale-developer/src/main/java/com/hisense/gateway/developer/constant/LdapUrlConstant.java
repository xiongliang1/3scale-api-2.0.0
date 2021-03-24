/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.developer.constant;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LdapUrlConstant {


    /*海信ldap的请求地址*/
    @Value("${ldap.callApiTokenUrl}")
    public String callApiTokenUrl;

    @Value("${ldap.checkLoginUrl}")
    public String checkLoginUrl;

    @Value("${ldap.ldapQueryTokenUrl}")
    public String ldapQueryTokenUrl;

    @Value("${ldap.ldapQueryInfoParamsUrl}")
    public String ldapQueryInfoParamsUrl;

    @Value("${ldap.forwardBusinessSystemUrl}")
    public String forwardBusinessSystemUrl;

    @Value("${ldap.targetUri}")
    public String targetUri;

    @Value("${ldap.ldapAppSecretId}")
    public String ldapAppSecretId;

    @Value("${ldap.checkSsoLoginTokenUrl}")
    public String checkSsoLoginTokenUrl;

    @Value("${ldap.userInfoUri}")
    public String userInfoUri;
}
