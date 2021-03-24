/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.developer.service.impl;

import com.hisense.gateway.library.constant.ParameterContentType;
import com.hisense.gateway.library.exception.OperationFailed;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.portal.DocRquestInfo;
import com.hisense.gateway.library.model.pojo.base.Instance;
import com.hisense.gateway.library.repository.DomainRepository;
import com.hisense.gateway.library.repository.InstanceRepository;
import com.hisense.gateway.library.stud.*;
import com.hisense.gateway.library.stud.model.*;
import com.hisense.gateway.library.utils.HttpUtil;
import com.hisense.gateway.library.constant.LdapResConstant;
import com.hisense.gateway.library.model.pojo.base.Domain;
import com.hisense.gateway.library.model.pojo.base.PublishApiInstanceRelationship;
import com.hisense.gateway.library.repository.PublishApiInstanceRelationshipRepository;
import com.hisense.gateway.developer.service.SwaggerDocService;

import com.hisense.gateway.library.web.form.ApplicationForm;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    InstanceRepository instanceRepository;

    @Autowired
    PublishApiInstanceRelationshipRepository publishApiInstanceRelationshipRepository;

    @Autowired
    ProxyPoliciesStud proxyPoliciesStud;

    @Override
    public List<ApiDocs> findAllSwaggerDoc(String domain) {
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        List<ApiDocs> result = swaggerDocStud.findAllSwaggerDoc(instance.getHost(),
                instance.getAccessToken());
        log.info("结果："+result);
        return result;
    }

    @Override
    public List<ApiDocs> findSwaggerDocByServiceId(String domain, String serviceId) {
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        //查询3scaleId
        PublishApiInstanceRelationship pair = publishApiInstanceRelationshipRepository
                .getByAPIidAndInstanceId(Integer.valueOf(serviceId),instance.getId());
        List<ApiDocs> result = swaggerDocStud.findSwaggerDocByServiceId(instance.getHost(),
                instance.getAccessToken(), String.valueOf(pair.getScaleApiId()));
        log.info("结果："+result);
        return result;
    }

    @Override
    public Map<String,String> getDefaultKey(String domainName, String serviceId) {
        Map<String,String> resultMap = new HashMap<>(4);
        resultMap.put("mark","0");
        ApplicationXml applicationXml = new ApplicationXml();
        //拿默认用户
        String defaultUserName = LdapResConstant.DEFAULT_USER_NAME;
        Domain domain =
                domainRepository.searchDomainByName(domainName);
        if (null == domain) {
            log.error("can not found domain,domain is {}",domainName);
            throw new OperationFailed("domain not exist");
        }
        //过去默认accountId,如果不存在则创建
        Long accountId = getAccountId(defaultUserName,domain);
        if (0L == accountId) {
            return resultMap;
        }
        //查询applications
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setUserAccountId(String.valueOf(accountId));
        applicationForm.setAccessToken(domain.getAccessToken());
        ApplicationXmlDtos applicationXmlDtos = null;
        applicationXmlDtos = applicationStud.findApplicationListByAccount(domain.getHost(),
                applicationForm);
        List<ApplicationXml> applicationXmls = null;
        if(null != applicationXmlDtos && applicationXmlDtos.getApplication().size() > 0) {
            applicationXmls = applicationXmlDtos.getApplication().stream()
                    .filter(item->serviceId.equals(item.getServiceId())).collect(Collectors.toList());
        }
        if (null != applicationXmls && applicationXmls.size() > 0) {
            //已存在
            applicationXml = applicationXmls.get(0);
        } else {
            //不存在需要创建
            List<AppPlanDto> appPlanDtos = serviceStud.appPlanDtoList(domain.getHost(),domain.getAccessToken(),serviceId);
            //服务的plan不需要创建，空则返回异常
            if (null == appPlanDtos || appPlanDtos.size() == 0) {
                return resultMap;
            }
            String appPlanId = appPlanDtos.get(0).getApplication_plan().getId();
            String appName = "hisense"+serviceId+String.valueOf(System.currentTimeMillis());
            //创建
            applicationXml = accountStud.addAppAndReturnKey(domain.getHost(),domain.getAccessToken()
                    ,String.valueOf(accountId),appPlanId,appName,appName);
        }
        if (StringUtils.isBlank(applicationXml.getApplicationId())) {
            //userKey模式
            resultMap.put("mark","1");
            resultMap.put("backendVersion", "1");
            resultMap.put("userKey", applicationXml.getUserKey());
        } else {
            //appId-key模式
            resultMap.put("mark","1");
            resultMap.put("backendVersion", "2");
            resultMap.put("appId", applicationXml.getApplicationId());
            resultMap.put("appKey", applicationXml.getKeys().getKey().get(0));
        }
        return resultMap;
    }

    @Override
    public Map<String,Object> docRequest(String domain, String serviceId, DocRquestInfo docRequestInfo) {
        Map<String,Object> resultMap = new HashMap<>(4);
        //发起请求
        String path = docRequestInfo.getPath();
        if (1 == docRequestInfo.getBackendVersion()){
            if (docRequestInfo.getPath().contains("?")) {
                path = path + "&user_key=" + docRequestInfo.getUserKey();
            } else {
                path = path + "?user_key=" + docRequestInfo.getUserKey();
            }
        } else if (2 == docRequestInfo.getBackendVersion()) {
            if(docRequestInfo.getPath().contains("?")){
                path = path+"&app_id="+docRequestInfo.getAppId()+"&app_key="+docRequestInfo.getAppKey();
            } else {
                path = path+"?app_id="+docRequestInfo.getAppId()+"&app_key="+docRequestInfo.getAppKey();
            }
        }
        try {
            if ("GET".equals(docRequestInfo.getType())) {
                resultMap = HttpUtil.sendGetAndGetCode(path,docRequestInfo.getHeader());
            } else if ("POST".equals(docRequestInfo.getType())) {
                if(ParameterContentType.JSON.equals(docRequestInfo.getParameterContentType())){
                    resultMap = HttpUtil.sendPostJsonAndGetCode(path, docRequestInfo.getParameterStr(), docRequestInfo.getHeader());
                } else if (ParameterContentType.FORM.contains(docRequestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPostFormAndGetCode(path, docRequestInfo.getParameterMap(), docRequestInfo.getHeader());
                } else if (ParameterContentType.XML.equals(docRequestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPostXmlAndGetCode(path, docRequestInfo.getParameterStr(),docRequestInfo.getHeader());
                }
            } else if ("PUT".equals(docRequestInfo.getType())) {
                if(ParameterContentType.JSON.equals(docRequestInfo.getParameterContentType())){
                    resultMap = HttpUtil.sendPutJsonAndGetCode(path, docRequestInfo.getParameterStr(), docRequestInfo.getHeader());
                } else if (ParameterContentType.FORM.contains(docRequestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPutFormAndGetCode(path, docRequestInfo.getParameterMap(), docRequestInfo.getHeader());
                } else if (ParameterContentType.XML.equals(docRequestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPutXmlAndGetCode(path, docRequestInfo.getParameterStr(), docRequestInfo.getHeader());
                }
            } else if ("PATCH".equals(docRequestInfo.getType())) {
                if (ParameterContentType.JSON.equals(docRequestInfo.getParameterContentType())) {
                    String paramJson = docRequestInfo.getParameterStr();
                    resultMap = HttpUtil.sendPatchJsonAndGetCode(path, paramJson, docRequestInfo.getHeader());
                } else if (ParameterContentType.FORM.contains(docRequestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPatchFormAndGetCode(path, docRequestInfo.getParameterMap(), docRequestInfo.getHeader());
                } else if (ParameterContentType.XML.equals(docRequestInfo.getParameterContentType())) {
                    String paramXml = docRequestInfo.getParameterStr();
                    resultMap = HttpUtil.sendPatchXmlAndGetCode(path, paramXml, docRequestInfo.getHeader());
                }
            } else if ("DELETE".equals(docRequestInfo.getType())) {
                resultMap = HttpUtil.sendDelAndGetCode(path,docRequestInfo.getHeader());
            }
            resultMap.put("state",true);
        } catch (Exception e) {
            log.error("context",e);
        }
        return resultMap;
    }

    @Override
    public Result<Map<String, String>> getAppIdAndkeyParam(String domain, Integer serviceId) {
        Result<Map<String, String>> returnResult = new Result<>();
        Map<String,String> result = new HashMap<>(2);
        Instance instance = instanceRepository.searchInstanceByName(domain);
        if (null == instance) {
            log.error("can not found domain,domain is {}", domain);
            throw new OperationFailed("domain not exist");
        }
        //查询service信息
        PublishApiInstanceRelationship pair = publishApiInstanceRelationshipRepository.getByAPIidAndInstanceId(serviceId , instance.getId());
        if(null != pair){
            ProxyDto proxyDto = proxyPoliciesStud.readProxy(instance.getHost(),instance.getAccessToken(),String.valueOf(pair.getScaleApiId()));
            Proxy proxy = proxyDto.getProxy();
            if (null != proxy) {
                result.put("authAppId", proxy.getAuthAppId());
                result.put("authAppKey", proxy.getAuthAppKey());
            }
        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    private Long getAccountId(String defaultUserName,  Domain domain) {
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
