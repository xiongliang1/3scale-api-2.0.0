/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2020/3/11 @author peiyun
 */
package com.hisense.gateway.library.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.constant.GatewayConstants;
import com.hisense.gateway.library.constant.ParameterContentType;
import com.hisense.gateway.library.exception.BadRequest;
import com.hisense.gateway.library.exception.NotExist;
import com.hisense.gateway.library.model.ModelConstant;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.RequestInfo;
import com.hisense.gateway.library.model.pojo.base.*;
import com.hisense.gateway.library.repository.*;
import com.hisense.gateway.library.service.ApiInvokeRecordService;
import com.hisense.gateway.library.service.DebuggingService;
import com.hisense.gateway.library.service.MappingRuleService;
import com.hisense.gateway.library.stud.ApplicationStud;
import com.hisense.gateway.library.stud.ProxyPoliciesStud;
import com.hisense.gateway.library.stud.model.ApplicationXml;
import com.hisense.gateway.library.stud.model.Proxy;
import com.hisense.gateway.library.stud.model.ProxyDto;
import com.hisense.gateway.library.utils.HttpUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Predicate;
import java.util.*;

import static com.hisense.gateway.library.constant.BaseConstants.TAG;
import static com.hisense.gateway.library.model.Result.FAIL;

@Slf4j
@Service
public class DebuggingServiceImpl implements DebuggingService {
    @Resource
    InstanceRepository instanceRepository;

    @Resource
    ProxyPoliciesStud proxyPoliciesStud;

    @Resource
    PublishApiInstanceRelationshipRepository pairRepository;

    @Resource
    PublishApplicationRepository publishApplicationRepository;

    @Resource
    PublishApiRepository publishApiRepository;

    @Resource
    MappingRuleService mappingRuleService;

    @Resource
    ApplicationStud applicationStud;

    @Resource
    UserInstanceRelationshipRepository userInstanceRelationshipRepository;

    @Resource
    ApiInvokeRecordService apiInvokeRecordService;

    @Override
    public Map<String, Object> debuggingRequest( Integer publishApiId,RequestInfo requestInfo) {
        Map<String, Object> resultMap = new HashMap<>(4);

        String path = requestInfo.getPath();
        log.info("{}path={}", TAG, path);
        //get或者delete请求需要把请求参数拼接到url上
        if (("GET".equals(requestInfo.getType()) || "DELETE".equals(requestInfo.getType())) && !CollectionUtils.isEmpty(requestInfo.getParameterMap())) {
           Map<String,Object> params =  requestInfo.getParameterMap();
           StringBuilder paramsBuf = new StringBuilder();
           for(String key:params.keySet()){
               paramsBuf.append(key).
                       append("=").
                       append(String.valueOf(params.get(key))).
                       append("&");
           }
           String paramStr = paramsBuf.toString();
           paramStr = paramStr.substring(0,paramStr.length()-1);
           if (path.contains("?")) {
               path = path + "&" + paramStr;
           } else {
               path = path + "?" + paramStr;
           }
        }
        //url加上鉴权的userKey
//        if (ModelConstant.API_AUTH.equals(requestInfo.getAuthType())) {
        if (StringUtils.isBlank(requestInfo.getUserKey())) {
            //历史数据迁移，没有userKey的情况，使用appId和appKey鉴权
            if(StringUtils.isBlank(requestInfo.getAppId()) || StringUtils.isBlank(requestInfo.getAppKey())){
                log.error("当前appId与appKey为空");
            }
            if (path.contains("?")) {
                path = path + "&app_id=" + requestInfo.getAppId() + "&app_key=" + requestInfo.getAppKey();
            } else {
                path = path + "?app_id=" + requestInfo.getAppId() + "&app_key=" + requestInfo.getAppKey();
            }
        }else{
            if (path.contains("?")) {
                path = path + "&user_key=" + requestInfo.getUserKey();
            } else {
                path = path + "?user_key=" + requestInfo.getUserKey();
            }
        }
        log.info("{}path={}", TAG, path);
//        }

        //发起请求
        try {
            if ("GET".equals(requestInfo.getType())) {
                resultMap = HttpUtil.sendGetAndGetCode(path, requestInfo.getHeader());
            } else if ("POST".equals(requestInfo.getType())) {
                if (ParameterContentType.JSON.equalsIgnoreCase(requestInfo.getParameterContentType())) {
                    String paramJson = requestInfo.getParameterStr();
                    if(paramJson==null){
                        throw new BadRequest("参数不能为空");
                    }
                    log.info("{}paramJson={},header={}", TAG, paramJson,requestInfo.getHeader());
                    resultMap = HttpUtil.sendPostJsonAndGetCode(path, paramJson, requestInfo.getHeader());
                } else if (ParameterContentType.FORM.contains(requestInfo.getParameterContentType())) {
                    log.info("{}parameterMap={}", TAG, requestInfo.getParameterMap());
                    resultMap = HttpUtil.sendPostFormAndGetCode(path, requestInfo.getParameterMap(),
                            requestInfo.getHeader());
                } else if (ParameterContentType.XML.equalsIgnoreCase(requestInfo.getParameterContentType())) {
                    String paramXml = requestInfo.getParameterStr();
                    if(paramXml==null){
                        throw new BadRequest("参数不能为空");
                    }
                    resultMap = HttpUtil.sendPostXmlAndGetCode(path, paramXml, requestInfo.getHeader());
                }
            } else if ("PUT".equals(requestInfo.getType())) {
                if (ParameterContentType.JSON.equalsIgnoreCase(requestInfo.getParameterContentType())) {
                    String paramJson = requestInfo.getParameterStr();
                    if(paramJson==null){
                        throw new BadRequest("参数不能为空");
                    }
                    resultMap = HttpUtil.sendPutJsonAndGetCode(path, paramJson, requestInfo.getHeader());
                } else if (ParameterContentType.FORM.contains(requestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPutFormAndGetCode(path, requestInfo.getParameterMap(),
                            requestInfo.getHeader());
                } else if (ParameterContentType.XML.equalsIgnoreCase(requestInfo.getParameterContentType())) {
                    String paramXml = requestInfo.getParameterStr();
                    if(paramXml==null){
                        throw new BadRequest("参数不能为空");
                    }
                    resultMap = HttpUtil.sendPutXmlAndGetCode(path, paramXml, requestInfo.getHeader());
                }
            } else if ("PATCH".equals(requestInfo.getType())) {
                if (ParameterContentType.JSON.equalsIgnoreCase(requestInfo.getParameterContentType())) {
                    String paramJson = requestInfo.getParameterStr();
                    if(paramJson==null){
                        throw new BadRequest("参数不能为空");
                    }
                    resultMap = HttpUtil.sendPatchJsonAndGetCode(path, paramJson, requestInfo.getHeader());
                } else if (ParameterContentType.FORM.contains(requestInfo.getParameterContentType())) {
                    resultMap = HttpUtil.sendPatchFormAndGetCode(path, requestInfo.getParameterMap(),
                            requestInfo.getHeader());
                } else if (ParameterContentType.XML.equalsIgnoreCase(requestInfo.getParameterContentType())) {
                    String paramXml = requestInfo.getParameterStr();
                    if(paramXml==null){
                        throw new BadRequest("参数不能为空");
                    }
                    resultMap = HttpUtil.sendPatchXmlAndGetCode(path, paramXml, requestInfo.getHeader());
                }
            } else if ("DELETE".equals(requestInfo.getType())) {
                resultMap = HttpUtil.sendDelAndGetCode(path, requestInfo.getHeader());
            }
            resultMap.put("state", true);
        } catch (Exception e) {
            log.error("调试发送请求异常：",e);
            resultMap.put("state", false);
            return resultMap;
        }
        return resultMap;
    }

    @Override
    public Result<Map<String, String>> getAppIdAndKeyParam(String partition, String environment, Integer publishApiId) {
        Result<Map<String, String>> returnResult = new Result<>();
        Map<String, String> result = new HashMap<>(2);

        log.info("{}partition={},publishApiId={},", TAG,  partition,
                publishApiId);

        // 通过租户+发布环境,查询scale实例
        Instance instances = instanceRepository.findAllByTenantIdAndPartition(environment, partition);
        if (null == instances) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("失败，未查询到instance信息");
            returnResult.setData(null);
            return returnResult;
        }

        log.info("{}instances={}", TAG, instances);

        // 查询service信息
        PublishApiInstanceRelationship pair = pairRepository.getByAPIidAndInstanceId(publishApiId, instances.getId());
        if (null != pair) {
            ProxyDto proxyDto = proxyPoliciesStud.readProxy(instances.getHost(), instances.getAccessToken(),
                    String.valueOf(pair.getScaleApiId()));
            Proxy proxy = proxyDto.getProxy();
            log.info("{} proxy={} ", TAG, proxy);
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

    @Override
    public Result<Map<String,String>> getApiByUserKey(String partition, String environment, Integer publishApiId,Integer systemId) {

        Result<Map<String,String>> returnResult = new Result();
        Map<String,String> result = new HashMap<>();
        log.info("{}partition={},publishApiId={},", TAG,partition,
                publishApiId);
        // 通过租户+发布环境,查询scale实例
        Instance instances = instanceRepository.findAllByTenantIdAndPartition(environment, partition);
        if (null == instances) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("失败，未查询到instance信息");
            returnResult.setData(null);
            return returnResult;
        }
        log.info("{}instances={}", TAG, instances);

        String userKey=null;
        Map<String,String> keyMap =  new HashMap<>();
        List<PublishApplication> application = publishApplicationRepository.findByPublishApiIdAndSystemId(instances.getId(),systemId,publishApiId);
        log.info("applications is:"+ application.toString());
        if(!CollectionUtils.isEmpty(application)){
            for(PublishApplication app:application){
                if(StringUtils.isNotBlank(app.getUserKey())) {
                    userKey = app.getUserKey();
                    break;
                }else {
                    //获取appid和appKey
                    if(StringUtils.isBlank(keyMap.get("appId")) || StringUtils.isBlank(keyMap.get("appKey"))){
                        try{
                            Instance ins = instanceRepository.findOne(app.getInstance().getId());
                            if(null == ins){
                                log.error("Instance is null,", JSONObject.toJSONString(app));
                            }else {
                                UserInstanceRelationship uir =
                                        userInstanceRelationshipRepository.findByUserAndInstanceId(app.getCreator(), ins.getId());
                                if(uir.getId()==null){
                                    returnResult.setError(FAIL,"未找到对应的accountId");
                                    return returnResult;
                                }
                                log.info("{}uir={}",TAG,uir);
                                ApplicationXml applicationXml = applicationStud.applicationRead(ins.getHost(), ins.getAccessToken(),
                                        String.valueOf(uir.getAccountId()), app.getScaleApplicationId());
                                if(null != applicationXml && null != applicationXml.getApplicationId() &&  null != applicationXml.getKeys() &&
                                        !CollectionUtils.isEmpty(applicationXml.getKeys().getKey()) ){

                                    log.info("keyMap result is:" + keyMap.toString());
                                    keyMap.put("appId", applicationXml.getApplicationId());
                                    keyMap.put("appKey", applicationXml.getKeys().getKey().get(0));
                                    apiInvokeRecordService.updateApiIdAndAppKey(app.getId(),applicationXml.getApplicationId(),applicationXml.getKeys().getKey().get(0));
                                }else{
                                    log.info(String.format("appId or appKey is null,applicationXml:%s",applicationXml));
                                }
                            }
                        }catch (Exception e){
                            log.info("get appId and appKey Exception,application",JSONObject.toJSONString(app));
                            log.error("get appId and appKey Exception:",e);
                        }
                    }
                }
            }
            if(userKey != null){
                result.put("userKey",userKey);
            }else {
                result.put("authAppId", keyMap.get("appId"));
                result.put("authAppKey", keyMap.get("appKey"));
            }
        }else {
            returnResult.setError(Result.FAIL,"未查询到成功订阅记录,application记录为空！");
            return returnResult;
        }

        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);

        return returnResult;
    }

    @Override
    public Result<Map<String,String>> getApiByUserKey(String partition, String environment, Integer publishApiId) {

        Result<Map<String,String>> returnResult = new Result(Result.OK,"查询正常",null);
        Map<String,String> result = new HashMap<>();
        log.info("{}partition={},publishApiId={},", TAG,partition,
                publishApiId);
        // 通过租户+发布环境,查询scale实例
        Instance instances = instanceRepository.findAllByTenantIdAndPartition(environment, partition);
        if (null == instances) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("失败，未查询到instance信息");
            returnResult.setData(null);
            return returnResult;
        }
        log.info("{}instances={}", TAG, instances);

        PublishApplication application = publishApplicationRepository.findByPublishApiId(publishApiId, instances.getId());
        log.info("{}application={}",TAG,application);
        if(null == application){
            returnResult.setError(Result.FAIL,"API发布异常，application 不存在！");
            return returnResult;
        }
        if(application.getUserKey() != null){
            result.put("userKey",application.getUserKey());
        }else {
            UserInstanceRelationship uir =
                    userInstanceRelationshipRepository.findByUserAndInstanceId(application.getCreator(), instances.getId());
            if(uir==null){
                returnResult.setError(FAIL,"未找到对应的accountId");
                return returnResult;
            }
            log.info("{}uir={}",TAG,uir);
            ApplicationXml applicationXml = applicationStud.applicationRead(instances.getHost(), instances.getAccessToken(),
                    String.valueOf(uir.getAccountId()), application.getScaleApplicationId());
            if(null != applicationXml && null != applicationXml.getApplicationId() &&  null != applicationXml.getKeys() &&
                    !CollectionUtils.isEmpty(applicationXml.getKeys().getKey()) ){

                result.put("authAppId", applicationXml.getApplicationId());
                result.put("authAppKey", applicationXml.getKeys().getKey().get(0));
            }else{
                log.info(String.format("appId or appKey is null,applicationXml:%s",applicationXml));
            }

        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);

        return returnResult;
    }

    @Override
    public Result getPath(String partition, String environment, Integer publishApiId) {
        Result returnResult = new Result();
        List<Map<String,StringBuffer>> list = new ArrayList<>();
        log.info("{},partition={},publishApiId={},", TAG,  partition,publishApiId);
        // 通过租户+发布环境,查询scale实例
        Instance instances = instanceRepository.findAllByTenantIdAndPartition(environment, partition);
        if (null == instances) {
            returnResult.setCode("0");
            returnResult.setMsg("失败，未查询到instance信息");
            returnResult.setData(null);
            return returnResult;
        }
        log.info("{}instances={}", TAG, instances);
        String sandbox = instances.getSandbox();
        String requestSandbox = instances.getRequestSandbox();
        String production = instances.getProduction();
        String requestProduction = instances.getRequestProduction();
        log.info("{}sandbox={},requestSandbox={}",TAG, sandbox,requestSandbox);
        log.info("{}production={},requestProduction={}",TAG, production,requestProduction);

        PublishApi publishApi = publishApiRepository.findOne(publishApiId);
        List<ApiMappingRule> mappingRules = mappingRuleService.findRuleByApiId(publishApiId);
        if(mappingRules.isEmpty()){
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg("mappingRule不存在");
            returnResult.setData(null);
            return returnResult;
        }
        String mappingRule=null;
        String httpMethod = null;
        //api未发布 Path =  sandbox + api的url(即为前缀) + mappingRule.pattern
        //api已发布 Path =  Production + api的url(即为前缀) + mappingRule.pattern
        for(int i=0;i<mappingRules.size();i++){
            StringBuffer buffer = new StringBuffer();
            Map<String,StringBuffer> map = new HashMap<>();
            httpMethod = mappingRules.get(i).getHttpMethod();
            mappingRule = mappingRules.get(i).getPattern();
            if(publishApi.getIsOnline()==0){
                buffer.append(requestSandbox);
                buffer.append(mappingRule);
            }else {
                buffer.append(requestProduction);
                buffer.append(mappingRule);
            }
            map.put(httpMethod,buffer);
            list.add(map);
            log.info("{}httpMethod={},buffer={}",TAG,httpMethod,buffer);
        }
        log.info("{}list={}",TAG, list);
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(list);
        return returnResult;
    }
}
