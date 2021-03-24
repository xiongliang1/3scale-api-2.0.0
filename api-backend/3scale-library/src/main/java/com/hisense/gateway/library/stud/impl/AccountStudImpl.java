/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019-11-24 @author jinshan
 */
package com.hisense.gateway.library.stud.impl;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hisense.api.library.utils.MiscUtil;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.stud.model.Error;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.utils.XmlUtils;
import com.hisense.gateway.library.stud.ServiceStud;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AccountStudImpl implements AccountStud {
    private static final String PATH_SIGNUP = "/admin/api/signup.json";
    private static final String PATH_FIND_ACCOUNT = "/admin/api/accounts/find.json";
    private static final String PATH_ACCOUNT = "/admin/api/accounts/";
    private static final String PATH_APPLICATION_ADD_XML = "/admin/api/accounts/%s/applications.xml?";

    private static final String PATH_FIND_ACCOUNT_BYID = "/admin/api/accounts/%s.json";
    
    private static final String PATH_FIND_USER_BYID = "/admin/api/accounts/%s/users.json";
    
    public static final ExecutorService executorService = Executors.newFixedThreadPool(5);

    @Autowired
    ServiceStud serviceStud;

    @Override
    public Result signUp(String host, Account account) {

//		Account o =  restProxy.postFormDataForObject(host + PATH_FIND_ACCOUNT+ "?"+"access_token="+
//				account.getAccessToken() + "&username="+account.getUsername(), 
//				account, Account.class);
        Result<AccountDto> rlt = new Result<AccountDto>();
        Map<String, String> map = new HashMap<String, String>();
        map.put("access_token", account.getAccessToken());
        String userName= MiscUtil.toHexString(account.getUsername());
        map.put("username", userName.length()>2?userName:userName+"_user");
        map.put("org_name", account.getOrgName());
        map.put("email", account.getEmail());
        map.put("password", account.getPassword());
        try {
            String strxml = HttpUtil.sendPost(host + PATH_SIGNUP, map);
            if (null == strxml) {
                rlt.setError("调用网关接口失败,url:"+PATH_SIGNUP);
                return rlt;
            } else {
                Error err =
                        JSON.parseObject(strxml, Error.class);

                if (null != err && null != err.getError()) {
                    rlt.setError(err.getError());
                    return rlt;
                } else {
                    AccountDto a =
                            JSON.parseObject(strxml, AccountDto.class);
                    rlt.setData(a);
                    return rlt;
                }
            }
        } catch (Exception e) {
            log.error("singup exception：",e);
        }
        return rlt;
    }

    @Override
    public AccountDto accountFind(String host, Account account) {
        log.info("******start to invoke accountFind, host is {}, account is {}",
                host, account);
        AccountDto o = null;
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_FIND_ACCOUNT + "?" + "access_token=" +
                            account.getAccessToken() + "&username=" + account.getUsername());
            log.info("******end to invoke accountFind, host is {}, "
                            + "account is {}, rlt is {}",
                    host, account, rlt);
            if (null == rlt) {
                return o;
            } else {
                o = JSON.parseObject(rlt, AccountDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke accountFind,host is {}, "
                            + "account is {}, e is {}",
                    host, account, e);
        }
        return o;
    }

    @Override
    public ApplicationDtos appDtoList(String host, String accessToken, String accountId) {
        log.info("******start to invoke appDtoList, "
                        + "host is {}, accountid is {}",
                host, accountId);
        ApplicationDtos dtos = new ApplicationDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_ACCOUNT + accountId + "/applications.json?access_token=" + accessToken);
            log.info("******end to invoke appDtoList, "
                            + "host is {}, accountid is {}, rlt is {}",
                    host, accountId, rlt);
            if (null == rlt) {
                return dtos;
            } else {
                dtos = JSON.parseObject(rlt, ApplicationDtos.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke appDtoList, "
                            + "host is {}, accountid is {}, e is {}",
                    host, accountId, e);
        }
        return dtos;
    }

    @Override
    public ApplicationXmlDtos appXmlDtoList(String host, String accessToken, String accountId) {
        log.info("******start to invoke appXmlDtoList, "
                        + "host is {}, accountid is {}",
                host, accountId);
        ApplicationXmlDtos dtos = new ApplicationXmlDtos();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_ACCOUNT + accountId + "/applications.xml?access_token=" + accessToken);
            log.info("******end to invoke appXmlDtoList, "
                            + "host is {}, accountid is {}, rlt is {}",
                    host, accountId, rlt);
            if (null == rlt) {
                return dtos;
            } else {
                dtos = (ApplicationXmlDtos) XmlUtils.xmlStrToObject(ApplicationXmlDtos.class, rlt);
            }
        } catch (Exception e) {
            log.error("******fail to invoke appXmlDtoList, "
                            + "host is {}, accountid is {}, e is {}",
                    host, accountId, e);
        }
        return dtos;
    }

    @Override
    public ApplicationDto appDto(String host, String accessToken, String accountId, String id) {
        log.info("******start to invoke appDto, "
                        + "host is {}, accountid is {}, id is {}",
                host, accountId, id);
        ApplicationDto dto = new ApplicationDto();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_ACCOUNT + accountId + "/applications/" + id + ".json?access_token=" + accessToken);
            log.info("******end to invoke appDto, "
                            + "host is {}, accountid is {}, id is {}, rlt is {}",
                    host, accountId, id, rlt);
            if (null == rlt) {
                return dto;
            } else {
                dto = JSON.parseObject(rlt, ApplicationDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke appDto, "
                            + "host is {}, accountid is {}, id is {}, e is {}",
                    host, accountId, id, e);
            ;
        }
        return dto;
    }

    @Override
    public ApplicationXml appXml(String host, String accessToken, String accountId, String id) {
        log.info("******start to invoke appXml,"
                        + "host is {}, accountid is {}, id is {}",
                host, accountId, id);
        ApplicationXml xml = new ApplicationXml();
        try {
            String rlt =
                    HttpUtil.sendGet(host + PATH_ACCOUNT + accountId + "/applications/" + id + ".xml?access_token=" + accessToken);
            log.info("******end to invoke appXml,"
                            + "host is {},accountid is {},id is {},rlt is {}",
                    host, accountId, id, rlt);
            if (StringUtils.isEmpty(rlt)) {
                return xml;
            } else {
                xml = (ApplicationXml) XmlUtils.xmlStrToObject(ApplicationXml.class, rlt);
            }
        } catch (Exception e) {
            log.error("******fail to invoke appXml,"
                            + "host is {},accountid is {},id is {}, e is {}",
                    host, accountId, id, e);
            ;
        }
        return xml;
    }

    @Override
    public String delAppDto(String host, String accessToken, String accountId, String id) {
        log.info("******start to invoke delAppDto, "
                        + "host is {}, accountid is {}, id is {}",
                host, accountId, id);
        String rlt = "";
        try {
            rlt = HttpUtil.sendDel(host + PATH_ACCOUNT + accountId + "/applications/" + id + ".json?access_token=" + accessToken);
            log.info("******end to invoke delAppDto, "
                            + "host is {}, accountid is {}, id is {}, rlt is {}",
                    host, accountId, id, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke delAppDto, "
                            + "host is {}, accountid is {}, id is {}, e is {}",
                    host, accountId, id, e);
        }
        return rlt;
    }
    @Override
    public Result<Application> readApplication(String host, String accountId, Application application) {
        log.info("******start to invoke readApplication, host is {}, accountId is {}, application is {}",
                host, accountId, application);
        String url = host + PATH_ACCOUNT + accountId + "/applications/" + application.getId()
                + ".xml?access_token="+application.getAccessToken();
        Result<ApplicationXml> result = parseResult("GET", url, ApplicationXml.class, new HashMap<>());
        return convertAppXmlToDto(result);
    }

    @Override
    public Result<Application> updateApplication(String host, String accountId, Application application) {
        log.info("******start to invoke updateApplication, host is {}, accountId is {}, application is {}",
                host, accountId, application);
        Map<String, String> map = new HashMap<>();
        map.put("access_token", application.getAccessToken());
        map.put("name", application.getName());
        map.put("description", application.getDescription());
        String url = host + PATH_ACCOUNT + accountId + "/applications/" + application.getId() + ".xml";
        Result<ApplicationXml> result = parseResult("PUT", url, ApplicationXml.class, map);
        return convertAppXmlToDto(result);
    }

    @Override
    public Result<Application> createApplication(String host, String accountId, Application application) {
        log.info("******start to invoke creatApplication, host is {}, accountId is {}, application is {}",
                host, accountId, application);
        Map<String, String> map = new HashMap<>();
        map.put("access_token", application.getAccessToken());
        map.put("name", application.getName());
        map.put("description", application.getDescription());
        map.put("plan_id", application.getPlan().getId());
        if(StringUtils.isNotBlank(application.getUserKey()))map.put("user_key", application.getUserKey());
        if(StringUtils.isNotBlank(application.getApplicationId())){
            map.put("application_id", application.getApplicationId());
            map.put("application_key", application.getKeys().getKey().get(0));
        }
        String url = host + PATH_ACCOUNT + accountId + "/applications.xml";
        Result<ApplicationXml> result = parseResult("POST", url, ApplicationXml.class, map);
        return convertAppXmlToDto(result);
    }

    @Override
    public ApplicationXml addAppAndReturnKey(String host, String accessToken, String accountId,
                                             String appPlanId, String appName, String appDesc) {

        log.info("******start to invoke addAppAndReturnKey, host is {}, accountId is {}",
                host, accountId);
        ApplicationXml o = null;
        Map<String, String> paramMap = new HashMap<>();
        paramMap.put("access_token", accessToken);
        paramMap.put("plan_id", appPlanId);
        paramMap.put("name", appName);
        paramMap.put("description", appDesc);
        try {
            String rlt =
                    HttpUtil.sendPost(host + String.format(PATH_APPLICATION_ADD_XML,
                            accountId), paramMap);
            log.info("******end to invoke addAppAndReturnKey, "
                            + "host is {}, accountId is {}, rlt is {}",
                    host, accountId, rlt);
            if (null == rlt) {
                return o;
            } else {
                o = (ApplicationXml)
                        XmlUtils.xmlStrToObject(ApplicationXml.class, rlt);
            }
        } catch (Exception e) {
            log.error("******fail to invoke addAppAndReturnKey, "
                            + "host is {}, accountId is {}, e is {}",
                    host, accountId, e);
        }
        return o;
    }

    public Result<Application> createNewKey(String host, String accessToken, String accountId, String applicationId,
                                            String key, ExecutorService executorService) {
        log.info("******start to invoke creatNewKey,"
                        + "host is {}, accountid is {}, id is {}, key is {}",
                host, accountId, applicationId, key);
        ApplicationXml applicationXml = appXml(host, accessToken, accountId, applicationId);
        if (applicationXml.getKeys().getKey() != null) {
            for (String oldKey : applicationXml.getKeys().getKey()) {
                Future<String> serviceFeature = executorService.submit(new Callable<String>() {
                    @Override
                    public String call() {
                        return deleteKey(host, accessToken, accountId, applicationId, oldKey);
                    }
                });
            }
        }
        String url = host + PATH_ACCOUNT + accountId + "/applications/" + applicationId + "/keys.xml";
        Map<String, String> map = new HashMap<>();
        map.put("access_token", accessToken);
        map.put("key", key);
        Result<ApplicationXml> result = parseResult("POST", url, ApplicationXml.class, map);
        return convertAppXmlToDto(result);
    }

    @Override
    public String deleteKey(String host, String accessToken, String accountId, String applicationId, String key) {
        log.info("******start to invoke deleteKey, "
                        + "host is {}, accountid is {}, id is {}, key is {}",
                host, accountId, applicationId, key);
        String rlt = "";
        try {
            rlt = HttpUtil.sendDel(host + PATH_ACCOUNT + accountId + "/applications/" + applicationId + "/keys/" + key + ".json?access_token=" + accessToken);
            log.info("******end to invoke deleteKey, "
                            + "host is {}, accountid is {}, id is {}, key is {}, rlt is {}",
                    host, accountId, applicationId, key, rlt);
        } catch (Exception e) {
            log.error("******fail to invoke deleteKey, "
                            + "host is {}, accountid is {}, id is {}, key is {}, e is {}",
                    host, accountId, applicationId, key, e);
        }
        return rlt;
    }

    public <T>  Result<T> parseResult(String method, String url, Class<T> t, Map map) {
        Map<String, Integer> map1 = new HashMap<>();
        Result<T> rlt = new Result<T>();
        try {
            String strxml = null;
            if ("POST".equals(method.toUpperCase())) {
                strxml = HttpUtil.sendPost(url, map);
            } else if ("PUT".equals(method.toUpperCase())) {
                strxml = HttpUtil.sendPut(url, map);
            }else if ("GET".equals(method.toUpperCase())) {
                strxml = HttpUtil.sendGet(url);
            }
            log.info("******end to invoke parseResult, strxml is {}", strxml);
            if (strxml == null || "".equals(strxml)) {
                rlt.setError("调用网关接口失败,url:"+url);
                return rlt;
            } else {
                if (url.contains(".json")) {
                    Error err =
                            JSON.parseObject(strxml, Error.class);
                    if (null != err && null != err.getError()) {
                        rlt.setError(err.getError());
                        return rlt;
                    } else {
                        T dto = JSON.parseObject(strxml, (Type) t);
                        rlt.setData(dto);
                        return rlt;
                    }
                } else {
                    if (strxml.contains("error")) {
                        rlt.setError(strxml);
                        return rlt;
                    }
                    T xml = (T) XmlUtils.xmlStrToObject(t, strxml);
                    rlt.setData(xml);
                    return rlt;
                }

            }
        } catch (Exception e) {
            log.error("******fail to invoke parseResult,"
                    + "e is {}", e);
        }
        return rlt;
    }

    public Result<Application> convertAppXmlToDto(Result<ApplicationXml> result) {
        Result<Application> res = new Result<>();
        if (result.getData() == null) {
            res.setError(result.getMsg());
            return res;
        }
        ApplicationXml applicationXml = result.getData();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        try {
            String jsonStr = mapper.writeValueAsString(applicationXml);
            Application app = mapper.readValue(jsonStr, Application.class);
            res.setData(app);
            res.setCode(Result.OK);
        } catch (JsonProcessingException e) {
            res.setError(e.getMessage());
        } catch (IOException e) {
            res.setError(e.getMessage());
        }
        return res;
    }
    
    
    @Override
    public AccountSynDto accountFindById(String host, Account account){
        log.info("******start to invoke accountFindById, host is {}, account is {}",
                host, account);
        AccountSynDto o = null;
        try {
            String rlt =
                    HttpUtil.sendGet(host + String.format(PATH_FIND_ACCOUNT_BYID,
                    		account.getId()) + "?" + "access_token=" +
                            account.getAccessToken() );
            log.info("******end to invoke accountFindById, host is {}, "
                            + "account is {}, rlt is {}",
                    host, account, rlt);
            if (null == rlt) {
                return o;
            } else {
                o = JSON.parseObject(rlt, AccountSynDto.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke accountFindById,host is {}, "
                            + "account is {}, e is {}",
                    host, account, e);
        }
        return o;
    }
    
    
    @Override
    public AccountSyn accountUserFindById(String host, Account account){
        log.info("******start to invoke accountUserFindById, host is {}, account is {}",
                host, account);
        AccountSyn o = null;
        try {
            String rlt =
                    HttpUtil.sendGet(host + String.format(PATH_FIND_USER_BYID,
                    		account.getId()) + "?" + "access_token=" +
                            account.getAccessToken() );
            log.info("******end to invoke accountUserFindById, host is {}, "
                            + "account is {}, rlt is {}",
                    host, account, rlt);
            if (null == rlt) {
                return o;
            } else {
                o = JSON.parseObject(rlt, AccountSyn.class);
            }
        } catch (Exception e) {
            log.error("******fail to invoke accountUserFindById,host is {}, "
                            + "account is {}, e is {}",
                    host, account, e);
        }
        return o;
    }

    @Override
    public ApplicationXml appXml2(String host, String accessToken, String accountId, String id) {
        ApplicationXml xml = new ApplicationXml();
        try {
            String rlt = HttpUtil.sendGet2(host + PATH_ACCOUNT + accountId + "/applications/" + id + ".xml?access_token=" + accessToken);
            if (StringUtils.isEmpty(rlt)) {
                return xml;
            } else {
                xml = (ApplicationXml) XmlUtils.xmlStrToObject(ApplicationXml.class, rlt);
            }
        } catch (Exception e) {
            log.error("******fail to invoke appXml,host is {},accountid is {},id is {}, e is {}", host, accountId, id, e);
        }
        return xml;
    }
}
