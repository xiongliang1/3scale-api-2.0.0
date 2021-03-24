/*
 * Licensed Materials - Property of tenxcloud.com
 * (C) Copyright 2019 TenxCloud. All Rights Reserved.
 *
 * 2019/11/25 @author peiyun
 */
package com.hisense.gateway.management.web.controller;

import com.hisense.gateway.library.model.Result;
import com.hisense.gateway.library.model.dto.web.DocRquestInfo;
import com.hisense.gateway.management.service.ApplicationService;
import com.hisense.gateway.management.service.SwaggerDocService;
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

import static com.hisense.gateway.library.constant.BaseConstants.URL_SWAGGER_DOC;

@Slf4j
@RequestMapping(URL_SWAGGER_DOC)
@RestController
public class SwaggerDocController {
    @Resource
    SwaggerDocService swaggerDocService;

    @Resource
    ApplicationService applicationService;

    @RequestMapping(value = "/services/{serviceId}/docs", method = RequestMethod.GET)
    public Result<List<ApiDocs>> findSwaggerDocByServiceId(@PathVariable String domain, @PathVariable String serviceId,
                                                           HttpServletRequest request) {
        List<ApiDocs> result = swaggerDocService.findSwaggerDocByServiceId(domain, serviceId);
        log.info("docs:{}", result);
        Result<List<ApiDocs>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "findAllDocs", method = RequestMethod.GET)
    public Result<List<ApiDocs>> findAllDoc(@PathVariable String domain, HttpServletRequest request) {
        List<ApiDocs> result = swaggerDocService.findAllSwaggerDoc(domain);
        log.info("findAllDocs result:{}", result);
        Result<List<ApiDocs>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "getApplicationListByAccount", method = RequestMethod.GET)
    public Result<ApplicationXmlDtos> getApplicationListByAccount(@PathVariable String domain,
                                                           HttpServletRequest request) {
        ApplicationForm applicationForm = new ApplicationForm();
        applicationForm.setUserAccountId("115");
        ApplicationXmlDtos result = applicationService.findApplicationListByAccount(domain, applicationForm);
        log.info("getApplicationListByAccount result:{}", result);
        Result<ApplicationXmlDtos> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(result);
        return returnResult;
    }

    @RequestMapping(value = "/services/{serviceId}/getDefaultKey", method = RequestMethod.GET)
    public Result<Map<String, String>> getDefaultKey(@PathVariable String domain, @PathVariable String serviceId,
                                                     HttpServletRequest request) {
        Result<Map<String, String>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("查询成功");
        returnResult.setData(null);
        return returnResult;
    }

    @RequestMapping(value = "/services/{serviceId}/docRequest", method = RequestMethod.POST)
    public Result<Map<String, Object>> docRequest(@RequestBody DocRquestInfo docRequestInfo,
                                           HttpServletRequest request) {
        Map pathVariables = (Map) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);
        String domain = String.valueOf(pathVariables.get("domain"));
        String serviceId = String.valueOf(pathVariables.get("serviceId"));
        Map<String, Object> result = swaggerDocService.docRequest(domain, serviceId, docRequestInfo);
        log.info("docRequestInfo result:{}", result);

        Result<Map<String, Object>> returnResult = new Result<>();
        returnResult.setCode(Result.OK);
        returnResult.setMsg("请求成功");
        returnResult.setData(result);
        return returnResult;
    }
}
