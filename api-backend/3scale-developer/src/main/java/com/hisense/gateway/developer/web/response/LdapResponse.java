/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */

package com.hisense.gateway.developer.web.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LdapResponse {

    /**
     * 	返回码	1、“SUCCESS”成功； 2、“FAILURE”错误；
     */
    private String resCode;

    /**
     * 操作结果信息	接口返回信息
     */
    private String resMess;

    /**
     * 	Token信息id	IAM系统校验 appSecretId 及appSecretKey信息通过后返回token信息
     */
    private String tokenId;

    /**
     * Token信息有效期	每个tokenId默认有效时长为2个小时
     */
    private String effecTime;

    /**
     * 用户名
     */
    private String uid;

    /**
     * 认证完成后跳转uri
     */
    private String targetUri;

    /**
     * 身份票据	该身份票据会在checkLogin接口中生成并返回，与用户登录帐号绑定。仅有效一次。
     */
    private String ticketId;

    /**
     * mail
     */
    private String email;

    /**
     * mobile
     */
    private String mobile;
}
