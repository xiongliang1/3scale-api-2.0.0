/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.developer.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.developer.service.LdapHisenseService;
import com.hisense.gateway.developer.service.UserService;
import com.hisense.gateway.developer.web.response.LdapResponse;
import com.hisense.gateway.library.constant.LdapResConstant;
import com.hisense.gateway.developer.constant.Audience;
import com.hisense.gateway.developer.constant.LdapUrlConstant;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.portal.UserDto;
import com.hisense.gateway.library.model.dto.web.UserInfo;
import com.hisense.gateway.library.utils.AesUtils;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.developer.utils.JwtTokenUtil;
import com.hisense.gateway.library.utils.client.LdapQueryInfoNoImport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.cxf.jaxws.JaxWsProxyFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Service
public class LdapHisenseServiceImpl implements LdapHisenseService {

    @Resource
    LdapUrlConstant ldapUrlConstant;

    @Resource
    UserService userService;

    @Autowired
    private Audience audience;

    @Override
    public String getTokenId() {
        String callApiTokenUrl = ldapUrlConstant.callApiTokenUrl;
        try {
            log.info("callApiTokenUrl:"+callApiTokenUrl);
            String result = HttpUtil.sendGet(callApiTokenUrl);
            log.info("getTokenIdResult:"+result);
            LdapResponse ldapResponse = JSONObject.parseObject(result,LdapResponse.class);
            if (LdapResConstant.SUCCESS.equals(ldapResponse.getResCode())) {
                //计入缓存，过期时间按照返回值计算
                return ldapResponse.getTokenId();
            }
            return ldapResponse.getResMess();
        } catch (Exception e) {
            log.error("getTokenIdException:",e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Override
    public Result<Map<String,String>> checkLogin(String domain, String tokenId, String clientIp, String userName, String password) {
        Result<Map<String,String>> returnResult = new Result<Map<String,String>>();
        Map<String,String> userMap = new HashMap<>(2);
        String checkLoginUrl = ldapUrlConstant.checkLoginUrl;
        log.info("checkLoginUrl:" + checkLoginUrl);
        JSONObject param = new JSONObject();
        param.put("tokenId",tokenId);
        param.put("uid",userName);
        param.put("clientIp",clientIp);
        param.put("password", AesUtils.aesEncrypt(password));
        param.put("targetUri",ldapUrlConstant.targetUri);
        param.put("area",LdapResConstant.AREA);
        try {
            String encoderJson = URLEncoder.encode(JSONObject.toJSONString(param), AesUtils.UTF);
            String result = HttpUtil.sendPost(checkLoginUrl,encoderJson);
            log.info("checkLoginResult:"+result);
            LdapResponse ldapResponse = JSONObject.parseObject(result,LdapResponse.class);
            if (LdapResConstant.SUCCESS.equals(ldapResponse.getResCode())) {
                Map<String,String> queryResult = this.ldapQueryInfoParams(this.getLdapQueryToken(),userName);
                UserDto userDto = new UserDto();
                userDto.setDescription("for hisense");
                if(LdapResConstant.SUCCESS.equals(queryResult.get("resCode"))){
                    //本次异常获取不到，则记录后台日志，暂时允许登录
                    userDto.setName(userName);
                    if(StringUtils.isBlank(queryResult.get("mail"))){
                        userDto.setEmail(queryResult.get("uid")+"@hisense.com");
                    } else {
                        userDto.setEmail(queryResult.get("mail"));
                    }
                    if(StringUtils.isBlank(queryResult.get("mobile"))){
                        userDto.setPhone("13132133213");
                    } else {
                        userDto.setPhone(queryResult.get("mobile"));
                    }
                } else {
                    userDto.setName(userName);
                    userDto.setEmail("");
                    userDto.setPhone("13132133213");
                }
                userDto.setOrgName("hisense");
                userDto.setPwd("123456");
                userService.signUp( userDto);
                //拼接重定向地址
                String forwardBusinessSystemUrl = ldapUrlConstant.forwardBusinessSystemUrl;
                StringBuffer forwardUrl = new StringBuffer();
                forwardUrl.append(forwardBusinessSystemUrl).append("?uid=").append(ldapResponse.getUid())
                        .append("&ticketId=").append(ldapResponse.getTicketId());
                log.info("login:" + forwardUrl.toString());
                returnResult.setCode(Result.OK);
                returnResult.setMsg("登录成功");
                userMap.put("url",forwardUrl.toString());
                userMap.put("userName",userName);
                returnResult.setData(userMap);
                return returnResult;
            }
            returnResult.setCode(Result.OK);
            returnResult.setMsg(ldapResponse.getResMess());
            return returnResult;
        } catch (UnsupportedEncodingException e) {
            log.error("checkLogin exception:",e);
            throw new OperationFailed(e.getMessage());
        } catch (IOException e) {
            log.error("checkLogin exception:",e);
            throw new OperationFailed(e.getMessage());
        }
    }

    @Override
    public String getLdapQueryToken() {
        String ldapQueryTokenUrl = ldapUrlConstant.ldapQueryTokenUrl;
        try {
            log.info("ldapQueryTokenUrl:"+ldapQueryTokenUrl);
            String result = HttpUtil.sendGet(ldapQueryTokenUrl);
            log.info("getLdapQueryTokenUrlResult:"+result);
            LdapResponse ldapResponse = JSONObject.parseObject(result,LdapResponse.class);
            if (LdapResConstant.SUCCESS.equals(ldapResponse.getResCode())) {
                return ldapResponse.getTokenId();
            }
            return ldapResponse.getResMess();
        }catch (Exception e) {
            log.error("context",e);
            return ExceptionUtils.getStackTrace(e);
        }
    }

    @Override
    public Map<String,String> ldapQueryInfoParams(String tokenId,String uid) {
        Map<String,String> returnMap = new HashMap<>(8);
        // 加入如下代码 解决调用HTTPS 协议接口的 证书问题。
        Properties props = System.getProperties();
        props.setProperty("org.apache.cxf.stax.allowInsecureParser", "1"); props.setProperty("UseSunHttpHandler", "true");
        JaxWsProxyFactoryBean factory = new JaxWsProxyFactoryBean();
        // 自行根据wsdl文件或者URI生成客户端即可
        factory.setServiceClass(LdapQueryInfoNoImport.class);
        log.info("ldapQueryInfoParamsUrl:" + ldapUrlConstant.ldapQueryInfoParamsUrl);
        factory.setAddress(ldapUrlConstant.ldapQueryInfoParamsUrl);
        LdapQueryInfoNoImport service = (LdapQueryInfoNoImport) factory.create();
        // 新增查询条件,在管理员配置了筛选条件后，此项值填写才有意义，默认为空即可，填写示例：uid=xxxx,mail=xxxx
        String params="uid="+uid;
        String ldapQueryInfoParams = service.ldapQueryInfoParams(ldapUrlConstant.ldapAppSecretId, tokenId, params, "ldap");
        JSONObject ldapQueryInfoResult = JSONObject.parseObject(ldapQueryInfoParams);
        log.info("登录查询用户属性："+JSONObject.toJSONString(ldapQueryInfoResult));
        JSONObject resultJson = ldapQueryInfoResult.getJSONObject("result");
        returnMap.put("resCode",resultJson.getString("resCode"));
        returnMap.put("resMess",resultJson.getString("resMess"));
        if (LdapResConstant.SUCCESS.equals(resultJson.getString("resCode"))) {
            if(null == ldapQueryInfoResult.getJSONArray("data") || ldapQueryInfoResult.getJSONArray("data").size()==0){
                returnMap.put("mail",uid+"@hisense.com");
                returnMap.put("uid",uid);
                returnMap.put("mobile","13132133213");
            } else {
                JSONObject jsonObject = ldapQueryInfoResult.getJSONArray("data").getJSONObject(0);
                returnMap.put("uid",jsonObject.getString("uid"));
                returnMap.put("mail",jsonObject.getString("mail"));
                returnMap.put("name",jsonObject.getString("cn"));
                returnMap.put("mobile",jsonObject.getString("mobile"));
            }
        } else {
            log.error("查询用户信息异常，导致邮箱录入异常：resultJson: {}, uid:{}",ldapQueryInfoParams,uid);
        }
        return returnMap;
    }

    @Override
    public Map<String, String> checkSsoLoginToken(String tokenId, String ssoLoginToken) {
        Map<String,String> returnMap = new HashMap<>(3);
        String checkSsoLoginTokenUrl = ldapUrlConstant.checkSsoLoginTokenUrl;
        try {
            StringBuffer checkSb = new StringBuffer();
            checkSb.append(checkSsoLoginTokenUrl).append("?tokenId=").append(tokenId)
                    .append("&ssoLoginToken=").append(ssoLoginToken);
            String result = HttpUtil.sendGet(checkSb.toString());
            log.info("checkSsoLoginTokenResult:"+result);
            LdapResponse ldapResponse = JSONObject.parseObject(result,LdapResponse.class);
            returnMap.put("resCode",ldapResponse.getResCode());
            if (LdapResConstant.SUCCESS.equals(ldapResponse.getResCode())) {
                returnMap.put("uid",ldapResponse.getUid());
                returnMap.put("mail",ldapResponse.getEmail());
                returnMap.put("mobile",ldapResponse.getMobile());
                return returnMap;
            }
        }catch (Exception e) {
            log.error("context",e);
        }
        return returnMap;
    }

    @Override
    public String checkLoginTest(String tokenId, String realIP, String username, String password) {
        String checkLoginUrl = ldapUrlConstant.checkLoginUrl;
        log.info("checkLoginUrl:" + checkLoginUrl);
        JSONObject param = new JSONObject();
        param.put("tokenId",tokenId);
        param.put("uid",username);
        param.put("clientIp",realIP);
        param.put("password", AesUtils.aesEncrypt(password));
        param.put("targetUri","http://10.19.52.19:21105/api/v1/domains/gw-inter-dev/redirectTest");
        param.put("area",LdapResConstant.AREA);
        try {
            String encoderJson = URLEncoder.encode(JSONObject.toJSONString(param), AesUtils.UTF);
            String result = HttpUtil.sendPost(checkLoginUrl,encoderJson);
            log.info("checkLoginResult:"+result);
            LdapResponse ldapResponse = JSONObject.parseObject(result,LdapResponse.class);
            if (LdapResConstant.SUCCESS.equals(ldapResponse.getResCode())) {
                //拼接重定向地址
                String forwardBusinessSystemUrl = ldapUrlConstant.forwardBusinessSystemUrl;
                StringBuffer forwardUrl = new StringBuffer();
                forwardUrl.append(forwardBusinessSystemUrl).append("?uid=").append(ldapResponse.getUid())
                        .append("&ticketId=").append(ldapResponse.getTicketId()).append("&targetUri=")
                        .append(URLEncoder.encode(ldapResponse.getTargetUri(), "utf-8"));
                log.info("login:" + forwardUrl.toString());
                return forwardUrl.toString();
            }
            return "";
        } catch (UnsupportedEncodingException e) {
            log.error("context",e);
            throw new OperationFailed(e.getMessage());
        } catch (IOException e) {
            log.error("context",e);
            throw new OperationFailed(e.getMessage());
        }
    }

    @Override
    public Result<Map<String, String>> loginNew(String realIP, UserInfo userInfo) {
        Result<Map<String, String>> returnResult = new Result<Map<String, String>>();
        Map<String, String> userMap = new HashMap<>(8);
        String accessTokenStr = "";
        Long start1 = Calendar.getInstance().getTimeInMillis();
        try {
            accessTokenStr = HttpUtil.sendGet(userInfo.getUrl());
            Long end1 = Calendar.getInstance().getTimeInMillis()-start1;
            log.info("获取accessToken开始:"+start1+",时长："+end1);
            log.info("获取accessToken:"+accessTokenStr+",url:"+userInfo.getUrl());
        } catch (Exception e) {
            log.error("context",e);
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("oauth异常");
            returnResult.setData(null);
            return returnResult;
        }
        if(StringUtils.isBlank(accessTokenStr)){
            returnResult.setError("获取accessToken为空，登录异常");
            return returnResult;
        }
        Map<String, String> accessTokenMap = new HashMap<>();
        accessTokenMap = urlSplit(accessTokenStr);
        if (StringUtils.isNotBlank(accessTokenMap.get("error"))) {
            if("invalid_grant".equals(accessTokenMap.get("error"))){
                returnResult.setCode("3");
                returnResult.setMsg("code获取token异常,重新获取code");
                returnResult.setData(accessTokenMap);
                return returnResult;
            } else {
                returnResult.setCode("0");
                returnResult.setMsg("code获取token异常");
                returnResult.setData(accessTokenMap);
                return returnResult;
            }
        }
        String accessToken = accessTokenMap.get("access_token");
        userMap.put("accessToken",accessToken);
        userMap.put("expires",accessTokenMap.get("expires"));
        String uid = "";
        //获取userInfo
        String userInfoUri = ldapUrlConstant.userInfoUri;
        Long start2 = Calendar.getInstance().getTimeInMillis();
        try {
            StringBuffer checkSb = new StringBuffer();
            checkSb.append(userInfoUri).append("?access_token=").append(accessToken);
            String result = HttpUtil.sendGet(checkSb.toString());
            Long end2 = Calendar.getInstance().getTimeInMillis()-start2;
            log.info("获取userInfo开始:"+start2+",时长："+end2);
            log.info("checkAccessToken:" + result);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (StringUtils.isNotBlank(resultJson.getString("error"))) {
                returnResult.setCode("0");
                returnResult.setMsg(resultJson.getString("error"));
                returnResult.setData(null);
                return returnResult;
            }
            uid = resultJson.getString("id");
            userMap.put("uid", resultJson.getString("id"));
        }catch (Exception e) {
            log.error("context",e);
            returnResult.setCode("0");
            returnResult.setMsg("获取用户信息异常");
            returnResult.setData(null);
            return returnResult;
        }
        Long start3 = Calendar.getInstance().getTimeInMillis();
        UserDto userDto = new UserDto();
        userDto.setDescription("for hisense");
        //本次异常获取不到，则记录后台日志，暂时允许登录
        userDto.setName(uid);
        if (StringUtils.isBlank(userInfo.getEmail())) {
            userDto.setEmail(uid + "@hisense.com");
        } else {
            userDto.setEmail(userInfo.getEmail());
        }
        if (StringUtils.isBlank(userInfo.getMobile())) {
            userDto.setPhone("13132133213");
        } else {
            userDto.setPhone(userInfo.getMobile());
        }
        userDto.setOrgName("hisense");
        userDto.setPwd("123456");
        boolean result = userService.signUp( userDto);
        Long end3 = Calendar.getInstance().getTimeInMillis()-start3;
        log.info("3scale登录开始:"+start3+",时长："+end3);
        log.info("登录创建结果:"+result);
        returnResult.setCode("1");
        returnResult.setMsg("登录成功");
        userMap.put("userName", uid);
        // 创建token
        log.info("默认Token过期时间(s)："+audience.getExpiresSecond());
        audience.setExpiresSecond(2*60*60*1000);//设置token过期时间-2小时
        String token = JwtTokenUtil.createJWT(uid, audience);
        log.info("### 登录成功, token={} ###", token);
        // 将token响应给客户端
        userMap.put("token", token);
        returnResult.setData(userMap);
        Long end4 = Calendar.getInstance().getTimeInMillis()-start1;
        log.info("最终结果返回开始:"+start1+",时长："+end4);
        return returnResult;
    }

    @Override
    public Map<String, String> checkAccessToken(String accessToken) {
        Map<String,String> returnMap = new HashMap<>(8);
        returnMap.put("resCode","SUCCESS");
        String userInfoUri = ldapUrlConstant.userInfoUri;
        try {
            StringBuffer checkSb = new StringBuffer();
            checkSb.append(userInfoUri).append("?access_token=").append(accessToken);
            String result = HttpUtil.sendGet(checkSb.toString());
            log.info("checkAccessToken:"+result);
            JSONObject resultJson = JSONObject.parseObject(result);
            if (StringUtils.isNotBlank(resultJson.getString("error"))) {
                returnMap.put("error",resultJson.getString("error"));
                returnMap.put("resCode","FAILURE");
                return returnMap;
            }
            returnMap.put("uid",resultJson.getString("id"));
            //获取ldap信息
            Map<String,String> queryResult = this.ldapQueryInfoParams(this.getLdapQueryToken(),resultJson.getString("id"));
            if(LdapResConstant.SUCCESS.equals(queryResult.get("resCode"))){
                //本次异常获取不到，则记录后台日志，暂时允许登录
                if(StringUtils.isBlank(queryResult.get("mail"))){
                    returnMap.put("mail",queryResult.get("uid")+"@hisense.com");
                } else {
                    returnMap.put("mail",queryResult.get("mail"));
                }
                if(StringUtils.isBlank(queryResult.get("mobile"))){
                    returnMap.put("mobile","13132133213");
                } else {
                    returnMap.put("mobile",queryResult.get("mobile"));
                }
            } else {
                returnMap.put("mail","");
                returnMap.put("mobile","13132133213");
            }
            return returnMap;
        }catch (Exception e) {
            log.error("context",e);
        }
        return returnMap;
    }

    @Override
    public Result<String> getAccessToken(String url) {
        Result<String> returnResult = new Result<>();
        String result = "";
        try {
            result = HttpUtil.sendGet(url);
        } catch (Exception e) {
            log.error("context",e);
        }
        returnResult.setCode("1");
        returnResult.setMsg("获取成功");
        returnResult.setData(result);
        return returnResult;
    }

    public static Map<String, String> urlSplit(String URL){
        Map<String, String> mapRequest = new HashMap<String, String>();
        String[] arrSplit=null;
        arrSplit=URL.split("[&]");
        for(String strSplit:arrSplit){
            String[] arrSplitEqual=null;
            arrSplitEqual= strSplit.split("[=]");
            //解析出键值
            if(arrSplitEqual.length>1){
                //正确解析
                mapRequest.put(arrSplitEqual[0], arrSplitEqual[1]);
            }else{
                if(!"".equals(arrSplitEqual[0])){
                    //只有参数没有值，不加入
                    mapRequest.put(arrSplitEqual[0], "");
                }
            }
        }
        return mapRequest;
    }

}
