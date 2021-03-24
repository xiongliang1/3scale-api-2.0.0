/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.developer.web.controller;

import com.hisense.gateway.developer.constant.Audience;
import com.hisense.gateway.developer.service.LdapHisenseService;
import com.hisense.gateway.developer.utils.JwtTokenUtil;
import com.hisense.gateway.library.beans.CommonBeanUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.UserInfo;
import com.hisense.gateway.library.utils.IpUtils;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

@Slf4j
@RequestMapping("/api/v1/login/")
@RestController
public class LoginController {

    @Resource
    LdapHisenseService ldapHisenseService;
    @Value("${sso.accessToken.url}")
    private String accessTokenUrl;
    @Value("${sso.client.id}")
    private String clientId;
    @Value("${sso.client.secret}")
    private String clientSecret;
    @Value("${sso.login.url}")
    private String ssoUrl;
    @Value("${sso.redirect.url}")
    private String redirectUrl;
    @Value("${sso.target.url}")
    private String targetUrl;
    @Autowired
    private Audience audience;


    @ResponseBody
    @RequestMapping(value = "/loginNew", method = RequestMethod.POST)
    public Result<Map<String,String>> loginNew(@RequestBody UserInfo userInfo, HttpServletRequest request, HttpServletResponse response) throws IOException {
        log.info("=======start login========");
        StringBuffer urlBuffer = new StringBuffer(accessTokenUrl);
        urlBuffer.append("?code=")
                .append(userInfo.getCode())
                .append("&client_id=")
                .append(clientId)
                .append("&client_secret=")
                .append(clientSecret)
                .append("&redirect_uri=")
                .append(redirectUrl);
        log.info("accessTokenUrl is :"+urlBuffer.toString());
        userInfo.setUrl(urlBuffer.toString());
        Result<Map<String,String>> result = ldapHisenseService.loginNew( IpUtils.getRealIP(request), userInfo);
        if ("1".equals(result.getCode())) {
            response.setHeader("userName", result.getData().get("userName"));
            // 将token放在响应头
            response.setHeader(JwtTokenUtil.AUTH_HEADER_KEY, JwtTokenUtil.TOKEN_PREFIX + result.getData().get("token"));
        }
        return result;
    }


    /**
     * 访问sso登录
     *
     * @param request
     * @param response
     * @throws IOException
     */
    @GetMapping("ssologin")
    public void ssologin(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = ssoUrl + "?client_id=" + clientId + "&redirect_uri=" + redirectUrl + "&response_type=code&target_uri=" + targetUrl;
        response.sendRedirect(url);
    }

    @GetMapping("getUserName")
    public String  getUserName(HttpServletRequest request) throws IOException {
        String loginUser = CommonBeanUtil.getLoginUserName();
        log.info("loginUser1:"+loginUser);
        String authorization = request.getHeader("Authorization");
        audience.setExpiresSecond(2*60*60*1000);
        String[] parts = authorization.split(" ");
        Claims chaims = JwtTokenUtil.parseJWT(parts[1], audience.getBase64Secret(),audience.getExpiresSecond());
        String uid = String.valueOf(chaims.get("uid"));
        log.info("loginUser2:"+uid);
        return String.format("loginUser1:%s <==> loginUser2:%s",loginUser,uid);
    }

}
