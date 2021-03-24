/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.developer.service;

import java.util.Map;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.UserInfo;

public interface LdapHisenseService {

    /**
     * SSO获取token
     * @return
     */
    public String getTokenId();

    /**
     * SSO登录校验
     * @param tokenId
     * @param clientIp
     * @param userName
     * @param password
     * @return
     */
    public Result<Map<String,String>> checkLogin(String domain, String tokenId, String clientIp, String userName, String password);

    /**
     * ldap获取token
     * @return
     */
    public String getLdapQueryToken();

    /**
     * ldap查询用户参数
     * @return
     */
    public Map<String,String> ldapQueryInfoParams(String tokenId, String uid);

    /**
     * 登录后校验值
     * @param tokenId
     * @param ssoLoginToken
     * @return
     */
    public Map<String,String> checkSsoLoginToken(String tokenId, String ssoLoginToken);


    String checkLoginTest(String tokenId, String realIP, String username, String password);

    Result<Map<String, String>> loginNew( String realIP, UserInfo userInfo);

    Map<String, String> checkAccessToken(String accessToken);

    Result<String> getAccessToken(String url);
}
