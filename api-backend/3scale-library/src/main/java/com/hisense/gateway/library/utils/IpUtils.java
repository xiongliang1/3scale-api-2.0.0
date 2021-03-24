/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.library.utils;

import com.hisense.gateway.library.model.Result;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.regex.Pattern;

public class IpUtils {
    public static String getRealIP(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip != null && ip.length() != 0 && !"unknown".equalsIgnoreCase(ip)) {
            // 多次反向代理后会有多个ip值，第一个ip才是真实ip
            if( ip.indexOf(",")!=-1 ){
                ip = ip.split(",")[0];
            }
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }

    public static boolean ipValidationCheck(String address) {
        String pattern = "([0,1]?\\d?\\d|2[0-4]\\d|25[0-5])\\.){3}([0,1]?\\d?\\d|2[0-4]\\d|25[0-5]";
        return Pattern.compile("("+pattern+")")
                .matcher(address).matches() ||
                Pattern.compile("("+pattern+")-("+pattern+")")
                .matcher(address).matches() ;
    }

    public static Result<String> ipValidationCheck(List<String> addresses) {
        Result<String> result = new Result<>(Result.OK,"IP校验成功！",null);
        StringBuilder invalidIps = new StringBuilder();
        for (String address : addresses) {
            address=address.replaceAll(" ","");
            if (!ipValidationCheck(address)) {
                invalidIps.append(address);
            }
        }
        if(invalidIps.length()>0){
            result.setError(String.format("invalid Ips=%s",invalidIps.toString()));
        }
        return result;
    }
}
