/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.developer.web.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.hisense.gateway.library.model.dto.portal.DocRquestInfo;
import com.hisense.gateway.developer.service.ApplicationService;
import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.developer.service.SwaggerDocService;
import com.hisense.gateway.library.stud.model.ApiDocs;
import com.hisense.gateway.library.stud.model.ApplicationXmlDtos;

import com.hisense.gateway.library.web.form.ApplicationForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.HandlerMapping;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/domains/{domain}/docs")
public class SwaggerDocController {

    @Resource
    SwaggerDocService swaggerDocService;

    @Resource
    ApplicationService applicationService;

    @RequestMapping(value = "/services/{serviceId}/docs", method = RequestMethod.GET)
    public Result<List<ApiDocs>> findSwaggerDocByServiceId(@PathVariable String domain,@PathVariable String serviceId,
                                                           HttpServletRequest request) {
        List<ApiDocs> result = swaggerDocService.findSwaggerDocByServiceId(domain,serviceId);
        log.info("docs:"+result);
        Result<List<ApiDocs>> returnResult = new Result<List<ApiDocs>>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "findAllDocs", method = RequestMethod.GET)
    public Result<List<ApiDocs>> findAllDoc(@PathVariable String domain,HttpServletRequest request) {
        List<ApiDocs> result = swaggerDocService.findAllSwaggerDoc(domain);
        log.info("findAllDocResult:"+result);
        Result<List<ApiDocs>> returnResult = new Result<List<ApiDocs>>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "getApplicationListByAccount", method = RequestMethod.GET)
    public Result<ApplicationXmlDtos> getApplicationListByAccount(@PathVariable String domain,HttpServletRequest request) {
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setUserAccountId("115");
        ApplicationXmlDtos result = applicationService.findApplicationListByAccount(domain,applicationForm);
        log.info("getApplicationListByAccountResult:"+result);
        Result<ApplicationXmlDtos> returnResult = new Result<ApplicationXmlDtos>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "/services/{serviceId}/getDefaultKey", method = RequestMethod.GET)
    public Result<Map<String,String>> getDefaultKey(@PathVariable String domain, @PathVariable String serviceId,
                                                    HttpServletRequest request) {
        //Map<String,String> result = swaggerDocService.getDefaultKey(domain,serviceId);
        //log.info("getDefaultKeyResult:"+result);
        Result<Map<String,String>> returnResult = new Result<Map<String,String>>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(null);
        return returnResult;
    }

    @RequestMapping(value = "/services/{serviceId}/docRequest", method = RequestMethod.POST)
    public Result<Object> docRequest(@RequestBody DocRquestInfo docRequestInfo, HttpServletRequest request) {
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String domain = String.valueOf(pathVariables.get("domain"));
        String serviceId = String.valueOf(pathVariables.get("serviceId"));
        Map<String,Object> result = swaggerDocService.docRequest(domain,serviceId,docRequestInfo);
        log.info("docRequestResult:"+result);
//        returnResult.setCode("1");
//        returnResult.setMsg("请求成功");
//        returnResult.setData(result);
//        return returnResult;
        String contentType = String.valueOf(result.get("contentType"));
        result.remove("contentType");
        Result<Object> returnResult = new Result<Object>();
        if (!Boolean.valueOf(String.valueOf(result.get("state")))) {
            returnResult.setCode(Result.FAIL);
            returnResult.setMsg(String.valueOf(result.get("msg")));
            returnResult.setData(null);
            return returnResult;
        }
        returnResult.setData(result.get("data"));
        if(contentType.contains("application/json")){
            if(String.valueOf(result.get("data")).startsWith("[")){
                JSONArray dataJsonArray = JSONArray.parseArray(String.valueOf(result.get("data")));
                returnResult.setData(dataJsonArray);
            } else {
                JSONObject dataJsonObject = JSONObject.parseObject(String.valueOf(result.get("data")));
                returnResult.setData(dataJsonObject);
            }
        }
        returnResult.setCode(Result.OK);
        returnResult.setMsg("请求成功");
        return returnResult;
    }

    @RequestMapping(value = "/services/{serviceId}/getAppIdAndkeyParam", method = RequestMethod.GET)
    public Result<Map<String,String>> getAppIdAndkeyParam(@PathVariable String domain,
                                                          @PathVariable Integer serviceId,
                                                          HttpServletRequest request) {
        return swaggerDocService.getAppIdAndkeyParam(domain, serviceId);
    }

}
