/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.management.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.LdapResConstant;
import com.hisense.gateway.library.constant.ParameterContentType;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.dto.web.DocRquestInfo;
import com.hisense.gateway.library.model.pojo.base.Domain;
import com.hisense.gateway.library.repository.DomainRepository;
import com.hisense.gateway.management.service.SwaggerDocService;
import com.hisense.gateway.library.stud.AccountStud;
import com.hisense.gateway.library.stud.ApplicationStud;
import com.hisense.gateway.library.stud.ServiceStud;
import com.hisense.gateway.library.stud.SwaggerDocStud;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.web.form.ApplicationForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SwaggerDocServiceImpl implements SwaggerDocService {
    @Resource
    DomainRepository domainRepository;

    @Resource
    SwaggerDocStud swaggerDocStud;

    @Resource
    AccountStud accountStud;

    @Resource
    ApplicationStud applicationStud;

    @Resource
    ServiceStud serviceStud;

    @Override
    public List<ApiDocs> findAllSwaggerDoc(String domainName) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (domain == null) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        List<ApiDocs> result = swaggerDocStud.findAllSwaggerDoc(domain.getHost(), domain.getAccessToken());

        log.info("结果：" + result);
        return result;
    }

    @Override
    public List<ApiDocs> findSwaggerDocByServiceId(String domainName, String serviceId) {
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (domain == null) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        List<ApiDocs> result = swaggerDocStud.findSwaggerDocByServiceId(domain.getHost(), domain.getAccessToken(),
                serviceId);

        log.info("结果：" + result);
        return result;
    }

    @Override
    public Map<String, String> getDefaultKey(String domainName, String serviceId) {
        Map<String, String> resultMap = new HashMap<>(4);
        resultMap.put("mark", "0");
        ApplicationXml applicationXml = new ApplicationXml();

        // 拿默认用户
        String defaultUserName = LdapResConstant.DEFAULT_USER_NAME;
        Domain domain = domainRepository.searchDomainByName(domainName);
        if (domain == null) {
            log.error("can not found domain,domain is {}", domainName);
            throw new OperationFailed("domain not exist");
        }

        // 若默认accountid不存在则创建
        Long accountId = getAccountId(defaultUserName, domain);
        if (accountId == 0L) {
            return resultMap;
        }

        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setUserAccountId(String.valueOf(accountId));
        applicationForm.setAccessToken(domain.getAccessToken());
        ApplicationXmlDtos applicationXmlDtos = applicationStud.findApplicationListByAccount(domain.getHost(),
                applicationForm);

        List<ApplicationXml> applicationXmls = null;
        if (null != applicationXmlDtos && applicationXmlDtos.getApplication().size() > 0) {
            applicationXmls = applicationXmlDtos.getApplication().stream()
                    .filter(item -> serviceId.equals(item.getServiceId())).collect(Collectors.toList());
        }

        if (null != applicationXmls && applicationXmls.size() > 0) {
            // 已存在
            applicationXml = applicationXmls.get(0);
        } else {
            // 不存在需要创建
            List<AppPlanDto> appPlanDtos = serviceStud.appPlanDtoList(domain.getHost(), domain.getAccessToken(),
                    serviceId);

            // 服务的plan不需要创建，空则返回异常
            if (null == appPlanDtos || appPlanDtos.size() == 0) {
                return resultMap;
            }

            String appPlanId = appPlanDtos.get(0).getApplication_plan().getId();
            String appName = "hisense" + serviceId + String.valueOf(System.currentTimeMillis());
            //创建
            applicationXml = accountStud.addAppAndReturnKey(domain.getHost(), domain.getAccessToken(),
                    String.valueOf(accountId), appPlanId, appName, appName);
        }

        if (StringUtils.isBlank(applicationXml.getApplicationId())) {
            // userKey模式
            resultMap.put("mark", "1");
            resultMap.put("backendVersion", "1");
            resultMap.put("userKey", applicationXml.getUserKey());
        } else {
            // appId-key 模式
            resultMap.put("mark", "1");
            resultMap.put("backendVersion", "2");
            resultMap.put("appId", applicationXml.getApplicationId());
            resultMap.put("appKey", applicationXml.getKeys().getKey().get(0));
        }
        return resultMap;
    }

    @Override
    public Map<String, Object> docRequest(String domain, String serviceId, DocRquestInfo docRequestInfo) {
        Map<String, Object> resultMap = new HashMap<>(4);
        Map<String, String> result = this.getDefaultKey(domain, serviceId);
        if ("0".equals(result.get("mark"))) {//获取秘钥失败
            resultMap.put("state", false);
            log.error("can not found key,domain is {},serviceId is {},docRequestInfo is {}",
                    domain, serviceId, JSONObject.toJSONString(docRequestInfo));
            return resultMap;
        }

        // 发起请求
        String path = "";
        if ("1".equals(result.get("backendVersion"))) {
            if (docRequestInfo.getPath().contains("?")) {
                path = docRequestInfo.getPath() + "&user_key=" + result.get("userKey");
            } else {
                path = docRequestInfo.getPath() + "?user_key=" + result.get("userKey");
            }
        } else if ("2".equals(result.get("backendVersion"))) {
            if (docRequestInfo.getPath().contains("?")) {
                path = docRequestInfo.getPath() + "&app_id=" + result.get("appId") + "&app_key=" + result.get("appKey");
            } else {
                path = docRequestInfo.getPath() + "?app_id=" + result.get("appId") + "&app_key=" + result.get("appKey");
            }
        }

        try {
            if ("Get".equals(docRequestInfo.getType())) {
                resultMap = HttpUtil.sendGetAndGetCode(path, null);
            } else if ("Post".equals(docRequestInfo.getType())) {
                Map<String, Object> params = docRequestInfo.getParameters();
                if (ParameterContentType.JSON.equals(docRequestInfo.getParameterContentType())) {
                    String paramJson = JSONObject.toJSONString(params);
                    resultMap = HttpUtil.sendPostJsonAndGetCode(path, paramJson, null);
                } else if (ParameterContentType.FORM.contains(docRequestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPostFormAndGetCode(path, params, null);
                }
            } else if ("Put".equals(docRequestInfo.getType())) {
                Map<String, Object> params = docRequestInfo.getParameters();
                if (ParameterContentType.JSON.equals(docRequestInfo.getParameterContentType())) {
                    String paramJson = JSONObject.toJSONString(params);
                    resultMap = HttpUtil.sendPutJsonAndGetCode(path, paramJson, null);
                } else if (ParameterContentType.FORM.contains(docRequestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPutFormAndGetCode(path, params, null);
                }
            } else if ("Delete".equals(docRequestInfo.getType())) {
                resultMap = HttpUtil.sendDelAndGetCode(path, null);
            }
            resultMap.put("state", true);
        } catch (Exception e) {
            log.error("do request exception:",e);
        }
        return resultMap;
    }

    private Long getAccountId(String defaultUserName, Domain domain) {
        Account account = new Account();
        account.setAccessToken(domain.getAccessToken());
        account.setUsername(defaultUserName);
        AccountDto accountFind =
                accountStud.accountFind(domain.getHost(), account);
        if (null == accountFind) {
            return 0l;
        }
        return accountFind.getAccount().getId();
    }
}
